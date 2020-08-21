package com.snap2buy.themobilebackend.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.snap2buy.themobilebackend.model.SkuTypePOJO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.snap2buy.themobilebackend.dao.AltriaDashboardDao;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.dao.StoreMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.ImageAnalysis;
import com.snap2buy.themobilebackend.model.ImageResultCode;
import com.snap2buy.themobilebackend.model.ImageStore;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.service.AltriaDashboardService;
import com.snap2buy.themobilebackend.service.ProcessImageService;

/**
 * Created by Anoop on 09/10/18.
 */
@Component(value = BeanMapper.BEAN_ALTRIA_DASHBOARD_SERVICE)
@Scope("prototype")
public class AltriaDashboardServiceImpl implements AltriaDashboardService {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
    @Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
    private StoreMasterDao storeMasterDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_ALTRIA_DASHBOARD_DAO)
    private AltriaDashboardDao altriaDashboardDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
    private ProcessImageService processImageService;
    
    //Round to 1 decimal point
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#");
    
	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectSummary(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectSummary----------------\n");
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();
		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		String userHigherGeoName = (String) storesForUser.get("userHigherGeoName");
		List<String> userHigherGeoLevelStores = (List<String>) storesForUser.get("userHigherGeoLevelStores");

		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		LinkedHashMap<String, String> summaryForRequestedMonthUserGeo = altriaDashboardDao.getAltriaProjectSummary(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectSummary::summary = {}", summaryForRequestedMonthUserGeo);
		LinkedHashMap<String,Object> countryLevel = new LinkedHashMap<String,Object>();
		countryLevel.put("level", userGeoName);
		countryLevel.put("data", summaryForRequestedMonthUserGeo);
		result.add(countryLevel);
				
		LinkedHashMap<String, String> summaryForRequestedMonthHigherGeo = new LinkedHashMap<String, String>();
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		summaryForRequestedMonthHigherGeo = altriaDashboardDao.getAltriaProjectSummary(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectSummary::summary territory= {}", summaryForRequestedMonthHigherGeo );
		
		LinkedHashMap<String,Object> accountLevel = new LinkedHashMap<String,Object>();
		accountLevel.put("level", userHigherGeoName);
		accountLevel.put("data", summaryForRequestedMonthHigherGeo);
		result.add(accountLevel);
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectSummary----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectBrandShares(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectBrandShares----------------\n");
		Map<String,List<LinkedHashMap<String,Object>>> groupedData = new HashMap<String,List<LinkedHashMap<String,Object>>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		String userHigherGeoName = (String) storesForUser.get("userHigherGeoName");
		List<String> userHigherGeoLevelStores = (List<String>) storesForUser.get("userHigherGeoLevelStores");
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandSharesForRequestedMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandShares::visitMonthUserGeo = {}", brandSharesForRequestedMonthUserGeo);

				
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandSharesForPreviousMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandShares::previousMonthUserGeo = {}", brandSharesForPreviousMonthUserGeo);

		//calculate percentage change w.r.t previous month - country level
		for ( String brand : brandSharesForRequestedMonthUserGeo.keySet()) {
			LinkedHashMap<String, Object> brandShares = brandSharesForRequestedMonthUserGeo.get(brand);
			LinkedHashMap<String, Object> brandSharesPrevMonth = brandSharesForPreviousMonthUserGeo.get(brand);
			String brandShareValue = (String)brandShares.get("brandShare");
			String brandSharePrevMonthValue = "0";
			if (brandSharesPrevMonth != null ) {
				brandSharePrevMonthValue = (String)brandSharesPrevMonth.get("brandShare");
			}
			float changeInShare = Float.parseFloat(brandShareValue) - Float.parseFloat(brandSharePrevMonthValue);
			brandShares.put("level", userGeoName);
			brandShares.put("changeInShare", DECIMAL_FORMATTER.format(changeInShare));
			List<LinkedHashMap<String,Object>> list = new ArrayList<LinkedHashMap<String,Object>>();
			list.add(brandShares);
			groupedData.put(brand, list);
		}		
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandSharesForRequestedMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandShares::visitMonthHigherGeo = {}", brandSharesForRequestedMonthHigherGeo );

		
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandSharesForPreviousMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandShares::previousMonthHigherGeo = {}", brandSharesForPreviousMonthHigherGeo );
		
		for ( String brand : brandSharesForRequestedMonthHigherGeo.keySet()) {
			LinkedHashMap<String, Object> brandShares = brandSharesForRequestedMonthHigherGeo.get(brand);
			LinkedHashMap<String, Object> brandSharesPrevMonth = brandSharesForPreviousMonthHigherGeo.get(brand);
			String brandShareValue = (String)brandShares.get("brandShare");
			String brandSharePrevMonthValue = "0";
			if (brandSharesPrevMonth != null ) {
				brandSharePrevMonthValue = (String)brandSharesPrevMonth.get("brandShare");
			}
			float changeInShare = Float.parseFloat(brandShareValue) - Float.parseFloat(brandSharePrevMonthValue);
			brandShares.put("level", userHigherGeoName);
			brandShares.put("changeInShare", DECIMAL_FORMATTER.format(changeInShare));
			groupedData.get(brand).add(brandShares);
		}
		
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();
		
		for ( String brandName : groupedData.keySet() ) {
			LinkedHashMap<String,Object> brandData = new LinkedHashMap<String,Object>();
			brandData.put("brand" , brandName);
			brandData.put("data", groupedData.get(brandName));
			result.add(brandData);
		}
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectBrandShares----------------\n");
		
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectBrandAvailability(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectBrandAvailability----------------\n");
		Map<String,List<LinkedHashMap<String,Object>>> groupedData = new LinkedHashMap<String,List<LinkedHashMap<String,Object>>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		String userHigherGeoName = (String) storesForUser.get("userHigherGeoName");
		List<String> userHigherGeoLevelStores = (List<String>) storesForUser.get("userHigherGeoLevelStores");
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("skuTypeIds", inputObject.getSkuTypeIds());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForRequestedMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailability::visitMonthUserGeo = {}", brandAvailabilityForRequestedMonthUserGeo);

				
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForPreviousMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailability::previousMonthUserGeo = {}", brandAvailabilityForPreviousMonthUserGeo);

		//calculate percentage change w.r.t previous month - country level
		for ( String brand : brandAvailabilityForRequestedMonthUserGeo.keySet()) {
			LinkedHashMap<String, Object> brandAvailability = brandAvailabilityForRequestedMonthUserGeo.get(brand);
			LinkedHashMap<String, Object> brandAvailabilityPrevMonth = brandAvailabilityForPreviousMonthUserGeo.get(brand);
			String percentageStoresWithBrand = (String)brandAvailability.get("percentageStoresWithBrand");
			String percentageStoresWithBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				percentageStoresWithBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("percentageStoresWithBrand");
			}
			float percentageChange = Float.parseFloat(percentageStoresWithBrand) - Float.parseFloat(percentageStoresWithBrandPrevMonth);
			
			String avgProductsForBrand = (String)brandAvailability.get("avgProductsForBrand");
			String avgProductsForBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				avgProductsForBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("avgProductsForBrand");
			}
			float changeInAvgProducts = Float.parseFloat(avgProductsForBrand) - Float.parseFloat(avgProductsForBrandPrevMonth);
			
			String percentageStoresWithAllProductsForBrand = (String)brandAvailability.get("percentageStoresWithAllProductsForBrand");
			String percentageStoresWithAllProductsForBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				percentageStoresWithAllProductsForBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("percentageStoresWithAllProductsForBrand");
			}
			float changeInStoresWithAllProducts = Float.parseFloat(percentageStoresWithAllProductsForBrand) - Float.parseFloat(percentageStoresWithAllProductsForBrandPrevMonth);
			
			brandAvailability.put("level", userGeoName);
			brandAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
			brandAvailability.put("changeInAvgProducts", DECIMAL_FORMATTER.format(changeInAvgProducts));
			brandAvailability.put("percentageChangeInStoresWithAllProducts", DECIMAL_FORMATTER.format(changeInStoresWithAllProducts));
			List<LinkedHashMap<String,Object>> list = new ArrayList<LinkedHashMap<String,Object>>();
			list.add(brandAvailability);
			groupedData.put(brand, list);
		}
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForRequestedMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailability::visitMonthHigherGeo = {}", brandAvailabilityForRequestedMonthHigherGeo);

		
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForPreviousMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailability::previousMonthHigherGeo = {}", brandAvailabilityForPreviousMonthHigherGeo);
		
		//calculate percentage change w.r.t previous month - territory level
		for ( String brand : brandAvailabilityForRequestedMonthHigherGeo.keySet()) {
			LinkedHashMap<String, Object> brandAvailability = brandAvailabilityForRequestedMonthHigherGeo.get(brand);
			LinkedHashMap<String, Object> brandAvailabilityPrevMonth = brandAvailabilityForPreviousMonthHigherGeo.get(brand);
			String percentageStoresWithBrand = (String)brandAvailability.get("percentageStoresWithBrand");
			String percentageStoresWithBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				percentageStoresWithBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("percentageStoresWithBrand");
			}
			float percentageChange = Float.parseFloat(percentageStoresWithBrand) - Float.parseFloat(percentageStoresWithBrandPrevMonth);
			
			String avgProductsForBrand = (String)brandAvailability.get("avgProductsForBrand");
			String avgProductsForBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				avgProductsForBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("avgProductsForBrand");
			}
			float changeInAvgProducts = Float.parseFloat(avgProductsForBrand) - Float.parseFloat(avgProductsForBrandPrevMonth);
			
			String percentageStoresWithAllProductsForBrand = (String)brandAvailability.get("percentageStoresWithAllProductsForBrand");
			String percentageStoresWithAllProductsForBrandPrevMonth = "0";
			if (brandAvailabilityPrevMonth != null ) {
				percentageStoresWithAllProductsForBrandPrevMonth = (String)brandAvailabilityPrevMonth.get("percentageStoresWithAllProductsForBrand");
			}
			float changeInStoresWithAllProducts = Float.parseFloat(percentageStoresWithAllProductsForBrand) - Float.parseFloat(percentageStoresWithAllProductsForBrandPrevMonth);
			
			brandAvailability.put("level", userHigherGeoName);
			brandAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
			brandAvailability.put("changeInAvgProducts", DECIMAL_FORMATTER.format(changeInAvgProducts));
			brandAvailability.put("percentageChangeInStoresWithAllProducts", DECIMAL_FORMATTER.format(changeInStoresWithAllProducts));
			groupedData.get(brand).add(brandAvailability);
		}
		
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();
		
		for ( String brandName : groupedData.keySet() ) {
			LinkedHashMap<String,Object> brandData = new LinkedHashMap<String,Object>();
			brandData.put("brand" , brandName);
			brandData.put("data", groupedData.get(brandName));
			result.add(brandData);
		}

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectBrandAvailability----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectWarningSignAvailability(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectWarningSignAvailability----------------\n");
		Map<String,List<LinkedHashMap<String,Object>>> groupedData = new HashMap<String,List<LinkedHashMap<String,Object>>>();
		Map<String,String> warningSignUPCNameMap = new HashMap<String,String>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		String userHigherGeoName = (String) storesForUser.get("userHigherGeoName");
		List<String> userHigherGeoLevelStores = (List<String>) storesForUser.get("userHigherGeoLevelStores");
		
		//warning sign is aggregated only at parent project level, so the incoming projectId can be treated as parentProjectId.
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("customerCode", inputObject.getCustomerCode());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> signagesForRequestedMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailability::visitMonthUserGeo = {}", signagesForRequestedMonthUserGeo );

		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> signagesForPreviousMonthUserGeo = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailability::previousMonthUserGeo = {}", signagesForPreviousMonthUserGeo );

		//calculate percentage change w.r.t previous month - country level
		for ( String warningSignUpc : signagesForRequestedMonthUserGeo.keySet()) {
			LinkedHashMap<String, Object> warningSignAvailability = signagesForRequestedMonthUserGeo.get(warningSignUpc);
			LinkedHashMap<String, Object> warningSignAvailabilityPrevMonth = signagesForPreviousMonthUserGeo.get(warningSignUpc);
			String percentageStoresWithWarningSign = (String)warningSignAvailability.get("percentageStoresWithWarningSign");
			String percentageStoresWithWarningSignPrevMonth = "0";
			if (warningSignAvailabilityPrevMonth != null ) {
				percentageStoresWithWarningSignPrevMonth = (String)warningSignAvailabilityPrevMonth.get("percentageStoresWithWarningSign");
			}
			float percentageChange = Float.parseFloat(percentageStoresWithWarningSign) - Float.parseFloat(percentageStoresWithWarningSignPrevMonth);
			warningSignAvailability.put("level", userGeoName);
			warningSignAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
			List<LinkedHashMap<String,Object>> list = new ArrayList<LinkedHashMap<String,Object>>();
			list.add(warningSignAvailability);
			groupedData.put(warningSignUpc, list);
			warningSignUPCNameMap.put(warningSignUpc, (String)signagesForRequestedMonthUserGeo.get(warningSignUpc).get("warningSignName"));
		}	
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> signagesForRequestedMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailability::visitMonthHigherGeo = {}", signagesForRequestedMonthHigherGeo );

		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> signagesForPreviousMonthHigherGeo = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailability::previousMonthHigherGeo = {}",signagesForPreviousMonthHigherGeo);
		
		//calculate percentage change w.r.t previous month - country level
		for ( String warningSignUpc : signagesForRequestedMonthHigherGeo.keySet()) {
			LinkedHashMap<String, Object> warningSignAvailability = signagesForRequestedMonthHigherGeo.get(warningSignUpc);
			LinkedHashMap<String, Object> warningSignAvailabilityPrevMonth = signagesForPreviousMonthHigherGeo.get(warningSignUpc);
			String percentageStoresWithWarningSign = (String)warningSignAvailability.get("percentageStoresWithWarningSign");
			String percentageStoresWithWarningSignPrevMonth = "0";
			if (warningSignAvailabilityPrevMonth != null ) {
				percentageStoresWithWarningSignPrevMonth = (String)warningSignAvailabilityPrevMonth.get("percentageStoresWithWarningSign");
			}
			float percentageChange = Float.parseFloat(percentageStoresWithWarningSign) - Float.parseFloat(percentageStoresWithWarningSignPrevMonth);
			warningSignAvailability.put("level", userHigherGeoName);
			warningSignAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
			groupedData.get(warningSignUpc).add(warningSignAvailability);
		}
		
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();
		
		for ( String warningSignUpc : groupedData.keySet() ) {
			LinkedHashMap<String,Object> warningSigndData = new LinkedHashMap<String,Object>();
			warningSigndData.put("warningSignUpc" , warningSignUpc);
			warningSigndData.put("warningSignName", warningSignUPCNameMap.get(warningSignUpc));
			warningSigndData.put("data", groupedData.get(warningSignUpc));
			result.add(warningSigndData);
		}
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectWarningSignAvailability----------------\n");
		return result;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectBrandAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectBrandAvailabilityByStore----------------\n");
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		List<String> storeList = new ArrayList<String>();
		storeList.add(inputObject.getStoreId());
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
 		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("skuTypeIds", inputObject.getSkuTypeIds());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForRequestedMonth = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailabilityByStore::visitMonth = {}", brandAvailabilityForRequestedMonth );

				
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> brandAvailabilityForPreviousMonth = 
				altriaDashboardDao.getAltriaProjectBrandAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandAvailabilityByStore::previousMonth = {}", brandAvailabilityForPreviousMonth);

		for ( String brand : brandAvailabilityForRequestedMonth.keySet()) {
			LinkedHashMap<String, Object> brandAvailabilityInStore = new LinkedHashMap<String,Object>();
			brandAvailabilityInStore.put("brand", brand);
			String storesWithBrand = (String) brandAvailabilityForRequestedMonth.get(brand).get("storesWithBrand");
			String present="0";
			if ( StringUtils.isNotBlank(storesWithBrand) && Integer.parseInt(storesWithBrand) > 0 ) {
				present="1";
			}
			String storesWithBrandPrevMonth = (String) brandAvailabilityForPreviousMonth.get(brand).get("storesWithBrand");
			String presentInPreviousVisit="0";
			if ( StringUtils.isNotBlank(storesWithBrandPrevMonth) && Integer.parseInt(storesWithBrandPrevMonth) > 0 ) {
				presentInPreviousVisit="1";
			}
			brandAvailabilityInStore.put("present", present);
			brandAvailabilityInStore.put("presentInPreviousVisit", presentInPreviousVisit);
			result.add(brandAvailabilityInStore);
		}

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectBrandAvailabilityByStore----------------\n");
		return result;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectBrandSharesByStore(InputObject inputObject) {

		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectBrandSharesByStore----------------\n");
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
		
		List<String> storeList = new ArrayList<String>();
		storeList.add(inputObject.getStoreId());
		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> brandSharesForRequestedMonth = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandSharesByStore::visitMonth = {}", brandSharesForRequestedMonth );

				
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> brandSharesForPreviousMonth = 
				altriaDashboardDao.getAltriaProjectBrandShares(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectBrandSharesByStore::previousMonth = {}", brandSharesForPreviousMonth);

		for ( String brand : brandSharesForRequestedMonth.keySet()) {
			LinkedHashMap<String, Object> brandShares = brandSharesForRequestedMonth.get(brand);
			LinkedHashMap<String, Object> brandSharesPrevMonth = brandSharesForPreviousMonth.get(brand);
			String brandShareValue = (String)brandShares.get("brandShare");
			String brandSharePrevMonthValue = "0";
			if (brandSharesPrevMonth != null ) {
				brandSharePrevMonthValue = (String)brandSharesPrevMonth.get("brandShare");
			}
			float changeInShare = Float.parseFloat(brandShareValue) - Float.parseFloat(brandSharePrevMonthValue);
			LinkedHashMap<String,Object> brandShareInStore = new LinkedHashMap<String,Object>();
			brandShareInStore.put("brand",brand);
			brandShareInStore.put("brandShare", brandShareValue);
			brandShareInStore.put("changeInShare", DECIMAL_FORMATTER.format(changeInShare));
			result.add(brandShareInStore);
		}		
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectBrandSharesByStore----------------\n");
		
		return result;
	
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectWarningSignAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectWarningSignAvailabilityByStore----------------\n");
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		List<String> storeList = new ArrayList<String>();
		storeList.add(inputObject.getStoreId());
		
		//warning sign is aggregated only at parent project level, so the incoming projectId can be treated as parentProjectId.
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());	
		queryArgs.put("customerCode", inputObject.getCustomerCode());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> signagesForRequestedMonth = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailabilityByStore::visitMonth = {}", signagesForRequestedMonth );

		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> signagesForPreviousMonth = 
				altriaDashboardDao.getAltriaProjectWarningSignAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectWarningSignAvailabilityByStore::previousMonth = {}", signagesForPreviousMonth);

		for ( String warningSign : signagesForRequestedMonth.keySet()) {
			LinkedHashMap<String, Object> warningSignAvailabilityInStore = new LinkedHashMap<String,Object>();
			warningSignAvailabilityInStore.put("warningSignUpc", warningSign);
			warningSignAvailabilityInStore.put("warningSignName", signagesForRequestedMonth.get(warningSign).get("warningSignName"));
			
			String storesWithWarningSign = (String) signagesForRequestedMonth.get(warningSign).get("storesWithWarningSign");
			String present = "0";
			if(StringUtils.isNotBlank(storesWithWarningSign) && Integer.parseInt(storesWithWarningSign) > 0 ) {
				present = "1";
			}
			String storesWithWarningSignPrevMonth = (String) signagesForPreviousMonth.get(warningSign).get("storesWithWarningSign");
			String presentInPrevMonth = "0";
			if(StringUtils.isNotBlank(storesWithWarningSignPrevMonth) && Integer.parseInt(storesWithWarningSignPrevMonth) > 0 ) {
				presentInPrevMonth = "1";
			}
			warningSignAvailabilityInStore.put("present", present);
			warningSignAvailabilityInStore.put("presentInPreviousVisit", presentInPrevMonth);
			result.add(warningSignAvailabilityInStore);
		}
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectWarningSignAvailabilityByStore----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectProductAvailability(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectProductAvailability----------------\n");
		Map<String,List<Map<String,List<LinkedHashMap<String,Object>>>>> groupedData = new HashMap<String,List<Map<String,List<LinkedHashMap<String,Object>>>>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		String userHigherGeoName = (String) storesForUser.get("userHigherGeoName");
		List<String> userHigherGeoLevelStores = (List<String>) storesForUser.get("userHigherGeoLevelStores");
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("skuTypeIds", inputObject.getSkuTypeIds());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> productAvailabilityForRequestedMonthByBrandUserGeo = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailability::visitMonthUserGeo = {}", productAvailabilityForRequestedMonthByBrandUserGeo);

				
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> productAvailabilityForPreviousMonthByBrandUserGeo = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailability::previousMonthUserGeo = {}", productAvailabilityForPreviousMonthByBrandUserGeo);

		//calculate percentage change w.r.t previous month - country level
		for ( String brand : productAvailabilityForRequestedMonthByBrandUserGeo.keySet()) {
			LinkedHashMap<String, Object> brandAvailability = productAvailabilityForRequestedMonthByBrandUserGeo.get(brand);
			LinkedHashMap<String, Object> brandAvailabilityPrevMonth = productAvailabilityForPreviousMonthByBrandUserGeo.get(brand);
			Map<String,List<LinkedHashMap<String,Object>>> groupedDataProductLevel = new HashMap<String,List<LinkedHashMap<String,Object>>>();
			for ( String product : brandAvailability.keySet() ) {
				LinkedHashMap<String, Object> productAvailability = (LinkedHashMap<String, Object>) brandAvailability.get(product);
				String percentageStoresWithProduct = (String)productAvailability.get("percentageStoresWithProduct");
				String percentageStoresWithProductPrevMonth = "0";
				if ( brandAvailabilityPrevMonth != null ) {
					Object productAvailabilityPrevMonth =  brandAvailabilityPrevMonth.get(product);
					if ( productAvailabilityPrevMonth != null ) {
						percentageStoresWithProductPrevMonth =(String) ((LinkedHashMap<String, Object>)productAvailabilityPrevMonth).get("percentageStoresWithProduct");
					}
				}
				float percentageChange = Float.parseFloat(percentageStoresWithProduct) - Float.parseFloat(percentageStoresWithProductPrevMonth);
				productAvailability.put("level", userGeoName);
				productAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
				List<LinkedHashMap<String,Object>> list = new ArrayList<LinkedHashMap<String,Object>>();
				list.add(productAvailability);
				groupedDataProductLevel.put(product, list);
			}
			List<Map<String,List<LinkedHashMap<String,Object>>>> list = new ArrayList<Map<String,List<LinkedHashMap<String,Object>>>>();
			list.add(groupedDataProductLevel);
			groupedData.put(brand, list);
		}
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> productAvailabilityForRequestedMonthByBrandHigherGeo = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailability::visitMonthHigherGeo = {}", productAvailabilityForRequestedMonthByBrandHigherGeo );

		
		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userHigherGeoLevelStores);
		Map<String, LinkedHashMap<String, Object>> productAvailabilityForPreviousMonthByBrandHigherGeo = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailability::previousMonthHigherGeo = {}", productAvailabilityForPreviousMonthByBrandHigherGeo);
		
		for ( String brand : productAvailabilityForRequestedMonthByBrandHigherGeo.keySet()) {
			LinkedHashMap<String, Object> brandAvailability = productAvailabilityForRequestedMonthByBrandHigherGeo.get(brand);
			LinkedHashMap<String, Object> brandAvailabilityPrevMonth = productAvailabilityForPreviousMonthByBrandHigherGeo.get(brand);
			List<Map<String,List<LinkedHashMap<String,Object>>>> groupedDataProductLevel = groupedData.get(brand);
			for ( String product : brandAvailability.keySet() ) {
				LinkedHashMap<String, Object> productAvailability = (LinkedHashMap<String, Object>) brandAvailability.get(product);
				String percentageStoresWithProduct = (String)productAvailability.get("percentageStoresWithProduct");
				String percentageStoresWithBrandPrevMonth = "0";
				if ( brandAvailabilityPrevMonth != null ) {
					Object productAvailabilityPrevMonth =  brandAvailabilityPrevMonth.get(product);
					if ( productAvailabilityPrevMonth != null ) {
						percentageStoresWithBrandPrevMonth =(String) ((LinkedHashMap<String, Object>)productAvailabilityPrevMonth).get("percentageStoresWithProduct");
					}
				}
				float percentageChange = Float.parseFloat(percentageStoresWithProduct) - Float.parseFloat(percentageStoresWithBrandPrevMonth);
				productAvailability.put("level", userHigherGeoName);
				productAvailability.put("percentageChange", DECIMAL_FORMATTER.format(percentageChange));
				for ( Map<String, List<LinkedHashMap<String, Object>>> map : groupedDataProductLevel) {
					map.get(product).add(productAvailability);
				}
			}
		}
		
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();
		
		for ( String brandName : groupedData.keySet() ) {
			LinkedHashMap<String,Object> brandData = new LinkedHashMap<String,Object>();
			List<Map<String,List<LinkedHashMap<String,Object>>>> groupedDataProductLevel = groupedData.get(brandName);
			List<Map<String,Object>> productData = new ArrayList<Map<String,Object>>();
			for ( Map<String, List<LinkedHashMap<String, Object>>> map : groupedDataProductLevel ) {
				for ( String product : map.keySet() ) {
					Map<String,Object> oneProductData = new HashMap<String,Object>();
					oneProductData.put("name", product);
					oneProductData.put("data", map.get(product));
					productData.add(oneProductData);
				}
			}
			brandData.put("brand" , brandName);
			brandData.put("products", productData);
			result.add(brandData);
		}

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectProductAvailability----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaProjectProductAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaProjectProductAvailabilityByStore----------------\n");
		List<LinkedHashMap<String,Object>> result = new ArrayList<LinkedHashMap<String,Object>>();

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
		LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(visitYear), Integer.parseInt(visitMonth), Integer.parseInt("1"));
		LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
		String previousMonth = ""+previousMonthDate.getMonthValue();
		if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
		String previousYear = ""+previousMonthDate.getYear();
		
		List<String> storeList = new ArrayList<String>();
		storeList.add(inputObject.getStoreId());
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String parentProjectId = projectDetail.get(0).get("parentProjectId");
		
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("parentProjectId", parentProjectId);
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("skuTypeIds", inputObject.getSkuTypeIds());

		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> productsForRequestedMonthByBrand = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailabilityByStore::visitMonth = {}", productsForRequestedMonthByBrand);

		queryArgs.put("year", previousYear);
		queryArgs.put("month", previousMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", storeList);
		Map<String, LinkedHashMap<String, Object>> productsForPreviousMonthByBrand = 
				altriaDashboardDao.getAltriaProjectProductAvailability(queryArgs);
		LOGGER.info("---------------AltriaDashboardServiceImpl::getAltriaProjectProductAvailabilityByStore::previousMonth = {}", productsForPreviousMonthByBrand);

		for ( String brand : productsForRequestedMonthByBrand.keySet()) {
			List<LinkedHashMap<String, Object>> productAvailabilityInStore = new ArrayList<LinkedHashMap<String,Object>>();
			if (productsForPreviousMonthByBrand.containsKey(brand)) {
				LinkedHashMap<String, Object> productsForRequestedMonth = productsForRequestedMonthByBrand.get(brand);
				LinkedHashMap<String, Object> productsForPreviousMonth = productsForPreviousMonthByBrand.get(brand);
				for (String product : productsForRequestedMonth.keySet() ) {
					LinkedHashMap<String, Object> oneProduct = new LinkedHashMap<String, Object>();
					oneProduct.put("name", product);
					
					LinkedHashMap<String,Object> productMap = (LinkedHashMap<String, Object>) productsForRequestedMonth.get(product);
					String storesWithProduct = (String) productMap.get("storesWithProduct");
					String present = "0";
					if ( StringUtils.isNotBlank(storesWithProduct) && Integer.parseInt(storesWithProduct) > 0 ) {
						present = "1";
					}
					oneProduct.put("present",present);
					
					String presentInPreviousVisit = "0";
					if ( productsForPreviousMonth != null && productsForPreviousMonth.containsKey(product) ) {
						LinkedHashMap<String,Object> productMapPrevMonth = (LinkedHashMap<String, Object>) productsForPreviousMonth.get(product);
						String storesWithProductPrevMonth = (String) productMapPrevMonth.get("storesWithProduct");
						if ( StringUtils.isNotBlank(storesWithProductPrevMonth) && Integer.parseInt(storesWithProductPrevMonth) > 0 ) {
							presentInPreviousVisit = "1";
						}
					}
					oneProduct.put("presentInPreviousVisit",presentInPreviousVisit);
					
					productAvailabilityInStore.add(oneProduct);
				}
			} else {
				LinkedHashMap<String, Object> productsForRequestedMonth = productsForRequestedMonthByBrand.get(brand);
				for (String product : productsForRequestedMonth.keySet() ) {
					LinkedHashMap<String, Object> oneProduct = new LinkedHashMap<String, Object>();
					LinkedHashMap<String,Object> productMap = (LinkedHashMap<String, Object>) productsForRequestedMonth.get(product);
					String storesWithProduct = (String) productMap.get("storesWithProduct");
					String present = "0";
					if ( StringUtils.isNotBlank(storesWithProduct) && Integer.parseInt(storesWithProduct) > 0 ) {
						present = "1";
					}
					oneProduct.put("name", product);
					oneProduct.put("present",present);
					oneProduct.put("presentInPreviousVisit","0");
					productAvailabilityInStore.add(oneProduct);
				}
			}
			LinkedHashMap<String, Object> brandLevel = new LinkedHashMap<String,Object>();
			brandLevel.put("brand", brand);
			brandLevel.put("products", productAvailabilityInStore);
			result.add(brandLevel);
		}
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaProjectProductAvailabilityByStore----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getAltriaProjectAllStoreResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getAltriaProjectAllStoreResults----------------\n");
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		String userGeoName = (String) storesForUser.get("userGeoName");
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		
		List<String> childProjectIds = new ArrayList<String>();
		List<LinkedHashMap<String, String>> childProjects = metaServiceDao.listChildProjects(
				inputObject.getCustomerCode(),inputObject.getProjectId());
		for (LinkedHashMap<String, String> oneProject : childProjects ) {
			childProjectIds.add(oneProject.get("id"));
		}
		
		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
				
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("parentProjectId", ""+inputObject.getProjectId());
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("stores", userGeoLevelStores);
		queryArgs.put("childProjectIds", childProjectIds);

		List<LinkedHashMap<String, String>> result = altriaDashboardDao.getAltriaProjectAllStoreResults(queryArgs);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getAltriaProjectAllStoreResults----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getAltriaProjectStoreImagesByStore(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getAltriaProjectStoreImagesByStore----------------\n");
		
		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
				
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("parentProjectId", ""+inputObject.getProjectId());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("store", inputObject.getStoreId());
		queryArgs.put("customerCode", inputObject.getCustomerCode());

		List<LinkedHashMap<String, String>> result = altriaDashboardDao.getAltriaProjectStoreImagesByStore(queryArgs);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getAltriaProjectStoreImagesByStore----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getAltriaStoresForReview(InputObject inputObject){
		LOGGER.info("--------------AltriaDashboardServiceImpl starts getAltriaStoresForReview-----------------");
		List<LinkedHashMap<String, String>> result = altriaDashboardDao.getAltriaStoresForReview(inputObject);
		LOGGER.info("--------------AltriaDashboardServiceImpl ends getAltriaStoresForReview-----------------");
		return result;
	}

	private Map<String,Object> getMappedStoresByUser(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getMappedStoresByUser userId = {}", inputObject.getUserId());
		List<LinkedHashMap<String, String>> mappedStoresForUser = 
				storeMasterDao.getGeoMappedStoresByUserId(inputObject.getCustomerCode(), inputObject.getUserId());
		String geoLevel = "";
		String geoLevelId = "";
		String geoLevelName = "";
		List<String> userGeoLevelStores = new ArrayList<String>();
		for(LinkedHashMap<String, String> store : mappedStoresForUser) {
			userGeoLevelStores.add(store.get("storeId"));
			geoLevel = store.get("geoLevel");
			geoLevelId = store.get("geoLevelId");
			geoLevelName = store.get("geoLevelName");
		}
		
		Map<String,Object> mappedStores = new HashMap<String,Object>();
		mappedStores.put("userGeoLevel", geoLevel);
		mappedStores.put("userGeoName", geoLevelName);
		mappedStores.put("userGeoId", geoLevelId);
		mappedStores.put("userGeoLevelStores", userGeoLevelStores);
		
		Map<String, List<String>> storeIdsInNextLevel = storeMasterDao.getNextGeoLevelStoresByLevel(inputObject.getCustomerCode(), geoLevel, geoLevelId);
		for ( String nextLevel : storeIdsInNextLevel.keySet() ) {
			mappedStores.put("userHigherGeoName",nextLevel);
			mappedStores.put("userHigherGeoLevelStores",storeIdsInNextLevel.get(nextLevel));
		}
		
		LOGGER.info("---------------AltriaDashboardServiceImpl::getMappedStoresByUser:: Stores For User = {}", mappedStores);
		
		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getMappedStoresByUser----------------\n");
		return mappedStores;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getAltriaStoreImagesForReview(InputObject inputObject) {
		LOGGER.info("----------------------AltriaDashboardServiceImpl getAltriaStoreImagesForReview Start : {}", inputObject);
		List<LinkedHashMap<String, String>> queryResult = altriaDashboardDao.getAltriaStoreImagesForReview(inputObject);

		Map<String, Set<String>> imageUUIDs = new HashMap<String, Set<String>>();

		for(LinkedHashMap<String, String> imageData: queryResult) {
			if(imageUUIDs.containsKey(imageData.get("projectId") + "___" +imageData.get("categoryId") + "___" +imageData.get("categoryName"))){
				imageUUIDs.get(imageData.get("projectId") + "___" +imageData.get("categoryId") + "___" +imageData.get("categoryName")).add(imageData.get("imageUUID"));
			} else {
				Set<String> uuids = new HashSet<String>();
				uuids.add(imageData.get("imageUUID"));
				imageUUIDs.put(imageData.get("projectId") + "___" +imageData.get("categoryId") + "___" +imageData.get("categoryName"), uuids);
			}
		}

		List<LinkedHashMap<String, Object>> result = new ArrayList<LinkedHashMap<String, Object>>();

		for(Map.Entry<String, Set<String>> entry: imageUUIDs.entrySet()) {
			LinkedHashMap<String, Object> entryData = new LinkedHashMap<String, Object>();
			entryData.put("projectId",entry.getKey().split("___")[0]);
			entryData.put("categoryId",entry.getKey().split("___")[1]);
			entryData.put("categoryName",entry.getKey().split("___")[2]);
			entryData.put("imageUUID",entry.getValue());
			result.add(entryData);
		}
		
		result.sort(Comparator.comparing(
                m ->(String) m.get("categoryName"), 
                Comparator.nullsLast(Comparator.naturalOrder()))
           );
		
		LOGGER.info("----------------------AltriaDashboardServiceImpl getAltriaStoreImagesForReview ends --------------------------------");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getProductDetectionsForReview(InputObject inputObject) {
		LOGGER.info("----------------------AltriaDashboardServiceImpl getProductDetectionsForReview Start : {}", inputObject);
		List<LinkedHashMap<String, String>> imageAnalysisDetails = altriaDashboardDao.getAltriaImageAnalysisDetails(inputObject);
		List<LinkedHashMap<String, String>> upcDetails = altriaDashboardDao.getAltriaProductUPCs(inputObject);

		String imageReviewRecommendations = "";
		if ( imageAnalysisDetails != null && !imageAnalysisDetails.isEmpty() ) {
			imageReviewRecommendations = imageAnalysisDetails.get(0).get("imageReviewRecommendations");
		}
		
		Map<String, SkuTypePOJO> skuTypePOJOS = new HashMap<String,SkuTypePOJO>();
		SkuTypePOJO skuTypePOJO;

		for(LinkedHashMap<String, String> imageData: upcDetails) {

			skuTypePOJO = new SkuTypePOJO();
			skuTypePOJO.setSkuTypeId(imageData.get("skuTypeId"));
			skuTypePOJO.setSkuType(imageData.get("skuType"));

			String upc = imageData.get("upc");
			String name = imageData.get("name");
			if ( StringUtils.isBlank(name) ) { name = upc; }
			
			if(imageAnalysisDetails.stream().anyMatch(o -> o.get("upc").equals(upc))) {
				if(!skuTypePOJOS.isEmpty() && skuTypePOJOS.containsKey(imageData.get("skuTypeId")+"__"+imageData.get("skuType"))) {
					skuTypePOJO = skuTypePOJOS.get(imageData.get("skuTypeId")+"__"+imageData.get("skuType"));
				}
				skuTypePOJO.addFound(upc, name);
				skuTypePOJOS.put(imageData.get("skuTypeId")+"__"+imageData.get("skuType"), skuTypePOJO);
			} else {
				if(!skuTypePOJOS.isEmpty() && skuTypePOJOS.containsKey(imageData.get("skuTypeId")+"__"+imageData.get("skuType"))) {
					skuTypePOJO = skuTypePOJOS.get(imageData.get("skuTypeId")+"__"+imageData.get("skuType"));
				}
				skuTypePOJO.addNotFound(upc, name);
				skuTypePOJOS.put(imageData.get("skuTypeId")+"__"+imageData.get("skuType"), skuTypePOJO);
			}
		}

		List<LinkedHashMap<String, Object>> result = new ArrayList<LinkedHashMap<String, Object>>();

		if(skuTypePOJOS.isEmpty()){
			return result;
		}

		//Combine focus and non focus Products into one so that it is easier to review
		SkuTypePOJO focusSKUs =  skuTypePOJOS.get("1__Products");
		SkuTypePOJO nonFocusSKUs =  skuTypePOJOS.get("2__Non Focus Products");
		if ( focusSKUs != null && nonFocusSKUs != null ) {
			focusSKUs.getFound().addAll(nonFocusSKUs.getFound());
			focusSKUs.getNotFound().addAll(nonFocusSKUs.getNotFound());
			focusSKUs.setSkuType("Products");
			skuTypePOJOS.put("1__Products", focusSKUs);
			skuTypePOJOS.remove("2__Non Focus Products");
		}
		
		//Combine focus and non focus Signages into one so that it is easier to review
		SkuTypePOJO focusSignages =  skuTypePOJOS.get("4__Signages");
		SkuTypePOJO nonFocusSignages =  skuTypePOJOS.get("5__Non Focus Signages");
		if ( focusSignages != null && nonFocusSignages != null ) {
			focusSignages.getFound().addAll(nonFocusSignages.getFound());
			focusSignages.getNotFound().addAll(nonFocusSignages.getNotFound());
			focusSignages.setSkuType("Signages");
			skuTypePOJOS.put("4__Signages", focusSignages);
			skuTypePOJOS.remove("5__Non Focus Signages");
		}

		LinkedHashMap<String, Object> entryData = new LinkedHashMap<String, Object>();
		entryData.put("imageUUID", inputObject.getImageUUID());
		entryData.put("projectId", inputObject.getProjectId());
		entryData.put("storeId", inputObject.getStoreId());
		
		if ( !imageAnalysisDetails.isEmpty() ) {
			entryData.put("dateId", imageAnalysisDetails.get(0).get("dateId"));
			entryData.put("taskId", imageAnalysisDetails.get(0).get("taskId"));
		} else {
			ImageStore image = processImageDao.findByImageUUId(inputObject.getImageUUID());
			entryData.put("dateId", image.getDateId());
			entryData.put("taskId", image.getTaskId());
		}
		
		entryData.put("imageReviewRecommendations", imageReviewRecommendations);

		List<SkuTypePOJO> resultPOJOS = new ArrayList<>();
		for(Map.Entry<String, SkuTypePOJO> entry: skuTypePOJOS.entrySet()) {
			resultPOJOS.add(entry.getValue());
		}

		entryData.put("data", resultPOJOS);
		result.add(entryData);
		LOGGER.info("----------------------AltriaDashboardServiceImpl getProductDetectionsForReview ends --------------------------------");
		return result;
	}

	@Override
	public void altriaAddOrUpdateProductDetection(InputObject inputObject, List<SkuTypePOJO> skuTypePOJOS) {
		LOGGER.info("----------------------AltriaDashboardServiceImpl altriaAddOrUpdateProductDetection Start : {}", inputObject);

		List<String> upcsToAdd = new ArrayList<String>();
		List<String> upcsToDelete = new ArrayList<String>();
		
		//To filter existing UPCs out from newly ones added via review
		List<LinkedHashMap<String, String>> existingDetections = altriaDashboardDao.getAltriaImageAnalysisDetails(inputObject);
		Set<String> existingUPCs = new HashSet<String>();
		for (LinkedHashMap<String, String> oneDetection : existingDetections ) {
			existingUPCs.add(oneDetection.get("upc"));
		}
		
		LinkedHashMap<String, String> inputMap = new LinkedHashMap<String,String>();
		inputMap.put("projectId", inputObject.getProjectId()+"");
		inputMap.put("storeId", inputObject.getStoreId());
		inputMap.put("imageUUID", inputObject.getImageUUID());
		inputMap.put("dateId", inputObject.getValue());
		inputMap.put("taskId", inputObject.getTaskId());
		
		for(SkuTypePOJO skuTypePOJO: skuTypePOJOS) {
			for(Map<String, String> upc: skuTypePOJO.getFound()) {
				if ( !existingUPCs.contains(upc.get("upc")) ) {
					upcsToAdd.add(upc.get("upc"));
				}
			}

			for(Map<String, String> upc: skuTypePOJO.getNotFound()) {
				upcsToDelete.add(upc.get("upc"));
			}
		}

		if(!upcsToAdd.isEmpty()) {
			altriaDashboardDao.createImageAnalysisNewForAltria(inputMap, upcsToAdd);
		}

		if(!upcsToDelete.isEmpty()) {
			altriaDashboardDao.deleteImageAnalysisNewForAltria(inputMap, upcsToDelete);
		}
		
		LOGGER.info("----------------------AltriaDashboardServiceImpl::altriaAddOrUpdateProductDetection::"
				+ "Detections updated. Computing Image Results --------------------------------");

		//Recompute image result
		ImageStore imageStore = processImageDao.findByImageUUId(inputObject.getImageUUID());
		List<ImageAnalysis> imageAnalysisList = processImageDao.getImageAnalysisForRecompute(inputObject.getImageUUID());
		Map<String, String> resultMap = processImageDao.generateImageResult(imageStore, imageAnalysisList);
		ImageResultCode resultCode = ImageResultCode.getImageResultCodeFromCode(resultMap.get("resultCode"));
		String resultComment = (String) resultMap.get("resultComment");
		String objectiveResultStatus = (String) resultMap.get("objectiveResultStatus");

		String imageReviewStatus = "1";
		if (resultCode == ImageResultCode.REJECT_LEVEL_1
				|| resultCode == ImageResultCode.REJECT_LEVEL_3
				|| resultCode == ImageResultCode.APPROVED_PENDING_REVIEW
				|| resultCode == ImageResultCode.REJECT_INSUFFICIENT
				|| resultCode == ImageResultCode.UNAPPROVED) {
			imageReviewStatus = "0"; // for review
		}
		processImageDao.updateImageResultCodeAndStatus(imageStore.getImageUUID(),
				resultCode.getCode(), resultComment, imageReviewStatus, objectiveResultStatus);

		LOGGER.info("----------------------AltriaDashboardServiceImpl altriaAddOrUpdateProductDetection ends --------------------------------");
	}

	@Override
	public List<LinkedHashMap<String, Object>> runAggregationForAltria(InputObject inputObject) {
		LOGGER.info("----------------------AltriaDashboardServiceImpl getProductDetectionsForReview Start : {}",inputObject );
		
		List<LinkedHashMap<String, String>> storeVisitsToAggregate = altriaDashboardDao.getStoreVisitsToAggregateByProjectIdStoreId(inputObject);

		LOGGER.info("----------------------AltriaDashboardServiceImpl::getProductDetectionsForReview::Store Visits to Aggregate = " 
				+ storeVisitsToAggregate + "--------------------------------");
		
		List<Map<String,String>> storeVisits = new ArrayList<Map<String,String>>();
		
		for(LinkedHashMap<String, String> project: storeVisitsToAggregate){
			LOGGER.info("----------------------AltriaDashboardServiceImpl::getProductDetectionsForReview::Calling recompute for : "
					+ project + "--------------------------------");
			
			InputObject recomputeDetails = new InputObject();
			recomputeDetails.setProjectId(Integer.parseInt(project.get("projectId")));
			recomputeDetails.setStoreId(project.get("storeId"));
			recomputeDetails.setTaskId(project.get("taskId"));
			recomputeDetails.setGranularity("agg-store");
			
			processImageService.recomputeProjectByStoreVisit(recomputeDetails);
			
			LOGGER.info("----------------------AltriaDashboardServiceImpl::getProductDetectionsForReview::recompute ends--------------------------------");

			Map<String,String> storeVisit = new HashMap<String,String>();
			storeVisit.put("projectId",project.get("projectId"));
			storeVisit.put("storeId", project.get("storeId"));
			storeVisit.put("taskId", project.get("taskId"));
			storeVisits.add(storeVisit);
		}

		LOGGER.info("----------------------AltriaDashboardServiceImpl::getProductDetectionsForReview::Updating store visit status for : {}", storeVisits );
		altriaDashboardDao.updateProjectStoreResultsResultCodeAndStatusForAltria(storeVisits);

		List<LinkedHashMap<String, Object>> result = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> entryData = new LinkedHashMap<String, Object>();
		entryData.put("projectId", inputObject.getProjectId());
		entryData.put("storeId", inputObject.getStoreId());
		entryData.put("date", inputObject.getValue());
		entryData.put("storeVisits", storeVisits);

		result.add(entryData);
		LOGGER.info("----------------------AltriaDashboardServiceImpl getProductDetectionsForReview ends --------------------------------");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getLinearFootageByStore(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getLinearFootageByStore::projectId="+inputObject.getProjectId()
			+",storeId="+inputObject.getStoreId()+",level="+ inputObject.getLevel()+",value="+inputObject.getValue()+"----------------\n");
		
		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
				
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("store", inputObject.getStoreId());

		List<LinkedHashMap<String, String>> result = altriaDashboardDao.getLinearFootageByStore(queryArgs);

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getLinearFootageByStore----------------\n");

		return result;
	}
	
	@Override
	public void updateLinearFootageByStore(InputObject inputObject) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts updateLinearFootageByStore::projectId="+inputObject.getProjectId()
			+",storeId="+inputObject.getStoreId()+",level="+ inputObject.getLevel()+",value="+inputObject.getValue()
			+",footage="+inputObject.getLinearFootage()+"----------------\n");
		
		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
				
		Map<String,Object> queryArgs = new HashMap<String,Object>();
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("store", inputObject.getStoreId());
		queryArgs.put("linearFootage", inputObject.getLinearFootage());

		altriaDashboardDao.updateLinearFootageByStore(queryArgs);

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends updateLinearFootageByStore----------------\n");
	}

	@Override
	public File getAltriaStoreVisitReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------AltriaDashboardServiceImpl Starts getAltriaStoreVisitReport----------------\n");

		//Current implementation assumes level is always month.
		String month = inputObject.getValue();
		// month will be of format YYYYMM
		String visitYear = month.substring(0, 4);
		String visitMonth = month.substring(4,6);
				
		List<LinkedHashMap<String, String>> childProjects = metaServiceDao.listChildProjects(inputObject.getCustomerCode(), inputObject.getProjectId());
		Map<String, String> projectToCategoryMap = new HashMap<String,String>();
		for ( LinkedHashMap<String, String> childProject : childProjects) {
			projectToCategoryMap.put(childProject.get("id"),childProject.get("category"));
		}
		
		Map<String, Object> storesForUser = getMappedStoresByUser(inputObject);
		List<String> userGeoLevelStores = (List<String>) storesForUser.get("userGeoLevelStores");
		String userGeoLevel = (String) storesForUser.get("userGeoLevel");
		String userGeoLevelId = (String) storesForUser.get("userGeoId");
		
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet storeVisitStatsSheet = workbook.createSheet("Store visits by TSM");
		XSSFSheet storeWisePhotoCountSheet = workbook.createSheet("Photo count by store");
		XSSFSheet storeWiseProductAvailabilitySheet = workbook.createSheet("Product avaialability by store");
		workbook.setSheetOrder("Store visits by TSM", 0);
		workbook.setSheetOrder("Photo count by store", 1);
		workbook.setSheetOrder("Product avaialability by store", 2);

		Map<String,Object> queryArgs = new HashMap<String,Object>();
		int colNum = 0;
		int rowNum = 0;

		//First worksheet = Store Audited By Rep
		Map<String, Map<String, String>> agentMap = altriaDashboardDao.getAgentsByGeoLevelAndId(inputObject.getCustomerCode(), userGeoLevel, userGeoLevelId);
		
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("stores", userGeoLevelStores);
		Map<String, Object> photosByAgentMap = altriaDashboardDao.getJobStatsByAgent(queryArgs);
		
		if ( null != photosByAgentMap.get("maxVisitDateId") ) {
			String maxVisitDateId = (String) photosByAgentMap.get("maxVisitDateId");
			photosByAgentMap.remove(maxVisitDateId);
			
			TreeMap<String,String> visitDaysMap = altriaDashboardDao.getVisitDaysInASalesMonth(inputObject.getValue());
			TreeMap<String,String> visitDaysToReportMap = new TreeMap<String,String>();
			for(String visitDateId:visitDaysMap.keySet()) {
				visitDaysToReportMap.put(visitDateId, visitDaysMap.get(visitDateId));
				if(visitDateId.equals(maxVisitDateId)) {
					break;
				}
			}
			
			List<String> columns = new ArrayList<String>();
			columns.add("Territory");
			columns.add("TSM");
			columns.add("Total Store Visits");
			for(String visitDateId:visitDaysToReportMap.descendingKeySet()) {
				columns.add(visitDaysToReportMap.get(visitDateId));
			}
			Row headerRow1 = storeVisitStatsSheet.createRow(rowNum++);
			
			for(String columnHeader : columns ) {
				Cell cell = headerRow1.createCell(colNum++);
				cell.setCellValue(columnHeader);
			}
			int grandTotal = 0;
			for(String oneAgent : agentMap.keySet() ) {
				colNum = 0;
				Map<String,String> oneMap = agentMap.get(oneAgent);
				Map<String,Map<String,String>> stats = null;
				if ( photosByAgentMap.get(oneAgent) != null ) {
					stats = (Map<String, Map<String, String>>)photosByAgentMap.get(oneAgent);
				}
				
				Row oneRow = storeVisitStatsSheet.createRow(rowNum++);
				String territoryId = oneMap.get("geoLevelId");
				oneRow.createCell(colNum++).setCellValue(territoryId);
				oneRow.createCell(colNum++).setCellValue(oneAgent);
				oneRow.createCell(colNum++).setCellValue("");
				int totalStoresByAgent = 0;
				for(String visitDateId:visitDaysToReportMap.descendingKeySet()) {
					String storeCount = "";
					
					if ( stats != null &&  stats.get(visitDateId) != null ) {
						storeCount = stats.get(visitDateId).get("storeCount");
					}
					oneRow.createCell(colNum++).setCellValue(storeCount);
					totalStoresByAgent = totalStoresByAgent + Integer.parseInt(storeCount.isEmpty() ? "0" : storeCount);
				}
				oneRow.getCell(2).setCellValue(totalStoresByAgent);
				grandTotal = grandTotal + totalStoresByAgent;
			}
			Row grandTotalRow = storeVisitStatsSheet.createRow(rowNum++);
			grandTotalRow.createCell(1).setCellValue("Grand Total");
			grandTotalRow.createCell(2).setCellValue(grandTotal);
		}
		
		//Second worksheet = Storewise Photo Count
		queryArgs.clear();
		rowNum = 0;
		colNum = 0;
		String headers  = "RC Number,Territory,Unit,District,Section,Region,Retail Name,Street Address,City,State,Store Visit Date,Agent,Photo Count,Poor Photo Count";
		List<String>columns = Arrays.asList(headers.split(","));
		Row headerRow2 = storeWisePhotoCountSheet.createRow(rowNum++);
		for(String columnHeader : columns ) {
			Cell cell = headerRow2.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("stores", userGeoLevelStores);
		List<Map<String, String>> photosByStoreMap = altriaDashboardDao.getStorewisePhotoCount(queryArgs);
		for (Map<String, String> row : photosByStoreMap) {
			colNum = 0;
			Row oneRow = storeWisePhotoCountSheet.createRow(rowNum++);
			oneRow.createCell(colNum++).setCellValue(row.get("customerStoreNumber"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel1Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel2Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel3Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel4Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel5Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("retailName"));
			oneRow.createCell(colNum++).setCellValue(row.get("street"));
			oneRow.createCell(colNum++).setCellValue(row.get("city"));
			oneRow.createCell(colNum++).setCellValue(row.get("stateCode"));
			oneRow.createCell(colNum++).setCellValue(row.get("visitDate"));
			oneRow.createCell(colNum++).setCellValue(row.get("agentId"));
			oneRow.createCell(colNum++).setCellValue(row.get("photoCount"));
			oneRow.createCell(colNum++).setCellValue(row.get("notUsablePhotoCount"));
		}
		
		//Third worksheet = Storewise Product Avaialability
		queryArgs.clear();
		rowNum = 0;
		colNum = 0;
		queryArgs.put("aggregationLevel", "territory");
		queryArgs.put("projectId", ""+inputObject.getProjectId());
		queryArgs.put("year", visitYear);
		queryArgs.put("month", visitMonth);
		queryArgs.put("stores", userGeoLevelStores);
		queryArgs.put("customerCode", inputObject.getCustomerCode());
		queryArgs.put("projectToCategoryMap", projectToCategoryMap);

		List<Map<String, String>> productsFound = altriaDashboardDao.getAltriaProjectProductAvailabilityForReport(queryArgs);
		
		headers = "RC Number,Territory,Unit,District,Section,Region,Retail Name,Street Address,City,State,Store Visit Date,Agent,Category,SKU Type,Brand Name,Product Name,Is Available";
		columns = Arrays.asList(headers.split(","));
		Row headerRow3 = storeWiseProductAvailabilitySheet.createRow(rowNum++);
		for(String columnHeader : columns ) {
			Cell cell = headerRow3.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}

		for (Map<String, String> row : productsFound) {
			colNum = 0;
			Row oneRow = storeWiseProductAvailabilitySheet.createRow(rowNum++);
			oneRow.createCell(colNum++).setCellValue(row.get("customerStoreNumber"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel1Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel2Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel3Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel4Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("geoLevel5Id"));
			oneRow.createCell(colNum++).setCellValue(row.get("retailName"));
			oneRow.createCell(colNum++).setCellValue(row.get("street"));
			oneRow.createCell(colNum++).setCellValue(row.get("city"));
			oneRow.createCell(colNum++).setCellValue(row.get("stateCode"));
			oneRow.createCell(colNum++).setCellValue(row.get("visitDate"));
			oneRow.createCell(colNum++).setCellValue(row.get("agentId"));
			oneRow.createCell(colNum++).setCellValue(row.get("categoryName"));
			oneRow.createCell(colNum++).setCellValue(row.get("skuTypeName"));
			oneRow.createCell(colNum++).setCellValue(row.get("brandName"));
			oneRow.createCell(colNum++).setCellValue(row.get("productName"));
			oneRow.createCell(colNum++).setCellValue(row.get("available"));
		}
		
		//Now write the workbook to file and return
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
		}

		File f = new File(tempFilePath);

		LOGGER.info("---------------AltriaDashboardServiceImpl Ends getAltriaStoreVisitReport----------------\n");
		return f;
	}
}

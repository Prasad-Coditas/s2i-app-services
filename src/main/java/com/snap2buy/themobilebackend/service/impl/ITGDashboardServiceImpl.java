package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.Gson;
import com.snap2buy.themobilebackend.dao.ITGDashboardDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.itg.*;
import com.snap2buy.themobilebackend.service.ITGDashboardService;
import com.snap2buy.themobilebackend.util.ConverterUtil;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by ANOOP.
 */
@Component(value = BeanMapper.BEAN_ITG_DASHBOARD_SERVICE)
@Scope("prototype")
public class ITGDashboardServiceImpl implements ITGDashboardService {

	private static Logger LOGGER = LoggerFactory.getLogger(ITGDashboardServiceImpl.class);
	
	private final DecimalFormat ITG_PERCENTAGE_FORMATTER = new DecimalFormat("##0.0");
	
	private final DecimalFormat ITG_ABS_FORMATTER = new DecimalFormat("###");
	
	private final DecimalFormat ITG_PRICE_FORMATTER = new DecimalFormat("##0.00");
	
	private final String ITG_VALUE_TYPE_PERCENTAGE = "%";
	
	private final String ITG_VALUE_TYPE_PRICE = "$";
	
	private final String ITG_VALUE_TYPE_ABS = "-";
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_ITG_DASHBOARD_DAO)
	private ITGDashboardDao itgDashboardDao;

	@Override
	public List<LinkedHashMap<String, Object>> getITGStoreDetails(InputObject inputObject) {
		LOGGER.info("---------------ITGDashboardServiceImpl Starts getITGStoreDetails----------------\n");
		
		List<String> itgBrands = Arrays.asList("Winston","Kool","Maverick","USA Gold","Salem","Montclair","Sonoma", "Crowns", "Fortuna", "Rave");
		List<String> competitionBrands = Arrays.asList("Marlboro SS","Newport Menthol","Camel Original","Pall Mall","Eagle");
		
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		
		Map<String,String> storeDataMap = itgDashboardDao.getITGStoreDetails(inputObject.getProjectId(),inputObject.getStoreId(),
				inputObject.getTimePeriodType(),inputObject.getTimePeriod());
		
		if ( storeDataMap.keySet().isEmpty() ) {
			return resultList; //no need to go further
		}
		
		result.put("storeId",ConverterUtil.ifNullToEmpty(storeDataMap.get("STORE_ID")));
		result.put("storeCRS",ConverterUtil.ifNullToEmpty(storeDataMap.get("STORE_CRS")));
		result.put("storePlanType",ConverterUtil.ifNullToEmpty(storeDataMap.get("STORE_PLAN")));
		result.put("timePeriod",ConverterUtil.ifNullToEmpty(storeDataMap.get("PERIOD")));
		result.put("isEDLPStore",ConverterUtil.ifNullToEmpty(storeDataMap.get("EDLP_STATUS")));
		result.put("facingCompliance",ConverterUtil.ifNullToEmpty(storeDataMap.get("PLAN_FACING_COMPLIANCE")));
		result.put("isAboveVolumeShare",ConverterUtil.ifNullToEmpty(storeDataMap.get("ABOVE_VOLUME_SHARE")));
		result.put("isAboveMarketShare",ConverterUtil.ifNullToEmpty(storeDataMap.get("ABOVE_MARKET_SHARE")));
		result.put("score",ConverterUtil.ifNullToEmpty(storeDataMap.get("SCORE")));
		
		Map<String,Object> shareOfShelfData = new HashMap<String,Object>();
		shareOfShelfData.put("planFacingCount", ConverterUtil.ifNullToEmpty(storeDataMap.get("PLAN_FACING_COUNT")));
		shareOfShelfData.put("itgbFacingCount", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_FACING_COUNT")));
		shareOfShelfData.put("itgbOOSCount", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_OOS_COUNT")));
		shareOfShelfData.put("totalFacingCount", ConverterUtil.ifNullToEmpty(storeDataMap.get("TOTAL_FACING_COUNT")));
		shareOfShelfData.put("totalOOSCount", ConverterUtil.ifNullToEmpty(storeDataMap.get("TOTAL_OOS_COUNT")));
		shareOfShelfData.put("itgbShareOfFacings", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_SHARE_OF_FACINGS")));
		shareOfShelfData.put("itgbShareOfShelf", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_SHARE_OF_SHELF")));
		shareOfShelfData.put("itgbOSA", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_OSA_PERCENTAGE")));
		shareOfShelfData.put("volumeShare", ConverterUtil.ifNullToEmpty(storeDataMap.get("VOLUME_SHARE")));
		shareOfShelfData.put("marketShare", ConverterUtil.ifNullToEmpty(storeDataMap.get("MARKET_SHARE")));
		
		List<Map<String,String>> brandFacings = new ArrayList<Map<String,String>>();
		for(String brand : itgBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			String brandColumn = brand.toUpperCase()+"_FACING_COUNT";
			if (brand.equals("USA Gold") ) { brandColumn = "USAG_FACING_COUNT"; }
			oneBrand.put("brandName", brand);
			oneBrand.put("count", storeDataMap.get(ConverterUtil.ifNullToEmpty(brandColumn)));
			brandFacings.add(oneBrand);
		}
		shareOfShelfData.put("brandFacings", brandFacings);
		
		result.put("shareOfShelf",shareOfShelfData);
		
		Map<String,Object> pricingData = new HashMap<String,Object>();
		pricingData.put("highestPriceBrands", ConverterUtil.ifNullToEmpty(storeDataMap.get("HIGHEST_PRICE_BRAND")));
		pricingData.put("highestPrice", ConverterUtil.ifNullToEmpty(storeDataMap.get("HIGHEST_PRICE")));
		pricingData.put("lowestPriceBrands", ConverterUtil.ifNullToEmpty(storeDataMap.get("LOWEST_PRICE_BRAND")));
		pricingData.put("lowestPrice", ConverterUtil.ifNullToEmpty(storeDataMap.get("LOWESET_PRICE")));
		
		List<Map<String,String>> itgBrandPrices = new ArrayList<Map<String,String>>();
		for(String brand : itgBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			String brandColumn = brand.toUpperCase()+"_PRICE";
			if (brand.equals("USA Gold") ) { brandColumn = "USAG_PRICE"; }
			oneBrand.put("brandName", brand);
			oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			itgBrandPrices.add(oneBrand);
		}
		pricingData.put("itgBrandPrices", itgBrandPrices);
		
		List<Map<String,String>> competitionBrandPrices = new ArrayList<Map<String,String>>();
		for(String brand : competitionBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			oneBrand.put("brandName", brand);
			String brandColumn = brand.toUpperCase().replace(" ","_")+"_PRICE";
			if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_PRICE"; }
			oneBrand.put("brandName", brand);
			oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			competitionBrandPrices.add(oneBrand);
		}
		pricingData.put("competitionBrandPrices", competitionBrandPrices);
		
		List<Map<String,String>> itgBrandMultipackPrices = new ArrayList<Map<String,String>>();
		for(String brand : itgBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			oneBrand.put("brandName", brand);
			String brandColumn = brand.toUpperCase()+"_MP_PRICE";
			if (brand.equals("USA Gold") ) { brandColumn = "USAG_MP_PRICE"; }
			oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			itgBrandMultipackPrices.add(oneBrand);
		}
		pricingData.put("itgBrandMultipackPrices", itgBrandMultipackPrices);
		
		List<Map<String,String>> competitionBrandMultipackPrices = new ArrayList<Map<String,String>>();
		for(String brand : competitionBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			oneBrand.put("brandName", brand);
			String brandColumn = brand.toUpperCase().replace(" ","_")+"_MP_PRICE";
			if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_MP_PRICE"; }
			oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			competitionBrandMultipackPrices.add(oneBrand);
		}
		pricingData.put("competitionBrandMultipackPrices", competitionBrandMultipackPrices);
		
		result.put("pricing",pricingData);
		
		Map<String,Object> promotionData = new HashMap<String,Object>();
		promotionData.put("promoFacingsITG", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_PROMOTED_FACING_COUNT")));
		promotionData.put("promoFacingsNonITG", ConverterUtil.ifNullToEmpty(storeDataMap.get("NON_ITGB_PROMOTED_FACING_COUNT")));
		promotionData.put("promoShareWithinITGBrands", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_PROMOTION_SOS_IN_ITGB_FACINGS")));
		promotionData.put("promoShareOverall", ConverterUtil.ifNullToEmpty(storeDataMap.get("ITGB_PROMOTION_SOS")));
		
		List<Map<String,String>> itgBrandPromotion = new ArrayList<Map<String,String>>();
		for(String brand : itgBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			oneBrand.put("brandName", brand);
			oneBrand.put("brandName", brand);
			String brandColumn = brand.toUpperCase()+"_PROMO_FACING_COUNT";
			if (brand.equals("USA Gold") ) { brandColumn = "USAG_PROMO_FACING_COUNT"; }
			oneBrand.put("count", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			itgBrandPromotion.add(oneBrand);
		}
		promotionData.put("itgBrandPromotion", itgBrandPromotion);
		
		List<Map<String,String>> competitionBrandPromotion = new ArrayList<Map<String,String>>();
		for(String brand : competitionBrands) {
			Map<String,String> oneBrand = new HashMap<String,String>();
			oneBrand.put("brandName", brand);
			String brandColumn = brand.toUpperCase().replace(" ","_")+"_PROMO_FACING_COUNT";
			if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_PROMO_FACING_COUNT"; }
			oneBrand.put("count", ConverterUtil.ifNullToEmpty(storeDataMap.get(brandColumn)));
			competitionBrandPromotion.add(oneBrand);
		}
		promotionData.put("competitionBrandPromotion", competitionBrandPromotion);
		
		result.put("promotion",promotionData);
		
		List<Map<String,Object>> storeVisitList = itgDashboardDao.getITGStoreVisitImages(inputObject.getProjectId(),inputObject.getStoreId(),
				inputObject.getTimePeriodType(),inputObject.getTimePeriod());
		result.put("visits", storeVisitList);
		
		resultList.add(result);
		
		LOGGER.info("---------------ITGDashboardServiceImpl Ends getITGStoreDetails :: data = {} ----------------\n",result);
		
		return resultList;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getITGStoresWithFilters(InputObject inputObject) {
		LOGGER.info("---------------ITGDashboardServiceImpl Starts getITGStoresWithFilters------");
		
		String filters = inputObject.getValue();
		
		Gson gson = new Gson();
		SearchFilter filter = gson.fromJson(filters, SearchFilter.class);
		
		List<LinkedHashMap<String,Object>> returnList = itgDashboardDao.getITGStoresWithFilters(inputObject.getProjectId(), inputObject.getGeoLevelId(),
					inputObject.getTimePeriodType(),inputObject.getTimePeriod(),inputObject.getLimit(), filter);
		
		LOGGER.info("---------------ITGDashboardServiceImpl Ends getITGStoresWithFilter ----------------\n");
		return returnList;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getITGStats(InputObject inputObject) {
		LOGGER.info("---------------ITGDashboardServiceImpl Starts getITGStats----------------\n");
		
		List<String> itgBrands = Arrays.asList("Winston","Kool","Maverick","USA Gold","Salem","Montclair","Sonoma","Crowns","Fortuna","Rave");
		List<String> competitionBrands = Arrays.asList("Marlboro SS","Camel Original","Newport Menthol","Pall Mall","Eagle");
		List<String> storeTypes = Arrays.asList("ALL","EDLP","NON_EDLP","P1","P2","P3");
		Map<String,String> storeTypeToStoreTypeDisplayNameMap = new HashMap<String,String>();
		storeTypeToStoreTypeDisplayNameMap.put("ALL", "ALL STORES");
		storeTypeToStoreTypeDisplayNameMap.put("EDLP", "ON EDLP");
		storeTypeToStoreTypeDisplayNameMap.put("NON_EDLP", "Non-EDLP");
		storeTypeToStoreTypeDisplayNameMap.put("P1", "GOLD");
		storeTypeToStoreTypeDisplayNameMap.put("P2", "PLATINUM");
		storeTypeToStoreTypeDisplayNameMap.put("P3", "FMC 85%");
		
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		
		String customerCode = inputObject.getCustomerCode();
		String userId = inputObject.getUserId();
		int projectId = inputObject.getProjectId();
		String timePeriodType = inputObject.getTimePeriodType();
		String timePeriod = inputObject.getTimePeriod();
		String previousTimePeriod = getPreviousTimePeriod(timePeriod, timePeriodType);
		String geoLevel = inputObject.getGeoLevel();
		String geoLevelId = inputObject.getGeoLevelId();
		
		String userGeoLevelName = "";
		String userGeoLevelId = "";
		String userGeoLevelInternalId = "";
		String parentGeoLevelName = "";
		String parentGeoLevelId="";
		String parentGeoLevelInternalId = "";
		String childGeoLevelName="";
		List<String> childGeoLevelIds = new ArrayList<String>();
		List<String> childGeoLevelInternalIds = new ArrayList<String>();
		
		String[] parts = geoLevelId.split("-");
		
		GenericGeo rootGeo = new GenericGeo(); //dummy
		
		List<AreaGeo> areas = itgDashboardDao.getITGGeoMappingForUser(customerCode, userId, rootGeo);
		
		switch(geoLevel) {
			case "Territory": 
				userGeoLevelName="Territory";
				userGeoLevelId = parts[4];
				userGeoLevelInternalId = geoLevelId;
				parentGeoLevelName="Division";
				parentGeoLevelId = parts[3];
				parentGeoLevelInternalId = parts[0]+"-"+parts[1]+"-"+parts[2]+"-"+parts[3];
				break;
			case "Division": 
				userGeoLevelName="Division";
				userGeoLevelId = parts[3];
				userGeoLevelInternalId = geoLevelId;
				parentGeoLevelName="Region";
				parentGeoLevelId = parts[2];
				parentGeoLevelInternalId = parts[0]+"-"+parts[1]+"-"+parts[2];
				childGeoLevelName="Territory";
				for(AreaGeo area : areas ) {
					if ( parts[1].equals(area.getAreaId())) {
						for(RegionGeo region : area.getRegions() ) {
							if (parts[2].equals(region.getRegionId())) {
								for(DivisionGeo division : region.getDivisions()) {
									if ( parts[3].equals(division.getDivisionId()) ) {
										for(TerritoryGeo territory : division.getTerritories() ) {
											childGeoLevelInternalIds.add(parts[0]+"-"+parts[1]+"-"+parts[2]+"-"+parts[3]+"-"+territory.getTerritoryId());
											childGeoLevelIds.add(territory.getTerritoryId());
										}
										break;
									}
								}
								break;
							}
						}
						break;
					}
				}
				break;
			case "Region": 
				userGeoLevelName="Region";
				userGeoLevelId = parts[2];
				userGeoLevelInternalId = geoLevelId;
				parentGeoLevelName="Area";
				parentGeoLevelId = parts[1];
				parentGeoLevelInternalId = parts[0]+"-"+parts[1];
				childGeoLevelName="Division";
				for(AreaGeo area : areas ) {
					if ( parts[1].equals(area.getAreaId())) {
						for(RegionGeo region : area.getRegions() ) {
							if (parts[2].equals(region.getRegionId())) {
								for(DivisionGeo division : region.getDivisions()) {
									childGeoLevelIds.add(division.getDivisionId());
									childGeoLevelInternalIds.add(parts[0]+"-"+parts[1]+"-"+parts[2]+"-"+division.getDivisionId());
								}
								break;
							}
						}
						break;
					}
				}
				break;
			case "Area": 
				userGeoLevelName="Area";
				userGeoLevelId = geoLevelId;
				userGeoLevelInternalId = geoLevelId;
				parentGeoLevelName="Country";
				parentGeoLevelId = parts[0];
				parentGeoLevelInternalId = parts[0];
				childGeoLevelName="Region";
				for(AreaGeo area : areas ) {
					if ( parts[1].equals(area.getAreaId())) {
						for(RegionGeo region : area.getRegions() ) {
							childGeoLevelIds.add(region.getRegionId());
							childGeoLevelInternalIds.add(parts[0]+"-"+parts[1]+"-"+region.getRegionId());
						}
						break;
					}
				}
				break;
			case "Country": 
				userGeoLevelName="Country";
				userGeoLevelId = geoLevelId;
				userGeoLevelInternalId = parts[0];
				parentGeoLevelName="Country";
				parentGeoLevelId = parts[0];
				parentGeoLevelInternalId = parts[0];
				childGeoLevelName="Area";
				for(AreaGeo area : areas ) {
					childGeoLevelIds.add(area.getAreaId());
					childGeoLevelInternalIds.add(parts[0]+"-"+area.getAreaId());
				}
				break;
		}
		
		Map<String,Map<String,String>> userGeoDataMap = new LinkedHashMap<String,Map<String,String>>();
		Map<String,Map<String,String>> parentGeoDataMap = new LinkedHashMap<String,Map<String,String>>();
		Map<String,Map<String,String>> childGeoDataMap = new LinkedHashMap<String,Map<String,String>>();
		
		Map<String,Map<String,String>> userGeoPreviousDataMap = new LinkedHashMap<String,Map<String,String>>();
		Map<String,Map<String,String>> parentGeoPreviousDataMap = new LinkedHashMap<String,Map<String,String>>();
		Map<String,Map<String,String>> childGeoPreviousDataMap = new LinkedHashMap<String,Map<String,String>>();
		
		for(String storeType : storeTypes ) {
			userGeoDataMap.put(storeType, itgDashboardDao.getITGStats(projectId,userGeoLevelInternalId,timePeriodType,timePeriod,storeType));
			userGeoPreviousDataMap.put(storeType, itgDashboardDao.getITGStats(projectId,userGeoLevelInternalId,timePeriodType,previousTimePeriod,storeType));
			parentGeoDataMap.put(storeType, itgDashboardDao.getITGStats(projectId,parentGeoLevelInternalId,timePeriodType,timePeriod,storeType));
			parentGeoPreviousDataMap.put(storeType, itgDashboardDao.getITGStats(projectId,parentGeoLevelInternalId,timePeriodType,previousTimePeriod,storeType));
		}
		
		String storeCountForUserGeo = userGeoDataMap.get("ALL").get("STORE_COUNT");
		if ( StringUtils.isBlank(storeCountForUserGeo) || storeCountForUserGeo.equals("0") ) {
			return resultList; // no need to go further
		}
		
		for(String childGeoLevelInternalId : childGeoLevelInternalIds ) {
			childGeoDataMap.put(childGeoLevelInternalId, itgDashboardDao.getITGStats(projectId,childGeoLevelInternalId,timePeriodType,timePeriod,"ALL"));
			childGeoPreviousDataMap.put(childGeoLevelInternalId, itgDashboardDao.getITGStats(projectId,childGeoLevelInternalId,timePeriodType,previousTimePeriod,"ALL"));
		}
		
		List<Map<String,Object>> shareOfShelfData = new ArrayList<Map<String,Object>>();
		Map<String,Object> shareOfShelfGeoDataMap = new LinkedHashMap<String,Object>();
		List<Map<String,Object>> userGeoShareOfShelfData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> parentGeoShareOfShelfData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> childGeoShareOfShelfData = new ArrayList<Map<String,Object>>();
		
		List<Map<String,Object>> distributionData = new ArrayList<Map<String,Object>>();
		Map<String,Object> distributionGeoDataMap = new LinkedHashMap<String,Object>();
		List<Map<String,Object>> userGeoDistributionData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> parentGeoDistributionData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> childGeoDistributionData = new ArrayList<Map<String,Object>>();
		
		List<Map<String,Object>> promotionData = new ArrayList<Map<String,Object>>();
		Map<String,Object> promotionGeoDataMap = new LinkedHashMap<String,Object>();
		List<Map<String,Object>> userGeoPromotionData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> parentGeoPromotionData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> childGeoPromotionData = new ArrayList<Map<String,Object>>();
		
		List<Map<String,Object>> priceData = new ArrayList<Map<String,Object>>();
		Map<String,Object> priceGeoDataMap = new LinkedHashMap<String,Object>();
		List<Map<String,Object>> userGeoPriceData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> parentGeoPriceData = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> childGeoPriceData = new ArrayList<Map<String,Object>>();

		for(String storeType : storeTypes ) {
			Map<String,String> storeTypeDataMap =  userGeoDataMap.get(storeType);
			Map<String,String> storeTypePreviousDataMap =  userGeoPreviousDataMap.get(storeType);
			
			Map<String,Object> shareOfShelfStoreTypeDataMap = new HashMap<String,Object>();
			shareOfShelfStoreTypeDataMap.put("geoLevel", userGeoLevelName);
			shareOfShelfStoreTypeDataMap.put("geoLevelId", userGeoLevelId);
			shareOfShelfStoreTypeDataMap.put("geoLevelInternalId", userGeoLevelInternalId);
			shareOfShelfStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			shareOfShelfStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelf", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelfChange", 
					computeChange(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF"), storeTypePreviousDataMap.get("ITGB_SHARE_OF_SHELF"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("facingCompliance", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("facingComplianceChange", 
					computeChange(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"), storeTypePreviousDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShareChange", 
					computeChange(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"), storeTypePreviousDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShareChange", 
					computeChange(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"), storeTypePreviousDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> facings = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange", 
					computeChange(storeTypeDataMap.get(brandColumn), storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facings.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facings",facings);
			List<Map<String,String>> facingsInStoresCarryingBrand = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_STORE_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_STORE_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange", 
						computeChange(storeTypeDataMap.get(brandColumn), storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facingsInStoresCarryingBrand.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facingsInStoresCarryingBrand",facingsInStoresCarryingBrand);
			String score = ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("SCORE"));
			if ( score.contains(".") ) { score = score.substring(0,score.indexOf(".")); }
			shareOfShelfStoreTypeDataMap.put("score",score);
			shareOfShelfStoreTypeDataMap.put("scoreChange",
					computeChange(storeTypeDataMap.get("SCORE"),storeTypePreviousDataMap.get("SCORE"), ITG_VALUE_TYPE_ABS));
			userGeoShareOfShelfData.add(shareOfShelfStoreTypeDataMap);
			
			Map<String,Object> distributionStoreTypeDataMap = new HashMap<String,Object>();
			distributionStoreTypeDataMap.put("geoLevel", userGeoLevelName);
			distributionStoreTypeDataMap.put("geoLevelId", userGeoLevelId);
			distributionStoreTypeDataMap.put("geoLevelInternalId", userGeoLevelInternalId);
			distributionStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			distributionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			distributionStoreTypeDataMap.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_DISTRIBUTION")));
			distributionStoreTypeDataMap.put("distributionChange", 
					computeChange(storeTypeDataMap.get("ITGB_DISTRIBUTION"),storeTypePreviousDataMap.get("ITGB_DISTRIBUTION"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("oosPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("oosPercentageChange", 
					computeChange(storeTypeDataMap.get("OOS_PERCENTAGE"),storeTypePreviousDataMap.get("OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("itgbOOSPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("itgbOOSPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> distirbutions = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_DISTRIBUTION";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				distirbutions.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("itgBrandDistribution",distirbutions);
			List<Map<String,String>> competitionBrandDistribution = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_DISTRIBUTION";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionBrandDistribution.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("competitionBrandDistribution",competitionBrandDistribution);
			userGeoDistributionData.add(distributionStoreTypeDataMap);
			
			Map<String,Object> promotionStoreTypeDataMap = new HashMap<String,Object>();
			promotionStoreTypeDataMap.put("geoLevel", userGeoLevelName);
			promotionStoreTypeDataMap.put("geoLevelId", userGeoLevelId);
			promotionStoreTypeDataMap.put("geoLevelInternalId", userGeoLevelInternalId);
			promotionStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			promotionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("itgPromotionShareChange", 
					computeChange(storeTypeDataMap.get("ITGB_PROMOTION_SHARE"),storeTypePreviousDataMap.get("ITGB_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("competitionPromotionShareChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandPromotionalPricing",itgPromotionalPricingMap);
			List<Map<String,String>> competitionPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandPromotionalPricing",competitionPromotionalPricingMap);
			
			List<Map<String,String>> itgMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandMultipackPromotionalPricing",itgMultipackPromotionalPricingMap);
			List<Map<String,String>> competitionMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandMultipackPromotionalPricing",competitionMultipackPromotionalPricingMap);
			
			userGeoPromotionData.add(promotionStoreTypeDataMap);
			
			Map<String,Object> priceStoreTypeDataMap = new HashMap<String,Object>();
			priceStoreTypeDataMap.put("geoLevel", userGeoLevelName);
			priceStoreTypeDataMap.put("geoLevelId", userGeoLevelId);
			priceStoreTypeDataMap.put("geoLevelInternalId", userGeoLevelInternalId);
			priceStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			priceStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			priceStoreTypeDataMap.put("itgPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ITGB_PRICE")));
			priceStoreTypeDataMap.put("itgPriceChange", 
				computeChange(storeTypeDataMap.get("AVG_ITGB_PRICE"),storeTypePreviousDataMap.get("AVG_ITGB_PRICE"), ITG_VALUE_TYPE_PRICE));
			priceStoreTypeDataMap.put("competitionPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_COMPETITION_PRICE")));
			priceStoreTypeDataMap.put("competitionPriceChange", 
				computeChange(storeTypeDataMap.get("AVG_COMPETITION_PRICE"),storeTypePreviousDataMap.get("AVG_COMPETITION_PRICE"), ITG_VALUE_TYPE_PRICE));
			
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_PRICE";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				itgBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandPrices",itgBrandPrices);
			List<Map<String,String>> competitionBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_PRICE";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandPrices",competitionBrandPrices);
			
			List<Map<String,String>> itgBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_MP_PRICE_DIFF";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				itgBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandMPPriceDifference",itgBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> competitionBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_MP_PRICE_DIFF";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandMPPriceDifference",competitionBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> priceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> parityToPallMallMap = new HashMap<String,String>();
			parityToPallMallMap.put("name", "Maverick - Parity to Pall Mall");
			parityToPallMallMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE")));
			parityToPallMallMap.put("storePercentageChange", 
					computeChange(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(parityToPallMallMap);
			Map<String,String> aboveNewportMap = new HashMap<String,String>();
			aboveNewportMap.put("name", "Kool above Newport Menthol");
			aboveNewportMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE")));
			aboveNewportMap.put("storePercentageChange", 
					computeChange(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(aboveNewportMap);
			Map<String,String> belowMarlboroMap = new HashMap<String,String>();
			belowMarlboroMap.put("name", "Winston $ 0.15 below Marlboro SS");
			belowMarlboroMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE")));
			belowMarlboroMap.put("storePercentageChange", 
					computeChange(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(belowMarlboroMap);
			priceStoreTypeDataMap.put("priceGapGridData",priceGapGridData);
			
			List<Map<String,String>> absolutePriceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> maverickPallMallAbsolutePriceMap = new HashMap<String,String>();
			maverickPallMallAbsolutePriceMap.put("name", "Maverick - Pall Mall");
			maverickPallMallAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL")));
			maverickPallMallAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(maverickPallMallAbsolutePriceMap);
			Map<String,String> koolNewportAbsolutePriceMap = new HashMap<String,String>();
			koolNewportAbsolutePriceMap.put("name", "Kool - Newport Menthol");
			koolNewportAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL")));
			koolNewportAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(koolNewportAbsolutePriceMap);
			Map<String,String> winstonMarlboroAbsolutePriceMap = new HashMap<String,String>();
			winstonMarlboroAbsolutePriceMap.put("name", "Winston - Marlboro SS");
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS")));
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonMarlboroAbsolutePriceMap);
			Map<String,String> winstonCamelOriginalAbsolutePriceMap = new HashMap<String,String>();
			winstonCamelOriginalAbsolutePriceMap.put("name", "Winston - Camel Original");
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL")));
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGapChange",
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonCamelOriginalAbsolutePriceMap);
			
			priceStoreTypeDataMap.put("absolutePriceGapGridData",absolutePriceGapGridData);
			
			
			
			List<Map<String,Object>> priceGapChartData = new ArrayList<Map<String,Object>>();
			
			if ( storeType.equals("ALL") ) {
				Map<String,Object> maverickPallMallMap = new HashMap<String,Object>();
				maverickPallMallMap.put("pairName", "Maverick - Pall Mall");
				List<String> maverickPallMallStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_MAVERICK_PALLMALL_6"))
						);
				List<String> maverickPallMallAbsolutePriceStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_6")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_7")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL_8"))
						);
				maverickPallMallMap.put("byPriceGapPercentage", maverickPallMallStoreList);
				maverickPallMallMap.put("byAbsolutePriceGap", maverickPallMallAbsolutePriceStoreList);
				priceGapChartData.add(maverickPallMallMap);
				
				Map<String,Object> koolNewportMap = new HashMap<String,Object>();
				koolNewportMap.put("pairName", "Kool - Newport Menthol");
				List<String> koolNewportStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_KOOL_NEWPORT_MENTHOL_6"))
						);
				List<String> koolNewportAbsolutePriceStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_6")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_7")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL_8"))
						);
				koolNewportMap.put("byPriceGapPercentage", koolNewportStoreList);
				koolNewportMap.put("byAbsolutePriceGap", koolNewportAbsolutePriceStoreList);
				priceGapChartData.add(koolNewportMap);
				
				Map<String,Object> winstonMarlboroMap = new HashMap<String,Object>();
				winstonMarlboroMap.put("pairName", "Winston - Marlboro SS");
				List<String> winstonMarlboroStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_MARLBORO_SS_6"))
						);
				List<String> winstonMarlboroAbsolutePriceStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_6")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_7")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS_8"))
						);
				winstonMarlboroMap.put("byPriceGapPercentage", winstonMarlboroStoreList);
				winstonMarlboroMap.put("byAbsolutePriceGap", winstonMarlboroAbsolutePriceStoreList);
				priceGapChartData.add(winstonMarlboroMap);
				
				Map<String,Object> winstonCamelMap = new HashMap<String,Object>();
				winstonCamelMap.put("pairName", "Winston - Camel Original");
				List<String> winstonCamelStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("PRICE_GAP_WINSTON_CAMEL_ORIGINAL_6"))
						);
				List<String> winstonCamelAbsolutePriceStoreList = Arrays.asList(
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_0")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_1")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_2")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_3")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_4")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_5")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_6")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_7")),
						ConverterUtil.ifNullToZero(storeTypeDataMap.get("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL_8"))
						);
				winstonCamelMap.put("byPriceGapPercentage", winstonCamelStoreList);
				winstonCamelMap.put("byAbsolutePriceGap", winstonCamelAbsolutePriceStoreList);
				priceGapChartData.add(winstonCamelMap);
				
				priceStoreTypeDataMap.put("priceGapChartData",priceGapChartData);
			}
			
			userGeoPriceData.add(priceStoreTypeDataMap);
		}
		
		for(String storeType : storeTypes ) {
			Map<String,String> storeTypeDataMap =  parentGeoDataMap.get(storeType);
			Map<String,String> storeTypePreviousDataMap =  parentGeoPreviousDataMap.get(storeType);
			
			Map<String,Object> shareOfShelfStoreTypeDataMap = new HashMap<String,Object>();
			shareOfShelfStoreTypeDataMap.put("geoLevel", parentGeoLevelName);
			shareOfShelfStoreTypeDataMap.put("geoLevelId", parentGeoLevelId);
			shareOfShelfStoreTypeDataMap.put("geoLevelInternalId", parentGeoLevelInternalId);
			shareOfShelfStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			shareOfShelfStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelf", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelfChange",
					computeChange(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF"),storeTypePreviousDataMap.get("ITGB_SHARE_OF_SHELF"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("facingCompliance", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("facingComplianceChange",
					computeChange(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShareChange",
					computeChange(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShareChange",
					computeChange(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> facings = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange",
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facings.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facings",facings);
			List<Map<String,String>> facingsInStoresCarryingBrand = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_STORE_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_STORE_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange",
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facingsInStoresCarryingBrand.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facingsInStoresCarryingBrand",facingsInStoresCarryingBrand);
			String score = ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("SCORE"));
			if ( score.contains(".") ) { score = score.substring(0,score.indexOf(".")); }
			shareOfShelfStoreTypeDataMap.put("score",score);
			shareOfShelfStoreTypeDataMap.put("scoreChange",
					computeChange(storeTypeDataMap.get("SCORE"),storeTypePreviousDataMap.get("SCORE"), ITG_VALUE_TYPE_ABS));
			parentGeoShareOfShelfData.add(shareOfShelfStoreTypeDataMap);
			
			Map<String,Object> distributionStoreTypeDataMap = new HashMap<String,Object>();
			distributionStoreTypeDataMap.put("geoLevel", parentGeoLevelName);
			distributionStoreTypeDataMap.put("geoLevelId", parentGeoLevelId);
			distributionStoreTypeDataMap.put("geoLevelInternalId", parentGeoLevelInternalId);
			distributionStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			distributionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			distributionStoreTypeDataMap.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_DISTRIBUTION")));
			distributionStoreTypeDataMap.put("distributionChange",
					computeChange(storeTypeDataMap.get("ITGB_DISTRIBUTION"),storeTypePreviousDataMap.get("ITGB_DISTRIBUTION"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("oosPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("oosPercentageChange",
					computeChange(storeTypeDataMap.get("OOS_PERCENTAGE"),storeTypePreviousDataMap.get("OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("itgbOOSPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("itgbOOSPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> distirbutions = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_DISTRIBUTION";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				distirbutions.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("itgBrandDistribution",distirbutions);
			List<Map<String,String>> competitionBrandDistribution = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_DISTRIBUTION";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionBrandDistribution.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("competitionBrandDistribution",competitionBrandDistribution);
			parentGeoDistributionData.add(distributionStoreTypeDataMap);
			
			Map<String,Object> promotionStoreTypeDataMap = new HashMap<String,Object>();
			promotionStoreTypeDataMap.put("geoLevel", parentGeoLevelName);
			promotionStoreTypeDataMap.put("geoLevelId", parentGeoLevelId);
			promotionStoreTypeDataMap.put("geoLevelInternalId", parentGeoLevelInternalId);
			promotionStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			promotionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentageChange",
					computeChange(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentageChange",
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("itgPromotionShareChange",
					computeChange(storeTypeDataMap.get("ITGB_PROMOTION_SHARE"),storeTypePreviousDataMap.get("ITGB_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("competitionPromotionShareChange",
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentageChange",
					computeChange(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentageChange",
					computeChange(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandPromotionalPricing",itgPromotionalPricingMap);
			List<Map<String,String>> competitionPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandPromotionalPricing",competitionPromotionalPricingMap);
			
			List<Map<String,String>> itgMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandMultipackPromotionalPricing",itgMultipackPromotionalPricingMap);
			List<Map<String,String>> competitionMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandMultipackPromotionalPricing",competitionMultipackPromotionalPricingMap);
			
			parentGeoPromotionData.add(promotionStoreTypeDataMap);
			
			Map<String,Object> priceStoreTypeDataMap = new HashMap<String,Object>();
			priceStoreTypeDataMap.put("geoLevel", parentGeoLevelName);
			priceStoreTypeDataMap.put("geoLevelId", parentGeoLevelId);
			priceStoreTypeDataMap.put("geoLevelInternalId", parentGeoLevelInternalId);
			priceStoreTypeDataMap.put("storeType", storeTypeToStoreTypeDisplayNameMap.get(storeType));
			priceStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			priceStoreTypeDataMap.put("itgPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ITGB_PRICE")));
			priceStoreTypeDataMap.put("itgPriceChange",
					computeChange(storeTypeDataMap.get("AVG_ITGB_PRICE"),storeTypePreviousDataMap.get("AVG_ITGB_PRICE"), ITG_VALUE_TYPE_PRICE));
			priceStoreTypeDataMap.put("competitionPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_COMPETITION_PRICE")));
			priceStoreTypeDataMap.put("competitionPriceChange", 
					computeChange(storeTypeDataMap.get("AVG_COMPETITION_PRICE"),storeTypePreviousDataMap.get("AVG_COMPETITION_PRICE"), ITG_VALUE_TYPE_PRICE));
			
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_PRICE";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				itgBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandPrices",itgBrandPrices);
			
			List<Map<String,String>> competitionBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_PRICE";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandPrices",competitionBrandPrices);
			
			List<Map<String,String>> itgBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_MP_PRICE_DIFF";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				itgBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandMPPriceDifference",itgBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> competitionBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_MP_PRICE_DIFF";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandMPPriceDifference",competitionBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> priceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> parityToPallMallMap = new HashMap<String,String>();
			parityToPallMallMap.put("name", "Maverick - Parity to Pall Mall");
			parityToPallMallMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE")));
			parityToPallMallMap.put("storePercentageChange", 
					computeChange(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(parityToPallMallMap);
			Map<String,String> aboveNewportMap = new HashMap<String,String>();
			aboveNewportMap.put("name", "Kool above Newport Menthol");
			aboveNewportMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE")));
			aboveNewportMap.put("storePercentageChange",
					computeChange(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(aboveNewportMap);
			Map<String,String> belowMarlboroMap = new HashMap<String,String>();
			belowMarlboroMap.put("name", "Winston $ 0.15 below Marlboro SS");
			belowMarlboroMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE")));
			belowMarlboroMap.put("storePercentageChange", 
					computeChange(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(belowMarlboroMap);
			priceStoreTypeDataMap.put("priceGapGridData",priceGapGridData);
			
			List<Map<String,String>> absolutePriceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> maverickPallMallAbsolutePriceMap = new HashMap<String,String>();
			maverickPallMallAbsolutePriceMap.put("name", "Maverick - Pall Mall");
			maverickPallMallAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL")));
			maverickPallMallAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(maverickPallMallAbsolutePriceMap);
			Map<String,String> koolNewportAbsolutePriceMap = new HashMap<String,String>();
			koolNewportAbsolutePriceMap.put("name", "Kool - Newport Menthol");
			koolNewportAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL")));
			koolNewportAbsolutePriceMap.put("absolutePriceGapChange",
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(koolNewportAbsolutePriceMap);
			Map<String,String> winstonMarlboroAbsolutePriceMap = new HashMap<String,String>();
			winstonMarlboroAbsolutePriceMap.put("name", "Winston - Marlboro SS");
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS")));
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonMarlboroAbsolutePriceMap);
			Map<String,String> winstonCamelOriginalAbsolutePriceMap = new HashMap<String,String>();
			winstonCamelOriginalAbsolutePriceMap.put("name", "Winston - Camel Original");
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL")));
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonCamelOriginalAbsolutePriceMap);
			
			priceStoreTypeDataMap.put("absolutePriceGapGridData",absolutePriceGapGridData);
			
			parentGeoPriceData.add(priceStoreTypeDataMap);
			
		}
		
		for(String childGeoLevelInternalId : childGeoDataMap.keySet() ) {
			Map<String,String> storeTypeDataMap =  childGeoDataMap.get(childGeoLevelInternalId);
			Map<String,String> storeTypePreviousDataMap =  childGeoPreviousDataMap.get(childGeoLevelInternalId);
			
			Map<String,Object> shareOfShelfStoreTypeDataMap = new HashMap<String,Object>();
			shareOfShelfStoreTypeDataMap.put("geoLevel", childGeoLevelName);
			shareOfShelfStoreTypeDataMap.put("geoLevelId", childGeoLevelInternalId.substring(childGeoLevelInternalId.lastIndexOf("-")+1,childGeoLevelInternalId.length()));
			shareOfShelfStoreTypeDataMap.put("geoLevelInternalId",childGeoLevelInternalId);
			shareOfShelfStoreTypeDataMap.put("storeType", "ALL STORES");
			shareOfShelfStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelf", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF")));
			shareOfShelfStoreTypeDataMap.put("shareOfShelfChange",
					computeChange(storeTypeDataMap.get("ITGB_SHARE_OF_SHELF"),storeTypePreviousDataMap.get("ITGB_SHARE_OF_SHELF"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("facingCompliance", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("facingComplianceChange",
					computeChange(storeTypeDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("FACING_COMPLIANT_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveVolumeShareChange",
					computeChange(storeTypeDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ABOVE_VOLUME_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE")));
			shareOfShelfStoreTypeDataMap.put("aboveMarketShareChange",
					computeChange(storeTypeDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ABOVE_MARKET_SHARE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> facings = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facings.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facings",facings);
			List<Map<String,String>> facingsInStoresCarryingBrand = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_STORE_FACING_COUNT";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_STORE_FACING_COUNT"; }
				oneBrand.put("facings", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("facingsChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				facingsInStoresCarryingBrand.add(oneBrand);
			}
			shareOfShelfStoreTypeDataMap.put("facingsInStoresCarryingBrand",facingsInStoresCarryingBrand);
			String score = ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("SCORE"));
			if ( score.contains(".") ) { score = score.substring(0,score.indexOf(".")); }
			shareOfShelfStoreTypeDataMap.put("score",score);
			shareOfShelfStoreTypeDataMap.put("scoreChange",
					computeChange(storeTypeDataMap.get("SCORE"),storeTypePreviousDataMap.get("SCORE"), ITG_VALUE_TYPE_ABS));
			childGeoShareOfShelfData.add(shareOfShelfStoreTypeDataMap);
			
			Map<String,Object> distributionStoreTypeDataMap = new HashMap<String,Object>();
			distributionStoreTypeDataMap.put("geoLevel", childGeoLevelName);
			distributionStoreTypeDataMap.put("geoLevelId", childGeoLevelInternalId.substring(childGeoLevelInternalId.lastIndexOf("-")+1,childGeoLevelInternalId.length()));
			distributionStoreTypeDataMap.put("geoLevelInternalId", childGeoLevelInternalId);
			distributionStoreTypeDataMap.put("storeType", "ALL STORES");
			distributionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			distributionStoreTypeDataMap.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_DISTRIBUTION")));
			distributionStoreTypeDataMap.put("distributionChange", 
					computeChange(storeTypeDataMap.get("ITGB_DISTRIBUTION"),storeTypePreviousDataMap.get("ITGB_DISTRIBUTION"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("oosPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("oosPercentageChange", 
					computeChange(storeTypeDataMap.get("OOS_PERCENTAGE"),storeTypePreviousDataMap.get("OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			distributionStoreTypeDataMap.put("itgbOOSPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE")));
			distributionStoreTypeDataMap.put("itgbOOSPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_OOS_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_OOS_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			List<Map<String,String>> distirbutions = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_DISTRIBUTION";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				distirbutions.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("itgBrandDistribution",distirbutions);
			List<Map<String,String>> competitionBrandDistribution = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_DISTRIBUTION";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_DISTRIBUTION"; }
				oneBrand.put("distribution", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("distributionChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionBrandDistribution.add(oneBrand);
			}
			distributionStoreTypeDataMap.put("competitionBrandDistribution",competitionBrandDistribution);
			childGeoDistributionData.add(distributionStoreTypeDataMap);
			
			Map<String,Object> promotionStoreTypeDataMap = new HashMap<String,Object>();
			promotionStoreTypeDataMap.put("geoLevel", childGeoLevelName);
			promotionStoreTypeDataMap.put("geoLevelId", childGeoLevelInternalId.substring(childGeoLevelInternalId.lastIndexOf("-")+1,childGeoLevelInternalId.length()));
			promotionStoreTypeDataMap.put("geoLevelInternalId", childGeoLevelInternalId);
			promotionStoreTypeDataMap.put("storeType", "ALL STORES");
			promotionStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionAnyPromotedStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_SMP_OR_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgPromotedStoresPercentageChange",
					computeChange(storeTypeDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionPromotedStoresPercentageChange",
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTED_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("itgPromotionShareChange",
					computeChange(storeTypeDataMap.get("ITGB_PROMOTION_SHARE"),storeTypePreviousDataMap.get("ITGB_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionPromotionShare", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE")));
			promotionStoreTypeDataMap.put("competitionPromotionShareChange",
					computeChange(storeTypeDataMap.get("COMPETITION_PROMOTION_SHARE"),storeTypePreviousDataMap.get("COMPETITION_PROMOTION_SHARE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("itgMPPStoresPercentageChange",
					computeChange(storeTypeDataMap.get("ITGB_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE")));
			promotionStoreTypeDataMap.put("competitionMPPStoresPercentageChange",
					computeChange(storeTypeDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_MPP_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandPromotionalPricing",itgPromotionalPricingMap);
			List<Map<String,String>> competitionPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_PROMOTED_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_PROMOTED_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandPromotionalPricing",competitionPromotionalPricingMap);
			
			List<Map<String,String>> itgMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase()+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("USA Gold") ) { brandColumn = "USAG_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
					computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				itgMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("itgBrandMultipackPromotionalPricing",itgMultipackPromotionalPricingMap);
			List<Map<String,String>> competitionMultipackPromotionalPricingMap = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = brand.toUpperCase().replace(" ","_")+"_MP_PRICE_STORE_PERCENTAGE";
				if (brand.equals("Pall Mall") ) { brandColumn = "PALLMALL_MP_PRICE_STORE_PERCENTAGE"; }
				oneBrand.put("percentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("percentageChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PERCENTAGE));
				competitionMultipackPromotionalPricingMap.add(oneBrand);
			}
			promotionStoreTypeDataMap.put("competitionBrandMultipackPromotionalPricing",competitionMultipackPromotionalPricingMap);
			
			childGeoPromotionData.add(promotionStoreTypeDataMap);
			
			Map<String,Object> priceStoreTypeDataMap = new HashMap<String,Object>();
			priceStoreTypeDataMap.put("geoLevel", childGeoLevelName);
			priceStoreTypeDataMap.put("geoLevelId", childGeoLevelInternalId.substring(childGeoLevelInternalId.lastIndexOf("-")+1,childGeoLevelInternalId.length()));
			priceStoreTypeDataMap.put("geoLevelInternalId", childGeoLevelInternalId);
			priceStoreTypeDataMap.put("storeType", "ALL STORES");
			priceStoreTypeDataMap.put("storeCount", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("STORE_COUNT")));
			priceStoreTypeDataMap.put("itgPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ITGB_PRICE")));
			priceStoreTypeDataMap.put("itgPriceChange",
					computeChange(storeTypeDataMap.get("AVG_ITGB_PRICE"),storeTypePreviousDataMap.get("AVG_ITGB_PRICE"), ITG_VALUE_TYPE_PRICE));
			priceStoreTypeDataMap.put("competitionPrice", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_COMPETITION_PRICE")));
			priceStoreTypeDataMap.put("competitionPriceChange", 
					computeChange(storeTypeDataMap.get("AVG_COMPETITION_PRICE"),storeTypePreviousDataMap.get("AVG_COMPETITION_PRICE"), ITG_VALUE_TYPE_PRICE));
			
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("itgWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("ITGB_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE")));
			priceStoreTypeDataMap.put("competitionWithPriceStoresPercentageChange", 
					computeChange(storeTypeDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("COMPETITION_WITH_PRICE_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			
			List<Map<String,String>> itgBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_PRICE";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE)); 
				itgBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandPrices",itgBrandPrices);
			List<Map<String,String>> competitionBrandPrices = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_PRICE";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_PRICE"; }
				oneBrand.put("price", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceChange", 
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPrices.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandPrices",competitionBrandPrices);
			
			List<Map<String,String>> itgBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : itgBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase()+"_MP_PRICE_DIFF";
				if (brand.equals("USA Gold") ) { brandColumn = "AVG_USAG_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				itgBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("itgBrandMPPriceDifference",itgBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> competitionBrandPriceDifferenceWithMPP = new ArrayList<Map<String,String>>();
			for(String brand : competitionBrands) {
				Map<String,String> oneBrand = new HashMap<String,String>();
				oneBrand.put("brandName", brand);
				String brandColumn = "AVG_"+brand.toUpperCase().replace(" ","_")+"_MP_PRICE_DIFF";
				if (brand.equals("Pall Mall") ) { brandColumn = "AVG_PALLMALL_MP_PRICE_DIFF"; }
				oneBrand.put("priceDifference", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get(brandColumn)));
				oneBrand.put("priceDifferenceChange",
						computeChange(storeTypeDataMap.get(brandColumn),storeTypePreviousDataMap.get(brandColumn), ITG_VALUE_TYPE_PRICE));
				competitionBrandPriceDifferenceWithMPP.add(oneBrand);
			}
			priceStoreTypeDataMap.put("competitionBrandMPPriceDifference",competitionBrandPriceDifferenceWithMPP);
			
			List<Map<String,String>> priceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> parityToPallMallMap = new HashMap<String,String>();
			parityToPallMallMap.put("name", "Maverick - Parity to Pall Mall");
			parityToPallMallMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE")));
			parityToPallMallMap.put("storePercentageChange",
					computeChange(storeTypeDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_PARITY_TO_PALLMALL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(parityToPallMallMap);
			Map<String,String> aboveNewportMap = new HashMap<String,String>();
			aboveNewportMap.put("name", "Kool above Newport Menthol");
			aboveNewportMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE")));
			aboveNewportMap.put("storePercentageChange",
					computeChange(storeTypeDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(aboveNewportMap);
			Map<String,String> belowMarlboroMap = new HashMap<String,String>();
			belowMarlboroMap.put("name", "Winston $ 0.15 below Marlboro SS");
			belowMarlboroMap.put("storePercentage", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE")));
			belowMarlboroMap.put("storePercentageChange",
					computeChange(storeTypeDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"),storeTypePreviousDataMap.get("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS_STORE_PERCENTAGE"), ITG_VALUE_TYPE_PERCENTAGE));
			priceGapGridData.add(belowMarlboroMap);
			priceStoreTypeDataMap.put("priceGapGridData",priceGapGridData);

			List<Map<String,String>> absolutePriceGapGridData = new ArrayList<Map<String,String>>();
			Map<String,String> maverickPallMallAbsolutePriceMap = new HashMap<String,String>();
			maverickPallMallAbsolutePriceMap.put("name", "Maverick - Pall Mall");
			maverickPallMallAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL")));
			maverickPallMallAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(maverickPallMallAbsolutePriceMap);
			Map<String,String> koolNewportAbsolutePriceMap = new HashMap<String,String>();
			koolNewportAbsolutePriceMap.put("name", "Kool - Newport Menthol");
			koolNewportAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL")));
			koolNewportAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(koolNewportAbsolutePriceMap);
			Map<String,String> winstonMarlboroAbsolutePriceMap = new HashMap<String,String>();
			winstonMarlboroAbsolutePriceMap.put("name", "Winston - Marlboro SS");
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS")));
			winstonMarlboroAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonMarlboroAbsolutePriceMap);
			Map<String,String> winstonCamelOriginalAbsolutePriceMap = new HashMap<String,String>();
			winstonCamelOriginalAbsolutePriceMap.put("name", "Winston - Camel Original");
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGap", ConverterUtil.ifNullToEmpty(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL")));
			winstonCamelOriginalAbsolutePriceMap.put("absolutePriceGapChange", 
					computeChange(storeTypeDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"),storeTypePreviousDataMap.get("AVG_ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL"), ITG_VALUE_TYPE_PRICE));
			absolutePriceGapGridData.add(winstonCamelOriginalAbsolutePriceMap);
			
			priceStoreTypeDataMap.put("absolutePriceGapGridData",absolutePriceGapGridData);
			
			childGeoPriceData.add(priceStoreTypeDataMap);
		}
		
		shareOfShelfGeoDataMap.put("userGeo",userGeoShareOfShelfData);
		shareOfShelfGeoDataMap.put("parentGeo",parentGeoShareOfShelfData);
		shareOfShelfGeoDataMap.put("childGeo",childGeoShareOfShelfData);
		shareOfShelfData.add(shareOfShelfGeoDataMap);
		
		distributionGeoDataMap.put("userGeo",userGeoDistributionData);
		distributionGeoDataMap.put("parentGeo",parentGeoDistributionData);
		distributionGeoDataMap.put("childGeo",childGeoDistributionData);
		distributionData.add(distributionGeoDataMap);
		
		promotionGeoDataMap.put("userGeo",userGeoPromotionData);
		promotionGeoDataMap.put("parentGeo",parentGeoPromotionData);
		promotionGeoDataMap.put("childGeo",childGeoPromotionData);
		promotionData.add(promotionGeoDataMap);
		
		priceGeoDataMap.put("userGeo",userGeoPriceData);
		priceGeoDataMap.put("parentGeo",parentGeoPriceData);
		priceGeoDataMap.put("childGeo",childGeoPriceData);
		priceData.add(priceGeoDataMap);
		
		result.put("shareOfShelfData", shareOfShelfData);
		result.put("distributionData", distributionData);
		result.put("priceAnalysisData", priceData);
		result.put("promotionData", promotionData);
		
		resultList.add(result);
		
		LOGGER.info("---------------ITGDashboardServiceImpl Ends getITGStats----------------\n");
		
		return resultList;
	}
	
	private String computeChange(String current, String previous, String valueType) {
		String changeValue = "";
		if ( StringUtils.isNotBlank(current) && StringUtils.isNotBlank(previous) ) {
			Double currentVal = Double.parseDouble(current);
			Double previousVal = Double.parseDouble(previous);
			Double changeVal = currentVal - previousVal;
			if ( changeVal > 0 ) { changeValue = "+"; };
			switch(valueType) {
			case ITG_VALUE_TYPE_PERCENTAGE :
				changeValue = changeValue + ITG_PERCENTAGE_FORMATTER.format(changeVal);
				break;
			case ITG_VALUE_TYPE_PRICE :
				changeValue = changeValue + ITG_PRICE_FORMATTER.format(changeVal);
				break;
			case ITG_VALUE_TYPE_ABS :
				changeValue = changeValue + ITG_ABS_FORMATTER.format(changeVal);
				break;
			default:
				changeValue = ""+changeVal;
			}
		}
		return changeValue;
	}

	private String getPreviousTimePeriod(String timePeriod, String timePeriodType) {
		String previousTimePeriod = "";
		String requestedYear = timePeriod.substring(0, 4);
		switch(timePeriodType) {
		case "M":
			String requestedMonth = timePeriod.substring(4,6);
			LocalDate requestedMonthDate = LocalDate.of(Integer.parseInt(requestedYear), Integer.parseInt(requestedMonth), Integer.parseInt("1"));
			LocalDate previousMonthDate = requestedMonthDate.minusMonths(1);
			String previousMonth = ""+previousMonthDate.getMonthValue();
			if ( previousMonth.length() == 1 ) { previousMonth = "0"+previousMonth;}
			previousTimePeriod = previousMonthDate.getYear() + previousMonth;
			break;
		case "Q":
			String requestedQuarter = timePeriod.substring(5,6);
			int previousQuarter = Integer.parseInt(requestedQuarter) - 1;
			if( previousQuarter == 0 ) {
				previousTimePeriod = (Integer.parseInt(requestedYear) - 1) + "Q4";
			} else {
				previousTimePeriod = requestedYear + "Q" + previousQuarter;
			}
			break;
		case "Y":
			previousTimePeriod = "" + (Integer.parseInt(requestedYear) - 1);
			break;
		}
		
		
		return previousTimePeriod;
	}

	@Override
	public File getITGReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ITGDashboardServiceImpl Starts getITGReport----------------\n");

		List<LinkedHashMap<String,String>> storesDataList = itgDashboardDao.getITGStoresForReport(inputObject.getProjectId(), inputObject.getCustomerCode(), 
					inputObject.getGeoLevel(), inputObject.getGeoLevelId(),inputObject.getTimePeriodType(),inputObject.getTimePeriod());
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet metaSheet = workbook.createSheet("Meta Info");
		XSSFSheet storeSheet = workbook.createSheet("Report");
		workbook.setSheetOrder("Report", 0);
		workbook.setSheetOrder("Meta Info", 1);
		
		Row metaSheetHeaderRow = metaSheet.createRow(0);
		List<String> metaSheetHeaders = Arrays.asList(new String[] {
				"Generated By","Generated On","Geo Level","Geo Level ID","Time Period"});
		int colNum=0;
		for(String columnHeader : metaSheetHeaders ) {
			Cell cell = metaSheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		Row metaSheetDataRow = metaSheet.createRow(1);
		Cell cell = metaSheetDataRow.createCell(0);
		cell.setCellValue(inputObject.getUserId());
		cell = metaSheetDataRow.createCell(1);
		cell.setCellValue(Calendar.getInstance().getTime().toString());
		cell = metaSheetDataRow.createCell(2);
		cell.setCellValue(inputObject.getGeoLevel());
		cell = metaSheetDataRow.createCell(3);
		cell.setCellValue(inputObject.getGeoLevelId());
		cell = metaSheetDataRow.createCell(4);
		cell.setCellValue(inputObject.getTimePeriod());
		
		
		Map<String,String> storeSheetKeyValueMap = new LinkedHashMap<String,String>();
		storeSheetKeyValueMap.put("Store CRS","STORE_CRS");
		storeSheetKeyValueMap.put("Street","STREET");
		storeSheetKeyValueMap.put("City","CITY");
		storeSheetKeyValueMap.put("State","STATECODE");
		storeSheetKeyValueMap.put("A-R-D-T","GEO_MAPPING_ID");
		storeSheetKeyValueMap.put("Visit Month","TIMEPERIOD");
		storeSheetKeyValueMap.put("Store Type","STORE_PLAN");
		storeSheetKeyValueMap.put("Plan Facings","PLAN_FACING_COUNT");
		storeSheetKeyValueMap.put("Score","SCORE");
		storeSheetKeyValueMap.put("EDLP Store","EDLP_STATUS");
		storeSheetKeyValueMap.put("ITG Brand On-shelf Facings","ITGB_FACING_COUNT");
		storeSheetKeyValueMap.put("ITG Brand OOS Facings","ITGB_OOS_COUNT");
		storeSheetKeyValueMap.put("Total On-shelf Facings","TOTAL_FACING_COUNT");
		storeSheetKeyValueMap.put("Total OOS Facings","TOTAL_OOS_COUNT");
		storeSheetKeyValueMap.put("Facing Compliance","PLAN_FACING_COMPLIANCE");
		storeSheetKeyValueMap.put("ITG Brands Share of Shelf (%)","ITGB_SHARE_OF_SHELF");
		storeSheetKeyValueMap.put("ITG Brands Share of Facings (%)","ITGB_SHARE_OF_FACINGS");
		storeSheetKeyValueMap.put("ITG Brands OSA (%)","ITGB_OSA_PERCENTAGE");
		storeSheetKeyValueMap.put("OOS Percentage","OOS_PERCENTAGE");
		storeSheetKeyValueMap.put("ITG Brands Volume Share (SOM) (%)","VOLUME_SHARE");
		storeSheetKeyValueMap.put("Above Volume Share","ABOVE_VOLUME_SHARE");
		storeSheetKeyValueMap.put("Market Share","MARKET_SHARE");
		storeSheetKeyValueMap.put("Above Market Share","ABOVE_MARKET_SHARE");
		storeSheetKeyValueMap.put("Winston - Facing Count","WINSTON_FACING_COUNT");
		storeSheetKeyValueMap.put("Kool - Facing Count","KOOL_FACING_COUNT");
		storeSheetKeyValueMap.put("Maverick - Facing Count","MAVERICK_FACING_COUNT");
		storeSheetKeyValueMap.put("USA Gold - Facing Count","USAG_FACING_COUNT");
		storeSheetKeyValueMap.put("Salem - Facing Count","SALEM_FACING_COUNT");
		storeSheetKeyValueMap.put("Sonoma - Facing Count","SONOMA_FACING_COUNT");
		storeSheetKeyValueMap.put("Montclair - Facing Count","MONTCLAIR_FACING_COUNT");
		storeSheetKeyValueMap.put("Crowns - Facing Count","CROWNS_FACING_COUNT");
		storeSheetKeyValueMap.put("Fortuna - Facing Count","FORTUNA_FACING_COUNT");
		storeSheetKeyValueMap.put("Rave - Facing Count","RAVE_FACING_COUNT");
		storeSheetKeyValueMap.put("Marlboro Special Select - Facing Count","MARLBORO_SS_FACING_COUNT");
		storeSheetKeyValueMap.put("Camel Original - Facing Count","CAMEL_ORIGINAL_FACING_COUNT");
		storeSheetKeyValueMap.put("Pall Mall - Facing Count","PALLMALL_FACING_COUNT");
		storeSheetKeyValueMap.put("Newport Menthol - Facing Count","NEWPORT_MENTHOL_FACING_COUNT");
		storeSheetKeyValueMap.put("Eagle - Facing Count","EAGLE_FACING_COUNT");
		storeSheetKeyValueMap.put("Highest Price","HIGHEST_PRICE");
		storeSheetKeyValueMap.put("Highest Priced Brands","HIGHEST_PRICE_BRAND");
		storeSheetKeyValueMap.put("Lowest Price","LOWESET_PRICE");
		storeSheetKeyValueMap.put("Lowest Priced Brands","LOWEST_PRICE_BRAND");
		storeSheetKeyValueMap.put("Winston - Regular Price","WINSTON_PRICE");
		storeSheetKeyValueMap.put("Kool - Regular Price","KOOL_PRICE");
		storeSheetKeyValueMap.put("Maverick - Regular Price","MAVERICK_PRICE");
		storeSheetKeyValueMap.put("USA Gold - Regular Price","USAG_PRICE");
		storeSheetKeyValueMap.put("Salem - Regular Price","SALEM_PRICE");
		storeSheetKeyValueMap.put("Sonoma - Regular Price","SONOMA_PRICE");
		storeSheetKeyValueMap.put("Montclair - Regular Price","MONTCLAIR_PRICE");
		storeSheetKeyValueMap.put("Crowns - Regular Price","CROWNS_PRICE");
		storeSheetKeyValueMap.put("Fortuna - Regular Price","FORTUNA_PRICE");
		storeSheetKeyValueMap.put("Rave - Regular Price","RAVE_PRICE");
		storeSheetKeyValueMap.put("Marlboro Special Select - Regular Price","MARLBORO_SS_PRICE");
		storeSheetKeyValueMap.put("Camel Original - Regular Price","CAMEL_ORIGINAL_PRICE");
		storeSheetKeyValueMap.put("Pall Mall - Regular Price","PALLMALL_PRICE");
		storeSheetKeyValueMap.put("Newport Menthol - Regular Price","NEWPORT_MENTHOL_PRICE");
		storeSheetKeyValueMap.put("Eagle - Regular Price","EAGLE_PRICE");
		storeSheetKeyValueMap.put("Winston - Multipack Price","WINSTON_MP_PRICE");
		storeSheetKeyValueMap.put("Kool - Multipack Price","KOOL_MP_PRICE");
		storeSheetKeyValueMap.put("Maverick - Multipack Price","MAVERICK_MP_PRICE");
		storeSheetKeyValueMap.put("USA Gold - Multipack Price","USAG_MP_PRICE");
		storeSheetKeyValueMap.put("Salem - Multipack Price","SALEM_MP_PRICE");
		storeSheetKeyValueMap.put("Sonoma - Multipack Price","SONOMA_MP_PRICE");
		storeSheetKeyValueMap.put("Montclair - Multipack Price","MONTCLAIR_MP_PRICE");
		storeSheetKeyValueMap.put("Crowns - Multipack Price","CROWNS_MP_PRICE");
		storeSheetKeyValueMap.put("Fortuna - Multipack Price","FORTUNA_MP_PRICE");
		storeSheetKeyValueMap.put("Rave - Multipack Price","RAVE_MP_PRICE");
		storeSheetKeyValueMap.put("Marlboro Special Select - Multipack Price","MARLBORO_SS_MP_PRICE");
		storeSheetKeyValueMap.put("Camel Original - Multipack Price","CAMEL_ORIGINAL_MP_PRICE");
		storeSheetKeyValueMap.put("Pall Mall - Multipack Price","PALLMALL_MP_PRICE");
		storeSheetKeyValueMap.put("Newport Menthol - Multipack Price","NEWPORT_MENTHOL_MP_PRICE");
		storeSheetKeyValueMap.put("Eagle - Multipack Price","EAGLE_MP_PRICE");
		storeSheetKeyValueMap.put("Maverick - Pall Mall - Regular Price - Price Gap %","PRICE_GAP_MAVERICK_PALLMALL");
		storeSheetKeyValueMap.put("Maverick - Pall Mall - Regular Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL");
		storeSheetKeyValueMap.put("Winston - Camel Original - Regular Price - Price Gap %","PRICE_GAP_WINSTON_CAMEL_ORIGINAL");
		storeSheetKeyValueMap.put("Winston - Camel Original - Regular Price -  Absolute Price Gap","ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL");
		storeSheetKeyValueMap.put("Winston - Marlboro Special Select - Regular Price - Price Gap %","PRICE_GAP_WINSTON_MARLBORO_SS");
		storeSheetKeyValueMap.put("Winston - Marlboro Special Select - Regular Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS");
		storeSheetKeyValueMap.put("Kool - Newport Menthol - Regular Price - Price Gap %","PRICE_GAP_KOOL_NEWPORT_MENTHOL");
		storeSheetKeyValueMap.put("Kool - Newport Menthol - Regular Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL");
		storeSheetKeyValueMap.put("Maverick - Pall Mall - Multipack Price - Price Gap %","PRICE_GAP_MP_MAVERICK_PALLMALL");
		storeSheetKeyValueMap.put("Maverick - Pall Mall - Multipack Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_MP_MAVERICK_PALLMALL");
		storeSheetKeyValueMap.put("Winston - Camel Original - Multipack Price - Price Gap %","PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL");
		storeSheetKeyValueMap.put("Winston - Camel Original - Multipack Price -  Absolute Price Gap","ABSOLUTE_PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL");
		storeSheetKeyValueMap.put("Winston - Marlboro Special Select - Multipack Price - Price Gap %","PRICE_GAP_MP_WINSTON_MARLBORO_SS");
		storeSheetKeyValueMap.put("Winston - Marlboro Special Select - Multipack Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_MP_WINSTON_MARLBORO_SS");
		storeSheetKeyValueMap.put("Kool - Newport Menthol - Multipack Price - Price Gap %","PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL");
		storeSheetKeyValueMap.put("Kool - Newport Menthol - Multipack Price - Absolute Price Gap","ABSOLUTE_PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL");
		storeSheetKeyValueMap.put("ITG Brands Facings on Promo","ITGB_PROMOTED_FACING_COUNT");
		storeSheetKeyValueMap.put("Non ITG Facings on Promo","NON_ITGB_PROMOTED_FACING_COUNT");
		storeSheetKeyValueMap.put("% ITG Brands Facings on Promo","ITGB_PROMOTION_SOS_IN_ITGB_FACINGS");
		storeSheetKeyValueMap.put("ITG Brand Share of Promotion","ITGB_PROMOTION_SOS");
		storeSheetKeyValueMap.put("Winston - Promotional Facing Count","WINSTON_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Kool - Promotional Facing Count","KOOL_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Maverick - Promotional Facing Count","MAVERICK_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("USA Gold - Promotional Facing Count","USAG_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Salem - Promotional Facing Count","SALEM_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Sonoma - Promotional Facing Count","SONOMA_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Montclair - Promotional Facing Count","MONTCLAIR_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Crowns - Promotional Facing Count","CROWNS_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Fortuna - Promotional Facing Count","FORTUNA_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Rave - Promotional Facing Count","RAVE_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Marlboro Special Select - Promotional Facing Count","MARLBORO_SS_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Camel Original - Promotional Facing Count","CAMEL_ORIGINAL_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Pall Mall - Promotional Facing Count","PALLMALL_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Newport Menthol - Promotional Facing Count","NEWPORT_MENTHOL_PROMO_FACING_COUNT");
		storeSheetKeyValueMap.put("Eagle - Promotional Facing Count","EAGLE_PROMO_FACING_COUNT");
		
		Row storeSheetHeaderRow = storeSheet.createRow(0);		
		colNum=0;
		for(String columnHeader : storeSheetKeyValueMap.keySet() ) {
			Cell headerCell = storeSheetHeaderRow.createCell(colNum++);
			headerCell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			for (Map<String, String> row : storesDataList) {
				Row storeSheetRow = storeSheet.createRow(rowNum++);
				int storeSheetColNum = 0;
				for(String dbColumnName : storeSheetKeyValueMap.values()) {
					String value = ConverterUtil.ifNullToEmpty(row.get(dbColumnName));
					if ( dbColumnName.equals("GEO_MAPPING_ID") ) {
						value = value.replace("US-", "");
					}
					storeSheetRow.createCell(storeSheetColNum++).setCellValue(value);
				}
			}
		} catch (Exception e) {
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}

		LOGGER.info("---------------ITGDashboardServiceImpl Ends getITGReport----------------\n");
		
		//Now write the workbook to file and return
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}

		File f = new File(tempFilePath);
		return f;
	}
	
	@Override
	public List<GenericGeo> getITGGeoMappingForUser(InputObject inputObject) {
		LOGGER.info("---------------ITGDashboardServiceImpl Starts getITGGeoMappingForUser----------------\n");
		String customerCode = inputObject.getCustomerCode();
		String userId = inputObject.getUserId();
		
		GenericGeo countryGeo = new GenericGeo();
		countryGeo.setId("US");
		countryGeo.setChildGeoLevelType("area");
		countryGeo.setIsDefault(false); //will change to if country level user, in next call.

		List<AreaGeo> configuredGeos = itgDashboardDao.getITGGeoMappingForUser(customerCode, userId, countryGeo);

		List<GenericGeo> genericGeoConvertedList = new ArrayList<GenericGeo>();

		for(AreaGeo area : configuredGeos) {
			GenericGeo genericAreaGeo = new GenericGeo();
			genericAreaGeo.setIsDefault(area.getIsDefault());
			genericAreaGeo.setId(area.getAreaId());
			genericAreaGeo.setChildGeoLevelType("region");
			for(RegionGeo region : area.getRegions()) {
				GenericGeo genericRegionGeo = new GenericGeo();
				genericRegionGeo.setIsDefault(region.getIsDefault());
				genericRegionGeo.setId(region.getRegionId());
				genericRegionGeo.setChildGeoLevelType("division");
				for(DivisionGeo division : region.getDivisions()) {
					GenericGeo genericDivisionGeo = new GenericGeo();
					genericDivisionGeo.setIsDefault(division.getIsDefault());
					genericDivisionGeo.setId(division.getDivisionId());
					genericDivisionGeo.setChildGeoLevelType("territory");
					for(TerritoryGeo territory : division.getTerritories()) {
						GenericGeo genericTerritoryGeo = new GenericGeo();
						genericTerritoryGeo.setIsDefault(territory.getIsDefault());
						genericTerritoryGeo.setId(territory.getTerritoryId());
						genericDivisionGeo.getChildGeoLevels().add(genericTerritoryGeo);
					}
					genericRegionGeo.getChildGeoLevels().add(genericDivisionGeo);
				}
				genericAreaGeo.getChildGeoLevels().add(genericRegionGeo);
			}
			countryGeo.getChildGeoLevels().add(genericAreaGeo);
		}
		
		GenericGeo rootGeo = new GenericGeo();
		rootGeo.setId("root");
		rootGeo.setChildGeoLevelType("country");
		rootGeo.setIsDefault(false);
		rootGeo.setChildGeoLevels(Arrays.asList(countryGeo));
		
		genericGeoConvertedList.add(rootGeo);
		LOGGER.info("---------------ITGDashboardServiceImpl Ends getITGGeoMappingForUser----------------\n");

		return genericGeoConvertedList;
	}
}

package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ITGAggregationDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.util.Constants;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Anoop
 */
@Component(value = BeanMapper.BEAN_ITG_AGGREGATION_DAO)
@Scope("prototype")
public class ITGAggregationDaoImpl implements ITGAggregationDao {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.##");
	
	private final List<String> itgBrands = Arrays.asList("Winston","Kool","Maverick","USA Gold","Salem","Montclair","Sonoma","Crowns", "Fortuna", "Rave" );
	
	private final List<String> competitionPairs = Arrays.asList("Kool - Newport Menthol", "Winston - Marlboro Special Select","Winston - Camel Original",
			"Maverick - Pall Mall","USA Gold - Pall Mall","Montclair - Eagle","Sonoma - Eagle");
	
	private List<String> focusBrands = Arrays.asList("Winston","Kool","Maverick","USA Gold","Salem","Montclair","Sonoma","Crowns", "Fortuna", "Rave", 
			"Marlboro Special Select","Newport Menthol","Camel Original","Pall Mall","Eagle");
	
	private List<String> storeVisits = new ArrayList<String>();
	private Map<String,String> geoMappingIds = new HashMap<String,String>();
	private Map<String,String> planTypeMap = new HashMap<String,String>();
	private Map<String,String> volumeShareMap = new HashMap<String,String>();
	private Map<String,String> planFacingsMap = new HashMap<String,String>();
	private Map<String,String> marketShareMap = new HashMap<String,String>();
	private Map<String,String> storeVisitPreviewImageMap = new HashMap<String,String>();
	private Map<String,Map<String,String>> storeDetailsMap = new HashMap<String,Map<String,String>>();
	private List<String> edlpStores = new ArrayList<String>();
	
	String customerCode = null;
	int projectId = 0;
	String visitDateId = null;
    
	@Autowired
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	@Override
	public void runDailyAggregation(String customerCode, int projectId, String visitDateId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--runDailyAggregation::customerCode={},projectId={},visitDateId={}" , 
				customerCode, projectId, visitDateId);
		this.customerCode = customerCode;
		this.projectId = projectId;
		this.visitDateId = visitDateId;
		
		Connection conn = null;
		
		Statement insertStmt = null, deleteStmt = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			insertStmt = conn.createStatement();
			deleteStmt = conn.createStatement();
			
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Getting store visits to aggregate");
			storeVisits = getStoreVisits(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::no of stores to aggregate = {}", storeVisits.size());
			
			geoMappingIds = getGeoMappingIds(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched geomapping data");
			
			storeVisitPreviewImageMap = getStorePreviewImage(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched preview imageuuid data");
			
			storeDetailsMap = getStoreDetails(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched store details data");
			
			//Get actual facings by brand
			Map<String,Map<String,String>> actualFacings = getActualFacingsAllBrands(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched actualFacings");
			//Get ITG actual Facings
			Map<String,Integer> itgbActualFacings  = getStoreWiseITGBFacingCount(actualFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched itgb actual facings");
			//Get non ITG actual Facings
			Map<String,Integer> nonITGBActualFacings  = getStoreWiseNonITGBFacingCount(actualFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched non itgb actual facings");
			//Get total actual facings in store
			Map<String,Integer> overallActualFacings  = getStoreWiseFacingCount(actualFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched overall actual facings");
			
			
			//Get out of stock facings by brand
			Map<String,Map<String,String>> oosFacings = getOOSFacingsAllBrands(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched oosFacings");
			//Get ITG OOS Facings
			Map<String,Integer> itgbOOSFacings  = getStoreWiseITGBFacingCount(oosFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched itgb oos facings");
			//Get non ITG OOS Facings
			Map<String,Integer> nonITGBOOSFacings  = getStoreWiseNonITGBFacingCount(oosFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched non itgb oos facings");
			//Get total OOS facings in store
			Map<String,Integer> overallOOSFacings  = getStoreWiseFacingCount(oosFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched overall oos facings");
			
			//Get regular prices
			Map<String,Map<String,Double>> regularPrices = getRegularPriceAllBrands(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched regular prices");
			//Get MPP prices
			Map<String,Map<String,Double>> multipackPrices = getMultipackPriceAllBrands(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched multipack prices");
			//Get highest price per store
			Map<String, Map<String,String>> highestPrice = getHighestPrice(regularPrices, multipackPrices);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched highest price");
			//Get lowest price per store
			Map<String, Map<String,String>> lowestPrice = getLowestPrice(regularPrices, multipackPrices);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched lowest price");

			//Get SMP facings by brand
			Map<String,Map<String,String>> SMPFacings = getSMPFacingsAllBrands(conn);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched SMP facings");
			//Get ITGB SMP Facings
			Map<String,Integer> itgbSMPFacings  = getStoreWiseITGBFacingCount(SMPFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched itgb SMP facings");
			//Get non ITG SMP facings
			Map<String,Integer> nonitgbSMPFacings  = getStoreWiseNonITGBFacingCount(SMPFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched non itgb SMP facings");
			//Get total SMP facings in store
			Map<String,Integer> overallSMPFacings  = getStoreWiseFacingCount(SMPFacings);
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Fetched overall SMP facings");
			
			List<String> aggregationMetricNames = new ArrayList<String>();
			aggregationMetricNames.add("Store CRS");
			aggregationMetricNames.add("Store Plan Type");
			aggregationMetricNames.add("Store SOM");
			aggregationMetricNames.add("Store Visit Date");
			aggregationMetricNames.add("Store Fixture Size");
			aggregationMetricNames.add("Space Execution - Above Plan Facings");
			aggregationMetricNames.add("Space Execution - ITG Shelf Share");
			aggregationMetricNames.add("Space Execution - Above Dollar Share In Store");
			aggregationMetricNames.add("Space Execution - Above Dollar Share In Market");
			aggregationMetricNames.add("Pricing - Highest Price");
			aggregationMetricNames.add("Pricing - Highest Priced Brands");
			aggregationMetricNames.add("Pricing - Lowest Price");
			aggregationMetricNames.add("Pricing - Lowest Priced Brands");
			aggregationMetricNames.add("Pricing - EDLP Status");
			aggregationMetricNames.add("SMP - % SMP Facings In ITG Brand Facings");
			aggregationMetricNames.add("SMP - % SMP Facings In Non ITG Brand Facings");
			aggregationMetricNames.add("SMP - % ITG SMP Facings In Overall SMP Facings");
			aggregationMetricNames.add("OOS - % Out of Stock Facings");
			for(String pair : competitionPairs) {
				aggregationMetricNames.add("Price Gap % - Regular Price - " + pair);
			}
			for(String pair : competitionPairs) {
				aggregationMetricNames.add("Price Gap % - Multipack Price - " + pair);
			}
			for( String brand : focusBrands) {
				aggregationMetricNames.add("Facings - " + brand);
				aggregationMetricNames.add("Regular Price - " + brand);
				aggregationMetricNames.add("Multipack Price - " + brand);
				aggregationMetricNames.add("SMP Facings - " + brand);
			}
			
			String visitMonth = visitDateId.substring(0, 6);
			
			for(String storeVisitKey : storeVisits ) {
				
				LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation::Aggregating store visit = {}", storeVisitKey);
				
				Map<String,String> rowMap = initializeRowMap();
				
				rowMap.put("PROJECT_ID",""+projectId);
				
				String[] parts = storeVisitKey.split("#");
				
				String storeId = parts[0].replace(Constants.ITG_CUSTOMER_CODE+"_", "");
				
				rowMap.put("GEO_MAPPING_ID", geoMappingIds.get(parts[0]));
				
				String planType = planTypeMap.get(parts[0]);
				String planFacingCount = planFacingsMap.get(parts[0]);
				if ( planType == null || planType.equals("") ) {
					planType = "NA";
					planFacingCount = null;
				} 
				
				String effectivePlanType  = planType;
				if ( effectivePlanType.equals("NOT ON PLAN") || effectivePlanType.equals("FMC 85%") 
						|| effectivePlanType.equals("INACTIVE ACCOUNTS")  ) {
					effectivePlanType = "NA";
				}
				
				String volumeShare = volumeShareMap.get(parts[0]);
				if ( volumeShare == null || volumeShare.equals("") ) {
					volumeShare = "NA"; 
				}
				
				String marketShare = marketShareMap.get(parts[0]);
				if ( marketShare == null || marketShare.equals("") ) {
					marketShare = "NA"; 
				}
				
				if ( edlpStores.contains(parts[0]) ) {
					rowMap.put("EDLP_STATUS", "1");
				} else {
					rowMap.put("EDLP_STATUS", "0");
				}
				
				rowMap.put("TIMEPERIOD", visitMonth);
				
				rowMap.put("PREVIEW_IMAGEUUID",storeVisitPreviewImageMap.get(storeVisitKey));
				
				Map<String,String> details = storeDetailsMap.get(Constants.ITG_CUSTOMER_CODE+"_"+storeId);
				if ( details != null ) {
					rowMap.put("STREET", details.get("STREET"));
					rowMap.put("CITY", details.get("CITY"));
					rowMap.put("STATECODE", details.get("STATECODE"));
				}
				
				for(String metric:aggregationMetricNames ) {
					if ( metric.equals("Store CRS") ) {
						rowMap.put("STORE_ID", Constants.ITG_CUSTOMER_CODE+"_"+storeId);
						rowMap.put("STORE_CRS", storeId);
					} else if ( metric.equals("Store Plan Type") ) {
						rowMap.put("STORE_PLAN",planType);
						if ( !effectivePlanType.equals("NA") ) {
							rowMap.put("PLAN_FACING_COUNT",planFacingCount);
						}
					} else if ( metric.startsWith("Facings - ") ) {
						String brandName = metric.replace("Facings - ", "");
						String actualValue = "0", oosValue = "0";
						if ( actualFacings.get(storeVisitKey) != null ) {
							actualValue = actualFacings.get(storeVisitKey).get(brandName);
						}
						if ( oosFacings.get(storeVisitKey) != null ) {
							oosValue = actualFacings.get(storeVisitKey).get(brandName);
						}
						if ( actualValue == null ) { actualValue = "0"; }
						if ( oosValue == null ) { oosValue = "0"; }
						switch(brandName) {
							case "Winston": rowMap.put("WINSTON_FACING_COUNT", actualValue); rowMap.put("WINSTON_OOS_COUNT", oosValue); break;
							case "Kool": rowMap.put("KOOL_FACING_COUNT", actualValue); rowMap.put("KOOL_OOS_COUNT", oosValue); break;
							case "Maverick": rowMap.put("MAVERICK_FACING_COUNT", actualValue); rowMap.put("MAVERICK_OOS_COUNT", oosValue); break;
							case "USA Gold": rowMap.put("USAG_FACING_COUNT", actualValue); rowMap.put("USAG_OOS_COUNT", oosValue); break;
							case "Salem": rowMap.put("SALEM_FACING_COUNT", actualValue); rowMap.put("SALEM_OOS_COUNT", oosValue); break;
							case "Montclair": rowMap.put("MONTCLAIR_FACING_COUNT", actualValue); rowMap.put("MONTCLAIR_OOS_COUNT", oosValue); break;
							case "Sonoma": rowMap.put("SONOMA_FACING_COUNT", actualValue); rowMap.put("SONOMA_OOS_COUNT", oosValue); break;
							case "Crowns": rowMap.put("CROWNS_FACING_COUNT", actualValue); rowMap.put("CROWNS_OOS_COUNT", oosValue); break;
							case "Fortuna": rowMap.put("FORTUNA_FACING_COUNT", actualValue); rowMap.put("FORTUNA_OOS_COUNT", oosValue); break;
							case "Rave": rowMap.put("RAVE_FACING_COUNT", actualValue); rowMap.put("RAVE_OOS_COUNT", oosValue); break;
							case "Marlboro Special Select": rowMap.put("MARLBORO_SS_FACING_COUNT", actualValue); rowMap.put("MARLBORO_SS_OOS_COUNT", oosValue); break;
							case "Newport Menthol": rowMap.put("NEWPORT_MENTHOL_FACING_COUNT", actualValue); rowMap.put("NEWPORT_MENTHOL_OOS_COUNT", oosValue); break;
							case "Camel Original": rowMap.put("CAMEL_ORIGINAL_FACING_COUNT", actualValue); rowMap.put("CAMEL_ORIGINAL_OOS_COUNT", oosValue); break;
							case "Pall Mall": rowMap.put("PALLMALL_FACING_COUNT", actualValue); rowMap.put("PALLMALL_OOS_COUNT", oosValue); break;
							case "Eagle": rowMap.put("EAGLE_FACING_COUNT", actualValue); rowMap.put("EAGLE_OOS_COUNT", oosValue); break;
						}
					} else if ( metric.startsWith("Regular Price - ") ) {
						String brandName = metric.replace("Regular Price - ", "");
						String value = "NA";
						if ( regularPrices.get(storeVisitKey) != null ) {
							Double priceValue = regularPrices.get(storeVisitKey).get(brandName);
							if ( priceValue != null ) {
								value = priceValue.toString();
								switch(brandName) {
								case "Winston": rowMap.put("WINSTON_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Kool": rowMap.put("KOOL_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Maverick": rowMap.put("MAVERICK_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "USA Gold": rowMap.put("USAG_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Salem": rowMap.put("SALEM_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Montclair": rowMap.put("MONTCLAIR_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Sonoma": rowMap.put("SONOMA_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Crowns": rowMap.put("CROWNS_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Fortuna": rowMap.put("FORTUNA_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Rave": rowMap.put("RAVE_PRICE", value); rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1); break;
								case "Marlboro Special Select": rowMap.put("MARLBORO_SS_PRICE", value); rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1); break;
								case "Newport Menthol": rowMap.put("NEWPORT_MENTHOL_PRICE", value); rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1); break;
								case "Camel Original": rowMap.put("CAMEL_ORIGINAL_PRICE", value); rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1); break;
								case "Pall Mall": rowMap.put("PALLMALL_PRICE", value); rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1); break;
								case "Eagle": rowMap.put("EAGLE_PRICE", value); rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1); break;
								}
							}
						}
					} else if (metric.startsWith("Multipack Price - ")) {
						String brandName = metric.replace("Multipack Price - ", "");
						String value = "NA";
						if ( multipackPrices.get(storeVisitKey) != null ) {
							Double priceValue = multipackPrices.get(storeVisitKey).get(brandName);
							if ( priceValue != null ) {
								value = priceValue.toString();
								switch(brandName) {
								case "Winston": rowMap.put("WINSTON_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Kool": rowMap.put("KOOL_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Maverick": rowMap.put("MAVERICK_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "USA Gold": rowMap.put("USAG_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Salem": rowMap.put("SALEM_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Montclair": rowMap.put("MONTCLAIR_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Sonoma": rowMap.put("SONOMA_MP_PRICE", value);
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Crowns": rowMap.put("CROWNS_MP_PRICE", value);
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Fortuna": rowMap.put("FORTUNA_MP_PRICE", value);
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Rave": rowMap.put("RAVE_MP_PRICE", value); 
									rowMap.put("HAS_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_ITGB_MPP_FACINGS",""+1);break;
								case "Marlboro Special Select": rowMap.put("MARLBORO_SS_MP_PRICE", value); 
									rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_NON_ITGB_MPP_FACINGS",""+1); break;
								case "Newport Menthol": rowMap.put("NEWPORT_MENTHOL_MP_PRICE", value);
									rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_NON_ITGB_MPP_FACINGS",""+1); break;
								case "Camel Original": rowMap.put("CAMEL_ORIGINAL_MP_PRICE", value);
									rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_NON_ITGB_MPP_FACINGS",""+1); break;
								case "Pall Mall": rowMap.put("PALLMALL_MP_PRICE", value); 
									rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_NON_ITGB_MPP_FACINGS",""+1); break;
								case "Eagle": rowMap.put("EAGLE_MP_PRICE", value);
									rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",""+1);
									rowMap.put("HAS_NON_ITGB_MPP_FACINGS",""+1); break;
								}
							}
						}
					} else if ( metric.startsWith("SMP Facings - ") ) {
						String brandName = metric.replace("SMP Facings - ", "");
						String value = "0";
						if ( SMPFacings.get(storeVisitKey) != null ) {
							value = SMPFacings.get(storeVisitKey).get(brandName);
						}
						if ( value == null ) { value = "0"; }
						switch(brandName) {
						case "Winston": rowMap.put("WINSTON_PROMO_FACING_COUNT", value); break;
						case "Kool": rowMap.put("KOOL_PROMO_FACING_COUNT", value); break;
						case "Maverick": rowMap.put("MAVERICK_PROMO_FACING_COUNT", value); break;
						case "USA Gold": rowMap.put("USAG_PROMO_FACING_COUNT", value); break;
						case "Salem": rowMap.put("SALEM_PROMO_FACING_COUNT", value); break;
						case "Montclair": rowMap.put("MONTCLAIR_PROMO_FACING_COUNT", value); break;
						case "Sonoma": rowMap.put("SONOMA_PROMO_FACING_COUNT", value); break;
						case "Crowns": rowMap.put("CROWNS_PROMO_FACING_COUNT", value); break;
						case "Fortuna": rowMap.put("FORTUNA_PROMO_FACING_COUNT", value); break;
						case "Rave": rowMap.put("RAVE_PROMO_FACING_COUNT", value); break;
						case "Marlboro Special Select": rowMap.put("MARLBORO_SS_PROMO_FACING_COUNT", value); break;
						case "Newport Menthol": rowMap.put("NEWPORT_MENTHOL_PROMO_FACING_COUNT", value); break;
						case "Camel Original": rowMap.put("CAMEL_ORIGINAL_PROMO_FACING_COUNT", value); break;
						case "Pall Mall": rowMap.put("PALLMALL_PROMO_FACING_COUNT", value); break;
						case "Eagle": rowMap.put("EAGLE_PROMO_FACING_COUNT", value); break;
						}
					} 
					else if ( metric.startsWith("Space Execution - Above Plan Facings") ) {
						String value = "NA";
						Integer itgbFacingInStore = itgbActualFacings.get(storeVisitKey);
						if ( itgbFacingInStore == null ) { itgbFacingInStore = 0; }
						rowMap.put("ITGB_FACING_COUNT",""+itgbFacingInStore);
						Integer itgbOOSFacingInStore = itgbOOSFacings.get(storeVisitKey);
						if ( itgbOOSFacingInStore == null ) { itgbOOSFacingInStore = 0; }
						rowMap.put("ITGB_OOS_COUNT",""+itgbOOSFacingInStore);
						
						Integer itgbTotalFacingsInStore = itgbFacingInStore + itgbOOSFacingInStore;
						
						if (!effectivePlanType.equals("NA")) {
							if ( itgbTotalFacingsInStore == Integer.parseInt(planFacingCount) ) {
								value = "1";
							} else if ( itgbTotalFacingsInStore > Integer.parseInt(planFacingCount)) {
								value = "2";
							} else {
								value = "0";
							}
							rowMap.put("PLAN_FACING_COMPLIANCE",value);
						}
						
						Integer nonItgbActualFacingInStore = nonITGBActualFacings.get(storeVisitKey);
						if ( nonItgbActualFacingInStore == null ) { nonItgbActualFacingInStore = 0; }
						rowMap.put("NON_ITGB_FACING_COUNT",""+ nonItgbActualFacingInStore);
						Integer nonItgbOOSFacingInStore = nonITGBOOSFacings.get(storeVisitKey);
						if ( nonItgbOOSFacingInStore == null ) { nonItgbOOSFacingInStore = 0; }
						rowMap.put("NON_ITGB_OOS_COUNT",""+ nonItgbOOSFacingInStore);
						
					} else if ( metric.startsWith("Space Execution - ITG Shelf Share") ) {
						String value = "NA";
						Integer itgbActualFacingInStore = itgbActualFacings.get(storeVisitKey);
						if ( itgbActualFacingInStore == null ) { itgbActualFacingInStore = 0; }
						Integer overallActualFacingInStore = overallActualFacings.get(storeVisitKey);
						if ( overallActualFacingInStore == null ) { overallActualFacingInStore = 0; }
						rowMap.put("TOTAL_FACING_COUNT", ""+overallActualFacingInStore);
						if ( overallActualFacingInStore != 0 ) {
							Double shelfShare = ((double) itgbActualFacingInStore / overallActualFacingInStore) * 100;
							value = DECIMAL_FORMATTER.format(shelfShare);
							rowMap.put("ITGB_SHARE_OF_FACINGS", value);
						}
						
						Integer itgbOOSFacingInStore = itgbOOSFacings.get(storeVisitKey);
						if ( itgbOOSFacingInStore == null ) { itgbOOSFacingInStore = 0; }
						Integer overallOOSFacingInStore = overallOOSFacings.get(storeVisitKey);
						if ( overallOOSFacingInStore == null ) { overallOOSFacingInStore = 0; }
						rowMap.put("TOTAL_OOS_COUNT", ""+overallOOSFacingInStore);
						Integer totalFacingsIncludingOOS = overallActualFacingInStore + overallOOSFacingInStore;
						if ( totalFacingsIncludingOOS != 0 ) {
							Double shelfShare = ((double) (itgbActualFacingInStore+itgbOOSFacingInStore) / totalFacingsIncludingOOS) * 100;
							value = DECIMAL_FORMATTER.format(shelfShare);
							rowMap.put("ITGB_SHARE_OF_SHELF", value);
						}
						
						Integer itgbTotalFacingsInStore = itgbActualFacingInStore + itgbOOSFacingInStore;
						if ( itgbTotalFacingsInStore != 0 ) {
							Double shelfShare = ((double) itgbActualFacingInStore / itgbTotalFacingsInStore) * 100;
							value = DECIMAL_FORMATTER.format(shelfShare);
							rowMap.put("ITGB_OSA_PERCENTAGE", value);
						}
					} else if ( metric.startsWith("Space Execution - Above Dollar Share In Store") ) {
						String value = "NA";
						if ( !volumeShare.equals("NA") ) {
							Double volumeShareVal = Double.parseDouble(volumeShare);
							
							Integer itgbActualFacingInStore = itgbActualFacings.get(storeVisitKey);
							if ( itgbActualFacingInStore == null ) { itgbActualFacingInStore = 0; }
							Integer overallActualFacingInStore = overallActualFacings.get(storeVisitKey);
							if ( overallActualFacingInStore == null ) { overallActualFacingInStore = 0; }
							
							Integer itgbOOSFacingInStore = itgbOOSFacings.get(storeVisitKey);
							if ( itgbOOSFacingInStore == null ) { itgbOOSFacingInStore = 0; }
							Integer overallOOSFacingInStore = overallOOSFacings.get(storeVisitKey);
							if ( overallOOSFacingInStore == null ) { overallOOSFacingInStore = 0; }
							Integer totalFacingsIncludingOOS = overallActualFacingInStore + overallOOSFacingInStore;
							
							if ( totalFacingsIncludingOOS != 0 ) {
								Double shelfShare = ((double) (itgbActualFacingInStore+itgbOOSFacingInStore) / totalFacingsIncludingOOS) * 100;
								System.out.println("Dollar Share = " + volumeShareVal +", ITGB SoS = " + shelfShare);
								if ( shelfShare > volumeShareVal ) {
									value = "1";
								} else {
									value = "0";
								}
								rowMap.put("VOLUME_SHARE",volumeShare);
								rowMap.put("ABOVE_VOLUME_SHARE",value);
							}
						}
					} else if ( metric.startsWith("Space Execution - Above Dollar Share In Market") ) {
						String value = "NA";
						if ( !marketShare.equals("NA") ) {
							Double marketShareVal = Double.parseDouble(marketShare);
							
							Integer itgbActualFacingInStore = itgbActualFacings.get(storeVisitKey);
							if ( itgbActualFacingInStore == null ) { itgbActualFacingInStore = 0; }
							Integer overallActualFacingInStore = overallActualFacings.get(storeVisitKey);
							if ( overallActualFacingInStore == null ) { overallActualFacingInStore = 0; }
							
							Integer itgbOOSFacingInStore = itgbOOSFacings.get(storeVisitKey);
							if ( itgbOOSFacingInStore == null ) { itgbOOSFacingInStore = 0; }
							Integer overallOOSFacingInStore = overallOOSFacings.get(storeVisitKey);
							if ( overallOOSFacingInStore == null ) { overallOOSFacingInStore = 0; }
							Integer totalFacingsIncludingOOS = overallActualFacingInStore + overallOOSFacingInStore;
							
							if ( totalFacingsIncludingOOS != 0 ) {
								double shelfShare = ((double) (itgbActualFacingInStore+itgbOOSFacingInStore) / totalFacingsIncludingOOS) * 100;
								if ( shelfShare > marketShareVal ) {
									value = "1";
								} else {
									value = "0";
								}
								rowMap.put("MARKET_SHARE",marketShare);
								rowMap.put("ABOVE_MARKET_SHARE",value);
							}
						}
					}  else if ( metric.equals("Pricing - Highest Price")) {
						String highestPricePerStore = "NA";
						if ( highestPrice.get(storeVisitKey) != null ) {
							highestPricePerStore = highestPrice.get(storeVisitKey).get("highestPrice");
							rowMap.put("HIGHEST_PRICE",highestPricePerStore);
						}
					} else if ( metric.equals("Pricing - Highest Priced Brands")) {
						String highestPriceBrandsPerStore = "NA";
						if ( highestPrice.get(storeVisitKey) != null ) {
							highestPriceBrandsPerStore = highestPrice.get(storeVisitKey).get("highestPriceBrands");
							rowMap.put("HIGHEST_PRICE_BRAND",highestPriceBrandsPerStore);
						}
					} else if (metric.equals("Pricing - Lowest Price")) {
						String lowestPricePerStore = "NA";
						if ( lowestPrice.get(storeVisitKey) != null ) {
							lowestPricePerStore = lowestPrice.get(storeVisitKey).get("lowestPrice");
							rowMap.put("LOWESET_PRICE",lowestPricePerStore);
						}
					} else if (metric.equals("Pricing - Lowest Priced Brands")) {
						String lowestPriceBrandsPerStore = "NA";
						if ( lowestPrice.get(storeVisitKey) != null ) {
							lowestPriceBrandsPerStore = lowestPrice.get(storeVisitKey).get("lowestPriceBrands");
							rowMap.put("LOWEST_PRICE_BRAND",lowestPriceBrandsPerStore);
						}
					} else if (metric.equals("Pricing - EDLP Status")) {
						/*String value = null;
						if ( lowestPrice.get(storeVisitKey) != null ) {
							String lowestPriceBrandsPerStore = lowestPrice.get(storeVisitKey).get("lowestPriceBrands");
							if ( lowestPriceBrandsPerStore.contains("Pall Mall") ) {
								value = "1";
							} else {
								value = "0";
							}
						}
						rowMap.put("EDLP_STATUS", "1");*/
					} else if (metric.equals("SMP - % SMP Facings In ITG Brand Facings")) {
						String value = "NA";
						Integer itgbSMPFacingsPerStore = itgbSMPFacings.get(storeVisitKey);
						if ( itgbSMPFacingsPerStore == null ) { itgbSMPFacingsPerStore = 0; }
						
						rowMap.put("ITGB_PROMOTED_FACING_COUNT", ""+itgbSMPFacingsPerStore);
						
						Integer itgbFacingsPerStore = itgbActualFacings.get(storeVisitKey);
						if ( itgbFacingsPerStore != null && itgbFacingsPerStore != 0 ) {
							 double percentage = (double) itgbSMPFacingsPerStore / itgbFacingsPerStore;
							 value = DECIMAL_FORMATTER.format(percentage * 100);
							 rowMap.put("ITGB_PROMOTION_SOS_IN_ITGB_FACINGS", value);
						}
					} else if (metric.equals("SMP - % SMP Facings In Non ITG Brand Facings")) {
						//String value = "NA";
						Integer nonitgbSMPFacingsPerStore = nonitgbSMPFacings.get(storeVisitKey);
						if ( nonitgbSMPFacingsPerStore == null ) { nonitgbSMPFacingsPerStore = 0; }
						
						rowMap.put("NON_ITGB_PROMOTED_FACING_COUNT", ""+nonitgbSMPFacingsPerStore);
						
						/*Integer nonitgbFacingsPerStore = nonITGBActualFacings.get(storeVisitKey);
						if ( nonitgbFacingsPerStore != null && nonitgbFacingsPerStore != 0 ) {
							 double percentage = (double) nonitgbSMPFacingsPerStore / nonitgbFacingsPerStore;
							 value = DECIMAL_FORMATTER.format(percentage * 100);
						}*/
					} else if (metric.equals("SMP - % ITG SMP Facings In Overall SMP Facings")) {
						String value = "NA";
						Integer itgbSMPFacingsPerStore = itgbSMPFacings.get(storeVisitKey);
						if ( itgbSMPFacingsPerStore == null ) { itgbSMPFacingsPerStore = 0; }
						Integer overallSMPFacingsPerStore = overallSMPFacings.get(storeVisitKey);
						if ( overallSMPFacingsPerStore != null && overallSMPFacingsPerStore != 0 ) {
							 double percentage = (double) itgbSMPFacingsPerStore / overallSMPFacingsPerStore;
							 value = DECIMAL_FORMATTER.format(percentage * 100);
							 rowMap.put("ITGB_PROMOTION_SOS", value);
							 
							 Integer nonitgbSMPFacingsPerStore = nonitgbSMPFacings.get(storeVisitKey);
							 if ( nonitgbSMPFacingsPerStore == null ) { nonitgbSMPFacingsPerStore = 0; }
							 percentage = (double) nonitgbSMPFacingsPerStore / overallSMPFacingsPerStore;
							 value = DECIMAL_FORMATTER.format(percentage * 100);
							 rowMap.put("NON_ITGB_PROMOTION_SOS", value);
							 
						}
					} else if (metric.equals("OOS - % Out of Stock Facings")) {
						String value = "NA";
						
						Integer overallActualFacingInStore = overallActualFacings.get(storeVisitKey);
						if ( overallActualFacingInStore == null ) { overallActualFacingInStore = 0; }
						
						Integer overallOOSFacingInStore = overallOOSFacings.get(storeVisitKey);
						if ( overallOOSFacingInStore == null ) { overallOOSFacingInStore = 0; }
						
						Integer totalFacingsIncludingOOS = overallActualFacingInStore + overallOOSFacingInStore;
						
						if ( totalFacingsIncludingOOS != 0 ) {
							double percentage = (double) overallOOSFacingInStore / totalFacingsIncludingOOS ;
							value = DECIMAL_FORMATTER.format(percentage * 100);
							rowMap.put("OOS_PERCENTAGE",value);
						}
					} else if (metric.startsWith("Price Gap % - Regular Price -")) {
						String value = "NA";
						String[] headerParts = metric.split("-");
						String brand1 = headerParts[2].trim();
						String brand2 = headerParts[3].trim();
						Map<String, Double> brandPrices = regularPrices.get(storeVisitKey);
						if ( brandPrices != null ) {
							Double brand1Price = brandPrices.get(brand1);
							Double brand2Price = brandPrices.get(brand2);
							if ( brand1Price != null && brand2Price != null && !brand1Price.equals(0.0) && !brand2Price.equals(0.0) ) {
								Double priceGap = brand1Price - brand2Price;
								Double priceGapPercentage = (brand1Price - brand2Price) / brand1Price ;
								value = DECIMAL_FORMATTER.format(priceGapPercentage * 100);
								
								if ( brand1.toLowerCase().contains("maverick") && brand2.toLowerCase().contains("pall mall") ) {
									rowMap.put("PRICE_GAP_MAVERICK_PALLMALL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL", ""+priceGap);
									if ( brand1Price.doubleValue() == brand2Price.doubleValue() ) {
										rowMap.put("PRICE_GAP_PARITY_TO_PALLMALL", "1");
									} else {
										rowMap.put("PRICE_GAP_PARITY_TO_PALLMALL", "0");
									}
								}
								
								if ( brand1.toLowerCase().contains("winston") && brand2.toLowerCase().contains("camel") ) {
									rowMap.put("PRICE_GAP_WINSTON_CAMEL_ORIGINAL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL", ""+priceGap);
								}
								
								if ( brand1.toLowerCase().contains("winston") && brand2.toLowerCase().contains("marlboro") ) {
									rowMap.put("PRICE_GAP_WINSTON_MARLBORO_SS", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS", ""+priceGap);
									if ( brand1Price.doubleValue() <= (brand2Price.doubleValue() - 0.15) ) {
										rowMap.put("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS", "1");
									} else {
										rowMap.put("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS", "0");
									}
								}
								
								if ( brand1.toLowerCase().contains("kool") && brand2.toLowerCase().contains("newport") ) {
									rowMap.put("PRICE_GAP_KOOL_NEWPORT_MENTHOL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL", ""+priceGap);
									if ( brand1Price.doubleValue() > brand2Price.doubleValue() ) {
										rowMap.put("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL", "1");
									} else {
										rowMap.put("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL", "0");
									}
								}
							}
						}
					} else if (metric.startsWith("Price Gap % - Multipack Price -")) {
						String value = "NA";
						String[] headerParts = metric.split("-");
						String brand1 = headerParts[2].trim();
						String brand2 = headerParts[3].trim();
						Map<String, Double> brandPrices = multipackPrices.get(storeVisitKey);
						if ( brandPrices != null ) {
							Double brand1Price = brandPrices.get(brand1);
							Double brand2Price = brandPrices.get(brand2);
							if ( brand1Price != null && brand2Price != null && !brand1Price.equals(0.0) && !brand2Price.equals(0.0) ) {
								Double priceGap = brand1Price - brand2Price;
								Double priceGapPercentage = (brand1Price - brand2Price) / brand1Price ;
								value = DECIMAL_FORMATTER.format(priceGapPercentage * 100);
								
								if ( brand1.toLowerCase().contains("maverick") && brand2.toLowerCase().contains("pall mall") ) {
									rowMap.put("PRICE_GAP_MP_MAVERICK_PALLMALL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_MP_MAVERICK_PALLMALL", ""+priceGap);
									if ( brand1Price.doubleValue() == brand2Price.doubleValue() ) {
										rowMap.put("PRICE_GAP_MP_PARITY_TO_PALLMALL", "1");
									} else {
										rowMap.put("PRICE_GAP_MP_PARITY_TO_PALLMALL", "0");
									}
								}
								
								if ( brand1.toLowerCase().contains("winston") && brand2.toLowerCase().contains("camel") ) {
									rowMap.put("PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL", ""+priceGap);
								}
								
								if ( brand1.toLowerCase().contains("winston") && brand2.toLowerCase().contains("marlboro") ) {
									rowMap.put("PRICE_GAP_MP_WINSTON_MARLBORO_SS", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_MP_WINSTON_MARLBORO_SS", ""+priceGap);
									if ( brand1Price.doubleValue() <= (brand2Price.doubleValue() - 0.15) ) {
										rowMap.put("PRICE_GAP_MP_WINSTON_BELOW_MARLBORO_SS", "1");
									} else {
										rowMap.put("PRICE_GAP_MP_WINSTON_BELOW_MARLBORO_SS", "0");
									}
								}
								
								if ( brand1.toLowerCase().contains("kool") && brand2.toLowerCase().contains("newport") ) {
									rowMap.put("PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL", value);
									rowMap.put("ABSOLUTE_PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL", ""+priceGap);
									if ( brand1Price.doubleValue() > brand2Price.doubleValue() ) {
										rowMap.put("PRICE_GAP_MP_KOOL_ABOVE_NEWPORT_MENTHOL", "1");
									} else {
										rowMap.put("PRICE_GAP_MP_KOOL_ABOVE_NEWPORT_MENTHOL", "0");
									}
								}
							}
						}
					}
				}
				
				rowMap.put("SCORE", computeScore(rowMap));
				
				String insertQuery = buildInsertQuery(rowMap);
				insertStmt.addBatch(insertQuery);
				
				String deleteQuery = "DELETE FROM ITG_AGG_STORE_MONTH WHERE PROJECT_ID="+rowMap.get("PROJECT_ID")+" AND STORE_ID='"+rowMap.get("STORE_ID")+
						"' AND TIMEPERIOD='"+rowMap.get("TIMEPERIOD")+"'";
				deleteStmt.addBatch(deleteQuery);
			}
			
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation:: adding/replacing aggregation table entries");
			int[] idsDeleted = deleteStmt.executeBatch();
			LOGGER.info("---------------ITGAggregationDaoImpl--runDailyAggregation:: deleted {} records", idsDeleted.length);
			int[] idsInserted = insertStmt.executeBatch();
			LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation:: inserted {} records", idsInserted.length);
			conn.commit();
		} catch (Exception e) {
			LOGGER.error("Error while running daily aggregation :: EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
        	if (conn != null) {
                try {
                	insertStmt.close();
                	deleteStmt.close();
                	conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
		LOGGER.info("---------------ITGAggregationDaoImpl--runDailyAggregation completed");
	}
	
	private String computeScore(Map<String, String> rowMap) {
		int score = 0;
		String aboveVolumeShare = rowMap.get("ABOVE_VOLUME_SHARE");
		if (null != aboveVolumeShare && aboveVolumeShare.equals("1") ) {
			score = score + 15;
		}
		String aboveMarketShare = rowMap.get("ABOVE_MARKET_SHARE");
		if (null != aboveMarketShare && aboveMarketShare.equals("1") ) {
			score = score + 15;
		}
		String planFacingCompliance = rowMap.get("PLAN_FACING_COMPLIANCE");
		if (null != planFacingCompliance && !planFacingCompliance.equals("0") ) { //on or above plan
			score = score + 20;
		}
		
		/*String anyITGBrandOnPromotion = rowMap.get("ITGB_PROMOTED_FACING_COUNT");
		if (null != anyITGBrandOnPromotion && !anyITGBrandOnPromotion.isEmpty() && !anyITGBrandOnPromotion.equals("NA") ) {
			int itgbPromotionFacingCount = Integer.parseInt(anyITGBrandOnPromotion);
			if ( itgbPromotionFacingCount > 0 ) {
				score = score + 20;
			}
		}*/
		
		int brandPresencePoints = 0;
		
		String winstonCount = rowMap.get("WINSTON_FACING_COUNT");
		if ( !winstonCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 10; }
		String koolCount = rowMap.get("KOOL_FACING_COUNT");
		if ( !koolCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 10; }
		String maverickCount = rowMap.get("MAVERICK_FACING_COUNT");
		if ( !maverickCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 10; }
		String usagCount = rowMap.get("USAG_FACING_COUNT");
		if ( !usagCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String salemCount = rowMap.get("SALEM_FACING_COUNT");
		if ( !salemCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String montclairCount = rowMap.get("MONTCLAIR_FACING_COUNT");
		if ( !montclairCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String sonomaCount = rowMap.get("SONOMA_FACING_COUNT");
		if ( !sonomaCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String crownsCount = rowMap.get("CROWNS_FACING_COUNT");
		if ( !crownsCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String fortunaCount = rowMap.get("FORTUNA_FACING_COUNT");
		if ( !fortunaCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		String raveCount = rowMap.get("RAVE_FACING_COUNT");
		if ( !raveCount.equals("0") ) { brandPresencePoints = brandPresencePoints + 5; }
		
		brandPresencePoints = ( brandPresencePoints > 50 ) ? 50 : brandPresencePoints;
		
		score = score + brandPresencePoints;
		
		return score+"";
	}

	private Map<String, String> getGeoMappingIds(Connection conn) throws Exception {
		String sql = "SELECT storeId,geoLevel5Id,geoLevel4Id,geoLevel3Id,geoLevel2Id,geoLevel1Id "
				+ "FROM StoreGeoLevelMap WHERE customerCode='"+customerCode+"'";
		Map<String,String> mappings = new HashMap<String,String>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			mappings.put(rs.getString("storeId"),rs.getString("geoLevel5Id")+"-"+rs.getString("geoLevel4Id")+"-"+rs.getString("geoLevel3Id")+"-"+
					rs.getString("geoLevel2Id")+"-"+rs.getString("geoLevel1Id"));
		}
		rs.close();
		ps.close();
		return mappings;
		
	}

	private String buildInsertQuery(Map<String, String> rowMap) {
		LOGGER.info("ITGAggregationDaoImpl--runDailyAggregation:: Final Store Data :: " + rowMap);
		String query = "INSERT INTO ITG_AGG_STORE_MONTH VALUES (";
		for(String value : rowMap.values()) {
			if ( null == value ) {
				query = query + "null,";
			} else {
				query = query + "'" + value.replace("'", "\\'").replace("\"", "\\\"") + "',"; //quotes escaping
			}
			
		}
		query = query.substring(0,query.length()-1);
		query = query + ");";
		return query;
	}

	private Map<String, String> initializeRowMap() {
		Map<String,String> rowMap = new LinkedHashMap<String,String>();
		rowMap.put("PROJECT_ID",null);
		rowMap.put("STORE_ID",null);
		rowMap.put("STORE_CRS",null);
		rowMap.put("GEO_MAPPING_ID",null);
		rowMap.put("STORE_PLAN",null);
		rowMap.put("PLAN_FACING_COUNT",null);
		rowMap.put("TIMEPERIOD",null);
		rowMap.put("SCORE",null);
		rowMap.put("EDLP_STATUS",null);
		rowMap.put("ITGB_FACING_COUNT",null);
		rowMap.put("ITGB_OOS_COUNT",null);
		rowMap.put("NON_ITGB_FACING_COUNT",null);
		rowMap.put("NON_ITGB_OOS_COUNT",null);
		rowMap.put("PLAN_FACING_COMPLIANCE",null);
		rowMap.put("TOTAL_FACING_COUNT",null);
		rowMap.put("TOTAL_OOS_COUNT",null);
		rowMap.put("ITGB_SHARE_OF_SHELF",null);
		rowMap.put("ITGB_SHARE_OF_FACINGS",null);
		rowMap.put("ITGB_OSA_PERCENTAGE",null);
		rowMap.put("OOS_PERCENTAGE",null);
		rowMap.put("VOLUME_SHARE",null);
		rowMap.put("ABOVE_VOLUME_SHARE",null);
		rowMap.put("MARKET_SHARE",null);
		rowMap.put("ABOVE_MARKET_SHARE",null);
		rowMap.put("WINSTON_FACING_COUNT",""+0);
		rowMap.put("WINSTON_OOS_COUNT",""+0);
		rowMap.put("KOOL_FACING_COUNT",""+0);
		rowMap.put("KOOL_OOS_COUNT",""+0);
		rowMap.put("MAVERICK_FACING_COUNT",""+0);
		rowMap.put("MAVERICK_OOS_COUNT",""+0);
		rowMap.put("USAG_FACING_COUNT",""+0);
		rowMap.put("USAG_OOS_COUNT",""+0);
		rowMap.put("SALEM_FACING_COUNT",""+0);
		rowMap.put("SALEM_OOS_COUNT",""+0);
		rowMap.put("SONOMA_FACING_COUNT",""+0);
		rowMap.put("SONOMA_OOS_COUNT",""+0);
		rowMap.put("MONTCLAIR_FACING_COUNT",""+0);
		rowMap.put("MONTCLAIR_OOS_COUNT",""+0);
		rowMap.put("CROWNS_FACING_COUNT",""+0);
		rowMap.put("CROWNS_OOS_COUNT",""+0);
		rowMap.put("FORTUNA_FACING_COUNT",""+0);
		rowMap.put("FORTUNA_OOS_COUNT",""+0);
		rowMap.put("RAVE_FACING_COUNT",""+0);
		rowMap.put("RAVE_OOS_COUNT",""+0);
		rowMap.put("MARLBORO_SS_FACING_COUNT",""+0);
		rowMap.put("MARLBORO_SS_OOS_COUNT",""+0);
		rowMap.put("CAMEL_ORIGINAL_FACING_COUNT",""+0);
		rowMap.put("CAMEL_ORIGINAL_OOS_COUNT",""+0);
		rowMap.put("PALLMALL_FACING_COUNT",""+0);
		rowMap.put("PALLMALL_OOS_COUNT",""+0);
		rowMap.put("NEWPORT_MENTHOL_FACING_COUNT",""+0);
		rowMap.put("NEWPORT_MENTHOL_OOS_COUNT",""+0);
		rowMap.put("EAGLE_FACING_COUNT",""+0);
		rowMap.put("EAGLE_OOS_COUNT",""+0);
		rowMap.put("HIGHEST_PRICE",null);
		rowMap.put("HIGHEST_PRICE_BRAND",null);
		rowMap.put("LOWESET_PRICE",null);
		rowMap.put("LOWEST_PRICE_BRAND",null);
		rowMap.put("WINSTON_PRICE",null);
		rowMap.put("KOOL_PRICE",null);
		rowMap.put("MAVERICK_PRICE",null);
		rowMap.put("USAG_PRICE",null);
		rowMap.put("SALEM_PRICE",null);
		rowMap.put("SONOMA_PRICE",null);
		rowMap.put("MONTCLAIR_PRICE",null);
		rowMap.put("CROWNS_PRICE",null);
		rowMap.put("FORTUNA_PRICE",null);
		rowMap.put("RAVE_PRICE",null);
		rowMap.put("MARLBORO_SS_PRICE",null);
		rowMap.put("CAMEL_ORIGINAL_PRICE",null);
		rowMap.put("PALLMALL_PRICE",null);
		rowMap.put("NEWPORT_MENTHOL_PRICE",null);
		rowMap.put("EAGLE_PRICE",null);
		rowMap.put("WINSTON_MP_PRICE",null);
		rowMap.put("KOOL_MP_PRICE",null);
		rowMap.put("MAVERICK_MP_PRICE",null);
		rowMap.put("USAG_MP_PRICE",null);
		rowMap.put("SALEM_MP_PRICE",null);
		rowMap.put("SONOMA_MP_PRICE",null);
		rowMap.put("MONTCLAIR_MP_PRICE",null);
		rowMap.put("CROWNS_MP_PRICE",null);
		rowMap.put("FORTUNA_MP_PRICE",null);
		rowMap.put("RAVE_MP_PRICE",null);
		rowMap.put("MARLBORO_SS_MP_PRICE",null);
		rowMap.put("CAMEL_ORIGINAL_MP_PRICE",null);
		rowMap.put("PALLMALL_MP_PRICE",null);
		rowMap.put("NEWPORT_MENTHOL_MP_PRICE",null);
		rowMap.put("EAGLE_MP_PRICE",null);
		rowMap.put("HAS_ITGB_PRICE_FACINGS",null);
		rowMap.put("HAS_NON_ITGB_PRICE_FACINGS",null);
		rowMap.put("HAS_ITGB_MPP_FACINGS",null);
		rowMap.put("HAS_NON_ITGB_MPP_FACINGS",null);
		rowMap.put("PRICE_GAP_MAVERICK_PALLMALL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_MAVERICK_PALLMALL",null);
		rowMap.put("PRICE_GAP_PARITY_TO_PALLMALL",null);
		rowMap.put("PRICE_GAP_WINSTON_CAMEL_ORIGINAL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_WINSTON_CAMEL_ORIGINAL",null);
		rowMap.put("PRICE_GAP_WINSTON_MARLBORO_SS",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_WINSTON_MARLBORO_SS",null);
		rowMap.put("PRICE_GAP_WINSTON_BELOW_MARLBORO_SS",null);
		rowMap.put("PRICE_GAP_KOOL_NEWPORT_MENTHOL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_KOOL_NEWPORT_MENTHOL",null);
		rowMap.put("PRICE_GAP_KOOL_ABOVE_NEWPORT_MENTHOL",null);
		rowMap.put("PRICE_GAP_MP_MAVERICK_PALLMALL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_MP_MAVERICK_PALLMALL",null);
		rowMap.put("PRICE_GAP_MP_PARITY_TO_PALLMALL",null);
		rowMap.put("PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_MP_WINSTON_CAMEL_ORIGINAL",null);
		rowMap.put("PRICE_GAP_MP_WINSTON_MARLBORO_SS",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_MP_WINSTON_MARLBORO_SS",null);
		rowMap.put("PRICE_GAP_MP_WINSTON_BELOW_MARLBORO_SS",null);
		rowMap.put("PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL",null);
		rowMap.put("ABSOLUTE_PRICE_GAP_MP_KOOL_NEWPORT_MENTHOL",null);
		rowMap.put("PRICE_GAP_MP_KOOL_ABOVE_NEWPORT_MENTHOL",null);
		rowMap.put("ITGB_PROMOTED_FACING_COUNT",null);
		rowMap.put("NON_ITGB_PROMOTED_FACING_COUNT",null);
		rowMap.put("ITGB_PROMOTION_SOS_IN_ITGB_FACINGS",null);
		rowMap.put("ITGB_PROMOTION_SOS",null);
		rowMap.put("NON_ITGB_PROMOTION_SOS",null);
		rowMap.put("WINSTON_PROMO_FACING_COUNT",""+0);
		rowMap.put("KOOL_PROMO_FACING_COUNT",""+0);
		rowMap.put("MAVERICK_PROMO_FACING_COUNT",""+0);
		rowMap.put("USAG_PROMO_FACING_COUNT",""+0);
		rowMap.put("SALEM_PROMO_FACING_COUNT",""+0);
		rowMap.put("SONOMA_PROMO_FACING_COUNT",""+0);
		rowMap.put("MONTCLAIR_PROMO_FACING_COUNT",""+0);
		rowMap.put("CROWNS_PROMO_FACING_COUNT",""+0);
		rowMap.put("FORTUNA_PROMO_FACING_COUNT",""+0);
		rowMap.put("RAVE_PROMO_FACING_COUNT",""+0);
		rowMap.put("MARLBORO_SS_PROMO_FACING_COUNT",""+0);
		rowMap.put("CAMEL_ORIGINAL_PROMO_FACING_COUNT",""+0);
		rowMap.put("PALLMALL_PROMO_FACING_COUNT",""+0);
		rowMap.put("NEWPORT_MENTHOL_PROMO_FACING_COUNT",""+0);
		rowMap.put("EAGLE_PROMO_FACING_COUNT",""+0);
		rowMap.put("PREVIEW_IMAGEUUID",null);
		rowMap.put("STREET",null);
		rowMap.put("CITY",null);
		rowMap.put("STATECODE",null);
		return rowMap;
	}

	private Map<String, Integer> getStoreWiseITGBFacingCount(Map<String, Map<String, String>> facings) {
		Map<String,Integer> itgbFacings = new HashMap<String,Integer>();
		for(String storeVisitKey : facings.keySet()) {
			int totalFacings = 0;
			Map<String,String> brandFacings = facings.get(storeVisitKey);
			for(String brand : brandFacings.keySet()) {
				if ( itgBrands.contains(brand) ) {
					totalFacings = totalFacings + Integer.parseInt(brandFacings.get(brand));
				}
			}
			itgbFacings.put(storeVisitKey, totalFacings);
		}
		return itgbFacings;
	}
	
	private Map<String, Integer> getStoreWiseNonITGBFacingCount(Map<String, Map<String, String>> facings) {
		Map<String,Integer> itgbFacings = new HashMap<String,Integer>();
		for(String storeVisitKey : facings.keySet()) {
			int totalFacings = 0;
			Map<String,String> brandFacings = facings.get(storeVisitKey);
			for(String brand : brandFacings.keySet()) {
				if ( !itgBrands.contains(brand) ) {
					totalFacings = totalFacings + Integer.parseInt(brandFacings.get(brand));
				}
			}
			itgbFacings.put(storeVisitKey, totalFacings);
		}
		return itgbFacings;
	}
	
	private Map<String, Integer> getStoreWiseFacingCount(Map<String, Map<String, String>> facings) {
		Map<String,Integer> itgbFacings = new HashMap<String,Integer>();
		for(String storeVisitKey : facings.keySet()) {
			int totalFacings = 0;
			Map<String,String> brandFacings = facings.get(storeVisitKey);
			for(String brand : brandFacings.keySet()) {
				totalFacings = totalFacings + Integer.parseInt(brandFacings.get(brand));
			}
			itgbFacings.put(storeVisitKey, totalFacings);
		}
		return itgbFacings;
	}
	
	private List<String> getStoreVisits(Connection conn) throws Exception {
		int APPROVED_REVIEW_STATUS = 1;
		String sql = "SELECT storeVisit.storeId,storeVisit.taskId,metaInfo.VOLUME_SHARE,metaInfo.MARKET_SHARE,metaInfo.STORE_PLAN_TYPE,metaInfo.PLAN_FACINGS,metaInfo.EDLP_STATUS  \n" + 
				" FROM ITG_AGG_STORE_VISIT storeVisit, ITG_STORE_META_INFO metaInfo \n" + 
				" WHERE \n" + 
				"    storeVisit.projectId = ? \n" + 
				"    AND storeVisit.visitDateId = ?\n" + 
				"    AND storeVisit.reviewStatus = ?\n" + 
				"    AND storeVisit.storeId = metaInfo.STORE_ID \n" + 
				" GROUP BY storeVisit.storeId,storeVisit.taskId,metaInfo.VOLUME_SHARE,metaInfo.MARKET_SHARE,metaInfo.STORE_PLAN_TYPE,metaInfo.PLAN_FACINGS ;" ;
				
		List<String> storeVisits = new ArrayList<String>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, this.projectId);
		ps.setString(2, this.visitDateId);
		ps.setInt(3, APPROVED_REVIEW_STATUS);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			storeVisits.add(rs.getString("storeId")+"#"+rs.getString("taskId"));
			planTypeMap.put(rs.getString("storeId"), rs.getString("STORE_PLAN_TYPE"));
			volumeShareMap.put(rs.getString("storeId"), rs.getString("VOLUME_SHARE"));
			planFacingsMap.put(rs.getString("storeId"), rs.getString("PLAN_FACINGS"));
			marketShareMap.put(rs.getString("storeId"), rs.getString("MARKET_SHARE"));
			if(StringUtils.isNotBlank(rs.getString("EDLP_STATUS")) && rs.getString("EDLP_STATUS").equals("1") ) {
				edlpStores.add(rs.getString("storeId"));
			}
		}
		rs.close();
		ps.close();
		return storeVisits;
	}
	
	private Map<String,String> getStorePreviewImage(Connection conn) throws Exception {
		String sql = "SELECT storeId,taskId,imageUUID FROM ProjectStoreResult WHERE projectId = ? AND visitDateId = ?" ;
				
		Map<String,String> storeVisitImageMap = new HashMap<String,String>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, this.projectId);
		ps.setString(2, this.visitDateId);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			storeVisitImageMap.put(rs.getString("storeId")+"#"+rs.getString("taskId"),rs.getString("imageUUID"));
		}
		rs.close();
		ps.close();
		return storeVisitImageMap;
	}
	
	private Map<String,Map<String,String>> getStoreDetails(Connection conn) throws Exception {
		String sql = "SELECT sm.storeId,sm.street,sm.city,sm.statecode"
				+ " FROM ProjectStoreResult psr, StoreMaster sm WHERE psr.projectId = ? AND psr.visitDateId = ? "
				+ " AND psr.storeId = sm.storeId " ;
				
		Map<String,Map<String,String>> storeDetailsMap = new HashMap<String,Map<String,String>>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, this.projectId);
		ps.setString(2, this.visitDateId);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			Map<String,String> storeDetails = new HashMap<String,String>();
			storeDetails.put("STREET", rs.getString("street"));
			storeDetails.put("CITY", rs.getString("city"));
			storeDetails.put("STATECODE", rs.getString("statecode"));
			storeDetailsMap.put(rs.getString("storeId"),storeDetails);
		}
		rs.close();
		ps.close();
		return storeDetailsMap;
	}

	private Map<String, Map<String, String>> getActualFacingsAllBrands(Connection conn) throws Exception{
		String sql = "SELECT" + 
				"    storeVisit.storeId,storeVisit.taskId, storeVisit.brandName, storeVisit.facingCount " + 
				" FROM" + 
				"    ITG_AGG_STORE_VISIT storeVisit " + 
				" WHERE " + 
				"    storeVisit.projectId = " + projectId +
				"	 AND storeVisit.visitDateId = '"+ visitDateId +"' ";
		
		Map<String,Map<String,String>> facings = new HashMap<String,Map<String,String>>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String storeVisitKey = rs.getString("storeId")+"#"+rs.getString("taskId");
			if( ! facings.containsKey(storeVisitKey) ) {
				facings.put(storeVisitKey, new HashMap<String,String>());
			}
			facings.get(storeVisitKey).put(rs.getString("brandName"), rs.getString("facingCount").split("\\.")[0]);
		}
		rs.close();
		ps.close();
		return facings;
		
	}
	
	private Map<String, Map<String, String>> getOOSFacingsAllBrands(Connection conn) throws Exception{
		String sql = "SELECT" + 
				"    storeVisit.storeId,storeVisit.taskId, storeVisit.brandName, storeVisit.oosFacingCount " + 
				" FROM" + 
				"    ITG_AGG_STORE_VISIT storeVisit " + 
				" WHERE " + 
				"    storeVisit.projectId = " + projectId +
				"	 AND storeVisit.visitDateId = '"+ visitDateId +"' ";
		
		Map<String,Map<String,String>> facings = new HashMap<String,Map<String,String>>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String storeVisitKey = rs.getString("storeId")+"#"+rs.getString("taskId");
			if( ! facings.containsKey(storeVisitKey) ) {
				facings.put(storeVisitKey, new HashMap<String,String>());
			}
			facings.get(storeVisitKey).put(rs.getString("brandName"), rs.getString("oosFacingCount").split("\\.")[0]);
		}
		rs.close();
		ps.close();
		return facings;
		
	}
	
	private Map<String, Map<String, Double>> getRegularPriceAllBrands(Connection conn) throws Exception {
		String sql = "SELECT" + 
				"    storeVisit.storeId,storeVisit.taskId, storeVisit.brandName, storeVisit.regularPrice, storeVisit.posPrice " + 
				" FROM" + 
				"    ITG_AGG_STORE_VISIT storeVisit " + 
				" WHERE" + 
				"    storeVisit.projectId = " + projectId +
				"	 AND storeVisit.visitDateId = '"+ visitDateId +"' ";
		
		Map<String,Map<String,Double>> prices = new HashMap<String,Map<String,Double>>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String storeVisitKey = rs.getString("storeId")+"#"+rs.getString("taskId");
			if( ! prices.containsKey(storeVisitKey) ) {
				prices.put(storeVisitKey, new HashMap<String,Double>());
			}
			
			String regularPrice = rs.getString("regularPrice");
			String posPrice = rs.getString("posPrice");

			if ( StringUtils.isAllBlank(regularPrice, posPrice) ) {
				continue;
			} else {
				String price = null;
				if ( StringUtils.isNotBlank(posPrice) ) {
					price = posPrice;
				} else {
					price = regularPrice;
				}
				Double priceInDouble = 0.0;
				try {
					priceInDouble = Double.parseDouble(price);
				} catch (Exception e) {
					//GROUND
				}
				prices.get(storeVisitKey).put(rs.getString("brandName"), priceInDouble ); 
			}
		}
		rs.close();
		ps.close();
		return prices;
	}
	
	private Map<String, Map<String, Double>> getMultipackPriceAllBrands(Connection conn) throws Exception {
		String sql = "SELECT" + 
				"    storeVisit.storeId,storeVisit.taskId, storeVisit.brandName, storeVisit.multipackPrice " + 
				" FROM" + 
				"    ITG_AGG_STORE_VISIT storeVisit " + 
				" WHERE" + 
				"    storeVisit.projectId = " + projectId +
				"	 AND storeVisit.visitDateId = '"+ visitDateId +"' " +
				"    AND storeVisit.multipackPrice <> '' " + 
				"    AND storeVisit.multipackPrice IS NOT NULL " ;
		
		Map<String,Map<String,Double>> prices = new HashMap<String,Map<String,Double>>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String storeVisitKey = rs.getString("storeId")+"#"+rs.getString("taskId");
			if( ! prices.containsKey(storeVisitKey) ) {
				prices.put(storeVisitKey, new HashMap<String,Double>());
			}
			
			String price = rs.getString("multipackPrice");
			Double priceInDouble = 0.0;
			try {
				priceInDouble = Double.parseDouble(price);
			} catch (Exception e) {
				//GROUND
			}
			
			prices.get(storeVisitKey).put(rs.getString("brandName"), priceInDouble );
		}
		rs.close();
		ps.close();
		return prices;
	}
	
	private Map<String, Map<String, String>> getSMPFacingsAllBrands(Connection conn) throws Exception {
		String sql = "SELECT " + 
				"    storeVisit.storeId,storeVisit.taskId, storeVisit.brandName, storeVisit.promotionFacingCount " + 
				" FROM " + 
				"    ITG_AGG_STORE_VISIT storeVisit " + 
				" WHERE " + 
				"    storeVisit.projectId = " + projectId +
				"	 AND storeVisit.visitDateId = '"+ visitDateId +"' " ;
		
		Map<String,Map<String,String>> SMPFacings = new HashMap<String,Map<String,String>>();
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String storeVisitKey = rs.getString("storeId")+"#"+rs.getString("taskId");
			if( ! SMPFacings.containsKey(storeVisitKey) ) {
				SMPFacings.put(storeVisitKey, new HashMap<String,String>());
			}
			SMPFacings.get(storeVisitKey).put(rs.getString("brandName"), rs.getString("promotionFacingCount").split("\\.")[0]);
		}
		rs.close();
		ps.close();
		return SMPFacings;
		
	}
	
	private Map<String, Map<String,String>> getHighestPrice(Map<String, Map<String, Double>> regularPrices, Map<String, Map<String, Double>>multipackPrices) {
		Map<String, Map<String,String>> highestPriceMap = new HashMap<String,Map<String,String>>();
		for(String storeVisitKey : storeVisits ) {
			Double highestRegularPrice = null;
			Double highestMultipackPrice = null;
			Double highestPrice = null;
			Map<String, Double> regularPricesInStore = regularPrices.get(storeVisitKey);
			Map<String, Double> multipackPricesInStore = multipackPrices.get(storeVisitKey);
			
			if ( ( regularPricesInStore == null || regularPricesInStore.isEmpty() ) && 
					( multipackPricesInStore == null || multipackPricesInStore.isEmpty() ) ) {
				continue;
			}
			
			if (regularPricesInStore != null && !regularPricesInStore.isEmpty() ) {
				regularPricesInStore = sortByValueDesc(regularPricesInStore);
				highestRegularPrice = (Double) regularPricesInStore.values().toArray()[0];
			}
			if (multipackPricesInStore != null && !multipackPricesInStore.isEmpty() ) {
				multipackPricesInStore = sortByValueDesc(multipackPricesInStore);
				highestMultipackPrice = (Double) multipackPricesInStore.values().toArray()[0];
			}
			
			if ( highestRegularPrice == null ) {
				highestPrice = highestMultipackPrice; 
			} else if (highestMultipackPrice == null ) {
				highestPrice = highestRegularPrice;
			} else if ( highestRegularPrice.equals(highestMultipackPrice) ){
				highestPrice = highestRegularPrice;
			} else if ( highestRegularPrice > highestMultipackPrice ) {
				highestPrice = highestRegularPrice;
			} else {
				highestPrice = highestMultipackPrice;
			}
			
			String highestPriceBrands = "";
			if (regularPricesInStore != null ) {
				for(String brand : regularPricesInStore.keySet() ) {
					if ( highestPrice.equals(regularPricesInStore.get(brand)) ) {
						highestPriceBrands = highestPriceBrands + brand + " - Regular" + " / ";
					} else {
						break;
					}
				}
			}
			if ( multipackPricesInStore != null ) {
				for(String brand : multipackPricesInStore.keySet() ) {
					if ( highestPrice.equals(multipackPricesInStore.get(brand)) ) {
						highestPriceBrands = highestPriceBrands + brand + " - Multipack" + " / ";
					} else {
						break;
					}
				}
			}
			
			Map<String,String> valueMap = new HashMap<String,String>();
			valueMap.put("highestPrice", highestPrice+"");
			valueMap.put("highestPriceBrands", highestPriceBrands.substring(0,highestPriceBrands.length()-2));
			
			highestPriceMap.put(storeVisitKey, valueMap);
		}
		return highestPriceMap;
	}
	
	private Map<String, Map<String,String>> getLowestPrice(Map<String, Map<String, Double>> regularPrices, Map<String, Map<String, Double>>multipackPrices) {
		Map<String, Map<String,String>> lowestPriceMap = new HashMap<String,Map<String,String>>();
		for(String storeVisitKey : storeVisits ) {
			Double lowestRegularPrice = null;
			Double lowestMultipackPrice = null;
			Double lowestPrice = null;
			Map<String, Double> regularPricesInStore = regularPrices.get(storeVisitKey);
			Map<String, Double> multipackPricesInStore = multipackPrices.get(storeVisitKey);
			
			if ( (regularPricesInStore == null || regularPricesInStore.isEmpty()) && 
					(multipackPricesInStore == null || multipackPricesInStore.isEmpty() ) ) {
				continue;
			}
			
			if (regularPricesInStore != null && !regularPricesInStore.isEmpty() ) {
				regularPricesInStore = sortByValueAsc(regularPricesInStore);
				lowestRegularPrice = (Double) regularPricesInStore.values().toArray()[0];
			}
			if (multipackPricesInStore != null && !multipackPricesInStore.isEmpty() ) {
				multipackPricesInStore = sortByValueAsc(multipackPricesInStore);
				lowestMultipackPrice = (Double) multipackPricesInStore.values().toArray()[0];
			}
			
			if ( lowestRegularPrice == null ) {
				lowestPrice = lowestMultipackPrice; 
			} else if (lowestMultipackPrice == null ) {
				lowestPrice = lowestRegularPrice;
			} else if ( lowestRegularPrice.equals(lowestMultipackPrice)){
				lowestPrice = lowestRegularPrice;
			} else if ( lowestRegularPrice < lowestMultipackPrice ) {
				lowestPrice = lowestRegularPrice;
			} else {
				lowestPrice = lowestMultipackPrice;
			}
			String lowestPriceBrands = "";
			if (regularPricesInStore != null ) {
				for(String brand : regularPricesInStore.keySet() ) {
					if ( lowestPrice.equals(regularPricesInStore.get(brand)) ) {
						lowestPriceBrands = lowestPriceBrands + brand + " - Regular" + " / ";
					} else {
						break;
					}
				}
			}
			if ( multipackPricesInStore != null ) {
				for(String brand : multipackPricesInStore.keySet() ) {
					if ( lowestPrice.equals(multipackPricesInStore.get(brand)) ) {
						lowestPriceBrands = lowestPriceBrands + brand + " - Multipack" + " / ";
					} else {
						break;
					}
				}
			}
			
			Map<String,String> valueMap = new HashMap<String,String>();
			valueMap.put("lowestPrice", lowestPrice+"");
			valueMap.put("lowestPriceBrands", lowestPriceBrands.substring(0,lowestPriceBrands.length()-2));
			
			lowestPriceMap.put(storeVisitKey, valueMap);
		}
		return lowestPriceMap;
	}
	
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAsc(Map<K, V> unsortMap) {

		   List<Map.Entry<K, V>> list =
	                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

	        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                return (o1.getValue()).compareTo(o2.getValue());
	            }
	        });

	        Map<K, V> result = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }

	        return result;
    }
	
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> unsortMap) {

		   List<Map.Entry<K, V>> list =
	                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

	        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                return (o2.getValue()).compareTo(o1.getValue());
	            }
	        });

	        Map<K, V> result = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }

	        return result;
	}

	@Override
	public Map<String,Map<String,Object>> runStoreVisitAggregation(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--runStoreVisitAggregation::projectId={},storeID={},taskId={}" , 
				projectId, storeId, taskId);
		Map<String,Map<String,Object>> aggMap = new HashMap<String,Map<String,Object>>();
		Connection conn = null;
		String aggQuery = "SELECT\n" + 
				"    projectId,\n" + 
				"    storeId,\n" + 
				"    taskId,\n" + 
				"    visitDate AS visitDateId,\n" + 
				"    brandName,\n" + 
				"    SUM(CASE WHEN productSubType = 'Cigarette' THEN facing ELSE 0 END ) AS facingCount,\n" + 
				"    SUM(CASE WHEN productSubType = 'Cigarette' AND UPC LIKE '%\\_%' THEN facing ELSE 0 END) AS promotionFacingCount,\n" + 
				"    SUM(CASE WHEN productSubType = 'Cigarette OOS' THEN facing ELSE 0 END ) AS oosFacingCount,\n" +
				"    MIN(CASE WHEN productSubType = 'Cigarette Price' AND UPC LIKE '%\\_%' THEN CAST(price AS DECIMAL(5,2)) ELSE null END) AS regularPrice,\n" + 
				"    MIN(CASE WHEN productSubType = 'Cigarette Price' AND UPC NOT LIKE '%\\_%' THEN CAST(price AS DECIMAL(5,2)) ELSE null END) AS posPrice,\n" + 
				"    MIN(CASE WHEN productSubType = 'Cigarette Multipack Price' THEN CAST(price AS DECIMAL(5,2)) ELSE null END) AS multipackPrice\n" + 
				"FROM\n" + 
				"    ProjectStoreData\n" + 
				"WHERE\n" + 
				"    projectId = ?\n" + 
				"    AND storeId = ?\n" + 
				"    AND taskId = ?\n" + 
				"GROUP BY\n" + 
				"    projectId,storeId,taskId,brandName";
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement aggPs = conn.prepareStatement(aggQuery);
			
			aggPs.setInt(1, projectId);
			aggPs.setString(2, storeId);
			aggPs.setString(3, taskId);
			
			ResultSet rs = aggPs.executeQuery(); //run agg query
			while(rs.next()) {
				Map<String,Object> brandData = new HashMap<String,Object>();
				brandData.put("facingCount", rs.getInt("facingCount"));
				brandData.put("promotionFacingCount",rs.getInt("promotionFacingCount"));
				brandData.put("oosFacingCount", rs.getInt("oosFacingCount"));
				brandData.put("regularPrice", rs.getDouble("regularPrice"));
				brandData.put("posPrice", rs.getDouble("posPrice"));
				brandData.put("multipackPrice", rs.getDouble("multipackPrice"));
				brandData.put("visitDateId", rs.getString("visitDateId"));
				aggMap.put(rs.getString("brandName"), brandData);
			}
			
			rs.close();
			aggPs.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while running store visit aggregation :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--runStoreVisitAggregation completed");
		return aggMap;
	}
	
	@Override
	public int getInvalidHighPriceInRawDataCount(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getInvalidHighPriceInRawDataCount::projectId={},storeID={},taskId={}" , 
				projectId, storeId, taskId);
		Connection conn = null;
		String query = "SELECT COUNT(*) AS invalidPriceCount FROM ImageAnalysisNew "
				+ " WHERE projectId = ? AND storeId = ? AND taskId = ? AND CAST(price AS DECIMAL(5,2)) >= 100 ";
		int invalidPriceCount = 0;
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setInt(1, projectId);
			ps.setString(2, storeId);
			ps.setString(3, taskId);
			
			ResultSet rs = ps.executeQuery(); 
			
			while(rs.next()) {
				invalidPriceCount = rs.getInt("invalidPriceCount");
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching Invalid High Price In Raw Data Count :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--getInvalidHighPriceInRawDataCount completed");
		return invalidPriceCount;
	}
	
	@Override
	public boolean hasStitchingFailed(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--hasStitchingFailed::projectId={},storeID={},taskId={}" , 
				projectId, storeId, taskId);
		Connection conn = null;
		String query = "SELECT distinct(imageNotUsableComment) FROM ImageStoreNew WHERE"
				+ " projectId=? AND storeId=? AND taskId = ?";
		boolean stitchFailedStatus = false;
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setInt(1, projectId);
			ps.setString(2, storeId);
			ps.setString(3, taskId);
			
			ResultSet rs = ps.executeQuery(); 
			
			while(rs.next()) {
				String comment = rs.getString("imageNotUsableComment");
				if ( StringUtils.isNotBlank(comment) && comment.contains("6") ) {
					stitchFailedStatus = true;
					break;
				}
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching Stitch Failed Status :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--hasStitchingFailed completed");
		return stitchFailedStatus;
	}
	
	@Override
	public Map<String,Object> getStoreMetaInfo(String storeId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreMetaInfo::storeID={}" , storeId);
		Map<String,Object> metaInfo = new HashMap<String,Object>();
		Connection conn = null;
		String query = "SELECT * FROM ITG_STORE_META_INFO WHERE STORE_ID=?";
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setString(1, storeId);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				metaInfo.put("planType", rs.getString("STORE_PLAN_TYPE"));
				metaInfo.put("planFacing", rs.getInt("PLAN_FACINGS"));
				metaInfo.put("volumeShare", rs.getDouble("VOLUME_SHARE"));
				metaInfo.put("marketShare", rs.getDouble("MARKET_SHARE"));
			}
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching store meta info:: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreMetaInfo completed");
		return metaInfo;
	}
	
	@Override
	public void insertStoreVisitAggregationData(int projectId, String storeId, String taskId, String visitDateId,
			Map<String,Map<String,Object>> aggData, int bucketId, int reviewStatus, String reviewComments, String rejectReason) {
		LOGGER.info("---------------ITGAggregationDaoImpl--insertStoreVisitAggregationData::projectId={},storeID={},taskId={},visitDateId={},"
				+ "bucketId={},reviewStatus={},reviewComments={},rejectReason={}",
				projectId,storeId,taskId,visitDateId,bucketId,reviewStatus,reviewComments,rejectReason); 
		
		Connection conn = null;

		String insertQuery = "INSERT INTO ITG_AGG_STORE_VISIT (projectId,storeId,taskId,visitDateId,brandName,facingCount,promotionFacingCount,"
				+ "oosFacingCount,regularPrice,posPrice,multipackPrice,bucketId,reviewStatus,reviewComments,rejectReason,brandLevelReview)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String deleteQuery = "DELETE FROM ITG_AGG_STORE_VISIT WHERE projectId = ? AND storeId = ? AND taskId = ?";
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement deletePs = conn.prepareStatement(deleteQuery);
			PreparedStatement insertPs = conn.prepareStatement(insertQuery);
			
			deletePs.setInt(1, projectId);
			deletePs.setString(2, storeId);
			deletePs.setString(3, taskId);
			deletePs.execute(); //Delete existing records
			deletePs.close();
			
			for(String brandName : aggData.keySet()) {
				Map<String,Object> brandData = aggData.get(brandName);
				insertPs.setInt(1, projectId);
				insertPs.setString(2, storeId);
				insertPs.setString(3, taskId);
				insertPs.setString(4, visitDateId);
				insertPs.setString(5, brandName);
				insertPs.setInt(6, (int) brandData.get("facingCount"));
				insertPs.setInt(7, (int) brandData.get("promotionFacingCount"));
				insertPs.setInt(8, (int) brandData.get("oosFacingCount"));
				Double regularPrice = (Double) brandData.get("regularPrice");
				insertPs.setString(9, regularPrice == 0 ? null : ""+regularPrice);
				Double posPrice = (Double) brandData.get("posPrice");
				insertPs.setString(10, posPrice == 0 ? null : ""+posPrice);
				Double multipackPrice = (Double) brandData.get("multipackPrice");
				insertPs.setString(11, multipackPrice == 0 ? null : ""+multipackPrice);
				insertPs.setInt(12, bucketId);
				insertPs.setInt(13, reviewStatus);
				insertPs.setString(14, reviewComments);
				insertPs.setString(15, rejectReason);
				int brandLevelReviewFlag = brandData.get("brandLevelReview") == null ? 0 : 1;
				insertPs.setInt(16, brandLevelReviewFlag);
				insertPs.addBatch();
			}
			
			insertPs.executeBatch(); //insert 
			insertPs.close();
			conn.commit();
		}
		catch (Exception e) {
			LOGGER.error("Error while inserting store visit aggregation data:: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	              conn.setAutoCommit(true);
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--insertStoreVisitAggregationData completed");
	}

	@Override
	public List<LinkedHashMap<String, Object>> getStoreVisitsToReview(int projectId, String visitDate,
			int bucketId, String inputStoreId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreVisitsToReview::projectId={},visitDate={},bucketId={},storeId={}" , 
				projectId, visitDate, bucketId,inputStoreId);
		
		Connection conn = null;
		int ALL_BUCKETS = 100;
		String bucketQuery = "SELECT * FROM ITG_AGG_STORE_VISIT, ITG_STORE_META_INFO \n" + 
				" WHERE projectId=? AND visitDateId = ? AND bucketId = ? AND storeId = STORE_ID\n" + 
				" ORDER BY storeId";
		String queryWithoutBucketFilter = "SELECT * FROM ITG_AGG_STORE_VISIT, ITG_STORE_META_INFO \n" + 
				" WHERE projectId=? AND visitDateId = ? AND storeId = STORE_ID\n" + 
				" ORDER BY storeId";
		String storeQuery = "SELECT * FROM ITG_AGG_STORE_VISIT, ITG_STORE_META_INFO \n" + 
				" WHERE projectId=? AND visitDateId = ? AND storeId = ? AND storeId = STORE_ID\n" + 
				" ORDER BY storeId";
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String, Object>>();
		Map<String,LinkedHashMap<String, Object>> tempMap = new LinkedHashMap<String,LinkedHashMap<String, Object>>();
		Map<String,Integer> storeFacingCountMap = new HashMap<String,Integer>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = null;
			
			if ( StringUtils.isNotBlank(inputStoreId) ) {
				ps = conn.prepareStatement(storeQuery);
				ps.setInt(1, projectId);
				ps.setString(2, visitDate);
				ps.setString(3, inputStoreId);
			} else if ( bucketId == ALL_BUCKETS ){
				ps = conn.prepareStatement(queryWithoutBucketFilter);
				ps.setInt(1, projectId);
				ps.setString(2, visitDate);
			} else {
				ps = conn.prepareStatement(bucketQuery);
				ps.setInt(1, projectId);
				ps.setString(2, visitDate);
				ps.setInt(3, bucketId);
			}

			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				String storeId = rs.getString("storeId");
				if ( tempMap.get(storeId) == null ) {
					LinkedHashMap<String,Object> storeMap = new LinkedHashMap<String,Object>();
					storeMap.put("projectId", rs.getString("projectId"));
					storeMap.put("storeId", storeId);
					storeMap.put("taskId", rs.getString("taskId"));
					storeMap.put("visitDateId", rs.getString("visitDateId"));
					storeMap.put("storeType", rs.getString("STORE_PLAN_TYPE"));
					storeMap.put("planFacings", rs.getString("PLAN_FACINGS"));
					storeMap.put("itgFacings", "0.0");
					storeMap.put("volumeShare", rs.getString("VOLUME_SHARE"));
					storeMap.put("shareOfShelf", "0.0");
					storeMap.put("reviewStatus", rs.getInt("reviewStatus"));
					
					String reviewComment = rs.getString("reviewComments");
					List<String> reviewComments = new ArrayList<String>();
					if ( StringUtils.isNotBlank(reviewComment) ) {
						reviewComments.addAll(Arrays.asList(reviewComment.split(",")));
					}
					storeMap.put("reviewComments", reviewComments);
					
					String rejectReason = rs.getString("rejectReason");
					List<String> rejectReasons = new ArrayList<String>();
					if ( StringUtils.isNotBlank(rejectReason) ) {
						rejectReasons.addAll(Arrays.asList(rejectReason.split(",")));
					}
					storeMap.put("rejectReason", rejectReasons);
					
					storeMap.put("bucketId", rs.getInt("bucketId"));
					storeMap.put("brands", new ArrayList<LinkedHashMap<String,Object>>());
					tempMap.put(storeId, storeMap);
					storeFacingCountMap.put(storeId,0);
				}
				LinkedHashMap<String,Object> storeMap = tempMap.get(storeId);
				List<LinkedHashMap<String,Object>> brandList = (List<LinkedHashMap<String,Object>>) storeMap.get("brands");
				String brandName = rs.getString("brandName");
				LinkedHashMap<String,Object> brand = new LinkedHashMap<String,Object>();
				brand.put("brandName", brandName);
				brand.put("facingCount", rs.getInt("facingCount"));
				brand.put("promotionFacingCount", rs.getInt("promotionFacingCount"));
				brand.put("oosFacingCount", rs.getInt("oosFacingCount"));
				brand.put("regularPrice", ConverterUtil.ifNullToEmpty(rs.getString("regularPrice")));
				brand.put("posPrice", ConverterUtil.ifNullToEmpty(rs.getString("posPrice")));
				brand.put("multipackPrice", ConverterUtil.ifNullToEmpty(rs.getString("multipackPrice")));
				brand.put("brandLevelReview", rs.getInt("brandLevelReview"));
				brandList.add(brand);
				
				int storeFacingCount = storeFacingCountMap.get(storeId) + ( rs.getInt("facingCount") + rs.getInt("oosFacingCount") );
				storeFacingCountMap.put(storeId,storeFacingCount);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching store visits to review :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		for( String key : tempMap.keySet() ) {
			LinkedHashMap<String, Object> oneStore = tempMap.get(key);
			int itgFacingCount = 0;
			int totalFacingCount = storeFacingCountMap.get(key);
			List<LinkedHashMap<String,Object>> brands = (List<LinkedHashMap<String,Object>>) oneStore.get("brands");
			TreeSet<Double> brandPriceSet = new TreeSet<Double>();
			for(LinkedHashMap<String, Object> brand : brands ) {
				String brandName = (String) brand.get("brandName");
				if ( itgBrands.contains(brandName) ) {
					itgFacingCount = itgFacingCount + ( (int) brand.get("facingCount") + (int) brand.get("oosFacingCount") );
				}
				
				String regularPriceStr = (String) brand.get("regularPrice");
				String posPriceStr = (String) brand.get("posPrice");
				if ( StringUtils.isNotBlank(posPriceStr) ) {
					regularPriceStr = posPriceStr;
				}
				
				String multipackPriceStr = (String) brand.get("multipackPrice");
				
				double regularPrice = StringUtils.isNotBlank(regularPriceStr) ? Double.parseDouble(regularPriceStr) : 0;
				double multipackPrice = StringUtils.isNotBlank(multipackPriceStr) ? Double.parseDouble(multipackPriceStr) : 0;
				
				double lowestForBrand = 0.00d;
				if(regularPrice != 0 && multipackPrice != 0) {
					if (regularPrice < multipackPrice) {
						lowestForBrand = regularPrice;
					} else if ( regularPrice > multipackPrice){
						lowestForBrand = multipackPrice;
					} else {
						lowestForBrand= regularPrice;
					}
				} else if ( regularPrice == 0 ) {
					lowestForBrand = multipackPrice;
				} else {
					lowestForBrand = regularPrice;
				}
				if ( lowestForBrand > 0 ) {
					brandPriceSet.add(lowestForBrand);
				}
			}
			
			oneStore.put("lowestPrice", "");
			if ( !brandPriceSet.isEmpty() ) {
				oneStore.put("lowestPrice", brandPriceSet.first().toString());
			}
			
			double itgSoS = 0.0d;
			if ( totalFacingCount != 0 ) {
				itgSoS = ((double) itgFacingCount / totalFacingCount ) * 100;
			}
			
			oneStore.put("itgFacings", itgFacingCount);
			oneStore.put("shareOfShelf", DECIMAL_FORMATTER.format(itgSoS));
			
			returnList.add(oneStore);
		}
		
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreVisitsToReview completed");
		return returnList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getStoreVisitImagesToReview(int projectId, String storeId,
			String taskId, String showAll) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreVisitImagesToReview::projectId={},storeId={},taskId={},showAll={}" , 
				projectId, storeId, taskId, showAll);
		
		Connection conn = null;
		String imagesQuery = "SELECT imageUUID,origWidth,origHeight,imageNotUsable,imageNotUsableComment FROM ImageStoreNew WHERE projectId=? AND storeId=? AND taskId=? ORDER BY sequenceNumber";
		String imageAnalysisQuery = "SELECT i.id, i.upc, i.leftTopX, i.leftTopY, i.width, i.height, i.price, i.priceConfidence, i.isDuplicate, pm.BRAND_NAME, pm.PRODUCT_LONG_NAME, pm.PRODUCT_SUB_TYPE FROM ImageAnalysisNew i "  
				+ " INNER JOIN ( SELECT UPC,BRAND_NAME,PRODUCT_LONG_NAME,PRODUCT_SUB_TYPE FROM ProductMaster WHERE PRODUCT_TYPE='Cigarette' AND PRODUCT_SUB_TYPE <> 'Cigarette Signage' ) pm "
				+ "  ON i.upc = pm.UPC WHERE i.imageUUID = ?";
		
		String imageAnalysisShowAllQuery = "SELECT i.id, i.upc, i.leftTopX, i.leftTopY, i.width, i.height, i.price, i.priceConfidence, i.isDuplicate, pm.BRAND_NAME, pm.PRODUCT_LONG_NAME, pm.PRODUCT_SUB_TYPE FROM ImageAnalysisNew i "  
				+ " INNER JOIN ( SELECT UPC,BRAND_NAME,PRODUCT_LONG_NAME,PRODUCT_SUB_TYPE FROM ProductMaster WHERE PRODUCT_TYPE='Cigarette' ) pm "
				+ "  ON i.upc = pm.UPC WHERE i.imageUUID = ?";
		
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String, Object>>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement imagePs = conn.prepareStatement(imagesQuery);
			PreparedStatement imageAnalysisPs = conn.prepareStatement(imageAnalysisQuery);
			PreparedStatement imageAnalysisShowAllPs = conn.prepareStatement(imageAnalysisShowAllQuery);
			
			imagePs.setInt(1, projectId);
			imagePs.setString(2, storeId);
			imagePs.setString(3, taskId);
			
			ResultSet imageRs = imagePs.executeQuery();
			
			Map<String,Map<String,String>> imageUUIDs = new LinkedHashMap<String,Map<String,String>>();
			while(imageRs.next()) {
				Map<String,String> oneImage = new HashMap<String,String>();
				oneImage.put("imageUUID", imageRs.getString("imageUUID"));
				oneImage.put("origWidth", imageRs.getString("origWidth"));
				oneImage.put("origHeight", imageRs.getString("origHeight"));
				oneImage.put("imageNotUsable", imageRs.getString("imageNotUsable"));
				oneImage.put("imageNotUsableComment", imageRs.getString("imageNotUsableComment"));
				imageUUIDs.put(imageRs.getString("imageUUID"), oneImage);
			}
			imageRs.close();
			imagePs.close();
			
			for(String imageUUID : imageUUIDs.keySet()) {
				
				Map<String,String> oneImage = imageUUIDs.get(imageUUID);
				
				LinkedHashMap<String,Object> imageMap = new LinkedHashMap<String,Object>();
				imageMap.put("imageUUID", oneImage.get("imageUUID"));
				imageMap.put("origWidth", oneImage.get("origWidth"));
				imageMap.put("origHeight", oneImage.get("origHeight"));
				imageMap.put("hasQualityIssue", 
						StringUtils.isBlank(oneImage.get("imageNotUsable")) ? 0 : Integer.parseInt(oneImage.get("imageNotUsable")));
				imageMap.put("qualityIssueReason", oneImage.get("imageNotUsableComment"));
				
				List<LinkedHashMap<String,Object>> imageAnalysisList = new ArrayList<LinkedHashMap<String,Object>>();
				
				if ( StringUtils.isNotBlank(showAll) && showAll.equals("true") ) {
					
					imageAnalysisShowAllPs.setString(1, oneImage.get("imageUUID"));
					
					ResultSet analysisRs = imageAnalysisShowAllPs.executeQuery();
					
					while(analysisRs.next()) {
						LinkedHashMap<String,Object> oneDetection = new LinkedHashMap<String,Object>();
						oneDetection.put("id", analysisRs.getLong("id"));
						oneDetection.put("upc", analysisRs.getString("upc"));
						oneDetection.put("x", analysisRs.getInt("leftTopX"));
						oneDetection.put("y", analysisRs.getInt("leftTopY"));
						oneDetection.put("w", analysisRs.getInt("width"));
						oneDetection.put("h", analysisRs.getInt("height"));
						oneDetection.put("price", ConverterUtil.ifNullToEmpty(analysisRs.getString("price")));
						oneDetection.put("priceConfidence", ConverterUtil.ifNullToEmpty(analysisRs.getString("priceConfidence")));
						oneDetection.put("brandName", analysisRs.getString("BRAND_NAME"));
						oneDetection.put("productName", analysisRs.getString("PRODUCT_LONG_NAME"));
						oneDetection.put("productSubType", analysisRs.getString("PRODUCT_SUB_TYPE"));
						oneDetection.put("isDuplicate", analysisRs.getInt("isDuplicate"));
						imageAnalysisList.add(oneDetection);
					}
					analysisRs.close();
				} else {
					imageAnalysisPs.setString(1, oneImage.get("imageUUID"));
					
					ResultSet analysisRs = imageAnalysisPs.executeQuery();
					
					while(analysisRs.next()) {
						LinkedHashMap<String,Object> oneDetection = new LinkedHashMap<String,Object>();
						oneDetection.put("id", analysisRs.getLong("id"));
						oneDetection.put("upc", analysisRs.getString("upc"));
						oneDetection.put("x", analysisRs.getInt("leftTopX"));
						oneDetection.put("y", analysisRs.getInt("leftTopY"));
						oneDetection.put("w", analysisRs.getInt("width"));
						oneDetection.put("h", analysisRs.getInt("height"));
						oneDetection.put("price", ConverterUtil.ifNullToEmpty(analysisRs.getString("price")));
						oneDetection.put("priceConfidence", ConverterUtil.ifNullToEmpty(analysisRs.getString("priceConfidence")));
						oneDetection.put("brandName", analysisRs.getString("BRAND_NAME"));
						oneDetection.put("productName", analysisRs.getString("PRODUCT_LONG_NAME"));
						oneDetection.put("productSubType", analysisRs.getString("PRODUCT_SUB_TYPE"));
						oneDetection.put("isDuplicate", analysisRs.getInt("isDuplicate"));
						imageAnalysisList.add(oneDetection);
					}
					analysisRs.close();
				}
				
				imageMap.put("detections", imageAnalysisList);
				
				returnList.add(imageMap);
			}
			imageAnalysisPs.close();
			imageAnalysisShowAllPs.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching store images to review :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------ITGAggregationDaoImpl--getStoreVisitImagesToReview completed");
		return returnList;
	}
	
	@Override
	public void updateStoreVisitAggregationData(int projectId, String storeId, String taskId, String visitDateId,
			List<Map<String,Object>> brandList, int bucketId, int reviewStatus, String reviewComments,String rejectReason) {
		LOGGER.info("---------------ITGAggregationDaoImpl--updateStoreVisitAggregationData::projectId={},storeID={},taskId={},visitDateId={},"
				+ "bucketId={},reviewStatus={},reviewComments={},rejectReason={}",projectId,storeId,taskId,visitDateId,bucketId,
				reviewStatus,reviewComments,rejectReason); 
		
		Connection conn = null;
		
		String deleteQuery = "DELETE FROM ITG_AGG_STORE_VISIT WHERE projectId = ? AND storeId = ? AND taskId = ? AND brandName = ?";
		
		String currentReviewStatusQuery = "SELECT reviewStatus FROM ITG_AGG_STORE_VISIT WHERE projectId = ? AND storeId = ? AND taskId = ?";
		
		String statusUpdateQuery = "UPDATE ITG_AGG_STORE_VISIT SET o_reviewStatus= ?, reviewStatus= ? , rejectReason = ? WHERE projectId = ? AND storeId = ? AND taskId = ?";

		String insertOrUpdateQuery = "INSERT INTO ITG_AGG_STORE_VISIT " + 
				" (projectId,storeId,taskId,visitDateId,brandName,facingCount,promotionFacingCount,oosFacingCount," + 
				" regularPrice,posPrice,multipackPrice,bucketId,reviewStatus,reviewComments,rejectReason,brandLevelReview) " + 
				" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " + 
				" ON DUPLICATE KEY UPDATE " + 
				"  o_facingCount = facingCount, " + 
				"  o_promotionFacingCount = promotionFacingCount, " + 
				"  o_oosFacingCount = oosFacingCount, " + 
				"  o_regularPrice = regularPrice, " + 
				"  o_posPrice = posPrice, " + 
				"  o_multipackPrice = multipackPrice, " +
				"  facingCount = VALUES(facingCount), " + 
				"  promotionFacingCount = VALUES(promotionFacingCount), " + 
				"  oosFacingCount = VALUES(oosFacingCount), " + 
				"  regularPrice = VALUES(regularPrice), " + 
				"  posPrice = VALUES(posPrice), " + 
				"  multipackPrice = VALUES(multipackPrice) ";
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement insertOrUpdatePs = conn.prepareStatement(insertOrUpdateQuery);
			PreparedStatement deletePs = conn.prepareStatement(deleteQuery);
			PreparedStatement statusUpdatePs = conn.prepareStatement(statusUpdateQuery);
			PreparedStatement currentReviewStatusPs = conn.prepareStatement(currentReviewStatusQuery);
			
			currentReviewStatusPs.setInt(1, projectId);
			currentReviewStatusPs.setString(2, storeId);
			currentReviewStatusPs.setString(3, taskId);
			ResultSet curReviewStatusRs = currentReviewStatusPs.executeQuery();
			int currentReviewStatus = 0;
			if(curReviewStatusRs.next()) {
				currentReviewStatus = curReviewStatusRs.getInt("reviewStatus");
			}
			currentReviewStatusPs.close();
			
			boolean anyUpdate  = false, anyDelete = false;
			for(Map<String,Object> brandData : brandList) {
				Object action  = brandData.get("action");
				if ( action == null ) {
					insertOrUpdatePs.setInt(1, projectId);
					insertOrUpdatePs.setString(2, storeId);
					insertOrUpdatePs.setString(3, taskId);
					insertOrUpdatePs.setString(4, visitDateId);
					insertOrUpdatePs.setString(5, (String) brandData.get("brandName"));
					insertOrUpdatePs.setInt(6, (int) brandData.get("facingCount"));
					insertOrUpdatePs.setInt(7, (int) brandData.get("promotionFacingCount"));
					insertOrUpdatePs.setInt(8, (int) brandData.get("oosFacingCount"));
					String regularPrice = (String) brandData.get("regularPrice");
					insertOrUpdatePs.setString(9, StringUtils.isBlank(regularPrice) ? null : regularPrice);
					String posPrice = (String) brandData.get("posPrice");
					insertOrUpdatePs.setString(10, StringUtils.isBlank(posPrice) ? null : posPrice);
					String multipackPrice = (String) brandData.get("multipackPrice");
					insertOrUpdatePs.setString(11, StringUtils.isBlank(multipackPrice) ? null : multipackPrice);
					insertOrUpdatePs.setInt(12, bucketId);
					insertOrUpdatePs.setInt(13, reviewStatus);
					insertOrUpdatePs.setString(14, reviewComments);
					insertOrUpdatePs.setString(15, rejectReason);
					insertOrUpdatePs.setInt(16, 0); //brandLevelReview = 0 if its a new brand getting added via review.
					insertOrUpdatePs.addBatch();
					anyUpdate = true;
				} else if ( action.equals("DELETE") ) {
					deletePs.setInt(1, projectId);
					deletePs.setString(2, storeId);
					deletePs.setString(3, taskId);
					deletePs.setString(4, (String) brandData.get("brandName"));
					deletePs.addBatch();
					anyDelete = true;
				}
			}
				
			if ( anyUpdate ) {
				insertOrUpdatePs.executeBatch(); //insert OR update
			}
			if ( anyDelete ) {
				deletePs.executeBatch(); //delete brands which are removed while reviewing
			}
			
			insertOrUpdatePs.close();
			deletePs.close();
			
			statusUpdatePs.setInt(1, currentReviewStatus);
			statusUpdatePs.setInt(2,reviewStatus);
			statusUpdatePs.setString(3, rejectReason);
			statusUpdatePs.setInt(4, projectId);
			statusUpdatePs.setString(5, storeId);
			statusUpdatePs.setString(6, taskId);
			statusUpdatePs.execute();
			
			conn.commit();
		}
		catch (Exception e) {
			LOGGER.error("Error while updating store visit aggregation data :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	              conn.setAutoCommit(true);
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--updateStoreVisitAggregationData completed");
	}
	
	@Override
	public Map<String, String> getPriceConfidenceData(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getPriceConfidenceData::projectId={},storeID={},taskId={}",projectId,storeId,taskId); 
		
		Connection conn = null;
		
		Map<String,String> brandPriceMap = new LinkedHashMap<String,String>();
		
		String query = "select \n" + 
				"    pm.brand_name,\n" + 
				"    pm.product_sub_type,\n" + 
				"    ia.price,\n" + 
				"    group_concat(distinct(ia.priceConfidence)) as confidences \n" + 
				" from\n" + 
				"    imageanalysisnew ia \n" + 
				" inner join\n" + 
				"    (\n" + 
				"        select \n" + 
				"            upc, \n" + 
				"            brand_name, \n" + 
				"            case when (product_sub_type='Cigarette Price' AND upc like '%\\_%') then 'Cigarette Regular Price' ELSE product_sub_type END as product_sub_type\n" + 
				"        from \n" + 
				"            productmaster \n" + 
				"        where product_sub_type in ( 'Cigarette Price', 'Cigarette Multipack Price')\n" + 
				"    ) pm\n" + 
				" on\n" + 
				"    ia.upc = pm.upc\n" + 
				" where\n" + 
				"    ia.projectId=? and ia.storeid=? AND ia.TASKID=?\n" + 
				" group by\n" + 
				"    pm.brand_name,pm.product_sub_type,ia.price\n" + 
				" order by\n" + 
				"    pm.brand_name,pm.product_sub_type,ia.price";
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setInt(1, projectId);
			ps.setString(2, storeId);
			ps.setString(3, taskId);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String brand = rs.getString("brand_name");
				String priceType = rs.getString("product_sub_type");
				String price = rs.getString("price");
				String confidences = rs.getString("confidences");
				
				brandPriceMap.put(brand.trim()+"#"+priceType.trim()+"#"+price, confidences);
				
			}
			rs.close();
			
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching price confidence data :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	              conn.setAutoCommit(true);
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		LOGGER.info("---------------ITGAggregationDaoImpl--getPriceConfidenceData completed");
		return brandPriceMap;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getITGProductBrands(String productType) {
		LOGGER.info("---------------ITGAggregationDaoImpl--getITGProductBrands::productType={}" , productType);
		
		Connection conn = null;
		String imagesQuery = "SELECT DISTINCT(BRAND_NAME) AS brandName FROM ProductMaster "
				+ " WHERE PRODUCT_TYPE = 'CIGARETTE' AND PRODUCT_SUB_TYPE='CIGARETTE'"
				+ " ORDER BY BRAND_NAME";
		
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String, Object>>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement brandsPs = conn.prepareStatement(imagesQuery);
			
			ResultSet brandsRs = brandsPs.executeQuery();
			
			while(brandsRs.next()) {
				LinkedHashMap<String,Object> oneImage = new LinkedHashMap<String,Object>();
				oneImage.put("brandName", brandsRs.getString("brandName"));
				returnList.add(oneImage);
			}

			brandsRs.close();
			brandsPs.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching brands list to review :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------ITGAggregationDaoImpl--getITGProductBrands completed");
		return returnList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getITGReviewStatsSummary() {
		LOGGER.info("---------------ITGAggregationDaoImpl--getITGReviewStatsSummary=------" );
		
		Connection conn = null;
		
		String dayPhotoCountQuery = 
				"SELECT taskid,count(*) as photoCount, (count(*) / count(distinct(storeid))) as avgPerStore from imagestorenew where  projectId=1643 and taskid>'20200726' group by taskid order by taskid;";
		String dayAgentCountQuery = 
				"Select taskid,count(distinct(agentId)) as agentCount from ProjectStoreResult where projectId=1643 and taskid>'20200726' group by taskid order by taskid;"; 
		String dayStoreCountQuery = 
				"SELECT taskid,count(*) as storeCount from ProjectStoreResult where projectId=1643 and taskid>'20200726' group by taskid order by taskid;";
		String dayReviewStoreCountQuery = 
				"SELECT taskid,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and ( reviewstatus='0' OR o_reviewstatus='0' ) and taskid>'20200726' group by taskid  order by taskid;"; 
		String dayAutoRejectStoreCountQuery = 
				"SELECT taskid,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and ( reviewstatus='2' and o_reviewstatus is null) and bucketId=13 and taskid>'20200726' group by taskid order by taskid;";
		String dayReviewRejectStoreCountQuery = 
				"SELECT taskid,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and ( reviewstatus='2' and o_reviewstatus is not null) and bucketId<>13 and taskid>'20200726' group by taskid order by taskid;" ; 
		String dayReviewApprovedStoreCountQuery = 
				"SELECT taskid,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and ( reviewstatus='1' AND o_reviewstatus is not null ) and taskid>'20200726' group by taskid  order by taskid;" ;
		String dayDashboardStoreCountQuery = 
				"SELECT taskid,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and ( reviewstatus='1' ) and taskid>'20200726' group by taskid  order by taskid;";
		String dayReviewCommentStoreCountQuery = 
				"select taskid,reviewcomments,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and (reviewcomments is not null  and reviewcomments <> '' and reviewcomments not in ('14','15') ) and taskid>'20200726' group by taskid ,reviewcomments order by taskid,reviewcomments;";
		String dayRejectReasonStoreCountQuery = 
				"select taskid,rejectreason,count(distinct(storeid)) as storeCount from itg_agg_store_visit where projectId=1643 and reviewstatus='2' and taskid>'20200726' group by taskid ,rejectReason order by taskid,rejectreason;";
		String dayImageQCStoreCountQuery = "SELECT taskid,count(distinct(storeid)) as storeCount ,count(*) photoCount from imagestorenew where projectId=1643 and taskid>'20200726'\n" + 
				"	and imagenotusablecomment is not null and imagenotusablecomment <> '' group by taskid order by taskid;";
		String dayImageQCCountQuery = 
				"SELECT taskid,imagenotusablecomment,count(distinct(storeid)) as storeCount ,count(*) photoCount from imagestorenew where projectId=1643 and taskid>'20200726' \n" + 
				"    and imagenotusablecomment is not null and imagenotusablecomment <> '' group by taskid,imagenotusablecomment order by taskid, imagenotusablecomment;";
		
		
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> dayMap = new LinkedHashMap<String, Object>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement dayPhotoCountQueryPs = conn.prepareStatement(dayPhotoCountQuery);
			PreparedStatement dayAgentCountQueryPs = conn.prepareStatement(dayAgentCountQuery);
			PreparedStatement dayStoreCountQueryPs = conn.prepareStatement(dayStoreCountQuery);
			PreparedStatement dayReviewStoreCountQueryPs = conn.prepareStatement(dayReviewStoreCountQuery);
			PreparedStatement dayAutoRejectStoreCountQueryPs = conn.prepareStatement(dayAutoRejectStoreCountQuery);
			PreparedStatement dayReviewRejectStoreCountQueryPs = conn.prepareStatement(dayReviewRejectStoreCountQuery);
			PreparedStatement dayReviewApprovedStoreCountQueryPs = conn.prepareStatement(dayReviewApprovedStoreCountQuery);
			PreparedStatement dayDashboardStoreCountQueryPs = conn.prepareStatement(dayDashboardStoreCountQuery);
			PreparedStatement dayReviewCommentStoreCountQueryPs = conn.prepareStatement(dayReviewCommentStoreCountQuery);
			PreparedStatement dayRejectReasonStoreCountQueryPs = conn.prepareStatement(dayRejectReasonStoreCountQuery);
			PreparedStatement dayImageQCStoreCountQueryPs = conn.prepareStatement(dayImageQCStoreCountQuery);
			PreparedStatement dayImageQCCountQueryPs = conn.prepareStatement(dayImageQCCountQuery);

			
			ResultSet rs = dayPhotoCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int photoCount = rs.getInt("photoCount");
				double avgPhotoPerStore = rs.getDouble("avgPerStore");
				LinkedHashMap<String,Object> data = new LinkedHashMap<String,Object>();
				data.put("photoCount", photoCount);
				data.put("avgPhotoPerStore", avgPhotoPerStore);
				dayMap.put(dateId, data);
			}
			rs.close();
			
			rs = dayAgentCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int agentCount = rs.getInt("agentCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("agentCount", agentCount);
			}
			rs.close();
			
			rs = dayStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("storeCount", storeCount);
			}
			rs.close();
			
			rs = dayReviewStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("reviewFlaggedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayAutoRejectStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("autoRejectedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayReviewRejectStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("reviewRejectedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayReviewApprovedStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("reviewApprovedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayDashboardStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("publishedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayReviewCommentStoreCountQueryPs.executeQuery();
			Map<String,Map<String,Integer>> reviewDistribution = new HashMap<String,Map<String,Integer>>();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				if ( reviewDistribution.get(dateId) == null ) {
					reviewDistribution.put(dateId, new HashMap<String,Integer>());
				}
				String reviewComments = rs.getString("reviewComments");
				int storeCount = rs.getInt("storeCount");
				String[] reviewCommentParts = reviewComments.split(",");
				for(String comment : reviewCommentParts ) {
					comment = comment.trim();
					if ( !comment.isEmpty() ) {
						if ( reviewDistribution.get(dateId).get(comment) == null ) {
							reviewDistribution.get(dateId).put(comment, 0);
						}
						reviewDistribution.get(dateId).put(comment,reviewDistribution.get(dateId).get(comment)+storeCount);
					}
				}
				
			}
			for(String dateId : reviewDistribution.keySet()) {
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("reviewDistribution",reviewDistribution.get(dateId));
			}
			rs.close();
			
			rs = dayRejectReasonStoreCountQueryPs.executeQuery();
			Map<String,Map<String,Integer>> rejectDistribution = new HashMap<String,Map<String,Integer>>();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				if ( rejectDistribution.get(dateId) == null ) {
					rejectDistribution.put(dateId, new HashMap<String,Integer>());
				}
				String rejectrReason = rs.getString("rejectreason");
				int storeCount = rs.getInt("storeCount");
				String[] rejectrReasonParts = rejectrReason.split(",");
				for(String reason : rejectrReasonParts ) {
					reason = reason.trim();
					if ( !reason.isEmpty() ) {
						if ( rejectDistribution.get(dateId).get(reason) == null ) {
							rejectDistribution.get(dateId).put(reason, 0);
						}
						rejectDistribution.get(dateId).put(reason,rejectDistribution.get(dateId).get(reason)+storeCount);
					}
				}
				
			}
			for(String dateId : rejectDistribution.keySet()) {
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("rejectDistribution",rejectDistribution.get(dateId));
			}
			rs.close();
			
			rs = dayImageQCStoreCountQueryPs.executeQuery();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				int storeCount = rs.getInt("storeCount");
				int photoCount = rs.getInt("photoCount");
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("qcFlaggedPhotoCount", photoCount);
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("qcFlaggedStoreCount", storeCount);
			}
			rs.close();
			
			rs = dayImageQCCountQueryPs.executeQuery();
			Map<String,Map<String,Map<String,Integer>>> qcDistribution = new HashMap<String,Map<String,Map<String,Integer>>>();
			while(rs.next()) {
				String dateId = rs.getString("taskId");
				if ( qcDistribution.get(dateId) == null ) {
					qcDistribution.put(dateId, new HashMap<String,Map<String,Integer>>());
				}
				String imagenotusablecomment = rs.getString("imagenotusablecomment");
				int storeCount = rs.getInt("storeCount");
				int photoCount = rs.getInt("photoCount");
				String[] imagenotusablecommentParts = imagenotusablecomment.split(",");
				for(String comment : imagenotusablecommentParts ) {
					comment = comment.trim();
					if ( !comment.isEmpty() ) {
						if ( qcDistribution.get(dateId).get(comment) == null ) {
							Map<String,Integer> counts = new HashMap<String,Integer>();
							counts.put("storeCount", 0);
							counts.put("photoCount", 0);
							qcDistribution.get(dateId).put(comment, counts);
						}
						
						Map<String,Integer> qcMap = qcDistribution.get(dateId).get(comment);
						qcMap.put("storeCount",qcMap.get("storeCount")+storeCount);
						qcMap.put("photoCount",qcMap.get("photoCount")+photoCount);
						qcDistribution.get(dateId).put(comment, qcMap);
					}
				}
				
			}
			for(String dateId : qcDistribution.keySet()) {
				((LinkedHashMap<String,Object>)dayMap.get(dateId)).put("imageQcDistribution",qcDistribution.get(dateId));
			}
			rs.close();

			dayPhotoCountQueryPs.close();
			dayAgentCountQueryPs.close();
			dayStoreCountQueryPs.close();
			dayReviewStoreCountQueryPs.close();
			dayAutoRejectStoreCountQueryPs.close();
			dayReviewRejectStoreCountQueryPs.close();
			dayReviewApprovedStoreCountQueryPs.close();
			dayDashboardStoreCountQueryPs.close();
			dayReviewCommentStoreCountQueryPs.close();
			dayRejectReasonStoreCountQueryPs.close();
			dayImageQCStoreCountQueryPs.close();
			dayImageQCCountQueryPs.close();
			
			
			for(String day : dayMap.keySet() ) {
				LinkedHashMap<String,Object> returnMap = new LinkedHashMap<String,Object>();
				returnMap.put("date", day);
				returnMap.put("stats", dayMap.get(day));
				returnList.add(returnMap);
			}
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching review stats :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------ITGAggregationDaoImpl--getITGReviewStatsSummary completed");
		return returnList;
	}
	
}

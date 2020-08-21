package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.AltriaDashboardDao;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Anoop on 09/10/18.
 */
@Component(value = BeanMapper.BEAN_ALTRIA_DASHBOARD_DAO)
@Scope("prototype")
public class AltriaDashboardDaoImpl implements AltriaDashboardDao {

    private static final String ALTRIA_BRAND_SKU_TYPE = "1";

    private static final String NON_ALTRIA_BRAND_SKU_TYPE = "3";
    
    private static final String ALTRIA_SIGNAGE_SKU_TYPE = "4";
    
    private static final String NON_ALTRIA_SIGNAGE_SKU_TYPE = "6";
    
    private static final String ALTRIA_WARNING_SIGNAGE_SKU_TYPE = "7";
    
    private static final String ALTRIA_WARNING_SIGNAGE_CATEGORY_ID = "80";

    private static final int ALTRIA_TOTAL_STORES_COUNTRY_LEVEL = 702;
    
    private static final Map<String,List<String>> MONTH_TO_SALES_CYCLE_MAP = new HashMap<String,List<String>>();
    
    static {
    	MONTH_TO_SALES_CYCLE_MAP.put("201809", Arrays.asList("20180825","20180928"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201810", Arrays.asList("20180929","20181027"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201811", Arrays.asList("20181028","20181124"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201812", Arrays.asList("20181125","20181229"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201901", Arrays.asList("20181230","20190126"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201902", Arrays.asList("20190127","20190223"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201903", Arrays.asList("20190224","20190330"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201904", Arrays.asList("20190331","20190427"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201905", Arrays.asList("20190428","20190525"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201906", Arrays.asList("20190526","20190629"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201907", Arrays.asList("20190630","20190727"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201908", Arrays.asList("20190728","20190824"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201909", Arrays.asList("20190825","20190928"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201910", Arrays.asList("20190929","20191026"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201911", Arrays.asList("20191027","20191123"));
    	MONTH_TO_SALES_CYCLE_MAP.put("201912", Arrays.asList("20191124","20191228"));
    }
    
	//Round to 1 decimal point
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#");

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

	@Override
	public LinkedHashMap<String, String> getAltriaProjectSummary(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectSummary :: input={}----------------\n", queryArgs);

		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlSummaryFileName = "brandSummary_country.sql";
		String sqlWarningSignFileName = "warningSignSummary_country.sql";
		
		if ( territoryAgg ) {
			sqlSummaryFileName = "brandSummary_territory.sql";
			sqlWarningSignFileName = "warningSignSummary_territory.sql";
		}
		
	    String sqlSummary = ConverterUtil.getResourceFromClasspath(sqlSummaryFileName, "/queries/altria/");
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sqlSummary = sqlSummary.replaceAll("::storeIds", listOfStoresToFilter);
        }
        
	    String sqlWarningSign = ConverterUtil.getResourceFromClasspath(sqlWarningSignFileName, "/queries/altria/");
	    if ( territoryAgg ) {
	    	sqlWarningSign = sqlWarningSign.replaceAll("::storeIds", listOfStoresToFilter);
	    }
	    
	    String month = (String)queryArgs.get("month");
	    String year = (String)queryArgs.get("year");
	    List<String> salesCycle  = MONTH_TO_SALES_CYCLE_MAP.get(year+month);

	    LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement psSummary = conn.prepareStatement(sqlSummary);
            PreparedStatement psWarningSign = conn.prepareStatement(sqlWarningSign);

            psSummary.setString(1, (String)queryArgs.get("customerCode"));
            psSummary.setInt(2, Integer.parseInt((String)queryArgs.get("projectId")));
            psSummary.setString(3, salesCycle.get(0));
            psSummary.setString(4, salesCycle.get(1));
            
            psWarningSign.setString(1, (String)queryArgs.get("customerCode"));
            psWarningSign.setInt(2, Integer.parseInt((String)queryArgs.get("projectId")));
            psWarningSign.setString(3, salesCycle.get(0));
            psWarningSign.setString(4, salesCycle.get(1));
            
            ResultSet rsSummary = psSummary.executeQuery();
            ResultSet rsWarningSign = psWarningSign.executeQuery();
            
            String storesWithData = "0";
            if (rsSummary.next()) {
                storesWithData = rsSummary.getString("storesWithData");
            }
            rsSummary.close();
            psSummary.close();
            
            String storesWithWarningSignage = "0";
            String storesWithBothWarningSignage = "0";
            if (rsWarningSign.next()) {
            	storesWithWarningSignage = rsWarningSign.getString("storesWithWarningSignage");
            	storesWithBothWarningSignage = rsWarningSign.getString("storesWithBothWarningSignage");
            }
            rsWarningSign.close();
            psWarningSign.close();
            
            if (territoryAgg) {
            	float percentageStoresWithData = (float) Integer.parseInt(storesWithData)/storesForTerritory.size();
            	float percentageStoresWithWarningSignage = (float) Integer.parseInt(storesWithWarningSignage)/Integer.parseInt(storesWithData);
            	float percentageStoresWithBothWarningSignage = (float) Integer.parseInt(storesWithBothWarningSignage)/Integer.parseInt(storesWithData);
            	result.put("totalStores", ""+storesForTerritory.size());
            	result.put("percentageStoresWithData", storesWithData.equals("0") ? "0.0" : DECIMAL_FORMATTER.format(percentageStoresWithData*100));
            	result.put("percentageStoresWithWarningSignage", storesWithData.equals("0") ? "0.0" : DECIMAL_FORMATTER.format(percentageStoresWithWarningSignage*100));
            	result.put("percentageStoresWithBothWarningSignage", storesWithData.equals("0") ? "0.0" : DECIMAL_FORMATTER.format(percentageStoresWithBothWarningSignage*100));
            } else {
            	float percentageStoresWithData = (float) Integer.parseInt(storesWithData)/ALTRIA_TOTAL_STORES_COUNTRY_LEVEL;
            	float percentageStoresWithWarningSignage = (float) Integer.parseInt(storesWithWarningSignage)/Integer.parseInt(storesWithData);
            	result.put("totalStores", ""+ALTRIA_TOTAL_STORES_COUNTRY_LEVEL);
            	result.put("percentageStoresWithData", storesWithData.equals("0") ? "0.0" : DECIMAL_FORMATTER.format(percentageStoresWithData*100));
            	result.put("percentageStoresWithWarningSignage", storesWithData.equals("0") ? "0.0" : DECIMAL_FORMATTER.format(percentageStoresWithWarningSignage*100));
            }
            result.put("storesWithData", storesWithData);
            result.put("storesWithWarningSignage", storesWithWarningSignage);
            result.put("storesWithBothWarningSignage", storesWithBothWarningSignage);
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectSummary----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
	}

	@Override
	public Map<String, LinkedHashMap<String, Object>> getAltriaProjectBrandShares(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectBrandShares ::"
				+ "input={}----------------\n", queryArgs);
		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlFileName = "brandShare_country.sql";
		if ( territoryAgg ) sqlFileName = "brandShare_territory.sql";
		
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/altria/");
	    
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        }
        
        Map<String, LinkedHashMap<String,Object>>  result = new HashMap<String,LinkedHashMap<String, Object>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(2, (String)queryArgs.get("month"));
            ps.setString(3, (String)queryArgs.get("year"));
            ps.setInt(4, Integer.parseInt((String)queryArgs.get("parentProjectId")));
            ps.setString(5, (String)queryArgs.get("month"));
            ps.setString(6, (String)queryArgs.get("year"));
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	LinkedHashMap<String,Object> oneBrand = new LinkedHashMap<String,Object>();
            	oneBrand.put("brandName", rs.getString("brandName"));
            	oneBrand.put("brandFacings", rs.getString("brandFacings"));
            	oneBrand.put("totalFacings", rs.getString("totalFacings"));
            	oneBrand.put("brandShare", rs.getString("brandShare"));
            	result.put(rs.getString("brandName"), oneBrand);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectBrandShares----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	@Override
	public Map<String, LinkedHashMap<String,Object>> getAltriaProjectBrandAvailability(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectBrandAvailability ::"
				+ "input={}----------------\n", queryArgs);
		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlFileName = "brandAvailability_country.sql";
		if ( territoryAgg ) sqlFileName = "brandAvailability_territory.sql";
		
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/altria/");
	    
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        	
        	List<String> skuTypeIds = (List<String>) queryArgs.get("skuTypeIds");
        	step1 = StringUtils.join(skuTypeIds, "\", \"");// Join with ", "
        	String listOfSkuTypeIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::skuTypeIds", listOfSkuTypeIdsToFilter);
        }
        
        String month = (String)queryArgs.get("month");
        String year = (String)queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(year+month);
	    
	    Map<String,LinkedHashMap<String,Object>> result = new LinkedHashMap<String,LinkedHashMap<String,Object>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(2, salesCycle.get(0));
            ps.setString(3, salesCycle.get(1));
            ps.setInt(4, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setInt(5, Integer.parseInt((String)queryArgs.get("parentProjectId")));
            ps.setString(6, salesCycle.get(0));
            ps.setString(7, salesCycle.get(1));
            ps.setInt(8, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(9, salesCycle.get(0));
            ps.setString(10, salesCycle.get(1));
            ps.setInt(11, Integer.parseInt((String)queryArgs.get("projectId")));

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	LinkedHashMap<String,Object> oneBrand = new LinkedHashMap<String,Object>();
            	oneBrand.put("brandName", rs.getString("brandName"));
            	oneBrand.put("storesWithBrand", rs.getString("storesWithBrand"));
            	oneBrand.put("storesWithData", rs.getString("storesWithData"));
            	oneBrand.put("percentageStoresWithBrand", rs.getString("percentageStoresWithBrand"));
            	oneBrand.put("productsFoundForBrand", rs.getString("productsFoundForBrand"));
            	oneBrand.put("avgProductsForBrand", rs.getString("avgProductsForBrand"));
            	oneBrand.put("storesWithAllProducts", rs.getString("storesWithAllProducts"));
            	oneBrand.put("percentageStoresWithAllProductsForBrand", rs.getString("percentageStoresWithAllProductsForBrand"));

            	result.put(rs.getString("brandName"), oneBrand);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectBrandAvailability----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	@Override
	public Map<String, LinkedHashMap<String, Object>>  getAltriaProjectWarningSignAvailability(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectWarningSignAvailability ::"
				+ "input={}----------------\n", queryArgs);

		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlFileName = "warningSign_country.sql";
		if ( territoryAgg ) sqlFileName = "warningSign_territory.sql";
		
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/altria/");
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        }
        
        String month = (String)queryArgs.get("month");
        String year = (String)queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(year+month);
        
        Map<String, LinkedHashMap<String, Object>>  result = new HashMap<String, LinkedHashMap<String, Object>> ();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, (String)queryArgs.get("customerCode"));
            ps.setInt(2, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(3, salesCycle.get(0));
            ps.setString(4, salesCycle.get(1));
            ps.setString(5, (String)queryArgs.get("customerCode"));
            ps.setInt(6, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(7, (String)queryArgs.get("customerCode"));
            ps.setInt(8, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(9, salesCycle.get(0));
            ps.setString(10, salesCycle.get(1));

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	LinkedHashMap<String,Object> oneWarningSign = new LinkedHashMap<String,Object>();
            	oneWarningSign.put("warningSignUpc", rs.getString("warningSignUpc"));
            	oneWarningSign.put("warningSignName", rs.getString("warningSignName"));
            	oneWarningSign.put("storesWithWarningSign", rs.getString("storesWithWarningSign"));
            	oneWarningSign.put("storesWithData", rs.getString("storesWithData"));
            	oneWarningSign.put("percentageStoresWithWarningSign", rs.getString("percentageStoresWithWarningSign"));
            	result.put(rs.getString("warningSignUpc"), oneWarningSign);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectWarningSignAvailability----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	@Override
	public Map<String, LinkedHashMap<String, Object>> getAltriaProjectProductAvailability(
			Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectProductAvailability ::"
				+ "input={}----------------\n", queryArgs);
		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlFileName = "productAvailability_country.sql";
		if ( territoryAgg ) sqlFileName = "productAvailability_territory.sql";
		
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/altria/");
	    
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        	
        	List<String> skuTypeIds = (List<String>) queryArgs.get("skuTypeIds");
        	step1 = StringUtils.join(skuTypeIds, "\", \"");// Join with ", "
        	String listOfSkuTypeIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::skuTypeIds", listOfSkuTypeIdsToFilter);
        }
	    
        String month = (String)queryArgs.get("month");
        String year = (String)queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(year+month);
        
	    Map<String,LinkedHashMap<String,Object>> result = new LinkedHashMap<String,LinkedHashMap<String,Object>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(2, salesCycle.get(0));
            ps.setString(3, salesCycle.get(1));
            ps.setInt(4, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setInt(5, Integer.parseInt((String)queryArgs.get("parentProjectId")));
            ps.setString(6, salesCycle.get(0));
            ps.setString(7, salesCycle.get(1));

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	LinkedHashMap<String,Object> oneProduct = new LinkedHashMap<String,Object>();
            	String brandName = rs.getString("brandName");
            	String productName = rs.getString("productName");
            	oneProduct.put("productName", productName);
            	oneProduct.put("brandName", brandName);
            	oneProduct.put("storesWithProduct", rs.getString("storesWithProduct"));
            	oneProduct.put("storesWithData", rs.getString("storesWithData"));
            	oneProduct.put("percentageStoresWithProduct", rs.getString("percentageStoresWithProduct"));
            	if ( result.get(brandName) == null ) {
            		LinkedHashMap<String,Object> oneBrand = new LinkedHashMap<String,Object>();
            		result.put(brandName, oneBrand);
            	}
            	result.get(brandName).put(productName, oneProduct);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectProductAvailability----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	@Override
	public List<LinkedHashMap<String, String>> getAltriaProjectAllStoreResults(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectAllStoreResults ::"
				+ "input={}----------------\n", queryArgs);
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();
		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		String sqlFileName = "stores_country.sql";
		if ( territoryAgg ) sqlFileName = "stores_territory.sql";
    	
		String visitMonth = (String)queryArgs.get("month");
		String visitYear = (String)queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);
        queryArgs.put("salesCycle", salesCycle);
		
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/altria/");
	    
	    List<String> childProjectIds = (List<String>) queryArgs.get("childProjectIds");
		String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String storesWithWarningSignSql = "SELECT distinct(storeId) "
        		+ "FROM ProjectStoreData "
        		+ "WHERE parentProjectId = ? AND storeId in (::storeIds) "
        		+ "AND skuTypeId = ? AND visitDate BETWEEN ? AND ?";
    	
    	String storeCategoriesSql = "SELECT storeId, GROUP_CONCAT(distinct projectId) as projects "
    			+ "FROM ProjectStoreData "
    			+ "WHERE parentProjectId = ? AND storeId IN (::storeIds) AND skuTypeId IN ('1','2') AND visitDate BETWEEN ? AND ? group by storeId" ; 

        if ( territoryAgg ) {
        	List<String> storesForTerritory = (List<String>) queryArgs.get("stores");
        	step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	String listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        	storesWithWarningSignSql = storesWithWarningSignSql.replaceAll("::storeIds", listOfStoresToFilter);
        	storeCategoriesSql = storeCategoriesSql.replaceAll("::storeIds", listOfStoresToFilter);
        }
	    
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement psStoresWithWarnSign = conn.prepareStatement(storesWithWarningSignSql);
            PreparedStatement psStoreCategories = conn.prepareStatement(storeCategoriesSql);


            List<String> storesWithWarningSign = getStoresWithWarningSign(queryArgs, psStoresWithWarnSign);
            
            Map<String,String> missingCategoriesByStore = getMissingCategoriesByStore(queryArgs, psStoreCategories);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, salesCycle.get(0) );
            ps.setString(2, salesCycle.get(1) );
            ps.setString(3, salesCycle.get(0) );
            ps.setString(4, salesCycle.get(1) );
            ps.setString(5, (String)queryArgs.get("customerCode")); 

            ResultSet rs = ps.executeQuery();
            Map<String,LinkedHashMap<String,String>> resultMap = new HashMap<String,LinkedHashMap<String,String>>();
            while (rs.next()) {
            	LinkedHashMap<String,String> oneStore = new LinkedHashMap<String,String>();
            	String storeId = rs.getString("storeId");
            	oneStore.put("storeId", storeId);
            	oneStore.put("customerStoreNumber", rs.getString("customerStoreNumber"));
            	oneStore.put("street", rs.getString("street"));
            	oneStore.put("city", rs.getString("city"));
            	oneStore.put("stateCode", rs.getString("stateCode"));
            	oneStore.put("imageUUID", rs.getString("imageUUID"));
            	oneStore.put("projectId", rs.getString("projectId"));
            	oneStore.put("resultCode", "1");
            	if ( !resultMap.containsKey(storeId)) {
            		String resultComment = "";
            		if (missingCategoriesByStore.containsKey(storeId) ){
                		oneStore.put("resultCode", "2");
                		resultComment = missingCategoriesByStore.get(storeId);
                	}
            		if (!storesWithWarningSign.contains(storeId)) {
                		oneStore.put("resultCode","3");
                		resultComment = "YTP or We Card signage, " + resultComment;
                	}
            		if ( StringUtils.isNotBlank(resultComment)) {
            			resultComment = "Missing " + resultComment.trim();
            			if ( resultComment.endsWith(",") ) {
                    		resultComment = resultComment.substring(0,resultComment.length() - 1);
            			}
                		oneStore.put("resultComment",resultComment);
            		}
            		resultMap.put(storeId, oneStore);
            	}
            }
            rs.close();
            ps.close();
            
            for(String storeId : resultMap.keySet()) {
            	result.add(resultMap.get(storeId));
            }
            
            result.sort(Comparator.comparing(
                    m -> m.get("resultCode"), 
                    Comparator.nullsLast(Comparator.reverseOrder()))
               );
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectAllStoreResults----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	private Map<String, String> getMissingCategoriesByStore(Map<String, Object> queryArgs, PreparedStatement psStoreCategories) throws SQLException {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getMissingCategoriesByStore ::"
				+ "input={}----------------\n", queryArgs);
		int parentProjectId = Integer.parseInt((String)queryArgs.get("parentProjectId"));
		List<String> salesCycle = (List<String>)queryArgs.get("salesCycle");

		psStoreCategories.setInt(1, Integer.parseInt((String)queryArgs.get("parentProjectId")));
		psStoreCategories.setString(2, salesCycle.get(0) );
		psStoreCategories.setString(3, salesCycle.get(1));
		
		ResultSet rsStoreCategories = psStoreCategories.executeQuery();
		Map<String,String> storeCategories = new HashMap<String,String>();
		while (rsStoreCategories.next()) {
			storeCategories.put(rsStoreCategories.getString("storeId"),rsStoreCategories.getString("projects"));
		}
		rsStoreCategories.close();
		psStoreCategories.close();
		
		List<LinkedHashMap<String, String>> childProjects = metaServiceDao.listChildProjects((String)queryArgs.get("customerCode"), parentProjectId);
		Map<String,String> projectToCategoryMap = new HashMap<String,String>();
		for ( LinkedHashMap<String, String> childProject : childProjects ) {
			if (! childProject.get("categoryId").equals(ALTRIA_WARNING_SIGNAGE_CATEGORY_ID) ) {
				projectToCategoryMap.put(childProject.get("id"), childProject.get("category"));
			}
		}
		
		Map<String,String> storesWithMissingCategories = new HashMap<String,String>();
		for ( String store : storeCategories.keySet() ) {
			String projectList = storeCategories.get(store);
			List<String> listOfProjects = Arrays.asList(projectList.split(","));
			for ( String project : projectToCategoryMap.keySet() ) {
				if ( !listOfProjects.contains(project) ) {
					String category = storesWithMissingCategories.get(store);
					if ( StringUtils.isBlank(category)) {
						category = "";
					} else {
						category = category + ", ";
					}
					storesWithMissingCategories.put(store,  category + projectToCategoryMap.get(project));
				}
			}
		}
		LOGGER.info("---------------AltriaDashboardDaoImpl Ends getMissingCategoriesByStore ::{}----------------\n", storesWithMissingCategories);
		return storesWithMissingCategories;
	}

	private List<String> getStoresWithWarningSign(Map<String, Object> queryArgs, PreparedStatement psStoresWithWarnSign)
			throws SQLException {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getStoresWithWarningSign ::"
				+ "input={}----------------\n", queryArgs);
		
		List<String> salesCycle = (List<String>)queryArgs.get("salesCycle");
		
		psStoresWithWarnSign.setInt(1, Integer.parseInt((String)queryArgs.get("parentProjectId")));
		psStoresWithWarnSign.setString(2, ALTRIA_WARNING_SIGNAGE_SKU_TYPE);
		psStoresWithWarnSign.setString(3, salesCycle.get(0) );
		psStoresWithWarnSign.setString(4, salesCycle.get(1));
		
		ResultSet rsWarnSign = psStoresWithWarnSign.executeQuery();
		List<String> storesWithWarningSign = new ArrayList<String>();
		while (rsWarnSign.next()) {
			storesWithWarningSign.add(rsWarnSign.getString("storeId"));
		}
		rsWarnSign.close();
		psStoresWithWarnSign.close();
		LOGGER.info("---------------AltriaDashboardDaoImpl Ends getStoresWithWarningSign ::"
				+ storesWithWarningSign+"----------------\n");
		return storesWithWarningSign;
	}

	@Override
	public List<LinkedHashMap<String, String>> getAltriaProjectStoreImagesByStore(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectStoreImagesByStore ::"
				+ "input={}----------------\n", queryArgs);
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();
    	
		String visitMonth = (String)queryArgs.get("month");
		String visitYear = (String)queryArgs.get("year");
		List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);

	    String sql = "SELECT" + 
	    		"    ImageStoreNew.imageUUID, ImageStoreNew.projectId, Category.name as category" + 
	    		"  FROM" + 
	    		"    ImageStoreNew, Category" + 
	    		"  WHERE" + 
	    		"    ImageStoreNew.projectId IN (" + 
	    		"        SELECT project.id FROM Project project  " + 
	    		"        INNER JOIN ( " + 
	    		"            SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ? " + 
	    		"        ) customerMappedProjects  " + 
	    		"        ON ( project.id = customerMappedProjects.projectId ) " + 
	    		"        WHERE project.parentProjectId = ? AND project.status = '1'" + 
	    		"    )" + 
	    		"    AND ImageStoreNew.storeId = ?" + 
	    		"    AND ImageStoreNew.dateId BETWEEN ? AND ?" +
	    		"    AND ImageStoreNew.categoryId = Category.id";
	    
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, (String)queryArgs.get("customerCode") );
            ps.setInt(2, Integer.parseInt((String)queryArgs.get("parentProjectId")) );
            ps.setString(3, (String)queryArgs.get("store") );
            ps.setString(4, salesCycle.get(0) );
            ps.setString(5, salesCycle.get(1) );

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	LinkedHashMap<String,String> oneImage = new LinkedHashMap<String,String>();
            	oneImage.put("imageUUID", rs.getString("imageUUID"));
            	oneImage.put("projectId", rs.getString("projectId"));
            	oneImage.put("category", rs.getString("category"));
            	result.add(oneImage);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectStoreImagesByStore----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

    @Override
    public List<LinkedHashMap<String, String>> getAltriaStoresForReview(InputObject inputObject) {
        LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaStoresForReview :: input = {}----------------\n", inputObject);
        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();
        int batchId = Integer.parseInt(inputObject.getBatchId());

        String sql = "select " +
                "DISTINCT(ps.storeId), sm.Street as street, sm.City as city, sm.StateCode as stateCode " +
                "from ProjectStoreResult ps " +
                "LEFT JOIN StoreMaster sm " +
                    "ON sm.StoreID = ps.storeId " +
                "where ps.visitDateId between ? and ? " +
                "and ps.status = 0 " +
                "and ps.resultCode <> '99' " +
                "and ps.projectId " +
                " in (SELECT project.id FROM Project project " +
                " INNER JOIN (" +
                " SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" +
                " ) customerMappedProjects " +
                " ON ( project.id = customerMappedProjects.projectId )" +
                " WHERE project.parentProjectId = ? and project.status = 1" +
                " )";
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(inputObject.getValue());
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, salesCycle.get(0));
            ps.setString(2, salesCycle.get(1));
            ps.setString(3, inputObject.getCustomerCode());
            ps.setInt(4, inputObject.getProjectId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	String storeId = rs.getString("storeId");
            	if ( (storeId.chars().sum() % 10) == batchId ) {
                    LinkedHashMap<String,String> store = new LinkedHashMap<String,String>();
                    store.put("storeId", storeId);
                    store.put("street", rs.getString("street"));
                    store.put("city", rs.getString("city"));
                    store.put("stateCode", rs.getString("stateCode"));
                    result.add(store);
            	}
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaStoresForReview----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getAltriaStoreImagesForReview(InputObject inputObject) {
	    LOGGER.info("-----------------------AltriaDashboardDaoImpl getAltriaStoreImagesForReview Input: {}", inputObject);

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT " +
                        "imageUUID, projectId, storeId, Project.categoryId as categoryId, Category.name as categoryName " +
                        "FROM ImageStoreNew " +
                            "LEFT JOIN Project " +
                                "on Project.id = ImageStoreNew.projectId " +
                                    "LEFT JOIN Category " +
                                        "ON Project.categoryId = Category.id " +
                            "WHERE storeId = ? " +
                                "and dateId between ? and ? " +
                                "and projectId " +
                                    "in (select id from Project where parentProjectId = ?) "
                              + "and imageStatus = 'done' and imageNotUsable = '0'";
        
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(inputObject.getValue());
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, inputObject.getStoreId());
            ps.setString(2, salesCycle.get(0));
            ps.setString(3, salesCycle.get(1));
            ps.setString(4, inputObject.getProjectId()+"");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("imageUUID", rs.getString("imageUUID"));
                data.put("projectId", rs.getString("projectId"));
                data.put("categoryId", rs.getString("categoryId"));
                data.put("categoryName", rs.getString("categoryName"));
                data.put("storeId", rs.getString("storeId"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaStoreImagesForReview----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getAltriaProductUPCs(InputObject inputObject) {
        LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProductUPCs----------------\n");

        String sql = "SELECT " +
                        "DISTINCT pu.upc, st.id as skuTypeId, st.name as skuType, pm.PRODUCT_LONG_NAME as product_name FROM ProjectUpc pu " +
                            "LEFT JOIN SkuType st " +
                                "on st.id = pu.skuTypeId " +
                            "LEFT JOIN ProductMaster pm " +
                                "on pm.UPC = pu.upc " +
                        "where pu.projectId = ? and pu.skuTypeId in (1,2,4,5,7)";

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, inputObject.getProjectId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("upc", rs.getString("upc"));
                data.put("skuTypeId", rs.getString("skuTypeId"));
                data.put("skuType", rs.getString("skuType"));
                data.put("name", rs.getString("product_name"));
                result.add(data);
            }

            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProductUPCs----------------\n");

            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getAltriaImageAnalysisDetails(InputObject inputObject) {
        LOGGER.info("-----------------------AltriaDashboardDaoImpl getProductDetectionsForReview Input: {}", inputObject);

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT " + 
        		"    ia.upc,ia.imageUUID,ia.dateId,ia.taskId,ia.storeId,ia.projectId,isn.imageReviewRecommendations" + 
        		" FROM ImageAnalysisNew ia, ImageStoreNew isn" + 
        		" WHERE ia.imageUUID = ? AND ia.imageUUID = isn.imageUUID";
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, inputObject.getImageUUID());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("upc", rs.getString("upc"));
                data.put("imageUUID", rs.getString("imageUUID"));
                data.put("dateId", rs.getString("dateId"));
                data.put("taskId", rs.getString("taskId"));
                data.put("storeId", rs.getString("storeId"));
                data.put("projectId", rs.getString("projectId"));
                data.put("imageReviewRecommendations", ConverterUtil.ifNullToEmpty(rs.getString("imageReviewRecommendations")));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getProductDetectionsForReview----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getAltriaImageAnalysisNewByImageUUIDProjectIDAndUPCAndStore(InputObject inputObject) {
        LOGGER.info("-----------------------AltriaDashboardDaoImpl getAltriaImageAnalysisNewByImageUUIDProjectIDAndUPCAndStore Input: {}", inputObject);

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT " +
                "upc,imageUUID,dateId,taskId,storeId,projectId " +
                "FROM ImageAnalysisNew " +
                "where storeId = ? AND projectId = ? AND imageUUID = ? AND upc = ? AND dateId = ?";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, inputObject.getStoreId());
            ps.setString(2, inputObject.getProjectId()+"");
            ps.setString(3, inputObject.getImageUUID());
            ps.setString(4, inputObject.getUpc());
            ps.setString(5, inputObject.getValue());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("upc", rs.getString("upc"));
                data.put("imageUUID", rs.getString("imageUUID"));
                data.put("storeId", rs.getString("storeId"));
                data.put("projectId", rs.getString("projectId"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaImageAnalysisNewByImageUUIDProjectIDAndUPCAndStore----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void createImageAnalysisNewForAltria(LinkedHashMap<String, String> inputMap, List<String> upcsToAdd) {
        LOGGER.info("---------------AltriaDashboardDaoImpl Starts createImageAnalysisNewForAltria: UPCs To Add : {}----------------\n", upcsToAdd);
        String sql = "INSERT IGNORE INTO ImageAnalysisNew (upc, imageUUID, dateId, taskId, storeId, projectId, leftTopY, leftTopX) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for (String upc : upcsToAdd) {
                ps.setString(1, upc);
                ps.setString(2, inputMap.get("imageUUID"));
                ps.setString(3, inputMap.get("dateId"));
                ps.setString(4, inputMap.get("taskId"));
                ps.setString(5, inputMap.get("storeId"));
                ps.setString(6, inputMap.get("projectId"));
                ps.setString(7, "0");
                ps.setString(8, "0");
                ps.addBatch();
                if(++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();

            conn.commit();

            ps.close();
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends createImageAnalysisNewForAltria----------------\n");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void deleteImageAnalysisNewForAltria(LinkedHashMap<String, String> inputMap, List<String> upcsToDelete) {
        LOGGER.info("---------------AltriaDashboardDaoImpl Starts deleteImageAnalysisNewForAltria::UPCs To Delete: {}----------------\n", upcsToDelete);
        String sql = "DELETE FROM ImageAnalysisNew WHERE upc = ? AND imageUUID = ? AND dateId = ? AND taskId = ? AND storeId = ? AND projectId = ?";
        Connection conn = null;
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for (String upc : upcsToDelete) {
                ps.setString(1, upc);
                ps.setString(2, inputMap.get("imageUUID"));
                ps.setString(3, inputMap.get("dateId"));
                ps.setString(4, inputMap.get("taskId"));
                ps.setString(5, inputMap.get("storeId"));
                ps.setString(6, inputMap.get("projectId"));
                ps.addBatch();
                if(++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();

            conn.commit();

            ps.close();
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends deleteImageAnalysisNewForAltria----------------\n");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreVisitsToAggregateByProjectIdStoreId(InputObject inputObject) {

	    LOGGER.info("---------------AltriaDashboardDaoImpl Starts getStoreVisitsToAggregateByProjectIdStoreId ----------------\n");

	    List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT " +
                    "ps.storeId, ps.taskId, ps.projectId " +
                    "from ProjectStoreResult ps " +
                    "where ps.visitDateId between ? and ? " +
                    "and ps.storeId = ? " +
                    "and ps.projectId " +
                    " in (SELECT project.id FROM Project project " +
                    " WHERE project.parentProjectId = ?)";
        
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(inputObject.getValue());
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, salesCycle.get(0));
            ps.setString(2, salesCycle.get(1));
            ps.setString(3, inputObject.getStoreId());
            ps.setString(4, inputObject.getProjectId()+"");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("projectId", rs.getString("projectId"));
                data.put("storeId", rs.getString("storeId"));
                data.put("taskId", rs.getString("taskId"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getStoreVisitsToAggregateByProjectIdStoreId ----------------\n");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void updateProjectStoreResultsResultCodeAndStatusForAltria(List<Map<String, String>> projects){
        LOGGER.info("---------------AltriaDashboardDaoImpl Starts updateProjectStoreResultsResultCodeAndStatusForAltria::No of records: {}", projects.size());
        String sql = "UPDATE ProjectStoreResult SET status = ?, resultCode = ? WHERE taskId = ? AND storeId = ? AND projectId = ?";
        Connection conn = null;
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for (Map<String, String> project : projects) {
                ps.setString(1, "1");
                ps.setString(2, "1");
                ps.setString(3, project.get("taskId"));
                ps.setString(4, project.get("storeId"));
                ps.setString(5, project.get("projectId"));
                ps.addBatch();
                if(++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();

            conn.commit();

            ps.close();
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends updateProjectStoreResultsResultCodeAndStatusForAltria----------------\n");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

	@Override
	public List<LinkedHashMap<String, String>> getLinearFootageByStore(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getLinearFootageByStore ::input={}----------------\n", queryArgs);
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();

		String visitMonth = (String) queryArgs.get("month");
		String visitYear = (String) queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);

		String sql = "SELECT linearFootage FROM ProjectStoreResult WHERE projectId=? AND storeId=? AND visitDateId BETWEEN ? AND ?";

		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, Integer.parseInt((String) queryArgs.get("projectId")));
			ps.setString(2, (String) queryArgs.get("store"));
			ps.setString(3, salesCycle.get(0));
			ps.setString(4, salesCycle.get(1));

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				LinkedHashMap<String, String> footage = new LinkedHashMap<String, String>();
				footage.put("linearFootage", rs.getString("linearFootage"));
				result.add(footage);
			}
			rs.close();
			ps.close();

			LOGGER.info("---------------AltriaDashboardDaoImpl Ends getLinearFootageByStore----------------\n");
			return result;
		} catch (SQLException e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
			LOGGER.error("exception", e);
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					LOGGER.error("exception", e);
				}
			}
		}
	}

	@Override
	public void updateLinearFootageByStore(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts updateLinearFootageByStore::input={}----------------\n",queryArgs);

		String visitMonth = (String) queryArgs.get("month");
		String visitYear = (String) queryArgs.get("year");
        List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);

		String sql = "UPDATE ProjectStoreResult SET linearFootage=? WHERE projectId=? AND storeId=? AND visitDateId BETWEEN ? AND ?";

		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, (String) queryArgs.get("linearFootage"));
			ps.setInt(2, Integer.parseInt((String) queryArgs.get("projectId")));
			ps.setString(3, (String) queryArgs.get("store"));
			ps.setString(4, salesCycle.get(0));
			ps.setString(5, salesCycle.get(1));

			ps.executeUpdate();

			ps.close();

			LOGGER.info("---------------AltriaDashboardDaoImpl Ends updateLinearFootageByStore----------------\n");
		} catch (SQLException e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
			LOGGER.error("exception", e);
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					LOGGER.error("exception", e);
				}
			}
		}
	}
	
	@Override
	public List<Map<String, String>> getAltriaProjectProductAvailabilityForReport(
			Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAltriaProjectProductAvailabilityForReport::"
				+ "input={}----------------\n", queryArgs);
		
		String aggLevel = (String) queryArgs.get("aggregationLevel");
		boolean territoryAgg = aggLevel.equals("territory");
		
	    String sql = "SELECT" + 
	    		"    psd.storeId," + 
	    		"    psd.visitDay," + 
	    		"    psd.visitMonth," + 
	    		"    psd.visitYear," + 
	    		"    psr.agentId," + 
	    		"    psd.upc," + 
	    		"    psd.productName," + 
	    		"    psd.brandName," + 
	    		"    psd.projectId," + 
	    		"    psd.skuTypeId," + 
	    		"    sm.name as retailName," + 
	    		"    sm.street," + 
	    		"    sm.city," + 
	    		"    sm.stateCode," + 
	    		"    sglm.geoLevel1Id," + 
	    		"    sglm.geoLevel2Id," + 
	    		"    sglm.geoLevel3Id," + 
	    		"    sglm.geoLevel4Id," + 
	    		"    sglm.geoLevel5Id," + 
	    		"    sglm.customerStoreNumber" + 
	    		" FROM " + 
	    		"    ProjectStoreData psd, StoreMaster sm, StoreGeoLevelMap sglm, ProjectStoreResult psr" + 
	    		" WHERE" + 
	    		"    psd.parentProjectId= ?" + 
	    		"    AND psd.skuTypeId in ('1','2','4','5','7')" + 
	    		"    AND psd.visitDate BETWEEN ? AND ?" + 
	    		"    AND psd.storeId IN (::storeIds)" + 
	    		"    AND psd.storeId = sm.storeId" + 
	    		"    AND psd.storeId = sglm.storeId" + 
	    		"    AND sglm.customerCode = ?" + 
	    		"    AND psr.status = '1'" + 
	    		"    AND psr.projectId = psd.projectId" + 
	    		"    AND psr.storeId = psd.storeId" + 
	    		"    AND psr.taskId = psd.taskId" + 
	    		" ORDER BY" + 
	    		"    psd.storeId," + 
	    		"    psd.projectId," + 
	    		"    psd.skuTypeId," + 
	    		"    psd.upc";
	    
	    String projectUpcSql = "SELECT" + 
	    		"    ProjectUpc.upc," + 
	    		"    ProjectUpc.projectId," + 
	    		"    ProjectUpc.skuTypeId," + 
	    		"    SkuType.name as skuTypeName," + 
	    		"    ProductMaster.product_short_name as productName," + 
	    		"    ProductMaster.brand_name as brandName" + 
	    		" FROM" + 
	    		"    ProjectUpc, ProductMaster, SkuType" + 
	    		" WHERE" + 
	    		"    ProjectUpc.projectId IN ( SELECT id FROM Project WHERE parentProjectId = ? AND status = '1')" + 
	    		"    AND ProjectUpc.skuTypeId IN ('1','2','4','5','7')" +
	    		"    AND ProjectUpc.upc = ProductMaster.upc" +
	    		"    AND ProjectUpc.skuTypeId = SkuType.id" + 
	    		" ORDER BY" + 
	    		"    ProjectUpc.projectId," + 
	    		"    ProjectUpc.skuTypeId," + 
	    		"    ProjectUpc.upc" ;
	    
	    List<String> storesForTerritory = new ArrayList<String>();
	    String listOfStoresToFilter = "";
        if ( territoryAgg ) {
        	storesForTerritory = (List<String>) queryArgs.get("stores");
        	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
        	listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
        	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
        }
	    
        Map<String,String> projectToCategoryMap = (Map<String,String>) queryArgs.get("projectToCategoryMap");
        
        Map<String,Map<String,String>> projectUpcMap = new HashMap<String,Map<String,String>>();
        Map<String,List<Map<String,String>>> storeUpcMap = new HashMap<String,List<Map<String,String>>>();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            
            PreparedStatement psProjectUpc = conn.prepareStatement(projectUpcSql);
            psProjectUpc.setInt(1, Integer.parseInt((String)queryArgs.get("projectId")));
            ResultSet rsProjectUpc = psProjectUpc.executeQuery();
            while (rsProjectUpc.next()) {
            	Map<String,String> upcMap = new HashMap<String,String>();
            	String upc =  rsProjectUpc.getString("upc");
            	upcMap.put("upc", upc);
            	upcMap.put("productName", rsProjectUpc.getString("productName"));
            	upcMap.put("brandName", rsProjectUpc.getString("brandName"));
            	upcMap.put("projectId", rsProjectUpc.getString("projectId"));
            	upcMap.put("categoryName", projectToCategoryMap.get(rsProjectUpc.getString("projectId")));
            	upcMap.put("skuTypeName", rsProjectUpc.getString("skuTypeName"));
            	projectUpcMap.put(upc, upcMap);
            }
            rsProjectUpc.close();
            psProjectUpc.close();
            
            PreparedStatement ps = conn.prepareStatement(sql);

            String month = (String)queryArgs.get("month");
            String year = (String)queryArgs.get("year");
            List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(year+month);
            
            ps.setInt(1, Integer.parseInt((String)queryArgs.get("projectId")));
            ps.setString(2, salesCycle.get(0));
            ps.setString(3, salesCycle.get(1));
            ps.setString(4, (String)queryArgs.get("customerCode"));

            ResultSet rs = ps.executeQuery();
            String prevStoreId=null;
            while (rs.next()) {
            	Map<String,String> oneProductInStore = new HashMap<String,String>();
            	String storeId = rs.getString("storeId");
            	String upc = rs.getString("upc");
            	oneProductInStore.put("storeId", storeId);
            	oneProductInStore.put("projectId", rs.getString("projectId"));
            	oneProductInStore.put("visitDate", rs.getString("visitMonth")+"/"+rs.getString("visitDay")+"/"+rs.getString("visitYear"));
            	oneProductInStore.put("agentId",rs.getString("agentId"));
            	oneProductInStore.put("upc", upc);
            	oneProductInStore.put("productName", rs.getString("productName"));
            	oneProductInStore.put("brandName", rs.getString("brandName"));
            	oneProductInStore.put("retailName", rs.getString("retailName"));
            	oneProductInStore.put("street", rs.getString("street"));
            	oneProductInStore.put("city", rs.getString("city"));
            	oneProductInStore.put("stateCode", ConverterUtil.ifNullToEmpty(rs.getString("stateCode")));
            	oneProductInStore.put("geoLevel1Id", rs.getString("geoLevel1Id"));
            	oneProductInStore.put("geoLevel2Id", rs.getString("geoLevel2Id"));
            	oneProductInStore.put("geoLevel3Id", rs.getString("geoLevel3Id"));
            	oneProductInStore.put("geoLevel4Id", rs.getString("geoLevel4Id"));
            	oneProductInStore.put("geoLevel5Id", rs.getString("geoLevel5Id"));
            	oneProductInStore.put("customerStoreNumber", rs.getString("customerStoreNumber"));
            	oneProductInStore.put("available", "Yes");

            	if ( storeUpcMap.get(storeId) == null ) {
            		List<Map<String,String>> oneStore = new ArrayList<Map<String,String>>();
            		storeUpcMap.put(storeId, oneStore);
            	}
            	storeUpcMap.get(storeId).add(oneProductInStore);
            	
            	if ( prevStoreId != null && !prevStoreId.equals(storeId) ) {
            		List<Map<String,String>> oneStoreProducts = storeUpcMap.get(prevStoreId);
            		addCategoryNamesAndMissingUPCs(projectToCategoryMap, projectUpcMap, oneStoreProducts);
            		
            	}
            	prevStoreId = storeId;
            }
            
            if ( prevStoreId != null ) {
            	List<Map<String,String>> oneStoreProducts = storeUpcMap.get(prevStoreId);
            	addCategoryNamesAndMissingUPCs(projectToCategoryMap, projectUpcMap, oneStoreProducts);
            }
            
            rs.close();
            ps.close();
            
            List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
            for(Map.Entry<String, List<Map<String,String>>> oneStore : storeUpcMap.entrySet()) {
            	resultList.addAll(oneStore.getValue());
            }
            
            resultList.sort(Comparator.comparing((Map<String, String> m) -> (String) m.get("customerStoreNumber"))
    				.thenComparing(m -> (String) m.get("categoryName"))
    				.thenComparing(m -> (String) m.get("skuTypeName"))
    				.thenComparing(m -> (String) m.get("brandName"))
    				.thenComparing(m -> (String) m.get("productName")));
            
            LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAltriaProjectProductAvailabilityForReport----------------\n");
            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            LOGGER.error("exception {}", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    LOGGER.error("exception {}", e);
                }
            }
        }
	}

	private void addCategoryNamesAndMissingUPCs(Map<String, String> projectToCategoryMap,
			Map<String, Map<String, String>> projectUpcMap, List<Map<String, String>> oneStoreProducts) {
		List<String> storeUPCs = new ArrayList<String>();
		for ( Map<String,String> oneProduct : oneStoreProducts) {
			storeUPCs.add(oneProduct.get("upc"));
			oneProduct.put("categoryName", projectToCategoryMap.get(oneProduct.get("projectId")));
			oneProduct.put("skuTypeName", projectUpcMap.get(oneProduct.get("upc")).get("skuTypeName"));
		}
		for(String projectUpc : projectUpcMap.keySet()) {
			if(!storeUPCs.contains(projectUpc)) {
				Map<String,String> missingProduct = new HashMap<String,String>();
				missingProduct.putAll(oneStoreProducts.get(0));
				missingProduct.put("upc", projectUpc);
				missingProduct.put("productName", projectUpcMap.get(projectUpc).get("productName"));
				missingProduct.put("brandName", projectUpcMap.get(projectUpc).get("brandName"));
				missingProduct.put("available", "No");
				missingProduct.put("categoryName", projectToCategoryMap.get(projectUpcMap.get(projectUpc).get("projectId")));
				missingProduct.put("skuTypeName", projectUpcMap.get(projectUpc).get("skuTypeName"));
				oneStoreProducts.add(missingProduct);
			}
		}
	}
	
	@Override
	public Map<String,Map<String,String>> getAgentsByGeoLevelAndId(String customerCode, String userGeoLevel, String userGeoLevelId) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getAgentsByGeoLevelAndId ::"
				+ "customerCode={}, userGeoLevel={}, userGeoLevelId={}----------------\n",customerCode,userGeoLevel,userGeoLevelId );
		Map<String,Map<String,String>> result = new LinkedHashMap<String,Map<String,String>>();

		String sql = "SELECT userId, geoLevelId FROM UserGeoMap WHERE customerCode=? AND geoLevel='geoLevel1'" + 
				" AND geoLevelId IN ( SELECT DISTINCT(geoLevel1Id) FROM StoreGeoLevelMap WHERE customerCode=? AND ";
		String geoLevelPart = userGeoLevel+"Id = '" + userGeoLevelId + "') ORDER BY geoLevelId";
		sql = sql.concat(geoLevelPart);

		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, customerCode);
			ps.setString(2, customerCode);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String userId = rs.getString("userId");
				String geoLevelId = rs.getString("geoLevelId");
				Map<String,String> oneUser = new HashMap<String,String>();
				oneUser.put("userId", userId);
				oneUser.put("geoLevelId", geoLevelId);
				result.put(userId, oneUser);
			}
			rs.close();
			ps.close();

			LOGGER.info("---------------AltriaDashboardDaoImpl Ends getAgentsByGeoLevelAndId----------------\n");
			return result;
		} catch (SQLException e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
			LOGGER.error("exception", e);
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					LOGGER.error("exception", e);
				}
			}
		}
	}
	
	@Override
	public Map<String,Object> getJobStatsByAgent(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getJobStatsByAgent ::"
				+ "input={}----------------\n",queryArgs );
		Map<String,Object> result = new LinkedHashMap<String,Object>();

		String sql = "SELECT" + 
				"    a.agentId," + 
				"    c.geoLevelId," + 
				"    a.visitDateId," + 
				"    count(distinct(a.storeId)) storeCount," + 
				"    b.maxVisitDateId " + 
				"FROM" + 
				"    ProjectStoreResult a," + 
				"    (SELECT" + 
				"        MAX(visitDateId) AS maxVisitDateId" + 
				"     FROM" + 
				"        ProjectStoreResult" + 
				"     WHERE " + 
				"        projectId IN (SELECT id FROM Project WHERE parentProjectId = ? AND status = '1')" + 
				"        AND storeId in (::storeIds)" + 
				"        AND status='1' AND visitDateId BETWEEN ? AND ?" + 
				"    )b," + 
				"    UserGeoMap c " + 
				"WHERE" + 
				"    a.projectId IN (SELECT id FROM Project WHERE parentProjectId = ? AND status = '1')" + 
				"    AND storeId in (::storeIds)" + 
				"    AND a.status='1' AND a.visitDateId BETWEEN ? AND ?" + 
				"    AND a.agentId = c.userId " + 
				"GROUP BY" + 
				"    a.agentId, a.visitDateId " + 
				"ORDER BY " + 
				"    c.geoLevelId";
		
		List<String> storesForTerritory = (List<String>) queryArgs.get("stores");
    	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
    	String listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
    	
    	String visitMonth = (String)queryArgs.get("month");
		String visitYear = (String)queryArgs.get("year");
		List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, Integer.parseInt((String) queryArgs.get("projectId")));
			ps.setString(2, salesCycle.get(0));
			ps.setString(3, salesCycle.get(1));
			ps.setInt(4, Integer.parseInt((String) queryArgs.get("projectId")));
			ps.setString(5, salesCycle.get(0));
			ps.setString(6, salesCycle.get(1));

			ResultSet rs = ps.executeQuery();
			String maxVisitDateId = null;
			while (rs.next()) {
				String agentId = rs.getString("agentId");
				String geoLevelId = rs.getString("geoLevelId");
				String visitDateId = rs.getString("visitDateId");
				String storeCount = rs.getString("storeCount");
				maxVisitDateId = rs.getString("maxVisitDateId");
				
				Map<String,String> oneAgentDay = new HashMap<String,String>();
				oneAgentDay.put("agentId", agentId);
				oneAgentDay.put("geoLevelId", geoLevelId);
				oneAgentDay.put("visitDateId", visitDateId);
				oneAgentDay.put("storeCount", storeCount);
				oneAgentDay.put("maxVisitDateId", maxVisitDateId);
				
				if (result.get(agentId) == null ) {
					result.put(agentId, new HashMap<String,Map<String,String>>());
				}
				((Map<String,Map<String,String>>)result.get(agentId)).put(visitDateId, oneAgentDay);
			}
			result.put("maxVisitDateId", maxVisitDateId);
			
			rs.close();
			ps.close();

			LOGGER.info("---------------AltriaDashboardDaoImpl Ends getJobStatsByAgent----------------\n");
			return result;
		} catch (SQLException e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
			LOGGER.error("exception", e);
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					LOGGER.error("exception", e);
				}
			}
		}
	}
	
	@Override
	public List<Map<String,String>> getStorewisePhotoCount(Map<String, Object> queryArgs) {
		LOGGER.info("---------------AltriaDashboardDaoImpl Starts getStorewisePhotoCount ::"
				+ "input={}----------------\n",queryArgs );
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
		outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
		inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		String sql = "SELECT" + 
				"    img.storeId," + 
				"    img.agentId," + 
				"    img.dateId," + 
				"    count(*) as photoCount," +
				"    count(if(img.imageNotUsable='1',1,NULL)) as notUsablePhotoCount," +
				"    sm.name as retailName," + 
				"	sm.street," + 
				"	sm.city," + 
				"	sm.stateCode," + 
				"	sglm.geoLevel1Id," + 
				"	sglm.geoLevel2Id," + 
				"	sglm.geoLevel3Id," + 
				"	sglm.geoLevel4Id," + 
				"	sglm.geoLevel5Id," + 
				"	sglm.customerStoreNumber " + 
				"FROM" + 
				"    ImageStoreNew img, StoreMaster sm, StoreGeoLevelMap sglm, ProjectStoreResult psr " + 
				"WHERE" + 
				"    img.projectId IN (SELECT id FROM Project WHERE parentProjectId=? AND status='1')" + 
				"    AND img.storeId in (::storeIds)" +
				"    AND img.dateId BETWEEN ? AND ?" +
				"    AND img.storeId = sm.storeId" + 
				"    AND sglm.customerCode = ?" + 
				"    AND img.storeId = sglm.storeId" + 
				"    AND img.projectId = psr.projectId" + 
				"    AND img.storeId = psr.storeId" + 
				"    AND img.taskId = psr.taskId" + 
				"    AND psr.status = '1' " + 
				"GROUP BY" + 
				"    img.storeId " +
				"ORDER BY " +
				"     sglm.customerStoreNumber";
		
		List<String> storesForTerritory = (List<String>) queryArgs.get("stores");
    	String step1 = StringUtils.join(storesForTerritory, "\", \"");// Join with ", "
    	String listOfStoresToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::storeIds", listOfStoresToFilter);
    	
    	String visitMonth = (String)queryArgs.get("month");
		String visitYear = (String)queryArgs.get("year");
		List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(visitYear+visitMonth);
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setInt(1, Integer.parseInt((String) queryArgs.get("projectId")));
			ps.setString(2, salesCycle.get(0));
			ps.setString(3, salesCycle.get(1));
			ps.setString(4, (String)queryArgs.get("customerCode"));

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String,String> oneStore = new HashMap<String,String>();
            	oneStore.put("storeId", rs.getString("storeId"));
            	String dateId = rs.getString("dateId");
            	try {
            		dateId = outSdf.format(inSdf.parse(dateId));
            	} catch (ParseException e) {
            		LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
            	}
            	oneStore.put("visitDate", dateId);
            	oneStore.put("photoCount", rs.getString("photoCount"));
            	oneStore.put("notUsablePhotoCount", rs.getString("notUsablePhotoCount"));
            	oneStore.put("agentId",rs.getString("agentId"));
            	oneStore.put("retailName", rs.getString("retailName"));
            	oneStore.put("street", rs.getString("street"));
            	oneStore.put("city", rs.getString("city"));
            	oneStore.put("stateCode", ConverterUtil.ifNullToEmpty(rs.getString("stateCode")));
            	oneStore.put("geoLevel1Id", rs.getString("geoLevel1Id"));
            	oneStore.put("geoLevel2Id", rs.getString("geoLevel2Id"));
            	oneStore.put("geoLevel3Id", rs.getString("geoLevel3Id"));
            	oneStore.put("geoLevel4Id", rs.getString("geoLevel4Id"));
            	oneStore.put("geoLevel5Id", rs.getString("geoLevel5Id"));
            	oneStore.put("customerStoreNumber", rs.getString("customerStoreNumber"));
            	result.add(oneStore);
			}
			
			rs.close();
			ps.close();

			LOGGER.info("---------------AltriaDashboardDaoImpl Ends getStorewisePhotoCount----------------\n");
			return result;
		} catch (SQLException e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
			LOGGER.error("exception", e);
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					LOGGER.error("exception", e);
				}
			}
		}
	}
	
	@Override
	public TreeMap<String,String> getVisitDaysInASalesMonth(String calendarMonth) {
		TreeMap<String,String> visitDaysMap = new TreeMap<String,String>();
		List<String> salesCycle = MONTH_TO_SALES_CYCLE_MAP.get(calendarMonth);
		String startDate = salesCycle.get(0);
		String formattedStartDate = startDate.substring(0,4)+"-"+startDate.substring(4,6)+"-"+startDate.substring(6,8);
		String endDate = salesCycle.get(1);
		String formattedEndDate = endDate.substring(0,4)+"-"+endDate.substring(4,6)+"-"+endDate.substring(6,8);
		LocalDate start = LocalDate.parse(formattedStartDate);
		LocalDate end = LocalDate.parse(formattedEndDate);
		List<LocalDate> totalDates = new ArrayList<>();
		while (!start.isAfter(end)) {
			totalDates.add(start);
			String displayDateId = 
    	    		(start.getMonthValue() < 10 ? "0"+start.getMonthValue() : start.getMonthValue()) +"/"+
    	    		(start.getDayOfMonth() < 10 ? "0"+start.getDayOfMonth() : start.getDayOfMonth()) +"/"+
    	    		start.getYear();
			String visitDateId = start.getYear()+""+
    	    		(start.getMonthValue() < 10 ? "0"+start.getMonthValue() : start.getMonthValue())+
    	    		(start.getDayOfMonth() < 10 ? "0"+start.getDayOfMonth() : start.getDayOfMonth());
			visitDaysMap.put(visitDateId, displayDateId);
			start = start.plusDays(1);
		}
		return visitDaysMap;
	}
	
}

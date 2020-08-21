package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ITGDashboardDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.StoreGeoLevel;
import com.snap2buy.themobilebackend.model.itg.*;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Anoop
 */
@Component(value = BeanMapper.BEAN_ITG_DASHBOARD_DAO)
@Scope("prototype")
public class ITGDashboardDaoImpl implements ITGDashboardDao {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
	@Autowired
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
	@Override
	public Map<String, String> getITGStoreDetails(int projectId, String storeId, String timePeriodType, String timePeriod) {
		LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGStoreDetails::projectId={}::storeId={}::timePeriodType={}::timePeriod={}",
				projectId,storeId,timePeriodType,timePeriod);
        String sql = "SELECT * FROM ITG_AGG_STORE_MONTH WHERE PROJECT_ID=? AND STORE_ID=? AND TIMEPERIOD LIKE ? ORDER BY TIMEPERIOD ASC";
        
        if(timePeriodType.equals("Q")) {
        	timePeriod = "2020";
        }
        Map<String, String> dataMap = new HashMap<String,String>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, timePeriod+"%");

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
            	for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String key = meta.getColumnName(i);
                    String value = rs.getString(key);
                    dataMap.put(key, value);
                }
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGStoreDetails----------------");

            return dataMap;
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
	public List<Map<String, Object>> getITGStoreVisitImages(int projectId, String storeId, String timePeriodType, String timePeriod) {
		LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGStoreVisitImages::projectId={}::storeId={}::timePeriodType={}::timePeriod={}", 
				projectId,storeId,timePeriodType,timePeriod);
        String sql = "SELECT dateId,GROUP_CONCAT(imageUUID) AS images FROM ImageStoreNew"
        		+ " WHERE projectId = ? AND storeId = ? AND (storeId,taskId) IN ("
        		+ "    SELECT storeId,taskId from ProjectStoreResult WHERE projectId = ? AND storeId = ? AND status = '1' " //AND SUBSTR(visitDateId,1,6) = ?  "
        		+ ") GROUP BY dateId ORDER BY dateId desc";
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setInt(3, projectId);
            ps.setString(4, storeId);
            //ps.setString(5, timePeriod); TODO - Q/Y search

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, Object> oneVisit = new HashMap<String,Object>();
            	
            	try {
            		oneVisit.put("visitDate", outSdf.format(inSdf.parse(rs.getString("dateId"))));
            	} catch (ParseException e) {
            		LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
            	}
            	
            	String images = rs.getString("images");
            	if ( StringUtils.isBlank(images) ) {
            		oneVisit.put("photos", new ArrayList<String>()); 
            	} else {
            		oneVisit.put("photos",Arrays.asList(images.split(",")));
            	}
            	data.add(oneVisit);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGStoreVisitImages----------------");

            return data;
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
	public List<LinkedHashMap<String, Object>> getITGStoresWithFilters(int projectId, String geoLevelId,
			String timePeriodType, String timePeriod, String limit, SearchFilter filter) {
		LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGStoresWithFilters::projectId={}::geoLevelId={}::"
				+ "timePeriodType={}::timePeriod={}::limit={}::filter={}", projectId, geoLevelId, timePeriodType, timePeriod, limit,filter);
        
		String filterClause = prepareFilterClause(filter);
		
		String orderByClause = prepareOrderByClause(filter);
		
		String sql = "SELECT  \n" + 
				"	STORE_ID, \n" + 
				"	STORE_CRS, \n" + 
				"	STORE_PLAN, \n" + 
				"	PLAN_FACING_COUNT, \n" + 
				"	VOLUME_SHARE, \n" + 
				"	ROUND(ITGB_SHARE_OF_SHELF,1) AS ITGB_SHARE_OF_SHELF, \n" + 
				"	ITGB_FACING_COUNT, \n" + 
				"	COALESCE(HAS_ITGB_PRICE_FACINGS,0) AS HAS_ITGB_PRICE, \n" +
				"	EDLP_STATUS, \n" + 
				"	PLAN_FACING_COMPLIANCE, \n" + 
				"	ABOVE_VOLUME_SHARE, \n" + 
				"	ABOVE_MARKET_SHARE, \n" + 
				"	SCORE, \n" + 
				"   PREVIEW_IMAGEUUID,\n" + 
				"	STREET, \n" + 
				"	CITY, \n" + 
				"	STATECODE\n" + 
				" FROM ITG_AGG_STORE_MONTH\n" + 
				" WHERE  \n" + 
				"	ITG_AGG_STORE_MONTH.PROJECT_ID = ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.TIMEPERIOD LIKE ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.GEO_MAPPING_ID like ?\n " + 
				filterClause +
				" GROUP BY STORE_ID  \n" + 
				orderByClause;
		
		if ( StringUtils.isNotBlank(limit) && !limit.equals("-9") ) {
			sql = sql + " LIMIT " + limit ;
		}
		
		String countQuerySql = "SELECT  COUNT(*) AS totalStoreCount \n" + 
				" FROM ITG_AGG_STORE_MONTH\n" + 
				" WHERE  \n" + 
				"	ITG_AGG_STORE_MONTH.PROJECT_ID = ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.TIMEPERIOD LIKE ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.GEO_MAPPING_ID like ?\n " + 
				filterClause ;
		
		int totalStoreCount = 0;
		
		if ( timePeriodType.equals("Q") ) {
			timePeriod = "2020";
		}
		
		//CustomerCode - Retailer Chain Code Mapping - Special Handling - for query performance to force index on storemaster
		//String storeRetailerChainCode = projectId == 1643 ? "ITGBND" : "ITGIND" ;
		
        List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
        List<LinkedHashMap<String, String>> storesList = new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String,Object> result = new LinkedHashMap<String,Object>();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            
            PreparedStatement countPs = conn.prepareStatement(countQuerySql);
            countPs.setInt(1, projectId);
            countPs.setString(2, timePeriod+"%");
            countPs.setString(3, geoLevelId+"%");
            ResultSet countRs = countPs.executeQuery();
            if (countRs.next() ) {
            	totalStoreCount = countRs.getInt("totalStoreCount");
            }
            countRs.close();
            countPs.close();
            
            result.put("totalCount", totalStoreCount);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            
            //ps.setString(1, storeRetailerChainCode);
            ps.setInt(1, projectId);
            ps.setString(2, timePeriod+"%");
            ps.setString(3, geoLevelId+"%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	LinkedHashMap<String, String> oneStore = new LinkedHashMap<String,String>();
            	oneStore.put("storeId", rs.getString("STORE_ID"));
            	oneStore.put("storeCRS", rs.getString("STORE_CRS"));
            	oneStore.put("score", ConverterUtil.ifNullToEmpty(rs.getString("SCORE")));
            	oneStore.put("itgbShareOfShelf", rs.getString("ITGB_SHARE_OF_SHELF"));
            	oneStore.put("storePlanType", ConverterUtil.ifNullToEmpty(rs.getString("STORE_PLAN")));
            	oneStore.put("planFacingCount", ConverterUtil.ifNullToEmpty(rs.getString("PLAN_FACING_COUNT")));
            	oneStore.put("volumeShare", ConverterUtil.ifNullToEmpty(rs.getString("VOLUME_SHARE")));
            	oneStore.put("itgbFacingCount", rs.getString("ITGB_FACING_COUNT"));
            	oneStore.put("hasITGBPrice", rs.getString("HAS_ITGB_PRICE"));
            	oneStore.put("isEDLPStore", ConverterUtil.ifNullToEmpty(rs.getString("EDLP_STATUS")));
            	oneStore.put("planFacingCompliance", ConverterUtil.ifNullToEmpty(rs.getString("PLAN_FACING_COMPLIANCE")));
            	oneStore.put("isAboveVolumeShare", ConverterUtil.ifNullToEmpty(rs.getString("ABOVE_VOLUME_SHARE")));
            	oneStore.put("isAboveMarketShare", ConverterUtil.ifNullToEmpty(rs.getString("ABOVE_MARKET_SHARE")));
            	oneStore.put("street", ConverterUtil.ifNullToEmpty(rs.getString("STREET")));
            	oneStore.put("city", ConverterUtil.ifNullToEmpty(rs.getString("CITY")));
            	oneStore.put("stateCode", ConverterUtil.ifNullToEmpty(rs.getString("STATECODE")));
            	oneStore.put("storeImageUUID", rs.getString("PREVIEW_IMAGEUUID"));
            	
            	storesList.add(oneStore);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGStoresWithFilters----------------");
            
            result.put("stores", storesList);
            
            resultList.add(result);

            return resultList;
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

	private String prepareFilterClause(SearchFilter filter) {
		StringBuilder filterBuilder = new StringBuilder("");
		
		if ( filter == null ) {
			return filterBuilder.toString();
		} else if ( StringUtils.isNotBlank(filter.getStoreId())) {
			return " AND STORE_CRS = '" + filter.getStoreId() + "' ";
		}
		
		if ( filter.getStorePlanType() != null ) {
			String storePlanType = filter.getStorePlanType();
			if ( storePlanType.isEmpty() ) {
				filterBuilder = filterBuilder.append(" AND ( STORE_PLAN IS NULL OR STORE_PLAN = '' ) ");
			} else {
				filterBuilder = filterBuilder.append(" AND STORE_PLAN = '"+storePlanType+"' ");
			}
		}
		
		if ( filter.getPlanFacingCompliance() != null ) {
			String planFacingCompliance = filter.getPlanFacingCompliance();
			if ( planFacingCompliance.isEmpty() ) {
				filterBuilder = filterBuilder.append(" AND ( PLAN_FACING_COMPLIANCE IS NULL OR PLAN_FACING_COMPLIANCE = '' ) ");
			} else {
				filterBuilder = filterBuilder.append(" AND PLAN_FACING_COMPLIANCE = '"+planFacingCompliance+"' ");
			}
		}
		
		if ( filter.getIsAboveVolumeShare() != null ) {
			String isAboveVolumeShare = filter.getIsAboveVolumeShare();
			if ( isAboveVolumeShare.isEmpty() ) {
				filterBuilder = filterBuilder.append(" AND ( ABOVE_VOLUME_SHARE IS NULL OR ABOVE_VOLUME_SHARE = '' ) ");
			} else {
				filterBuilder = filterBuilder.append(" AND ABOVE_VOLUME_SHARE = '"+isAboveVolumeShare+"' ");
			}
		}
		
		if ( filter.getItgbFacingCount() != null ) {
			String itgbFacingCount = filter.getItgbFacingCount();
			if ( itgbFacingCount.isEmpty() ) {
				filterBuilder = filterBuilder.append(" AND ( ITGB_FACING_COUNT IS NULL OR ITGB_FACING_COUNT = '' ) ");
			} else if ( itgbFacingCount.equals("1") ){
				filterBuilder = filterBuilder.append(" AND ITGB_FACING_COUNT > 0 ");
			} else {
				filterBuilder = filterBuilder.append(" AND ITGB_FACING_COUNT = 0 ");
			}
		}
		
		if ( filter.getIsEDLPStore() != null ) {
			String isEDLPStore = filter.getIsEDLPStore();
			if ( isEDLPStore.isEmpty() ) {
				filterBuilder = filterBuilder.append(" AND ( EDLP_STATUS IS NULL OR EDLP_STATUS = '' ) ");
			} else {
				filterBuilder = filterBuilder.append(" AND EDLP_STATUS = '"+isEDLPStore+"' ");
			}
		}
		
		if ( filter.getHasITGBPrice() != null && !filter.getHasITGBPrice().isEmpty() ) {
			String hasITGBPrice = filter.getHasITGBPrice();
			filterBuilder = filterBuilder.append(" AND COALESCE(HAS_ITGB_PRICE_FACINGS,0) = " + hasITGBPrice + " ");
		}
		
		return filterBuilder.toString();
	}
	
	private String prepareOrderByClause(SearchFilter filter) {
		
		String orderByClause = " ORDER BY SCORE DESC";
		
		if ( filter != null && StringUtils.isNoneBlank(filter.getSortBy(), filter.getSortOrder())) {
			String orderByField = filter.getSortBy();
			String orderByOrder = filter.getSortOrder();
			
			if ( orderByField.equalsIgnoreCase("shareOfShelf") ) {
				orderByField = "ITGB_SHARE_OF_SHELF";
			}
			
			if ( StringUtils.isNoneBlank(orderByField, orderByOrder) ) {
				orderByClause = " ORDER BY " + orderByField + " " + orderByOrder + " ";
			}
		}
		
		return orderByClause;
	}

	@Override
	public Map<String, String> getITGStats(int projectId, String geoLevelInternalId, String timePeriodType,
			String timePeriod, String storeType) {
		LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGStats::projectId={}::geoLevelInternalId={}::timePeriodType={}::timePeriod={}::storeType={}", 
				projectId,geoLevelInternalId, timePeriodType, timePeriod, storeType );
		
		String sqlFileName = "ITG_GEO_LEVEL_AGG_QUERY_"+storeType+".sql";
		String query = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/ITG/");
		
		if ( timePeriodType.equals("Q") ) {
			timePeriod = "2020";
		}
		
        Map<String, String> dataMap = new HashMap<String,String>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, projectId);
            ps.setString(2, timePeriod+"%");
            ps.setString(3, geoLevelInternalId+"%");

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
            	for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String key = meta.getColumnName(i);
                    String value = rs.getString(key);
                    dataMap.put(key, value);
                }
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGStats----------------");

            return dataMap;
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
	public List<LinkedHashMap<String, String>> getITGStoresForReport(int projectId, String customerCode, String geoLevel, String geoLevelId,
			String timePeriodType, String timePeriod) {
		LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGStoresForReport::projectId={}::customerCode={}::"
				+ "geoLevel={}::geoLevelId={}::timePeriodType={}::timePeriod={}", projectId,customerCode, geoLevel, geoLevelId, timePeriodType, timePeriod);
        
		String sql = "SELECT \n" + 
				"	ITG_AGG_STORE_MONTH.*\n" + 
				"FROM \n" + 
				"	ITG_AGG_STORE_MONTH\n" + 
				"WHERE \n" + 
				"	ITG_AGG_STORE_MONTH.PROJECT_ID = ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.TIMEPERIOD LIKE ?\n" + 
				"	AND ITG_AGG_STORE_MONTH.GEO_MAPPING_ID LIKE ?\n" + 
				"GROUP BY STORE_ID \n" + 
				"ORDER BY STORE_ID;";
		
        List<LinkedHashMap<String, String>> data = new ArrayList<LinkedHashMap<String, String>>();
        
        //CustomerCode - Retailer Chain Code Mapping - Special Handling - for query performance to force index on storemaster
        //String storeRetailerChainCode = projectId == 1643 ? "ITGBND" : "ITGIND" ;
        
        if ( timePeriodType.equals("Q") ) {
			timePeriod = "2020";
		}
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            //ps.setString(1, storeRetailerChainCode);
            ps.setInt(1, projectId);
            ps.setString(2, timePeriod+"%");
            ps.setString(3, geoLevelId+"%");

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
            	LinkedHashMap<String, String> oneStore = new LinkedHashMap<String,String>();
            	for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String key = meta.getColumnName(i);
                    String value = rs.getString(key);
                    oneStore.put(key.toUpperCase(), value);
                }
            	data.add(oneStore);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGStoresForReport----------------");

            return data;
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
    public List<AreaGeo> getITGGeoMappingForUser(String customerCode, String userId, GenericGeo rootGeo) {
        LOGGER.info("---------------ITGDashboardDaoImpl Starts getITGGeoMappingForUser customerCode={},userId={}",customerCode, userId);

        String sql = "SELECT * from UserGeoMap where customercode = ? AND userId = ?";

        Connection conn = null;
        List<AreaGeo> areas = new ArrayList<AreaGeo>();
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, userId);

            ResultSet rs = ps.executeQuery();
            String geoLevel = "";
            String geoLevelId = "";
            if (rs.next()) {
            	geoLevel = rs.getString("geoLevel");
            	geoLevelId = rs.getString("geoLevelId");
            }
            rs.close();
            ps.close();
            
            String geoLevel1Id = "%";
            String geoLevel2Id = "%";
            String geoLevel3Id = "%";
            String geoLevel4Id = "%";
            String geoLevel5Id = "%";
            
            String[] levels = geoLevelId.split("-");
            switch(levels.length) {
            	case 1 : 
            		geoLevel5Id = levels[0];
            		break;
            	case 2 : 
            		geoLevel5Id = levels[0];
            		geoLevel4Id = levels[1];
            		break;
            	case 3 : 
            		geoLevel5Id = levels[0];
            		geoLevel4Id = levels[1];
            		geoLevel3Id = levels[2];
            		break;
            	case 4 : 
            		geoLevel5Id = levels[0];
            		geoLevel4Id = levels[1];
            		geoLevel3Id = levels[2];
            		geoLevel2Id = levels[3];
            		break;
            	case 5 : 
            		geoLevel5Id = levels[0];
            		geoLevel4Id = levels[1];
            		geoLevel3Id = levels[2];
            		geoLevel2Id = levels[3];
            		geoLevel1Id = levels[4];
            		break;
            }
            
            if ( StringUtils.isNotBlank(geoLevel) && StoreGeoLevel.contains(geoLevel) ) {
            	sql = "SELECT  \n" + 
            			"    map.geoLevel1Id,\n" + 
            			"    map.geoLevel2Id,\n" + 
            			"    map.geoLevel3Id,\n" + 
            			"    map.geoLevel4Id,\n" + 
            			"    map.geoLevel5Id\n" + 
            			"FROM  \n" + 
            			"    StoreGeoLevelMap map\n" + 
            			"WHERE  \n" + 
            			"    map.customerCode = ?  \n" + 
            			"    AND map.geoLevel1Id LIKE ? \n" + 
            			"    AND map.geoLevel2Id LIKE ? \n" + 
            			"    AND map.geoLevel3Id LIKE ? \n" + 
            			"    AND map.geoLevel4Id LIKE ? \n" + 
            			"    AND map.geoLevel5Id LIKE ? \n" + 
            			"GROUP BY \n" + 
            			"    map.geoLevel1Id,\n" + 
            			"    map.geoLevel2Id,\n" + 
            			"    map.geoLevel3Id,\n" + 
            			"    map.geoLevel4Id,\n" + 
            			"    map.geoLevel5Id\n" + 
            			"ORDER BY\n" + 
            			"    map.geoLevel1Id,\n" + 
            			"    map.geoLevel2Id,\n" + 
            			"    map.geoLevel3Id,\n" + 
            			"    map.geoLevel4Id,\n" + 
            			"    map.geoLevel5Id";

                ps = conn.prepareStatement(sql);
                ps.setString(1, customerCode);
                ps.setString(2, geoLevel1Id);
                ps.setString(3, geoLevel2Id);
                ps.setString(4, geoLevel3Id);
                ps.setString(5, geoLevel4Id);
                ps.setString(6, geoLevel5Id);

                rs = ps.executeQuery();
                while (rs.next()) {
                	//String mappedGeoLevel5Id = rs.getString("geoLevel5Id");
                	String mappedGeoLevel4Id = rs.getString("geoLevel4Id");
                	String mappedGeoLevel3Id = rs.getString("geoLevel3Id");
                	String mappedGeoLevel2Id = rs.getString("geoLevel2Id");
                	String mappedGeoLevel1Id = rs.getString("geoLevel1Id");
                	
                	AreaGeo newArea = new AreaGeo();
                	newArea.setAreaId(mappedGeoLevel4Id);
                	if (! areas.stream().anyMatch(o -> o.getAreaId().equals(mappedGeoLevel4Id)) ) {
                		areas.add(newArea);
                	}
                	for(AreaGeo area : areas) {
                		if (area.getAreaId().equals(mappedGeoLevel4Id)) {
                			RegionGeo newRegion = new RegionGeo();
                			newRegion.setRegionId(mappedGeoLevel3Id);
                			if ( ! area.getRegions().stream().anyMatch(o -> o.getRegionId().equals(mappedGeoLevel3Id)) ) {
                				area.getRegions().add(newRegion);
                			}
                			for(RegionGeo region : area.getRegions()) {
                				if ( region.getRegionId().equals(mappedGeoLevel3Id)) {
                					DivisionGeo newDivision = new DivisionGeo();
                					newDivision.setDivisionId(mappedGeoLevel2Id);
                					if ( ! region.getDivisions().stream().anyMatch(o -> o.getDivisionId().equals(mappedGeoLevel2Id)) ) {
                						region.getDivisions().add(newDivision);
                					}
                					for( DivisionGeo division : region.getDivisions() ) {
                						if ( division.getDivisionId().equals(mappedGeoLevel2Id)) {
                							TerritoryGeo newTerritory = new TerritoryGeo();
                    						newTerritory.setTerritoryId(mappedGeoLevel1Id);
                    						if ( !division.getTerritories().stream().anyMatch(o -> o.getTerritoryId().equals(mappedGeoLevel1Id)) ) {
                    							division.getTerritories().add(newTerritory);
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
                }
                
                rs.close();
                ps.close();
            } else {
                LOGGER.info("---------------StoreMasterImpl::getITGGeoMappingForUser::No valid geo mapping found for user----------------\n");
            }
            
            if ( geoLevel.equals("geoLevel5") ) {
            	rootGeo.setIsDefault(true);
            } else {
            	String geoLevelIdWithoutCountry = geoLevelId.substring(geoLevelId.indexOf("-")+1,geoLevelId.length());
                
                for(AreaGeo area : areas ) {
                	if (area.getAreaId().equals(geoLevelIdWithoutCountry)) {
                		area.setIsDefault(true);
                		break;
                	} else {
                		for(RegionGeo region : area.getRegions() ) {
                			if ( (area.getAreaId()+"-"+region.getRegionId()).equals(geoLevelIdWithoutCountry) ) {
                				region.setIsDefault(true);
                				break;
                			} else {
                				for(DivisionGeo division : region.getDivisions()) {
                					if ( (area.getAreaId()+"-"+region.getRegionId()+"-"+division.getDivisionId()).equals(geoLevelIdWithoutCountry) ) {
                						division.setIsDefault(true);
                						break;
                					} else {
                						for(TerritoryGeo territory : division.getTerritories()) {
                							if ( (area.getAreaId()+"-"+region.getRegionId()+"-"+division.getDivisionId()+"-"+territory.getTerritoryId()).equals(geoLevelIdWithoutCountry) ) {
                								territory.setIsDefault(true);
                								break;
                							}
                						}
                					}
                				}
                			}
                		}
                	}
                }
            }
            
            LOGGER.info("---------------ITGDashboardDaoImpl Ends getITGGeoMappingForUser::customerCode={}, userId={}----------------\n",customerCode,userId);

            return areas;
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
}

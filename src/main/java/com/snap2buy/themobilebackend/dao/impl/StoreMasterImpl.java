package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.StoreMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.StoreGeoLevel;
import com.snap2buy.themobilebackend.model.StoreMaster;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;

/**
 * Created by sachin on 10/31/15.
 */
@Component(value = BeanMapper.BEAN_STORE_MASTER_DAO)
@Scope("prototype")
public class StoreMasterImpl implements StoreMasterDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getStoreId(String longitude, String latitude) {
        LOGGER.info("---------------StoreMasterImpl Starts getStoreId::longitude={}, latitude={}",longitude, latitude);
        String sql = "SELECT * FROM StoreMaster";

        Connection conn = null;
        try {
            String storeId = "NA";
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            Double minDistance = 0.0;
            Double currDistance = 0.0;
            if (rs.next()) {
                minDistance = Math.abs(Double.parseDouble(longitude) - Double.parseDouble(rs.getString("Longitude"))) + Math.abs(Double.parseDouble(latitude) - Double.parseDouble(rs.getString("Latitude")));
                if (minDistance <= 0.005) {
                    storeId = rs.getString("StoreId");
                } else {
                    storeId = "0";
                }
            }
            while (rs.next()) {
                currDistance = Math.abs(Double.parseDouble(longitude) - Double.parseDouble(rs.getString("Longitude"))) + Math.abs(Double.parseDouble(latitude) - Double.parseDouble(rs.getString("Latitude")));

                if (currDistance < minDistance) {
                    minDistance = currDistance;
                    if (minDistance <= 0.005) {
                        storeId = rs.getString("StoreId");
                    } else {
                        storeId = "0";
                    }
                }
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getStoreId----------------\n");

            return storeId;
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
    public String getRetailerByStoreId(String storeId) {
        LOGGER.info("---------------StoreMasterImpl Starts getRetailerByStoreId::storeId={}",storeId);
        String sql = "SELECT Retailer,RetailerChainCode FROM StoreMaster WHERE storeId = ? ";

        Connection conn = null;
        String retailer = "NA";
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	retailer = rs.getString("Retailer");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getRetailerByStoreId----------------\n");

            return retailer;
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
    public List<LinkedHashMap<String,String>> getStoreOptions(String retailerCode) {
        LOGGER.info("---------------StoreMasterImpl Starts getStoreOptions----------------\n");
        String sql = "SELECT RetailerChainCode,Retailer,StateCode,State,City FROM StoreMaster where RetailerChainCode = ? group by RetailerChainCode,Retailer,StateCode,State,City";

        Connection conn = null;
        StoreMaster storeMaster = null;
        List<LinkedHashMap<String,String>> storeMasterList=new ArrayList<LinkedHashMap<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("retailerCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("city", rs.getString("City"));
                storeMasterList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getStoreOptions" + storeMasterList.size() + "----------------\n");

            return storeMasterList;
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
    public List<LinkedHashMap<String, String>>  getStores(String retailerChainCode, String stateCode, String city) {
        LOGGER.info("---------------StoreMasterImpl Starts getStores::retailerChainCode={}, stateCode={}, city={}",retailerChainCode,stateCode,city);
        String sql = "SELECT RetailerStoreID,StoreID,Street,Latitude,Longitude FROM StoreMaster where RetailerChainCode = ? and StateCode = ? and City = ?";

        Connection conn = null;
        StoreMaster storeMaster = null;
        List<LinkedHashMap<String,String>> storeMasterList=new ArrayList<LinkedHashMap<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerChainCode);
            ps.setString(2, stateCode);
            ps.setString(3, city);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("storeId", rs.getString("StoreID"));
                map.put("street",rs.getString("Street"));
                map.put("latitude",rs.getString("Latitude"));
                map.put("longitude",rs.getString("Longitude"));
                storeMasterList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getStores----------------\n");

            return storeMasterList;
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
    public List<LinkedHashMap<String, String>> getStoreMasterByPlaceId(String placeId) {
        LOGGER.info("---------------StoreMasterImpl Starts getStoreMasterByPlaceId::PlaceId={}", placeId);
        String sql = "SELECT RetailerStoreID, StoreID, Street, placeId FROM StoreMaster WHERE placeId = ? ";

        Connection conn = null;
        List<LinkedHashMap<String,String>> storeMasterList=new ArrayList<LinkedHashMap<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, placeId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("storeId", rs.getString("StoreID"));
                map.put("street",rs.getString("Street"));
                map.put("placeId",rs.getString("placeId"));
                storeMasterList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getStoreMasterByPlaceId----------------\n");

            return storeMasterList;
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
    public void createStoreWithPlaceId(StoreMaster storeMaster) {
        LOGGER.info("---------------StoreMasterDaoImpl Starts createStoreWithPlaceId::storeMaster={}", storeMaster);
        String sql = "INSERT INTO StoreMaster ( StoreID, RetailerStoreID, RetailerChainCode, Retailer, Street, City, StateCode, State, ZIP, Latitude, Longitude, comments ,placeId, createdDate, name, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeMaster.getStoreId());
            ps.setString(2, storeMaster.getRetailerStoreId());
            ps.setString(3, storeMaster.getRetailerChainCode());
            ps.setString(4, storeMaster.getRetailer());
            ps.setString(5, storeMaster.getStreet());
            ps.setString(6, storeMaster.getCity());
            ps.setString(7, storeMaster.getStateCode());
            ps.setString(8, storeMaster.getState());
            ps.setString(9, storeMaster.getZip());
            ps.setString(10, storeMaster.getLatitude());
            ps.setString(11, storeMaster.getLongitude());
            ps.setString(12, storeMaster.getComments());
            ps.setString(13, storeMaster.getPlaceId());
            ps.setTimestamp(14, timestamp);
            ps.setString(15, storeMaster.getName());
            ps.setString(16, storeMaster.getCountry());
            Boolean status = ps.execute();
            ps.close();
            LOGGER.info("---------------StoreMasterDaoImpl Ends createStoreWithPlaceId----------------\n");

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
    public List<LinkedHashMap<String, String>> getGeoMappedStoresByUserId(String customerCode, String userId) {
        LOGGER.info("---------------StoreMasterImpl Starts getGeoMappedStoresByUserId customerCode={},userId={}",customerCode, userId);

        String sql = "SELECT * from UserGeoMap where customercode = ? AND userId = ?";

        Connection conn = null;
        List<LinkedHashMap<String,String>> storeMasterList=new ArrayList<LinkedHashMap<String,String>>();
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
            	sql = "SELECT \n" + 
            			"    map.storeId,map.customerStoreNumber,\n" + 
            			"    store.name,store.placeId,store.Street,\n" + 
            			"    store.City,store.State,store.StateCode,\n" + 
            			"    store.RetailerStoreID,store.Latitude,store.Longitude,\n" + 
            			"    store.country,customerGeo.geoLevelName\n" + 
            			"FROM \n" + 
            			"    StoreGeoLevelMap map, StoreMaster store, CustomerGeoLevelMap customerGeo \n" + 
            			"WHERE \n" + 
            			"    map.customerCode= ? \n" + 
            			"    AND map.storeId=store.storeId \n" + 
            			"    AND map.customerCode = customerGeo.customerCode \n" + 
            			"    AND customerGeo.geoLevel = ?\n" + 
            			"    AND map.geoLevel1Id LIKE ? \n" + 
            			"    AND map.geoLevel2Id LIKE ? \n" + 
            			"    AND map.geoLevel3Id LIKE ? \n" + 
            			"    AND map.geoLevel4Id LIKE ? \n" + 
            			"    AND map.geoLevel5Id LIKE ? \n" + 
            			"ORDER BY \n" + 
            			"    map.customerStoreNumber LIMIT 10000";
                
                //LOGGER.info("---------------StoreMasterImpl::getGeoMappedStoresByUserId::query={}",sql);
                
                ps = conn.prepareStatement(sql);
                ps.setString(1, customerCode);
                ps.setString(2, geoLevel);
                ps.setString(3, geoLevel1Id);
                ps.setString(4, geoLevel2Id);
                ps.setString(5, geoLevel3Id);
                ps.setString(6, geoLevel4Id);
                ps.setString(7, geoLevel5Id);

                rs = ps.executeQuery();
                while (rs.next()) {
                    LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                    map.put("name", rs.getString("name"));
                    map.put("storeId", rs.getString("storeId"));
                    map.put("placeId",rs.getString("placeId"));
                    map.put("street",rs.getString("Street"));
                    map.put("city", rs.getString("City"));
                    map.put("state", rs.getString("State"));
                    map.put("stateCode", rs.getString("StateCode"));
                    map.put("lat", rs.getString("Latitude"));
                    map.put("lng", rs.getString("Longitude"));
                    map.put("country", rs.getString("country"));
                    map.put("geoLevel", geoLevel);
                    map.put("geoLevelId", geoLevelId);
                    map.put("geoLevelName", rs.getString("geoLevelName"));
                    map.put("customerStoreNumber", ConverterUtil.ifNullToEmpty(rs.getString("customerStoreNumber")));
                    map.put("retailerStoreNumber", ConverterUtil.ifNullToEmpty(rs.getString("RetailerStoreID")));
                    storeMasterList.add(map);
                }
                rs.close();
                ps.close();
            } else {
                LOGGER.info("---------------StoreMasterImpl::getGeoMappedStoresByUserId::No valid geo mapping found for user----------------\n");
            }
            
            LOGGER.info("---------------StoreMasterImpl Ends getGeoMappedStoresByUserId----------------\n");

            return storeMasterList;
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
    public Map<String, List<String>> getNextGeoLevelStoresByLevel(String customerCode, String geoLevel, String geoLevelId) {
        LOGGER.info("---------------StoreMasterImpl Starts getNextGeoLevelStoresByLevel ::customerCode={}, geoLevel={}, geoLevelId={}",customerCode,geoLevel,geoLevelId);

        Map<String,List<String>> storesMap = new HashMap<String,List<String>>();

        String sqlTemplate = "SELECT" + 
				"    distinct(map.storeId)," + 
				"    customerGeo.geoLevelName" + 
				" FROM" + 
				"    StoreGeoLevelMap map, CustomerGeoLevelMap customerGeo" + 
				" WHERE" + 
				"    map.customerCode = ?" + 
				"    AND map.{1}Id = (" + 
				"        SELECT" + 
				"            distinct(map.{1}Id)" + 
				"        FROM" + 
				"            StoreGeoLevelMap" + 
				"        WHERE" + 
				"            StoreGeoLevelMap.customerCode=?" + 
				"            AND StoreGeoLevelMap.{0}Id = ?" + 
				"    )" + 
				"    AND map.customerCode = customerGeo.customerCode" + 
				"    AND customerGeo.geoLevel = \"{1}\""; 
        
        if (StoreGeoLevel.contains(geoLevel)) {
        	String nextLevel = "geoLevel"+(Integer.parseInt(geoLevel.substring(geoLevel.length() - 1))+1);
        	
        	if ( !StoreGeoLevel.contains(nextLevel)) {
                LOGGER.info("---------------StoreMasterImpl::getNextGeoLevelStoresByLevel::No valid next level geo mapping found::using same level as next level----------------\n");
        		nextLevel = geoLevel;
        	}
        	
        	String sql = MessageFormat.format(sqlTemplate, geoLevel, nextLevel);
            
            Connection conn = null;
            try {
                conn = dataSource.getConnection();

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, customerCode);
                ps.setString(2, customerCode);
                ps.setString(3, geoLevelId);
                
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                	String nextLevelName = rs.getString("geoLevelName");
                	if ( !storesMap.containsKey(nextLevelName)) {
                		storesMap.put(nextLevelName,new ArrayList<String>());
                	}
                    storesMap.get(nextLevelName).add(rs.getString("storeId"));
                }
                rs.close();
                ps.close();

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
        } else {
            LOGGER.info("---------------StoreMasterImpl::getNextGeoLevelStoresByLevel::No valid geo mapping found for geo level----------------\n");
        }
        
        LOGGER.info("---------------StoreMasterImpl Ends getNextGeoLevelStoresByLevel----------------\n");
        return storesMap;
    }
    
    @Override
    public void createStores(List<StoreMaster> stores) {
        LOGGER.info("---------------StoreMasterDaoImpl Starts createStores::no of stores={}", stores.size());
        String sql = "INSERT IGNORE INTO StoreMaster ( StoreID, RetailerStoreID, RetailerChainCode, Retailer, Street, City, StateCode, State, ZIP, Latitude, Longitude, comments, placeId, name, createdDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for ( StoreMaster store : stores) {
            	ps.setString(1, store.getStoreId());
                ps.setString(2, store.getRetailerStoreId());
                ps.setString(3, store.getRetailerChainCode());
                ps.setString(4, store.getRetailer());
                ps.setString(5, store.getStreet());
                ps.setString(6, store.getCity());
                ps.setString(7, store.getStateCode());
                ps.setString(8, store.getState());
                ps.setString(9, store.getZip());
                ps.setString(10, store.getLatitude());
                ps.setString(11, store.getLongitude());
                ps.setString(12, store.getComments());
                ps.setString(13, store.getPlaceId());
                ps.setString(14, store.getName());
                ps.setTimestamp(15, timestamp);
                ps.addBatch();
                if(++count % batchSize == 0) {
            		ps.executeBatch();
            	}                
            }
            ps.executeBatch();
            
            conn.commit();
            
            ps.close();
            LOGGER.info("---------------StoreMasterDaoImpl Ends createStores----------------\n");

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
    public void updateStores(List<StoreMaster> stores) {
        LOGGER.info("---------------StoreMasterDaoImpl Starts updateStores::no of stores={}", stores.size());
        String sql = "UPDATE StoreMaster " +
                "set RetailerStoreID= ?" +
                ", RetailerChainCode= ? " +
                ", Retailer= ? " +
                ", Street=?" +
                ", City=?" +
                ", StateCode=?" +
                ", State=? " +
                ", ZIP=? " +
                ", Latitude=?  " +
                ", Longitude=? " +
                ", comments=? " +
                "where StoreID=? ";
        Connection conn = null;
        
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for ( StoreMaster store : stores) {
                ps.setString(1, store.getRetailerStoreId());
                ps.setString(2, store.getRetailerChainCode());
                ps.setString(3, store.getRetailer());
                ps.setString(4, store.getStreet());
                ps.setString(5, store.getCity());
                ps.setString(6, store.getStateCode());
                ps.setString(7, store.getState());
                ps.setString(8, store.getZip());
                ps.setString(9, store.getLatitude());
                ps.setString(10, store.getLongitude());
                ps.setString(11, store.getComments());
                ps.setString(12, store.getStoreId());
                ps.addBatch();
                if(++count % batchSize == 0) {
            		ps.executeBatch();
            	}                
            }
            ps.executeBatch();
            
            conn.commit();
            
            ps.close();
            LOGGER.info("---------------StoreMasterDaoImpl Ends updateStores----------------\n");

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
	public void createStoreGeoMappings(String customerCode, Map<String,Map<String, String>> storeGeoMap) {
        LOGGER.info("---------------StoreMasterDaoImpl Starts createStoreGeoMappings::no of mappings={}", storeGeoMap.size());
        String sql = "INSERT IGNORE INTO StoreGeoLevelMap (customerCode, storeId, geoLevel1Id, geoLevel2Id, geoLevel3Id, geoLevel4Id, geoLevel5Id, customerStoreNumber)"
        		+ " VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = null;
        
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for ( String oneStoreId : storeGeoMap.keySet()) {
            	ps.setString(1, customerCode);
                ps.setString(2, oneStoreId);
                ps.setString(3, storeGeoMap.get(oneStoreId).get("geoLevel1Id"));
                ps.setString(4, storeGeoMap.get(oneStoreId).get("geoLevel2Id"));
                ps.setString(5, storeGeoMap.get(oneStoreId).get("geoLevel3Id"));
                ps.setString(6, storeGeoMap.get(oneStoreId).get("geoLevel4Id"));
                ps.setString(7, storeGeoMap.get(oneStoreId).get("geoLevel5Id"));
                ps.setString(8, storeGeoMap.get(oneStoreId).get("customerStoreNumber"));

                ps.addBatch();
                if(++count % batchSize == 0) {
            		ps.executeBatch();
            	}                
            }
            ps.executeBatch();
            
            conn.commit();
            
            ps.close();
            LOGGER.info("---------------StoreMasterDaoImpl Ends createStoreGeoMappings----------------\n");

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
	public void createStoreGeoMappingsViaPlaceID(String customerCode, Map<String,Map<String, String>> placeIdGeoMap) {
        LOGGER.info("---------------StoreMasterDaoImpl Starts createStoreGeoMappingsViaPlaceID::no of mappings={}", placeIdGeoMap.size());
        String sql = "INSERT IGNORE INTO StoreGeoLevelMap"
        		+ " (customerCode, storeId, geoLevel1Id, geoLevel2Id, geoLevel3Id, geoLevel4Id, geoLevel5Id, customerStoreNumber)"
        		+ " VALUES (?,(SELECT storeId from StoreMaster where placeId = ?),?,?,?,?,?,?)";
        Connection conn = null;
        
        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            for ( String onePlaceId : placeIdGeoMap.keySet()) {
            	ps.setString(1, customerCode);
                ps.setString(2, onePlaceId);
                ps.setString(3, placeIdGeoMap.get(onePlaceId).get("geoLevel1Id"));
                ps.setString(4, placeIdGeoMap.get(onePlaceId).get("geoLevel2Id"));
                ps.setString(5, placeIdGeoMap.get(onePlaceId).get("geoLevel3Id"));
                ps.setString(6, placeIdGeoMap.get(onePlaceId).get("geoLevel4Id"));
                ps.setString(7, placeIdGeoMap.get(onePlaceId).get("geoLevel5Id"));
                ps.setString(8, placeIdGeoMap.get(onePlaceId).get("customerStoreNumber"));
                ps.addBatch();
                if(++count % batchSize == 0) {
            		ps.executeBatch();
            	}                
            }
            ps.executeBatch();
            
            conn.commit();
            
            ps.close();
            LOGGER.info("---------------StoreMasterDaoImpl Ends createStoreGeoMappingsViaPlaceID----------------\n");

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
    public LinkedHashMap<String, String> getRetailerStoreIdMap(String retailerChainCode) {
        LOGGER.info("---------------StoreMasterImpl Starts getRetailerStoreIdMap::retailerChainCode={}",retailerChainCode);
        String sql = "SELECT RetailerStoreID,StoreID FROM StoreMaster where RetailerChainCode = ?";
         Connection conn = null;
        LinkedHashMap<String,String> result =new LinkedHashMap<String,String>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerChainCode);
             ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(retailerChainCode+"_"+rs.getString("RetailerStoreID"), rs.getString("StoreID"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreMasterImpl Ends getStores----------------\n");
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
}

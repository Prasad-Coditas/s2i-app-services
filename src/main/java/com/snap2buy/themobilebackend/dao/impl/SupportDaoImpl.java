package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.SupportDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Anoop
 */
@Component(value = BeanMapper.BEAN_SUPPORT_DAO)
@Scope("prototype")
public class SupportDaoImpl implements SupportDao {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	@Override
	public List<Map<String, String>> getStoreVisitsByUserId(String customerCode, String userId) {
		LOGGER.info("---------------SupportDaoImpl--getStoreVisitsByUserId::customerCode={}, userId={}",customerCode,userId);
		Connection conn = null;
		String query = "SELECT\n" + 
				"	u.userId, u.firstName, u.lastName, u.phoneNumber, u.email, u.lastLoginDate, u.lastAccessedDate,\n" + 
				"    storeVisits.storeId, storeVisits.retailerStoreId, storeVisits.street, storeVisits.city, storeVisits.state, storeVisits.zip,\n" + 
				"	storeVisits.projectId, storeVisits.projectName, storeVisits.agentId, storeVisits.visitDateId, storeVisits.receivedImageCount,\n" + 
				"	storeVisits.expectedImageCount,storeVisits.processedDate\n" + 
				"FROM\n" + 
				"	User u\n" + 
				"	LEFT JOIN\n" + 
				"	(\n" + 
				"		SELECT \n" + 
				"			visits.agentId, visits.projectId, visits.projectName, visits.storeId, visits.taskId, visits.visitDateId, visits.processedDate, \n" + 
				"			visits.receivedImageCount, CAST(repResponses.imageCount AS UNSIGNED) AS expectedImageCount,\n" + 
				"            visits.retailerStoreId, visits.street, visits.city, visits.state, visits.zip\n" + 
				"		FROM\n" + 
				"            (\n" + 
				"                SELECT \n" + 
				"			        psr.agentId, psr.projectId, p.projectName, psr.storeId, psr.taskId, psr.visitDateId, psr.processedDate, \n" + 
				"			        count(im.imageUUID) AS receivedImageCount, sm.retailerStoreId, sm.street, sm.city, sm.state, sm.zip\n" + 
				"		        FROM \n" + 
				"			        ProjectStoreResult psr\n" + 
				"			        LEFT JOIN \n" + 
				"				        ImageStoreNew im\n" + 
				"			        ON  \n" + 
				"				        psr.projectId = im.projectId AND\n" + 
				"				        psr.storeId = im.storeId AND\n" + 
				"				        psr.taskId = im.taskId\n" + 
				"			        LEFT JOIN\n" + 
				"				        Project p\n" + 
				"			        ON\n" + 
				"				        psr.projectId = p.id\n" + 
				"			        LEFT JOIN\n" + 
				"				        StoreMaster sm\n" + 
				"			        ON\n" + 
				"				        psr.storeId = sm.storeId\n" + 
				"			        INNER JOIN\n" + 
				"				        (\n" + 
				"					        SELECT DISTINCT(visitDateId) FROM ProjectStoreResult WHERE agentId = ? ORDER BY visitDateId DESC LIMIT 10\n" + 
				"				        ) dates\n" + 
				"			        ON\n" + 
				"				        psr.visitDateId = dates.visitDateId\n" + 
				"			        WHERE \n" + 
				"				        psr.agentId = ? \n" + 
				"			        GROUP BY \n" + 
				"				        psr.storeId, psr.projectId,psr.agentId, psr.taskId, psr.visitDateId\n" + 
				"            ) visits \n" + 
				"            LEFT JOIN\n" + 
				"                (\n" + 
				"					SELECT prr.projectId,prr.storeId,prr.taskId,sum(prr.repResponse) as imageCount\n" + 
				"					FROM ProjectRepResponses prr\n" + 
				"					INNER JOIN\n" + 
				"						ProjectRepQuestions prq\n" + 
				"					ON\n" + 
				"						prr.projectId = prq.projectId AND\n" + 
				"						prr.questionid = prq.questionId \n" + 
				"					WHERE prq.questionType = 'PH'\n" + 
				"					GROUP BY prr.projectId,prr.storeId,prr.taskId\n" + 
				"				) repResponses \n" + 
				"            ON \n" + 
				"                visits.projectId = repResponses.projectId AND \n" + 
				"                visits.storeId = repResponses.storeId AND \n" + 
				"                visits.taskId = repResponses.taskId\n" + 
				"		) storeVisits\n" + 
				"	ON \n" + 
				"		u.userId = storeVisits.agentId\n" + 
				"WHERE\n" + 
				"	u.userId = ? AND\n" + 
				"    u.customerCode = ?\n" + 
				"ORDER BY storeVisits.visitDateId DESC";
		List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setString(1, userId);
			ps.setString(2, userId);
			ps.setString(3, userId);
			ps.setString(4, customerCode);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				Map<String,String> storeMap = new HashMap<String,String>();
				storeMap.put("userId", rs.getString("userId"));
				storeMap.put("firstName", rs.getString("firstName"));
				storeMap.put("lastName", rs.getString("lastName"));
				storeMap.put("email", rs.getString("email"));
				storeMap.put("phoneNumber", ConverterUtil.ifNullToNA(rs.getString("phoneNumber")));
				storeMap.put("lastLoginDate", rs.getString("lastLoginDate"));
				storeMap.put("lastAccessedDate", rs.getString("lastAccessedDate"));
				storeMap.put("storeId", ConverterUtil.ifNullToNA(rs.getString("retailerStoreId")));
				storeMap.put("street", rs.getString("street"));
				storeMap.put("city", rs.getString("city"));
				storeMap.put("state", rs.getString("state"));
				storeMap.put("zip", rs.getString("zip"));
				storeMap.put("projectId", rs.getString("projectId"));
				storeMap.put("projectName", rs.getString("projectName"));
				storeMap.put("visitDateId", rs.getString("visitDateId"));
				storeMap.put("processedDate", rs.getString("processedDate"));
				storeMap.put("receivedImageCount", ConverterUtil.ifNullToZero(rs.getString("receivedImageCount")));
				storeMap.put("expectedImageCount", ConverterUtil.ifNullToZero(rs.getString("expectedImageCount")));
				returnList.add(storeMap);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching recent store visits :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------SupportDaoImpl--getStoreVisitsByUserId completed");
		return returnList;
	}
	
	@Override
	public List<Map<String, String>> getStoreVisitsByStoreId(String customerCode, String storeId) {
		LOGGER.info("---------------SupportDaoImpl--getStoreVisitsByStoreId::customerCode={},storeId={}",customerCode,storeId);
		Connection conn = null;
		String query = "SELECT\n" + 
				"	sm.storeId, sm.retailerStoreId, sm.street, sm.city, sm.state, sm.zip,\n" + 
				"	storeVisits.projectId, storeVisits.projectName, storeVisits.agentId, storeVisits.visitDateId, storeVisits.receivedImageCount,\n" + 
				"	storeVisits.expectedImageCount,storeVisits.processedDate, storeVisits.firstName, storeVisits.lastName, storeVisits.phoneNumber\n" + 
				"FROM\n" + 
				"	StoreMaster sm\n" + 
				"	LEFT JOIN\n" + 
				"	(\n" + 
				"		SELECT \n" + 
				"			psr.agentId, psr.projectId, p.projectName, psr.storeId, psr.taskId, psr.visitDateId, psr.processedDate, \n" + 
				"			count(im.imageUUID) AS receivedImageCount, CAST(prr.imageCount AS UNSIGNED) AS expectedImageCount, u.firstName, u.lastName, u.phoneNumber\n" + 
				"		FROM \n" + 
				"			ProjectStoreResult psr\n" + 
				"			LEFT JOIN \n" + 
				"				ImageStoreNew im\n" + 
				"			ON  \n" + 
				"				psr.projectId = im.projectId AND\n" + 
				"				psr.storeId = im.storeId AND\n" + 
				"				psr.taskId = im.taskId\n" + 
				"			LEFT JOIN \n" + 
				"				(\n" + 
				"					SELECT prr.projectId,prr.storeId,prr.taskId,sum(prr.repResponse) as imageCount\n" + 
				"					FROM ProjectRepResponses prr\n" + 
				"					INNER JOIN\n" + 
				"						ProjectRepQuestions prq\n" + 
				"					ON\n" + 
				"						prr.projectId = prq.projectId AND\n" + 
				"						prr.questionid = prq.questionId \n" + 
				"					WHERE prr.storeId = ? AND prq.questionType = 'PH'\n" + 
				"					GROUP BY prr.projectId,prr.storeId,prr.taskId\n" + 
				"				) prr\n" + 
				"			ON  \n" + 
				"				psr.projectId = prr.projectId AND\n" + 
				"				psr.storeId = prr.storeId AND\n" + 
				"				psr.taskId = prr.taskId\n" + 
				"			LEFT JOIN\n" + 
				"				Project p\n" + 
				"			ON\n" + 
				"				psr.projectId = p.id\n" + 
				"			LEFT JOIN\n" + 
				"				User u\n" + 
				"			ON\n" + 
				"				psr.agentId = u.userId\n" + 
				"			INNER JOIN\n" + 
				"				(\n" + 
				"					SELECT DISTINCT(visitDateId) FROM ProjectStoreResult WHERE storeId = ? ORDER BY visitDateId DESC LIMIT 10\n" + 
				"				) dates\n" + 
				"			ON\n" + 
				"				psr.visitDateId = dates.visitDateId\n" + 
				"			WHERE \n" + 
				"				psr.storeId = ? \n" + 
				"			GROUP BY \n" + 
				"				psr.storeId, psr.projectId,psr.agentId, psr.taskId, psr.visitDateId\n" + 
				"		) storeVisits\n" + 
				"	ON \n" + 
				"		sm.storeId = storeVisits.storeId\n" + 
				"WHERE\n" + 
				"	sm.storeId = ?\n" + 
				"ORDER BY storeVisits.visitDateId DESC";
		
		List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
		
		String internalStoreId = customerCode+"_"+storeId;
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setString(1, internalStoreId);
			ps.setString(2, internalStoreId);
			ps.setString(3, internalStoreId);
			ps.setString(4, internalStoreId);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				Map<String,String> storeMap = new HashMap<String,String>();
				storeMap.put("storeId", ConverterUtil.ifNullToNA(rs.getString("retailerStoreId")));
				storeMap.put("street", rs.getString("street"));
				storeMap.put("city", rs.getString("city"));
				storeMap.put("state", rs.getString("state"));
				storeMap.put("zip", rs.getString("zip"));
				storeMap.put("userId", rs.getString("agentId"));
				storeMap.put("firstName", rs.getString("firstName"));
				storeMap.put("lastName", rs.getString("lastName"));
				storeMap.put("phoneNumber", ConverterUtil.ifNullToNA(rs.getString("phoneNumber")));
				storeMap.put("projectId", rs.getString("projectId"));
				storeMap.put("projectName", rs.getString("projectName"));
				storeMap.put("visitDateId", rs.getString("visitDateId"));
				storeMap.put("processedDate", rs.getString("processedDate"));
				storeMap.put("receivedImageCount", ConverterUtil.ifNullToZero(rs.getString("receivedImageCount")));
				storeMap.put("expectedImageCount", ConverterUtil.ifNullToZero(rs.getString("expectedImageCount")));
				returnList.add(storeMap);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching recent store visits :: EXCEPTION {} , {}", e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------SupportDaoImpl--getStoreVisitsByStoreId completed");
		return returnList;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getUserListByCustomerCode(String customerCode) {
		LOGGER.info("---------------SupportDaoImpl--getUserListByCustomerCode::customerCode={}",customerCode);
		Connection conn = null;
		String query = "SELECT firstName, lastName, userId, email, phoneNumber FROM User WHERE customerCode=?";
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String, Object>>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(query);
			
			ps.setString(1, customerCode);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				LinkedHashMap<String,Object> userMap = new LinkedHashMap<String,Object>();
				userMap.put("firstName", rs.getString("firstName"));
				userMap.put("lastName", rs.getString("lastName"));
				userMap.put("userId", rs.getString("userId"));
				userMap.put("email", rs.getString("email"));
				userMap.put("phoneNumber", ConverterUtil.ifNullToNA(rs.getString("phoneNumber")));
				returnList.add(userMap);
			}
			
			rs.close();
			ps.close();
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching users for customerCode {} :: EXCEPTION {} , {}", customerCode, e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------SupportDaoImpl--getStoreVisitsByStoreId completed");
		return returnList;
	}
	
	@Override
	public Map<String,String> getStoreAssignementByStoreId(String customerCode, String storeId) {
		LOGGER.info("---------------SupportDaoImpl--getStoreAssignementByStoreId::customerCode={},storeId={}",customerCode,storeId);
		Connection conn = null;
		String customerGeoNamesSql = "SELECT * FROM CustomerGeoLevelMap WHERE customercode=?";
		String mappedUserSql = "SELECT userId FROM UserGeoMap WHERE customerCode=? AND geoLevel=? AND geoLevelId=?";
		String geoDataSql = "SELECT \n" + 
				"    storeId,geoLevel5Id,geoLevel4Id,geoLevel3Id,geoLevel2Id,geoLevel1Id\n" + 
				"FROM\n" + 
				"    StoreGeoLevelMap\n" + 
				"WHERE\n" + 
				"    storeId=? AND customerCode=?";
		
		String internalStoreId = customerCode+"_"+storeId;
		
		Map<String,String> map = new HashMap<String,String>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(customerGeoNamesSql);
			
			ps.setString(1, customerCode);
			
			ResultSet rs = ps.executeQuery();
			Map<String,String> customerGeoMap = new HashMap<String,String>();
			while(rs.next()) {
				customerGeoMap.put(rs.getString("geoLevel"), rs.getString("geoLevelName"));
			}
			
			rs.close();
			ps.close();
			
			PreparedStatement ps1 = conn.prepareStatement(geoDataSql);
			ps1.setString(1, internalStoreId);
			ps1.setString(2, customerCode);
			
			ResultSet rs1 = ps1.executeQuery();
			String geoLevel1Id="",geoLevel2Id="",geoLevel3Id="",geoLevel4Id="",geoLevel5Id="";
			while(rs1.next()) {
				geoLevel1Id = rs1.getString("geoLevel1Id");
				geoLevel2Id = rs1.getString("geoLevel2Id");
				geoLevel3Id = rs1.getString("geoLevel3Id");
				geoLevel4Id = rs1.getString("geoLevel4Id");
				geoLevel5Id = rs1.getString("geoLevel5Id");
			}
			
			rs1.close();
			ps1.close();
			
			String geoMappingString = geoLevel5Id+"-"+geoLevel4Id+"-"+geoLevel3Id+"-"+geoLevel2Id+"-"+geoLevel1Id;
			String geoMappingStringForUI = customerGeoMap.get("geoLevel4")+" " +geoLevel4Id+" - "+
					customerGeoMap.get("geoLevel3")+" " + geoLevel3Id+" - "+
					customerGeoMap.get("geoLevel2")+" " + geoLevel2Id+" - "+
					customerGeoMap.get("geoLevel1")+" " + geoLevel1Id;
			
			PreparedStatement ps2 = conn.prepareStatement(mappedUserSql);
			ps2.setString(1, customerCode);
			ps2.setString(2, "geoLevel1");
			ps2.setString(3, geoMappingString);
			
			ResultSet rs2 = ps2.executeQuery();
			String userId = "";
			while(rs2.next()) {
				userId = rs2.getString("userId");
			}
			
			rs2.close();
			ps2.close();
			
			map.put("mappedUser",userId);
			map.put("mappedTerritory", geoMappingStringForUI);
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching users for customerCode {} :: EXCEPTION {} , {}", customerCode, e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------SupportDaoImpl--getStoreAssignementByStoreId completed");
		return map;
	}
	
	@Override
	public Map<String,String> getStoreAssignementByUserId(String customerCode, String userId) {
		LOGGER.info("---------------SupportDaoImpl--getStoreAssignementByStoreId::customerCode={},userId={}",customerCode,userId);
		Connection conn = null;
		String customerGeoNamesSql = "SELECT * FROM CustomerGeoLevelMap WHERE customercode=?";
		String mappedUserSql = "SELECT geoLevel,geolevelId FROM UserGeoMap WHERE customerCode=? AND userId=?";
		
		Map<String,String> map = new HashMap<String,String>();
		
		try {
			conn = dataSource.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(customerGeoNamesSql);
			
			ps.setString(1, customerCode);
			
			ResultSet rs = ps.executeQuery();
			Map<String,String> customerGeoMap = new HashMap<String,String>();
			while(rs.next()) {
				customerGeoMap.put(rs.getString("geoLevel"), rs.getString("geoLevelName"));
			}
			
			rs.close();
			ps.close();
			
			PreparedStatement ps1 = conn.prepareStatement(mappedUserSql);
			ps1.setString(1, customerCode);
			ps1.setString(2, userId);
			
			ResultSet rs1 = ps1.executeQuery();
			String geoLevelId = "", geoLevel="";
			while(rs1.next()) {
				geoLevel = rs1.getString("geoLevel");
				geoLevelId = rs1.getString("geoLevelId");
			}
			
			rs1.close();
			ps1.close();
			
			String geoMappingStringForUI = "";
			switch(geoLevel) {
			case "geoLevel1":
				String[] parts1 = geoLevelId.split("-");
				geoMappingStringForUI = customerGeoMap.get("geoLevel4")+" " +parts1[1]+" - "+
						customerGeoMap.get("geoLevel3")+" " + parts1[2]+" - "+
						customerGeoMap.get("geoLevel2")+" " + parts1[3]+" - "+
						customerGeoMap.get("geoLevel1")+" " + parts1[4];
				break;
			case "geoLevel2":
				String[] parts2 = geoLevelId.split("-");
				geoMappingStringForUI = customerGeoMap.get("geoLevel4")+" " +parts2[1]+" - "+
						customerGeoMap.get("geoLevel3")+" " + parts2[2]+" - "+
						customerGeoMap.get("geoLevel2")+" " + parts2[3];
				break;
			case "geoLevel3":
				String[] parts3 = geoLevelId.split("-");
				geoMappingStringForUI = customerGeoMap.get("geoLevel4")+" " +parts3[1]+" - "+
						customerGeoMap.get("geoLevel3")+" " + parts3[2];
				break;
			case "geoLevel4":
				String[] parts4 = geoLevelId.split("-");
				geoMappingStringForUI = customerGeoMap.get("geoLevel4")+" " +parts4[1];
				break;
			case "geoLevel5":
				geoMappingStringForUI = "Nationwide";
				break;
			}
			
			map.put("mappedUser",userId);
			map.put("mappedTerritory", geoMappingStringForUI);
		}
		catch (Exception e) {
			LOGGER.error("Error while fetching users for customerCode {} :: EXCEPTION {} , {}", customerCode, e.getMessage(), e);
	    } finally {
	      	if (conn != null) {
	          try {
	        	  conn.close();
	          } catch (SQLException e) {
	              LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	          }
	      	}
	    }
		
		LOGGER.info("---------------SupportDaoImpl--getStoreAssignementByStoreId completed");
		return map;
	}
}

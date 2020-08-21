package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ShelfAnalysisDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.ShelfAnalysis;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import com.snap2buy.themobilebackend.util.StandardTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.script.*;
import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sachin on 10/31/15.
 */
@Component(value = BeanMapper.BEAN_SHELF_ANALYSIS_DAO)
@Scope("prototype")
public class ShelfAnalysisDaoImpl implements ShelfAnalysisDao {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) throws ScriptException, IOException {

		StringWriter writer = new StringWriter(); // ouput will be stored here

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptContext context = new SimpleScriptContext();

		context.setWriter(writer); // configures output redirection
		ScriptEngine engine = manager.getEngineByName("python");
		engine.eval(new FileReader("/Users/sachin/test/test.py"), context);
		System.out.println(writer.toString());
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void storeShelfAnalysis(ShelfAnalysis shelfAnalysis) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts storeShelfAnalysis::=shelfAnalysis {}",shelfAnalysis.toString());
		String sql = "INSERT INTO ShelfAnalysis (imageUUID, product_code, expected_facings, on_shelf_availability, detected_facings, promotion_label_present, price, promo_price, storeId, categoryId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, shelfAnalysis.getImageUUID());
			ps.setString(2, shelfAnalysis.getUpc());
			ps.setString(3, shelfAnalysis.getExpected_facings());
			ps.setString(4, shelfAnalysis.getOn_shelf_availability());
			ps.setString(5, shelfAnalysis.getDetected_facings());
			ps.setString(6, shelfAnalysis.getPromotion_label_present());
			ps.setString(7, shelfAnalysis.getPrice());
			ps.setString(8, shelfAnalysis.getPromo_price());
			ps.setString(9, shelfAnalysis.getStoreId());
			ps.setString(10, shelfAnalysis.getCategoryId());
			ps.executeUpdate();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends storeShelfAnalysis----------------\n");

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
	public ShelfAnalysis getShelfAnalysis(String imageUUID) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getShelfAnalysis::imageUUID= {}", imageUUID);
		String sql = "SELECT * FROM ShelfAnalysis WHERE imageUUID = ?";

		Connection conn = null;
		ShelfAnalysis shelfAnalysis = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, imageUUID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				shelfAnalysis = new ShelfAnalysis(rs.getString("imageUUID"), rs.getString("product_code"),
						rs.getString("expected_facings"), rs.getString("on_shelf_availability"),
						rs.getString("detected_facings"), rs.getString("promotion_label_present"),
						rs.getString("price"), rs.getString("promo_price"), rs.getString("storeId"),
						rs.getString("categoryId"));
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getShelfAnalysis----------------\n");

			return shelfAnalysis;
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
	public List<ShelfAnalysis> getShelfAnalysisCsv() {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getShelfAnalysisCsv----------------\n");
		String sql = "SELECT * FROM ShelfAnalysis";

		Connection conn = null;
		ShelfAnalysis shelfAnalysis = null;
		List<ShelfAnalysis> shelfAnalysisList = new ArrayList<ShelfAnalysis>();

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				shelfAnalysis = new ShelfAnalysis(rs.getString("imageUUID"), rs.getString("product_code"),
						rs.getString("expected_facings"), rs.getString("on_shelf_availability"),
						rs.getString("detected_facings"), rs.getString("promotion_label_present"),
						rs.getString("price"), rs.getString("promo_price"), rs.getString("storeId"),
						rs.getString("categoryId"));
				shelfAnalysisList.add(shelfAnalysis);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getShelfAnalysisCsv----------------\n");

			return shelfAnalysisList;
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
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStores(int projectId, String month, String rollup) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProjectBrandSharesAllStores::projectId={}, month={} ,rollup={}" + projectId,month,rollup);

		String sqlFileName = "getProjectBrandSharesAllStores_"+rollup+".sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
	    boolean isBrandRollup = "brand".equals(rollup);
	    
		List<LinkedHashMap<String, Object>> stores = new ArrayList<LinkedHashMap<String, Object>>();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY");
		SimpleDateFormat visitDateSdf = new SimpleDateFormat("yyyyMMdd");

		List<Map<String, Object>> listForShelfLevel = new ArrayList<Map<String, Object>>();
		if ( isBrandRollup ) {
			listForShelfLevel = getShelfLevelFacingsForStores(projectId, month);
		}
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, month + "%");
			ps.setInt(3, projectId);
			ps.setString(4, month + "%");

			ResultSet rs = ps.executeQuery();
			String prevStoreId = null;
			int storeCount = -1;
			while (rs.next()) {
				String storeId = rs.getString("storeId");
				if (prevStoreId == null || !storeId.equals(prevStoreId)) {
					storeCount++;
					prevStoreId = storeId;
					LinkedHashMap<String, Object> storeData = new LinkedHashMap<String, Object>();
					storeData.put("storeId", storeId);
					storeData.put("retailerStoreId", rs.getString("retailerStoreId"));
					storeData.put("retailer", rs.getString("retailer"));
					storeData.put("street", rs.getString("street"));
					storeData.put("city", rs.getString("city"));
					storeData.put("state", rs.getString("state"));
					storeData.put("taskId", rs.getString("taskId"));
					storeData.put("agentId", rs.getString("agentId"));
					storeData.put("visitDate", sdf.format(visitDateSdf.parse(rs.getString("visitDateId"))));
					storeData.put("processedDate", rs.getString("processedDate"));
					storeData.put("imageUUID", rs.getString("imageUUID"));

					stores.add(storeCount, storeData);
				}

				LinkedHashMap<String, Object> storeData = stores.get(storeCount);

				LinkedHashMap<String, Object> brandInfo = new LinkedHashMap<String, Object>();
				if ( isBrandRollup ) {
					brandInfo.put("brandName", rs.getString("BRAND_NAME"));
					brandInfo.put("upcCount", rs.getString("BRAND_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("BRAND_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
				} else {
					brandInfo.put("mfgName", rs.getString("MFG_NAME"));
					brandInfo.put("upcCount", rs.getString("MFG_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("MFG_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("MFG_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("MFG_FACES_SHARE"));
				}
				
				if (isBrandRollup) {
					List<Map<String, Object>> facings = StandardTemplates.getShelfLevelTemplate();

					for (Map<String, Object> map : listForShelfLevel) {
						if (storeId.equals(map.get("storeId"))) {

							if (rs.getString("BRAND_NAME").equals(map.get("brand"))) {
								map.remove("brand");

								if ("Top".equals((String) map.get("levelName"))) {
									facings.get(0).put("facingCount", map.get("facingCount"));
								} else if ("Middle".equals((String) map.get("levelName"))) {
									facings.get(1).put("facingCount", map.get("facingCount"));
								} else if ("Bottom".equals((String) map.get("levelName"))) {
									facings.get(2).put("facingCount", map.get("facingCount"));
								} else if ("NA".equals((String) map.get("levelName"))) {
									facings.get(3).put("facingCount", map.get("facingCount"));
								}
							}
						}
					}

					if (facings.get(3).get("facingCount").equals("0")) {
						facings.remove(3);
					}
					brandInfo.put("shelfLevel", facings);
				}

				if (storeData.get(isBrandRollup ? "brands" : "manufacturers") == null) {
					storeData.put(isBrandRollup ? "brands" : "manufacturers", new ArrayList<LinkedHashMap<String, String>>());
				}
				((ArrayList<LinkedHashMap<String, Object>>) storeData.get(isBrandRollup ? "brands" : "manufacturers")).add(brandInfo);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProjectBrandSharesAllStores----------------\n");
			return stores;

		} catch (Exception e) {
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
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStates(int projectId, String month, String rollup) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProjectBrandSharesAllStates::projectId={}, rollup={}, month={}",projectId,rollup,month);
		String sqlFileName = "getProjectBrandSharesAllStates_"+rollup+".sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
	    boolean isBrandRollup = "brand".equals(rollup);
	    
		List<LinkedHashMap<String, Object>> states = new ArrayList<LinkedHashMap<String, Object>>();

		List<Map<String, Object>> listForShelfLevel = new ArrayList<Map<String, Object>>();

		if ( isBrandRollup ) {
			listForShelfLevel = getShelfLevelFacingsForStates(projectId, month);
		}

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, month + "%");
			ps.setInt(3, projectId);
			ps.setString(4, month + "%");
			ps.setInt(5, projectId);
			ps.setString(6, month + "%");
			ps.setInt(7, projectId);
			ps.setString(8, month + "%");

			ResultSet rs = ps.executeQuery();
			String prevState = null;
			int stateCount = -1;
			while (rs.next()) {
				String state = rs.getString("State");
				if (prevState == null || !state.equals(prevState)) {
					stateCount++;
					prevState = state;

					LinkedHashMap<String, Object> stateData = new LinkedHashMap<String, Object>();
					stateData.put("state", state);
					states.add(stateCount, stateData);
				}

				LinkedHashMap<String, Object> stateData = states.get(stateCount);

				LinkedHashMap<String, Object> brandInfo = new LinkedHashMap<String, Object>();

				brandInfo.put("stores", rs.getString("STORES"));
				if ( isBrandRollup ) {
					brandInfo.put("brandName", rs.getString("BRAND_NAME"));
					brandInfo.put("upcCount", rs.getString("BRAND_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("BRAND_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
				} else {
					brandInfo.put("mfgName", rs.getString("MFG_NAME"));
					brandInfo.put("upcCount", rs.getString("MFG_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("MFG_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("MFG_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("MFG_FACES_SHARE"));
				}

				brandInfo.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
				brandInfo.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
				
				if ( isBrandRollup ) {
					List<Map<String, Object>> facings = StandardTemplates.getShelfLevelTemplate();

					for (Map<String, Object> map : listForShelfLevel) {

						if (state.equals(map.get("state"))) {

							if (rs.getString("BRAND_NAME").equals(map.get("brand"))) {
								map.remove("brand");

								if ("Top".equals((String) map.get("levelName"))) {
									facings.get(0).put("facingCount", map.get("facingCount"));
								} else if ("Middle".equals((String) map.get("levelName"))) {
									facings.get(1).put("facingCount", map.get("facingCount"));
								} else if ("Bottom".equals((String) map.get("levelName"))) {
									facings.get(2).put("facingCount", map.get("facingCount"));
								} else if ("NA".equals((String) map.get("levelName"))) {
									facings.get(3).put("facingCount", map.get("facingCount"));
								}
							}
						}
					}

					if (facings.get(3).get("facingCount").equals("0")) {
						facings.remove(3);
					}

					brandInfo.put("shelfLevel", facings);
				}

				if (stateData.get(isBrandRollup ? "brands" : "manufacturers") == null) {
					stateData.put(isBrandRollup ? "brands" : "manufacturers", new ArrayList<LinkedHashMap<String, String>>());
				}
				((ArrayList<LinkedHashMap<String, Object>>) stateData.get(isBrandRollup ? "brands" : "manufacturers")).add(brandInfo);
			}

			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProjectBrandSharesAllStates----------------\n");
			return states;

		} catch (Exception e) {
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
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllCities(int projectId, String month, String rollup) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProjectBrandSharesAllCities::projectId={}, month={}, rollup={}",projectId,month,rollup);

		String sqlFileName = "getProjectBrandSharesAllCities_"+rollup+".sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
	    boolean isBrandRollup = "brand".equals(rollup);
	    
		List<LinkedHashMap<String, Object>> stateCities = new ArrayList<LinkedHashMap<String, Object>>();
		List<Map<String, Object>> listForShelfLevel = new ArrayList<Map<String, Object>>();

		if ( isBrandRollup ) {
			listForShelfLevel = getShelfLevelFacingsForCities(projectId, month);
		}

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, month + "%");
			ps.setInt(3, projectId);
			ps.setString(4, month + "%");
			ps.setInt(5, projectId);
			ps.setString(6, month + "%");
			ps.setInt(7, projectId);
			ps.setString(8, month + "%");

			ResultSet rs = ps.executeQuery();
			String prevStateCity = null;
			int stateCityCount = -1;
			while (rs.next()) {
				String state = rs.getString("State");
				String city = rs.getString("City");
				String stateCity = state + city;
				if (prevStateCity == null || !stateCity.equals(prevStateCity)) {
					stateCityCount++;
					prevStateCity = stateCity;
					LinkedHashMap<String, Object> stateCityData = new LinkedHashMap<String, Object>();
					stateCityData.put("city", city);
					stateCityData.put("state", state);

					stateCities.add(stateCityCount, stateCityData);
				}

				LinkedHashMap<String, Object> stateCityData = stateCities.get(stateCityCount);

				LinkedHashMap<String, Object> brandInfo = new LinkedHashMap<String, Object>();
				brandInfo.put("stores", rs.getString("STORES"));
				
				if(isBrandRollup) {
					brandInfo.put("brandName", rs.getString("BRAND_NAME"));
					brandInfo.put("upcCount", rs.getString("BRAND_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("BRAND_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
				} else {
					brandInfo.put("mfgName", rs.getString("MFG_NAME"));
					brandInfo.put("upcCount", rs.getString("MFG_UPC"));
					brandInfo.put("shareOfUpcs", rs.getString("MFG_UPC_SHARE"));
					brandInfo.put("facingCount", rs.getString("MFG_FACING"));
					brandInfo.put("shareOfFacings", rs.getString("MFG_FACES_SHARE"));
				}
				
				brandInfo.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
				brandInfo.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
				
				if (isBrandRollup) {
					List<Map<String, Object>> facings = StandardTemplates.getShelfLevelTemplate();

					for (Map<String, Object> map : listForShelfLevel) {

						if (state.equals(map.get("state"))) {

							if (city.equals(map.get("city"))) {

								if (rs.getString("BRAND_NAME").equals(map.get("brand"))) {
									map.remove("brand");

									if ("Top".equals((String) map.get("levelName"))) {
										facings.get(0).put("facingCount", map.get("facingCount"));
									} else if ("Middle".equals((String) map.get("levelName"))) {
										facings.get(1).put("facingCount", map.get("facingCount"));
									} else if ("Bottom".equals((String) map.get("levelName"))) {
										facings.get(2).put("facingCount", map.get("facingCount"));
									} else if ("NA".equals((String) map.get("levelName"))) {
										facings.get(3).put("facingCount", map.get("facingCount"));

									}
								}
							}
						}
					}

					if (facings.get(3).get("facingCount").equals("0")) {
						facings.remove(3);
					}

					brandInfo.put("shelfLevel", facings);
				}

				if (stateCityData.get(isBrandRollup ? "brands" : "manufacturers") == null) {
					stateCityData.put(isBrandRollup ? "brands" : "manufacturers", new ArrayList<LinkedHashMap<String, String>>());
				}
				((ArrayList<LinkedHashMap<String, Object>>) stateCityData.get(isBrandRollup ? "brands" : "manufacturers")).add(brandInfo);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProjectBrandSharesAllCities----------------\n");
			return stateCities;

		} catch (Exception e) {
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
	public LinkedHashMap<String, Object> getProjectDistributionSummary(String customerCode, String customerProjectId) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProjectDistributionSummary::customerCode={}, customerProjectId={}",customerCode, customerProjectId);

		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		/*
		 * result.putAll(getProjectDistributionHighLevelSummary(customerCode,
		 * customerProjectId));
		 * 
		 * result.put("stores", getDistributionSummaryStoreLevelData(customerCode,
		 * customerProjectId));
		 * 
		 * result.put("states", getDistributionSummaryStateLevelData(customerCode,
		 * customerProjectId));
		 * 
		 * result.put("skus", getDistributionSummaryUPCLevelData(customerCode,
		 * customerProjectId));
		 */

		LOGGER.info("---------------ShelfAnalysisDaoImpl::getProjectDistributionSummary::output={}",result);

		LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProjectDistributionSummary----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getDistributionSummaryStoreLevelData(int projectId, String waveId) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getDistributionSummaryStoreLevelData::projectId={}, waveId={}",projectId,waveId);

		String sqlFileName = "getDistributionSummaryStoreLevelData.sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
	    
		List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, waveId);
			ps.setInt(3, projectId);
			ps.setString(4, waveId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
				result.put("storeId", rs.getString("STOREID"));
				result.put("retailerStoreId", rs.getString("RETAILERSTOREID"));
				result.put("retailer", rs.getString("RETAILER"));
				result.put("street", rs.getString("STORE_STREET"));
				result.put("city", rs.getString("STORE_CITY"));
				result.put("state", rs.getString("STORE_STATE"));
				result.put("stateCode", rs.getString("STORE_STATE_CODE"));
				result.put("taskId", rs.getString("TASK_ID"));
				result.put("agentId", ConverterUtil.ifNullToEmpty(rs.getString("AGENTID")));
				result.put("visitDateId", ConverterUtil.ifNullToEmpty(rs.getString("VISITDATEID")));
				result.put("processedDate", ConverterUtil.ifNullToEmpty(rs.getString("PROCESSEDDATE")));
				result.put("skuFacingCount", ConverterUtil.ifNullToEmpty(rs.getString("UPC_FACING_COUNT")));
				result.put("skuMissingCount", ConverterUtil.ifNullToEmpty(rs.getString("UPC_MISSING_COUNT")));
				result.put("skuFoundCount", ConverterUtil.ifNullToEmpty(rs.getString("UPC_COUNT")));
				result.put("skuDistribution", ConverterUtil.ifNullToEmpty(rs.getString("DISTRIBUTION")));
				result.put("imageUUID", ConverterUtil.ifNullToEmpty(rs.getString("IMAGEUUID")));
				resultList.add(result);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getDistributionSummaryStoreLevelData----------------\n");
			return resultList;

		} catch (Exception e) {
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
	public List<LinkedHashMap<String, String>> getDistributionSummaryStateLevelData(int projectId, String waveId) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getDistributionSummaryStateLevelData::projectId={}, waveId={}",projectId,waveId);
		
		String sqlFileName = "getDistributionSummaryStateLevelData.sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");

		List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, waveId);
			ps.setInt(3, projectId);
			ps.setString(4, waveId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
				result.put("stateCode", rs.getString("STATECODE"));
				result.put("state", rs.getString("STATE"));
				result.put("skusInAllStore", ConverterUtil.ifNullToEmpty(rs.getString("UPC_IN_ALL_STORES")));
				result.put("skusNotInAllStore", ConverterUtil.ifNullToEmpty(rs.getString("UPC_NOT_IN_ALL_STORES")));
				result.put("skusTotalFacings", ConverterUtil.ifNullToEmpty(rs.getString("UPC_FACING_COUNT")));
				result.put("storeWithAllSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORES_ALL_UPC")));
				result.put("storeWithNotAllSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORE_NOT_ALL_UPC")));
				result.put("storeWithNoSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORES_NO_UPC")));
				result.put("skuDistribution", ConverterUtil.ifNullToEmpty(rs.getString("UPC_DISTRIBUTION")));

				resultList.add(result);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getDistributionSummaryStateLevelData----------------\n");
			return resultList;

		} catch (Exception e) {
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
	public List<LinkedHashMap<String, String>> getDistributionSummaryUPCLevelData(int projectId, String waveId) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getDistributionSummaryUPCLevelData::projectId={}, waveId={}",projectId,waveId);
		
		String sqlFileName = "getDistributionSummaryUPCLevelData.sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");

		List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
		DecimalFormat df = new DecimalFormat(".###");
		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, waveId);
			ps.setInt(3, projectId);
			ps.setString(4, waveId);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
				result.put("upc", rs.getString("UPC"));
				result.put("brandName", rs.getString("BRAND_NAME"));
				result.put("productShortName", rs.getString("PRODUCT_SHORT_NAME"));
				String storeFoundCount = ConverterUtil.ifNullToEmpty(rs.getString("UPC_STORE_COUNT"));
				result.put("storeCount", storeFoundCount);
				String storeMissingCount = ConverterUtil.ifNullToEmpty(rs.getString("MISSING_STORE_COUNT"));
				result.put("missingStoreCount", storeMissingCount);
				result.put("facingCount", ConverterUtil.ifNullToEmpty(rs.getString("UPC_FACING_COUNT")));
				result.put("averageFacingCount", ConverterUtil.ifNullToEmpty(rs.getString("UPC_AVG_FACING_PER_STORE")));

				float distribution = Float.parseFloat(storeFoundCount)
						/ (Float.parseFloat(storeFoundCount) + Float.parseFloat(storeMissingCount));
				result.put("storeDistribution", df.format(distribution));

				resultList.add(result);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getDistributionSummaryUPCLevelData----------------\n");
			return resultList;

		} catch (Exception e) {
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
	public LinkedHashMap<String, String> getProjectDistributionHighLevelSummary(int projectId, String waveId) {
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProjectDistributionSummary::projectId={}, waveId={}", projectId, waveId);
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		String sqlFileName = "getProjectDistributionHighLevelSummary.sql";
	    String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");

		Connection conn = null;

		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setString(2, waveId);
			ps.setInt(3, projectId);
			ps.setString(4, waveId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result.put("skusInAllStore", ConverterUtil.ifNullToEmpty(rs.getString("UPC_IN_ALL_STORES")));
				result.put("skusNotInAllStore", ConverterUtil.ifNullToEmpty(rs.getString("UPC_NOT_IN_ALL_STORES")));
				result.put("skusInNoStore", ConverterUtil.ifNullToEmpty(rs.getString("UPC_IN_NO_STORES")));
				result.put("skusTotalFacing", ConverterUtil.ifNullToEmpty(rs.getString("UPC_FACING_COUNT")));
				result.put("storeWithAllSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORES_ALL_UPC")));
				result.put("storeWithNotAllSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORES_NOT_ALL_UPC")));
				result.put("storeWithNoSkus", ConverterUtil.ifNullToEmpty(rs.getString("STORES_NO_UPC")));
				result.put("distribution", ConverterUtil.ifNullToEmpty(rs.getString("UPC_DISTRIBUTION")));
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProjectDistributionHighLevelSummary----------------\n");
			return result;

		} catch (Exception e) {
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

	public List<Map<String, Object>> getShelfLevelFacingsForStates(int projectId, String month) {

		String SQL_FOR_STATES = "select C.State as STATE, C.BRAND_NAME as BRAND_NAME, C.shelfLevel as SHELF_LEVEL , sum(C.SUM_FACINGS) as FACINGS from\n"
				+ "(select  sum(A.SUM_FACINGS) as SUM_FACINGS, A.storeId , A.shelfLevel , A.taskId, A.State, B.UPC, B.BRAND_NAME from\n"
				+ "(select P.upc, sum(P.SUM_FACINGS)as SUM_FACINGS , P.storeId, P.shelfLevel, P.taskId, S.State from\n"
				+ "(select distinct(upc) as upc, sum(facing) AS SUM_FACINGS, storeId, shelfLevel, taskId from ProjectStoreData where projectId =? \n"
				+ "and taskId IN (SELECT taskId FROM ProjectStoreResult WHERE projectId =? AND visitDateId like ?) \n"
				+ "group by storeId, upc, shelfLevel ) P INNER JOIN (select StoreID, State, City from StoreMaster) S where P.storeId = S.StoreID group by P.storeId, P.upc, P.shelfLevel) A\n"
				+ "INNER JOIN (select UPC, BRAND_NAME from ProductMaster where UPC <> '99999999999') B\n"
				+ "where A.upc = B.UPC group by A.State, B.BRAND_NAME, B.UPC, A.storeId, A.shelfLevel) C group by C.State, C.BRAND_NAME,C.shelfLevel order by C.State,C.BRAND_NAME,(\n"
				+ "CASE C.shelfLevel WHEN 'Top' THEN 1 WHEN 'Middle' THEN 2 WHEN 'Bottom' THEN 3\n"
				+ "WHEN 'NA' THEN 4 END )ASC";

		List<Map<String, Object>> shelfFacings = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		int i = 0;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(SQL_FOR_STATES);

			ps.setInt(1, projectId);
			ps.setInt(2, projectId);
			ps.setString(3, month + "%");

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, Object> level = new HashMap<String, Object>();
				level.put("state", rs.getString("STATE"));
				level.put("brand", rs.getString("BRAND_NAME"));
				level.put("levelName", rs.getString("SHELF_LEVEL"));
				level.put("facingCount", rs.getString("FACINGS"));

				shelfFacings.add(level);
			}
			rs.close();
			ps.close();
			return shelfFacings;

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

	public List<Map<String, Object>> getShelfLevelFacingsForCities(int projectId, String month) {

		String SQL_FOR_CITIES = "select C.State as STATE, C.City as CITY, C.BRAND_NAME as BRAND_NAME, C.shelfLevel as SHELF_LEVEL , sum(C.SUM_FACINGS) as FACINGS from\n"
				+ "(select  sum(A.SUM_FACINGS) as SUM_FACINGS, A.storeId , A.shelfLevel , A.taskId, A.State, A.City, B.UPC, B.BRAND_NAME from\n"
				+ "(select P.upc, sum(P.SUM_FACINGS)as SUM_FACINGS , P.storeId, P.shelfLevel, P.taskId, S.State, S.City from\n"
				+ "(select distinct(upc) as upc, sum(facing) AS SUM_FACINGS, storeId, shelfLevel, taskId from ProjectStoreData where projectId =? \n"
				+ "and taskId IN (SELECT taskId FROM ProjectStoreResult WHERE projectId =? AND visitDateId like ?) \n"
				+ "group by storeId, upc, shelfLevel ) P INNER JOIN (select StoreID, State, City from StoreMaster) S where P.storeId = S.StoreID group by P.storeId, P.upc, P.shelfLevel) A\n"
				+ "INNER JOIN (select UPC, BRAND_NAME from ProductMaster where UPC <> '99999999999') B\n"
				+ "where A.upc = B.UPC group by A.State, A.City, B.BRAND_NAME, B.UPC, A.storeId, A.shelfLevel) C group by C.State, C.City, C.BRAND_NAME,C.shelfLevel order by C.State,C.City,C.BRAND_NAME,(\n"
				+ "CASE C.shelfLevel WHEN 'Top' THEN 1 WHEN 'Middle' THEN 2 WHEN 'Bottom' THEN 3\n"
				+ "WHEN 'NA' THEN 4 END )ASC";

		List<Map<String, Object>> shelfFacings = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		int i = 0;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(SQL_FOR_CITIES);

			ps.setInt(1, projectId);
			ps.setInt(2, projectId);
			ps.setString(3, month + "%");

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, Object> level = new HashMap<String, Object>();
				level.put("state", rs.getString("STATE"));
				level.put("city", rs.getString("CITY"));
				level.put("brand", rs.getString("BRAND_NAME"));
				level.put("levelName", rs.getString("SHELF_LEVEL"));
				level.put("facingCount", rs.getString("FACINGS"));

				shelfFacings.add(level);
			}
			rs.close();
			ps.close();
			return shelfFacings;

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

	public List<Map<String, Object>> getShelfLevelFacingsForStores(int projectId,
			String month) {

		String SQL_FOR_STORES = "select C.storeId, C.BRAND_NAME as brandName, SUM(C.SUM_FACINGS) as facings,  C.shelfLevel from\n"
				+ "(select A.storeId ,B.UPC, B.BRAND_NAME, sum(A.SUM_FACINGS) as SUM_FACINGS,  A.shelfLevel , A.taskId  from\n"
				+ "(select distinct(upc) as upc, sum(facing) AS SUM_FACINGS, storeId, shelfLevel, taskId from ProjectStoreData where projectId =? \n"
				+ "and taskId IN (SELECT taskId FROM ProjectStoreResult WHERE projectId=? AND visitDateId like ?) group by storeId, upc, shelfLevel) A \n"
				+ "INNER JOIN (select UPC, BRAND_NAME from ProductMaster where UPC <> '99999999999') B \n"
				+ "ON A.upc = B.UPC group by  A.storeId, B.BRAND_NAME, B.UPC,  A.shelfLevel)C group by C.storeId, C.BRAND_NAME,C.shelfLevel";

		List<Map<String, Object>> shelfFacings = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		int i = 0;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(SQL_FOR_STORES);

			ps.setInt(1, projectId);
			ps.setInt(2, projectId);
			ps.setString(3, month + "%");

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, Object> level = new HashMap<String, Object>();
				level.put("storeId", rs.getString("storeId"));
				level.put("brand", rs.getString("brandName"));
				level.put("levelName", rs.getString("shelfLevel"));
				level.put("facingCount", rs.getString("facings"));

				shelfFacings.add(level);
			}
			rs.close();
			ps.close();
			return shelfFacings;

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
	public List<LinkedHashMap<String, String>> getProductDetections(String imageUUID){
		LOGGER.info("---------------ShelfAnalysisDaoImpl Starts getProductDetections::ImageUUID={}",imageUUID );

		String sql = "SELECT i.imageUUID, i.id, i.upc, i.leftTopX, i.leftTopY, i.width, i.height " + 
				" FROM ImageAnalysisNew i, ProjectUpc pu WHERE i.imageUUID = ? AND i.upc = pu.UPC AND i.projectId = pu.projectId AND pu.skuTypeId = 1";
		Connection conn = null;
		List<LinkedHashMap<String,String>> imageAnalysisList=new ArrayList<LinkedHashMap<String,String>>();
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, imageUUID);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
				//map.put("imageUUID", rs.getString("imageUUID"));
				map.put("id", rs.getString("id"));
				map.put("upc", rs.getString("upc"));
				map.put("leftTopX", rs.getString("leftTopX"));
				map.put("leftTopY", rs.getString("leftTopY"));
				map.put("width", rs.getString("width"));
				map.put("height", rs.getString("height"));
				imageAnalysisList.add(map);
			}
			rs.close();
			ps.close();
			LOGGER.info("---------------ShelfAnalysisDaoImpl Ends getProductDetections----------------\n");

			return imageAnalysisList;
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

package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ScoreDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.StoreVisitScore;
import com.snap2buy.themobilebackend.model.StoreVisitScoreComponent;
import com.snap2buy.themobilebackend.model.StoreVisitScoreComponentSummary;
import com.snap2buy.themobilebackend.model.StoreVisitScoreSummary;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.snap2buy.themobilebackend.util.ConverterUtil.ifNullToEmpty;

/**
 * Created by Anoop on 09/26/17.
 */
@Component(value = BeanMapper.BEAN_SCORE_DAO)
@Scope("prototype")
public class ScoreDaoImpl implements ScoreDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataSource dataSource;
    
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("###");
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	@Override
	public List<LinkedHashMap<String, String>> getProjectAllStoreScores(int projectId, String scoreId, String level, String value) {
		LOGGER.info("---------------ScoreDaoImpl Starts getProjectAllStoreScores::projectId={}, scoreId={}, level={}, value={}",projectId,scoreId,level,value);
		
        String sql = "SELECT result.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip, store.Latitude, store.Longitude, " + 
        		"	result.agentId, result.taskId,DATE_FORMAT(result.processedDate,'%m/%d/%Y') as processedDate, result.visitDateId, result.imageUUID, " + 
        		"	pss.scoreId, ROUND(pss.score,0) as score, pss.scoreGroupId, pss.scoreGroupName, pss.scoreGroupColor " + 
        		" FROM ProjectStoreResult result   " + 
        		" INNER JOIN ( SELECT a.projectId, a.storeId, MAX(concat(a.visitDateId,a.taskId)) as maxVisitTask FROM ProjectStoreResult a WHERE a.projectId = ? AND a.visitDateId LIKE ? AND COALESCE(a.waveId,'') LIKE ? GROUP BY a.projectId, a.storeId ) maxresult " + 
        		"	ON result.projectId = maxresult.projectId AND result.storeId = maxresult.storeId AND concat(result.visitDateId,result.taskId) <=> maxresult.maxVisitTask " + 
        		" LEFT JOIN StoreMaster store ON result.storeId = store.storeId " + 
        		" LEFT JOIN (" + 
        		"    SELECT a.projectId,a.storeId,a.taskId,a.scoreId,a.score,a.scoreGroupId,b.scoreGroupName,b.scoreGroupColor FROM ProjectStoreScore a" +
        		"    LEFT JOIN ProjectScoreGroupDefinition b " +
        		"    ON a.projectId = b.projectId AND a.scoreId = b.scoreId AND a.scoreGroupId = b.scoreGroupId" +
        		"    WHERE a.projectId=? AND a.scoreId = ?" + 
        		" ) pss ON result.projectId = pss.projectId AND result.storeId = pss.storeId AND result.taskId = pss.taskId " + 
        		" WHERE result.projectId=? AND result.status='1' AND result.visitDateId LIKE ? AND COALESCE(result.waveId,'') LIKE ? " + 
        		" ORDER BY CAST(pss.score AS UNSIGNED) desc";
        
        Connection conn = null;
        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
        String monthFilter = "%";
        String waveFilter = "%";
        
        if ( StringUtils.isNotBlank(level) && level.equals("wave") ) {
        	waveFilter = value;
        }
        
        if ( StringUtils.isNotBlank(level) && level.equals("month") ) {
        	// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
    		// this logic won't work by 2100 :)
    		String[] parts = value.split("/");
    		value = "20" + parts[1] + parts[0];
        	monthFilter = value+monthFilter; 
        }
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, monthFilter);
            ps.setString(3, waveFilter);
            ps.setInt(4, projectId);
            ps.setInt(5, Integer.parseInt(scoreId));
            ps.setInt(6, projectId);
            ps.setString(7, monthFilter);
            ps.setString(8, waveFilter);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", ifNullToEmpty(rs.getString("storeId")));
                map.put("retailerStoreId", ifNullToEmpty(rs.getString("retailerStoreId")));
                map.put("retailerChainCode", ifNullToEmpty(rs.getString("retailerChainCode")));
                map.put("retailer", ifNullToEmpty(rs.getString("retailer")));
                map.put("street", ifNullToEmpty(rs.getString("street")));
                map.put("city", ifNullToEmpty(rs.getString("city")));
                map.put("stateCode", ifNullToEmpty(rs.getString("stateCode")));
                map.put("state", ifNullToEmpty(rs.getString("state")));
                map.put("zip", ifNullToEmpty(rs.getString("zip")));
                map.put("lat", ifNullToEmpty(rs.getString("Latitude")));
                map.put("long", ifNullToEmpty(rs.getString("Longitude")));
                map.put("agentId", ifNullToEmpty(rs.getString("agentId")));
                map.put("taskId", ifNullToEmpty(rs.getString("taskId")));

                String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
                if ( !visitDate.isEmpty() ) {
                	try {
						visitDate = outSdf.format(inSdf.parse(visitDate));
					} catch (ParseException e) {
                        LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
					}
                }
                map.put("visitDate", visitDate);
                map.put("processedDate", ifNullToEmpty(rs.getString("processedDate")));
                map.put("imageUUID", ifNullToEmpty(rs.getString("imageUUID")));
                map.put("scoreId",scoreId);
                map.put("score",""+rs.getInt("score"));
                map.put("scoreGroupId",rs.getString("scoreGroupId"));
                map.put("scoreGroupName",rs.getString("scoreGroupName"));
                map.put("scoreGroupColor",rs.getString("scoreGroupColor"));

                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ScoreDaoImpl Ends getProjectAllStoreScores----------------\n");
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

	private List<StoreVisitScoreComponent> getComponentScores(int projectId, String storeId,
			String taskId, String agentId, String visitDate, String scoreId) {
		LOGGER.info("---------------StoreVisitScoreDaoImpl Starts getComponentScores----------------\n");
        String sql = "select componentId, componentName, componentScore, componentScoreDesc, componentScoreComment from ProjectStoreScoreComponents"
        		+ " where projectId=? and storeId=? and taskId=? and agentId=? and visitDate=? and scoreId=?";
        
        Connection conn = null;
        List<StoreVisitScoreComponent> result=new ArrayList<StoreVisitScoreComponent>();
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ps.setString(4, agentId);
            ps.setString(5, visitDate);
            ps.setString(6, scoreId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	StoreVisitScoreComponent scoreComponent = new StoreVisitScoreComponent();
            	scoreComponent.setComponentId(rs.getString("componentId"));
            	scoreComponent.setComponentScore(rs.getString("componentScore"));
            	scoreComponent.setComponentScoreComment(rs.getString("componentScoreComment"));
            	scoreComponent.setComponentScoreDesc(rs.getString("componentScoreDesc"));
            	scoreComponent.setComponentScoreName(rs.getString("componentName"));
                result.add(scoreComponent);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------StoreVisitScoreDaoImpl Ends getComponentScores----------------\n");
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
	public List<StoreVisitScore> getProjectStoreScores(int projectId, String storeId) {
		
		LOGGER.info("---------------StoreVisitScoreDaoImpl Starts getProjectStoreScores----------------\n");

        String sql = "select scores.storeId, store.retailerStoreId, store.retailer, store.street, store.city, store.state, scores.agentId, scores.taskId, DATE_FORMAT(scores.processedDate,'%m/%d/%Y') as processedDate, scores.visitDate, scores.scoreId, scores.scoreName, scores.score, scores.scoreDesc, scores.scoreComment, scores.imageUUID from ProjectStoreScores scores" + 
        		" left join StoreMaster store on scores.storeId = store.storeId" + 
        		" where scores.projectId=? and scores.storeId=?";
        
        Connection conn = null;
        List<StoreVisitScore> result=new ArrayList<StoreVisitScore>();
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	StoreVisitScore score = new StoreVisitScore();
                score.setStoreId(ifNullToEmpty(storeId));
                score.setRetailerStoreId(ifNullToEmpty(rs.getString("retailerStoreId")));
                score.setRetailer(ifNullToEmpty(rs.getString("retailer")));
                score.setStreet(ifNullToEmpty(rs.getString("street")));
                score.setCity(ifNullToEmpty(rs.getString("city")));
                score.setState(ifNullToEmpty(rs.getString("state")));
                String agentId = rs.getString("agentId");
                score.setAgentId(ifNullToEmpty(agentId));
                String taskId = rs.getString("taskId");
                score.setTaskId(ifNullToEmpty(taskId));
                String visitDate = ifNullToEmpty(rs.getString("visitDate"));
                String formattedVisitDate = "";
                if ( !visitDate.isEmpty() ) {
                	try {
                		formattedVisitDate = outSdf.format(inSdf.parse(visitDate));
					} catch (ParseException e) {
                        LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
					}
                }
                score.setVisitDate(formattedVisitDate);
                score.setProcessedDate(ifNullToEmpty(rs.getString("processedDate")));
                String scoreId = rs.getString("scoreId");
                score.setScoreId(ifNullToEmpty(rs.getString("scoreId")));
                score.setScoreName(ifNullToEmpty(rs.getString("scoreName")));
                score.setScore(ifNullToEmpty(rs.getString("score")));
                score.setScoreDesc(ifNullToEmpty(rs.getString("scoreDesc")));
                score.setScoreComment(ifNullToEmpty(rs.getString("scoreComment")));
                score.setImageUUID(rs.getString("imageUUID"));
                
                List<StoreVisitScoreComponent> componentScores = getComponentScores(projectId, storeId, taskId, agentId, visitDate, scoreId );
                score.setComponentScores(componentScores);
                result.add(score);
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
        LOGGER.info("---------------StoreVisitScoreDaoImpl Ends getProjectAllStoreScores----------------\n");

		return result;
	}

	@Override
	public List<StoreVisitScoreSummary> getProjectAllStoresScoreSummary(int projectId) {
		LOGGER.info("---------------StoreVisitScoreDaoImpl Starts getProjectAllStoresScoreSummary----------------\n");
        String sql = "select scoreId,scoreName,scoreDesc,count(scoreDesc) as count from ProjectStoreScores"
        		+ " where projectId=?"
        		+ " group by scoreId, scoreDesc order by scoreId,scoreName";
        
        String totalStoresSql = "SELECT storeCount as value FROM Project where id =\"" + projectId + "\"";
        String notProcessedStoresSql = "SELECT COUNT(*) as value FROM ProjectStoreResult WHERE projectId =\"" + projectId + "\" AND resultCode in ( \"0\",\"4\") AND status = \"1\"";
        
        Connection conn = null;
        Map<String,StoreVisitScoreSummary> result=new HashMap<String,StoreVisitScoreSummary>();
        
        try {
            conn = dataSource.getConnection();
            
            String totalStores = executeOneQueryReturnOneValue(totalStoresSql, conn);
            String notProcessedStores = executeOneQueryReturnOneValue(notProcessedStoresSql, conn);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		String scoreId = rs.getString("scoreId");
            		String scoreName = rs.getString("scoreName");
            		String key = scoreId+scoreName;
            		if ( result.get(key) == null ) {
            			StoreVisitScoreSummary summary = new StoreVisitScoreSummary();
            			summary.setScoreId(scoreId);
                		summary.setScoreName(scoreName);
                		summary.getStoreVisitCounts().put("Total", totalStores);
                		summary.getStoreVisitCounts().put("NotProcessed", notProcessedStores);
                		summary.setComponentNames(getStoreVisitScoreComponentSummary(projectId, scoreId));
            			result.put(key, summary);
            		}
            		StoreVisitScoreSummary summary = result.get(key);
            		summary.getStoreVisitCounts().put(rs.getString("scoreDesc"), rs.getString("count"));
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------StoreVisitScoreDaoImpl Ends getProjectAllStoresScoreSummary----------------\n");
            return new ArrayList<StoreVisitScoreSummary>(result.values());
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

	private String executeOneQueryReturnOneValue(String sql, Connection conn) throws SQLException {
		String value = "";
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
		     value = rs.getString("value");
		}
		rs.close();
		ps.close();
		return value;
	}

	private List<StoreVisitScoreComponentSummary> getStoreVisitScoreComponentSummary(int projectId, String scoreId) {
		
		LOGGER.info("---------------StoreVisitScoreDaoImpl Starts getStoreVisitScoreComponentSummary----------------\n");
		
		Map<String,StoreVisitScoreComponentSummary> scoreComponentSummaryMap = new HashMap<String,StoreVisitScoreComponentSummary>();
		
		String sql = "select distinct componentScoreDesc, componentName from ProjectStoreScoreComponents"
				+ " where projectId=? and scoreId=?";
		
		Connection conn = null;
        
        try {
            conn = dataSource.getConnection();
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, scoreId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		String componentScoreDesc = rs.getString("componentScoreDesc");
            		String componentName = rs.getString("componentName");
            		if ( scoreComponentSummaryMap.get(componentName) == null ) {
            			StoreVisitScoreComponentSummary summary = new StoreVisitScoreComponentSummary();
            			summary.setName(componentName);
            			summary.setComponentScoreDesc(new HashMap<String,String>());
            			scoreComponentSummaryMap.put(componentName,summary);
            		}
            		StoreVisitScoreComponentSummary summary = scoreComponentSummaryMap.get(componentName);
            		int count = summary.getComponentScoreDesc().keySet().size()+1;
            		summary.getComponentScoreDesc().put(""+count, componentScoreDesc);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------StoreVisitScoreDaoImpl Ends getStoreVisitScoreComponentSummary----------------\n");
            return new ArrayList<StoreVisitScoreComponentSummary>(scoreComponentSummaryMap.values());
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
    public Map<String, Object> getProjectScoreSummary(int projectId, String level, String value) {
        LOGGER.info("---------------ScoreDaoImpl Starts getProjectScoreSummary----------------\n");
        
        String storesSql = "SELECT count(*) as storeCount FROM ProjectStoreResult result " + 
        		"    INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? AND b.visitDateId LIKE ? AND COALESCE(b.waveId,'') LIKE ? GROUP BY b.projectId, b.storeId) c " + 
        		"    ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask " + 
        		"    WHERE result.projectId = ? AND result.visitDateId LIKE ? AND COALESCE(result.waveId,'') LIKE ? AND result.status = ? ";

        String projectWavesSql = "SELECT waveId, waveName FROM ProjectWaveConfig WHERE projectId=? ORDER BY waveId";
        String projectMonthsSql = "SELECT DISTINCT(SUBSTR(visitDateId,1,6)) AS month FROM ProjectStoreResult WHERE projectId=? AND status = '1' ORDER BY month";
        
	    String scoreSummarySql = ConverterUtil.getResourceFromClasspath("scoreSummary.sql", "/queries/score/");
	    String scoreGroupSummarySql = ConverterUtil.getResourceFromClasspath("scoreGroupSummary.sql", "/queries/score/");
	    String componentScoreSummarySql = ConverterUtil.getResourceFromClasspath("componentScoreSummary.sql", "/queries/score/");
	    String scoreTrendByWaveSql = ConverterUtil.getResourceFromClasspath("scoreTrendByWave.sql", "/queries/score/");
	    String scoreTrendByMonthSql = ConverterUtil.getResourceFromClasspath("scoreTrendByMonth.sql", "/queries/score/");
	    String keyMetricsSql = ConverterUtil.getResourceFromClasspath("keyMetrics.sql", "/queries/score/");

        
        Map<String,Object> resultMap = new HashMap<String,Object>();
        String storesProcessed = "0";
        String storesNotprocessed = "0";
        
        String monthFilter = "%";
        String waveFilter = "%";
        
        if ( level.equals("wave") ) {
        	waveFilter = value;
        }
        if ( level.equals("month") ) {
        	// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
    		// this logic won't work by 2100 :)
    		String[] parts = value.split("/");
    		value = "20" + parts[1] + parts[0];
        	monthFilter = value+monthFilter; 
        }
        
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement storesPs = conn.prepareStatement(storesSql);
            
            storesPs.setInt(1, projectId);
            storesPs.setString(2, monthFilter);
            storesPs.setString(3, waveFilter);
            storesPs.setInt(4, projectId);
            storesPs.setString(5, monthFilter);
            storesPs.setString(6, waveFilter);
            storesPs.setString(7, "1");
            
            ResultSet storesProcessedRs = storesPs.executeQuery();
            if (storesProcessedRs.next()) {
            	storesProcessed = storesProcessedRs.getString("storeCount");
            	resultMap.put("processedStores", storesProcessed);
            }
            storesProcessedRs.close();

            storesPs.setString(7, "0");
            ResultSet storesNotProcessedRs = storesPs.executeQuery();
            if (storesNotProcessedRs.next()) {
            	storesNotprocessed = storesNotProcessedRs.getString("storeCount");
            	resultMap.put("notProcessedStores", storesNotprocessed);
            }
            storesNotProcessedRs.close();
            storesPs.close();
            
            resultMap.put("totalStores", (Integer.parseInt(storesProcessed)+Integer.parseInt(storesNotprocessed))+"");
            
            resultMap.put("scores", new ArrayList<Map<String,Object>>());
            
            PreparedStatement scoreSummaryPs = conn.prepareStatement(scoreSummarySql);
            scoreSummaryPs.setInt(1, projectId);
            scoreSummaryPs.setInt(2, projectId);
            scoreSummaryPs.setString(3, monthFilter);
            scoreSummaryPs.setString(4, waveFilter);
            scoreSummaryPs.setInt(5, projectId);
            scoreSummaryPs.setString(6, monthFilter);
            scoreSummaryPs.setString(7, waveFilter);
            scoreSummaryPs.setInt(8, projectId);
            ResultSet scoreSummaryRs = scoreSummaryPs.executeQuery();
            while (scoreSummaryRs.next()) {
                Map<String,Object> oneScoreMap = new HashMap<String,Object>();
                oneScoreMap.put("scoreId", ""+scoreSummaryRs.getInt("scoreId"));
                oneScoreMap.put("scoreName", scoreSummaryRs.getString("scoreName"));
                oneScoreMap.put("scoreUnit", scoreSummaryRs.getString("unit"));
                oneScoreMap.put("avgScore",  scoreSummaryRs.getInt("avgScore"));
                oneScoreMap.put("totalScore", scoreSummaryRs.getInt("totalScore"));
                ((List<Map<String,Object>>)resultMap.get("scores")).add(oneScoreMap);
            }
            scoreSummaryRs.close();
            scoreSummaryPs.close();
            
            PreparedStatement scoreGroupSummaryPs = conn.prepareStatement(scoreGroupSummarySql);
            scoreGroupSummaryPs.setInt(1, projectId);
            scoreGroupSummaryPs.setInt(2, projectId);
            scoreGroupSummaryPs.setString(3, monthFilter);
            scoreGroupSummaryPs.setString(4, waveFilter);
            scoreGroupSummaryPs.setInt(5, projectId);
            scoreGroupSummaryPs.setString(6, monthFilter);
            scoreGroupSummaryPs.setString(7, waveFilter);
            scoreGroupSummaryPs.setInt(8, projectId);
            ResultSet scoreGroupSummaryRs = scoreGroupSummaryPs.executeQuery();
            Map<String,List<Map<String,String>>> scoreGroupsByScore = new HashMap<String,List<Map<String,String>>>();
            while (scoreGroupSummaryRs.next()) {
                Map<String,String> oneScoreGroupMap = new HashMap<String,String>();
                oneScoreGroupMap.put("groupId", ""+scoreGroupSummaryRs.getInt("scoreGroupId"));
                oneScoreGroupMap.put("groupName", scoreGroupSummaryRs.getString("scoreGroupName"));
                oneScoreGroupMap.put("groupColor", scoreGroupSummaryRs.getString("scoreGroupColor"));
                oneScoreGroupMap.put("storeCount",  scoreGroupSummaryRs.getString("storeCount"));
                
                String scoreId = scoreGroupSummaryRs.getString("scoreId");
                if ( scoreGroupsByScore.get(scoreId) == null ) {
                	scoreGroupsByScore.put(scoreId, new ArrayList<Map<String,String>>());
                }
                scoreGroupsByScore.get(scoreId).add(oneScoreGroupMap);
            }
            scoreGroupSummaryRs.close();
            scoreGroupSummaryPs.close();
            
            PreparedStatement componentScoreSummaryPs = conn.prepareStatement(componentScoreSummarySql);
            componentScoreSummaryPs.setInt(1, projectId);
            componentScoreSummaryPs.setInt(2, projectId);
            componentScoreSummaryPs.setString(3, monthFilter);
            componentScoreSummaryPs.setString(4, waveFilter);
            componentScoreSummaryPs.setInt(5, projectId);
            componentScoreSummaryPs.setString(6, monthFilter);
            componentScoreSummaryPs.setString(7, waveFilter);
            componentScoreSummaryPs.setInt(8, projectId);
            ResultSet componentScoreSummaryRs = componentScoreSummaryPs.executeQuery();
            Map<String,List<Map<String,String>>> componentScoreSummaryByScore = new HashMap<String,List<Map<String,String>>>();
            while (componentScoreSummaryRs.next()) {
                Map<String,String> oneComponentScoreMap = new HashMap<String,String>();
                oneComponentScoreMap.put("componentScoreId", ""+componentScoreSummaryRs.getInt("componentScoreId"));
                oneComponentScoreMap.put("componentScoreName", componentScoreSummaryRs.getString("componentScoreName"));
                oneComponentScoreMap.put("maxComponentScore",  componentScoreSummaryRs.getString("componentMaxScore"));
                oneComponentScoreMap.put("avgComponentScore", ""+componentScoreSummaryRs.getInt("avgComponentScore"));
                oneComponentScoreMap.put("totalComponentScore", ""+componentScoreSummaryRs.getInt("totalComponentScore"));
                
                String scoreId = componentScoreSummaryRs.getString("scoreId");
                if ( componentScoreSummaryByScore.get(scoreId) == null ) {
                	componentScoreSummaryByScore.put(scoreId, new ArrayList<Map<String,String>>());
                }
                componentScoreSummaryByScore.get(scoreId).add(oneComponentScoreMap);
            }
            componentScoreSummaryRs.close();
            componentScoreSummaryPs.close();
            
            PreparedStatement keyMetricsPs = conn.prepareStatement(keyMetricsSql);
            keyMetricsPs.setInt(1, projectId);
            keyMetricsPs.setInt(2, projectId);
            keyMetricsPs.setString(3, monthFilter);
            keyMetricsPs.setString(4, waveFilter);
            keyMetricsPs.setInt(5, projectId);
            keyMetricsPs.setString(6, monthFilter);
            keyMetricsPs.setString(7, waveFilter);
            keyMetricsPs.setInt(8, projectId);
            keyMetricsPs.setInt(9, projectId);
            keyMetricsPs.setInt(10, projectId);
            keyMetricsPs.setString(11, monthFilter);
            keyMetricsPs.setString(12, waveFilter);
            keyMetricsPs.setInt(13, projectId);
            keyMetricsPs.setString(14, monthFilter);
            keyMetricsPs.setString(15, waveFilter);
            keyMetricsPs.setInt(16, projectId);
            
            Map<String,List<Map<String,String>>> keyMetricsByScore = new HashMap<String,List<Map<String,String>>>();
            List<Map<String,String>> keyMetricsList = new ArrayList<Map<String,String>>();

            ResultSet keyMetricsRs = keyMetricsPs.executeQuery();
            while(keyMetricsRs.next()) {
            	Map<String,String> keyMetric = new HashMap<String,String>();
            	
            	keyMetric.put("scoreId", keyMetricsRs.getString("scoreId"));
            	
            	keyMetric.put("metric", keyMetricsRs.getString("criteriaDesc"));
            	
            	String storeCount = keyMetricsRs.getString("storeCount");
            	keyMetric.put("value",StringUtils.isBlank(storeCount) ? "0" : storeCount);
            	
            	if ( keyMetricsByScore.get(keyMetric.get("scoreId")) == null ) {
            		keyMetricsByScore.put(keyMetric.get("scoreId"), new ArrayList<Map<String,String>>());
            	}
            	keyMetricsByScore.get(keyMetric.get("scoreId")).add(keyMetric);
            	keyMetricsList.add(keyMetric);
            }
            
            keyMetricsRs.close();
            keyMetricsPs.close();
            
            //TODO Remove
            resultMap.put("keyMetrics", keyMetricsList);
            
            //Add scoreGroups, component score summary and keyMetrics to corresponding scores
            for(Map<String, Object> oneScore : (List<Map<String,Object>>)resultMap.get("scores")) {
            	String scoreId = (String) oneScore.get("scoreId");
            	
            	if ( scoreGroupsByScore.get(scoreId) != null ) {
            		oneScore.put("scoreGroups", scoreGroupsByScore.get(scoreId));
            	} else {
            		oneScore.put("scoreGroups", new ArrayList<Map<String,String>>());
            	}
            	
            	if ( componentScoreSummaryByScore.get(scoreId) != null ) {
            		oneScore.put("componentScores", componentScoreSummaryByScore.get(scoreId));
            	} else {
            		oneScore.put("componentScores", new ArrayList<Map<String,String>>());
            	}
            	
            	if ( keyMetricsByScore.get(scoreId) != null ) {
            		oneScore.put("keyMetrics", keyMetricsByScore.get(scoreId));
            	} else {
            		oneScore.put("keyMetrics", new ArrayList<Map<String,String>>());
            	}
            }
            
            if ( level.equals("-9") ) { //high level summary call, response should include wave/month level data for trend line plotting.
            	//all months
                PreparedStatement allMonthsPs = conn.prepareStatement(projectMonthsSql);
                allMonthsPs.setInt(1, projectId);
            	ResultSet allMonthsRs = allMonthsPs.executeQuery();
            	List<String> allMonths  = new ArrayList<String>();
            	while(allMonthsRs.next()) {
            		String oneMonth = allMonthsRs.getString("month");
            		String yearPart = oneMonth.substring(2, 4);
            		String monthPart = oneMonth.substring(4);
            		allMonths.add(monthPart+"/"+yearPart);
            		
            	}
            	allMonthsRs.close();
            	allMonthsPs.close();
            	resultMap.put("months",allMonths);
            	//all waves
            	PreparedStatement allWavesPs = conn.prepareStatement(projectWavesSql);
            	allWavesPs.setInt(1, projectId);
            	ResultSet allWavesRs = allWavesPs.executeQuery();
            	List<Map<String,String>> allWaves  = new ArrayList<Map<String,String>>();
            	while(allWavesRs.next()) {
            		Map<String,String> oneWave = new HashMap<String,String>();
            		oneWave.put("waveId", allWavesRs.getString("waveId"));
            		oneWave.put("waveName", allWavesRs.getString("waveName"));
            		allWaves.add(oneWave);
            	}
            	allWavesRs.close();
            	allWavesPs.close();
            	resultMap.put("waves",allWaves);

            	//month level data
            	PreparedStatement monthTrendPs = conn.prepareStatement(scoreTrendByMonthSql);
            	monthTrendPs.setInt(1, projectId);
            	monthTrendPs.setInt(2, projectId);
            	monthTrendPs.setInt(3, projectId);
            	ResultSet monthTrendRs = monthTrendPs.executeQuery();
                Map<String,List<Map<String,String>>> trendsByMonthByScore = new HashMap<String,List<Map<String,String>>>();
            	while(monthTrendRs.next()) {
            		Map<String,String> oneMonth = new HashMap<String,String>();
            		String oneYearMonth = monthTrendRs.getString("month");
            		String yearPart = oneYearMonth.substring(2, 4);
            		String monthPart = oneYearMonth.substring(4);
            		oneMonth.put("month", monthPart+"/"+yearPart );
            		oneMonth.put("avgScore", ""+monthTrendRs.getInt("avgScore"));
            		oneMonth.put("totalScore", ""+monthTrendRs.getInt("totalScore"));
            		
            		String scoreId = monthTrendRs.getString("scoreId");
            		if ( trendsByMonthByScore.get(scoreId) == null ) {
            			trendsByMonthByScore.put(scoreId, new ArrayList<Map<String,String>>());
                    }
            		trendsByMonthByScore.get(scoreId).add(oneMonth);
            	}
                monthTrendRs.close();
                monthTrendPs.close();
                
                //Add month trend to corresponding scores
                for(Map<String, Object> oneScore : (List<Map<String,Object>>)resultMap.get("scores")) {
                	String scoreId = (String) oneScore.get("scoreId");
                	if ( trendsByMonthByScore.get(scoreId) != null ) {
                		oneScore.put("scoreTrendByMonth", trendsByMonthByScore.get(scoreId));
                	} else {
                		oneScore.put("scoreTrendByMonth", new ArrayList<Map<String,String>>());
                	}
                }
                
                //wave level data
                PreparedStatement waveTrendPs = conn.prepareStatement(scoreTrendByWaveSql);
                waveTrendPs.setInt(1, projectId);
                waveTrendPs.setInt(2, projectId);
                waveTrendPs.setInt(3, projectId);

            	ResultSet waveTrendRs = waveTrendPs.executeQuery();
                Map<String,List<Map<String,String>>> trendsByWaveByScore = new HashMap<String,List<Map<String,String>>>();
            	while(waveTrendRs.next()) {
            		Map<String,String> oneWave = new HashMap<String,String>();
            		oneWave.put("waveId", waveTrendRs.getString("waveId"));
            		oneWave.put("waveName", waveTrendRs.getString("waveName"));
            		oneWave.put("avgScore", ""+waveTrendRs.getInt("avgScore"));
            		oneWave.put("totalScore", ""+waveTrendRs.getInt("totalScore"));
            		
            		String scoreId = waveTrendRs.getString("scoreId");
            		if ( trendsByWaveByScore.get(scoreId) == null ) {
            			trendsByWaveByScore.put(scoreId, new ArrayList<Map<String,String>>());
                    }
            		trendsByWaveByScore.get(scoreId).add(oneWave);
            	}
                waveTrendRs.close();
                waveTrendPs.close();
                
                //Add wave trend to corresponding scores
                for(Map<String, Object> oneScore : (List<Map<String,Object>>)resultMap.get("scores")) {
                	String scoreId = (String) oneScore.get("scoreId");
                	if ( trendsByWaveByScore.get(scoreId) != null ) {
                		oneScore.put("scoreTrendByWave", trendsByWaveByScore.get(scoreId));
                	} else {
                		oneScore.put("scoreTrendByWave", new ArrayList<Map<String,String>>());
                	}
                }
            }
            
            LOGGER.info("---------------ScoreDaoImpl Ends getProjectScoreSummary----------------\n");

            return resultMap;
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
	public List<Map<String,Object>> getProjectStoreScores(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ScoreDaoImpl Starts getProjectStoreScores::projectId={}, storeId={}, taskId={}",projectId,storeId,taskId);
		
        String sql = "SELECT score.projectId,score.storeId,score.taskId,score.scoreId,scoreDef.scoreName,ROUND(score.score,0) as score," + 
        		" componentScore.componentScoreId,componentScoreDef.componentScoreName,ROUND(componentScore.componentScore,0) as componentScore,componentScore.componentScoreComment,componentScore.componentScoreAction " + 
        		" FROM ProjectStoreScore score, ProjectScoreDefinition scoreDef, ProjectStoreComponentScore componentScore, ProjectComponentScoreDefinition componentScoreDef " + 
        		" WHERE score.projectId=? AND score.storeId=? AND score.taskId =? " + 
        		" AND score.projectId = scoreDef.projectId AND score.scoreId = scoreDef.scoreId " + 
        		" AND score.projectId = componentScore.projectId AND score.storeId = componentScore.storeId AND score.taskId = componentScore.taskId AND score.scoreId = componentScore.scoreId " + 
        		" AND componentScore.projectId = componentScoreDef.projectId AND componentScore.scoreId = componentScoreDef.scoreId  AND componentScore.componentScoreId = componentScoreDef.componentScoreId " + 
        		" ORDER BY score.scoreId, componentScore.componentScoreId";
        
        Connection conn = null;
        Map<String,List<Map<String,String>>> componentScoresMap = new LinkedHashMap<String,List<Map<String,String>>>();
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new LinkedHashMap<String, String>();
                map.put("scoreId", ifNullToEmpty(rs.getString("scoreId")));
                map.put("scoreName", ifNullToEmpty(rs.getString("scoreName")));
                map.put("score", ifNullToEmpty(""+rs.getInt("score")));
                map.put("componentScoreId", ifNullToEmpty(rs.getString("componentScoreId")));
                map.put("componentScoreName", ifNullToEmpty(rs.getString("componentScoreName")));
                map.put("componentScore", ifNullToEmpty(""+rs.getInt("componentScore")));
                map.put("componentScoreComment", ifNullToEmpty(rs.getString("componentScoreComment")));
                map.put("componentScoreAction", ifNullToEmpty(rs.getString("componentScoreAction")));
               
                if ( componentScoresMap.get(map.get("scoreId")) == null ) {
                	componentScoresMap.put(map.get("scoreId"), new ArrayList<Map<String,String>>());
                }
                componentScoresMap.get(map.get("scoreId")).add(map);
            }
            rs.close();
            ps.close();
            
            List<Map<String,Object>> resultsList = new ArrayList<Map<String,Object>>();
            for (String scoreId : componentScoresMap.keySet() ) {
            	Map<String,Object> oneResultMap = new LinkedHashMap<String,Object>();
            	oneResultMap.put("scoreId", scoreId);
            	oneResultMap.put("scoreName", componentScoresMap.get(scoreId).get(0).get("scoreName"));
            	oneResultMap.put("score", componentScoresMap.get(scoreId).get(0).get("score"));
            	oneResultMap.put("componentScores", new ArrayList<Map<String,String>>());
            	for(Map<String,String> componentScore : componentScoresMap.get(scoreId)) {
            		Map<String,String> oneComponentScoreMap = new LinkedHashMap<String,String>();
            		oneComponentScoreMap.put("componentScoreId", componentScore.get("componentScoreId"));
            		oneComponentScoreMap.put("componentScoreName", componentScore.get("componentScoreName"));
            		oneComponentScoreMap.put("componentScore", componentScore.get("componentScore"));
            		oneComponentScoreMap.put("componentScoreComment", componentScore.get("componentScoreComment"));
            		oneComponentScoreMap.put("componentScoreAction", componentScore.get("componentScoreAction"));
            		((List<Map<String,String>>)oneResultMap.get("componentScores")).add(oneComponentScoreMap);
            	}
            	resultsList.add(oneResultMap);
            }
            
            LOGGER.info("---------------ScoreDaoImpl Ends getProjectStoreScores----------------\n");
            return resultsList;
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
	public List<Map<String, String>> getKeyMetricsByStoreVisit(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ScoreDaoImpl Starts getKeyMetricsByStoreVisit::projectId={}, storeId={}, taskId={}",projectId,storeId,taskId);
		
        String sql = "SELECT keyMetricDef.criteriaDesc,  criteriaScore.result FROM " + 
        		" ProjectStoreComponentCriteriaScore criteriaScore " + 
        		" RIGHT JOIN " + 
        		" (SELECT * FROM ProjectComponentCriteriaScoreDefinition WHERE projectId = ? AND focusCriteria = 1 ) keyMetricDef " + 
        		" ON criteriaScore.projectId = keyMetricDef.projectId " + 
        		" AND criteriaScore.scoreId = keyMetricDef.scoreId " + 
        		" AND criteriaScore.componentScoreId = keyMetricDef.componentScoreId " + 
        		" AND criteriaScore.groupId = keyMetricDef.groupId " + 
        		" AND criteriaScore.groupSequenceNumber = keyMetricDef.groupSequenceNumber " + 
        		" WHERE criteriaScore.projectId = ? AND criteriaScore.storeId = ?  AND criteriaScore.taskId = ?";
        
        Connection conn = null;
        List<Map<String,String>> keyMetricsList = new ArrayList<Map<String,String>>();
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);
            ps.setString(3, storeId);
            ps.setString(4, taskId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new LinkedHashMap<String, String>();
                map.put("metric", ifNullToEmpty(rs.getString("criteriaDesc")));
                String value = rs.getString("result");
                String metricValue = "0";
                if ( StringUtils.isNotBlank(value) ) {
                	if(value.trim().equals("true")) {
                		metricValue = "1";
                	}
                }
                map.put("value", metricValue);
                keyMetricsList.add(map);
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------ScoreDaoImpl Ends getKeyMetricsByStoreVisit----------------\n");
            return keyMetricsList;
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
	public Map<String,Map<String, String>> getKeyMetricsForAllStoreVisits(int projectId) {
		LOGGER.info("---------------ScoreDaoImpl Starts getKeyMetricsForAllStoreVisits::projectId={}",projectId);
		
        String sql = "SELECT criteriaScore.storeId,criteriaScore.taskId,keyMetricDef.scoreId, keyMetricDef.scoreName, keyMetricDef.componentScoreId,\n" + 
        		"        		keyMetricDef.groupId,keyMetricDef.groupSequenceNumber, keyMetricDef.criteriaDesc, criteriaScore.result FROM\n" + 
        		"        		       		ProjectStoreComponentCriteriaScore criteriaScore\n" + 
        		"        		       		RIGHT JOIN\n" + 
        		"        		       		(SELECT \n" + 
        		"                               a.projectId, a.scoreId, a.componentScoreId, a.groupId, a.groupSequenceNumber, a.criteriaDesc, b.scoreName\n" + 
        		"                             FROM ProjectComponentCriteriaScoreDefinition a, ProjectScoreDefinition b \n" + 
        		"                             WHERE a.projectId = ? AND a.focusCriteria != 0 \n" + 
        		"                             AND a.projectId = b.projectId AND a.scoreId = b.scoreId \n" + 
        		"                            ) keyMetricDef\n" + 
        		"        		       		ON criteriaScore.projectId = keyMetricDef.projectId\n" + 
        		"        		       		AND criteriaScore.scoreId = keyMetricDef.scoreId\n" + 
        		"        		       		AND criteriaScore.componentScoreId = keyMetricDef.componentScoreId\n" + 
        		"        		       		AND criteriaScore.groupId = keyMetricDef.groupId\n" + 
        		"        		       		AND criteriaScore.groupSequenceNumber = keyMetricDef.groupSequenceNumber\n" + 
        		"        		       		WHERE criteriaScore.projectId = ?\n" + 
        		"        		              ORDER BY criteriaScore.storeId, criteriaScore.taskId;";
        
        Connection conn = null;
        Map<String,Map<String, String>> keyMetricsByStoreVisit = new HashMap<String,Map<String,String>>();
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String storeId = rs.getString("storeId");
                String taskId = rs.getString("taskId");
                String key = storeId+"#"+taskId;
                
                if ( keyMetricsByStoreVisit.get(key) == null ) {
                	keyMetricsByStoreVisit.put(key,new HashMap<String,String>());
                }
                keyMetricsByStoreVisit.get(key).put(rs.getString("scoreName") + " - " + rs.getString("criteriaDesc"), rs.getString("result"));
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------ScoreDaoImpl Ends getKeyMetricsForAllStoreVisits----------------\n");
            return keyMetricsByStoreVisit;
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
	public Map<String,Map<String, String>> getScoresForAllStoreVisits(int projectId) {
		LOGGER.info("---------------ScoreDaoImpl Starts getScoresForAllStoreVisits::projectId={}",projectId);
		
        String sql = "SELECT storeId, taskId, scoreId, ROUND(score,0) as score " + 
        		" FROM ProjectStoreScore " + 
        		" WHERE projectId = ? " + 
        		" ORDER BY storeId,taskId ";
        
        Connection conn = null;
        Map<String,Map<String, String>> highLevelScoresByStoreVisit = new HashMap<String,Map<String,String>>();
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String storeId = rs.getString("storeId");
                String taskId = rs.getString("taskId");
                String key = storeId+"#"+taskId;
                
                if ( highLevelScoresByStoreVisit.get(key) == null ) {
                	highLevelScoresByStoreVisit.put(key,new HashMap<String,String>());
                }
                highLevelScoresByStoreVisit.get(key).put(rs.getString("scoreId"), ifNullToEmpty(""+rs.getInt("score")));
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------ScoreDaoImpl Ends getScoresForAllStoreVisits----------------\n");
            return highLevelScoresByStoreVisit;
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

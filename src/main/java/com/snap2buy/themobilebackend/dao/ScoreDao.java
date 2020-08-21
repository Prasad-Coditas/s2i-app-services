package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.StoreVisitScore;
import com.snap2buy.themobilebackend.model.StoreVisitScoreSummary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Anoop on 09/26/17.
 */
public interface ScoreDao {

	List<LinkedHashMap<String, String>> getProjectAllStoreScores(int projectId, String scoreId, String level, String value);

	List<StoreVisitScore> getProjectStoreScores(int projectId, String storeId);

	List<StoreVisitScoreSummary> getProjectAllStoresScoreSummary(int projectId);
	
	Map<String, Object> getProjectScoreSummary(int projectId, String level, String value);
	
	List<Map<String,Object>> getProjectStoreScores(int projectId, String storeId, String taskId);

	List<Map<String,String>> getKeyMetricsByStoreVisit(int projectId, String storeId, String taskId);
	
	Map<String,Map<String, String>> getKeyMetricsForAllStoreVisits(int projectId);
	
	Map<String,Map<String, String>> getScoresForAllStoreVisits(int projectId);

}

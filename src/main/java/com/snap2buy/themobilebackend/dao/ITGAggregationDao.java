package com.snap2buy.themobilebackend.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop
 */
public interface ITGAggregationDao {

	public void runDailyAggregation(String customerCode, int projectId, String visitDateId);

	public Map<String, Map<String, Object>> runStoreVisitAggregation(int projectId, String storeId, String taskId);

	public List<LinkedHashMap<String, Object>> getStoreVisitsToReview(int projectId, String visitDate, int bucketId, String storeId);

	public List<LinkedHashMap<String, Object>> getStoreVisitImagesToReview(int projectId, String storeId,
			String taskId, String showAll);

	public Map<String, Object> getStoreMetaInfo(String storeId);

	public void insertStoreVisitAggregationData(int projectId, String storeId, String taskId, String visitDateId,
			Map<String, Map<String, Object>> aggData, int bucketId, int reviewStatus, String reviewComments, String rejectReason);
	
	public void updateStoreVisitAggregationData(int projectId, String storeId, String taskId, String visitDateId,
			List<Map<String,Object>> brandList, int bucketId, int reviewStatus, String reviewComments, String rejectReason);

	public int getInvalidHighPriceInRawDataCount(int projectId, String storeId, String taskId);

	public boolean hasStitchingFailed(int projectId, String storeId, String taskId);

	public Map<String,String> getPriceConfidenceData(int projectId, String storeId, String taskId);

	public List<LinkedHashMap<String, Object>> getITGProductBrands(String string);
	
	public List<LinkedHashMap<String, Object>> getITGReviewStatsSummary();

}

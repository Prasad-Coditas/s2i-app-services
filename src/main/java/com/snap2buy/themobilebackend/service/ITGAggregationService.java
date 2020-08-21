package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.InputObject;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Anoop
 */
public interface ITGAggregationService {
    
	public void runDailyAggregation(int projectId, String visitDateId);

	public void runStoreVisitAggregation(int projectId, String storeId, String taskId);
	
	public List<LinkedHashMap<String, Object>> getStoreVisitsToReview(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getStoreVisitImagesToReview(InputObject inputObject);

	public void updateStoreVisitAggregationData(String jsonPayload);

	public List<LinkedHashMap<String, Object>> getITGStoreVisitReviewComments(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getITGProductBrands(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getITGReviewStatsSummary(InputObject inputObject);
	
}

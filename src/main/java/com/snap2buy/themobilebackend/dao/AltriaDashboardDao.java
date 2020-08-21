package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Anoop on 09/10/18.
 */
public interface AltriaDashboardDao {

	LinkedHashMap<String, String> getAltriaProjectSummary(Map<String, Object> queryArgs);

	Map<String, LinkedHashMap<String, Object>> getAltriaProjectBrandShares(Map<String, Object> queryArgs);

	Map<String, LinkedHashMap<String, Object>> getAltriaProjectBrandAvailability(Map<String, Object> queryArgs);

	Map<String, LinkedHashMap<String, Object>> getAltriaProjectWarningSignAvailability(Map<String, Object> queryArgs);

	Map<String, LinkedHashMap<String, Object>> getAltriaProjectProductAvailability(Map<String, Object> queryArgs);

	List<LinkedHashMap<String, String>> getAltriaProjectAllStoreResults(Map<String, Object> queryArgs);

	List<LinkedHashMap<String, String>> getAltriaProjectStoreImagesByStore(Map<String, Object> queryArgs);

	List<LinkedHashMap<String, String>> getAltriaStoresForReview(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaStoreImagesForReview(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaProductUPCs(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaImageAnalysisDetails(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaImageAnalysisNewByImageUUIDProjectIDAndUPCAndStore(InputObject inputObject);

	List<LinkedHashMap<String, String>> getStoreVisitsToAggregateByProjectIdStoreId(InputObject inputObject);

	void updateProjectStoreResultsResultCodeAndStatusForAltria(List<Map<String, String>> projects);

	void createImageAnalysisNewForAltria(LinkedHashMap<String, String> inputMap, List<String> upcsToAdd);

	void deleteImageAnalysisNewForAltria(LinkedHashMap<String, String> inputMap, List<String> upcsToDelete);

	List<LinkedHashMap<String, String>> getLinearFootageByStore(Map<String, Object> queryArgs);

	void updateLinearFootageByStore(Map<String, Object> queryArgs);

	List<Map<String, String>> getAltriaProjectProductAvailabilityForReport(Map<String, Object> queryArgs);

	Map<String, Map<String, String>> getAgentsByGeoLevelAndId(String customerCode, String userGeoLevel, String userGeoLevelId);

	Map<String, Object> getJobStatsByAgent(Map<String, Object> queryArgs);

	List<Map<String, String>> getStorewisePhotoCount(Map<String, Object> queryArgs);

	TreeMap<String, String> getVisitDaysInASalesMonth(String calendarMonth);
}

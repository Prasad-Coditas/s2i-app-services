package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Anoop on 09/10/18.
 */
public interface AltriaDashboardService {

	List<LinkedHashMap<String, Object>> getAltriaProjectSummary(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectBrandShares(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectBrandAvailability(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectWarningSignAvailability(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectBrandAvailabilityByStore(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectBrandSharesByStore(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectWarningSignAvailabilityByStore(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectProductAvailability(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaProjectProductAvailabilityByStore(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaProjectAllStoreResults(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaProjectStoreImagesByStore(InputObject inputObject);

	List<LinkedHashMap<String, String>> getAltriaStoresForReview(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getAltriaStoreImagesForReview(InputObject inputObject);

	List<LinkedHashMap<String, Object>> getProductDetectionsForReview(InputObject inputObject);

	void altriaAddOrUpdateProductDetection(InputObject inputObject, List<SkuTypePOJO> skuTypePOJOS);

	List<LinkedHashMap<String, Object>> runAggregationForAltria(InputObject inputObject);

	List<LinkedHashMap<String, String>> getLinearFootageByStore(InputObject inputObject);

	void updateLinearFootageByStore(InputObject inputObject);

	File getAltriaStoreVisitReport(InputObject inputObject, String tempFilePath);
}

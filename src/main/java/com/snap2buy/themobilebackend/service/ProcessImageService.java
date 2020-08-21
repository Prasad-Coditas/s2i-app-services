package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.*;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 10/17/15.
 */
public interface ProcessImageService {

    public Map<String, Object> storeImageDetails(InputObject inputObject, boolean isBulkUpload);
    public List<LinkedHashMap<String, String>> getImageAnalysis (String imageUUID);
    public List<LinkedHashMap<String, String>> doShareOfShelfAnalysis (InputObject inputObject);
    public LinkedHashMap<String, String> getJob(InputObject inputObject);
    public LinkedHashMap<String, String> getCronJobCount(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getStoreOptions(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getImages(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getProjectStoreImages(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getStores(InputObject inputObject);
    public List<LinkedHashMap<String, String>> doDistributionCheck(InputObject inputObject);
    public List<LinkedHashMap<String, String>> doBeforeAfterCheck(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getImageMetaData(InputObject inputObject);
    public void updateLatLong(InputObject inputObject);
    public File doShareOfShelfAnalysisCsv(InputObject inputObject,String tempFilePath);
    public File getProjectAllStoreResultsCsv(InputObject inputObject,String tempFilePath);
    public List<LinkedHashMap<String, String>> generateAggs(InputObject inputObject);
    public List<ProjectStoreResultWithUPC> getProjectStoreResults(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getProjectTopStores(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getProjectBottomStores(InputObject inputObject);
	public List<StoreWithImages> getProjectStoresWithNoUPCs(InputObject inputObject);
	public List<StoreWithImages> getProjectAllStoreImages(InputObject inputObject);
	public List<DuplicateImages> getProjectStoresWithDuplicateImages(InputObject inputObject);
	public List<LinkedHashMap<String, String>> generateStoreVisitResults(InputObject inputObject);
	public List<LinkedHashMap<String, String>> getProjectAllStoreResults(InputObject inputObject);
	public void recomputeProjectByStoreVisit(InputObject inputObject);
	public void reprocessProjectByStore(InputObject inputObject);
	public File getProjectAllStoreResultsDetailCsv(InputObject inputObject, String tempFilePath);
	public void updateProjectResultStatus(StoreVisitResult[] storeUpdateArray);
	public void bulkUploadProjectImage(int projectId, String sync, String filenamePath);
    public List<StoreVisit> getStoreVisitsWithImages(int projectId);
    public List<StoreVisit> getAlreadyUploadedStoreVisit(int projectId);
    public void saveStoreVisitRepResponses(int projectId, String storeId, String taskId, Map<String, String> repResponses);
    public List<LinkedHashMap<String, String>> insertOrUpdateStoreResult(int projectId, String storeId, String countDistinctUpc, String sumFacing, String sumUpcConfidence,	String resultCode, String status, String agentId, String taskId, String visitDateId, String imageUrl, String batchId, String customerProjectId);
	public File getProjectAllStoreImageResultsCsv(InputObject inputObject, String tempFilePath);
	public void computeImageResults(InputObject inputObject);
	public List<Map<String, String>> getImageResultsForPremium(InputObject inputObject);
    public List<LinkedHashMap<String, Object>> getProjectAllStoreImageResults(InputObject inputObject);
    public void pollAndSubmitStoreForAnalysis();
	public void updateProjectImageResultStatus(ImageStore[] imageResultUpdateArray);
	public void changeProjectImageStatus(InputObject inputObject);
	public File getProjectStoresWithDuplicateImagesCsv(InputObject inputObject, String tempFilePath);
	public void updateProjectImageResultUploadStatus(List<String> imageUUIDs, String string);

    List<LinkedHashMap<String, Object>> getProjectBrandSummary(InputObject inputObject);
    List<LinkedHashMap<String, Object>> getProjectBrandSummaryNew(InputObject inputObject);
	public List<ProjectStoreResultWithUPC> getProjectStoreDistribution(InputObject inputObject);
	public File getStoreLevelDistributionReport(InputObject inputObject, String tempFilePath);
	public List<LinkedHashMap<String, Object>> getProjectBrandShares(InputObject inputObject);
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesNew(InputObject inputObject);
	
	public String computeShelfLevel(String imageAnalysisList);
	public File getProjectBrandProductsReport(InputObject inputObject, String tempFilePath);

    void updateUPCForImageAnalysis(String newUpc,String id, String imageUUID, String leftTopX, String leftTopY, String shelfLevel, 
    		String price, String promotion, String compliant);
    void addImageAnalysisNew(String newUpc, String imageUUID, String leftTopX, String leftTopY,
                             String shelfLevel, int projectId,
                             String storeId, String height, String width,
                             String price, String promotion, String compliant);

    void deleteImageAnalysisNew(String id);

    void updateProjectImagePath(int projectId, String imagePath);
    Map<String, String> getProjectByCustomerCodeAndCustomerProjectId(int projectId);
	public Map<String, List<LinkedHashMap<String, String>>> getImagesByProjectIdList(List<String> projectIdList);
	public File getPremiumReportCsv(InputObject inputObject, String tempFilePath);
	public void pauseResumeImageAnalysis(String pauseImageAnalysis);
	List<LinkedHashMap<String, Object>> getProjectShareOfShelfByBrand(InputObject inputObject);
	public List<Map<String, String>> getProjectAllStoreShareOfShelfByBrand(InputObject inputObject);
	public Map<String, List<String>> getDailyImageErrorStats();
	void sendAppNotification(InputObject inputObject, String storeVisitStatus, String projectName,
			String resultComment);
	public void updateImageQualityParams(ImageStore imageToUpdate);
	public void deleteAllDetectionsByImageUUID(String imageUUID);
	public void updateStoreReviewStatus(String projectId, String storeId, String taskId, String reviewStatus,
			String status);
	public List<LinkedHashMap<String, String>> getProjectStoreRepResponses(InputObject inputObject);
	public List<LinkedHashMap<String, String>> getProjectStoresForReview(InputObject inputObject);
	public File getStoreLevelDistributionCSVReport(InputObject inputObject, String tempFilePath);
	File getStoreLevelImageCSVReport(InputObject inputObject, String tempFilePath);
	public void pollAndSubmitImagesForAnalysis(String batchSize);
	public File getHomePanelProjectStatusReport(String tempFilePath);
	public void updateDuplicateDetections(List<Long> duplicateDetections);
	public String getInternalProjectStatus(InputObject inputObject);
	public List<LinkedHashMap<String, String>> resolveGroupUpcs(String projectId, String storeId, String taskId,
			Map<String, List<String>> orderedUPCGroupMap);
	public void saveImageAnalysisData(String imageUUID, String imageAnalysisData);
	public List<LinkedHashMap<String, String>> getNextImagesToProcessExternally(InputObject inputObject);
	public void saveStoreLevelData(String inputPayload);
	void updateImageAnalysisStatus(String imageUUID, String inputPayload);
	public List<LinkedHashMap<String, Object>> getRepPerformanceMetrics(String userId);
	public void pollAndProcessStoreAnalysis();

}

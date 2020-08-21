package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 10/17/15.
 */
public interface ProcessImageDao {

    public ImageStore findByImageUUId(String imageUUId);
    public ImageStore getImageByStatus(String shelfStatus);
    public Integer getJobCount(String shelfStatus);
    public Integer getCronJobCount();
    public String getImageAnalysisStatus(String imageUUID);
    public ImageStore getNextImageDetails();
    public ImageStore getNextImageAndUpdateStatusAndHost(String hostId);

    public void insert(ImageStore imageStore);
    public void storeImageAnalysis(List<ImageAnalysis> ImageAnalysisList, ImageStore imageStore);
    public void updateStatusAndHost(String hostId, String status, String imageUUID);
    public void updateImageAnalysisStatus(String status, String imageUUID);
    public void updateImageAnalysisHostId(String hostId, String imageUUID);
    public void updateOrientationDetails(ImageStore imageStore);
    public void updateShelfAnalysisStatus(String status, String imageUUID);
    public void updateStoreId(String storeId, String imageUUID);
    public List<LinkedHashMap<String,String>> getImages(String storeId, String dateId);
    public List<LinkedHashMap<String,String>> getProjectStoreImages(int projectId, String storeId, String taskId);
    public List<ImageAnalysis> getImageAnalysis(String imageUUID);
    public LinkedHashMap<String,Object> getFacing(String imageUUID);
    public List<LinkedHashMap<String,String>> doShareOfShelfAnalysis(String getImageUUIDCsvString);
//    public List<LinkedHashMap<String,String>> doShareOfShelfAnalysisCsv(String getImageUUIDCsvString);
    public void updateLatLong(String imageUUID,String latitude,String longitude);

    public  List<LinkedHashMap<String, String>>  generateAggs(int projectId, String storeId, String taskId) throws SQLException;
    public Map<String,List<Map<String,String>>>  getProjectStoreData(int projectId, String storeId, String taskId);
    public List<LinkedHashMap<String,String>>  getProjectTopStores(int projectId, String limit);
    public List<LinkedHashMap<String,String>>  getProjectBottomStores(int projectId, String limit);
	public List<StoreWithImages> getProjectStoresWithNoUPCs(int projectId);
	public List<StoreWithImages> getProjectAllStoreImages(int projectId);
	public List<DuplicateImages> getProjectStoresWithDuplicateImages(int projectId);
	public List<LinkedHashMap<String, String>> generateStoreVisitResults(int projectId, String storeId,String taskId);
	public List<LinkedHashMap<String, String>> getProjectAllStoreResults(int projectId, String level, String value);
	public void reprocessProjectByStore(int projectId,List<String> storeIdsToReprocess);
	public List<String> getProjectStoreIds(int projectId, boolean onlyDone);
	public List<LinkedHashMap<String, String>> getProjectAllStoreResultsDetail(int projectId);
	public void updateProjectResultStatus(List<StoreVisitResult> storeVisitResults);
    public List<StoreVisit> getStoreVisitsWithImages(int projectId);
    public List<LinkedHashMap<String, String>> insertOrUpdateStoreResult(int projectId, String storeId, String countDistinctUpc, String sumFacing, String sumUpcConfidence,	String resultCode, String status, String agentId, String taskId, String visitDateId, String imageUrl, String batchId, String string);
    public void saveStoreVisitRepResponses(int projectId, String storeId, String taskId, Map<String, String> repResponses);
    public Map<String, String> getRepResponsesByStoreVisit(int projectId, String storeId,String taskId);
    public List<StoreVisit> getStoreVisitsForRecompute(int projectId);
    public List<StoreVisit> getAlreadyUploadedStoreVisit(int projectId);
    public boolean isStoreAvailableInStoreResults(int projectId, String storeId, String taskId);
	public List<LinkedHashMap<String, String>> getProjectStoreResults(int projectId, String storeId, String month, String taskId);
	public Map<String, String> generateImageResult(ImageStore image, List<ImageAnalysis> imageAnalysisOutput);
	public void updateImageResultCodeAndStatus(String imageUUID, String code, String comment, String imageReviewStatus, String objectiveResultStatus);
	public List<LinkedHashMap<String, String>> getProjectAllStoreImageResults(int projectId, String status);
	public Map<String, Map<String, String>> getAllImageResultsForStoresWithMultipleImages(int projectId);
	void updateImageResultCodeBatch(Map<String, String> imageResultMap);
	public List<Map<String, String>> getAllImageResultsForDuplicateImages(int projectId);
	public List<Map<String, String>> getImageResultsForPremium(int projectId);
	public void updateImageResultCodeByImageHashScoreBatch(Map<String,String> imageHashScoresWithDuplicates, int projectId);
	public void updateImageHashScoreResults(ImageStore imageStore);
	public List<String> getImagesByStoreVisit(int projectId, String storeId, String taskId);
	public void updateImageResultCodeAndStatusBatch(List<ImageStore> imageStoreList) throws Exception;
    public List<LinkedHashMap<String,String>> getAggsReadyStoreVisit();
	public List<ImageAnalysis> getImageAnalysisForRecompute(String imageUUID);
	public void updateProjectStoreResultByBatchId(int projectId, String batchId, String resultCode);
	public void updateBatchIdForProjectStoreResults(int projectId, List<StoreVisit> storeVisits, String batchId);
	public void changeProjectImageStatus(int projectId, String currentImageStatus, String newImageStatus);
	public Map<String, Map<String, String>> getImagesForSurveyUpload(int projectId);
	public Map<String, List<Map<String, String>>> getImageAnalysisForSurveyUpload(int projectId);
	public Map<String, List<Map<String, String>>> getProjectStoreImageMetaData(int projectId, String storeId);
	public void updateProjectImageResultUploadStatus(List<String> imageUUIDs, String resultUploaded);
	void updatePreviewImageUUIDForStoreVisit(int projectId, String storeId,
			String taskId, String previewImageUUID);


    String getDistinctStoreIdCountByMonth(int projectId, List<String> childProjectIds, String month, String waveId, String subCategory, String modular, String storeFormatToFilter, String retailerToFilter);

    String getTotalUpcs(int projectId);

    String getTotalBrands(int projectId);

    String getTotalFacings(int projectId);

    List<String> getListBrands(int projectId);
    List<String> getListBrandsNew(String customerCode, int projectId);

    String getAverageUpcsPerStore(int projectId);

    String getAverageFacingPerStore(int projectId);

    List<String> getListMonths(int projectId);
    List<String> getListMonthsNew(String customerCode, int projectId);

    String isDistributionCheckProject(int projectId);

    void insertIntoProjectDistributionStoreData(int projectId, String storeId, String taskId);

    void updateProjectStoreResult(int projectId, String storeId, String taskId);
	public Map<String, List<Map<String, String>>> getProjectStoreDistributionData(int projectId, String storeId);
	public List<Map<String,String>> getStoreLevelDistribution(int projectId);
	List<Map<String, String>> getDetailedBrandShareByMonth(int projectId, String month, String rollup);
	public List<Map<String, String>> getDetailedBrandShareByMonthNew(int projectId,
			List<String> childProjects, String month, String waveId, String rollup, String subCategoryToFilter,
			String modularToFilter, String storeFormatToFilter, String retailerToFilter);
	public List<Map<String, String>> getListStores(int projectId);
	List<Map<String, String>> getShelfLevelFacings(int projectId, String month, String brand);
	public List<Map<String, String>> getShelfLevelFacingsNew(int projectId, List<String> childProjectIds, String month, String waveId,
			String subCategoryToFilter, String modularToFilter, String storeFormatToFilter, String retailerToFilter);
	List<String> getListManufacturers(int projectId);
	List<String> getListManufacturersNew(String customerCode, int projectId);

	List<Map<String, String>> getProjectBrandProducts(int projectId, String month, String waveId);
	public Map<String, List<Map<String, String>>> getProjectStoreImageWithDetectionsMetaData(int projectId, String storeId, String taskId);
    List<LinkedHashMap<String, String>> getImageDetailsByStoreVisit(int projectId, String storeId, String taskId);

    void updateLinearFootageInProjectStoreResult(int projectId, String storeId,String taskId,
                                                 String linearFootage, String stitchedImagePath,
                                                 String subCategoryHeatMapPath);

    //String sumOfOosCountForImageStoreNew(int projectId,String storeId, String taskId);

    Map<String, String> getProjectStoreResultByCustomerCodeAndProjectId(int projectId, String storeId, String taskId);
    void updateUPCForImageAnalysis(String newUpc,String id, String imageUUID, String leftTopX, String leftTopY, String shelfLevel, 
    		String price, String promotion, String compliant);
    void addImageAnalysisNew(String newUpc, String imageUUID, String leftTopX, String leftTopY,
                             String shelfLevel, int projectId,
                             String storeId, String height, String width,
                             String price, String promotion, String compliant);

    void updateProjectImagePath(int projectId, String imagePath);
    Map<String, String> getProjectByCustomerCodeAndCustomerProjectId(int projectId);
	public Map<String, List<LinkedHashMap<String, String>>> getImagesByProjectIdList(List<String> projectIdList);
	public void updateImageResultCodeToUnapprovedForNotEnoughTimeResponse(int projectId, int parentProjectId);
	public List<Map<String, Object>> getQuestionsBasedDetections(int projectId);
	public List<String> getListSubCategoriesNew(String customerCode, int projectId);
	public List<String> getListModularsNew(String customerCode, int projectId);
	//public List<String> getListRetailersNew(List<String> childProjectIds);
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllModulars(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter);
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllRetailers(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter);
	public List<Map<String, String>> getProjectAllStoreShareOfShelfByBrand(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter);

	void deleteImageAnalysisNew(String id);
	
	List<Map<String, Object>> getProjectStoreCountForAllProductsAllModulars(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter);
	public Map<String, List<String>> getDailyImageErrorStats();
	public void updateImageResultCodeToUnapprovedForFeedbackQuestionResponse(int projectId, String feedbackQuestionId);

	public void saveProjectStoreScores(int projectId, String storeId, String taskId,
			List<Map<String,Object>> scores, List<Map<String,Object>> componentScores, List<Map<String,Object>> componentCriteriaScores);
	Map<String, Object> getStoreLevelDataForAnalysis(int projectId, String storeId, String taskId);
	Map<String, String> getPhotoQualityReportByStoreVisit(int projectId);
	public void setStoreVisitForAggregation(int projectId, String storeId, String taskId);
	public void updateImageQualityParams(ImageStore imageToUpdate);
	public void deleteAllDetectionsByImageUUID(String imageUUID);
	public void updateStoreReviewStatus(String projectId, String storeId, String taskId, String reviewStatus,
			String status);
	public void updateImageReviewStatusByStoreVisit(String projectId, String storeId, String taskId, String imageReviewStatus);
	public List<LinkedHashMap<String, String>> getProjectStoreRepResponses(int projectId, String storeId,
			String taskId);
	public List<LinkedHashMap<String, String>> getProjectStoresForReview(int projectId, String waveId, String fromDate,
			String toDate, String reviewStatus);
	public List<Map<String, String>> getStoreLevelDistribution(int projectId, String fromDate, String toDate, String customerCode);
	public List<Map<String, String>> getStoreImagesWithStoreMetadata(int projectId, String fromDate, String toDate,
			String customerCode);
	List<String> getImagesForProcessing(String batchSize);
	//List<String> getListStoreFormats(String customerCode, int parentProjectId);
	List<Map<String, String>> getProjectBrandSharesByBrandForAllSubCategories(int projectId,
			List<String> childProjectIds, String month, String waveId, String brandNameToFilter,
			String storeFormatToFilter, String retailerToFilter);
	List<Map<String, String>> getProjectBrandSharesByBrandForAllStoreFormats(int projectId,
			List<String> childProjectIds, String month, String waveId, String brandNameToFilter,
			String subCategoryToFilter, String retailerToFilter);
	List<Map<String, String>> getHomePanelProjectStatusData();
	public void updateDuplicateDetections(List<Long> duplicateDetections);
	public List<LinkedHashMap<String, String>> generateAggsType1(int projectId, String storeId, String taskId) throws SQLException;
	public Map<String, List<String>> getListRetailersAndStoreFormats(List<String> childProjectIds);
	public List<Map<String, String>> getProjectPhotoCountGroups(int projectId, String limit, String waveId);
	public List<Map<String, String>> getProjectAllStorePhotoCount(int projectId, String waveId);
	public List<Map<String, String>> getProjectDetectedUPCCountGroups(int projectId, String limit, String waveId);
	public List<Map<String, String>> getProjectAllStoreDetectedUPCCount(int projectId, String waveId);
	public List<Map<String, String>> getProjectDistributionPercentageGroups(int projectId, String limit, String waveId);
	public List<Map<String, String>> getProjectAllStoreDistributionPercentage(int projectId, String waveId);
	public List<Map<String, String>> getProjectAllStoreShareOfShelf(int projectId, List<String> childProjectIds,String waveId);
	public Map<String, List<String>> getImages(String projectId, String storeId, String taskId);
	void updateUPCForImageAnalysis(String imageUUID, String id, String newUpc);
	List<ImageStore> getImagesForExternalProcessing(int projectId, String limit);
	public boolean isProcessingComplete(int projectId, String storeId, String taskId);
	public void updateShelfLevelAndDuplicateDetections(List<String> updateList);
	void setStoreVisitForStoreAnalysis(int projectId, String storeId, String taskId);
	public LinkedHashMap<String, Object> getNextStoreVisitToReview(int projectId);
	public LinkedHashMap<String, Object> getPhotoQualityMetricsByUserId(String userId);
	void setStoreVisitToStoreAnalysisReceived(int projectId, String storeId, String taskId);
	List<LinkedHashMap<String, String>> getStoreVisitsForStoreAnalysisProcessing();
	void setStoreVisitToStoreAnalysisFailed(int projectId, String storeId, String taskId);
	
}

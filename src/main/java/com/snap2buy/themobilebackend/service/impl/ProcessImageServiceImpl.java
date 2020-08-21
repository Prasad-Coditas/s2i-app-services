package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.*;
import com.snap2buy.themobilebackend.async.CloudStorageService;
import com.snap2buy.themobilebackend.dao.*;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.*;
import com.snap2buy.themobilebackend.upload.BulkUploadEngine;
import com.snap2buy.themobilebackend.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
@Scope("prototype")
public class ProcessImageServiceImpl implements ProcessImageService {

	private static final String PAUSE_IMAGE_ANALYSIS_FILE_LOCK = "/root/pauseImageAnalysis";
	private static final String IMAGE_ANALYSIS_RUNTIME_CONFIGURATION_FILE = "/root/imageAnalysis.conf";

	private static Logger LOGGER = LoggerFactory.getLogger(ProcessImageServiceImpl.class);
	
	private static final Marker MARKER_IMG_PROC =  MarkerFactory.getMarker("IMG_PROC");

	@Autowired
	@Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
	private ProcessImageDao processImageDao;

	@Autowired
	@Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
	private StoreMasterDao storeMasterDao;

	@Autowired
	@Qualifier(BeanMapper.BEAN_PRODUCT_MASTER_DAO)
	private ProductMasterDao productMasterDao;

	@Autowired
	@Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
	private MetaServiceDao metaServiceDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_SCORE_DAO)
	private ScoreDao scoreDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_SHELF_ANALYSIS_DAO)
	private ShelfAnalysisDao shelfAnalysisDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_META_SERVICE)
	private MetaService metaService;

	@Autowired
	@Qualifier(BeanMapper.BEAN_SHELF_ANALYSIS_SERVICE)
	private ShelfAnalysisService shelfAnalysisService;

	private static int tracker = 0;

	@Autowired
	private Environment env;
	
	@Autowired
	private CloudStorageService cloudStorageService;

	@Autowired
	@Qualifier(BeanMapper.BEAN_UI_SERVICE)
	private UIService uiService;
	
	@Autowired
	@Qualifier("batchImageAnalysisWorkerPool")
	private ThreadPoolTaskExecutor batchImageAnalysisWorkerPool;
	
	@Autowired
	@Qualifier("syncImageAnalysisWorkerPool")
	private ThreadPoolTaskExecutor syncImageAnalysisWorkerPool;
	
	@Autowired
	@Qualifier("thumbnailGeneratorWorkerPool")
	private ThreadPoolTaskExecutor thumbnailGeneratorWorkerPool;
	
	@Autowired
    @Qualifier(BeanMapper.BEAN_ITG_AGGREGATION_SERVICE)
    private ITGAggregationService itgAggregationService;

	private final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#");
	
	private final DecimalFormat SCORE_DECIMAL_FORMATTER = new DecimalFormat("##.###");
	
	@Override
	public Map<String,Object> storeImageDetails(InputObject inputObject, boolean isBulkUpload) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts storeImageDetails----------------\n");
		
		Map<String,Object> result = new LinkedHashMap<String,Object>();

		/*if (inputObject.getStoreId().equalsIgnoreCase("-9")) {
			inputObject.setStoreId(storeMasterDao.getStoreId(inputObject.getLongitude(), inputObject.getLatitude()));
		}
		
		LOGGER.info("--------------storeId={}", inputObject.getStoreId() );*/
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		inputObject.setCategoryId(projectDetail.get(0).get("categoryId"));
		
		if("app".equalsIgnoreCase(inputObject.getSource())){

			String storeId = inputObject.getStoreId();
			String retailerStoreId = inputObject.getRetailerStoreId();
			String placeId = inputObject.getPlaceId();
			String customerStoreNumber = inputObject.getCustomerStoreNumber();

			boolean storeIdPresent = (storeId == null || storeId.equalsIgnoreCase("-9")) ? false : true;
			boolean retailerStoreIdPresent = (retailerStoreId == null || retailerStoreId.equalsIgnoreCase("-9")) ? false : true;
			boolean placeIdPresent = (placeId == null || placeId.equalsIgnoreCase("-9")) ? false : true;
			boolean customerStoreNumberPresent = (customerStoreNumber == null || customerStoreNumber.equalsIgnoreCase("-9")) ? false : true;
			
			if (!retailerStoreIdPresent) { retailerStoreId = null; }
			if (!placeIdPresent) { placeId = null; }

			boolean createNewStore = false;
	        
			//if storeId is present in request, use it. Else, try lookup.
			if(!storeIdPresent) {
				//If customerStoreNumber is present in request, lookup using that from customer-store mapping.
				if ( customerStoreNumberPresent ) {
					//lookup, if found set this storeId
					String mappedStoreId = metaServiceDao.getStoreByCustomerCodeAndCustomerStoreNumber(inputObject.getCustomerCode(), customerStoreNumber);
					inputObject.setStoreId(mappedStoreId);
				} else {
					//If retailerStoreId is present in request, lookup using retailerStoreId+projectRetailerChainCode combination.
					//If placeId is present in request, try lookup by placeId.
					if ( retailerStoreIdPresent ) {
						String retailerCode = projectDetail.get(0).get("retailerCode");
						//Check for   and RetailerChainCode
						List<LinkedHashMap<String, String>> storeResult = metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(retailerStoreId, retailerCode);
						if ( storeResult == null || storeResult.isEmpty()) {
							createNewStore = true;
						} else {
							inputObject.setStoreId(storeResult.get(0).get("storeId"));
						}
					} else if ( placeIdPresent ) {
						List<LinkedHashMap<String, String>> storeByPlaceId = storeMasterDao.getStoreMasterByPlaceId(inputObject.getPlaceId());
						if(null == storeByPlaceId || storeByPlaceId.isEmpty()) {
							createNewStore = true;
						} else {
							inputObject.setStoreId(storeByPlaceId.get(0).get("storeId"));
						}
					} else {
						throw new IllegalStateException("Unable to determine store for the rep response. Request failed.");
					}
				}
			}	
	            
			if(createNewStore) {
				String uniqueStoreId = metaService.generateStoreId();

				StoreMaster storeMaster = new StoreMaster();
				storeMaster.setStoreId(uniqueStoreId);
				storeMaster.setRetailerStoreId(retailerStoreId);
				storeMaster.setPlaceId(placeId);
				storeMaster.setLatitude(inputObject.getLatitude());
				storeMaster.setLongitude(inputObject.getLongitude());
				storeMaster.setComments("IMAGE_UPLOAD");
				storeMaster.setName(inputObject.getName());
				storeMaster.setStreet(inputObject.getStreet());
				storeMaster.setCity(inputObject.getCity());
				storeMaster.setState(inputObject.getState());
				storeMaster.setCountry(inputObject.getCountry());
				
				storeMasterDao.createStoreWithPlaceId(storeMaster);
				
				inputObject.setStoreId(uniqueStoreId);
			}
			LOGGER.info("--------------Converted StoreId={}", inputObject.getStoreId() );
		}

		LOGGER.info("---------------ProcessImageServiceImpl inputObject.getUserId() = {}",inputObject.getUserId());

		if ((inputObject.getUserId().isEmpty()) || (inputObject.getUserId().equalsIgnoreCase(""))
				|| (inputObject.getUserId().equalsIgnoreCase("-9"))) {
			inputObject.setUserId("app-web");
		}

		String imageStatus = null;
		String shelfStatus = "new";
		String imageReviewStatus = "0";
		String imageResultComments = null;
		String imageResultCode = null;
		String processedDate = null;
		String resultUploaded = "0";
		String storeResultCode = "99";
		String storeStatus = "0";

		LOGGER.info("---------------ProcessImageServiceImpl inputObject.getImageStatus() = {}",inputObject.getImageStatus());

		if ((inputObject.getImageStatus() == null) || (inputObject.getImageStatus().isEmpty())
				|| (inputObject.getImageStatus().equalsIgnoreCase(""))
				|| (inputObject.getImageStatus().equalsIgnoreCase("-9"))) {
			imageStatus = "new";
		} else {
			imageStatus = inputObject.getImageStatus();
		}

		LOGGER.info("---------------ProcessImageServiceImpl imageStatus = {}", imageStatus );

		ImageStore imageStore = new ImageStore(inputObject.getImageUUID(), inputObject.getImageFilePath(),
				inputObject.getCategoryId(), inputObject.getLatitude(), inputObject.getLongitude(),
				inputObject.getTimeStamp(), inputObject.getStoreId(), inputObject.getHostId(),
				inputObject.getVisitDate(), imageStatus, shelfStatus, inputObject.getOrigWidth(),
				inputObject.getOrigHeight(), inputObject.getNewWidth(), inputObject.getNewHeight(),
				inputObject.getThumbnailPath(), inputObject.getUserId(), inputObject.getTaskId(), inputObject.getAgentId(),
				inputObject.getTimeStamp(), inputObject.getImageHashScore(), inputObject.getImageRotation(),
				inputObject.getFileId(), inputObject.getQuestionId(), imageResultCode, imageReviewStatus,
				inputObject.getImageUrl(), processedDate, imageResultComments, resultUploaded,
				inputObject.getPreviewPath(), inputObject.getProjectId());
		imageStore.setSequenceNumber(inputObject.getSequenceNumber());

		LOGGER.info("---------------ProcessImageServiceImpl imagestoreobj = {}", imageStore );
		LOGGER.info("---------------ProcessImageServiceImpl is bulk upload = {}", isBulkUpload );
		LOGGER.info("---------------ProcessImageServiceImpl is sync = {}", inputObject.getSync() );

		if (!isBulkUpload) { // insert an entry to project store results table, if an image is being uploaded
			// for the first time for a store.
			if (!processImageDao.isStoreAvailableInStoreResults(inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId())) {
				String batchId = ConverterUtil.getBatchIdForImport(inputObject.getProjectId()+"");
				String imageUrl = "";
				processImageDao.insertOrUpdateStoreResult(inputObject.getProjectId(), inputObject.getStoreId(), "0", "0", "0", storeResultCode, storeStatus,
						inputObject.getAgentId(), inputObject.getTaskId(), inputObject.getVisitDate(), imageUrl,
						batchId,inputObject.getCustomerProjectId());
				processImageDao.updatePreviewImageUUIDForStoreVisit(inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId(),
						inputObject.getImageUUID());
			}
		}

		boolean realtimeAnalysis = metaServiceDao.getRealtimeProcessingEnabled(inputObject.getProjectId());

		if (inputObject.getSync().equals("true") && realtimeAnalysis) {
			LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: store image details----------------\n");
			processImageDao.insert(imageStore);
			
			boolean isCallFromApp = inputObject.getSource().equalsIgnoreCase("app");
			
			Future<Map<String,Object>> future = syncImageAnalysisWorkerPool.submit( new Callable<Map<String,Object>>() {
				@Override
				public Map<String,Object> call() {
					return doSyncProcessing(inputObject, projectDetail, imageStore, isCallFromApp);
				}
			}
			);
		
			if ( ! isCallFromApp ) { //if from UI, wait for the thumbnail generation to get over.
				try {
					result = future.get();
				} catch (Exception e) {
					LOGGER.error("---------------ProcessImageServiceImpl :: sync call :: Error while generating thumbnail----------------\n", e);
				}
			}
		} else {
			LOGGER.info("---------------ProcessImageServiceImpl inserting image for async processing----------------\n");
			LOGGER.info("---------------ProcessImageServiceImpl inputObject.getSync() = {}", inputObject.getSync());
			LOGGER.info("---------------ProcessImageServiceImpl inputObject.getImageStatus() = {}", inputObject.getImageStatus() );

			if (inputObject.getSync().equals("paused")) {
				imageStore.setImageStatus("paused");
			} else {
				if ((inputObject.getImageStatus() == null) || (inputObject.getImageStatus().isEmpty())
						|| (inputObject.getImageStatus().equalsIgnoreCase(""))
						|| (inputObject.getImageStatus().equalsIgnoreCase("-9"))) {
					imageStore.setImageStatus("cron");
				} else {
					imageStore.setImageStatus(inputObject.getImageStatus());
				}
			}
			
			//Handle image status for external processing, if enabled.
			boolean externalProcessingEnabled = metaServiceDao.getExternalProcessingEnabled(inputObject.getProjectId());
			if ( externalProcessingEnabled ) {
				String oldImageStatus = imageStore.getImageStatus();
				
				imageStore.setImageStatus(Constants.EXTERNAL_PROCESSING_IMAGE_STATUS);
				
				// Switch back to internal processing if question for which photo is taken is marked for "skipping image analysis".
				String questionId = imageStore.getQuestionId();
				if ( StringUtils.isNotBlank(questionId) ) {
					List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(imageStore.getProjectId());
					for( ProjectQuestion question : projectQuestions ) {
						if ( questionId.equalsIgnoreCase(question.getId()) && question.getSkipImageAnalysis().equals("1") ) {
							imageStore.setCategoryId(Constants.PASSTHROUGH_CATEGORY_ID); //override category id with pass through category id, just for GPU call.
							imageStore.setImageStatus(oldImageStatus);
							LOGGER.info("--------------storeImageDetails:: setting passthrough category id because image should be skipped from analysis, imageUUID = {}", imageStore.getImageUUID());
							break;
						}
					}
				}
			}
			
			LOGGER.info("---------------ProcessImageServiceImpl start store image details not sync----------------\n");
			processImageDao.insert(imageStore);
			
			if(inputObject.getSource().equalsIgnoreCase("app")){
				if(inputObject.getSequenceNumber().equalsIgnoreCase(inputObject.getTotalImages())){
					String storeVisitStatus = "0";
					String projectName = "";
					String resultComment = "";
					this.sendAppNotification(inputObject, storeVisitStatus, projectName, resultComment);
				}
			}
			
			LOGGER.info("---------------ProcessImageServiceImpl Ends storeImageDetails not sync----------------\n");
			result = new LinkedHashMap<String, Object>();
		}
		
		return result;
	}

	private Map<String,Object> doSyncProcessing(InputObject inputObject, List<LinkedHashMap<String, String>> projectDetail,
			ImageStore imageStore, boolean isCallFromApp ) {
		LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: Starting image processing for imgaeUUID {}", imageStore.getImageUUID() );
		String retailer = storeMasterDao.getRetailerByStoreId(imageStore.getStoreId());
		LOGGER.info("--------------retailer={}", retailer );

		List<LinkedHashMap<String, String>> project = metaServiceDao.getProjectDetail(imageStore.getProjectId());
		String projectTypeId = project.get(0).get("projectTypeId");
		LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: runImageAnalysis::projectTypeId={}", projectTypeId );

		boolean syncCall = true;
		List<ImageAnalysis> imageAnalysisList = invokeImageAnalysis(imageStore, retailer, projectTypeId,syncCall);

		// Store imageHashScore, imageResultCode and imageResultComments
		processImageDao.updateImageHashScoreResults(imageStore);
		
		String computedImageResultComments = imageStore.getImageResultComments();
		String computedImageResultCode = imageStore.getImageResultCode();
		
		boolean imageAnalysisSuccess = true;
		
		if (null != imageAnalysisList) {

			LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: storing imageAnalysis results----------------\n");
			processImageDao.storeImageAnalysis(imageAnalysisList, imageStore);
			
			LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: updated status to done----------------\n");
			processImageDao.updateImageAnalysisStatus("done", imageStore.getImageUUID());
			
		} else {
			imageAnalysisSuccess = false;
			LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: image analysis failed. Mark as error.----------------\n");
			processImageDao.updateImageAnalysisStatus("error", imageStore.getImageUUID());
		}
		
		
		LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: Generate thumbnail asynchronously----------------\n");
		Future<?> future = thumbnailGeneratorWorkerPool.submit( new Runnable() {
				@Override
				public void run() {
					boolean isThumbNailProcessingSuccess = true;
					createThumbnail(imageStore, isThumbNailProcessingSuccess);
				}
			}
		);
		
		if ( ! isCallFromApp ) { //if from UI, wait for the thumbnail generation to get over.
			try {
				future.get();
			} catch (Exception e) {
				LOGGER.error("---------------ProcessImageServiceImpl :: sync call :: Error while generating thumbnail----------------\n", e);
			}
		}
		
		// Checked if this is the last file in process which is uploaded from device
		if(isCallFromApp && inputObject.getSequenceNumber().equalsIgnoreCase(inputObject.getTotalImages())){
		    //Check if all images for this store visit are already processed. If yes, proceed to agg, if not, wait till they are processed.
			//Max time out of 5 minutes.
			int i=0;
			boolean isProcessingComplete =false;
			while(true) {
				LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: checking if all images are processed::projectId {}, storeId {}, taskId {}", 
						imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId());
				isProcessingComplete = processImageDao.isProcessingComplete(imageStore.getProjectId(),imageStore.getStoreId(),imageStore.getTaskId());
				if ( isProcessingComplete ) {
					LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: processing complete----------------\n");
					break;
				} else {
					if ( i > 60 ) {
						LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: some images are still pending. Timed out, Not aggregating----------------\n");
						break;
					}
					try {
						LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: some images are pending. Sleeping for 5 seconds----------------\n");
						Thread.sleep(5000);
						i=i+1;
					} catch (InterruptedException e) {
						LOGGER.info("--------------ProcessImageServiceImpl :: sync call :: Error while waiting for all images to get processed----------------\n");
					}
					
				}
			}
			
			if ( isProcessingComplete ) {
				// Call to generate aggs
			    try {
					processImageDao.generateAggs(imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    if ( projectDetail.get(0).get("projectTypeId").equalsIgnoreCase("5") ) {
			       computeDistributionMetric(imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId() );
			    }

				List<LinkedHashMap<String, String>> visitResults = processImageDao.generateStoreVisitResults(imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId());

			    this.generateScore(imageStore.getProjectId(), new StoreVisit(imageStore.getStoreId(), imageStore.getTaskId()));
			            
			    String storeVisitStatus = visitResults.get(0).get("status");
			    String projectName = project.get(0).get("projectName");
			    String resultComment = visitResults.get(0).get("resultComment");
			            
			    this.sendAppNotification(inputObject, storeVisitStatus, projectName, resultComment);
			            
			    LOGGER.info("---------------ProcessImageServiceImpl :: sync call :: Processing done, notification sent ----------------\n");
			}
		 }

		 if ( imageAnalysisSuccess ) {
		    // Compute imageResultCode only if GPU call didn't return one.
			if (StringUtils.isBlank(imageStore.getImageResultCode())
					&& StringUtils.isNotBlank(imageStore.getQuestionId())
					&& ( StringUtils.isNotBlank(imageStore.getFileId()) || !isCallFromApp ) ) {
				Map<String, String> resultMap = processImageDao.generateImageResult(imageStore, imageAnalysisList);

				LOGGER.info("---------------GPU Call didn't return anything, resultMap: {}", resultMap);

				ImageResultCode resultCode = ImageResultCode
						.getImageResultCodeFromCode(resultMap.get("resultCode"));
				
				computedImageResultCode = resultCode.getDesc();
				computedImageResultComments = (String) resultMap.get("resultComment");
				String objectiveResultStatus = (String) resultMap.get("objectiveResultStatus");
				String imageReviewStatus = "1";
				if (resultCode == ImageResultCode.REJECT_LEVEL_1 || resultCode == ImageResultCode.REJECT_LEVEL_3
						|| resultCode == ImageResultCode.APPROVED_PENDING_REVIEW
						|| resultCode == ImageResultCode.REJECT_INSUFFICIENT
						|| resultCode == ImageResultCode.UNAPPROVED) {
					imageReviewStatus = "0"; // for review
				}
				processImageDao.updateImageResultCodeAndStatus(imageStore.getImageUUID(), resultCode.getCode(),
						computedImageResultComments, imageReviewStatus, objectiveResultStatus);
			}
		}

		List<LinkedHashMap<String, String>> analysisMap = new ArrayList<LinkedHashMap<String, String>>();
		if ( !isCallFromApp ) { //Return image analysis data only for UI calls.
			List<ImageAnalysis> analysisResult = processImageDao.getImageAnalysis(imageStore.getImageUUID());
			LOGGER.info("---------------ProcessImageServiceImpl Ends storeImageDetails   sync, Result: {}", analysisResult);
			analysisMap = ConverterUtil.convertImageAnalysisObjectToMap(analysisResult);
		}
		
		Map<String, Object> result = new HashMap<String,Object>();
		result.put("imageUUID", imageStore.getImageUUID());
		result.put("imageResultComments", computedImageResultComments);
		result.put("imageResult", computedImageResultCode);
		result.put("detections", analysisMap);
		
		return result;
	}

	@Override
	public void sendAppNotification(InputObject inputObject, String storeVisitStatus, String projectName,
			String resultComment) {
		LOGGER.info("ProcessImageServiceImpl call is from APP: sending FCM notification");
		List<LinkedHashMap<String, String>> resultData;

		JSONObject inputJSON = new JSONObject();
		JSONObject dataJSON = new JSONObject();
		inputJSON.put("type", FirebaseWrapper.NotificationType.IMAGE_ANALYSIS.name());
		dataJSON.put("storeId", inputObject.getStoreId());
		dataJSON.put("placeId", inputObject.getPlaceId());
		dataJSON.put("visitDateId",inputObject.getVisitDate());
		dataJSON.put("taskId",inputObject.getTaskId());
		dataJSON.put("projectName",projectName);
		dataJSON.put("projectStoreVisitStatus",storeVisitStatus);
		dataJSON.put("projectId",inputObject.getProjectId()+"");
		dataJSON.put("resultComment", resultComment);
		inputJSON.put("data", dataJSON);

		resultData = uiService.getUserNotificationTokenByUserId(inputObject.getUserId());

		for (LinkedHashMap<String, String> notificationData: resultData) {
			FirebaseWrapper.sendFirebaseNotification(inputJSON, notificationData.get("fcmToken"));
		}
		LOGGER.info("ProcessImageServiceImpl FCM notification successfully sent");
	}

	private boolean createThumbnail(ImageStore imageStore, boolean isThumbNailProcessingSuccess) {
		try {
			ShellUtil.createThumbnail(imageStore, cloudStorageService.getContainerName(),
					env.getProperty("disk_directory"));
			LOGGER.info("---------------ProcessImageServiceImpl create thumbnail done----------------\n");

			processImageDao.updateOrientationDetails(imageStore);
			LOGGER.info("---------------ProcessImageServiceImpl all orientation details update done----------------\n");
		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl - createThumbnail and Update Orientation failed----------------\n");
			LOGGER.error("---------------ProcessImageServiceImpl - Marking imageStatus as error----------------\n");
			LOGGER.info("ProcessImageServiceImpl createThumbnail Exception: {}", e);
			processImageDao.updateImageAnalysisStatus("error", imageStore.getImageUUID());
			isThumbNailProcessingSuccess = false;
		}
		return isThumbNailProcessingSuccess;
	}

	@Override
	public LinkedHashMap<String, String> getJob(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getJob----------------\n");

		LinkedHashMap<String, String> unProcessedJob = new LinkedHashMap<String, String>();
		String shelfStatus = "new";
		Integer newJobCount = processImageDao.getJobCount(shelfStatus);
		if (newJobCount > 1) {

			ImageStore imageStore = processImageDao.getImageByStatus(shelfStatus);
			shelfStatus = "processing";

			LOGGER.info("got this job imageStore {}", imageStore.toString());

			processImageDao.updateStatusAndHost(inputObject.getHostId(), shelfStatus, imageStore.getImageUUID());
			String storeId = storeMasterDao.getStoreId(imageStore.getLongitude(), imageStore.getLatitude());
			processImageDao.updateStoreId(storeId, imageStore.getImageUUID());
			String retailer = storeMasterDao.getRetailerByStoreId(storeId);

			Integer remainingJob = newJobCount - 1;
			unProcessedJob.put("imageUUID", imageStore.getImageUUID());
			unProcessedJob.put("categoryId", imageStore.getCategoryId());
			unProcessedJob.put("imageFilePath", imageStore.getImageFilePath());
			unProcessedJob.put("latitude", imageStore.getLatitude());
			unProcessedJob.put("longitude", imageStore.getLongitude());
			unProcessedJob.put("storeId", storeId);
			unProcessedJob.put("timeStamp", imageStore.getTimeStamp());
			unProcessedJob.put("userId", imageStore.getUserId());
			unProcessedJob.put("dateId", imageStore.getDateId());
			unProcessedJob.put("retailer", retailer);
			unProcessedJob.put("remainingJob", remainingJob.toString());
		} else {
			unProcessedJob.put("remainingJob", newJobCount.toString());
		}
		LOGGER.info("---------------ProcessImageServiceImpl Ends getJob----------------\n");

		return unProcessedJob;
	}

	@Override
	public LinkedHashMap<String, String> getCronJobCount(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getCronJobCount----------------\n");
		LinkedHashMap<String, String> unProcessedJob = new LinkedHashMap<String, String>();
		Integer newJobCount = processImageDao.getCronJobCount();
		unProcessedJob.put("remainingJob", newJobCount.toString());
		LOGGER.info("---------------ProcessImageServiceImpl Ends getCronJobCount----------------\n");
		return unProcessedJob;
	}

	public List<ImageAnalysis> invokeImageAnalysis(ImageStore imageStore, String retailer, String projectTypeId, boolean isSyncCall) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts invokeImageAnalysis----------------\n");
		LOGGER.info("---------------ProcessImageServiceImpl imageFilePath={},"
				+ ", category={}, uuid={}, retailer={}, store= {},userId= {},projectTypeId= {},isSyncCall= {}",
				imageStore.getImageFilePath(),imageStore.getCategoryId(),imageStore.getImageUUID(),retailer,imageStore.getStoreId(),imageStore.getUserId(), projectTypeId, isSyncCall );
		
		if ( StringUtils.isBlank(retailer) ) retailer = "IND"; //independent retailer.
		
		String result = Constants.PASSTHROUGH_IMAGE_ANALYSIS_RESPONSE;
		
		if ( imageStore.getProjectId() == 1607 ) { //Pepsi live project
			doPepsiPogAnalysis(imageStore); //ignore results for now.
		} else if( !imageStore.getCategoryId().equals(Constants.PASSTHROUGH_CATEGORY_ID) ) {
			result = doImageAnalysis(cloudStorageService.getBucketPath(true) + imageStore.getProjectId() + "/"  + imageStore.getImageUUID() + ".jpg", imageStore.getCategoryId(),
					imageStore.getImageUUID(), retailer, imageStore.getStoreId(), imageStore.getUserId(), projectTypeId, imageStore.getProjectId(), isSyncCall);
		}

		List<ImageAnalysis> imageAnalysisList = new ArrayList<ImageAnalysis>();
		if ( isSyncCall || imageStore.getCategoryId().equals(Constants.PASSTHROUGH_CATEGORY_ID) ) {
			imageAnalysisList = parseImageAnalysisOutput(imageStore, result);
		}
		
		return imageAnalysisList;
	}

	private List<ImageAnalysis> parseImageAnalysisOutput(ImageStore imageStore, String analysisOutput) {
		// Initializing imageAnalysis list to null. If it remains null at the end of execution, it is considered as a failure.
		// If it is empty, it is considered as a successful analysis which resulted in no SKUs.
		// If it has one ore more elements, it is considered as a successful analysis which resulted in identification of one ore more SKUs.
		List<ImageAnalysis> imageAnalysisList = null;
		
		JsonObject resultObject =null;
		try {
			resultObject = new JsonParser().parse(analysisOutput).getAsJsonObject();
		} catch(Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl :: Malformed response from image analysis service : response={}, error={}",analysisOutput,e);
			e.printStackTrace();
			return imageAnalysisList;
		}

		String status = resultObject.get("status").getAsString();
		String message = resultObject.get("message").isJsonNull() ? "null" : resultObject.get("message").getAsString();
		
		LOGGER.error(MARKER_IMG_PROC,"projectId={}, imageUUID={}, status={}, message={}", imageStore.getProjectId(), imageStore.getImageUUID(), status, message);

		if (status.trim().equals("success")) {
			
			String imageAnalysisJSON = resultObject.get("data").toString();

			//Run shelf analysis only for walmart projects -- THIS IS TURNED OFF -- Shelf Level is computed externally now.
			//String shelfAnalysisJSON = retailer.equalsIgnoreCase("WMT") ? computeShelfLevel(imageAnalysisJSON) : imageAnalysisJSON;
			//LOGGER.info("---------------shelfAnalysisJSON={}", shelfAnalysisJSON);

			JsonObject obj = new JsonParser().parse(imageAnalysisJSON).getAsJsonObject();

			imageAnalysisList = unmarshalImageAnalysisData(imageStore, obj);
			
			LOGGER.info("---------------ProcessImageServiceImpl :: Uploading image analysis metadata to cloud storage----------------\n");
			cloudStorageService.storeImageAnalysisMetadata(""+imageStore.getProjectId(), imageStore.getImageUUID(), imageAnalysisJSON);
			LOGGER.info("---------------ProcessImageServiceImpl :: Uploaded image analysis metadata to cloud storage----------------\n");
			
			LOGGER.info("---------------ProcessImageServiceImpl Ends invokeImageAnalysis----------------\n");
			return imageAnalysisList;
		} else {
			return imageAnalysisList;
		}
	}

	private List<ImageAnalysis> unmarshalImageAnalysisData(ImageStore imageStore,  JsonObject obj) {
		
		String lowConfidenceDetectionFound = "0";
		
		List<ImageAnalysis> imageAnalysisList = new ArrayList<ImageAnalysis>();

		JsonArray skusArray = obj.get("skus").getAsJsonArray();
		for (JsonElement skus : skusArray) {

			ImageAnalysis imageAnalysis = new ImageAnalysis();
			JsonObject upcEntry = skus.getAsJsonObject();
			imageAnalysis.setUpc(upcEntry.get("UPC").getAsString().trim());
			imageAnalysis.setLeftTopX(upcEntry.get("LEFT_TOP_X").getAsString().trim());
			imageAnalysis.setLeftTopY(upcEntry.get("LEFT_TOP_Y").getAsString().trim());
			imageAnalysis.setWidth(upcEntry.get("Width").getAsString().trim());
			imageAnalysis.setHeight(upcEntry.get("Height").getAsString().trim());
			imageAnalysis.setUpcConfidence(upcEntry.get("UPC_Confidence").getAsString().trim());
			imageAnalysis.setAlternateUpc(upcEntry.get("Alt_UPC").getAsString().trim());
			imageAnalysis.setAlternateUpcConfidence(upcEntry.get("Alt_UPC_Confidence").getAsString().trim());
			imageAnalysis.setPromotion(upcEntry.get("Promotion").getAsString().trim());
			imageAnalysis.setPrice(upcEntry.get("Price").getAsString().trim());
			imageAnalysis.setPriceLabel(upcEntry.get("Price_Label").getAsString().trim());
			imageAnalysis.setPriceConfidence(upcEntry.get("Price_Confidence").getAsString().trim());
			if (upcEntry.get("Shelflevel") == null) {
				imageAnalysis.setShelfLevel("NA");
			} else {
				imageAnalysis.setShelfLevel(upcEntry.get("Shelflevel").getAsString());
			}
			
			String compliant = upcEntry.get("Compliant") != null ? upcEntry.get("Compliant").getAsString() : "0";
			imageAnalysis.setCompliant(compliant);
			
			String isDuplicate = upcEntry.get("isDuplicate") != null ? upcEntry.get("isDuplicate").getAsString() : "False";
			boolean isDuplicateBool = Boolean.parseBoolean(isDuplicate);
			imageAnalysis.setIsDuplicate(isDuplicateBool ? "1" : "0");

			imageAnalysisList.add(imageAnalysis);
			
			if ( StringUtils.isNotBlank(imageAnalysis.getUpcConfidence())
					&& Float.parseFloat(imageAnalysis.getUpcConfidence()) == 0 ) {
				lowConfidenceDetectionFound = "1";
			}
		}
		String imageRotation = obj.get("imageRotation").getAsString();
		String imageHashScore = obj.get("imageHashScore").getAsString();
		String imageResultComments = obj.get("imageResultComments").getAsString();
		String imageResultCode = obj.get("imageResultCode").getAsString();
		String pixelsPerInch = (obj.get("pixelsPerInch") != null ? obj.get("pixelsPerInch").getAsString() : "0");
		String oosCount = (obj.get("oosCount") != null ? obj.get("oosCount").getAsString() : "0");
		String oosPercentage = (obj.get("oosPercentage") != null ? obj.get("oosPercentage").getAsString() : "0");
		String imageAngle = (obj.get("imageAngle") != null ? obj.get("imageAngle").getAsString() : "0");
		String shelfLevels = (obj.get("shelfLevels") != null ? obj.get("shelfLevels").getAsString() : "0");

		String imageReviewRecommendations = "";
		if ( obj.get("imageReviewRecommendations") != null ) {
			ArrayList recommendations = new Gson().fromJson(obj.get("imageReviewRecommendations").getAsJsonArray(),ArrayList.class);
			imageReviewRecommendations = recommendations.toString().replaceAll("\\[", "").replaceAll("\\]","");
		}

		String imageNotUsable = (obj.get("imageNotUsable") != null ? obj.get("imageNotUsable").getAsString() : "0");
		String imageNotUsableComment = (obj.get("imageNotUsableComment") != null ? obj.get("imageNotUsableComment").getAsString() : "");
		String newWidth = (obj.get("newWidth") != null ? obj.get("newWidth").getAsString() : null);
		String newHeight = (obj.get("newHeight") != null ? obj.get("newHeight").getAsString() : null);
		String origHeight = (obj.get("origHeight") != null ? obj.get("origHeight").getAsString() : null);
		String origWidth = (obj.get("origWidth") != null ? obj.get("origWidth").getAsString() : null);

		imageStore.setImageRotation(imageRotation);
		imageStore.setImageHashScore(imageHashScore);
		imageStore.setImageResultComments(imageResultComments);
		imageStore.setImageResultCode(imageResultCode);
		imageStore.setPixelsPerInch(pixelsPerInch);
		imageStore.setOosCount(oosCount);
		imageStore.setOosPercentage(oosPercentage);
		imageStore.setImageAngle(imageAngle);
		imageStore.setShelfLevels(shelfLevels);
		imageStore.setImageReviewRecommendations(imageReviewRecommendations);
		imageStore.setImageNotUsable(imageNotUsable);
		imageStore.setImageNotUsableComment(imageNotUsableComment);
		imageStore.setLowConfidence(lowConfidenceDetectionFound);
		imageStore.setNewHeight(newHeight);
		imageStore.setNewWidth(newWidth);
		imageStore.setOrigHeight(origHeight);
		imageStore.setOrigWidth(origWidth);
		
		return imageAnalysisList;
	}
	
	private String doImageAnalysis(String imageFilePath, String categoryId, String imageUUID, String retailer, String storeId, String userId, String projectTypeId, int projectId, boolean isSyncCall) {
		String analysisResponse = "";

		String imageAnalysisURI = env.getProperty("image_analysis_url_"+env.getProperty("instance"));
		String imageAnalysisSyncURI = env.getProperty("image_analysis_sync_url_"+env.getProperty("instance"));
		
		if ( new File(IMAGE_ANALYSIS_RUNTIME_CONFIGURATION_FILE).exists() ) {
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(IMAGE_ANALYSIS_RUNTIME_CONFIGURATION_FILE));
			} catch (Exception e) {
				LOGGER.error("---------------ProcessImageServiceImpl::doImageAnalysis::Unable to load imageAnalysis configuration properties file. Using default values.");
			}
			imageAnalysisURI = StringUtils.isNotBlank(prop.getProperty("image_analysis_url")) ? prop.getProperty("image_analysis_url") : imageAnalysisURI;
			imageAnalysisSyncURI = StringUtils.isNotBlank(prop.getProperty("image_analysis_sync_url")) ? prop.getProperty("image_analysis_sync_url") : imageAnalysisSyncURI;
		}

		final RequestConfig requestConfig = RequestConfig.custom()
		        .setConnectTimeout(5000)
		        .setConnectionRequestTimeout(5000)
		        .setSocketTimeout(300000) //5 minutes wait for getting back response.
		        .build();
		
		CloseableHttpClient client = HttpClientBuilder.create().disableAuthCaching()
		        .disableAutomaticRetries()
		        .disableConnectionState()
		        .disableContentCompression()
		        .disableCookieManagement()
		        .disableRedirectHandling()
		        .setDefaultRequestConfig(requestConfig)
		        .build();
		
		String targetURL = isSyncCall ? imageAnalysisSyncURI.trim() : imageAnalysisURI.trim();
		LOGGER.debug("---------------ProcessImageServiceImpl::doImageAnalysis::Pipeline server URL = {}", targetURL);
		
		HttpPost httpPost = new HttpPost(targetURL);
		CloseableHttpResponse response = null;
		HttpEntity httpEntity = null;
		StringEntity entity = null;

		JSONObject inputObject = new JSONObject();
		inputObject.put("uuid", imageUUID);
		inputObject.put("image_url", imageFilePath);
		inputObject.put("category", categoryId);
		inputObject.put("project_type_id", projectTypeId);
		inputObject.put("retailer_code", retailer);
		inputObject.put("store_id",storeId);
		inputObject.put("user_id",userId);

		try {
			entity = new StringEntity(inputObject.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			response = client.execute(httpPost);
			
			httpEntity = response.getEntity();
			analysisResponse = EntityUtils.toString(httpEntity);

			if (response.getStatusLine().getStatusCode() == 200) {
				LOGGER.info("---------------ProcessImageServiceImpl submitted imageUUID {} for processing----------------\n", imageUUID);
			} else {
				LOGGER.error("---------------ProcessImageServiceImpl failed to submit imageUUID {} for processing----------------\n", imageUUID);
				LOGGER.error(MARKER_IMG_PROC,"projectId={}, imageUUID={}, status={}, message={}", projectId, imageUUID, "error", analysisResponse);
			}
		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl gets exception in Image Analysis service : {}", e);
			e.printStackTrace();
			LOGGER.error(MARKER_IMG_PROC,"projectId={}, imageUUID={}, status={}, message={}", projectId, imageUUID, "error", e.getMessage());
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("---------------ProcessImageServiceImpl gets exception in closing http client for Image Analysis service : {}", e);
			}
		}
		LOGGER.info("---------------ProcessImageServiceImpl ends doImageAnalysis :: response = {}----------------\n", analysisResponse);

		return analysisResponse;
	}
	
	private void doPepsiPogAnalysis(ImageStore imageStore) {
		String imageURL = cloudStorageService.getBucketPath(true) + imageStore.getProjectId() + "/"  + imageStore.getImageUUID() + ".jpg";
		Integer planogramId = 8; //GON_1
		String analysisResponse = "";
		final RequestConfig requestConfig = RequestConfig.custom()
		        .setConnectTimeout(5000)
		        .setConnectionRequestTimeout(5000)
		        .setSocketTimeout(300000) //5 minutes wait for getting back response.
		        .build();
		
		CloseableHttpClient client = HttpClientBuilder.create().disableAuthCaching()
		        .disableAutomaticRetries()
		        .disableConnectionState()
		        .disableContentCompression()
		        .disableCookieManagement()
		        .disableRedirectHandling()
		        .setDefaultRequestConfig(requestConfig)
		        .build();
		
		HttpPost httpPost = new HttpPost("http://dev-gpu02.snap2insight.com:8887/predict/");
		CloseableHttpResponse response = null;
		HttpEntity httpEntity = null;
		StringEntity entity = null;

		JsonObject inputObject = new JsonObject();
		inputObject.addProperty("uuid", imageStore.getImageUUID());
		inputObject.addProperty("image_url", imageURL);
		inputObject.addProperty("planogram_id", planogramId);
		inputObject.addProperty("planogram_url", "https://snap2insightdev.blob.core.windows.net/livedemo/planograms/" + planogramId + ".json");
		
		LOGGER.info("Pepsi - Invoking Pipeline server with payload = {}", inputObject.toString());

		try {
			entity = new StringEntity(inputObject.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			response = client.execute(httpPost);
			
			httpEntity = response.getEntity();
			analysisResponse = EntityUtils.toString(httpEntity);

			if (response.getStatusLine().getStatusCode() == 200) {
				LOGGER.info("Pepsi - Image Analysis Success for imageUUID = {}",imageStore.getImageUUID());
				
				BufferedWriter writer = new BufferedWriter(new FileWriter("/usr/share/s2i/images/pepsi/"+imageStore.getProjectId()+"_"+imageStore.getStoreId()+"_"+imageStore.getTaskId()+".json"));
			    writer.write(analysisResponse);
			     
			    writer.close();
				
			} else {
				LOGGER.error("Pepsi - Image Analysis Failed for imageUUID = {} , Response = {}", imageStore.getImageUUID(), analysisResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Pepsi - Exception while processing image " + imageStore.getImageUUID(), e);
		} finally {
			try {
				if ( response != null ) {
					response.close();
				}
				client.close();
			} catch (IOException e) {
				LOGGER.error("Error while closing image analysis request", e);
			}
		}
	}

	private void runImageAnalysis(String imageUUID) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts runImageAnalysis----------------\n");

		ImageStore imageStore = processImageDao.findByImageUUId(imageUUID);
		
		LOGGER.info("--------------runImageAnalysis::imageStore={}", imageStore );

		if (imageStore == null) {
			LOGGER.info("--------------runImageAnalysis:: Image with UUID {} doesn't exist", imageUUID );
		} else {
			
			String imageStatus = "";
			if (imageStore.getImageStatus().equalsIgnoreCase("cron")) { //logic to stop infinite retries, only 2 retries after first attempt failed.
				imageStatus = "processing";
			} else if (imageStore.getImageStatus().equalsIgnoreCase("cron1")) {
				imageStatus = "processing1";
			} else if (imageStore.getImageStatus().equalsIgnoreCase("cron2")) {
				imageStatus = "processing2";
			}
            
			processImageDao.updateImageAnalysisStatus(imageStatus, imageUUID);
			
			String retailer = storeMasterDao.getRetailerByStoreId(imageStore.getStoreId());
			LOGGER.info("--------------runImageAnalysis::retailer={}", retailer );

			List<LinkedHashMap<String, String>> project = metaServiceDao.getProjectDetail(imageStore.getProjectId());
			String projectTypeId = project.get(0).get("projectTypeId");
			LOGGER.info("--------------runImageAnalysis::projectTypeId={}", projectTypeId );
			
			String description = project.get(0).get("description");
			if ( StringUtils.isNotBlank(description) && description.trim().startsWith("PASS") ) {
				imageStore.setCategoryId(Constants.PASSTHROUGH_CATEGORY_ID); //override category id with passthrough category id, just for GPU call.
				LOGGER.info("--------------runImageAnalysis:: setting passthrough category id because project is passthroug, imageUUID = {}", imageStore.getImageUUID());
			}
			
			String questionId = imageStore.getQuestionId();
			if ( StringUtils.isNotBlank(questionId) ) {
				List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(imageStore.getProjectId());
				for( ProjectQuestion question : projectQuestions ) {
					if ( questionId.equalsIgnoreCase(question.getId()) && question.getSkipImageAnalysis().equals("1") ) {
						imageStore.setCategoryId(Constants.PASSTHROUGH_CATEGORY_ID); //override category id with pass through category id, just for GPU call.
						LOGGER.info("--------------runImageAnalysis:: setting passthrough category id because image should be skipped from analysis, imageUUID = {}", imageStore.getImageUUID());
						break;
					}
				}
			}

			boolean syncCall = false;

			//Send image for processing and return without waiting, results are pushed back later. Passthru category is exempted.
			List<ImageAnalysis> detections = invokeImageAnalysis(imageStore, retailer, projectTypeId, syncCall);
			
			if (imageStore.getCategoryId().equals(Constants.PASSTHROUGH_CATEGORY_ID)) {
				boolean updateImageStatus=true;
				saveAndPostProcessImageAnalysisData(imageUUID, imageStore, detections,updateImageStatus);
			}

			LOGGER.info("---------------ProcessImageServiceImpl Ends runImageAnalysis ----------------\n");
		}
	}

	private void saveAndPostProcessImageAnalysisData(String imageUUID, ImageStore imageStore,
			List<ImageAnalysis> imageAnalysisList, boolean updateImageStatus) {

		processImageDao.storeImageAnalysis(imageAnalysisList, imageStore);
		LOGGER.info("--------------saveAndPostProcessImageAnalysisData::storeImageAnalysis done-----------------\n");
		
		if ( updateImageStatus ) {
			String imageStatus = "done";
			processImageDao.updateImageAnalysisStatus(imageStatus, imageUUID);
			processImageDao.setStoreVisitForAggregation(imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId() );
			LOGGER.info("--------------saveAndPostProcessImageAnalysisData::Marked this store visit for (re)aggregation-----------------\n");
		}
		
		
		//Make request to create thumbnail/preview
		submitImageToCompressor(imageUUID,imageStore,imageAnalysisList);

		/*boolean isThumbNailProcessingSuccess = true;
		try {
			ShellUtil.createThumbnail(imageStore, cloudStorageService.getContainerName(), env.getProperty("disk_directory"));
			LOGGER.info("---------------ProcessImageServiceImpl create thumbnail done----------------\n");

			processImageDao.updateOrientationDetails(imageStore);
			LOGGER.info("---------------ProcessImageServiceImpl all orientation details update done----------------\n");
		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl - createThumbNail and Update Orientation failed----------------\n");
			LOGGER.error("---------------ProcessImageServiceImpl - Marking imageStatus as error----------------\n");
			processImageDao.updateImageAnalysisStatus("error", imageStore.getImageUUID());
			isThumbNailProcessingSuccess = false;
		}

		if (isThumbNailProcessingSuccess) {
			String imageStatus = "done";
			processImageDao.updateImageAnalysisStatus(imageStatus, imageUUID);
			LOGGER.info("--------------saveAndPostProcessImageAnalysisData::updateStatus={}", imageStatus );
		}*/
		
		// Compute imageResultCode only if GPU call didn't return one.
		if (StringUtils.isBlank(imageStore.getImageResultCode())
				&& StringUtils.isNotBlank(imageStore.getQuestionId())
				&& StringUtils.isNotBlank(imageStore.getFileId())) {

			Map<String, String> resultMap = processImageDao.generateImageResult(imageStore, imageAnalysisList);
			ImageResultCode resultCode = ImageResultCode
					.getImageResultCodeFromCode(resultMap.get("resultCode"));
			String resultComment = (String) resultMap.get("resultComment");
			String objectiveResultStatus = (String) resultMap.get("objectiveResultStatus");

			String imageReviewStatus = "1";
			if (resultCode == ImageResultCode.REJECT_LEVEL_1 || resultCode == ImageResultCode.REJECT_LEVEL_3
					|| resultCode == ImageResultCode.APPROVED_PENDING_REVIEW
					|| resultCode == ImageResultCode.REJECT_INSUFFICIENT
					|| resultCode == ImageResultCode.UNAPPROVED) {
				imageReviewStatus = "0"; // for review
			}
			processImageDao.updateImageResultCodeAndStatus(imageStore.getImageUUID(), resultCode.getCode(),
					resultComment, imageReviewStatus, objectiveResultStatus);
		}
	}
	
	private void submitImageToCompressor(String imageUUID, ImageStore imageStore, List<ImageAnalysis> imageAnalysisList) {
		LOGGER.info("---------------ProcessImageServiceImpl starts submitImageToCompressor ----------------\n");
		
		String url = env.getProperty("compressor_url");
		String environment = env.getProperty("instance");
		String token = env.getProperty("compressor_token");
		
		Map<String,Object> inputMap = new LinkedHashMap<String,Object>();
		inputMap.put("env", environment);
		inputMap.put("projectId",  imageStore.getProjectId());
		inputMap.put("imageUUID", imageUUID);
		inputMap.put("imageRotation", Integer.parseInt(ConverterUtil.ifNullToZero(imageStore.getImageRotation())));
		
		List<Map<String,Integer>> blurCoordinates = new ArrayList<Map<String,Integer>>();
		for(ImageAnalysis oneDetection : imageAnalysisList) {
			if ( oneDetection.getUpc().equals(Constants.PERSON_FACE_UPC) ) {
				Map<String,Integer> oneBlurDetection = new HashMap<String,Integer>();
				oneBlurDetection.put("leftTopX",Integer.parseInt(oneDetection.getLeftTopX()));
				oneBlurDetection.put("leftTopY",Integer.parseInt(oneDetection.getLeftTopY()));
				oneBlurDetection.put("width",Integer.parseInt(oneDetection.getWidth()));
				oneBlurDetection.put("height",Integer.parseInt(oneDetection.getHeight()));
				blurCoordinates.add(oneBlurDetection);
			}
		}
		inputMap.put("blurCoordinates", blurCoordinates);
		
		Gson gson = new Gson();
		String inputJSON = gson.toJson(inputMap);
		LOGGER.info("---------------ProcessImageServiceImpl::submitImageToCompressor::payload={}",inputJSON); 

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		StringEntity entity = null;

		try {
			entity = new StringEntity(inputJSON);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("x-auth-token",token);
			
			response = client.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				LOGGER.info("---------------ProcessImageServiceImpl submitImageToCompressor Response : {}",response);
			} else {
				LOGGER.error("---------------ERROR: ProcessImageServiceImpl ends submitImageToCompressor: Response not equal 200 {}", response );
			}
			LOGGER.info("---------------ProcessImageServiceImpl ends submitImageToCompressor----------------\n");
		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl gets exception in submitImageToCompressor : {}", e );
			LOGGER.info("--------------- EXCEPTION ProcessImageServiceImpl submitImageToCompressor----------------\n");
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<LinkedHashMap<String, String>> getImageAnalysis(String imageUUID) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getImageAnalysis----------------\n");
		List<ImageAnalysis> imageAnalysisList = processImageDao.getImageAnalysis(imageUUID);
		LOGGER.info("---------------ProcessImageServiceImpl Ends getImageAnalysis ----------------\n");
		return ConverterUtil.convertImageAnalysisObjectToMap(imageAnalysisList);
	}

	@Override
	public List<LinkedHashMap<String, String>> getStoreOptions(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getStoreOptions----------------\n");

		List<LinkedHashMap<String, String>> storeMasterList = storeMasterDao
				.getStoreOptions(inputObject.getRetailerCode());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getStoreOptions ----------------\n");
		return storeMasterList;
	}

	@Override
	public List<LinkedHashMap<String, String>> getImages(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getImages----------------\n");

		List<LinkedHashMap<String, String>> imageStoreList = processImageDao.getImages(inputObject.getStoreId(),
				inputObject.getVisitDate());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getImages ----------------\n");
		return imageStoreList;
	}

	@Override
	public List<LinkedHashMap<String, String>> getProjectStoreImages(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoreImages----------------\n");

		List<LinkedHashMap<String, String>> imageStoreList = processImageDao.getProjectStoreImages(inputObject.getProjectId(), inputObject.getStoreId(),inputObject.getTaskId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoreImages ----------------\n");
		return imageStoreList;
	}

	@Override
	public List<LinkedHashMap<String, String>> getStores(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getStores----------------\n");

		List<LinkedHashMap<String, String>> storeMasterList = storeMasterDao
				.getStores(inputObject.getRetailerChainCode(), inputObject.getStateCode(), inputObject.getCity());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getStores ----------------\n");
		return storeMasterList;
	}

	@Override
	public List<LinkedHashMap<String, String>> getImageMetaData(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getImageMetaData----------------\n");
		List<ImageStore> imageStoreList = new ArrayList<ImageStore>();
		ImageStore imageStore = processImageDao.findByImageUUId(inputObject.getImageUUID());
		imageStoreList.add(imageStore);
		LOGGER.info("---------------ProcessImageServiceImpl Ends getImageMetaData ----------------\n");
		return ConverterUtil.convertImageStoreObjectToMap(imageStoreList);
	}

	@Override
	public List<LinkedHashMap<String, String>> doDistributionCheck(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts doDistributionCheck----------------\n");
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();

		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		String imageStatus = processImageDao.getImageAnalysisStatus(inputObject.getPrevImageUUID());
		if (imageStatus.equalsIgnoreCase("done")) {
			map = processImageDao.getFacing(inputObject.getImageUUID());
		} else {
			getImageAnalysis(inputObject.getPrevImageUUID());
			map = processImageDao.getFacing(inputObject.getImageUUID());
		}

		Set<String> keySet = map.keySet();
		List<UpcFacingDetail> listDistributionList = productMasterDao.getUpcForList(inputObject.getListId());

		for (UpcFacingDetail unit : listDistributionList) {
			LinkedHashMap<String, String> entry = new LinkedHashMap<String, String>();
			if (keySet.contains(unit.getUpc())) {
				UpcFacingDetail upcFacingDetail1 = (UpcFacingDetail) map.get(unit.getUpc());
				entry.put("upc", unit.getUpc());
				entry.put("productLongName", unit.getProductLongName());
				entry.put("productShortName", unit.getProductShortName());
				entry.put("brandName", unit.getBrandName());
				entry.put("facing", upcFacingDetail1.getCount());
				entry.put("osa", "1");
			} else {
				entry.put("upc", unit.getUpc());
				entry.put("productLongName", unit.getProductLongName());
				entry.put("productShortName", unit.getProductShortName());
				entry.put("brandName", unit.getBrandName());
				entry.put("facing", "0");
				entry.put("osa", "0");
			}
			result.add(entry);
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends doDistributionCheck ----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> doBeforeAfterCheck(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts doBeforeAfterCheck----------------\n");
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
		LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> map2 = new LinkedHashMap<String, Object>();
		String prevImageStatus = processImageDao.getImageAnalysisStatus(inputObject.getPrevImageUUID());
		if (prevImageStatus.equalsIgnoreCase("done")) {
			map1 = processImageDao.getFacing(inputObject.getPrevImageUUID());
		} else {
			getImageAnalysis(inputObject.getPrevImageUUID());
			map1 = processImageDao.getFacing(inputObject.getPrevImageUUID());
		}

		String imageStatus = processImageDao.getImageAnalysisStatus(inputObject.getImageUUID());
		if (imageStatus.equalsIgnoreCase("done")) {
			map2 = processImageDao.getFacing(inputObject.getImageUUID());
		} else {
			getImageAnalysis(inputObject.getImageUUID());
			map2 = processImageDao.getFacing(inputObject.getImageUUID());
		}

		Set<String> keySet1 = map1.keySet();
		Set<String> keySet2 = map2.keySet();

		Set<String> union = new HashSet<String>(keySet1);
		union.addAll(keySet2);

		Set<String> intersection = new HashSet<String>(keySet1);
		intersection.retainAll(keySet2);

		for (String unit : union) {
			LinkedHashMap<String, String> entry = new LinkedHashMap<String, String>();
			if (intersection.contains(unit)) {
				UpcFacingDetail upcFacingDetail1 = (UpcFacingDetail) map1.get(unit);
				LOGGER.info("---------------ProcessImageServiceImpl {}", upcFacingDetail1.toString() );
				UpcFacingDetail upcFacingDetail2 = (UpcFacingDetail) map2.get(unit);
				LOGGER.info("---------------ProcessImageServiceImpl {}", upcFacingDetail2.toString());
				entry.put("upc", unit);
				entry.put("productLongName", upcFacingDetail1.getProductLongName());
				entry.put("productShortName", upcFacingDetail1.getProductShortName());
				entry.put("brandName", upcFacingDetail1.getBrandName());
				entry.put("before_facing", upcFacingDetail1.getCount());
				entry.put("before_osa", "1");
				entry.put("after_facing", upcFacingDetail2.getCount());
				entry.put("after_osa", "1");
			} else if (keySet1.contains(unit)) {
				UpcFacingDetail upcFacingDetail1 = (UpcFacingDetail) map1.get(unit);
				LOGGER.info("---------------ProcessImageServiceImpl {}", upcFacingDetail1.toString() );

				entry.put("upc", unit);
				entry.put("productLongName", upcFacingDetail1.getProductLongName());
				entry.put("productShortName", upcFacingDetail1.getProductShortName());
				entry.put("brandName", upcFacingDetail1.getBrandName());
				entry.put("before_facing", upcFacingDetail1.getCount());
				entry.put("before_osa", "1");
				entry.put("after_facing", "0");
				entry.put("after_osa", "0");
			} else if (keySet2.contains(unit)) {
				UpcFacingDetail upcFacingDetail2 = (UpcFacingDetail) map2.get(unit);
				LOGGER.info("---------------ProcessImageServiceImpl {}", upcFacingDetail2.toString());
				entry.put("upc", unit);
				entry.put("productLongName", upcFacingDetail2.getProductLongName());
				entry.put("productShortName", upcFacingDetail2.getProductShortName());
				entry.put("brandName", upcFacingDetail2.getBrandName());
				entry.put("before_facing", "0");
				entry.put("before_osa", "0");
				entry.put("after_facing", upcFacingDetail2.getCount());
				entry.put("after_osa", "1");
			}
			result.add(entry);
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends doBeforeAfterCheck ----------------\n");
		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> doShareOfShelfAnalysis(InputObject inputObject) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts doShareOfShelfAnalysis::{}", inputObject.getImageUUIDCsvString() );

		for (String imageUUID : inputObject.getImageUUIDCsvString().split(",")) {
			String status = processImageDao.getImageAnalysisStatus(imageUUID);
			if (!status.equalsIgnoreCase("done")) {
				getImageAnalysis(imageUUID);
			}
		}

		List<LinkedHashMap<String, String>> imageAnalysisList = processImageDao
				.doShareOfShelfAnalysis(inputObject.getImageUUIDCsvString());

		LOGGER.info("---------------ProcessImageServiceImpl Ends doShareOfShelfAnalysis ----------------\n");

		return imageAnalysisList;

	}

	@Override
	public File doShareOfShelfAnalysisCsv(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts doShareOfShelfAnalysisCsv----------------\n");

		List<LinkedHashMap<String, String>> imageAnalysisList = processImageDao
				.doShareOfShelfAnalysis(inputObject.getImageUUIDCsvString());

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			String input = "Input:" + "\n";
			String info1 = "retailer" + "," + inputObject.getRetailer() + "\n";
			String info2 = "state" + "," + inputObject.getState() + "\n";
			String info3 = "city" + "," + inputObject.getCity() + "\n";
			String info4 = "street" + "," + inputObject.getStreet() + "\n";
			String line = " " + "," + " " + "\n";
			Long totalCount = 0L;

			LinkedHashMap<String, Long> summeryMetric = new LinkedHashMap<String, Long>();
			for (LinkedHashMap<String, String> row : imageAnalysisList) {
				String brand = row.get("brandName");
				Long updateCount = 0L;
				if (summeryMetric.keySet().contains(brand)) {
					Long brandCount = summeryMetric.get(brand);
					updateCount = brandCount + Long.valueOf(row.get("facing"));
					totalCount = totalCount + Long.valueOf(row.get("facing"));
				} else {
					updateCount = Long.valueOf(row.get("facing"));
					totalCount = totalCount + Long.valueOf(row.get("facing"));
				}
				summeryMetric.put(brand, updateCount);
			}
			String summary = "Summary:" + "\n";
			String info6 = "BrandName" + "," + "Percentage" + "\n";
			StringBuilder info7 = new StringBuilder();
			for (String x : summeryMetric.keySet()) {
				Double percentage = (summeryMetric.get(x).doubleValue() / totalCount) * 100;
				info7.append(x + "," + String.format("%.2f", percentage) + "\n");
			}

			String meta = input + info1 + info2 + info3 + info4 + line + summary + info6 + info7.toString() + line;
			fileWriter.append(meta);

			String headers = "UPC,Facing,Product Short Name,Product Long Name,Brand Name" + "\n";
			fileWriter.append(headers);

			for (LinkedHashMap<String, String> row : imageAnalysisList) {
				StringBuilder shareOfShelfAnalysisRow = new StringBuilder();
				shareOfShelfAnalysisRow.append(row.get("upc") + ",");
				shareOfShelfAnalysisRow.append(row.get("facing") + ",");
				shareOfShelfAnalysisRow.append(row.get("productShortName") + ",");
				shareOfShelfAnalysisRow.append(row.get("productLongName") + ",");
				shareOfShelfAnalysisRow.append(row.get("brandName"));

				fileWriter.append(shareOfShelfAnalysisRow.toString() + "\n");
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("---------------ProcessImageServiceImpl Ends doShareOfShelfAnalysisCsv----------------\n");
		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void updateLatLong(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateLatLong----------------\n");

		processImageDao.updateLatLong(inputObject.getImageUUID(), inputObject.getLatitude(),
				inputObject.getLongitude());

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateLatLong ----------------\n");
	}

	@Override
	public List<LinkedHashMap<String, String>> generateAggs(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts generateAggs----------------\n");

		List<LinkedHashMap<String, String>> result = null;
		try {
			result = processImageDao.generateAggs(inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends generateAggs----------------\n");

		return result;
	}

	@Override
	public List<ProjectStoreResultWithUPC> getProjectStoreResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoreResults----------------\n");
		
		List<String> distributionUpcList = new ArrayList<String>();
		boolean callFromAppForDistProject = 
				inputObject.getSource().equals("app") && metaServiceDao.getProjectDetail(inputObject.getProjectId()).get(0).get("projectTypeId").equals("5");
		if ( callFromAppForDistProject ) {
			distributionUpcList = metaServiceDao.getStoreDistributionUPCs(""+inputObject.getProjectId(), inputObject.getStoreId());
		}
		
		Map<String, List<Map<String, String>>> storeImageData = processImageDao.getProjectStoreData(inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId());
		
		List<Map<String, String>> projectUpcs = metaServiceDao.getProjectUpcDetailWithMetaInfo(inputObject.getProjectId());
		List<String> foundSkuTypes = new ArrayList<String>();
		List<SkuType> skuTypes = new ArrayList<SkuType>();
		for( Map<String,String> upc : projectUpcs ) {
			String skuTypeId = upc.get("skuTypeId");
			String skuTypeName = upc.get("skuTypeName");
			if ( !skuTypeId.equals("99") && !foundSkuTypes.contains(skuTypeId) ) {
				skuTypes.add(new SkuType(skuTypeId,skuTypeName,null,null));
				foundSkuTypes.add(skuTypeId);
			}
		}

		String month = inputObject.getMonth();
		// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
		// this logic won't work by 2100 :)
		if (month.equals("-9")) {
			month = null;
		} else {
			String[] parts = month.split("/");
			month = "20" + parts[1] + parts[0];
		}

		List<LinkedHashMap<String, String>> resultData = processImageDao.getProjectStoreResults(inputObject.getProjectId(),
				inputObject.getStoreId(), month, inputObject.getTaskId());

		Map<String, List<Map<String, String>>> storeImageMetaData = processImageDao.getProjectStoreImageWithDetectionsMetaData(
				inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId());

		List<ProjectStoreResultWithUPC> result = ConverterUtil.convertProjectDataResultToObject(storeImageData,
				resultData, storeImageMetaData, skuTypes);
		
		for(ProjectStoreResultWithUPC oneStoreVisit : result ) {
			String projectId = oneStoreVisit.getProjectId();
			String storeId = oneStoreVisit.getStoreId();
			String taskId = oneStoreVisit.getTaskId();
			oneStoreVisit.setScores(scoreDao.getProjectStoreScores(Integer.parseInt(projectId), storeId, taskId));
			oneStoreVisit.setKeyMetrics(scoreDao.getKeyMetricsByStoreVisit(Integer.parseInt(projectId), storeId, taskId));
			
			if ( callFromAppForDistProject ) {
				List<StoreUPC> allUPCs = oneStoreVisit.getProjectUPCs();
				List<StoreUPC> distributionUPCs = new ArrayList<StoreUPC>();
				for( StoreUPC upc : allUPCs) {
					String pUpc = upc.getUpc();
					if ( distributionUpcList.contains(pUpc) ) {
						distributionUPCs.add(upc);
					}
				}
				oneStoreVisit.setProjectUPCs(distributionUPCs);
			}
			
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoreResults----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getProjectTopStores(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectTopStores----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.getProjectTopStores(inputObject.getProjectId(), inputObject.getLimit());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectTopStores----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getProjectBottomStores(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBottomStores----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.getProjectBottomStores(inputObject.getProjectId(), inputObject.getLimit());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBottomStores----------------\n");

		return result;
	}

	@Override
	public List<StoreWithImages> getProjectStoresWithNoUPCs(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoresWithNoUPCs----------------\n");

		List<StoreWithImages> result = processImageDao.getProjectStoresWithNoUPCs(inputObject.getProjectId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoresWithNoUPCs----------------\n");

		return result;
	}

	@Override
	public List<StoreWithImages> getProjectAllStoreImages(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreImages----------------\n");

		List<StoreWithImages> result = processImageDao.getProjectAllStoreImages(inputObject.getProjectId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreImages----------------\n");

		return result;
	}

	@Override
	public List<DuplicateImages> getProjectStoresWithDuplicateImages(InputObject inputObject) {
		LOGGER.info(
				"---------------ProcessImageServiceImpl Starts getProjectStoresWithDuplicateImages----------------\n");

		List<DuplicateImages> result = processImageDao.getProjectStoresWithDuplicateImages(inputObject.getProjectId());

		LOGGER.info(
				"---------------ProcessImageServiceImpl Ends getProjectStoresWithDuplicateImages----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> generateStoreVisitResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts generateStoreVisitResults----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.generateStoreVisitResults(
				inputObject.getProjectId(), inputObject.getStoreId(),inputObject.getTaskId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends generateStoreVisitResults----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> getProjectAllStoreResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreResults----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.getProjectAllStoreResults(inputObject.getProjectId(), inputObject.getLevel(), inputObject.getValue());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreResults----------------\n");

		return result;
	}

	@Override
	public File getProjectAllStoreResultsCsv(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreResultsCsv----------------\n");

		List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());
		List<LinkedHashMap<String, String>> resultList = processImageDao.getProjectAllStoreResults(inputObject.getProjectId(), null, null);

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			String input = "Input:" + "\n";
			String info1 = "projectId" + "," + inputObject.getProjectId() + "\n";
			String line = " " + "," + " " + "\n";
			fileWriter.append(input + info1 + line);

			String headers = "Retailer Store Id,Retailer,Street,City,State Code,Zip,Agent Id,Task Id,Store Visit Date,Processed Date,Result,Photos";
			if (projectQuestions != null && !projectQuestions.isEmpty()) {
				for (ProjectQuestion question : projectQuestions) {
					headers = headers.concat("," + question.getDesc());
				}
			}
			headers = headers.concat("\n");

			fileWriter.append(headers);

			for (LinkedHashMap<String, String> row : resultList) {
				if (row.get("status").equalsIgnoreCase("1")) {
					StringBuilder result = new StringBuilder();
					result.append(row.get("retailerStoreId") + ",");
					result.append(row.get("retailer") + ",");
					result.append(row.get("street").replace(",", " ") + ",");
					result.append(row.get("city") + ",");
					result.append(row.get("stateCode") + ",");
					result.append(row.get("zip") + ",");
					result.append(row.get("agentId") + ",");
					result.append(row.get("taskId") + ",");
					result.append(row.get("visitDate") + ",");
					result.append(row.get("processedDate") + ",");
					result.append(row.get("result") + ",");
					result.append(row.get("imageURL"));
					if (projectQuestions != null && !projectQuestions.isEmpty()) {
						Map<String, String> repResponses = processImageDao.getRepResponsesByStoreVisit(
								inputObject.getProjectId(), row.get("storeId"),
								row.get("taskId"));
						for (ProjectQuestion question : projectQuestions) {
							String response = repResponses.get(question.getId());
							if (response == null)
								response = "";
							result.append(",\"" + response + "\"");
						}
					}

					fileWriter.append(result.toString() + "\n");
				}
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreResultsCsv----------------\n");
		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public File getProjectAllStoreResultsDetailCsv(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreResultsDetailCsv----------------\n");

		List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());

		List<LinkedHashMap<String, String>> resultList = processImageDao.getProjectAllStoreResultsDetail(inputObject.getProjectId());

		List<Map<String, String>> projectUpcList = metaServiceDao.getProjectUpcDetailWithMetaInfo(inputObject.getProjectId());
		
		List<ProjectScoreDefinition> scoreDefinitions = metaServiceDao.getProjectScoreDefinition(inputObject.getProjectId());
		
		Map<String, Map<String, String>> storeVisitScores = scoreDao.getScoresForAllStoreVisits(inputObject.getProjectId());
		
		Map<String,Map<String,String>> storeVisitKeyMetrics = scoreDao.getKeyMetricsForAllStoreVisits(inputObject.getProjectId());

		
		//Filter out internal use SKUs
		String skuTypeKey = "skuTypeId";
		String internalSkuType = "99";
		projectUpcList = projectUpcList.stream().
				filter(map -> !map.get(skuTypeKey).equals(internalSkuType)).collect(Collectors.toList());

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);

			String headers = "Retailer Store Id,Retailer,Street,City,State Code,Zip,Agent Id,Task Id,Store Visit Date,Processed Date,Wave Name,Result,Result Comment,Photos,Number of distinct SKUs,Total Facings";

			List<String> keyMetricsNameList  = new ArrayList<String>();
			if ( scoreDefinitions != null && !scoreDefinitions.isEmpty() ) {
				for (ProjectScoreDefinition scoreDef : scoreDefinitions) {
					headers = headers.concat("," + scoreDef.getScoreName());
					for ( ProjectComponentScoreDefinition componentScoreDef : scoreDef.getComponentScores() ) {
						for ( ProjectComponentCriteriaScoreDefinition criteriaScoreDef : componentScoreDef.getComponentCriteriaScores() ) {
							if ( !criteriaScoreDef.getFocusCriteria().equals("0") ) {
								keyMetricsNameList.add(scoreDef.getScoreName() + " - " + criteriaScoreDef.getCriteriaDesc());
							}
						}
					}
				}
			}
			
			if (keyMetricsNameList != null && !keyMetricsNameList.isEmpty()) {
				for (String keyMetricName : keyMetricsNameList) {
					headers = headers.concat("," + keyMetricName );
				}
			}
			
			for (Map<String, String> upc : projectUpcList) {
				headers = headers.concat("," + upc.get("productName"));
			}

			if (projectQuestions != null && !projectQuestions.isEmpty()) {
				for (ProjectQuestion question : projectQuestions) {
					headers = headers.concat("," + question.getDesc());
				}
			}

			headers = headers.concat("\n");

			fileWriter.append(headers);

			for (LinkedHashMap<String, String> row : resultList) {
				StringBuilder result = new StringBuilder();
				result.append(row.get("retailerStoreId") + ",");
				result.append(row.get("retailer") + ",");
				result.append(row.get("street").replace(",", " ") + ",");
				result.append(row.get("city") + ",");
				result.append(row.get("stateCode") + ",");
				result.append(row.get("zip") + ",");
				result.append(row.get("agentId") + ",");
				result.append(row.get("taskId") + ",");
				result.append(row.get("visitDate") + ",");
				result.append(row.get("processedDate") + ",");
				result.append(row.get("waveName") + ",");
				result.append(row.get("result") + ",");
				result.append("\""+row.get("resultComment")+"\"" + ",");
				result.append(row.get("imageURL") + ",");
				result.append(row.get("countDistinctUpc") + ",");
				result.append(row.get("sumFacing"));
				

				if ( scoreDefinitions != null && !scoreDefinitions.isEmpty() ) {
					for (ProjectScoreDefinition scoreDef : scoreDefinitions) {
						String scoreValue = storeVisitScores.get(row.get("storeId")+"#"+row.get("taskId")).get(""+scoreDef.getScoreId());
						if ( StringUtils.isBlank(scoreValue) ) {
							result.append("," + "NA");
						} else {
							result.append("," + scoreValue);
						}
					}
				}
				
				if ( keyMetricsNameList != null && !keyMetricsNameList.isEmpty() ) {
					for (String keyMetricName : keyMetricsNameList) {
						String keyMetricValue = storeVisitKeyMetrics.get(row.get("storeId")+"#"+row.get("taskId")).get(keyMetricName);
						if ( StringUtils.isBlank(keyMetricValue) ) {
							result.append("," + "NA");
						} else {
							result.append("," + (Boolean.parseBoolean(keyMetricValue) ? "Yes" : "No") );
						}
					}
				}
				
				for (Map<String, String> upc : projectUpcList) {
					result.append("," + row.get(upc.get("upc")));
				}
				
				if (projectQuestions != null && !projectQuestions.isEmpty()) {
					Map<String, String> repResponses = processImageDao.getRepResponsesByStoreVisit(
							inputObject.getProjectId(), row.get("storeId"),
							row.get("taskId"));
					for (ProjectQuestion question : projectQuestions) {
						String response = repResponses.get(question.getId());
						if (response == null)
							response = "";
						result.append(",\"" + response + "\"");
					}
				}
				
				fileWriter.append(result.toString() + "\n");
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreResultsDetailCsv----------------\n");

		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void recomputeProjectByStoreVisit(InputObject inputObject) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts recomputeProjectByStore----------------\n");

		int projectId = inputObject.getProjectId();
		String storeId = inputObject.getStoreId();
		String taskId = inputObject.getTaskId();

		String granularity = inputObject.getGranularity();

		LOGGER.info("---------------ProcessImageServiceImpl recomputeProjectByStore :: granularity specified {}",granularity);

		if (StringUtils.isBlank(granularity) || granularity.equals("-9")) {
			LOGGER.info("---------------ProcessImageServiceImpl recomputeProjectByStore :: no granularity specified :: do nothing");
			return; // do nothing if granularity is not specified
		}

		boolean generateAggs = granularity.contains("agg");
		boolean recomputeStoreVisitResult = granularity.contains("store");
		boolean recomputeImageResult = granularity.contains("image");
		boolean runStitching = granularity.contains("stitch");
		boolean recomputeScore = granularity.contains("score");

		if (generateAggs == false && recomputeStoreVisitResult == false && recomputeImageResult == false && runStitching == false && recomputeScore == false) {
			LOGGER.info("---------------ProcessImageServiceImpl recomputeProjectByStore :: no supported granularity specified :: do nothing");
			return; // Unsupported granularity
		}

		List<StoreVisit> storeVisits = new ArrayList<StoreVisit>();
		if (storeId.equalsIgnoreCase("all")) {// If not supplied in user input, recompute all which are not in Not
			// Processed or Generate Aggregations status
			storeVisits = processImageDao.getStoreVisitsForRecompute(projectId);
		} else {
			StoreVisit storeVisit = new StoreVisit(storeId, taskId);
			storeVisits.add(storeVisit);
		}

		LOGGER.info("---------------ProcessImageServiceImpl recomputeProjectByStore for stores {}",storeVisits);
		boolean isDistributionCheckProject = processImageDao
				.isDistributionCheckProject(inputObject.getProjectId())
				.equals("5");
		
		/*boolean isDuplicateAnalysisEnabled = metaServiceDao
				.getDuplicateAnalysisEnabled(inputObject.getProjectId());*/
		
		String aggregationType = metaServiceDao.getProjectDetail(projectId).get(0).get("aggregationType");
		
		if (storeVisits != null && !storeVisits.isEmpty()) {
			for (StoreVisit storeVisit : storeVisits) {
				/*if ( runStitching && isDuplicateAnalysisEnabled ) {
					//analyseDuplicates(projectId, storeVisit);
					continue; //if duplicates need to be analysed, just send the request and move to next visit.
				}*/
				
				if (generateAggs) {
					boolean success = true;
					for(int i=1; i <= 3; i++) {
						LOGGER.info("---------------ProcessImageServiceImpl::aggregation attempt {} for projectId={}::storeId={}::taskId={}", i, projectId, storeId, taskId);
						try {
							aggregate(projectId, storeVisit.getStoreId(), storeVisit.getTaskId(), aggregationType);
						} catch (Exception e) {
							success = false;
						}
						if ( success ) {
							LOGGER.info("---------------ProcessImageServiceImpl::aggregation succeeded for projectId={}::storeId={}::taskId={}", projectId, storeId, taskId);
							break;
						} else {
							LOGGER.error("---------------ProcessImageServiceImpl::aggregation failed for projectId={}::storeId={}::taskId={}", projectId, storeId, taskId);
						}
					}
					if (!success) {
						LOGGER.error("---------------ProcessImageServiceImpl::aggregation failed in all retries for projectId={}::storeId={}::taskId={}", projectId, storeId, taskId);
					}
				}

				if (isDistributionCheckProject) {
					computeDistributionMetric(projectId, storeVisit.getStoreId(),
							storeVisit.getTaskId());
				}

				if (recomputeStoreVisitResult) {
					// 2 Recompute project result for store visit
					processImageDao.generateStoreVisitResults(projectId, storeVisit.getStoreId(),
							storeVisit.getTaskId());
				}
				
				if (recomputeScore) {
					generateScore(projectId, storeVisit);
				}

				if (recomputeImageResult) {
					// 3 Recompute image result for all images for store visit
					// 3.1 Get all images for this store visit
					List<String> storeVisitImageUUIDs = processImageDao.getImagesByStoreVisit(projectId, storeVisit.getStoreId(), storeVisit.getTaskId());
					// 3.2 For each image, compute result
					for (String imageUUID : storeVisitImageUUIDs) {
						// 3.2.1 Get image details
						ImageStore imageStore = processImageDao.findByImageUUId(imageUUID);
						// 3.2.2 Proceed only if questionId is populated,
						// image is processed,
						// image review status is 1 or 0 (to filter manually reviewed)
						// and imageResultCode is null or not reject insufficient
						if (StringUtils.isNotBlank(imageStore.getQuestionId())
								&& imageStore.getImageStatus().equals("done")
								&& (imageStore.getImageReviewStatus().equals("0")
								|| imageStore.getImageReviewStatus().equals("1"))
								&& (imageStore.getImageResultCode() == null ? true
								: !imageStore.getImageResultCode()
								.equals(ImageResultCode.REJECT_INSUFFICIENT.getCode()))) {
							// 3.2.3 Get image analysis list
							List<ImageAnalysis> imageAnalysisList = processImageDao
									.getImageAnalysisForRecompute(imageUUID);
							// 3.2.4 Recomputing image result
							Map<String, String> resultMap = processImageDao.generateImageResult(imageStore,
									imageAnalysisList);
							ImageResultCode resultCode = ImageResultCode
									.getImageResultCodeFromCode(resultMap.get("resultCode"));
							String resultComment = (String) resultMap.get("resultComment");
							String objectiveResultStatus = (String) resultMap.get("objectiveResultStatus");

							String imageReviewStatus = "1";
							if (resultCode == ImageResultCode.REJECT_LEVEL_1
									|| resultCode == ImageResultCode.REJECT_LEVEL_3
									|| resultCode == ImageResultCode.APPROVED_PENDING_REVIEW
									|| resultCode == ImageResultCode.REJECT_INSUFFICIENT
									|| resultCode == ImageResultCode.UNAPPROVED) {
								imageReviewStatus = "0"; // for review
							}
							// 3.2.5 Update newly computed result code to image
							processImageDao.updateImageResultCodeAndStatus(imageStore.getImageUUID(),
									resultCode.getCode(), resultComment, imageReviewStatus, objectiveResultStatus);
						}
					}
				}

			}
			/*if (recomputeImageResult) {
				// 4 Do post-processing of image result at project level
				computeImageResults(inputObject);
			}*/

		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends recomputeProjectByStore----------------\n");

	}

	private void aggregate(int projectId, String storeId, String taskId, String aggregationType) throws Exception {
		if ( aggregationType.equals("0") ) {
			processImageDao.generateAggs(projectId, storeId, taskId);
		} else if ( aggregationType.equals("1") ) {
			processImageDao.generateAggsType1(projectId, storeId, taskId);
		}
		if ( Constants.ITG_CIGARETTES_PROJECT_IDS.contains(projectId) ) { //ITG Specific
			itgAggregationService.runStoreVisitAggregation(projectId, storeId, taskId);
		}
	}
	
	/*private void analyseDuplicates(int projectId, StoreVisit storeVisit) {
		LOGGER.info("---------------ProcessImageServiceImpl:: analyseDuplicates Starts for projectId={} and storeVisit={}",projectId,storeVisit);

		stitchImagesForShelfLevelsAndDuplicates(projectId, storeVisit.getStoreId(), storeVisit.getTaskId());
		
		LOGGER.info("---------------ProcessImageServiceImpl:: analyseAndUpdateDuplicates Ends for projectId={} and storeVisit={}",projectId,storeVisit);
	}*/

	private void generateScore(int projectId, StoreVisit storeVisit) {
		LOGGER.info("---------------ProcessImageServiceImpl:: generateScore Starts for projectId={} and storeVisit={}",projectId,storeVisit);
		
		LOGGER.info("---------------ProcessImageServiceImpl:: Loadig scoring defintions for projectId={}",projectId);

		// Get scoring definition for this project
		List<ProjectScoreDefinition> scoreDefinitions = metaServiceDao.getProjectScoreDefinition(projectId);
		
		if ( scoreDefinitions.isEmpty() ) {
			LOGGER.info("---------------ProcessImageServiceImpl:: No scoring defintions found for projectId={}.. Returning...",projectId);
			return;
		}
		
		List<Map<String,Object>> highLevelScoreResults = new ArrayList<Map<String,Object>>();
		
		List<Map<String,Object>> componentScoreResults = new ArrayList<Map<String,Object>>();

		List<Map<String,Object>> criteriaScoreResults = new ArrayList<Map<String,Object>>();


		Map<String,Object> analysisData = processImageDao.getStoreLevelDataForAnalysis(projectId, storeVisit.getStoreId(), storeVisit.getTaskId());
		List<String> aggUPCs = (List<String>) analysisData.get("aggUPCs");
		Map<String,Object> projectStoreData = (Map<String, Object>) analysisData.get("projectStoreData");
		Map<String,String> repResponses = (Map<String, String>) analysisData.get("repResponses");
		String countDistinctUpc = (String) analysisData.get("countDistinctUpc");
		String percentageOsa = (String) analysisData.get("percentageOsa");
		String distributionPercentage = (String) analysisData.get("distributionPercentage");
		String hasLowQualityImages = (String) analysisData.get("hasLowQualityImages");
		String hasLowConfidenceDetections = (String) analysisData.get("hasLowConfidenceDetections");
		String sumFacing = (String) analysisData.get("sumFacing");
		String sumUpcConfidence = (String) analysisData.get("sumUpcConfidence");
		String countMissingUPC = (String) analysisData.get("countMissingUPC");
		
		for(ProjectScoreDefinition score : scoreDefinitions) {
			LOGGER.info("---------------ProcessImageServiceImpl:: Evaluating score {} for storeVisit {}", score.getScoreName(), storeVisit);
			double scoreValue = 0;
			for(ProjectComponentScoreDefinition componentScore : score.getComponentScores()) {
				LOGGER.info("---------------ProcessImageServiceImpl:: Evaluating component score {} for storeVisit {}", score.getScoreName() + "," + componentScore.getComponentScoreName(),  storeVisit);
				double componentScoreValue = 0;
				int lastSuccessfullyEvaluatedGroupId = 0;
				Map<String,String> componentScoreCommentMap = new LinkedHashMap<String,String>();
				Map<String,String> componentScoreActionMap = new LinkedHashMap<String,String>();

				for(ProjectComponentCriteriaScoreDefinition criteriaScore : componentScore.getComponentCriteriaScores()) {
					
					if ( lastSuccessfullyEvaluatedGroupId == criteriaScore.getGroupId() ) {
						//In a group, skip all criteria after a successful criteria evaluation.
						continue;
					}
					
					LOGGER.info("---------------ProcessImageServiceImpl:: Evaluating criteria score {} for storeVisit {}", 
							score.getScoreName() + "," + componentScore.getComponentScoreName() + "," + criteriaScore.getGroupId() + "," + criteriaScore.getGroupSequenceNumber(),  storeVisit);
					
					String criteria = criteriaScore.getCriteria();
					double criteriaPoints = Double.parseDouble(criteriaScore.getPoints());

					boolean criteriaEvaluationResult = false;
					String criteriaEvaluationValue = null;
					
					if ( ExpressionEvaluator.isPercentageCriteria(criteria)) {
						criteriaEvaluationResult = true;
						double percentage = ExpressionEvaluator.evaluatePercentageCriteria(projectStoreData, criteria);
						criteriaPoints = percentage * criteriaPoints;
						criteriaEvaluationValue = SCORE_DECIMAL_FORMATTER.format(percentage);
					} else {
						criteriaEvaluationResult = ExpressionEvaluator.evaluate(criteria, aggUPCs, projectStoreData, repResponses, countDistinctUpc, 
								percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
						criteriaEvaluationValue = String.valueOf(criteriaEvaluationResult);
					}

					Map<String,Object> criteriaScoreResult = new HashMap<String,Object>();
					criteriaScoreResult.put("scoreId", criteriaScore.getScoreId());
					criteriaScoreResult.put("componentScoreId", criteriaScore.getComponentScoreId());
					criteriaScoreResult.put("groupId", criteriaScore.getGroupId());
					criteriaScoreResult.put("groupSequenceNumber", criteriaScore.getGroupSequenceNumber());
					criteriaScoreResult.put("result", criteriaEvaluationValue);
					criteriaScoreResult.put("score", "0");
					
					if ( criteriaEvaluationResult == true ) {
						componentScoreValue = componentScoreValue + criteriaPoints;
						lastSuccessfullyEvaluatedGroupId = criteriaScore.getGroupId();
						criteriaScoreResult.put("score", SCORE_DECIMAL_FORMATTER.format(criteriaPoints));
						
						//If criteria evaluates to true, but points are -ve, add comment and action
						if (criteriaPoints < 0) {
							if ( StringUtils.isNotBlank(criteriaScore.getComment())) {
								componentScoreCommentMap.put(""+criteriaScore.getGroupId(),criteriaScore.getComment());
							}
							if (StringUtils.isNotBlank(criteriaScore.getAction())) {
								componentScoreActionMap.put(""+criteriaScore.getGroupId(),criteriaScore.getAction());
							}
						}
					} else {
						//If criteria evaluates to false, but points are +ve, add comment and action
						if (criteriaPoints > 0) {
							if ( StringUtils.isNotBlank(criteriaScore.getComment())) {
								componentScoreCommentMap.put(""+criteriaScore.getGroupId(),criteriaScore.getComment());
							}
							if (StringUtils.isNotBlank(criteriaScore.getAction())) {
								componentScoreActionMap.put(""+criteriaScore.getGroupId(),criteriaScore.getAction());
							}
						}
					}
					criteriaScoreResults.add(criteriaScoreResult);
				}
				
				Map<String,Object>componentScoreResult = new HashMap<String,Object>();
				componentScoreResult.put("scoreId", componentScore.getScoreId());
				componentScoreResult.put("componentScoreId", componentScore.getComponentScoreId());
				componentScoreResult.put("componentScore", SCORE_DECIMAL_FORMATTER.format(componentScoreValue));
				componentScoreResult.put("componentScoreComment", 
						componentScoreCommentMap.values().stream().map(Object::toString).collect(Collectors.joining(", ")));
				componentScoreResult.put("componentScoreAction", 
						componentScoreActionMap.values().stream().map(Object::toString).collect(Collectors.joining(", ")));
				
				componentScoreResults.add(componentScoreResult);
				
				scoreValue = scoreValue + componentScoreValue;
			}
			
			//If Total score goes negative, set to 0.
			scoreValue = scoreValue < 0 ? 0 : scoreValue;
			
			//Compute the score group to which this score belongs to
			int scoreGroupId = 0;
			List<ProjectScoreGroupingDefinition> scoreGroups = score.getScoreGrouping();
			for(ProjectScoreGroupingDefinition scoreGroup : scoreGroups) {
				Double minScore = Double.parseDouble(scoreGroup.getGroupMinScore());
				Double maxScore = Double.parseDouble(scoreGroup.getGroupMaxScore());
				if( scoreValue >= minScore && scoreValue <= maxScore ) {
					scoreGroupId = scoreGroup.getScoreGroupId();
					break;
				}
			}
			
			Map<String,Object> highLevelScoreResult = new HashMap<String,Object>();
			highLevelScoreResult.put("scoreId", score.getScoreId());
			highLevelScoreResult.put("score", SCORE_DECIMAL_FORMATTER.format(scoreValue));
			highLevelScoreResult.put("scoreGroupId", scoreGroupId);
			highLevelScoreResults.add(highLevelScoreResult);
		}
		
		processImageDao.saveProjectStoreScores(projectId,storeVisit.getStoreId(),storeVisit.getTaskId(),
				highLevelScoreResults, componentScoreResults, criteriaScoreResults );
		
		LOGGER.info("---------------ProcessImageServiceImpl:: Calculated highlevel scores for projectId={},storeVisit={} are {}",
				projectId, storeVisit, highLevelScoreResults);
	}

	private void computeDistributionMetric(int projectId, String storeId,
										   String taskId) {

		processImageDao.insertIntoProjectDistributionStoreData(projectId, storeId, taskId);
		processImageDao.updateProjectStoreResult(projectId, storeId, taskId);
	}

	@Override
	public void reprocessProjectByStore(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts reprocessProjectByStore----------------\n");

		int projectId = inputObject.getProjectId();
		String storeId = inputObject.getStoreId();
		List<String> storeIdsToReprocess = null;
		if (storeId.equalsIgnoreCase("all")) { // If not supplied in user input, reprocess all
			storeIdsToReprocess = processImageDao.getProjectStoreIds(projectId, false);
		} else {
			storeIdsToReprocess = Arrays.asList(storeId.split("\\s*,\\s*"));
		}

		LOGGER.info("---------------ProcessImageServiceImpl reprocessProjectByStore for stores {}", storeIdsToReprocess);

		if (storeIdsToReprocess != null && !storeIdsToReprocess.isEmpty()) {
			processImageDao.reprocessProjectByStore(projectId, storeIdsToReprocess);
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends reprocessProjectByStore----------------\n");

	}

	@Override
	public void updateProjectResultStatus(StoreVisitResult[] stores) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateProjectResultStatus----------------\n");

		processImageDao.updateProjectResultStatus(Arrays.asList(stores));

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateProjectResultStatus----------------\n");

	}

	@Override
	public void bulkUploadProjectImage(int projectId, String sync, String filenamePath) {
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(projectId);
		String categoryId = "";
		String retailerCode = "";
		if (projectDetail != null && !projectDetail.isEmpty()) {
			categoryId = projectDetail.get(0).get("categoryId");
			retailerCode = projectDetail.get(0).get("retailerCode");
		}

		List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(projectId);

		BulkUploadEngine engine = new BulkUploadEngine();
		engine.setProjectId(projectId);
		engine.setSync(sync);
		engine.setCategoryId(categoryId);
		engine.setRetailerCode(retailerCode);
		engine.setFilenamePath(filenamePath);
		engine.setProjectQuestions(projectQuestions);
		engine.setProcessImageService(this);
		engine.setProcessImageDao(processImageDao);
		engine.setMetaServiceDao(metaServiceDao);
		engine.setMetaService(metaService);
		engine.setEnv(env);
		engine.setCloudStorageService(cloudStorageService);
		Thread uploadThread = new Thread(engine);
		uploadThread.start();
	}

	@Override
	public List<StoreVisit> getStoreVisitsWithImages(int projectId) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoreIdsWithImages----------------\n");

		List<StoreVisit> result = processImageDao.getStoreVisitsWithImages(projectId);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoreIdsWithImages----------------\n");

		return result;
	}

	@Override
	public List<StoreVisit> getAlreadyUploadedStoreVisit(int projectId) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getAlreadyUploadedStoreVisit----------------\n");

		List<StoreVisit> result = processImageDao.getAlreadyUploadedStoreVisit(projectId);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getAlreadyUploadedStoreVisit----------------\n");

		return result;
	}

	@Override
	public List<LinkedHashMap<String, String>> insertOrUpdateStoreResult(int projectId,
																		 String storeId, String countDistinctUpc, String sumFacing, String sumUpcConfidence, String resultCode,
																		 String status, String agentId, String taskId, String visitDateId, String imageUrl, String batchId, String customerProjectId) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts insertOrUpdateStoreResult projectId ="
				+ projectId + "storeId =" + storeId
				+ "countDistinctUpc =" + countDistinctUpc + "sumFacing =" + sumFacing + "sumUpcConfidence ="
				+ sumUpcConfidence + "resultCode =" + resultCode + "status =" + status + "agentId =" + agentId
				+ "taskId =" + taskId + "visitDateId =" + visitDateId + "imageUrl =" + imageUrl );

		List<LinkedHashMap<String, String>> result = processImageDao.insertOrUpdateStoreResult(projectId, storeId, countDistinctUpc, sumFacing, sumUpcConfidence, resultCode, status, agentId,
				taskId, visitDateId, imageUrl, batchId,customerProjectId);

		LOGGER.info("---------------ProcessImageServiceImpl Ends insertOrUpdateStoreResult----------------\n");

		return result;
	}

	@Override
	public void saveStoreVisitRepResponses(int projectId, String storeId, String taskId,
										   Map<String, String> repResponses) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts saveStoreVisitRepResponses----------------\n");

		processImageDao.saveStoreVisitRepResponses(projectId, storeId, taskId, repResponses);

		LOGGER.info("---------------ProcessImageServiceImpl Ends saveStoreVisitRepResponses----------------\n");
	}

	@Override
	public File getProjectAllStoreImageResultsCsv(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreImageResultsCsv----------------\n");
		
		LOGGER.info("---------------ProcessImageServiceImpl :: starts computing image results for one final time----------------\n");
		computeImageResults(inputObject);
		LOGGER.info("---------------ProcessImageServiceImpl :: ends computing image results for one final time----------------\n");

		List<LinkedHashMap<String, String>> resultList = processImageDao.getProjectAllStoreImageResults(inputObject.getProjectId(), inputObject.getStatus());

		List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String customerProjectId = projectDetail.get(0).get("customerProjectId");
		String projectName = projectDetail.get(0).get("projectName");

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			
			String headers = "Action Id,Action Description,Retailer Store Id,Retailer,Street,City,State Code,Zip,Photo Date,Service Order Id,Rep Id,Question Id,File Id,Photo Link,Photo Result,Photo Result Comments,Date Reviewed";

			if (projectQuestions != null && !projectQuestions.isEmpty()) {
				for (ProjectQuestion question : projectQuestions) {
					headers = headers.concat("," + question.getSequenceNumber() + "-" + question.getDesc());
				}
			}

			headers = headers.concat("\n");

			fileWriter.append(headers);

			for (LinkedHashMap<String, String> row : resultList) {
				StringBuilder result = new StringBuilder();
				result.append(customerProjectId + ",");
				result.append(projectName + ",");
				result.append(row.get("retailerStoreId") + ",");
				result.append(row.get("retailer") + ",");
				result.append(row.get("street").replace(",", " ") + ",");
				result.append(row.get("city") + ",");
				result.append(row.get("stateCode") + ",");
				result.append(row.get("zip") + ",");
				result.append(row.get("visitDate") + ",");
				result.append(row.get("taskId") + ",");
				result.append(row.get("agentId") + ",");
				result.append(row.get("questionId") + ",");
				result.append(row.get("fileId") + ",");
				result.append(row.get("imageURL") + ",");
				result.append(row.get("imageResultDesc") + ",");
				result.append("\"" + row.get("imageResultComments") + "\",");
				result.append(row.get("processedDate"));
				if (projectQuestions != null && !projectQuestions.isEmpty()) {
					Map<String, String> repResponses = processImageDao.getRepResponsesByStoreVisit(inputObject.getProjectId(), row.get("storeId"),row.get("taskId"));
					for (ProjectQuestion question : projectQuestions) {
						String response = repResponses.get(question.getId());
						if (response == null)
							response = "";
						result.append(",\"" + response + "\"");
					}
				}
				fileWriter.append(result.toString() + "\n");
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreResultsDetailCsv----------------\n");

		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void computeImageResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts computeImageResults----------------\n");

		int projectId = inputObject.getProjectId();
		
		LOGGER.info("---------------ProcessImageServiceImpl :: computeImageResults :: projectId = {}",projectId);

		Map<String, String> imagesForUpdate = new LinkedHashMap<String, String>();
		List<Project> projectDetails = metaService.getProjectDetail(inputObject);
		
		//Setting image to Unapproved if rep response to feedback question for that project store visit
		//has more than 10 characters length. A project will have only one feedback question, and its
		//description will be set as FEEDBACK.
		List<ProjectQuestion> questions = projectDetails.get(0).getProjectQuestionsList();
		String feedbackQuestionId = null;
		for(ProjectQuestion question : questions) {
			if("FEEDBACK".equals(question.getDesc().trim())) {
				feedbackQuestionId = question.getId();
				break;
			}
		}
		if(StringUtils.isNotBlank(feedbackQuestionId)) {
			LOGGER.info("---------------ProcessImageServiceImpl :: checking and updating image result code to Unapproved for "
					+ "feedback question {}", feedbackQuestionId );
			processImageDao.updateImageResultCodeToUnapprovedForFeedbackQuestionResponse(projectId, feedbackQuestionId);	
		} else {
			LOGGER.info("---------------ProcessImageServiceImpl :: No feedback question configured for project" );
		}
		
		boolean isParentProject = Boolean.parseBoolean(projectDetails.get(0).getIsParentProject());
		int parentProjectId = Integer.parseInt(projectDetails.get(0).getParentProjectId());
		
		if ( !isParentProject && parentProjectId != 0 && projectId != parentProjectId ) {
			//If rep didn't have time enough to audit the store, it would have been indicated
			//as a rep response in the parent project for this particular project.
			//Mark all images from such store visits with result code = Unapproved, retain the computed result comment.
			LOGGER.info("---------------ProcessImageServiceImpl :: checking and updating image result code to Unapproved for "
					+ "Not enough time response in parent project id = {}", parentProjectId );
			processImageDao.updateImageResultCodeToUnapprovedForNotEnoughTimeResponse(projectId, parentProjectId);
		}

		// Find images for Unapprove status
		// For the same storeId+taskId+questionId,
		// if there are more than one photos, then
		// imageUUIDwith highest value of imageResultCode will be left as is and all
		// others set to Unapprove status
		Map<String, Map<String, String>> imageResultsMap = processImageDao.getAllImageResultsForStoresWithMultipleImages(projectId);
		LOGGER.info("---------------ProcessImageServiceImpl :: imageResults for multiple images ::{}", imageResultsMap);

		for (Map<String, String> imageResultForStoreTaskQuestion : imageResultsMap.values()) {
			Entry<String, String> imageWithHighestResultCode = null;
			for (Entry<String, String> imageResult : imageResultForStoreTaskQuestion.entrySet()) {
				if (imageWithHighestResultCode == null
						|| imageResult.getValue().compareTo(imageWithHighestResultCode.getValue()) > 0) {
					imageWithHighestResultCode = imageResult;
				}
				imagesForUpdate.put(imageResult.getKey(), imageResult.getValue());
			}
			imagesForUpdate.remove(imageWithHighestResultCode.getKey());
		}
		String UNAPPROVE_RESULT_COMMENT = "Extra photo";
		for (String uuid : imagesForUpdate.keySet()) {
			imagesForUpdate.put(uuid, ImageResultCode.UNAPPROVED.getCode() + "#" + UNAPPROVE_RESULT_COMMENT);
		}

		LOGGER.info("---------------ProcessImageServiceImpl :: images to set for Unapprove ::{}", imagesForUpdate);
		// Update all such images to UNAPPROVE
		processImageDao.updateImageResultCodeBatch(imagesForUpdate);

		// Reuse for duplicate computation as well
		imagesForUpdate.clear();

		// Find images for Reject Duplicate status
		List<Map<String, String>> dupIimageResults = processImageDao.getAllImageResultsForDuplicateImages(projectId);

		// Group them as imageHashScore to storeVisit
		Map<String, Set<String>> imageHashScoreToStoreVisitMap = new LinkedHashMap<String, Set<String>>();
		for (Map<String, String> map : dupIimageResults) {
			Set<String> storeVisitSetForHashScore = imageHashScoreToStoreVisitMap.get(map.get("imageHashScore"));
			String storeVisit = map.get("retailerStoreId") + "/" + map.get("taskId");
			if (storeVisitSetForHashScore == null) {
				storeVisitSetForHashScore = new HashSet<String>();
			}
			storeVisitSetForHashScore.add(storeVisit);
			imageHashScoreToStoreVisitMap.put(map.get("imageHashScore"), storeVisitSetForHashScore);
		}

		// If an imageHashScore is found for more than one store visit (storeId+taskId),
		// mark all images with that imageHashScore as Reject Duplicate.
		for (Entry<String, Set<String>> hashToStoreVisits : imageHashScoreToStoreVisitMap.entrySet()) {
			if (hashToStoreVisits.getValue().size() > 1) {
				String dupStoreVisits = hashToStoreVisits.getValue().toString().replaceAll("\\[", "").replaceAll("\\]",
						"");
				String dupResultComment = "Duplicate photos for Store Number / Service Order - " + dupStoreVisits;
				imagesForUpdate.put(hashToStoreVisits.getKey(),
						ImageResultCode.REJECT_DUPLICATE.getCode() + "#" + dupResultComment);
			}
		}

		LOGGER.info("---------------ProcessImageServiceImpl :: imageHashCodes to set for Reject Duplicate ::{}",imagesForUpdate );
		// Update all images for these imageHashScore to Reject Duplicate
		processImageDao.updateImageResultCodeByImageHashScoreBatch(imagesForUpdate, projectId);

		LOGGER.info("---------------ProcessImageServiceImpl Ends computeImageResults----------------\n");
	}

	@Override
	public List<Map<String, String>> getImageResultsForPremium(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getImageResultsForPremium----------------\n");

		List<Map<String, String>> resultMap = processImageDao.getImageResultsForPremium(inputObject.getProjectId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getImageResultsForPremium----------------\n");

		return resultMap;
	}

	/*@Override
	public List<LinkedHashMap<String, Object>> getProjectAllStoreImageResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreImageResults----------------\n");

		Map<String,LinkedHashMap<String,Object>> resultMap = new HashMap<String,LinkedHashMap<String,Object>>();
		
		List<LinkedHashMap<String, String>> images = processImageDao.getProjectAllStoreImageResults(
				inputObject.getProjectId(), inputObject.getStatus());

		List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());
		Set<String> storeIdsToReview = new HashSet<String>();

		for (LinkedHashMap<String, String> row : images) {
			String storeId = row.get("storeId");
			String taskId = row.get("taskId");
			String imagePendingReview = row.get("imageReviewStatus");
			int projectId = inputObject.getProjectId();
			String storeVisitKey = storeId+taskId;
			
			if ( !resultMap.containsKey(storeVisitKey) ) {
				LinkedHashMap<String,Object> storeVisitMap = new LinkedHashMap<String,Object>();
				storeVisitMap.put("storeId", storeId);
				storeVisitMap.put("taskId", taskId);
				storeVisitMap.put("projectId", projectId);
				storeVisitMap.put("imagesPendingReview", "0");
				storeVisitMap.put("visitDate", row.get("visitDateId"));
				storeVisitMap.put("images", new ArrayList<LinkedHashMap<String,String>>());
				if (projectQuestions != null && !projectQuestions.isEmpty()) {
					Map<String, String> repResponses = processImageDao.getRepResponsesByStoreVisit(projectId, storeId, taskId);
					List<Map<String, String>> repQuestionResponseList = new ArrayList<Map<String,String>>();
					for (ProjectQuestion question : projectQuestions) {
						Map<String, String> oneRepQuestionResponse = new HashMap<String,String>();
						oneRepQuestionResponse.put("repQuestionId", question.getId());
						oneRepQuestionResponse.put("repQuestionSequenceNumber", question.getSequenceNumber());
						oneRepQuestionResponse.put("repQuestion", question.getDesc());
						oneRepQuestionResponse.put("repResponse", ConverterUtil.ifNullToEmpty(repResponses.get(question.getId())));
						repQuestionResponseList.add(oneRepQuestionResponse);
					}
					storeVisitMap.put("repResponses", repQuestionResponseList);
				}
				resultMap.put(storeVisitKey, storeVisitMap);
			}
			
			List<LinkedHashMap<String,String>> imageList = (List<LinkedHashMap<String, String>>) resultMap.get(storeVisitKey).get("images");
			LinkedHashMap<String,String> oneImage = new LinkedHashMap<String,String>();
			oneImage.put("imageResultCode", row.get("imageResultCode"));
			oneImage.put("imageUUID", row.get("imageUUID"));
			oneImage.put("resultUploaded", row.get("resultUploaded"));
			oneImage.put("questionId", row.get("questionId"));
			oneImage.put("fileId", row.get("fileId"));
			oneImage.put("visitDate", row.get("visitDate"));
			oneImage.put("processedDate", row.get("processedDate"));
			oneImage.put("imageStatus", row.get("imageStatus"));
			oneImage.put("imageReviewStatus", row.get("imageReviewStatus"));
			oneImage.put("imageResultDesc", row.get("imageResultDesc"));
			oneImage.put("imageResultComments", row.get("imageResultComments"));
			oneImage.put("origWidth", row.get("origWidth"));
			oneImage.put("newWidth", row.get("newWidth"));
			oneImage.put("origHeight", row.get("origHeight"));
			oneImage.put("newHeight", row.get("newHeight"));
			imageList.add(oneImage);
			if ( imagePendingReview.equals("0") ) {
				resultMap.get(storeVisitKey).put("imagesPendingReview", "1");
				storeIdsToReview.add(storeVisitKey);
			}
		}

		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String,Object>>(resultMap.values());
		
		resultList.sort(Comparator.comparing((LinkedHashMap<String, Object> m) -> (String) m.get("imagesPendingReview")).reversed()
				.thenComparing(m -> (String) m.get("visitDate")));
		
		LinkedHashMap<String, Object> oneStorePendingReview = resultList.get(0);
		oneStorePendingReview.put("totalStoresPendingReview", storeIdsToReview.size());
		resultList.clear();
		
		if ( ((String)oneStorePendingReview.get("imagesPendingReview")).equals("1") ) {
			resultList.add(oneStorePendingReview);
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreImageResults----------------\n");

		return resultList;
	}*/
	
	@Override
	public List<LinkedHashMap<String, Object>> getProjectAllStoreImageResults(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreImageResults----------------\n");

		List<LinkedHashMap<String,Object>> resultList = new ArrayList<LinkedHashMap<String,Object>>();
		
		LinkedHashMap<String,Object> resultMap = processImageDao.getNextStoreVisitToReview(inputObject.getProjectId());
		
		if ( !resultMap.isEmpty() ) {
			String storeId = (String) resultMap.get("storeId");
			String taskId = (String) resultMap.get("taskId");
			List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());
			
			if (projectQuestions != null && !projectQuestions.isEmpty()) {
				Map<String, String> repResponses = processImageDao.getRepResponsesByStoreVisit(inputObject.getProjectId(), storeId, taskId);
				List<Map<String, String>> repQuestionResponseList = new ArrayList<Map<String,String>>();
				for (ProjectQuestion question : projectQuestions) {
					Map<String, String> oneRepQuestionResponse = new HashMap<String,String>();
					oneRepQuestionResponse.put("repQuestionId", question.getId());
					oneRepQuestionResponse.put("repQuestionSequenceNumber", question.getSequenceNumber());
					oneRepQuestionResponse.put("repQuestion", question.getDesc());
					oneRepQuestionResponse.put("repResponse", ConverterUtil.ifNullToEmpty(repResponses.get(question.getId())));
					repQuestionResponseList.add(oneRepQuestionResponse);
				}
				resultMap.put("repResponses", repQuestionResponseList);
			}
			resultList.add(resultMap);
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreImageResults----------------\n");

		return resultList;
	}

	@Override
	public void updateProjectImageResultStatus(ImageStore[] imageResultUpdateArray) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts updateProjectImageResultStatus----------------\n");

		try {
			processImageDao.updateImageResultCodeAndStatusBatch(Arrays.asList(imageResultUpdateArray));
		} catch (Exception e) {
			LOGGER.error("-------------------Wait for 3 seconds and retry updateProjectImageResultStatus---------------");
			try {
				Thread.sleep(3000);
				processImageDao.updateImageResultCodeAndStatusBatch(Arrays.asList(imageResultUpdateArray));
			} catch (Exception e1) {
				LOGGER.error("Updating image result status, failed in retry: {}",e1.getMessage(),e1);
			}
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateProjectImageResultStatus----------------\n");

	}

	@Override
	public void pollAndSubmitStoreForAnalysis() {
		LOGGER.info("---------------ProcessImageServiceImpl Starts pollAndSubmitStoreForAnalysis----------------\n");

		List<LinkedHashMap<String, String>> resultList = processImageDao.getAggsReadyStoreVisit();
		
		LOGGER.info("Number of store visits to analyse :: {}",resultList.size());
        for(LinkedHashMap<String, String> oneStoreVisit : resultList) {
        	int projectId = Integer.parseInt(oneStoreVisit.get("projectId"));
            String storeId = oneStoreVisit.get("storeId");
            String taskId = oneStoreVisit.get("taskId");
            /*InputObject obj = new InputObject();
            obj.setProjectId(projectId);
            obj.setStoreId(storeId);
            obj.setTaskId(taskId);
            obj.setGranularity("agg-store-stitch-score");
            try {
                recomputeProjectByStoreVisit(obj);
            } catch (Exception e) {
            	e.printStackTrace();
    			LOGGER.error("EXCEPTION WHILE AGGREGATION {} , {}", e.getMessage(), e);
            }*/
            LOGGER.info("Requesting store analysis for ProjectId={}, StoreId={}, TaskId={}",projectId,storeId,taskId);
			submitStoreForAnalysis(projectId, storeId, taskId);
			LOGGER.info("Marking store visit as \"store analysis in progress\" for ProjectId={}, StoreId={}, TaskId={}",projectId,storeId,taskId);
			processImageDao.setStoreVisitForStoreAnalysis(projectId,storeId,taskId);
        }

		LOGGER.info("---------------ProcessImageServiceImpl Ends pollAndSubmitStoreForAnalysis----------------\n");
	}

	@Override
	public void changeProjectImageStatus(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts changeProjectImageStatus----------------\n");

		processImageDao.changeProjectImageStatus(inputObject.getProjectId(),inputObject.getCurrentImageStatus(), inputObject.getNewImageStatus());

		LOGGER.info("---------------ProcessImageServiceImpl Ends changeProjectImageStatus----------------\n");

	}

	@Override
	public File getProjectStoresWithDuplicateImagesCsv(InputObject inputObject, String tempFilePath) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoresWithDuplicateImagesCsv----------------\n");

		List<DuplicateImages> dupImageList = processImageDao.getProjectStoresWithDuplicateImages(inputObject.getProjectId());

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			String input = "Input:" + "\n";
			String info1 = "projectId" + "," + inputObject.getProjectId() + "\n";
			String line = " " + "," + " " + "\n";
			fileWriter.append(input + info1 + line);

			String headers = "Photo Number,Store Number,Retailer,Street,City,State,Store Visit Date,Task Id,Agent Id,Photo link";

			headers = headers.concat("\n");

			fileWriter.append(headers);

			for (DuplicateImages row : dupImageList) {
				for (DuplicateImageInfo info : row.getStoreIds()) {
					StringBuilder result = new StringBuilder();
					result.append(row.getSlNo() + ",");
					result.append(info.getRetailerStoreId() + ",");
					result.append(info.getRetailer() + ",");
					result.append(info.getStreet().replace(",", " ") + ",");
					result.append(info.getCity() + ",");
					result.append(info.getState() + ",");
					result.append(info.getDateId() + ",");
					result.append(info.getTaskId() + ",");
					result.append(info.getAgentId() + ",");
					result.append("\"" + info.getImageURL() + "\"");

					fileWriter.append(result.toString() + "\n");
				}
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoresWithDuplicateImagesCsv----------------\n");

		File f = new File(tempFilePath);
		return f;

	}

	@Override
	public void updateProjectImageResultUploadStatus(List<String> imageUUIDs, String resultUploaded) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateProjectImageResultUploadStatus----------------\n");

		processImageDao.updateProjectImageResultUploadStatus(imageUUIDs, resultUploaded);

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateProjectImageResultUploadStatus----------------\n");

	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSummary(InputObject inputObject) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBrandSummary----------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> summary = new LinkedHashMap<String, Object>();

		List<String> monthList = processImageDao.getListMonths(inputObject.getProjectId());
		List<String> months = new ArrayList<String>();
		for (String oneMonth : monthList) {
			String yearPart = oneMonth.substring(2, 4);
			String monthPart = oneMonth.substring(4, 6);
			String formattedMonth = monthPart + "/" + yearPart;
			months.add(formattedMonth);
		}
		summary.put("months", months);

		List<String> brands = processImageDao.getListBrands(inputObject.getProjectId());
		summary.put("brands", brands);

		List<String> manufacturers = processImageDao.getListManufacturers(inputObject.getProjectId());
		summary.put("manufacturers", manufacturers);

		List<Map<String, String>> stores = processImageDao.getListStores(inputObject.getProjectId());

		Map<String, Object> storesGroupedByState = new LinkedHashMap<String, Object>();
		for (Map<String, String> store : stores) {
			String storeId = store.get("storeId");
			String state = store.get("state");
			String city = store.get("city");
			String street = store.get("street");
			if (storesGroupedByState.get(state) == null) {
				storesGroupedByState.put(state, new LinkedHashMap<String, Object>());
			}
			Map<String, Object> byState = (Map<String, Object>) storesGroupedByState.get(state);
			if (byState.get(city) == null) {
				byState.put(city, new ArrayList<Map<String, String>>());
			}
			List<Map<String, String>> byCity = (List<Map<String, String>>) byState.get(city);
			Map<String, String> oneStore = new LinkedHashMap<String, String>();
			oneStore.put("storeId", storeId);
			oneStore.put("street", street);
			byCity.add(oneStore);
		}

		summary.put("stores", storesGroupedByState);
		resultList.add(summary);
		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandSummary----------------\n");

		return resultList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSummaryNew(InputObject inputObject) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBrandSummaryNew----------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> summary = new LinkedHashMap<String, Object>();

		List<String> monthList = processImageDao.getListMonthsNew(inputObject.getCustomerCode(),inputObject.getProjectId());
		List<String> months = new ArrayList<String>();
		for (String oneMonth : monthList) {
			String yearPart = oneMonth.substring(2, 4);
			String monthPart = oneMonth.substring(4, 6);
			String formattedMonth = monthPart + "/" + yearPart;
			months.add(formattedMonth);
		}
		summary.put("months", months);
		
		summary.put("waves", metaServiceDao.getProjectWaves(inputObject.getProjectId()));

		summary.put("brands", processImageDao.getListBrandsNew(inputObject.getCustomerCode(),inputObject.getProjectId()));

		summary.put("manufacturers", processImageDao.getListManufacturersNew(inputObject.getCustomerCode(),inputObject.getProjectId()));
		
		summary.put("subCategories", processImageDao.getListSubCategoriesNew(inputObject.getCustomerCode(),inputObject.getProjectId()));
		
		summary.put("modulars", processImageDao.getListModularsNew(inputObject.getCustomerCode(),inputObject.getProjectId()));
		
		List<LinkedHashMap<String,String>> childProjects = metaServiceDao.listChildProjects(inputObject.getCustomerCode(), inputObject.getProjectId());
		List<String> childProjectIds = new ArrayList<String>();
		for(LinkedHashMap<String, String> childProject : childProjects) {
			childProjectIds.add(childProject.get("id"));
		}
		Map<String,List<String>> retailerStoreFormatMap = processImageDao.getListRetailersAndStoreFormats(childProjectIds);
		
		List<String> retailerList = new ArrayList<String>();
		List<String> storeFormatList = new ArrayList<String>();
		for(String retailer : retailerStoreFormatMap.keySet()) {
			retailerList.add(retailer);
			storeFormatList.addAll(retailerStoreFormatMap.get(retailer));
		}
		summary.put("retailers", retailerList);
		summary.put("storeFormats", storeFormatList);
		
		resultList.add(summary);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandSummaryNew----------------\n");

		return resultList;
	}

	@Override
	public List<ProjectStoreResultWithUPC> getProjectStoreDistribution(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoreDistribution----------------\n");

		Map<String, List<Map<String, String>>> storeImageData = processImageDao.getProjectStoreDistributionData(
				inputObject.getProjectId(), inputObject.getStoreId());

		String month = null;

		List<LinkedHashMap<String, String>> resultData = processImageDao.getProjectStoreResults(
				inputObject.getProjectId(), inputObject.getStoreId(), month, inputObject.getTaskId());

		Map<String, List<Map<String, String>>> storeImageMetaData = processImageDao.getProjectStoreImageMetaData(
				inputObject.getProjectId(), inputObject.getStoreId());

		List<ProjectStoreResultWithUPC> result = ConverterUtil.convertProjectDataResultToObject(storeImageData,
				resultData, storeImageMetaData, null);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoreResults----------------\n");

		return result;
	}

	@Override
	public File getStoreLevelDistributionReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getStoreLevelDistributionReport----------------\n");

		List<Map<String, String>> storeLevelDistribution = processImageDao.getStoreLevelDistribution(inputObject.getProjectId());
		
		Map<String, String> photoQualityReportByStoreVisitMap = processImageDao.getPhotoQualityReportByStoreVisit(inputObject.getProjectId());
		
		String allWaves = "%";
		List<LinkedHashMap<String,String>> distributionReport = shelfAnalysisDao.getDistributionSummaryStoreLevelData(inputObject.getProjectId(), allWaves);
		Map<String, String> distributionPercentageByStoreVisitMap = new HashMap<String,String>();
		for( LinkedHashMap<String, String> oneStoreVisit : distributionReport ) {
			String storeId = oneStoreVisit.get("storeId");
			String taskId = oneStoreVisit.get("taskId");
			distributionPercentageByStoreVisitMap.put(storeId+"#"+taskId, oneStoreVisit.get("skuDistribution"));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet storeSummarySheet = workbook.createSheet("Store Summary");
		XSSFSheet storeDistributionSheet = workbook.createSheet("Product distribution by store");
		workbook.setSheetOrder("Store Summary", 0);
		workbook.setSheetOrder("Product distribution by store", 1);
		
		Row storeSummarySheetHeaderRow = storeSummarySheet.createRow(0);
		List<String> storeSummarySheetHeaders = Arrays.asList(new String[] {
				"Retailer Store Number","Retailer","Street","City","State","ZIP","Wave Name","Visit Date","Distribution Percentage","Good Photo Percentage"});
		int colNum=0;
		for(String columnHeader : storeSummarySheetHeaders ) {
			Cell cell = storeSummarySheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		Row storeDistributionSheetHeaderRow = storeDistributionSheet.createRow(0);
		List<String> storeDistributionSheetHeaders = Arrays.asList(new String[] {
				"Retailer Store Number","Retailer","Street","City","State","ZIP","Wave Name","Visit Date","UPC","Brand Name","Product Name","Facing Count"});
		colNum=0;
		for(String columnHeader : storeDistributionSheetHeaders ) {
			Cell cell = storeDistributionSheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			List<String> reportedStoreVisits = new ArrayList<String>();
			
			for (Map<String, String> row : storeLevelDistribution) {
				
				String storeVisitKey = row.get("storeId") + "#" + row.get("taskId");

				if ( reportedStoreVisits.contains(storeVisitKey) ) {
					continue;
				} else {
					reportedStoreVisits.add(storeVisitKey);
				}

				Row summarySheetRow = storeSummarySheet.createRow(rowNum++);

				int summarySheetColNum = 0;

				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("retailerStoreId"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("retailer"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("street"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("city"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("state"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("zip"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("waveName"));
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(row.get("visitDate"));
				
				String distributionPercentage = distributionPercentageByStoreVisitMap.get(storeVisitKey);
				if ( StringUtils.isBlank(distributionPercentage) ) { distributionPercentage = "0.0"; };
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(DECIMAL_FORMATTER.format(Float.parseFloat(distributionPercentage) * 100));

				String goodPhotoPercentage =  photoQualityReportByStoreVisitMap.get(storeVisitKey);
				if ( StringUtils.isBlank(goodPhotoPercentage) ) { goodPhotoPercentage = "0.0"; };
				summarySheetRow.createCell(summarySheetColNum++).setCellValue(goodPhotoPercentage);
			}
			
			rowNum = 1;
			for (Map<String, String> row : storeLevelDistribution) {
				Row distributionSheetRow = storeDistributionSheet.createRow(rowNum++);

				int distributionSheetColNum = 0;

				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("retailerStoreId"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("retailer"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("street"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("city"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("state"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("zip"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("waveName"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("visitDate"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("upc"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("brand_name"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("product_short_name"));
				distributionSheetRow.createCell(distributionSheetColNum++).setCellValue(row.get("facing"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getStoreLevelDistributionReport----------------\n");
		
		//Now write the workbook to file and return
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}

		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandShares(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBrandShares----------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		/*LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		String month = inputObject.getMonth();
		// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
		// this logic won't work by 2100 :)
		String[] parts = month.split("/");
		month = "20" + parts[1] + parts[0];

		boolean isBrandRollup = "brand".equals(inputObject.getRollup());

		List<Map<String, String>> brandShareByMonth = processImageDao.getDetailedBrandShareByMonth(inputObject.getProjectId(), month, inputObject.getRollup());
		int totalUpcsCount = 0, totalFacingsCount = 0;
		float totalAverageUpcPerStore = 0f;
		List<LinkedHashMap<String, Object>> brandShare = new ArrayList<LinkedHashMap<String, Object>>();

		List<Map<String, String>> shelfLevelinfo = processImageDao.getShelfLevelFacings(inputObject.getProjectId(), month,null);

		for (Map<String, String> oneBrandShare : brandShareByMonth) {
			LinkedHashMap<String, Object> filteredBrandShare = new LinkedHashMap<String, Object>();

			List<Map<String, Object>> facings = StandardTemplates.getShelfLevelTemplate();

			if ( isBrandRollup ) {
				filteredBrandShare.put("brandName", oneBrandShare.get("brandName"));
				filteredBrandShare.put("upcCount", oneBrandShare.get("brandUpc"));
				filteredBrandShare.put("shareOfUpcs", oneBrandShare.get("brandUpcShare"));
				filteredBrandShare.put("facingCount", oneBrandShare.get("brandFacing"));
				filteredBrandShare.put("shareOfFacings", oneBrandShare.get("brandFacesShare"));
			} else {
				filteredBrandShare.put("mfgName", oneBrandShare.get("mfgName"));
				filteredBrandShare.put("upcCount", oneBrandShare.get("mfgUpc"));
				filteredBrandShare.put("shareOfUpcs", oneBrandShare.get("mfgUpcShare"));
				filteredBrandShare.put("facingCount", oneBrandShare.get("mfgFacing"));
				filteredBrandShare.put("shareOfFacings", oneBrandShare.get("mfgFacesShare"));
			}

			filteredBrandShare.put("averageUpcPerStore", oneBrandShare.get("avgUpcPerStore"));
			filteredBrandShare.put("averageFacingPerStore", oneBrandShare.get("avgFacingPerStore"));

			if ( isBrandRollup ) {
				for (Map<String, String> map : shelfLevelinfo) {

					if (oneBrandShare.get("brandName").equals(map.get("brandName"))) {
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

				if(facings.get(3).get("facingCount").equals("0")) {
					facings.remove(3);
				}
				filteredBrandShare.put("shelfLevel", facings);
			}

			brandShare.add(filteredBrandShare);

			totalUpcsCount = totalUpcsCount + Integer.parseInt(oneBrandShare.get(isBrandRollup ? "brandUpc" : "mfgUpc"));
			totalFacingsCount = totalFacingsCount + Integer.parseInt(oneBrandShare.get(isBrandRollup ? "brandFacing" : "mfgFacing"));
			totalAverageUpcPerStore = totalAverageUpcPerStore + Float.parseFloat(oneBrandShare.get("avgUpcPerStore"));

		}

		result.put(isBrandRollup ? "brands" : "manufacturers", brandShare);

		LinkedHashMap<String, String> summary = new LinkedHashMap<String, String>();
		String storeIdCountStr = processImageDao.getDistinctStoreIdCountByMonth(inputObject.getProjectId(), month);
		summary.put("storeIds", storeIdCountStr);
		summary.put(isBrandRollup ? "totalBrands" : "totalManufacturers", "" + brandShare.size());
		summary.put("totalUpcs", "" + totalUpcsCount);
		summary.put("totalFacings", "" + totalFacingsCount);
		int storeIdCount = Integer.parseInt(storeIdCountStr);
		summary.put("averageUpcsPerStore", "" + totalAverageUpcPerStore);

		String averageFacingPerStore = "" + ((float) totalFacingsCount / storeIdCount);
		if (averageFacingPerStore.length() > 5) {
			averageFacingPerStore = averageFacingPerStore.substring(0, 5);
		}
		summary.put("averageFacingPerStore", averageFacingPerStore);
		result.put("summary", summary);

		resultList.add(result);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandShares----------------\n");*/

		return resultList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesNew(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBrandSharesNew----------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		String month = inputObject.getMonth();
		
		if ( StringUtils.isBlank(month) || month.equals("-9") ) {
			month = "";
		} else {
			// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
			// this logic won't work by 2100 :)
			String[] parts = month.split("/");
			month = "20" + parts[1] + parts[0];
		}
		
		String waveId = inputObject.getWaveId();
		
		if ( StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
			waveId = "%";
		}

		boolean isBrandRollup = "brand".equals(inputObject.getRollup());
		
		String retailerToFilter = inputObject.getRetailer();
		String modularToFilter = inputObject.getModular();
		String subCategoryToFilter = inputObject.getSubCategory();
		String storeFormatToFilter = inputObject.getStoreFormat();
		String customerCode = inputObject.getCustomerCode();
		
		int parentProjectId = inputObject.getProjectId();
		List<LinkedHashMap<String,String>> childProjects = metaServiceDao.listChildProjects(customerCode, parentProjectId);
		List<String> childProjectIds = new ArrayList<String>();
		for(LinkedHashMap<String, String> childProject : childProjects) {
					childProjectIds.add(childProject.get("id"));
		}
		

		List<Map<String, String>> brandShareByMonth = processImageDao.getDetailedBrandShareByMonthNew(inputObject.getProjectId(), childProjectIds, 
				month,waveId, inputObject.getRollup(), subCategoryToFilter, modularToFilter, storeFormatToFilter,retailerToFilter);
		
		int totalUpcsCount = 0, totalFacingsCount = 0, sumOfBrandTotalUpc = 0;

		List<LinkedHashMap<String, Object>> brandShare = new ArrayList<LinkedHashMap<String, Object>>();

		List<Map<String, String>> shelfLevelinfo = processImageDao.getShelfLevelFacingsNew(inputObject.getProjectId(), childProjectIds,
				month, waveId, subCategoryToFilter, modularToFilter, storeFormatToFilter, retailerToFilter);

		for (Map<String, String> oneBrandShare : brandShareByMonth) {
			LinkedHashMap<String, Object> filteredBrandShare = new LinkedHashMap<String, Object>();

			List<Map<String, Object>> facings = StandardTemplates.getShelfLevelTemplate();

			if ( isBrandRollup ) {
				filteredBrandShare.put("brandName", oneBrandShare.get("brandName"));
				filteredBrandShare.put("upcCount", oneBrandShare.get("brandUpc"));
				filteredBrandShare.put("shareOfUpcs", oneBrandShare.get("brandUpcShare"));
				filteredBrandShare.put("facingCount", oneBrandShare.get("brandFacing"));
				filteredBrandShare.put("shareOfFacings", oneBrandShare.get("brandFacesShare"));
			} else {
				filteredBrandShare.put("mfgName", oneBrandShare.get("mfgName"));
				filteredBrandShare.put("upcCount", oneBrandShare.get("mfgUpc"));
				filteredBrandShare.put("shareOfUpcs", oneBrandShare.get("mfgUpcShare"));
				filteredBrandShare.put("facingCount", oneBrandShare.get("mfgFacing"));
				filteredBrandShare.put("shareOfFacings", oneBrandShare.get("mfgFacesShare"));
			}

			filteredBrandShare.put("averageUpcPerStore", oneBrandShare.get("avgUpcPerStore"));
			filteredBrandShare.put("averageFacingPerStore", oneBrandShare.get("avgFacingPerStore"));
			filteredBrandShare.put("stores", oneBrandShare.get("stores"));

			if ( isBrandRollup ) {
				for (Map<String, String> map : shelfLevelinfo) {

					if (oneBrandShare.get("brandName").equals(map.get("brandName"))) {
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

				if(facings.get(3).get("facingCount").equals("0")) {
					facings.remove(3);
				}
				filteredBrandShare.put("shelfLevel", facings);
			}

			brandShare.add(filteredBrandShare);

			totalUpcsCount = totalUpcsCount + Integer.parseInt((oneBrandShare.get(isBrandRollup ? "brandUpc" : "mfgUpc")).replaceAll("\\.0*$", ""));
			totalFacingsCount = totalFacingsCount + Integer.parseInt((oneBrandShare.get(isBrandRollup ? "brandFacing" : "mfgFacing")).replaceAll("\\.0*$", ""));
			sumOfBrandTotalUpc = sumOfBrandTotalUpc + Integer.parseInt((oneBrandShare.get(isBrandRollup ? "brandTotalUpc" : "mfgTotalUpc")).replaceAll("\\.0*$", ""));

		}

		result.put(isBrandRollup ? "brands" : "manufacturers", brandShare);

		LinkedHashMap<String, String> summary = new LinkedHashMap<String, String>();
		String storeIdCountStr = processImageDao.getDistinctStoreIdCountByMonth(inputObject.getProjectId(), childProjectIds,
				month, waveId, subCategoryToFilter, modularToFilter, storeFormatToFilter, retailerToFilter);
		summary.put("storeIds", storeIdCountStr);
		summary.put(isBrandRollup ? "totalBrands" : "totalManufacturers", "" + brandShare.size());
		summary.put("totalUpcs", "" + totalUpcsCount);
		summary.put("totalFacings", "" + totalFacingsCount);
		int storeIdCount = Integer.parseInt(storeIdCountStr);

		String averageUpcsPerStore =  (storeIdCount == 0) ? "0" : ""+DECIMAL_FORMATTER.format((float) sumOfBrandTotalUpc / storeIdCount);
		summary.put("averageUpcsPerStore", averageUpcsPerStore);
		
		String averageFacingPerStore =  (storeIdCount == 0) ? "0" : ""+DECIMAL_FORMATTER.format((float) totalFacingsCount / storeIdCount);
		summary.put("averageFacingPerStore", averageFacingPerStore);
		result.put("summary", summary);

		resultList.add(result);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandSharesNew----------------\n");

		return resultList;
	}

	public String computeShelfLevel(String imgAnalysedData) {

		String imageMetaData = imgAnalysedData;
		String shelfAnalysisURI = env.getProperty("python_server_url_"+env.getProperty("instance"))+"/getShelfAnalysis";
		String s = "";

		LOGGER.info("---------------ProcessImageServiceImpl starts computeShelfLevel----------------\n");

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(shelfAnalysisURI);
		CloseableHttpResponse response = null;
		HttpEntity httpEntity = null;
		StringEntity entity = null;


		try {
			entity = new StringEntity(imageMetaData);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			response = client.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				LOGGER.info("---------------ProcessImageServiceImpl gets response from ShelfAnalysis service----------------\n");
				httpEntity = response.getEntity();
				s = EntityUtils.toString(httpEntity);
				LOGGER.info("---------------ProcessImageServiceImpl ends computeShelfLevel----------------\n");

				return s;

			} else {

				LOGGER.info("---------------ProcessImageServiceImpl didn't get response from ShelfAnalysis service----------------\n");
				String dummyShelfData = getDummyShelfLevels(imageMetaData);
				LOGGER.info("---------------ProcessImageServiceImpl inputs dummy shelf levels----------------\n");

				return dummyShelfData;
			}

		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl gets exception in computeShelfLevel : {}", e);
			e.printStackTrace();
			String dummyShelfData = getDummyShelfLevels(imageMetaData);
			LOGGER.info("---------------ProcessImageServiceImpl inputs dummy shelf levels----------------\n");
			return dummyShelfData;
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public String getDummyShelfLevels(String imgAnalysedData) {

		List<ImageAnalysis> imageAnalysisList = new ArrayList<ImageAnalysis>();
		Map<String, Object> result = new HashMap<String, Object>();

		JsonObject obj = new JsonParser().parse(imgAnalysedData).getAsJsonObject();

		JsonArray skusArray = obj.get("skus").getAsJsonArray();
		for (JsonElement skus : skusArray) {

			ImageAnalysis imageAnalysis = new ImageAnalysis();
			JsonObject upcEntry = skus.getAsJsonObject();

			imageAnalysis.setUpc(upcEntry.get("UPC").getAsString().trim());
			imageAnalysis.setLeftTopX(upcEntry.get("LEFT_TOP_X").getAsString().trim());
			imageAnalysis.setLeftTopY(upcEntry.get("LEFT_TOP_Y").getAsString().trim());
			imageAnalysis.setWidth(upcEntry.get("Width").getAsString().trim());
			imageAnalysis.setHeight(upcEntry.get("Height").getAsString().trim());
			imageAnalysis.setUpcConfidence(upcEntry.get("UPC_Confidence").getAsString().trim());
			imageAnalysis.setAlternateUpc(upcEntry.get("Alt_UPC").getAsString().trim());
			imageAnalysis.setAlternateUpcConfidence(upcEntry.get("Alt_UPC_Confidence").getAsString().trim());
			imageAnalysis.setPromotion(upcEntry.get("Promotion").getAsString().trim());
			imageAnalysis.setPrice(upcEntry.get("Price").getAsString().trim());
			imageAnalysis.setPriceLabel(upcEntry.get("Price_Label").getAsString().trim());
			imageAnalysis.setPriceConfidence(upcEntry.get("Price_Confidence").getAsString().trim());
			
			String compliant = upcEntry.get("Compliant") != null ? upcEntry.get("Compliant").getAsString() : "0";
			imageAnalysis.setCompliant(compliant);
			
			imageAnalysis.setShelfLevel("NA");

			imageAnalysisList.add(imageAnalysis);
		}
		List<String> imageHash = new ArrayList<>();

		result.put("skus", imageAnalysisList);
		result.put("imageRotation", obj.get("imageRotation").getAsString());
		result.put("imageResultComments", obj.get("imageResultComments").getAsString());
		result.put("imageResultCode", obj.get("imageResultCode").getAsString());
		result.put("pixelsPerInch", obj.get("pixelsPerInch").getAsString());
		result.put("oosCount", obj.get("oosCount").getAsString());
		result.put("oosPercentage", obj.get("oosPercentage").getAsString());
		result.put("imageAngle", obj.get("imageAngle").getAsString());
		result.put("shelfLevels", obj.get("shelfLevels").getAsString());
		imageHash.add(obj.get("imageHashScore").getAsString());
		result.put("imageHashScore", imageHash);
		result.put("imageNotUsable", obj.get("imageNotUsable").getAsString());
		result.put("imageNotUsableComment", obj.get("imageNotUsableComment").getAsString());
		if ( obj.get("imageReviewRecommendations") != null ) {
			ArrayList recommendations = new Gson().fromJson(obj.get("imageReviewRecommendations").getAsJsonArray(),ArrayList.class);
			result.put("imageReviewRecommendations",recommendations);
		}
		
		Gson gson = new Gson();
		String retString = gson.toJson(result);
		return retString;

	}

	@Override
	public File getProjectBrandProductsReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectBrandProductsReport----------------\n");

		XSSFWorkbook workbook = new XSSFWorkbook();
		
		int parentProjectId = inputObject.getProjectId();
		List<LinkedHashMap<String,String>> childProjects = metaServiceDao.listChildProjects(inputObject.getCustomerCode(), parentProjectId);
		List<String> childProjectIds = new ArrayList<String>();
		for(LinkedHashMap<String, String> childProject : childProjects) {
				childProjectIds.add(childProject.get("id"));
		}
		
		List<Map<String, String>> waveList = metaServiceDao.getProjectWaves(inputObject.getProjectId());

		String fromMonth = inputObject.getFromMonth();
		String formattedFromMonth = "";
		if ( StringUtils.isBlank(fromMonth) || fromMonth.equals("-9") ) {
			formattedFromMonth = "";
		} else {
			// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
			// this logic won't work by 2100 :)
			String[] parts = fromMonth.split("/");
			formattedFromMonth = "20" + parts[1] + parts[0];
		}
		
		String toMonth = inputObject.getToMonth();
		String formattedToMonth = "";
		if ( StringUtils.isBlank(toMonth) || toMonth.equals("-9") ) {
			formattedToMonth = "";
		} else {
			// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
			// this logic won't work by 2100 :)
			String[] parts = toMonth.split("/");
			formattedToMonth = "20" + parts[1] + parts[0];
		}
		
		String fromWaveId = inputObject.getFromWave();
		String fromWaveName = "";
		if ( StringUtils.isBlank(fromWaveId) || fromWaveId.equals("-9") ) {
			fromWaveId = "%";
		} else {
			for( Map<String, String> oneWave : waveList ) {
				if (fromWaveId.equals(oneWave.get("waveId")) ) {
					fromWaveName = oneWave.get("waveName");
				}
			}
		}
		
		String toWaveId = inputObject.getToWave();
		String toWaveName = "";
		if ( StringUtils.isBlank(toWaveId) || toWaveId.equals("-9") ) {
			toWaveId = "%";
		} else {
			for( Map<String, String> oneWave : waveList ) {
				if (toWaveId.equals(oneWave.get("waveId")) ) {
					toWaveName = oneWave.get("waveName");
				}
			}
		}
		
		List<Map<String, String>> storeLevelDistributionForFromDataset = processImageDao.getProjectBrandProducts(inputObject.getProjectId(),formattedFromMonth, fromWaveId);

		if ( StringUtils.isNotBlank(formattedFromMonth) ) {
			prepareSKULevelReport(fromMonth, workbook, storeLevelDistributionForFromDataset,"month");
		} else {
			List<Map<String, String>> storeLevelBrandShareForFromDataset = processImageDao.getProjectAllStoreShareOfShelf(inputObject.getProjectId(),childProjectIds,fromWaveId);
			prepareSKULevelReport(fromWaveName, workbook, storeLevelDistributionForFromDataset,"wave");
			prepareBrandLevelReport(fromWaveName, workbook, storeLevelBrandShareForFromDataset,"wave");
			prepareStoreLevelReport(fromWaveName, workbook, storeLevelBrandShareForFromDataset,"wave");
		}
			
		if ( (StringUtils.isNotBlank(formattedFromMonth) && StringUtils.isNotBlank(formattedToMonth)) || 
				(!fromWaveId.equals("%") && !toWaveId.equals("%"))) {
			List<Map<String, String>> storeLevelDistributionForToDataset = processImageDao.getProjectBrandProducts(inputObject.getProjectId(),formattedToMonth, toWaveId);

			if ( StringUtils.isNotBlank(formattedToMonth) ) {
				prepareSKULevelReport(toMonth, workbook, storeLevelDistributionForToDataset,"month");
			} else {
				List<Map<String, String>> storeLevelBrandShareForToDataset = processImageDao.getProjectAllStoreShareOfShelf(inputObject.getProjectId(),childProjectIds,toWaveId);
				prepareSKULevelReport(toWaveName, workbook, storeLevelDistributionForToDataset,"wave");
				prepareBrandLevelReport(toWaveName, workbook, storeLevelBrandShareForToDataset,"wave");
				prepareStoreLevelReport(toWaveName, workbook, storeLevelBrandShareForToDataset,"wave");
			}
		}

		//Now write the workbook to file and return
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandProductsReport----------------\n");

		File f = new File(tempFilePath);
		return f;
	}

	private void prepareSKULevelReport(String sheetName, XSSFWorkbook workbook,
			List<Map<String, String>> skuLevelDistribution, String type) {
		LOGGER.info("---------------ProcessImageService Starts prepareSKULevelReport::sheetName={},type={}----------------", sheetName,type);
		
		String formattedSheetName = sheetName;
		String typeBasedColumnHeader = "";
		if ( type.equals("month") ) {
			String[] parts = sheetName.split("/");
			LocalDate ld = LocalDate.parse( "20"+parts[1]+"-"+parts[0]+"-01" ) ;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "MMM uuuu" , Locale.US ) ;
			formattedSheetName = ld.format(formatter);
			typeBasedColumnHeader = "Visit Month";
		} else {
			typeBasedColumnHeader = "Wave";
			formattedSheetName = sheetName + " - SKU";
		}
		
		XSSFSheet sheet = workbook.createSheet(formattedSheetName);
		
		Row sheetHeaderRow = sheet.createRow(0);
		
		List<String> headers = Arrays.asList(new String[] {
				"Retailer","Retailer Store","Street","City","State","ZIP","Store Type","Visit Date",typeBasedColumnHeader,"Modular",
				"Brand","Sub Category","UPC","Product Short Name","Facings","Price","Promoted","Shelf Level" });

		int colNum=0;
		for(String columnHeader : headers ) {
			Cell cell = sheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			for (Map<String, String> oneStore : skuLevelDistribution) {
				
				Row dataRow = sheet.createRow(rowNum++);
				
				int columnIndex = 0;

				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailer"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailerStoreId"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("street"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("city"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("state"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("zip"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("storeFormat"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("visitDate"));
				dataRow.createCell(columnIndex++).setCellValue(sheetName);
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("linearFootage"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("brand"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("product_sub_type"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("upc"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("product_short_name"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("facing"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("price"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("promoted"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("shelfLevel"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
		LOGGER.info("---------------ProcessImageService Ends prepareSKULevelReport----------------");
	}
	
	private void prepareBrandLevelReport(String sheetName, XSSFWorkbook workbook,
			List<Map<String, String>> storeLevelDistribution, String type) {
		LOGGER.info("---------------ProcessImageService Starts prepareBrandLevelReport::sheetName={},type={}----------------", sheetName,type);
		
		String formattedSheetName = sheetName;
		String typeBasedColumnHeader = "";
		if ( type.equals("month") ) {
			String[] parts = sheetName.split("/");
			LocalDate ld = LocalDate.parse( "20"+parts[1]+"-"+parts[0]+"-01" ) ;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "MMM uuuu" , Locale.US ) ;
			formattedSheetName = ld.format(formatter);
			typeBasedColumnHeader = "Visit Month";
		} else {
			typeBasedColumnHeader = "Wave";
			formattedSheetName = sheetName + " - Brand";
		}
		
		XSSFSheet sheet = workbook.createSheet(formattedSheetName);
		
		Row sheetHeaderRow = sheet.createRow(0);
		
		List<String> headers = Arrays.asList(new String[] {
				"Retailer","Retailer Store","Street","City","State","ZIP","Store Type","Visit Date",typeBasedColumnHeader,"Modular",
				"Brand","Share of Shelf (%)" });

		int colNum=0;
		for(String columnHeader : headers ) {
			Cell cell = sheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			for (Map<String, String> oneStore : storeLevelDistribution) {
				
				Row dataRow = sheet.createRow(rowNum++);
				
				int columnIndex = 0;

				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailer"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailerStoreId"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("street"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("city"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("state"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("zip"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("storeFormat"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("visitDate"));
				dataRow.createCell(columnIndex++).setCellValue(sheetName);
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("linearFootage"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("brandName"));
				dataRow.createCell(columnIndex++).setCellValue(oneStore.get("shareOfFacings"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
		LOGGER.info("---------------ProcessImageService Ends prepareBrandLevelReport----------------");
	}
	
	private void prepareStoreLevelReport(String sheetName, XSSFWorkbook workbook,
			List<Map<String, String>> storeLevelDistribution, String type) {
		LOGGER.info("---------------ProcessImageService Starts prepareStoreLevelReport::sheetName={},type={}----------------", sheetName,type);
		
		String formattedSheetName = sheetName;
		String typeBasedColumnHeader = "";
		if ( type.equals("month") ) {
			String[] parts = sheetName.split("/");
			LocalDate ld = LocalDate.parse( "20"+parts[1]+"-"+parts[0]+"-01" ) ;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "MMM uuuu" , Locale.US ) ;
			formattedSheetName = ld.format(formatter);
			typeBasedColumnHeader = "Visit Month";
		} else {
			typeBasedColumnHeader = "Wave";
			formattedSheetName = sheetName + " - Store";
		}
		
		XSSFSheet sheet = workbook.createSheet(formattedSheetName);
		
		Row sheetHeaderRow = sheet.createRow(0);
		
		List<String> headers = Arrays.asList(new String[] {
				"Retailer","Retailer Store","Street","City","State","ZIP","Store Type","Visit Date",typeBasedColumnHeader,"Modular",
				"OSA %" });

		int colNum=0;
		for(String columnHeader : headers ) {
			Cell cell = sheetHeaderRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			List<String> storeVisits = new ArrayList<String>();
			for (Map<String, String> oneStore : storeLevelDistribution) {
				String storeVisitKey = oneStore.get("storeId")+oneStore.get("taskId");
				if (storeVisits.contains(storeVisitKey) ) {
					continue;
				} else {
					Row dataRow = sheet.createRow(rowNum++);
					
					int columnIndex = 0;

					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailer"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("retailerStoreId"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("street"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("city"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("state"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("zip"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("storeFormat"));
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("visitDate"));
					dataRow.createCell(columnIndex++).setCellValue(sheetName);
					dataRow.createCell(columnIndex++).setCellValue(oneStore.get("linearFootage"));
					String OSAPercentage = oneStore.get("OSAPercentage");
					try {
						OSAPercentage = "" + Float.parseFloat(OSAPercentage) * 100;
					} catch (Exception e) {
						OSAPercentage = "0";
					}
					dataRow.createCell(columnIndex++).setCellValue(OSAPercentage);
					
					storeVisits.add(storeVisitKey);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
		LOGGER.info("---------------ProcessImageService Ends prepareStoreLevelReport----------------");
	}

	@Override
	public Map<String, List<LinkedHashMap<String,String>>> getImagesByProjectIdList(List<String> projectIdList){
		LOGGER.info("---------------ProcessImageService Starts getImagesByProjectIdList project ids= {}",projectIdList);
		Map<String, List<LinkedHashMap<String,String>>> resultList = processImageDao.getImagesByProjectIdList(projectIdList);

		LOGGER.info("---------------ProcessImageService Ends getImagesByProjectIdList project ids={}",projectIdList);
		return resultList;
	}

	private List<LinkedHashMap<String, String>> getImagesByStoreVisit(int projectId, String storeId, String taskId){
		LOGGER.info("---------------ProcessImageServiceImpl Starts getImagesByStoreVisit projectId={}, storeId={}, taskId={}",projectId,storeId,taskId);

		List<LinkedHashMap<String,String>> resultList = processImageDao.getImageDetailsByStoreVisit(projectId, storeId, taskId);
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getImagesByStoreVisit projectId={}, storeId={}, taskId={}",projectId,storeId,taskId);
		return resultList;
	}

	private String invokeStoreAnalysis(String inputJSON) {
		LOGGER.info("---------------ProcessImageServiceImpl starts invokeStoreAnalysis ----------------\n");
		
		LOGGER.info("---------------ProcessImageServiceImpl invokeStoreAnalysis Payload {}",inputJSON);

		String storeAnalysisURL = env.getProperty("store_analysis_url_"+env.getProperty("instance")).trim();
		String result = "";

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(storeAnalysisURL);
		CloseableHttpResponse response = null;
		HttpEntity httpEntity = null;
		StringEntity entity = null;

		try {
			entity = new StringEntity(inputJSON);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			response = client.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				LOGGER.info("---------------ProcessImageServiceImpl invokeStoreAnalysis Response {}",response.getEntity());
				httpEntity = response.getEntity();
				result = EntityUtils.toString(httpEntity);

				LOGGER.info("---------------ProcessImageServiceImpl ends invokeStoreAnalysis----------------\n");
				return result;

			} else {
				LOGGER.error("---------------ERROR: ProcessImageServiceImpl ends invokeStoreAnalysis: Response not equal 200 {}", response );
				return result;
			}

		} catch (Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl gets exception in invokeStoreAnalysis : {}", e );
			LOGGER.info("--------------- EXCEPTION ProcessImageServiceImpl invokeStoreAnalysis----------------\n");
			return result;
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method to submit for store analysis 
	 * @param projectId
	 * @param storeId
	 * @param taskId
	 */
	private void submitStoreForAnalysis(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts submitStoreForAnalysis----------------\n");
		List<LinkedHashMap<String, String>> images = getImagesByStoreVisit(projectId, storeId, taskId);

		JsonObject storeVisitObject = new JsonObject();
		storeVisitObject.addProperty("projectId", projectId);
		storeVisitObject.addProperty("storeId", storeId);
		storeVisitObject.addProperty("taskId", taskId);
		storeVisitObject.addProperty("imageCount", images.size());
		
		String categoryId = "";
		JsonArray imagesArray = new JsonArray();
		for(LinkedHashMap<String, String> image : images) {
			JsonObject imageObject = new JsonObject();
			imageObject.addProperty("imageUUID",image.get("imageUUID"));
			imageObject.addProperty("sequenceNumber",image.get("sequenceNumber"));
			imageObject.addProperty("imageFilePath",cloudStorageService.getBucketPath(true)+projectId+"/"+image.get("imageUUID")+".jpg");
			imagesArray.add(imageObject);
			categoryId = image.get("categoryId");
		}
		
		storeVisitObject.add("imageList", imagesArray);
		storeVisitObject.addProperty("categoryId", categoryId);

		invokeStoreAnalysis(storeVisitObject.toString()); //no-waiting

		LOGGER.info("---------------ProcessImageServiceImpl Ends submitStoreForAnalysis----------------\n");
	}

	/**
	 * Method to generate images JSON
	 * @param imageStoreNews
	 * 
	 * @return image wise detections
	 */
	private JSONArray prepareImagesStitchingJSONInput(int projectId, List<LinkedHashMap<String, String>> imageStoreNews) {
		JSONArray imageDetectionsObj = new JSONArray();
		for (LinkedHashMap<String, String> imageStore : imageStoreNews) {
			
			List<LinkedHashMap<String, String>> result = shelfAnalysisService.getProductDetections(imageStore.get("imageUUID"));
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("imageUUID",imageStore.get("imageUUID"));
			jsonObject.put("sequenceNumber",imageStore.get("sequenceNumber"));
			jsonObject.put("imageFilePath",cloudStorageService.getBucketPath(true)+projectId+"/"+imageStore.get("imageUUID")+".jpg");
			jsonObject.put("imageRotation",imageStore.get("imageRotation"));
			//jsonObject.put("origWidth",imageStore.get("origWidth"));
			//jsonObject.put("origHeight",imageStore.get("origHeight"));
			//jsonObject.put("newWidth",imageStore.get("newWidth"));
			//jsonObject.put("newHeight",imageStore.get("newHeight"));
			//jsonObject.put("pixelsPerInch",imageStore.get("pixelsPerInch"));

			JSONArray productDetectionArray = new JSONArray();
			if(!result.isEmpty()){
				for (LinkedHashMap<String, String> imageAnalysisModel : result) {
					JSONObject object = new JSONObject();

					object.put("id", imageAnalysisModel.get("id"));
					object.put("upc", imageAnalysisModel.get("upc"));
					object.put("x", imageAnalysisModel.get("leftTopX"));
					object.put("y", imageAnalysisModel.get("leftTopY"));
					object.put("w", imageAnalysisModel.get("width"));
					object.put("h", imageAnalysisModel.get("height"));
					//object.put("isBrand", (imageAnalysisModel.get("isBrandLevelUPC") == null ) ? "0" : imageAnalysisModel.get("isBrandLevelUPC"));
					//object.put("leftTopX", imageAnalysisModel.get("leftTopX"));
					//object.put("leftTopY", imageAnalysisModel.get("leftTopY"));
					//object.put("width", imageAnalysisModel.get("width"));
					//object.put("height", imageAnalysisModel.get("height"));
					//object.put("productCategory", (imageAnalysisModel.get("productCategory") == null ) ? "" : imageAnalysisModel.get("productCategory"));
					//object.put("productShortName", imageAnalysisModel.get("productShortName") == null ? "" : imageAnalysisModel.get("productShortName"));
					//object.put("productManufacturer", imageAnalysisModel.get("productManufacturer") == null ? "" : imageAnalysisModel.get("productManufacturer"));
					//object.put("productBrand", imageAnalysisModel.get("productBrand") == null ? "" : imageAnalysisModel.get("productBrand"));
					//object.put("productSubCategory", imageAnalysisModel.get("productSubCategory") == null ? "":imageAnalysisModel.get("productSubCategory"));
					//object.put("productWidth", (imageAnalysisModel.get("productWidth") == null ) ? "" : imageAnalysisModel.get("productWidth"));
					//object.put("productHeight", (imageAnalysisModel.get("productHeight") == null ) ? "" : imageAnalysisModel.get("productHeight"));
					//object.put("productDepth", (imageAnalysisModel.get("productDepth") == null ) ? "" : imageAnalysisModel.get("productDepth"));
					//object.put("shelfLevel", "NA");
					//object.put("isDuplicate", "0");

					productDetectionArray.put(object);
				}
			}
			jsonObject.put("productDetections",productDetectionArray);
			imageDetectionsObj.put(jsonObject);
		}
		return imageDetectionsObj;
	}

	@Override
	public void updateUPCForImageAnalysis(String newUpc,String id, String imageUUID, String leftTopX, String leftTopY, String shelfLevel,
			String price, String promotion, String compliant) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateUPCForImageAnalysis----------------\n");

		processImageDao.updateUPCForImageAnalysis(newUpc, id, imageUUID, leftTopX, leftTopY, shelfLevel, price, promotion, compliant);

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateUPCForImageAnalysis----------------\n");

	}

	@Override
	public void addImageAnalysisNew(String newUpc, String imageUUID, String leftTopX, String leftTopY,
									String shelfLevel, int projectId,
									String storeId, String height, String width,
									String price, String promotion, String compliant) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts AddImageAnalysisNew----------------\n");

		processImageDao.addImageAnalysisNew(newUpc, imageUUID, leftTopX, leftTopY, shelfLevel, projectId, storeId, height, width, price, promotion, compliant);

		LOGGER.info("---------------ProcessImageServiceImpl Ends AddImageAnalysisNew----------------\n");

	}

	@Override
	public void updateProjectImagePath(int projectId, String imagePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateProjectImagePath----------------\n");

		processImageDao.updateProjectImagePath(projectId, imagePath);

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateProjectImagePath----------------\n");

	}

	@Override
	public Map<String, String> getProjectByCustomerCodeAndCustomerProjectId(int projectId) {
		LOGGER.info("---------------ProcessImageServiceImpl getProjectByCustomerCodeAndCustomerProjectId----------------\n");

		return processImageDao.getProjectByCustomerCodeAndCustomerProjectId(projectId);
	}

	public String generateStoreId() {
		LOGGER.info("--------------- ProceImageServiceImpl Starts generateStoreId ----------------\n");
		String storeId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		LOGGER.info("################################## StoreId: {}", storeId );
		List<LinkedHashMap<String, String>> result = metaServiceDao.getStoreMasterByStoreId(storeId);
		if(null != result && !result.isEmpty()){
			storeId = generateStoreId();
			LOGGER.info("Generating storeId again StoreId: {}", storeId);
		}
		return storeId;
	}

	@Override
	public File getPremiumReportCsv(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getPremiumReportCsv----------------\n");
		
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
		outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
		inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		
		List<Map<String, Object>> storeQuestionUPCMap = processImageDao.getQuestionsBasedDetections(inputObject.getProjectId());
		
		List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		String customerProjectId = projectDetail.get(0).get("customerProjectId");
		String projectName = projectDetail.get(0).get("projectName");
		String[] customerProjectIdParts = customerProjectId.trim().split("#");
		String actionId = customerProjectIdParts[0];
		String jobId = customerProjectIdParts[1];
		
		
		List<Map<String, String>> projectUpcs = metaServiceDao.getProjectUpcDetailWithMetaInfo(inputObject.getProjectId());
		
		projectUpcs.sort(Comparator.comparing((Map<String, String> m) -> (String) m.get("productName")));

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			
			String headers = "Job Id,Action Id,Action Description,Retailer Store Id,Retailer,Street,City,State Code,Zip,Service Order Id,Rep Id,Photo Date,Photo Link,Question Id";

			if (projectUpcs != null && !projectUpcs.isEmpty()) {
				for (Map<String, String> projectUpc : projectUpcs) {
					headers = headers.concat("," + projectUpc.get("productName"));
				}
			}

			headers = headers.concat("\n");

			fileWriter.append(headers);

			for (Map<String, Object> row : storeQuestionUPCMap) {
				Map<String,Object> questionDetectionMap = (Map<String, Object>) row.get("questions");
				for(String oneQuestionId : questionDetectionMap.keySet()) {
					StringBuilder result = new StringBuilder();
					result.append(jobId + ",");
					result.append(actionId + ",");
					result.append(projectName + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("retailerStoreId")) + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("retailer")) + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("street")).replace(",", " ") + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("city")) + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("stateCode")) + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("zip")) + ",");
					result.append(ConverterUtil.ifNullToEmpty((String)row.get("taskId")) + ",");
					
					Map<String,Object> detections = (Map<String,Object>)questionDetectionMap.get(oneQuestionId);
					String metaInfo = (String) detections.get("metaInfo");
					String[] metaInfoParts = metaInfo.split("#",3);
					String agentId = metaInfoParts[0];
					String dateId = metaInfoParts[1];
					String imageURL = metaInfoParts[2];
					
					try {
						dateId = outSdf.format(inSdf.parse(dateId));
					} catch (ParseException e) {
						LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
					}
					
					result.append(agentId + ",");
					result.append(dateId + ",");
					result.append(imageURL + ",");
					result.append(oneQuestionId + ",");
					
					List<String> upcs = (List<String>) detections.get("products");
					for (Map<String, String> projectUpc : projectUpcs) {
						if(upcs.contains(projectUpc.get("upc"))) {
							result.append("Yes,");
						} else {
							result.append("No,");
						}
					}
					
					String oneRow = result.toString();
					oneRow = oneRow.substring(0, oneRow.length()-1); //remove last comma
					fileWriter.append(oneRow + "\n");
				}
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getPremiumReportCsv----------------\n");

		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void pauseResumeImageAnalysis(String pauseImageAnalysis) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts pauseResumeImageAnalysis----------------\n");
		File lockFile = new File(PAUSE_IMAGE_ANALYSIS_FILE_LOCK);
		if ( pauseImageAnalysis.equals("true") ) {
			if ( !lockFile.exists() ) {
				try {
					LOGGER.info("---------------Creating lock file----------------");
					lockFile.createNewFile();
					LOGGER.info("---------------Created lock file----------------");
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
					throw new RuntimeException(e);
				}
			} else {
				LOGGER.info("---------------Lock file present. Already paused---------------");
			}
		} else {
			LOGGER.info("---------------Resuming processing. Deleting lock file----------------");
			lockFile.delete();
			LOGGER.info("---------------Lock file deleted----------------");
		}
		LOGGER.info("---------------ProcessImageServiceImpl Ends pauseResumeImageAnalysis----------------\n");
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getProjectShareOfShelfByBrand(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectShareOfShelfByBrand----------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		String month = inputObject.getMonth();
		if ( month.equalsIgnoreCase("-9") ) {
			month = "";
		} else {
			// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
			// this logic won't work by 2100 :)
			String[] parts = month.split("/");
			month = "20" + parts[1] + parts[0];
		}
		
		String waveId = inputObject.getWaveId();
		
		if ( StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
			waveId = "%";
		}

		inputObject.setRollup("brand");
		inputObject.setModular("all");
		
		String brandNameToFilter = inputObject.getBrandName();
		String subCategoryToFilter = inputObject.getSubCategory();
		String storeFormatToFilter = inputObject.getStoreFormat();
		String retailerToFilter = inputObject.getRetailer();
		String customerCode = inputObject.getCustomerCode();
		
		int parentProjectId = inputObject.getProjectId();
		List<LinkedHashMap<String,String>> childProjects = metaServiceDao.listChildProjects(customerCode, parentProjectId);
		List<String> childProjectIds = new ArrayList<String>();
		for(LinkedHashMap<String, String> childProject : childProjects) {
				childProjectIds.add(childProject.get("id"));
		}

		List<LinkedHashMap<String, Object>> brandShareSummaryAllBrands = this.getProjectBrandSharesNew(inputObject);
		List<LinkedHashMap<String, Object>> brandShareSummary = (List<LinkedHashMap<String, Object>>)brandShareSummaryAllBrands.get(0).get("brands");
		for( LinkedHashMap<String, Object> oneBrand : brandShareSummary ) {
			if ( oneBrand.get("brandName").equals(brandNameToFilter) ) {
				result.put("summary", oneBrand);
			}
		}
		
		List<Map<String,String>> shareByModular = processImageDao.getProjectBrandSharesByBrandForAllModulars(inputObject.getProjectId(), childProjectIds, 
				month, waveId, brandNameToFilter, subCategoryToFilter, storeFormatToFilter, retailerToFilter);
		result.put("modulars", shareByModular);
		
		Map<String,String> modularStoreCountMap = new HashMap<String,String>();
		for(Map<String,String> oneShare: shareByModular) {
			modularStoreCountMap.put(oneShare.get("modular"), oneShare.get("stores"));
		}
		
		List<Map<String,Object>> poductStoreCountByModular = processImageDao.getProjectStoreCountForAllProductsAllModulars(inputObject.getProjectId(), childProjectIds,
				month, waveId, brandNameToFilter, subCategoryToFilter, storeFormatToFilter, retailerToFilter);
		
		Map<String,List<Map<String,String>>> poductStoreCountByAllModular = new HashMap<String,List<Map<String,String>>>();
		Map<String,Integer> totalStoresForProduct = new HashMap<String,Integer>();

		
		for(Map<String,Object> oneModularData : poductStoreCountByModular ) {
			List<Map<String,String>> productStoreShareList = (List<Map<String, String>>) oneModularData.get("productStoreShare");
			String modular = (String) oneModularData.get("modular");
			int totalStoresForModular = Integer.parseInt((String)modularStoreCountMap.get(modular));
			for(Map<String,String> oneProductData : productStoreShareList ) {
				String productStores = oneProductData.get("stores");
				float percentageStores = (float) Integer.parseInt(productStores)/totalStoresForModular;
				oneProductData.put("percentageStores", totalStoresForModular == 0 ? "0.0" : DECIMAL_FORMATTER.format(percentageStores*100));
				
				String key = oneProductData.get("upc") + "#" + oneProductData.get("productName");
				if (poductStoreCountByAllModular.get(key) == null ) {
					poductStoreCountByAllModular.put(key, new ArrayList<Map<String,String>>());
					totalStoresForProduct.put(key, 0);
				}
				
				Map<String,String> productStoreShareInModular = new HashMap<String,String>();
				productStoreShareInModular.put("modular", modular);
				productStoreShareInModular.put("percentageStores", oneProductData.get("percentageStores"));
				productStoreShareInModular.put("stores", productStores);

				poductStoreCountByAllModular.get(key).add(productStoreShareInModular);
				totalStoresForProduct.put(key, totalStoresForProduct.get(key) + Integer.parseInt(productStores));
			}
		}
		
		result.put("productStoreShareByModular", poductStoreCountByModular);
		
		Map<String,Integer> sortedProductsByTotalStores = 
				totalStoresForProduct.entrySet().stream()
			    .sorted(Entry.<String,Integer>comparingByValue().reversed())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));	
		
		List<Map<String,Object>> poductStoreCountByAllModularFinal = new ArrayList<Map<String,Object>>();
		for( String product : sortedProductsByTotalStores.keySet() ) {
			String[] productParts = product.split("#",2);
			Map<String,Object> oneProductData = new HashMap<String,Object>();
			oneProductData.put("upc", productParts[0]);
			oneProductData.put("productName", productParts[1]);
			oneProductData.put("productStoreShare", poductStoreCountByAllModular.get(product));
			poductStoreCountByAllModularFinal.add(oneProductData);
		}
		result.put("productStoreShareByAllModulars", poductStoreCountByAllModularFinal);
		
		if ( "all".equals(retailerToFilter) ) {
			List<Map<String,String>> shareByRetailer = processImageDao.getProjectBrandSharesByBrandForAllRetailers(inputObject.getProjectId(), childProjectIds, 
					month, waveId, brandNameToFilter, subCategoryToFilter, storeFormatToFilter);
			result.put("retailers", shareByRetailer);
		} else {
			result.put("retailers",new ArrayList<Map<String,String>>());
		}
		
		if ( "all".equals(storeFormatToFilter) ) {
			List<Map<String,String>> shareByStoreFormat = processImageDao.getProjectBrandSharesByBrandForAllStoreFormats(inputObject.getProjectId(), childProjectIds, 
					month, waveId, brandNameToFilter, subCategoryToFilter, retailerToFilter);
			result.put("storeFormats", shareByStoreFormat);
		}
		
		if ( "all".equals(subCategoryToFilter) ) {
			List<Map<String,String>> shareBySubCategory = processImageDao.getProjectBrandSharesByBrandForAllSubCategories(inputObject.getProjectId(), childProjectIds, 
					month, waveId, brandNameToFilter, storeFormatToFilter, retailerToFilter);
			result.put("subCategories", shareBySubCategory);
		}
		
		resultList.add(result);

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectBrandSharesNew----------------\n");

		return resultList;
	}
	
	@Override
	public List<Map<String, String>> getProjectAllStoreShareOfShelfByBrand(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectAllStoreShareOfShelfByBrand----------------\n");

		String month = inputObject.getMonth();
		if ( month.equalsIgnoreCase("-9") ) {
			month = "";
		} else {
			// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
			// this logic won't work by 2100 :)
			String[] parts = month.split("/");
			month = "20" + parts[1] + parts[0];
		}
		
		String waveId = inputObject.getWaveId();
		
		if ( StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
			waveId = "%";
		}

		String brandNameToFilter = inputObject.getBrandName();
		String subCategoryToFilter = inputObject.getSubCategory();
		String storeFormatToFilter = inputObject.getStoreFormat();
		String retailerToFilter = inputObject.getRetailer();
		String customerCode = inputObject.getCustomerCode();
		
		int parentProjectId = inputObject.getProjectId();
		List<LinkedHashMap<String,String>> childProjects = metaServiceDao.listChildProjects(customerCode, parentProjectId);
		List<String> childProjectIds = new ArrayList<String>();
		for(LinkedHashMap<String, String> childProject : childProjects) {
				childProjectIds.add(childProject.get("id"));
		}
		
		List<Map<String,String>> storeShareOfShelfByBrand = processImageDao.getProjectAllStoreShareOfShelfByBrand(inputObject.getProjectId(), childProjectIds, 
				month, waveId, brandNameToFilter, subCategoryToFilter, storeFormatToFilter, retailerToFilter);
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectAllStoreShareOfShelfByBrand----------------\n");

		return storeShareOfShelfByBrand;
	}

	@Override
	public void deleteImageAnalysisNew(String id) {
		LOGGER.info("-----------ProcessImageServiceImpl Starts deleteImageAnalysisNew");

		processImageDao.deleteImageAnalysisNew(id);

		LOGGER.info("-----------ProcessImageServiceImpl Ends deleteImageAnalysisNew");
	}

	@Override
	public Map<String,List<String>> getDailyImageErrorStats() {
		LOGGER.info("-----------ProcessImageServiceImpl Starts getDailyImageErrorStats");

		Map<String,List<String>> stats = processImageDao.getDailyImageErrorStats();

		LOGGER.info("-----------ProcessImageServiceImpl Ends getDailyImageErrorStats");
		return stats;
	}
	
	@Override
	public void updateImageQualityParams(ImageStore imageToUpdate) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts updateImageQualityParams----------------\n");

		processImageDao.updateImageQualityParams(imageToUpdate);

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateImageQualityParams----------------\n");

	}
	
	@Override
	public void deleteAllDetectionsByImageUUID(String imageUUID) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts deleteAllDetectionsByImageUUID----------------\n");

		processImageDao.deleteAllDetectionsByImageUUID(imageUUID);

		LOGGER.info("---------------ProcessImageServiceImpl Ends deleteAllDetectionsByImageUUID----------------\n");

	}
	
	@Override
	public void updateStoreReviewStatus(String projectId, String storeId, String taskId, String reviewStatus, String status) {

		LOGGER.info("---------------ProcessImageServiceImpl Starts updateStoreReviewStatus----------------\n");
		
		//If store is reviewed, mark all images as reviewed. If store is not reviewed, mark all images as not reviewed.
		processImageDao.updateImageReviewStatusByStoreVisit(projectId, storeId, taskId, reviewStatus);
		
		//Recompute store level metrics
		InputObject obj = new InputObject();
		obj.setProjectId(Integer.parseInt(projectId));
		obj.setStoreId(storeId);
		obj.setTaskId(taskId);
		obj.setGranularity("agg-store-score-stitch");
		
		this.recomputeProjectByStoreVisit(obj);

		//Override system generated store visit status with that of reviewer's. 
		processImageDao.updateStoreReviewStatus(projectId, storeId, taskId, reviewStatus, status);

		LOGGER.info("---------------ProcessImageServiceImpl Ends updateStoreReviewStatus----------------\n");

	}
	
	@Override
	public List<LinkedHashMap<String, String>> getProjectStoreRepResponses(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoreRepResponses----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.getProjectStoreRepResponses(inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoreRepResponses----------------\n");

		return result;
	}
	
	@Override
	public List<LinkedHashMap<String, String>> getProjectStoresForReview(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getProjectStoresForReview----------------\n");

		List<LinkedHashMap<String, String>> result = processImageDao.getProjectStoresForReview(
				inputObject.getProjectId(), inputObject.getWaveId(), inputObject.getFromDate(), inputObject.getToDate(), inputObject.getReviewStatus());

		LOGGER.info("---------------ProcessImageServiceImpl Ends getProjectStoresForReview----------------\n");

		return result;
	}
	
	@Override
	public File getStoreLevelDistributionCSVReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getStoreLevelDistributionCSVReport----------------\n");

		List<Map<String, String>> storeLevelDistribution = 
				processImageDao.getStoreLevelDistribution(inputObject.getProjectId(), inputObject.getFromDate(), inputObject.getToDate(), inputObject.getCustomerCode());
		
		
		String headers = "Customer Project Id,Customer Store Number,Retailer Store Number,Retailer,Street,City,State,ZIP,Rep Id,Task Id,Visit Date,UPC,Brand Name,Product Name,OSA,Raw Photo,Compressed Photo" + "\n";
		
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			
			fileWriter.append(headers);

			for (Map<String, String> row : storeLevelDistribution) {
				StringBuilder oneRowBuilder = new StringBuilder();
				oneRowBuilder.append(row.get("customerProjectId") + ",");
				oneRowBuilder.append(row.get("customerStoreNumber") + ",");
				oneRowBuilder.append(row.get("retailerStoreId") + ",");
				oneRowBuilder.append(row.get("retailer") + ",");
				oneRowBuilder.append("\""+row.get("street") + "\",");
				oneRowBuilder.append(row.get("city") + ",");
				oneRowBuilder.append(row.get("state") + ",");
				oneRowBuilder.append(row.get("zip") + ",");
				oneRowBuilder.append(row.get("agentId") + ",");
				oneRowBuilder.append(row.get("taskId") + ",");
				oneRowBuilder.append(row.get("visitDate") + ",");
				oneRowBuilder.append(row.get("upc") + ",");
				oneRowBuilder.append(row.get("brand_name") + ",");
				oneRowBuilder.append(row.get("product_short_name") + ",");
				oneRowBuilder.append(Integer.parseInt(row.get("facing")) > 0 ? "Y," : "N,");
				String rawImagePath = "";
				String compressedImagePath = "";
				if ( StringUtils.isNotBlank(row.get("imageUUID"))) {
					rawImagePath = cloudStorageService.getBucketPath(true) + inputObject.getProjectId() + "/"  + row.get("imageUUID") + ".jpg";
					compressedImagePath = cloudStorageService.getBucketPath(true) + inputObject.getProjectId() + "/"  + row.get("imageUUID") + "-thm.jpg";
				}
				oneRowBuilder.append(rawImagePath + ",");
				oneRowBuilder.append(compressedImagePath);

				fileWriter.append(oneRowBuilder.toString() + "\n");
			}
			fileWriter.flush();
			fileWriter.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			throw new RuntimeException("Unexpected Exception");
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getStoreLevelDistributionReport----------------\n");
		
		File f = new File(tempFilePath);
		return f;
	}
	
	@Override
	public File getStoreLevelImageCSVReport(InputObject inputObject, String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getStoreLevelImageCSVReport----------------\n");

		List<Map<String, String>> storeLevelDistribution = 
				processImageDao.getStoreImagesWithStoreMetadata(inputObject.getProjectId(), inputObject.getFromDate(), inputObject.getToDate(), inputObject.getCustomerCode());
		
		
		String headers = "Customer Project Id,Customer Store Number,Retailer Store Number,Retailer,Street,City,State,ZIP,Rep Id,Task Id,Visit Date,Raw Photo,Compressed Photo,Photo Quality Comment" + "\n";
		
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(tempFilePath);
			
			fileWriter.append(headers);

			for (Map<String, String> row : storeLevelDistribution) {
				StringBuilder oneRowBuilder = new StringBuilder();
				oneRowBuilder.append(row.get("customerProjectId") + ",");
				oneRowBuilder.append(row.get("customerStoreNumber") + ",");
				oneRowBuilder.append(row.get("retailerStoreId") + ",");
				oneRowBuilder.append(row.get("retailer") + ",");
				oneRowBuilder.append("\""+row.get("street") + "\",");
				oneRowBuilder.append(row.get("city") + ",");
				oneRowBuilder.append(row.get("state") + ",");
				oneRowBuilder.append(row.get("zip") + ",");
				oneRowBuilder.append(row.get("agentId") + ",");
				oneRowBuilder.append(row.get("taskId") + ",");
				oneRowBuilder.append(row.get("visitDate") + ",");
				String rawImagePath = cloudStorageService.getBucketPath(true) + inputObject.getProjectId() + "/"  + row.get("imageUUID") + ".jpg";
				String compressedImagePath = cloudStorageService.getBucketPath(true) + inputObject.getProjectId() + "/"  + row.get("imageUUID") + "-thm.jpg";
				oneRowBuilder.append(rawImagePath + ",");
				oneRowBuilder.append(compressedImagePath + ",");
				oneRowBuilder.append(ConverterUtil.ifNullToEmpty(row.get("imageNotUsableComment")));
				
				fileWriter.append(oneRowBuilder.toString() + "\n");
			}
			fileWriter.flush();
			fileWriter.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			throw new RuntimeException("Unexpected Exception");
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getStoreLevelImageCSVReport----------------\n");
		
		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void pollAndSubmitImagesForAnalysis(String batchSize) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts pollAndSubmitImagesForAnalysis:: batchSize={}----------------\n",batchSize);
		boolean shouldWait = false;
		if ( new File(PAUSE_IMAGE_ANALYSIS_FILE_LOCK).exists() ) {
			LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Processing is paused----------------" );
			shouldWait = true;
		} else {
			if ( new File(IMAGE_ANALYSIS_RUNTIME_CONFIGURATION_FILE).exists() ) {
				Properties prop = new Properties();
				try {
					prop.load(new FileInputStream(IMAGE_ANALYSIS_RUNTIME_CONFIGURATION_FILE));
				} catch (Exception e) {
					LOGGER.error("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Unable to load imageAnalysis configuration properties file. Using default value.");
				}
				batchSize = prop.getProperty("batchSize");
				LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Batch size from configuration file = {}",batchSize);
			}
			
			List<String> images = processImageDao.getImagesForProcessing(batchSize);
			LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Number of images to process = {}", images.size());
			if (!images.isEmpty()) {
				List<Future<?>> futures = new ArrayList<Future<?>>();
				for(String imageUUID : images ) {
					LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Submitting imageUUID = {} for processing", imageUUID);
					Future<?> future = batchImageAnalysisWorkerPool.submit(new Runnable() {
						@Override
						public void run() {
							runImageAnalysis(imageUUID);
						}
					});
					futures.add(future);
				}
				LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Submitted all images in this batch..Waiting for completion ----------------");
				boolean errorOccured = false;
				for( Future<?> future : futures ) {
					try {
						future.get();
					} catch (Exception e) {
						LOGGER.error("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Errror while awaiting task completion : ",e);
						errorOccured = true;
					}
				}
				LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Completed all tasks :: status = {} ----------------", errorOccured);
			} else {
				LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::No images to process----------------" );
				shouldWait = true;
			}
		}
		
		if ( shouldWait ) {
			try {
				LOGGER.info("---------------ProcessImageServiceImpl::pollAndSubmitImagesForAnalysis::Force sleep for 30 seconds----------------" );
				Thread.sleep(30000);
			} catch (Exception e ) {
				//grounding, no value in doing anything here.
			}
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends pollAndSubmitImagesForAnalysis----------------\n");

	}
	
	@Override
	public File getHomePanelProjectStatusReport(String tempFilePath) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getHomePanelProjectStatusReport----------------\n");

		List<Map<String, String>> homePanelStatusData = processImageDao.getHomePanelProjectStatusData();
		
		//Generate XLSX report
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Home Panel Data Collection Status");
		
		XSSFCellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		List<String> headerColumns = new ArrayList<String>();
		headerColumns.add("Retailer");
		headerColumns.add("Retailer Store Number");
		headerColumns.add("Rep Id");
		headerColumns.add("Internal Store Id");
		headerColumns.add("Task Id");
		headerColumns.add("Categories Not Completed");
		for(int i=1384; i<1421; i++) {
			if( i==1389 ) { continue; };
			//headerColumns.add(i+"-Reported");
			//headerColumns.add(i+"-Actual");
			headerColumns.add(i+"");
		}
		Row headerRow = sheet.createRow(0);
		int colNum=0;
		for(String columnHeader : headerColumns ) {
			Cell cell = headerRow.createCell(colNum++);
			cell.setCellValue(columnHeader);
		}
		
		try {
			int rowNum = 1;
			for (Map<String, String> oneStore : homePanelStatusData) {
				colNum=0;
				Row row = sheet.createRow(rowNum++);
				
				row.createCell(colNum++).setCellValue(oneStore.get("retailer"));
				row.createCell(colNum++).setCellValue(oneStore.get("retailerStoreId"));
				row.createCell(colNum++).setCellValue(oneStore.get("agentId"));
				row.createCell(colNum++).setCellValue(oneStore.get("storeId"));
				row.createCell(colNum++).setCellValue(oneStore.get("taskId"));
				row.createCell(colNum++).setCellValue(oneStore.get("notCompletedProjectCount"));
				for(int i=1384; i<1421; i++) {
					if( i==1389 ) { continue; };
					//row.createCell(colNum++).setCellValue(oneStore.get(i+"-Reported"));
					//row.createCell(colNum++).setCellValue(oneStore.get(i+"-Actual"));
					row.createCell(colNum++).setCellValue(oneStore.get(i+"-Difference"));
					if ( !oneStore.get(i+"-Difference").equals("NA") && Integer.parseInt(oneStore.get(i+"-Difference")) > 0 ) {
						row.getCell(colNum - 1).setCellStyle(style);
					}
				}
			}
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			throw new RuntimeException("Unexpected Exception");
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends getStoreLevelImageCSVReport----------------\n");
		
		File f = new File(tempFilePath);
		return f;
	}

	@Override
	public void updateDuplicateDetections(List<Long> duplicateDetections) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts updateDuplicateDetections for ids {}----------------\n", duplicateDetections);
		processImageDao.updateDuplicateDetections(duplicateDetections);
		LOGGER.info("---------------ProcessImageServiceImpl Ends updateDuplicateDetections----------------\n");

	}

	@Override
	public String getInternalProjectStatus(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getInternalProjectStatus for project id = {}, waveId = {}----------------\n",
				inputObject.getProjectId(), inputObject.getWaveId());
		
		List<LinkedHashMap<String, String>> projectDetails = metaServiceDao.getProjectDetail(inputObject.getProjectId());
		
		String templateFileName = "project-status-report.html";
        String templateString = ConverterUtil.getResourceFromClasspath(templateFileName, "/templates/");
        
		//HTML snippets for Groups
		String oneGroupBeginning = "<td>{minValue} - {maxValue}</td><td>{storeVisitCount}</td><td><button onclick=\"showHideDiv('{groupType}DivGroup{groupId}')\">Show store visits</button>" + 
						"<div id=\"{groupType}DivGroup{groupId}\" style=\"display:none\">" + 
						"<table id=\"'{groupType}TableGroup{groupId}\">" + 
						"<tr><th>Store ID</th><th>Task ID</th><th>Count</th></tr>";
		String oneGroupRow = "<tr><td>{storeId}</td><td>{taskId}</td><td>{count}</td></tr>";
		String oneGroupEnd = "</table></div></td></tr>";
		
		StringBuilder photoCountTableBuilder = new StringBuilder();
		StringBuilder upcCountTableBuilder = new StringBuilder();
		StringBuilder distPercentageTableBuilder = new StringBuilder();
		
		//1. Get Photo Count Report - grouped
		List<Map<String,String>> photoCountGroups = processImageDao.getProjectPhotoCountGroups(inputObject.getProjectId(), inputObject.getLimit(), inputObject.getWaveId());
		//2. Get Photo Count report - per store visit
		List<Map<String,String>> photoCountStoreVisits =  processImageDao.getProjectAllStorePhotoCount(inputObject.getProjectId(), inputObject.getWaveId());;
		for( Map<String,String> onePhotoCountGroup : photoCountGroups) {
			String groupId = onePhotoCountGroup.get("groupId");
			int minPhotoCount = Integer.parseInt(onePhotoCountGroup.get("minPhotoCount"));
			int maxPhotoCount = Integer.parseInt(onePhotoCountGroup.get("maxPhotoCount"));
			String totalStoreVisits = onePhotoCountGroup.get("totalStoreVisits");
			StringBuilder oneGroupHtmlBuilder = new StringBuilder(oneGroupBeginning.replace("{minValue}", ""+minPhotoCount)
					.replace("{maxValue}", ""+maxPhotoCount)
					.replace("{storeVisitCount}",totalStoreVisits)
					.replace("{groupType}", "photo")
					.replace("{groupId}", groupId));
			for ( Map<String,String> oneStoreVisit : photoCountStoreVisits ) {
				int photoCount = Integer.parseInt(oneStoreVisit.get("photoCount"));
				String storeId = oneStoreVisit.get("storeId");
				String taskId = oneStoreVisit.get("taskId");
				if ( photoCount >= minPhotoCount && photoCount <= maxPhotoCount ) {
					String storeSummaryPageLink = "https://snap2insight.com/dashboard/project-store-result/summary/DEM/"+inputObject.getProjectId()+"/"+storeId+"/"+taskId;
					String hyperlink = "<a href=\""+storeSummaryPageLink+"\" target=\"_blank\">"+photoCount+"</a>";
					oneGroupHtmlBuilder.append(oneGroupRow.replace("{storeId}", storeId).replace("{taskId}", taskId).replace("{count}",hyperlink));
				}
			}
			oneGroupHtmlBuilder.append(oneGroupEnd);
			photoCountTableBuilder.append(oneGroupHtmlBuilder.toString());
		}
		
		//3. Get UPC Count Report - grouped
		List<Map<String,String>> upcCountGroups = processImageDao.getProjectDetectedUPCCountGroups(inputObject.getProjectId(), inputObject.getLimit(), inputObject.getWaveId());
		//4. Get UPC Count Report - per store visit
		List<Map<String,String>> upcCountStoreVisits = processImageDao.getProjectAllStoreDetectedUPCCount(inputObject.getProjectId(), inputObject.getWaveId());
		for( Map<String,String> oneUpcCountGroup : upcCountGroups) {
			String groupId = oneUpcCountGroup.get("groupId");
			int minUpcCount = Integer.parseInt(oneUpcCountGroup.get("minUpcCount"));
			int maxUpcCount = Integer.parseInt(oneUpcCountGroup.get("maxUpcCount"));
			String totalStoreVisits = oneUpcCountGroup.get("totalStoreVisits");
			StringBuilder oneGroupHtmlBuilder = new StringBuilder(oneGroupBeginning.replace("{minValue}", ""+minUpcCount)
					.replace("{maxValue}", ""+maxUpcCount)
					.replace("{storeVisitCount}",totalStoreVisits)
					.replace("{groupType}", "upc")
					.replace("{groupId}", groupId));
			for ( Map<String,String> oneStoreVisit : upcCountStoreVisits ) {
				int upcCount = Integer.parseInt(oneStoreVisit.get("upcCount"));
				String storeId = oneStoreVisit.get("storeId");
				String taskId = oneStoreVisit.get("taskId");
				if ( upcCount >= minUpcCount && upcCount <= maxUpcCount ) {
					String storeSummaryPageLink = "https://snap2insight.com/dashboard/project-store-result/summary/DEM/"+inputObject.getProjectId()+"/"+storeId+"/"+taskId;
					String hyperlink = "<a href=\""+storeSummaryPageLink+"\" target=\"_blank\">"+upcCount+"</a>";
					oneGroupHtmlBuilder.append(oneGroupRow.replace("{storeId}", storeId).replace("{taskId}", taskId).replace("{count}",hyperlink));
				}
			}
			oneGroupHtmlBuilder.append(oneGroupEnd);
			upcCountTableBuilder.append(oneGroupHtmlBuilder.toString());
		}
		
		//5. Get Distribution Percentage Report - grouped
		List<Map<String,String>> distPercentageGroups = processImageDao.getProjectDistributionPercentageGroups(inputObject.getProjectId(), inputObject.getLimit(), inputObject.getWaveId());
		//6. Get Distribution Percentage Report - per store visit
		List<Map<String,String>> distPercentageStoreVisits = processImageDao.getProjectAllStoreDistributionPercentage(inputObject.getProjectId(), inputObject.getWaveId());
		for( Map<String,String> oneDistPercentageGroup : distPercentageGroups) {
			String groupId = oneDistPercentageGroup.get("groupId");
			int minDistPercentage = new Double(oneDistPercentageGroup.get("minDistributionPercentage")).intValue();
			int maxDistPercentage = new Double(oneDistPercentageGroup.get("maxDistributionPercentage")).intValue();
			String totalStoreVisits = oneDistPercentageGroup.get("totalStoreVisits");
			StringBuilder oneGroupHtmlBuilder = new StringBuilder(oneGroupBeginning.replace("{minValue}", ""+minDistPercentage)
					.replace("{maxValue}", ""+maxDistPercentage)
					.replace("{storeVisitCount}",totalStoreVisits)
					.replace("{groupType}", "dist")
					.replace("{groupId}", groupId));
			for ( Map<String,String> oneStoreVisit : distPercentageStoreVisits ) {
				int distPercentage = new Double(oneStoreVisit.get("distributionPercentage")).intValue();
				String storeId = oneStoreVisit.get("storeId");
				String taskId = oneStoreVisit.get("taskId");
				if ( distPercentage >= minDistPercentage && distPercentage <= maxDistPercentage ) {
					String storeSummaryPageLink = "https://snap2insight.com/dashboard/project-store-result/summary/DEM/"+inputObject.getProjectId()+"/"+storeId+"/"+taskId;
					String hyperlink = "<a href=\""+storeSummaryPageLink+"\" target=\"_blank\">"+distPercentage+"</a>";
					oneGroupHtmlBuilder.append(oneGroupRow.replace("{storeId}", storeId).replace("{taskId}", taskId).replace("{count}",hyperlink));
				}
			}
			oneGroupHtmlBuilder.append(oneGroupEnd);
			distPercentageTableBuilder.append(oneGroupHtmlBuilder.toString());
		}
		
		String reportHtmlString = templateString.replace("{PHOTO-GROUP-ROW}", photoCountTableBuilder.toString());
		reportHtmlString = reportHtmlString.replace("{UPC-GROUP-ROW}", upcCountTableBuilder.toString());
		reportHtmlString = reportHtmlString.replace("{DIST-GROUP-ROW}", distPercentageTableBuilder.toString());
		reportHtmlString = reportHtmlString.replace("{projectId}",""+inputObject.getProjectId());
		
		String projectName = projectDetails.get(0).get("projectName");
		if (StringUtils.isNotBlank(inputObject.getWaveId()) && !inputObject.getWaveId().equals("-9") ) {
			projectName = projectName + " - Wave " + inputObject.getWaveId();
		}
		reportHtmlString = reportHtmlString.replace("{projectName}", projectName);
		
		LOGGER.info("---------------ProcessImageServiceImpl Starts getInternalProjectStatus for project id = {}----------------\n", inputObject.getProjectId());
		return reportHtmlString;
	}

	@Override
	public List<LinkedHashMap<String, String>> resolveGroupUpcs(String projectId, String storeId, String taskId, Map<String, List<String>> orderedUPCGroupMap) {
		
		LOGGER.info("---------------ProcessImageServiceImpl Starts resolveGroupUpcs for projectId={}, storeId={}, taskId={}, groups={}----------------\n",
				projectId, storeId, taskId, orderedUPCGroupMap);
		List<LinkedHashMap<String, String>> returnList = new ArrayList<LinkedHashMap<String,String>>();
		
		// Get image list for given store visit OR all store visits
		Map<String,List<String>> storeImages = processImageDao.getImages(projectId, storeId, taskId);
		// For each store
		for(String internalStoreId : storeImages.keySet()) {
			// Get storeId & Get store distribution list
			List<String> distributionUPCs = metaServiceDao.getStoreDistributionUPCs(projectId, internalStoreId);
			// For each image
			for( String imageUUID : storeImages.get(internalStoreId) ) {
				// Get Row Id,Detected UPC list from ImageAnalysis
				List<ImageAnalysis> imageAnalysisList = processImageDao.getImageAnalysis(imageUUID);
				
				LinkedHashMap<String,String> modifiedDetections = new LinkedHashMap<String,String>();
				
				// For each group
				for(Entry<String, List<String>> oneGroup : orderedUPCGroupMap.entrySet()) {
					// Get group UPCs
					List<String> oneGroupUpcList = oneGroup.getValue();
					String primaryUPC = "";
					int numberOfGroupUPCsInDistribution = 0;
					for(String groupUPC : oneGroupUpcList) {
						if(distributionUPCs.contains(groupUPC)) {
							numberOfGroupUPCsInDistribution = numberOfGroupUPCsInDistribution + 1;
							primaryUPC = groupUPC;
						}
					}
					// If distribution list has only 1 UPC among the group UPCs
					if ( numberOfGroupUPCsInDistribution == 1 ) {
						// Replace all detections for all group UPCs to the single UPC in distribution list in detected UPCs list.
						for(ImageAnalysis oneDetection : imageAnalysisList ) {
							if ( oneGroupUpcList.contains(oneDetection.getUpc()) && !primaryUPC.equals(oneDetection.getUpc()) ) {
								LOGGER.info("---------------ProcessImageServiceImpl::resolveGroupUpcs::Updating primaryUPC={} for oldUpc={} & rowId={} ----------------\n",
										primaryUPC,oneDetection.getUpc(),oneDetection.getId() );
								oneDetection.setUpc(primaryUPC);
								modifiedDetections.put(oneDetection.getId(),primaryUPC);
							}
						}
					}
				}
				
				// Set updated rowId,UPC back to ImageAnalysis
				for(String rowId : modifiedDetections.keySet()) {
					processImageDao.updateUPCForImageAnalysis(imageUUID, rowId, modifiedDetections.get(rowId));
				}
				
				if (! modifiedDetections.isEmpty() ) {
					returnList.add(modifiedDetections);
				}
			}
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl Ends resolveGroupUpcs----------------\n");
		return returnList;
	}
	
	@Override
	public void saveImageAnalysisData(String imageUUID, String imageAnalysisData) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts saveImageAnalysisData----------------\n");
		
		ImageStore imageStore = processImageDao.findByImageUUId(imageUUID);
		
		if ( imageStore != null ) {
			
			LOGGER.info("ProcessImageServiceImpl::saveImageAnalysisData::Deleting all existing analysis data for imageUUID {}", imageUUID);
			processImageDao.deleteAllDetectionsByImageUUID(imageUUID);
			
			List<ImageAnalysis> imageAnalysisList = this.parseImageAnalysisOutput(imageStore, imageAnalysisData);

			// Store imageHashScore, imageResultCode and imageResultComments
			processImageDao.updateImageHashScoreResults(imageStore);
			boolean updateImageStatus = true;
			if (null != imageAnalysisList) {
				this.saveAndPostProcessImageAnalysisData(imageUUID, imageStore, imageAnalysisList, updateImageStatus);
			}
			
		} else {
			LOGGER.error("ProcessImageServiceImpl::saveImageAnalysisData::Unable to find image with id {} OR status is incorrect", imageUUID);
		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends saveImageAnalysisData----------------\n");
	}
	
	@Override
	public List<LinkedHashMap<String, String>> getNextImagesToProcessExternally(InputObject inputObject) {
		LOGGER.info("---------------ProcessImageServiceImpl Starts getNextImagesToProcessExternally for project id = {}, limit = {}----------------\n",
				inputObject.getProjectId(), inputObject.getLimit());

		List<LinkedHashMap<String,String>> returnList = new ArrayList<LinkedHashMap<String,String>>();

		List<ImageStore> images = processImageDao.getImagesForExternalProcessing(inputObject.getProjectId(), inputObject.getLimit());

		for(ImageStore image : images ) {
			LinkedHashMap<String, String> oneImageMap = new LinkedHashMap<String,String>();
			String imageURL = cloudStorageService.getBucketPath(true) + inputObject.getProjectId() + "/"  + image.getImageUUID() + ".jpg";
			oneImageMap.put("projectId", ""+inputObject.getProjectId());
			oneImageMap.put("storeId", image.getStoreId());
			oneImageMap.put("taskId", image.getTaskId());
			oneImageMap.put("categoryId", image.getCategoryId());
			oneImageMap.put("imageUUID", image.getImageUUID());
			oneImageMap.put("imageURL", imageURL);
			returnList.add(oneImageMap);

			processImageDao.updateImageAnalysisStatus(Constants.EXTERNAL_PROCESSING_IN_PROGRESS_IMAGE_STATUS, image.getImageUUID());

		}

		LOGGER.info("---------------ProcessImageServiceImpl Ends getNextImagesToProcessExternally with list of images = {}----------------\n", returnList);
		return returnList;
	}
	
	
	@Override
	public void pollAndProcessStoreAnalysis() {
		LOGGER.info("---------------ProcessImageServiceImpl:: Starts pollAndProcessStoreAnalysis---------------\n");
		
		List<LinkedHashMap<String, String>> resultList = processImageDao.getStoreVisitsForStoreAnalysisProcessing();
		
        for(LinkedHashMap<String, String> oneStoreVisit : resultList) {
        	int projectId = Integer.parseInt(oneStoreVisit.get("projectId"));
            String storeId = oneStoreVisit.get("storeId");
            String taskId = oneStoreVisit.get("taskId");
        	try {
                
                LOGGER.info("---------------ProcessImageServiceImpl::pollAndProcessStoreAnalysis::Starting store analysis for projectId::{}::storeId::{}::taskId::{}",projectId,storeId,taskId);
                
                String storeAnalysisJSON = cloudStorageService.getImageAnalysisMetadata(""+projectId, storeId+"_"+taskId+"_analysis");
                
                if (StringUtils.isBlank(storeAnalysisJSON)) {
                	LOGGER.error("---------------ProcessImageServiceImpl::pollAndProcessStoreAnalysis::Empty store analysis response for projectId::{}::storeId::{}::taskId::{}",projectId,storeId,taskId);
                	processImageDao.setStoreVisitToStoreAnalysisFailed(projectId, storeId, taskId);
                	continue;
                }

        		JsonObject resultObject =null;
        		try {
        			resultObject = new JsonParser().parse(storeAnalysisJSON).getAsJsonObject();
        		} catch(Exception e) {
        			LOGGER.error("---------------ProcessImageServiceImpl::pollAndProcessStoreAnalysis::Malformed response from store analysis service : response={}, error={}",storeAnalysisJSON,e);
        			processImageDao.setStoreVisitToStoreAnalysisFailed(projectId, storeId, taskId);
        			continue;
        		}

        		String status = resultObject.get("status").getAsString();
        		String message = resultObject.get("message").isJsonNull() ? "null" : resultObject.get("message").getAsString();
        		
        		if ( status.equals("error") ) {
        			LOGGER.error("---------------ProcessImageServiceImpl::pollAndProcessStoreAnalysis::Store analysis failed : response={}, message={}",storeAnalysisJSON,message);
        			processImageDao.setStoreVisitToStoreAnalysisFailed(projectId, storeId, taskId);
        			continue;
        		}
        		
        		JsonObject responseObj = resultObject.getAsJsonObject("data");
        		
        		JsonArray imageList = responseObj.getAsJsonArray("imageList");
        		
        		for(int i=0; i<imageList.size(); i++) {
        			if ( ! imageList.get(i).isJsonNull() ) {
        				JsonObject obj = imageList.get(i).getAsJsonObject();
        				String imageUUID = obj.get("uuid").getAsString();
        				ImageStore imageStore = processImageDao.findByImageUUId(imageUUID);
        				LOGGER.info("ProcessImageServiceImpl::pollAndProcessStoreAnalysis::Deleting all existing analysis data for imageUUID {}", imageUUID);
        				processImageDao.deleteAllDetectionsByImageUUID(imageUUID);
        				List<ImageAnalysis> detections = unmarshalImageAnalysisData(imageStore, obj);
        				// Store image level properties
        				processImageDao.updateImageHashScoreResults(imageStore);
        				if (null != detections) {
            				boolean updateImageStatus = false;
            				this.saveAndPostProcessImageAnalysisData(imageUUID, imageStore, detections, updateImageStatus);
            			}
        			}
        		}
        		
        		InputObject obj = new InputObject();
                obj.setProjectId(projectId);
                obj.setStoreId(storeId);
                obj.setTaskId(taskId);
                obj.setGranularity("agg-store-score");
                
                recomputeProjectByStoreVisit(obj);
        		
        	} catch (Exception e) {
        		LOGGER.error("EXCEPTION WHILE STORE ANALYSIS PROCESSING {} , {}", e.getMessage(), e);
        		processImageDao.setStoreVisitToStoreAnalysisFailed(projectId, storeId, taskId);
        	}
        }
		
		LOGGER.info("---------------ProcessImageServiceImpl:: Ends pollAndProcessStoreAnalysis---------------\n");
	}
	
	@Override
	public void saveStoreLevelData(String inputPayload) {
		LOGGER.info("---------------ProcessImageServiceImpl:: Starts saveStoreLevelData---------------\n");

		JsonObject resultObject =null;
		try {
			resultObject = new JsonParser().parse(inputPayload).getAsJsonObject();
		} catch(Exception e) {
			LOGGER.error("---------------ProcessImageServiceImpl :: Malformed response from store analysis service : response={}, error={}",inputPayload,e);
			e.printStackTrace();
		}

		String status = resultObject.get("status").getAsString();
		String message = resultObject.get("message").isJsonNull() ? "null" : resultObject.get("message").getAsString();
		
		if ( status.equals("error") ) {
			LOGGER.error("---------------ProcessImageServiceImpl :: Store analysis failed : response={}, message={}",inputPayload,message);
			return;
		}
		
		JsonObject responseObj = resultObject.getAsJsonObject("data");
		
		int projectId = responseObj.get("projectId").getAsInt();
		String storeId = responseObj.get("storeId").getAsString();
		String taskId = responseObj.get("taskId").getAsString();

		LOGGER.info("---------------ProcessImageServiceImpl :: Uploading store analysis metadata to cloud storage----------------\n");
		cloudStorageService.storeImageAnalysisMetadata(""+projectId, storeId+"_"+taskId+"_analysis", inputPayload);
		LOGGER.info("---------------ProcessImageServiceImpl :: Uploaded store analysis metadata to cloud storage----------------\n");
		
		LOGGER.info("---------------ProcessImageServiceImpl :: Marking store visit as \"store analysis received\" for ProjectId={}, StoreId={}, TaskId={}",projectId,storeId,taskId);
		processImageDao.setStoreVisitToStoreAnalysisReceived(projectId,storeId,taskId);
		
		LOGGER.info("---------------ProcessImageServiceImpl:: Ends saveStoreLevelData---------------\n");
	}
	
	@Override
	public void updateImageAnalysisStatus(String imageUUID, String inputPayload) {
		LOGGER.info("---------------ProcessImageServiceImpl:: Starts updateImageAnalysisStatus---------------\n");
		ImageStore imageStore = processImageDao.findByImageUUId(imageUUID);
		
		if ( imageStore != null && imageStore.getImageStatus().startsWith("processing")) {
			JsonObject responseObj = new JsonParser().parse(inputPayload).getAsJsonObject();
			String status = responseObj.get("status").getAsString();
			String imageStatus = "error";
			if ( status.trim().equals("success") ) {
				imageStatus = "done";
			}
			LOGGER.info("ProcessImageServiceImpl::updateImageAnalysisStatus::Updating imageanalysis status to {} for imageUUID {}", imageStatus, imageUUID);
			processImageDao.updateImageAnalysisStatus(imageStatus, imageUUID);
			LOGGER.info("ProcessImageServiceImpl::updateImageAnalysisStatus::Setting store visit for (re)aggregation", imageStatus, imageUUID);
			processImageDao.setStoreVisitForAggregation(imageStore.getProjectId(), imageStore.getStoreId(), imageStore.getTaskId());
		} else {
			LOGGER.error("ProcessImageServiceImpl::updateImageAnalysisStatus::Unable to find image with id {} & status=processing", imageUUID);
		}
		
		LOGGER.info("---------------ProcessImageServiceImpl:: Ends updateImageAnalysisStatus---------------\n");
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getRepPerformanceMetrics(String userId) {
		LOGGER.info("---------------ProcessImageServiceImpl:: Starts getRepPerformanceMetrics---------------\n");
		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		
		LinkedHashMap<String, Object> result = processImageDao.getPhotoQualityMetricsByUserId(userId);
		
		LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String,Object>();
		resultMap.put("type","photo-quality");
		resultMap.put("days","30");
		resultMap.put("stats",result);
		
		resultList.add(resultMap);

		LOGGER.info("---------------ProcessImageServiceImpl:: Ends getRepPerformanceMetrics---------------\n");
		return resultList;
	}

}

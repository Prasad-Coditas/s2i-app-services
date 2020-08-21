package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.dao.ProductMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.ProcessImageService;
import com.snap2buy.themobilebackend.service.SrvBulkUploaderService;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Anoop on 9/12/17.
 */
@Component(value = BeanMapper.BEAN_SRV_BULK_UPLOADER_SERVICE)
@Scope("prototype")
public class SrvBulkUploaderServiceImpl implements SrvBulkUploaderService {

    private static HttpClient client = HttpClientBuilder.create().build();

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
    private ProcessImageService processImageService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_PRODUCT_MASTER_DAO)
    private ProductMasterDao productMasterDao;

	@Autowired
	private Environment env;

    private final String SRV_X_AUTH_TOKEN_HEADER_KEY = "X-AUTH-TOKEN";
    private final String SRV_X_AUTH_TOKEN_HEADER_VALUE = "ujwR@Lw=P8sK+s#pu$H^f6_sh2nUvd5xDSLem%ts?am7$XbFVJ5mJZg%9VbKgDQd";

    DateFormat projectDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    DateFormat imageDateFormat = new SimpleDateFormat("yyyyMMdd");
    
    @Override
	public LinkedHashMap<String, String> loadSurveyData(InputObject inputObject) {
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts loadSurveyData----------------\n");
    	LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();
    	int storeVisitsFound = 0;
    	int storeVisitsLoaded = 0;
    	int imagesFound = 0;
    	int imagesLoaded = 0;
		int projectId = inputObject.getProjectId();
		String externalProjectId = inputObject.getExternalProjectId();
		String batchId = ConverterUtil.getBatchIdForImport(projectId+"");
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl::loadSurveyData::{}",projectId);
		LOGGER.info("=============================batchId={}",batchId);
		//Create the directory for images to download.
        String imageDirectoryForProject = env.getProperty("disk_directory") + projectId + "/";
        File imageDirectory = new File(imageDirectoryForProject);
        imageDirectory.mkdirs();
		
		List<LinkedHashMap<String, String>> projectDetails = metaServiceDao.getProjectDetail(projectId);
		
		if ( projectDetails != null && !projectDetails.isEmpty() ) {
			String categoryId = projectDetails.get(0).get("categoryId");
	        String retailerCode = projectDetails.get(0).get("retailerCode");
			String projectStartDateStr = projectDetails.get(0).get("startDate");
			Date projectStartDate = null;
			try {
				projectStartDate = projectDateFormat.parse(projectStartDateStr);
			} catch (Exception e) {
				LOGGER.error("EXCEPTION {} {}", e.getMessage(), e);
			    return null;
			}
			
			//Get already uploaded stores-task combination with images for this project
			//If at least one image is uploaded for a store, skip the store.
	        List<StoreVisit> storeVisitsWithImages = processImageService.getStoreVisitsWithImages(projectId);
	        
	        //Get already uploaded stores with results
	        List<StoreVisit> storeVisitsAlreadyUploaded = processImageDao.getAlreadyUploadedStoreVisit(projectId);
	        
	        //Store Visits from previous uploads which are processed again in this upload
	        List<StoreVisit> existingStoreVisitsForBatchIdUpdate = new ArrayList<StoreVisit>();
			
			// Getting data non-paged.. 
			String surveyDataURL = "https://reportapi.survey.com/api/analysis/irexport_j?id="+externalProjectId+"&page=-1";
			
			LOGGER.info("---------------SrvBulkUploaderServiceImpl::Downloading Data from::{}", surveyDataURL );
			
			SurveyComDownloadRecords records = getSurveyComData(surveyDataURL);

			LOGGER.info("---------------SrvBulkUploaderServiceImpl::Downloaded Data::{}", records);
			
			List<SurveyComDownloadRecord> storeVisits = records.getRecordList();
			
			storeVisitsFound = storeVisits.size();
			
			for( SurveyComDownloadRecord storeVisit : storeVisits ) {
				String storeId = storeVisit.getRetailerStoreId();
				String storeIdWithRetailCode = retailerCode+"_"+storeId;
				String taskId = storeVisit.getResultId();
				if ( shouldStoreImage(storeVisit, projectStartDate) ) {
					StoreVisit currentStoreVisit = new StoreVisit(storeIdWithRetailCode, taskId);
					//If at least one image is uploaded for a store-task combination, skip the store-task combination.
					if ( storeVisitsWithImages.contains(currentStoreVisit) ) {
						LOGGER.info("---------------SrvBulkUploaderServiceImpl :: storeId-taskId exists in DB ..Skipping this store and task::{} and {} ", storeIdWithRetailCode,taskId);
						continue;
					}
					List<SurveyComImageURL> imageURLs = storeVisit.getImageList();
					
					String dateId = null;
					try {
						dateId = imageDateFormat.format(projectDateFormat.parse(storeVisit.getVisitDate()));
					} catch (ParseException e) {
						LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error parsing dateId :: {}", e.getMessage());
			            continue;
					}
					
					boolean atleastOneImageSaved = false;
					String savedImageUrl = null;
					String previewImageUUID = null;
					for ( SurveyComImageURL imageURL : imageURLs) {
						imagesFound = imagesFound + 1;
						URL imageURLObject = null;
						try {
							imageURLObject = new URL(imageURL.getUrl());
						} catch (MalformedURLException e) {
				            LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error creating URL for download link :: {}", e.getMessage());
				            continue;
						}
						
						String sync = "false";
						String fileId = null, agentId = null, questionId = null, imageStatus = null;
						String imageUUID = saveImage(imageURLObject, imageDirectoryForProject, dateId, storeVisit.getRetailerStoreId(), categoryId, sync, agentId, projectId, taskId, fileId, questionId, imageStatus);
						if ( StringUtils.isNotBlank(imageUUID) ) {
							atleastOneImageSaved = true;
							savedImageUrl = imageURL.getUrl();
							previewImageUUID = imageUUID;
							imagesLoaded = imagesLoaded + 1;
						}
					}
					if ( atleastOneImageSaved ) {
						if ( !storeVisitsAlreadyUploaded.contains(currentStoreVisit) ) {
							//Make an entry in project store results table for this store
							String agentId = null;
							insertOrUpdateStoreVisitResult(projectId, batchId,storeIdWithRetailCode, taskId, agentId, dateId, savedImageUrl, previewImageUUID);
						} else {
							LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Store already exists in result table----------------\n");
							existingStoreVisitsForBatchIdUpdate.add(currentStoreVisit);
						}
						storeVisitsLoaded = storeVisitsLoaded + 1;
					}
				}
			}
			// Set batchId to current batchId for all stores which are getting reprocessed in this upload
			LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Start updating batchId for stores which are getting reprocessed in this upload ----------------\n");
			processImageDao.updateBatchIdForProjectStoreResults(projectId, existingStoreVisitsForBatchIdUpdate, batchId);
			LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Updated batchId for stores which are getting reprocessed in this upload ----------------\n");
			
			// Set store result status to "Generate Aggregations" for all the stores uploaded in this import, using the batchId.
			LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Start moving all stores in this upload to Generate Aggregations status ----------------\n");
			String resultCode = "99"; // Generate Aggregations
			processImageDao.updateProjectStoreResultByBatchId(projectId,batchId,resultCode);
			LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Moved all stores in this upload to Generate Aggregations status ----------------\n");
			
		} else {
			LOGGER.error("---------------SrvBulkUploaderServiceImpl::Unable to fetch project details---------------");
		}
		
		result.put("totalStoreVisitsFound", ""+storeVisitsFound);
		result.put("totalStoreVisitsLoaded", ""+storeVisitsLoaded);
		result.put("totalImagesFound", ""+imagesFound);
		result.put("totalImagesLoaded", ""+imagesLoaded);
		result.put("batchId", batchId);
		
		LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Upload Summary :: {}", result);

		LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends loadSurveyData----------------\n");
		return result;
	}
    

    private boolean shouldStoreImage(SurveyComDownloadRecord storeVisit, Date projectStartDate) {
    	boolean shouldStoreImage = false;
		Boolean imageUploadStatus = storeVisit.getImageUploadStatus();
		String visitDateStr = storeVisit.getVisitDate();
		Date visitDate = null;
		try {
			visitDate = projectDateFormat.parse(visitDateStr);
		} catch (ParseException e) {
			LOGGER.error("EXCEPTION {} {}",e.getMessage(),e);
		    return shouldStoreImage;
		}
		
		if ( visitDate.compareTo(projectStartDate) >= 0 &&  imageUploadStatus ) {
			shouldStoreImage = true;
		}
		return shouldStoreImage;
	}


	private String saveImage(URL imageURL, String imageDirectoryForProject, String dateId, String storeId, String categoryId, String sync, String agentId, int projectId, String taskId, String fileId, String questionId, String imageStatus) {
        LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Downloading from :: {}",imageURL.toString());
        
        int code = 200;
        boolean retryable = false;
        try {
            HttpURLConnection huc = (HttpURLConnection) imageURL.openConnection();
            huc.setInstanceFollowRedirects(true);
            huc.setRequestMethod("GET");
            huc.connect();
            code = huc.getResponseCode();
            if (code == 302) {
                LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Redirect URL : {}", huc.getHeaderField("Location"));
                imageURL = new URL(huc.getHeaderField("Location"));
            }
        } catch (Exception e) {
            LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error checking URL for Image Download Link :: {}", e.getMessage());
            retryable = true;
        }

        if (retryable) {
            try {
                HttpURLConnection huc = (HttpURLConnection) imageURL.openConnection();
                huc.setInstanceFollowRedirects(true);
                huc.setRequestMethod("GET");
                huc.connect();
                code = huc.getResponseCode();
                if (code == 302) {
                    LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Redirect URL : {}", huc.getHeaderField("Location"));
                    imageURL = new URL(huc.getHeaderField("Location"));
                }
            } catch (Exception e) {
                LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error checking URL for Image Download Link in retry :: {}", e.getMessage());
                return null;
            }
        }

        if (code == 200 || code == 303 || code == 302) {
            URL downloadURL = imageURL;
            if (code == 303) {
                String downloadLink = imageURL.toExternalForm();
                downloadLink = downloadLink.replace("http:", "https:");
                try {
                    downloadURL = new URL(downloadLink);
                } catch (Exception e) {
                    LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error creating URL for Image Download Link :: {}", e.getMessage());
                    return null;
                }
            }
            UUID uniqueKey = UUID.randomUUID();

            String imageFilePath = imageDirectoryForProject + uniqueKey.toString().trim() + ".jpg";
			String imageThumbnailPath = imageDirectoryForProject + uniqueKey.toString().trim() + "-thm.jpg";
			String previewPath = imageDirectoryForProject + uniqueKey.toString().trim() + "-prv.jpg";
            try {
                InputStream is = downloadURL.openStream();
                OutputStream os = new FileOutputStream(imageFilePath);
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
            } catch (Exception e) {
                LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Error downoading from Image Download Link :: {}", e.getMessage());
                return null;
            }
            File file = new File(imageFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.getParentFile().setReadable(true);
                file.getParentFile().setWritable(true);
                file.getParentFile().setExecutable(true);
            }

            if (file.length() > 10000) {
                LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Download Successful :: Storing to DB----------------\n");
                InputObject inputObject = new InputObject();
                
                inputObject.setVisitDate(dateId);
                
                inputObject.setStoreId(storeId);
                inputObject.setHostId("1");
                inputObject.setImageUUID(uniqueKey.toString().trim());
                inputObject.setCategoryId(categoryId.trim());
                inputObject.setLatitude("");
                inputObject.setLongitude("");
                inputObject.setTimeStamp("" + System.currentTimeMillis() / 1000L);
                inputObject.setUserId("web");
                inputObject.setSync(sync);
                inputObject.setAgentId(agentId);
                inputObject.setProjectId(projectId);
                inputObject.setTaskId(taskId);
                inputObject.setImageHashScore("0");
                inputObject.setImageRotation("0");
                inputObject.setOrigWidth("0");
                inputObject.setOrigHeight("0");
                inputObject.setNewWidth("0");
                inputObject.setNewHeight("0");
                inputObject.setImageFilePath(imageFilePath);
                inputObject.setThumbnailPath(imageThumbnailPath);
				inputObject.setPreviewPath(previewPath);
                inputObject.setFileId(fileId);
                inputObject.setQuestionId(questionId);
                inputObject.setImageUrl(imageURL.toString());
                inputObject.setImageStatus(imageStatus);
                LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Storing Image details to DB Start----------------\n");
                processImageService.storeImageDetails(inputObject, true); //true to indicate a bulk upload
                LOGGER.info("---------------SrvBulkUploaderServiceImpl :: Storing Image details to DB End----------------\n");
                return inputObject.getImageUUID(); //return imageUUID
            } else {
                LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Download Failed :: 0 byte file or thumbnail :: {}", imageFilePath );
                return null;
            }
        } else {
            LOGGER.error("---------------SrvBulkUploaderServiceImpl :: Download Failed :: Unexpected HTTP response code {}", code );
            return null;
        }
    }
	
	private void insertOrUpdateStoreVisitResult(int projectId, String batchId,
			String storeIdWithRetailCode, String ticketId, String agentId,
			String dateId, String imageLink, String previewImageUUID) {
		try {
			processImageDao.insertOrUpdateStoreResult(projectId, storeIdWithRetailCode, "0", "0", "0", "0", "1", agentId, ticketId, dateId, imageLink,batchId,"" );
			LOGGER.info("---------------BulkUploadEngine :: Inserted one record in store results table for this store...\n");
			processImageDao.updatePreviewImageUUIDForStoreVisit(projectId, storeIdWithRetailCode, ticketId, previewImageUUID);
			LOGGER.info("---------------BulkUploadEngine :: Updated store results table for this store with preview image UUID...\n");

		} catch (Throwable t) {
			LOGGER.info("---------------BulkUploadEngine :: Error inserting one record in store results table for this store...{}", t);
		}
	}

    @Override
	public String uploadSurveyImageResults(InputObject inputObject) {
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts uploadSurveyImageResults----------------\n");
    	String responseToRespondWith = "";
    	
    	SurveyComImageResult result = fetchSurveyImageResults(inputObject);
		
    	String surveyComImageResultJsonPayload = new Gson().toJson(result);
		
        LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadSurveyImageResults::Payload To Submit::{}",surveyComImageResultJsonPayload);

        if ( Boolean.parseBoolean(inputObject.getSendToDestination()) ) {
        	responseToRespondWith = sendToSurvey(inputObject, "https://etrigger.survey.com/api/job/imageb", surveyComImageResultJsonPayload );
        } else {
        	LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadSurveyImageResults::Not uploading to Survey.com----------------\n");
        	responseToRespondWith = surveyComImageResultJsonPayload;
        }
        
		LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends uploadSurveyImageResults----------------\n");
        
        return responseToRespondWith;
	}

	private SurveyComImageResult fetchSurveyImageResults(InputObject inputObject) {
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts fetchSurveyImageResults----------------\n");
    	
    	Map<String,Map<String,String>> images = processImageDao.getImagesForSurveyUpload(inputObject.getProjectId());
    	
    	Map<String,List<Map<String,String>>> imageAnalysis = processImageDao.getImageAnalysisForSurveyUpload(inputObject.getProjectId());
    	
    	List<ProjectUpc> projectUpcList = metaServiceDao.getProjectUpcDetail(inputObject.getProjectId());
    	Map<String,String> upcToShortNameMap = new LinkedHashMap<String,String>();
    	for ( ProjectUpc projectUpc : projectUpcList ) {
    		if ( projectUpc.getSkuTypeId().equals("1") || projectUpc.getSkuTypeId().equals("2") ) {
        		ProductMaster upcMaster = productMasterDao.getUpcDetails(projectUpc.getUpc());
        		upcToShortNameMap.put(projectUpc.getUpc(), upcMaster.getProduct_short_name());
    		}
    	}
    	
    	SurveyComImageResult result = new SurveyComImageResult();
    	result.setProjectId(inputObject.getExternalProjectId());
    	result.setImageCount(""+images.size());
    	
    	List<SurveyComImage> imageList = new ArrayList<SurveyComImage>();
    	
    	for(Map.Entry<String, Map<String,String>> oneImage : images.entrySet() ) {
    		SurveyComImage surveyImage = new SurveyComImage();
    		String imageUUID = oneImage.getKey();
    		Map<String,String> imageInfo = oneImage.getValue();
    		
    		surveyImage.setImageUUID(imageUUID);
    		surveyImage.setImageURL(imageInfo.get("imageURL"));
    		surveyImage.setImageRotation(imageInfo.get("imageRotation"));
    		surveyImage.setRetailerStoreId(imageInfo.get("retailerStoreId"));
    		surveyImage.setResultId(imageInfo.get("taskId"));
    		surveyImage.setProcessedDate(imageInfo.get("processedDate"));
    		
    		List<SurveyComProductDetectionDetail> details = new ArrayList<SurveyComProductDetectionDetail>();
    		
    		List<Map<String,String>> detections = imageAnalysis.get(imageUUID);
    		
    		if ( detections != null ) {
        		for ( Map<String,String> detection : detections ) {
        			SurveyComProductDetectionDetail detail = new SurveyComProductDetectionDetail();
        			detail.setUpc(detection.get("upc"));
        			detail.setProductShortName(upcToShortNameMap.get(detail.getUpc()));
        			detail.setLeftTopX(detection.get("leftTopX"));
        			detail.setLeftTopY(detection.get("leftTopY"));
        			detail.setWidth(detection.get("width"));
        			detail.setHeight(detection.get("height"));
        			details.add(detail);
        		}
    		}
    		
    		surveyImage.setImageUpcList(details);
    		imageList.add(surveyImage);
    	}
    	
    	result.setImageList(imageList);
    	
        LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends fetchSurveyImageResults----------------\n");

		return result;
	}

	@Override
	public String uploadSurveyStoreVisitResults(InputObject inputObject) {
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts uploadSurveyStoreVisitResults----------------\n");

    	String responseToRespondWith = "";
    	
		SurveyComStoreResult result = fetchSurveyStoreResults(inputObject);
		
		String surveyComStoreResultJsonPayload = new Gson().toJson(result);
		
        LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadSurveyStoreVisitResults::Payload To Submit::{}",surveyComStoreResultJsonPayload);
        
        if ( Boolean.parseBoolean(inputObject.getSendToDestination()) ) {
        	responseToRespondWith = sendToSurvey(inputObject, "https://etrigger.survey.com/api/job/imagej", surveyComStoreResultJsonPayload);
        } else {
        	LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadSurveyStoreVisitResults::Not uploading to Survey.com----------------\n");
        	responseToRespondWith = surveyComStoreResultJsonPayload;
        }
        
		LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends uploadSurveyStoreVisitResults----------------\n");
        
        return responseToRespondWith;
	}

	private SurveyComStoreResult fetchSurveyStoreResults(InputObject inputObject) {
    	LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts fetchSurveyStoreResults----------------\n");

		List<LinkedHashMap<String, String>> resultList = processImageDao.getProjectAllStoreResultsDetail(inputObject.getProjectId());
        
        List<ProjectUpc> projectUpcList = metaServiceDao.getProjectUpcDetail(inputObject.getProjectId());
        
        List<ProductMaster> projectUpcDetailList = new ArrayList<ProductMaster>();
        
        for ( ProjectUpc upc : projectUpcList ) {
        	if ( upc.getSkuTypeId().equals("1") || upc.getSkuTypeId().equals("2") ) {
        		ProductMaster upcMaster = productMasterDao.getUpcDetails(upc.getUpc());
                projectUpcDetailList.add(upcMaster);	
        	}
        }
        
        SurveyComStoreResult surveyResult = new SurveyComStoreResult();
        surveyResult.setProjectId(inputObject.getExternalProjectId());
        List<SurveyComStore> recordList = new ArrayList<SurveyComStore>();
        
        for ( LinkedHashMap<String,String> storeResult : resultList ) {
        	if ( !storeResult.get("status").equals("4") ) { //skip in progress stores
	        	SurveyComStore store = new SurveyComStore();
	        	store.setRetailerStoreId(storeResult.get("retailerStoreId"));
	        	store.setResultId(storeResult.get("taskId"));
	        	store.setProcessedDate(storeResult.get("processedDate"));
	        	List<SurveyComProduct> productList = new ArrayList<SurveyComProduct>();
	        	for( ProductMaster upcMaster : projectUpcDetailList ) {
	        		String upc = upcMaster.getUpc();
	        		String facings = storeResult.get(upc);
	        		if ( StringUtils.isBlank(facings) ) {
	        			facings = "0";
	        		}
	        		String productShortName = upcMaster.getProduct_short_name();
	        		SurveyComProduct product = new SurveyComProduct();
	        		product.setUpc(upc);
	        		product.setFacings(facings);
	        		product.setProductShortName(productShortName);
	        		productList.add(product);
	        	}
	        	store.setProductList(productList);
	        	store.setProductCount(""+productList.size());
	        	recordList.add(store);
        	}
        }
        surveyResult.setRecordList(recordList);
        surveyResult.setRecordCount(""+recordList.size());

        LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends fetchSurveyStoreResults----------------\n");

        return surveyResult;
	}
	
	private String sendToSurvey(InputObject inputObject, String url, String payload) {
		LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts sendToSurvey----------------\n");
		String responseToRespondWith;
		String response = null;
		try {
			response = uploadToSurveyCom(url,payload);
		} catch (Exception e) {
			LOGGER.error("EXCEPTION {} {}",e.getMessage(), e);
		    response = e.getMessage();
		}
		LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadSurveyImageResults::Response From Survey.com:: {}",response);
		responseToRespondWith = createSurveyUploadResponseJson(inputObject,response);
		LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends sendToSurvey----------------\n");
		return responseToRespondWith;
	}
	
	private String uploadToSurveyCom(String url, String payload) throws Exception {
		LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts uploadToSurveyCom ----------------\n");
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader(SRV_X_AUTH_TOKEN_HEADER_KEY,SRV_X_AUTH_TOKEN_HEADER_VALUE);
		post.setHeader("Content-Type", "application/json");
		StringEntity jsonEntity = new StringEntity(payload);
		post.setEntity(jsonEntity);
		
		for (Header h : post.getAllHeaders()) {
			LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadToSurveyCom::Header= {} :: Value: {}", h.getName(), h.getValue());
		}
		
		HttpResponse response = client.execute(post);
		
		int responseCode = response.getStatusLine().getStatusCode();
		LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadToSurveyCom::POST Response Code = {}", responseCode);
		
		String responseString = EntityUtils.toString(response.getEntity());
		LOGGER.info("---------------SrvBulkUploaderServiceImpl::uploadToSurveyCom::POST Response String = {}", responseString);

		return responseString;
	}
	
	private String createSurveyUploadResponseJson(InputObject inputObject, String responseFromSurvey) {
		JsonObject response = new JsonObject();
		response.addProperty("customerCode", inputObject.getCustomerCode());
		response.addProperty("customerProjectId", inputObject.getCustomerProjectId());
		response.addProperty("externalProjectId", inputObject.getExternalProjectId());
		response.addProperty("uploadResponse", responseFromSurvey);
		return response.toString();
	}
	
    private SurveyComDownloadRecords getSurveyComData(String url) {
        LOGGER.info("---------------SrvBulkUploaderServiceImpl Starts getSurveyComData ----------------\n");
        SurveyComDownloadRecords records = null;
        
        try {
			HttpGet get = new HttpGet(url);
			get.setHeader(SRV_X_AUTH_TOKEN_HEADER_KEY,SRV_X_AUTH_TOKEN_HEADER_VALUE);
			
			HttpResponse response = client.execute(get);
			
			int responseCode = response.getStatusLine().getStatusCode();
			LOGGER.info("---------------BulkUploaderServiceImpl::getSurveyComData::GET Response Code = {}", responseCode);

			String jsonString = EntityUtils.toString(response.getEntity());
			
			Gson gson = new Gson();
			records = gson.fromJson(jsonString, SurveyComDownloadRecords.class);
		} catch (Exception e) {
			LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
		    LOGGER.error("exception", e);
		}
        LOGGER.info("---------------SrvBulkUploaderServiceImpl Ends getSurveyComData ----------------\n");

        return records;
    }
    
}

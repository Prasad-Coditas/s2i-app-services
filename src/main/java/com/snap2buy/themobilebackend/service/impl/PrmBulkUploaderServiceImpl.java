package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.snap2buy.themobilebackend.service.ProcessImageService;
import com.snap2buy.themobilebackend.async.CloudStorageService;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.dao.StoreMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.PrmBulkUploaderService;
import com.snap2buy.themobilebackend.service.ProcessImageService;
import com.snap2buy.themobilebackend.upload.PrmResultUploadTracker;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_PRM_BULK_UPLOADER_SERVICE)
@Scope("prototype")
public class PrmBulkUploaderServiceImpl implements PrmBulkUploaderService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private HttpClient client;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
    private StoreMasterDao storeMasterDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
    private ProcessImageService processImageService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;
    
	@Autowired
	private Environment env;

	@Autowired
    private CloudStorageService cloudStorageService;

    private static final String UPLOAD_SUCCESS_RESPONSE_STRING = "true";
    
    //Use 10 minutes as connection timeout for PRM requests
    private static int PRM_REQUEST_TIMEOUT = 10 * 60 * 1000;
    
    @PostConstruct
    public void init() {
    	RequestConfig.Builder requestBuilder = RequestConfig.custom();
    	requestBuilder.setConnectTimeout(PRM_REQUEST_TIMEOUT);
    	requestBuilder.setConnectionRequestTimeout(PRM_REQUEST_TIMEOUT);
    	requestBuilder.setSocketTimeout(PRM_REQUEST_TIMEOUT);

    	HttpClientBuilder builder = HttpClientBuilder.create();     
    	builder.setDefaultRequestConfig(requestBuilder.build());
    	client = builder.build();
    }

    private boolean saveImage(URL imageURL, String imageDirectoryForProject, String dateId, String storeId, String categoryId, String sync, String agentId, int projectId, String taskId, String fileId, String questionId, String imageStatus) {
        LOGGER.info("---------------BulkUploadEngine :: Downloading from :: {}", imageURL.toString());
        long currTimestamp = System.currentTimeMillis() / 1000L;
        Date date = new Date(currTimestamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String currentDateId = dateFormat.format(date);

        int code = 200;
        boolean retryable = false;
        try {
            HttpURLConnection huc = (HttpURLConnection) imageURL.openConnection();
            huc.setInstanceFollowRedirects(true);
            huc.setRequestMethod("GET");
            huc.connect();
            code = huc.getResponseCode();
            if (code == 302) {
                LOGGER.info("---------------BulkUploadEngine :: Redirect URL : {}", huc.getHeaderField("Location") );
                imageURL = new URL(huc.getHeaderField("Location"));
            }
        } catch (Exception e) {
            LOGGER.error("---------------BulkUploadEngine :: Error checking URL for Image Download Link :: {}", e.getMessage() );
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
                    LOGGER.info("---------------BulkUploadEngine :: Redirect URL : {}",huc.getHeaderField("Location") );
                    imageURL = new URL(huc.getHeaderField("Location"));
                }
            } catch (Exception e) {
                LOGGER.error("---------------BulkUploadEngine :: Error checking URL for Image Download Link in retry :: {}", e.getMessage() );
                return false;
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
                    LOGGER.error("---------------BulkUploadEngine :: Error creating URL for Image Download Link :: {}", e.getMessage() );
                    return false;
                }
            }
            UUID uniqueKey = UUID.randomUUID();

            String imageFilePath = imageDirectoryForProject + uniqueKey.toString().trim() + ".jpg";
            String imageThumbnailPath = projectId + "/" + uniqueKey.toString().trim() + "-thm.jpg";
            String previewPath = projectId + "/" + uniqueKey.toString().trim() + "-prv.jpg";
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
                LOGGER.error("---------------BulkUploadEngine :: Error downoading from Image Download Link :: {}", e.getMessage());
                return false;
            }
            File file = new File(imageFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.getParentFile().setReadable(true);
                file.getParentFile().setWritable(true);
                file.getParentFile().setExecutable(true);
            }

            if (file.length() > 0L) {

                //Uploaded to GCS
                cloudStorageService.storeImageByPath(projectId+"", uniqueKey.toString().trim(), imageFilePath);

                try {
                    if(Files.deleteIfExists(file.toPath())) {
                        LOGGER.info("PrmBulkUploaderServiceImpl: Thumbnail image deleted successfully from local storage.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.error("EXCEPTION in PrmBulkUploaderServiceImpl {}, {}", e.getMessage(), e);
                }

                LOGGER.info("---------------BulkUploadEngine :: Download Successful :: Storing to DB----------------\n");
                InputObject inputObject = new InputObject();
                if (!dateId.isEmpty()) {
                    Date newdateId = null;
                    try {
                        newdateId = dateFormat.parse(dateId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    inputObject.setVisitDate(dateFormat.format(newdateId));
                } else {
                    inputObject.setVisitDate(currentDateId);
                }
                inputObject.setStoreId(storeId);
                inputObject.setHostId("1");
                inputObject.setImageUUID(uniqueKey.toString().trim());
                inputObject.setCategoryId(categoryId.trim());
                inputObject.setLatitude("");
                inputObject.setLongitude("");
                inputObject.setTimeStamp("" + currTimestamp);
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
                inputObject.setImageFilePath(projectId + "/" + uniqueKey.toString().trim() + ".jpg");
                inputObject.setThumbnailPath(imageThumbnailPath);
                inputObject.setPreviewPath(previewPath);
                inputObject.setFileId(fileId);
                inputObject.setQuestionId(questionId);
                inputObject.setImageUrl(imageURL.toString());
                inputObject.setImageStatus(imageStatus);
                inputObject.setSource("web");
                LOGGER.info("---------------BulkUploadEngine :: Storing Image details to DB Start----------------\n");
                processImageService.storeImageDetails(inputObject, true); //true to indicate a bulk upload
				//Use this as preview image. It will be updated to most significant image later during store visit result computation.
                processImageDao.updatePreviewImageUUIDForStoreVisit(projectId, storeId, taskId, inputObject.getImageUUID());
                LOGGER.info("---------------BulkUploadEngine :: Storing Image details to DB End----------------\n");
            } else {
                LOGGER.error("---------------BulkUploadEngine :: Download Failed :: 0 byte file :: {}", imageFilePath );
            }
        } else {
            LOGGER.error("---------------BulkUploadEngine :: Download Failed :: Unexpected HTTP response code {}", code );
        }
        return true;
    }


    @Override
    public String getToken() throws IOException {
        LOGGER.info("---------------BulkUploaderServiceImpl Starts getToken ----------------\n");

        String url = "https://integrations.qtraxweb.com/token";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(getUrlEncodedParams());
        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code : " + responseCode);
        String jsonString = EntityUtils.toString(response.getEntity());

        LOGGER.info("---------------BulkUploaderServiceImpl Ends getToken ----------------\n");

        return extractToken(jsonString);
    }

    @Override
    public String extractToken(String response) {
        JsonElement jsElement = new JsonParser().parse(response);
        return jsElement.getAsJsonObject().get("access_token").getAsString();
    }

    @Override
    public UrlEncodedFormEntity getUrlEncodedParams() throws UnsupportedEncodingException {
        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        //: Username=snap2insight&Password=Premium1!&grant_type=password
        postParams.add(new BasicNameValuePair("Username", "snap2insight"));
        postParams.add(new BasicNameValuePair("Password", "tPrs7!!AAB50C1C9235E1E48DB1"));
        postParams.add(new BasicNameValuePair("grant_type", "password"));
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(postParams);
        return urlEncodedFormEntity;
    }

    @Override
    public List<PrmResponse> getPrmResponse(String token, String assessmentId) throws IOException {
        LOGGER.info("---------------BulkUploaderServiceImpl Starts getPrmResponse ----------------\n");
        
        String[] assessmentIdParts = assessmentId.trim().split("#");
        String assessmentIdForReq = assessmentIdParts[0];
        String jobIdForReq = assessmentIdParts[1];

        Gson gson = new Gson();
        String url = "https://integrations.qtraxweb.com/api/answers/getqtraxanswersbyactionsnap";
        String inputString = "{\"AssessmentId\":\""+assessmentIdForReq +"\",\"JobId\":\""+jobIdForReq+"\"}";
        String jsonString = invokePremiumAPI(url, token, inputString);
        //LOGGER.info("---------------post result =" + jsonString);
        Type collectionType = new TypeToken<List<PrmResponse>>(){}.getType();
        List<PrmResponse> prmResponses = (List<PrmResponse>)gson.fromJson(jsonString, collectionType);
        //LOGGER.info("---------------BulkUploaderServiceImpl :: Removing leading 0s in store number ----------------\n");
        //prmResponses.forEach(response -> response.setStoreNumber(response.getStoreNumber().replaceFirst("^0+(?!$)", "").trim()));
        LOGGER.info("---------------BulkUploaderServiceImpl Ends getPrmResponse ----------------\n");
        return prmResponses;
    }


	private String invokePremiumAPI(String url, String token, String payload) throws UnsupportedEncodingException,
			IOException, ClientProtocolException {
		LOGGER.info("---------------BulkUploaderServiceImpl Starts invokePremiumAPI ----------------\n");
		HttpPost post = new HttpPost(url);
        post.setHeader("Accept", "application/json");
        post.setHeader("Authorization", "Bearer " + token);
        post.setHeader("Content-Type", "application/json");
        StringEntity jsonEntity = new StringEntity(payload);
        post.setEntity(jsonEntity);

        for (Header h : post.getAllHeaders()) {
        	LOGGER.info("---------------BulkUploaderServiceImpl::invokePremiumAPI::Header= {}, Value= {}", h.getName(), h.getValue());
        }
        
        LOGGER.info("---------------BulkUploaderServiceImpl::invokePremiumAPI::POST Payload ={}", payload);
        
        LOGGER.info("---------------BulkUploaderServiceImpl::invokePremiumAPI::POST URL ={}", url );

        HttpResponse response = client.execute(post);
        
        int responseCode = response.getStatusLine().getStatusCode();
        LOGGER.info("---------------BulkUploaderServiceImpl::invokePremiumAPI::POST Response Code ={}",responseCode );

        String jsonString = EntityUtils.toString(response.getEntity());
		return jsonString;
	}

    @Override
    public LinkedHashMap<String, String> loadPremiumData(InputObject inputObject) {
        LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();
        int projectId = inputObject.getProjectId();
        String imageStatus = inputObject.getImageStatus();
        String assessmentId = inputObject.getAssessmentId();
        String batchId = ConverterUtil.getBatchIdForImport(projectId+"");
        LOGGER.info("=============================batchId={}",batchId);
        result.put("batchId",String.valueOf(batchId));

        //Create the directory for images to download.
        String imageDirectoryForProject = env.getProperty("disk_directory") + projectId + "/";
        File imageDirectory = new File(imageDirectoryForProject);
        imageDirectory.mkdirs();

        String categoryId = null;
        String retailerCode = null;
        boolean isParentProject = false;
        List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(projectId);
        if (projectDetail != null && !projectDetail.isEmpty()) {
            categoryId = projectDetail.get(0).get("categoryId");
            retailerCode = projectDetail.get(0).get("retailerCode");
            String isParentProjectCode = projectDetail.get(0).get("isParentProject");
            isParentProject = isParentProjectCode.equals("1") ? true : false;
        }
        final String finalRetailerCode = retailerCode;

        LinkedHashMap<String, String> retailerStoreIdToInternalStoreIdMap = storeMasterDao.getRetailerStoreIdMap(finalRetailerCode);

        //get list of question id we are interesting in
        List<ProjectQuestion> projectQuestions = metaServiceDao.getProjectQuestionsDetail(projectId);
        List<String> filterQuestionIdList = new ArrayList<String>();
        projectQuestions.stream().forEach(x -> filterQuestionIdList.add(x.getId()));
        LOGGER.info("=============================filterQuestionIdList.size()={}",filterQuestionIdList.size());
        result.put("filterQuestionIdList",String.valueOf(filterQuestionIdList.size()));

        //Get already uploaded stores with results
        List<StoreVisit> storeVisitsWithResults = processImageService.getAlreadyUploadedStoreVisit(projectId);
        LOGGER.info("=============================storeVisitsWithResults.size()={}",storeVisitsWithResults.size());
        result.put("storeVisitsWithResults",String.valueOf(storeVisitsWithResults.size()));

        //get store and taskId for which we have image
        List<StoreVisit> storeVisitsWithImages = processImageService.getStoreVisitsWithImages(projectId);
        result.put("storeVisitsWithImages",String.valueOf(storeVisitsWithImages.size()));


        //make http call and extract the token
        String token = null;
        try {
            token = getToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("token=" + token);

        //make http call and get prmResponses
        List<PrmResponse> prmResponses = null;
        try {
            prmResponses = getPrmResponse(token, assessmentId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("=============================prmResponses.size()={}",prmResponses.size());
        result.put("prmResponses",String.valueOf(prmResponses.size()));


        List<PrmResponse> validPrmResponses = prmResponses.stream().filter(x -> x.getServiceOrderId()!=null && x.getQuestionId()!=null && x.getStoreId()!=null && x.getDateReported()!=null ).collect(Collectors.toList());
        LOGGER.info("=============================validPrmResponses.size()={}",validPrmResponses.size());
        result.put("validPrmResponses",String.valueOf(validPrmResponses.size()));

        //filter PrmResponses for selected questionids
        List<PrmResponse> FilteredPrmResponses = validPrmResponses.stream().filter(x -> filterQuestionIdList.contains(x.getQuestionId())).collect(Collectors.toList());
        LOGGER.info("=============================FilteredPrmResponses.size()={}",FilteredPrmResponses.size());
        result.put("FilteredPrmResponses",String.valueOf(FilteredPrmResponses.size()));


        //filter for those PrmResponse which have photolink not null
        List<PrmResponse> prmResponseWithPhotoLink = FilteredPrmResponses.stream().filter(x -> x.getPhotoLink() != null).collect(Collectors.toList());
        LOGGER.info("=============================prmResponseWithPhotoLink.size()= {}",prmResponseWithPhotoLink.size());
        result.put("prmResponseWithPhotoLink",String.valueOf(prmResponseWithPhotoLink.size()));


        //filter for those stores which have atleast one image in imageStoreNew
        List<PrmResponse> prmResponsesNewImage = prmResponseWithPhotoLink.stream()
        		.filter(x -> !storeVisitsWithImages.contains(new StoreVisit(retailerStoreIdToInternalStoreIdMap.get(finalRetailerCode + "_" + x.getStoreId()), x.getServiceOrderId())))
        		.collect(Collectors.toList());
        LOGGER.info("=============================prmResponsesNewImage.size()= {}",prmResponsesNewImage.size());
        result.put("prmResponsesNewImage",String.valueOf(prmResponsesNewImage.size()));

        //find out all prmResponses for BatchId Update
        List<PrmResponse> prmResponsesBatchIdUpdate = prmResponsesNewImage.stream()
        		.filter(x -> storeVisitsWithResults.contains(new StoreVisit(retailerStoreIdToInternalStoreIdMap.get(finalRetailerCode + "_" + x.getStoreId()), x.getServiceOrderId())))
        		.collect(Collectors.toList());
        LOGGER.info("=============================prmResponsesBatchIdUpdate.size()={}",prmResponsesBatchIdUpdate.size());
        result.put("prmResponsesBatchIdUpdate",String.valueOf(prmResponsesBatchIdUpdate.size()));


        //storeVisitsWithResults then insert rep response
        List<PrmResponse> prmResponsesNotProcessed = FilteredPrmResponses.stream()
        		.filter(x -> !storeVisitsWithResults.contains(new StoreVisit(retailerStoreIdToInternalStoreIdMap.get(finalRetailerCode + "_" + x.getStoreId()), x.getServiceOrderId())))
        		.collect(Collectors.toList());
        LOGGER.info("=============================prmResponsesNotProcessed.size()={}",prmResponsesNotProcessed.size());
        result.put("prmResponsesNotProcessed",String.valueOf(prmResponsesNotProcessed.size()));

        //group prm responses for serviceOrderId
        Map<String, List<PrmResponse>> prmResponseNotProcessedGroup = prmResponsesNotProcessed.stream().collect(Collectors.groupingBy(PrmResponse::getServiceOrderId));

        //store all repResponse
        LinkedHashMap<String, String> storeRepResponseResult=storeRepResponses(prmResponseNotProcessedGroup, projectId, retailerCode, batchId);;
        result.putAll(storeRepResponseResult);

        //store all images, only for non parent project
        if ( !isParentProject ) {
        	LinkedHashMap<String, String> storeImageResult = storeImages(prmResponsesNewImage, imageDirectoryForProject,
    				projectId, retailerCode, categoryId, imageStatus);;
            result.putAll(storeImageResult);
        } else {
        	result.put("No images uploaded for this parent proejct", "No images uploaded for this parent project");
        }

        // Do compute result for store using rep responses, if any. As this store won't be computed for results because of no images AND
        LinkedHashMap<String, String> updateBatchIdResult=updateBatchId(prmResponsesBatchIdUpdate, projectId, retailerCode, batchId, retailerStoreIdToInternalStoreIdMap);
        result.putAll(updateBatchIdResult);

        // Set store result status to "Generate Aggregations" for all the stores uploaded in this import, using the batchId.
        String resultCode = "99"; // Generate Aggregations
        try {
            //add insert for date Id
            processImageDao.updateProjectStoreResultByBatchId(projectId, batchId, resultCode);
            LOGGER.info("---------------updateProjectStoreResultByBatchId...\n");
        } catch (Throwable t) {
            LOGGER.info("---------------Failed updateProjectStoreResultByBatchId...{}", t);
        }

        return result;
    }

    private LinkedHashMap<String, String> storeRepResponses(Map<String, List<PrmResponse>> prmResponseNotProcessedGroup, int projectId, String retailerCode, String batchId) {
        LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();

        Set<String> prmResponseNotProcessedGroupKeySet = prmResponseNotProcessedGroup.keySet();

        LOGGER.info("=============================prmResponseNotProcessedGroup.keySet().size()={}",prmResponseNotProcessedGroupKeySet.size());
        result.put("prmResponseNotProcessedGroupKeySet",String.valueOf(prmResponseNotProcessedGroupKeySet.size()));

        //for each store visit insert repResponse and make an entry in project store result
        for (String serviceOrder : prmResponseNotProcessedGroupKeySet) {
            LOGGER.info("---------------BulkUploadEngine :: for serviceOrder={}",serviceOrder);
            String taskId = serviceOrder;
            List<PrmResponse> prmResponsesPerServiceOrder = prmResponseNotProcessedGroup.get(serviceOrder);
            PrmResponse prmResponse1 = prmResponsesPerServiceOrder.get(0);
            String agentId = prmResponse1.getRepId();
            String prmStoreId = prmResponse1.getStoreId();
            List<LinkedHashMap<String, String>> listOfStores = metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(prmStoreId, retailerCode);
            StoreVisit currentStoreVisit = new StoreVisit();
            if(null != listOfStores && !listOfStores.isEmpty()){
                currentStoreVisit = new StoreVisit(listOfStores.get(0).get("storeId"), taskId);
            } else {
                String uniqueStoreId = generateStoreId();
                StoreMaster storeMaster = new StoreMaster();
                storeMaster.setStoreId(uniqueStoreId);
                storeMaster.setRetailerChainCode(retailerCode);
                storeMaster.setRetailerStoreId(prmStoreId);
                storeMaster.setComments("PREMIUM_LOAD");
                metaServiceDao.createStore(storeMaster);
                currentStoreVisit.setStoreId(uniqueStoreId);
                currentStoreVisit.setTaskId(taskId);
            }
            String imageLinkList = null;
            String dateId=getDateId(prmResponse1.getVisitDate());

            Map<String, String> repResponses = new HashMap<String, String>();
            for (PrmResponse prmResponse : prmResponsesPerServiceOrder) {
                repResponses.put(prmResponse.getQuestionId(), prmResponse.getUserResponse());
                if (prmResponse.getPhotoLink()!=null)
                    imageLinkList=prmResponse.getPhotoLink();
            }

            try {
                //add insert for date Id
                processImageService.insertOrUpdateStoreResult(projectId, currentStoreVisit.getStoreId(), "0", "0", "0", "99", "0", agentId, currentStoreVisit.getTaskId(), dateId , imageLinkList, batchId,"");
                LOGGER.info("---------------BulkUploadEngine :: Inserted one record in store results table for this store...\n");
            } catch (Throwable t) {
                LOGGER.info("---------------BulkUploadEngine :: Error inserting one record in store results table for this store...{}" + t);
            }



            try {
                processImageService.saveStoreVisitRepResponses(projectId, currentStoreVisit.getStoreId(), currentStoreVisit.getTaskId(), repResponses);
                LOGGER.info("---------------BulkUploadEngine :: Storing Rep Responses to DB End----------------\n");
            } catch (Throwable t) {
                LOGGER.error("---------------BulkUploadEngine :: Storing Rep Responses to DB Failed :: {}", t );
            }
        }
        return result;
    }

    private LinkedHashMap<String, String> storeImages(List<PrmResponse> prmResponsesNewImage,String imageDirectoryForProject, int projectId, String retailerCode, String categoryId, String imageStatus){
        LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();
        int saveImagedFailedFor=0;
        //for each image link store images
        for (PrmResponse prmResponse : prmResponsesNewImage) {
            String taskId = prmResponse.getServiceOrderId();
            String agentId = prmResponse.getRepId();
            String prmStoreId = prmResponse.getStoreId();
            String fileId=prmResponse.getFileId();
            String questionId=prmResponse.getQuestionId();
            String dateId= getDateId(prmResponse.getVisitDate());
            String imageLink = prmResponse.getPhotoLink();
            String sync = "false";
            URL imageURL = null;
            try {
                imageURL = new URL(imageLink);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            
            String internalStoreId = "";
            List<LinkedHashMap<String, String>> listOfStores = metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(prmStoreId, retailerCode);
            if(null != listOfStores && !listOfStores.isEmpty()){
            	internalStoreId = listOfStores.get(0).get("storeId");
            } else {
                String uniqueStoreId = generateStoreId();
                StoreMaster storeMaster = new StoreMaster();
                storeMaster.setStoreId(uniqueStoreId);
                storeMaster.setRetailerChainCode(retailerCode);
                storeMaster.setRetailerStoreId(prmStoreId);
                storeMaster.setComments("PREMIUM_LOAD");
                metaServiceDao.createStore(storeMaster);
                internalStoreId=uniqueStoreId;
            }
            

            LOGGER.info("==="+imageURL+ imageDirectoryForProject+ dateId+ internalStoreId+ categoryId+ sync+ agentId+ projectId+ taskId+ fileId+ questionId+ imageStatus+"==");
            Boolean saveFlag = saveImage(imageURL, imageDirectoryForProject, dateId, internalStoreId, categoryId, sync, agentId, projectId, taskId, fileId, questionId, imageStatus);

            if (saveFlag==false){
                saveImagedFailedFor++;
            }
        }
        LOGGER.info("=============================saveImagedFailedFor= {}",saveImagedFailedFor);
        result.put("saveImagedFailedFor",String.valueOf(saveImagedFailedFor));
        return result;
    }

    private String getDateId(String visitDate){
        long currTimestamp = System.currentTimeMillis() / 1000L;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        DateFormat newdateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(currTimestamp);
        String currentDateId = dateFormat.format(date);
        String dateId=null;
        if (visitDate==null) {
            try {
                dateId=newdateFormat.format(dateFormat.parse(currentDateId));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else{
            try {
                dateId = newdateFormat.format(dateFormat.parse(visitDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dateId;
    }

    private LinkedHashMap<String, String> updateBatchId(List<PrmResponse> prmResponsesBatchIdUpdate,
    		int projectId, String retailerCode, String batchId, Map<String,String> retailerStoreIdToInternalStoreIdMap){
        LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();
        HashSet<StoreVisit> setOfStoreVisit = new HashSet<>();

        for (PrmResponse prmResponse : prmResponsesBatchIdUpdate) {
            setOfStoreVisit.add(new StoreVisit(retailerStoreIdToInternalStoreIdMap.get(retailerCode + "_" + prmResponse.getStoreId()), prmResponse.getServiceOrderId()));
        }

        List<StoreVisit> existingStoreVisitsForBatchIdUpdate = new ArrayList<StoreVisit>(setOfStoreVisit);

        LOGGER.info("=============================existingStoreVisitsForBatchIdUpdate.size()= {}",existingStoreVisitsForBatchIdUpdate.size());
        result.put("existingStoreVisitsForBatchIdUpdate",String.valueOf(existingStoreVisitsForBatchIdUpdate.size()));

        // Set batchId to current batchId for all stores which are getting reprocessed in this upload
        LOGGER.info("---------------BulkUploadEngine :: Start updating batchId for stores which are getting reprocessed in this upload ----------------\n");
        processImageDao.updateBatchIdForProjectStoreResults(projectId, existingStoreVisitsForBatchIdUpdate, batchId);
        LOGGER.info("---------------BulkUploadEngine :: Updated batchId for stores which are getting reprocessed in this upload ----------------\n");

        return result;
    }

    @Override
	public LinkedHashMap<String, String> submitPremiumResults(InputObject inputObject) {
        LOGGER.info("---------------PrmBulkUploaderServiceImpl :: starts submit results for projectId = {} ----------------\n", inputObject.getProjectId());
		String responseString = "Initialized";
		// Call computeImageResults() first, ignore response. -- DISABLED FOR NOW
        LOGGER.info("---------------BulkUploaderServiceImpl :: starts computing image results for one final time----------------\n");
	    processImageService.computeImageResults(inputObject);
		LOGGER.info("---------------BulkUploaderServiceImpl :: ends computing image results for one final time----------------\n");

		// Get list of Premium File result to submit
		LOGGER.info("---------------PrmBulkUploaderServiceImpl :: starts get image results for given project----------------\n");
		List<Map<String,String>> imageResultList = processImageService.getImageResultsForPremium(inputObject);
		LOGGER.info("---------------PrmBulkUploaderServiceImpl :: ends get image results for given project----------------\n");

		if ( !imageResultList.isEmpty() ) {
			// Convert image result to premium format
			LOGGER.info("---------------PrmBulkUploaderServiceImpl :: starts converting image result to premium API format----------------\n");
			List<PrmResult> resultList = new ArrayList<PrmResult>();
			List<PrmResult> oneChunk = new ArrayList<PrmResult>();
			List<List<PrmResult>> chunkedList = new ArrayList<List<PrmResult>>();
			int maxChunkSize = 250;
			List<String> imageUUIDs = new ArrayList<String>();
			int i=1;
			for(Map<String,String> imageResult : imageResultList ) {
				PrmResult result = new PrmResult();
				result.setServiceOrderId(Integer.valueOf(imageResult.get("taskId")));
				result.setAssessmentId(Integer.valueOf(inputObject.getAssessmentId()));
				result.setPhotoReviewStatusId(new Integer(ImageResultCode.getImageResultCodeFromCode(imageResult.get("imageResultCode")).getStatusCode()));
				result.setPhotoReviewStatus(imageResult.get("imageResultDesc"));
				result.setPhotoReviewComment(imageResult.get("imageResultComments"));
				result.setProcessDate(imageResult.get("processedDate"));
				result.setFileId(Integer.valueOf(imageResult.get("fileId")));
				resultList.add(result);
				
				oneChunk.add(result);
				
				if ( i % maxChunkSize == 0 ) {
					chunkedList.add(oneChunk);
					oneChunk = new ArrayList<PrmResult>();
					i = 1;
				} else {
					i = i + 1;
				}
				
				imageUUIDs.add(imageResult.get("imageUUID"));
			}
			
			if ( oneChunk.size() > 0 ) {
				chunkedList.add(oneChunk);
			}
			
			LOGGER.info("---------------PrmBulkUploaderServiceImpl :: Total Chunks = {} ----------------\n", chunkedList.size());
			
			if ( Boolean.parseBoolean(inputObject.getSendToDestination()) && 
					env.getProperty("instance").toString().equalsIgnoreCase("prod") ) {
				//Now, invoke premium POST method to submit the results
				LOGGER.info("---------------PrmBulkUploaderServiceImpl :: starts submitting image result to premium----------------\n");
				try {
					String token = getToken();
					if ( token != null && !token.isEmpty() ) {
						String url = "https://integrations.qtraxweb.com/api/photo/sendsnapresults"; 
						int batchCount = 1;
						boolean allBatchesUploaded = true;
						for(List<PrmResult> oneBatch : chunkedList ) {
							LOGGER.info("---------------PrmBulkUploaderServiceImpl :: Submitting Batch {} ----------------\n", batchCount);
							SnapResultList oneUploadBatch = new SnapResultList(oneBatch);
							//Convert list of premium result objects to json
							String oneUploadPayload = new Gson().toJson(oneUploadBatch);
							responseString = invokePremiumAPI(url, token, oneUploadPayload);
							LOGGER.info("---------------PrmBulkUploaderServiceImpl :: Response ::{}", responseString );
							if ( !responseString.trim().equalsIgnoreCase(UPLOAD_SUCCESS_RESPONSE_STRING)) {
								LOGGER.info("---------------PrmBulkUploaderServiceImpl :: Upload not successful for batch {} :: Please try again", batchCount);
								allBatchesUploaded = false;
								break;
							}
							batchCount = batchCount + 1;
						}
						
						LOGGER.info("---------------PrmBulkUploaderServiceImpl :: ends submitting image result to premium----------------\n");
						
						if ( allBatchesUploaded ) {
							LOGGER.info("---------------PrmBulkUploaderServiceImpl :: All results uploaded, Update uploadedimagestatus to 1----------------\n");
							processImageService.updateProjectImageResultUploadStatus(imageUUIDs,"1");
							LOGGER.info("---------------PrmBulkUploaderServiceImpl :: updated uploadedimagestatus to 1----------------\n");
						} else {
							LOGGER.info("---------------PrmBulkUploaderServiceImpl :: Upload not successful :: Image status not updated----------------\n");
						}
					} else {
						responseString = "Access token is null or empty. Results are not uploaded";
						LOGGER.error("---------------PrmBulkUploaderServiceImpl :: Access Token is null or empty :: Results will not be submited to premium----------------\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
		            LOGGER.error("---------------PrmBulkUploaderServiceImpl :: Error invoking premium services:: {}", e.getMessage() );
					responseString = "Unexpected exception while submitting results";
				}
			} else {
				LOGGER.info("---------------PrmBulkUploaderServiceImpl :: NOT submitting image result to premium, returning payload back to caller----------------\n");
				//Convert list of premium result objects to json
				SnapResultList uploadObject = new SnapResultList(resultList);
				String premiumResultJsonPayload = new Gson().toJson(uploadObject);
				responseString = premiumResultJsonPayload;
			}
		} else {
			LOGGER.info("---------------PrmBulkUploaderServiceImpl :: nothing to submit----------------\n");
			responseString = "No results found to submit";
		}
		
		LOGGER.info("---------------PrmBulkUploaderServiceImpl :: submit response :: {}", responseString);

		LinkedHashMap<String,String> resultMap = new LinkedHashMap<String, String>();
		resultMap.put("result", responseString);
		return resultMap;
	}
	
	@Override
	public LinkedHashMap<String, String> loadNewQuestionPremium(InputObject inputObject) {
		 LinkedHashMap<String, String> result=new LinkedHashMap<String,String>();
	        
            int projectId = inputObject.getProjectId();
	        String assessmentId = null;
	        String questionId = inputObject.getQuestionId();
	        result.put("questionId",questionId.toString());

	        //get category and retailerCode from project
	        String categoryId = null;
	        String retailerCode = null;
	        List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(projectId);
	        if (projectDetail != null && !projectDetail.isEmpty()) {
	            categoryId = projectDetail.get(0).get("categoryId");
	            retailerCode = projectDetail.get(0).get("retailerCode");
	            assessmentId = projectDetail.get(0).get("customerProjectId");
	        }
	        
	        LOGGER.info("=============================categoryId={}, retailerCode={}",categoryId,retailerCode);
	        result.put("categoryId",categoryId.toString());
	        result.put("retailerCode",retailerCode.toString());

	    
	        //Get already uploaded stores with results
	        List<StoreVisit> storeVisitsWithResults = processImageService.getAlreadyUploadedStoreVisit(projectId);
	        LOGGER.info("=============================storeVisitsWithResults.size()={}",storeVisitsWithResults.size());
	        result.put("storeVisitsWithResults",String.valueOf(storeVisitsWithResults.size()));

	        //make http call and extract the token
	        String token = null;
	        try {
	            token = getToken();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        System.out.println("token=" + token);

	        //make http call and get prmResponses
	        List<PrmResponse> prmResponses = null;
	        try {
	            prmResponses = getPrmResponse(token, assessmentId);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        //base filter
	        List<PrmResponse> prmResponseFilteredBase = prmResponses.stream().filter(x -> x.getServiceOrderId()!=null && x.getQuestionId()!=null && x.getStoreId()!=null  && x.getDateReported()!=null).collect(Collectors.toList());
	        LOGGER.info("=============================prmResponseFilteredBase.size()={}",prmResponseFilteredBase.size());
	        result.put("prmResponseFilteredBase",String.valueOf(prmResponseFilteredBase.size()));

	        
	        //filter PrmResponses for selected questionids
	        List<PrmResponse> prmResponseSelected = prmResponseFilteredBase.stream().filter(x -> x.getQuestionId().equalsIgnoreCase(questionId)).collect(Collectors.toList());
	      
	        LOGGER.info("=============================prmResponseSelected.size()={}",prmResponseSelected.size());
	        result.put("prmResponseSelected",String.valueOf(prmResponseSelected.size()));

	        //storeVisitsWithResults then insert rep response
	        final String finalRetailerCode = retailerCode;
	        
	        LinkedHashMap<String, String> retailerStoreIdToInternalStoreIdMap = storeMasterDao.getRetailerStoreIdMap(finalRetailerCode);

	        List<PrmResponse> prmResponsesProcessed = prmResponseSelected.stream()
	        		.filter(x -> storeVisitsWithResults.contains(new StoreVisit(retailerStoreIdToInternalStoreIdMap.get(finalRetailerCode + "_" + x.getStoreId()), x.getServiceOrderId())))
	        		.collect(Collectors.toList());

	        LOGGER.info("=============================prmResponsesProcessed.size()={}",prmResponsesProcessed.size());
	        result.put("prmResponsesProcessed",String.valueOf(prmResponsesProcessed.size()));

	        int repResponsesInserted=0;
	        for (PrmResponse prmResponse : prmResponsesProcessed) {
	        	 String taskId = prmResponse.getServiceOrderId();
		         String prmStoreId = prmResponse.getStoreId();
		         String storeId = retailerStoreIdToInternalStoreIdMap.get(retailerCode + "_"+ prmStoreId);;
		         Map<String, String> repResponses = new HashMap<String, String>();
		         repResponses.put(prmResponse.getQuestionId(), prmResponse.getUserResponse());
		         try {
		                processImageService.saveStoreVisitRepResponses(projectId, storeId, taskId, repResponses);
		                
		                LOGGER.info("---------------BulkUploadEngine :: Storing Rep Responses to DB End----------------\n");
		            } catch (Throwable t) {
		                LOGGER.error("---------------BulkUploadEngine :: Storing Rep Responses to DB Failed :: {}", t );
		            }
		         repResponsesInserted++;
            }
	        LOGGER.info("=============================repResponsesInserted= {}",repResponsesInserted);
	        result.put("repResponsesInserted",String.valueOf(repResponsesInserted));

	        return result;
	}

    public String generateStoreId() {
        LOGGER.info("--------------- PrmBulkUploaderServiceImpl Starts generateStoreId ----------------\n");
        String storeId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        LOGGER.info("################################## StoreId: {}", storeId);
        List<LinkedHashMap<String, String>> result = metaServiceDao.getStoreMasterByStoreId(storeId);
        if(null != result && !result.isEmpty()){
            storeId = generateStoreId();
            LOGGER.info("Generating storeId again StoreId: {}", storeId);
        }
        return storeId;
    }

	@Override
	public void bulkUploadPremiumResults(List<String> projectIds) {
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Starts bulkUploadPremiumResults for projects = {} ----------------\n", projectIds);
		
		if (!PrmResultUploadTracker.getAll().isEmpty()) {
			throw new IllegalStateException("Some projects are being uploaded. Please try after sometime");
		}
		
		//Initialize all project ids to "Queued"
		for (String projectId : projectIds ) {
			PrmResultUploadTracker.add(projectId);
		}
		
		try {
			for ( String projectId : projectIds ) {
				PrmResultUploadTracker.update(projectId, "In Progress");
				
				// Getting customerProjectId from Project and assigning assessmentId as customerProjectId
				List<LinkedHashMap<String, String>> projects = metaServiceDao.getProjectDetail(Integer.parseInt(projectId));
				String[] assessmentIdParts = projects.get(0).get("customerProjectId").trim().split("#");
				String assessmentIdForReq = assessmentIdParts[0];
				//String jobIdForReq = assessmentIdParts[1];

				InputObject inputObject = new InputObject();
				inputObject.setProjectId(Integer.parseInt(projectId));
				inputObject.setAssessmentId(assessmentIdForReq);
				inputObject.setSendToDestination("true");
				this.submitPremiumResults(inputObject);
				
				PrmResultUploadTracker.update(projectId, "Completed");
			}
		} finally {
			//Clear
			PrmResultUploadTracker.removeAll();
		}
		
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Ends bulkUploadPremiumResults----------------\n", projectIds);
	}

	@Override
	public List<LinkedHashMap<String, String>> getPremiumBulkUploadStatus() {
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Starts getPremiumBulkUploadStatus----------------\n");
		List<LinkedHashMap<String, String>> statusList = new ArrayList<LinkedHashMap<String, String>>();
		for(Entry<String, String> project : PrmResultUploadTracker.getAll().entrySet()) {
			LinkedHashMap<String, String> oneProject = new LinkedHashMap<String, String>();
			oneProject.put("projectId", project.getKey());
			oneProject.put("status",project.getValue());
			statusList.add(oneProject);
		}
		
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Ends getPremiumBulkUploadStatus = {}----------------\n", statusList);
		return statusList;
	}

	@Override
	public String getJobDetails(String jobId) {
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Starts getJobDetails for jobId = {}----------------\n",jobId);
		String jobDetailsJsonString = "{}";
		//make http call and extract the token
		String token = null;
		try {
		    token = getToken();
		} catch (Exception e) {
			LOGGER.error("---------------PrmBulkUploaderServiceImpl :: Unable to retrieve token :: {}", e.getMessage(),e );
			throw new RuntimeException("Unable to retrieve token");
		}
        
		String url = "https://integrations.qtraxweb.com/api/jobs/getqtraxprojectsnap";
		String inputString = "{\"JobId\":\""+jobId +"\"}";
		try {
			jobDetailsJsonString = invokePremiumAPI(url, token, inputString);
		} catch (Exception e) {
			LOGGER.error("---------------PrmBulkUploaderServiceImpl :: Unable to retrieve job details :: {}", e.getMessage(),e );
		}
		
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Ends getJobDetails for jobId = {}----------------\n",jobId);
		return jobDetailsJsonString;
        /*
		JsonObject resultObject =null;
		try {
			resultObject = new JsonParser().parse(jobDetailsJsonString).getAsJsonObject();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		String jobName = resultObject.get("QtraxJob").getAsJsonObject().get("JobName").getAsString();
		String projectName = resultObject.get("QtraxJob").getAsJsonObject().get("ProjectName").getAsString();
		String isJobFinalized = resultObject.get("QtraxJob").getAsJsonObject().get("JobFinalized").getAsString();

		List<String> actions = new ArrayList<String>();
		Map<String,List<Map<String,String>>> actionsToQuestionsMap = new LinkedHashMap<String,List<Map<String,String>>>();

		JsonArray actionsArray = resultObject.get("QtraxActions").getAsJsonArray();
		for ( int i=0; i<actionsArray.size();i++) {
			String assessmentId = actionsArray.get(i).getAsJsonObject().get("AssessmentId").getAsString();
			String assessmentName = actionsArray.get(i).getAsJsonObject().get("AssessmentName").getAsString();
			actions.add(assessmentId + "#" + assessmentName);
			actionsToQuestionsMap.put(assessmentId, new ArrayList<Map<String,String>>());
		}
		
		JsonArray surveyArray = resultObject.get("QtraxSurvey").getAsJsonArray();
		for ( int i=0;i<surveyArray.size();i++) {
			Map<String,String> questionMap = new LinkedHashMap<String,String>();
			questionMap.put("questionId", surveyArray.get(i).getAsJsonObject().get("QuestionId").getAsString());
			questionMap.put("questionText", surveyArray.get(i).getAsJsonObject().get("QuestionText").getAsString().replace("\n", "").replace("\r", "").trim());
			String assessmentId = surveyArray.get(i).getAsJsonObject().get("AssessmentId").getAsString();
			questionMap.put("sequenceNumber", surveyArray.get(i).getAsJsonObject().get("SequenceNumber").getAsString());
			questionMap.put("questionTypeDescription", surveyArray.get(i).getAsJsonObject().get("QuestionTypeDescription").getAsString());
				
			StringBuilder questionResponseOptionsBuilder = new StringBuilder();
			if ( !surveyArray.get(i).getAsJsonObject().get("QuestionResponses").isJsonNull() ) {
				JsonArray responseArray = surveyArray.get(i).getAsJsonObject().getAsJsonArray("QuestionResponses");
				for ( int j=0;j<responseArray.size();j++) {
					questionResponseOptionsBuilder
						.append("\"")
						.append(responseArray.get(j).getAsJsonObject().get("ResponseText").getAsString().replace("\n", "").replace("\r", "").trim())
						.append("\"")
						.append("/");
				}
			}
				
			questionMap.put("questionResponseOptions",questionResponseOptionsBuilder.toString());
			
			actionsToQuestionsMap.get(assessmentId).add(questionMap);
				
		}
		
		//Generate XLSX report
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		//Job Summary 
		XSSFSheet jobDetailsSheet = workbook.createSheet("Job Summary");
		
		int rowNum = 0; 
		Row row = jobDetailsSheet.createRow(rowNum++);
		row.createCell(0).setCellValue("Job Name");
		row.createCell(1).setCellValue(jobName);
		
		row = jobDetailsSheet.createRow(rowNum++);
		row.createCell(0).setCellValue("Project Name");
		row.createCell(1).setCellValue(projectName);
		
		row = jobDetailsSheet.createRow(rowNum++);
		row.createCell(0).setCellValue("Job Finalized ?");
		row.createCell(1).setCellValue(isJobFinalized);
		
		row = jobDetailsSheet.createRow(rowNum++);
		row.createCell(0).setCellValue("Actions");
		
		for( String action : actions ) {
			row = jobDetailsSheet.createRow(rowNum++);
			String[] actionParts = action.split("#");
			row.createCell(0).setCellValue(actionParts[0]);
			row.createCell(1).setCellValue(actionParts[1]);
		}
		
		//Worksheets for each action
		String[] headerColumns = new String[] {"Question Id", "Question Sequence Number", "Question Text", "Question Type", "Question Response Options"};

		for ( String action : actionsToQuestionsMap.keySet() ) {
			List<Map<String,String>> questions = actionsToQuestionsMap.get(action);
			
			XSSFSheet actionSheet = workbook.createSheet(action);
			Row questionSheetHeaderRow = actionSheet.createRow(0);
			
			int colNum = 0;
			for(String columnHeader : headerColumns ) {
				Cell cell = questionSheetHeaderRow.createCell(colNum++);
				cell.setCellValue(columnHeader);
			}
			
			int questionRowNum = 1; 
			for ( Map<String, String> question : questions ) {
				Row questionRow = actionSheet.createRow(questionRowNum++);
				questionRow.createCell(0).setCellValue(question.get("questionId"));
				questionRow.createCell(1).setCellValue(question.get("sequenceNumber"));
				questionRow.createCell(2).setCellValue(question.get("questionText"));
				questionRow.createCell(3).setCellValue(question.get("questionTypeDescription"));
				questionRow.createCell(4).setCellValue(question.get("questionResponseOptions"));
			}
		}
		
		//Now write the workbook to file and return
		try {
			FileOutputStream outputStream = new FileOutputStream(tempFilePath);
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
		
		File f = new File(tempFilePath);
		
		LOGGER.info("--------------- PrmBulkUploaderServiceImpl Ends getJobDetails for jobId = {}----------------\n",jobId);
		
		return f;*/
	}
}

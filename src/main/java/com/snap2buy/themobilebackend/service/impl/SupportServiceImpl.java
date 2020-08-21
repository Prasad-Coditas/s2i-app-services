package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snap2buy.themobilebackend.dao.SupportDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.service.SupportService;
import com.snap2buy.themobilebackend.util.Constants;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * @author Anoop
 */
@Component(value = BeanMapper.BEAN_SUPPORT_SERVICE)
@Scope("prototype")
public class SupportServiceImpl implements SupportService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private static final Map<String,String> CUSTOMER_CODES = new LinkedHashMap<String,String>();
	static {
		CUSTOMER_CODES.put(Constants.ITG_CUSTOMER_CODE,"ITG Brands");
	}

    @Autowired
    @Qualifier(BeanMapper.BEAN_SUPPORT_DAO)
    private SupportDao supportDao;

	@Override
	public List<LinkedHashMap<String, Object>> getSupportInfoByStoreId(String customerCode, String storeId) {
		LOGGER.info("---------------SupportServiceImpl Starts getSupportInfoByStoreId for customerCode={}, storeId={}\n",customerCode, storeId);
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String,Object>>();
		
		if ( ! CUSTOMER_CODES.containsKey(customerCode) ) {
			return returnList;
		}
		
		List<Map<String,String>> storeVisits = supportDao.getStoreVisitsByStoreId(customerCode, storeId);
		
		Map<String,String> assignments = supportDao.getStoreAssignementByStoreId(customerCode, storeId);
		
		LinkedHashMap<String, Object> storeDetails = new LinkedHashMap<String, Object>();
		for(Map<String,String> oneVisit : storeVisits ) {
			if ( storeDetails.isEmpty() ) {
				storeDetails.put("storeId", oneVisit.get("storeId"));
				storeDetails.put("street", oneVisit.get("street"));
				storeDetails.put("city", oneVisit.get("city"));
				storeDetails.put("state", oneVisit.get("state"));
				storeDetails.put("zip", oneVisit.get("zip"));
				storeDetails.put("mappedTerritory",assignments.get("mappedTerritory"));
				storeDetails.put("mappedUser",assignments.get("mappedUser"));
				storeDetails.put("visits", new ArrayList<Map<String,Object>>());

				if ( StringUtils.isBlank(oneVisit.get("visitDateId")) ) {
					break;
				}
			}
			
			List<Map<String,Object>> visits = (List<Map<String, Object>>) storeDetails.get("visits");
			if ( visits.isEmpty() ) {
				Map<String,Object> visit = new LinkedHashMap<String,Object>();
				visit.put("visitDateId", oneVisit.get("visitDateId"));
				visit.put("userId", oneVisit.get("userId"));
				visit.put("firstName", oneVisit.get("firstName"));
				visit.put("lastName", oneVisit.get("lastName"));
				visit.put("phoneNumber", oneVisit.get("phoneNumber"));
				visit.put("storeId", oneVisit.get("storeId"));
				visit.put("street", oneVisit.get("street"));
				visit.put("city", oneVisit.get("city"));
				visit.put("state", oneVisit.get("state"));
				visit.put("zip", oneVisit.get("zip"));
				visit.put("mappedTerritory",assignments.get("mappedTerritory"));
				visit.put("mappedUser",assignments.get("mappedUser"));
				
				List<Map<String,Object>> projects = new ArrayList<Map<String, Object>>();
				Map<String,Object> project = new HashMap<String,Object>();
				project.put("projectName",oneVisit.get("projectName"));
				project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
				project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
				project.put("visitDateTime",oneVisit.get("processedDate"));
				projects.add(project);
				visit.put("projects", projects);
				
				visits.add(visit);
				storeDetails.put("visits", visits);
			} else {
				boolean foundAgentVisitDateMatch = false;
				for(Map<String,Object> visit : visits ) {
					if ( ((String)visit.get("visitDateId")).equals(oneVisit.get("visitDateId")) &&
							((String)visit.get("userId")).equals(oneVisit.get("userId"))) {
						foundAgentVisitDateMatch = true;
						List<Map<String,Object>> projects = (List<Map<String, Object>>) visit.get("projects");
						Map<String,Object> project = new HashMap<String,Object>();
						project.put("projectName",oneVisit.get("projectName"));
						project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
						project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
						project.put("visitDateTime",oneVisit.get("processedDate"));
						projects.add(project);
					}
				}
				
				if ( !foundAgentVisitDateMatch ) {
					Map<String,Object> visit = new LinkedHashMap<String,Object>();
					visit.put("visitDateId", oneVisit.get("visitDateId"));
					visit.put("userId", oneVisit.get("userId"));
					visit.put("firstName", oneVisit.get("firstName"));
					visit.put("lastName", oneVisit.get("lastName"));
					visit.put("phoneNumber", oneVisit.get("phoneNumber"));
					visit.put("storeId", oneVisit.get("storeId"));
					visit.put("street", oneVisit.get("street"));
					visit.put("city", oneVisit.get("city"));
					visit.put("state", oneVisit.get("state"));
					visit.put("zip", oneVisit.get("zip"));
					visit.put("mappedTerritory",assignments.get("mappedTerritory"));
					visit.put("mappedUser",assignments.get("mappedUser"));
					
					List<Map<String,Object>> projects = new ArrayList<Map<String, Object>>();
					Map<String,Object> project = new HashMap<String,Object>();
					project.put("projectName",oneVisit.get("projectName"));
					project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
					project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
					project.put("visitDateTime",oneVisit.get("processedDate"));
					projects.add(project);
					visit.put("projects", projects);
					
					visits.add(visit);
					storeDetails.put("visits", visits);
				}
			}
		}
		
		returnList.add(storeDetails);
		LOGGER.info("---------------SupportServiceImpl Ends getSupportInfoByStoreId for customerCode={}, storeId={}\n",customerCode, storeId);
		return returnList;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getSupportInfoByUserId(String customerCode, String userId) {
		LOGGER.info("---------------SupportServiceImpl Starts getSupportInfoByUserId for customerCode={}, userId={}\n",customerCode, userId);
		List<LinkedHashMap<String, Object>> returnList = new ArrayList<LinkedHashMap<String,Object>>();
		
		if ( ! CUSTOMER_CODES.containsKey(customerCode) ) {
			return returnList;
		}
		
		List<Map<String,String>> storeVisits = supportDao.getStoreVisitsByUserId(customerCode, userId);
		
		Map<String,String> assignments = supportDao.getStoreAssignementByUserId(customerCode, userId);
		
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		for(Map<String,String> oneVisit : storeVisits ) {
			if ( result.isEmpty() ) {
				result.put("userId", oneVisit.get("userId"));
				result.put("firstName", oneVisit.get("firstName"));
				result.put("lastName", oneVisit.get("lastName"));
				result.put("phoneNumber", oneVisit.get("phoneNumber"));
				result.put("lastLoginDate", oneVisit.get("lastLoginDate"));
				result.put("lastAccessedDate", oneVisit.get("lastAccessedDate"));
				result.put("mappedTerritory",assignments.get("mappedTerritory"));
				result.put("mappedUser",assignments.get("mappedUser"));
				result.put("visits", new ArrayList<Map<String,Object>>());
				
				if ( StringUtils.isBlank(oneVisit.get("storeId")) ) {
					break;
				}
				
			}
			List<Map<String,Object>> visits = (List<Map<String, Object>>) result.get("visits");
			if ( visits.isEmpty() ) {
				Map<String,Object> visit = new LinkedHashMap<String,Object>();
				visit.put("storeId", oneVisit.get("storeId"));
				visit.put("visitDateId", oneVisit.get("visitDateId"));
				visit.put("street", oneVisit.get("street"));
				visit.put("city", oneVisit.get("city"));
				visit.put("state", oneVisit.get("state"));
				visit.put("zip", oneVisit.get("zip"));
				visit.put("userId", oneVisit.get("userId"));
				visit.put("firstName", oneVisit.get("firstName"));
				visit.put("lastName", oneVisit.get("lastName"));
				visit.put("phoneNumber", oneVisit.get("phoneNumber"));
				visit.put("mappedTerritory",assignments.get("mappedTerritory"));
				visit.put("mappedUser",assignments.get("mappedUser"));
				
				List<Map<String,Object>> projects = new ArrayList<Map<String, Object>>();
				Map<String,Object> project = new HashMap<String,Object>();
				project.put("projectName",oneVisit.get("projectName"));
				project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
				project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
				project.put("visitDateTime",oneVisit.get("processedDate"));
				projects.add(project);
				visit.put("projects", projects);
				
				visits.add(visit);
				result.put("visits", visits);
			} else {
				boolean foundStoreVisitDateMatch = false;
				for(Map<String,Object> store : visits ) {
					if ( ((String)store.get("storeId")).equals(oneVisit.get("storeId")) &&
							((String)store.get("visitDateId")).equals(oneVisit.get("visitDateId"))) {
						foundStoreVisitDateMatch = true;
						List<Map<String,Object>> projects = (List<Map<String, Object>>) store.get("projects");
						Map<String,Object> project = new HashMap<String,Object>();
						project.put("projectName",oneVisit.get("projectName"));
						project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
						project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
						project.put("visitDateTime",oneVisit.get("processedDate"));
						projects.add(project);
					}
				}
				
				if ( !foundStoreVisitDateMatch ) {
					Map<String,Object> visit = new LinkedHashMap<String,Object>();
					visit.put("storeId", oneVisit.get("storeId"));
					visit.put("visitDateId", oneVisit.get("visitDateId"));
					visit.put("street", oneVisit.get("street"));
					visit.put("city", oneVisit.get("city"));
					visit.put("state", oneVisit.get("state"));
					visit.put("zip", oneVisit.get("zip"));
					visit.put("userId", oneVisit.get("userId"));
					visit.put("firstName", oneVisit.get("firstName"));
					visit.put("lastName", oneVisit.get("lastName"));
					visit.put("phoneNumber", oneVisit.get("phoneNumber"));
					visit.put("mappedTerritory",assignments.get("mappedTerritory"));
					visit.put("mappedUser",assignments.get("mappedUser"));
					
					List<Map<String,Object>> projects = new ArrayList<Map<String, Object>>();
					Map<String,Object> project = new HashMap<String,Object>();
					project.put("projectName",oneVisit.get("projectName"));
					project.put("receivedImageCount",Integer.parseInt(oneVisit.get("receivedImageCount")));
					project.put("expectedImageCount",Integer.parseInt(oneVisit.get("expectedImageCount")));
					project.put("visitDateTime",oneVisit.get("processedDate"));
					projects.add(project);
					visit.put("projects", projects);
					
					visits.add(visit);
					
					result.put("visits", visits);
				}
			}
		}
		returnList.add(result);
		LOGGER.info("---------------SupportServiceImpl Ends getSupportInfoByUserId for customerCode={}, userId={}\n",customerCode, userId);
		return returnList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getUserListByCustomerCode(String customerCode) {
		LOGGER.info("---------------SupportServiceImpl Starts getUserListByCustomerCode for customerCode={}\n",customerCode);

		List<LinkedHashMap<String, Object>> returnList =  new ArrayList<LinkedHashMap<String, Object>>();
		
		if ( ! CUSTOMER_CODES.containsKey(customerCode) ) {
			return returnList;
		}
		
		returnList =  supportDao.getUserListByCustomerCode(customerCode);
		
		LOGGER.info("---------------SupportServiceImpl Ends getUserListByCustomerCode for customerCode={}\n",customerCode);
		return returnList;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getCustomers() {
		LOGGER.info("---------------SupportServiceImpl Starts getCustomers \n");
		List<LinkedHashMap<String, Object>> customers = new ArrayList<LinkedHashMap<String, Object>>();
		for(String customerCode : CUSTOMER_CODES.keySet()) {
			LinkedHashMap<String, Object> oneCustomer = new LinkedHashMap<String, Object>();
			oneCustomer.put("customerCode", customerCode);
			oneCustomer.put("customerName", CUSTOMER_CODES.get(customerCode));
			customers.add(oneCustomer);
		}
		LOGGER.info("---------------SupportServiceImpl Ends getCustomers \n");
		return customers;
	}

	@Override
	public boolean createSupportRequest(Map<String, String> requestContents, List<String> attachments) {
		LOGGER.info("---------------SupportServiceImpl Starts createSupportRequest for rqeuestContents={} \n", requestContents);
		boolean submissionStatus = false;
		
		String uploadedFilesToken = null;
		
		try {
			//Upload Attachment files if any
			for(String attachmentFileName : attachments ) {
				uploadedFilesToken = uploadFile(attachmentFileName, uploadedFilesToken);
			}
			//now raise request
			submitSupportRequest(requestContents, uploadedFilesToken);
			submissionStatus = true;
		} catch (Exception e) {
			LOGGER.error("Error while uploading and submitting request to Zendesk {}, {}", e.getMessage(), e);
		}
		return submissionStatus;
	}
	
	private String uploadFile(String fileName, String token) throws Exception{
		String ZENDESK_UPLOAD_URL = "https://snap2insight.zendesk.com/api/v2/uploads.json?filename={fileName}";
		String ZENDESK_UPLOAD_URL_WITH_TOKEN = "https://snap2insight.zendesk.com/api/v2/uploads.json?filename={fileName}&token={token}";
	    String CONTENT_TYPE_APPLICATION_BINARY = "application/binary";

		String urlToUse = ZENDESK_UPLOAD_URL;
		if ( token != null ) {
			urlToUse = ZENDESK_UPLOAD_URL_WITH_TOKEN.replace("{token}", token);
		}
		
		File file = new File(fileName);
		
		urlToUse = urlToUse.replace("{fileName}", file.getName());

		HttpPost httpPost = new HttpPost(urlToUse);

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

			httpPost.setEntity(new FileEntity(file,ContentType.create(CONTENT_TYPE_APPLICATION_BINARY)));
			
			httpPost.setHeader("Content-type", CONTENT_TYPE_APPLICATION_BINARY);

			CloseableHttpResponse response = client.execute(httpPost);
			String responseOutput = EntityUtils.toString(response.getEntity());
			LOGGER.info(responseOutput);
			
			if ( token == null ) {
				JsonObject resultObject = new JsonParser().parse(responseOutput).getAsJsonObject();
				token = resultObject.get("upload").getAsJsonObject().get("token").getAsString();
			}
			
		} catch (Exception e) {
			LOGGER.error("Error while uploading files to zendesk {}, {}", e.getMessage(), e);
			throw e;
		} finally {
			//delete file from disk regardless of upload status
			file.delete();
		}
		return token;
	}
	
	private void submitSupportRequest(Map<String,String> requestContents, String uploadToken) throws Exception {
		String ZENDESK_REQUEST_URL = "https://snap2insight.zendesk.com/api/v2/requests.json";

		Map<String,String> requesterMap = new LinkedHashMap<String,String>();
		requesterMap.put("name", requestContents.get("firstName") + " " + requestContents.get("lastName"));
		requesterMap.put("email",  requestContents.get("userId"));
		
		Map<String,Object> commentsMap = new LinkedHashMap<String,Object>();
		String projectId = ConverterUtil.ifNullToNA(requestContents.get("projectId"));
		String projectName = ConverterUtil.ifNullToNA(requestContents.get("projectName"));
		String body = ConverterUtil.ifNullToNA(requestContents.get("body"));
		
		String userBody = " Project ID : {projectId} \n Project Name : {projectName} \n Description : \n {body} ";
		userBody = userBody.replace("{projectId}", projectId);
		userBody = userBody.replace("{projectName}", projectName);
		userBody = userBody.replace("{body}", body);
		commentsMap.put("body", userBody);
		if ( uploadToken != null ) {
			commentsMap.put("uploads", Arrays.asList(new String[] {uploadToken}));
		}
		
		Map<String,Object> requestMap = new LinkedHashMap<String,Object>();
		requestMap.put("requester", requesterMap);
		requestMap.put("subject", requestContents.get("subject"));
		requestMap.put("comment", commentsMap);
		requestMap.put("group_id", 360010619532l);
		
		Map<String,Object> payloadMap = new LinkedHashMap<String,Object>();
		payloadMap.put("request", requestMap);
		
		Gson gson = new Gson();
		String payloadJSON = gson.toJson(payloadMap);
		
		LOGGER.info("Zendesk Support Request payload : {}", payloadJSON);
		
		HttpPost httpPost = new HttpPost(ZENDESK_REQUEST_URL);

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			
		    httpPost.setEntity(new StringEntity(payloadJSON));
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
			String responseOutput = EntityUtils.toString(response.getEntity());
			LOGGER.info(responseOutput);
		} catch (Exception e) {
			LOGGER.error("Error while creating request in zendesk {}, {}", e.getMessage(), e);
			throw e;
		}
	}
}

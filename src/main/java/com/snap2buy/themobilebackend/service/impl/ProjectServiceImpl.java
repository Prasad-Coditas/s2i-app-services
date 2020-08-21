package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.util.ConverterUtil;
import com.snap2buy.themobilebackend.dao.*;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component(value = BeanMapper.BEAN_PROJECT_SERVICE)
@Scope("prototype")
public class ProjectServiceImpl implements ProjectService {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROJECT_DAO)
    private ProjectDao projectDao;

	@Autowired
	@Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
	private ProcessImageDao processImageDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
	private MetaServiceDao metaServiceDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_META_SERVICE)
	private MetaService metaService;
    
	@Autowired
	@Qualifier(BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
	private ProcessImageService processImageService;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
	private StoreMasterDao storeMasterDao;
	
	/**
	 * Method to get Get Project by CustomerCode and Status
	 */
	@Override
	public List<LinkedHashMap<String, String>> getProjectByCustomerAndStatus(String customerCode, String status) {
		LOGGER.info("---------------ProjectServiceImpl Starts getProjectByCustomerAndStatus----------------\n");
		
        List<LinkedHashMap<String, String>> resultList = projectDao.getProjectByCustomerAndStatus(customerCode, status);
        
        LOGGER.info("---------------ProjectServiceImpl Ends getProjectByCustomerAndStatus ----------------\n");
        return resultList;
	}

	/**
	 * Method to get Projects by projectId
	 * @return List of LinkedHashMap<String, String>
	 */
	@Override
	public List<LinkedHashMap<String, String>> getProjectsByCustomerCodeAndCustomerProjectId(int projectId) {
		LOGGER.info("---------------ProjectServiceImpl Starts getProjectsByCustomerCodeAndCustomerProjectId----------------\n");
		
        List<LinkedHashMap<String, String>> resultList = projectDao.getProjectsByCustomerCodeAndCustomerProjectId(projectId);
        
        LOGGER.info("---------------ProjectServiceImpl Ends getProjectsByCustomerCodeAndCustomerProjectId ----------------\n");
        return resultList;
	}

	@Override
	public void saveProjectRepResponses(ProjectRepResponse projectRepResponse) {
		LOGGER.info("ProjectServiceImpl Starts saveProjectRepResponses {}", projectRepResponse);

		String storeId = projectRepResponse.getStoreId();
		String retailerStoreId = projectRepResponse.getRetailerStoreId();
		String placeId = projectRepResponse.getPlaceId();
		String customerStoreNumber = projectRepResponse.getCustomerStoreNumber();

		boolean storeIdPresent = (storeId == null || storeId.equalsIgnoreCase("-9")) ? false : true;
		boolean retailerStoreIdPresent = (retailerStoreId == null || retailerStoreId.equalsIgnoreCase("-9")) ? false : true;
		boolean placeIdPresent = (placeId == null || placeId.equalsIgnoreCase("-9")) ? false : true;
		boolean customerStoreNumberPresent = (customerStoreNumber == null || customerStoreNumber.equalsIgnoreCase("-9")) ? false : true;

		boolean createNewStore = false;

		if (!retailerStoreIdPresent) { retailerStoreId = null; }
		if (!placeIdPresent) { placeId = null; }
		
		//if storeId is present in request, use it. Else, try lookup.
		if(!storeIdPresent) {
			//If customerStoreNumber is present in request, lookup using that from customer-store mapping.
			if ( customerStoreNumberPresent ) {
				//lookup, if found set this storeId
				String mappedStoreId = metaServiceDao.getStoreByCustomerCodeAndCustomerStoreNumber(projectRepResponse.getCustomerCode(), customerStoreNumber);
				projectRepResponse.setStoreId(mappedStoreId);
			} else {
				//If retailerStoreId is present in request, lookup using retailerStoreId+projectRetailerChainCode combination.
				//Else If placeId is present in request, try lookup by placeId.
				if ( retailerStoreIdPresent ) {
					List<LinkedHashMap<String, String>> projectDetail = metaServiceDao.getProjectDetail(Integer.parseInt(projectRepResponse.getProjectId()));
					String retailerCode = projectDetail.get(0).get("retailerCode");
					//Check for   and RetailerChainCode
					List<LinkedHashMap<String, String>> storeResult = metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(retailerStoreId, retailerCode);
					if ( storeResult == null || storeResult.isEmpty()) {
						createNewStore = true;
					} else {
						projectRepResponse.setStoreId(storeResult.get(0).get("storeId"));
					}
				} else if ( placeIdPresent ) {
					List<LinkedHashMap<String, String>> result = storeMasterDao.getStoreMasterByPlaceId(projectRepResponse.getPlaceId());
					if(null == result || result.isEmpty()) {
						createNewStore = true;
					} else {
						projectRepResponse.setStoreId(result.get(0).get("storeId"));
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
			storeMaster.setLatitude(projectRepResponse.getLatitude());
			storeMaster.setLongitude(projectRepResponse.getLongitude());
			storeMaster.setComments("PROJECT_REP_RESPONSES");
			storeMaster.setName(projectRepResponse.getName());
			storeMaster.setStreet(projectRepResponse.getStreet());
			storeMaster.setCity(projectRepResponse.getCity());
			storeMaster.setState(projectRepResponse.getState());
			storeMaster.setCountry(projectRepResponse.getCountry());
    		
			storeMasterDao.createStoreWithPlaceId(storeMaster);
			
			projectRepResponse.setStoreId(uniqueStoreId);
		}
		
		String storeResultCode = "99";
		String storeStatus = "0";

		if (!processImageDao.isStoreAvailableInStoreResults(Integer.valueOf(projectRepResponse.getProjectId()), projectRepResponse.getStoreId(), projectRepResponse.getTaskId())) {
			LOGGER.info("ProjectServiceImpl saveProjectRepResponses StoreNotAvailable in storeResults");

			String batchId = ConverterUtil.getBatchIdForImport(projectRepResponse.getProjectId()+"");
			String imageUrl = "";

			processImageDao.insertOrUpdateStoreResult(Integer.valueOf(projectRepResponse.getProjectId()), projectRepResponse.getStoreId(),
					"0", "0", "0", storeResultCode, storeStatus,
					projectRepResponse.getAgentId(), projectRepResponse.getTaskId(), projectRepResponse.getVisitDate(), imageUrl,batchId, projectRepResponse.getCustomerProjectId());

		}
		
		projectDao.saveProjectRepResponses(projectRepResponse);
		
		//If rep response indicates there are no photos captured, send FCM notification for completion.
		//If at least one photo is captured for any of the photo questions, don't send FCM notification. It will be sent after images are uploaded.
		
		List<ProjectQuestion> questions = metaServiceDao.getProjectQuestionsDetail(Integer.valueOf(projectRepResponse.getProjectId()));
		List<String> photoQuestionIds = new ArrayList<String>();
		for(ProjectQuestion question : questions) {
			if ("PH".equalsIgnoreCase(question.getQuestionType())) {
				photoQuestionIds.add(question.getId());
			}
		}
		
		boolean hasPhotosToUpload = false;
		for(ProjectResponse questionRespone : projectRepResponse.getProjectResponseList() ) {
			if ( photoQuestionIds.contains(questionRespone.getQuestionId()) ) {
				hasPhotosToUpload = true;
				break;
			}
		}
		
		if (!hasPhotosToUpload) {
			InputObject inputObject = new InputObject();
			inputObject.setStoreId(projectRepResponse.getStoreId());
			inputObject.setPlaceId(projectRepResponse.getPlaceId());
			inputObject.setVisitDate(projectRepResponse.getVisitDate());
			inputObject.setTaskId(projectRepResponse.getTaskId());
			inputObject.setProjectId(Integer.valueOf(projectRepResponse.getProjectId()));
			inputObject.setUserId(projectRepResponse.getAgentId());
			String storeVisitStatus = "0";
			String projectName = "";
			String resultComment = "";
			processImageService.sendAppNotification(inputObject, storeVisitStatus, projectName, resultComment);
		}

		LOGGER.info("ProjectServiceImpl Ends saveProjectRepResponses");
	}

}

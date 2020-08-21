/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snap2buy.themobilebackend.rest.action;


import com.google.gson.Gson;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.service.MetaService;
import com.snap2buy.themobilebackend.service.PrmBulkUploaderService;
import com.snap2buy.themobilebackend.service.ProjectService;
import com.snap2buy.themobilebackend.util.CustomSnap2BuyOutput;
import com.snap2buy.themobilebackend.util.Snap2BuyOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author sachin
 */
@Component(value = BeanMapper.BEAN_REST_ACTION_S2P)
@Scope("prototype")
public class RestS2PAction {

    private static Logger LOGGER = LoggerFactory.getLogger(RestS2PAction.class);


    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE)
    private MetaService metaService;

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROJECT_SERVICE)
    private ProjectService projectService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PRM_BULK_UPLOADER_SERVICE)
    private PrmBulkUploaderService prmBulkUploaderService;




    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "base-spring-ctx.xml");
        RestS2PAction restS2PAction = (RestS2PAction) context.getBean("RestS2PAction");
        LOGGER.info("Checking logger");


        InputObject inputObject = new InputObject();
        inputObject.setCategoryId("test");
        inputObject.setLatitude("45.56531392");
        inputObject.setLongitude("-122.8443362");
        inputObject.setTimeStamp("2008-01-01 00:00:01");
        inputObject.setUserId("agsachin");
        inputObject.setInstanceId("i-05f14990");
        inputObject.setCustomerCode("DEM");
        inputObject.setCustomerProjectId("35");
        inputObject.setAssessmentId("114817");
        System.out.println(restS2PAction.loadPremiumData(inputObject));
    }


    public String listProject(InputObject inputObject) {
        LOGGER.info("RestAction Starts listProject:: customer code = {}", inputObject.getCustomerCode());
        List<LinkedHashMap<String, Object>> resultListToPass = metaService.listProject(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        reportInput.put("customerCode", inputObject.getCustomerCode());
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;

        if (resultListToPass.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(resultListToPass, metaList);
        }

        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("RestAction Ends listProject");
        return output;
    }

    public Snap2BuyOutput getGeoMappedStoresByUserId(String customerCode, String userId) {
        LOGGER.info("---------------RestAction Starts getGeoMappedStoresByUserId:: customerCode={}, userId: {}", customerCode, userId);
        List<LinkedHashMap<String, String>> storesList = metaService.getGeoMappedStoresByUserId(customerCode, userId);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(storesList, reportInput);
        LOGGER.info("---------------RestAction Ends getTerritoryMappedStoresByUserId----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput loadPremiumData(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts loadPremiumData----------------\n");
        List<LinkedHashMap<String,String>> resultListToPass=new ArrayList<LinkedHashMap<String, String>>();

        // Getting customerProjectId from Project and assigning assessmentId as customerProjectId
        List<LinkedHashMap<String, String>> projects = projectService.getProjectsByCustomerCodeAndCustomerProjectId(inputObject.getProjectId());
        inputObject.setAssessmentId(projects.get(0).get("customerProjectId"));

        LinkedHashMap<String, String> result= prmBulkUploaderService.loadPremiumData(inputObject);
        resultListToPass.add(result);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("assessmentId", inputObject.getAssessmentId());
        reportInput.put("imageStatus", inputObject.getImageStatus());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends loadPremiumData----------------\n");

        return reportIO;
    }
}

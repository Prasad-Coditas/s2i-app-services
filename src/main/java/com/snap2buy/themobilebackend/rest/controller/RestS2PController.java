/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snap2buy.themobilebackend.rest.controller;

import com.google.gson.Gson;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.mapper.ParamMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.rest.action.RestS2PAction;
import com.snap2buy.themobilebackend.service.MetaService;
import com.snap2buy.themobilebackend.util.CustomSnap2BuyOutput;
import com.snap2buy.themobilebackend.util.Snap2BuyOutput;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author sachin
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(value = "/s2b")
@Scope("request")
public class RestS2PController {

    private Logger LOGGER = LoggerFactory.getLogger(RestS2PController.class);


    @Autowired
    @Qualifier(BeanMapper.BEAN_REST_ACTION_S2P)
    private RestS2PAction restS2PAction;

    @Autowired
    //@Qualifier(BeanMapper.BEAN_META_SERVICE)
    private MetaService metaService;


    @GetMapping(value = "/listProject", produces = MediaType.APPLICATION_JSON)
    public String listProject(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.SHOW_ONLY_CHILD_PROJECTS) @DefaultValue("false") String showOnlyChildProjects,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts listProject");

        try {
            String platform = null != request.getAttribute("platform")
                    ? request.getAttribute("platform").toString().trim().toLowerCase() : "";

            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setShowOnlyChildProjects(Boolean.valueOf(showOnlyChildProjects));
            if (StringUtils.isNotBlank(platform) && (platform.equals("android") || platform.equals("ios"))) {
                inputObject.setSource("app");
            } else {
                inputObject.setSource("web");
            }

            //New Code Start
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
            //New Code End
            //return restS2PAction.listProject(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input", "-9");
            List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);

            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("Controller Ends listProject");
            return output;
        }
    }


    @GetMapping(value = "/getStoresByUserId", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput getStoresByUserId(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoresByUserId----------------\n");
        try {
            String customerCode = null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString() : "";
            String userId = null != request.getAttribute("userId") ? request.getAttribute("userId").toString().trim().toLowerCase() : "";

            LOGGER.info("---------------RestAction Starts getGeoMappedStoresByUserId:: customerCode={}, userId: {}", customerCode, userId);
            List<LinkedHashMap<String, String>> storesList = metaService.getGeoMappedStoresByUserId("customerCode", userId);

            HashMap<String, String> reportInput = new HashMap<String, String>();
            Snap2BuyOutput reportIO = new Snap2BuyOutput(storesList, reportInput);
            LOGGER.info("---------------RestAction Ends getTerritoryMappedStoresByUserId----------------\n");
            return reportIO;

            //return restS2PAction.getGeoMappedStoresByUserId(customerCode, userId);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input", "-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoresByUserId----------------\n");
            return rio;
        }
    }
}
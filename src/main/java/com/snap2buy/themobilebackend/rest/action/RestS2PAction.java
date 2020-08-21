/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snap2buy.themobilebackend.rest.action;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snap2buy.themobilebackend.async.CloudStorageService;
import com.snap2buy.themobilebackend.dao.StoreMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.model.itg.GenericGeo;
import com.snap2buy.themobilebackend.service.*;
import com.snap2buy.themobilebackend.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.URLEncoder;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author sachin
 */
@Component(value = BeanMapper.BEAN_REST_ACTION_S2P)
@Service
@Scope("prototype")
public class RestS2PAction {

    private static Logger LOGGER = LoggerFactory.getLogger(RestS2PAction.class);

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROCESS_IMAGE_SERVICE)
    private ProcessImageService processImageService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PRODUCT_MASTER_SERVICE)
    private ProductMasterService productMasterService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE)
    private MetaService metaService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_AUTH_SERVICE)
    private AuthenticationService authService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_REPORTING_SERVICE)
    private ReportingService reportingService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PRM_BULK_UPLOADER_SERVICE)
    private PrmBulkUploaderService prmBulkUploaderService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_SRV_BULK_UPLOADER_SERVICE)
    private SrvBulkUploaderService srvBulkUploaderService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_SCORE_SERVICE)
    private ScoreService scoreService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
    private StoreMasterDao storeMasterDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_SHELF_ANALYSIS_SERVICE)
    private ShelfAnalysisService shelfAnalysisService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_PROJECT_SERVICE)
    private ProjectService projectService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_UI_SERVICE)
    private UIService uiService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_ALTRIA_DASHBOARD_SERVICE)
    private AltriaDashboardService altriaDashboardService;

    @Autowired
    @Qualifier(BeanMapper.BEAN_USER_SERVICE)
    private UserService userService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_ITG_DASHBOARD_SERVICE)
    private ITGDashboardService itgDashboardService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_ITG_AGGREGATION_SERVICE)
    private ITGAggregationService itgAggregationService;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_SUPPORT_SERVICE)
    private SupportService supportService;
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Autowired
    private CloudStorageService cloudStorageService;


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

    public String saveImage(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts saveImage----------------\n");
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();

        LOGGER.info("user : {}", inputObject.getUserId());
        Map<String, Object> result = processImageService.storeImageDetails(inputObject, false); //false to indicate this is not a bulk upload
        resultList.add(result);
        
        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("categoryId", inputObject.getCategoryId());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("latitude", inputObject.getLatitude());
        reportInput.put("longitude", inputObject.getLongitude());
        reportInput.put("userId", inputObject.getUserId());
        reportInput.put("TimeStamp", inputObject.getTimeStamp());
        reportInput.put("agentId", inputObject.getAgentId());
        reportInput.put("taskId", inputObject.getTaskId());
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("questionId", inputObject.getQuestionId());
        reportInput.put("dateId", inputObject.getVisitDate());
        reportInput.put("retailerStoreId", inputObject.getRetailerStoreId());
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("placeId", inputObject.getPlaceId());
        reportInput.put("responseCode", "200");
        reportInput.put("responseMessage", "Image Stored Successfully");
        reportInput.put("imageFilePath", inputObject.getImageFilePath());
        reportInput.put("headers", "UPC, Left Top X, Left Top Y, Width, Height, Promotion, Price, Price_Flag");
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;
        if (resultList.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(resultList, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
        LOGGER.info("---------------RestAction Ends saveImage----------------\n");
        return output;
    }

    public Snap2BuyOutput getJob(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getJob----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result = processImageService.getJob(inputObject);

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("hostId", inputObject.getHostId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getJob----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getCronJobCount(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getCronJobCount----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result = processImageService.getCronJobCount(inputObject);

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("hostId", inputObject.getHostId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getCronJobCount----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getUpcDetails(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getUpcDetails----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result = productMasterService.getUpcDetails(inputObject);
        File image = productMasterService.getUpcImage(inputObject);
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("upc", inputObject.getUpc());
        reportInput.put("imagePth", image.getAbsolutePath());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getUpcDetails----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput storeShelfAnalysis(ShelfAnalysisInput shelfAnalysisInput) {
        LOGGER.info("---------------RestAction Starts storeShelfAnalysis----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("imageUUID : {}", shelfAnalysisInput.getImageUUID());
        shelfAnalysisService.storeShelfAnalysis(shelfAnalysisInput);
        LOGGER.info("StoreShelfAnalysis done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "ShelfAnalysis Stored Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("imageUUID", shelfAnalysisInput.getImageUUID());
        reportInput.put("storeId", shelfAnalysisInput.getStoreID());
        reportInput.put("categoryId", shelfAnalysisInput.getCategoryID());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends storeShelfAnalysis----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getShelfAnalysis(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getShelfAnalysis----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        LOGGER.info("imageUUID : {}", inputObject.getImageUUID());
        result = shelfAnalysisService.getShelfAnalysis(inputObject.getImageUUID());
        LOGGER.info("getShelfAnalysis done");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("imageUUID", inputObject.getImageUUID());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getShelfAnalysis----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getReport(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getReport----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        //String query = queryGenerationService.generateQuery(inputObject);

        LOGGER.info("getShelfAnalysis done");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("imageUUID", inputObject.getImageUUID());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getReport----------------\n");

        return reportIO;
    }

    public File getUpcImage(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getUpcImage----------------\n");

        LOGGER.info("upc : " + inputObject.getUpc());

        LOGGER.info("---------------RestAction Ends getUpcImage----------------\n");

        return productMasterService.getUpcImage(inputObject);
    }

    public void storeThumbnails(String imageFolderPath) {
        LOGGER.info("---------------RestAction Starts storeThumbnails----------------\n");

        productMasterService.storeThumbnails(imageFolderPath);

        LOGGER.info("---------------RestAction Ends storeThumbnails----------------\n");
    }

    public Snap2BuyOutput getImageAnalysis(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getImageAnalysis----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();
        List<LinkedHashMap<String, String>> filteredResultListToPass = new ArrayList<LinkedHashMap<String, String>>();
        LOGGER.info("imageUUID : {}", inputObject.getImageUUID());
        resultListToPass = processImageService.getImageAnalysis(inputObject.getImageUUID());
        
        if ( !Boolean.parseBoolean(inputObject.getShowAll()) && !resultListToPass.isEmpty() ) {
        	 int projectId = Integer.valueOf(resultListToPass.get(0).get("projectId"));
             inputObject.setProjectId(projectId);
        	List<LinkedHashMap<String, String>> projectUpcs = metaService.getProjectUpcDetail(inputObject);
            List<String> nonInternalSkuTypes = new ArrayList<String>();

            for(Map<String,String> upc : projectUpcs ) {
            	if ( !upc.get("skuTypeId").equals("99") ) {
            		nonInternalSkuTypes.add(upc.get("upc"));
            	}
            }
            for(LinkedHashMap<String, String> result : resultListToPass) {
            	if (nonInternalSkuTypes.contains(result.get("upc"))) {
            		filteredResultListToPass.add(result);
            	}
            }
            resultListToPass = filteredResultListToPass;
        }
        
        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("showAll", inputObject.getShowAll());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getImageAnalysis----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getStoreOptions(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStoreOptions----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.getStoreOptions(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("header", "retailerCode,retailer,stateCode,state,city");
        reportInput.put("retailerCode", inputObject.getRetailerCode());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getStoreOptions----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getImages(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getImages----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.getImages(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("dateId", inputObject.getVisitDate());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getImages----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getProjectStoreImages(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoreImages----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.getProjectStoreImages(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectStoreImages----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getStores(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStores----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.getStores(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("retailerChainCode", inputObject.getRetailerChainCode());
        reportInput.put("stateCode", inputObject.getStateCode());
        reportInput.put("city", inputObject.getCity());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getStores----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getDistributionLists() {
        LOGGER.info("---------------RestAction Starts getDistributionLists----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = productMasterService.getDistributionLists();

        HashMap<String, String> reportInput = new HashMap<String, String>();

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getDistributionLists----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput doDistributionCheck(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts doDistributionCheck----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.doDistributionCheck(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        Long numUpcFound = 0L;
        Long numUpcNotFound = 0L;
        Long totalFacings = 0L;

        for (LinkedHashMap<String, String> entry : resultListToPass) {
            if (entry.get("osa").equalsIgnoreCase("1")) {
                numUpcFound++;
                totalFacings += Long.parseLong(entry.get("facing"));
            } else if (entry.get("osa").equalsIgnoreCase("0")) {
                numUpcNotFound++;
                totalFacings += Long.parseLong(entry.get("facing"));
            }
        }

        reportInput.put("listId", inputObject.getListId());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("numUpcFound", String.valueOf(numUpcFound));
        reportInput.put("numUpcNotFound", String.valueOf(numUpcNotFound));
        reportInput.put("totalFacings", String.valueOf(totalFacings));

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends doDistributionCheck----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput doBeforeAfterCheck(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts doBeforeAfterCheck----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.doBeforeAfterCheck(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        Long newUpcAdded = 0L;
        Long numUpcRemoved = 0L;
        Long numUpcMoreFacings = 0L;
        Long numUpcLessFacings = 0L;
        Long numUpcUnChangedFacings = 0L;

        for (LinkedHashMap<String, String> entry : resultListToPass) {
            if ((entry.get("before_osa").equalsIgnoreCase("0")) && (entry.get("after_osa").equalsIgnoreCase("1"))) {
                newUpcAdded++;
            } else if ((entry.get("before_osa").equalsIgnoreCase("1")) && (entry.get("after_osa").equalsIgnoreCase("0"))) {
                numUpcRemoved++;
            } else if (Integer.parseInt(entry.get("after_facing")) > Integer.parseInt(entry.get("before_facing"))) {
                numUpcMoreFacings++;
            } else if (Integer.parseInt(entry.get("before_facing")) > Integer.parseInt(entry.get("after_facing"))) {
                numUpcLessFacings++;
            } else if (Integer.parseInt(entry.get("before_facing")) == Integer.parseInt(entry.get("after_facing"))) {
                numUpcUnChangedFacings++;
            }

        }
        reportInput.put("prevImageUUID", inputObject.getPrevImageUUID());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("newUpcAdded", String.valueOf(newUpcAdded));
        reportInput.put("numUpcRemoved", String.valueOf(numUpcRemoved));
        reportInput.put("numUpcMoreFacings", String.valueOf(numUpcMoreFacings));
        reportInput.put("numUpcLessFacings", String.valueOf(numUpcLessFacings));
        reportInput.put("numUpcUnChangedFacings", String.valueOf(numUpcUnChangedFacings));

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends doBeforeAfterCheck----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getImageMetaData(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getImageMetaData----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        resultListToPass = processImageService.getImageMetaData(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("imageUUID", inputObject.getImageUUID());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getImageMetaData----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput doShareOfShelfAnalysis(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts doShareOfShelfAnalysis----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("imageUUID : " + inputObject.getImageUUIDCsvString());
        resultListToPass = processImageService.doShareOfShelfAnalysis(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("imageUUID", inputObject.getImageUUIDCsvString());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends doShareOfShelfAnalysis----------------\n");

        return reportIO;
    }

    public File getShelfAnalysisCsv() {
        LOGGER.info("---------------RestAction Starts getShelfAnalysisCsv----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis();

        File shelfAnalysis = shelfAnalysisService.getShelfAnalysisCsv(tempFilePath);

        LOGGER.info("---------------RestAction Ends getShelfAnalysisCsv----------------\n");

        return shelfAnalysis;
    }

    public File doShareOfShelfAnalysisCsv(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts doShareOfShelfAnalysisCsv----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis();

        File shareOfShelfAnalysisCsv = processImageService.doShareOfShelfAnalysisCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends doShareOfShelfAnalysisCsv----------------\n");

        return shareOfShelfAnalysisCsv;
    }

    public Snap2BuyOutput updateLatLong(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts updateLatLong----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("imageUUID : {}", inputObject.getImageUUIDCsvString());
        processImageService.updateLatLong(inputObject);
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "updateLatLong Stored Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("imageUUID", inputObject.getImageUUID());
        reportInput.put("latitude", inputObject.getLatitude());
        reportInput.put("longitude", inputObject.getLongitude());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateLatLong----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput listCategory() {
        LOGGER.info("---------------RestAction Starts listCategory-----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listCategory();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listCategory----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput listCustomer() {
        LOGGER.info("---------------RestAction Starts listCustomer-----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listCustomer();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listCustomer----------------\n");
        return reportIO;
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

    public Snap2BuyOutput listProjectType() {
        LOGGER.info("---------------RestAction Starts listProjectType-----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listProjectType();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listProjectType----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput listSkuType() {
        LOGGER.info("---------------RestAction Starts listSkuType-----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listSkuType();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listSkuType----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput listRetailer() {
        LOGGER.info("---------------RestAction Starts listRetailer------------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listRetailer();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listRetailer----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput listProjectUpc(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts listProjectUpc------------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listProjectUpc(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listProjectUpc----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getCategoryDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getCategoryDetail--id : {}", inputObject.getId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getCategoryDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", inputObject.getId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getCategoryDetail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getCustomerDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getCustomerDetail--customerCode : {}", inputObject.getCustomerCode());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getCustomerDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", inputObject.getCustomerCode());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getCustomerDetail----------------\n");
        return reportIO;
    }

    public String getProjectDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectDetail--customerProjectId : {}, CustomerCode: {}", inputObject.getCustomerProjectId(), inputObject.getCustomerCode());
        List<Project> resultListToPass = metaService.getProjectDetail(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectDetail----------------\n");
        return output;
    }

    public Snap2BuyOutput getProjectTypeDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectTypeDetail--id : {}", inputObject.getId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getProjectTypeDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", inputObject.getId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectTypeDetail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getSkuTypeDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getSkuTypeDetail -- id : {}", inputObject.getId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getSkuTypeDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", inputObject.getId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getSkuTypeDetail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getProjectUpcDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectUpcDetail--projectId : {}", inputObject.getProjectId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getProjectUpcDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectUpcDetail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getRetailerDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getRetailerDetail--retailerCode : {}", inputObject.getRetailerCode());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getRetailerDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("retailerCode", inputObject.getRetailerCode());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getRetailerDetail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createCustomer(Customer customerInput) {
        LOGGER.info("---------------RestAction Starts createCustomer----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("code : " + customerInput.getCustomerCode());
        metaService.createCustomer(customerInput);
        LOGGER.info("createCustomer done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "Customer Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", customerInput.getId());
        reportInput.put("code", customerInput.getCustomerCode());
        reportInput.put("name", customerInput.getName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createCustomer----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createCategory(Category categoryInput) {
        LOGGER.info("---------------RestAction Starts createCategory----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name : " + categoryInput.getName());
        metaService.createCategory(categoryInput);
        LOGGER.info("createCategory done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "Category Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", categoryInput.getId());
        reportInput.put("name", categoryInput.getName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createCategory----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createRetailer(Retailer retailerInput) {
        LOGGER.info("---------------RestAction Starts createRetailer----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name : " + retailerInput.getName());
        metaService.createRetailer(retailerInput);
        LOGGER.info("createRetailer done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "Retailer Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", retailerInput.getId());
        reportInput.put("name", retailerInput.getName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createRetailer----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createProjectType(ProjectType projectTypeInput) {
        LOGGER.info("---------------RestAction Starts createProjectType----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name : " + projectTypeInput.getName());
        metaService.createProjectType(projectTypeInput);
        LOGGER.info("createProjectType done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "ProjectType Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", projectTypeInput.getId());
        reportInput.put("name", projectTypeInput.getName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createProjectType----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createSkuType(SkuType skuTypeInput) {
        LOGGER.info("---------------RestAction Starts createSkuType----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name : " + skuTypeInput.getName());
        metaService.createSkuType(skuTypeInput);
        LOGGER.info("createProjectType done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "SkuType Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", skuTypeInput.getId());
        reportInput.put("name", skuTypeInput.getName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createSkuType----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createProject(Project projectInput) {
        LOGGER.info("---------------RestAction Starts createProject----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name: {}", projectInput.getProjectName());

        InputObject inputObject = new InputObject();
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        
        // if isParentProject is null or "null" , consider this as a child project i.e. set to 0.
        if( StringUtils.isBlank(projectInput.getIsParentProject()) 
        		|| projectInput.getIsParentProject().equalsIgnoreCase("null")) {
        	projectInput.setIsParentProject("0");
        }
        
        // if parentProjectId is null or "null" , set it to 0.
        if( StringUtils.isBlank(projectInput.getParentProjectId()) 
        		|| projectInput.getParentProjectId().equalsIgnoreCase("null")) {
        	projectInput.setParentProjectId("0");
        }
        
        // if isParentProject=0, parentProjectId must be set to a valid value (existing parent project).
        if ( projectInput.getIsParentProject().equals("0") ) {
        	
        	if ( projectInput.getParentProjectId().equals("0") ||
        			projectInput.getParentProjectId().equals("-1")) {
        		LOGGER.info("Parent Project Id must be set for a child project");
                result.put("responseCode", "204");
                result.put("responseMessage", "Parent Project Id must be set for a child project");
        	} else {
        		inputObject.setProjectId(Integer.valueOf(projectInput.getParentProjectId()));
        		List<Project> projects = metaService.getProjectDetail(inputObject);
                if(null != projects && !projects.isEmpty() && projects.get(0).getIsParentProject().equals("1")) {
                    boolean projectCreated = metaService.createProject(projectInput);
                    
                    if ( !projectCreated ) {
                    	result.put("responseCode", "204");
                        result.put("responseMessage", "Project creation failed. Internal Error.");
                    } else {
                    	LOGGER.info("createProject done");

                        metaService.createCustomerCodeProjectMap(projectInput.getCustomerCode(), Integer.valueOf(projectInput.getId()));
                        LOGGER.info("CustomerCode and Project Mapping done");

                        result.put("responseCode", "200");
                        result.put("responseMessage", "Project Created Successfully");
                    }
                }else {
                    LOGGER.info("Parent Project with id {} not found", projectInput.getParentProjectId());
                    result.put("responseCode", "204");
                    result.put("responseMessage", "Parent Project with id " + projectInput.getParentProjectId() +" not found");
                }
        	}
        } else if ( projectInput.getIsParentProject().equals("1") ) {
            //project is both child and parent.
        	if (projectInput.getParentProjectId().equals("-1")){
                LOGGER.info("ProjectId itself parentProjectId");

                boolean projectCreated = metaService.createProject(projectInput);
                
                if ( !projectCreated ) {
                	result.put("responseCode", "204");
                    result.put("responseMessage", "Project creation failed. Internal Error.");
                } else {
                	LOGGER.info("createProject done");

                    projectInput.setParentProjectId(projectInput.getId());

                    metaService.updateParentProjectId(projectInput);
                    LOGGER.info("updateParentProjectId done");

                    metaService.createCustomerCodeProjectMap(projectInput.getCustomerCode(), Integer.valueOf(projectInput.getId()));
                    LOGGER.info("CustomerCode and Project Mapping done");

                    result.put("responseCode", "200");
                    result.put("responseMessage", "Project Created Successfully");
                }
                
            } else {
            	//If parentProjectId=0, go ahead. No grand parent.
            	if ( projectInput.getParentProjectId().equals("0") ) {
            		boolean projectCreated = metaService.createProject(projectInput);
            		
            		if ( !projectCreated ) {
            			result.put("responseCode", "204");
                        result.put("responseMessage", "Project creation failed. Internal Error.");
            		} else {
            			LOGGER.info("createProject done");

                        metaService.createCustomerCodeProjectMap(projectInput.getCustomerCode(), Integer.valueOf(projectInput.getId()));
                        LOGGER.info("CustomerCode and Project Mapping done");

                        result.put("responseCode", "200");
                        result.put("responseMessage", "Project Created Successfully");
            		}
                    
            	} else { // A grand parent is mentioned, check validity.
            		inputObject.setProjectId(Integer.valueOf(projectInput.getParentProjectId()));
            		List<Project> projects = metaService.getProjectDetail(inputObject);
                    if(null != projects && !projects.isEmpty() && projects.get(0).getIsParentProject().equals("1")) {
                    	LOGGER.info("Parent Project with id {} not found", projectInput.getParentProjectId());
                        result.put("responseCode", "204");
                        result.put("responseMessage", "Parent Project with id " + projectInput.getParentProjectId() +" not found");
                    } else {
                    	boolean projectCreated = metaService.createProject(projectInput);
                    	
                    	if ( !projectCreated) {
                    		result.put("responseCode", "204");
                            result.put("responseMessage", "Project creation failed. Internal Error.");
                    	} else {
                    		LOGGER.info("createProject done");

                            metaService.createCustomerCodeProjectMap(projectInput.getCustomerCode(), Integer.valueOf(projectInput.getId()));
                            LOGGER.info("CustomerCode and Project Mapping done");

                            result.put("responseCode", "200");
                            result.put("responseMessage", "Project Created Successfully");
                    	}
                    }
            	}
            }
        } else {
        	LOGGER.info("Invalid value for isParentProject : {}", projectInput.getIsParentProject());
            result.put("responseCode", "204");
            result.put("responseMessage", "Invalid value for isParentProject : " + projectInput.getIsParentProject());
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", projectInput.getId());
        reportInput.put("name", projectInput.getProjectName());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createProject----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createUpc(ProductMaster upcInput) {
        LOGGER.info("---------------RestAction Starts createUpc----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("UPC : {}", upcInput.getUpc());
        productMasterService.createUpc(upcInput);
        LOGGER.info("createUpc done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "UPC Created Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("upc", upcInput.getUpc());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createUpc----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput addUpcToProjectId(ProjectUpc projectUpc) {
        LOGGER.info("---------------RestAction Starts createProject----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("ProjectId : {}", projectUpc.getProjectId());
        metaService.addUpcToProjectId(projectUpc);
        LOGGER.info("createProject done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "add upc to project Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerProjectId", projectUpc.getProjectId());
        reportInput.put("facingCount", projectUpc.getExpectedFacingCount());
        reportInput.put("upc", projectUpc.getUpc());
        reportInput.put("skuType", projectUpc.getSkuTypeId());
        reportInput.put("ingUrl1", projectUpc.getImageUrl1());
        reportInput.put("ingUrl2", projectUpc.getImageUrl2());
        reportInput.put("ingUrl3", projectUpc.getImageUrl3());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createProject----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput generateAggs(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts generateAggs----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.generateAggs(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends generateAggs----------------\n");

        return reportIO;
    }
    public String getProjectStoreResults(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoreResults----------------\n");
        List<ProjectStoreResultWithUPC> storeList = processImageService.getProjectStoreResults(inputObject);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId",inputObject.getProjectId()+"");
        reportInput.put("storeId",inputObject.getStoreId());
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("taskId", inputObject.getTaskId());
        reportInput.put("source", inputObject.getSource());
        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);
        
        CustomSnap2BuyOutput reportIO = null;
        if ( storeList == null || storeList.isEmpty() ) {
        	Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
        	reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
        LOGGER.info("---------------RestAction Ends getProjectStoreResults----------------\n");

        return output;
    }

    public Snap2BuyOutput getProjectTopStores(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectTopStores----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getProjectTopStores(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("limit", inputObject.getLimit());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectTopStores----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getProjectBottomStores(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectBottomStores----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getProjectBottomStores(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("limit", inputObject.getLimit());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectBottomStores----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput listStores() {
        LOGGER.info("---------------RestAction Starts listStores-----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listStores();

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listStores----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput createStore(StoreMaster storeMaster) {
        LOGGER.info("---------------RestAction Starts storeMaster----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("id : " + storeMaster.getStoreId());
        metaService.createStoreWithPlaceId(storeMaster);
        LOGGER.info("createStore done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "store Created Successfully");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", storeMaster.getStoreId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends storeMaster----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput updateStore(StoreMaster storeMaster) {
        LOGGER.info("---------------RestAction Starts updateStore----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("id : {}", storeMaster.getStoreId());
        metaService.updateStore(storeMaster);
        LOGGER.info("updateStore done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "store updated Successfully");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", storeMaster.getStoreId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateStore----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput updateProject(Project projectInput) {
        LOGGER.info("---------------RestAction Starts updateProject----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("name: {}", projectInput.getProjectName());

        InputObject inputObject = new InputObject();
        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        if(null != projectInput.getIsParentProject() && projectInput.getIsParentProject().equalsIgnoreCase("0")){
            inputObject.setProjectId(Integer.valueOf(projectInput.getParentProjectId()));

            List<Project> projects = metaService.getProjectDetail(inputObject);
            if(null != projects && !projects.isEmpty()) {

                boolean projectUpdated = metaService.updateProject(projectInput);
                
                if ( !projectUpdated ) {
                	result.put("responseCode", "204");
                    result.put("responseMessage", "Project Update failed. Internal Error.");
                } else {
                	LOGGER.info("UpdateProject done");

                    result.put("responseCode", "200");
                    result.put("responseMessage", "Project Updated Successfully");
                }
                
            }else {
                LOGGER.info("Parent Project not found for projectId: {}", projectInput.getParentProjectId());
                result.put("responseCode", "204");
                result.put("responseMessage", "Invalid parent projectId");
            }
        }else {
            if (null != projectInput.getParentProjectId() && projectInput.getParentProjectId().equalsIgnoreCase("-9")){
                inputObject.setProjectId(Integer.valueOf(projectInput.getId()));

                List<Project> projects = metaService.getProjectDetail(inputObject);
                if(null != projects && !projects.isEmpty()) {
                    projectInput.setParentProjectId(projects.get(0).getParentProjectId());
                    projectInput.setIsParentProject(projects.get(0).getIsParentProject());

                    boolean projectUpdated = metaService.updateProject(projectInput);
                    
                    if ( !projectUpdated ) {
                    	result.put("responseCode", "204");
                        result.put("responseMessage", "Project Update failed. Internal Error.");
                    } else {
                    	LOGGER.info("UpdateProject done");

                        result.put("responseCode", "200");
                        result.put("responseMessage", "Project Updated Successfully");
                    }
                }else {
                    LOGGER.info("Project not found for projectId: {}", projectInput.getId());
                    result.put("responseCode", "204");
                    result.put("responseMessage", "Parent ProjectId missing");
                }
            }else {
            	boolean projectUpdated = metaService.updateProject(projectInput);
            	
            	if ( !projectUpdated ) {
                	result.put("responseCode", "204");
                    result.put("responseMessage", "Project Update failed. Internal Error.");
                } else {
                	LOGGER.info("updateProject done");

                    result.put("responseCode", "200");
                    result.put("responseMessage", "Project Updated Successfully");
                }
            }
        }

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", projectInput.getId());
        reportInput.put("name", projectInput.getProjectName());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateProject----------------\n");
        return reportIO;
    }

    public String getProjectSummary(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectSummary----------------\n");

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<Map<String,Object>> resultListToPass=new ArrayList<Map<String, Object>>();
        Map<String,Object> summary = metaService.getProjectSummary(inputObject);
        resultListToPass.add(summary);
        
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectSummary----------------\n");

        return output;
    }

    public Snap2BuyOutput getStoreDetail(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStoreDetail--storeId : {}", inputObject.getStoreId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.getStoreDetail(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("storeId", inputObject.getStoreId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getStoreDetail----------------\n");
        return reportIO;
    }

    public String getProjectStoresWithNoUPCs(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoresWithNoUPCs----------------\n");
        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<StoreWithImages> resultListToPass = new ArrayList<StoreWithImages>();
        resultListToPass = processImageService.getProjectStoresWithNoUPCs(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getProjectStoresWithNoUPCs----------------\n");
        return output;
    }

    public String getProjectAllStoreImages(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectAllStoreImages----------------\n");
        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<StoreWithImages> resultListToPass = new ArrayList<StoreWithImages>();
        resultListToPass = processImageService.getProjectAllStoreImages(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        return output;
    }

    public String getProjectStoresWithDuplicateImages(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoresWithDuplicateImages----------------\n");
        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<DuplicateImages> resultListToPass = new ArrayList<DuplicateImages>();
        resultListToPass = processImageService.getProjectStoresWithDuplicateImages(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        return output;
    }

    public Snap2BuyOutput generateStoreVisitResults(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts generateStoreVisitResults----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.generateStoreVisitResults(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends generateStoreVisitResults----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getProjectAllStoreResults(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectAllStoreResults----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getProjectAllStoreResults(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectAllStoreResults----------------\n");

        return reportIO;
    }

    public File getProjectAllStoreResultsCsv(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectAllStoreResultsCsv----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis();

        File projectAllStoreResultsCsv = processImageService.getProjectAllStoreResultsCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getProjectAllStoreResultsCsv----------------\n");

        return projectAllStoreResultsCsv;
    }

    public File getProjectAllStoreResultsDetailCsv(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectAllStoreResultsDetailCsv----------------\n");
        
        String tempFilePath = "/tmp/csvDownloadAllStoreResultsDetail" + System.currentTimeMillis();
        
        File projectAllStoreResultsCsvDetailCsv = processImageService.getProjectAllStoreResultsDetailCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getProjectAllStoreResultsDetailCsv----------------\n");

        return projectAllStoreResultsCsvDetailCsv;
    }

    public Snap2BuyOutput createUser(User userInput) {
        LOGGER.info("---------------RestAction Starts createUser----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getEmail());
        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getEmail());
        boolean isSuccess = createUser(userInput, userList, "No existing record found for userId : {}", "New user created with userId : {}", "User Id {} already exists in the system. Rejecting the request.");

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (isSuccess == true) {
            result.put("responseCode", "200");
            result.put("responseMessage", "User Created Successfully");

            String emailBody  = "<p>Dear " + userInput.getFirstName() + ",</p>"
                    + "<p>Thanks for signing up with Snap2Insight, please use below details to complete your sign up process.</p>"
                    + "<p><strong>Activation code: "+ userInput.getAuthToken().substring(0, 6).trim() +"</strong></p>"
                    + "<p><strong>Customer code</strong>: Please check with your company representative</p>"
                    + "<p>For any assistance, reach out to support@snap2insight.com</p>"
                    + "<p>Thanks,<br>"
                    + "Snap2Insight team</p>";
            sendEmail(emailBody, "Your Snap2Insight account has been created\n", userInput.getEmail(), null);

        } else {
            result.put("responseCode", "204");
            result.put("responseMessage", "Existing user found. Request Rejected.");
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("email", userInput.getEmail());
        reportInput.put("firstName", userInput.getFirstName());
        reportInput.put("lastName", userInput.getLastName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends createUser----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput getUserDetail(String userId) {
        LOGGER.info("---------------RestAction Starts getUserDetail --userName : {}", userId);
        List<LinkedHashMap<String, String>> resultListToPass = authService.getUserDetail(userId);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getUserDetail -- userId : {}", userId);
        return reportIO;
    }

    public Snap2BuyOutput updateUser(User userInput) {
        LOGGER.info("---------------RestAction Starts updateteUser----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("userId : " + userInput.getUserId());
        authService.updateUser(userInput);

        if(!userInput.getGeoLevel().isEmpty() && !userInput.getGeoLevelId().isEmpty()){
            userService.createUserGeoMap(userInput);
            LOGGER.info("updateUser: UserGeoMap created for UserId {}", userInput.getUserId());
        }

        LOGGER.info("udpateUser done");

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "User Updated Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());
        reportInput.put("firstName", userInput.getFirstName());
        reportInput.put("lastName", userInput.getLastName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateUser----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput updateUserPassword(User userInput) {
        LOGGER.info("---------------RestAction Starts updateUserPassword----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("userId : {}", userInput.getUserId());
        authService.updateUserPassword(userInput);
        LOGGER.info("updateUserPassword done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "User password updated Successfully");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateUserPassword----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput deleteUser(String userId) {
        LOGGER.info("---------------RestAction Starts deleteUser----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("userId : {}", userId);
        authService.deleteUser(userId);
        LOGGER.info("deleteUser done");


        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "User Deleted Successfully");
        resultListToPass.add(result);


        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends deleteUser----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput login(String userId, String password) {
        LOGGER.info("---------------RestAction Starts login for user : {}", userId);

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        List<LinkedHashMap<String, String>> userList = authService.getUserForAuth(userId);

        boolean isAuthenticated = true;
        if (userList == null || userList.isEmpty()) {
            isAuthenticated = false;
        } else {
            if (password == null || !password.equals(userList.get(0).get("password"))) {
                isAuthenticated = false;
            }
        }

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (isAuthenticated ) {
        	if ( userList.get(0).get("status").equalsIgnoreCase(Constants.USER_STATUS_ACTIVE)) {
	        	// Check auth token expiry, and refresh if required
	        	User userWithRefreshedToken = authService.refreshAuthToken(userId, userList);
	            try {
	                //Last login time added for userId
	                authService.updateUserLastLoginStatus(userId);
	            } catch (Exception e) {
	                LOGGER.info("EXCEPTION while updating user's last login time {}", e);
	            }
	
	            result.put("responseCode", "200");
	            result.put("status", "success");
	            result.put("customerCode", userList.get(0).get("customerCode"));
	            result.put("firstName", userList.get(0).get("firstName"));
	            result.put("lastName", userList.get(0).get("lastName"));
	            result.put("role",userList.get(0).get("role"));
	            result.put("logo", "");
	            result.put("supportEmail", Constants.S2I_SUPPORT_EMAIL);
	            result.put("supportPhoneNumber", Constants.S2I_SUPPORT_PHONE_NUMBER);
	            result.put("authToken", userWithRefreshedToken.getAuthToken());
        	} else {
                result.put("responseCode", "204");
                result.put("status", "Pending activation");
            }
        } else {
            result.put("responseCode", "401");
            result.put("status", "Invalid login credentials");
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends login----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput recomputeProjectByStoreVisit(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts recomputeProjectByStoreVisit----------------\n");
        processImageService.recomputeProjectByStoreVisit(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());
        reportInput.put("granularity", inputObject.getGranularity());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends recomputeProjectByStoreVisit----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput reprocessProjectByStore(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts reprocessProjectByStore----------------\n");
        processImageService.reprocessProjectByStore(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends reprocessProjectByStore----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput updateProjectResultStatus(String input) {
        LOGGER.info("---------------RestAction Starts updateProjectResultStatus----------------\n");
        if (input != null && !input.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            String jsonString = object.get("updateList").toString();
            Gson gson = new Gson();
            StoreVisitResult[] storeUpdateArray = gson.fromJson(jsonString, StoreVisitResult[].class);

            processImageService.updateProjectResultStatus(storeUpdateArray);

        }

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends updateProjectResultStatusByStore----------------\n");
        return reportIO;
    }

    public void bulkUploadProjectImages(int projectId, String sync, String filenamePath) {
        LOGGER.info("---------------RestAction Starts bulkUploadProjectImages----------------\n");
        processImageService.bulkUploadProjectImage(projectId, sync, filenamePath);
        LOGGER.info("---------------RestAction Ends bulkUploadProjectImages -- Upload will run in background----------------\n");
    }

    public Snap2BuyOutput getRepPerformance(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getRepPerformance----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = reportingService.getRepPerformance(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", inputObject.getCustomerCode());

        reportInput.put("repsCount", inputObject.getRepsCount());
        reportInput.put("totalProjects", inputObject.getTotalProjects());
        reportInput.put("storeVisits", inputObject.getStoreVisits());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getRepPerformance----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getRepPerformanceByProject(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getRepPerformanceByProject----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = reportingService.getRepPerformanceByProject(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", inputObject.getCustomerCode());
        reportInput.put("agentId", inputObject.getAgentId());

        reportInput.put("totalProjects", inputObject.getTotalProjects());
        reportInput.put("storeVisits", inputObject.getStoreVisits());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getRepPerformanceByProject----------------\n");

        return reportIO;
    }

    public Snap2BuyOutput getRepPerformanceByProjectStore(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getRepPerformanceByProjectStore----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = reportingService.getRepPerformanceByProjectStore(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("agentId", inputObject.getAgentId());
        reportInput.put("storeVisits", inputObject.getStoreVisits());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getRepPerformanceByProjectStore----------------\n");

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

	public File getProjectAllStoreImageResultsCsv(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectAllStoreImageResultsCsv----------------\n");
        
        String tempFilePath = "/tmp/csvDownloadAllStoreImageResults" + System.currentTimeMillis();
        
        File projectAllStoreResultsCsvDetailCsv = processImageService.getProjectAllStoreImageResultsCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getProjectAllStoreImageResultsCsv----------------\n");

        return projectAllStoreResultsCsvDetailCsv;
	}

	public Snap2BuyOutput computeImageResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts computeImageResults----------------\n");
        processImageService.computeImageResults(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends computeImageResults----------------\n");

        return reportIO;
	}

	public Snap2BuyOutput uploadPremiumResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts uploadPremiumResults----------------\n");
        List<LinkedHashMap<String,String>> resultListToPass=new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> result= prmBulkUploaderService.submitPremiumResults(inputObject);
        resultListToPass.add(result);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("assessmentId", inputObject.getAssessmentId());
        reportInput.put("send", inputObject.getSendToDestination());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends uploadPremiumResults----------------\n");

        return reportIO;
	}

	public String getProjectAllStoreImageResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectAllStoreImageResults----------------\n");
        List<LinkedHashMap<String, Object>> resultListToPass = processImageService.getProjectAllStoreImageResults(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("status", inputObject.getStatus());

        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getProjectAllStoreImageResults----------------\n");

        return output;
	}

	public Snap2BuyOutput updateProjectImageResultStatus(String input) {
		LOGGER.info("---------------RestAction Starts updateProjectImageResultStatus----------------\n");
        if (input != null && !input.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(input).getAsJsonObject();
            String jsonString = object.get("updateList").toString();
            Gson gson = new Gson();
            ImageStore[] imageResultUpdateArray = gson.fromJson(jsonString, ImageStore[].class);

            processImageService.updateProjectImageResultStatus(imageResultUpdateArray);

        }

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends updateProjectImageResultStatus----------------\n");
        return reportIO;
	}
	
	public Snap2BuyOutput loadNewQuestionPremium(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts loadNewQuestionPremium----------------\n");
        List<LinkedHashMap<String,String>> resultListToPass=new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> result= prmBulkUploaderService.loadNewQuestionPremium(inputObject);
        resultListToPass.add(result);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("assessmentId", inputObject.getAssessmentId());
        reportInput.put("questionId", inputObject.getQuestionId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends loadNewQuestionPremium----------------\n");

        return reportIO;
    }

	public Snap2BuyOutput changeProjectImageStatus(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts changeProjectImageStatus----------------\n");
        
		processImageService.changeProjectImageStatus(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends changeProjectImageStatus----------------\n");

        return reportIO;
	}

	public String uploadSurveyImageResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts uploadSurveyImageResults----------------\n");
        
		String uploadedData = srvBulkUploaderService.uploadSurveyImageResults(inputObject);

        LOGGER.info("---------------RestAction Ends uploadSurveyImageResults----------------\n");

        return uploadedData;
	}

	public String uploadSurveyStoreVisitResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts uploadSurveyStoreVisitResults----------------\n");
        
		String uploadedData = srvBulkUploaderService.uploadSurveyStoreVisitResults(inputObject);

        LOGGER.info("---------------RestAction Ends uploadSurveyStoreVisitResults----------------\n");

        return uploadedData;
	}

	public Snap2BuyOutput loadSurveyData(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts loadSurveyData----------------\n");
        
		List<LinkedHashMap<String,String>> resultListToPass=new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> result= srvBulkUploaderService.loadSurveyData(inputObject);
        resultListToPass.add(result);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", "" + inputObject.getProjectId());
        reportInput.put("exernalProjectId", inputObject.getExternalProjectId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
		
        LOGGER.info("---------------RestAction Ends loadSurveyData----------------\n");

        return reportIO;
	}

	public File getProjectStoresWithDuplicateImagesCsv(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectStoresWithDuplicateImagesCsv----------------\n");
        
        String tempFilePath = "/tmp/StoresWithDuplicateImages" + System.currentTimeMillis();
        
        File storesWithDuplicateImages = processImageService.getProjectStoresWithDuplicateImagesCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getProjectStoresWithDuplicateImagesCsv----------------\n");

        return storesWithDuplicateImages;
	}

	public Snap2BuyOutput getProjectAllStoreScores(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectAllStoreScores----------------\n");
        
        List<LinkedHashMap<String, String>> resultListToPass = scoreService.getProjectAllStoreScores(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectAllStoreScores----------------\n");

        return reportIO;
	}

	public String getProjectStoreScores(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectStoreScores----------------\n");
        
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<StoreVisitScoreWithImageInfo> resultListToPass = new ArrayList<StoreVisitScoreWithImageInfo>();
        resultListToPass = scoreService.getProjectStoreScores(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
        LOGGER.info("---------------RestAction Ends getProjectStoreScores----------------\n");
        
        return output;
	}

    public String getProjectBrandSharesAllStores (InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesAllStores----------------\n");
		
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("rollup", inputObject.getRollup());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
		
		List<LinkedHashMap<String, Object>> shareResult = shelfAnalysisService.getProjectBrandSharesAllStores(inputObject);
       
		CustomSnap2BuyOutput reportIO = null;

		if (shareResult.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(shareResult, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

		
        LOGGER.info("---------------RestAction Ends getProjectBrandSharesAllStores----------------\n");
		return output;

    }
    
    public String getProjectBrandSharesAllStates (InputObject inputObject ) {
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesAllStates----------------\n");
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("rollup", inputObject.getRollup());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
		
		List<LinkedHashMap<String, Object>> shareResult = shelfAnalysisService.getProjectBrandSharesAllStates(inputObject);
       
		CustomSnap2BuyOutput reportIO = null;

		if (shareResult.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(shareResult, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
		
        LOGGER.info("---------------RestAction Ends getProjectBrandSharesAllStates----------------\n");
		return output;

    }
    
    public String getProjectBrandSharesAllCities (InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesAllCities----------------\n");
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("rollup", inputObject.getRollup());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
		
		List<LinkedHashMap<String, Object>> shareResult = shelfAnalysisService.getProjectBrandSharesAllCities(inputObject);
       
		CustomSnap2BuyOutput reportIO = null;

		if (shareResult.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(shareResult, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectBrandSharesAllCities----------------\n");
		return output;

    }


    public String getProjectBrandSummary(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectBrandSummary----------------\n");

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getProjectBrandSummary(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectBrandSummary----------------\n");

        return output;
    }
    
    public String getProjectBrandSummaryNew(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectBrandSummaryNew----------------\n");

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getProjectBrandSummaryNew(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectBrandSummaryNew----------------\n");

        return output;
    }

	public String getProjectDistributionSummary(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectDistributionSummary----------------\n");
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("waveId", inputObject.getProjectId()+"");

        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
		
		List<LinkedHashMap<String, Object>> shareResult = shelfAnalysisService.getProjectDistributionSummary(inputObject);
       
		CustomSnap2BuyOutput reportIO = null;

		if (shareResult.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(shareResult, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectDistributionSummary----------------\n");
		return output;
	}

	public String getProjectStoreDistribution(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectStoreDistribution----------------\n");
        List<ProjectStoreResultWithUPC> storeList = processImageService.getProjectStoreDistribution(inputObject);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId",inputObject.getProjectId()+"");
        reportInput.put("storeId",inputObject.getStoreId());
        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);
        
        CustomSnap2BuyOutput reportIO = null;
        if ( storeList == null || storeList.isEmpty() ) {
        		Map<String, String> emptyOutput = new HashMap<String, String>();
        		emptyOutput.put("Message", "No Data Returned");
        		List<Map<String,String>> emptyOutputList = new ArrayList<>();
        		emptyOutputList.add(emptyOutput);
        		reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
        		reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
        LOGGER.info("---------------RestAction Ends getProjectStoreDistribution----------------\n");

        return output;
	}

	public File getStoreLevelDistributionReport(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getStoreLevelDistributionReport----------------\n");
        
        String tempFilePath = "/tmp/ProjectStoreLevelDistribution" + System.currentTimeMillis();
        
        File projectStoreDistributionFile = processImageService.getStoreLevelDistributionReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getStoreLevelDistributionReport----------------\n");

        return projectStoreDistributionFile;
	}

	public String getProjectBrandShares(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectBrandShares----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("rollup", inputObject.getRollup());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getProjectBrandShares(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
		LOGGER.info("---------------RestAction Starts getProjectBrandShares----------------\n");

        return output;
	}
	
	public String getProjectBrandSharesNew(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesNew----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("waveId", inputObject.getWaveId());
        reportInput.put("rollup", inputObject.getRollup());
        reportInput.put("retailer", inputObject.getRetailer());
        reportInput.put("modular", inputObject.getModular());
        reportInput.put("subCategory", inputObject.getSubCategory());
        reportInput.put("storeFormat", inputObject.getStoreFormat());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getProjectBrandSharesNew(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesNew----------------\n");

        return output;
	}
	
	public File getProjectBrandProductsReport(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectBrandProductsReport----------------\n");
        
        String tempFilePath = "/tmp/ProjectBrandProducts" + System.currentTimeMillis()+".xlsx";
        
        File projectBrandProducts = processImageService.getProjectBrandProductsReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getProjectBrandProductsReport----------------\n");

        return projectBrandProducts;
	}
	
	public String getListForShelfAnalysis() {
		return processImageService.computeShelfLevel(null);
	}
    
	public List<LinkedHashMap<String, String>> getProjectPhotosByCustomerCodeAndStatus(String customerCode, String status){
		LOGGER.info("---------------RestAction Starts getProjectBrandSharesAllStates----------------\n");
		Date startTime = new Date();
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();

		List<LinkedHashMap<String, String>> projectResult = projectService.getProjectByCustomerAndStatus(customerCode, status);
		
		String tableData = "";
		
		if(projectResult.isEmpty()) {
			tableData += "<p>No Projects Photos available at the moment</p>"
					+ "<p><strong> Customer Code: </strong>"+customerCode+" and <strong> Status: </strong>"+status+"</p>"
					+ "<p>Excution Start at: " +startTime+ " and Ends at: " + new Date() + "</p>";
		} else {
			InputObject inputObject;
			tableData += "<table> "
					+ "<tr> "
					+ "<th>PROJECT_ID</th>"
					+ "<th>CUSTOMER_PROJECT_ID</th>"
					+ "<th>PRM_RESPONSE_WITH_PHOTO_LINK</th>"
					+ "<th>PRM_RESPONSE_NEW_IMAGE</th> "
					+ "</tr>";
			
			for (LinkedHashMap<String, String> project : projectResult) {
	            inputObject = new InputObject();
	            inputObject.setCustomerCode(customerCode);
	            inputObject.setProjectId(Integer.parseInt(project.get("projectId")));
	            inputObject.setCustomerProjectId(project.get("customerProjectId"));   
	            inputObject.setAssessmentId(project.get("customerProjectId"));
	            inputObject.setImageStatus("cron");
				
	            output = getPremiumProjectData(inputObject);
	            tableData += "<tr>"
	            		+ "<td>" + project.get("projectId") + "</td> "
	            		+ "<td>" + output.get("CUSTOMER_PROJECT_ID") + "</td> "
	    				+ "<td>" + output.get("PRM_RESPONSE_WITH_PHOTO_LINK") + "</td> "
						+ "<td>" + output.get("PRM_RESPONSE_NEW_IMAGE") + "</td> "
	            		+ "</tr>";
			}

			tableData += "</table>";
			tableData = "<h3>Premium data stats " + env.getProperty("instance") + "!</h3>"
					+ "<p><strong> Customer Code: </strong>"+customerCode+" and <strong> Status: </strong>"+status+"</p>"
					+ "<h4>Execution Starts at: " +startTime+ " and Ends at: " + new Date() + "</h4>"
							+ tableData;
		}
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			mimeMessage.setContent(tableData, "text/html");
			
			Address fromAddress = new InternetAddress("noreply@snap2insight.com");
			mimeMessage.setFrom(fromAddress);
			
			Address toAddress = new InternetAddress("renish.pynadath@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.TO, toAddress);
			
			Address ccAddress1 = new InternetAddress("anoop.ramankandath@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress1);
			
			Address ccAddress2 = new InternetAddress("kathirmma.davis@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress2);
			
			Address replyAddress = new InternetAddress("noreply@snap2insight.com");
			Address addresses[] = {replyAddress};
			mimeMessage.setReplyTo(addresses);
			mimeMessage.setSubject("Premium data load stats " + new Date());
			
			mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
			mimeMessage.setSentDate(new Date());
			
			mailSender.send(mimeMessage);
		} catch (MailException e) {
            LOGGER.error("CRON_EXCEPTION {} , {}", e.getMessage(), e);
		} catch (MessagingException e) {
            LOGGER.error("CRON_EXCEPTION {} , {}", e.getMessage(), e);
		}
        LOGGER.info("---------------RestAction Ends getProjectBrandSharesAllStates----------------\n");
		return projectResult;
	}
	
	public LinkedHashMap<String, String> getPremiumProjectData(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getPremiumProjectData----------------\n");
		LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
        output.put("CUSTOMER_PROJECT_ID", inputObject.getCustomerProjectId());

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        try {
            result = prmBulkUploaderService.loadPremiumData(inputObject);
            output.put("PRM_RESPONSE_WITH_PHOTO_LINK",result.get("prmResponseWithPhotoLink"));
            output.put("PRM_RESPONSE_NEW_IMAGE",result.get("prmResponsesNewImage"));
        } catch (Exception e) {
            LOGGER.error("exception", e);
            output.put("PRM_RESPONSE_WITH_PHOTO_LINK","FAILED");
            output.put("PRM_RESPONSE_NEW_IMAGE","FAILED");
        }

    	LOGGER.info("****************** CUSTOMER_PROJECT_ID: {}", inputObject.getCustomerProjectId());
    	LOGGER.info("****************** PRM_RESPONSE_WITH_PHOTO_LINK: {}", result.get("prmResponseWithPhotoLink") );
    	LOGGER.info("****************** PRM_RESPONSE_NEW_IMAGE: {}", result.get("prmResponsesNewImage"));

        LOGGER.info("---------------RestAction Ends getPremiumProjectData----------------\n");

        return output;

    }
	
	/**
	 * Method to get Customer's Projects summary by CustmerCode
	 * @param inputObject
	 * @return Summary of all projects for the give customerCode and status
	 */
	public Snap2BuyOutput getCustomerProjectSummary(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectSummary----------------\n");        
        List<LinkedHashMap<String, String>> projects = projectService.getProjectByCustomerAndStatus(inputObject.getCustomerCode(), inputObject.getStatus());
        
        List<String> projectIdList = new ArrayList<String>();
        for ( LinkedHashMap<String, String> project : projects ) {
        	if ( project.get("isParentProject").equals("0") ) {
            	projectIdList.add(project.get("projectId"));
        	}
        }
        
        Map<String, List<LinkedHashMap<String,String>>> output = processImageService.getImagesByProjectIdList(projectIdList);
        
        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> projectEntry;
        Integer totalImages;
        Integer notProcessedImages;
        Integer processingImages;
        Integer errorImages;
        Integer imageToBeReviewed;
        Integer imageToBeUploaded;
        Integer imagesResultCode1;
        Integer imagesResultCode2;
        for (LinkedHashMap<String, String> project : projects) {
        	totalImages = 0;
            notProcessedImages = 0;
            processingImages = 0;
            errorImages = 0;
            imageToBeReviewed = 0;
            imageToBeUploaded = 0;
            imagesResultCode1 = 0;
            imagesResultCode2 = 0;
            
        	projectEntry = new LinkedHashMap<String, String>();
        	if (output.get(project.get("projectId")) == null) {
        		continue;
        	}
        	for(LinkedHashMap<String, String> customerProjectById : output.get(project.get("projectId"))){
        		
            	if(null != customerProjectById.get("imageStatus") && customerProjectById.get("imageStatus").startsWith("cron")){
            		notProcessedImages += 1;
            	}else if(null != customerProjectById.get("imageStatus") && customerProjectById.get("imageStatus").startsWith("processing")){
            		processingImages += 1;
            	}else if(null != customerProjectById.get("imageStatus") && customerProjectById.get("imageStatus").equalsIgnoreCase("error")){
            		errorImages += 1;
            	}else if(null != customerProjectById.get("imageStatus") && customerProjectById.get("imageStatus").equalsIgnoreCase("done")){
            		if(null != customerProjectById.get("imageReviewStatus") && customerProjectById.get("imageReviewStatus").equalsIgnoreCase("0")){
            			imageToBeReviewed += 1;
            		}
            	}
            	
            	if (null != customerProjectById.get("resultUploaded") && customerProjectById.get("resultUploaded").equalsIgnoreCase("0") &&
            			customerProjectById.get("imageStatus").equalsIgnoreCase("done")) {
            		imageToBeUploaded += 1;
                }

            	if (null != customerProjectById.get("imageResultCode") && customerProjectById.get("imageResultCode").equalsIgnoreCase("1")) {
            		imagesResultCode1 += 1;
                }
            	if (null != customerProjectById.get("imageResultCode") && customerProjectById.get("imageResultCode").equalsIgnoreCase("2")) {
            		imagesResultCode2 += 1;
                }

            	totalImages++;
            }
        	
        	projectEntry.put("projectId", project.get("projectId"));
        	projectEntry.put("customerProjectId", project.get("customerProjectId"));
        	projectEntry.put("categoryId", project.get("categoryId"));
        	projectEntry.put("projectName", project.get("projectName"));
        	projectEntry.put("totalImages", totalImages+"");
        	projectEntry.put("notProcessedImages", notProcessedImages+"");
        	projectEntry.put("processingImages", processingImages+"");
        	projectEntry.put("errorImages", errorImages+"");
        	projectEntry.put("imageToBeReviewed", imageToBeReviewed+"");
        	projectEntry.put("imageToBeUploaded", imageToBeUploaded+"");
        	projectEntry.put("imagesResultCode1", imagesResultCode1+"");
        	projectEntry.put("imagesResultCode2", imagesResultCode2+"");

        	result.add(projectEntry);
		}
        
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", inputObject.getCustomerCode());
        reportInput.put("status", inputObject.getStatus());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(result, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectSummary----------------\n");

        return reportIO;
    }

	/**
	 * Method to get StoreDetails by customerProjectId
	 * @param inputObject
	 * @return List of store details
	 */
	public Snap2BuyOutput getStoreDetailsByCustomerProjectId(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStoreDetailsByCustomerProjectId----------------\n");        
        
        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        
        JSONObject statesJSON = new JSONObject("{ 'AK':'Alaska', 'AL':'Alabama', 'AR':'Arkansas', 'AZ':'Arizona', 'CA':'California', 'CO':'Colorado', 'CT':'Connecticut', 'DC':'Dist of Columbia', 'DE':'Delaware', 'DS':'Demo', 'FL':'Florida', 'GA':'Georgia', 'HI':'Hawaii', 'IA':'Iowa', 'ID':'Idaho', 'IL':'Illinois', 'IN':'Indiana', 'KS':'Kansas', 'KY':'Kentucky', 'LA':'Louisiana', 'MA':'Massachusetts', 'MD':'Maryland', 'ME':'Maine', 'MI':'Michigan', 'MN':'Minnesota', 'MO':'Missouri', 'MS':'Mississippi', 'MT':'Montana', 'NC':'North Carolina', 'ND':'North Dakota', 'NE':'Nebraska', 'NH':'New Hampshire', 'NJ':'New Jersey', 'NM':'New Mexico', 'NV':'Nevada', 'NY':'New York', 'OH':'Ohio', 'OK':'Oklahoma', 'OR':'Oregon', 'PA':'Pennsylvania', 'PR':'Puerto Rico', 'RI':'Rhode Island', 'SC':'South Carolina', 'SD':'South Dakota', 'TN':'Tennessee', 'TX':'Texas', 'UT':'Utah', 'VA':'Virginia', 'VT':'Vermont', 'WA':'Washington', 'WI':'Wisconsin', 'WV':'West Virginia', 'WY':'Wyoming', 'AS':'American Samoa', 'FM':'Federated States of Micronesia', 'GU':'Guam', 'MH':'Marshall Islands', 'MP':'Northern Mariana Islands', 'PW': 'Palau', 'VI':'Virgin Islands' }");
        String token = null;
        String retailerCode = "";
        
        List<PrmResponse> prmResponses = new ArrayList<PrmResponse>();
		try {
			
			List<LinkedHashMap<String, String>>  projects = projectService.getProjectsByCustomerCodeAndCustomerProjectId(inputObject.getProjectId());
			result.addAll(projects);
			if(!projects.isEmpty()){
				retailerCode = projects.get(0).get("retailerCode");
				
				if(!StringUtils.isBlank(retailerCode)){
					String retailerName = retailerCode;
					inputObject.setRetailerCode(retailerCode);
					List<LinkedHashMap<String, String>> retailers = metaService.getRetailerDetail(inputObject);
					if(!retailers.isEmpty()){
						retailerName = retailers.get(0).get("name");
					}
					
					token = prmBulkUploaderService.getToken();
					prmResponses = prmBulkUploaderService.getPrmResponse(token, inputObject.getCustomerProjectId());

                    updateStoreMaster(inputObject, statesJSON, retailerCode, retailerName, prmResponses);
				}
			}
	        LOGGER.info("=============================validPrmResponses.size()= {}", prmResponses.size());
		} catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		}
        		
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("assessmentId", inputObject.getCustomerProjectId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(result, reportInput);
        LOGGER.info("---------------RestAction Ends getStoreDetailsByCustomerProjectId----------------\n");

        return reportIO;
    }

	/**
	 * add/update Store Details
	 * @param customerCode
	 * @param status
	 */
	public void saveStoreDetails(String customerCode, String status){
		LOGGER.info("---------------RestAction Starts saveStoreDetails customerCode {} and Status {}",customerCode,status);
		Date startTime = new Date();
		InputObject inputObject = new InputObject();
        inputObject.setCustomerCode(customerCode);
        inputObject.setStatus(status);
        
        JSONObject statesJSON = new JSONObject("{ 'AK':'Alaska', 'AL':'Alabama', 'AR':'Arkansas', 'AZ':'Arizona', 'CA':'California', 'CO':'Colorado', 'CT':'Connecticut', 'DC':'Dist of Columbia', 'DE':'Delaware', 'DS':'Demo', 'FL':'Florida', 'GA':'Georgia', 'HI':'Hawaii', 'IA':'Iowa', 'ID':'Idaho', 'IL':'Illinois', 'IN':'Indiana', 'KS':'Kansas', 'KY':'Kentucky', 'LA':'Louisiana', 'MA':'Massachusetts', 'MD':'Maryland', 'ME':'Maine', 'MI':'Michigan', 'MN':'Minnesota', 'MO':'Missouri', 'MS':'Mississippi', 'MT':'Montana', 'NC':'North Carolina', 'ND':'North Dakota', 'NE':'Nebraska', 'NH':'New Hampshire', 'NJ':'New Jersey', 'NM':'New Mexico', 'NV':'Nevada', 'NY':'New York', 'OH':'Ohio', 'OK':'Oklahoma', 'OR':'Oregon', 'PA':'Pennsylvania', 'PR':'Puerto Rico', 'RI':'Rhode Island', 'SC':'South Carolina', 'SD':'South Dakota', 'TN':'Tennessee', 'TX':'Texas', 'UT':'Utah', 'VA':'Virginia', 'VT':'Vermont', 'WA':'Washington', 'WI':'Wisconsin', 'WV':'West Virginia', 'WY':'Wyoming', 'AS':'American Samoa', 'FM':'Federated States of Micronesia', 'GU':'Guam', 'MH':'Marshall Islands', 'MP':'Northern Mariana Islands', 'PW': 'Palau', 'VI':'Virgin Islands' }");
        String token = null;
        String retailerCode = "";
        String retailerName = "";

        List<PrmResponse> prmResponses = new ArrayList<PrmResponse>();
		try {
			List<LinkedHashMap<String, String>> projectResult = projectService.getProjectByCustomerAndStatus(customerCode, status);
			LOGGER.info("---------------RestAction saveStoreDetails projects size: {}",(projectResult.isEmpty() ? 0 : projectResult.size()));
			
			List<LinkedHashMap<String, String>> retailers = new ArrayList<LinkedHashMap<String, String>>();
			System.out.println("Project Size " + projectResult.size());
			token = prmBulkUploaderService.getToken();
			
			for (LinkedHashMap<String, String> project : projectResult) {				
				retailerCode = project.get("retailerCode");
				
				if(!StringUtils.isBlank(retailerCode)){
					retailerName = retailerCode;
					inputObject.setRetailerCode(retailerCode);
					inputObject.setCustomerProjectId(project.get("customerProjectId"));
					retailers = metaService.getRetailerDetail(inputObject);
					if(!retailers.isEmpty()){
						retailerName = retailers.get(0).get("name");
						LOGGER.info("---------------RestAction saveStoreDetails Retailer name: {}",retailerName);
					}
															
					prmResponses = prmBulkUploaderService.getPrmResponse(token, inputObject.getCustomerProjectId());
					
					LOGGER.info("---------------RestAction saveStoreDetails prmResponse size: {}",(prmResponses.isEmpty() ? 0 : prmResponses.size()));
                    updateStoreMaster(inputObject, statesJSON, retailerCode, retailerName, prmResponses);
                }
			}

			try {
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				mimeMessage.setContent("Execution starts at "+ startTime +" and Ends at " + new Date(), "text/html");

				Address fromAddress = new InternetAddress("noreply@snap2insight.com");
				mimeMessage.setFrom(fromAddress);

				Address address = new InternetAddress("anoop.ramankandath@snap2insight.com");
				mimeMessage.addRecipient(Message.RecipientType.TO, address);

				Address replyAddress = new InternetAddress("noreply@snap2insight.com");
				Address addresses[] = {replyAddress};
				mimeMessage.setReplyTo(addresses);
				mimeMessage.setSubject("Execution Time " + new Date());

				mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
				mimeMessage.setSentDate(new Date());

				mailSender.send(mimeMessage);
			} catch (MailException e) {
				System.out.println("EXCEPTION "+ e);
                LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			} catch (MessagingException e) {
				System.out.println("EXCEPTION "+ e);
                LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			}

		} catch (Exception e) {
			System.out.println("EXCEPTION "+ e);
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		} 
        LOGGER.info("---------------RestAction Ends saveStoreDetails----------------\n");
	}

    /**
     * Add or Update StoreMaster
     * @param inputObject
     * @param statesJSON
     * @param retailerCode
     * @param retailerName
     * @param prmResponses
     */
    private void updateStoreMaster(InputObject inputObject, JSONObject statesJSON, String retailerCode, String retailerName, List<PrmResponse> prmResponses) {
        String prmStoreId;
        List<StoreMaster> storesToCreate = new ArrayList<StoreMaster>();
        List<StoreMaster> storesToUpdate = new ArrayList<StoreMaster>();
        List<LinkedHashMap<String, String>> storeMasterData;
        StoreMaster storeMaster;
        Set<String> processedStoreNumbers = new HashSet<String>();
        for (PrmResponse prmResponse : prmResponses) {
            prmStoreId = prmResponse.getStoreId();
            //storeNumber = StringUtils.stripStart(storeNumber, "0");
            
            if ( !processedStoreNumbers.contains(prmStoreId) ) {
            	storeMasterData = metaService.getStoreMasterByRetailerChainCodeAndRetailsStoreId(prmStoreId, retailerCode);

                if(storeMasterData.isEmpty()){
                	String storeId = generateStoreId();
                    storeMaster = new StoreMaster(storeId,
                    		prmStoreId,
                            retailerCode,
                            retailerName,
                            prmResponse.getStoreAddress(),
                            prmResponse.getStoreCity(),
                            prmResponse.getStoreState(),
                            statesJSON.getString(prmResponse.getStoreState().toUpperCase()),
                            prmResponse.getStoreZip(),
                            null,
                            null,
                            "PREMIUM_LOAD");
                    storesToCreate.add(storeMaster);
                } else if ( StringUtils.isBlank(storeMasterData.get(0).get("city")) ) {
                    storeMaster = new StoreMaster(storeMasterData.get(0).get("storeId"),
                    		prmStoreId,
                            retailerCode,
                            retailerName,
                            prmResponse.getStoreAddress(),
                            prmResponse.getStoreCity(),
                            prmResponse.getStoreState(),
                            statesJSON.getString(prmResponse.getStoreState().toUpperCase()),
                            prmResponse.getStoreZip(),
                            storeMasterData.get(0).get("latitude"),
                            storeMasterData.get(0).get("longitude"),
                            storeMasterData.get(0).get("comments"));
                    storesToUpdate.add(storeMaster);
                }
                processedStoreNumbers.add(prmStoreId);
            }
            
        }
		LOGGER.info("---------------RestAction updateStoreMaster :: Stores to create = {}",storesToCreate);
		storeMasterDao.createStores(storesToCreate);
		
		LOGGER.info("---------------RestAction updateStoreMaster :: Stores to update = {}", storesToUpdate);
		storeMasterDao.updateStores(storesToUpdate);
		
		LOGGER.info("---------------RestAction ends updateStoreMaster----------------\n");
    }

    
    public void sendEnquiryAckEmail(String emailBody, String subject, List<String> toList, List<String> ccList, List<String> bccList) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            mimeMessage.setContent(emailBody, "text/html");

            Address fromAddress = new InternetAddress("noreply@snap2insight.com");
            mimeMessage.setFrom(fromAddress);
            
            for(String toAddress : toList ) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            }

            if(null != ccList) {
            	for(String ccAddress : ccList ) {
            		mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(ccAddress));
            	}
            }

            if(null != bccList) {
            	for(String bccAddress : bccList ) {
            		mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccAddress));
            	}
            }
            
            Address replyAddress = new InternetAddress("sales@snap2insight.com");
            Address addresses[] = {replyAddress};
            mimeMessage.setReplyTo(addresses);
            mimeMessage.setSubject(subject);

            mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
            mimeMessage.setSentDate(new Date());

            mailSender.send(mimeMessage);
        } catch (MailException e) {
            LOGGER.error("EMAIL_EXCEPTION {} , {}", e.getMessage(), e);
        } catch (MessagingException e) {
            LOGGER.error("EMAIL_EXCEPTION {} , {}", e.getMessage(), e);
        }
    }
    
    public void sendEmail(String emailBody, String subject, String to, String cc) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            mimeMessage.setContent(emailBody, "text/html");

            Address fromAddress = new InternetAddress("noreply@snap2insight.com");
            mimeMessage.setFrom(fromAddress);

            Address toAddress = new InternetAddress(to);
            mimeMessage.addRecipient(Message.RecipientType.TO, toAddress);

            if(null != cc) {
                Address ccAddress = new InternetAddress(cc);
                mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress);
            }

            Address replyAddress = new InternetAddress("noreply@snap2insight.com");
            Address addresses[] = {replyAddress};
            mimeMessage.setReplyTo(addresses);
            mimeMessage.setSubject(subject);

            mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
            mimeMessage.setSentDate(new Date());

            mailSender.send(mimeMessage);
        } catch (MailException e) {
            LOGGER.error("EMAIL_EXCEPTION {} , {}", e.getMessage(), e);
        } catch (MessagingException e) {
            LOGGER.error("EMAIL_EXCEPTION {} , {}", e.getMessage(), e);
        }
    }

    public Snap2BuyOutput updateUPCForImageAnalysis(String newUpc,String id, String imageUUID, String leftTopX, String leftTopY, String shelfLevels,
    		String price, String promotion, String compliant) {
        LOGGER.info("---------------RestAction Starts updateUPCForImageAnalysis----------------\n");

        processImageService.updateUPCForImageAnalysis(newUpc, id, imageUUID, leftTopX, leftTopY, shelfLevels, price, promotion, compliant);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends updateUPCForImageAnalysis----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput addImageAnalysisNew(String newUpc, String imageUUID, String leftTopX, String leftTopY,
                                              String shelfLevel, int projectId, String storeId,
                                              String height, String width, String price, String promotion, String compliant) {
        LOGGER.info("---------------RestAction Starts addImageAnalysisNew----------------\n");

        processImageService.addImageAnalysisNew(newUpc, imageUUID, leftTopX, leftTopY, shelfLevel, projectId, storeId, height, width, price, promotion, compliant);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends addImageAnalysisNew----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput activateUser(User userInput) {
        LOGGER.info("---------------RestAction Starts activateUser----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getUserId());
        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getUserId().trim());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        if (null == userList || userList.isEmpty()) {
            LOGGER.error("No existing record found for userId : {}", userInput.getUserId());

            result.put("responseCode", "204");
            result.put("responseMessage", "User not found. Request Rejected.");
        } else {
        	if ( userList.get(0).get("status").equals(Constants.USER_STATUS_INACTIVE) ) {
                LOGGER.error("Activation request for a deactivated user: {}", userInput.getUserId());
                LOGGER.error("Activation cannot be done. Request Rejected.");
                result.put("responseCode", "204");
                result.put("responseMessage", "Activation cannot be done. Request Rejected.");
        	} else {
        		if(userList.get(0).get("authToken").substring(0, 6).trim().equalsIgnoreCase(userInput.getAuthToken().trim())){
                    userInput.setStatus(Constants.USER_STATUS_ACTIVE);
                    authService.updateUserStatusAndCustomerCode(userInput);
                    LOGGER.info("User Id {} activated.", userInput.getUserId());
                    result.put("responseCode", "200");
                    result.put("status", "success");
                    result.put("customerCode", userInput.getCustomerCode());
                    result.put("firstName", userList.get(0).get("firstName"));
                    result.put("lastName", userList.get(0).get("lastName"));
                    result.put("role",userList.get(0).get("role"));
                    result.put("logo", "");
                    result.put("supportEmail", Constants.S2I_SUPPORT_EMAIL);
    	            result.put("supportPhoneNumber", Constants.S2I_SUPPORT_PHONE_NUMBER);
                    result.put("authToken", userList.get(0).get("authToken"));
                    result.put("responseMessage", "User activated");
                } else {
                    result.put("responseCode", "204");
                    result.put("responseMessage", "Invalid activation code");
                }
        	}
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends activateUser----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput resendActivationCode(User userInput) {
        LOGGER.info("---------------RestAction Starts resendActivationCode----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getUserId());

        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getUserId());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        setAuthTokenAndDate(userInput);
//        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
//        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//
//        Date currentDate = new Date();
//
//        UUID newAuthToken = UUID.randomUUID();
//        String newAuthTokenIssueDate = inSdf.format(currentDate);
//        userInput.setAuthToken(newAuthToken.toString().trim());
//        userInput.setAuthTokenIssueDate(newAuthTokenIssueDate);

        if (null == userList || userList.isEmpty()) {
            LOGGER.info("No existing record found for userId : {}", userInput.getUserId());

            result.put("responseCode", "204");
            result.put("responseMessage", "User not found.");
        } else {
            result.put("responseCode", "200");
            result.put("responseMessage", "Activation code send Successfully");

            authService.updateAuthToken(userInput);

            String emailBody  = "<p>Dear " + userList.get(0).get("firstName") + ",</p>"
                    + "<p>Thanks for signing up with Snap2Insight, please use below details to complete your sign up process.</p>"
                    + "<p><strong>Activation code: "+ userInput.getAuthToken().substring(0, 6).trim() +"</strong></p>"
                    + "<p><strong>Customer code</strong>: Please check with your company representative</p>"
                    + "<p>For any assistance, reach out to support@snap2insight.com</p>"
                    + "<p>Thanks,<br>"
                    + "Snap2Insight team</p>";
            sendEmail(emailBody, "Your Snap2Insight account has been created\n", userInput.getUserId(), null);
            LOGGER.info("ActivationCode for userId : {}", userInput.getUserId());
        }

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());
        reportInput.put("firstName", userInput.getFirstName());
        reportInput.put("lastName", userInput.getLastName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends resendActivationCode----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput changeEmail(User userInput) {
        LOGGER.info("---------------RestAction Starts changeEmail----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getUserId());

        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getUserId());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        setAuthTokenAndDate(userInput);
//        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
//        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//
//        Date currentDate = new Date();
//
//        UUID newAuthToken = UUID.randomUUID();
//        String newAuthTokenIssueDate = inSdf.format(currentDate);
//        userInput.setAuthToken(newAuthToken.toString().trim());
//        userInput.setAuthTokenIssueDate(newAuthTokenIssueDate);

        if (null == userList || userList.isEmpty()) {
            userList = authService.getUserDetail(userInput.getOldEmail());
            if(userList.get(0).get("status").equalsIgnoreCase("0")) {
                result.put("responseCode", "200");
                result.put("responseMessage", "Email address Updated. Activation code send Successfully on updated email");

                authService.updateUserEmail(userInput);

                userList = authService.getUserDetail(userInput.getUserId());

                String emailBody  = "<p>Dear " + userList.get(0).get("firstName") + ",</p>"
                        + "<p>Thanks for signing up with Snap2Insight, please use below details to complete your sign up process.</p>"
                        + "<p><strong>Activation code: "+ userList.get(0).get("authToken").substring(0, 6).trim() +"</strong></p>"
                        + "<p><strong>Customer code</strong>: Please check with your company representative</p>"
                        + "<p>For any assistance, reach out to support@snap2insight.com</p>"
                        + "<p>Thanks,<br>"
                        + "Snap2Insight team</p>";
                sendEmail(emailBody, "Your Snap2Insight account has been created\n", userInput.getUserId(), null);
                LOGGER.info("ActivationCode for email : {}", userInput.getUserId());
            } else {
                result.put("responseCode", "204");
                result.put("responseMessage", "User already activated so can't change email.");
            }
        } else {
            LOGGER.info("Existing record found for email : {}", userInput.getUserId());

            result.put("responseCode", "204");
            result.put("responseMessage", "User already exist with this email.");
        }

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("email", userInput.getEmail());
        reportInput.put("firstName", userInput.getFirstName());
        reportInput.put("lastName", userInput.getLastName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends changeEmail----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput saveProjectImage(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts saveProjectImage input: {}", inputObject);

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        processImageService.updateProjectImagePath(inputObject.getProjectId(), inputObject.getImageFilePath());

        result.put("responseCode", "200");
        result.put("responseMessage", "Project Image Uploaded Successfully.");

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("--------------- RestAction Ends saveProjectImage ----------------\n");
        return reportIO;
    }

    public Map<String, String> getProjectByCustomerCodeAndCustomerProjectId(int projectId) {
        LOGGER.info("---------------RestAction Starts saveProjectImage projectId: {}", projectId);

        Map<String, String> result = new HashMap<>();

        result = processImageService.getProjectByCustomerCodeAndCustomerProjectId(projectId);

        result.put("responseCode", "200");
        result.put("responseMessage", "Project Image Uploaded Successfully.");

        return result;
    }

    public Snap2BuyOutput resetPassword(User userInput) {
        LOGGER.info("---------------RestAction Starts resetPassword----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getUserId());
        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getUserId().trim());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        if (null == userList || userList.isEmpty()) {
            LOGGER.error("No existing record found for userId : {}", userInput.getUserId());

            result.put("responseCode", "204");
            result.put("responseMessage", "User not found. Request Rejected.");
        } else {
            if((!userList.get(0).get("status").equals(Constants.USER_STATUS_INACTIVE))
            		&& userList.get(0).get("authToken").substring(0, 6).trim().equalsIgnoreCase(userInput.getAuthToken().trim())){

                authService.updateUserPasswordWithExistingSHA(userInput);

                setAuthTokenAndDate(userInput);
//                SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
//                inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
//
//                Date currentDate = new Date();
//
//                UUID newAuthToken = UUID.randomUUID();
//                String newAuthTokenIssueDate = inSdf.format(currentDate);
//                userInput.setAuthToken(newAuthToken.toString().trim());
//                userInput.setAuthTokenIssueDate(newAuthTokenIssueDate);

                authService.updateAuthToken(userInput);

                result.put("responseCode", "200");
                result.put("status", "success");
                result.put("responseMessage", "User Updated Successfully");
            } else {
                result.put("responseCode", "204");
                result.put("responseMessage", "Link has been expired");
            }
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends resetPassword----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput updateStorePlaceId(String retailerChainCode) {
        LOGGER.info("---------------RestAction Starts updateStorePlaceId retailer: {}", retailerChainCode);

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        List<LinkedHashMap<String, String>> storeMasters = metaService.getStoreMasterByRetailerChainCode(retailerChainCode);
        Map<String, String> response;
        for(LinkedHashMap<String, String> store: storeMasters){
            if(null != store.get("city") && StringUtils.isBlank(store.get("placeId"))) {
                response = metaService.getGooglePlaceIdByAddress(store.get("street") + ", " + store.get("city") + "," + store.get("state"));
                if(null != response && !response.isEmpty() && null != response.get("placeId") &&
                        response.get("placeId").substring(0,2).toLowerCase().equalsIgnoreCase("ch")){
                    String storeId = store.get("storeId");
                    String placeId = response.get("placeId");
                    String lat = ConverterUtil.ifNullToEmpty(response.get("lat")).trim();
                    String lng = ConverterUtil.ifNullToEmpty(response.get("lng")).trim();
                    String postal_code = ConverterUtil.ifNullToEmpty(response.get("postal_code")).trim();
                    result.put("StoreId: "+storeId, placeId+", "+lat+", "+lng+", "+postal_code);
                    metaService.updateStorePlaceIdAndLatLngAndPostCode(storeId.trim(), placeId.trim(),lat, lng, postal_code);
                } else {
                    result.put("StoreId: "+store.get("storeId"), "PlaceId not found" + ", "+response.get("lat")+", "+response.get("lng"));
                }
            }
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("RetailerChainCode", retailerChainCode);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends updateStorePlaceId----------------\n");
        return reportIO;
    }

    public Snap2BuyOutput forgotPasswordForDevice(String userId) {
        LOGGER.info("---------------RestAction Starts forgotPassword----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userId);

        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userId.trim());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        if (null == userList || userList.isEmpty()) {
            LOGGER.info("User not found");

            result.put("responseCode", "204");
            result.put("responseMessage", "User not found.");
        } else {
            result.put("responseCode", "200");
            result.put("responseMessage", "Thank you. We have shared steps to reset password on " + userId + ". Please check your email.");

            String encryptedUserId,
                    encryptedToken;

            try {

                KeySpec secretKey = new DESedeKeySpec(env.getProperty("encrypt_key_"+env.getProperty("instance")).getBytes("UTF8"));
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DESede");
                Cipher cipher = Cipher.getInstance("DESede");
                SecretKey key = secretKeyFactory.generateSecret(secretKey);

                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] encryptedText = cipher.doFinal(userId.getBytes("UTF8"));
                encryptedUserId = new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedText));

                encryptedText = cipher.doFinal(userList.get(0).get("authToken").substring(0, 6).trim().getBytes("UTF8"));
                encryptedToken = new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedText));

                encryptedUserId = URLEncoder.encode(encryptedUserId,"UTF-8");
                encryptedToken = URLEncoder.encode(encryptedToken, "UTF-8");

                String emailBody  = "<p>Dear " + userList.get(0).get("firstName") + ",</p>"
                        + "<p>We have received a request to reset your Snap2Insight account password.</p>"
                        + "<p><a href='" + env.getProperty("web_url_"+env.getProperty("instance")) + "/reset-password:" + encryptedUserId + ":" + encryptedToken + "' target='_blank'>Click here</a> to reset your password.</p>"
                        + "<p>For any assistance, reach out to support@snap2insight.com</p>"
                        + "<p>Thanks,<br>"
                        + "Snap2Insight team</p>";
                sendEmail(emailBody, "Snap2Insight - reset password instruction \n", userId, null);
                LOGGER.info("Forgot password link shared on: {}", userId);
            } catch (Exception e){
                LOGGER.info("EXCEPTION " + e);
            }
        }

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends forgotPassword----------------\n");
        return reportIO;
    }

    public String generateStoreId() {
        LOGGER.info("--------------- RestAction Starts generateStoreId ----------------\n");
        String storeId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        LOGGER.info("################################## StoreId: {}", storeId);
        List<LinkedHashMap<String, String>> result = metaService.getStoreMasterByStoreId(storeId);
        if(null != result && !result.isEmpty()){
            storeId = generateStoreId();
            LOGGER.info("Generating storeId again StoreId: {}" , storeId);
        }
        return storeId;
    }

    public Snap2BuyOutput getGeoMappedStoresByUserId(String customerCode, String userId) {
        LOGGER.info("---------------RestAction Starts getGeoMappedStoresByUserId:: customerCode={}, userId: {}", customerCode, userId);
        List<LinkedHashMap<String, String>> storesList = metaService.getGeoMappedStoresByUserId(customerCode, userId);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(storesList, reportInput);
        LOGGER.info("---------------RestAction Ends getTerritoryMappedStoresByUserId----------------\n");
        return reportIO;
    }
    
    public Snap2BuyOutput bulkUploadStores(String filenamePath) {
        LOGGER.info("---------------RestAction Starts bulkUploadStores----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LinkedHashMap<String, String> result = metaService.bulkUploadStores(filenamePath);
        
        resultListToPass.add(result);
        
        HashMap<String, String> reportInput = new HashMap<String, String>();

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends bulkUploadStores---------------\n");
        return reportIO;
    }

	public Snap2BuyOutput listChildProjects(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts listChildProjects:: project id = {}", inputObject.getProjectId());
        List<LinkedHashMap<String, String>> resultListToPass = metaService.listChildProjects(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends listChildProjects----------------\n");
        return reportIO;
    }

	public String getCategoryReviewComments(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getCategoryReviewComments:: category id = {}", inputObject.getCategoryId());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("categoryId", inputObject.getCategoryId());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = metaService.getCategoryReviewComments(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getCategoryReviewComments----------------\n");
        return output;
	}

	public String getAltriaProjectSummary(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectSummary:: "
				+ "project id = {},level = {}, value = {}",inputObject.getProjectId(),inputObject.getLevel(), inputObject.getValue());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectSummary(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectSummary----------------\n");
        return output;
	}
	
	public String getAltriaProjectBrandShares(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectBrandShares:: "
                + "project id = {},level = {}, value = {}",inputObject.getProjectId(),inputObject.getLevel(), inputObject.getValue());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectBrandShares(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectBrandShares----------------\n");
        return output;
	}

	public String getAltriaProjectBrandAvailability(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectBrandAvailability:: "
                + "project id = {},level = {}, value = {}",inputObject.getProjectId(),inputObject.getLevel(), inputObject.getValue());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectBrandAvailability(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectBrandAvailability----------------\n");
        return output;
	}

	public String getAltriaProjectWarningSignAvailability(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectWarningSignAvailability:: "
                + "project id = {},level = {}, value = {}",inputObject.getProjectId(),inputObject.getLevel(), inputObject.getValue());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectWarningSignAvailability(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectWarningSignAvailability----------------\n");
        return output;
	}

	public String getAltriaProjectBrandAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectBrandAvailabilityByStore:: "
                + "project id = {},level = {}, value = {}, storeId: {}",
                inputObject.getProjectId(),inputObject.getLevel(),
                inputObject.getValue(), inputObject.getStoreId());

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectBrandAvailabilityByStore(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectBrandAvailabilityByStore----------------\n");
        return output;
	}

	public String getAltriaProjectBrandSharesByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectBrandSharesByStore:: "
                + "project id = {},level = {}, value = {}, storeId: {}",
                inputObject.getProjectId(),inputObject.getLevel(),
                inputObject.getValue(), inputObject.getStoreId());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectBrandSharesByStore(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectBrandSharesByStore----------------\n");
        return output;
	}

	public String getAltriaProjectWarningSignAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectWarningSignAvailabilityByStore:: "
                + "project id = {},level = {}, value = {}, storeId: {}",
                inputObject.getProjectId(),inputObject.getLevel(),
                inputObject.getValue(), inputObject.getStoreId());

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectWarningSignAvailabilityByStore(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectWarningSignAvailabilityByStore----------------\n");
        return output;
	}

	public String getAltriaProjectProductAvailability(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectProductAvailability:: "
                + "project id = {},level = {}, value = {}",
                inputObject.getProjectId(),inputObject.getLevel(),
                inputObject.getValue());
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectProductAvailability(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectProductAvailability----------------\n");
        return output;
	}

	public String getAltriaProjectProductAvailabilityByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectProductAvailabilityByStore:: "
                + "project id = {},level = {}, value = {}, storeId: {}",
                inputObject.getProjectId(),inputObject.getLevel(),
                inputObject.getValue(), inputObject.getStoreId());

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = altriaDashboardService.getAltriaProjectProductAvailabilityByStore(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getAltriaProjectProductAvailabilityByStore----------------\n");
        return output;
	}

	public Snap2BuyOutput getAltriaProjectAllStoreResults(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectAllStoreResults----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = altriaDashboardService.getAltriaProjectAllStoreResults(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getAltriaProjectAllStoreResults----------------\n");

        return reportIO;
	}

	public Snap2BuyOutput getAltriaProjectStoreImagesByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaProjectStoreImagesByStore----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = altriaDashboardService.getAltriaProjectStoreImagesByStore(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getAltriaProjectStoreImagesByStore----------------\n");

        return reportIO;
	}

    public Snap2BuyOutput getAltriaStoresForReview(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getAltriaStoresForReview----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = altriaDashboardService.getAltriaStoresForReview(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("batchId", inputObject.getBatchId());
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getAltriaStoresForReview----------------\n");

        return reportIO;
    }

	public Snap2BuyOutput getMenusForUser(InputObject inputObject) {
	     LOGGER.info("---------------RestAction Starts getMenusForUser:: customerCode={}, userId: {}, role: {}, source:{}",
                 inputObject.getCustomerCode(),inputObject.getUserId(), inputObject.getRole(), inputObject.getSource());

	     List<LinkedHashMap<String, String>> menus = uiService.getMenusForUser(inputObject);

	     HashMap<String, String> reportInput = new HashMap<String, String>();
	     reportInput.put("userId", inputObject.getUserId());
	     reportInput.put("customerCode", inputObject.getCustomerCode());
	     reportInput.put("Source", inputObject.getSource());

	     Snap2BuyOutput reportIO = new Snap2BuyOutput(menus, reportInput);
	     
	     LOGGER.info("---------------RestAction Ends getMenusForUser----------------\n");
	     return reportIO;
	}

    public String getAltriaStoreImagesForReview(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getAltriaStoreImagesForReview----------------\n");
        List<LinkedHashMap<String, Object>> resultListToPass = altriaDashboardService.getAltriaStoreImagesForReview(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
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

        return output;
    }

    public String getProductDetectionsForReview(InputObject inputObject) {
        LOGGER.info("---------------- RestAction Starts getProductDetectionsForReview: input: {}", inputObject);

        List<LinkedHashMap<String, Object>> resultListToPass = altriaDashboardService.getProductDetectionsForReview(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
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

        LOGGER.info("---------------- RestAction Ends getProductDetectionsForReview -----------------");
        return output;
    }

    public String updateProductDetections(InputObject inputObject, List<SkuTypePOJO> skuTypePOJOs) {
        LOGGER.info("---------------- RestAction Starts getProductDetectionsForReview: input: {}"
        		+ ",detections={}",inputObject, skuTypePOJOs);

        altriaDashboardService.altriaAddOrUpdateProductDetection(inputObject, skuTypePOJOs);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;
        reportIO = new CustomSnap2BuyOutput(new ArrayList<>(), metaList);
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction Ends getProductDetectionsForReview -----------------");
        return output;
    }

    public String runAggregationForAltria(InputObject inputObject) {
        LOGGER.info("---------------- RestAction Starts runAggregationForAltria: inputObject: {}", inputObject);

        List<LinkedHashMap<String, Object>> resultListToPass = altriaDashboardService.runAggregationForAltria(inputObject);

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        reportInput.put("imageUUID", inputObject.getImageUUID());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
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

        LOGGER.info("---------------- RestAction Ends runAggregationForAltria -----------------");
        return output;
    }

	public Snap2BuyOutput getLinearFootageByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getLinearFootageByStore:: inputObject="+inputObject+" -----------------\n");

		List<LinkedHashMap<String, String>> footage = altriaDashboardService.getLinearFootageByStore(inputObject);

		HashMap<String, String> reportInput = new HashMap<String, String>();
		reportInput.put("projectId", ""+inputObject.getProjectId());
		reportInput.put("storeId", inputObject.getStoreId());
		reportInput.put("level", inputObject.getLevel());
		reportInput.put("value", inputObject.getValue());

		Snap2BuyOutput reportIO = new Snap2BuyOutput(footage, reportInput);
		     
		LOGGER.info("---------------RestAction Ends getLinearFootageByStore----------------\n");
		return reportIO;
	}
	
	public Snap2BuyOutput updateLinearFootageByStore(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts updateLinearFootageByStore:: inputObject="+inputObject+" -----------------\n");

		altriaDashboardService.updateLinearFootageByStore(inputObject);

		List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		result.put("responseCode", "200");
		result.put("responseMessage", "Updated footage Successfully");
		resultListToPass.add(result);

		HashMap<String, String> reportInput = new HashMap<String, String>();
		reportInput.put("projectId", ""+inputObject.getProjectId());
		reportInput.put("storeId", inputObject.getStoreId());
		reportInput.put("level", inputObject.getLevel());
		reportInput.put("value", inputObject.getValue());

		Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
		     
		LOGGER.info("---------------RestAction Ends updateLinearFootageByStore----------------\n");
		return reportIO;
	}

	public File getAltriaStoreVisitReport(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getAltriaStoreVisitReport----------------\n");
        
        String tempFilePath = "/tmp/Altria_StoreVisit_Report_" + System.currentTimeMillis()+".xlsx";
        
        File productAvailabilityFile = altriaDashboardService.getAltriaStoreVisitReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getAltriaStoreVisitReport----------------\n");

        return productAvailabilityFile;
	}

    public String listUsers() {
        LOGGER.info("---------------- RestAction Starts listUsers");

        List<LinkedHashMap<String, String>> response = userService.listUsers();

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Users found");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End listUsers");
        return output;
    }

    public Snap2BuyOutput userResetPassword(User userInput) {
        LOGGER.info("---------------RestAction Starts userResetPassword----------------\n");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getUserId());
        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getUserId().trim());
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        if (null == userList || userList.isEmpty()) {
            LOGGER.info("No existing record found for userId : {}", userInput.getUserId());

            result.put("responseCode", "204");
            result.put("responseMessage", "User not found. Request Rejected.");
        } else {

            String password = RandomStringUtils.randomAlphanumeric(8).toLowerCase();

            userInput.setPassword(password);

            authService.updateUserPassword(userInput);

            result.put("responseCode", "200");
            result.put("responseMessage", "Password Updated Successfully");
            result.put("password", password);
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userInput.getUserId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends userResetPassword----------------\n");
        return reportIO;
    }

    public String listGeoLevels(String customerCode){
        LOGGER.info("---------------- RestAction Starts listGeoLevels");

        List<LinkedHashMap<String, Object>> response = userService.listUsersGeoLevels(customerCode);

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();

        reportInput.put("customerCode", customerCode);

        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No GeoLevels found");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End listGeoLevels");
        return output;
    }

    public String listRoles() {
        LOGGER.info("---------------- RestAction Starts listRoles");

        List<LinkedHashMap<String, String>> response = userService.getUserRoles();

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();

        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Roles found");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End listRoles");
        return output;
    }

    public String listCustomerCodes() {
        LOGGER.info("---------------- RestAction Starts listCustomerCodes");

        List<LinkedHashMap<String, String>> response = userService.getCustomerCodes();

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();

        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No CustomerCodes found");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End listCustomerCodes");
        return output;
    }

    private void setAuthTokenAndDate(User userInput) {
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        UUID newAuthToken = UUID.randomUUID();
        String newAuthTokenIssueDate = inSdf.format(new Date());
        userInput.setAuthToken(newAuthToken.toString().trim());
        userInput.setAuthTokenIssueDate(newAuthTokenIssueDate);
    }

    public Snap2BuyOutput createUserAndUserGeoMap(User userInput) {
        LOGGER.info("Starts createUserAndUserGeoMap");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        LOGGER.info("Check if userId exists for userId : {}", userInput.getEmail());
        List<LinkedHashMap<String, String>> userList = authService.getUserDetail(userInput.getEmail());
        boolean isSuccess;
        isSuccess = createUser(userInput, userList, "Existing user not found for userId : {}", "User created with userId : {}", "UserId {} already exists.");

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (isSuccess == true) {
            if(!userInput.getGeoLevel().isEmpty() && !userInput.getGeoLevelId().isEmpty()){

                userService.createUserGeoMap(userInput);

                LOGGER.info("UserGeoMap created for UserId {}", userInput.getUserId());
            }
            result.put("responseCode", "200");
            result.put("responseMessage", "User Created Successfully");
        } else {
            result.put("responseCode", "204");
            result.put("responseMessage", "User already exist. Request Rejected.");
        }
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<>();
        reportInput.put("userId", userInput.getUserId());
        reportInput.put("firstName", userInput.getFirstName());
        reportInput.put("lastName", userInput.getLastName());


        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------Ends createUserAndUserGeoMap----------------\n");
        return reportIO;
    }

    private boolean createUser(User userInput, List<LinkedHashMap<String, String>> userList, String s, String s2, String s3) {
        boolean isSuccess;
        if (null == userList || userList.isEmpty()) {
            LOGGER.info(s, userInput.getEmail());
            if(null == userInput.getRole() || userInput.getRole().isEmpty()){
                userInput.setRole("agent");
            }
            setAuthTokenAndDate(userInput);

            authService.createUser(userInput);
            userInput.setUserId(userInput.getEmail());
            LOGGER.info(s2, userInput.getEmail());
            isSuccess = true;
        } else {
            LOGGER.error(s3, userInput.getUserId());
            isSuccess = false;
        }
        return isSuccess;
    }

	public File getPremiumReportCsv(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getPremiumReportCsv----------------\n");
        
        String tempFilePath = "/tmp/csvPremiumReport" + System.currentTimeMillis();
        
        File premiumReportCsv = processImageService.getPremiumReportCsv(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getPremiumReportCsv----------------\n");

        return premiumReportCsv;
	}

    public String getDeviceConfiguration() {
        LOGGER.info("---------------- RestAction Starts getDeviceConfiguration");

        List<LinkedHashMap<String, String>> response = userService.getDeviceConfiguration();

        Map<String, String> reportInput = new HashMap<>();
        List<Map<String, String>> metaList = new ArrayList<>();

        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = getCustomSnap2BuyOutput(response, metaList);
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End getDeviceConfiguration");
        return output;
    }

    public Object getUserScreenConfig(String userId) {
        LOGGER.info("---------------- RestAction Starts getUserScreenConfig");

        List<LinkedHashMap<String, Object>> response = new ArrayList<>();
        List<String> userScreens = userService.getUserScreenConfig(userId);

        if(null != userScreens && !userScreens.isEmpty()) {
            LinkedHashMap<String, Object> screens = new LinkedHashMap<>();
            screens.put("screens", userScreens);
            response.add(screens);
        }

        Map<String, String> reportInput = new HashMap<>();
        List<Map<String, String>> metaList = new ArrayList<>();

        reportInput.put("userId", userId);
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<>();
            emptyOutput.put("Message", "No Screens found");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End getUserScreenConfig");
        return output;
    }

    public Snap2BuyOutput saveUserScreenConfig(String userId, List<String> screens) {
        LOGGER.info("RestAction Starts saveUserScreenConfig");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<>();

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        userService.createUserScreenConfig(userId, screens);
        result.put("responseCode", "200");
        result.put("responseMessage", "Walk through for user updated.");

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<>();
        reportInput.put("userId", userId);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("RestAction Ends saveUserScreenConfig");
        return reportIO;
    }

    private CustomSnap2BuyOutput getCustomSnap2BuyOutput(List<LinkedHashMap<String, String>> response, List<Map<String, String>> metaList) {
        CustomSnap2BuyOutput reportIO;
        if (response.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<>();
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(response, metaList);
        }
        return reportIO;
    }

    public String getUserAppConfig(String userId) {
        LOGGER.info("---------------- RestAction Starts getUserAppConfig userId: {}", userId);

        List<LinkedHashMap<String, String>> response = userService.getUserAppConfig(userId);

        Map<String, String> reportInput = new HashMap<>();
        List<Map<String, String>> metaList = new ArrayList<>();

        reportInput.put("userId", userId);
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO;
        reportIO = getCustomSnap2BuyOutput(response, metaList);
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------- RestAction End getUserAppConfig");
        return output;
    }

    public Snap2BuyOutput insertOrUpdateUserAppConfig(UserAppConfig userAppConfig) {
        LOGGER.info("RestAction Starts insertOrUpdateUserAppConfig");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<>();

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        userService.insertOrUpdateUserAppConfig(userAppConfig);
        result.put("responseCode", "200");
        result.put("responseMessage", "UserAppConfig added/updated.");

        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<>();
        reportInput.put("userId", userAppConfig.getUserId());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("RestAction Ends insertOrUpdateUserAppConfig");
        return reportIO;
    }

	public Snap2BuyOutput updateGlobalSettings(JSONObject jsonObject) {
        LOGGER.info("---------------- RestAction Starts getUserAppConfig input: {}", jsonObject);
        if ( jsonObject.has("pauseImageAnalysis") ) {
        	String pauseImageAnalysis = jsonObject.getString("pauseImageAnalysis");
        	processImageService.pauseResumeImageAnalysis(pauseImageAnalysis);
        }
        LOGGER.info("---------------- RestAction Ends updateGlobalSettings" );
        HashMap<String, String> reportInput = new HashMap<>();
        reportInput.put("Global setting updated", "Yes");

        Snap2BuyOutput reportIO = new Snap2BuyOutput(new ArrayList<LinkedHashMap<String,String>>(), reportInput);
        return reportIO;
	}

    public String getRecentStoreVisits(InputObject inputObject) {
        LOGGER.info("RestAction Starts getRecentStoreVisits");

        List<LinkedHashMap<String, String>> storeList = userService.getRecentStoreVisits(inputObject.getUserId(), inputObject.getCustomerCode(), Integer.valueOf(inputObject.getValue()));

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("userId",inputObject.getUserId());
        reportInput.put("numberOfRecords",inputObject.getValue());

        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;

        if (storeList == null || storeList.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No stores found");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }

        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("RestAction Ends getRecentStoreVisits");

        return output;
    }

    public String getStoreVisitStatus(InputObject inputObject) {
        LOGGER.info("RestAction Starts getStoreVisitStatus inputObject:{}", inputObject);

        List<LinkedHashMap<String, String>> storeList = userService.getStoreVisitStatus(inputObject.getUserId(), inputObject.getStoreId(), inputObject.getVisitDate());

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("userId",inputObject.getUserId());
        reportInput.put("storeId",inputObject.getStoreId());
        reportInput.put("visitDate", inputObject.getVisitDate());

        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;

        if (storeList == null || storeList.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No stores found");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }

        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("RestAction Ends getStoreVisitStatus");

        return output;
    }

    public String getStoreVisitStatusByPlaceId(InputObject inputObject) {
        LOGGER.info("RestAction Starts getStoreVisitStatusByPlaceId inputObject:{}", inputObject);

        List<LinkedHashMap<String, String>> storeList = userService.getStoreVisitStatusByPlaceId(inputObject.getUserId(), inputObject.getPlaceId(), inputObject.getVisitDate());

        HashMap<String, String> reportInput = new HashMap<String, String>();

        reportInput.put("userId",inputObject.getUserId());
        reportInput.put("storeId",inputObject.getStoreId());
        reportInput.put("placeId",inputObject.getPlaceId());
        reportInput.put("visitDate", inputObject.getVisitDate());

        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);

        CustomSnap2BuyOutput reportIO = null;

        if (storeList == null || storeList.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No stores found");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }

        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("RestAction Ends getStoreVisitStatusByPlaceId");

        return output;
    }

    public String getProjectShareOfShelfByBrand(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectShareOfShelfByBrand----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("waveId", inputObject.getWaveId());
        reportInput.put("brandName", inputObject.getBrandName());
        reportInput.put("subCategory", inputObject.getSubCategory());
        reportInput.put("storeFormat", inputObject.getStoreFormat());
        reportInput.put("retailer", inputObject.getRetailer());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getProjectShareOfShelfByBrand(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
		LOGGER.info("---------------RestAction Starts getProjectShareOfShelfByBrand----------------\n");

        return output;
	}
    
    public String getProjectAllStoreShareOfShelfByBrand(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getProjectAllStoreShareOfShelfByBrand----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("month", inputObject.getMonth());
        reportInput.put("waveId", inputObject.getWaveId());
        reportInput.put("brandName", inputObject.getBrandName());
        reportInput.put("subCategory", inputObject.getSubCategory());
        reportInput.put("storeFormat", inputObject.getStoreFormat());
        reportInput.put("retailer", inputObject.getRetailer());
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<Map<String,String>> resultListToPass=new ArrayList<Map<String, String>>();
        resultListToPass = processImageService.getProjectAllStoreShareOfShelfByBrand(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
		LOGGER.info("---------------RestAction Starts getProjectAllStoreShareOfShelfByBrand----------------\n");

        return output;
	}

    public Snap2BuyOutput deleteImageAnalysisNew(String id) {
        LOGGER.info("---------------RestAction Starts deleteImageAnalysisNew");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        processImageService.deleteImageAnalysisNew(id);

        LOGGER.info("deleteUPC done");

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "UPC Deleted Successfully");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("id", id);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends deleteImageAnalysisNew----------------\n");
        return reportIO;
    }

	public void generateDailyImageErrorReport() {
		LOGGER.info("---------------RestAction Starts generateDailyImageErrorReport----------------\n");

		String tableData = "<html>"
				+ "<head><style>table, th, td {border: 1px solid black; border-collapse: collapse; padding-top:5px; padding-bottom:5px; padding-right:5px;}</style></head>"
				+ "<body> ";
		Map<String,List<String>> stats = processImageService.getDailyImageErrorStats();
		if (stats.isEmpty()) {
			LOGGER.info("---------------No Images in error status.. Report is not sent..----------------\n");
			return;
		} else {
			tableData += "<table> "
					+ "<tr> "
					+ "<th>Date</th>"
					+ "<th>Project ID</th>"
					+ "<th>Category ID</th>"
					+ "<th>Image Count</th> "
					//+ "<th>Image UUIDs</th> "
					+ "</tr>";
			for (String dateProjectCategory : stats.keySet()) {
				List<String> imageUUIDs = stats.get(dateProjectCategory);
				/*String imagesList ="";
				for(String imageUUID:imageUUIDs) {
					imagesList+=imageUUID+"<br>";
				}*/
				
				String[] parts = dateProjectCategory.split("#");
		        tableData += "<tr>"
		            + "<td>" + parts[0] + "</td> "
		            + "<td>" + parts[1] + "</td> "
		            + "<td>" + parts[2] + "</td> "
					+ "<td>" + imageUUIDs.size() + "</td> "
					//+ "<td>" + imagesList + "</td> "
		            + "</tr>";
			}
			tableData += "</table>";
		}
		
		tableData += "<br><br><p><i>Generated on " + new Date()+ "</i></p></body></html>";
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			mimeMessage.setContent(tableData, "text/html");
			
			Address fromAddress = new InternetAddress("noreply@snap2insight.com");
			mimeMessage.setFrom(fromAddress);
			
			Address toAddress = new InternetAddress("renish.pynadath@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.TO, toAddress);
			
			Address ccAddress1 = new InternetAddress("praveen.gopalakrishnan@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress1);
			
			Address ccAddress2 = new InternetAddress("anoop.ramankandath@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress2);
			
			Address ccAddress3 = new InternetAddress("balakumaran.nandagopal@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress3);
			
			Address ccAddress4 = new InternetAddress("gautam.malu@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress4);
			
			Address ccAddress5 = new InternetAddress("rahil.patel@snap2insight.com");
			mimeMessage.addRecipient(Message.RecipientType.CC, ccAddress5);
			
			Address replyAddress = new InternetAddress("noreply@snap2insight.com");
			Address addresses[] = {replyAddress};
			mimeMessage.setReplyTo(addresses);
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			mimeMessage.setSubject("Snap2Insight :: Image Processing Error Report for " + dtf.format(LocalDate.now()));
			
			mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
			mimeMessage.setSentDate(new Date());
			
			mailSender.send(mimeMessage);
		} catch (MailException e) {
            LOGGER.error("CRON_EXCEPTION {} , {}", e.getMessage(), e);
		} catch (MessagingException e) {
            LOGGER.error("CRON_EXCEPTION {} , {}", e.getMessage(), e);
		}
        LOGGER.info("---------------RestAction Ends generateDailyImageErrorReport----------------\n");
	}

    public Snap2BuyOutput addWebsiteEnquiry(String firstName, String lastName, String email, String phone, String company, String jobProfile, String note) {
        LOGGER.info("---------------RestAction Starts addWebsiteEnquiry");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();
        
        if ( StringUtils.isBlank(phone) && StringUtils.isBlank(company) && StringUtils.isBlank(jobProfile) ) {
        	//blog download
        	company = "BLOG DOWNLOAD";
        	uiService.addWebsiteEnquiry(firstName, lastName, email, phone, company, jobProfile, note);
        } else {
        	 uiService.addWebsiteEnquiry(firstName, lastName, email, phone, company, jobProfile, note);

             String emailBody  = "<html>" + 
             		"<head><style>table, th, td {border: 1px solid black; border-collapse: collapse; padding-top:5px; padding-bottom:5px; padding-right:5px;}</style></head>" + 
             		"<body>" + 
             		"<p>Hi "+firstName+",</p>" + 
             		"<p>Thanks for contacting us.</p>" + 
             		"<p>Snap2Insight is a leading provider of retail execution analytics solution using AI and Image Recognition. " + 
             		"Today we serve some of the leading brands and merchandisers in US and are also piloting in other geographies, " + 
             		"across modern trade and smaller independent (convenience) store formats. " + 
             		"We are focussed on helping companies like "+ company +"  get unprecedented visibility into " + 
             		"how products are merchandised at the shelf and trade promotions are executed in retail stores.</p>" + 
             		"<p>We will get back to you shortly, would love to learn and understand more about how we can help "+ company + " " + 
             		"measure and improve retail execution using our AI driven analytics platform.</p>" + 
             		"<p>Thanks,<br>" + 
             		"Snap2Insight Team</p>" + 
             		"<br>" + 
             		"Enquiry Details" + 
             		"<br>" + 
             		"<br>" + 
             		"<table>" + 
             		"<tr><td>Name</td><td>"+firstName+", "+lastName+"</td></tr>" + 
             		"<tr><td>Email</td><td>"+email+"</td></tr>" + 
             		"<tr><td>Phone</td><td>"+ (StringUtils.isBlank(phone)?"--":phone) +"</td></tr>" + 
             		"<tr><td>Company</td><td>"+company+"</td></tr>" + 
             		"<tr><td nowrap>Job Profile</td><td>"+(StringUtils.isBlank(jobProfile)?"--":jobProfile)+"</td></tr>" + 
             		"<tr><td>Message</td><td><pre>"+(StringUtils.isBlank(note)? "--" : StringEscapeUtils.escapeHtml4(note))+"</pre></td></tr>" + 
             		"</table>" + 
             		"</body>" + 
             		"</html>";

             sendEnquiryAckEmail(emailBody, "Snap2Insight - Thanks for reaching out", 
             		Arrays.asList(new String[] {email.trim()}),
             		Arrays.asList(new String[] {"hello@snap2insight.com"}),
             		Arrays.asList(new String[] {"praveen.gopalakrishnan@snap2insight.com","renish.pynadath@snap2insight.com"}));
        }

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "Thanks for reaching out, we will be in touch with you shortly");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("firstName", firstName);
        reportInput.put("lastName", lastName);
        reportInput.put("email", email);
        reportInput.put("phone", phone);
        reportInput.put("company", company);
        reportInput.put("jobProfile", jobProfile);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends addWebsiteEnquiry----------------\n");
        return reportIO;
    }

    public String getWebsiteEnquiries() {
        LOGGER.info("RestAction Starts getWebsiteEnquiries");

        Map<String, String> reportInput = new HashMap<String, String>();
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<Map<String,String>> resultListToPass = uiService.getWebsiteEnquiries();

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

        LOGGER.info("RestAction Starts getWebsiteEnquiries");

        return output;
    }

    public Snap2BuyOutput saveProjectRepResponses(ProjectRepResponse projectRepResponse) {
        LOGGER.info("RestAction Starts saveProjectRepResponses {}", projectRepResponse);

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        projectService.saveProjectRepResponses(projectRepResponse);

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        result.put("responseMessage", "Project Response added");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectResponse", projectRepResponse.getProjectResponseList().toString());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("RestAction Ends saveProjectRepResponses");
        return reportIO;
    }
  
    public Snap2BuyOutput updateFCMToken(String userId, String fcmToken, String platform) {
        LOGGER.info("RestAction Starts updateFCMToken");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        userService.updateFCMTokenForUser(userId, fcmToken, platform);

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);
        reportInput.put("fcmToken", fcmToken);

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("RestAction Ends updateFCMToken");
        return reportIO;
    }

    public Snap2BuyOutput saveUIMenus(CustomerRoleMenuMap customerRoleMenuMap) {
        LOGGER.info("RestAction Starts saveUIMenus");

        List<LinkedHashMap<String, String>> resultListToPass = new ArrayList<LinkedHashMap<String, String>>();

        try {
            uiService.addUIMenus(customerRoleMenuMap);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION RestAction {} , {}", e.getMessage(), e);
        }

        InputObject inputObject = new InputObject();
        inputObject.setCustomerCode(customerRoleMenuMap.getCustomerCode());
        inputObject.setRole(customerRoleMenuMap.getRole());
        inputObject.setSource("app");
        List<LinkedHashMap<String, String>> resultData = uiService.getMenusForUser(inputObject);

        JSONObject inputJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        inputJSON.put("type",FirebaseWrapper.NotificationType.MENU_CONFIGURATIONS.name());
        for (LinkedHashMap<String, String> menu: resultData) {
            JSONObject featureObject = new JSONObject();
            featureObject.put("feature", menu.get("routerLink"));
            jsonArray.put(featureObject);
        }
        inputJSON.put("data", jsonArray);

        resultData = uiService.getUserNotificationTokenByCustomerCodeAndRole(customerRoleMenuMap.getCustomerCode(), customerRoleMenuMap.getRole());

        for (LinkedHashMap<String, String> notificationData: resultData) {
            FirebaseWrapper.sendFirebaseNotification(inputJSON, notificationData.get("fcmToken"));
        }

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        result.put("responseCode", "200");
        resultListToPass.add(result);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", customerRoleMenuMap.getCustomerCode());
        reportInput.put("role", customerRoleMenuMap.getRole());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("RestAction Ends saveUIMenus");
        return reportIO;
    }

    
    public String getProjectAllScoreSummary(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectAllScoreSummary----------------\n");

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("level", inputObject.getLevel());
        reportInput.put("value", inputObject.getValue());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);

        List<Map<String,Object>> resultListToPass=new ArrayList<Map<String, Object>>();
        Map<String,Object> summary = scoreService.getProjectAllScoreSummary(inputObject);
        resultListToPass.add(summary);
        
        CustomSnap2BuyOutput reportIO = null;
        reportIO = new CustomSnap2BuyOutput(resultListToPass, metaList);
        if (resultListToPass.isEmpty()) {
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
            reportIO = new CustomSnap2BuyOutput(resultListToPass, metaList);
        }

        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getProjectAllScoreSummary----------------\n");

        return output;
    }
    
    public Snap2BuyOutput updateImageQualityParams(String input) {
		LOGGER.info("---------------RestAction Starts updateImageQualityParams----------------\n");
        if (input != null && !input.isEmpty()) {
            Gson gson = new Gson();
            ImageStore imageToUpdate = gson.fromJson(input, ImageStore.class);
            processImageService.updateImageQualityParams(imageToUpdate);
        }

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends updateImageQualityParams----------------\n");
        return reportIO;
	}

	public void deleteAllDetectionsByImageUUID(String imageUUID) {
		LOGGER.info("---------------RestAction Starts deleteAllDetectionsByImageUUID");

        processImageService.deleteAllDetectionsByImageUUID(imageUUID);

        LOGGER.info("Delete all detections for image {} done", imageUUID);

        LOGGER.info("---------------RestAction Ends deleteAllDetectionsByImageUUID----------------\n");
		
	}
	
	public Snap2BuyOutput updateStoreReviewStatus(String input) {
		LOGGER.info("---------------RestAction Starts updateStoreReviewStatus----------------\n");
        if (input != null && !input.isEmpty()) {
        	JsonObject obj = new JsonParser().parse(input).getAsJsonObject();
            String projectId = obj.get("projectId").getAsString();
            String storeId = obj.get("storeId").getAsString();
            String taskId = obj.get("taskId").getAsString();
            String reviewStatus = obj.get("reviewStatus").getAsString();
            String status = obj.get("status").getAsString();
            
            if ( status.equals("1") && !reviewStatus.equals("1") ) {
            	throw new IllegalArgumentException("To publish a store, review status should be set to Reviewed");
            }
            
            processImageService.updateStoreReviewStatus(projectId, storeId, taskId, reviewStatus, status);
        }

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends updateStoreReviewStatus----------------\n");
        return reportIO;
	}
	
	public Snap2BuyOutput getProjectStoreRepResponses(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoreRepResponses----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getProjectStoreRepResponses(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectStoreRepResponses----------------\n");

        return reportIO;
    }
	
	public Snap2BuyOutput getProjectStoresForReview(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getProjectStoresForReview----------------\n");
        String dateRange = inputObject.getFromDate() + inputObject.getToDate();
        
        if (dateRange.contains("-")) {
        	long count = dateRange.chars().filter(ch -> ch == '-').count();
        	if ( count != 2 ) {
        		throw new IllegalArgumentException("A date range must be specified");
        	}
        }
        
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getProjectStoresForReview(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("waveId", inputObject.getWaveId());
        reportInput.put("fromDate", inputObject.getFromDate());
        reportInput.put("toDate", inputObject.getToDate());
        reportInput.put("reviewStatus", inputObject.getReviewStatus());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getProjectStoresForReview----------------\n");

        return reportIO;
    }

	public Snap2BuyOutput bulkUploadPremiumResults(String input) {
		LOGGER.info("---------------RestAction Starts bulkUploadPremiumResults----------------\n");
        if (input != null && !input.isEmpty()) {
        	JsonObject obj = new JsonParser().parse(input).getAsJsonObject();
            JsonArray projectIdArray = obj.get("projectId").getAsJsonArray();
            List<String> projectsToUpload = new ArrayList<String>();
            for ( int i=0 ; i < projectIdArray.size() ; i++ ) {
            	projectsToUpload.add(projectIdArray.get(i).toString());
            }
            
            if (projectsToUpload.size() > 0 ) {
            	prmBulkUploaderService.bulkUploadPremiumResults(projectsToUpload);
            }
        }

        HashMap<String, String> reportInput = new HashMap<String, String>();
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        LOGGER.info("---------------RestAction Ends bulkUploadPremiumResults----------------\n");
        return reportIO;
	}

	public Snap2BuyOutput getPremiumBulkUploadStatus() {
		LOGGER.info("---------------RestAction Starts getPremiumBulkUploadStatus----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = prmBulkUploaderService.getPremiumBulkUploadStatus();

        HashMap<String, String> reportInput = new HashMap<String, String>();
       
        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getPremiumBulkUploadStatus----------------\n");

        return reportIO;
	}
	
	public File getStoreLevelDistributionCSVReport(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStoreLevelDistributionCSVReport----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis();

        File projectAllStoreResultsCsv = processImageService.getStoreLevelDistributionCSVReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getStoreLevelDistributionCSVReport----------------\n");

        return projectAllStoreResultsCsv;
    }
	
	public File getStoreLevelImageCSVReport(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getStoreLevelImageCSVReport----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis();

        File projectAllStoreResultsCsv = processImageService.getStoreLevelImageCSVReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getStoreLevelImageCSVReport----------------\n");

        return projectAllStoreResultsCsv;
    }
    
    public String getPremiumJobDetails(String jobId) {
        LOGGER.info("---------------RestAction Starts getPremiumJobDetails----------------\n");
        
        //String tempFilePath = "/tmp/Premium_JobDetails_" + System.currentTimeMillis()+".xlsx";
        
        //File productAvailabilityFile = prmBulkUploaderService.getJobDetails(jobId, tempFilePath);
        
        String response = prmBulkUploaderService.getJobDetails(jobId);

        LOGGER.info("---------------RestAction Ends getPremiumJobDetails----------------\n");

        return response;
	}

	public void pollAndSubmitImagesForAnalysis(String batchSize) {
		LOGGER.info("---------------RestAction Starts pollAndSubmitImagesForAnalysis----------------\n");
        
        processImageService.pollAndSubmitImagesForAnalysis(batchSize);

        LOGGER.info("---------------RestAction Ends pollAndSubmitImagesForAnalysis----------------\n");

	}
	
	public File getHomePanelProjectStatusReport() {
        LOGGER.info("---------------RestAction Starts getHomePanelProjectStatusReport----------------\n");

        String tempFilePath = "/tmp/csvDownload" + System.currentTimeMillis()+".xlsx";

        File homePanelStatusReport = processImageService.getHomePanelProjectStatusReport(tempFilePath);

        LOGGER.info("---------------RestAction Ends getHomePanelProjectStatusReport----------------\n");

        return homePanelStatusReport;
    }
	
	public void updateDuplicateDetections(List<Long> duplicateDetections) {
		LOGGER.info("---------------RestAction Starts updateDuplicateDetections");

        processImageService.updateDuplicateDetections(duplicateDetections);

        LOGGER.info("---------------RestAction Ends updateDuplicateDetections----------------\n");
		
	}
	
	public String getInternalProjectStatus(InputObject inputObject) {
        LOGGER.info("---------------RestAction Starts getInternalProjectStatus----------------\n");
        
        String statusReportHtml = processImageService.getInternalProjectStatus(inputObject);

        LOGGER.info("---------------RestAction Ends getProjectStoresForReview----------------\n");

        return statusReportHtml;
    }
	
	public Snap2BuyOutput resolveGroupUpcs(String input) {
		LOGGER.info("---------------RestAction Starts resolveGroupUpcs----------------\n");
		HashMap<String, String> reportInput = new HashMap<String, String>();
		Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
		
		if (input != null && !input.isEmpty()) {
            JsonObject obj = new JsonParser().parse(input).getAsJsonObject();
            String projectId = obj.get("projectId").getAsString();
            String storeId = obj.get("storeId").getAsString();
            String taskId = obj.get("taskId").getAsString();
            
            reportInput.put("projectId", projectId);
            reportInput.put("storeId", storeId);
            reportInput.put("taskId", taskId);
            
            JsonArray groupUPCs = obj.getAsJsonArray("groupUPCs");
            Map<String,List<String>> orderedUPCGroupMap = new LinkedHashMap<String,List<String>>();
            for(int i=0; i < groupUPCs.size(); i++) {
            	String oneGroup = groupUPCs.get(i).getAsString();
            	String[] parts = oneGroup.split(",");
            	List<String> upcs = new ArrayList<String>();
            	for(String part : parts) {
            		upcs.add(part.trim());
            	}
            	orderedUPCGroupMap.put(i+"", upcs);
            }
            List<LinkedHashMap<String, String>> returnList = processImageService.resolveGroupUpcs(projectId,storeId,taskId,orderedUPCGroupMap);
            reportIO = new Snap2BuyOutput(returnList, reportInput);
        }
        
        LOGGER.info("---------------RestAction Ends resolveGroupUpcs----------------\n");
        return reportIO;
	}
	
	public Snap2BuyOutput saveImageAnalysisData(String imageUUID, String imageAnalysisData) {
        LOGGER.info("---------------RestAction Starts saveImageAnalysisData----------------\n");
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("imageUUID", imageUUID);
        
        Snap2BuyOutput reportIO = new Snap2BuyOutput(null, reportInput);
        
        if (StringUtils.isNotBlank(imageUUID) && StringUtils.isNotBlank(imageAnalysisData)) {
            processImageService.saveImageAnalysisData(imageUUID, imageAnalysisData);
        }
        
        LOGGER.info("---------------RestAction Ends saveImageAnalysisData----------------\n");
        return reportIO;
	}
	
	public Snap2BuyOutput getNextImagesToProcessExternally(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getNextImagesToProcessExternally----------------\n");
        List<LinkedHashMap<String, String>> resultListToPass = processImageService.getNextImagesToProcessExternally(inputObject);

        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        reportInput.put("limit", inputObject.getLimit());

        Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
        LOGGER.info("---------------RestAction Ends getNextImagesToProcessExternally----------------\n");

        return reportIO;
	}

	public String getITGStoreDetails(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStoreDetails----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("storeId", inputObject.getStoreId());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgDashboardService.getITGStoreDetails(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}

	public String getITGStoresWithFilters(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStoresWithFilters----------------\n");

        Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("geoLevel", inputObject.getGeoLevel());
        reportInput.put("geoLevelId", inputObject.getGeoLevelId());
        reportInput.put("timePeriodType", inputObject.getTimePeriodType());
        reportInput.put("timePeriod", inputObject.getTimePeriod());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass = new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgDashboardService.getITGStoresWithFilters(inputObject);
        
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        LOGGER.info("---------------RestAction Ends getITGStoresWithFilters----------------\n");

        return output;
	}
	
	public String getITGStats(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStats----------------\n");
		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", inputObject.getProjectId()+"");
        reportInput.put("geoLevel", inputObject.getGeoLevel());
        reportInput.put("geoLevelId", inputObject.getGeoLevelId());
        reportInput.put("timePeriodType", inputObject.getTimePeriodType());
        reportInput.put("timePeriod", inputObject.getTimePeriod());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgDashboardService.getITGStats(inputObject);
        
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        LOGGER.info("---------------RestAction Ends getITGStats----------------\n");

        return output;
	}
	
	public File getITGReport(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGReport----------------\n");
        
        String tempFilePath = "/tmp/ITGStores" + System.currentTimeMillis();
        
        File projectStoreDistributionFile = itgDashboardService.getITGReport(inputObject, tempFilePath);

        LOGGER.info("---------------RestAction Ends getITGReport----------------\n");

        return projectStoreDistributionFile;
	}

	public String getITGGeoMappingForUser(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGGeoMappingForUser----------------\n");
        List<GenericGeo> storeList = itgDashboardService.getITGGeoMappingForUser(inputObject);
        HashMap<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId",inputObject.getUserId()+"");
        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(reportInput);
        
        CustomSnap2BuyOutput reportIO = null;
        if ( storeList == null || storeList.isEmpty() ) {
        	Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        } else {
        	reportIO = new CustomSnap2BuyOutput(storeList, metaList);
        }
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        
        LOGGER.info("---------------RestAction Ends getITGGeoMappingForUser----------------\n");

        return output;
	}
	
	public void pollAndProcessStoreAnalysis() {
        LOGGER.info("---------------RestAction Starts pollAndProcessStoreAnalysis----------------\n");
        processImageService.pollAndProcessStoreAnalysis();
        LOGGER.info("---------------RestAction Ends pollAndProcessStoreAnalysis----------------\n");
    }
	
	public void pollAndSubmitStoreForAnalysis() {
        LOGGER.info("---------------RestAction Starts pollAndSubmitStoreForAnalysis----------------\n");
        processImageService.pollAndSubmitStoreForAnalysis();
        LOGGER.info("---------------RestAction Ends pollAndSubmitStoreForAnalysis----------------\n");
    }
	
	public void doDailyITGAggregation(int projectId, String visitDateId) {
        LOGGER.info("---------------RestAction Starts doDailyITGAggregation----------------\n");
        LOGGER.info("Running ITG Daily Aggregation for projectId {} , visitDate {}",projectId,visitDateId);
        itgAggregationService.runDailyAggregation(projectId, visitDateId);
        LOGGER.info("---------------RestAction Ends doDailyITGAggregation----------------\n");
    }
	
	public String getITGStoreVisitsToReview(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStoreVisitsToReview----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("dateId", inputObject.getVisitDate());
        reportInput.put("projectId",  ""+inputObject.getProjectId());
        reportInput.put("bucketId", inputObject.getValue());
        reportInput.put("storeId", inputObject.getStoreId());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgAggregationService.getStoreVisitsToReview(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public String getITGStoreVisitImagesToReview(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStoreVisitImagesToReview----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        reportInput.put("storeId", inputObject.getStoreId());
        reportInput.put("taskId", inputObject.getTaskId());
        reportInput.put("showAll", inputObject.getShowAll());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgAggregationService.getStoreVisitImagesToReview(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}

	public void updateITGStoreVisitAggregationData(String jsonPayload) {
		LOGGER.info("---------------RestAction Starts getITGStoreVisitImagesToReview----------------\n");
		itgAggregationService.updateStoreVisitAggregationData(jsonPayload);
		LOGGER.info("---------------RestAction Ends getITGStoreVisitImagesToReview----------------\n");
		
	}
	
	public String getITGStoreVisitReviewComments(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGStoreVisitReviewComments----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgAggregationService.getITGStoreVisitReviewComments(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public String getITGProductBrands(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGProductBrands----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgAggregationService.getITGProductBrands(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public String getITGReviewStatsSummary(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getITGReviewStatsSummary----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("projectId", ""+inputObject.getProjectId());
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = itgAggregationService.getITGReviewStatsSummary(inputObject);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public void saveStoreLevelData(String jsonPayload) {
		LOGGER.info("---------------RestAction Starts saveStoreLevelData----------------\n");
		processImageService.saveStoreLevelData(jsonPayload);
		LOGGER.info("---------------RestAction Ends saveStoreLevelData----------------\n");
		
	}
	
	public void updateImageAnalysisStatus(String imageUUID, String jsonPayload) {
		LOGGER.info("---------------RestAction Starts updateImageAnalysisStatus----------------\n");
		processImageService.updateImageAnalysisStatus(imageUUID, jsonPayload);
		LOGGER.info("---------------RestAction Ends updateImageAnalysisStatus----------------\n");
		
	}
	
	public String getSupportInfoByStoreId(String customerCode, String storeId) {
		LOGGER.info("---------------RestAction Starts getSupportInfoByStoreId----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("storeId", storeId);
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = supportService.getSupportInfoByStoreId(customerCode, storeId);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public String getSupportInfoByUserId(String customerCode, String userId) {
		LOGGER.info("---------------RestAction Starts getSupportInfoByUserId----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = supportService.getSupportInfoByUserId(customerCode, userId);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}
	
	public String getUserListByCustomerCode(String customerCode) {
		LOGGER.info("---------------RestAction Starts getUserListByCustomerCode----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("customerCode", customerCode);
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = supportService.getUserListByCustomerCode(customerCode);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}

	public String getCustomerList() {
		LOGGER.info("---------------RestAction Starts getCustomerList----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = supportService.getCustomers();
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}

	public String createSupportRequest(Map<String, String> requestContents, List<String> attachments) {
		LOGGER.info("---------------RestAction Starts createSupportRequest----------------\n");

		boolean submissionStatus = supportService.createSupportRequest(requestContents,attachments);
		
		Map<String, String> input = new HashMap<String, String>();
		List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
		metaList.add(input);
		
		Map<String, String> emptyOutput = new HashMap<String, String>();
		emptyOutput.put("Message", "Support request submitted");
		emptyOutput.put("Status", ""+submissionStatus);
		List<Map<String,String>> emptyOutputList = new ArrayList<>();
		emptyOutputList.add(emptyOutput);
		CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
		//convert to json here
		Gson gson = new Gson();
		String output = gson.toJson(reportIO);

		LOGGER.info("---------------RestAction Ends createSupportRequest----------------\n");

		return output;
	}
	
	public String getRepPerformanceMetrics(String userId) {
		LOGGER.info("---------------RestAction Starts getITGStoreVisitImagesToReview----------------\n");

		Map<String, String> reportInput = new HashMap<String, String>();
        reportInput.put("userId", userId);
        
        List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
        metaList.add(reportInput);
        
        List<LinkedHashMap<String,Object>> resultListToPass=new ArrayList<LinkedHashMap<String, Object>>();
        resultListToPass = processImageService.getRepPerformanceMetrics(userId);
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
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);

        return output;
	}

	public Snap2BuyOutput getStoreByCustomerCodeStoreId(InputObject inputObject) {
		LOGGER.info("---------------RestAction Starts getStoreByCustomerCodeStoreId----------------\n");

		String storeId = inputObject.getCustomerCode()+"_"+inputObject.getCustomerStoreNumber();
		List<LinkedHashMap<String,String>> resultListToPass = metaService.getStoreMasterByStoreId(storeId);
		
		HashMap<String, String> reportInput = new HashMap<String, String>();
		reportInput.put("storeId", inputObject.getCustomerStoreNumber());
		Snap2BuyOutput reportIO = new Snap2BuyOutput(resultListToPass, reportInput);
		LOGGER.info("---------------RestAction Ends getProjectTypeDetail----------------\n");
		return reportIO;
	}		

}

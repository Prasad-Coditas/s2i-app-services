/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snap2buy.themobilebackend.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.snap2buy.themobilebackend.async.CloudStorageService;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.mapper.ParamMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.rest.action.RestS2PAction;
import com.snap2buy.themobilebackend.service.MetaService;
import com.snap2buy.themobilebackend.upload.UploadStatusTracker;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import com.snap2buy.themobilebackend.util.CustomSnap2BuyOutput;
import com.snap2buy.themobilebackend.util.Snap2BuyOutput;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author sachin
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(value = "/S2B")
@Scope("request")
public class RestS2PController {

    private Logger LOGGER = LoggerFactory.getLogger(RestS2PController.class);

    @Autowired
    ServletContext servletContext;

    @Autowired(required=true)
    @Qualifier(BeanMapper.BEAN_REST_ACTION_S2P)
    private RestS2PAction restS2PAction;

    @Autowired(required=true)
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;

    @Autowired(required=true)
    @Qualifier(BeanMapper.BEAN_META_SERVICE)
    private MetaService metaService;

    @Autowired
    private Environment env;

    @Autowired
    private CloudStorageService cloudStorageService;

    @PostMapping(value = "/saveImage", produces = MediaType.APPLICATION_JSON, consumes =MediaType.MULTIPART_FORM_DATA )
    public String saveImage(
            @QueryParam(ParamMapper.CATEGORY_ID) @DefaultValue("-9") String categoryId,
            @QueryParam(ParamMapper.LATITUDE) @DefaultValue("-9") String latitude,
            @QueryParam(ParamMapper.LONGITUDE) @DefaultValue("-9") String longitude,
            @QueryParam(ParamMapper.TIMESTAMP) @DefaultValue("-9") String timeStamp,
            @QueryParam(ParamMapper.USER_ID) @DefaultValue("app") String userId,
            @QueryParam(ParamMapper.SYNC) @DefaultValue("false") String sync,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @QueryParam(ParamMapper.AGENT_ID) @DefaultValue("-9") String agentId,
            @QueryParam(ParamMapper.RETAILER_STORE_ID) @DefaultValue("-9") String retailerStoreId,
            @QueryParam(ParamMapper.PLACE_ID) @DefaultValue("-9") String placeId,
            @QueryParam(ParamMapper.DATE_ID) @DefaultValue("-9") String dateId,
            @QueryParam(ParamMapper.NAME) @DefaultValue("-9") String storeName,
            @QueryParam(ParamMapper.STREET) @DefaultValue("-9") String street,
            @QueryParam(ParamMapper.CITY) @DefaultValue("-9") String city,
            @QueryParam(ParamMapper.COUNTRY) @DefaultValue("-9") String country,
            @QueryParam(ParamMapper.STATE) @DefaultValue("-9") String state,
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.QUESTION_ID) String questionId,
            @QueryParam(ParamMapper.SEQUENCE_NUMBER) @DefaultValue("1") String sequenceNumber,
            @QueryParam(ParamMapper.TOTAL_IMAGES) @DefaultValue("1") String totalImages,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.CUSTOMER_STORE_NUMBER) @DefaultValue("-9") String customerStoreNumber,
            @QueryParam(ParamMapper.CUSTOMER_PROJECT_ID) @DefaultValue("-9") String customerProjectId,
            
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts saveImage----------------\n");
        try {
            UUID uniqueKey = UUID.randomUUID();
            InputObject inputObject = new InputObject();

            String customerCode = null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "";
            
            String platform = null != request.getAttribute("platform")
                    ? request.getAttribute("platform").toString().trim().toLowerCase() : "";

            if (StringUtils.isNotBlank(platform) && ( platform.equals("android") || platform.equals("ios") )) {
                inputObject.setSource("app");
            } else {
                inputObject.setSource("web");
            }

            userId = userId.trim().toLowerCase();
            if (!dateId.equalsIgnoreCase("-9"))
            {
                LOGGER.info("---------------Controller----dateId = {}", dateId );
                inputObject.setVisitDate(dateId);

            } else if((!timeStamp.isEmpty())||(timeStamp!=null)||(!timeStamp.equalsIgnoreCase("-9"))) {
                LOGGER.info("---------------Controller----timeStamp = {}", timeStamp);
                Date date = new Date(Long.parseLong(timeStamp));
                DateFormat format = new SimpleDateFormat("yyyyMMdd");
                format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                String formattedDate = format.format(date);
                inputObject.setVisitDate(formattedDate);
            } else {
                LOGGER.info("---------------Controller----dateId and timeStamp are empty---------------");
                inputObject.setVisitDate(dateId);
            }

            inputObject.setRetailerStoreId(retailerStoreId);
            inputObject.setStoreId(storeId);
            inputObject.setPlaceId(placeId);

            inputObject.setHostId("1");
            inputObject.setImageUUID(uniqueKey.toString().trim());
            inputObject.setCategoryId(categoryId.trim());
            inputObject.setLatitude(latitude.trim());
            inputObject.setLongitude(longitude.trim());
            inputObject.setStreet(street.trim());
            inputObject.setCity(city.trim());
            inputObject.setState(state.trim());
            inputObject.setCountry(country.trim());
            inputObject.setName(storeName.trim());
            inputObject.setTimeStamp(timeStamp.trim());
            inputObject.setUserId(userId.trim());
            inputObject.setSync(sync);
            inputObject.setAgentId(agentId);
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setTaskId(taskId);
            inputObject.setImageHashScore("0");
            inputObject.setImageRotation("0");
            inputObject.setQuestionId(questionId);
            inputObject.setTotalImages(totalImages);
            inputObject.setSequenceNumber(sequenceNumber);
            inputObject.setCustomerStoreNumber(customerStoreNumber);
            inputObject.setCustomerProjectId(customerProjectId);
            inputObject.setCustomerCode(customerCode);

            LOGGER.info("Input object created {}", inputObject);
            //Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            LOGGER.info("Repository set to file object");
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            LOGGER.info("ServletFileUpload object created");
            String result = "";
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            LOGGER.info("Iterator added on list fileItems");
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            LOGGER.info("Iterator added");
            while (iter.hasNext()) {
                LOGGER.info("Controller: Inside while................");
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.", name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    String thumbnailPath =  projectId + "/" + uniqueKey.toString().trim() + "-thm.jpg";
                    String previewPath =  projectId + "/" + uniqueKey.toString().trim() + "-prv.jpg";

                    inputObject.setOrigWidth("0");
                    inputObject.setOrigHeight("0");
                    inputObject.setNewWidth("0");
                    inputObject.setNewHeight("0");
                    inputObject.setThumbnailPath(thumbnailPath);
                    inputObject.setPreviewPath(previewPath);


                    FileInputStream fileInputStream = (FileInputStream) item.getInputStream();

                    String filenamePath = cloudStorageService.storeImage(projectId, uniqueKey.toString().trim(), fileInputStream);
                }
            }
            LOGGER.info("---------------Controller Starts saveImage with details {}", inputObject);

            return restS2PAction.saveImage(inputObject);
        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends saveImage----------------\n");
            return output;
        }
    }

    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getJob")
    @GetMapping(value = "/getJob", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput getJob(
            @QueryParam(ParamMapper.HOST_ID) @DefaultValue("-9") String hostId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getJob::hostId::={}",hostId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setHostId(hostId);

            return restS2PAction.getJob(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("hostId", hostId);
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getJob----------------\n");
            return rio;
        }
    }


    @GetMapping(value = "/getCronJobCount", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput getCronJobCount(
            @QueryParam(ParamMapper.HOST_ID) @DefaultValue("-9") String hostId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCronJobCount::hostId::={}", hostId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setHostId(hostId);

            return restS2PAction.getCronJobCount(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("hostId", hostId);
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getCronJobCount----------------\n");
            return rio;
        }
    }

    @PostMapping(value = "/storeShelfAnalysis", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput storeShelfAnalysis(

            JAXBElement<ShelfAnalysisInput> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts storeShelfAnalysis----------------\n");
        try {

            ShelfAnalysisInput shelfAnalysisInput = p.getValue();

            LOGGER.info("---------------Controller  storeShelfAnalysis::={}",shelfAnalysisInput.toString());

            return restS2PAction.storeShelfAnalysis(shelfAnalysisInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getShelfAnalysis----------------\n");
            return rio;
        }
    }


    @GetMapping(value = "/getShelfAnalysis", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput getShelfAnalysis(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getShelfAnalysis::imageUUID::={}",imageUUID);
        try {
            Snap2BuyOutput rio;
            InputObject inputObject = new InputObject();

            inputObject.setImageUUID(imageUUID);

            return restS2PAction.getShelfAnalysis(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("imageUUID", imageUUID);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getShelfAnalysis----------------\n");
            return rio;
        }
    }


    @GetMapping(value = "/getUpcDetails", produces = MediaType.APPLICATION_JSON)
    public Snap2BuyOutput getUpcDetails(
            @QueryParam(ParamMapper.UPC) @DefaultValue("-9") String upc,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getUpcDetails::upc::={}",upc);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setUpc(upc);

            return restS2PAction.getUpcDetails(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("upc", upc);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getUpcDetails----------------\n");
            return rio;
        }
    }

    //@Produces({"image/jpeg", "image/png"})
    @GetMapping(value = "/getUpcImage", produces = MediaType.MEDIA_TYPE_WILDCARD)
    //produces part having jpeg and png images
    public Response getUpcImage(
            @QueryParam(ParamMapper.UPC) @DefaultValue("-9") String upc,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getUpcImage::upc {}",upc);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setUpc(upc);
            File f = restS2PAction.getUpcImage(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            r.header("Content-Disposition", "attachment; filename=" + f.getName());
            return r.build();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("upc", upc);
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getUpcImage----------------\n");
            return Response.serverError().build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/checkS2P")
    public Snap2BuyOutput checkS2P(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts checkS2P----------------\n");
        try {
            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("success", "Success");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends checkS2P----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/storeThumbnails")
    public Snap2BuyOutput storeThumbnails(
            @QueryParam(ParamMapper.IMAGE_FOLDER_PATH) @DefaultValue("-9") String imageFolderPath,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts storeThumbnails----------------\n");
        try {
            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            restS2PAction.storeThumbnails(imageFolderPath);
            inputList.put("success", "Success");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends checkS2P----------------\n");
            return rio;
        }
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getReport")
    public Snap2BuyOutput getReport(
            @QueryParam(ParamMapper.FREQUENCY) @DefaultValue("-9") String frequency,
            @QueryParam(ParamMapper.DATE_ID) @DefaultValue("-9") String dateId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.CATEGORY_ID) @DefaultValue("-9") String categoryId,
            @QueryParam(ParamMapper.BRAND_ID) @DefaultValue("-9") String brandId,
            @QueryParam(ParamMapper.MARKET_ID) @DefaultValue("-9") String marketId,
            @QueryParam(ParamMapper.CHAIN_ID) @DefaultValue("-9") String chainId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getReport----------------\n");
        try {

            InputObject inputObject = new InputObject();

            inputObject.setBrandId(brandId);
            inputObject.setCategoryId(categoryId);
            inputObject.setChainId(chainId);
            inputObject.setVisitDate(dateId);
            inputObject.setFrequency(frequency);
            inputObject.setMarketId(marketId);
            inputObject.setBrandId(brandId);
            LOGGER.info("---------------Controller getReport::={}",inputObject.toString());

            return restS2PAction.getReport(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("brandId",brandId);
            inputList.put("categoryId", categoryId);
            inputList.put("chainId", chainId);
            inputList.put("dateId", dateId);
            inputList.put("frequency", frequency);
            inputList.put("marketId", marketId);
            inputList.put("brandId", brandId);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getReport----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getImageAnalysis")
    public Snap2BuyOutput getImageAnalysis(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @QueryParam(ParamMapper.SHOW_ALL) @DefaultValue("false") String showAll,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getImageAnalysis::imageUUID={}, showAll={}", imageUUID,showAll);
        try {

            InputObject inputObject = new InputObject();

            inputObject.setImageUUID(imageUUID);
            inputObject.setShowAll(showAll);

            return restS2PAction.getImageAnalysis(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("imageUUID", imageUUID);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getImageAnalysis----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getStoreOptions")
    public Snap2BuyOutput getStoreOptions(
            @QueryParam(ParamMapper.RETAILER_CODE) @DefaultValue("-9") String retailerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreOptions----------------\n");
        try {
            InputObject inputObject = new InputObject();

            inputObject.setRetailerCode(retailerCode);

            return restS2PAction.getStoreOptions(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoreOptions----------------\n");
            return rio;
        }
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getImages")
    public Snap2BuyOutput getImages(
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.DATE_ID) @DefaultValue("-9") String dateId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getImages::storeId={}, dateId={}", storeId,dateId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setStoreId(storeId);
            inputObject.setVisitDate(dateId);
            return restS2PAction.getImages(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getImages----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoreImages")
    public Snap2BuyOutput getProjectStoreImages(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoreImages::projectId={}, storeId={}, taskId={}", projectId,storeId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            return restS2PAction.getProjectStoreImages(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectStoreImages----------------\n");
            return rio;
        }
    }

    @GET
      @Produces({MediaType.APPLICATION_JSON})
      @Path("/getStores")
      public Snap2BuyOutput getStores(
            @QueryParam(ParamMapper.RETAILER_CHAIN_CODE) @DefaultValue("-9") String retailerChainCode,
            @QueryParam(ParamMapper.STATE_CODE) @DefaultValue("-9") String stateCode,
            @QueryParam(ParamMapper.CITY) @DefaultValue("-9") String city,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStores::retailerChainCode={}, stateCode={}, city={}", retailerChainCode,stateCode,city);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setRetailerChainCode(retailerChainCode);
            inputObject.setStateCode(stateCode);
            inputObject.setCity(city);
            return restS2PAction.getStores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("retailerChainCode",retailerChainCode);
            inputList.put("stateCode",stateCode);
            inputList.put("city",city);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStores----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getDistributionLists")
    public Snap2BuyOutput getDistributionLists(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getDistributionLists----------------\n");
        try {
            return restS2PAction.getDistributionLists();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getDistributionLists----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/doDistributionCheck")
    public Snap2BuyOutput doDistributionCheck(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @QueryParam(ParamMapper.LIST_ID) @DefaultValue("-9") String listId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts doDistributionCheck::doDistributionCheck::listId={}, imageUUID={}", listId, imageUUID);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setListId(listId);
            inputObject.setImageUUID(imageUUID);
            return restS2PAction.doDistributionCheck(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("listId",listId);
            inputList.put("imageUUID",imageUUID);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends doDistributionCheck----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/doBeforeAfterCheck")
    public Snap2BuyOutput doBeforeAfterCheck(
            @QueryParam(ParamMapper.PREV_IMAGE_UUID) @DefaultValue("-9") String prevImageUUID,
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts doBeforeAfterCheck::imageUUID-1={}, imageUUID-2={}", prevImageUUID,imageUUID);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setPrevImageUUID(prevImageUUID);
            inputObject.setImageUUID(imageUUID);
            return restS2PAction.doBeforeAfterCheck(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("prevImageUUID",prevImageUUID);
            inputList.put("imageUUID",imageUUID);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends doBeforeAfterCheck----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getImageMetaData")
    public Snap2BuyOutput getImageMetaData(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getImageMetaData::imageUUID-1={}",imageUUID);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setImageUUID(imageUUID);
            return restS2PAction.getImageMetaData(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("imageUUID",imageUUID);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getImageMetaData----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/doShareOfShelfAnalysis")
    public Snap2BuyOutput doShareOfShelfAnalysis(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUIDCsvString,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts doShareOfShelfAnalysis::imageUUID={}",imageUUIDCsvString);
        try {

            InputObject inputObject = new InputObject();
            inputObject.setImageUUIDCsvString(imageUUIDCsvString);
            return restS2PAction.doShareOfShelfAnalysis(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("imageUUIDCsvString", imageUUIDCsvString);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends doShareOfShelfAnalysis----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateLatLong")
    public Snap2BuyOutput updateLatLong(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @QueryParam(ParamMapper.LATITUDE) @DefaultValue("-9") String latitude,
            @QueryParam(ParamMapper.LONGITUDE) @DefaultValue("-9") String longitude,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateLatLong::imageUUID={}, latitude={}, longitude={}",imageUUID,latitude,longitude);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setImageUUID(imageUUID);
            inputObject.setLatitude(latitude);
            inputObject.setLongitude(longitude);
            return restS2PAction.updateLatLong(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("retailerChainCode",imageUUID);
            inputList.put("stateCode",latitude);
            inputList.put("city",longitude);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateLatLong----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({"text/csv"})
    @Path("/getShelfAnalysisCsv")
    public Response getShelfAnalysisCsv(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getShelfAnalysisCsv----------------\n");
        try {
            File f = restS2PAction.getShelfAnalysisCsv();
            Response.ResponseBuilder r = Response.ok((Object) f);
            r.header("Content-Disposition", "attachment; filename= ShelfAnalysis .csv");
            return r.build();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getShelfAnalysisCsv----------------\n");
            return Response.serverError().build();
        }
    }

    @GET
    @Produces({"text/csv"})
    @Path("/doShareOfShelfAnalysisCsv")
    public Response doShareOfShelfAnalysisCsv(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUIDCsvString,
            @QueryParam(ParamMapper.RETAILER) @DefaultValue("-9") String retailer,
            @QueryParam(ParamMapper.STATE) @DefaultValue("-9") String state,
            @QueryParam(ParamMapper.CITY) @DefaultValue("-9") String city,
            @QueryParam(ParamMapper.STREET) @DefaultValue("-9") String street,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts doShareOfShelfAnalysisCsv----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setImageUUIDCsvString(imageUUIDCsvString);
            inputObject.setState(state);
            inputObject.setCity(city);
            inputObject.setRetailer(retailer);
            inputObject.setStreet(street);

            File f = restS2PAction.doShareOfShelfAnalysisCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            r.header("Content-Disposition", "attachment; filename= shareOfShelfAnalysis.csv");
            return r.build();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends doShareOfShelfAnalysisCsv----------------\n");
            return Response.serverError().build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listProjectType")
    public Snap2BuyOutput listProjectType(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listProjectType----------------\n");
        try {
            return restS2PAction.listProjectType();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listProjectType----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listSkuType")
    public Snap2BuyOutput listSkuType(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listSkuType----------------\n");
        try {
            return restS2PAction.listSkuType();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listSkuType----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listProject")
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
            if (StringUtils.isNotBlank(platform) && ( platform.equals("android") || platform.equals("ios") )) {
                inputObject.setSource("app");
            } else {
                inputObject.setSource("web");
            }

            return restS2PAction.listProject(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);

            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("Controller Ends listProject");
            return output;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listCategory")
    public Snap2BuyOutput listCategory(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listCategory----------------\n");
        try {
            return restS2PAction.listCategory();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listCategory----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listCustomer")
    public Snap2BuyOutput listCustomer(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listCustomer----------------\n");
        try {
            return restS2PAction.listCustomer();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listCustomer----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listRetailer")
    public Snap2BuyOutput listRetailer(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listRetailer----------------\n");
        try {
            return restS2PAction.listRetailer();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listRetailer----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listProjectUpc")
    public Snap2BuyOutput listProjectUpc(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listProjectUpc ProjectId= {}",projectId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.listProjectUpc(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listProjectUpc----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getCategoryDetail")
    public Snap2BuyOutput getCategoryDetail(
            @QueryParam(ParamMapper.ID) @DefaultValue("-9") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCategoryDetail::id::={}",id);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setId(id);
            return restS2PAction.getCategoryDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("id",id);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getCategoryDetail----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getCustomerDetail")
    public Snap2BuyOutput getCustomerDetail(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCustomerDetail::code::={}",customerCode);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            return restS2PAction.getCustomerDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("customerCode",customerCode);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getCustomerDetail----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectDetail")
    public String getProjectDetail(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectDetail::projectId::={}",projectId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectDetail(inputObject);

        } catch (Exception e) {
        	
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId+"");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectDetail----------------\n");
            return output;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectTypeDetail")
    public Snap2BuyOutput getProjectTypeDetail(
            @QueryParam(ParamMapper.ID) @DefaultValue("-9") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectTypeDetail::id={}",id);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setId(id);
            return restS2PAction.getProjectTypeDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("id",id);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectTypeDetail----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getSkuTypeDetail")
    public Snap2BuyOutput getSkuTypeDetail(
            @QueryParam(ParamMapper.ID) @DefaultValue("-9") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getSkuTypeDetail::id::={}",id);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setId(id);
            return restS2PAction.getSkuTypeDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("id",id);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getSkuTypeDetail----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectUpcDetail")
    public Snap2BuyOutput getProjectUpcDetail(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectUpcDetail::projectId::={}",projectId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectUpcDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectUpcDetail----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getRetailerDetail")
    public Snap2BuyOutput getRetailerDetail(
            @QueryParam(ParamMapper.RETAILER_CODE) @DefaultValue("-9") String retailerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getRetailerDetail::retailerCode::={}",retailerCode);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setRetailerCode(retailerCode);
            return restS2PAction.getRetailerDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("retailerCode",retailerCode);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getRetailerDetail----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createCustomer")
    public Snap2BuyOutput createCustomer(
            JAXBElement<Customer> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createCustomer----------------\n");
        try {

            Customer customerInput = p.getValue();

            LOGGER.info("---------------Controller  customerInput::={}",customerInput.toString());

            return restS2PAction.createCustomer(customerInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createCustomer----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createCategory")
    public Snap2BuyOutput createCategory(
            JAXBElement<Category> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createCategory----------------\n");
        try {

            Category categoryInput = p.getValue();

            LOGGER.info("---------------Controller  categoryInput={}",categoryInput.toString());

            return restS2PAction.createCategory(categoryInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createCategory----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createRetailer")
    public Snap2BuyOutput createRetailer(
            JAXBElement<Retailer> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createRetailer----------------\n");
        try {

            Retailer retailerInput = p.getValue();

            LOGGER.info("---------------Controller  retailerInput={}",retailerInput.toString());

            return restS2PAction.createRetailer(retailerInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createRetailer----------------\n");
            return rio;
        }
    }
    @POST
     @Produces({MediaType.APPLICATION_JSON})
     @Path("/createProjectType")
     public Snap2BuyOutput createProjectType(
            JAXBElement<ProjectType> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createProjectType----------------\n");
        try {

            ProjectType projectTypeInput = p.getValue();

            LOGGER.info("---------------Controller  projectTypeInput = {}",projectTypeInput.toString());

            return restS2PAction.createProjectType(projectTypeInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createProjectType----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createSkuType")
    public Snap2BuyOutput createSkuType(
            JAXBElement<SkuType> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createSkuType----------------\n");
        try {

            SkuType skuTypeInput = p.getValue();

            LOGGER.info("---------------Controller  projectTypeInput::={}",skuTypeInput.toString());

            return restS2PAction.createSkuType(skuTypeInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createSkuType----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createProject")
    public Snap2BuyOutput createProject(
            JAXBElement<Project> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createProject----------------\n");
        try {

            Project projectInput = p.getValue();

            projectInput.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            LOGGER.info("---------------Controller  projectInput::={}, CustomerCode:{} ",projectInput, projectInput.getCustomerCode());

            return restS2PAction.createProject(projectInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createProject----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createUpc")
    public Snap2BuyOutput createUpc(
            JAXBElement<ProductMaster> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createUpc----------------\n");
        try {

            ProductMaster upcInput = p.getValue();

            LOGGER.info("---------------Controller  upcInput::={}",upcInput.toString());

            return restS2PAction.createUpc(upcInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createUpc----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/addUpcToProjectId")
    public Snap2BuyOutput addUpcToProjectId(
            JAXBElement<ProjectUpc> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts addUpcToProjectId----------------\n");
        try {
            ProjectUpc projectUpc = p.getValue();
            LOGGER.info("---------------Controller  projectInput::={}",projectUpc.toString());
            return restS2PAction.addUpcToProjectId(projectUpc);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends addUpcToProjectId----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/generateAggs")
    public Snap2BuyOutput generateAggs(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts generateAggs----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            return restS2PAction.generateAggs(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId",storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends generateAggs----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoreResults")
    public String getProjectStoreResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoreResults----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setMonth(month);
            inputObject.setTaskId(taskId);
            
            String platform = null != request.getAttribute("platform")
                    ? request.getAttribute("platform").toString().trim().toLowerCase() : "";

            if (StringUtils.isNotBlank(platform) && ( platform.equals("android") || platform.equals("ios") )) {
                inputObject.setSource("app");
            } else {
                inputObject.setSource("web");
            }
            
            LOGGER.info("---------------Controller Ends getProjectStoreResults----------------\n");
            return restS2PAction.getProjectStoreResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            input.put("storeId",storeId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectTopStores")
    public Snap2BuyOutput getProjectTopStores(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LIMIT) @DefaultValue("-9") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectTopStores----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLimit(limit);
            return restS2PAction.getProjectTopStores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId",limit);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectTopStores----------------\n");
            return rio;
        }
    }

    //distinct upc count for non zero facing
    //then sum of facing and sum of confidence

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBottomStores")
    public Snap2BuyOutput getProjectBottomStores(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LIMIT) @DefaultValue("-9") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBottomStores----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLimit(limit);
            return restS2PAction.getProjectBottomStores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId",limit);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectBottomStores----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listStores")
    public Snap2BuyOutput listStores(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listStores----------------\n");
        try {
            return restS2PAction.listStores();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listStores----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createStore")
    public Snap2BuyOutput createStore(
            JAXBElement<StoreMaster> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createStore----------------\n");
        try {
            StoreMaster storeMaster = p.getValue();
            storeMaster.setStoreId(metaService.generateStoreId());
            return restS2PAction.createStore(storeMaster);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createStore----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateStore")
    public Snap2BuyOutput updateStore(
            JAXBElement<StoreMaster> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateStore----------------\n");
        try {
            StoreMaster storeMaster = p.getValue();
            return restS2PAction.updateStore(storeMaster);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateStore----------------\n");
            return rio;
        }
    }
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateProject")
    public Snap2BuyOutput updateProject(
            JAXBElement<Project> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateProject----------------\n");
        try {

            Project projectInput = p.getValue();

            LOGGER.info("---------------Controller  projectInput::={}",projectInput.toString());

            projectInput.setCustomerCode(null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString() : "");

            return restS2PAction.updateProject(projectInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateProject----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectSummary")
    public String getProjectSummary(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectSummary::id::={}",projectId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);

            return restS2PAction.getProjectSummary(inputObject);

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getStoreDetail")
    public Snap2BuyOutput getStoreDetail(
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreDetail::id::={}",storeId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setStoreId(storeId);
            return restS2PAction.getStoreDetail(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("storeId",storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoreDetail----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoresWithNoUPCs")
    public String getProjectStoresWithNoUPCs(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoresWithNoUPCs::projectId={}", projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectStoresWithNoUPCs(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectStoresWithNoUPCs----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllStoreImages")
    public String getProjectAllStoreImages(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreImages::projectId = {}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectAllStoreImages(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);
             
             Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectAllStoreImages----------------\n");
            return output;
        }
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoresWithDuplicateImages")
    public String getProjectStoresWithDuplicateImages (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoresWithDuplicateImages::projectId={}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectStoresWithDuplicateImages(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectStoresWithDuplicateImages----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/generateStoreVisitResults")
    public Snap2BuyOutput generateStoreVisitResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts generateStoreVisitResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            return restS2PAction.generateStoreVisitResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId", projectId);
            inputList.put("storeId",storeId);
            inputList.put("taskId",taskId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends generateStoreVisitResults----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllStoreResults")
    public Snap2BuyOutput getProjectAllStoreResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            return restS2PAction.getProjectAllStoreResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectAllStoreResults----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({"text/csv"})
    @Path("/getProjectAllStoreResultsCsv")
    public Response getProjectAllStoreResultsCsv(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreResultsCsv----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));

            File f = restS2PAction.getProjectAllStoreResultsCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_StoreResults_" + date + ".csv");
            LOGGER.info("---------------Controller Ends getProjectAllStoreResultsCsv----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }

    @GET
    @Produces({"text/csv"})
    @Path("/getProjectAllStoreResultsDetailCsv")
    public Response getProjectAllStoreResultsDetailCsv(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreResultsDetailCsv----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            File f = restS2PAction.getProjectAllStoreResultsDetailCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_StoreResultsDetail_" + date + ".csv");
            LOGGER.info("---------------Controller Ends getProjectAllStoreResultsDetailCsv----------------\n");
            return r.build();

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/createUser")
    public Snap2BuyOutput createUser(
            JAXBElement<User> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createUser----------------\n");
        try {

            User userInput = p.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }
            LOGGER.info("---------------Controller  userInput::={}", userInput.toString());

            return restS2PAction.createUser(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends createUser----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateUser")
    public Snap2BuyOutput updateUser(
            JAXBElement<User> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateUser----------------\n");
        try {

            User userInput = p.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller  userInput::={}",userInput.toString());

            return restS2PAction.updateUser(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateUser----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateUserPassword")
    public Snap2BuyOutput updateUserPassword(
            JAXBElement<User> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateUserPassword----------------\n");
        try {

            User userInput = p.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller  userInput::={}",userInput.toString());

            return restS2PAction.updateUserPassword(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateUserPassword----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getUserDetail")
    public Snap2BuyOutput getUserDetail(
            @QueryParam(ParamMapper.USER_ID) @DefaultValue("-9") String userId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getUserDetail::userId::={}",userId);
        try {

            userId = userId.trim().toLowerCase();
            return restS2PAction.getUserDetail(userId);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("userId",userId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getUserDetail----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/deleteUser")
    public Snap2BuyOutput deleteUser(
            JAXBElement<User> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts deleteUser----------------\n");
        try {

            User userInput = p.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }
            LOGGER.info("---------------Controller  userInput::={}",userInput.toString());

            return restS2PAction.deleteUser(userInput.getUserId());

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends deleteUser----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login")
    public Snap2BuyOutput login(
            JAXBElement<User> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts login request----------------\n");
        try {

            User userInput = p.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }
            LOGGER.info("---------------Controller  login request for user ::={}",userInput.getUserId());

            return restS2PAction.login(userInput.getUserId(), userInput.getPassword());

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends login request----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/recomputeProjectByStoreVisit")
    public Snap2BuyOutput recomputeProjectByStoreVisit(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @QueryParam(ParamMapper.GRANULARITY) @DefaultValue("-9") String granularity,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts recomputeProjectByStoreVisit----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            inputObject.setGranularity(granularity);
            return restS2PAction.recomputeProjectByStoreVisit(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId",storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends recomputeProjectByStoreVisit----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/reprocessProjectByStore")
    public Snap2BuyOutput reprocessProjectByStore(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts reprocessProjectByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            return restS2PAction.reprocessProjectByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId",storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends reprocessProjectByStore----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/updateProjectResultStatus")
    public Snap2BuyOutput updateProjectResultStatus(
            String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateProjectResultStatus----------------\n");
        try {
        	LOGGER.debug("---------------Controller Starts updateProjectResultStatus :: input string : {}", input);
        
            return restS2PAction.updateProjectResultStatus(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateProjectResultStatus----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/disableAutomation")
    public Snap2BuyOutput disableAutomation(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts disableAutomation ----------------\n");
            String content = "-1";
            File file = new File("/root/autoOnOffCheck/autoOnOffCheck.txt");
            FileOutputStream fop = new FileOutputStream(file,false);

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            System.out.println("Done");

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("new value",content);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends disableAutomation----------------\n");
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends disableAutomation----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/enableAutomation")
    public Snap2BuyOutput enableAutomation(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts enableAutomation ----------------\n");
            String content = "1";
            File file = new File("/root/autoOnOffCheck/autoOnOffCheck.txt");
            FileOutputStream fop = new FileOutputStream(file, false);

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
            LOGGER.info("---------------Write to file Done---------------");

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("new value",content);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends enableAutomation----------------\n");
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends enableAutomation----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/bulkUploadProjectImages")
    public Snap2BuyOutput bulkUploadProjectImages(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.SYNC) @DefaultValue("false") String sync,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts bulkUploadProjectImages----------------\n");
        String filenamePath = "";
        try {
       	
            //Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.", name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    filenamePath = env.getProperty("disk_directory")+ "upload/"+projectId+"/"+item.getName();

                    LOGGER.info("BulkUploadProjectImage GCS uploaded path: {}", filenamePath);
                    File uploadedFile = new File(filenamePath);
                    if (!uploadedFile.exists()) {
                        uploadedFile.getParentFile().mkdirs();
                        uploadedFile.getParentFile().setReadable(true);
                        uploadedFile.getParentFile().setWritable(true);
                        uploadedFile.getParentFile().setExecutable(true);
                    }
                    item.write(uploadedFile);
                }
            }
            
            if ( filenamePath != null ) {
            	restS2PAction.bulkUploadProjectImages(Integer.valueOf(projectId),sync,filenamePath);
            }

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("projectId", projectId);
            inputList.put("sync", sync);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends bulkUploadProjectImages---Upload will begin in async mode----------------\n");
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("projectId", projectId);
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/getUploadStatus")
    public Snap2BuyOutput getUploadStatus(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts getUploadStatus ----------------\n");
            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("projectId",projectId);

            List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
            LinkedHashMap<String, String> map = new LinkedHashMap<String,String>();
            map.put("status",UploadStatusTracker.get(projectId));
            resultList.add(map);
            
            rio = new Snap2BuyOutput(resultList, inputList);
            LOGGER.info("---------------Controller Ends getUploadStatus----------------\n");
            return rio;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getUploadStatus----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getRepPerformance")
    public Snap2BuyOutput getRepPerformance(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getRepPerformance----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            return restS2PAction.getRepPerformance(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("customerCode",customerCode);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getRepPerformance----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getRepPerformanceByProject")
    public Snap2BuyOutput getRepPerformanceByProject(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.AGENT_ID) @DefaultValue("-9") String agentId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getRepPerformanceByProject----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setAgentId(agentId);
            return restS2PAction.getRepPerformanceByProject(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("customerCode",customerCode);
            inputList.put("agentId",agentId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getRepPerformanceByProject----------------\n");
            return rio;
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getRepPerformanceByProjectStore")
    public Snap2BuyOutput getRepPerformanceByProjectStore(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.AGENT_ID) @DefaultValue("-9") String agentId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getRepPerformanceByProjectStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setAgentId(agentId);
            return restS2PAction.getRepPerformanceByProjectStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("agentId", agentId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getRepPerformanceByProjectStore----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/loadPremiumData")
    public Snap2BuyOutput loadPremiumData(
            @QueryParam(ParamMapper.IMAGE_STATUS) @DefaultValue("-9") String imageStatus,
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts loadPremiumData ----------------\n");

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setImageStatus(imageStatus);
            return restS2PAction.loadPremiumData(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("imageStatus",imageStatus);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends loadPremiumData----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({"text/csv"})
    @Path("/getProjectAllStoreImageResultsCsv")
    public Response getProjectAllStoreImageResultsCsv(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STATUS) @DefaultValue("") String status,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreImageResultsCsv----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStatus(status);

            File f = restS2PAction.getProjectAllStoreImageResultsCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_StoreImageResults_" + date + ".csv");
            LOGGER.info("---------------Controller Ends getProjectAllStoreImageResultsCsv----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/computeImageResults")
    public Snap2BuyOutput computeImageResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts computeImageResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.computeImageResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends computeImageResults----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/uploadPremiumResults")
    public Snap2BuyOutput uploadPremiumResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.ASSESSMENT_ID) @DefaultValue("-9") String assessmentId ,
            @QueryParam(ParamMapper.SEND) @DefaultValue("false") String sendToPrm,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts uploadPremiumResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setAssessmentId(assessmentId);
            inputObject.setSendToDestination(sendToPrm);
            return restS2PAction.uploadPremiumResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends uploadPremiumResults----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllStoreImageResults")
    public String getProjectAllStoreImageResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STATUS) @DefaultValue("") String status,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreImageResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStatus(status);
            return restS2PAction.getProjectAllStoreImageResults(inputObject);

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
           LOGGER.info("---------------Controller Ends getProjectAllStoreImageResults----------------\n");
           return output;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/updateProjectImageResultStatus")
    public Snap2BuyOutput updateProjectImageResultStatus(
            String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
        	LOGGER.debug("---------------Controller Starts updateProjectImageResultStatus :: input string : {}", input);
        
            return restS2PAction.updateProjectImageResultStatus(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateProjectImageResultStatus----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/loadNewQuestionPremium")
    public Snap2BuyOutput loadNewQuestionPremium(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.QUESTION_ID) @DefaultValue("-9") String questionId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts loadNewQuestionPremium ----------------\n");

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setQuestionId(questionId);
            return restS2PAction.loadNewQuestionPremium(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("questionId",questionId);


            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends loadNewQuestionPremium----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/changeProjectImageStatus")
    public Snap2BuyOutput changeProjectImageStatus(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.CURRENT_IMAGE_STATUS) @DefaultValue("-9") String currentImageStatus,
            @QueryParam(ParamMapper.NEW_IMAGE_STATUS) @DefaultValue("-9") String newImageStatus,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts changeProjectImageStatus ----------------\n");

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setCurrentImageStatus(currentImageStatus);
            inputObject.setNewImageStatus(newImageStatus);

            return restS2PAction.changeProjectImageStatus(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("currentImageStatus",currentImageStatus);
            inputList.put("newImageStatus",newImageStatus);


            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends changeProjectImageStatus----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/uploadSurveyImageResults")
    public String uploadSurveyImageResults(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.EXTERNAL_PROJECT_ID) @DefaultValue("-9") String externalProjectId,
            @QueryParam(ParamMapper.SEND) @DefaultValue("false") String sendToSurvey,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts uploadSurveyImageResults ----------------\n");

            if ( Boolean.parseBoolean(sendToSurvey) ) {
            	if ( !"SRV".equals(customerCode) ) {
            		throw new IllegalArgumentException("This API is only applicable for customerCode SRV");
            	}
            }
            
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setExternalProjectId(externalProjectId);
            inputObject.setSendToDestination(sendToSurvey);

            return restS2PAction.uploadSurveyImageResults(inputObject);

        } catch (Exception e) {
        	LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        } finally {
            LOGGER.info("---------------Controller Ends uploadSurveyImageResults----------------\n");
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/uploadSurveyStoreVisitResults")
    public String uploadSurveyStoreVisitResults(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.EXTERNAL_PROJECT_ID) @DefaultValue("-9") String externalProjectId,
            @QueryParam(ParamMapper.SEND) @DefaultValue("false") String sendToSurvey,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts uploadSurveyStoreVisitResults ----------------\n");

            if ( Boolean.parseBoolean(sendToSurvey) ) {
            	if ( !"SRV".equals(customerCode) ) {
            		throw new IllegalArgumentException("This API is only applicable for customerCode SRV");
            	}
            }
            
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setExternalProjectId(externalProjectId);
            inputObject.setSendToDestination(sendToSurvey);
            
            return restS2PAction.uploadSurveyStoreVisitResults(inputObject);

        } catch (Exception e) {
        	LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        } finally {
            LOGGER.info("---------------Controller Ends uploadSurveyStoreVisitResults----------------\n");
        }
    }

    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/loadSurveyData")
    public Snap2BuyOutput loadSurveyData(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.EXTERNAL_PROJECT_ID) @DefaultValue("-9") String externalProjectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.info("---------------Controller Starts loadSurveyData ----------------\n");
            
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setExternalProjectId(externalProjectId);
            
            return restS2PAction.loadSurveyData(inputObject);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("externalProjectId",externalProjectId);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends loadSurveyData----------------\n");
            return rio;
        } finally {
            LOGGER.info("---------------Controller Ends loadSurveyData----------------\n");
        }
    }
    
    @GET
    @Produces({"text/csv"})
    @Path("/getProjectStoresWithDuplicateImagesCsv")
    public Response getProjectStoresWithDuplicateImagesCsv (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoresWithDuplicateImagesCsv::ProjectId={}", projectId );
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            File f = restS2PAction.getProjectStoresWithDuplicateImagesCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_StoresWithDuplicateImages_" + date + ".csv");
            LOGGER.info("---------------Controller Ends getProjectStoresWithDuplicateImagesCsv----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllStoreScores")
    public Snap2BuyOutput getProjectAllStoreScores(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.SCORE_ID) @DefaultValue("-9") String scoreId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllStoreScores----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setScoreId(scoreId);
            inputObject.setLevel(level);
            inputObject.setValue(value);
            return restS2PAction.getProjectAllStoreScores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectAllStoreScores----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoreScores")
    public String getProjectStoreScores (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
    	LOGGER.info("---------------Controller Starts getProjectStoreScores::projectId={}, storeId={}", projectId,storeId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            return restS2PAction.getProjectStoreScores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectStoreScores----------------\n");
            return output;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandShares")
    public String getProjectBrandShares (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.ROLLUP) @DefaultValue("brand") String rollup,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBrandSummary::projectId={}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setMonth(month);
            inputObject.setRollup(rollup);
            return restS2PAction.getProjectBrandShares(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSharesNew")
    public String getProjectBrandSharesNew (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @QueryParam(ParamMapper.ROLLUP) @DefaultValue("brand") String rollup,
            @QueryParam(ParamMapper.RETAILER) @DefaultValue("all") String retailer,
            @QueryParam(ParamMapper.MODULAR) @DefaultValue("all") String modular,
            @QueryParam(ParamMapper.SUB_CATEGORY) @DefaultValue("all") String subCategory,
            @QueryParam(ParamMapper.STORE_FORMAT) @DefaultValue("all") String storeFormat,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBrandSharesNew::projectId={}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setMonth(month);
            inputObject.setRollup(rollup);
            inputObject.setRetailer(retailer);
            inputObject.setModular(modular);
            inputObject.setSubCategory(subCategory);
            inputObject.setWaveId(waveId);
            inputObject.setStoreFormat(storeFormat);
            
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getProjectBrandSharesNew(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSummary")
    public String getProjectBrandSummary (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBrandSummary::projectId={}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            return restS2PAction.getProjectBrandSummary(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSummaryNew")
    public String getProjectBrandSummaryNew (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBrandSummaryNew::projectId={}",projectId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().toLowerCase().trim() : "");
            
            return restS2PAction.getProjectBrandSummaryNew(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSummaryNew----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSharesAllStores")
    public String getProjectBrandSharesAllStores (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.ROLLUP) @DefaultValue("brand") String rollup,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
      	LOGGER.info("---------------Controller Starts getProjectBrandSharesAllStores::projectId={}, rollup={}, month={}",projectId,rollup,month);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setMonth(month);
            inputObject.setRollup(rollup);
            
            return restS2PAction.getProjectBrandSharesAllStores(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSharesAllStores----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSharesAllStates")
    public String getProjectBrandSharesAllStates (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.ROLLUP) @DefaultValue("brand") String rollup,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
      	LOGGER.info("---------------Controller Starts getProjectBrandSharesAllStates::projectId={}, rollup={}, month={}",projectId,rollup,month);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setMonth(month);
            inputObject.setRollup(rollup);
            
            return restS2PAction.getProjectBrandSharesAllStates(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSharesAllStates----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectBrandSharesAllCities")
    public String getProjectBrandSharesAllCities (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.ROLLUP) @DefaultValue("brand") String rollup,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
      	LOGGER.info("---------------Controller Starts getProjectBrandSharesAllCities::projectId={}, rollup{}, month={}",projectId,rollup,month);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setMonth(month);
            inputObject.setRollup(rollup);
                        
            return restS2PAction.getProjectBrandSharesAllCities(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSharesAllCities----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectDistributionSummary")
    public String getProjectDistributionSummary (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
      	LOGGER.info("---------------Controller Starts getProjectDistributionSummary::projectId={}, waveId={}",projectId,waveId);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setWaveId(waveId);
            
            return restS2PAction.getProjectDistributionSummary(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectDistributionSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoreDistribution")
    public String getProjectStoreDistribution(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoreDistribution----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            LOGGER.info("---------------Controller Ends getProjectStoreDistribution----------------\n");
            return restS2PAction.getProjectStoreDistribution(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            input.put("storeId",storeId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
        		List<Map<String,String>> emptyOutputList = new ArrayList<>();
        		emptyOutputList.add(emptyOutput);
        		CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        		//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"})
    @Path("/getStoreLevelDistributionReport")
    public Response getStoreLevelDistributionReport(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreLevelDistributionReport----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));

            File f = restS2PAction.getStoreLevelDistributionReport(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_StoreLevelDistribution_" + date + ".xlsx");
            LOGGER.info("---------------Controller Ends getStoreLevelDistributionReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"})
    @Path("/getProjectBrandProductsReport")
    public Response getProjectBrandProductsReport (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.FROM_MONTH) @DefaultValue("-9") String fromMonth,
            @QueryParam(ParamMapper.TO_MONTH) @DefaultValue("-9") String toMonth,
            @QueryParam(ParamMapper.FROM_WAVE) @DefaultValue("-9") String fromWave,
            @QueryParam(ParamMapper.TO_WAVE) @DefaultValue("-9") String toWave,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectBrandProductsReport::projectId={}, fromMonth={}, toMonth={}, fromWave={}, toWave={}",projectId,fromMonth,toMonth,fromWave,toWave);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setFromMonth(fromMonth);
            inputObject.setToMonth(toMonth);
            inputObject.setFromWave(fromWave);
            inputObject.setToWave(toWave);
            
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");

            File f = restS2PAction.getProjectBrandProductsReport(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_ProjectBrandProducts_" + date + ".xlsx");
            LOGGER.info("---------------Controller Ends getProjectBrandProductsReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getImageData")
    public String getListForShelfAnalysis() {
    	return restS2PAction.getListForShelfAnalysis();
	}
    
    /**
     * API to get New Photos for Project
     * @param customerCode
     * @param status
     * @param request
     * @param response
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/photosForPremium")
    public String getProjectsPhotos(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.STATUS) @DefaultValue("-9") String status,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
    	LOGGER.info("---------------Controller Start getPhotosForPremium----------------\n");
    	try {
    		List<LinkedHashMap<String, String>> premiumPhotosMap = restS2PAction.getProjectPhotosByCustomerCodeAndStatus(customerCode, status);  
    		
    		Map<String, String> input = new HashMap<String, String>();
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>(premiumPhotosMap);
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
    		Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends GetProjectPhotosForPremium----------------\n");
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
             
            Map<String, String> emptyOutput = new HashMap<String, String>();
         	emptyOutput.put("Message", "No Data Returned");
         	List<Map<String,String>> emptyOutputList = new ArrayList<>();
         	emptyOutputList.add(emptyOutput);
         	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
         	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends GetProjectPhotosForPremium----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getCustomerProjectSummary")
    public Snap2BuyOutput getCustomerProjectSummary(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCustomerProjectSummary CustomerCode = {}",customerCode);
        try {
            //TODO need to think about it
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setStatus("1");
            LOGGER.info("---------------Controller Ends getCustomerProjectSummary----------------\n");
            return restS2PAction.getCustomerProjectSummary(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("customerCode",customerCode);
            inputList.put("status","1");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getCustomerProjectSummary----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getStoreDetails")
    public Snap2BuyOutput getStoreDetailsByCustomerProjectId(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.CUSTOMER_PROJECT_ID) @DefaultValue("-9") String customerProjectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreDetailsByCustomerProjectId projectId = {}",projectId);
        try {
        	InputObject inputObject = new InputObject();
        	inputObject.setProjectId(Integer.valueOf(projectId));
        	inputObject.setCustomerProjectId(customerProjectId);
            return restS2PAction.getStoreDetailsByCustomerProjectId(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoreDetailsByCustomerProjectId----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/updateStoreDetails")
    public String addUpdateStoreDetails(
            @QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode,
            @QueryParam(ParamMapper.STATUS) @DefaultValue("-9") String status,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Start addUpdateStoreDetails----------------\n");
        try {
            restS2PAction.saveStoreDetails(customerCode, status);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "API execution has been started in background.");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, new ArrayList<Map<String,String>>());
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends addUpdateStoreDetails----------------\n");
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("STORE_DETAILS_EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends addUpdateStoreDetails----------------\n");
            return output;
        }
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("/displayProjectStoreVisitImage")
    public StreamingOutput displayProjectStoreVisitImage(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @QueryParam(ParamMapper.IMAGE_TYPE) @DefaultValue("-9") String imageType,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts displayProjectStoreVisitImage projectId::={}, imageType",projectId,imageType);
        String filePath;
        String defaultFilePath = null;
        if(projectId.equalsIgnoreCase("-9") || storeId.equalsIgnoreCase("-9")) {
            filePath="/usr/share/s2i/image_not_available/photo_not_available.png";
        } else {
            Map<String, String> result = processImageDao.getProjectStoreResultByCustomerCodeAndProjectId(Integer.valueOf(projectId),storeId, taskId);
            switch(imageType.toLowerCase()){
                case "stitched":filePath= result.get("stitchedImagePath") != null ? result.get("stitchedImagePath") :"/usr/share/s2i/image_not_available/original.png";
                    defaultFilePath="/usr/share/s2i/image_not_available/original.png";
                    break;
                case "heatmap":filePath= result.get("subCategoryHeatMapPath") != null ? result.get("subCategoryHeatMapPath") :"/usr/share/s2i/image_not_available/thumbnail.png";
                    defaultFilePath="/usr/share/s2i/image_not_available/original.png";
                    break;
                default:filePath= result.get("stitchedImagePath") != null ? result.get("stitchedImagePath") : "/usr/share/s2i/image_not_available/original.png";
                    defaultFilePath="/usr/share/s2i/image_not_available/original.png";
            }
        }
        File file = new File(filePath);
        if (file.exists()){
            LOGGER.info("---------------filepath ={} exists", filePath);
        }else{
            filePath = defaultFilePath;
        }

        String finalFilePath = filePath;
        LOGGER.info("---------------filepath selected is :finalFilePath={}",finalFilePath);
        StreamingOutput streamingOutput =   new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                File outputFile = new File(finalFilePath);

                InputStream in = new FileInputStream(outputFile);
                byte[] buffer = new byte[8192];
                int chunk=0;
                while ((chunk = in.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, chunk);
                    outputStream.flush();
                }

                outputStream.close();
            }
        };
        return streamingOutput;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateImageAnalysis")
    public Snap2BuyOutput updateUPCForImageAnalysis(String inputJSON) {
        LOGGER.info("---------------Controller Starts updateUPCForImageAnalysis {}",inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            JSONArray jsonArray = new JSONArray(jsonObject.get("imageAnalysisNew").toString());
            JSONObject imageAnalysisNew;
            for (int i = 0; i < jsonArray.length(); i++) {
                if(null != jsonArray.get(i)){
                    imageAnalysisNew = (JSONObject)jsonArray.get(i);
                    if(imageAnalysisNew.get("action").toString().equalsIgnoreCase("NEW")) {
                        restS2PAction.addImageAnalysisNew(imageAnalysisNew.get("upc").toString(), imageAnalysisNew.get("imageUUID").toString(),
                                imageAnalysisNew.get("leftTopX").toString(), imageAnalysisNew.get("leftTopY").toString(),
                                imageAnalysisNew.get("shelfLevel").toString(),
                                Integer.valueOf(imageAnalysisNew.get("projectId").toString()), imageAnalysisNew.get("storeId").toString(),
                                imageAnalysisNew.get("height").toString(), imageAnalysisNew.get("width").toString(), imageAnalysisNew.get("price").toString(),
                                imageAnalysisNew.get("promotion").toString(), imageAnalysisNew.get("compliant").toString());
                    } else if(imageAnalysisNew.get("action").toString().equalsIgnoreCase("DELETE")) {
                        restS2PAction.deleteImageAnalysisNew(imageAnalysisNew.get("id").toString());
                    } else if(imageAnalysisNew.get("action").toString().equalsIgnoreCase("DELETE_ALL")) {
                        restS2PAction.deleteAllDetectionsByImageUUID(imageAnalysisNew.get("imageUUID").toString());
                    } else {
                        restS2PAction.updateUPCForImageAnalysis(imageAnalysisNew.get("upc").toString(), imageAnalysisNew.get("id").toString(),
                                imageAnalysisNew.get("imageUUID").toString(), imageAnalysisNew.get("leftTopX").toString(),
                                imageAnalysisNew.get("leftTopY").toString(), imageAnalysisNew.get("shelfLevel").toString(),
                                imageAnalysisNew.get("price").toString(), imageAnalysisNew.get("promotion").toString(),
                                imageAnalysisNew.get("compliant").toString());
                    }
                }
            }

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("Total Updated",jsonArray.length()+"");

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Data updated sucessfully");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateUPCForImageAnalysis----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login/activateUser")
    public Snap2BuyOutput activateUser(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts activateUser----------------\n");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller  userInput::= {}", userInput.toString());

            return restS2PAction.activateUser(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends activateUser----------------\n");
            return rio;
        }
    }


    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login/resendActivationCode")
    public Snap2BuyOutput resendActivationCode(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts resendActivationCode----------------\n");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller  userInput::= {}", userInput.toString());

            return restS2PAction.resendActivationCode(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends resendActivationCode----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login/updateEmail")
    public Snap2BuyOutput changeEmail(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts changeEmail----------------\n");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller  userInput::= {}", userInput.toString());

            return restS2PAction.changeEmail(userInput);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends changeEmail----------------\n");
            return rio;
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login/forgotPassword")
    public Snap2BuyOutput forgotPassword(String inputJSON) {
        LOGGER.info("---------------Controller forgotPassword {}", inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);

            String userId = jsonObject.get("userId").toString().toLowerCase().trim();

            return restS2PAction.forgotPasswordForDevice(userId);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends forgotPassword----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/login/resetPassword")
    public Snap2BuyOutput resetPassword(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts resetPassword----------------\n");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("---------------Controller resetPassword userInput::= {}", userInput.toString());

            return restS2PAction.resetPassword(userInput);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends resetPassword----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/saveProjectImage")
    public Snap2BuyOutput saveProjectImage(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,

            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts saveProjectImage----------------\n");
        try {

            UUID uniqueKey = UUID.randomUUID();
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));

            //Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            String result = "";
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.", name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    FileInputStream fileInputStream = (FileInputStream) item.getInputStream();

                    cloudStorageService.storeImage( "projectimages", projectId, fileInputStream);

                    String filenamePath = env.getProperty("disk_directory") + "projectimages/" + projectId + ".jpg";
                    inputObject.setImageFilePath(filenamePath);

                    File uploadedFile = new File(filenamePath);

                    if (!uploadedFile.exists()) {
                        uploadedFile.getParentFile().mkdirs();
                    }
                    uploadedFile.getParentFile().setReadable(true);
                    uploadedFile.getParentFile().setWritable(true);
                    uploadedFile.getParentFile().setExecutable(true);
                    uploadedFile.setWritable(true);
                    uploadedFile.setReadable(true);
                    uploadedFile.setExecutable(true);
                    item.write(uploadedFile);
                    inputObject.setImageFilePath(filenamePath);
                }
            }
            LOGGER.info("---------------Controller Starts saveProjectImage with details {}", inputObject);

            return restS2PAction.saveProjectImage(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("projectId", projectId);
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller SaveProjectImage Ends----------------\n");
            return rio;
        }
    }


    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("/displayProjectImage")
    public StreamingOutput displayProjectImage(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts displayProjectImage projectId:{}", projectId);

        String filePath;
        String defaultFilePath = "/usr/share/s2i/image_not_available/original.png";

        if(projectId.equalsIgnoreCase("-9")) {
            filePath="/usr/share/s2i/image_not_available/photo_not_available.png";
        } else {
            Map<String, String> result = restS2PAction.getProjectByCustomerCodeAndCustomerProjectId(Integer.valueOf(projectId));
            filePath = (result.get("imagePath") != null && !result.get("imagePath").equalsIgnoreCase("0")) ? result.get("imagePath") :"/usr/share/s2i/image_not_available/original.png";
        }

        File file = new File(filePath);
        if (file.exists()){
            LOGGER.info("---------------filepath = {} exists", filePath);
        }else{
            filePath = defaultFilePath;
        }

        String finalFilePath = filePath;
        LOGGER.info("---------------filepath selected is finalFilePath: {}",finalFilePath);
        StreamingOutput streamingOutput =   new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                File outputFile = new File(finalFilePath);

                InputStream in = new FileInputStream(outputFile);
                byte[] buffer = new byte[8192];
                int chunk=0;
                while ((chunk = in.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, chunk);
                    outputStream.flush();
                }

                outputStream.close();
            }
        };
        return streamingOutput;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateStorePlaceId")
    public Snap2BuyOutput updateStorePlaceId(
            @QueryParam(ParamMapper.RETAILER_CHAIN_CODE) @DefaultValue("-9") String retailerChainCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateStorePlaceId RetailerChainCode:{}", retailerChainCode);

        return restS2PAction.updateStorePlaceId(retailerChainCode);
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
                   
        	return restS2PAction.getGeoMappedStoresByUserId(customerCode,userId);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoresByUserId----------------\n");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/saveUPCImage")
    public Snap2BuyOutput saveUPCImage(
            @QueryParam(ParamMapper.UPC) @DefaultValue("-9") String upc,

            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts saveUPCImage----------------\n");
        Snap2BuyOutput rio;
        HashMap<String, String> inputList = new HashMap<String, String>();

        inputList.put("upc", upc);
        try {

            UUID uniqueKey = UUID.randomUUID();

            //Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            String result = "";
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.", name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    FileInputStream fileInputStream = (FileInputStream) item.getInputStream();

                    String filenamePath = cloudStorageService.storeImage("upc", upc, fileInputStream);
                }
            }
            LOGGER.info("---------------Controller Ends saveUPCImage with details {}", upc);
            rio = new Snap2BuyOutput(null, inputList);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller saveUpcImage Ends----------------\n");
        }
        return rio;
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("/displayUPCImage")
    public StreamingOutput displayUPCImage(
            @QueryParam(ParamMapper.UPC) @DefaultValue("-9") String upc,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts displayUPCImage UPC: {}", upc);

        String filePath = env.getProperty("disk_directory") + "upc/" + upc + ".jpg";
        String defaultFilePath = "/usr/share/s2i/image_not_available/original.png";

        if(upc.equalsIgnoreCase("-9")) {
            filePath="/usr/share/s2i/image_not_available/photo_not_available.png";
        }

        File file = new File(filePath);
        if (file.exists()){
            LOGGER.info("---------------filepath = {} exists", filePath);
        }else{
            filePath = defaultFilePath;
        }

        String finalFilePath = filePath;
        LOGGER.info("---------------filepath selected is :finalFilePath:{}",finalFilePath);
        StreamingOutput streamingOutput =   new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                File outputFile = new File(finalFilePath);

                InputStream in = new FileInputStream(outputFile);
                byte[] buffer = new byte[8192];
                int chunk=0;
                while ((chunk = in.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, chunk);
                    outputStream.flush();
                }

                outputStream.close();
            }
        };
        return streamingOutput;
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/bulkUploadStores")
    public Snap2BuyOutput bulkUploadStores(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts bulkUploadStores----------------\n");
        String filenamePath = "";
        try {
       	
            //Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.",name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    filenamePath = env.getProperty("disk_directory") + "upload/stores/"+item.getName()+System.currentTimeMillis();
                    File uploadedFile = new File(filenamePath);
                    if (!uploadedFile.exists()) {
                        uploadedFile.getParentFile().mkdirs();
                        uploadedFile.getParentFile().setReadable(true);
                        uploadedFile.getParentFile().setWritable(true);
                        uploadedFile.getParentFile().setExecutable(true);
                    }
                    item.write(uploadedFile);
                }
            }
            
            Snap2BuyOutput uploadResponse  = new Snap2BuyOutput();
            if ( filenamePath != null ) {
            	uploadResponse =  restS2PAction.bulkUploadStores(filenamePath);
            }
            LOGGER.info("---------------Controller Ends bulkUploadStores----------------\n");
            return uploadResponse;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends bulkUploadStores----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectAllStoreResults")
    public Snap2BuyOutput getAltriaProjectAllStoreResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectAllStoreResults----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            return restS2PAction.getAltriaProjectAllStoreResults(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("level",level);
            inputList.put("value",value);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getAltriaProjectAllStoreResults----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/listChildProjects")
    public Snap2BuyOutput listChildProjects(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts listChildProjects----------------\n");

        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            return restS2PAction.listChildProjects(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends listChildProjects----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getCategoryReviewComments")
    public String getCategoryReviewComments(
            @QueryParam(ParamMapper.CATEGORY_ID) @DefaultValue("-9") String categoryId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCategoryReviewComments----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setCategoryId(categoryId);
            return restS2PAction.getCategoryReviewComments(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectBrandSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectSummary")
    public String getAltriaProjectSummary(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectSummary----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectSummary(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectSummary----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectBrandShares")
    public String getAltriaProjectBrandShares(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectBrandShares----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectBrandShares(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectBrandShares----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectBrandSharesByStore")
    public String getAltriaProjectBrandSharesByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectBrandSharesByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectBrandSharesByStore(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getAltriaProjectBrandSharesByStore----------------\n");
             return output;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectBrandAvailability")
    public String getAltriaProjectBrandAvailability(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectBrandAvailability----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"1","2"}));
            
            return restS2PAction.getAltriaProjectBrandAvailability(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getAltriaProjectBrandAvailability----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectPromoInitiativeAvailability")
    public String getAltriaProjectPromoInitiativeAvailability(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectPromoInitiativeAvailability----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            //Rest of the execution is same as brand availability, just SKU type changes.
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"4","5"}));

            return restS2PAction.getAltriaProjectBrandAvailability(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getAltriaProjectPromoInitiativeAvailability----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectBrandAvailabilityByStore")
    public String getAltriaProjectBrandAvailabilityByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectBrandAvailabilityByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"1", "2"}));
            
            return restS2PAction.getAltriaProjectBrandAvailabilityByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectBrandAvailabilityByStore----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectPromoInitiativeAvailabilityByStore")
    public String getAltriaProjectPromoInitiativeAvailabilityByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectPromoInitiativeAvailabilityByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            //Rest of the execution is same as brand availability, just SKU type changes.
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"4","5"}));
            
            return restS2PAction.getAltriaProjectBrandAvailabilityByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectPromoInitiativeAvailabilityByStore----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectProductAvailability")
    public String getAltriaProjectProductAvailability(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectProductAvailability----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"1"}));

            return restS2PAction.getAltriaProjectProductAvailability(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectProductAvailability----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectPOSAvailability")
    public String getAltriaProjectPOSAvailability(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectPOSAvailability----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            //Rest of the execution is same as product availability, just SKU type changes.
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"4"}));

            return restS2PAction.getAltriaProjectProductAvailability(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getAltriaProjectProductAvailability----------------\n");
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectProductAvailabilityByStore")
    public String getProjectProductAvailabilityByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectProductAvailabilityByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"1"}));
            
            return restS2PAction.getAltriaProjectProductAvailabilityByStore(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getProjectProductAvailabilityByStore----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectPOSAvailabilityByStore")
    public String getProjectPOSAvailabilityByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectPOSAvailabilityByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            //Rest of the execution is same as product availability, just SKU type changes.
            inputObject.setSkuTypeIds(Arrays.asList(new String[] {"4"}));
            
            return restS2PAction.getAltriaProjectProductAvailabilityByStore(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getProjectPOSAvailabilityByStore----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectWarningSignAvailability")
    public String getAltriaProjectWarningSignAvailability(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectWarningSignAvailability----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectWarningSignAvailability(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getAltriaProjectWarningSignAvailability----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectWarningSignAvailabilityByStore")
    public String getAltriaProjectWarningSignAvailabilityByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectWarningSignAvailabilityByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.parseInt(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectWarningSignAvailabilityByStore(inputObject);

        } catch (Exception e) {
        	 e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
             Map<String, String> input = new HashMap<String, String>();
             input.put("error in Input","-9");
             List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
             metaList.add(input);

             Map<String, String> emptyOutput = new HashMap<String, String>();
             emptyOutput.put("Message", "No Data Returned");
             List<Map<String,String>> emptyOutputList = new ArrayList<>();
             emptyOutputList.add(emptyOutput);
             CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
             //convert to json here
             Gson gson = new Gson();
             String output = gson.toJson(reportIO);
             LOGGER.info("---------------Controller Ends getAltriaProjectWarningSignAvailabilityByStore----------------\n");
             return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProjectStoreImages")
    public Snap2BuyOutput getAltriaProjectStoreImagesByStore(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaProjectStoreImagesByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getAltriaProjectStoreImagesByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("level",level);
            inputList.put("value",value);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getAltriaProjectStoreImagesByStore----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getStoresForReview")
    public Snap2BuyOutput getAltriaStoresForReview(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.BATCH_ID) @DefaultValue("-9") String batchId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaStoresForReview----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setBatchId(batchId);

            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");

            return restS2PAction.getAltriaStoresForReview(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("level",level);
            inputList.put("value",value);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getAltriaStoresForReview----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/ui/menus")
    public Snap2BuyOutput getMenusForUser(
            @QueryParam(ParamMapper.SOURCE) @DefaultValue("web") String source,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getMenusForUser----------------\n");
        try {
            String customerCode = null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "";
            String userId = null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
            String role = null != request.getAttribute("role")
                    ? request.getAttribute("role").toString() : "";

            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setUserId(userId);
            inputObject.setRole(role);
            inputObject.setSource(source);

            return restS2PAction.getMenusForUser(inputObject);

        } catch (Exception e) {

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getMenusForUser----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getStoreImagesForReview")
    public String getStoreImagesForReview(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreImagesForReview----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);

            return restS2PAction.getAltriaStoreImagesForReview(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("level",level);
            inputList.put("value",value);
            inputList.put("storeId", storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoreImagesForReview----------------\n");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getProductDetectionsForReview")
    public String getProductDetectionsForReview(
            @QueryParam(ParamMapper.IMAGE_UUID) @DefaultValue("-9") String imageUUID,
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProductDetectionsForReview----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setImageUUID(imageUUID);
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);

            return restS2PAction.getProductDetectionsForReview(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("imageUUID", imageUUID);
            inputList.put("level",level);
            inputList.put("value",value);
            inputList.put("storeId", storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProductDetectionsForReview----------------\n");
            return rio.toString();
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/updateProductDetections")
    public Snap2BuyOutput updateProductDetections(String inputJSON) {
        LOGGER.info("---------------Controller Starts updateProductDetections {}", inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            JSONArray skuArray = new JSONArray(jsonObject.get("data").toString());
            JSONObject skuTypeJson;
            SkuTypePOJO skuTypePOJO;
            JSONArray upcArray;
            JSONObject upcObject;
            List<SkuTypePOJO> skuTypeList = new ArrayList<>();
            List<Map<String, String>> upcList;
            Map<String, String> productMap;

            InputObject inputObject = new InputObject();
            inputObject.setImageUUID(jsonObject.getString("imageUUID"));
            inputObject.setProjectId(jsonObject.getInt("projectId"));
            inputObject.setStoreId(jsonObject.getString("storeId"));
            inputObject.setTaskId(jsonObject.getString("taskId"));
            inputObject.setValue(jsonObject.get("dateId").toString());

            for (int i = 0; i < skuArray.length(); i++) {
                if(null != skuArray.get(i)){
                    skuTypeJson = (JSONObject)skuArray.get(i);
                    skuTypePOJO = new SkuTypePOJO();
                    skuTypePOJO.setSkuTypeId(skuTypeJson.optString("skuTypeId"));
                    skuTypePOJO.setSkuType(skuTypeJson.optString("skuType"));

                    upcArray = new JSONArray(skuTypeJson.get("found").toString());

                    upcList = new ArrayList<>();
                    for(int j = 0; j < upcArray.length(); j++) {
                        upcObject = upcArray.getJSONObject(j);
                        productMap = new HashMap<>();
                        productMap.put("upc", upcObject.getString("upc"));
                        productMap.put("name", upcObject.optString("name"));
                        upcList.add(productMap);
                    }
                    skuTypePOJO.setFound(upcList);

                    upcArray = new JSONArray(skuTypeJson.get("notFound").toString());

                    upcList = new ArrayList<>();
                    for(int j = 0; j < upcArray.length(); j++) {
                        upcObject = upcArray.getJSONObject(j);
                        productMap = new HashMap<>();
                        productMap.put("upc", upcObject.getString("upc"));
                        productMap.put("name", upcObject.optString("name"));
                        upcList.add(productMap);
                    }
                    skuTypePOJO.setNotFound(upcList);

                    skuTypeList.add(skuTypePOJO);
                }
            }

            restS2PAction.updateProductDetections(inputObject, skuTypeList);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("Total Updated",skuArray.length()+"");

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Data updated sucessfully");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateProductDetections----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/runAggregation")
    public String runAggregationForAltria(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts runAggregationForAltria----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);

            return restS2PAction.runAggregationForAltria(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("level",level);
            inputList.put("value",value);
            inputList.put("storeId", storeId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends runAggregationForAltria----------------\n");
            return rio.toString();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/getLinearFootageByStore")
    public Snap2BuyOutput getLinearFootageByStore(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getLinearFootageByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);

            return restS2PAction.getLinearFootageByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId", storeId);
            inputList.put("level",level);
            inputList.put("value",value);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getLinearFootageByStore----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/altria/updateLinearFootageByStore")
    public Snap2BuyOutput updateLinearFootageByStore(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @QueryParam(ParamMapper.LINEAR_FOOTAGE) @DefaultValue("-9") String footage,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateLinearFootageByStore----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setStoreId(storeId);
            inputObject.setLinearFootage(footage);

            return restS2PAction.updateLinearFootageByStore(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            inputList.put("storeId", storeId);
            inputList.put("level",level);
            inputList.put("value",value);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateLinearFootageByStore----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"})
    @Path("/altria/getStoreVisitReport")
    public Response getAltriaStoreVisitReport (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getAltriaStoreVisitReport::projectId={}, level={}, value={}"
        		,projectId,level,value);
        try {
        	
        	String customerCode = null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "";
            String userId = null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
                    
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);
            inputObject.setCustomerCode(customerCode);
            inputObject.setUserId(userId);

            File f = restS2PAction.getAltriaStoreVisitReport(inputObject);
            
            Response.ResponseBuilder r = Response.ok((Object) f);
            r.header("Content-Disposition", "attachment; filename= StoreVisitReport_" + value + ".xlsx");
            LOGGER.info("---------------Controller Ends getAltriaStoreVisitReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/listUsers")
    public String listUsers(){
        LOGGER.info("START: ListUsers");
        try {

            return restS2PAction.listUsers();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends listUsers");
            return rio.toString();
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/resetPassword")
    public Snap2BuyOutput userResetPassword(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Starts userResetPassword----------------\n");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("--------------- UserResetPassword userInput::= {}", userInput.toString());

            return restS2PAction.userResetPassword(userInput);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Ends userResetPassword----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/listGeoLevels")
    public String listGeoLevels(@QueryParam(ParamMapper.CUSTOMER_CODE) @DefaultValue("-9") String customerCode){
        LOGGER.info("START: listGeoLevels {}", customerCode);
        try {

            return restS2PAction.listGeoLevels(customerCode);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends listGeoLevels");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/listCustomerCodes")
    public String listCustomerCodes(){
        LOGGER.info("START: listCustomerCodes");
        try {

            return restS2PAction.listCustomerCodes();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends listCustomerCodes");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/listRoles")
    public String listRoles(){
        LOGGER.info("START: listRoles");
        try {

            return restS2PAction.listRoles();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends listRoles");
            return rio.toString();
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/addUser")
    public Snap2BuyOutput userAdd(
            JAXBElement<User> inputUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Starts userAdd");
        try {

            User userInput = inputUser.getValue();

            if(null != userInput.getUserId()) {
                userInput.setUserId(userInput.getUserId().toLowerCase().trim());
            }

            LOGGER.info("userAdd userInput::= {}", userInput.toString());

            return restS2PAction.createUserAndUserGeoMap(userInput);

        } catch (Exception e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Ends userAdd");
            return rio;
        }
    }
    
    @GET
    @Produces({"text/csv"})
    @Path("/premium/report")
    public Response getPremiumReportCsv(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getPremiumReportCsv----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));

            File f = restS2PAction.getPremiumReportCsv(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            r.header("Content-Disposition", "attachment; filename= " + projectId + "_report_" + date + ".csv");
            LOGGER.info("---------------Controller Ends getPremiumReportCsv----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/device/versionInfo")
    public String getDeviceConfiguration(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getDeviceConfiguration");
        try {
            return restS2PAction.getDeviceConfiguration();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getDeviceConfiguration");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/device/screenConfig")
    public Object getUserScreenConfig(
            @QueryParam(ParamMapper.USER_ID) @DefaultValue("-9") String userId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getUserScreenConfig");
        try {
            userId = userId.toLowerCase().trim();
            return restS2PAction.getUserScreenConfig(userId);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("userId", userId);
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getUserScreenConfig");
            return rio.toString();
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/device/screenConfig")
    public Snap2BuyOutput saveUserScreenConfig(String inputJSON) {
        LOGGER.info("Controller saveUserScreenConfig {}", inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            String userId = jsonObject.get("userId").toString().toLowerCase().trim();

            ObjectMapper mapper = new ObjectMapper();
            List<String> screens = mapper.readValue(jsonObject.getJSONArray("screens").toString(), List.class);

            LOGGER.info("User Screens walk through {}", screens);
            return restS2PAction.saveUserScreenConfig(userId, screens);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends saveUserScreenConfig");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/device/appConfig")
    public String getUserAppConfig(
            @QueryParam(ParamMapper.USER_ID) @DefaultValue("-9") String userId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getUserAppConfig");
        try {
            return restS2PAction.getUserAppConfig(userId.toLowerCase().trim());
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("userId", userId);
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getUserAppConfig");
            return rio.toString();
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/device/appConfig")
    public Snap2BuyOutput saveUserAppConfig(String inputJSON) {
        LOGGER.info("Controller saveUserAppConfig {}", inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            String userId = jsonObject.get("userId").toString().toLowerCase().trim();

            if(null != userId && !userId.isEmpty()){
                UserAppConfig userAppConfig = new UserAppConfig();

                userAppConfig.setUserId(userId);
                userAppConfig.setAppVersionCode(jsonObject.getString("appVersionCode"));
                userAppConfig.setAppVersionName(jsonObject.getString("appVersionName"));
                userAppConfig.setCameraResolution(jsonObject.getString("cameraResolution"));
                userAppConfig.setDeviceBrand(jsonObject.getString("deviceBrand"));
                userAppConfig.setDeviceModel(jsonObject.getString("deviceModel"));
                userAppConfig.setDisplayDPI(jsonObject.getString("displayDPI"));
                userAppConfig.setDisplayDensity(jsonObject.getString("displayDensity"));
                userAppConfig.setInstallationDate(jsonObject.getString("installationDate"));
                userAppConfig.setManufacturer(jsonObject.getString("manufacturer"));
                userAppConfig.setOsName(jsonObject.getString("osName"));
                userAppConfig.setScreenResolution(jsonObject.getString("screenResolution"));
                userAppConfig.setSdkVersion(jsonObject.getString("sdkVersion"));
                userAppConfig.setUpdationDate(jsonObject.getString("updationDate"));
                userAppConfig.setPlatform(jsonObject.getString("platform"));

                return restS2PAction.insertOrUpdateUserAppConfig(userAppConfig);
            }else{
                Snap2BuyOutput rio;
                HashMap<String, String> inputList = new HashMap<String, String>();

                inputList.put("userId","UserId not found");

                rio = new Snap2BuyOutput(null, inputList);
                LOGGER.info("Controller Ends saveUserAppConfig");
                return rio;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends saveUserAppConfig");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/globalSettings")
    public Snap2BuyOutput updateGlobalSettings(
            String inputJSON,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts updateGlobalSettings----------------\n");
        try {

        	JSONObject jsonObject = new JSONObject(inputJSON);

            LOGGER.info("---------------Controller  input::= {}", jsonObject.toString());

            return restS2PAction.updateGlobalSettings(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateGlobalSettings----------------\n");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/getRecentStoreVisits")
    public String getRecentStoreVisits(
            @QueryParam(ParamMapper.LIMIT) @DefaultValue("20") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getRecentStoreVisits");
        String userId = null != request.getAttribute("userId")
                ? request.getAttribute("userId").toString().trim().toLowerCase() : "";

        String customerCode = null != request.getAttribute("customerCode")
                ? request.getAttribute("customerCode").toString() : "";
        try {
            InputObject inputObject = new InputObject();
            inputObject.setValue(limit);
            inputObject.setUserId(userId.trim().toLowerCase());
            inputObject.setCustomerCode(customerCode.trim());

            return restS2PAction.getRecentStoreVisits(inputObject);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("userId", userId);
            inputList.put("customerCode", customerCode);
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getRecentStoreVisits");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user/getStoreVisitStatus")
    public String getStoreVisitStatus(
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.PLACE_ID) @DefaultValue("-9") String placeId,
            @QueryParam(ParamMapper.DATE_ID) @DefaultValue("-9") String visitedDate,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getStoreVisitStatus");

        String userId = null != request.getAttribute("userId")
                ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
        try {

            InputObject inputObject = new InputObject();
            inputObject.setUserId(userId.trim().toLowerCase());
            inputObject.setVisitDate(visitedDate);

            if(!storeId.equalsIgnoreCase("-9")) {
                inputObject.setStoreId(storeId);
                return restS2PAction.getStoreVisitStatus(inputObject);
            } else {
                inputObject.setPlaceId(placeId);
                return restS2PAction.getStoreVisitStatusByPlaceId(inputObject);
            }
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("userId", userId);
            inputList.put("storeId", storeId);
            inputList.put("visitDate", visitedDate);
            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getStoreVisitStatus");
            return rio.toString();
        }
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectShareOfShelfByBrand")
    public String getProjectShareOfShelfByBrand (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @QueryParam(ParamMapper.BRAND_NAME) @DefaultValue("-9") String brandName,
            @QueryParam(ParamMapper.SUB_CATEGORY) @DefaultValue("all") String subCategory,
            @QueryParam(ParamMapper.STORE_FORMAT) @DefaultValue("all") String storeFormat,
            @QueryParam("Retailer") @DefaultValue("all") String retailer,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectShareOfShelfByBrand::projectId={},month={},waveId={},brandName={},subCategory={},storeFormat={},retailer={}",
        		projectId,month,waveId,brandName,subCategory,storeFormat,retailer);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setMonth(month);
            inputObject.setBrandName(brandName);
            inputObject.setSubCategory(subCategory);
            inputObject.setWaveId(waveId);
            inputObject.setStoreFormat(storeFormat);
            inputObject.setRetailer(retailer);
            
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");
            
            return restS2PAction.getProjectShareOfShelfByBrand(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectShareOfShelfByBrand----------------\n");
            return output;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllStoreShareOfShelfByBrand")
    public String getProjectAllStoreShareOfShelfByBrand (
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @QueryParam(ParamMapper.BRAND_NAME) @DefaultValue("-9") String brandName,
            @QueryParam(ParamMapper.SUB_CATEGORY) @DefaultValue("all") String subCategory,
            @QueryParam(ParamMapper.STORE_FORMAT) @DefaultValue("all") String storeFormat,
            @QueryParam("Retailer") @DefaultValue("all") String retailer,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
    	LOGGER.info("---------------Controller Starts getProjectAllStoreShareOfShelfByBrand::projectId={},month={},waveId={},brandName={},subCategory={},storeFormat={},retailer={}",
        		projectId,month,waveId,brandName,subCategory,storeFormat,retailer);
        try {
            InputObject inputObject = new InputObject();

            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setMonth(month);
            inputObject.setBrandName(brandName);
            inputObject.setSubCategory(subCategory);
            inputObject.setWaveId(waveId);
            inputObject.setStoreFormat(storeFormat);
            inputObject.setRetailer(retailer);
            
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");
            inputObject.setUserId(null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "");

            return restS2PAction.getProjectAllStoreShareOfShelfByBrand(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("Message", "No Data Returned");
            List<Map<String,String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);
            CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
            //convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectAllStoreShareOfShelfByBrand----------------\n");
            return output;
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/enquiry/add")
    public Snap2BuyOutput addContactEnquiry(String inputJSON) {
        LOGGER.info("Controller addContactEnquiry {}", inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            String firstName = jsonObject.get("firstName").toString();
            String lastName = jsonObject.get("lastName").toString();
            String email = jsonObject.get("email").toString();
            String phone = jsonObject.get("phone").toString();
            String company = jsonObject.get("company").toString();
            String jobProfile = jsonObject.get("jobProfile").toString();
            String note = jsonObject.get("note").toString();

            return restS2PAction.addWebsiteEnquiry(firstName, lastName, email, phone, company, jobProfile, note);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends addContactEnquiry");
            return rio;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/enquiry/list")
    public String getWebsiteEnquiries(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getWebsiteEnquiries");

        try {
            return restS2PAction.getWebsiteEnquiries();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getWebsiteEnquiries");
            return rio.toString();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getBucketPath")
    public String getBucketPath(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts getBucketPath");

        try {
            String bucketPath = cloudStorageService.getBucketPath(true);

            CustomSnap2BuyOutput reportIO = null;

            Map<String, String> reportInput = new HashMap<String, String>();
            List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
            metaList.add(reportInput);

            Map<String, String> emptyOutput = new HashMap<String, String>();
            emptyOutput.put("url", bucketPath);
            List<Map<String, String>> emptyOutputList = new ArrayList<>();
            emptyOutputList.add(emptyOutput);

            reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);

            Gson gson = new Gson();
            String output = gson.toJson(reportIO);

            LOGGER.info("Controller End getBucketPath");

            return output;

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends getBucketPath");
            return rio.toString();
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateFCMToken")
    public Snap2BuyOutput updateFCMToken (String inputJSON,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts updateFCMToken::fcmToken={}", inputJSON);

        JSONObject jsonObject = new JSONObject(inputJSON);
        String fcmToken = jsonObject.get("fcmToken").toString().trim();
        String userId = null != request.getAttribute("userId")
                ? request.getAttribute("userId").toString().trim().toLowerCase() : "";

        String platform = null != request.getAttribute("platform")
                ? request.getAttribute("platform").toString().trim().toLowerCase() : "";
        try {

            if(!userId.isEmpty()){
                return restS2PAction.updateFCMToken(userId.trim().toLowerCase(),
                        fcmToken.trim(), platform.trim().toLowerCase());
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);            

        }
        Snap2BuyOutput rio;
        HashMap<String, String> inputList = new HashMap<String, String>();

        inputList.put("error in Input","-9");
        inputList.put("fcmToken", fcmToken);
        inputList.put("userId", userId);
        rio = new Snap2BuyOutput(null, inputList);
        LOGGER.info("Controller Ends updateFCMToken");
        return rio;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/saveProjectRepResponses")
    public Snap2BuyOutput saveProjectRepResponses(
            JAXBElement<ProjectRepResponse> p,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts saveProjectRepResponses::projectResponse = {}", p);
        try {

            ProjectRepResponse projectRepResponseInput = p.getValue();

            String agentId = null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
            String customerCode = null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "";

            if ( StringUtils.isBlank(projectRepResponseInput.getVisitDate()) ) {
            	Date date = new Date();
                DateFormat format = new SimpleDateFormat("yyyyMMdd");
                format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                String visitDate = format.format(date);
                projectRepResponseInput.setVisitDate(visitDate);
            }
                    
            projectRepResponseInput.setAgentId(agentId);
            
            projectRepResponseInput.setCustomerCode(customerCode);

            return restS2PAction.saveProjectRepResponses(projectRepResponseInput);
         
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends saveProjectRepResponses");
            return rio;
        }
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/ui/menus")
    public Snap2BuyOutput saveUIMenus(

            JAXBElement<CustomerRoleMenuMap> customerRoleMenuMapJAXBElement,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("Controller Starts saveUIMenus");
        try {

            CustomerRoleMenuMap customerRoleMenuMap = customerRoleMenuMapJAXBElement.getValue();

            LOGGER.info("Controller saveUIMenus::={}", customerRoleMenuMap.toString());

            return restS2PAction.saveUIMenus(customerRoleMenuMap);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("Controller Ends saveUIMenus");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectAllScoreSummary")
    public String getProjectAllScoreSummary(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.LEVEL) @DefaultValue("-9") String level,
            @QueryParam(ParamMapper.VALUE) @DefaultValue("-9") String value,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectAllScoreSummary::id::={}",projectId);
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLevel(level);
            inputObject.setValue(value);

            return restS2PAction.getProjectAllScoreSummary(inputObject);

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            LOGGER.info("---------------Controller Ends getProjectAllScoreSummary----------------\n");
            return output;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/updateImageQualityParams")
    public Snap2BuyOutput updateImageQualityParams(
            String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
        	LOGGER.debug("---------------Controller Starts updateImageQualityParams :: input string : {}", input);
        
            return restS2PAction.updateImageQualityParams(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateImageQualityParams----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getNotUsableImageCommentOptions")
    public String getNotUsableImageCommentOptions(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getNotUsableImageCommentOptions--------------");
        Map<String, String> input = new HashMap<String, String>();
        List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
        metaList.add(input);
        
        List<String> options = Arrays.asList(new String[] {"Blurry", "Too Far", "Low Contrast", "Angled", "Incorrect Photo"});
        
        CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(options, metaList);
       
        //convert to json here
        Gson gson = new Gson();
        String output = gson.toJson(reportIO);
        return output;
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/updateStoreReviewStatus")
    public Snap2BuyOutput updateStoreReviewStatus(
            String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
        	LOGGER.debug("---------------Controller Starts updateStoreReviewStatus :: input string : {}", input);
        
            return restS2PAction.updateStoreReviewStatus(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateStoreReviewStatus----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoreRepResponses")
    public Snap2BuyOutput getProjectStoreRepResponses(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoreRepResponses----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            return restS2PAction.getProjectStoreRepResponses(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectStoreRepResponses----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getProjectStoresForReview")
    public Snap2BuyOutput getProjectStoresForReview(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @QueryParam(ParamMapper.FROM_DATE) @DefaultValue("-9") String fromDate,
            @QueryParam(ParamMapper.TO_DATE) @DefaultValue("-9") String toDate,
            @QueryParam(ParamMapper.REVIEW_STATUS) @DefaultValue("-9") String reviewStatus,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoresForReview----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setWaveId(waveId);
            inputObject.setFromDate(fromDate);
            inputObject.setToDate(toDate);
            inputObject.setReviewStatus(reviewStatus);

            return restS2PAction.getProjectStoresForReview(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getProjectStoresForReview----------------\n");
            return rio;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/bulkUploadPremiumResults")
    public Snap2BuyOutput bulkUploadPremiumResults(
    		String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts bulkUploadPremiumResults----------------\n");
        try {
            return restS2PAction.bulkUploadPremiumResults(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends bulkUploadPremiumResults----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getPremiumBulkUploadStatus")
    public Snap2BuyOutput getPremiumBulkUploadStatus(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getPremiumBulkUploadStatus----------------\n");
        try {
            return restS2PAction.getPremiumBulkUploadStatus();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getPremiumBulkUploadStatus----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({"text/csv"})
    @Path("/integrations/reports/distributionReport")
    public Response getStoreLevelDistributionCSVReport(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.FROM_DATE) @DefaultValue("-9") String fromDate,
            @QueryParam(ParamMapper.TO_DATE) @DefaultValue("-9") String toDate,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreDistributionCSVReport----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setFromDate(fromDate);
            inputObject.setToDate(toDate);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");

            String userId = null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString() : "";
            if ( !userId.trim().toLowerCase().equals("driveline@snap2insight.com") ) {
            	throw new Exception("Invalid Access");
            }
            
            File f = restS2PAction.getStoreLevelDistributionCSVReport(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            
            String downloadFileName = projectId+"_StoreDistribution_"+fromDate.replace("/", "-")+"_"+toDate.replace("/", "-")+".csv";
            r.header("Content-Disposition", "attachment; filename= " + downloadFileName);
            LOGGER.info("---------------Controller Ends getStoreDistributionCSVReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces({"text/csv"})
    @Path("/integrations/reports/storeImageReport")
    public Response getStoreLevelImageCSVReport(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.FROM_DATE) @DefaultValue("-9") String fromDate,
            @QueryParam(ParamMapper.TO_DATE) @DefaultValue("-9") String toDate,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreLevelImageCSVReport----------------\n");
        try {

            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setFromDate(fromDate);
            inputObject.setToDate(toDate);
            inputObject.setCustomerCode(null != request.getAttribute("customerCode")
                    ? request.getAttribute("customerCode").toString() : "");

            String userId = null != request.getAttribute("userId")
                    ? request.getAttribute("userId").toString() : "";
            if ( !userId.trim().toLowerCase().equals("driveline@snap2insight.com") ) {
            	throw new Exception("Invalid Access");
            }
            
            File f = restS2PAction.getStoreLevelImageCSVReport(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            
            String downloadFileName = projectId+"_StoreImages_"+fromDate.replace("/", "-")+"_"+toDate.replace("/", "-")+".csv";
            r.header("Content-Disposition", "attachment; filename= " + downloadFileName);
            LOGGER.info("---------------Controller Ends getStoreLevelImageCSVReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/premium/getJobDetails")
    public String getPremiumJobDetails (
            @QueryParam("jobId") @DefaultValue("-9") String jobId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getPremiumJobDetails::jobId={}",jobId);
        try {
        	String prmJobDetailsResponse = restS2PAction.getPremiumJobDetails(jobId);
            //File f = restS2PAction.getPremiumJobDetails(jobId);
            
            //Response.ResponseBuilder r = Response.ok((Object) f);
            //r.header("Content-Disposition", "attachment; filename= Premium_JobDefinition_" + jobId + ".xlsx");
            LOGGER.info("---------------Controller Ends getPremiumJobDetails----------------\n");
            return prmJobDetailsResponse;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return "{}";
        }
    }
    
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"})
    @Path("/integrations/reports/homePanel")
    public Response getHomePanelProjectStatusReport(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getHomePanelProjectStatusReport----------------\n");
        try {

            File f = restS2PAction.getHomePanelProjectStatusReport();
            Response.ResponseBuilder r = Response.ok((Object) f);
            
            String downloadFileName = "HomePanel-DataCollection-Status.xlsx";
            r.header("Content-Disposition", "attachment; filename= " + downloadFileName);
            LOGGER.info("---------------Controller Ends getHomePanelProjectStatusReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/updateDuplicateDetections")
    public Snap2BuyOutput updateDuplicateDetections(String inputJSON) {
        LOGGER.info("---------------Controller Starts updateDuplicateDetections {}",inputJSON);
        try {
            JSONObject jsonObject = new JSONObject(inputJSON);
            JSONArray jsonArray = (JSONArray) jsonObject.get("ids");
            List<Long> detectionIds = new ArrayList<Long>();
            for (int i = 0; i < jsonArray.length(); i++) {
            	detectionIds.add(jsonArray.getLong(i));
            }
            
            restS2PAction.updateDuplicateDetections(detectionIds);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("Updated",jsonArray.length()+"");

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Data updated sucessfully");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateUPCForImageAnalysis----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/internal/project/status")
    public String getInternalProjectStatus(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.WAVE_ID) @DefaultValue("-9") String waveId,
            @QueryParam(ParamMapper.LIMIT) @DefaultValue("5") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getInternalProjectStatus----------------\n");
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setWaveId(waveId);
            inputObject.setLimit(limit);

            return restS2PAction.getInternalProjectStatus(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("projectId",projectId);
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getInternalProjectStatus----------------\n");
            return rio.toString();
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/internal/imageAnalysis/resolveGroupUpcs")
    public Snap2BuyOutput resolveGroupUpcs(
            String input,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
        	LOGGER.debug("---------------Controller Starts resolveGroupUpcs :: input string : {}", input);
        
            return restS2PAction.resolveGroupUpcs(input);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends resolveGroupUpcs----------------\n");
            return rio;
        }
    }
    
    
    /*
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/internal/imageAnalysis/{imageUUID}")
    public Snap2BuyOutput saveImageAnalysisData(
            @PathParam(ParamMapper.IMAGE_UUID) String imageUUID,
            String imageAnalysisData,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        try {
            LOGGER.debug("---------------Controller Starts saveImageAnalysisData :: imageUUID : {}, imageAnalysisData : {}", imageUUID, imageAnalysisData);
            
            return restS2PAction.saveImageAnalysisData(imageUUID,imageAnalysisData);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends saveImageAnalysisData----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/internal/imageAnalysis/externalProcessing/images")
    public Snap2BuyOutput getNextImagesToProcessExternally(
    		@QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
    		@QueryParam(ParamMapper.LIMIT) @DefaultValue("1") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getNextImagesToProcessExternally----------------\n");
        try {
        	InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setLimit(limit);

        	return restS2PAction.getNextImagesToProcessExternally(inputObject);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getNextImagesToProcessExternally----------------\n");
            return rio;
        }
    }*/
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getStoreComplianceResults")
    public String getProjectStoreComplianceResults(
            @QueryParam(ParamMapper.PROJECT_ID) @DefaultValue("-9") String projectId,
            @QueryParam(ParamMapper.STORE_ID) @DefaultValue("-9") String storeId,
            @QueryParam(ParamMapper.MONTH) @DefaultValue("-9") String month,
            @QueryParam(ParamMapper.TASK_ID) @DefaultValue("-9") String taskId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getProjectStoreComplianceResults----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setMonth(month);
            inputObject.setTaskId(taskId);
            
            String platform = null != request.getAttribute("platform")
                    ? request.getAttribute("platform").toString().trim().toLowerCase() : "";

            if (StringUtils.isNotBlank(platform) && ( platform.equals("android") || platform.equals("ios") )) {
                inputObject.setSource("app");
            } else {
                inputObject.setSource("web");
            }
            
            String mockdataFile = "pog-compliance-mockdata.sql";
    		String data = "";
    		
    		if ( projectId.equals("1607") ) {
    			data = ConverterUtil.getPepsiPOGFormattedResult(inputObject);
    		} else {
    			data = ConverterUtil.getResourceFromClasspath(mockdataFile, "/mockdata/");
    		}
            
            LOGGER.info("---------------Controller Ends getProjectStoreComplianceResults----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            input.put("storeId",storeId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/geomapping")
    public String getITGGeoMappingByUser(
    		@Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGGeoMappingByUser----------------\n");
        
        try {
            
            String userId = null != request.getAttribute("userId") ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
            String customerCode = null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "";
            
            InputObject inputObject = new InputObject();
            inputObject.setCustomerCode(customerCode);
            inputObject.setUserId(userId);

            String mappingData = restS2PAction.getITGGeoMappingForUser(inputObject); 
            
            LOGGER.info("---------------Controller Ends getITGGeoMappingByUser----------------\n");
            return mappingData;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/stats")
    public String getITGStats(
    		@QueryParam(ParamMapper.PROJECT_ID) String projectId,
    		@QueryParam("geoLevel") @DefaultValue("-9") String geoLevel,
            @QueryParam("geoLevelId") @DefaultValue("-9") String geoLevelId,
            @QueryParam("timePeriodType") @DefaultValue("-9") String timePeriodType,
            @QueryParam("timePeriod") @DefaultValue("-9") String timePeriod,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStats----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setGeoLevel(geoLevel);
            inputObject.setGeoLevelId(geoLevelId);
            inputObject.setTimePeriod(timePeriod);
            inputObject.setTimePeriodType(timePeriodType);
            
            String customerCode = null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "";
            String userId = null != request.getAttribute("userId") ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
            inputObject.setCustomerCode(customerCode);
            inputObject.setUserId(userId);
            
            String output = restS2PAction.getITGStats(inputObject);
            
            LOGGER.info("---------------Controller Ends getITGStats----------------\n");
            return output;

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/stores")
    public String getITGStoresWithFilters(
    		@QueryParam(ParamMapper.PROJECT_ID) String projectId,
    		@QueryParam("geoLevel") @DefaultValue("-9") String geoLevel,
            @QueryParam("geoLevelId") @DefaultValue("-9") String geoLevelId,
            @QueryParam("timePeriodType") @DefaultValue("-9") String timePeriodType,
            @QueryParam("timePeriod") @DefaultValue("-9") String timePeriod,
            @QueryParam("limit") @DefaultValue("-9") String limit,
            String filters,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStoresWithFilters----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setGeoLevel(geoLevel);
            inputObject.setGeoLevelId(geoLevelId);
            inputObject.setTimePeriod(timePeriod);
            inputObject.setTimePeriodType(timePeriodType);
            inputObject.setLimit("100");
            inputObject.setValue(filters);
            
            String customerCode = null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "";
            inputObject.setCustomerCode(customerCode);
            
            String output = restS2PAction.getITGStoresWithFilters(inputObject);
            
            LOGGER.info("---------------Controller Ends getITGStoresWithFilters----------------\n");
            return output;

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/store")
    public String getITGStoreDetails(
    		@QueryParam(ParamMapper.PROJECT_ID) String projectId,
    		@QueryParam(ParamMapper.STORE_ID) String storeId,
            @QueryParam("timePeriodType") @DefaultValue("-9") String timePeriodType,
            @QueryParam("timePeriod") @DefaultValue("-9") String timePeriod,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStoreDetails----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setStoreId(storeId);
            inputObject.setTimePeriodType(timePeriodType);
            inputObject.setTimePeriod(timePeriod);
            
            String data = restS2PAction.getITGStoreDetails(inputObject);
            LOGGER.info("---------------Controller Ends getITGStoreDetails----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"})
    @Path("/itg/report")
    public Response getITGReport(
    		@QueryParam(ParamMapper.PROJECT_ID) String projectId,
    		@QueryParam("geoLevel") @DefaultValue("Region") String geoLevel,
            @QueryParam("geoLevelId") @DefaultValue("20") String geoLevelId,
            @QueryParam("timePeriodType") @DefaultValue("-9") String timePeriodType,
            @QueryParam("timePeriod") @DefaultValue("-9") String timePeriod,
            @QueryParam("limit") @DefaultValue("-9") String limit,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGReport----------------\n");
        try {

        	InputObject inputObject = new InputObject();
            inputObject.setProjectId(Integer.valueOf(projectId));
            inputObject.setGeoLevel(geoLevel);
            inputObject.setGeoLevelId(geoLevelId);
            inputObject.setTimePeriod(timePeriod);
            inputObject.setTimePeriodType(timePeriodType);
            inputObject.setLimit(limit);
            
            inputObject.setCustomerCode(null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString().trim().toLowerCase() : "");
            inputObject.setUserId(null != request.getAttribute("userId") ? request.getAttribute("userId").toString().trim().toLowerCase() : "");

            File f = restS2PAction.getITGReport(inputObject);
            Response.ResponseBuilder r = Response.ok((Object) f);
            r.header("Content-Disposition", "attachment; filename= ITG_StoreVisit_Report_For_" + geoLevel + "_" + geoLevelId + "_" + timePeriod + ".xlsx");
            LOGGER.info("---------------Controller Ends getITGReport----------------\n");
            return r.build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            return Response.serverError().build();
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/help")
    public String getITGHelpStrings(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGHelpStrings----------------\n");
        
        try {
            String mockdataFile = "";
            
    		String mockData = ConverterUtil.getResourceFromClasspath(mockdataFile, "/help/itg_dashboard.json");
            
            LOGGER.info("---------------Controller Ends getITGHelpStrings----------------\n");
            return mockData;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/dailyAggregation/{projectId}/{visitDateId}")
    public String doITGDailyAggregation(
    		@PathParam("projectId") int projectId,
    		@PathParam("visitDateId") String visitDateId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts doITGDailyAggregation----------------\n");
        
        try {
        	restS2PAction.doDailyITGAggregation(projectId, visitDateId);
            
            LOGGER.info("---------------Controller Ends doITGDailyAggregation----------------\n");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/stores")
    public String getITGStoreVisitsToReview(
    		@QueryParam(ParamMapper.DATE_ID) String visitDateId,
    		@QueryParam(ParamMapper.PROJECT_ID) int projectId,
    		@QueryParam("bucketId") String bucketId,
    		@QueryParam(ParamMapper.STORE_ID) String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStoreVisitsToReview----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setVisitDate(visitDateId);
            inputObject.setProjectId(projectId);
            inputObject.setValue(bucketId);
            inputObject.setStoreId(storeId);
            
            String data = restS2PAction.getITGStoreVisitsToReview(inputObject);
            LOGGER.info("---------------Controller Ends getITGStoreVisitsToReview----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("dateId",visitDateId);
            input.put("projectId",""+projectId);
            input.put("bucketId",bucketId);
            input.put("storeId",storeId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/images")
    public String getITGStoreVisitImagesToReview(
    		@QueryParam(ParamMapper.PROJECT_ID) int projectId,
    		@QueryParam(ParamMapper.STORE_ID) String storeId,
    		@QueryParam(ParamMapper.TASK_ID) String taskId,
    		@QueryParam(ParamMapper.SHOW_ALL) String showAll,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStoreVisitImagesToReview----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(projectId);
            inputObject.setStoreId(storeId);
            inputObject.setTaskId(taskId);
            inputObject.setShowAll(showAll);
            
            String data = restS2PAction.getITGStoreVisitImagesToReview(inputObject);
            LOGGER.info("---------------Controller Ends getITGStoreVisitImagesToReview----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",""+projectId);
            input.put("storeId",storeId);
            input.put("taskId",taskId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/store")
    public Snap2BuyOutput updateStoreVisitAggregationData(String jsonPayload) {
        LOGGER.info("---------------Controller Starts updateStoreVisitAggregationData {}",jsonPayload);
        try {

        	restS2PAction.updateITGStoreVisitAggregationData(jsonPayload);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("Status","success");

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Data updated sucessfully");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateStoreVisitAggregationData----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/comments")
    public String getITGStoreVisitReviewComments(
    		@QueryParam(ParamMapper.PROJECT_ID) int projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGStoreVisitReviewComments----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(projectId);
            
            String data = restS2PAction.getITGStoreVisitReviewComments(inputObject);
            LOGGER.info("---------------Controller Ends getITGStoreVisitReviewComments----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",""+projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/brands")
    public String getITGProductBrands(
    		@QueryParam(ParamMapper.PROJECT_ID) int projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGProductBrands----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(projectId);
            
            String data = restS2PAction.getITGProductBrands(inputObject);
            LOGGER.info("---------------Controller Ends getITGProductBrands----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",""+projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/review/stats/summary")
    public String getITGReviewStatsSummary(
    		@QueryParam(ParamMapper.PROJECT_ID) int projectId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getITGReviewStatsSummary----------------\n");
        
        try {
            InputObject inputObject = new InputObject();
            inputObject.setProjectId(projectId);
            
            String data = restS2PAction.getITGReviewStatsSummary(inputObject);
            LOGGER.info("---------------Controller Ends getITGReviewStatsSummary----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("projectId",""+projectId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/internal/storeAnalysis")
    public Snap2BuyOutput saveStoreLevelData(String inputJSON) {
        LOGGER.info("---------------Controller Starts saveStoreLevelData {}",inputJSON);
        try {
            
            restS2PAction.saveStoreLevelData(inputJSON);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("Updated","?");

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Data received");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends saveStoreLevelData----------------\n");
            return rio;
        }
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/internal/imageAnalysis/{imageUUID}")
    public Snap2BuyOutput updateImageAnalysisStatus(
    		@PathParam(ParamMapper.IMAGE_UUID) String imageUUID,
    		String inputJSON) {
        LOGGER.info("---------------Controller Starts updateImageAnalysisStatus for imageUUID {} with {}",imageUUID, inputJSON);
        try {
            
            restS2PAction.updateImageAnalysisStatus(imageUUID, inputJSON);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();
            inputList.put("imageUUID",imageUUID);

            HashMap<String, String> reportInput = new HashMap<String, String>();
            reportInput.put("Message", "Status updated sucessfully");

            rio = new Snap2BuyOutput(null, inputList);
            return rio;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends updateImageAnalysisStatus----------------\n");
            return rio;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/support/search")
    public String getSupportInfo(
    		@QueryParam(ParamMapper.STORE_ID) String storeId,
    		@QueryParam(ParamMapper.USER_ID) String userId,
    		@QueryParam(ParamMapper.CUSTOMER_CODE) String customerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getSupportInfo----------------\n");
        
        try {
            if ( StringUtils.isNotBlank(storeId) && StringUtils.isNotBlank(userId) ) {
            	throw new IllegalStateException("Invalid search condition. Either search by storeId OR by userId, not both.");
            }
            String data = "";
            if (  StringUtils.isNotBlank(storeId) ) {
            	data = restS2PAction.getSupportInfoByStoreId(customerCode, storeId);
            } else {
            	data = restS2PAction.getSupportInfoByUserId(customerCode, userId);
            }
            
            LOGGER.info("---------------Controller Ends getSupportInfo----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            input.put("storeId",storeId);
            input.put("userId",userId);
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/support/users")
    public String getUserList(
    		@QueryParam(ParamMapper.CUSTOMER_CODE) String customerCode,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getSupportInfo----------------\n");
        
        try {
            String data = restS2PAction.getUserListByCustomerCode(customerCode);
            
            LOGGER.info("---------------Controller Ends getSupportInfo----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/support/customers")
    public String getCustomerList(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getCustomerList----------------\n");
        
        try {
            String data = restS2PAction.getCustomerList();
            
            LOGGER.info("---------------Controller Ends getCustomerList----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/support/request")
    public String createSupportRequest(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts createSupportRequest----------------\n");
        
        try {
        	//Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            List<String> attachments = new ArrayList<String>();
            Map<String,String> requestContents = new HashMap<String,String>();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.",name, value);
                    requestContents.put(name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    String filenamePath = env.getProperty("disk_directory") + "support/"+System.currentTimeMillis()+"_"+name;
                    File uploadedFile = new File(filenamePath);
                    if (!uploadedFile.exists()) {
                        uploadedFile.getParentFile().mkdirs();
                        uploadedFile.getParentFile().setReadable(true);
                        uploadedFile.getParentFile().setWritable(true);
                        uploadedFile.getParentFile().setExecutable(true);
                    }
                    item.write(uploadedFile);
                    attachments.add(filenamePath);
                }
            }
            LOGGER.info("Attachment files to upload : {}",attachments);
            
            String output = restS2PAction.createSupportRequest(requestContents, attachments);
            
            LOGGER.info("---------------Controller Ends createSupportRequest----------------\n");
            
            return output;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/getRepPerformanceMetrics")
    public String getRepPerformanceMetrics(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getRepPerformanceMetrics----------------\n");
        
        try {
        	String userId = null != request.getAttribute("userId") ? request.getAttribute("userId").toString().trim().toLowerCase() : "";
            String data = restS2PAction.getRepPerformanceMetrics(userId);
            LOGGER.info("---------------Controller Ends getRepPerformanceMetrics----------------\n");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Map<String, String> input = new HashMap<String, String>();
            input.put("error in Input","-9");
            List<Map<String,String>> metaList = new ArrayList<Map<String,String>>();
            metaList.add(input);
            
            Map<String, String> emptyOutput = new HashMap<String, String>();
        	emptyOutput.put("Message", "No Data Returned");
        	List<Map<String,String>> emptyOutputList = new ArrayList<>();
        	emptyOutputList.add(emptyOutput);
        	CustomSnap2BuyOutput reportIO = new CustomSnap2BuyOutput(emptyOutputList, metaList);
        	//convert to json here
            Gson gson = new Gson();
            String output = gson.toJson(reportIO);
            return output;
        }
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/itg/masterdata/{dataType}")
    public Snap2BuyOutput ingestITGMasterData(
    		@PathParam("dataType") String dataType,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts ingestITGMasterData----------------\n");
        Snap2BuyOutput rio;
        HashMap<String, String> inputList = new HashMap<String, String>();
        inputList.put("dataType",dataType);
        
        //String fileName = dataType+"_"+ LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)+".zip";
        
        try {
        	//Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                LOGGER.info("Controller: Inside while................");
                FileItem item = iter.next();
                String name = item.getFieldName();
                String value = item.getString();
                if (item.isFormField()) {
                    LOGGER.info("Form field {} with value {} detected.", name, value);
                } else {
                    LOGGER.info("File field {} with file name {} detected.", name, item.getName());

                    FileInputStream fileInputStream = (FileInputStream) item.getInputStream();

                    cloudStorageService.storeImage("ITG", name, fileInputStream);
                }
            }
            
            inputList.put("status","success");

        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            inputList.put("status","error");
        }
        
        rio = new Snap2BuyOutput(null, inputList);
        LOGGER.info("---------------Controller Ends ingestITGMasterData----------------\n");

        return rio;
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/store/{storeId}")
    public Snap2BuyOutput getStoreByCustomerCodeStoreId(
    		@PathParam(ParamMapper.STORE_ID) String storeId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) {
        LOGGER.info("---------------Controller Starts getStoreByCustomerCodeStoreId----------------\n");
        try {
        	String customerCode = null != request.getAttribute("customerCode") ? request.getAttribute("customerCode").toString(): "";

        	InputObject inputObject = new InputObject();
        	inputObject.setCustomerCode(customerCode);
            inputObject.setCustomerStoreNumber(storeId);

            return restS2PAction.getStoreByCustomerCodeStoreId(inputObject);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

            Snap2BuyOutput rio;
            HashMap<String, String> inputList = new HashMap<String, String>();

            inputList.put("error in Input","-9");
            inputList.put("storeId", storeId);

            rio = new Snap2BuyOutput(null, inputList);
            LOGGER.info("---------------Controller Ends getStoreByCustomerCodeStoreId----------------\n");
            return rio;
        }
    }
    
}
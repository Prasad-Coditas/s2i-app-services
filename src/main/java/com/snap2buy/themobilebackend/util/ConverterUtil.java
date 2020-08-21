package com.snap2buy.themobilebackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snap2buy.themobilebackend.dao.impl.ProcessImageDaoImpl;
import com.snap2buy.themobilebackend.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sachin on 3/15/16.
 */
public class ConverterUtil {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ConverterUtil.class);
	
	private static AtomicInteger ITG_BUCKET_ID_COUNTER_GENERATOR = new AtomicInteger(1);
	private static Integer ITG_BUCKET_SIZE = 10;

	public static List<LinkedHashMap<String, String>> convertImageAnalysisObjectToMap(List<ImageAnalysis> dataList) {

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        for (ImageAnalysis listEntry : dataList) {
            java.util.LinkedHashMap<String, String> temp = new java.util.LinkedHashMap<String, String>();
            temp.put("imageUUID", listEntry.getImageUUID());
            temp.put("projectId", listEntry.getProjectId()+"");
            temp.put("storeId", listEntry.getStoreId());
            temp.put("dateId", listEntry.getDateId());
            temp.put("upc", listEntry.getUpc());
            temp.put("upcConfidence", listEntry.getUpcConfidence());
            temp.put("leftTopX", listEntry.getLeftTopX());
            temp.put("leftTopY", listEntry.getLeftTopY());
            temp.put("width", listEntry.getWidth());
            temp.put("height", listEntry.getHeight());
            temp.put("promotion", listEntry.getPromotion());
            temp.put("price", listEntry.getPrice());
            temp.put("priceLabel", listEntry.getPriceLabel());
            temp.put("priceConfidence", listEntry.getPriceConfidence());
            temp.put("productShortName", listEntry.getProductShortName());
            temp.put("productLongName", listEntry.getProductLongName());
            temp.put("brandName", listEntry.getBrandName());
            temp.put("shelfLevel", listEntry.getShelfLevel());
            temp.put("compliant", listEntry.getCompliant());
            temp.put("id", listEntry.getId());
            result.add(temp);
        }
        return result;
    }


    public static List<LinkedHashMap<String, String>> convertProjectUpcObjectToMap(List<ProjectUpc> dataList) {

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        for (ProjectUpc listEntry : dataList) {
            java.util.LinkedHashMap<String, String> temp = new java.util.LinkedHashMap<String, String>();
            temp.put("id", listEntry.getId());
            temp.put("projectId", listEntry.getProjectId()+"");
            temp.put("expectedFacingCount", listEntry.getExpectedFacingCount());
            temp.put("upc", listEntry.getUpc());
            temp.put("skuTypeId", listEntry.getSkuTypeId());
            temp.put("imageUrl1", listEntry.getImageUrl1());
            temp.put("imageUrl2", listEntry.getImageUrl2());
            temp.put("imageUrl3", listEntry.getImageUrl3());
            result.add(temp);
        }
        return result;
    }

    public static List<LinkedHashMap<String, String>> convertImageStoreObjectToMap(List<ImageStore> dataList) {

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        for (ImageStore listEntry : dataList) {
            java.util.LinkedHashMap<String, String> temp = new java.util.LinkedHashMap<String, String>();
            temp.put("imageUUID", listEntry.getImageUUID());
            temp.put("ImageFilePath", listEntry.getImageFilePath());
            temp.put("userId", listEntry.getUserId());
            temp.put("categoryId", listEntry.getCategoryId());
            temp.put("latitude", listEntry.getLatitude());
            temp.put("longitude", listEntry.getLongitude());
            temp.put("timeStamp", listEntry.getTimeStamp());
            temp.put("storeId", listEntry.getStoreId());
            temp.put("hostId", listEntry.getHostId());
            temp.put("dateId", listEntry.getDateId());
            temp.put("imageStatus", listEntry.getImageStatus());
            temp.put("shelfStatus", listEntry.getShelfStatus());
            temp.put("origWidth", listEntry.getOrigWidth());
            temp.put("origHeight", listEntry.getOrigHeight());
            temp.put("newWidth", listEntry.getNewWidth());
            temp.put("newHeight", listEntry.getNewHeight());
            temp.put("thumbnailPath", listEntry.getThumbnailPath());
            temp.put("taskId", listEntry.getTaskId());
            temp.put("agentId", listEntry.getAgentId());
            temp.put("lastUpdatedTimestamp", listEntry.getLastUpdatedTimestamp());
            temp.put("imageHashScore", listEntry.getImageHashScore());
			temp.put("imageRotation", listEntry.getImageRotation());
			temp.put("previewPath", listEntry.getPreviewPath());
            temp.put("projectId", listEntry.getProjectId()+"");
            result.add(temp);
        }
        return result;
    }


	public static List<ProjectStoreResultWithUPC> convertProjectDataResultToObject(
			Map<String, List<Map<String, String>>> storeImageData, List<LinkedHashMap<String, String>> resultDataMap,
			Map<String, List<Map<String, String>>> storeImageMetaData, List<SkuType> skuTypes) {

		List<ProjectStoreResultWithUPC> resultList = new ArrayList<ProjectStoreResultWithUPC>();
		
		for ( LinkedHashMap<String, String> resultData : resultDataMap ) {
			ProjectStoreResultWithUPC storeResult = new ProjectStoreResultWithUPC();
			storeResult.setAgentId(resultData.get("agentId"));
			storeResult.setCity(resultData.get("city"));
			storeResult.setCountDistinctUpc(resultData.get("countDistinctUpc"));
			storeResult.setCountDistinctBrands(resultData.get("countDistinctBrands"));
			storeResult.setProjectId(resultData.get("projectId"));
			storeResult.setProcessedDate(resultData.get("processedDate"));
			storeResult.setResult(resultData.get("result"));
			storeResult.setResultCode(resultData.get("resultCode"));
			storeResult.setResultComment(resultData.get("resultComment"));
			storeResult.setRetailer(resultData.get("retailer"));
			storeResult.setRetailerChainCode(resultData.get("retailerChainCode"));
			storeResult.setRetailerStoreId(resultData.get("retailerStoreId"));
			storeResult.setState(resultData.get("state"));
			storeResult.setStateCode(resultData.get("stateCode"));
			storeResult.setStatus(resultData.get("status"));
			storeResult.setStoreId(resultData.get("storeId"));
			storeResult.setTaskId(resultData.get("taskId"));
			storeResult.setVisitDateId(resultData.get("visitDateId"));
			storeResult.setStreet(resultData.get("street"));
			storeResult.setSumFacing(resultData.get("sumFacing"));
			storeResult.setSumUpcConfidence(resultData.get("sumUpcConfidence"));
			storeResult.setZip(resultData.get("zip"));
			storeResult.setLinearFootage(resultData.get("linearFootage"));
			storeResult.setCountMissingUpc(resultData.get("countMissingUpc"));
			storeResult.setPercentageOsa(resultData.get("percentageOsa"));
			storeResult.setWaveName(resultData.get("waveName"));
			storeResult.setWaveId(resultData.get("waveId"));
			if ( skuTypes != null ) { storeResult.setSkuTypes(skuTypes); }
			
			List<StoreUPC> storeUPCs = new ArrayList<StoreUPC>();
			
			List<Map<String, String>> entries = storeImageData.get(resultData.get("taskId"));
			if ( entries != null ) {
				for ( Map<String, String> storeDataEntry : entries ) {
					String projectId = resultData.get("projectId");
					if  ( Constants.ITG_CIGARETTES_PROJECT_IDS.contains(Integer.parseInt(projectId)) ) {
						String facing = storeDataEntry.get("facing");
						if ( StringUtils.isBlank(facing) || facing.equals("0") ) {
							continue;
						}
					}
					StoreUPC upc = convertStoreUPCMapToObject(storeDataEntry);
					storeUPCs.add(upc);
				}
			}
			storeResult.setProjectUPCs(storeUPCs);
			
			List<StoreImageDetails> imageUUIDs = new ArrayList<StoreImageDetails>();
			
			List<Map<String, String>> imageEntries = storeImageMetaData.get(resultData.get("taskId"));
			if ( imageEntries != null ) {
				Set<String> questionGroups = new HashSet<String>();
				for ( Map<String, String> imageEntry : imageEntries ) {
					StoreImageDetails detail = convertStoreImageDetailsMapToObject(imageEntry);
					imageUUIDs.add(detail);
					questionGroups.add(detail.getQuestionGroupName());
				}
				
				String otherPhotosGroupName = "";
				if ( questionGroups.contains("") && questionGroups.size() == 1 ) {
					otherPhotosGroupName = "Photos";
				} else if ( questionGroups.contains("") && questionGroups.size() > 1 ) {
					otherPhotosGroupName = "Other Photos";
				}
				
				for(StoreImageDetails oneImage : imageUUIDs ) {
					if ( StringUtils.isBlank(oneImage.getQuestionGroupName()) ) {
						oneImage.setQuestionGroupName(otherPhotosGroupName);
					}
				}
			}
			storeResult.setImageUUIDs(imageUUIDs);
			
			resultList.add(storeResult);
		}
		return resultList;
	}


	public static StoreImageDetails convertStoreImageDetailsMapToObject(Map<String, String> imageEntry) {
		StoreImageDetails detail = new StoreImageDetails();
		detail.setImageUUID(imageEntry.get("imageUUID"));
		detail.setImageStatus(imageEntry.get("imageStatus"));
		detail.setDateId(imageEntry.get("dateId"));
		detail.setAgentId(imageEntry.get("agentId"));
		detail.setOrigWidth(imageEntry.get("origWidth"));
		detail.setOrigHeight(imageEntry.get("origHeight"));
		detail.setNewWidth(imageEntry.get("newWidth"));
		detail.setNewHeight(imageEntry.get("newHeight"));
		detail.setImageRotation(imageEntry.get("imageRotation"));
		detail.setQuestionId(imageEntry.get("questionId"));
		detail.setQuestionGroupName(imageEntry.get("questionGroupName"));
		detail.setSequenceNumber(imageEntry.get("sequenceNumber"));
		return detail;
	}


	public static StoreUPC convertStoreUPCMapToObject(Map<String, String> storeDataEntry) {
		StoreUPC upc = new StoreUPC();
		upc.setBrand_name(storeDataEntry.get("brand_name"));
//		upc.setCustomerCode(storeDataEntry.get("customerCode"));
		upc.setProjectId(storeDataEntry.get("projectId"));
		upc.setFacing(storeDataEntry.get("facing"));
		upc.setImageUUID(storeDataEntry.get("imageUUID"));
		upc.setPrice(storeDataEntry.get("price"));
		upc.setPriceConfidence(storeDataEntry.get("priceConfidence"));
		upc.setProduct_long_name(storeDataEntry.get("product_long_name"));
		upc.setProduct_short_name(storeDataEntry.get("product_short_name"));
		upc.setPromotion(storeDataEntry.get("promotion"));
		upc.setStoreId(storeDataEntry.get("storeId"));
		upc.setTaskId(storeDataEntry.get("taskId"));
		upc.setUpc(storeDataEntry.get("upc"));
		upc.setUpcConfidence(storeDataEntry.get("upcConfidence"));
		upc.setShelfLevel(storeDataEntry.get("shelfLevel"));
		upc.setProduct_type(storeDataEntry.get("product_type"));
		upc.setProduct_sub_type(storeDataEntry.get("product_sub_type"));
		upc.setSkuTypeId(storeDataEntry.get("skuTypeId"));
		return upc;
	}
	
	public static String ifNullToEmpty(String input) {
		if ( input == null || input.equals("null") ) {
			return "";
		} else {
			return input;
		}
	}
	
	public static String ifNullToZero(String input) {
		if ( input == null || input.isEmpty() || input.equalsIgnoreCase("null") ) {
			return "0";
		} else {
			return input;
		}
	}
	
	public static String ifNullToNA(String input) {
		if ( input == null || input.isEmpty() || input.equalsIgnoreCase("null") ) {
			return "Not Available";
		} else {
			return input;
		}
	}
	
	public static String getBatchIdForImport(String projectId) {
		return projectId+"_"+System.currentTimeMillis()/1000;
	}
	
	public static String getResourceFromClasspath(String resourceName, String resourcePath) {
		String resourceToFetch = resourcePath + resourceName;
		String resource = null;
		try {
			resource = IOUtils.toString(ProcessImageDaoImpl.class.getResourceAsStream(resourceToFetch));
		} catch (Exception e) {
			LOGGER.error("EXCEPTION {}", e);
		}
		return resource;
	}


	public static String getPepsiPOGFormattedResult(InputObject inputObject) {
		String responseFileName = "/usr/share/s2i/images/pepsi/"+inputObject.getProjectId()+"_"+inputObject.getStoreId()+"_"+inputObject.getTaskId()+".json";
		
		Map<String,UPCPOGEntity> actualComplianceMap = new LinkedHashMap<String,UPCPOGEntity>();
		
		Map<String,String> planoMap = getPlanoMap(); //GON type
		
		JsonObject resultObject =null;
		try {
			String result = new String(Files.readAllBytes(Paths.get(responseFileName)));
			resultObject = new JsonParser().parse(result).getAsJsonObject();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		resultObject = resultObject.get("data").getAsJsonObject();

		JsonArray products = resultObject.get("product_info").getAsJsonArray();
		Map<String,String> upcNameMap = new HashMap<String,String>();
		for(int i=0 ; i < products.size() ; i++ ) {
			JsonObject oneEntry = products.get(i).getAsJsonObject();
			if( oneEntry.get("upc").isJsonNull() ) {
				continue;
			}
			String upc = oneEntry.get("upc").getAsString();
			String name = oneEntry.get("name").getAsString();
			upcNameMap.put(upc, name);
		}
		
		JsonArray analysisEntries = resultObject.get("image_analysis").getAsJsonArray();
		
		for(int i=0 ; i < analysisEntries.size() ; i++ ) {
			JsonObject oneEntry = analysisEntries.get(i).getAsJsonObject();
			if ( oneEntry.get("predicted_upc").isJsonNull() ) {
				continue;
			}
		
			String predictedUpc = oneEntry.get("predicted_upc").getAsString();
			boolean isNotInPOG = false;
			if ( !planoMap.containsKey(predictedUpc) ) {
				isNotInPOG = true;
			}
			
			boolean hasFacingIssue = oneEntry.get("is_facing_issue").getAsBoolean();
			boolean hasPlacementIssue = oneEntry.get("is_placement_issue").getAsBoolean();
			boolean hasNotInPOGIssue = oneEntry.get("is_not_in_pog").getAsBoolean();
			System.out.println(predictedUpc + " " + hasFacingIssue + " " + hasPlacementIssue + " " + hasNotInPOGIssue );
			
			if (hasFacingIssue == false && hasPlacementIssue == false && hasNotInPOGIssue == false ) {
				continue; //IF NO ISSUE, IT IS A COMPLIANT UPC
			}
			
			if ( !actualComplianceMap.containsKey(predictedUpc) ) {
				UPCPOGEntity entity = new UPCPOGEntity();
				entity.setUpc(predictedUpc);
				entity.setExpectedFacings("");
				entity.setExpectedLocation("");
				entity.setProductShortName(upcNameMap.get(predictedUpc));
				actualComplianceMap.put(predictedUpc, entity);
			}
			
			UPCPOGEntity entity = actualComplianceMap.get(predictedUpc);
			if( hasPlacementIssue ) {
				entity.getIssueTypes().add("1");
			}
			if( hasFacingIssue ) {
				entity.getIssueTypes().add("2");
			}
			if ( isNotInPOG ) {
				entity.getIssueTypes().clear();
				entity.getIssueTypes().add("4");
				entity.setExpectedFacings("0");
				entity.setExpectedLocation("");
			}
			int actualFacings = Integer.parseInt(entity.getActualFacings()) + 1 ;
			entity.setActualFacings(""+actualFacings);
		}
		
		//OOS UPCs
		if( !resultObject.get("oos_upcs").isJsonNull() ) {
			JsonArray oosUPCs = resultObject.get("oos_upcs").getAsJsonArray();
			for(int i=0 ; i<oosUPCs.size();i++) {
				String oosUPC = oosUPCs.get(i).getAsString();
				UPCPOGEntity oosEntity = new UPCPOGEntity();
				oosEntity.setUpc(oosUPC);
				oosEntity.setProductShortName(upcNameMap.get(oosUPC));
				oosEntity.getIssueTypes().add("3");
				actualComplianceMap.put(oosUPC, oosEntity);
			}
		}
		
		for(String upc : planoMap.keySet() ) {
			UPCPOGEntity entity = actualComplianceMap.get(upc);
			String location = planoMap.get(upc);
			long expectedFacings = location.chars().filter(num -> num == ',').count();
			if ( entity != null ) {
				entity.setExpectedFacings("" + (expectedFacings + 1));
				entity.setExpectedLocation(location);
			}
		}
		
		Map<String, String> reportInput = new HashMap<String, String>();
	    List<Map<String, String>> metaList = new ArrayList<Map<String, String>>();
	    reportInput.put("projectId", ""+inputObject.getProjectId());
	    reportInput.put("storeId", inputObject.getStoreId());
	    reportInput.put("taskId", inputObject.getTaskId());
	    metaList.add(reportInput);

	    CustomSnap2BuyOutput reportIO = null;
	    List<LinkedHashMap<String, Object>> resultListToPass = new ArrayList<LinkedHashMap<String, Object>>();
	    LinkedHashMap<String,Object> returnMap = new LinkedHashMap<String,Object>();
	    returnMap.put("projectId", inputObject.getProjectId()+"");
	    returnMap.put("storeId", inputObject.getStoreId());
	    returnMap.put("taskId", inputObject.getTaskId());
	    
	    List<Map<String,Object>> fixtures = new ArrayList<Map<String,Object>>() ;
	    Map<String,Object> fixture = new LinkedHashMap<String,Object>();
	    fixture.put("fixtureId", "8");
	    fixture.put("fixtureName", "GON_1");
	    fixture.put("products", actualComplianceMap.values());
	    fixtures.add(fixture);
	    
	    returnMap.put("fixtures",fixtures);
	    resultListToPass.add(returnMap);
	    
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


	private static Map<String, String> getPlanoMap() {
		Map<String,String> planoMap = new HashMap<String,String>();
		planoMap.put("999999986293","Shelf 4 > Position 1");
		planoMap.put("999999986294","Shelf 4 > Position 2");
		planoMap.put("999999986332","Shelf 4 > Position 3");
		planoMap.put("999999986344","Shelf 4 > Position 4");
		planoMap.put("999999986319","Shelf 4 > Position 5");
		planoMap.put("999999986377","Shelf 4 > Position 6");
		planoMap.put("999999986304","Shelf 3 > Position 1,2");
		planoMap.put("999999986302","Shelf 3 > Position 3,4,5");
		planoMap.put("999999986305","Shelf 3 > Position 6");
		planoMap.put("999999986308","Shelf 2 > Position 1");
		planoMap.put("999999986321","Shelf 2 > Position 2,3");
		planoMap.put("999999986325","Shelf 2 > Position 4");
		planoMap.put("999999986340","Shelf 2 > Position 5");
		planoMap.put("999999986329","Shelf 2 > Position 6");
		planoMap.put("999999986358","Shelf 1 > Position 1");
		planoMap.put("999999986338","Shelf 1 > Position 2,3");
		planoMap.put("999999986374","Shelf 1 > Position 4");
		planoMap.put("999999986337","Shelf 1 > Position 5");
		return planoMap;
	}


	public static synchronized int getITGReviewBucketId() {
		int bucketId = ITG_BUCKET_ID_COUNTER_GENERATOR.getAndIncrement();
		if (bucketId == ITG_BUCKET_SIZE) {
			ITG_BUCKET_ID_COUNTER_GENERATOR.set(1);
		}
		return bucketId;
	}

	
}

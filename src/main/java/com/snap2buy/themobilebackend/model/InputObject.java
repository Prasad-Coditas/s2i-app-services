/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.snap2buy.themobilebackend.model;
//

import java.util.List;
import java.util.Map;

/**
 * @author sachin
 */
public class InputObject {

    private String visitId;
    private String retailerChainCode;
    private String listId;
    private String state;
    private String stateCode;
    private String city;
    private String upc;
    private String hostId;
    private String visitDate;
    private String shopId;
    private String categoryId;
    private String userId;
    private String timeStamp;
    private String imageFilePath;
    private String imageUUID;
    private String prevImageUUID;
    private String latitude;
    private String longitude;
    private String sync;
    private Integer responseCode;
    private String responseMessage;
    private String startTime;
    private String endTime;
    private String endDate;
    private String startDate;
    private String frequency;
    //private String dateId;
    private String storeId;
    private String retailerStoreId;
    private String brandId;
    private String marketId;
    private String chainId;
    private String queryId;
    private String limit;
    private String prevStartTime;
    private String prevEndTime;
    private String origWidth;
    private String origHeight;
    private String newWidth;
    private String newHeight;
    private String thumbnailPath;
    private String imageUUIDCsvString;
    private String street;
    private String retailer;
    private String customerCode;
    private String retailerCode;
    private String customerProjectId;
    private String taskId;
    private String agentId;
    private String id;
    private String imageHashScore;
    private String imageRotation;
    private String resultCode;
    private String status;
    private String storeVisits;
    private String totalProjects;
    private String repsCount;
    private String accessKey;
    private String secretKey;
    private String instanceId;
    private String assessmentId;
    private String fileId;
    private String questionId;
    private String imageUrl;
    private String imageStatus;
    private String currentImageStatus;
    private String newImageStatus;
    private String granularity;
    private String externalProjectId;
    private String sendToDestination;
    private String showAll;
    private String previewPath;
    private String month;
    private String rollup;
    private int projectId;
    private Boolean showOnlyChildProjects;
    private String level;
    private String value;
    private String role;
    private String linearFootage;
    private String batchId;
    private List<String> skuTypeIds;
    private String modular;
    private String subCategory;
    private String brandName;
    private String sequenceNumber;
    private String totalImages;
    private String placeId;
    private String source;
    private String scoreId;
    private String name;
    private String country;
    private String fromMonth;
    private String toMonth;
    private String fromDate;
    private String toDate;
    private String waveId;
    private String reviewStatus;
    private String customerStoreNumber;
    private String fromWave;
    private String toWave;
    private String storeFormat;
    private String geoLevel;
    private String geoLevelId;
    private String timePeriodType;
    private String timePeriod;

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(String imageStatus) {
        this.imageStatus = imageStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getImageHashScore() {
        return imageHashScore;
    }

    public void setImageHashScore(String imageHashScore) {
        this.imageHashScore = imageHashScore;
    }

    public String getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(String imageRotation) {
        this.imageRotation = imageRotation;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerProjectId() {
        return customerProjectId;
    }

    public void setCustomerProjectId(String customerProjectId) {
        this.customerProjectId = customerProjectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getImageUUIDCsvString() {
        return imageUUIDCsvString;
    }

    public void setImageUUIDCsvString(String imageUUIDCsvString) {
        this.imageUUIDCsvString = imageUUIDCsvString;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getOrigWidth() {
        return origWidth;
    }

    public void setOrigWidth(String origWidth) {
        this.origWidth = origWidth;
    }

    public String getOrigHeight() {
        return origHeight;
    }

    public void setOrigHeight(String origHeight) {
        this.origHeight = origHeight;
    }

    public String getNewWidth() {
        return newWidth;
    }

    public void setNewWidth(String newWidth) {
        this.newWidth = newWidth;
    }

    public String getNewHeight() {
        return newHeight;
    }

    public void setNewHeight(String newHeight) {
        this.newHeight = newHeight;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getPrevImageUUID() {
        return prevImageUUID;
    }

    public void setPrevImageUUID(String prevImageUUID) {
        this.prevImageUUID = prevImageUUID;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getRetailerChainCode() {
        return retailerChainCode;
    }

    public void setRetailerChainCode(String retailerChainCode) {
        this.retailerChainCode = retailerChainCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private Map<Object, Object> morefiltersMap;

    public Map<Object, Object> getMorefiltersMap() {
        return morefiltersMap;
    }

    public void setMorefiltersMap(Map<Object, Object> morefiltersMap) {
        this.morefiltersMap = morefiltersMap;
    }

    public String getPrevStartTime() {
        return prevStartTime;
    }

    public void setPrevStartTime(String prevStartTime) {
        this.prevStartTime = prevStartTime;
    }

    public String getPrevEndTime() {
        return prevEndTime;
    }

    public void setPrevEndTime(String prevEndTime) {
        this.prevEndTime = prevEndTime;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

//    public String getDateId() {
//        return dateId;
//    }
//
//    public void setDateId(String dateId) {
//        this.dateId = dateId;
//    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

//    public GridFS getImage() {
//        return image;
//    }
//
//    public void setImage(GridFS image) {
//        this.image = image;
//    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStoreVisits() {
        return storeVisits;
    }

    public void setStoreVisits(String storeVisits) {
        this.storeVisits = storeVisits;
    }

    public String getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(String totalProjects) {
        this.totalProjects = totalProjects;
    }

    public String getRepsCount() {
        return repsCount;
    }

    public void setRepsCount(String repsCount) {
        this.repsCount = repsCount;
    }
    
    public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	public String getCurrentImageStatus() {
		return currentImageStatus;
	}

	public void setCurrentImageStatus(String currentImageStatus) {
		this.currentImageStatus = currentImageStatus;
	}

	public String getNewImageStatus() {
		return newImageStatus;
	}

	public void setNewImageStatus(String newImageStatus) {
		this.newImageStatus = newImageStatus;
	}

	public String getExternalProjectId() {
		return externalProjectId;
	}

	public void setExternalProjectId(String externalProjectId) {
		this.externalProjectId = externalProjectId;
	}

	public String getShowAll() {
		return showAll;
	}

	public void setShowAll(String showAll) {
		this.showAll = showAll;
	}

    public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}
	
	public String getRollup() {
		return rollup;
	}

	public void setRollup(String rollup) {
		this.rollup = rollup;
	}

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Boolean getShowOnlyChildProjects() {
        return showOnlyChildProjects;
    }

    public void setShowOnlyChildProjects(Boolean showOnlyChildProjects) {
        this.showOnlyChildProjects = showOnlyChildProjects;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getLinearFootage() {
		return linearFootage;
	}

	public void setLinearFootage(String linearFootage) {
		this.linearFootage = linearFootage;
	}
	
	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public List<String> getSkuTypeIds() {
		return skuTypeIds;
	}

	public void setSkuTypeIds(List<String> skuTypeIds) {
		this.skuTypeIds = skuTypeIds;
	}
	
	public String getModular() {
		return modular;
	}

	public void setModular(String modular) {
		this.modular = modular;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	
	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(String totalImages) {
        this.totalImages = totalImages;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    public String getScoreId() {
		return scoreId;
	}

	public void setScoreId(String scoreId) {
		this.scoreId = scoreId;
	}

    public String getSendToDestination() {
		return sendToDestination;
	}

	public void setSendToDestination(String sendToSurvey) {
		this.sendToDestination = sendToSurvey;
	}
	
	public String getRetailerStoreId() {
		return retailerStoreId;
	}

	public void setRetailerStoreId(String retailerStoreId) {
		this.retailerStoreId = retailerStoreId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFromMonth() {
		return fromMonth;
	}

	public void setFromMonth(String fromMonth) {
		this.fromMonth = fromMonth;
	}

	public String getToMonth() {
		return toMonth;
	}

	public void setToMonth(String toMonth) {
		this.toMonth = toMonth;
	}
	
	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getWaveId() {
		return waveId;
	}

	public void setWaveId(String waveId) {
		this.waveId = waveId;
	}

	public String getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
	}
	
	public String getCustomerStoreNumber() {
		return customerStoreNumber;
	}

	public void setCustomerStoreNumber(String customerStoreNumber) {
		this.customerStoreNumber = customerStoreNumber;
	}
	
	public String getFromWave() {
		return fromWave;
	}

	public void setFromWave(String fromWave) {
		this.fromWave = fromWave;
	}

	public String getToWave() {
		return toWave;
	}

	public void setToWave(String toWave) {
		this.toWave = toWave;
	}

	public String getStoreFormat() {
		return storeFormat;
	}

	public void setStoreFormat(String storeFormat) {
		this.storeFormat = storeFormat;
	}
	
	public String getGeoLevel() {
		return geoLevel;
	}

	public void setGeoLevel(String geoLevel) {
		this.geoLevel = geoLevel;
	}

	public String getGeoLevelId() {
		return geoLevelId;
	}

	public void setGeoLevelId(String geoLevelId) {
		this.geoLevelId = geoLevelId;
	}

	public String getTimePeriodType() {
		return timePeriodType;
	}

	public void setTimePeriodType(String timePeriodType) {
		this.timePeriodType = timePeriodType;
	}

	public String getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	@Override
	public String toString() {
		return "InputObject [visitId=" + visitId + ", retailerChainCode=" + retailerChainCode + ", listId=" + listId
				+ ", state=" + state + ", stateCode=" + stateCode + ", city=" + city + ", upc=" + upc + ", hostId="
				+ hostId + ", visitDate=" + visitDate + ", shopId=" + shopId + ", categoryId=" + categoryId
				+ ", userId=" + userId + ", timeStamp=" + timeStamp + ", imageFilePath=" + imageFilePath
				+ ", imageUUID=" + imageUUID + ", prevImageUUID=" + prevImageUUID + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", sync=" + sync + ", responseCode=" + responseCode
				+ ", responseMessage=" + responseMessage + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", endDate=" + endDate + ", startDate=" + startDate + ", frequency=" + frequency + ", storeId="
				+ storeId + ", retailerStoreId=" + retailerStoreId + ", brandId=" + brandId + ", marketId=" + marketId
				+ ", chainId=" + chainId + ", queryId=" + queryId + ", limit=" + limit + ", prevStartTime="
				+ prevStartTime + ", prevEndTime=" + prevEndTime + ", origWidth=" + origWidth + ", origHeight="
				+ origHeight + ", newWidth=" + newWidth + ", newHeight=" + newHeight + ", thumbnailPath="
				+ thumbnailPath + ", imageUUIDCsvString=" + imageUUIDCsvString + ", street=" + street + ", retailer="
				+ retailer + ", customerCode=" + customerCode + ", retailerCode=" + retailerCode
				+ ", customerProjectId=" + customerProjectId + ", taskId=" + taskId + ", agentId=" + agentId + ", id="
				+ id + ", imageHashScore=" + imageHashScore + ", imageRotation=" + imageRotation + ", resultCode="
				+ resultCode + ", status=" + status + ", storeVisits=" + storeVisits + ", totalProjects="
				+ totalProjects + ", repsCount=" + repsCount + ", accessKey=" + accessKey + ", secretKey=" + secretKey
				+ ", instanceId=" + instanceId + ", assessmentId=" + assessmentId + ", fileId=" + fileId
				+ ", questionId=" + questionId + ", imageUrl=" + imageUrl + ", imageStatus=" + imageStatus
				+ ", currentImageStatus=" + currentImageStatus + ", newImageStatus=" + newImageStatus + ", granularity="
				+ granularity + ", externalProjectId=" + externalProjectId + ", sendToDestination=" + sendToDestination
				+ ", showAll=" + showAll + ", previewPath=" + previewPath + ", month=" + month + ", rollup=" + rollup
				+ ", projectId=" + projectId + ", showOnlyChildProjects=" + showOnlyChildProjects + ", level=" + level
				+ ", value=" + value + ", role=" + role + ", linearFootage=" + linearFootage + ", batchId=" + batchId
				+ ", skuTypeIds=" + skuTypeIds + ", modular=" + modular + ", subCategory=" + subCategory
				+ ", brandName=" + brandName + ", sequenceNumber=" + sequenceNumber + ", totalImages=" + totalImages
				+ ", placeId=" + placeId + ", source=" + source + ", scoreId=" + scoreId + ", name=" + name
				+ ", country=" + country + ", fromMonth=" + fromMonth + ", toMonth=" + toMonth + ", fromDate="
				+ fromDate + ", toDate=" + toDate + ", waveId=" + waveId + ", reviewStatus=" + reviewStatus
				+ ", customerStoreNumber=" + customerStoreNumber + ", fromWave=" + fromWave + ", toWave=" + toWave
				+ ", storeFormat=" + storeFormat + ", geoLevel=" + geoLevel + ", geoLevelId=" + geoLevelId
				+ ", timePeriodType=" + timePeriodType + ", timePeriod=" + timePeriod + ", morefiltersMap="
				+ morefiltersMap + "]";
	}

}

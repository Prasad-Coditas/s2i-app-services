package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 5/29/16.
 */
public class ImageStore {
    String imageUUID;
    String imageFilePath;
    String categoryId;
    String latitude;
    String longitude;
    String timeStamp;
    String storeId;
    String hostId;
    String dateId;
    String imageStatus;
    String shelfStatus;
    String origWidth;
    String origHeight;
    String newWidth;
    String newHeight;
    String thumbnailPath;
    String userId;
//    String customerCode;
//    String customerProjectId;
    int projectId;
    String taskId;
    String agentId;
    String lastUpdatedTimestamp;
    String imageHashScore;
    String imageRotation;
    String fileId;
    String questionId;
    String imageResultCode;
    String imageReviewStatus;
    String imageURL;
    String processedDate;
    String imageResultComments;
    String resultUploaded;
    String previewPath;
    String pixelsPerInch = "0";
    String oosCount = "0";
    String oosPercentage = "0";
    String imageAngle = "0";
    String shelfLevels = "0";
    String imageReviewRecommendations;
	String imageNotUsable;
	String imageNotUsableComment;
	String lowConfidence;
	String sequenceNumber;

	public ImageStore() {
        super();
    }

    public ImageStore(String imageUUID, String imageFilePath, String categoryId, String latitude, String longitude, String timeStamp, String storeId, String hostId, String dateId, String imageStatus, String shelfStatus, String origWidth, String origHeight, String newWidth, String newHeight, String thumbnailPath, String userId, String taskId, String agentId, String lastUpdatedTimestamp, String imageHashScore, String imageRotation, String fileId, String questionId, String imageResultCode, String imageReviewStatus, String imageURL, String processedDate, String imageResultComments, String resultUploaded, String previewPath, int projectId) {
        this.imageUUID = imageUUID;
        this.imageFilePath = imageFilePath;
        this.categoryId = categoryId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.storeId = storeId;
        this.hostId = hostId;
        this.dateId = dateId;
        this.imageStatus = imageStatus;
        this.shelfStatus = shelfStatus;
        this.origWidth = origWidth;
        this.origHeight = origHeight;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
        this.thumbnailPath = thumbnailPath;
        this.userId = userId;
//        this.customerCode = customerCode;
//        this.customerProjectId = customerProjectId;
        this.projectId = projectId;
        this.taskId = taskId;
        this.agentId = agentId;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.imageHashScore = imageHashScore;
        this.imageRotation = imageRotation;
        this.fileId = fileId;
        this.questionId = questionId;
        this.imageResultCode = imageResultCode;
        this.imageReviewStatus = imageReviewStatus;
        this.imageURL = imageURL;
        this.processedDate = processedDate;
        this.imageResultComments = imageResultComments;
        this.resultUploaded = resultUploaded;
        this.previewPath = previewPath;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(String processedDate) {
        this.processedDate = processedDate;
    }

    public String getImageResultComments() {
		return imageResultComments;
	}

	public void setImageResultComments(String imageResultComments) {
		this.imageResultComments = imageResultComments;
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

    public String getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getDateId() {
        return dateId;
    }

    public void setDateId(String dateId) {
        this.dateId = dateId;
    }

    public String getImageStatus() {
        return imageStatus;
    }

    public void setImageStatus(String imageStatus) {
        this.imageStatus = imageStatus;
    }

    public String getShelfStatus() {
        return shelfStatus;
    }

    public void setShelfStatus(String shelfStatus) {
        this.shelfStatus = shelfStatus;
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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

//    public String getCustomerCode() {
//        return customerCode;
//    }
//
//    public void setCustomerCode(String customerCode) {
//        this.customerCode = customerCode;
//    }
//
//    public String getCustomerProjectId() {
//        return customerProjectId;
//    }
//
//    public void setCustomerProjectId(String customerProjectId) {
//        this.customerProjectId = customerProjectId;
//    }

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
    
    public String getImageResultCode() {
		return imageResultCode;
	}

	public void setImageResultCode(String imageResultCode) {
		this.imageResultCode = imageResultCode;
	}

	public String getImageReviewStatus() {
		return imageReviewStatus;
	}

	public void setImageReviewStatus(String imageReviewStatus) {
		this.imageReviewStatus = imageReviewStatus;
	}

    public String getResultUploaded() {
		return resultUploaded;
	}

    public void setResultUploaded(String resultUploaded) {
		this.resultUploaded = resultUploaded;
	}

    public String getPixelsPerInch() {
        return pixelsPerInch;
    }

    public void setPixelsPerInch(String pixelsPerInch) {
        this.pixelsPerInch = pixelsPerInch;
    }

    public String getOosCount() {
        return oosCount;
    }

    public void setOosCount(String oosCount) {
        this.oosCount = oosCount;
    }

    public String getOosPercentage() {
        return oosPercentage;
    }

    public void setOosPercentage(String oosPercentage) {
        this.oosPercentage = oosPercentage;
    }

    public String getImageAngle() {
        return imageAngle;
    }

    public void setImageAngle(String imageAngle) {
        this.imageAngle = imageAngle;
    }

    public String getShelfLevels() {
        return shelfLevels;
    }

    public void setShelfLevels(String shelfLevels) {
        this.shelfLevels = shelfLevels;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getImageReviewRecommendations() {
		return imageReviewRecommendations;
	}

	public void setImageReviewRecommendations(String imageReviewRecommendations) {
		this.imageReviewRecommendations = imageReviewRecommendations;
	}

	public String getImageNotUsable() {
		return imageNotUsable;
	}

	public void setImageNotUsable(String imageNotUsable) {
		this.imageNotUsable = imageNotUsable;
	}

	public String getImageNotUsableComment() {
		return imageNotUsableComment;
	}

	public void setImageNotUsableComment(String imageNotUsableComment) {
		this.imageNotUsableComment = imageNotUsableComment;
	}
	
	public String getLowConfidence() {
		return lowConfidence;
	}

	public void setLowConfidence(String lowConfidence) {
		this.lowConfidence = lowConfidence;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public String toString() {
		return "ImageStore [imageUUID=" + imageUUID + ", imageFilePath=" + imageFilePath + ", categoryId=" + categoryId
				+ ", latitude=" + latitude + ", longitude=" + longitude + ", timeStamp=" + timeStamp + ", storeId="
				+ storeId + ", hostId=" + hostId + ", dateId=" + dateId + ", imageStatus=" + imageStatus
				+ ", shelfStatus=" + shelfStatus + ", origWidth=" + origWidth + ", origHeight=" + origHeight
				+ ", newWidth=" + newWidth + ", newHeight=" + newHeight + ", thumbnailPath=" + thumbnailPath
				+ ", userId=" + userId + ", projectId=" + projectId + ", taskId=" + taskId + ", agentId=" + agentId
				+ ", lastUpdatedTimestamp=" + lastUpdatedTimestamp + ", imageHashScore=" + imageHashScore
				+ ", imageRotation=" + imageRotation + ", fileId=" + fileId + ", questionId=" + questionId
				+ ", imageResultCode=" + imageResultCode + ", imageReviewStatus=" + imageReviewStatus + ", imageURL="
				+ imageURL + ", processedDate=" + processedDate + ", imageResultComments=" + imageResultComments
				+ ", resultUploaded=" + resultUploaded + ", previewPath=" + previewPath + ", pixelsPerInch="
				+ pixelsPerInch + ", oosCount=" + oosCount + ", oosPercentage=" + oosPercentage + ", imageAngle="
				+ imageAngle + ", shelfLevels=" + shelfLevels + ", imageReviewRecommendations="
				+ imageReviewRecommendations + ", imageNotUsable=" + imageNotUsable + ", imageNotUsableComment="
				+ imageNotUsableComment + ", lowConfidence=" + lowConfidence + ", sequenceNumber=" + sequenceNumber
				+ "]";
	}
}

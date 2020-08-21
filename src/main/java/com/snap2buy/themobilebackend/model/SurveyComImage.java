package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComImage {
	String imageURL;
	String imageUUID;
	String imageRotation;
	String retailerStoreId;
	String resultId;
	String processedDate;
	List<SurveyComProductDetectionDetail> imageUpcList;
	
	public String getRetailerStoreId() {
		return retailerStoreId;
	}
	public void setRetailerStoreId(String retailerStoreId) {
		this.retailerStoreId = retailerStoreId;
	}
	public String getResultId() {
		return resultId;
	}
	public void setResultId(String resultId) {
		this.resultId = resultId;
	}
	public String getProcessedDate() {
		return processedDate;
	}
	public void setProcessedDate(String processedDate) {
		this.processedDate = processedDate;
	}
	public List<SurveyComProductDetectionDetail> getImageUpcList() {
		return imageUpcList;
	}
	public void setImageUpcList(List<SurveyComProductDetectionDetail> imageUpcList) {
		this.imageUpcList = imageUpcList;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	public String getImageUUID() {
		return imageUUID;
	}
	public void setImageUUID(String imageUUID) {
		this.imageUUID = imageUUID;
	}
	public String getImageRotation() {
		return imageRotation;
	}
	public void setImageRotation(String imageRotation) {
		this.imageRotation = imageRotation;
	}
	
}

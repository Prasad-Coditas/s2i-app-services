package com.snap2buy.themobilebackend.model;

import java.util.List;

public class SurveyComDownloadRecord {
	
	private String retailerStoreId;
	private String resultId;
	private String resultStatus;
	private Boolean imageUploadStatus;
	private String visitDate;
	private String brand;
	private String imageCount;
	private List<SurveyComImageURL> imageList;
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
	public String getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
	public Boolean getImageUploadStatus() {
		return imageUploadStatus;
	}
	public void setImageUploadStatus(Boolean imageUploadStatus) {
		this.imageUploadStatus = imageUploadStatus;
	}
	public String getVisitDate() {
		return visitDate;
	}
	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getImageCount() {
		return imageCount;
	}
	public void setImageCount(String imageCount) {
		this.imageCount = imageCount;
	}
	public List<SurveyComImageURL> getImageList() {
		return imageList;
	}
	public void setImageList(List<SurveyComImageURL> imageList) {
		this.imageList = imageList;
	}
}

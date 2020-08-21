package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 3/15/16.
 */
public class ImageAnalysis {
	
	String imageUUID;
    String storeId;
    String dateId;
    String upc;
    String upcConfidence;
    String alternateUpc;
    String alternateUpcConfidence;
    String leftTopX;
    String leftTopY;
    String width;
    String height;
    String promotion;
    String price;
    String priceLabel;
    String productShortName;
    String productLongName;
    String brandName;
    String priceConfidence;
    String taskId;
    String shelfLevel;
    int projectId;
    String id;
    String compliant;
    String isDuplicate;
    
    public ImageAnalysis(String imageUUID, String storeId, String dateId, String upc, String upcConfidence, String alternateUpc, String alternateUpcConfidence, String leftTopX, String leftTopY, String width, String height, String promotion, String price, String priceLabel, String productShortName, String productLongName, String brandName, String priceConfidence, String taskId, int projectId, String id) {
        this.imageUUID = imageUUID;
        this.storeId = storeId;
        this.dateId = dateId;
        this.upc = upc;
        this.upcConfidence = upcConfidence;
        this.alternateUpc = alternateUpc;
        this.alternateUpcConfidence = alternateUpcConfidence;
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.width = width;
        this.height = height;
        this.promotion = promotion;
        this.price = price;
        this.priceLabel = priceLabel;
        this.productShortName = productShortName;
        this.productLongName = productLongName;
        this.brandName = brandName;
        this.priceConfidence = priceConfidence;
        this.taskId = taskId;
        this.projectId = projectId;
        this.id = id;
    }

	public ImageAnalysis(String imageUUID, String storeId, String dateId,
			String upc, String upcConfidence, String alternateUpc, String alternateUpcConfidence, String leftTopX,
			String leftTopY, String width, String height, String promotion, String price, String priceLabel,
			String productShortName, String productLongName, String brandName, String priceConfidence, String taskId,
			String shelfLevel, int projectId) {
		super();
		this.imageUUID = imageUUID;
		this.storeId = storeId;
		this.dateId = dateId;
		this.upc = upc;
		this.upcConfidence = upcConfidence;
		this.alternateUpc = alternateUpc;
		this.alternateUpcConfidence = alternateUpcConfidence;
		this.leftTopX = leftTopX;
		this.leftTopY = leftTopY;
		this.width = width;
		this.height = height;
		this.promotion = promotion;
		this.price = price;
		this.priceLabel = priceLabel;
		this.productShortName = productShortName;
		this.productLongName = productLongName;
		this.brandName = brandName;
		this.priceConfidence = priceConfidence;
		this.taskId = taskId;
		this.shelfLevel = shelfLevel;
		this.projectId = projectId;
	}

	public ImageAnalysis() {
		// TODO Auto-generated constructor stub
	}

	public String getImageUUID() {
		return imageUUID;
	}
	public void setImageUUID(String imageUUID) {
		this.imageUUID = imageUUID;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getDateId() {
		return dateId;
	}
	public void setDateId(String dateId) {
		this.dateId = dateId;
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public String getUpcConfidence() {
		return upcConfidence;
	}
	public void setUpcConfidence(String upcConfidence) {
		this.upcConfidence = upcConfidence;
	}
	public String getAlternateUpc() {
		return alternateUpc;
	}
	public void setAlternateUpc(String alternateUpc) {
		this.alternateUpc = alternateUpc;
	}
	public String getAlternateUpcConfidence() {
		return alternateUpcConfidence;
	}
	public void setAlternateUpcConfidence(String alternateUpcConfidence) {
		this.alternateUpcConfidence = alternateUpcConfidence;
	}
	public String getLeftTopX() {
		return leftTopX;
	}
	public void setLeftTopX(String leftTopX) {
		this.leftTopX = leftTopX;
	}
	public String getLeftTopY() {
		return leftTopY;
	}
	public void setLeftTopY(String leftTopY) {
		this.leftTopY = leftTopY;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getPromotion() {
		return promotion;
	}
	public void setPromotion(String promotion) {
		this.promotion = promotion;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getPriceLabel() {
		return priceLabel;
	}
	public void setPriceLabel(String priceLabel) {
		this.priceLabel = priceLabel;
	}
	public String getProductShortName() {
		return productShortName;
	}
	public void setProductShortName(String productShortName) {
		this.productShortName = productShortName;
	}
	public String getProductLongName() {
		return productLongName;
	}
	public void setProductLongName(String productLongName) {
		this.productLongName = productLongName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getPriceConfidence() {
		return priceConfidence;
	}
	public void setPriceConfidence(String priceConfidence) {
		this.priceConfidence = priceConfidence;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getShelfLevel() {
		return shelfLevel;
	}
	public void setShelfLevel(String shelfLevel) {
		this.shelfLevel = shelfLevel;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompliant() {
		return compliant;
	}

	public void setCompliant(String compliant) {
		this.compliant = compliant;
	}
	
	public String getIsDuplicate() {
		return isDuplicate;
	}

	public void setIsDuplicate(String isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	@Override
	public String toString() {
		return "ImageAnalysis [imageUUID=" + imageUUID + ", storeId=" + storeId + ", dateId=" + dateId + ", upc=" + upc
				+ ", upcConfidence=" + upcConfidence + ", alternateUpc=" + alternateUpc + ", alternateUpcConfidence="
				+ alternateUpcConfidence + ", leftTopX=" + leftTopX + ", leftTopY=" + leftTopY + ", width=" + width
				+ ", height=" + height + ", promotion=" + promotion + ", price=" + price + ", priceLabel=" + priceLabel
				+ ", productShortName=" + productShortName + ", productLongName=" + productLongName + ", brandName="
				+ brandName + ", priceConfidence=" + priceConfidence + ", taskId=" + taskId + ", shelfLevel="
				+ shelfLevel + ", projectId=" + projectId + ", id=" + id + ", compliance=" + compliant + "]";
	}
}

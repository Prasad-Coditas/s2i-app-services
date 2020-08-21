package com.snap2buy.themobilebackend.model;

public class StoreUPC {

	private String imageUUID;
	private String customerCode;
	private String projectId;
	private String storeId;
	private String upc;
	private String brand_name;
	private String product_short_name;
	private String product_long_name;
	private String facing;
	private String upcConfidence;
	private String price;
	private String priceConfidence;
	private String promotion;
	private String taskId;
	private String shelfLevel;
	private String product_type;
	private String product_sub_type;
	private String skuTypeId;


	public String getShelfLevel() {
		return shelfLevel;
	}

	public void setShelfLevel(String shelfLevel) {
		this.shelfLevel = shelfLevel;
	}

	public String getImageUUID() {
		return imageUUID;
	}

	public void setImageUUID(String imageUUID) {
		if (imageUUID == null)
			imageUUID = "";
		this.imageUUID = imageUUID;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		if (customerCode == null)
			customerCode = "";
		this.customerCode = customerCode;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		if (storeId == null)
			storeId = "";
		this.storeId = storeId;
	}

	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		if (upc == null)
			upc = "";
		this.upc = upc;
	}

	public String getBrand_name() {
		return brand_name;
	}

	public void setBrand_name(String brand_name) {
		if (brand_name == null)
			brand_name = "";
		this.brand_name = brand_name;
	}

	public String getProduct_short_name() {
		return product_short_name;
	}

	public void setProduct_short_name(String product_short_name) {
		if (product_short_name == null)
			product_short_name = "";
		this.product_short_name = product_short_name;
	}

	public String getProduct_long_name() {
		return product_long_name;
	}

	public void setProduct_long_name(String product_long_name) {
		if (product_long_name == null)
			product_long_name = "";
		this.product_long_name = product_long_name;
	}

	public String getFacing() {
		return facing;
	}

	public void setFacing(String facing) {
		if (facing == null)
			facing = "";
		this.facing = facing;
	}

	public String getUpcConfidence() {
		return upcConfidence;
	}

	public void setUpcConfidence(String upcConfidence) {
		if (upcConfidence == null)
			upcConfidence = "";
		this.upcConfidence = upcConfidence;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		if (price == null)
			price = "";
		this.price = price;
	}

	public String getPriceConfidence() {
		return priceConfidence;
	}

	public void setPriceConfidence(String priceConfidence) {
		if (priceConfidence == null)
			priceConfidence = "";
		this.priceConfidence = priceConfidence;
	}

	public String getPromotion() {
		return promotion;
	}

	public void setPromotion(String promotion) {
		if (promotion == null)
			promotion = "";
		this.promotion = promotion;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}


	public String getProduct_type() {
		return product_type;
	}

	public void setProduct_type(String product_type) {
		this.product_type = product_type;
	}

	public String getProduct_sub_type() {
		return product_sub_type;
	}

	public void setProduct_sub_type(String product_sub_type) {
		this.product_sub_type = product_sub_type;
	}

	public String getSkuTypeId() {
		return skuTypeId;
	}

	public void setSkuTypeId(String skuTypeId) {
		this.skuTypeId = skuTypeId;
	}
	
}

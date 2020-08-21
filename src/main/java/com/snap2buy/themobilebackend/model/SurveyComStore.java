package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComStore {
	String retailerStoreId;
	String resultId;
	String processedDate;
	String productCount;
	List<SurveyComProduct> productList;
	
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
	public String getProductCount() {
		return productCount;
	}
	public void setProductCount(String productCount) {
		this.productCount = productCount;
	}
	public List<SurveyComProduct> getProductList() {
		return productList;
	}
	public void setProductList(List<SurveyComProduct> productList) {
		this.productList = productList;
	}
	
}

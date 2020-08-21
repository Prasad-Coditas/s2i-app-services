package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComProduct {
	String upc;
	String productShortName;
	String facings;
	
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public String getProductShortName() {
		return productShortName;
	}
	public void setProductShortName(String productShortName) {
		this.productShortName = productShortName;
	}
	public String getFacings() {
		return facings;
	}
	public void setFacings(String facings) {
		this.facings = facings;
	}
}

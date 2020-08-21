package com.snap2buy.themobilebackend.model;

import java.util.HashSet;
import java.util.Set;

public class UPCPOGEntity {
	String upc;
	Set<String> issueTypes = new HashSet<String>();
	String productShortName = "";
	String expectedLocation = "";
	String expectedFacings = "";
	String actualFacings = "0";
	
	@Override
	public String toString() {
		return "UPCPOGEntity [upc=" + upc + ", issueTypes=" + issueTypes + ", productShortName=" + productShortName
				+ ", expectedLocation=" + expectedLocation + ", expectedFacings=" + expectedFacings + ", actualFacings="
				+ actualFacings + "]";
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public Set<String> getIssueTypes() {
		return issueTypes;
	}
	public void setIssueTypes(Set<String> issueTypes) {
		this.issueTypes = issueTypes;
	}
	public String getProductShortName() {
		return productShortName;
	}
	public void setProductShortName(String productShortName) {
		this.productShortName = productShortName;
	}
	public String getExpectedLocation() {
		return expectedLocation;
	}
	public void setExpectedLocation(String expectedLocation) {
		this.expectedLocation = expectedLocation;
	}
	public String getExpectedFacings() {
		return expectedFacings;
	}
	public void setExpectedFacings(String expectedFacings) {
		this.expectedFacings = expectedFacings;
	}
	public String getActualFacings() {
		return actualFacings;
	}
	public void setActualFacings(String actualFacings) {
		this.actualFacings = actualFacings;
	}
	
}

package com.snap2buy.themobilebackend.model;

import java.util.List;
import java.util.Map;

public class ProjectStoreResultWithUPC {
	
//	private String customerCode;
//	private String customerProjectId;
	private String storeId;
	private String retailerStoreId;
	private String retailerChainCode;
	private String retailer;
	private String street;
	private String city;
	private String stateCode;
	private String state;
	private String zip;
	private String resultCode;
	private String result;
	private String countDistinctUpc;
	private String countDistinctBrands;
	private String sumFacing;
	private String sumUpcConfidence;
	private String status;
	private String taskId;
	private String visitDateId;
	private String agentId;
	private String processedDate;
	private String linearFootage;
	private String countMissingUpc;
	private String percentageOsa;
	private String projectId;
	private String resultComment;
	private String waveId;
	private String waveName;
	
	private List<StoreUPC> projectUPCs;
	private List<StoreImageDetails> imageUUIDs;
	private List<SkuType> skuTypes;
	private List<Map<String,Object>> scores;
	private List<Map<String,String>> keyMetrics;
	
	public String getResultCode() {
		return resultCode;
	}
	public void setResultCode(String resultCode) {
		if ( resultCode == null ) resultCode ="";
		this.resultCode = resultCode;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		if ( result == null ) result ="";
		this.result = result;
	}
	public String getCountDistinctUpc() {
		return countDistinctUpc;
	}
	public void setCountDistinctUpc(String countDistinctUpc) {
		if ( countDistinctUpc == null ) countDistinctUpc ="";
		this.countDistinctUpc = countDistinctUpc;
	}
	public String getSumFacing() {
		return sumFacing;
	}
	public void setSumFacing(String sumFacing) {
		if ( sumFacing == null ) sumFacing ="";
		this.sumFacing = sumFacing;
	}
	public String getSumUpcConfidence() {
		return sumUpcConfidence;
	}
	public void setSumUpcConfidence(String sumUpcConfidence) {
		if ( sumUpcConfidence == null ) sumUpcConfidence ="";
		this.sumUpcConfidence = sumUpcConfidence;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		if ( status == null ) status ="";
		this.status = status;
	}
	public String getProcessedDate() {
		return processedDate;
	}
	public void setProcessedDate(String processedDate) {
		if ( processedDate == null ) processedDate ="";
		this.processedDate = processedDate;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		if ( storeId == null ) storeId ="";
		this.storeId = storeId;
	}
	public String getRetailerStoreId() {
		return retailerStoreId;
	}
	public void setRetailerStoreId(String retailerStoreId) {
		if ( retailerStoreId == null ) retailerStoreId ="";
		this.retailerStoreId = retailerStoreId;
	}
	public String getRetailerChainCode() {
		return retailerChainCode;
	}
	public void setRetailerChainCode(String retailerChainCode) {
		if ( retailerChainCode == null ) retailerChainCode ="";
		this.retailerChainCode = retailerChainCode;
	}
	public String getRetailer() {
		return retailer;
	}
	public void setRetailer(String retailer) {
		if ( retailer == null ) retailer ="";
		this.retailer = retailer;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		if ( street == null ) street ="";
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		if ( city == null ) city ="";
		this.city = city;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		if ( stateCode == null ) stateCode ="";
		this.stateCode = stateCode;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		if ( state == null ) state ="";
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		if ( zip == null ) zip ="";
		this.zip = zip;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		if ( taskId == null ) taskId ="";
		this.taskId = taskId;
	}
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		if ( agentId == null ) agentId ="";
		this.agentId = agentId;
	}
	public List<StoreUPC> getProjectUPCs() {
		return projectUPCs;
	}
	public void setProjectUPCs(List<StoreUPC> projectUPCs) {
		this.projectUPCs = projectUPCs;
	}
	public String getVisitDateId() {
		return visitDateId;
	}
	public void setVisitDateId(String visitDateId) {
		this.visitDateId = visitDateId;
	}
	public List<StoreImageDetails> getImageUUIDs() {
		return imageUUIDs;
	}
	public void setImageUUIDs(List<StoreImageDetails> imageUUIDs) {
		this.imageUUIDs = imageUUIDs;
	}

	public String getLinearFootage() {
		return linearFootage;
	}

	public void setLinearFootage(String linearFootage) {
		this.linearFootage = linearFootage;
	}

	public String getCountMissingUpc() {
		return countMissingUpc;
	}

	public void setCountMissingUpc(String countMissingUpc) {
		this.countMissingUpc = countMissingUpc;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getPercentageOsa() {
		return percentageOsa;
	}

	public void setPercentageOsa(String percentageOsa) {
		this.percentageOsa = percentageOsa;
	}
	public String getResultComment() {
		return resultComment;
	}
	public void setResultComment(String resultComment) {
		this.resultComment = resultComment;
	}
	public String getWaveId() {
		return waveId;
	}
	public void setWaveId(String waveId) {
		this.waveId = waveId;
	}
	public String getWaveName() {
		return waveName;
	}
	public void setWaveName(String waveName) {
		this.waveName = waveName;
	}
	public List<SkuType> getSkuTypes() {
		return skuTypes;
	}
	public void setSkuTypes(List<SkuType> skuTypes) {
		this.skuTypes = skuTypes;
	}
	public List<Map<String, Object>> getScores() {
		return scores;
	}
	public void setScores(List<Map<String, Object>> scores) {
		this.scores = scores;
	}
	public List<Map<String, String>> getKeyMetrics() {
		return keyMetrics;
	}
	public void setKeyMetrics(List<Map<String, String>> keyMetrics) {
		this.keyMetrics = keyMetrics;
	}
	public String getCountDistinctBrands() {
		return countDistinctBrands;
	}
	public void setCountDistinctBrands(String countDistinctBrands) {
		this.countDistinctBrands = countDistinctBrands;
	}
	
}

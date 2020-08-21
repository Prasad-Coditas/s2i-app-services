package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 09/26/17.
 */
public class StoreVisitScore {
	String storeId;
    String retailerStoreId;
    String retailer;
    String street;
    String city;
    String state;
    String taskId;
    String agentId;
    String visitDate;
    String processedDate;
    String scoreId;
    String scoreName;
    String score;
    String scoreDesc;
    String scoreComment;
    String imageUUID;
      
    List<StoreVisitScoreComponent> componentScores;

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getRetailerStoreId() {
		return retailerStoreId;
	}

	public void setRetailerStoreId(String retailerStoreId) {
		this.retailerStoreId = retailerStoreId;
	}

	public String getRetailer() {
		return retailer;
	}

	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
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

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}

	public String getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(String processedDate) {
		this.processedDate = processedDate;
	}

	public String getScoreId() {
		return scoreId;
	}

	public void setScoreId(String scoreId) {
		this.scoreId = scoreId;
	}

	public String getScoreName() {
		return scoreName;
	}

	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getScoreDesc() {
		return scoreDesc;
	}

	public void setScoreDesc(String scoreDesc) {
		this.scoreDesc = scoreDesc;
	}

	public String getScoreComment() {
		return scoreComment;
	}

	public void setScoreComment(String scoreComment) {
		this.scoreComment = scoreComment;
	}
	
	public String getImageUUID() {
		return imageUUID;
	}

	public void setImageUUID(String imageUUID) {
		this.imageUUID = imageUUID;
	}

	public List<StoreVisitScoreComponent> getComponentScores() {
		return componentScores;
	}

	public void setComponentScores(List<StoreVisitScoreComponent> componentScores) {
		this.componentScores = componentScores;
	}

	@Override
	public String toString() {
		return "StoreVisitScore [storeId=" + storeId + ", retailerStoreId="
				+ retailerStoreId + ", retailer=" + retailer + ", street="
				+ street + ", city=" + city + ", state=" + state + ", taskId="
				+ taskId + ", agentId=" + agentId + ", visitDate=" + visitDate
				+ ", processedDate=" + processedDate + ", scoreId=" + scoreId
				+ ", scoreName=" + scoreName + ", score=" + score
				+ ", scoreDesc=" + scoreDesc + ", scoreComment=" + scoreComment
				+ ", imageUUID=" + imageUUID +", componentScores=" + componentScores + "]";
	}

}

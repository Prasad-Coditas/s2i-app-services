package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 11/15/18.
 */
public class ProjectStoreGradingCriteria {
	
	int projectId;
	int criteriaSequenceNumber;
	String criteriaName;
	int resultCode;
	String resultColor;
	String criteriaExpression;
	String criteriaComment;
	int storeStatus;
	
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getCriteriaSequenceNumber() {
		return criteriaSequenceNumber;
	}
	public void setCriteriaSequenceNumber(int criteriaSequenceNumber) {
		this.criteriaSequenceNumber = criteriaSequenceNumber;
	}
	public String getCriteriaName() {
		return criteriaName;
	}
	public void setCriteriaName(String criteriaName) {
		this.criteriaName = criteriaName;
	}
	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public String getResultColor() {
		return resultColor;
	}
	public void setResultColor(String resultColor) {
		this.resultColor = resultColor;
	}
	public String getCriteriaExpression() {
		return criteriaExpression;
	}
	public void setCriteriaExpression(String criteriaExpression) {
		this.criteriaExpression = criteriaExpression;
	}
	public String getCriteriaComment() {
		return criteriaComment;
	}
	public void setCriteriaComment(String criteriaComment) {
		this.criteriaComment = criteriaComment;
	}
	public int getStoreStatus() {
		return storeStatus;
	}
	public void setStoreStatus(int storeStatus) {
		this.storeStatus = storeStatus;
	}
	
	@Override
	public String toString() {
		return "ProjectStoreGradingCriteria [projectId=" + projectId + ", criteriaSequenceNumber="
				+ criteriaSequenceNumber + ", criteriaName=" + criteriaName + ", resultCode=" + resultCode
				+ ", resultColor=" + resultColor + ", criteriaExpression=" + criteriaExpression + ", criteriaComment="
				+ criteriaComment + ", storeStatus=" + storeStatus + "]";
	}
	
	
}

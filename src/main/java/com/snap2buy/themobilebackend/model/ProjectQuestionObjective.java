package com.snap2buy.themobilebackend.model;

public class ProjectQuestionObjective {
	
	String customerCode,
			customerProjectId,
			questionId,
			objectiveId,
			objectiveDesc,
			objectiveType,
			objectiveMetAndPresentCriteria,
			objectiveMetAndPresentComment,
			objectiveNotPresentCriteria,
			objectiveNotPresentComment,
			objectiveMetCriteria,
			objectiveMetComment,
			objectiveFalsifiedCriteria,
			objectiveFalsifiedComment,
			objectiveMismatchCriteria,
			objectiveMismatchComment;

	int projectId;

	public String getCustomerCode() {
		return customerCode;
	}

	public String getObjectiveType() {
		return objectiveType;
	}

	public void setObjectiveType(String objectiveType) {
		this.objectiveType = objectiveType;
	}

	public String getObjectiveMetAndPresentCriteria() {
		return objectiveMetAndPresentCriteria;
	}

	public void setObjectiveMetAndPresentCriteria(
			String objectiveMetAndPresentCriteria) {
		this.objectiveMetAndPresentCriteria = objectiveMetAndPresentCriteria;
	}

	public String getObjectiveMetAndPresentComment() {
		return objectiveMetAndPresentComment;
	}

	public void setObjectiveMetAndPresentComment(
			String objectiveMetAndPresentComment) {
		this.objectiveMetAndPresentComment = objectiveMetAndPresentComment;
	}

	public String getObjectiveNotPresentCriteria() {
		return objectiveNotPresentCriteria;
	}

	public void setObjectiveNotPresentCriteria(String objectiveNotPresentCriteria) {
		this.objectiveNotPresentCriteria = objectiveNotPresentCriteria;
	}

	public String getObjectiveNotPresentComment() {
		return objectiveNotPresentComment;
	}

	public void setObjectiveNotPresentComment(String objectiveNotPresentComment) {
		this.objectiveNotPresentComment = objectiveNotPresentComment;
	}

	
	public String getObjectiveMetComment() {
		return objectiveMetComment;
	}

	public void setObjectiveMetComment(String objectiveMetComment) {
		this.objectiveMetComment = objectiveMetComment;
	}

	public String getObjectiveFalsifiedComment() {
		return objectiveFalsifiedComment;
	}

	public void setObjectiveFalsifiedComment(String objectiveFalsifiedComment) {
		this.objectiveFalsifiedComment = objectiveFalsifiedComment;
	}

	public String getObjectiveMismatchComment() {
		return objectiveMismatchComment;
	}

	public void setObjectiveMismatchComment(String objectiveMismatchComment) {
		this.objectiveMismatchComment = objectiveMismatchComment;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getCustomerProjectId() {
		return customerProjectId;
	}

	public void setCustomerProjectId(String customerProjectId) {
		this.customerProjectId = customerProjectId;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getObjectiveId() {
		return objectiveId;
	}

	public void setObjectiveId(String objectiveId) {
		this.objectiveId = objectiveId;
	}

	public String getObjectiveDesc() {
		return objectiveDesc;
	}

	public void setObjectiveDesc(String objectiveDesc) {
		this.objectiveDesc = objectiveDesc;
	}

	public String getObjectiveMetCriteria() {
		return objectiveMetCriteria;
	}

	public void setObjectiveMetCriteria(String objectiveMetCriteria) {
		this.objectiveMetCriteria = objectiveMetCriteria;
	}

	public String getObjectiveFalsifiedCriteria() {
		return objectiveFalsifiedCriteria;
	}

	public void setObjectiveFalsifiedCriteria(String objectFalsifiedCriteria) {
		this.objectiveFalsifiedCriteria = objectFalsifiedCriteria;
	}

	public String getObjectiveMismatchCriteria() {
		return objectiveMismatchCriteria;
	}

	public void setObjectiveMismatchCriteria(String objectiveMismatchCriteria) {
		this.objectiveMismatchCriteria = objectiveMismatchCriteria;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
}

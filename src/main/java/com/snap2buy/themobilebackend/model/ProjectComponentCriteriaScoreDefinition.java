package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Anoop on 02/20/19.
 */
public class ProjectComponentCriteriaScoreDefinition {
	
	@XmlElement
	int projectId;

	@XmlElement
	int scoreId;
	
	@XmlElement
	int componentScoreId;
	
	@XmlElement
	int groupId;
	
	@XmlElement
	int groupSequenceNumber;
	
	@XmlElement
	String criteria;
	
	@XmlElement
	String points;
	
	@XmlElement
	String comment;
	
	@XmlElement
	String action;
	
	@XmlElement
	String focusCriteria;
	
	@XmlElement
	String criteriaDesc;

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public int getScoreId() {
		return scoreId;
	}

	public void setScoreId(int scoreId) {
		this.scoreId = scoreId;
	}

	public int getComponentScoreId() {
		return componentScoreId;
	}

	public void setComponentScoreId(int componentScoreId) {
		this.componentScoreId = componentScoreId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupSequenceNumber() {
		return groupSequenceNumber;
	}

	public void setGroupSequenceNumber(int groupSequenceNumber) {
		this.groupSequenceNumber = groupSequenceNumber;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String getFocusCriteria() {
		return focusCriteria;
	}

	public void setFocusCriteria(String focusCriteria) {
		this.focusCriteria = focusCriteria;
	}

	public String getCriteriaDesc() {
		return criteriaDesc;
	}

	public void setCriteriaDesc(String criteriaDesc) {
		this.criteriaDesc = criteriaDesc;
	}

	@Override
	public String toString() {
		return "ProjectComponentCriteriaScoreDefinition [projectId=" + projectId + ", scoreId=" + scoreId
				+ ", componentScoreId=" + componentScoreId + ", groupId=" + groupId + ", groupSequenceNumber="
				+ groupSequenceNumber + ", criteria=" + criteria + ", points=" + points + ", comment=" + comment
				+ ", action=" + action + ", focusCriteria=" + focusCriteria + ", criteriaDesc=" + criteriaDesc + "]";
	}
	
}

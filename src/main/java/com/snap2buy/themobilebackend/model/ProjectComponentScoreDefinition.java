package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anoop on 02/20/19.
 */
public class ProjectComponentScoreDefinition {
	
	@XmlElement
	int projectId;

	@XmlElement
	int scoreId;
	
	@XmlElement
	int componentScoreId;
	
	@XmlElement
	String componentScoreName;
	
	@XmlElement
	String componentMaxScore;
	
	@XmlElement
	String weightage;
	
	@XmlElement(name = "componentCriteriaScores")
	List<ProjectComponentCriteriaScoreDefinition> componentCriteriaScores = new ArrayList<ProjectComponentCriteriaScoreDefinition>();

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

	public String getComponentScoreName() {
		return componentScoreName;
	}

	public void setComponentScoreName(String componentScoreName) {
		this.componentScoreName = componentScoreName;
	}

	public String getComponentMaxScore() {
		return componentMaxScore;
	}

	public void setComponentMaxScore(String componentMaxScore) {
		this.componentMaxScore = componentMaxScore;
	}

	public String getWeightage() {
		return weightage;
	}

	public void setWeightage(String weightage) {
		this.weightage = weightage;
	}

	public List<ProjectComponentCriteriaScoreDefinition> getComponentCriteriaScores() {
		return componentCriteriaScores;
	}

	public void setComponentCriteriaScores(List<ProjectComponentCriteriaScoreDefinition> componentCriteriaScores) {
		this.componentCriteriaScores = componentCriteriaScores;
	}

	@Override
	public String toString() {
		return "ProjectComponentScoreDefinition [projectId=" + projectId + ", scoreId=" + scoreId
				+ ", componentScoreId=" + componentScoreId + ", componentScoreName=" + componentScoreName
				+ ", componentMaxScore=" + componentMaxScore + ", weightage=" + weightage + ", componentCriteriaScores="
				+ componentCriteriaScores + "]";
	}

}

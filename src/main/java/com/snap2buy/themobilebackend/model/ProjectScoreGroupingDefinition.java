package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Anoop on 02/20/19.
 */
public class ProjectScoreGroupingDefinition {
	
	@XmlElement
	int projectId;

	@XmlElement
	int scoreId;
	
	@XmlElement
	int scoreGroupId;
	
	@XmlElement
	String scoreGroupName;
	
	@XmlElement
	String scoreGroupColor;
	
	@XmlElement
	String groupMinScore;
	
	@XmlElement
	String groupMaxScore;
	
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

	public int getScoreGroupId() {
		return scoreGroupId;
	}

	public void setScoreGroupId(int scoreGroupId) {
		this.scoreGroupId = scoreGroupId;
	}

	public String getScoreGroupName() {
		return scoreGroupName;
	}

	public void setScoreGroupName(String scoreGroupName) {
		this.scoreGroupName = scoreGroupName;
	}

	public String getScoreGroupColor() {
		return scoreGroupColor;
	}

	public void setScoreGroupColor(String scoreGroupColor) {
		this.scoreGroupColor = scoreGroupColor;
	}

	public String getGroupMinScore() {
		return groupMinScore;
	}

	public void setGroupMinScore(String groupMinScore) {
		this.groupMinScore = groupMinScore;
	}

	public String getGroupMaxScore() {
		return groupMaxScore;
	}

	public void setGroupMaxScore(String groupMaxScore) {
		this.groupMaxScore = groupMaxScore;
	}

	@Override
	public String toString() {
		return "ProjectScoreGroupingDefinition [projectId=" + projectId + ", scoreId=" + scoreId + ", scoreGroupId="
				+ scoreGroupId + ", scoreGroupName=" + scoreGroupName + ", scoreGroupColor=" + scoreGroupColor
				+ ", groupMinScore=" + groupMinScore + ", groupMaxScore=" + groupMaxScore + "]";
	}

}

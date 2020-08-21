package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anoop on 02/20/19.
 */
@XmlRootElement(name = "ProjectScore")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectScoreDefinition {
	
	@XmlElement
	int projectId;
	
	@XmlElement
	int scoreId;
	
	@XmlElement
	String scoreName;

	@XmlElement(name = "componentScores")
	List<ProjectComponentScoreDefinition> componentScores = new ArrayList<ProjectComponentScoreDefinition>();
	
	@XmlElement(name = "scoreGrouping")
	List<ProjectScoreGroupingDefinition> scoreGrouping = new ArrayList<ProjectScoreGroupingDefinition>();
	
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
	public String getScoreName() {
		return scoreName;
	}
	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}
	public List<ProjectComponentScoreDefinition> getComponentScores() {
		return componentScores;
	}
	public void setComponentScores(List<ProjectComponentScoreDefinition> componentScores) {
		this.componentScores = componentScores;
	}
	public List<ProjectScoreGroupingDefinition> getScoreGrouping() {
		return scoreGrouping;
	}
	public void setScoreGrouping(List<ProjectScoreGroupingDefinition> scoreGrouping) {
		this.scoreGrouping = scoreGrouping;
	}
	@Override
	public String toString() {
		return "ProjectScoreDefinition [projectId=" + projectId + ", scoreId=" + scoreId + ", scoreName=" + scoreName
				+ ", componentScores=" + componentScores + ", scoreGrouping=" + scoreGrouping + "]";
	}
}

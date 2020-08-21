package com.snap2buy.themobilebackend.model;

public class StoreVisitScoreComponent {
	
	String componentId;
    String componentScoreName;
    String componentScore;
    String componentScoreDesc;
    String componentScoreComment;
	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public String getComponentScoreName() {
		return componentScoreName;
	}
	public void setComponentScoreName(String componentScoreName) {
		this.componentScoreName = componentScoreName;
	}
	public String getComponentScore() {
		return componentScore;
	}
	public void setComponentScore(String componentScore) {
		this.componentScore = componentScore;
	}
	public String getComponentScoreDesc() {
		return componentScoreDesc;
	}
	public void setComponentScoreDesc(String componentScoreDesc) {
		this.componentScoreDesc = componentScoreDesc;
	}
	public String getComponentScoreComment() {
		return componentScoreComment;
	}
	public void setComponentScoreComment(String componentScoreComment) {
		this.componentScoreComment = componentScoreComment;
	}
	@Override
	public String toString() {
		return "StoreVisitScoreComponent [componentId=" + componentId
				+ ", componentScoreName=" + componentScoreName
				+ ", componentScore=" + componentScore
				+ ", componentScoreDesc=" + componentScoreDesc
				+ ", componentScoreComment=" + componentScoreComment + "]";
	}
}

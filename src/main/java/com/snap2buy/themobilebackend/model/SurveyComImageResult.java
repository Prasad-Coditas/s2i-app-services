package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComImageResult {
	String projectId;
	String imageCount;
	List<SurveyComImage> imageList;
	
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getImageCount() {
		return imageCount;
	}
	public void setImageCount(String imageCount) {
		this.imageCount = imageCount;
	}
	public List<SurveyComImage> getImageList() {
		return imageList;
	}
	public void setImageList(List<SurveyComImage> imageList) {
		this.imageList = imageList;
	}
	
}

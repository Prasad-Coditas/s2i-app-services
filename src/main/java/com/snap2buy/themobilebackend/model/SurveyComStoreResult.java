package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComStoreResult {
	String projectId;
	String recordCount;
	List<SurveyComStore> recordList;
	
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(String recordCount) {
		this.recordCount = recordCount;
	}
	public List<SurveyComStore> getRecordList() {
		return recordList;
	}
	public void setRecordList(List<SurveyComStore> recordList) {
		this.recordList = recordList;
	}
}

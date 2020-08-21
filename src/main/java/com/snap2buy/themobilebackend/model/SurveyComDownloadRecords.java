package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SurveyComDownloadRecords {
	
	String projectId;
	String recordCount;
	List<SurveyComDownloadRecord> recordList;
	Integer recordTotal;
	Integer page;
	Integer num;
	
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
	public List<SurveyComDownloadRecord> getRecordList() {
		return recordList;
	}
	public void setRecordList(List<SurveyComDownloadRecord> recordList) {
		this.recordList = recordList;
	}
	public Integer getRecordTotal() {
		return recordTotal;
	}
	public void setRecordTotal(Integer recordTotal) {
		this.recordTotal = recordTotal;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	
	@Override
	public String toString() {
		return "SurveyComDownloadRecords [projectId=" + projectId
				+ ", recordCount=" + recordCount + ", recordList=" + recordList
				+ ", recordTotal=" + recordTotal + ", page=" + page + ", num="
				+ num + "]";
	}
}

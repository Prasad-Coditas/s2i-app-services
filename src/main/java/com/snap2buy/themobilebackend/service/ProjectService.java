package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.ProjectRepResponse;

import java.util.LinkedHashMap;
import java.util.List;

public interface ProjectService {

	List<LinkedHashMap<String, String>> getProjectByCustomerAndStatus(String customerCode, String status);
	
	List<LinkedHashMap<String, String>> getProjectsByCustomerCodeAndCustomerProjectId(int projectId);

	void saveProjectRepResponses(ProjectRepResponse projectRepResponse);
}

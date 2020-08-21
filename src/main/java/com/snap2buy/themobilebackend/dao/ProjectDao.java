package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.ProjectRepResponse;

import java.util.LinkedHashMap;
import java.util.List;

public interface ProjectDao {

	List<LinkedHashMap<String, String>> getProjectByCustomerAndStatus(String customerCode, String status);

	List<LinkedHashMap<String, String>> getProjectsByCustomerCodeAndCustomerProjectId(int projectId);

	void saveProjectRepResponses(ProjectRepResponse projectRepResponse);

}

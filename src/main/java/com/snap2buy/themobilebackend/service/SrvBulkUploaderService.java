package com.snap2buy.themobilebackend.service;

import java.util.LinkedHashMap;

import com.snap2buy.themobilebackend.model.InputObject;

/**
 * Created by Anoop on 9/12/17.
 */
public interface SrvBulkUploaderService {

    public LinkedHashMap<String, String> loadSurveyData(InputObject inputObject);

	public String uploadSurveyImageResults(InputObject inputObject);

	public String uploadSurveyStoreVisitResults(InputObject inputObject);

}

package com.snap2buy.themobilebackend.service;



import com.snap2buy.themobilebackend.model.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 2/15/17.
 */
public interface PrmBulkUploaderService {

    public String getToken() throws IOException;
    public String extractToken(String response);
    public UrlEncodedFormEntity getUrlEncodedParams() throws UnsupportedEncodingException;
    List<PrmResponse> getPrmResponse(String token, String assessmentId) throws IOException;
    public LinkedHashMap<String, String> loadPremiumData(InputObject inputObject);
    public LinkedHashMap<String, String> loadNewQuestionPremium(InputObject inputObject);
	public LinkedHashMap<String, String> submitPremiumResults(InputObject inputObject);
	public void bulkUploadPremiumResults(List<String> projectIds);
	public List<LinkedHashMap<String, String>> getPremiumBulkUploadStatus();
	public String getJobDetails(String jobId);
}

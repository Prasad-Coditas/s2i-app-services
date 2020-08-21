package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.ShelfAnalysisInput;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 10/17/15.
 */
public interface ShelfAnalysisService {
    public void storeShelfAnalysis(ShelfAnalysisInput shelfAnalysisInput);

    public LinkedHashMap<String, String> getShelfAnalysis(String imageUUID);
    public File getShelfAnalysisCsv(String tempFilePath);

	public void getProjectBrandSummary(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStores(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStates(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllCities(InputObject inputObject);

	public List<LinkedHashMap<String, Object>> getProjectDistributionSummary(InputObject inputObject);

	List<LinkedHashMap<String, String>> getProductDetections(String imageUUID);
}

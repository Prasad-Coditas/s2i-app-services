package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.ShelfAnalysis;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 10/31/15.
 */
public interface ShelfAnalysisDao {
    void storeShelfAnalysis(ShelfAnalysis shelfAnalysis);

    ShelfAnalysis getShelfAnalysis(String imageUUID);

    List<ShelfAnalysis> getShelfAnalysisCsv();

	List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStores(int projectId, String month, String rollup);

	List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStates(int projectId, String month, String rollup);

	List<LinkedHashMap<String, Object>> getProjectBrandSharesAllCities(int projectId, String month, String rollup);

	LinkedHashMap<String, Object> getProjectDistributionSummary(String customerCode, String customerProjectId);

	LinkedHashMap<String, String> getProjectDistributionHighLevelSummary(int projectId, String waveId);

	List<LinkedHashMap<String, String>> getDistributionSummaryUPCLevelData(int projectId, String waveId);

	List<LinkedHashMap<String, String>> getDistributionSummaryStateLevelData(int projectId, String waveId);

	List<LinkedHashMap<String, String>> getDistributionSummaryStoreLevelData(int projectId, String waveId);

	List<LinkedHashMap<String, String>> getProductDetections(String imageUUID);
}

package com.snap2buy.themobilebackend.dao;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 1/19/17.
 */
public interface ReportingServiceDao {
    List<LinkedHashMap<String, String>> getRepPerformance(String customerCode);
    List<LinkedHashMap<String, String>> getRepPerformanceByProject(String customerCode,String agentId);
    List<LinkedHashMap<String, String>> getRepPerformanceByProjectStore(int projectId, String agentId);
    List<LinkedHashMap<String, String>> getRepPerformanceSummary(String customerCode);
    List<LinkedHashMap<String, String>> getRepPerformanceByProjectSummary(String customerCode,String agentId);
    List<LinkedHashMap<String, String>> getRepPerformanceByProjectStoreSummary(int projectId, String agentId);

}

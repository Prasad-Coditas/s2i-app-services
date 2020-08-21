package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.InputObject;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 1/19/17.
 */
public interface ReportingService {

    public List<LinkedHashMap<String, String>> getRepPerformance(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getRepPerformanceByProject(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getRepPerformanceByProjectStore(InputObject inputObject);
}

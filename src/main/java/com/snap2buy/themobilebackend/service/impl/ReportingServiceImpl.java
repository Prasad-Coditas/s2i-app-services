package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.*;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 1/19/17.
 */
@Component(value = BeanMapper.BEAN_REPORTING_SERVICE)
@Scope("prototype")
public class ReportingServiceImpl implements ReportingService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_REPORTING_DAO)
    private ReportingServiceDao reportingServiceDao;

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformance(InputObject inputObject) {
        LOGGER.info("---------------ProcessImageServiceImpl Starts getRepPerformance----------------\n");
        List<LinkedHashMap<String, String>> resultTemp = reportingServiceDao.getRepPerformanceSummary(inputObject.getCustomerCode());

        inputObject.setRepsCount(resultTemp.get(0).getOrDefault("repsCount","no data"));
        inputObject.setStoreVisits(resultTemp.get(0).getOrDefault("storeVisits","no data"));
        inputObject.setTotalProjects(resultTemp.get(0).getOrDefault("totalProjects","no data"));
        List<LinkedHashMap<String, String>> result = reportingServiceDao.getRepPerformance(inputObject.getCustomerCode());

        LOGGER.info("---------------ProcessImageServiceImpl Ends getRepPerformance----------------\n");

        return result;
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProject(InputObject inputObject) {
        LOGGER.info("---------------ProcessImageServiceImpl Starts getRepPerformanceByProject----------------\n");
        List<LinkedHashMap<String, String>> resultTemp = reportingServiceDao.getRepPerformanceByProjectSummary(inputObject.getCustomerCode(), inputObject.getAgentId());

        inputObject.setStoreVisits(resultTemp.get(0).getOrDefault("storeVisits","no data"));
        inputObject.setTotalProjects(resultTemp.get(0).getOrDefault("totalProjects","no data"));
        List<LinkedHashMap<String, String>> result = reportingServiceDao.getRepPerformanceByProject(inputObject.getCustomerCode(),inputObject.getAgentId());


        LOGGER.info("---------------ProcessImageServiceImpl Ends getRepPerformanceByProject----------------\n");

        return result;
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProjectStore(InputObject inputObject) {
        LOGGER.info("---------------ProcessImageServiceImpl Starts getRepPerformanceByProjectStore----------------\n");

        List<LinkedHashMap<String, String>> resultTemp = reportingServiceDao.getRepPerformanceByProjectStoreSummary(inputObject.getProjectId(), inputObject.getAgentId());

        inputObject.setStoreVisits(resultTemp.get(0).getOrDefault("storeVisits","no data"));
        List<LinkedHashMap<String, String>> result = reportingServiceDao.getRepPerformanceByProjectStore(inputObject.getProjectId(), inputObject.getAgentId());

        LOGGER.info("---------------ProcessImageServiceImpl Ends getRepPerformanceByProjectStore----------------\n");

        return result;
    }
}

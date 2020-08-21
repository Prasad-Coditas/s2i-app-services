package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ReportingServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_REPORTING_DAO)
@Scope("prototype")
public class ReportingServiceDaoImpl implements ReportingServiceDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformance(String customerCode) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformance----------------\n");
        String sql = "select agentId, count(distinct(customerProjectId)) as totalProjects, SUM(resultCode=1) as successful, SUM(resultCode=2) as partialSuccess, SUM(resultCode=3) as failed, count(*) as totalTasks , (SUM(resultCode=1)+SUM(resultCode=2)/2)/count(*) as performanceIndex from ProjectStoreResult where customerCode= ?  and agentId is not null and agentId!=\"\" group by agentId order by (SUM(resultCode=1)+SUM(resultCode=2)/2)/count(*) desc,count(*) desc;";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,customerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("agentId", rs.getString("agentId"));
                map.put("totalProjects", rs.getString("totalProjects"));
                map.put("successful", rs.getString("successful"));
                map.put("partialSuccess", rs.getString("partialSuccess"));
                map.put("failed", rs.getString("failed"));
                map.put("totalTasks", rs.getString("totalTasks"));
                map.put("performanceIndex", rs.getString("performanceIndex"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformance----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProject(String customerCode, String agentId) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformanceByProject----------------\n");
        String sql = "select d.name as projectType,agentId,  successful, partialSuccess, Failed, totalTasks,performanceIndex from (select agentId, projectTypeId, successful, partialSuccess, Failed, totalTasks, (successful+partialSuccess/2)/totalTasks as performanceIndex from ( select customerCode,agentId, SUM(resultCode=1) as successful, SUM(resultCode=2) as partialSuccess, SUM(resultCode=3) as Failed, count(*) as totalTasks from ProjectStoreResult where customerCode= ?  and agentId = ? and agentId is not null and agentId!=\"\" group by agentId) a left join Project b on a.customerCode = b.customerCode) c  left join ProjectType d on c.projectTypeId=d.id order by performanceIndex desc,totalTasks desc;";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,customerCode);
            ps.setString(2,agentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("customerProjectId", rs.getString("customerProjectId"));
                map.put("successful", rs.getString("successful"));
                map.put("partialSuccess", rs.getString("partialSuccess"));
                map.put("projectType", rs.getString("projectType"));
                map.put("failed", rs.getString("failed"));
                map.put("totalTasks", rs.getString("totalTasks"));
                map.put("performanceIndex", rs.getString("performanceIndex"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformanceByProject----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProjectStore(int projectId, String agentId) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformanceByProjectStore----------------\n");
        String sql = "select a.agentId ,a.storeId, RetailerStoreID ,Retailer ,City ,StateCode, Street, successful, partialSuccess, Failed, totalTasks, (successful+partialSuccess/2)/totalTasks as performanceIndex from (select agentId, storeId, SUM(resultCode=1) as successful, SUM(resultCode=2) as partialSuccess, SUM(resultCode=3) as Failed, count(*) as totalTasks from ProjectStoreResult where projectId= ?  and agentId =? and agentId is not null and agentId!=\"\" group by agentId,storeId) a left join StoreMaster b on a.storeId = b.StoreID order by (successful+Failed/2)/totalTasks desc , totalTasks desc;";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,projectId);
            ps.setString(2,agentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("storeId"));
                map.put("successful", rs.getString("successful"));
                map.put("partialSuccess", rs.getString("partialSuccess"));
                map.put("failed", rs.getString("failed"));
                map.put("totalTasks", rs.getString("totalTasks"));
                map.put("retailerStoreID", rs.getString("RetailerStoreID"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("street", rs.getString("Street"));
                map.put("performanceIndex", rs.getString("performanceIndex"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformanceByProjectStore----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceSummary(String customerCode) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformanceSummary----------------\n");
        String sql = "select count(distinct(agentId)) as repsCount, customerCode,sum(totalTasks) as storeVisits,count(distinct(customerProjectId)) as totalProjects from (select customerCode,agentId, customerProjectId,SUM(resultCode=1) as successful, SUM(resultCode=2) as partialSuccess, SUM(resultCode=3) as failed, count(*) as totalTasks from ProjectStoreResult where customerCode= ?  and agentId is not null and agentId!=\"\" group by customerCode,agentId, customerProjectId) a group by customerCode;";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,customerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("repsCount", rs.getString("repsCount"));
                map.put("storeVisits", rs.getString("storeVisits"));
                map.put("totalProjects", rs.getString("totalProjects"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformanceSummary----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProjectSummary(String customerCode, String agentId) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformanceByProjectSummary----------------\n");
        String sql = "select customerCode,agentId, count(distinct(customerProjectId)) as totalProjects, count(*) as storeVisits from ProjectStoreResult where customerCode= ?  and agentId = ? and agentId is not null and agentId!=\"\" group by agentId";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,customerCode);
            ps.setString(2,agentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("customerCode", rs.getString("customerCode"));
                map.put("agentId", rs.getString("agentId"));
                map.put("totalProjects", rs.getString("totalProjects"));
                map.put("storeVisits", rs.getString("storeVisits"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformanceByProjectSummary----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getRepPerformanceByProjectStoreSummary(int projectId, String agentId) {
        LOGGER.info("---------------ReportingServiceDaoImpl Starts getRepPerformanceByProjectStoreSummary----------------\n");
        String sql = "select projectId,agentId, count(*) as storeVisits from ProjectStoreResult where projectId= ? and agentId = ? and agentId is not null and agentId!=\"\" group by agentId,projectId;";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,projectId);
            ps.setString(2,agentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectId", rs.getString("projectId"));
                map.put("agentId", rs.getString("agentId"));
                map.put("storeVisits", rs.getString("storeVisits"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ReportingServiceDaoImpl Ends getRepPerformanceByProjectStoreSummary----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }
}
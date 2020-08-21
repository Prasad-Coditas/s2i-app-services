package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ProjectDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.ProjectRepResponse;
import com.snap2buy.themobilebackend.model.ProjectResponse;
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

@Component(value = BeanMapper.BEAN_PROJECT_DAO)
@Scope("prototype")
public class ProjectDaoImpl implements ProjectDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Get Project by customerCode and status
     */
	@Override
	public List<LinkedHashMap<String, String>> getProjectByCustomerAndStatus(String customerCode, String status) {

		LOGGER.info("---------------ProjectDaoImpl Starts getProjectByCustomerAndStatus customerCode ={}, status={}",customerCode, status);
        String sql = "SELECT id, isParentProject, parentProjectId, projectName, customerProjectId, CustomerCodeProjectMap.customerCode,"
        		+ " projectTypeId, categoryId, retailerCode, storeCount, startDate, status, owner, endDate FROM Project "
                + " left join CustomerCodeProjectMap on CustomerCodeProjectMap.projectId = Project.id "
                + " where CustomerCodeProjectMap.customerCode = ? and status = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectId", rs.getString("id"));
                map.put("isParentProject", rs.getString("isParentProject"));
                map.put("parentProjectId", rs.getString("parentProjectId"));
                map.put("projectName", rs.getString("projectName"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("customerProjectId", rs.getString("customerProjectId"));
                map.put("status", rs.getString("status"));
                map.put("owner", rs.getString("owner"));
                map.put("retailerCode", rs.getString("retailerCode"));
                map.put("categoryId", rs.getString("categoryId"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProjectDaoImpl Ends getProjectByCustomerAndStatus----------------\n");

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

	/**
	 * Method to get Projects by Id
	 * @return List of LinkedHashMap<String, String>
	 */
	@Override
	public List<LinkedHashMap<String, String>> getProjectsByCustomerCodeAndCustomerProjectId(int projectId) {
		LOGGER.info("---------------ProjectDaoImpl Starts getProjectsByCustomerCodeAndCustomerProjectId projectId = {}", projectId);
        String sql = "SELECT id,projectName,CustomerCodeProjectMap.customerCode, customerProjectId, projectTypeId, categoryId, retailerCode, storeCount,"
        		 + "startDate, status, owner, endDate FROM Project "
                + " left join CustomerCodeProjectMap on CustomerCodeProjectMap.projectId = Project.id "
                + " where id = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectName", rs.getString("projectName"));
                map.put("customerProjectId", rs.getString("customerProjectId"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("id", rs.getString("id"));
                map.put("status", rs.getString("status"));
                map.put("retailerCode", rs.getString("retailerCode"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProjectDaoImpl Ends getProjectsByCustomerCodeAndCustomerProjectId----------------\n");

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
    public void saveProjectRepResponses(ProjectRepResponse projectRepResponse) {
        LOGGER.info("Starts saveProjectRepResponses : saveProjectRepResponses = {}", projectRepResponse);

        String sql = "INSERT INTO ProjectRepResponses (storeId, questionId, repResponse, taskId, projectId) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;

        final int batchSize = 1000;
        int count = 0;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            for(ProjectResponse projectResponse: projectRepResponse.getProjectResponseList()) {

                ps.setString(1, projectRepResponse.getStoreId());
                ps.setInt(2, Integer.valueOf(projectResponse.getQuestionId()));
                ps.setString(3, projectResponse.getResponse());
                ps.setString(4, projectRepResponse.getTaskId());
                ps.setInt(5, Integer.valueOf(projectRepResponse.getProjectId()));

                ps.addBatch();

                if(++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            conn.commit();

            ps.close();
            LOGGER.info("Ends saveProjectRepResponses");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
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

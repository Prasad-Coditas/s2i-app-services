package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

import static com.snap2buy.themobilebackend.util.ConverterUtil.ifNullToEmpty;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_META_SERVICE_DAO)
@Scope("prototype")
public class MetaServiceDaoImpl implements MetaServiceDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<LinkedHashMap<String, String>> listCategory() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listCategory----------------\n");
        String sql = "SELECT * FROM Category where status = 1";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listCategory----------------\n");

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
    public List<LinkedHashMap<String, String>> listCustomer() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listCustomer----------------\n");
        String sql = "SELECT * FROM Customer  where status = 1";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("name", rs.getString("name"));
                map.put("type", rs.getString("type"));
                map.put("logo", rs.getString("logo"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listCustomer----------------\n");

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
    public List<LinkedHashMap<String, Object>> listProject(String customerCode, Boolean isParentProject, String source) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listProject----------------\n");
        
        String sql = "select project.id, project.customerProjectId, project.projectName, project.storeCount, project.startDate, project.createdDate, project.createdBy, project.updatedDate,project.status,project.description,project.owner,project.endDate, CustomerCodeProjectMap.customerCode,project.categoryId, category.name as categoryName, project.projectTypeId, projectType.name as projectTypeName, project.retailerCode, retailer.name as retailerName, project.blurThreshold from Project project"
        		+ " left join Category category on project.categoryId = category.id left join ProjectType projectType on project.projectTypeId = projectType.id left join Retailer retailer on project.retailerCode = retailer.retailerCode"
                + " left join CustomerCodeProjectMap on CustomerCodeProjectMap.projectId = project.id "
        		+ " where project.status = 1 and CustomerCodeProjectMap.customerCode = ? "
        		+ " and ( project.isParentProject = ? OR project.id = project.parentProjectId)";
        
        boolean callFromApp = source.equalsIgnoreCase("app");

        if(callFromApp) { 
            sql += " and date_format(str_to_date(endDate, '%m/%d/%Y'), '%Y%m%d') >= date_format(str_to_date(?, '%m/%d/%Y'), '%Y%m%d')";
        }

        sql += " order by project.id desc";

        List<LinkedHashMap<String, Object>> resultList = new ArrayList<>();
        List<ProjectQuestion> projectQuestions = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setByte(2, (byte)(isParentProject ? 0 : 1));

            if(callFromApp) {
            	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                String endDate = formatter.format(new Date());
                ps.setString(3, endDate);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("id", ifNullToEmpty(rs.getString("id")));
                map.put("projectName", ifNullToEmpty(rs.getString("projectName")));
                map.put("customerProjectId", ifNullToEmpty(rs.getString("customerProjectId")));
                map.put("customerCode", ifNullToEmpty(rs.getString("customerCode")));
                map.put("projectTypeId", ifNullToEmpty(rs.getString("projectTypeId")));
                map.put("projectType", ifNullToEmpty(rs.getString("projectTypeName")));
                map.put("categoryId", ifNullToEmpty(rs.getString("categoryId")));
                map.put("category", ifNullToEmpty(rs.getString("categoryName")));
                map.put("retailerCode", ifNullToEmpty(rs.getString("retailerCode")));
                map.put("retailer", ifNullToEmpty(rs.getString("retailerName")));
                map.put("storeCount", ifNullToEmpty(rs.getString("storeCount")));
                map.put("startDate", ifNullToEmpty(rs.getString("startDate")));
                map.put("createdDate", ifNullToEmpty(rs.getString("createdDate")));
                map.put("createdBy", ifNullToEmpty(rs.getString("createdBy")));
                map.put("updatedDate", ifNullToEmpty(rs.getString("updatedDate")));
                map.put("status", ifNullToEmpty(rs.getString("status")));
                map.put("description", ifNullToEmpty(rs.getString("description")));
                map.put("owner", ifNullToEmpty(rs.getString("owner")));
                map.put("endDate", ifNullToEmpty(rs.getString("endDate")));
                map.put("blurThreshold", ifNullToEmpty(rs.getString("blurThreshold")));

                if(callFromApp) { //Include project questions in response if the call is from app.
                    projectQuestions = getProjectRepQuestions(ifNullToEmpty(rs.getString("id")), conn);
                    map.put("projectQuestionsList", projectQuestions);
                } else { //Include wave information in response if the call is from web.
                    List<Map<String,String>> waves = getProjectWaves(rs.getString("id"), conn);
                    map.put("waves", waves);
                }

                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listProject----------------\n");

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

    private List<Map<String, String>> getProjectWaves(String projectId, Connection conn) {
    	LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectWaves With Connection----------------\n");
        String sql = "SELECT waveId, waveName FROM ProjectWaveConfig WHERE projectId = ? order by waveId";
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("waveId", rs.getString("waveId"));
                map.put("waveName", rs.getString("waveName"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectWaves With Connection----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
	}
    
    @Override
    public List<Map<String, String>> getProjectWaves(int projectId) {
    	LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectWaves----------------\n");
        String sql = "SELECT waveId, waveName FROM ProjectWaveConfig WHERE projectId = ? order by waveId";
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        Connection conn = null;
        try {
        	conn = dataSource.getConnection();
        	PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("waveId", rs.getString("waveId"));
                map.put("waveName", rs.getString("waveName"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectWaves----------------\n");

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
    public List<LinkedHashMap<String, String>> listProjectType() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listProjectType----------------\n");
        String sql = "SELECT * FROM ProjectType where status = 1";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listProjectType----------------\n");

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
    public List<LinkedHashMap<String, String>> listSkuType() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listSkuType----------------\n");
        String sql = "SELECT * FROM SkuType where status = 1";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listSkuType----------------\n");

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
    public List<LinkedHashMap<String, String>> listProjectUpc(int projectId) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listProjectUpc----------------\n");
        String sql = "SELECT pu.id, pu.projectId,pu.upc,pu.skuTypeId,pu.expectedFacingCount,pu.imageUrl1,pu.imageUrl2,pu.imageUrl3,"
        		+ "pm.PRODUCT_SHORT_NAME, pm.PRODUCT_LONG_NAME,pm.BRAND_NAME FROM ProjectUpc pu LEFT JOIN ProductMaster pm ON pu.upc = pm.upc where pu.projectId = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("projectId", rs.getInt("projectId")+"");
                map.put("upc", rs.getString("upc"));
                map.put("skuTypeId", rs.getString("skuTypeId"));
                map.put("expectedFacingCount", rs.getString("expectedFacingCount"));
                map.put("imageUrl1", rs.getString("imageUrl1"));
                map.put("imageUrl2", rs.getString("imageUrl2"));
                map.put("imageUrl3", rs.getString("imageUrl3"));
                map.put("productShortName", ifNullToEmpty(rs.getString("PRODUCT_SHORT_NAME")));
                map.put("productLongName", ifNullToEmpty(rs.getString("PRODUCT_LONG_NAME")));
                map.put("brandName", ifNullToEmpty(rs.getString("BRAND_NAME")));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listProjectUpc----------------\n");

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
    public List<LinkedHashMap<String, String>> listRetailer() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listRetailer----------------\n");
        String sql = "SELECT * FROM Retailer where status = 1";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("retailerCode", rs.getString("retailerCode"));
                map.put("name", rs.getString("name"));
                map.put("type", rs.getString("type"));
                map.put("logo", rs.getString("logo"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listRetailer----------------\n");

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
    public List<LinkedHashMap<String, String>> getRetailerDetail(String retailerCode) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getRetailerDetail retailerCode = {}",retailerCode);
        String sql = "SELECT * FROM Retailer where retailerCode = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("retailerCode", rs.getString("retailerCode"));
                map.put("name", rs.getString("name"));
                map.put("type", rs.getString("type"));
                map.put("logo", rs.getString("logo"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getRetailerDetail----------------\n");

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
    public List<ProjectUpc> getProjectUpcDetail(int projectId) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectUpcDetail----------------\n");
        String sql = "SELECT * FROM ProjectUpc  where projectId = ? order by upc";
        List<ProjectUpc> resultList = new ArrayList<ProjectUpc>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProjectUpc projectUpc = new ProjectUpc();
                projectUpc.setId(rs.getString("id"));
                projectUpc.setProjectId(rs.getInt("projectId")+"");
                projectUpc.setUpc(rs.getString("upc"));
                projectUpc.setSkuTypeId(rs.getString("skuTypeId"));
                projectUpc.setExpectedFacingCount(rs.getString("expectedFacingCount"));
                String url = rs.getString("imageUrl1");
                projectUpc.setImageUrl1( url == null ? "" : url );
                url = rs.getString("imageUrl2");
                projectUpc.setImageUrl2(url == null ? "" : url );
                url = rs.getString("imageUrl3");
                projectUpc.setImageUrl3(url == null ? "" : url );
                resultList.add(projectUpc);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectUpcDetail----------------\n");

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
    public List<LinkedHashMap<String, String>> getProjectTypeDetail(String id) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectTypeDetail----------------\n");
        String sql = "SELECT * FROM ProjectType where id = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectTypeDetail----------------\n");

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
    public List<LinkedHashMap<String, String>> getSkuTypeDetail(String id) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getSkuTypeDetail----------------\n");
        String sql = "SELECT * FROM SkuType where id = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getSkuTypeDetail----------------\n");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
            LOGGER.error("exception", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
                    LOGGER.error("exception", e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getProjectDetail(int projectId) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectDetail ::ProjectId::{}",projectId);
        String sql = "select project.id, project.projectName, project.customerProjectId, project.storeCount, project.startDate, project.createdDate, project.createdBy,"
        		+ " project.updatedDate,project.status,project.description,project.owner,project.endDate,"
        		+ " project.categoryId, category.name as categoryName, project.projectTypeId, projectType.name as projectTypeName,"
        		+ " project.retailerCode, retailer.name as retailerName, project.isParentProject, project.parentProjectId, project.blurThreshold, project.aggregationType from Project project"
        		+ " left join Category category on project.categoryId = category.id left join ProjectType projectType on project.projectTypeId = projectType.id left join Retailer retailer on project.retailerCode = retailer.retailerCode"
        		+ " where project.id= ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", ifNullToEmpty(rs.getString("id")));
                map.put("projectName", ifNullToEmpty(rs.getString("projectName")));
                map.put("customerProjectId", ifNullToEmpty(rs.getString("customerProjectId")));
                map.put("projectTypeId", ifNullToEmpty(rs.getString("projectTypeId")));
                map.put("projectType", ifNullToEmpty(rs.getString("projectTypeName")));
                map.put("categoryId", ifNullToEmpty(rs.getString("categoryId")));
                map.put("category", ifNullToEmpty(rs.getString("categoryName")));
                map.put("retailerCode", ifNullToEmpty(rs.getString("retailerCode")));
                map.put("retailer", ifNullToEmpty(rs.getString("retailerName")));
                map.put("storeCount", ifNullToEmpty(rs.getString("storeCount")));
                map.put("startDate", ifNullToEmpty(rs.getString("startDate")));
                map.put("createdDate", ifNullToEmpty(rs.getString("createdDate")));
                map.put("createdBy", ifNullToEmpty(rs.getString("createdBy")));
                map.put("updatedDate", ifNullToEmpty(rs.getString("updatedDate")));
                map.put("status", ifNullToEmpty(rs.getString("status")));
                map.put("description", ifNullToEmpty(rs.getString("description")));
                map.put("owner", ifNullToEmpty(rs.getString("owner")));
                map.put("endDate", ifNullToEmpty(rs.getString("endDate")));
                map.put("isParentProject", ifNullToEmpty(rs.getString("isParentProject")));
                map.put("parentProjectId", ifNullToEmpty(rs.getString("parentProjectId")));
                map.put("blurThreshold", ifNullToEmpty(rs.getString("blurThreshold")));
                map.put("aggregationType", ifNullToEmpty(rs.getString("aggregationType")));
                resultList.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectDetail ProjectDetails: {}", resultList);

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
    public List<LinkedHashMap<String, String>> getCustomerDetail(String id) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getCustomerDetail----------------\n");
        String sql = "SELECT * FROM Customer where customerCode = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("name", rs.getString("name"));
                map.put("type", rs.getString("type"));
                map.put("logo", rs.getString("logo"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getCustomerDetail----------------\n");

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
    public List<LinkedHashMap<String, String>> getCategoryDetail(String id) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getCategoryDetail----------------\n");
        String sql = "SELECT * FROM Category where id = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("name", rs.getString("name"));
                map.put("createdDate", rs.getString("createdDate"));
                map.put("status", rs.getString("status"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getCategoryDetail----------------\n");

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
    public void createCustomer(Customer customerInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createCustomer::customerInput={}",customerInput);
        String sql = "INSERT INTO Customer ( customerCode, name, type, logo, createdDate, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customerInput.getCustomerCode());
            ps.setString(2, customerInput.getName());
            ps.setString(3, customerInput.getType());
            ps.setString(4, customerInput.getLogo());
            ps.setString(5, customerInput.getCreatedDate());
            ps.setString(6, customerInput.getStatus());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customerInput.setId(generatedKeys.getString(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createCustomer----------------\n");

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


    @Override
    public void createCategory(Category categoryInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createCategory::categoryInput={}", categoryInput);
        String sql = "INSERT INTO Category ( name, createdDate, status) VALUES (?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ;
            ps.setString(1, categoryInput.getName());
            ps.setString(2, categoryInput.getCreatedDate());
            ps.setString(3, categoryInput.getStatus());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    categoryInput.setId(generatedKeys.getString(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createCategory----------------\n");


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

    @Override
    public void createRetailer(Retailer retailerInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createRetailer::retailerInput={}", retailerInput);
        String sql = "INSERT INTO Retailer ( retailerCode, name, type, logo, createdDate, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ;
            ps.setString(1, retailerInput.getRetailerCode());
            ps.setString(2, retailerInput.getName());
            ps.setString(3, retailerInput.getType());
            ps.setString(4, retailerInput.getLogo());
            ps.setString(5, retailerInput.getCreatedDate());
            ps.setString(6, retailerInput.getStatus());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    retailerInput.setId(generatedKeys.getString(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createRetailer----------------\n");


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

    @Override
    public void createProjectType(ProjectType projectTypeInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createProjectType::projectTypeInput={}", projectTypeInput );
        String sql = "INSERT INTO ProjectType (id, name, createdDate, status) VALUES ( (SELECT MAX(id)+1 FROM ProjectType pt), ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectTypeInput.getName());
            ps.setString(2, projectTypeInput.getCreatedDate());
            ps.setString(3, projectTypeInput.getStatus());
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createProjectType----------------\n");
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

    @Override
    public void createSkuType(SkuType skuTypeInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createSkuType::skuTypeInput= {}" + skuTypeInput);
        String sql = "INSERT INTO SkuType ( name, createdDate, status) VALUES (?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ;
            ps.setString(1, skuTypeInput.getName());
            ps.setString(2, skuTypeInput.getCreatedDate());
            ps.setString(3, skuTypeInput.getStatus());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    skuTypeInput.setId(generatedKeys.getString(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createSkuType----------------\n");


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

    @Override
    public boolean createProject(Project projectInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createProject::projectInput={}", projectInput);
        String sql = "INSERT INTO Project (projectName, projectTypeId, categoryId, retailerCode, storeCount, startDate, createdDate, createdBy, updatedDate, updatedBy, status, description, owner, endDate, customerProjectId, isParentProject, parentProjectId, blurThreshold, aggregationType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        boolean projectCreated = false;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, projectInput.getProjectName());
            ps.setString(2, projectInput.getProjectTypeId());
            ps.setString(3, projectInput.getCategoryId());
            ps.setString(4, projectInput.getRetailerCode());
            ps.setString(5, projectInput.getStoreCount());
            ps.setString(6, projectInput.getStartDate());
            ps.setString(7, projectInput.getCreatedDate());
            ps.setString(8, projectInput.getCreatedBy());
            ps.setString(9, projectInput.getUpdatedDate());
            ps.setString(10, projectInput.getUpdatedBy());
            ps.setString(11, projectInput.getStatus());
            ps.setString(12, projectInput.getDescription());
            ps.setString(13, projectInput.getOwner());
            ps.setString(14, projectInput.getEndDate());
            ps.setString(15, projectInput.getCustomerProjectId());
            ps.setByte(16, (byte)(null != projectInput.getIsParentProject() && projectInput.getIsParentProject().equalsIgnoreCase("0") ? 0 : 1));
            ps.setInt(17, null != projectInput.getParentProjectId() ? Integer.valueOf(projectInput.getParentProjectId()) : null);
            ps.setString(18, projectInput.getBlurThreshold());
            ps.setString(19, StringUtils.isBlank(projectInput.getAggregationType()) ? "0" : projectInput.getAggregationType());
            id = ps.executeUpdate();

            if ((id == 0)||(id == null)) {
                throw new SQLException("Creating project failed, no rows affected.");
            }
            
            int autoGeneratedProjectId;
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                	autoGeneratedProjectId = Integer.parseInt(generatedKeys.getString(1));
                    projectInput.setId(autoGeneratedProjectId+"");
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
            ps.close();

            LOGGER.info("---------------MetaServiceDaoImpl project created with id ={} now adding upc list", projectInput.getId());
            
            String createdDate = projectInput.getStartDate();
            String[] parts = createdDate.split("/");
            Timestamp createdTimestamp = Timestamp.valueOf(parts[2]+"-"+parts[0]+"-"+parts[1]+" 00:00:00");
            this.addUpcListToProjectId(projectInput.getProjectUpcList(), autoGeneratedProjectId, createdTimestamp, conn);
            LOGGER.info("---------------MetaServiceDaoImpl UPC list added -------------");
            
            this.addObjectivesListToProjectId(projectInput.getProjectObjectives(), autoGeneratedProjectId, conn);
            LOGGER.info("---------------MetaServiceDaoImpl Objective list added -------------");
            
            this.addStoreGradingCriteriaListToProjectId(projectInput.getProjectStoreGradingCriteriaList(), autoGeneratedProjectId, conn);
            LOGGER.info("---------------MetaServiceDaoImpl Store Grading Criteria list added -------------");

            this.addProjectRepQuestionsWithDetail(projectInput.getProjectQuestionsList(), String.valueOf(autoGeneratedProjectId), conn);
            conn.commit();
            
            projectCreated = true;
            
            LOGGER.info("---------------MetaServiceDaoImpl Ends createProject----------------\n");

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            projectCreated = false;
        } finally {
            if (conn != null) {
                try {
                	if ( !projectCreated ) {
                        conn.rollback();
                	}
                    conn.setAutoCommit(true);
                	conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
        return projectCreated;
    }

    private void addUpcListToProjectId(List<ProjectUpc> projectUpcList, int projectId, Timestamp projectCreatedDate, Connection conn) throws Exception {
        LOGGER.info("---------------MetaServiceDaoImpl Starts addUpcToProjectId::"
        		+ "projectUpcList={}, ProjectId = {}, timestamp = {}",projectUpcList, projectId, projectCreatedDate);
        
        String sql = "INSERT INTO ProjectUpc ( projectId, upc, skuTypeId, expectedFacingCount, imageUrl1, imageUrl2, imageUrl3, isActive, activeFrom) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (!projectUpcList.isEmpty()) {
            for (ProjectUpc projectUpc : projectUpcList) {
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                	ps.setInt(1, projectId);
                    ps.setString(2, projectUpc.getUpc());
                    ps.setString(3, projectUpc.getSkuTypeId());
                    ps.setString(4, projectUpc.getExpectedFacingCount());
                    ps.setString(5, projectUpc.getImageUrl1());
                    ps.setString(6, projectUpc.getImageUrl2());
                    ps.setString(7, projectUpc.getImageUrl3());
                    ps.setInt(8, 1); //default active
                    ps.setTimestamp(9, projectCreatedDate);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                	LOGGER.error("Failed to add UPC to project. Exception : {}",e.getMessage());
                	throw e;
                } 
            }
        }
    }

    private void updateUpcListToProjectId(List<ProjectUpc> projectUpcList, int projectId, Timestamp projectCreatedDate, Connection conn) throws Exception {
		LOGGER.info("---------------MetaServiceDaoImpl Starts updateUpcListToProjectId::projectUpcList={}, ProjectId = {}",
				projectUpcList, projectId);
		String deleteSql = "delete from ProjectUpc where projectId = ?";

		try {
			PreparedStatement deletePs = conn.prepareStatement(deleteSql);
			deletePs.setInt(1, projectId);
			deletePs.execute();
			deletePs.close();

			if (!projectUpcList.isEmpty()) {
				this.addUpcListToProjectId(projectUpcList, projectId, projectCreatedDate, conn);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to update UPC to project. Exception : {}", e.getMessage());
			throw e;
		}
    }

    @Override
    public void addUpcToProjectId(ProjectUpc projectUpc) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts addUpcToProjectId::projectUpc={}", projectUpc );
        String sql = "INSERT INTO ProjectUpc (projectId, upc, skuTypeId, expectedFacingCount, imageUrl1, imageUrl2, imageUrl3) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.valueOf(projectUpc.getProjectId()));
            ps.setString(2, projectUpc.getUpc());
            ps.setString(3, projectUpc.getSkuTypeId());
            ps.setString(4, projectUpc.getExpectedFacingCount());
            ps.setString(5, projectUpc.getImageUrl1());
            ps.setString(6, projectUpc.getImageUrl2());
            ps.setString(7, projectUpc.getImageUrl3());
            ps.executeUpdate();
            ps.close();

            LOGGER.info("---------------MetaServiceDaoImpl Ends addUpcToProjectId----------------\n");

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

    @Override
    public List<LinkedHashMap<String, String>> listStores() {
        LOGGER.info("---------------MetaServiceDaoImpl Starts listStores----------------\n");
        String sql = "SELECT * FROM StoreMaster";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("StoreID"));
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("retailerChainCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("street", rs.getString("Street"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("zip", rs.getString("ZIP"));
                map.put("latitude", rs.getString("Latitude"));
                map.put("longitude", rs.getString("Longitude"));
                map.put("comments", rs.getString("comments"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listStores----------------\n");

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
    public List<LinkedHashMap<String, String>> getStoreDetail(String storeId) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreDetail----------------\n");
        String sql = "SELECT * FROM StoreMaster where StoreID = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("StoreID"));
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("retailerChainCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("street", rs.getString("Street"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("zip", rs.getString("ZIP"));
                map.put("latitude", rs.getString("Latitude"));
                map.put("longitude", rs.getString("Longitude"));
                map.put("comments", rs.getString("comments"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreDetail----------------\n");

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
    public void createStore(StoreMaster storeMaster) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createStore::storeMaster={}", storeMaster);
        String sql = "INSERT INTO StoreMaster ( StoreID, RetailerStoreID, RetailerChainCode, Retailer, Street, City, StateCode, State, ZIP, Latitude, Longitude, comments, createdDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeMaster.getStoreId());
            ps.setString(2, storeMaster.getRetailerStoreId());
            ps.setString(3, storeMaster.getRetailerChainCode());
            ps.setString(4, storeMaster.getRetailer());
            ps.setString(5, storeMaster.getStreet());
            ps.setString(6, storeMaster.getCity());
            ps.setString(7, storeMaster.getStateCode());
            ps.setString(8, storeMaster.getState());
            ps.setString(9, storeMaster.getZip());
            ps.setString(10, storeMaster.getLatitude());
            ps.setString(11, storeMaster.getLongitude());
            ps.setString(12, storeMaster.getComments());
            ps.setTimestamp(13, timestamp);
            Boolean status = ps.execute();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends createStore----------------\n");

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

    @Override
    public void updateStore(StoreMaster storeMaster) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateStore::storeMaster={}", storeMaster );
        String sql = "UPDATE StoreMaster " +
                "set RetailerStoreID=\"" + storeMaster.getRetailerStoreId() + "\"  " +
                ", RetailerChainCode=\"" + storeMaster.getRetailerChainCode() + "\"  " +
                ", Retailer=\"" + storeMaster.getRetailer() + "\"  " +
                ", Street=\"" + storeMaster.getStreet() + "\"  " +
                ", City=\"" + storeMaster.getCity() + "\"  " +
                ", StateCode=\"" + storeMaster.getStateCode() + "\"  " +
                ", State=\"" + storeMaster.getState() + "\"  " +
                ", ZIP=\"" + storeMaster.getZip() + "\"  " +
                ", Latitude=\"" + storeMaster.getLatitude() + "\"  " +
                ", Longitude=\"" + storeMaster.getLongitude() + "\"  " +
                ", comments=\"" + storeMaster.getComments() + "\"  " +
                "where StoreID=\"" + storeMaster.getStoreId() + "\" ";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            int id = ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends updateStore----------------\n");

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


    @Override
    public boolean updateProject(Project projectInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateProject::projectInput={}", projectInput );
        
        String sql = "UPDATE Project  " +
                "set projectName=?  " +
                ", projectTypeId=?  " +
                ", categoryId=?  " +
                ", retailerCode=?  " +
                ", storeCount=?  " +
                ", startDate=?  " +
                ", createdDate=?  " +
                ", createdBy=?  " +
                ", updatedDate=?  " +
                ", updatedBy=?  " +
                ", status=?  " +
                ", description=?  " +
                ", owner=?  " +
                ", endDate=?  " +
                ", parentProjectId=?  " +
                ", isParentProject=?  " +
                ", customerProjectId=?  " +
                ", blurThreshold=?  " +
                ", aggregationType=?  " +
                "where id= ? ";
        Connection conn = null;
        boolean projectUpdated = false;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectInput.getProjectName());
            ps.setString(2, projectInput.getProjectTypeId());
            ps.setString(3, projectInput.getCategoryId());
            ps.setString(4, projectInput.getRetailerCode());
            ps.setString(5, projectInput.getStoreCount());
            ps.setString(6, projectInput.getStartDate());
            ps.setString(7, projectInput.getCreatedDate());
            ps.setString(8, projectInput.getCreatedBy());
            ps.setString(9, projectInput.getUpdatedDate());
            ps.setString(10, projectInput.getUpdatedBy());
            ps.setString(11, projectInput.getStatus());
            ps.setString(12, projectInput.getDescription());
            ps.setString(13,projectInput.getOwner());
            ps.setString(14,projectInput.getEndDate());            				 
            ps.setInt(15, null != projectInput.getParentProjectId() ? Integer.valueOf(projectInput.getParentProjectId()) : null);
            ps.setByte(16, (byte)(null != projectInput.getIsParentProject() && projectInput.getIsParentProject().equalsIgnoreCase("0") ? 0 : 1));
            ps.setString(17, projectInput.getCustomerProjectId());
            ps.setString(18, projectInput.getBlurThreshold());
            ps.setString(19, StringUtils.isBlank(projectInput.getAggregationType()) ? "0" : projectInput.getAggregationType());
            ps.setInt(20, Integer.valueOf(projectInput.getId()));

            ps.executeUpdate();
            ps.close();
            
            LOGGER.info("---------------MetaServiceDaoImpl project updated with id ={} now updating upc list ", projectInput.getId());
            
            String createdDate = projectInput.getStartDate();
            String[] parts = createdDate.split("/");
            Timestamp createdTimestamp = Timestamp.valueOf(parts[2]+"-"+parts[0]+"-"+parts[1]+" 00:00:00");
            this.updateUpcListToProjectId(projectInput.getProjectUpcList(), Integer.valueOf(projectInput.getId()), createdTimestamp, conn);
            LOGGER.info("---------------MetaServiceDaoImpl upc list updated -------------");
            
            this.updateObjectivesListToProjectId(projectInput.getProjectObjectives(), Integer.valueOf(projectInput.getId()), conn);
            LOGGER.info("---------------MetaServiceDaoImpl objectives list updated -------------");
            
            this.updateStoreGradingCriteriaListToProjectId(projectInput.getProjectStoreGradingCriteriaList(), Integer.valueOf(projectInput.getId()), conn);
            LOGGER.info("---------------MetaServiceDaoImpl store grading criteria list updated -------------");

            this.updateProjectReqQuestionsWithDetails(projectInput.getProjectQuestionsList(), projectInput.getId(), conn);
            LOGGER.info("MetaServiceDaoImpl project questions and questions details list updated -------------");
            projectUpdated = true;
            
            conn.commit();
            
            LOGGER.info("---------------MetaServiceDaoImpl Ends updateProject----------------\n");

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            projectUpdated = false;
        } finally {
            if (conn != null) {
                try {
                	if ( !projectUpdated ) {
                		conn.rollback();
                	}
                	conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
        return projectUpdated;
    }

    @Override
    public Map<String, Object> getProjectSummary(int projectId, String level, String value) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectSummary----------------\n");
        
        String storesSql = "SELECT count(*) as storeCount FROM ProjectStoreResult result " + 
        		"    INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? AND b.visitDateId LIKE ? AND COALESCE(b.waveId,'') LIKE ? GROUP BY b.projectId, b.storeId) c " + 
        		"    ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask " + 
        		"    WHERE result.projectId = ? AND result.visitDateId LIKE ? AND COALESCE(result.waveId,'') LIKE ? AND result.status = ? ";

        String storesByResultSql = "SELECT count(*) as storeCount FROM ProjectStoreResult result " + 
        		"    INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? AND b.visitDateId LIKE ? AND COALESCE(b.waveId,'') LIKE ? GROUP BY b.projectId, b.storeId) c " + 
        		"    ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask " + 
        		"    WHERE result.projectId = ? AND result.visitDateId LIKE ? AND COALESCE(result.waveId,'') LIKE ? AND result.status = '1' AND result.resultCode = ? ";
        
        String monthWiseDataForTrendSql = "SELECT A.resultCode, A.month, A.storeCount, ROUND((A.storeCount/B.storeCount)*100,1) as percentageStores\n" + 
        		"FROM (\n" + 
        		"SELECT resultCode,SUBSTR(visitDateId,1,6) as month,count(*) as storeCount FROM ProjectStoreResult result  \n" + 
        		"INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? GROUP BY b.projectId, b.storeId) c  \n" + 
        		"ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask  \n" + 
        		"WHERE result.projectId = ? AND result.status = '1'\n" + 
        		"GROUP BY resultCode,SUBSTR(visitDateId,1,6)\n" + 
        		") A,\n" + 
        		"(\n" + 
        		"SELECT SUBSTR(visitDateId,1,6) as month,count(*) as storeCount FROM ProjectStoreResult result  \n" + 
        		"INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? GROUP BY b.projectId, b.storeId) c  \n" + 
        		"ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask  \n" + 
        		"WHERE result.projectId = ? AND result.status = '1'\n" + 
        		"GROUP BY SUBSTR(visitDateId,1,6)\n" + 
        		") B\n" + 
        		"WHERE A.month = B.month\n" + 
        		"ORDER BY A.resultCode,A.month";
        
        String waveWiseDataForTrendSql = "SELECT A.resultCode, A.waveId, C.waveName, A.storeCount, ROUND((A.storeCount/B.storeCount)*100,1) as percentageStores\n" + 
        		"FROM (\n" + 
        		"SELECT result.projectId,resultCode,waveId,count(*) as storeCount FROM ProjectStoreResult result  \n" + 
        		"INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? GROUP BY b.projectId, b.storeId) c  \n" + 
        		"ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask  \n" + 
        		"WHERE result.projectId = ? AND result.status = '1'\n" + 
        		"GROUP BY projectId,resultCode, waveId\n" + 
        		") A,\n" + 
        		"(\n" + 
        		"SELECT result.projectId,waveId,count(*) as storeCount FROM ProjectStoreResult result  \n" + 
        		"INNER JOIN ( select b.projectId, b.storeId, MAX(CONCAT(b.visitDateId,b.taskId)) AS maxVisitTask FROM ProjectStoreResult b WHERE b.projectId = ? GROUP BY b.projectId, b.storeId) c  \n" + 
        		"ON result.projectId = c.projectId AND result.storeId = c.storeId AND CONCAT(result.visitDateId,result.taskId) = c.maxVisitTask  \n" + 
        		"WHERE result.projectId = ? AND result.status = '1'\n" + 
        		"GROUP BY result.projectId,waveId\n" + 
        		") B,\n" + 
        		"ProjectWaveConfig C\n" + 
        		"WHERE A.waveId = B.waveId\n" + 
        		"AND A.waveId = C.waveId\n" + 
        		"AND A.projectId = C.projectId\n" + 
        		"ORDER BY A.resultCode,A.waveId";
        
        String projectWavesSql = "SELECT waveId, waveName FROM ProjectWaveConfig WHERE projectId=? ORDER BY waveId";
        String projectMonthsSql = "SELECT DISTINCT(SUBSTR(visitDateId,1,6)) AS month FROM ProjectStoreResult WHERE projectId=? AND status = '1' ORDER BY month";

        
        List<ProjectStoreGradingCriteria> storeGradingCriteriaList = getProjectStoreGradingCriterias(projectId);
		Map<String,List<ProjectStoreGradingCriteria>> storeGradingCriteriaGroupedByResultCode = new LinkedHashMap<String,List<ProjectStoreGradingCriteria>>();
        for ( ProjectStoreGradingCriteria gradingCriteria : storeGradingCriteriaList ) {
			if ( storeGradingCriteriaGroupedByResultCode.get(""+gradingCriteria.getResultCode()) == null ) {
				storeGradingCriteriaGroupedByResultCode.put(""+gradingCriteria.getResultCode(), new ArrayList<ProjectStoreGradingCriteria>());
			}
			storeGradingCriteriaGroupedByResultCode.get(""+gradingCriteria.getResultCode()).add(gradingCriteria);
		}
        
        Map<String,Object> resultMap = new HashMap<String,Object>();
        String storesProcessed = "0";
        String storesNotprocessed = "0";
        
        String monthFilter = "%";
        String waveFilter = "%";
        
        if ( level.equals("wave") ) {
        	waveFilter = value;
        }
        if ( level.equals("month") ) {
        	// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
    		// this logic won't work by 2100 :)
    		String[] parts = value.split("/");
    		value = "20" + parts[1] + parts[0];
        	monthFilter = value+monthFilter; 
        }
        
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement storesPs = conn.prepareStatement(storesSql);
            
            storesPs.setInt(1, projectId);
            storesPs.setString(2, monthFilter);
            storesPs.setString(3, waveFilter);
            storesPs.setInt(4, projectId);
            storesPs.setString(5, monthFilter);
            storesPs.setString(6, waveFilter);
            storesPs.setString(7, "1");
            
            ResultSet storesProcessedRs = storesPs.executeQuery();
            if (storesProcessedRs.next()) {
            	storesProcessed = storesProcessedRs.getString("storeCount");
            	resultMap.put("processedStores", storesProcessed);
            }
            storesProcessedRs.close();

            storesPs.setString(7, "0");
            ResultSet storesNotProcessedRs = storesPs.executeQuery();
            if (storesNotProcessedRs.next()) {
            	storesNotprocessed = storesNotProcessedRs.getString("storeCount");
            	resultMap.put("notProcessedStores", storesNotprocessed);
            }
            storesNotProcessedRs.close();
            storesPs.close();
            
            resultMap.put("totalStores", (Integer.parseInt(storesProcessed)+Integer.parseInt(storesNotprocessed))+"");
            
            resultMap.put("storesByResult", new ArrayList<Map<String,String>>());
            PreparedStatement storesByResultPs = conn.prepareStatement(storesByResultSql);
            for (String resultCode : storeGradingCriteriaGroupedByResultCode.keySet() ) {
            	storesByResultPs.setInt(1, projectId);
            	storesByResultPs.setString(2, monthFilter);
            	storesByResultPs.setString(3, waveFilter);
            	storesByResultPs.setInt(4, projectId);
            	storesByResultPs.setString(5, monthFilter);
            	storesByResultPs.setString(6, waveFilter);
            	storesByResultPs.setString(7, resultCode);
            	ResultSet storesForResultCodeRs = storesByResultPs.executeQuery();
                if (storesForResultCodeRs.next()) {
                	int storesForResultCode = storesForResultCodeRs.getInt("storeCount");
                	Map<String,String> oneResultMap = new HashMap<String,String>();
                	oneResultMap.put("resultCode", resultCode);
                	oneResultMap.put("result", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getCriteriaName());
                	oneResultMap.put("resultColor", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getResultColor());
                	oneResultMap.put("storeCount",  ""+storesForResultCode);
                	((List<Map<String,String>>)resultMap.get("storesByResult")).add(oneResultMap);
                }
                storesForResultCodeRs.close();
            }
            storesByResultPs.close();
            
            if ( level.equals("-9") ) { //high level summary call, response should include wave/month level data for trend line plotting.
            	//all months
                PreparedStatement allMonthsPs = conn.prepareStatement(projectMonthsSql);
                allMonthsPs.setInt(1, projectId);
            	ResultSet allMonthsRs = allMonthsPs.executeQuery();
            	List<String> allMonths  = new ArrayList<String>();
            	while(allMonthsRs.next()) {
            		String oneMonth = allMonthsRs.getString("month");
            		String yearPart = oneMonth.substring(2, 4);
            		String monthPart = oneMonth.substring(4);
                	allMonths.add(monthPart+"/"+yearPart);
            	}
            	allMonthsRs.close();
            	allMonthsPs.close();
            	resultMap.put("months",allMonths);
            	//all waves
            	PreparedStatement allWavesPs = conn.prepareStatement(projectWavesSql);
            	allWavesPs.setInt(1, projectId);
            	ResultSet allWavesRs = allWavesPs.executeQuery();
            	List<Map<String,String>> allWaves  = new ArrayList<Map<String,String>>();
            	while(allWavesRs.next()) {
            		Map<String,String> oneWave = new HashMap<String,String>();
            		oneWave.put("waveId", allWavesRs.getString("waveId"));
            		oneWave.put("waveName", allWavesRs.getString("waveName"));
            		allWaves.add(oneWave);
            	}
            	allWavesRs.close();
            	allWavesPs.close();
            	resultMap.put("waves",allWaves);

            	//month level data
            	PreparedStatement monthTrendPs = conn.prepareStatement(monthWiseDataForTrendSql);
            	monthTrendPs.setInt(1, projectId);
            	monthTrendPs.setInt(2, projectId);
            	monthTrendPs.setInt(3, projectId);
            	monthTrendPs.setInt(4, projectId);
            	ResultSet monthTrendRs = monthTrendPs.executeQuery();
            	Map<String,Map<String,Object>> resultCodeMonthStoreMap = new LinkedHashMap<String,Map<String,Object>>();
            	while(monthTrendRs.next()) {
            		String resultCode = monthTrendRs.getString("resultCode");
            		if ( resultCodeMonthStoreMap.get(resultCode) == null ) {
            			Map<String,Object> oneResultMap = new HashMap<String,Object>();
            			oneResultMap.put("resultCode", resultCode);
                    	oneResultMap.put("result", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getCriteriaName());
                    	oneResultMap.put("resultColor", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getResultColor());
                    	oneResultMap.put("trend", new ArrayList<Map<String,String>>());
                    	resultCodeMonthStoreMap.put(resultCode, oneResultMap);
            		}
            		List<Map<String,String>> trendByMonth = (List<Map<String, String>>) resultCodeMonthStoreMap.get(resultCode).get("trend");
            		Map<String,String> oneMonth = new HashMap<String,String>();
            		String oneYearMonth = monthTrendRs.getString("month");
            		String yearPart = oneYearMonth.substring(2, 4);
            		String monthPart = oneYearMonth.substring(4);
            		oneMonth.put("month", monthPart+"/"+yearPart );
            		oneMonth.put("storeCount", monthTrendRs.getString("storeCount"));
            		oneMonth.put("percentageStores", monthTrendRs.getString("percentageStores"));
            		trendByMonth.add(oneMonth);
            		resultCodeMonthStoreMap.get(resultCode).put("trend", trendByMonth);
            	}
                monthTrendRs.close();
                monthTrendPs.close();
            	resultMap.put("storesByResultByMonth",resultCodeMonthStoreMap.values());

                //wave level data
                PreparedStatement waveTrendPs = conn.prepareStatement(waveWiseDataForTrendSql);
                waveTrendPs.setInt(1, projectId);
                waveTrendPs.setInt(2, projectId);
                waveTrendPs.setInt(3, projectId);
                waveTrendPs.setInt(4, projectId);
            	ResultSet waveTrendRs = waveTrendPs.executeQuery();
            	Map<String,Map<String,Object>> resultCodeWaveStoreMap = new LinkedHashMap<String,Map<String,Object>>();
            	while(waveTrendRs.next()) {
            		String resultCode = waveTrendRs.getString("resultCode");
            		if ( resultCodeWaveStoreMap.get(resultCode) == null ) {
            			Map<String,Object> oneResultMap = new HashMap<String,Object>();
            			oneResultMap.put("resultCode", resultCode);
                    	oneResultMap.put("result", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getCriteriaName());
                    	oneResultMap.put("resultColor", storeGradingCriteriaGroupedByResultCode.get(resultCode).get(0).getResultColor());
                    	oneResultMap.put("trend", new ArrayList<Map<String,String>>());
                    	resultCodeWaveStoreMap.put(resultCode, oneResultMap);
            		}
            		List<Map<String,String>> trendByWave = (List<Map<String, String>>) resultCodeWaveStoreMap.get(resultCode).get("trend");
            		Map<String,String> oneWave = new HashMap<String,String>();
            		oneWave.put("waveId", waveTrendRs.getString("waveId"));
            		oneWave.put("waveName", waveTrendRs.getString("waveName"));
            		oneWave.put("storeCount", waveTrendRs.getString("storeCount"));
            		oneWave.put("percentageStores", waveTrendRs.getString("percentageStores"));
            		trendByWave.add(oneWave);
            		resultCodeWaveStoreMap.get(resultCode).put("trend", trendByWave);
            	}
            	waveTrendRs.close();
                waveTrendPs.close();
            	resultMap.put("storesByResultByWave",resultCodeWaveStoreMap.values());
            }
            
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectSummary----------------\n");

            return resultMap;
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
	public List<ProjectQuestion> getProjectQuestionsDetail(int projectId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectQuestionsDetail----------------\n");
        String sql = "SELECT * FROM ProjectRepQuestions  where projectId = ? order by questionId";
        List<ProjectQuestion> resultList = new ArrayList<ProjectQuestion>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProjectQuestion question = new ProjectQuestion();
                question.setProjectId(rs.getInt("projectId")+"");
                question.setId(""+rs.getInt("questionId"));
                question.setDesc(rs.getString("questionDesc"));
                question.setSequenceNumber(rs.getString("questionSequenceNumber"));
                question.setQuestionType(rs.getString("questionType"));
                question.setSkipImageAnalysis(rs.getString("skipImageAnalysis"));
                resultList.add(question);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectQuestionsDetail----------------\n");

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

	private void addObjectivesListToProjectId(List<ProjectQuestionObjective> projectObjectives,int projectId,Connection conn) throws Exception {
		LOGGER.info("---------------MetaServiceDaoImpl Starts addObjectivesListToProjectId::projectObjectives={}, projectId = ",projectObjectives, projectId);
        String sql = "INSERT INTO ProjectObjectives (projectId, questionId, objectiveId, objectiveDesc, objectiveType, objectiveMetAndPresentCriteria, objectiveMetCriteria, objectiveFalsifiedCriteria, objectiveMismatchCriteria, objectiveMetAndPresentComment, objectiveMetComment, objectiveFalsifiedComment, objectiveMismatchComment, objectiveNotPresentCriteria, objectiveNotPresentComment)"
        		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (!projectObjectives.isEmpty()) {
            for (ProjectQuestionObjective projectObjective : projectObjectives) {
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, projectId);
                    ps.setInt(2, Integer.parseInt(projectObjective.getQuestionId()));
                    ps.setInt(3, Integer.parseInt(projectObjective.getObjectiveId()));
                    ps.setString(4, projectObjective.getObjectiveDesc());
                    ps.setInt(5, Integer.parseInt(projectObjective.getObjectiveType()));
                    ps.setString(6, projectObjective.getObjectiveMetAndPresentCriteria());
                    ps.setString(7, projectObjective.getObjectiveMetCriteria());
                    ps.setString(8, projectObjective.getObjectiveFalsifiedCriteria());
                    ps.setString(9, projectObjective.getObjectiveMismatchCriteria());
                    ps.setString(10, projectObjective.getObjectiveMetAndPresentComment());
                    ps.setString(11, projectObjective.getObjectiveMetComment());
                    ps.setString(12, projectObjective.getObjectiveFalsifiedComment());
                    ps.setString(13, projectObjective.getObjectiveMismatchComment());
                    ps.setString(14, projectObjective.getObjectiveNotPresentCriteria());
                    ps.setString(15, projectObjective.getObjectiveNotPresentComment());
                    ps.executeUpdate();
                    ps.close();

                } catch (Exception e) {
                	LOGGER.error("Failed to add Objectives to project. Exception : {}",e.getMessage());
                	throw e;
                }
            }

            LOGGER.info("---------------MetaServiceDaoImpl Ends addObjectivesListToProjectId----------------\n");
        }
		
	}

	private void updateObjectivesListToProjectId(List<ProjectQuestionObjective> projectObjectives, int projectId, Connection conn) throws Exception {
		LOGGER.info("---------------MetaServiceDaoImpl Starts updateObjectivesListToProjectId::projectObjectives={}, projectId = ",
				projectObjectives, projectId);
		String deleteSql = "delete from ProjectObjectives where projectId = ?";

		try {
			PreparedStatement deletePs = conn.prepareStatement(deleteSql);
			deletePs.setInt(1, projectId);
			deletePs.execute();
			deletePs.close();

			if (!projectObjectives.isEmpty()) {
				this.addObjectivesListToProjectId(projectObjectives, projectId, conn);
			}
			LOGGER.info("---------------MetaServiceDaoImpl Ends updateObjectivesListToProjectId----------------\n");
		} catch (Exception e) {
			LOGGER.error("Failed to update Objectives to project. Exception : {}", e.getMessage());
			throw e;
		}
		
	}

	@Override
	public List<ProjectQuestionObjective> getProjectObjectivesDetail(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectObjectivesDetail:: ProjectId = {}", projectId);
        String sql = "SELECT * FROM ProjectObjectives  WHERE projectId = ? ";
        Connection conn = null;
        List<ProjectQuestionObjective> questionObjectives = new ArrayList<ProjectQuestionObjective>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProjectQuestionObjective objective = new ProjectQuestionObjective();
                objective.setProjectId(rs.getInt("projectId"));
                objective.setQuestionId(String.valueOf(rs.getInt("questionId")));
                objective.setObjectiveId(String.valueOf(rs.getInt("objectiveId")));
                objective.setObjectiveDesc(rs.getString("objectiveDesc"));
                objective.setObjectiveType(String.valueOf(rs.getInt("objectiveType")));
                objective.setObjectiveMetAndPresentCriteria(rs.getString("objectiveMetAndPresentCriteria"));
                objective.setObjectiveMetCriteria(rs.getString("objectiveMetCriteria"));
                objective.setObjectiveFalsifiedCriteria(rs.getString("objectiveFalsifiedCriteria"));
                objective.setObjectiveMismatchCriteria(rs.getString("objectiveMismatchCriteria"));
                objective.setObjectiveMetAndPresentComment(rs.getString("objectiveMetAndPresentComment"));
                objective.setObjectiveMetComment(rs.getString("objectiveMetComment"));
                objective.setObjectiveFalsifiedComment(rs.getString("objectiveFalsifiedComment"));
                objective.setObjectiveMismatchComment(rs.getString("objectiveMismatchComment"));
                objective.setObjectiveNotPresentCriteria(rs.getString("objectiveNotPresentCriteria"));
                objective.setObjectiveNotPresentComment(rs.getString("objectiveNotPresentComment"));
                questionObjectives.add(objective);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectQuestionObjectives numberOfObjectives = "+questionObjectives.size()+"----------------\n");

            return questionObjectives;
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
    public List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCodeAndRetailsStoreId(String retailerStoreId, String retailerChainCode){

        LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreMasterByRetailerChainCodeAndRetailsStoreId----------------\n");

        String sql = "SELECT * FROM StoreMaster where RetailerStoreID = ? and RetailerChainCode = ?";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerStoreId);
            ps.setString(2, retailerChainCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("StoreID"));
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("retailerChainCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("street", rs.getString("Street"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("zip", rs.getString("ZIP"));
                map.put("latitude", rs.getString("Latitude"));
                map.put("longitude", rs.getString("Longitude"));
                map.put("comments", rs.getString("comments"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreMasterByRetailerChainCodeAndRetailsStoreId----------------\n");

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
    public List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCode(String retailerChainCode){

        LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreMasterByRetailerChainCode----------------\n");

        String sql = "SELECT * FROM StoreMaster where RetailerChainCode = ?";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, retailerChainCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("StoreID"));
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("retailerChainCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("street", rs.getString("Street"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("zip", rs.getString("ZIP"));
                map.put("latitude", rs.getString("Latitude"));
                map.put("longitude", rs.getString("Longitude"));
                map.put("comments", rs.getString("comments"));
                map.put("placeId", rs.getString("placeId"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreMasterByRetailerChainCode----------------\n");

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
    public void updateStorePlaceIdAndLatLngAndPostCode(String storeId, String placeId, String lat, String lng, String postalCode) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateStorePlaceIdAndLatLng storeId: {}, placeId: {}",storeId, placeId);

        String sql = "UPDATE StoreMaster SET placeId = ?, Latitude = ?, Longitude = ?, ZIP = ? WHERE StoreID = ?";
        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, placeId);
            preparedStatement.setString(2, lat);
            preparedStatement.setString(3, lng);
            preparedStatement.setString(4, postalCode);
            preparedStatement.setString(5, storeId);
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreMasterByStoreId(String storeId){

        LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreMasterByStoreId----------------\n");

        String sql = "SELECT * FROM StoreMaster where StoreID = ?";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("StoreID"));
                map.put("retailerStoreId", rs.getString("RetailerStoreID"));
                map.put("retailerChainCode", rs.getString("RetailerChainCode"));
                map.put("retailer", rs.getString("Retailer"));
                map.put("street", rs.getString("Street"));
                map.put("city", rs.getString("City"));
                map.put("stateCode", rs.getString("StateCode"));
                map.put("state", rs.getString("State"));
                map.put("zip", rs.getString("ZIP"));
                map.put("lat", rs.getString("Latitude"));
                map.put("lng", rs.getString("Longitude"));
                map.put("country", rs.getString("country"));
                map.put("placeId", rs.getString("placeId"));
                map.put("customerStoreNumber", rs.getString("RetailerStoreID"));
                map.put("retailerStoreNumber", rs.getString("RetailerStoreID"));
                map.put("name", rs.getString("name"));
                //BEGIN dummy values for API consistency
                map.put("geoLevel", "");
                map.put("geoLevelId", "");
                map.put("geoLevelName", "");
                //END dummy values for API consistency
                
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreMasterByStoreId----------------\n");

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
    public void updateParentProjectId(Project projectInput) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateParentProjectId::projectInput={}", projectInput);

        String sql = "UPDATE Project set parentProjectId=? where id= ? ";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.valueOf(projectInput.getParentProjectId()));
            ps.setInt(2, Integer.valueOf(projectInput.getId()));

            int id = ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends updateParentProjectId----------------\n");

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

    @Override
    public void createCustomerCodeProjectMap(String customerCode, int projectId) {
        LOGGER.info("---------------MetaServiceDaoImpl Starts createCustomerCodeProjectMap::customerInput={} projectId: {}",customerCode, projectId);
        String sql = "INSERT INTO CustomerCodeProjectMap ( customerCode, projectId) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, customerCode);
            ps.setInt(2, projectId);
            ps.executeUpdate();
            ps.close();

            LOGGER.info("---------------MetaServiceDaoImpl Ends createCustomerCodeProjectMap----------------\n");

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

    @Override
    public void updateCustomerCodeProjectMap(String customerCode, int projectId, String oldProjectId){
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateCustomerCodeProjectMap::customerInput={}, projectId:{} ", customerCode, projectId);
        String sql = "UPDATE CustomerCodeProjectMap set projectId=? where projectId= ? and customerCode =?";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, projectId);
            ps.setInt(2, Integer.parseInt(oldProjectId));
            ps.setString(3, customerCode);
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating CustomerCodeProjectMap failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Updating CustomerCodeProjectMap failed, no ID obtained.");
                }
            }
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends updateCustomerCodeProjectMap----------------\n");

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

	@Override
	public List<LinkedHashMap<String, String>> listChildProjects(String customerCode, int projectId) {
		
        LOGGER.info("---------------MetaServiceDaoImpl Starts listChildProjects::customerCode={} , projectId= ", customerCode, projectId);

		List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();

		String sql = "SELECT project.id, project.projectName, project.projectTypeId, projectType.name as projectType, project.categoryId, category.name as category, project.retailerCode, project.blurThreshold, retailer.name as retailer" +
				"    FROM Project project " + 
				"        INNER JOIN (" + 
				"             SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" + 
				"        ) customerMappedProjects " + 
				"            ON ( project.id = customerMappedProjects.projectId )" + 
				"        LEFT JOIN ProjectType projectType" + 
				"            ON ( project.projectTypeId = projectType.id)" + 
				"        LEFT JOIN Category category" + 
				"            ON ( project.categoryId = category.id)" + 
				"        LEFT JOIN Retailer retailer " + 
				"            ON ( project.retailerCode = retailer.retailerCode)" + 
				"    WHERE project.parentProjectId = ? AND project.status = '1'" +
				"    ORDER BY project.customerProjectId";
		
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, projectId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", ifNullToEmpty(rs.getString("id")));
                map.put("name", ifNullToEmpty(rs.getString("projectName")));
                map.put("projectTypeId", ifNullToEmpty(rs.getString("projectTypeId")));
                map.put("projectType", ifNullToEmpty(rs.getString("projectType")));
                map.put("categoryId", ifNullToEmpty(rs.getString("categoryId")));
                map.put("category", ifNullToEmpty(rs.getString("category")));
                map.put("retailerCode", ifNullToEmpty(rs.getString("retailerCode")));
                map.put("retailer", ifNullToEmpty(rs.getString("retailer")));
                map.put("blurThreshold", ifNullToEmpty(rs.getString("blurThreshold")));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends listChildProjects----------------\n");

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
	public List<LinkedHashMap<String, Object>> getCategoryReviewComments(String categoryId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getCategoryReviewComments::categoryId={}", categoryId);

		List<LinkedHashMap<String, Object>> resultList = new ArrayList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String,List<String>> tempMap = new LinkedHashMap<String,List<String>>();
		String sql = "SELECT resultCode,resultComment FROM CategoryReviewComments"
				+ " where categoryId = ? and status = '1' order by resultCode";
		
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, categoryId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	String resultCode = rs.getString("resultCode");
            	String resultComment = rs.getString("resultComment");
            	if ( tempMap.get(resultCode) == null ) {
            		tempMap.put(resultCode, new ArrayList<String>());
            	}
            	tempMap.get(resultCode).add(resultComment);
            }
            rs.close();
            ps.close();
            
            for(String resultCode : tempMap.keySet()) {
            	LinkedHashMap<String, Object> resultCodeCommentGroup = new LinkedHashMap<String, Object>();
            	resultCodeCommentGroup.put("resultCode", resultCode);
            	resultCodeCommentGroup.put("comments", tempMap.get(resultCode));
            	resultList.add(resultCodeCommentGroup);
            }
            LOGGER.info("---------------MetaServiceDaoImpl Ends getCategoryReviewComments----------------\n");

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
	public List<Map<String,String>> getProjectUpcDetailWithMetaInfo(int projectId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectUpcDetailWithMetaInfo::projectId={}", projectId);

		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		String projectUpcSql = "SELECT" + 
	    		"    ProjectUpc.upc," + 
	    		"    ProjectUpc.projectId," + 
	    		"    ProjectUpc.skuTypeId," + 
	    		"    SkuType.name as skuTypeName," + 
	    		"    ProductMaster.product_short_name as productName," + 
	    		"    ProductMaster.brand_name as brandName" + 
	    		" FROM" + 
	    		"    ProjectUpc, ProductMaster, SkuType" + 
	    		" WHERE" + 
	    		"    ProjectUpc.projectId = ?" + 
	    		"    AND ProjectUpc.upc = ProductMaster.upc" +
	    		"    AND ProjectUpc.skuTypeId = SkuType.id" +
	    		" ORDER BY" + 
	    		"    ProjectUpc.upc" ;
		
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(projectUpcSql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	Map<String,String> upc = new HashMap<String,String>();
            	upc.put("upc", rs.getString("upc"));
            	upc.put("projectId", rs.getString("projectId"));
            	upc.put("skuTypeId", rs.getString("skuTypeId"));
            	upc.put("skuTypeName", rs.getString("skuTypeName"));
            	upc.put("productName", rs.getString("productName"));
            	upc.put("brandName", rs.getString("brandName"));
            	resultList.add(upc);
            }
            rs.close();
            ps.close();
           
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectUpcDetailWithMetaInfo----------------\n");
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

	private void addStoreGradingCriteriaListToProjectId(
			List<ProjectStoreGradingCriteria> projectStoreGradingCriteriaList, int projectId, Connection conn) throws Exception {
		LOGGER.info("---------------MetaServiceDaoImpl Starts addStoreGradingCriteriaListToProjectId::"
				+ "projectStoreGradingCriteriaList={}, ProjectId = ", projectStoreGradingCriteriaList, projectId);
        String sql = "INSERT INTO ProjectStoreGradingCriteria ( projectId, criteriaSequenceNumber, criteriaName, resultCode, resultColor, criteriaExpression, criteriaComment, storeStatus)"
        		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!projectStoreGradingCriteriaList.isEmpty()) {
                for (ProjectStoreGradingCriteria storeGradingCriteria : projectStoreGradingCriteriaList) {
                        ps.setInt(1, projectId);
                        ps.setInt(2, storeGradingCriteria.getCriteriaSequenceNumber());
                        ps.setString(3, storeGradingCriteria.getCriteriaName());
                        ps.setInt(4, storeGradingCriteria.getResultCode());
                        ps.setString(5, storeGradingCriteria.getResultColor());
                        ps.setString(6, storeGradingCriteria.getCriteriaExpression());
                        ps.setString(7, storeGradingCriteria.getCriteriaComment());
                        ps.setInt(8, storeGradingCriteria.getStoreStatus());
                        ps.executeUpdate();
                }
            }
            ps.close();
        } catch (Exception e) {
        	LOGGER.error("Failed to add store grading criterias to project. Exception : {}",e.getMessage());
            throw e;
        } 
        LOGGER.info("---------------MetaServiceDaoImpl Ends addStoreGradingCriteriaListToProjectId----------------\n");
	}

	private void updateStoreGradingCriteriaListToProjectId(
			List<ProjectStoreGradingCriteria> projectStoreGradingCriteriaList, int projectId, Connection conn) throws Exception {
		LOGGER.info("---------------MetaServiceDaoImpl Starts updateStoreGradingCriteriaListToProjectId::"
				+ "projectStoreGradingCriteriaList={}, ProjectId = ",projectStoreGradingCriteriaList, projectId);
		
        String deleteSql = "DELETE FROM ProjectStoreGradingCriteria WHERE projectId = ?";
        try {
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setInt(1, projectId);
            deletePs.executeUpdate();
            deletePs.close();
            
            if (null != projectStoreGradingCriteriaList && !projectStoreGradingCriteriaList.isEmpty()) {
            	this.addStoreGradingCriteriaListToProjectId(projectStoreGradingCriteriaList, projectId, conn);
            }
        } catch (Exception e) {
			LOGGER.error("Failed to update store grading criteria to project. Exception : {}", e.getMessage());
			throw e;
        } 
        
        LOGGER.info("---------------MetaServiceDaoImpl Ends updateQuestionsListToProjectId----------------\n");
	}
	
	@Override
	public List<ProjectStoreGradingCriteria> getProjectStoreGradingCriterias(int projectId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectStoreGradingCriterias----------------\n");
        String sql = "SELECT * FROM ProjectStoreGradingCriteria  where projectId = ? order by criteriaSequenceNumber";
        List<ProjectStoreGradingCriteria> resultList = new ArrayList<ProjectStoreGradingCriteria>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	ProjectStoreGradingCriteria criteria = new ProjectStoreGradingCriteria();
                criteria.setCriteriaSequenceNumber(rs.getInt("criteriaSequenceNumber"));
                criteria.setCriteriaName(rs.getString("criteriaName"));
                criteria.setResultCode(rs.getInt("resultCode"));
                criteria.setResultColor(rs.getString("resultColor"));
                criteria.setCriteriaExpression(rs.getString("criteriaExpression"));
                criteria.setCriteriaComment(rs.getString("criteriaComment"));
                criteria.setStoreStatus(rs.getInt("storeStatus"));
                resultList.add(criteria);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectStoreGradingCriterias----------------\n");

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

    private void updateProjectReqQuestionsWithDetails(List<ProjectQuestion> projectQuestions, String projectId, Connection conn) throws Exception {
        LOGGER.info("---------------MetaServiceDaoImpl Starts updateProjectReqQuestionsWithDetails::"
                + "projectQuestions={}, ProjectId = ",projectQuestions, projectId);

        String deleteProjectQuestionsSql = "DELETE FROM ProjectRepQuestions WHERE projectId = ?";
        String deleteProjectQDetailsSql = "DELETE FROM ProjectRepQuestionDetail WHERE projectId = ?";
        try {
            PreparedStatement deletePs = conn.prepareStatement(deleteProjectQuestionsSql);
            deletePs.setString(1, projectId);
            deletePs.executeUpdate();
            deletePs.close();

            PreparedStatement deleteProjectDetailss = conn.prepareStatement(deleteProjectQDetailsSql);
            deleteProjectDetailss.setString(1, projectId);
            deleteProjectDetailss.executeUpdate();
            deleteProjectDetailss.close();

            if (null != projectQuestions && !projectQuestions.isEmpty()) {
                this.addProjectRepQuestionsWithDetail(projectQuestions, projectId, conn);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update ProjectQuestion to project. Exception : {}", e.getMessage());
            throw e;
        }

        LOGGER.info("MetaServiceDaoImpl Ends updateProjectReqQuestionsWithDetails");
    }

    public void addProjectRepQuestionsWithDetail(List<ProjectQuestion> projectQuestions, String projectId, Connection conn) throws SQLException {
        LOGGER.info("Starts addProjectRepQuestionsWithDetail : projectRepQuestionsList = {}", projectQuestions);
        String projectQuestionSQL = "INSERT INTO ProjectRepQuestions (questionId, questionDesc, questionSequenceNumber, projectId, questionType, mandatoryQuestion, questionPhotoLink, minimumValue, maximumValue, incrementInterval, goToSequenceNumber, questionGroupName, skipImageAnalysis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sql = "INSERT INTO ProjectRepQuestionDetail (projectId, questionId, responseId, responseValue, goToSequenceNumber) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement psProjectRepQuestion = conn.prepareStatement(projectQuestionSQL);
            PreparedStatement psProjectRepQuestionDetails = conn.prepareStatement(sql);
            for ( ProjectQuestion projectQuestion: projectQuestions) {
            	//If question type not defined, use default question type as Long Text
                String questionType = StringUtils.isBlank(projectQuestion.getQuestionType()) ? "LT" : projectQuestion.getQuestionType(); 
                
            	String skipImageAnalysis = projectQuestion.getSkipImageAnalysis();
            	skipImageAnalysis = StringUtils.isBlank(skipImageAnalysis) ? "0" : skipImageAnalysis.trim();
            	
                psProjectRepQuestion.setInt(1, Integer.valueOf(projectQuestion.getId()));
                psProjectRepQuestion.setString(2, projectQuestion.getDesc());
                psProjectRepQuestion.setString(3, projectQuestion.getSequenceNumber());
                psProjectRepQuestion.setInt(4, Integer.valueOf(projectId));
                psProjectRepQuestion.setString(5, questionType);
                psProjectRepQuestion.setString(6, projectQuestion.getMandatoryQuestion());
                psProjectRepQuestion.setString(7, projectQuestion.getQuestionPhotoLink());
                psProjectRepQuestion.setString(8, projectQuestion.getMinimumValue());
                psProjectRepQuestion.setString(9, projectQuestion.getMaximumValue());
                psProjectRepQuestion.setString(10, projectQuestion.getIncrementInterval());
                psProjectRepQuestion.setString(11, projectQuestion.getGoToSequenceNumber());
                psProjectRepQuestion.setString(12, projectQuestion.getGroupName());
                psProjectRepQuestion.setString(13, skipImageAnalysis);
                psProjectRepQuestion.executeUpdate();

                if(null != projectQuestion.getProjectRepQuestionDetails() &&!projectQuestion.getProjectRepQuestionDetails().isEmpty()) {
                    for(ProjectRepQuestionDetail projectRepQuestionDetail:projectQuestion.getProjectRepQuestionDetails()){
                        psProjectRepQuestionDetails.setString(1, projectId);
                        psProjectRepQuestionDetails.setString(2, projectQuestion.getId());
                        psProjectRepQuestionDetails.setString(3, projectRepQuestionDetail.getResponseId());
                        psProjectRepQuestionDetails.setString(4, projectRepQuestionDetail.getResponseValue());
                        psProjectRepQuestionDetails.setString(5, projectRepQuestionDetail.getGoToSequenceNumber());
                        psProjectRepQuestionDetails.executeUpdate();
                    }
                }

            }

            psProjectRepQuestionDetails.close();
            psProjectRepQuestion.close();
            LOGGER.info("Ends addProjectRepQuestionsWithDetail");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<ProjectQuestion> getProjectRepQuestions(String projectId, Connection conn) {
        LOGGER.info("MetaServiceDaoImpl Starts getProjectReqQuestions projectId = {}", projectId);

        String sql ="SELECT questionId, questionDesc, questionSequenceNumber, projectId, questionType, " +
                "mandatoryQuestion, questionPhotoLink, minimumValue, maximumValue, incrementInterval, goToSequenceNumber, questionGroupName " +
                "FROM ProjectRepQuestions where projectId = ?";

        String questionDetailsSQL ="SELECT questionId, projectId, responseId, responseValue, goToSequenceNumber " +
                "FROM ProjectRepQuestionDetail where projectId = ? and questionId = ?";

        List<ProjectQuestion> resultList = new ArrayList<>();
        List<ProjectRepQuestionDetail> projectRepQuestionsList = null;

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectId);
            ResultSet rs = ps.executeQuery();

            PreparedStatement questionDetailsPS = conn.prepareStatement(questionDetailsSQL);

            while (rs.next()) {
                ProjectQuestion projectQuestion = new ProjectQuestion();
                projectQuestion.setId(rs.getString("questionId"));
                projectQuestion.setDesc((rs.getString("questionDesc") == null) ? "" : rs.getString("questionDesc"));
                projectQuestion.setSequenceNumber((rs.getString("questionSequenceNumber") == null) ? "" : rs.getString("questionSequenceNumber"));
                projectQuestion.setProjectId(rs.getString("projectId"));
                projectQuestion.setMandatoryQuestion(rs.getString("mandatoryQuestion"));
                projectQuestion.setQuestionType(rs.getString("questionType"));
                projectQuestion.setQuestionPhotoLink((rs.getString("questionPhotoLink") == null) ? "" : rs.getString("questionPhotoLink"));
                projectQuestion.setMinimumValue((rs.getString("minimumValue") == null) ? "" : rs.getString("minimumValue"));
                projectQuestion.setMaximumValue((rs.getString("maximumValue") == null) ? "" : rs.getString("maximumValue"));
                projectQuestion.setIncrementInterval((rs.getString("incrementInterval") == null) ? "" : rs.getString("incrementInterval"));
                projectQuestion.setGoToSequenceNumber((rs.getString("goToSequenceNumber") == null) ? "" : rs.getString("goToSequenceNumber"));
                projectQuestion.setGroupName((rs.getString("questionGroupName") == null) ? "" : rs.getString("questionGroupName"));


                if(rs.getString("questionType").equalsIgnoreCase("MS") || rs.getString("questionType").equalsIgnoreCase("SS")) {

                    projectRepQuestionsList = new ArrayList<>();

                    questionDetailsPS.setString(1, projectId);
                    questionDetailsPS.setString(2, rs.getString("questionId"));
                    ResultSet questionsRS = questionDetailsPS.executeQuery();
                    while (questionsRS.next()) {
                        ProjectRepQuestionDetail projectRepQuestionDetail = new ProjectRepQuestionDetail();
                        projectRepQuestionDetail.setProjectId(questionsRS.getString("projectId"));
                        projectRepQuestionDetail.setQuestionId(questionsRS.getString("questionId"));
                        projectRepQuestionDetail.setResponseId(questionsRS.getString("responseId"));
                        projectRepQuestionDetail.setResponseValue(questionsRS.getString("responseValue"));
                        projectRepQuestionDetail.setGoToSequenceNumber(questionsRS.getString("goToSequenceNumber"));

                        projectRepQuestionsList.add(projectRepQuestionDetail);
                    }
                    questionsRS.close();

                    projectQuestion.setProjectRepQuestionDetails(projectRepQuestionsList);
                }

                resultList.add(projectQuestion);
            }

            questionDetailsPS.close();
            rs.close();
            ps.close();
            LOGGER.info("MetaServiceDaoImpl Ends getProjectReqQuestions");

            return resultList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
	public List<ProjectScoreDefinition> getProjectScoreDefinition(int projectId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getProjectScoreDefinition----------------\n");
        String highLevelScoreSql = "SELECT * FROM ProjectScoreDefinition WHERE projectId = ? order by scoreId";
        String componentScoreSql = "SELECT * FROM ProjectComponentScoreDefinition WHERE projectId = ? AND scoreId = ? order by scoreId,componentScoreId";
        String criteriaScoreSql = "SELECT * FROM ProjectComponentCriteriaScoreDefinition WHERE projectId = ? AND scoreId = ? AND componentScoreId = ? order by scoreId,componentScoreId,groupId,groupSequenceNumber";
        String scoreGroupsSql = "SELECT scoreGroupId,groupMinScore,groupMaxScore FROM ProjectScoreGroupDefinition WHERE projectId = ? AND scoreId = ? order by scoreGroupId";

        List<ProjectScoreDefinition> scores = new ArrayList<ProjectScoreDefinition>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement highLevelScorePs = conn.prepareStatement(highLevelScoreSql);
            PreparedStatement componentScorePs = conn.prepareStatement(componentScoreSql);
            PreparedStatement criteriaScorePs = conn.prepareStatement(criteriaScoreSql);
            PreparedStatement scoreGroupsPs = conn.prepareStatement(scoreGroupsSql);

            highLevelScorePs.setInt(1, projectId);
            componentScorePs.setInt(1, projectId);
            criteriaScorePs.setInt(1, projectId);
            scoreGroupsPs.setInt(1, projectId);
            
            ResultSet highLevelScoreRs = highLevelScorePs.executeQuery();

            while (highLevelScoreRs.next()) {
            	ProjectScoreDefinition score = new ProjectScoreDefinition();
            	score.setProjectId(projectId);
                score.setScoreId(highLevelScoreRs.getInt("scoreId"));
                score.setScoreName(highLevelScoreRs.getString("scoreName"));
                
                componentScorePs.setInt(2, score.getScoreId());
                ResultSet componentScoreRs = componentScorePs.executeQuery();
                while (componentScoreRs.next()) {
                	ProjectComponentScoreDefinition componentScore = new ProjectComponentScoreDefinition();
                	componentScore.setProjectId(projectId);
                	componentScore.setScoreId(score.getScoreId());
                	componentScore.setComponentScoreId(componentScoreRs.getInt("componentScoreId"));
                	componentScore.setComponentScoreName(componentScoreRs.getString("componentScoreName"));
                	componentScore.setComponentMaxScore(componentScoreRs.getString("componentMaxScore"));
                	componentScore.setWeightage(componentScoreRs.getString("weightage"));
                	
                	criteriaScorePs.setInt(2, score.getScoreId());
                	criteriaScorePs.setInt(3, componentScore.getComponentScoreId());
                	ResultSet criteriaScoreRs = criteriaScorePs.executeQuery();
                	while (criteriaScoreRs.next()) {
                		ProjectComponentCriteriaScoreDefinition criteriaScore = new ProjectComponentCriteriaScoreDefinition();
                		criteriaScore.setProjectId(projectId);
                		criteriaScore.setScoreId(score.getScoreId());
                		criteriaScore.setComponentScoreId(componentScore.getComponentScoreId());
                		criteriaScore.setGroupId(criteriaScoreRs.getInt("groupId"));
                		criteriaScore.setGroupSequenceNumber(criteriaScoreRs.getInt("groupSequenceNumber"));
                		criteriaScore.setCriteria(criteriaScoreRs.getString("criteria"));
                		criteriaScore.setPoints(criteriaScoreRs.getString("points") );
                		criteriaScore.setComment(criteriaScoreRs.getString("comment"));
                		criteriaScore.setAction(criteriaScoreRs.getString("action"));
                		criteriaScore.setFocusCriteria(criteriaScoreRs.getString("focusCriteria"));
                		criteriaScore.setCriteriaDesc(criteriaScoreRs.getString("criteriaDesc"));
                		componentScore.getComponentCriteriaScores().add(criteriaScore);
                	}
                	criteriaScoreRs.close();
                	score.getComponentScores().add(componentScore);
                }
                componentScoreRs.close();
                
                scoreGroupsPs.setInt(2, score.getScoreId());
                ResultSet scoreGroupsRs = scoreGroupsPs.executeQuery();
                while (scoreGroupsRs.next()) {
            		ProjectScoreGroupingDefinition scoreGrouping = new ProjectScoreGroupingDefinition();
            		scoreGrouping.setProjectId(projectId);
            		scoreGrouping.setScoreId(score.getScoreId());
            		scoreGrouping.setScoreGroupId(scoreGroupsRs.getInt("scoreGroupId"));
            		scoreGrouping.setGroupMinScore(scoreGroupsRs.getString("groupMinScore"));
            		scoreGrouping.setGroupMaxScore(scoreGroupsRs.getString("groupMaxScore"));
            		score.getScoreGrouping().add(scoreGrouping);
                }
                scoreGroupsRs.close();
                
                scores.add(score);
            }
            highLevelScoreRs.close();
            
            criteriaScorePs.close();
            componentScorePs.close();
            scoreGroupsPs.close();
            highLevelScorePs.close();
            
            LOGGER.info("---------------MetaServiceDaoImpl Ends getProjectScoreDefinition----------------\n");

            return scores;
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
	public String getStoreByCustomerCodeAndCustomerStoreNumber(String customerCode, String customerStoreNumber) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreByCustomerCodeAndCustomerStoreNumber"
				+ ": customerCode = {}, customerStoreNumber = {}----------------\n", customerCode, customerStoreNumber);
		
        String sql = "SELECT storeId FROM StoreGeoLevelMap WHERE customerCode = ? AND customerStoreNumber = ?";
        String storeId = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, customerStoreNumber);
            
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                storeId =  rs.getString("storeId");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreByCustomerCodeAndCustomerStoreNumber----------------\n");

            return storeId;
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
	public List<String> getStoreDistributionUPCs(String projectId, String storeId) {
		LOGGER.info("---------------MetaServiceDaoImpl Starts getStoreDistributionUPCs::projectId={}, storeId={}", projectId, storeId);

		List<String> resultList = new ArrayList<String>();
		String projectUpcSql = "SELECT distinct(upc) FROM ProjectDistributionUpc WHERE projectId = ? AND storeType IN ( " + 
				"     SELECT storeType FROM ProjectDistributionStoreType WHERE projectId = ? AND storeId = ? )";
		
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(projectUpcSql);
            ps.setString(1, projectId);
            ps.setString(2, projectId);
            ps.setString(3, storeId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	resultList.add(rs.getString("upc"));
            }
            
            rs.close();
            ps.close();
           
            LOGGER.info("---------------MetaServiceDaoImpl Ends getStoreDistributionUPCs----------------\n");
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
    public boolean getRealtimeProcessingEnabled(int projectId) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts getSyncStatus::projectId={}",projectId);
        String sql = "SELECT projectId,realtimeAnalysis  FROM ProjectAction WHERE projectId = ?";

        Connection conn = null;
        try {
            Boolean realtimeAnalysis = false;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                realtimeAnalysis = (rs.getInt("realtimeAnalysis") == 1? true : false);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getSyncStatus realtimeAnalysis = {} ",realtimeAnalysis);

            return realtimeAnalysis;
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
	public boolean getExternalProcessingEnabled(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getExternalProcessingEnabled::projectId={}",projectId);
        String sql = "SELECT projectId,externalAnalysis  FROM ProjectAction WHERE projectId = ?";

        Connection conn = null;
        try {
            Boolean externalAnalysis = false;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	externalAnalysis = (rs.getInt("externalAnalysis") == 1? true : false);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getExternalProcessingEnabled externalAnalysis = {} ",externalAnalysis);

            return externalAnalysis;
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
	public boolean getDuplicateAnalysisEnabled(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getDuplicateAnalysisEnabled::projectId={}",projectId);
        String sql = "SELECT projectId,duplicateAnalysis  FROM ProjectAction WHERE projectId = ?";

        Connection conn = null;
        try {
            Boolean duplicateAnalysis = false;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	duplicateAnalysis = (rs.getInt("duplicateAnalysis") == 1? true : false);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getDuplicateAnalysisEnabled duplicateAnalysis = {} ",duplicateAnalysis);

            return duplicateAnalysis;
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

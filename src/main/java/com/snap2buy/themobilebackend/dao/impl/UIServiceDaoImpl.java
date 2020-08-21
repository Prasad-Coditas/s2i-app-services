package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.UIServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.CustomerRoleMenuMap;
import com.snap2buy.themobilebackend.model.MenuMap;
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
import java.util.Map;

/**
 * Created by Anoop on 09/28/18.
 */
@Component(value = BeanMapper.BEAN_UI_SERVICE_DAO)
@Scope("prototype")
public class UIServiceDaoImpl implements UIServiceDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	@Override
	public List<LinkedHashMap<String, String>> getMenusByCustomerCodeRole(String customerCode, String role, String source) {
		LOGGER.info("---------------UIServiceDaoImpl Starts getMenusByCustomerCodeRole:: customerCode={}, role=",customerCode, role );
       
		String sql = "SELECT MenuMaster.routerLink, MenuMaster.label, MenuMaster.css,RoleMenuMap.isDefault"
				+ " FROM MenuMaster, RoleMenuMap"
				+ " WHERE RoleMenuMap.customerCode=? AND RoleMenuMap.role=? AND RoleMenuMap.menuId = MenuMaster.id AND RoleMenuMap.source= ?"
				+ " ORDER BY isDefault DESC";
        
		List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, role);
            ps.setString(3, source);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("routerLink", rs.getString("routerLink"));
                map.put("label", rs.getString("label"));
                map.put("css", rs.getString("css"));
                map.put("isDefault", rs.getInt("isDefault")+"");
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------UIServiceDaoImpl Ends getMenusByCustomerCodeRole----------------\n");

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
    public void addWebsiteEnquiry(String firstName, String lastName, String email, String phone, String company, String jobProfile, String note) {
        LOGGER.info("Starts addWebsiteEnquiry");

        String sql = "INSERT " +
                "INTO WebsiteEnquiries " +
                    "(firstName, lastName, email, phone, company, jobProfile, note) " +
                "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, company);
            ps.setString(6, jobProfile);
            ps.setString(7, note);

            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("addWebsiteEnquiry Insert/Update failed.");
            }

            ps.close();
            LOGGER.info("Ends addWebsiteEnquiry");

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
    public List<Map<String, String>> getWebsiteEnquiries() {
        LOGGER.info("UIServiceDaoImpl Starts getWebsiteEnquiries" );

        String sql = "SELECT *"
                + " FROM WebsiteEnquiries"
                + " ORDER BY id DESC";

        List<Map<String, String>> resultList = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("id", rs.getString("id"));
                map.put("firstName", rs.getString("firstName"));
                map.put("lastName", rs.getString("lastName"));
                map.put("email", rs.getString("email"));
                map.put("phone", rs.getString("phone"));
                map.put("company", rs.getString("company"));
                map.put("jobProfile", rs.getString("jobProfile"));
                map.put("note", rs.getString("note"));
                map.put("createdOn", rs.getString("createdOn"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("UIServiceDaoImpl Ends getWebsiteEnquiries");

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
    public void addUIMenus(CustomerRoleMenuMap menuMapping) {
        LOGGER.info("Starts addUIMenus::customerCode={} role: {}", menuMapping.getCustomerCode(), menuMapping.getRole());

        String deleteSql = "DELETE FROM RoleMenuMap WHERE customerCode = ? and role = ?";

        Connection conn = null;

        boolean isMenuAdded = false;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(deleteSql);

            ps.setString(1, menuMapping.getCustomerCode());
            ps.setString(2, menuMapping.getRole());
            ps.executeUpdate();
            ps.close();

            if (!menuMapping.getMenuMapList().isEmpty()) {
                this.addCustomerRoleMenuMap(menuMapping, conn);
            }

            isMenuAdded = true;
            conn.commit();
            LOGGER.info("Ends addUIMenus");

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    if (!isMenuAdded) {
                        conn.rollback();
                    }

                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    private void addCustomerRoleMenuMap(CustomerRoleMenuMap customerRoleMenuMap, Connection conn) throws Exception {
        LOGGER.info("Starts AddCustomerRoleMenuMap");

        String sql = "INSERT INTO RoleMenuMap ( customerCode, role, menuId, isDefault, source) VALUES (?, ?, ?, ?, ?)";
        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            for ( MenuMap menuMap : customerRoleMenuMap.getMenuMapList()) {
                ps.setString(1, customerRoleMenuMap.getCustomerCode());
                ps.setString(2, customerRoleMenuMap.getRole());
                ps.setInt(3, Integer.valueOf(menuMap.getMenuId().trim()));
                ps.setBoolean(4, Boolean.valueOf(menuMap.getIsDefault()));
                ps.setString(5, menuMap.getSource());
                ps.executeUpdate();
            }
            ps.close();

        } catch (Exception e) {
            LOGGER.error("Failed to Add MenuMap. Exception : {}", e.getMessage());
            throw e;
        }

        LOGGER.info("AddCustomerRoleMenuMap");
    }

    @Override
    public List<LinkedHashMap<String, String>> getUserNotificationTokenByCustomerCodeAndRole(String customerCode, String role) {
        LOGGER.info("---------------UIServiceDaoImpl Starts getUserNotificationTokenByCustomerCodeAndRole:: customerCode={}",customerCode);

        String sql ="SELECT ut.userId, ut.platform, ut.fcmToken " +
                    "FROM UserNotificationToken ut " +
                        "LEFT JOIN " +
                            "User u " +
                                "on u.userId = ut.userId " +
                    "WHERE u.customerCode = ? AND u.role = ?";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, role);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("userId", rs.getString("userId"));
                map.put("platform", rs.getString("platform"));
                map.put("fcmToken", rs.getString("fcmToken"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("UIServiceDaoImpl Ends getUserNotificationTokenByCustomerCodeAndRole");

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
    public List<LinkedHashMap<String, String>> getUserNotificationTokenByUserId(String userId) {
        LOGGER.info("---------------UIServiceDaoImpl Starts getUserNotificationTokenByUserId:: userId={}",userId);

        String sql ="SELECT ut.userId, ut.platform, ut.fcmToken " +
                "FROM UserNotificationToken ut " +
                "WHERE ut.userId = ?";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("userId", rs.getString("userId"));
                map.put("platform", rs.getString("platform"));
                map.put("fcmToken", rs.getString("fcmToken"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("UIServiceDaoImpl Ends getUserNotificationTokenByUserId");

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

package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.UserServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.User;
import com.snap2buy.themobilebackend.model.UserAppConfig;
import com.snap2buy.themobilebackend.util.ConverterUtil;
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
 * @author Harshal
 */
@Component(value = BeanMapper.BEAN_USER_SERVICE_DAO)
@Scope("prototype")
public class UserServiceDaoImpl implements UserServiceDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSource dataSource;

    public List<LinkedHashMap<String, String>> listUsers(){

        LOGGER.info("Start: ListUsers");
        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT " +
                        "DISTINCT (u.userId), u.firstName, u.lastName, u.email, u.customerCode, u.status, u.role, " +
                        "DATE_FORMAT(u.createdDate, '%Y-%m-%d %T') as createdDate, " +
                        "DATE_FORMAT(u.lastLoginDate, '%Y-%m-%d %T') as lastLoginDate, " +
                        "ug.geoLevel, ug.geoLevelId, cg.geoLevelName " +
                    "FROM User u " +
                        "LEFT JOIN " +
                            "UserGeoMap ug " +
                                "on u.userId=ug.userId " +
                        "LEFT JOIN " +
                            "CustomerGeoLevelMap cg " +
                                "on ug.geoLevel=cg.geoLevel";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("userId", rs.getString("userId"));
                data.put("firstName", rs.getString("firstName"));
                data.put("lastName", rs.getString("lastName"));
                data.put("email", rs.getString("email"));
                data.put("customerCode", rs.getString("customerCode"));
                data.put("status", rs.getString("status"));
                data.put("role", rs.getString("role"));
                data.put("createdDate", rs.getString("createdDate"));
                data.put("lastLoginDate",rs.getString("lastLoginDate") == null?"0":rs.getString("lastLoginDate"));
                data.put("geoLevel", rs.getString("geoLevel") == null ? "" : rs.getString("geoLevel"));
                data.put("geoLevelId",rs.getString("geoLevelId") == null ? "" : rs.getString("geoLevelId"));
                data.put("geoLevelName", rs.getString("geoLevelName") == null ? "" : rs.getString("geoLevelName"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: ListUsers");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> listUsersGeoLevels(String customerCode){
        LOGGER.info("Start: ListUsersGeoLevels");
        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String,String>>();

        String sql = "SELECT * FROM StoreGeoLevelMap sg where sg.customerCode= ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, customerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<String,String>();
                data.put("geoLevel1Id", rs.getString("geoLevel1Id"));
                data.put("geoLevel2Id",rs.getString("geoLevel2Id"));
                data.put("geoLevel3Id",rs.getString("geoLevel3Id"));
                data.put("geoLevel4Id",rs.getString("geoLevel4Id"));
                data.put("geoLevel5Id",rs.getString("geoLevel5Id"));
                data.put("storeId",rs.getString("storeId"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: ListUsersGeoLevels");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public LinkedHashMap<String, String> getCustomerGeoLevels(String customerCode){
        LOGGER.info("Start: getCustomerGeoLevels");
        LinkedHashMap<String, String> result = new LinkedHashMap<String,String>();

        String sql = "SELECT geoLevel, geoLevelName FROM CustomerGeoLevelMap cg where cg.customerCode= ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, customerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.put(rs.getString("geoLevel"), rs.getString("geoLevelName"));
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getCustomerGeoLevels");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> getCustomerCodes(){
        LOGGER.info("Start: getCustomerCodes");
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql = "SELECT customerCode, name FROM Customer";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("name", rs.getString("name"));
                data.put("customerCode", rs.getString("customerCode"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getCustomerCodes");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> getUserRoles(){
        LOGGER.info("Start: getUserRoles");
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql = "SELECT role FROM RoleMaster";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("role", rs.getString("role"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getUserRoles");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public void createUserGeoMap(User user){
        LOGGER.info("Starts createUserGeoMap::userInput= {}", user);
        String sql = "INSERT " +
                        "INTO UserGeoMap " +
                            "( customerCode, userId, geoLevel, geoLevelId) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                            "geoLevel= ?, geoLevelId= ?";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getCustomerCode());
            ps.setString(2, user.getUserId());
            ps.setString(3, user.getGeoLevel());
            ps.setString(4, user.getGeoLevelId());
            ps.setString(5, user.getGeoLevel());
            ps.setString(6, user.getGeoLevelId());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating UserGeoMap failed, no rows affected.");
            }

            ps.close();
            LOGGER.info("Ends createUserGeoMap");

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
    public void insertOrUpdateDeviceConfiguration(String supportedVersionName, int supportedVersionCode,
                                                  String latestVersionName, int latestVersionCode){
        LOGGER.info("Starts insertOrUpdateDeviceConfiguration");
        String sql = "INSERT " +
                        "INTO DeviceConfiguration " +
                            "(id, supportedVersionCode,supportedVersionName, latestVersionCode,latestVersionName) " +
                        "VALUES (1, ?, ?, ?, ?) " +
                            "ON DUPLICATE " +
                            "KEY UPDATE " +
                                "supportedVersionName = ?, supportedVersionCode = ?, " +
                                "latestVersionCode = ?, latestVersionName = ?";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,supportedVersionCode);
            ps.setString(2, supportedVersionName);
            ps.setInt(3, latestVersionCode);
            ps.setString(4, latestVersionName);
            ps.setInt(5,supportedVersionCode);
            ps.setString(6, supportedVersionName);
            ps.setInt(7, latestVersionCode);
            ps.setString(8, latestVersionName);

            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("DeviceConfiguration Insert/Update failed.");
            }

            ps.close();
            LOGGER.info("Ends insertOrUpdateDeviceConfiguration");

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
    public List<LinkedHashMap<String, String>> getDeviceConfiguration() {
        LOGGER.info("Start: getDeviceConfiguration");
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql ="SELECT " +
                        "platform, supportedVersionCode, supportedVersionName, latestVersionCode, latestVersionName " +
                    "FROM DeviceConfiguration";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("platform", rs.getString("platform"));
                data.put("supportedVersionCode", rs.getString("supportedVersionCode"));
                data.put("supportedVersionName", rs.getString("supportedVersionName"));
                data.put("latestVersionCode", rs.getString("latestVersionCode"));
                data.put("latestVersionName", rs.getString("latestVersionName"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getDeviceConfiguration");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public void createUserScreenConfig(String userId, List<String> screens){
        LOGGER.info("Starts createUserScreenConfig::userId= {} and screens= {}", userId, screens);
        String sql = "INSERT IGNORE INTO UserScreenConfig (userId, screenName) VALUES (?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            for (String screen: screens) {
                ps.setString(1, userId);
                ps.setString(2, screen);
                id = ps.executeUpdate();
            }

            ps.close();
            LOGGER.info("Ends createUserScreenConfig");

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
    public List<String> getUserScreenConfig(String userId) {
        LOGGER.info("Start: getUserScreenConfig userId: {}", userId);
        List<String> result = new ArrayList<>();

        String sql ="SELECT * FROM UserScreenConfig where userId = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getString("screenName"));
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getUserScreenConfig");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public void insertOrUpdateUserAppConfig(UserAppConfig userAppConfig) {
        LOGGER.info("Starts insertOrUpdateUserAppConfig");
        String sql = "INSERT " +
                        "INTO UserAppConfig " +
                            "(userId, appVersionCode, appVersionName, cameraResolution, " +
                            "deviceBrand, deviceModel, displayDPI, displayDensity, installationDate, " +
                            "manufacturer, osName, screenResolution, sdkVersion, updationDate, platform) " +
                        "VALUES " +
                            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE " +
                            "KEY UPDATE " +
                                "appVersionCode = ?, appVersionName = ?, cameraResolution = ?, deviceBrand = ?, " +
                                "deviceModel = ?, displayDPI = ?, displayDensity = ?, installationDate = ?, " +
                                "manufacturer = ?, osName = ?, screenResolution = ?, sdkVersion = ?, updationDate = ?";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userAppConfig.getUserId());
            ps.setString(2,userAppConfig.getAppVersionCode());
            ps.setString(3, userAppConfig.getAppVersionName());
            ps.setString(4, userAppConfig.getCameraResolution());
            ps.setString(5, userAppConfig.getDeviceBrand());
            ps.setString(6, userAppConfig.getDeviceModel());
            ps.setString(7, userAppConfig.getDisplayDPI());
            ps.setString(8, userAppConfig.getDisplayDensity());
            ps.setString(9, userAppConfig.getInstallationDate());
            ps.setString(10, userAppConfig.getManufacturer());
            ps.setString(11, userAppConfig.getOsName());
            ps.setString(12, userAppConfig.getScreenResolution());
            ps.setString(13, userAppConfig.getSdkVersion());
            ps.setString(14, userAppConfig.getUpdationDate());
            ps.setString(15, userAppConfig.getPlatform());
            ps.setString(16,userAppConfig.getAppVersionCode());
            ps.setString(17, userAppConfig.getAppVersionName());
            ps.setString(18, userAppConfig.getCameraResolution());
            ps.setString(19, userAppConfig.getDeviceBrand());
            ps.setString(20, userAppConfig.getDeviceModel());
            ps.setString(21, userAppConfig.getDisplayDPI());
            ps.setString(22, userAppConfig.getDisplayDensity());
            ps.setString(23, userAppConfig.getInstallationDate());
            ps.setString(24, userAppConfig.getManufacturer());
            ps.setString(25, userAppConfig.getOsName());
            ps.setString(26, userAppConfig.getScreenResolution());
            ps.setString(27, userAppConfig.getSdkVersion());
            ps.setString(28, userAppConfig.getUpdationDate());

            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("UserAppConfig Insert/Update failed.");
            }

            ps.close();
            LOGGER.info("Ends insertOrUpdateUserAppConfig");

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
    public List<LinkedHashMap<String, String>> getUserAppConfig(String userId) {
        LOGGER.info("Start: getUserAppConfig userId: {}", userId);
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql ="SELECT " +
                        "userId, appVersionCode, appVersionName, cameraResolution, " +
                        "deviceBrand, deviceModel, displayDPI, displayDensity, installationDate, " +
                        "manufacturer, osName, screenResolution, sdkVersion, updationDate " +
                    "FROM UserAppConfig " +
                    "where userId = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("userId", rs.getString("userId"));
                data.put("appVersionCode", rs.getString("appVersionCode"));
                data.put("appVersionName", rs.getString("appVersionName"));
                data.put("cameraResolution", rs.getString("cameraResolution"));
                data.put("deviceBrand", rs.getString("deviceBrand"));
                data.put("deviceModel", rs.getString("deviceModel"));
                data.put("displayDPI", rs.getString("displayDPI"));
                data.put("displayDensity", rs.getString("displayDensity"));
                data.put("installationDate", rs.getString("installationDate"));
                data.put("manufacturer", rs.getString("manufacturer"));
                data.put("osName", rs.getString("osName"));
                data.put("screenResolution", rs.getString("screenResolution"));
                data.put("sdkVersion", rs.getString("sdkVersion"));
                data.put("updationDate", rs.getString("updationDate"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getUserAppConfig");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> getRecentStoreVisits(String userId, String customerCode, int numberOfRecords) {
        LOGGER.info("Start: getRecentStoreVisits: {} and limit: {}", userId, numberOfRecords);
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql ="SELECT " +
                        "DISTINCT(ps.storeId), ps.visitDateId, sm.RetailerStoreID, sm.Retailer, sm.name, " +
                        "sm.Street, sm.City, sm.State, sm.StateCode, sm.ZIP, sm.placeId, sg.customerStoreNumber " +
                    "FROM ProjectStoreResult ps " +
                        "LEFT JOIN " +
                            "StoreMaster sm " +
                                "on sm.StoreID = ps.storeId " +
                        "LEFT JOIN " +
                            "StoreGeoLevelMap sg " +
                                "on sg.storeId = ps.storeId and sg.customerCode = ? " +
                        "WHERE " +
                            "ps.agentId = ? " +
                        "ORDER BY " +
                            "ps.id DESC " +
                        "LIMIT ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setString(2, userId);
            ps.setInt(3, numberOfRecords);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("storeId", rs.getString("storeId"));
                data.put("visitDateId", rs.getString("visitDateId"));
                data.put("retailerStoreNumber", ConverterUtil.ifNullToEmpty(rs.getString("RetailerStoreID")));
                data.put("customerStoreNumber", ConverterUtil.ifNullToEmpty(rs.getString("customerStoreNumber")));
                data.put("retailer", rs.getString("Retailer"));
                data.put("name", rs.getString("name"));
                data.put("street", rs.getString("Street"));
                data.put("city", rs.getString("City"));
                data.put("state", rs.getString("State"));
                data.put("stateCode", rs.getString("StateCode"));
                data.put("zip", rs.getString("ZIP"));
                data.put("placeId", rs.getString("placeId"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getRecentStoreVisits");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> getStoreVisitStatus(String userId, String storeId, String visitDate) {
        LOGGER.info("Start: getStoreVisitStatus: userId: {}, storeId: {}, visitDate:{}", userId, storeId, visitDate);
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql ="SELECT " +
                        "ps.storeId, ps.visitDateId, ps.taskId, ps.status, ps.resultComment, ps.projectId, p.projectName " +
                    "FROM " +
                        "ProjectStoreResult ps " +
                        "LEFT JOIN " +
                            "Project p " +
                                "on p.id = ps.projectId " +
                            "WHERE " +
                                "ps.storeId = ? " +
                                "AND ps.agentId = ? " +
                                "AND ps.visitDateId = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, storeId);
            ps.setString(2, userId);
            ps.setString(3, visitDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("storeId", rs.getString("storeId"));
                data.put("visitDateId", rs.getString("visitDateId"));
                data.put("taskId", rs.getString("taskId"));
                data.put("projectName", rs.getString("projectName"));
                data.put("projectStoreVisitStatus", rs.getString("status"));
                data.put("projectId", rs.getString("projectId"));
                data.put("resultComment", rs.getString("resultComment"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getStoreVisitStatus");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public List<LinkedHashMap<String, String>> getStoreVisitStatusByPlaceId(String userId, String placeId, String visitDate) {
        LOGGER.info("Start: getStoreVisitStatusByPlaceId: userId: {}, placeId: {}, visitDate:{}", userId, placeId, visitDate);
        List<LinkedHashMap<String, String>> result = new ArrayList<>();

        String sql ="SELECT " +
                        "ps.storeId, ps.visitDateId, ps.taskId, ps.status, ps.resultComment, ps.projectId, p.projectName " +
                    "FROM " +
                        "ProjectStoreResult ps " +
                            "LEFT JOIN " +
                                "Project p " +
                            "on p.id = ps.projectId " +
                        "WHERE " +
                            "ps.storeId = (select storeId from StoreMaster where placeId = ? ) " +
                            "AND ps.agentId = ? " +
                            "AND ps.visitDateId = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, placeId);
            ps.setString(2, userId);
            ps.setString(3, visitDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String,String> data = new LinkedHashMap<>();
                data.put("storeId", rs.getString("storeId"));
                data.put("visitDateId", rs.getString("visitDateId"));
                data.put("taskId", rs.getString("taskId"));
                data.put("projectName", rs.getString("projectName"));
                data.put("projectStoreVisitStatus", rs.getString("status"));
                data.put("projectId", rs.getString("projectId"));
                data.put("resultComment", rs.getString("resultComment"));
                result.add(data);
            }
            rs.close();
            ps.close();

            LOGGER.info("End: getStoreVisitStatusByPlaceId");
            return result;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
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
    public void updateFCMTokenForUser(String userId, String fcmToken, String platform) {
        LOGGER.info("UserServiceDaoImpl Starts updateFCMTokenForUser: userId={}, fcmToken = ",userId, fcmToken);

        String sql = "DELETE from UserNotificationToken WHERE userId = ? AND platform = ?";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, platform);

            ps.executeUpdate();
            ps.close();

            if (null != fcmToken && !fcmToken.isEmpty()) {
                this.addFCMTokenForUser(userId, platform, fcmToken, conn);
            }

            conn.commit();
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

        LOGGER.info("UserServiceDaoImpl Ends updateFCMTokenForUser");
    }

    public void addFCMTokenForUser(String userId, String platform, String fcmToken, Connection conn) {
        LOGGER.info("UserServiceDaoImpl Starts addFCMTokenForUser::userId={}, platform: {}, fcmToken: {}",userId,platform, fcmToken);
        String sql = "INSERT INTO UserNotificationToken (userId, platform, fcmToken) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, platform);
            ps.setString(3, fcmToken);
            ps.executeUpdate();
            ps.close();

            LOGGER.info("UserServiceDaoImpl Ends addFCMTokenForUser");

        } catch (Exception e) {

        }
    }
}

package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.AuthenticationServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.User;
import com.snap2buy.themobilebackend.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by anoop on 07/23/16.
 */
@Component(value = BeanMapper.BEAN_AUTH_SERVICE_DAO)
@Scope("prototype")
public class AuthenticationServiceDaoImpl implements AuthenticationServiceDao {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createUser(User userInput) {
        LOGGER.info("---------------AuthenticationServiceDaoImpl Starts createUser::userInput= {}", userInput);
        String sql = "INSERT INTO User ( firstName, lastName, userId, password, email, customerCode, authToken, authTokenIssueDate, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userInput.getFirstName());
            ps.setString(2, userInput.getLastName() );
            ps.setString(3, userInput.getEmail());
            ps.setString(4, userInput.getPassword());
            ps.setString(5, userInput.getEmail());
            ps.setString(6, userInput.getCustomerCode() != null ? userInput.getCustomerCode().toUpperCase() : null);
            ps.setString(7, userInput.getAuthToken());
            ps.setString(8, userInput.getAuthTokenIssueDate());
            ps.setString(9, userInput.getRole());
            ps.setString(10, userInput.getStatus() != null ? userInput.getStatus() : "0");
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends createUser----------------\n");

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
	public List<LinkedHashMap<String, String>> getUserDetail(String userId) {
		LOGGER.info("---------------AuthenticationServiceDaoImpl Starts getUserDetail user Id = {}", userId);
        String sql = "SELECT userId, firstName, lastName, email, customerCode, authToken, status, role FROM User where userId = ?";
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
                map.put("firstName", rs.getString("firstName"));
                map.put("lastName", rs.getString("lastName"));
                map.put("email", rs.getString("email"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("authToken", rs.getString("authToken"));
                map.put("status", rs.getString("status"));
                map.put("role", rs.getString("role"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends getUserDetail----------------\n");

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
	public List<LinkedHashMap<String, String>> getUserForAuth(String userId) {
		LOGGER.info("---------------AuthenticationServiceDaoImpl Starts getUserForAuth user Id = {}" , userId);
        String sql = "SELECT userId, password, customerCode, firstName, lastName, authToken, authTokenIssueDate, role, status"
        		+ " FROM User where userId = ? and status <> ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, Constants.USER_STATUS_INACTIVE);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("userId", rs.getString("userId"));
                map.put("password", rs.getString("password"));
                map.put("customerCode", rs.getString("customerCode"));
                map.put("firstName", rs.getString("firstName"));
                map.put("lastName", rs.getString("lastName"));
                map.put("authToken", rs.getString("authToken"));
                map.put("role", rs.getString("role"));
                map.put("status", rs.getString("status"));
                map.put("authTokenIssueDate", rs.getString("authTokenIssueDate"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends getUserForAuth----------------\n");

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
	public void updateUser(User userInput) {
		LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateUser::userInput= {}", userInput);
		
		String sql ="UPDATE User  " +
                    "set " +
                        "firstName= ?  , lastName= ? , email= ? , customerCode= ? , role= ? , status= ? " +
                    "where userId = ? ";

        Connection conn = null;
        Integer id;
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, userInput.getFirstName());
            ps.setString(2, userInput.getLastName());
            ps.setString(3, userInput.getEmail());
            ps.setString(4, userInput.getCustomerCode());
            ps.setString(5, userInput.getRole());
            ps.setString(6, userInput.getStatus());
            ps.setString(7, userInput.getUserId());

            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
            
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends updateUser----------------\n");

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
	public void updateUserPassword(User userInput) {
	LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateUserPassword::userInput= {}", userInput);
		
		String sql = "UPDATE User  " +
                "set password=\"" + userInput.getPassword() + "\"  " +
                "where userId = \""+userInput.getUserId()+"\" ";

        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating user password failed, no rows affected.");
            }
            
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends updateUserPassword----------------\n");

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
	public void updateAuthToken(User userInput) {
	LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateAuthToken::userInput= {}", userInput);
		
		String sql = "UPDATE User set " +
                "authToken=\"" + userInput.getAuthToken() + "\"  " +
                ",authTokenIssueDate=\"" + userInput.getAuthTokenIssueDate() + "\"  " +
                "where userId = \""+userInput.getUserId()+"\" ";

        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating user auth token failed, no rows affected.");
            }
            
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends updateAuthToken----------------\n");

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
	public void deleteUser(String userId) {
		String sql = "DELETE FROM User where userId = \""+userId+"\" ";

        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
            
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends deleteUser----------------\n");

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
    public void updateUserStatusAndCustomerCode(User userInput) {
        LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateUserStatusAndCustomerCode::userInput={}", userInput);

        String sql = "UPDATE User set status = ? , customerCode = ?, lastLoginDate = ? where userId = ?";
        
        Timestamp timestamp = new Timestamp(new Date().getTime());
        
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userInput.getStatus());
            ps.setString(2, userInput.getCustomerCode().toUpperCase());
            ps.setTimestamp(3, timestamp);
            ps.setString(4, userInput.getUserId());
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }

            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends updateUserStatusAndCustomerCode----------------\n");

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
    public void updateUserEmail(User userInput) {
        LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateUserEmail::userInput={}", userInput);

        String sql = "UPDATE User set email=\"" + userInput.getUserId() + "\"  " +
                ", userId = \"" + userInput.getUserId() + "\"  " +
                "where email = \""+userInput.getOldEmail()+"\" AND " +
                "firstName = \""+userInput.getFirstName()+"\" AND " +
                "lastName = \""+userInput.getLastName()+"\" AND " +
                "userId = \""+userInput.getOldEmail()+"\"";

        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }

            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends updateUserEmail----------------\n");

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
    public void updateUserLastLoginStatus(String userId) {
        LOGGER.info("---------------AuthenticationServiceDaoImpl Starts updateUserLastLoginStatus::userID={}", userId);

        String sql = "UPDATE User SET lastLoginDate = ? WHERE userId = ?";
        Connection connection = null;

        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        try {
            connection = dataSource.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, timestamp);
            preparedStatement.setString(2, userId);
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
    public List<LinkedHashMap<String, String>> getCustomersProjectsByProjectIdAndCustomerCode(int projectId, String customerCode) {
        LOGGER.info("---------------AuthenticationServiceDaoImpl Starts getCustomersProjectsByProjectIdAndCustomerCode projectId = {}", projectId);
        String sql = "SELECT customerCode, projectId FROM CustomerCodeProjectMap where projectId = ? and customerCode = ?";
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, customerCode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("customerCode", rs.getString("customerCode"));
                map.put("projectId", rs.getString("projectId"));
                resultList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------AuthenticationServiceDaoImpl Ends getCustomersProjectsByProjectIdAndCustomerCode----------------\n");

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
    public void updateUsersLastAccessDate(String userId) {

        LOGGER.info("Starts updateUsersLastAccessDate");

        String sql = "UPDATE User set lastAccessedDate = NOW() where userId = ?";

        Connection conn = null;
        Integer id = -1;

        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setTimestamp(1, new Timestamp(new Date().getTime()));
            ps.setString(1, userId);

            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("updateUsersLastAccessDate failed.");
            }

            ps.close();
            LOGGER.info("Ends updateUsersLastAccessDate");

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

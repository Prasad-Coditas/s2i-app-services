package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.*;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 10/17/15.
 */
public interface AuthenticationServiceDao {

	 public void createUser(User user);
	 public List<LinkedHashMap<String, String>> getUserDetail(String userId);
	 public void updateUser(User user);
	 public void updateUserPassword(User user);
	 public void deleteUser(String userId);
	 public List<LinkedHashMap<String, String>> getUserForAuth(String userId);
	 public void updateAuthToken(User userInput);
	public void updateUserStatusAndCustomerCode(User user);
	public void updateUserEmail(User user);
	void updateUserLastLoginStatus(String userId);
	List<LinkedHashMap<String, String>> getCustomersProjectsByProjectIdAndCustomerCode(int projectId, String customerCode);

	void updateUsersLastAccessDate(String userId);
}

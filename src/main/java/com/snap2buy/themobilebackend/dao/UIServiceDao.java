package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.CustomerRoleMenuMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop on 09/28/18.
 */
public interface UIServiceDao {

    public List<LinkedHashMap<String, String>> getMenusByCustomerCodeRole(String customerCode, String role, String source);

    void addWebsiteEnquiry(String firstName, String lastName, String email, String phone, String company, String jobProfile, String note);

    List<Map<String, String>> getWebsiteEnquiries();

    void addUIMenus(CustomerRoleMenuMap customerRoleMenuMap);

    List<LinkedHashMap<String, String>> getUserNotificationTokenByCustomerCodeAndRole(String customerCode, String role);

    List<LinkedHashMap<String, String>> getUserNotificationTokenByUserId(String userId);
}

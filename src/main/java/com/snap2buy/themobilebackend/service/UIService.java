package com.snap2buy.themobilebackend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.CustomerRoleMenuMap;

/**
 * Created by Anoop on 09/28/18.
 */
public interface UIService {

    public List<LinkedHashMap<String, String>> getMenusForUser(InputObject inputObject);

    void addWebsiteEnquiry(String firstName, String lastName, String email, String phone, String company, String jobProfile, String note);

    List<Map<String, String>> getWebsiteEnquiries();

    void addUIMenus(CustomerRoleMenuMap customerRoleMenuMap);

    List<LinkedHashMap<String, String>> getUserNotificationTokenByCustomerCodeAndRole(String customerCode, String role);

    List<LinkedHashMap<String, String>> getUserNotificationTokenByUserId(String userId);
}

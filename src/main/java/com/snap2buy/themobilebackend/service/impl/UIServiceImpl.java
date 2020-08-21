package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.UIServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.UIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop on 09/28/18.
 */
@Component(value = BeanMapper.BEAN_UI_SERVICE)
@Scope("prototype")
public class UIServiceImpl implements UIService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_UI_SERVICE_DAO)
    private UIServiceDao uiServiceDao;

    @Override
	public List<LinkedHashMap<String, String>> getMenusForUser(InputObject inputObject) {
    	LOGGER.info("---------------MetaServiceImpl Starts getMenusForUser----------------\n");

        List<LinkedHashMap<String, String>> resultList = 
        		uiServiceDao.getMenusByCustomerCodeRole(inputObject.getCustomerCode(), inputObject.getRole(), inputObject.getSource());

        LOGGER.info("---------------MetaServiceImpl Ends getMenusForUser ----------------\n");
        return resultList;
	}

	@Override
    public void addWebsiteEnquiry(String firstName, String lastName, String email, String phone, String company, String jobProfile, String note) {
        LOGGER.info("Start UIService: addWebsiteEnquiry");

        uiServiceDao.addWebsiteEnquiry(firstName, lastName, email, phone, company, jobProfile, note);

        LOGGER.info("End: addWebsiteEnquiry");
    }

    @Override
    public List<Map<String, String>> getWebsiteEnquiries() {
        LOGGER.info("Start UIService: getWebsiteEnquiries");

        List<Map<String, String>> result = uiServiceDao.getWebsiteEnquiries();

        LOGGER.info("Ends UIService: getWebsiteEnquiries");
        return result;
    }

    @Override
    public void addUIMenus(CustomerRoleMenuMap customerRoleMenuMap) {
        LOGGER.info("Start UIService: addUIMenus");

        uiServiceDao.addUIMenus(customerRoleMenuMap);

        LOGGER.info("End: addUIMenus");
    }

    @Override
    public List<LinkedHashMap<String, String>> getUserNotificationTokenByCustomerCodeAndRole(String customerCode, String role) {
        LOGGER.info("Start UIService: getUserNotificationTokenByCustomerCodeAndRole");

        List<LinkedHashMap<String, String>> result = uiServiceDao.getUserNotificationTokenByCustomerCodeAndRole(customerCode, role);

        LOGGER.info("Ends UIService: getUserNotificationTokenByCustomerCodeAndRole");
        return result;
    }

    @Override
    public List<LinkedHashMap<String, String>> getUserNotificationTokenByUserId(String userId) {
        LOGGER.info("Start UIService: getUserNotificationTokenByUserId");

        List<LinkedHashMap<String, String>> result = uiServiceDao.getUserNotificationTokenByUserId(userId);

        LOGGER.info("Ends UIService: getUserNotificationTokenByUserId");
        return result;
    }
}

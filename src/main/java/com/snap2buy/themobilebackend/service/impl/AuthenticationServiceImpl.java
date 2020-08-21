package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.AuthenticationServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.AuthenticationService;
import com.snap2buy.themobilebackend.util.CryptoUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by ANOOP on 07/23/16.
 */
@Component(value = BeanMapper.BEAN_AUTH_SERVICE)
@Service
@Scope("prototype")
public class AuthenticationServiceImpl implements AuthenticationService{
	
	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

 	@Autowired
 	@Qualifier(BeanMapper.BEAN_AUTH_SERVICE_DAO)
 	private AuthenticationServiceDao authServiceDao;

	@Override
	public void createUser(User userInput) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts createUser user id = {}", userInput.getEmail());
		
		authServiceDao.createUser(userInput);

        LOGGER.info("---------------AuthenticationServiceImpl Ends createUser user id ={} ",userInput.getEmail());
	}

	@Override
	public List<LinkedHashMap<String, String>> getUserDetail(String userId) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts getUserDetail user id = {}", userId);

		List<LinkedHashMap<String, String>> user = authServiceDao.getUserDetail(userId);

        LOGGER.info("---------------AuthenticationServiceImpl Ends getUserDetail user id = {}",userId);
        return user;
	}

	@Override
	public void updateUser(User userInput) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUser user id = {}", userInput.getUserId());

		authServiceDao.updateUser(userInput);

        LOGGER.info("---------------AuthenticationServiceImpl Ends updateUser user id = {}", userInput.getUserId());
	}

	@Override
	public void updateUserPassword(User userInput) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUserPassword user id = {}", userInput.getUserId());

		userInput.setPassword(CryptoUtil.encrypt(userInput.getPassword()));
		
		authServiceDao.updateUserPassword(userInput);

        LOGGER.info("---------------AuthenticationServiceImpl Ends updateUserPassword user id = {}",userInput.getUserId());
	}
	
	@Override
	public void updateAuthToken(User userInput) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateAuthToken user id = {}", userInput.getUserId());

		authServiceDao.updateAuthToken(userInput);


        LOGGER.info("---------------AuthenticationServiceImpl Ends updateAuthToken user id = {}",userInput.getUserId());
	}

	@Override
	public void deleteUser(String userId) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts deleteUser user id = {}" , userId);

		authServiceDao.deleteUser(userId);

        LOGGER.info("---------------AuthenticationServiceImpl Ends deleteUser user id = {}",userId);
	}
	
	@Override
	public List<LinkedHashMap<String, String>> getUserForAuth(String userId) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts getUserForAuth with user id = {}", userId);

		List<LinkedHashMap<String, String>> userList = authServiceDao.getUserForAuth(userId);

        LOGGER.info("---------------AuthenticationServiceImpl Ends getUserForAuth with user id = {}", userId);
        return userList;
	}

	@Override
	public User refreshAuthToken(String userId, List<LinkedHashMap<String, String>> userList) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts refreshAuthToken with user id = {}", userId);
		User user = new User();
		user.setUserId(userId);
		
		SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	    inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	    
	    String authToken = userList.get(0).get("authToken");
		String authTokenIssueDate = userList.get(0).get("authTokenIssueDate");
		
		boolean refreshToken = StringUtils.isBlank(authToken); //if first login, refresh token
		
		Date currentDate = new Date();
		
		if ( !refreshToken ) { //if some token exists, see if it expired..if expired, refresh token
			try {
				Calendar authTokenIssueDateCal = Calendar.getInstance();
				authTokenIssueDateCal.setTime(inSdf.parse(authTokenIssueDate));
				authTokenIssueDateCal.add(Calendar.DATE, 14); //two weeks expiration
				if ( currentDate.after(authTokenIssueDateCal.getTime()) ) {
					refreshToken = true;
				}
			} catch (Exception e) {
				LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			}
		}
		
		if (  refreshToken ) {
			LOGGER.info("---------------AuthenticationServiceImpl::refreshAuthToken:: token expired..refreshing for user id = {}", userId);
			UUID newAuthToken = UUID.randomUUID();
			String newAuthTokenIssueDate = inSdf.format(currentDate);
			user.setAuthToken(newAuthToken.toString().trim());
			user.setAuthTokenIssueDate(newAuthTokenIssueDate);
			//Persist in DB
			updateAuthToken(user);
		} else {
			user.setAuthToken(authToken);
			user.setAuthTokenIssueDate(authTokenIssueDate);
			LOGGER.info("---------------AuthenticationServiceImpl::refreshAuthToken:: token still valid..not refreshing for user id = {}", userId);
		}
        LOGGER.info("---------------AuthenticationServiceImpl Ends refreshAuthToken with user id = {}", userId);
        return user;
	}

	public void updateUserStatusAndCustomerCode(User user) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUserStatusAndCustomerCode user id = {}", user.getUserId());

		authServiceDao.updateUserStatusAndCustomerCode(user);

		LOGGER.info("---------------AuthenticationServiceImpl Ends updateUserStatusAndCustomerCode user id = {}", user.getUserId());
	}

	public void updateUserEmail(User user) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUserEmail user email = {}", user.getEmail());

		authServiceDao.updateUserEmail(user);

		LOGGER.info("---------------AuthenticationServiceImpl Ends updateUserEmail user email = {}", user.getEmail());
	}

	@Override
	public void updateUserPasswordWithExistingSHA(User userInput) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUserPasswordWithExistingSHA user id = {}", userInput.getUserId());

		authServiceDao.updateUserPassword(userInput);

		LOGGER.info("---------------AuthenticationServiceImpl Ends updateUserPasswordWithExistingSHA user id = {}",userInput.getUserId());
	}

	@Override
	public void updateUserLastLoginStatus(String userId) {
		LOGGER.info("---------------AuthenticationServiceImpl Starts updateUserLastLoginStatus userId = {}", userId);

		authServiceDao.updateUserLastLoginStatus(userId);

		LOGGER.info("---------------AuthenticationServiceImpl Ends updateUserLastLoginStatus ----------------\n");
	}

	@Override
	public List<LinkedHashMap<String, String>> getCustomersProjectsByProjectIdAndCustomerCode(int projectId, String customerCode){
		LOGGER.info("-------------AuthenticationServiceImpl projectId: {}", projectId);

		return authServiceDao.getCustomersProjectsByProjectIdAndCustomerCode(projectId, customerCode);
	}

	@Override
	public void updateUsersLastAccessDate(String userId) {
		LOGGER.info("START: updateUsersLastAccessDate, userId:{}", userId);

		authServiceDao.updateUsersLastAccessDate(userId);

		LOGGER.info("END: updateUsersLastAccessDate");
	}
}

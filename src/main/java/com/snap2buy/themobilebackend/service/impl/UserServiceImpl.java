package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.UserServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.User;
import com.snap2buy.themobilebackend.model.UserAppConfig;
import com.snap2buy.themobilebackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Harshal
 */
@Component(value = BeanMapper.BEAN_USER_SERVICE)
@Scope("prototype")
public class UserServiceImpl implements UserService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_USER_SERVICE_DAO)
    private UserServiceDao userServiceDao;

    @Override
    public List<LinkedHashMap<String, String>> listUsers(){
        LOGGER.info("Start List Users");

        List<LinkedHashMap<String, String>> users = userServiceDao.listUsers();

        LOGGER.info("End List Users");
        return users;
    }

    @Override
    public List<LinkedHashMap<String, Object>> listUsersGeoLevels(String customerCode){
        LOGGER.info("Start listUsersGeoLevels");

        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        List<LinkedHashMap<String, String>> usersGeoLevels = userServiceDao.listUsersGeoLevels(customerCode);
        LinkedHashMap<String, String> customerGeoLevels = userServiceDao.getCustomerGeoLevels(customerCode);

        Map<String, List<String>> geoLevels = new HashMap<>();

        for (LinkedHashMap<String, String> userGeoLevel: usersGeoLevels){
            createGeoLevels(geoLevels, userGeoLevel, "geoLevel1", "geoLevel1Id");

            createGeoLevels(geoLevels, userGeoLevel, "geoLevel2", "geoLevel2Id");

            createGeoLevels(geoLevels, userGeoLevel, "geoLevel3", "geoLevel3Id");

            createGeoLevels(geoLevels, userGeoLevel, "geoLevel4", "geoLevel4Id");

            createGeoLevels(geoLevels, userGeoLevel, "geoLevel5", "geoLevel5Id");
        }

        LinkedHashMap<String, Object> resultObject;

        geoLevels = geoLevels.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        for(Map.Entry geoData: geoLevels.entrySet()){
            resultObject = new LinkedHashMap<>();
            resultObject.put("geoLevel", geoData.getKey());
            resultObject.put("name", customerGeoLevels.get(geoData.getKey()) == null ? "0": customerGeoLevels.get(geoData.getKey()));
            resultObject.put("geoLevelIds", geoData.getValue());
            result.add(resultObject);
        }
        LOGGER.info("End listUsersGeoLevels");
        return result;
    }

    private void createGeoLevels(Map<String, List<String>> geoLevels, LinkedHashMap<String, String> userGeoLevel, String geoLevel, String geoLevelId) {
        List<String> geoLevelIdsList;
        if (geoLevels.containsKey(geoLevel)) {
            geoLevelIdsList = geoLevels.get(geoLevel);
            geoLevelIdsList.add(userGeoLevel.get(geoLevelId));
        } else {
            geoLevelIdsList = new ArrayList<>();
            geoLevelIdsList.add(userGeoLevel.get(geoLevelId));
        }
        geoLevels.put(geoLevel, geoLevelIdsList.stream().sorted().distinct().collect(Collectors.toList()));
    }

    @Override
    public List<LinkedHashMap<String, String>> getCustomerCodes() {
        LOGGER.info("Start getCustomerCodes");

        List<LinkedHashMap<String, String>> customerCodes = userServiceDao.getCustomerCodes();

        LOGGER.info("End getCustomerCodes");
        return customerCodes;
    }

    @Override
    public List<LinkedHashMap<String, String>> getUserRoles() {
        LOGGER.info("Start getUserRoles");

        List<LinkedHashMap<String, String>> userRoles = userServiceDao.getUserRoles();

        LOGGER.info("End getUserRoles");
        return userRoles;
    }

    @Override
    public void createUserGeoMap(User user){
        LOGGER.info("Start createUserGeoMap");

        userServiceDao.createUserGeoMap(user);

        LOGGER.info("END createUserGeoMap");
    }

    @Override
    public void insertOrUpdateDeviceConfiguration(String supportedVersionName, int supportedVersionCode,
                                           String latestVersionName, int latestVersionCode){
        LOGGER.info("Start insertOrUpdateDeviceConfiguration");

        userServiceDao.insertOrUpdateDeviceConfiguration(supportedVersionName, supportedVersionCode,
                latestVersionName, latestVersionCode);

        LOGGER.info("END insertOrUpdateDeviceConfiguration");
    }

    @Override
    public List<LinkedHashMap<String, String>> getDeviceConfiguration(){
        LOGGER.info("Start getDeviceConfiguration");

        List<LinkedHashMap<String, String>> configs = userServiceDao.getDeviceConfiguration();

        LOGGER.info("END getDeviceConfiguration");
        return configs;
    }

    @Override
    public void createUserScreenConfig(String userId, List<String> screens) {
        LOGGER.info("Start: createUserScreenConfig");
        userServiceDao.createUserScreenConfig(userId, screens);
        LOGGER.info("End: createUserScreenConfig");
    }

    @Override
    public List<String> getUserScreenConfig(String userId) {
        LOGGER.info("Start: getUserScreenConfig");

        List<String> configs = userServiceDao.getUserScreenConfig(userId);

        LOGGER.info("End: getUserScreenConfig");
        return configs;
    }

    @Override
    public List<LinkedHashMap<String, String>> getUserAppConfig(String userId) {
        LOGGER.info("Start: getUserAppConfig");

        List<LinkedHashMap<String, String>> configs = userServiceDao.getUserAppConfig(userId);

        LOGGER.info("End: getUserAppConfig");

        return configs;
    }

    @Override
    public void insertOrUpdateUserAppConfig(UserAppConfig userAppConfig) {
        LOGGER.info("Start insertOrUpdateUserAppConfig UserAppConfig: {}", userAppConfig);

        userServiceDao.insertOrUpdateUserAppConfig(userAppConfig);

        LOGGER.info("END insertOrUpdateUserAppConfig");
    }

    @Override
    public List<LinkedHashMap<String, String>> getRecentStoreVisits(String userId, String customerCode, int numberOfRecords) {
        LOGGER.info("Start: getRecentStoreVisits userId:{}, numberOfRows:{}", userId,numberOfRecords);

        List<LinkedHashMap<String, String>> storeVisits = userServiceDao.getRecentStoreVisits(userId, customerCode, numberOfRecords);

        LOGGER.info("End: getRecentStoreVisits");
        return storeVisits;
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreVisitStatus(String userId, String storeId, String visitDate) {
        LOGGER.info("Start: getStoreVisitStatus userId:{}, storeId:{}, visitDate:{}", userId, storeId, visitDate);

        List<LinkedHashMap<String, String>> storeVisits = userServiceDao.getStoreVisitStatus(userId, storeId, visitDate);

        LOGGER.info("End: getStoreVisitStatus");
        return storeVisits;
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreVisitStatusByPlaceId(String userId, String placeId, String visitDate) {
        LOGGER.info("Start: getStoreVisitStatusByPlaceId userId:{}, placeId:{}, visitDate:{}", userId, placeId, visitDate);

        List<LinkedHashMap<String, String>> storeVisits = userServiceDao.getStoreVisitStatusByPlaceId(userId, placeId, visitDate);

        LOGGER.info("End: getStoreVisitStatusByPlaceId");
        return storeVisits;
    }

    @Override
    public void updateFCMTokenForUser(String userId, String fcmToken, String platform) {
        LOGGER.info("Start updateFCMTokenForUser UserId: {}, FCMToken: {}, platform:{}", userId, fcmToken, platform);

        userServiceDao.updateFCMTokenForUser(userId, fcmToken, platform);

        LOGGER.info("END updateFCMTokenForUser");
    }
}

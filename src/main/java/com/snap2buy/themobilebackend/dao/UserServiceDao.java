package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.User;
import com.snap2buy.themobilebackend.model.UserAppConfig;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Harshal
 */
public interface UserServiceDao {

    List<LinkedHashMap<String, String>> listUsers();

    List<LinkedHashMap<String, String>> listUsersGeoLevels(String customerCode);

    LinkedHashMap<String, String> getCustomerGeoLevels(String customerCode);

    List<LinkedHashMap<String, String>> getCustomerCodes();

    List<LinkedHashMap<String, String>> getUserRoles();

    void createUserGeoMap(User user);

    void insertOrUpdateDeviceConfiguration(String supportedVersionName, int supportedVersionCode,
                                                                          String latestVersionName, int latestVersionCode);

    List<LinkedHashMap<String, String>> getDeviceConfiguration();

    void createUserScreenConfig(String userId, List<String> screens);

    List<String> getUserScreenConfig(String userId);

    List<LinkedHashMap<String, String>> getUserAppConfig(String userId);

    void insertOrUpdateUserAppConfig(UserAppConfig userAppConfig);

    List<LinkedHashMap<String, String>> getRecentStoreVisits(String userId, String customerCode, int numberOfRecords);

    List<LinkedHashMap<String, String>> getStoreVisitStatus(String userId, String storeId, String visitDate);

    List<LinkedHashMap<String, String>> getStoreVisitStatusByPlaceId(String userId, String placeId, String visitDate);

    void updateFCMTokenForUser(String userId, String fcmToken, String platform);
}

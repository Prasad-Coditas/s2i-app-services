package com.snap2buy.themobilebackend.service;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anoop
 */
public interface SupportService {

	List<LinkedHashMap<String, Object>> getSupportInfoByStoreId(String customerCode, String storeId);

	List<LinkedHashMap<String, Object>> getSupportInfoByUserId(String customerCode, String userId);

	List<LinkedHashMap<String, Object>> getUserListByCustomerCode(String customerCode);

	List<LinkedHashMap<String, Object>> getCustomers();

	boolean createSupportRequest(Map<String, String> requestContents, List<String> attachments);

}

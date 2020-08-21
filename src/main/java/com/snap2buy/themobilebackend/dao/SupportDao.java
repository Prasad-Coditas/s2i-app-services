package com.snap2buy.themobilebackend.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop
 */
public interface SupportDao {

	List<Map<String, String>> getStoreVisitsByUserId(String customerCode, String userId);

	List<Map<String, String>> getStoreVisitsByStoreId(String customerCode, String storeId);

	List<LinkedHashMap<String, Object>> getUserListByCustomerCode(String customerCode);

	Map<String, String> getStoreAssignementByStoreId(String customerCode, String storeId);
	
	Map<String, String> getStoreAssignementByUserId(String customerCode, String userId);


}

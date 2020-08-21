package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.StoreMaster;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 10/31/15.
 */
public interface StoreMasterDao {
    String getStoreId(String longitude, String latitude);
    String getRetailerByStoreId(String storeId);
    List<LinkedHashMap<String,String>> getStoreOptions(String retailerCode);
    List<LinkedHashMap<String, String>>  getStores(String retailerChainCode, String state,String city);
    List<LinkedHashMap<String, String>> getStoreMasterByPlaceId(String placeId);
    void createStoreWithPlaceId(StoreMaster storeMaster);
    List<LinkedHashMap<String, String>> getGeoMappedStoresByUserId(String userId, String customerCode);
	void createStores(List<StoreMaster> storesToCreate);
	void updateStores(List<StoreMaster> storesToUpdate);
	public LinkedHashMap<String, String> getRetailerStoreIdMap(String retailerChainCode);
	Map<String, List<String>> getNextGeoLevelStoresByLevel(String customerCode, String geoLevel,
			String geoLevelId);
	void createStoreGeoMappings(String customerCode, Map<String, Map<String, String>> storeIdGeoLevelMap);
	void createStoreGeoMappingsViaPlaceID(String customerCode, Map<String, Map<String, String>> placeIdGeoLevelMap);

}

package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.itg.AreaGeo;
import com.snap2buy.themobilebackend.model.itg.GenericGeo;
import com.snap2buy.themobilebackend.model.itg.SearchFilter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop
 */
public interface ITGDashboardDao {

	public Map<String, String> getITGStoreDetails(int projectId, String storeId, String timePeriodType, String timePeriod);
	
	public List<Map<String, Object>> getITGStoreVisitImages(int projectId, String storeId, String timePeriodType, String timePeriod);
	
	public List<AreaGeo> getITGGeoMappingForUser(String customerCode, String userId, GenericGeo rootGeo);
	
	public List<LinkedHashMap<String, Object>> getITGStoresWithFilters(int projectId, String geoLevelId, String timePeriodType,
			String timePeriod, String limit, SearchFilter filter);
	
	public List<LinkedHashMap<String, String>> getITGStoresForReport(int projectId, String customerCode,
			String geoLevel, String geoLevelId, String timePeriodType, String timePeriod);
	
	public Map<String, String> getITGStats(int projectId, String geoLevelInternalId, String timePeriodType,
			String timePeriod, String storeType);
	
}

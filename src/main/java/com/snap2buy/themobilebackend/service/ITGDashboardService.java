package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.model.itg.GenericGeo;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Anoop
 */
public interface ITGDashboardService {
    
	public List<LinkedHashMap<String, Object>> getITGStoreDetails(InputObject inputObject);
	
	public List<LinkedHashMap<String, Object>> getITGStoresWithFilters(InputObject inputObject);
	
	public List<LinkedHashMap<String, Object>> getITGStats(InputObject inputObject);
	
	public File getITGReport(InputObject inputObject, String tempFilePath);
	
	public List<GenericGeo> getITGGeoMappingForUser(InputObject inputObject);
}

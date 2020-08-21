package com.snap2buy.themobilebackend.upload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PrmResultUploadTracker {
	
	private PrmResultUploadTracker(){
		//private constructor to force singleton
	}
	
	private static final Map<String,String> UPLOAD_STATUS_MAP = new ConcurrentHashMap<String,String>();
	
	public static void add(String projectId){
		UPLOAD_STATUS_MAP.put(projectId, "Queued");
	}
	
	public static void update(String projectId, String status){
		UPLOAD_STATUS_MAP.put(projectId, status);
	}
	
	public static void removeAll() {
		UPLOAD_STATUS_MAP.clear();
	}
	
	public static Map<String,String> getAll() {
		return UPLOAD_STATUS_MAP;
	}
}

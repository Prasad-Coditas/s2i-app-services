package com.snap2buy.themobilebackend.upload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UploadStatusTracker {
	
	private UploadStatusTracker(){
		//private constructor to force singleton
	}
	
	private static final Map<String,String> UPLOAD_STATUS_MAP = new ConcurrentHashMap<String,String>();
	
	public static void add(String projectId){
		UPLOAD_STATUS_MAP.put(projectId+"", "");
	}
	
	public static void remove(String projectId){
		UPLOAD_STATUS_MAP.remove(projectId);
	}
	
	public static String get(String projectId){
		if ( UPLOAD_STATUS_MAP.containsKey(projectId) ) {
			return "IN PROGRESS";
		} else {
			return "NOT IN PROGRESS";
		}
	}
}

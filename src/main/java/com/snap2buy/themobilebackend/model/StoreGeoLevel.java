package com.snap2buy.themobilebackend.model;

public enum StoreGeoLevel {
	
	GEO_LEVEL_1("geoLevel1"),
	GEO_LEVEL_2("geoLevel2"),
	GEO_LEVEL_3("geoLevel3"),
	GEO_LEVEL_4("geoLevel4"),
	GEO_LEVEL_5("geoLevel5");

	private final String geoLevel;
	
	StoreGeoLevel(String geoLevel){
		this.geoLevel=geoLevel;
	}
	
	public String getGeoLevel(){
		return this.geoLevel;
	}
	
	public static boolean contains(String value) {
		for (StoreGeoLevel geoLevel : StoreGeoLevel.values()) {
	        if (geoLevel.getGeoLevel().equals(value)) {
	            return true;
	        }
	    }
		return false;
	}
}

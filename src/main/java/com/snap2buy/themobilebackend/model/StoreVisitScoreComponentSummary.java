package com.snap2buy.themobilebackend.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class StoreVisitScoreComponentSummary {
	
	private String name;
    private Map<String,String> componentScoreDesc = new LinkedHashMap<String,String>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getComponentScoreDesc() {
		return componentScoreDesc;
	}
	public void setComponentScoreDesc(Map<String, String> componentScoreDesc) {
		this.componentScoreDesc = componentScoreDesc;
	}
	@Override
	public String toString() {
		return "StoreVisitScoreComponentSummary [name=" + name + ", componentScoreDesc=" + componentScoreDesc + "]";
	}
}

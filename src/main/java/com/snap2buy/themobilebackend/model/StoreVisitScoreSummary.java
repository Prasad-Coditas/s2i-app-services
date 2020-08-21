package com.snap2buy.themobilebackend.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop on 09/26/17.
 */
public class StoreVisitScoreSummary {
    String scoreId;
    String scoreName;
    Map<String,String> storeVisitCounts = new LinkedHashMap<String,String>();
    List<StoreVisitScoreComponentSummary> componentNames = new ArrayList<StoreVisitScoreComponentSummary>();

	public Map<String, String> getStoreVisitCounts() {
		return storeVisitCounts;
	}

	public void setStoreVisitCounts(Map<String, String> storeVisitCounts) {
		this.storeVisitCounts = storeVisitCounts;
	}

	public String getScoreId() {
		return scoreId;
	}

	public void setScoreId(String scoreId) {
		this.scoreId = scoreId;
	}

	public String getScoreName() {
		return scoreName;
	}

	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}

	public List<StoreVisitScoreComponentSummary> getComponentNames() {
		return componentNames;
	}

	public void setComponentNames(List<StoreVisitScoreComponentSummary> componentNames) {
		this.componentNames = componentNames;
	}

	@Override
	public String toString() {
		return "StoreVisitScoreSummary [scoreId=" + scoreId + ", scoreName=" + scoreName + ", storeVisitCounts="
				+ storeVisitCounts + ", componentNames=" + componentNames + "]";
	}
}

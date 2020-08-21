package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by Anoop on 09/26/17.
 */
public class StoreVisitAllScores {
    
	private List<StoreVisitScoreSummary> summary;
	
	private List<StoreVisitScore> storeVisits;

	public List<StoreVisitScoreSummary> getSummary() {
		return summary;
	}

	public void setSummary(List<StoreVisitScoreSummary> summary) {
		this.summary = summary;
	}

	public List<StoreVisitScore> getStoreVisits() {
		return storeVisits;
	}

	public void setStoreVisits(List<StoreVisitScore> storeVisits) {
		this.storeVisits = storeVisits;
	}

	@Override
	public String toString() {
		return "StoreVisitAllScores [summary=" + summary + ", storeVisits=" + storeVisits + "]";
	}
	
}

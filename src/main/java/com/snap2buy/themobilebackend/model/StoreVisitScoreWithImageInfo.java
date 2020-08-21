package com.snap2buy.themobilebackend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anoop on 10/28/17.
 */
public class StoreVisitScoreWithImageInfo extends StoreVisitScore{
	
	List<StoreUPC> projectUPCs = new ArrayList<StoreUPC>();
	
	List<StoreImageDetails> imageUUIDs = new ArrayList<StoreImageDetails>();

	public List<StoreUPC> getProjectUPCs() {
		return projectUPCs;
	}

	public void setProjectUPCs(List<StoreUPC> projectUPCs) {
		this.projectUPCs = projectUPCs;
	}

	public List<StoreImageDetails> getImageUUIDs() {
		return imageUUIDs;
	}

	public void setImageUUIDs(List<StoreImageDetails> imageUUIDs) {
		this.imageUUIDs = imageUUIDs;
	}

	@Override
	public String toString() {
		return "StoreVisitScoreWithImageInfo [projectUPCs=" + projectUPCs + ", imageUUIDs=" + imageUUIDs + ", storeId="
				+ storeId + ", retailerStoreId=" + retailerStoreId + ", retailer=" + retailer + ", street=" + street
				+ ", city=" + city + ", state=" + state + ", taskId=" + taskId + ", agentId=" + agentId + ", visitDate="
				+ visitDate + ", processedDate=" + processedDate + ", scoreId=" + scoreId + ", scoreName=" + scoreName
				+ ", score=" + score + ", scoreDesc=" + scoreDesc + ", scoreComment=" + scoreComment
				+ ", componentScores=" + componentScores + "]";
	}   
}

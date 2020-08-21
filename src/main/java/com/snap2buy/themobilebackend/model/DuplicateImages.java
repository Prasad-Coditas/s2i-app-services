package com.snap2buy.themobilebackend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sachin on 10/31/15.
 */
public class DuplicateImages {
	
	private String imageHashScore;
    private String slNo;
    private List<DuplicateImageInfo> storeIds = new ArrayList<DuplicateImageInfo>();

    
    public String getImageHashScore() {
		return imageHashScore;
	}


	public void setImageHashScore(String imageHashScore) {
		this.imageHashScore = imageHashScore;
	}


	public String getSlNo() {
		return slNo;
	}


	public void setSlNo(String slNo) {
		this.slNo = slNo;
	}


	public List<DuplicateImageInfo> getStoreIds() {
		return storeIds;
	}


	public void setStoreIds(List<DuplicateImageInfo> storeIds) {
		this.storeIds = storeIds;
	}

    @Override
    public String toString() {
        return "Duplicate Iamges{" +
                "imageHashScore='" + imageHashScore + '\'' +
                ", slNo='" + slNo + '\'' +
                ", storeIds='" + storeIds + '\'' +
                '}';
    }
}

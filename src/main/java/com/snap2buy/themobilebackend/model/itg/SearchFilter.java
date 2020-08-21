package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Search Filters")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchFilter {
	
	@XmlElement
    private String storePlanType;
	
	@XmlElement
    private String planFacingCompliance;
	
	@XmlElement
    private String isAboveVolumeShare;
	
	@XmlElement
    private String itgbFacingCount;
	
	@XmlElement
    private String isEDLPStore;
	
	@XmlElement
    private String hasITGBPrice;
	
	@XmlElement
    private String sortBy;
	
	@XmlElement
    private String sortOrder;
	
	@XmlElement
    private String storeId;

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getStorePlanType() {
		return storePlanType;
	}

	public void setStorePlanType(String storePlanType) {
		this.storePlanType = storePlanType;
	}

	public String getPlanFacingCompliance() {
		return planFacingCompliance;
	}

	public void setPlanFacingCompliance(String planFacingCompliance) {
		this.planFacingCompliance = planFacingCompliance;
	}

	public String getIsAboveVolumeShare() {
		return isAboveVolumeShare;
	}

	public void setIsAboveVolumeShare(String isAboveVolumeShare) {
		this.isAboveVolumeShare = isAboveVolumeShare;
	}

	public String getItgbFacingCount() {
		return itgbFacingCount;
	}

	public void setItgbFacingCount(String itgbFacingCount) {
		this.itgbFacingCount = itgbFacingCount;
	}

	public String getIsEDLPStore() {
		return isEDLPStore;
	}

	public void setIsEDLPStore(String isEDLPStore) {
		this.isEDLPStore = isEDLPStore;
	}

	public String getHasITGBPrice() {
		return hasITGBPrice;
	}

	public void setHasITGBPrice(String hasITGBPrice) {
		this.hasITGBPrice = hasITGBPrice;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public String toString() {
		return "SearchFilter [storePlanType=" + storePlanType + ", planFacingCompliance=" + planFacingCompliance
				+ ", isAboveVolumeShare=" + isAboveVolumeShare + ", itgbFacingCount=" + itgbFacingCount
				+ ", isEDLPStore=" + isEDLPStore + ", hasITGBPrice=" + hasITGBPrice + ", sortBy=" + sortBy
				+ ", sortOrder=" + sortOrder + ", storeId=" + storeId + "]";
	}
	
}

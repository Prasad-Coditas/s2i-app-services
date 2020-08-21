package com.snap2buy.themobilebackend.model;

import java.util.List;

/**
 * Created by sachin on 10/31/15.
 */
public class StoreWithImages {
    String storeId;
    String retailerStoreId;
    String retailerChainCode;
    String retailer;
    String street;
    String city;
    String stateCode;
    String state;
    String zip;
    String imageCount;
    List<StoreImageInfo> imageUUIDs;

    public String getImageCount() {
		return imageCount;
	}

	public void setImageCount(String imageCount) {
		this.imageCount = imageCount;
	}

	public List<StoreImageInfo> getImageUUIDs() {
		return imageUUIDs;
	}

	public void setImageUUIDs(List<StoreImageInfo> imageUUIDs) {
		this.imageUUIDs = imageUUIDs;
	}

	public StoreWithImages() {
        super();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getRetailerStoreId() {
        return retailerStoreId;
    }

    public void setRetailerStoreId(String retailerStoreId) {
        this.retailerStoreId = retailerStoreId;
    }

    public String getRetailerChainCode() {
        return retailerChainCode;
    }

    public void setRetailerChainCode(String retailerChainCode) {
        this.retailerChainCode = retailerChainCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    @Override
    public String toString() {
        return "StoreWithImages{" +
                "storeId='" + storeId + '\'' +
                ", retailerStoreId='" + retailerStoreId + '\'' +
                ", retailerChainCode='" + retailerChainCode + '\'' +
                ", retailer='" + retailer + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", imageCount='" + imageCount + '\'' +
                ", imageUUIDs='" + imageUUIDs + '\'' +
                '}';
    }
}

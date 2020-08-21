package com.snap2buy.themobilebackend.model;

import java.util.Map;

/**
 * Created by sachin on 10/31/15.
 */
public class StoreMaster {
    String storeId;
    String retailerStoreId;
    String retailerChainCode;
    String retailer;
    String street;
    String city;
    String stateCode;
    String state;
    String zip;
    String latitude;
    String longitude;
    String comments;
    String placeId;
    String name;
    String country;

    public StoreMaster() {
        super();
    }

    public StoreMaster(String storeId, String retailerStoreId, String retailerChainCode, String retailer, String street, String city, String stateCode, String state, String zip, String latitude, String longitude, String comments) {
        this.storeId = storeId;
        this.retailerStoreId = retailerStoreId;
        this.retailerChainCode = retailerChainCode;
        this.retailer = retailer;
        this.street = street;
        this.city = city;
        this.stateCode = stateCode;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
        this.comments = comments;
    }

    public StoreMaster(Map<String, String> oneStoreMap) {
    	this.storeId = oneStoreMap.get("storeId");
        this.retailerStoreId = oneStoreMap.get("retailerStoreId");
        this.retailerChainCode = oneStoreMap.get("retailerChainCode");
        this.retailer = oneStoreMap.get("retailer");
        this.street = oneStoreMap.get("street");
        this.city = oneStoreMap.get("city");
        this.stateCode = oneStoreMap.get("stateCode");
        this.state = oneStoreMap.get("state");
        this.zip = oneStoreMap.get("zip");
        this.latitude = oneStoreMap.get("latitude");
        this.longitude = oneStoreMap.get("longitude");
        this.comments = oneStoreMap.get("comments");
        this.placeId = oneStoreMap.get("placeId");
        this.name = oneStoreMap.get("name");
        this.country = oneStoreMap.get("country");
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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

    public String getComments() {
        return comments;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "StoreMaster{" +
                "storeId='" + storeId + '\'' +
                ", retailerStoreId='" + retailerStoreId + '\'' +
                ", retailerChainCode='" + retailerChainCode + '\'' +
                ", retailer='" + retailer + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}

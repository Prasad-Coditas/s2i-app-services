package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 5/29/16.
 */
public class StoreImageAnalysis {

    String storeId;
    String retailerCode;
    String retailerStoreId;
    String state;
    String city;
    String customerProjectId;
    String taskId;
    String upc;
    String facingCount;
    String score;
    String brand;
    String visitDate;

    public StoreImageAnalysis(String storeId, String retailerCode, String retailerStoreId, String state, String city, String customerProjectId, String taskId, String upc, String facingCount, String score, String brand, String visitDate) {
        this.storeId = storeId;
        this.retailerCode = retailerCode;
        this.retailerStoreId = retailerStoreId;
        this.state = state;
        this.city = city;
        this.customerProjectId = customerProjectId;
        this.taskId = taskId;
        this.upc = upc;
        this.facingCount = facingCount;
        this.score = score;
        this.brand = brand;
        this.visitDate = visitDate;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getRetailerStoreId() {
        return retailerStoreId;
    }

    public void setRetailerStoreId(String retailerStoreId) {
        this.retailerStoreId = retailerStoreId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCustomerProjectId() {
        return customerProjectId;
    }

    public void setCustomerProjectId(String customerProjectId) {
        this.customerProjectId = customerProjectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getFacingCount() {
        return facingCount;
    }

    public void setFacingCount(String facingCount) {
        this.facingCount = facingCount;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    @Override
    public String toString() {
        return "StoreImageAnalysis{" +
                "storeId='" + storeId + '\'' +
                ", retailerCode='" + retailerCode + '\'' +
                ", retailerStoreId='" + retailerStoreId + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", customerProjectId='" + customerProjectId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", upc='" + upc + '\'' +
                ", expectedFacingCount='" + facingCount + '\'' +
                ", score='" + score + '\'' +
                ", brand='" + brand + '\'' +
                ", visitDate='" + visitDate + '\'' +
                '}';
    }
}

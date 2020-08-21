package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 6/18/16.
 */
public class ProjectStoreData {

    String customerCode;
    String customerProjectId;
    String storeId;
    String upc;
    String facing;
    String upcConfidence;


    public ProjectStoreData() {
        super();
    }

    public ProjectStoreData(String customerCode, String customerProjectId, String storeId, String upc, String facing, String upcConfidence) {
        this.customerCode = customerCode;
        this.customerProjectId = customerProjectId;
        this.storeId = storeId;
        this.upc = upc;
        this.facing = facing;
        this.upcConfidence = upcConfidence;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerProjectId() {
        return customerProjectId;
    }

    public void setCustomerProjectId(String customerProjectId) {
        this.customerProjectId = customerProjectId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }

    public String getUpcConfidence() {
        return upcConfidence;
    }

    public void setUpcConfidence(String upcConfidence) {
        this.upcConfidence = upcConfidence;
    }

    @Override
    public String toString() {
        return "ProjectStoreData{" +
                "customerCode='" + customerCode + '\'' +
                ", customerProjectId='" + customerProjectId + '\'' +
                ", storeId='" + storeId + '\'' +
                ", upc='" + upc + '\'' +
                ", facing='" + facing + '\'' +
                ", upcConfidence='" + upcConfidence + '\'' +
                '}';
    }
}

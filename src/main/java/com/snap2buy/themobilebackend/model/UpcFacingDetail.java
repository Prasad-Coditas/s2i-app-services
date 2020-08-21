package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 4/9/16.
 */
public class UpcFacingDetail {
    String upc;
    String count;
    String productShortName;
    String productLongName;
    String brandName;

    public UpcFacingDetail(String upc, String count, String productShortName, String productLongName, String brandName) {
        this.upc = upc;
        this.count = count;
        this.productShortName = productShortName;
        this.productLongName = productLongName;
        this.brandName = brandName;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductLongName() {
        return productLongName;
    }

    public void setProductLongName(String productLongName) {
        this.productLongName = productLongName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public String toString() {
        return "UpcFacingDetail{" +
                "upc='" + upc + '\'' +
                ", count='" + count + '\'' +
                ", productShortName='" + productShortName + '\'' +
                ", productLongName='" + productLongName + '\'' +
                ", brandName='" + brandName + '\'' +
                '}';
    }
}

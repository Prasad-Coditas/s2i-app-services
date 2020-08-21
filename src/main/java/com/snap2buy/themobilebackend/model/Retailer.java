package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by sachin on 5/29/16.
 */
@XmlRootElement(name = "Retailer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Retailer {
    @XmlElement
    String id;
    @XmlElement
    String retailerCode;
    @XmlElement
    String name;
    @XmlElement
    String type;
    @XmlElement
    String logo;
    @XmlElement
    String createdDate;
    @XmlElement
    String status;

    public Retailer() {
        super();
    }

    public Retailer(String id, String retailerCode, String name, String type, String logo, String createdDate, String status) {
        this.id = id;
        this.retailerCode = retailerCode;
        this.name = name;
        this.type = type;
        this.logo = logo;
        this.createdDate = createdDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Retailer{" +
                "id='" + id + '\'' +
                ", customerCode='" + retailerCode + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", logo='" + logo + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

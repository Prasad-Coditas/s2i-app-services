package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Harshal
 */
@XmlRootElement(name = "ProjectRepResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectRepResponse {

    @XmlElement
    String storeId;
    
    @XmlElement
    String retailerStoreId;
    
    @XmlElement
    String customerStoreNumber;

    @XmlElement
    String projectId;

    @XmlElement
    String customerProjectId;
    
    @XmlElement
    String customerCode;
    
    @XmlElement
    String taskId;

    @XmlElement
    String placeId;

    @XmlElement
    String name;

    @XmlElement
    String street;

    @XmlElement
    String city;

    @XmlElement
    String state;

    @XmlElement
    String country;

    @XmlElement
    String latitude;

    @XmlElement
    String longitude;

    String agentId;

    String visitDate;

    @XmlElement(name = "responses")
    List<ProjectResponse> projectResponseList = new ArrayList<>();

    public ProjectRepResponse() {
        super();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public List<ProjectResponse> getProjectResponseList() {
        return projectResponseList;
    }

    public void setProjectResponseList(List<ProjectResponse> projectResponseList) {
        this.projectResponseList = projectResponseList;
    }

	public String getRetailerStoreId() {
		return retailerStoreId;
	}

	public void setRetailerStoreId(String retailerStoreId) {
		this.retailerStoreId = retailerStoreId;
	}

	public String getCustomerStoreNumber() {
		return customerStoreNumber;
	}

	public void setCustomerStoreNumber(String customerStoreNumber) {
		this.customerStoreNumber = customerStoreNumber;
	}

	public String getCustomerProjectId() {
		return customerProjectId;
	}

	public void setCustomerProjectId(String customerProjectId) {
		this.customerProjectId = customerProjectId;
	}
	
	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	@Override
	public String toString() {
		return "ProjectRepResponse [storeId=" + storeId + ", retailerStoreId=" + retailerStoreId
				+ ", customerStoreNumber=" + customerStoreNumber + ", projectId=" + projectId + ", customerProjectId="
				+ customerProjectId + ", customerCode=" + customerCode + ", taskId=" + taskId + ", placeId=" + placeId
				+ ", name=" + name + ", street=" + street + ", city=" + city + ", state=" + state + ", country="
				+ country + ", latitude=" + latitude + ", longitude=" + longitude + ", agentId=" + agentId
				+ ", visitDate=" + visitDate + ", projectResponseList=" + projectResponseList + "]";
	}
	
}

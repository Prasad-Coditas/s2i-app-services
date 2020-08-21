package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by sachin on 5/29/16.
 */
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
public class User {
   
    @XmlElement
    String firstName;
    @XmlElement
    String lastName;
    @XmlElement
    String customerCode;
    @XmlElement
    String userId;
    @XmlElement
    String password;
    @XmlElement
    String email;
    @XmlElement
    String role;
    @XmlElement
    String createdDate;
    @XmlElement
    String createdBy;
    @XmlElement
    String updatedDate;
    @XmlElement
    String updatedBy;
    @XmlElement
    String lastLoginDate;
    @XmlElement
    String status;
    @XmlElement
    String authToken;
    @XmlElement
    String authTokenIssueDate;

    @XmlElement
    String oldEmail;

    @XmlElement
    String geoLevel;

    @XmlElement
    String geoLevelId;

    @XmlElement
    String geoLevelName;

    public User() {
        super();
    }

    public User(String userId, String password, String firstName,
                String lastName, String email, String customerCode,
                String role, String createdDate, String createdBy,
                String updatedDate, String updatedBy, String lastLoginDate, String status) {
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.customerCode = customerCode;
        this.role = role;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.updatedDate = updatedDate;
        this.updatedBy = updatedBy;
        this.lastLoginDate = lastLoginDate;
        this.status = status;
    }

    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthTokenIssueDate() {
		return authTokenIssueDate;
	}

	public void setAuthTokenIssueDate(String authTokenIssueDate) {
		this.authTokenIssueDate = authTokenIssueDate;
	}

    public String getOldEmail() {
        return oldEmail;
    }

    public void setOldEmail(String oldEmail) {
        this.oldEmail = oldEmail;
    }

    public String getGeoLevel() {
        return geoLevel;
    }

    public void setGeoLevel(String geoLevel) {
        this.geoLevel = geoLevel;
    }

    public String getGeoLevelId() {
        return geoLevelId;
    }

    public void setGeoLevelId(String geoLevelId) {
        this.geoLevelId = geoLevelId;
    }

    public String getGeoLevelName() {
        return geoLevelName;
    }

    public void setGeoLevelName(String geoLevelName) {
        this.geoLevelName = geoLevelName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", customerCode='" + customerCode + '\'' +
                ", role='" + role + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedDate='" + updatedDate + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", lastLoginDate='" + lastLoginDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

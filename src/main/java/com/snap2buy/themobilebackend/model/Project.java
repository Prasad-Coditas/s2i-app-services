package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 5/29/16.
 */
@XmlRootElement(name = "Project")
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {
	@XmlElement
    String id;
    @XmlElement
    String customerProjectId;
	@XmlElement
    String projectName;
    @XmlElement
    String projectTypeId;
    @XmlElement
    String categoryId;
    @XmlElement
    String retailerCode;
    @XmlElement
    String storeCount;
    @XmlElement
    String startDate;
    @XmlElement
    String createdDate;
    @XmlElement
    String createdBy;
    @XmlElement
    String updatedDate;
    @XmlElement
    String updatedBy;
    @XmlElement
    String status;
    @XmlElement
    String description;
    @XmlElement
    String owner;
    @XmlElement
    String endDate;
    @XmlElement
    String isParentProject;
    @XmlElement
    String parentProjectId;
    
    String customerCode;

    @XmlElement
    String blurThreshold;

	@XmlElement(name = "projectUpcList")
    List<ProjectUpc> projectUpcList = new ArrayList<>();

    @XmlElement(name = "projectQuestionsList")
    List<ProjectQuestion> projectQuestionsList = new ArrayList<>();
    
    @XmlElement(name = "projectObjectivesList")
    List<ProjectQuestionObjective> projectObjectives = new ArrayList<>();
    
    @XmlElement(name = "projectStoreGradingCriteriaList")
    List<ProjectStoreGradingCriteria> projectStoreGradingCriteriaList = new ArrayList<>();
    
    @XmlElement(name = "waves")
    List<ProjectWaveConfig> waves = new ArrayList<ProjectWaveConfig>();
    
    @XmlElement
    String aggregationType;

    public Project() {
        super();
    }

    public Project(String id, String projectName, String customerProjectId, String customerCode, String projectTypeId, String categoryId, String retailerCode, String storeCount, String startDate, String createdDate, String createdBy, String updatedDate, String updatedBy, String status, String description, String owner, String endDate, List<ProjectUpc> projectUpcList, String isParentProject, String parentProjectId) {
        this.id = id;
        this.projectName = projectName;
        this.projectTypeId = projectTypeId;
        this.customerProjectId = customerProjectId;
        this.categoryId = categoryId;
        this.retailerCode = retailerCode;
        this.storeCount = storeCount;
        this.startDate = startDate;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.updatedDate = updatedDate;
        this.updatedBy = updatedBy;
        this.status = status;
        this.description = description;
        this.owner = owner;
        this.endDate = endDate;
        this.projectUpcList = projectUpcList;
        this.isParentProject = isParentProject;
        this.parentProjectId = parentProjectId;
    }

    public Project(String id, String projectName, String customerProjectId, String customerCode, String projectTypeId, String categoryId, String retailerCode, String storeCount, String startDate, String createdDate, String createdBy, String updatedDate, String updatedBy, String status, String description, String owner, String endDate, String isParentProject, String parentProjectId) {
        this.id = id;
        this.projectName = projectName;
        this.projectTypeId = projectTypeId;
        this.customerProjectId = customerProjectId;
        this.categoryId = categoryId;
        this.retailerCode = retailerCode;
        this.storeCount = storeCount;
        this.startDate = startDate;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.updatedDate = updatedDate;
        this.updatedBy = updatedBy;
        this.status = status;
        this.description = description;
        this.owner = owner;
        this.endDate = endDate;
        this.isParentProject = isParentProject;
        this.parentProjectId = parentProjectId;
    }
    
    public Project(Map<String,String> inputMap) {
        this.id = inputMap.get("id") == null ? "" : inputMap.get("id");
        this.customerProjectId = inputMap.get("customerProjectId") == null ? "" : inputMap.get("customerProjectId");
        this.projectName = inputMap.get("projectName") == null ? "" : inputMap.get("projectName");
        this.projectTypeId = inputMap.get("projectTypeId") == null ? "" : inputMap.get("projectTypeId");
        this.categoryId = inputMap.get("categoryId") == null ? "" : inputMap.get("categoryId");
        this.retailerCode = inputMap.get("retailerCode") == null ? "" : inputMap.get("retailerCode");
        this.storeCount = inputMap.get("storeCount") == null ? "" : inputMap.get("storeCount");
        this.startDate =inputMap.get("startDate") == null ? "" : inputMap.get("startDate") ;
        this.createdDate = inputMap.get("createdDate") == null ? "" : inputMap.get("createdDate");
        this.createdBy = inputMap.get("createdBy") == null ? "" : inputMap.get("createdBy");
        this.updatedDate = inputMap.get("updatedDate") == null ? "" : inputMap.get("updatedDate");
        this.updatedBy =inputMap.get("updatedBy") == null ? "" : inputMap.get("updatedBy");
        this.status = inputMap.get("status") == null ? "" : inputMap.get("status");
        this.description = inputMap.get("description") == null ? "" : inputMap.get("description");
        this.owner = inputMap.get("owner") == null ? "" : inputMap.get("owner");
        this.endDate = inputMap.get("endDate") == null ? "" : inputMap.get("endDate");
        this.isParentProject = inputMap.get("isParentProject") == null ? "" : inputMap.get("isParentProject");
        this.parentProjectId = inputMap.get("parentProjectId") == null ? "" : inputMap.get("parentProjectId");
    }

    public List<ProjectUpc> getProjectUpcList() {
        return projectUpcList;
    }

    public void setProjectUpcList(List<ProjectUpc> projectUpcList) {
        this.projectUpcList = projectUpcList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public String getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(String projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(String storeCount) {
        this.storeCount = storeCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public List<ProjectQuestion> getProjectQuestionsList() {
		 return projectQuestionsList;
	}

	public void setProjectQuestionsList(List<ProjectQuestion> projectQuestionsList) {
		this.projectQuestionsList = projectQuestionsList;
	}
	
	public List<ProjectQuestionObjective> getProjectObjectives() {
		return projectObjectives;
	}

	public void setProjectObjectives(
			List<ProjectQuestionObjective> projectObjectives) {
		this.projectObjectives = projectObjectives;
	}

    public String getIsParentProject() {
        return isParentProject;
    }

    public void setIsParentProject(String isParentProjectarent) {
        this.isParentProject = isParentProjectarent;
    }

    public String getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(String parentProjectId) {
        this.parentProjectId = parentProjectId;
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
    
    public List<ProjectStoreGradingCriteria> getProjectStoreGradingCriteriaList() {
		return projectStoreGradingCriteriaList;
	}

	public void setProjectStoreGradingCriteriaList(List<ProjectStoreGradingCriteria> projectStoreGradingCriteriaList) {
		this.projectStoreGradingCriteriaList = projectStoreGradingCriteriaList;
	}

    public String getBlurThreshold() {
        return blurThreshold;
    }

    public void setBlurThreshold(String blurThreshold) {
        this.blurThreshold = blurThreshold;
    }
    
	public List<ProjectWaveConfig> getWaves() {
		return waves;
	}

	public void setWaves(List<ProjectWaveConfig> waves) {
		this.waves = waves;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}

	@Override
	public String toString() {
		return "Project [id=" + id + ", customerProjectId=" + customerProjectId + ", projectName=" + projectName
				+ ", projectTypeId=" + projectTypeId + ", categoryId=" + categoryId + ", retailerCode=" + retailerCode
				+ ", storeCount=" + storeCount + ", startDate=" + startDate + ", createdDate=" + createdDate
				+ ", createdBy=" + createdBy + ", updatedDate=" + updatedDate + ", updatedBy=" + updatedBy + ", status="
				+ status + ", description=" + description + ", owner=" + owner + ", endDate=" + endDate
				+ ", isParentProject=" + isParentProject + ", parentProjectId=" + parentProjectId + ", customerCode="
				+ customerCode + ", blurThreshold=" + blurThreshold + ", projectUpcList=" + projectUpcList
				+ ", projectQuestionsList=" + projectQuestionsList + ", projectObjectives=" + projectObjectives
				+ ", projectStoreGradingCriteriaList=" + projectStoreGradingCriteriaList + ", waves=" + waves
				+ ", aggregationType=" + aggregationType + "]";
	}
}

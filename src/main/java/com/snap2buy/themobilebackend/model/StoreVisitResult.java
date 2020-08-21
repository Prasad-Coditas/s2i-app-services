package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 5/4/17.
 */
public class StoreVisitResult {
	String projectId;
	String storeId;
    String taskId;
    String resultCode;
    String status;
    String linearFootage;
    String percentageOsa;

    public StoreVisitResult(String storeId, String taskId, String resultCode, String status, String projectId, String linearFootage, String percentageOsa) {
		super();
		this.storeId = storeId;
		this.taskId = taskId;
		this.resultCode = resultCode;
		this.status = status;
		this.projectId = projectId;
		this.linearFootage = linearFootage;
		this.percentageOsa = percentageOsa;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLinearFootage() {
		return linearFootage;
	}

	public void setLinearFootage(String linearFootage) {
		this.linearFootage = linearFootage;
	}

	public String getPercentageOsa() {
		return percentageOsa;
	}

	public void setPercentageOsa(String percentageOsa) {
		this.percentageOsa = percentageOsa;
	}

	@Override
	public String toString() {
		return "StoreVisitResult [projectId=" + projectId + ", storeId=" + storeId + ", taskId=" + taskId
				+ ", resultCode=" + resultCode + ", status=" + status + ", linearFootage=" + linearFootage
				+ ", percentageOsa=" + percentageOsa + "]";
	}
}

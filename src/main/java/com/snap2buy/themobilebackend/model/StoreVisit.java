package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 4/12/17.
 */
public class StoreVisit {
    String storeId;
    String taskId;

    public StoreVisit(String storeId, String taskId) {
        this.storeId = storeId;
        this.taskId = taskId;
    }

    public StoreVisit() {
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

    @Override
    public String toString() {
        return "StoreVisit{" +
                "storeId='" + storeId + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ( obj != null && obj instanceof StoreVisit ) {
			StoreVisit otherVisit = (StoreVisit) obj;
			if ( otherVisit.storeId.equals(this.storeId) && otherVisit.taskId.equals(this.taskId) ) {
				isEqual = true;
			}
		}
		return isEqual;
	}
    
    
}

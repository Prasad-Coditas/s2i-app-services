package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 7/06/16.
 */
public class StoreImageInfo {
    String imageUUID;
    String dateId;
    String taskId;
    String agentId;

    public StoreImageInfo() {
        super();
    }
    
    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getDateId() {
        return dateId;
    }

    public void setDateId(String dateId) {
        this.dateId = dateId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "StoreImageInfo{" +
                "imageUUID='" + imageUUID + '\'' +
                ", dateId='" + dateId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", agentId='" + agentId + '\'' +
                '}';
    }
}

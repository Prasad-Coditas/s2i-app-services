package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 3/16/16.
 */
public class DistributionList {
    String listId;
    String listName;
    String owner;
    String createdTime;
    String lastModifiedTime;

    public DistributionList(String listId, String listName, String owner, String createdTime, String lastModifiedTime) {
        this.listId = listId;
        this.listName = listName;
        this.owner = owner;
        this.createdTime = createdTime;
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @Override
    public String toString() {
        return "DistributionList{" +
                "listName='" + listName + '\'' +
                ", listId='" + listId + '\'' +
                ", owner='" + owner + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", lastModifiedTime='" + lastModifiedTime + '\'' +
                '}';
    }
}

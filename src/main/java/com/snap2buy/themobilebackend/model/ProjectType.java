package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by sachin on 5/29/16.
 */
@XmlRootElement(name = "ProjectType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectType {
    @XmlElement
    String id;
    @XmlElement
    String name;
    @XmlElement
    String createdDate;
    @XmlElement
    String status;

    public ProjectType() {
        super();
    }

    public ProjectType(String id, String name, String createdDate, String status) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "ProjectType{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

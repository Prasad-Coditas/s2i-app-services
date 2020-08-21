package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Harshal
 */
@XmlRootElement(name = "ProjectRepQuestionDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectRepQuestionDetail {

    @XmlElement
    String projectId;

    @XmlElement
    String questionId;

    @XmlElement
    String responseId;

    @XmlElement
    String responseValue;

    @XmlElement
    String goToSequenceNumber;

    public ProjectRepQuestionDetail() {
        super();
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public String getGoToSequenceNumber() {
        return goToSequenceNumber;
    }

    public void setGoToSequenceNumber(String goToSequenceNumber) {
        this.goToSequenceNumber = goToSequenceNumber;
    }

    @Override
    public String toString() {
        return "ProjectRepQuestionDetail{" +
            "projectId='" + projectId + '\'' +
            ", questionId='" + questionId + '\'' +
            ", responseId='" + responseId + '\'' +
            ", responseValue='" + responseValue + '\'' +
            ", goToSequenceNumber='" + goToSequenceNumber + '\'' +
        '}';
    }
}

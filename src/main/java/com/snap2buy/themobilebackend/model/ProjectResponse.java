package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Harshal
 */
@XmlRootElement(name = "ProjectResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectResponse {

    @XmlElement
    String questionId;

    @XmlElement
    String response;

    public ProjectResponse() {
        super();
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "ProjectResponse{" +
                ", questionId='" + questionId + '\'' +
                ", response='" + response + '\'' +
        '}';
    }
}

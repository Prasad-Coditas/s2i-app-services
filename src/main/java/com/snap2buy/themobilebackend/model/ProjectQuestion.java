package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anoop.
 */
@XmlRootElement(name = "ProjectQuestion")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectQuestion {
	
    @XmlElement
    String id;
    @XmlElement
    String desc;
    @XmlElement
    String sequenceNumber;
    @XmlElement
    String projectId;

    @XmlElement
    String questionType;

    @XmlElement
    String mandatoryQuestion;

    @XmlElement
    String questionPhotoLink;

    @XmlElement
    String minimumValue;

    @XmlElement
    String maximumValue;

    @XmlElement
    String incrementInterval;
    
    @XmlElement
    String goToSequenceNumber;
    
    @XmlElement
    String groupName;
    
    @XmlElement
    String skipImageAnalysis;

    @XmlElement(name = "projectRepQuestionDetails")
    List<ProjectRepQuestionDetail> projectRepQuestionDetails = new ArrayList<>();

    public ProjectQuestion() {
        super();
    }

    public ProjectQuestion(String id, String desc, String sequenceNumber, String projectId) {
        this.id = id;
        this.desc = desc;
        this.sequenceNumber = sequenceNumber;
        this.projectId = projectId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getMandatoryQuestion() {
        return mandatoryQuestion;
    }

    public void setMandatoryQuestion(String mandatoryQuestion) {
        this.mandatoryQuestion = mandatoryQuestion;
    }

    public String getQuestionPhotoLink() {
        return questionPhotoLink;
    }

    public void setQuestionPhotoLink(String questionPhotoLink) {
        this.questionPhotoLink = questionPhotoLink;
    }

    public String getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(String minimumValue) {
        this.minimumValue = minimumValue;
    }

    public String getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(String maximumValue) {
        this.maximumValue = maximumValue;
    }

    public String getIncrementInterval() {
        return incrementInterval;
    }

    public void setIncrementInterval(String incrementInterval) {
        this.incrementInterval = incrementInterval;
    }

    public List<ProjectRepQuestionDetail> getProjectRepQuestionDetails() {
        return projectRepQuestionDetails;
    }

    public void setProjectRepQuestionDetails(List<ProjectRepQuestionDetail> projectRepQuestionDetails) {
        this.projectRepQuestionDetails = projectRepQuestionDetails;
    }
    
    public String getGoToSequenceNumber() {
		return goToSequenceNumber;
	}

	public void setGoToSequenceNumber(String goToSequenceNumber) {
		this.goToSequenceNumber = goToSequenceNumber;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getSkipImageAnalysis() {
		return skipImageAnalysis;
	}

	public void setSkipImageAnalysis(String skipImageAnalysis) {
		this.skipImageAnalysis = skipImageAnalysis;
	}

	@Override
	public String toString() {
		return "ProjectQuestion [id=" + id + ", desc=" + desc + ", sequenceNumber=" + sequenceNumber + ", projectId="
				+ projectId + ", questionType=" + questionType + ", mandatoryQuestion=" + mandatoryQuestion
				+ ", questionPhotoLink=" + questionPhotoLink + ", minimumValue=" + minimumValue + ", maximumValue="
				+ maximumValue + ", incrementInterval=" + incrementInterval + ", goToSequenceNumber="
				+ goToSequenceNumber + ", groupName=" + groupName + ", skipImageAnalysis=" + skipImageAnalysis
				+ ", projectRepQuestionDetails=" + projectRepQuestionDetails + "]";
	}
}

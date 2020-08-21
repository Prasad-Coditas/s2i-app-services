package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 7/06/16.
 */
public class StoreImageDetails {
    String imageUUID;
    String dateId;
    String taskId;
    String agentId;
    String imageStatus;
    String origWidth;
    String origHeight;
    String newWidth;
    String newHeight;
    String imageRotation;
    String questionId;
    String questionGroupName;
    String sequenceNumber;

    public StoreImageDetails() {
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

    
    public String getImageStatus() {
		return imageStatus;
	}

	public void setImageStatus(String imageStatus) {
		this.imageStatus = imageStatus;
	}

	public String getOrigWidth() {
		return origWidth;
	}

	public void setOrigWidth(String origWidth) {
		this.origWidth = origWidth;
	}

	public String getOrigHeight() {
		return origHeight;
	}

	public void setOrigHeight(String origHeight) {
		this.origHeight = origHeight;
	}

	public String getNewWidth() {
		return newWidth;
	}

	public void setNewWidth(String newWidth) {
		this.newWidth = newWidth;
	}

	public String getNewHeight() {
		return newHeight;
	}

	public void setNewHeight(String newHeight) {
		this.newHeight = newHeight;
	}

	public String getImageRotation() {
		return imageRotation;
	}

	public void setImageRotation(String imageRotation) {
		this.imageRotation = imageRotation;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getQuestionGroupName() {
		return questionGroupName;
	}

	public void setQuestionGroupName(String questionGroupName) {
		this.questionGroupName = questionGroupName;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public String toString() {
		return "StoreImageDetails [imageUUID=" + imageUUID + ", dateId=" + dateId + ", taskId=" + taskId + ", agentId="
				+ agentId + ", imageStatus=" + imageStatus + ", origWidth=" + origWidth + ", origHeight=" + origHeight
				+ ", newWidth=" + newWidth + ", newHeight=" + newHeight + ", imageRotation=" + imageRotation
				+ ", questionId=" + questionId + ", questionGroupName=" + questionGroupName + ", sequenceNumber="
				+ sequenceNumber + "]";
	}

}

package com.snap2buy.themobilebackend.model;

/**
 * Created by Anoop on 5/11/17.
 */
public class PrmResult {
            Integer ServiceOrderId;
            Integer AssessmentId;
            Integer PhotoReviewStatusId;
            String PhotoReviewStatus;
            String PhotoReviewComment;
			String ProcessDate;
            Integer FileId;

    public Integer getServiceOrderId() {
        return ServiceOrderId;
    }

    public void setServiceOrderId(Integer serviceOrderId) {
        ServiceOrderId = serviceOrderId;
    }

    public Integer getAssessmentId() {
        return AssessmentId;
    }

    public void setAssessmentId(Integer assessmentId) {
        AssessmentId = assessmentId;
    }

    public Integer getFileId() {
        return FileId;
    }

    public void setFileId(Integer fileId) {
        FileId = fileId;
    }
    
    public Integer getPhotoReviewStatusId() {
		return PhotoReviewStatusId;
	}

	public void setPhotoReviewStatusId(Integer photoReviewStatusId) {
		PhotoReviewStatusId = photoReviewStatusId;
	}

	public String getPhotoReviewStatus() {
		return PhotoReviewStatus;
	}

	public void setPhotoReviewStatus(String photoReviewStatus) {
		PhotoReviewStatus = photoReviewStatus;
	}
	
	public String getPhotoReviewComment() {
		return PhotoReviewComment;
	}

	public void setPhotoReviewComment(String photoReviewComment) {
		PhotoReviewComment = photoReviewComment;
	}

	public String getProcessDate() {
		return ProcessDate;
	}

	public void setProcessDate(String processDate) {
		ProcessDate = processDate;
	}

	@Override
    public String toString() {
        return "PrmResult{" +
                "ServiceOrderId='" + ServiceOrderId + '\'' +
                ", AssessmentId='" + AssessmentId + '\'' +
                ", PhotoReviewStatusId='" + PhotoReviewStatusId + '\'' +
                ", PhotoReviewStatus='" + PhotoReviewStatus + '\'' +
                ", PhotoReviewComment='" + PhotoReviewComment + '\'' +
                ", ProcessDate='" + ProcessDate + '\'' +
                ", FileId='" + FileId + '\'' +
                '}';
    }
}

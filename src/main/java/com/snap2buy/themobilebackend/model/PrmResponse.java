package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 4/7/17.
 */
public class PrmResponse {
            String ServiceOrderId;
            String QuestionId;
            String AssessmentId;
            String UserResponse;
            String VisitDate;
            String DateReported;
            String QuestionText;
            String AssessmentName;
            String JobId;
            String PhotoLink;
            String RepId;
            String RepName;
            String StoreId;
            String StoreAddress;
            String StoreCity;
            String StoreState;
            String StoreZip;
            String StoreNumber;
            String FileId;

    public String getServiceOrderId() {
        return ServiceOrderId;
    }

    public void setServiceOrderId(String serviceOrderId) {
        ServiceOrderId = serviceOrderId;
    }

    public String getQuestionId() {
        return QuestionId;
    }

    public void setQuestionId(String questionId) {
        QuestionId = questionId;
    }

    public String getAssessmentId() {
        return AssessmentId;
    }

    public void setAssessmentId(String assessmentId) {
        AssessmentId = assessmentId;
    }

    public String getUserResponse() {
        return UserResponse;
    }

    public void setUserResponse(String userResponse) {
        UserResponse = userResponse;
    }

    public String getVisitDate() {
        return VisitDate;
    }

    public void setVisitDate(String visitDate) {
        VisitDate = visitDate;
    }

    public String getQuestionText() {
        return QuestionText;
    }

    public void setQuestionText(String questionText) {
        QuestionText = questionText;
    }

    public String getAssessmentName() {
        return AssessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        AssessmentName = assessmentName;
    }

    public String getJobId() {
        return JobId;
    }

    public void setJobId(String jobId) {
        JobId = jobId;
    }

    public String getPhotoLink() {
        return PhotoLink;
    }

    public void setPhotoLink(String photoLink) {
        PhotoLink = photoLink;
    }

    public String getRepId() {
        return RepId;
    }

    public void setRepId(String repId) {
        RepId = repId;
    }

    public String getRepName() {
        return RepName;
    }

    public void setRepName(String repName) {
        RepName = repName;
    }

    public String getStoreId() {
        return StoreId;
    }

    public void setStoreId(String storeId) {
        StoreId = storeId;
    }

    public String getStoreAddress() {
        return StoreAddress;
    }

    public void setStoreAddress(String storeAddress) {
        StoreAddress = storeAddress;
    }

    public String getStoreCity() {
        return StoreCity;
    }

    public void setStoreCity(String storeCity) {
        StoreCity = storeCity;
    }

    public String getStoreState() {
        return StoreState;
    }

    public void setStoreState(String storeState) {
        StoreState = storeState;
    }

    public String getStoreZip() {
        return StoreZip;
    }

    public void setStoreZip(String storeZip) {
        StoreZip = storeZip;
    }

    public String getStoreNumber() {
        return StoreNumber;
    }

    public void setStoreNumber(String storeNumber) {
        StoreNumber = storeNumber;
    }

    public String getFileId() {
        return FileId;
    }

    public void setFileId(String fileId) {
        FileId = fileId;
    }

    public String getDateReported() {
		return DateReported;
	}

	public void setDateReported(String dateReported) {
		DateReported = dateReported;
	}

	@Override
	public String toString() {
		return "PrmResponse [ServiceOrderId=" + ServiceOrderId + ", QuestionId=" + QuestionId + ", AssessmentId="
				+ AssessmentId + ", UserResponse=" + UserResponse + ", VisitDate=" + VisitDate + ", DateReported="
				+ DateReported + ", QuestionText=" + QuestionText + ", AssessmentName=" + AssessmentName + ", JobId="
				+ JobId + ", PhotoLink=" + PhotoLink + ", RepId=" + RepId + ", RepName=" + RepName + ", StoreId="
				+ StoreId + ", StoreAddress=" + StoreAddress + ", StoreCity=" + StoreCity + ", StoreState=" + StoreState
				+ ", StoreZip=" + StoreZip + ", StoreNumber=" + StoreNumber + ", FileId=" + FileId + "]";
	}
}

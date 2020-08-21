package com.snap2buy.themobilebackend.model;

import org.apache.commons.lang3.StringUtils;

public enum ImageResultCode {
	
	UNAPPROVED("0","Unapprove","5"),
	REJECT_INSUFFICIENT("1", "Reject! - Insufficient","10"),
	REJECT_LEVEL_1("2","Reject! Level 1 Execution Error","6"),
	REJECT_LEVEL_2("3","Reject! Level 2 Execution Error","8"),
	REJECT_LEVEL_3("4","Reject! Level 3 Execution Error","9"),
	REJECT_DUPLICATE("5","Reject! - Duplicate","12"),
	APPROVED_PENDING_REVIEW("6","Approve pending review","101"),
	APPROVED_WITH_ISSUES("7","Approved w/ Issues","14"),
	APPROVE("8","Approve","1"),
	APPROVED_EXCELLENT("9","Approved - Excellent","3"),
	UNKNOWN("100","Unknown","100");
	
	private final String code;
	private final String desc;
	private final String statusCode;
	
	ImageResultCode(String code, String desc, String statusCode){
		this.code=code;
		this.desc=desc;
		this.statusCode=statusCode;
	}
	
	public String getCode(){
		return this.code;
	}

	public String getDesc() {
		return this.desc;
	}
	
	public String getStatusCode() {
		return this.statusCode;
	}
	
	public static ImageResultCode getImageResultCodeFromCode(String code){
		if ( StringUtils.isBlank(code) ) {
			return UNKNOWN;
		}
        for(ImageResultCode e : ImageResultCode.values()){
            if(code.equals(e.code)) return e;
        }
        return UNKNOWN;
    }
}

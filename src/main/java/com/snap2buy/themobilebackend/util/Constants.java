package com.snap2buy.themobilebackend.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Anoop on 09/27/18.
 */
public interface Constants {
	
	//User status constants
	public static final String USER_STATUS_ACTIVE = "1";
	public static final String USER_STATUS_INACTIVE = "-1";
	public static final String USER_STATUS_PENDING_VERIFICATION = "0";
	public static final String EXTERNAL_PROCESSING_IMAGE_STATUS = "external-cron";
	public static final String EXTERNAL_PROCESSING_IN_PROGRESS_IMAGE_STATUS = "external-processing";
	public static final String PASSTHROUGH_CATEGORY_ID = "65";
	public static final String PASSTHROUGH_IMAGE_ANALYSIS_RESPONSE = "{\"status\": \"success\",\"data\": {\"imageHashScore\":\"0\",\"imageNotUsable\":\"0\",\"imageNotUsableComment\":\"\",\"imageResultCode\":\"\",\"imageResultComments\":\"\",\"imageReviewRecommendations\":[],\"imageRotation\":0,\"skus\":[{\"Alt_UPC\":\"\",\"Alt_UPC_Confidence\":1,\"Height\":10,\"LEFT_TOP_X\":0,\"LEFT_TOP_Y\":0,\"Price\":\"\",\"Price_Confidence\":1,\"Price_Label\":\"N\",\"Promotion\":\"1\",\"UPC\":\"999999999999\",\"UPC_Confidence\":1,\"Width\":10}],\"uuid\":\"\"}, \"message\": null}";
	public static final List<Integer> ITG_CIGARETTES_PROJECT_IDS = Arrays.asList(new Integer[] {1643,1630});
	public static final String ITG_CUSTOMER_CODE = "ITGBND";
	public static final String S2I_SUPPORT_EMAIL = "support@snap2insight.com";
	public static final String S2I_SUPPORT_PHONE_NUMBER = "+1 (510) 876-4952";
	public static final String PERSON_FACE_UPC = "999999971801";
	
}

package com.snap2buy.themobilebackend.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StandardTemplates {
	
	public static List<Map<String, Object>> getShelfLevelTemplate() {

		List<Map<String, Object>> facings = new ArrayList<Map<String, Object>>();

		Map<String, Object> resultMapForTop = new LinkedHashMap<String, Object>();
		resultMapForTop.put("levelName", "Top");
		resultMapForTop.put("facingCount", "0");

		Map<String, Object> resultMapForMiddle = new LinkedHashMap<String, Object>();
		resultMapForMiddle.put("levelName", "Middle");
		resultMapForMiddle.put("facingCount", "0");

		Map<String, Object> resultMapForBottom = new LinkedHashMap<String, Object>();
		resultMapForBottom.put("levelName", "Bottom");
		resultMapForBottom.put("facingCount", "0");

		Map<String, Object> resultMapForNA = new LinkedHashMap<String, Object>();
		resultMapForNA.put("levelName", "NA");
		resultMapForNA.put("facingCount", "0");

		facings.add(resultMapForTop);
		facings.add(resultMapForMiddle);
		facings.add(resultMapForBottom);
		facings.add(resultMapForNA);

		return facings;

	}

}

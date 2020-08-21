package com.snap2buy.themobilebackend.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Anoop
 *
 */
public class ExpressionEvaluator {

	private static Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);
	
	private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.##");
	
	private static final List<String> PERCENTAGE_CRITERIA = Arrays.asList(new String[] {"percentageOfSKUsPromoted", "percentageOfSKUsWithPriceTag"});

	public static boolean evaluate(String criteria, List<String> aggUPCs,
			Map<String, Object> projectStoreData, Map<String,String> repResponses, String distinctUpcCount, 
			String percentageOsa, String distributionPercentage, String hasLowQualityImages, String hasLowConfidenceDetections) {
		LOGGER.info("---------------ExpressionEvaluator--evaluate:: evaluating criteria :: {}", criteria );
		boolean evaluateResult = false;
		List<String> criteriaParts = Arrays.asList(criteria.split(" "));
		StringBuilder criteriaBuilder = new StringBuilder();
		int currentPartIndex =0;
		for(String part : criteriaParts ) {
			if ( part.startsWith("containsAll") ) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				if ( aggUPCs.containsAll(upcList)){
					criteriaBuilder.append("true");
				} else {
					criteriaBuilder.append("false");
				}
			} else if ( part.startsWith("containsAny")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				if ( ifAnyPresentInList(upcList, aggUPCs)){
					criteriaBuilder.append("true");
				} else {
					criteriaBuilder.append("false");
				}
			} else if ( part.startsWith("containsNone")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				if ( !ifAnyPresentInList(upcList, aggUPCs)){
					criteriaBuilder.append("true");
				} else {
					criteriaBuilder.append("false");
				}
			} else if ( part.startsWith("sumFacings")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				Map<String, Map<String, String>> upcLevelData = (Map<String,Map<String,String>>) projectStoreData.get("UPCs");
				int sumFacings = 0;
				for(String upc : upcList) {
					String facing = null;
					if ( upcLevelData.get(upc) != null ) {
						facing = upcLevelData.get(upc).get("facing");
					}
					if (facing != null && !facing.isEmpty() ) {
						sumFacings = sumFacings + Integer.parseInt(facing);
					}
				}
				criteriaBuilder.append(sumFacings);
			} else if ( part.startsWith("responseContains")) { //responseContains("no display",questionId(215456)) 
				int questionIdsBeginIndex = part.indexOf(",questionId(");
				int questionIdsEndIndex = part.length()-2;
				String questionIdPart = part.substring(questionIdsBeginIndex+",questionId(".length(),questionIdsEndIndex);
				List<String> questionIds =  Arrays.asList(questionIdPart.split(","));
				int strToCompareStartIndex = part.indexOf("\"") + 1;
				int strToCompareEndIndex = questionIdsBeginIndex - 1;
				String strToCompare = part.substring(strToCompareStartIndex, strToCompareEndIndex);
				boolean foundMatch = false;
				for( String questionId : questionIds) {
					String response = repResponses.get(questionId);
					if ( response == null ) response = "null" ;
					response = response.replaceAll("\\s+","").toLowerCase().trim();
					strToCompare = strToCompare.replaceAll("\\s+","").toLowerCase().trim();
					if ( response.contains(strToCompare) ) {
						foundMatch = true;
						break;
					}
				}
				criteriaBuilder.append(foundMatch);
			} else if ( part.startsWith("response")) { //response(1919880) == \"NO\"
				String questionIdPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				String response = repResponses.get(questionIdPart);
				if ( response == null ) response = "null" ;
				//If value to compare is a string, wrap the rep response value in double quotes. Else, leave it as it is.
				if ( criteriaParts.get(currentPartIndex + 2).startsWith("\"") ){
					response="\""+response+"\"";
				}
				criteriaBuilder.append(response.toLowerCase());
			} else if ( part.startsWith("countOfSKUsPresent")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				int skusPresentCount = 0;
				for(String upc : upcList) {
					if ( aggUPCs.contains(upc) ) {
						skusPresentCount = skusPresentCount + 1;
					}
				}
				criteriaBuilder.append(skusPresentCount);
			} else if ( part.startsWith("countOfSKUsPromoted")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				int skusPromotedCount = 0;
				Map<String, Map<String, String>> upcLevelData = (Map<String,Map<String,String>>) projectStoreData.get("UPCs");
				for(String upc : upcList) {
					String promotion = null;
					if ( upcLevelData.get(upc) != null ) {
						promotion = upcLevelData.get(upc).get("promotion");
					}
					if (StringUtils.isNotBlank(promotion) && promotion.equals("1") ) {
						skusPromotedCount = skusPromotedCount + 1;
					}
				}
				criteriaBuilder.append(skusPromotedCount);
			} else if ( part.startsWith("countOfSKUsWithPriceTag")) {
				String upcPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> upcList = Arrays.asList(upcPart.split(","));
				int skusWithPriceTagCount = 0;
				Map<String, Map<String, String>> upcLevelData = (Map<String,Map<String,String>>) projectStoreData.get("UPCs");
				for(String upc : upcList) {
					String price = null;
					if ( upcLevelData.get(upc) != null ) {
						price = upcLevelData.get(upc).get("price");
					}
					if (StringUtils.isNotBlank(price)) {
						skusWithPriceTagCount = skusWithPriceTagCount + 1;
					}
				}
				criteriaBuilder.append(skusWithPriceTagCount);
			} else if ( part.startsWith("countOf")) {
				int count = evaluateCountOf(repResponses, part);
				criteriaBuilder.append(count);
			} else if ( part.startsWith("isResponseNotEmpty")) {
				String[] splitParts = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")")).split(",");
				String response = repResponses.get(splitParts[0]);
				int responseLength = 0;
				if ( StringUtils.isNotBlank(response) ) { responseLength = response.length(); }
				int referenceLength = 0;
				if ( splitParts.length > 1 ) { referenceLength = Integer.parseInt(splitParts[1]); }
				if ( responseLength > referenceLength ) {
					criteriaBuilder.append("true");
				} else {
					criteriaBuilder.append("false");
				}
			} else if ( part.startsWith("shareOfShelfByBrand")) {
				String brandsPart = part.substring(part.indexOf("(") + 1, part.lastIndexOf(")"));
				List<String> brandsList = Arrays.asList(brandsPart.split(","));
				Map<String,Integer> brandLevelData = (Map<String,Integer>) projectStoreData.get("brands");
				int sumFacingsByBrands = 0;
				for(String brand : brandsList) {
					Integer facing = null;
					if ( brandLevelData.get(brand.toUpperCase()) != null ) {
						facing = brandLevelData.get(brand.toUpperCase());
					}
					if (facing != null) {
						sumFacingsByBrands = sumFacingsByBrands + facing;
					}
				}
				
				String shareOfShelf = "0";
				Integer totalFacings = (Integer) projectStoreData.get("facing");
				if ( totalFacings != 0 ) {
					shareOfShelf = DECIMAL_FORMATTER.format( ((float)sumFacingsByBrands/totalFacings) );
				}
				criteriaBuilder.append(shareOfShelf);
			} else if ( part.startsWith("distinctUpcCount")){
				criteriaBuilder.append(distinctUpcCount);
			} else if ( part.startsWith("distributionPercentage")){
				criteriaBuilder.append(distributionPercentage);
			} else if ( part.startsWith("hasLowQualityImages")){
				criteriaBuilder.append(hasLowQualityImages);
			} else if ( part.startsWith("hasLowConfidenceDetections")){
				criteriaBuilder.append(hasLowConfidenceDetections);
			} else if ( part.startsWith("osaPercentage")){
				criteriaBuilder.append(percentageOsa);
			} else if ( part.equals("AND")){
				criteriaBuilder.append("&&");
			} else if ( part.equals("OR")) {
				criteriaBuilder.append("||");
			} else {
				criteriaBuilder.append(part.toLowerCase());
			}
			criteriaBuilder.append(" ");
			currentPartIndex++;
		}
		LOGGER.info("---------------ExpressionEvaluator--evaluate:: evaluating processed criteria :: {}", criteriaBuilder.toString());
		try {
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine se = sem.getEngineByName("JavaScript");
            evaluateResult = (Boolean)se.eval(criteriaBuilder.toString());
        } catch (Exception e) {
           evaluateResult = false;
           LOGGER.error("---------------ExpressionEvaluator--evaluate:: evaluating processed criteria failed :: {}", e.getMessage());
        }
		LOGGER.info("---------------ExpressionEvaluator--evaluate:: evaluation result :: {}", evaluateResult);
		return evaluateResult;
	
	}
	
	private static boolean ifAnyPresentInList(List<String> skuUPCsInProject,List<String> aggUPCs) {
		boolean isPresent = false;
		for( String upc : skuUPCsInProject ) {
			if ( aggUPCs.contains(upc) ) {
				isPresent = true;
				break;
			}
		}
		return isPresent;
	}

	private static int evaluateCountOf(Map<String, String> repResponses,String countOfExpStr) {
		String responseConditionPart = countOfExpStr.substring(countOfExpStr.indexOf("(") + 2, countOfExpStr.lastIndexOf("\")")+1);
		String[] responseConditionPartSplits = responseConditionPart.split("\\|\\|");
		List<String> equalsResponses = new ArrayList<String>();
		List<String> containsResponses = new ArrayList<String>();
		List<String> notEqualsResponses = new ArrayList<String>();
		List<String> notContainsResponses = new ArrayList<String>();

		for ( String responseCondition : responseConditionPartSplits ) {
			if ( responseCondition.startsWith("response==") ) {
				String respConditionValue = responseCondition.split("==")[1];
				respConditionValue = respConditionValue.substring(1,respConditionValue.length()-1);
				equalsResponses.add(respConditionValue);
			} else if (responseCondition.startsWith("response!=")) {
				String respConditionValue = responseCondition.split("!=")[1];
				respConditionValue = respConditionValue.substring(1,respConditionValue.length()-1);
				notEqualsResponses.add(respConditionValue);
			} else if ( responseCondition.startsWith("responseContains==") ) {
				String respConditionValue = responseCondition.split("==")[1];
				respConditionValue = respConditionValue.substring(1,respConditionValue.length()-1);
				containsResponses.add(respConditionValue);
			} else if ( responseCondition.startsWith("responseNotContains==") ) {
				String respConditionValue = responseCondition.split("==")[1];
				respConditionValue = respConditionValue.substring(1,respConditionValue.length()-1);
				notContainsResponses.add(respConditionValue);
			}
		}
		int questionIdsBeginIndex = countOfExpStr.indexOf(",questionId(");
		int questionIdsEndIndex = countOfExpStr.length()-2;
		String questionIdPart = countOfExpStr.substring(questionIdsBeginIndex+",questionId(".length(),questionIdsEndIndex);
		List<String> questionIds =  Arrays.asList(questionIdPart.split(","));
		int count = 0;
		for(String response : equalsResponses ) {
			for(String questionId : questionIds ){
				String repResponse = repResponses.get(questionId);
				if ( repResponse == null ) repResponse = "null"; 
				String repResponseWithoutSpaces = repResponse.replaceAll("\\s+","");
				if ( repResponseWithoutSpaces.equalsIgnoreCase(response) ){
					count++;
				}
			}
		}
		
		for(String response : notEqualsResponses ) {
			for(String questionId : questionIds ){
				String repResponse = repResponses.get(questionId);
				if ( repResponse == null ) repResponse = "null"; 
				String repResponseWithoutSpaces = repResponse.replaceAll("\\s+","");
				if ( !repResponseWithoutSpaces.equalsIgnoreCase(response) ){
					count++;
				}
			}
		}
		
		for(String response : containsResponses ) {
			for(String questionId : questionIds ){
				String repResponse = repResponses.get(questionId);
				if ( repResponse == null ) repResponse = "null"; 
				String repResponseWithoutSpaces = repResponse.replaceAll("\\s+","").toLowerCase().trim();
				if ( repResponseWithoutSpaces.contains(response.toLowerCase().trim()) ){
					count++;
				}
			}
		}
		
		for(String response : notContainsResponses ) {
			for(String questionId : questionIds ){
				String repResponse = repResponses.get(questionId);
				if ( repResponse == null ) repResponse = "null"; 
				String repResponseWithoutSpaces = repResponse.replaceAll("\\s+","").toLowerCase().trim();
				if ( !repResponseWithoutSpaces.contains(response.toLowerCase().trim()) ){
					count++;
				}
			}
		}
		
		return count;
	}
	
	public static double evaluatePercentageCriteria(Map<String, Object> projectStoreData, String criteria) {
		
		LOGGER.info("---------------ExpressionEvaluator--evaluatePercentageCriteria:: evaluating criteria :: {}", criteria );

		String upcPart = criteria.substring(criteria.indexOf("(") + 1, criteria.lastIndexOf(")"));
		List<String> upcList = Arrays.asList(upcPart.split(","));
		Map<String, Map<String, String>> upcLevelData = (Map<String,Map<String,String>>) projectStoreData.get("UPCs");
		
		double percentage = 0.0;
		
		if ( criteria.startsWith("percentageOfSKUsPromoted") ) {
			int skusPromotedCount = 0;
			int skusFound = 0;
			for(String upc : upcList) {
				String promotion = null;
				if ( upcLevelData.get(upc) != null ) {
					promotion = upcLevelData.get(upc).get("promotion");
					skusFound = skusFound + 1;
				}
				if (StringUtils.isNotBlank(promotion) && promotion.equals("1") ) {
					skusPromotedCount = skusPromotedCount + 1;
				}
			}
			
			if ( skusFound > 0 ) {
				percentage = (double)skusPromotedCount/skusFound;
			}
		} else if ( criteria.startsWith("percentageOfSKUsWithPriceTag") ) {
			int skusWithPriceTag = 0;
			int skusFound = 0;
			for(String upc : upcList) {
				String priceTag = null;
				if ( upcLevelData.get(upc) != null ) {
					priceTag = upcLevelData.get(upc).get("price");
					skusFound = skusFound + 1;
				}
				if (StringUtils.isNotBlank(priceTag) && !(priceTag.trim().equals("0")) ) {
					skusWithPriceTag = skusWithPriceTag + 1;
				}
			}
			
			if ( skusFound > 0 ) {
				percentage = (double)skusWithPriceTag/skusFound;
			}
		}
		
		LOGGER.info("---------------ExpressionEvaluator--evaluatePercentageCriteria:: evaluation result :: {}", percentage);
		
		return percentage;
	}

	public static boolean isPercentageCriteria(String criteria) {
		boolean isPercentageCriteria = false;
		for ( String percentageCriteriaConstruct : PERCENTAGE_CRITERIA ) {
			if ( criteria.startsWith(percentageCriteriaConstruct) ) {
				isPercentageCriteria = true;
				break;
			}
		}
		return isPercentageCriteria;
	}

}

package com.snap2buy.themobilebackend.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snap2buy.themobilebackend.dao.ITGAggregationDao;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.service.ITGAggregationService;
import com.snap2buy.themobilebackend.util.Constants;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by ANOOP.
 */
@Component(value = BeanMapper.BEAN_ITG_AGGREGATION_SERVICE)
@Scope("prototype")
public class ITGAggregationServiceImpl implements ITGAggregationService {

	private static Logger LOGGER = LoggerFactory.getLogger(ITGAggregationServiceImpl.class);
	
	private static final List<String> ITG_BRANDS = Arrays.asList("Winston","Kool","Maverick","USA Gold","Salem","Montclair","Sonoma","Crowns", "Fortuna", "Rave" );

	private static final List<String> DISCOUNT_BRANDS = Arrays.asList("Maverick","Sonoma","Crowns","Montclair","Pall Mall","Eagle","Edgefield","LD","THIS","Decade","Chesterfield");

	private static final List<String> PREMIUM_BRANDS = Arrays.asList("Salem","Natural American Spirit", "Parliament","Newport Menthol","Virginia Slims", "Benson & Hedges", "Capri");

	private static final double VALID_LOWEST_PRICE = 4.0;
	private static final double VALID_HIGHEST_PRICE = 20.0;
	private static final int EXPECTED_MINIMUM_FACING_COUNT = 50;
	private static final int ITG_MIN_ALLOWED_DEVIATION_IN_FACING_COUNT = 2;
	private static final int ITG_MAX_ALLOWED_DEVIATION_IN_FACING_COUNT = 15;
	private static final int ITG_MAX_ALLOWED_DEVIATION_IN_WINSTON_FACING_COUNT = 19;
	private static final int ITG_MAX_ALLOWED_DEVIATION_IN_MAVERICK_FACING_COUNT = 15;
	private static final int ITG_MAX_ALLOWED_DEVIATION_IN_KOOL_FACING_COUNT = 9;
	private static final double ITG_SOS_LOWER_BOUND_PERCENTAGE = 4.0;
	private static final double ITG_SOS_UPPER_BOUND_PERCENTAGE = 40.0;
	
	private static final List<LinkedHashMap<String,Object>> REVIEW_COMMENT_ID_TO_TEXT_MAP = new ArrayList<LinkedHashMap<String,Object>>();
	private static final Map<String,String> REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP = new LinkedHashMap<String,String>();
	static {
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("1","Check ITG facings and if photo may be cut off.");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("2","Check ITG facings and if photo may be cut off.");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("3","Check all ITG brand facings. Make sure we are not missing any");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("4","Check ITG Brands with more than 15 facings");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("5","Check Winston Facings");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("6","Check Maverick Facings");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("7","Check all prices below $4");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("8","Check the YELLOW price boxes and update the brand price, if required.");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("9","Check Winston/Maverick/Kool where price is empty but has facings");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("10","Check all ITG Brands prices");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("11","Confirm if this store has any price tag below $5");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("12","Confirm if this store has any price tag above $15");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("13","If there is vertical overlap, reject with Photo Quality issue. If not approve the store");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("14","Walgreens Store Auto Rejection Rule");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("15","ITG SoS Based Auto Rejection Rule");
		REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.put("16","Check prices for highlighted brands");
		
		for( String id : REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.keySet() ) {
			LinkedHashMap<String,Object> oneComment = new LinkedHashMap<String,Object>();
			oneComment.put("id", id);
			oneComment.put("comment",REVIEW_COMMENT_ID_TO_TEXT_FLAT_MAP.get(id));
			REVIEW_COMMENT_ID_TO_TEXT_MAP.add(oneComment);
		}
	}
	
	private static final Map<String,String> RETAILER_SPECIFIC_THRESHOLDS = new LinkedHashMap<String,String>();
	static {
		RETAILER_SPECIFIC_THRESHOLDS.put("62560 WALGREENS","30");
	}
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_ITG_AGGREGATION_DAO)
	private ITGAggregationDao itgAggregationDao;
	
	@Autowired
	@Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
	private MetaServiceDao metaServiceDao;

	@Override
	public void runDailyAggregation(int projectId, String visitDateId) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts runDailyAggregation for projectId {} , visitDateId {}----------------\n", projectId, visitDateId);
		
		itgAggregationDao.runDailyAggregation(Constants.ITG_CUSTOMER_CODE, projectId, visitDateId);
		
		LOGGER.info("---------------ITGAggregationServiceImpl Ends runDailyAggregation for projectId {} , visitDateId {}----------------\n", projectId, visitDateId);
	}
	
	@Override
	public void runStoreVisitAggregation(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts runStoreVisitAggregation for projectId={}, storeId={}, taskId={}\n", 
				projectId,storeId,taskId);
		
		List<LinkedHashMap<String, String>> storeDetails = metaServiceDao.getStoreDetail(storeId);
		String retailer = storeDetails.get(0).get("retailer").trim();
		
		Map<String,Object> metaInfo = itgAggregationDao.getStoreMetaInfo(storeId);

		Map<String, Map<String, Object>> aggData = itgAggregationDao.runStoreVisitAggregation(projectId,storeId,taskId);
		
		int invalidHighPriceTagCount = itgAggregationDao.getInvalidHighPriceInRawDataCount(projectId, storeId, taskId);
		
		Map<String,String> priceConfidenceMap = itgAggregationDao.getPriceConfidenceData(projectId, storeId, taskId);
		
		boolean stitchingFailed = itgAggregationDao.hasStitchingFailed(projectId, storeId, taskId);
		
		String visitDateId = "";
		int bucketId = 0;
		int itgFacingCount = 0;
		int totalFacingCount = 0;
		int marlboroFacingCount = 0;
		int winstonFacingCount = 0;
		int koolFacingCount = 0;
		int maverickFacingCount = 0;
		boolean topITGBrandWithoutPrice = false;
		boolean wrongLowPriceDetections = false;
		boolean wrongHighPriceDetections = false;
		boolean itgbPriceTagFound = false;
		Double winstonPrice = null;
		Double koolPrice = null;
		Double maverickPrice = null;
		Double salemPrice = null;
		Double usagPrice = null;
		
		List<String> itgBrandsWithHighFacingCount = new ArrayList<String>();
		List<String> discountBrandsWithLowPrice = new ArrayList<String>();
		Set<String> brandsFlaggedBasedOnPriceHeuristics = new HashSet<String>();
		TreeMap<Double,Set<String>> priceToBrandMap = new TreeMap<Double, Set<String>>();
		
		boolean confidenceViolation = false;
		
		for(String brandName : aggData.keySet() ) {
			Map<String,Object> brandData = aggData.get(brandName);
			
			visitDateId = (String) brandData.get("visitDateId");
			
			int facingCount = (Integer) brandData.get("facingCount");
			
			int oosFacingCount = (Integer) brandData.get("oosFacingCount");
			
			Double regularPrice = (Double) brandData.get("regularPrice");
			if ( regularPrice == 0 ) { 
				regularPrice = null; 
			} else {
				if ( priceToBrandMap.get(regularPrice) == null ) {
					priceToBrandMap.put(regularPrice, new HashSet<String>());
				}
				priceToBrandMap.get(regularPrice).add(brandName);
				
				String confidences = priceConfidenceMap.get(brandName.trim()+"#Cigarette Regular Price#"+regularPrice);
				if ( confidences != null && !confidences.isEmpty() ) {
					String[] confidenceItems = confidences.split(",");
					if ( confidenceItems.length == 1 && confidenceItems[0].equals("0.0") ) {
						confidenceViolation = true;
					}
				}
			}
			
			Double posPrice = (Double) brandData.get("posPrice");
			if ( posPrice == 0 ) { 
				posPrice = null; 
			} else {
				if ( priceToBrandMap.get(posPrice) == null ) {
					priceToBrandMap.put(posPrice, new HashSet<String>());
				}
				priceToBrandMap.get(posPrice).add(brandName);
				
				String confidences = priceConfidenceMap.get(brandName.trim()+"#Cigarette Price#"+posPrice);
				if ( confidences != null && !confidences.isEmpty() ) {
					String[] confidenceItems = confidences.split(",");
					if ( confidenceItems.length == 1 && confidenceItems[0].equals("0.0") ) {
						confidenceViolation = true;
					}
				}
			}
			
			Double multipackPrice = (Double) brandData.get("multipackPrice");
			if ( multipackPrice == 0 ) { 
				multipackPrice = null; 
			} else {
				if ( priceToBrandMap.get(multipackPrice) == null ) {
					priceToBrandMap.put(multipackPrice, new HashSet<String>());
				}
				priceToBrandMap.get(multipackPrice).add(brandName);
				
				String confidences = priceConfidenceMap.get(brandName.trim()+"#Cigarette Multipack Price#"+multipackPrice);
				if ( confidences != null && !confidences.isEmpty() ) {
					String[] confidenceItems = confidences.split(",");
					if ( confidenceItems.length == 1 && confidenceItems[0].equals("0.0") ) {
						confidenceViolation = true;
					}
				}
			}
			
			if (regularPrice != null && regularPrice < VALID_LOWEST_PRICE) {
				if ( DISCOUNT_BRANDS.contains(brandName) ) {
					discountBrandsWithLowPrice.add(brandName);
				} else {
					wrongLowPriceDetections = true;
				}
			} else if (posPrice != null && posPrice < VALID_LOWEST_PRICE) {
				if ( DISCOUNT_BRANDS.contains(brandName) ) {
					discountBrandsWithLowPrice.add(brandName);
				} else {
					wrongLowPriceDetections = true;
				}
			} else if (multipackPrice != null && multipackPrice < VALID_LOWEST_PRICE) {
				if ( DISCOUNT_BRANDS.contains(brandName) ) {
					discountBrandsWithLowPrice.add(brandName);
				} else {
					wrongLowPriceDetections = true;
				}
			}
			
			if (regularPrice != null && regularPrice > VALID_HIGHEST_PRICE ) {
				wrongHighPriceDetections = true;
			} else if (posPrice != null && posPrice > VALID_HIGHEST_PRICE ) {
				wrongHighPriceDetections = true;
			} else if (multipackPrice != null && multipackPrice > VALID_HIGHEST_PRICE ) {
				wrongHighPriceDetections = true;
			}
			
			int brandFacingCount = facingCount + oosFacingCount;
			
			totalFacingCount = totalFacingCount + brandFacingCount;
			
			if(ITG_BRANDS.contains(brandName)) {
				
				itgFacingCount = itgFacingCount + brandFacingCount;
				
				if( brandFacingCount >= ITG_MAX_ALLOWED_DEVIATION_IN_FACING_COUNT ) {
					itgBrandsWithHighFacingCount.add(brandName+"#"+brandFacingCount);
				}
				
				if ( regularPrice != null || posPrice != null || multipackPrice != null ) {
					itgbPriceTagFound = true;
				}
				
				switch(brandName) {
				case "Winston": 
					winstonFacingCount = winstonFacingCount + brandFacingCount;
					if ( regularPrice != null || posPrice != null || multipackPrice != null ) {
						if ( multipackPrice == null ) {
							if ( posPrice != null ) {
								winstonPrice = posPrice;
							} else {
								winstonPrice = regularPrice;
							}
						}
					} else if (winstonFacingCount > 1 ){
						topITGBrandWithoutPrice = true;
					}
					break;
				case "Kool": 
					koolFacingCount = koolFacingCount + brandFacingCount;
					if ( regularPrice != null || posPrice != null || multipackPrice != null ) {
						if ( multipackPrice == null ) {
							if ( posPrice != null ) {
								koolPrice = posPrice;
							} else {
								koolPrice = regularPrice;
							}
						}
					} else if (koolFacingCount > 1 ){
						topITGBrandWithoutPrice = true;
					}
					break;
				case "Maverick": 
					maverickFacingCount = maverickFacingCount + brandFacingCount;
					if ( regularPrice != null || posPrice != null || multipackPrice != null ) {
						if ( multipackPrice == null ) {
							if ( posPrice != null ) {
								maverickPrice = posPrice;
							} else {
								maverickPrice = regularPrice;
							}
						}
					} else if (maverickFacingCount > 1 ){
						topITGBrandWithoutPrice = true;
					}
					break;
				case "Salem": 
					if ( posPrice != null ) {
						salemPrice = posPrice;
					} else {
						salemPrice = regularPrice;
					}
					break;
				case "USA Gold": 
					if ( posPrice != null ) {
						usagPrice = posPrice;
					} else {
						usagPrice = regularPrice;
					}
					break;
				}
			} else if ( brandName.contains("Marlboro") ) {
				marlboroFacingCount = marlboroFacingCount + brandFacingCount;
			}
		}
		
		List<String> rulesViolated = new ArrayList<String>();
		
		boolean rejectStore = false;
		String rejectReason = null;
		String reviewComments = null;
		
		//Rejection Rule 1 : IfSoS is very high (more than 30 percentage points) compared to sales.
		//if ( totalFacingCount < EXPECTED_MINIMUM_FACING_COUNT ) {
			if ( totalFacingCount != 0 ) {
				double itgSoS = ((double) itgFacingCount / totalFacingCount) * 100;
				double volumeShare = (double)metaInfo.get("volumeShare");
				if ( itgSoS > 0.0  && volumeShare > 0.0 && Math.abs(itgSoS - volumeShare) > 30 ) {
					//REJECT THE STORE
					rejectStore = true;
					rejectReason = "10";
					reviewComments = "15";
					bucketId = 13;
				}
			}
		//Rejection Rule 2; Reject Walgreens	
			if ( RETAILER_SPECIFIC_THRESHOLDS.containsKey(retailer.trim()) ) {
				rejectStore = true;
				rejectReason = "11";
				reviewComments = "14";
				bucketId = 14;
			}
		//} 
		
		if( !rejectStore ) {
			/*//Rule 2 : If total ITG facings  in store is 0 and no ITG price tags
			if ( itgFacingCount == 0 && itgbPriceTagFound == false ) {
				rulesViolated.add("2");
			} 
			
			//Rule 3 : If no marlboro facings
			if ( marlboroFacingCount == 0 ) {
				rulesViolated.add("3");
			}*/
			
			//Rule 1 : If ITG SoS is greater than 40
			//Rule 2 : If ITG SoS is not equal to zero, is less than 4% and is less than volume share in the store 
			if ( totalFacingCount != 0 ) {
				double itgSoS = ((double) itgFacingCount / totalFacingCount) * 100;
				double volumeShare = (double)metaInfo.get("volumeShare");
				if ( itgSoS > 0.0  ) {
					if ( itgSoS > ITG_SOS_UPPER_BOUND_PERCENTAGE ) {
						rulesViolated.add("1");
					} else if ( itgSoS < ITG_SOS_LOWER_BOUND_PERCENTAGE && itgSoS < volumeShare ) {
						rulesViolated.add("2");
					}
				}
			}
			
			//Rule 3 : If ITG facings is less than planFacings
			int planFacingCount = (int) metaInfo.get("planFacing");
			if ( planFacingCount != 0 && itgFacingCount != 0 && ( itgFacingCount < (planFacingCount - ITG_MIN_ALLOWED_DEVIATION_IN_FACING_COUNT) ) ) {
				rulesViolated.add("3");
			}
					
			//Rule 4 : If ANY one ITG brand has 15 or more facings, 
			//         AND when that brand has more than 30% facings within ITG facings 
			//         AND volume share is off from shelf share by 25% or more
			if ( itgBrandsWithHighFacingCount.size() == 1 ) {
				String outlierBrand = itgBrandsWithHighFacingCount.get(0);
				int facingCount = Integer.parseInt(outlierBrand.split("#")[1]);
				if ( ((double)facingCount / itgFacingCount) * 100 > 30) {
					double itgSoS = ((double) itgFacingCount / totalFacingCount) * 100;
					double volumeShare = (double)metaInfo.get("volumeShare");
					if ( volumeShare > 0.0 && ( Math.abs((itgSoS - volumeShare)) / volumeShare ) * 100 >= 25 ) {
						rulesViolated.add("4");
					}
				}
			} 
			
			//Rule 5 : If Winston facing is more than expected
			if ( winstonFacingCount > ITG_MAX_ALLOWED_DEVIATION_IN_WINSTON_FACING_COUNT ) {
				rulesViolated.add("5");
			}
			
			//Rule 6 :  If maverick facing is more than expected
			if ( maverickFacingCount > ITG_MAX_ALLOWED_DEVIATION_IN_MAVERICK_FACING_COUNT ) {
				rulesViolated.add("6");
			}
			
			/*//Rule 10 :  If kool facing is more than expected
			if ( koolFacingCount > ITG_MAX_ALLOWED_DEVIATION_IN_KOOL_FACING_COUNT ) {
				rulesViolated.add("10");
			}*/
			
			boolean nonDiscountBrandHasLowestPriceInStore = false;
			if ( priceToBrandMap.firstEntry() != null ) {
				Set<String> brands = priceToBrandMap.firstEntry().getValue();
				for( String brand : brands ) {
					if ( ! DISCOUNT_BRANDS.contains(brand) ) {
						nonDiscountBrandHasLowestPriceInStore = true;
						break;
					}
				}
			}
			
			//Rule 7 : If invalid price tag found (less than 4$)
			// Only one discount brand below 4,..flag.
			// For ALL brandS OTHER THAN DISCOUNTED, below 4 ..flag.
			// AND Discount brand is NOT the lowest price in this store
			if ( ( wrongLowPriceDetections || discountBrandsWithLowPrice.size() == 1 )
					&& nonDiscountBrandHasLowestPriceInStore ) {
				rulesViolated.add("7");
			}
			
			//Rule 8 : If invalid price tag found (more than 20$) OR 100$ price in raw data
			/*if (wrongHighPriceDetections || invalidHighPriceTagCount > 0) {
				rulesViolated.add("8");
			}*/
			//New Rule 8
			if ( confidenceViolation ) {
				rulesViolated.add("8");
			}
			
			//Rule 9: If any of the top 3 ITG brands (Winston / Maverick / Kool) has a facing but no price 
			// Excluding from this rule if the brand had only 1 facing.
			// AND When at least 1 ITG Top-3 Brand has price
			if (topITGBrandWithoutPrice && 
					( (winstonPrice != null && winstonPrice > 0) || (koolPrice != null && koolPrice > 0) || (maverickPrice != null && maverickPrice > 0) )) {
				//rulesViolated.add("9"); --> removed temporarily
			}
			
			//Rule 10 : If Maverick regular price is greater than regular price of any of (Winston, Kool, Salem, USA gold) 
			if ( maverickPrice != null ) {
				if ( (winstonPrice != null && maverickPrice > winstonPrice) || (koolPrice != null && maverickPrice > koolPrice) ||
						(salemPrice != null && maverickPrice > salemPrice) || (usagPrice != null && maverickPrice > usagPrice)) {
					rulesViolated.add("10");
				}
			}
			
			/*
			//Rule 11: If a non-Premium brand has the highest store price
			if ( priceToBrandMap.lastEntry() != null ) {
				Set<String> brands = priceToBrandMap.lastEntry().getValue();
				for( String brand : brands ) {
					if ( ! PREMIUM_BRANDS.contains(brand) ) {
						rulesViolated.add("11");
						break;
					}
				}
			}
			
			//Rule 12: If a non-discount brand has the lowest store price
			if ( priceToBrandMap.firstEntry() != null ) {
				Set<String> brands = priceToBrandMap.firstEntry().getValue();
				for( String brand : brands ) {
					if ( ! DISCOUNT_BRANDS.contains(brand) ) {
						rulesViolated.add("12");
						break;
					}
				}
			}*/
			
			//Rule 13: If stitching failed for store, flag for review
			if ( stitchingFailed ) {
				rulesViolated.add("13");
			}
			
			//Rule 16: Price Band based rules
			if(priceToBrandMap.size() > 1) {
				double distinctPriceCount = priceToBrandMap.size();
				
				double percentileValue = Math.ceil(distinctPriceCount * 0.2);
				
				double i = 1;
				List<Double> candidateLowPrices =  new ArrayList<Double>();
				for(Double price : priceToBrandMap.keySet()) {
					candidateLowPrices.add(price);
					i = i + 1;
					if ( i > percentileValue ) {
						break;
					}
				}
				
				i = 1;
				List<Double> candidateHighPrices =  new ArrayList<Double>();
				for(Double price : priceToBrandMap.descendingKeySet()) {
					candidateHighPrices.add(price);
					i = i + 1;
					if ( i > percentileValue ) {
						break;
					}
				}
				
				Double avgHighPrice = 0.0;
				int priceOccurenceCounts = 0;
				for(Double price : candidateHighPrices ) {
					Set<String> brands = priceToBrandMap.get(price);
					for(String brand : brands) {
						if ( PREMIUM_BRANDS.contains(brand.trim()) ) {
							avgHighPrice = avgHighPrice + price;
							priceOccurenceCounts = priceOccurenceCounts + 1;
						}
					}
				}
				
				if ( priceOccurenceCounts > 0 ) {
					
					avgHighPrice = avgHighPrice / priceOccurenceCounts;

					Double avgLowPrice = 0.0;
					priceOccurenceCounts = 0;
					for(Double price : candidateLowPrices ) {
						Set<String> brands = priceToBrandMap.get(price);
						for(String brand : brands) {
							if ( DISCOUNT_BRANDS.contains(brand.trim()) ) {
								avgLowPrice = avgLowPrice + price;
								priceOccurenceCounts = priceOccurenceCounts + 1;
							}
						}
					}
					
					if ( priceOccurenceCounts > 0 ) {
						avgLowPrice = avgLowPrice / priceOccurenceCounts;	
						
						Double highPriceLowerBound = avgHighPrice - (avgHighPrice*.05);
						Double highPriceUpperBound = avgHighPrice + (avgHighPrice*.05);
						
						Double lowPriceLowerBound = avgLowPrice - (avgLowPrice*.05);
						Double lowPriceUpperBound = avgLowPrice + (avgLowPrice*.05);
						
						for(Double price : priceToBrandMap.keySet()) {
							Set<String> brands = priceToBrandMap.get(price);
							for(String brand : brands) {
								if ( PREMIUM_BRANDS.contains(brand.trim()) ) {
									if ( price < lowPriceUpperBound ) {
										//System.out.println(taskId+","+storeId+",PREMIUM,"+brand+","+price+","+lowPriceUpperBound);
										brandsFlaggedBasedOnPriceHeuristics.add(brand);
									}
								} else if ( DISCOUNT_BRANDS.contains(brand.trim()) ) {
									if ( price > highPriceLowerBound ) {
										//System.out.println(taskId+","+storeId+",DISCOUNT,"+brand+","+price+","+highPriceLowerBound);
										brandsFlaggedBasedOnPriceHeuristics.add(brand);
									}
								} else {
									
									if ( price < (avgLowPrice - (avgLowPrice * 0.1) ) || price > (avgHighPrice + (avgHighPrice * 0.1)) ) {
										//System.out.println(taskId+","+storeId+",REGULAR,"+brand+","+price);
										brandsFlaggedBasedOnPriceHeuristics.add(brand);
									}
								}
							}
						}
						
						if ( brandsFlaggedBasedOnPriceHeuristics.size() > 0 ) {
							rulesViolated.add("16");
							for(String brandName : brandsFlaggedBasedOnPriceHeuristics) {
								Map<String,Object> brandData = aggData.get(brandName);
								brandData.put("brandLevelReview", "1");
								aggData.put(brandName,brandData);
							}
						}
						
					}
				}
				
			}
		}
		
		
		int reviewStatus = 1;
		if ( rejectStore ) {
			reviewStatus = 2;
		} else {
			if ( rulesViolated.size() > 0 ) {
				reviewComments = "";
				bucketId = ConverterUtil.getITGReviewBucketId();
				reviewStatus = 0;
				for(String ruleId :  rulesViolated ) {
					reviewComments = reviewComments + ruleId + ",";
				}
				reviewComments = reviewComments.substring(0,reviewComments.length()-1);
			}
		}
		
		itgAggregationDao.insertStoreVisitAggregationData(projectId, storeId, taskId, visitDateId, aggData, bucketId, reviewStatus, reviewComments, rejectReason);
		
		LOGGER.info("---------------ITGAggregationServiceImpl Ends runStoreVisitAggregation for projectId={}, storeId={}, taskId={}\n", 
				projectId,storeId,taskId);
	}

	@Override
	public List<LinkedHashMap<String, Object>> getStoreVisitsToReview(InputObject inputObject) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts getReviewStoreVisits for projectId={}, date={}, bucketId={}, storeId={}\n", 
				inputObject.getProjectId(),inputObject.getVisitDate(),inputObject.getValue(), inputObject.getStoreId());
		
		int bucketId = 100;
		if ( !inputObject.getValue().equals("ALL") ) {
			bucketId = Integer.parseInt(inputObject.getValue());
		}
		
		List<LinkedHashMap<String, Object>> result = itgAggregationDao.getStoreVisitsToReview(
				inputObject.getProjectId(), inputObject.getVisitDate(), bucketId, inputObject.getStoreId());
		
		LOGGER.info("---------------ITGAggregationServiceImpl Ends getReviewStoreVisits for category={}, date={}, bucketId={}, storeId={}\n", 
				inputObject.getProjectId(),inputObject.getVisitDate(),bucketId, inputObject.getStoreId());
		return result;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getStoreVisitImagesToReview(InputObject inputObject) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts getStoreVisitImagesToReview for projectId={}, storeId={}, taskId={}, showAll={}\n", 
				inputObject.getProjectId(),inputObject.getStoreId(),inputObject.getTaskId(),inputObject.getShowAll());
		
		List<LinkedHashMap<String, Object>> result = itgAggregationDao.getStoreVisitImagesToReview(
				inputObject.getProjectId(), inputObject.getStoreId(), inputObject.getTaskId(), inputObject.getShowAll());
		
		LOGGER.info("---------------ITGAggregationServiceImpl Ends getStoreVisitImagesToReview for projectId={}, storeId={}, taskId={}, showAll={}\n", 
				inputObject.getProjectId(),inputObject.getStoreId(),inputObject.getTaskId(),inputObject.getShowAll());
		return result;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getITGStoreVisitReviewComments(InputObject inputObject) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts getITGStoreVisitReviewComments for projectId={}\n", 
				inputObject.getProjectId());
		LOGGER.info("---------------ITGAggregationServiceImpl Ends getITGStoreVisitReviewComments for projectId={}\n", 
				inputObject.getProjectId());
		return REVIEW_COMMENT_ID_TO_TEXT_MAP;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getITGProductBrands(InputObject inputObject) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts getITGProductBrands for projectId={}\n", 
				inputObject.getProjectId());
		List<LinkedHashMap<String, Object>> brands = itgAggregationDao.getITGProductBrands("CIGARETTE");
		LOGGER.info("---------------ITGAggregationServiceImpl Ends getITGProductBrands for projectId={}\n", 
				inputObject.getProjectId());
		return brands;
	}
	
	@Override
	public List<LinkedHashMap<String, Object>> getITGReviewStatsSummary(InputObject inputObject) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts getITGReviewStatsSummary for projectId={}\n", 
				inputObject.getProjectId());
		List<LinkedHashMap<String, Object>> stats = itgAggregationDao.getITGReviewStatsSummary();
		LOGGER.info("---------------ITGAggregationServiceImpl Ends getITGReviewStatsSummary for projectId={}\n", 
				inputObject.getProjectId());
		return stats;
	}
	
	@Override
	public void updateStoreVisitAggregationData(String jsonPayload) {
		LOGGER.info("---------------ITGAggregationServiceImpl Starts updateStoreVisitAggregationData with payload = {}", jsonPayload);
		
		JsonObject resultObject =null;
		try {
			resultObject = new JsonParser().parse(jsonPayload).getAsJsonObject();
		} catch(Exception e) {
			//GROUND;
		}
		
		int projectId = resultObject.get("projectId").getAsInt();
		String storeId = resultObject.get("storeId").getAsString();
		String taskId = resultObject.get("taskId").getAsString();
		String visitDateId = resultObject.get("visitDateId").getAsString(); 
		int reviewStatus = resultObject.get("reviewStatus").getAsInt();
		int bucketId = resultObject.get("bucketId").getAsInt();
		
		JsonArray reviewCommentsArray = resultObject.get("reviewComments").getAsJsonArray();
		String reviewComments = "";
		for(int i=0;i<reviewCommentsArray.size();i++) {
			reviewComments = reviewComments + reviewCommentsArray.get(i).getAsString() + ",";
		}
		if(reviewComments.length() > 0 ) {
			reviewComments = reviewComments.substring(0,reviewComments.length()-1);
		}
		
		JsonArray rejectReasonArray = resultObject.get("rejectReason").getAsJsonArray();
		String rejectReason = "";
		for(int i=0;i<rejectReasonArray.size();i++) {
			rejectReason = rejectReason + rejectReasonArray.get(i).getAsString() + ",";
		}
		if(rejectReason.length() > 0 ) {
			rejectReason = rejectReason.substring(0,rejectReason.length()-1);
		}
		
		List<Map<String, Object>> brandList = new ArrayList<Map<String,Object>>();
		if ( reviewStatus == 1 ) { //parse brands info only if store is getting approved.
			JsonArray brands = resultObject.get("brands").getAsJsonArray();
			for(int i=0; i < brands.size() ; i++ ) {
				JsonObject brandElement = brands.get(i).getAsJsonObject();
				Map<String,Object> brand = new HashMap<String,Object>();
				
				brand.put("brandName", brandElement.get("brandName").getAsString());
				
				String facingCount = brandElement.get("facingCount").getAsString();
				String promotionFacingCount = brandElement.get("promotionFacingCount").getAsString();
				String oosFacingCount = brandElement.get("oosFacingCount").getAsString();
				String regularPrice = brandElement.get("regularPrice").getAsString();
				String posPrice = brandElement.get("posPrice").getAsString();
				String multipackPrice = brandElement.get("multipackPrice").getAsString();
				
				//if all values are empty, this brand can be deleted from table
				/*if ( (facingCount.isEmpty() || facingCount.equals("0")) &&
						(promotionFacingCount.isEmpty() || promotionFacingCount.equals("0")) &&
						(oosFacingCount.isEmpty() || oosFacingCount.equals("0")) &&
						(regularPrice.isEmpty() || regularPrice.equals("0")) &&
						(posPrice.isEmpty() || posPrice.equals("0")) &&
						(multipackPrice.isEmpty() || multipackPrice.equals("0"))) {
					brand.put("action", "DELETE"); 
				} */
				
				brand.put("facingCount", facingCount.isEmpty() ? "0" : Integer.parseInt(facingCount));
				brand.put("promotionFacingCount", promotionFacingCount.isEmpty() ? "0" : Integer.parseInt(promotionFacingCount));
				brand.put("oosFacingCount", oosFacingCount.isEmpty() ? "0" : Integer.parseInt(oosFacingCount));
				brand.put("regularPrice", regularPrice);
				brand.put("posPrice", posPrice);
				brand.put("multipackPrice",multipackPrice);
				
				brandList.add(brand);
			}
		}
		
		itgAggregationDao.updateStoreVisitAggregationData(projectId, storeId, taskId, visitDateId, 
				brandList, bucketId, reviewStatus, reviewComments,rejectReason);
		
		LOGGER.info("---------------ITGAggregationServiceImpl Ends updateStoreVisitAggregationData for projectId={}, storeId={}, taskId={}\n"
				);
	}
	
}

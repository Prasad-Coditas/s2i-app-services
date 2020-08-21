package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop on 09/26/17.
 */
public interface ScoreService {

	public List<LinkedHashMap<String, String>> getProjectAllStoreScores(InputObject inputObject);

	List<StoreVisitScoreWithImageInfo> getProjectStoreScores(InputObject inputObject);
	
	Map<String, Object> getProjectAllScoreSummary(InputObject inputObject);

}

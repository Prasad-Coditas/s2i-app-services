package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.dao.ScoreDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.StoreVisitScore;
import com.snap2buy.themobilebackend.model.StoreVisitScoreWithImageInfo;
import com.snap2buy.themobilebackend.service.ScoreService;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anoop on 09/26/17.
 */
@Component(value = BeanMapper.BEAN_SCORE_SERVICE)
@Scope("prototype")
public class ScoreServiceImpl implements ScoreService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_SCORE_DAO)
    private ScoreDao scoreDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;

	@Override
	public List<LinkedHashMap<String, String>> getProjectAllStoreScores(InputObject inputObject) {
		LOGGER.info("---------------ScoreServiceImpl Starts getProjectAllStoreScores----------------\n");

		List<LinkedHashMap<String, String>> result = scoreDao.getProjectAllStoreScores(inputObject.getProjectId(), inputObject.getScoreId(),
				inputObject.getLevel(), inputObject.getValue());

		LOGGER.info("---------------ScoreServiceImpl Ends getProjectAllStoreScores----------------\n");

		return result;
	}

	@Override
	public List<StoreVisitScoreWithImageInfo> getProjectStoreScores(InputObject inputObject) {
		LOGGER.info("---------------ScoreServiceImpl Starts getProjectStoreScores----------------\n");

        List<StoreVisitScore>  scores = scoreDao.getProjectStoreScores(inputObject.getProjectId(), inputObject.getStoreId());

        Map<String,List<Map<String,String>>> storeImageData = processImageDao.getProjectStoreData(inputObject.getProjectId(),inputObject.getStoreId(), inputObject.getTaskId());

        Map<String,List<Map<String,String>>> storeImageMetaData = processImageDao.getProjectStoreImageMetaData(inputObject.getProjectId(),inputObject.getStoreId());

        List<StoreVisitScoreWithImageInfo> result = new ArrayList<StoreVisitScoreWithImageInfo>();
         
        for ( StoreVisitScore score : scores ) {
        		StoreVisitScoreWithImageInfo scoreWithImageInfo = initializeScoreInfo(score);
        		String taskId = score.getTaskId();
        		List<Map<String,String>> storeImageDataForTask = storeImageData.get(taskId);
        		for(Map<String,String> oneStoreImageDataMap : storeImageDataForTask) {
        			scoreWithImageInfo.getProjectUPCs().add(ConverterUtil.convertStoreUPCMapToObject(oneStoreImageDataMap));
        		}
        		List<Map<String,String>> storeImageMetaDataForTask = storeImageMetaData.get(taskId);
        		for(Map<String,String> oneStoreImageMetaDataForTask : storeImageMetaDataForTask) {
        			scoreWithImageInfo.getImageUUIDs().add(ConverterUtil.convertStoreImageDetailsMapToObject(oneStoreImageMetaDataForTask));
        		}
        		result.add(scoreWithImageInfo);
        }
        
        LOGGER.info("--------------ScoreServiceImpl::getProjectStoreScores::return value = {}", result);
        
        LOGGER.info("---------------ScoreServiceImpl Ends getProjectStoreScores----------------\n");

        return result;
	}

	private StoreVisitScoreWithImageInfo initializeScoreInfo(StoreVisitScore score) {
		StoreVisitScoreWithImageInfo scoreWithImageInfo = new StoreVisitScoreWithImageInfo();
		BeanUtils.copyProperties(score, scoreWithImageInfo);
		return scoreWithImageInfo;
	}
	
	public Map<String, Object> getProjectAllScoreSummary(InputObject inputObject){
        LOGGER.info("---------------ScoreServiceImpl Starts getProjectAllScoreSummary inputObject={}",inputObject);
        
        Map<String, Object> resultList = scoreDao.getProjectScoreSummary(inputObject.getProjectId(), inputObject.getLevel(), inputObject.getValue());

        LOGGER.info("---------------ScoreServiceImpl Ends getProjectAllScoreSummary ----------------\n");
        return resultList;
    }

}

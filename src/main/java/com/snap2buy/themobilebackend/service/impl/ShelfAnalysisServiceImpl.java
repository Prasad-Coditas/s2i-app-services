package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.dao.ShelfAnalysisDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.ShelfAnalysis;
import com.snap2buy.themobilebackend.model.ShelfAnalysisInput;
import com.snap2buy.themobilebackend.model.Skus;
import com.snap2buy.themobilebackend.service.ShelfAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_SHELF_ANALYSIS_SERVICE)
@Scope("prototype")
public class ShelfAnalysisServiceImpl implements ShelfAnalysisService {

	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
    @Qualifier(BeanMapper.BEAN_SHELF_ANALYSIS_DAO)
    private ShelfAnalysisDao shelfAnalysisDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_IMAGE_STORE_DAO)
    private ProcessImageDao processImageDao;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

    @Override
    public void storeShelfAnalysis(ShelfAnalysisInput shelfAnalysisInput) {
        LOGGER.info("---------------ShelfAnalysisServiceImpl Starts storeShelfAnalysis----------------\n");

        ShelfAnalysis shelfAnalysis = new ShelfAnalysis();

        List<Skus> skus = shelfAnalysisInput.getSkus();

        for (Skus s : skus) {
            shelfAnalysis.setImageUUID(shelfAnalysisInput.getImageUUID());
            shelfAnalysis.setUpc(s.getUpc());
            shelfAnalysis.setExpected_facings(s.getExpected_facings());
            shelfAnalysis.setOn_shelf_availability(s.getOn_shelf_availability());
            shelfAnalysis.setDetected_facings(s.getDetected_facings());
            shelfAnalysis.setPromotion_label_present(s.getPromotion_label_present());
            shelfAnalysis.setPrice(s.getPrice());
            shelfAnalysis.setPromo_price(s.getPromo_price());
            shelfAnalysis.setStoreId(shelfAnalysisInput.getStoreID());
            shelfAnalysis.setCategoryId(shelfAnalysisInput.getCategoryID());
            shelfAnalysisDao.storeShelfAnalysis(shelfAnalysis);
        }
        String status = "done";
        processImageDao.updateShelfAnalysisStatus(status, shelfAnalysisInput.getImageUUID());
        LOGGER.info("---------------ShelfAnalysisServiceImpl Ends storeShelfAnalysis----------------\n");

    }

    @Override
    public LinkedHashMap<String, String> getShelfAnalysis(String imageUUID) {
        LOGGER.info("---------------ShelfAnalysisServiceImpl Starts getShelfAnalysis----------------\n");

        ShelfAnalysis shelfAnalysis = shelfAnalysisDao.getShelfAnalysis(imageUUID);
        LinkedHashMap<String, String> shelfAnalysisResult = new LinkedHashMap<String, String>();
        shelfAnalysisResult.put("imageUUID", shelfAnalysis.getImageUUID());
        shelfAnalysisResult.put("upc", shelfAnalysis.getUpc());
        shelfAnalysisResult.put("pog", shelfAnalysis.getExpected_facings());
        shelfAnalysisResult.put("osa", shelfAnalysis.getOn_shelf_availability());
        shelfAnalysisResult.put("facing", shelfAnalysis.getDetected_facings());
        shelfAnalysisResult.put("priceLabel", shelfAnalysis.getPrice());
        shelfAnalysisResult.put("storeId", shelfAnalysis.getStoreId());
        shelfAnalysisResult.put("categoryId", shelfAnalysis.getCategoryId());
        LOGGER.info("---------------ShelfAnalysisServiceImpl Ends getShelfAnalysis----------------\n");

        return shelfAnalysisResult;
    }

    @Override
    public File getShelfAnalysisCsv(String tempFilePath) {
        LOGGER.info("---------------ShelfAnalysisServiceImpl Starts getShelfAnalysisCsv----------------\n");

        List<ShelfAnalysis> shelfAnalysisList = shelfAnalysisDao.getShelfAnalysisCsv();


        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(tempFilePath);
            for (ShelfAnalysis shelfAnalysis : shelfAnalysisList) {
                StringBuilder shelfAnalysisRow = new StringBuilder();
                shelfAnalysisRow.append(shelfAnalysis.getImageUUID() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getUpc() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getExpected_facings() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getOn_shelf_availability() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getDetected_facings() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getPrice() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getStoreId() + ",");
                shelfAnalysisRow.append(shelfAnalysis.getCategoryId());

                fileWriter.append(shelfAnalysisRow.toString() + "\n");
            }
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("---------------ShelfAnalysisServiceImpl Ends getShelfAnalysisCsv----------------\n");
        File f = new File(tempFilePath);
        return f;
    }

	@Override
	public void getProjectBrandSummary(InputObject inputObject) {

	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStores(InputObject inputObject) {
		int projectId  = inputObject.getProjectId();
		String rollup = inputObject.getRollup();
		String month = inputObject.getMonth();
		//month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
		//this logic won't work by 2100 :)
		String[] parts = month.split("/");
		month = "20"+parts[1]+parts[0];
		
		return shelfAnalysisDao.getProjectBrandSharesAllStores(projectId, month, rollup);
	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllStates(InputObject inputObject) {
		int projectId  = inputObject.getProjectId();
		String rollup = inputObject.getRollup();
		String month = inputObject.getMonth();
		//month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
		//this logic won't work by 2100 :)
		String[] parts = month.split("/");
		month = "20"+parts[1]+parts[0];
		
		return shelfAnalysisDao.getProjectBrandSharesAllStates(projectId, month, rollup);
		
	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectBrandSharesAllCities(InputObject inputObject) {
		int projectId  = inputObject.getProjectId();
		String rollup = inputObject.getRollup();
		String month = inputObject.getMonth();
		//month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
		//this logic won't work by 2100 :)
		String[] parts = month.split("/");
		month = "20"+parts[1]+parts[0];
		
		return shelfAnalysisDao.getProjectBrandSharesAllCities(projectId, month, rollup);
		
	}

	@Override
	public List<LinkedHashMap<String, Object>> getProjectDistributionSummary(InputObject inputObject) {
		int projectId  = inputObject.getProjectId();
		
		String waveId = inputObject.getWaveId().equals("-9") ? "%" : inputObject.getWaveId();
		
		List<LinkedHashMap<String,Object>> results = new ArrayList<LinkedHashMap<String,Object>>();
		LinkedHashMap<String, Object> result = new LinkedHashMap<String,Object>();
		
		ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);
		
		Runnable highLevelSummaryRunnable = new Runnable() {
			@Override
			public void run() {
				 LOGGER.info("---------------ShelfAnalysisServiceImpl::highlevelsummary::starting execution----------------\n");
			     long startTime = System.currentTimeMillis();
				 result.putAll(shelfAnalysisDao.getProjectDistributionHighLevelSummary(projectId, waveId));
			     long endTime = System.currentTimeMillis();
				 LOGGER.info("---------------ShelfAnalysisServiceImpl::highlevelsummary::exeuctionTime={} ms",(endTime-startTime));
			}
		};

		Runnable storeLevelSummaryRunnable = new Runnable() {
			@Override
			public void run() {
				LOGGER.info("---------------ShelfAnalysisServiceImpl::storeLevel::starting execution----------------\n");
			    long startTime = System.currentTimeMillis();
				result.put("stores", shelfAnalysisDao.getDistributionSummaryStoreLevelData(projectId, waveId));
				long endTime = System.currentTimeMillis();
				LOGGER.info("---------------ShelfAnalysisServiceImpl::storeLevel::exeuctionTime= {} ms",(endTime-startTime));
			}
		};

		Runnable stateLevelSummaryRunnable = new Runnable() {
			@Override
			public void run() {
				LOGGER.info("---------------ShelfAnalysisServiceImpl::stateLevel::starting execution----------------\n");
			    long startTime = System.currentTimeMillis();
				result.put("states", shelfAnalysisDao.getDistributionSummaryStateLevelData(projectId, waveId));
				long endTime = System.currentTimeMillis();
				LOGGER.info("---------------ShelfAnalysisServiceImpl::stateLevel::exeuctionTime= {} ms",(endTime-startTime));
			}
		};
		
		Runnable skuLevelSummaryRunnable = new Runnable() {
			@Override
			public void run() {		
				LOGGER.info("---------------ShelfAnalysisServiceImpl::skuLevel::starting execution----------------\n");
			    long startTime = System.currentTimeMillis();
				result.put("skus", shelfAnalysisDao.getDistributionSummaryUPCLevelData(projectId, waveId));
				long endTime = System.currentTimeMillis();
				LOGGER.info("---------------ShelfAnalysisServiceImpl::skuLevel::exeuctionTime={} ms",(endTime-startTime));
			}
		};

		//Run them in parallel
		threadPoolExecutor.execute(highLevelSummaryRunnable);
		threadPoolExecutor.execute(storeLevelSummaryRunnable);
		threadPoolExecutor.execute(stateLevelSummaryRunnable);
		threadPoolExecutor.execute(skuLevelSummaryRunnable);

		//Ask threads to finish off
		threadPoolExecutor.shutdown();
		
		//Check status till it is finished, in interval of 5 seconds
		try {
			while (!threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
			  LOGGER.info("---------------ShelfAnalysisServiceImpl::getProjectDistributionSummary::Waiting for all tasks to finish::----------------\n");
			}
		} catch (InterruptedException e) {
			LOGGER.error("EXCEPTION {} {}", e.getMessage(), e);
            throw new RuntimeException(e);
		}
		
		result.put("waves", metaServiceDao.getProjectWaves(inputObject.getProjectId()));
		
		results.add(result);
		return results;
	}

	@Override
	public List<LinkedHashMap<String, String>> getProductDetections(String imageUUID) {

		LOGGER.info("---------------ShelfAnalysisServiceImpl getProductDetections imageUUID={}", imageUUID);

		return shelfAnalysisDao.getProductDetections(imageUUID);
	}
}

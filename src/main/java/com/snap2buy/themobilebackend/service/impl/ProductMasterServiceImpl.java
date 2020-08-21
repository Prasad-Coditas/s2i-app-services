package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.dao.ProductMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.DistributionList;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.ProductMaster;
import com.snap2buy.themobilebackend.service.ProductMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_PRODUCT_MASTER_SERVICE)
@Scope("prototype")
public class ProductMasterServiceImpl implements ProductMasterService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(BeanMapper.BEAN_PRODUCT_MASTER_DAO)
    private ProductMasterDao productMasterDao;

    @Override
    public LinkedHashMap<String, String> getUpcDetails(InputObject inputObject) {
        LOGGER.info("---------------productMasterServiceImpl Starts getUpcDetails----------------\n");


        LinkedHashMap<String, String> upcDetails = new LinkedHashMap<String, String>();
        ProductMaster productMaster = productMasterDao.getUpcDetails(inputObject.getUpc());

        upcDetails.put("upc", productMaster.getUpc());
        upcDetails.put("mfg_name", productMaster.getMfg_name());
        upcDetails.put("brand_name", productMaster.getBrand_name());
        upcDetails.put("product_type", productMaster.getProduct_type());
        upcDetails.put("product_short_name", productMaster.getProduct_short_name());
        upcDetails.put("product_long_name", productMaster.getProduct_long_name());
        upcDetails.put("attribute_1", productMaster.getAttribute_1());
        upcDetails.put("attribute_2", productMaster.getAttribute_2());
        upcDetails.put("attribute_3", productMaster.getAttribute_3());
        upcDetails.put("attribute_4", productMaster.getAttribute_4());
        upcDetails.put("brandLevel", productMaster.getBrandLevel());
        upcDetails.put("why_buy_1", productMaster.getWhy_buy_1());
        upcDetails.put("why_buy_2", productMaster.getWhy_buy_2());
        upcDetails.put("why_buy_3", productMaster.getWhy_buy_3());
        upcDetails.put("why_buy_4", productMaster.getWhy_buy_4());
        upcDetails.put("romance_copy_1", productMaster.getRomance_copy_1());
        upcDetails.put("romance_copy_2", productMaster.getRomance_copy_2());
        upcDetails.put("romance_copy_3", productMaster.getRomance_copy_3());
        upcDetails.put("romance_copy_4", productMaster.getRomance_copy_4());
        upcDetails.put("height", productMaster.getHeight());
        upcDetails.put("width", productMaster.getWidth());
        upcDetails.put("depth", productMaster.getDepth());
        upcDetails.put("product_rating", productMaster.getProduct_rating());

        LOGGER.info("---------------productMasterServiceImpl Ends getUpcDetails----------------\n");

        return upcDetails;
    }

    @Override
    public File getUpcImage(InputObject inputObject) {
        LOGGER.info("---------------productMasterServiceImpl Starts getUpcDetails----------------\n");
        LOGGER.info("upc : {}", inputObject.getUpc());
        LOGGER.info("---------------productMasterServiceImpl Ends getUpcDetails----------------\n");
        return productMasterDao.getThumbnails(inputObject.getUpc());
    }

    @Override
    public void storeThumbnails(String imageFolderPath) {
        LOGGER.info("---------------productMasterServiceImpl Starts storeThumbnails----------------\n");
        productMasterDao.storeThumbnails(imageFolderPath);
        LOGGER.info("---------------productMasterServiceImpl Ends storeThumbnails----------------\n");
    }
    @Override
    public List<LinkedHashMap<String, String>> getDistributionLists() {
        LOGGER.info("---------------productMasterServiceImpl Starts getDistributionLists----------------\n");

          List<DistributionList> listDistributionList = productMasterDao.getDistributionLists();

        List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
        for (DistributionList mapEntry : listDistributionList) {
            LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
            temp.put("id", mapEntry.getListId());
            temp.put("name", mapEntry.getListName());
            temp.put("owner", mapEntry.getOwner());
            temp.put("createdTime", mapEntry.getCreatedTime());
            temp.put("lastModifiedTime", mapEntry.getLastModifiedTime());
            result.add(temp);
        }
        LOGGER.info("---------------productMasterServiceImpl Ends getDistributionLists ----------------\n");
        return result;
    }

	@Override
	public void createUpc(ProductMaster upcInput) {
		LOGGER.info("---------------productMasterServiceImpl Starts createUpc UPC = {}", upcInput.getUpc());
		productMasterDao.createUpc(upcInput);
		LOGGER.info("---------------productMasterServiceImpl Ends createUpc UPC = {}", upcInput.getUpc());
	}
}

package com.snap2buy.themobilebackend.dao;

import com.snap2buy.themobilebackend.model.DistributionList;
import com.snap2buy.themobilebackend.model.ProductMaster;
import com.snap2buy.themobilebackend.model.UpcFacingDetail;

import java.io.File;
import java.util.List;

/**
 * Created by sachin on 10/17/15.
 */
public interface ProductMasterDao {

    ProductMaster getUpcDetails(String upc);

    void storeThumbnails(String imageFolderPath);

    File getThumbnails(String upc);

    List<DistributionList> getDistributionLists();

    List<UpcFacingDetail> getUpcForList(String listId);

	void createUpc(ProductMaster upcInput);

}

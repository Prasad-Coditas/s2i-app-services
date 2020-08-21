package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 10/17/15.
 */
public interface MetaService {

    public List<LinkedHashMap<String, String>> listCategory();
    public List<LinkedHashMap<String, String>> listCustomer();
    public List<LinkedHashMap<String, Object>> listProject(InputObject inputObject);
    public List<LinkedHashMap<String, String>> listProjectType();
    public List<LinkedHashMap<String, String>> listSkuType();
    public List<LinkedHashMap<String, String>> listProjectUpc(InputObject inputObject);
    public List<LinkedHashMap<String, String>> listRetailer();

    public List<LinkedHashMap<String, String>> getCategoryDetail(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getCustomerDetail(InputObject inputObject);
    public List<Project> getProjectDetail(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getProjectTypeDetail(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getSkuTypeDetail(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getProjectUpcDetail(InputObject inputObject);
    public List<LinkedHashMap<String, String>> getRetailerDetail(InputObject inputObject);

    public void createCustomer(Customer customerInput);
    public void createCategory(Category categoryInput);
    public void createRetailer(Retailer retailerInput);
    public void createProjectType(ProjectType projectTypeInput);
    public void createSkuType(SkuType skuTypeInput);
    public boolean createProject(Project projectInput);
    public void addUpcToProjectId(ProjectUpc projectUpc);

    public void createStore(StoreMaster storeMaster);
    public void updateStore(StoreMaster storeMaster);
    public boolean updateProject(Project projectInput);
    public List<LinkedHashMap<String, String>> listStores();
    public List<LinkedHashMap<String, String>> getStoreDetail(InputObject inputObject);

    public Map<String, Object> getProjectSummary(InputObject inputObject);

    List<LinkedHashMap<String, String>> getStoreMasterByPlaceId(String placeId);
    void createStoreWithPlaceId(StoreMaster storeMaster);
    List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCodeAndRetailsStoreId(String retailerStoreId, String retailerChainCode);
    List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCode(String retailerChainCode);
    void updateStorePlaceIdAndLatLngAndPostCode(String storeId, String placeId, String lat, String lng, String postalCode);
    List<LinkedHashMap<String, String>> getStoreMasterByStoreId(String storeId);
    List<LinkedHashMap<String, String>> getGeoMappedStoresByUserId(String userId, String customerCode);
	public LinkedHashMap<String, String> bulkUploadStores(String filenamePath);
	public String generateStoreId();
	public Map<String, String> getGooglePlaceIdByAddress(String address);
    public void updateParentProjectId(Project projectInput);

    void createCustomerCodeProjectMap(String customerCode, int projectId);
    void updateCustomerCodeProjectMap(String customerCode, int projectId, String oldProjectId);
	public List<LinkedHashMap<String, String>> listChildProjects(InputObject inputObject);
	public List<LinkedHashMap<String, Object>> getCategoryReviewComments(InputObject inputObject);
}

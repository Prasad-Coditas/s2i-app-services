package com.snap2buy.themobilebackend.dao;


import com.snap2buy.themobilebackend.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sachin on 10/17/15.
 */
public interface MetaServiceDao {

    public List<LinkedHashMap<String, String>> listCategory();
    public List<LinkedHashMap<String, String>> listCustomer();
    public List<LinkedHashMap<String, Object>> listProject(String customerCode, Boolean isParentProject, String source);
    public List<LinkedHashMap<String, String>> listProjectType();
    public List<LinkedHashMap<String, String>> listSkuType();
    public List<LinkedHashMap<String, String>> listProjectUpc(int projectId);
    public List<LinkedHashMap<String, String>> listRetailer();

    public List<LinkedHashMap<String, String>> getCategoryDetail(String id);
    public List<LinkedHashMap<String, String>> getCustomerDetail(String customerCode);
    public List<LinkedHashMap<String, String>> getProjectDetail(int projectId);
    public List<LinkedHashMap<String, String>> getProjectTypeDetail(String id);
    public List<LinkedHashMap<String, String>> getSkuTypeDetail(String id);
    public List<ProjectUpc> getProjectUpcDetail(int projectId);
    public List<LinkedHashMap<String, String>> getRetailerDetail(String retailerCode);

    public void createCustomer(Customer customerInput);
    public void createCategory(Category categoryInput);
    public void createRetailer(Retailer retailerInput);
    public void createProjectType(ProjectType projectTypeInput);
    public void createSkuType(SkuType skuTypeInput);
    public boolean createProject(Project projectInput);
    public boolean updateProject(Project projectInput);
    public void addUpcToProjectId(ProjectUpc projectUpcList);

    public List<LinkedHashMap<String, String>> listStores();
    public void createStore(StoreMaster storeMaster);
    public void updateStore(StoreMaster storeMaster);
    public List<LinkedHashMap<String, String>> getStoreDetail(String storeId);

    public Map<String, Object> getProjectSummary(int projectId, String level, String value);
	public List<ProjectQuestion> getProjectQuestionsDetail(int projectId);
	public List<ProjectQuestionObjective> getProjectObjectivesDetail(int projectId);
    List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCodeAndRetailsStoreId(String retailerStoreId, String retailerChainCode);
    List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCode(String retailerChainCode);
    void updateStorePlaceIdAndLatLngAndPostCode(String storeId, String placeId, String lat, String lng, String postalCode);
    List<LinkedHashMap<String, String>> getStoreMasterByStoreId(String storeId);
    void updateParentProjectId(Project projectInput);
    void createCustomerCodeProjectMap(String customerCode, int projectId);
    void updateCustomerCodeProjectMap(String customerCode, int projectId, String oldProjectId);
	public List<LinkedHashMap<String, String>> listChildProjects(String customerCode, int projectId);
	public List<LinkedHashMap<String, Object>> getCategoryReviewComments(String categoryId);
	public List<Map<String, String>> getProjectUpcDetailWithMetaInfo(int projectId);
	List<ProjectStoreGradingCriteria> getProjectStoreGradingCriterias(int projectId);
	public List<ProjectScoreDefinition> getProjectScoreDefinition(int projectId);
	public String getStoreByCustomerCodeAndCustomerStoreNumber(String customerCode, String customerStoreNumber);
	List<Map<String, String>> getProjectWaves(int projectId);
	public List<String> getStoreDistributionUPCs(String projectId, String storeId);
	public boolean getExternalProcessingEnabled(int projectId);
	public boolean getRealtimeProcessingEnabled(int projectId);
	public boolean getDuplicateAnalysisEnabled(int projectId);
}

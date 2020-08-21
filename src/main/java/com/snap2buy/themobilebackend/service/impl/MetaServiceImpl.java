package com.snap2buy.themobilebackend.service.impl;

import com.snap2buy.themobilebackend.util.ConverterUtil;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.StoreMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.service.MetaService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.CellType;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by sachin on 10/17/15.
 */
/*@Component(value = BeanMapper.BEAN_META_SERVICE)*/
@Scope("prototype")
@Service
public class MetaServiceImpl implements MetaService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

    @Autowired
    @Qualifier(BeanMapper.BEAN_STORE_MASTER_DAO)
    private StoreMasterDao storeMasterDao;
    
    private static final JSONObject STATES_MAP_JSON = new JSONObject("{ 'AK':'Alaska', 'AL':'Alabama', 'AR':'Arkansas', 'AZ':'Arizona', 'CA':'California', 'CO':'Colorado', 'CT':'Connecticut', 'DC':'Dist of Columbia', 'DE':'Delaware', 'DS':'Demo', 'FL':'Florida', 'GA':'Georgia', 'HI':'Hawaii', 'IA':'Iowa', 'ID':'Idaho', 'IL':'Illinois', 'IN':'Indiana', 'KS':'Kansas', 'KY':'Kentucky', 'LA':'Louisiana', 'MA':'Massachusetts', 'MD':'Maryland', 'ME':'Maine', 'MI':'Michigan', 'MN':'Minnesota', 'MO':'Missouri', 'MS':'Mississippi', 'MT':'Montana', 'NC':'North Carolina', 'ND':'North Dakota', 'NE':'Nebraska', 'NH':'New Hampshire', 'NJ':'New Jersey', 'NM':'New Mexico', 'NV':'Nevada', 'NY':'New York', 'OH':'Ohio', 'OK':'Oklahoma', 'OR':'Oregon', 'PA':'Pennsylvania', 'PR':'Puerto Rico', 'RI':'Rhode Island', 'SC':'South Carolina', 'SD':'South Dakota', 'TN':'Tennessee', 'TX':'Texas', 'UT':'Utah', 'VA':'Virginia', 'VT':'Vermont', 'WA':'Washington', 'WI':'Wisconsin', 'WV':'West Virginia', 'WY':'Wyoming', 'AS':'American Samoa', 'FM':'Federated States of Micronesia', 'GU':'Guam', 'MH':'Marshall Islands', 'MP':'Northern Mariana Islands', 'PW': 'Palau', 'VI':'Virgin Islands' }");

    @Override
    public List<LinkedHashMap<String, String>> listCategory() {
        LOGGER.info("---------------MetaServiceImpl Starts listCategory----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listCategory();

        LOGGER.info("---------------MetaServiceImpl Ends listCategory ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> listCustomer() {
        LOGGER.info("---------------MetaServiceImpl Starts listCustomer----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listCustomer();

        LOGGER.info("---------------MetaServiceImpl Ends listCustomer ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, Object>> listProject(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts listProject----------------\n");

        List<LinkedHashMap<String, Object>> resultList = metaServiceDao.listProject(inputObject.getCustomerCode(), inputObject.getShowOnlyChildProjects(), inputObject.getSource());

        LOGGER.info("---------------MetaServiceImpl Ends listProject ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> listProjectType() {
        LOGGER.info("---------------MetaServiceImpl Starts listProjectType----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listProjectType();

        LOGGER.info("---------------MetaServiceImpl Ends listProjectType ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> listSkuType() {
        LOGGER.info("---------------MetaServiceImpl Starts listSkuType----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listSkuType();

        LOGGER.info("---------------MetaServiceImpl Ends listSkuType ----------------\n");
        return resultList;
    }
    @Override
    public List<LinkedHashMap<String, String>> listProjectUpc(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts listProjectUpc----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listProjectUpc(inputObject.getProjectId());

        LOGGER.info("---------------MetaServiceImpl Ends listProjectUpc ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> listRetailer() {
        LOGGER.info("---------------MetaServiceImpl Starts listRetailer----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listRetailer();

        LOGGER.info("---------------MetaServiceImpl Ends listRetailer ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> getRetailerDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getRetailerDetail retailerCode = {}", inputObject.getRetailerCode());

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getRetailerDetail(inputObject.getRetailerCode());

        LOGGER.info("---------------MetaServiceImpl Ends getRetailerDetail ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>>  getProjectUpcDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getProjectUpcDetail customerProjectId = {}, customerCode",inputObject.getCustomerProjectId(),inputObject.getCustomerCode());

        List<ProjectUpc> resultList = metaServiceDao.getProjectUpcDetail(inputObject.getProjectId());

        LOGGER.info("---------------MetaServiceImpl Ends getProjectUpcDetail ----------------\n");
        return ConverterUtil.convertProjectUpcObjectToMap(resultList);
    }

    @Override
    public List<LinkedHashMap<String, String>> getProjectTypeDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getProjectTypeDetail id = {}", inputObject.getId());

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getProjectTypeDetail(inputObject.getId());

        LOGGER.info("---------------MetaServiceImpl Ends getProjectTypeDetail ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> getSkuTypeDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getSkuTypeDetail id = {}", inputObject.getId());

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getSkuTypeDetail(inputObject.getId());

        LOGGER.info("---------------MetaServiceImpl Ends getSkuTypeDetail ----------------\n");
        return resultList;

    }

    @Override
    public List<Project> getProjectDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getProjectDetail projectId = {}", inputObject.getProjectId());
        
        List<ProjectUpc> productUpcList = metaServiceDao.getProjectUpcDetail(inputObject.getProjectId());
        LOGGER.info("---------------MetaServiceImpl getProjectUpcDetail got size ={}", productUpcList.size());

        List<ProjectQuestion> projectQuestionsList = metaServiceDao.getProjectQuestionsDetail(inputObject.getProjectId());
        LOGGER.info("---------------MetaServiceImpl getProjectQuestionsDetail got size = {}", projectQuestionsList.size());
        
        List<ProjectQuestionObjective> projectObjectivesList = metaServiceDao.getProjectObjectivesDetail(inputObject.getProjectId());
        LOGGER.info("---------------MetaServiceImpl getProjectObjectivesDetail got size = {}",projectQuestionsList.size());
        
        List<Map<String, String>> projectWaves = metaServiceDao.getProjectWaves(inputObject.getProjectId());
        LOGGER.info("---------------MetaServiceImpl getProjectWaves got size = {}",projectQuestionsList.size());
        
        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getProjectDetail(inputObject.getProjectId());
        
        List<Project> projectList = new ArrayList<Project>();

        if ( !resultList.isEmpty() ) {
            Project project = new Project(resultList.get(0));
            project.setProjectUpcList(productUpcList);
            project.setProjectQuestionsList(projectQuestionsList);
            project.setProjectObjectives(projectObjectivesList);
            List<ProjectWaveConfig> waves = new ArrayList<ProjectWaveConfig>();
            for ( Map<String, String> wave : projectWaves ) {
            	ProjectWaveConfig oneWave = new ProjectWaveConfig(inputObject.getProjectId()+"",wave.get("waveId"),wave.get("waveName"));
            	waves.add(oneWave);
            }
            project.setWaves(waves);
            projectList.add(project);
        }
        
        LOGGER.info("---------------MetaServiceImpl Ends getProjectDetail ----------------\n");
        return projectList;
    }

    @Override
    public List<LinkedHashMap<String, String>> getCustomerDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getCustomerDetail customerCode = {}",inputObject.getCustomerCode() );

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getCustomerDetail(inputObject.getCustomerCode());

        LOGGER.info("---------------MetaServiceImpl Ends getCustomerDetail ----------------\n");
        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> getCategoryDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getCategoryDetail id = {}", inputObject.getId());

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getCategoryDetail(inputObject.getId());

        LOGGER.info("---------------MetaServiceImpl Ends getCategoryDetail ----------------\n");
        return resultList;
    }

    @Override
    public void createCustomer(Customer customerInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createCustomer customer code = {}", customerInput.getCustomerCode());

        metaServiceDao.createCustomer(customerInput);


        LOGGER.info("---------------MetaServiceImpl Ends createCustomer id generate = {}",customerInput.getId());
    }

    @Override
    public void createCategory(Category categoryInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createCategory category name = {}", categoryInput.getName());

        metaServiceDao.createCategory(categoryInput);

        LOGGER.info("---------------MetaServiceImpl Ends createCategory id generate = {}",categoryInput.getId()+"----------------\n");

    }

    @Override
    public void createRetailer(Retailer retailerInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createRetailer retailer code = {}", retailerInput.getName());

        metaServiceDao.createRetailer(retailerInput);

        LOGGER.info("---------------MetaServiceImpl Ends createRetailer id generate = {}", retailerInput.getId());

    }

    @Override
    public void createProjectType(ProjectType projectTypeInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createProjectType projectType code = {}", projectTypeInput.getName());

        metaServiceDao.createProjectType(projectTypeInput);

        LOGGER.info("---------------MetaServiceImpl Ends createProjectType id generate = {}",projectTypeInput.getId());

    }

    @Override
    public void createSkuType(SkuType skuTypeInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createSkuType skuType code = {}", skuTypeInput.getName());

        metaServiceDao.createSkuType(skuTypeInput);

        LOGGER.info("---------------MetaServiceImpl Ends createSkuType id generate = {}",skuTypeInput.getId());

    }

    @Override
    public boolean createProject(Project projectInput) {
        LOGGER.info("---------------MetaServiceImpl Starts createProject Project name = {}", projectInput.getProjectName());
        
        boolean projectCreated = metaServiceDao.createProject(projectInput);
        
        LOGGER.info("---------------MetaServiceImpl Ends createProject id = {}, status = {}",projectInput.getId(), projectCreated);
        
        return projectCreated;
    }

    @Override
    public void addUpcToProjectId(ProjectUpc projectUpc) {
        LOGGER.info("---------------MetaServiceImpl Starts addUpcToProjectId ProjectId = {} upc =",projectUpc.getProjectId(),projectUpc.getUpc());

        metaServiceDao.addUpcToProjectId(projectUpc);

        LOGGER.info("---------------MetaServiceImpl Ends addUpcToProjectId ----------------\n");

    }

    @Override
    public List<LinkedHashMap<String, String>> listStores() {
        LOGGER.info("---------------MetaServiceImpl Starts listStores ----------------\n");

        List<LinkedHashMap<String, String>> resultList =  metaServiceDao.listStores();

        LOGGER.info("---------------MetaServiceImpl Ends listStores ----------------\n");

        return resultList;
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreDetail(InputObject inputObject) {
        LOGGER.info("---------------MetaServiceImpl Starts getStoreDetail storeId = {}", inputObject.getStoreId() );

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.getStoreDetail(inputObject.getStoreId());

        LOGGER.info("---------------MetaServiceImpl Ends getStoreDetail ----------------\n");
        return resultList;
    }
    
    @Override
    public void createStore(StoreMaster storeMaster) {
        LOGGER.info("---------------MetaServiceImpl Starts createStore id = {}", storeMaster.getStoreId());

        metaServiceDao.createStore(storeMaster);

        LOGGER.info("---------------MetaServiceImpl Ends createStore ----------------\n");

    }
    @Override
    public void updateStore(StoreMaster storeMaster) {
        LOGGER.info("---------------MetaServiceImpl Starts updateStore id = {}", storeMaster.getStoreId());

        metaServiceDao.updateStore(storeMaster);

        LOGGER.info("---------------MetaServiceImpl Ends updateStore ----------------\n");

    }
    @Override
    public boolean updateProject(Project projectInput) {
        LOGGER.info("---------------MetaServiceImpl Starts updateProject Project id={}, name = {}", projectInput.getId(), projectInput.getProjectName());

        boolean projectUpdated = metaServiceDao.updateProject(projectInput);
        
        LOGGER.info("---------------MetaServiceImpl Ends updateProject id = {}, status = {}",projectInput.getId(),projectUpdated);
        
        return projectUpdated;
    }

    public Map<String, Object> getProjectSummary(InputObject inputObject){
        LOGGER.info("---------------MetaServiceImpl Starts getProjectSummary inputObject={}",inputObject);
        Map<String, Object> resultList = metaServiceDao.getProjectSummary(inputObject.getProjectId(), inputObject.getLevel(), inputObject.getValue());

        LOGGER.info("---------------MetaServiceImpl Ends getProjectSummary ----------------\n");
        return resultList;
    }

    public List<LinkedHashMap<String, String>> getStoreMasterByPlaceId(String placeId) {
        LOGGER.info("---------------MetaServiceImpl Ends getStoreMasterByPlaceId ----------------\n");
        List<LinkedHashMap<String, String>> resultList = storeMasterDao.getStoreMasterByPlaceId(placeId);
        LOGGER.info("---------------MetaServiceImpl Ends getStoreMasterByPlaceId ----------------\n");
        return resultList;
    }

    public void createStoreWithPlaceId(StoreMaster storeMaster) {
        LOGGER.info("---------------MetaServiceImpl Ends createStoreWithPlaceId ----------------\n");
        storeMasterDao.createStoreWithPlaceId(storeMaster);
        LOGGER.info("---------------MetaServiceImpl Ends createStoreWithPlaceId ----------------\n");
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCodeAndRetailsStoreId(String retailerStoreId, String retailerChainCode) {
        LOGGER.info("---------------MetaServiceImpl getStoreMasterByRetailerChainCodeAndRetailsStoreId ----------------\n");
        return metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(retailerStoreId, retailerChainCode);
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreMasterByRetailerChainCode(String retailerChainCode) {
        LOGGER.info("---------------MetaServiceImpl getStoreMasterByRetailerChainCode ----------------\n");
        return metaServiceDao.getStoreMasterByRetailerChainCode(retailerChainCode);
    }

    @Override
    public void updateStorePlaceIdAndLatLngAndPostCode(String storeId, String placeId, String lat, String lng, String postalCode) {
        LOGGER.info("---------------MetaServiceImpl updateStorePlaceId ----------------\n");
        metaServiceDao.updateStorePlaceIdAndLatLngAndPostCode(storeId, placeId, lat, lng, postalCode);
    }

    @Override
    public List<LinkedHashMap<String, String>> getStoreMasterByStoreId(String storeId) {
        LOGGER.info("---------------MetaServiceImpl getStoreMasterByStoreId ----------------\n");
        return metaServiceDao.getStoreMasterByStoreId(storeId);
    }

    @Override
    public List<LinkedHashMap<String, String>> getGeoMappedStoresByUserId(String customerCode, String userId) {
        LOGGER.info("---------------MetaServiceImpl getGeoMappedStoresByUserId::customerCode={}, userId: {}", customerCode, userId);
        return storeMasterDao.getGeoMappedStoresByUserId(customerCode, userId);
    }

	@Override
	public LinkedHashMap<String,String> bulkUploadStores(String filenamePath) {
        LOGGER.info("---------------MetaServiceImpl bulkUploadStores using file name: {}",filenamePath);
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        boolean continueProcessing = true;
		DecimalFormat format = new DecimalFormat("0.#");
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(filenamePath));
		} catch ( Exception e ){
			LOGGER.error("---------------MetaServiceImpl :: Failed to open the workbook for stores----------------\n");
			continueProcessing = false;
		}
		
		if ( continueProcessing == true ) {
			try {
				HSSFSheet metaDataSheet = workbook.getSheet("metaDataSheet");
				int numRows = metaDataSheet.getPhysicalNumberOfRows();
				String storesSheetName = null;
				String customerCode = null;
				Map<String,String> storeFieldsColumnMap = new HashMap<String,String>();
				for ( int i=0; i < numRows; i++){
					HSSFRow row = metaDataSheet.getRow(i);
					String column = "";
					if ( row.getCell(0) != null ) {
						CellType type = row.getCell(0).getCellTypeEnum();
						if ( type == CellType.NUMERIC ) {
							column = format.format(row.getCell(0).getNumericCellValue());
						} else {
							column = row.getCell(0).getStringCellValue();
						}
					}
					column = column.trim();
					String value = "";
					if ( row.getCell(1) != null ) {
						CellType type = row.getCell(1).getCellTypeEnum();
						if ( type == CellType.NUMERIC ) {
							value = format.format(row.getCell(1).getNumericCellValue());
						} else {
							value = row.getCell(1).getStringCellValue();
						}
					}
					value = value.trim();
					if ( column.equals("storesSheet")) {
						storesSheetName = value;
					} else if ( column.equals("customerCode") ) {
						customerCode = value;
					} else {
						storeFieldsColumnMap.put(column, value);
					}
				}
				HSSFSheet storesSheet = workbook.getSheet(storesSheetName);
				numRows = storesSheet.getPhysicalNumberOfRows();
				LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Stores sheet name = {}", storesSheetName );
				LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Customer Code = {}", customerCode );
				LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Number of records = {}", numRows );
				
				List<StoreMaster> storesToImport = new ArrayList<StoreMaster>();
				
				Map<String,Map<String,String>> placeIdGeoLevelMap = new HashMap<String,Map<String,String>>();
				
				Map<String,Map<String,String>> storeIdGeoLevelMap = new HashMap<String,Map<String,String>>();
				
				for ( int i = 1 ; i < numRows; i++){
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores:: Processing Record :: {}", i );
					HSSFRow row = storesSheet.getRow(i);
					if ( row != null ) {
						Map<String,String> oneStoreMap = new HashMap<String,String>();
						for(Map.Entry<String, String> oneField : storeFieldsColumnMap.entrySet()) {
							String fieldValue = null;
							CellReference fieldCell = new CellReference(oneField.getValue());
							if ( row.getCell(fieldCell.getCol()) != null ) {
								CellType type = row.getCell(fieldCell.getCol()).getCellTypeEnum();
								if ( type == CellType.NUMERIC ) {
									fieldValue = format.format(row.getCell(fieldCell.getCol()).getNumericCellValue());
								} else {
									fieldValue = row.getCell(fieldCell.getCol()).getStringCellValue();
								}
							}
							if ( StringUtils.isNotBlank(fieldValue)) {
								fieldValue = fieldValue.trim();
							}
							oneStoreMap.put(oneField.getKey(), fieldValue);
						}
						StoreMaster oneStore = new StoreMaster(oneStoreMap);
						if ( StringUtils.isBlank(oneStore.getState()) && StringUtils.isNotBlank(oneStore.getStateCode())) {
							oneStore.setState(STATES_MAP_JSON.getString(oneStore.getStateCode()));
						}

						List<LinkedHashMap<String, String>> storeInDb = 
								metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(oneStore.getRetailerStoreId(), oneStore.getRetailerChainCode());

						if ( storeInDb != null && !storeInDb.isEmpty() ) {
							LOGGER.info("---------------MetaServiceImpl::bulkUploadStores::Store already exists. Skipping. ::{}-{}-----------",
									oneStore.getRetailerChainCode(), oneStore.getRetailerStoreId() );
							result.put("StoreID:" + oneStore.getRetailerStoreId(), "Existing Store");
							continue;
						}

						//set auto-generated store id
						oneStore.setStoreId(generateStoreId());
						//set placeId, lat-long and zip
						if(null != oneStore.getCity() && StringUtils.isBlank(oneStore.getPlaceId())) {
							Map<String, String> response = getGooglePlaceIdByAddress(oneStore.getStreet() + ", " + oneStore.getCity() + ", " + oneStore.getStateCode());
							LOGGER.info("---------------MetaServiceImpl::bulkUploadStores:: Google API result :: {}", response );
			                if(null != response && !response.isEmpty() && null != response.get("placeId") &&
			                        response.get("placeId").substring(0,2).toLowerCase().equalsIgnoreCase("ch")){
			                	oneStore.setPlaceId(response.get("placeId").trim());
			                	oneStore.setLatitude(response.get("lat").trim());
			                	oneStore.setLongitude(response.get("lng").trim());
			                	oneStore.setZip(ConverterUtil.ifNullToEmpty(response.get("postal_code")).trim());
			                	result.put("StoreID:" + oneStore.getStoreId() + ":" + oneStoreMap.get("customerStoreNumber"), oneStore.getPlaceId());
			                } else {
			                    LOGGER.error("---------------MetaServiceImpl::bulkUploadStoresStoreId: {} PlaceId not found, {}, {}", oneStore.getStoreId() ,response.get("lat"), response.get("lng"));
			                    result.put("StoreID:" + oneStore.getStoreId() + ":" + oneStoreMap.get("customerStoreNumber"), "No Place Id Found");
			                }
			            }
						
						storesToImport.add(oneStore);
						
						//Map a placeId (unique store) with the geo levels, if present.
						if ( StringUtils.isNotBlank(oneStoreMap.get("geoLevel1Id")) ) {
							Map<String,String> storeGeoLevelMap = new HashMap<String,String>();
							storeGeoLevelMap.put("geoLevel1Id", oneStoreMap.get("geoLevel1Id"));
							storeGeoLevelMap.put("geoLevel2Id", oneStoreMap.get("geoLevel2Id"));
							storeGeoLevelMap.put("geoLevel3Id", oneStoreMap.get("geoLevel3Id"));
							storeGeoLevelMap.put("geoLevel4Id", oneStoreMap.get("geoLevel4Id"));
							storeGeoLevelMap.put("geoLevel5Id", oneStoreMap.get("geoLevel5Id"));
							storeGeoLevelMap.put("customerStoreNumber", oneStoreMap.get("customerStoreNumber"));
							if ( StringUtils.isNotBlank(oneStore.getPlaceId()) ) {
								placeIdGeoLevelMap.put(oneStore.getPlaceId(), storeGeoLevelMap);
							} else {
								storeIdGeoLevelMap.put(oneStore.getStoreId(), storeGeoLevelMap);
							}
						}
					}
				}
				
				//Close the import file
				workbook.close();
				
				//Insert all stores to tables - if placeId is not unique, that store will not be inserted.
				LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Starts Inserting stores----------------\n");
				storeMasterDao.createStores(storesToImport);
				LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Ends Inserting stores----------------\n");
				
				//Insert into store-geo level mapping table, only if customerCode is specified in the input file.
				if ( StringUtils.isNotBlank(customerCode)) {
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Starts Inserting geo level mapping----------------\n");
					storeMasterDao.createStoreGeoMappings(customerCode, storeIdGeoLevelMap);
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Ends Inserting geo level mapping----------------\n");
					
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Starts Inserting geo level mapping by placeId----------------\n");
					storeMasterDao.createStoreGeoMappingsViaPlaceID(customerCode, placeIdGeoLevelMap);
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Ends Inserting geo level mapping by placeId----------------\n");
				} else {
					LOGGER.info("---------------MetaServiceImpl::bulkUploadStores :: Not handling geo level mapping since customerCode is not defined----------------\n");
				}

			} catch(Exception e) {
				LOGGER.error("EXCEPTION {} {}", e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
	@Override
	public String generateStoreId() {
		LOGGER.info("--------------- MetaServiceImpl Starts generateStoreId ----------------\n");
		String storeId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
		LOGGER.info("################################## StoreId: {}", storeId );
		List<LinkedHashMap<String, String>> result = metaServiceDao.getStoreMasterByStoreId(storeId);
		if(null != result && !result.isEmpty()){
			storeId = generateStoreId();
			LOGGER.info("Generating storeId again StoreId: {}", storeId);
		}
		return storeId;
	}
	
	@Override
	public Map<String, String> getGooglePlaceIdByAddress(String address){
		LOGGER.info("---------------MetaServiceImpl starts getGooglePlaceIdByAddress with Address= {}", address);

		String API_KEY = "AIzaSyAQN8E-FngmQZHA28PLHAZHbJ08xG8jBCQ";
		String googlePlacesAPI = null;
		try {
			googlePlacesAPI = "https://maps.googleapis.com/maps/api/geocode/json?address="+ URLEncoder.encode(address, "utf-8")+"&key=" + API_KEY;
		} catch (UnsupportedEncodingException e) {
			LOGGER.info("Exception occurred during URL encoding {}", e);
		}
		LOGGER.info("---------------MetaServiceImpl getGooglePlaceIdByAddress URL = {}",googlePlacesAPI);

		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(googlePlacesAPI);
		CloseableHttpResponse response;
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map;
		LinkedHashMap<String, Object> parsedObject;
		Map<String, String> result = new HashMap<String, String>();
		try {
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Content-type", "application/json");
			response = client.execute(httpGet);

			if (response.getStatusLine().getStatusCode() == 200) {

				map = mapper.readValue(response.getEntity().getContent(), Map.class);
				List<Map<String, Object>> places = (List<Map<String, Object>>)map.get("results");

				LOGGER.info("---------------MetaServiceImpl getGooglePlaceIdByAddress Response = {}", places);

				if(!places.isEmpty()) {

					if(null != places.get(0).get("place_id")) {
						result.put("placeId",places.get(0).get("place_id").toString());
					}
					parsedObject = (LinkedHashMap<String, Object>)places.get(0).get("geometry");
					parsedObject = (LinkedHashMap<String, Object>)parsedObject.get("location");
					result.put("lat", parsedObject.get("lat").toString());
					result.put("lng", parsedObject.get("lng").toString());

					places = (List<Map<String, Object>>)places.get(0).get("address_components");

					for(Map<String, Object> data: places) {
						List<String> postal = (ArrayList<String>)data.get("types");
						if(postal.contains("postal_code")){
							result.put("postal_code", data.get("long_name").toString());
						}
					}
				}
				LOGGER.info("---------------MetaServiceImpl ends getGooglePlaceIdByAddress----------------\n");

			} else {
				LOGGER.info("---------------ERROR: MetaServiceImpl ends getGooglePlaceIdByAddress: Response not equal 200 {}", response );
			}
			return result;
		} catch (Exception e) {
			LOGGER.info("--------------- EXCEPTION MetaServiceImpl getGooglePlaceIdByAddress {}", e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
    public void updateParentProjectId(Project projectInput) {
	    LOGGER.info("Start: MetaServiceImpl: updateParentProjectId ");

	    metaServiceDao.updateParentProjectId(projectInput);

	    LOGGER.info("End: MetaServiceImpl: updateParentProjectId ");
    }

    @Override
    public void createCustomerCodeProjectMap(String customerCode, int projectId) {
        LOGGER.info("Start: MetaServiceImpl: createCustomerCodeProjectMap");

        metaServiceDao.createCustomerCodeProjectMap(customerCode, projectId);

        LOGGER.info("End: MetaServiceImpl: createCustomerCodeProjectMap");
    }

    @Override
    public void updateCustomerCodeProjectMap(String customerCode, int projectId, String oldProjectId){
        LOGGER.info("Start: MetaServiceImpl: updateCustomerCodeProjectMap");

        metaServiceDao.updateCustomerCodeProjectMap(customerCode, projectId, oldProjectId);

        LOGGER.info("End: MetaServiceImpl: updateCustomerCodeProjectMap");
    }

	@Override
	public List<LinkedHashMap<String, String>> listChildProjects(InputObject inputObject) {
		LOGGER.info("---------------MetaServiceImpl Starts listChildProjects----------------\n");

        List<LinkedHashMap<String, String>> resultList = metaServiceDao.listChildProjects(inputObject.getCustomerCode(), inputObject.getProjectId());

        LOGGER.info("---------------MetaServiceImpl Ends listChildProjects ----------------\n");
        return resultList;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getCategoryReviewComments(InputObject inputObject) {
		LOGGER.info("---------------MetaServiceImpl Starts getCategoryReviewComments----------------\n");
		List<LinkedHashMap<String, Object>> resultList = metaServiceDao.getCategoryReviewComments(inputObject.getCategoryId());
        LOGGER.info("---------------MetaServiceImpl Ends getCategoryReviewComments ----------------\n");
		return resultList;
	}
}

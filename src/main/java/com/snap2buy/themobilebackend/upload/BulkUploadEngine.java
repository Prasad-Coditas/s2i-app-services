package com.snap2buy.themobilebackend.upload;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.snap2buy.themobilebackend.async.CloudStorageService;
import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.model.InputObject;
import com.snap2buy.themobilebackend.model.ProjectQuestion;
import com.snap2buy.themobilebackend.model.StoreMaster;
import com.snap2buy.themobilebackend.model.StoreVisit;
import com.snap2buy.themobilebackend.service.MetaService;
import com.snap2buy.themobilebackend.service.ProcessImageService;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.CellType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BulkUploadEngine implements Runnable {
	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private String sync;
	private String filenamePath;
	private String categoryId;
	private String retailerCode;
	private int projectId;
	private List<ProjectQuestion> projectQuestions;
	private ProcessImageService processImageService;
	private MetaService metaService;
	private ProcessImageDao processImageDao;

	private Environment env;

	private MetaServiceDao metaServiceDao;

	private CloudStorageService cloudStorageService;

	public ProcessImageDao getProcessImageDao() {
		return processImageDao;
	}
	public MetaServiceDao getMetaServiceDao() {
		return metaServiceDao;
	}
	public void setProcessImageDao(ProcessImageDao processImageDao) {
		this.processImageDao = processImageDao;
	}
	public ProcessImageService getProcessImageService() {
		return processImageService;
	}
	public void setProcessImageService(ProcessImageService processImageService) {
		this.processImageService = processImageService;
	}

	public void setMetaServiceDao(MetaServiceDao metaServiceDao) {
		this.metaServiceDao = metaServiceDao;
	}
	public void setMetaService(MetaService metaService) {
		this.metaService = metaService;
	}
	public MetaService getMetaService() {
		return metaService;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getSync() {
		return sync;
	}
	public void setSync(String sync) {
		this.sync = sync;
	}
	public String getFilenamePath() {
		return filenamePath;
	}
	public void setFilenamePath(String filenamePath) {
		this.filenamePath = filenamePath;
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public CloudStorageService getCloudStorageService() {
		return cloudStorageService;
	}

	public void setCloudStorageService(CloudStorageService cloudStorageService) {
		this.cloudStorageService = cloudStorageService;
	}

	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	@Override
	public void run() {
		LOGGER.info("---------------BulkUploadEngine :: Start Processing file :: {}", filenamePath );
		//Add this project status as IN PROGRESS in tracker
		UploadStatusTracker.add(projectId+"");
		//Proceed
		DecimalFormat format = new DecimalFormat("0.#");
		long currTimestamp = System.currentTimeMillis() / 1000L;
		Date date = new Date(currTimestamp);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		String currentDateId = sdf.format(date);

		DateFormat INPUT_FILE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

		String batchId = ConverterUtil.getBatchIdForImport(projectId+""); //Batch ID for this upload

		//Create the directory for images to download.
		String imageDirectoryForProject = "/usr/share/s2i/" + projectId + "/";

		File imageDirectory = new File(imageDirectoryForProject);
		imageDirectory.mkdirs();
		imageDirectory.getParentFile().setReadable(true);
		imageDirectory.getParentFile().setWritable(true);
		imageDirectory.getParentFile().setExecutable(true);

		//Get already uploaded stores-task combination with images for this project
		//If at least one image is uploaded for a store, skip the store.
		List<StoreVisit> storeVisitsWithImages = processImageService.getStoreVisitsWithImages(projectId);

		//Get already uploaded stores with results
		List<StoreVisit> storeVisitsAlreadyUploaded = processImageDao.getAlreadyUploadedStoreVisit(projectId);

		//Store Visits from previous uploads which are processed again in this upload
		List<StoreVisit> existingStoreVisitsForBatchIdUpdate = new ArrayList<StoreVisit>();


		boolean continueProcessing = true;
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(filenamePath));
		} catch ( Exception e ){
			LOGGER.error("---------------BulkUploadEngine :: Failed to open the workbook----------------\n");
			continueProcessing = false;
		}

		if ( continueProcessing == true ) {
			try {
				String imageSheetName = "Images";
				String internalCustomerCode = "";
				String storeIdColumn = "";
				String agentIdColumn = "";
				String taskIdColumn = "";
				String dateIdColumn = "";
				boolean multiplePhotoLinksInOneColumn = true;
				Map<String,List<String>> photoQuestionColumnMap = new LinkedHashMap<String,List<String>>();
				Map<String,String> repResponseColumnMap = new HashMap<String,String>();

				HSSFSheet metaDataSheet = workbook.getSheet("metaDataSheet");
				int numRows = metaDataSheet.getPhysicalNumberOfRows();
				for ( int i=0; i < numRows; i++){
					HSSFRow row = metaDataSheet.getRow(i);
					if ( row != null ) {
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

						if ( column.startsWith("imageSheet") ) {
							imageSheetName = value;
						} else if ( column.startsWith("internalCustomerCode") ) {
							internalCustomerCode = value;
						} else if ( column.startsWith("retailerStoreId")) {
							storeIdColumn = value;
						} else if ( column.startsWith("agentId")) {
							agentIdColumn = value;
						} else if ( column.startsWith("taskId")) {
							taskIdColumn = value;
						} else if ( column.startsWith("dateId")) {
							dateIdColumn = value;
						} else if ( column.startsWith("photo")) {
							String questionId = column.replace("photo", "").trim();
							if (photoQuestionColumnMap.get(questionId) == null ) {
								photoQuestionColumnMap.put(questionId, new ArrayList<String>());
							}
							photoQuestionColumnMap.get(questionId).add(value);
						} else if ( column.startsWith("response")) {
							String respQuestionId = column.replace("response", "").trim();
							repResponseColumnMap.put(respQuestionId,value);
						} else if ( column.startsWith("multiplePhotoLinksInOneColumn") ) {
							multiplePhotoLinksInOneColumn = Boolean.parseBoolean(value.trim());
						}
					}
				}

				HSSFSheet imagesSheet = workbook.getSheet(imageSheetName);
				numRows = imagesSheet.getPhysicalNumberOfRows();
				LOGGER.info("---------------BulkUploadEngine :: Number of records :: {}",numRows );

				for ( int i = 1 ; i < numRows; i++){
					LOGGER.info("---------------BulkUploadEngine :: Processing Record :: {}", i);
					HSSFRow row = imagesSheet.getRow(i);
					if ( row != null ) {
						String storeId = "";
						CellReference storeCell = new CellReference(storeIdColumn);
						if ( row.getCell(storeCell.getCol()) != null ) {
							CellType type = row.getCell(storeCell.getCol()).getCellTypeEnum();
							if ( type == CellType.NUMERIC ) {
								storeId = format.format(row.getCell(storeCell.getCol()).getNumericCellValue());
							} else {
								storeId = row.getCell(storeCell.getCol()).getStringCellValue();
							}
						}

						String storeIdWithRetailCode;
						List<LinkedHashMap<String, String>> result = metaServiceDao.getStoreMasterByRetailerChainCodeAndRetailsStoreId(storeId, retailerCode);
						if(null == result || result.isEmpty()) {
							String uniqueStoreId = metaService.generateStoreId();
							StoreMaster storeMaster = new StoreMaster();
							storeMaster.setStoreId(uniqueStoreId);
							storeMaster.setRetailerChainCode(retailerCode);
							storeMaster.setRetailerStoreId(storeId);
							storeMaster.setComments("BULK_UPLOAD_Call");
							metaServiceDao.createStore(storeMaster);
							storeIdWithRetailCode = uniqueStoreId;
						}else{
							storeIdWithRetailCode = result.get(0).get("storeId");
						}

						String ticketId = "";
						CellReference taskCell = new CellReference(taskIdColumn);
						if ( row.getCell(taskCell.getCol()) != null ) {
							CellType type = row.getCell(taskCell.getCol()).getCellTypeEnum();
							if ( type == CellType.NUMERIC ) {
								ticketId = format.format(row.getCell(taskCell.getCol()).getNumericCellValue());
							} else {
								ticketId = row.getCell(taskCell.getCol()).getStringCellValue();
							}
						}
						if ( ticketId == null ) { ticketId="";}

						StoreVisit currentStoreVisit = new StoreVisit(storeIdWithRetailCode, ticketId);

						//If at least one image is uploaded for a store-task combination, skip the store-task combination.
						if ( storeVisitsWithImages.contains(currentStoreVisit) ) {
							LOGGER.info("---------------BulkUploadEngine :: storeId-taskId exists in DB ..Skipping this store and task:: {} and {}", storeIdWithRetailCode ,ticketId);
							continue;
						}

						String agentId = "";
						CellReference agentCell = new CellReference(agentIdColumn);
						if ( row.getCell(agentCell.getCol()) != null ) {
							CellType type = row.getCell(agentCell.getCol()).getCellTypeEnum();
							if ( type == CellType.NUMERIC ) {
								agentId = format.format(row.getCell(agentCell.getCol()).getNumericCellValue());
							} else {
								agentId = row.getCell(agentCell.getCol()).getStringCellValue();
							}
						}

						String dateId = "";
						CellReference dateCell = new CellReference(dateIdColumn);
						if ( row.getCell(dateCell.getCol()) != null ) {
							CellType type = row.getCell(dateCell.getCol()).getCellTypeEnum();
							if ( type == CellType.NUMERIC ) {
								dateId = sdf.format(row.getCell(dateCell.getCol()).getDateCellValue());
							} else {
								dateId = row.getCell(dateCell.getCol()).getStringCellValue();
								try {
									dateId = sdf.format(INPUT_FILE_DATE_FORMAT.parse(dateId));
								} catch (Exception e) {
									LOGGER.error("---------------BulkUploadEngine :: Error converting dateId ::{} :: {} ",dateId, e.getMessage());
								}
							}
						}

						Map<String,List<String>> photoQuestionImageLinkMap = new LinkedHashMap<String,List<String>>();
						for ( String questionId : photoQuestionColumnMap.keySet() ) {
							List<String> imageLinkList = new ArrayList<String>();
							for (String imageColumn : photoQuestionColumnMap.get(questionId) ) {
								CellReference imageCell = new CellReference(imageColumn);
								if ( row.getCell(imageCell.getCol()) != null ) {
									if (row.getCell(imageCell.getCol()).getHyperlink() != null ) {
										String link = row.getCell(imageCell.getCol()).getHyperlink().getAddress();
										if ( StringUtils.isNotBlank(link) ) {
											if ( multiplePhotoLinksInOneColumn ) {
												imageLinkList.addAll(Arrays.asList(link.split(",")));
											} else {
												imageLinkList.add(link);
											}
										}
									} else {
										String link = row.getCell(imageCell.getCol()).getStringCellValue();
										if ( StringUtils.isNotBlank(link) ) {
											if ( multiplePhotoLinksInOneColumn ) {
												imageLinkList.addAll(Arrays.asList(link.split(",")));
											} else {
												imageLinkList.add(link);
											}
										}
									}
								}
							}
							photoQuestionImageLinkMap.put(questionId, imageLinkList);
						}

						LOGGER.info("---------------BulkUploadEngine :: Record Data :: storeId={}, agentId={},taskId={},dateId={}",storeIdWithRetailCode,agentId,ticketId,dateId);

						//Insert an entry for project store results with defaults, and store rep responses -> only if store is not present in ProjectStoreResult table
						//Defer this to run after image upload for SRV projects.
						if ( !"SRV".equals(internalCustomerCode)) {
							if ( !storeVisitsAlreadyUploaded.contains(currentStoreVisit) ) {
								String imageLink = "";
								if (!photoQuestionImageLinkMap.isEmpty()) {
									if (!photoQuestionImageLinkMap.values().isEmpty() ) {
										List<List<String>> allImageLinks = photoQuestionImageLinkMap.values().stream().collect(Collectors.toList());
										for(List<String> imageLinks : allImageLinks) {
											if (!imageLinks.isEmpty()) {
												imageLink = imageLinks.get(0);
												break;
											}
										}
									}
								}
								
								//Make an entry in project store results table for this store
								insertOrUpdateStoreVisitResult(batchId,storeIdWithRetailCode, ticketId, agentId, dateId, imageLink);
								//Store project rep responses for each store
								processRepResponses(format, repResponseColumnMap, row, storeIdWithRetailCode, ticketId);
							} else {
								LOGGER.info("---------------BulkUploadEngine :: Store already exists in result table :: Skipping to image download----------------\n");
								existingStoreVisitsForBatchIdUpdate.add(currentStoreVisit);
							}
						}
						//Now, start image processing

						//For CMK and FAG, we need to scrape the imageLink for actual images
						//For others, imageLink can be used as-is for download
						Map<String,List<URL>> photoQuestionImageURLMap = new LinkedHashMap<String,List<URL>>();
						for(String onePhotoQuestionId : photoQuestionImageLinkMap.keySet()) {
							List<String> onePhotoQuestionImageLinks = photoQuestionImageLinkMap.get(onePhotoQuestionId);
							Map<Integer,URL> seqNoImageURLMap = new TreeMap<Integer,URL>();
							Integer imageLinkSequenceNumber = 1;
							for(String imageLink : onePhotoQuestionImageLinks) {
								imageLink = imageLink.trim();
								LOGGER.info("---------------BulkUploadEngine :: Accessing Image Download Link :: {}", imageLink);

								if ( internalCustomerCode.equalsIgnoreCase("CMK")) {
									Document document = null;
									boolean retryable = false;
									try {
										document = Jsoup.connect(imageLink).get();
									} catch (Exception e) {
										LOGGER.error("---------------BulkUploadEngine :: Error Accessing image store link :: {}", e.getMessage());
										retryable = true;
									}

									if ( retryable ) {
										try {
											document = Jsoup.connect(imageLink).get();
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error Accessing image store link in retry :: {}", e.getMessage());
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next row----------------\n");
											continue;
										}
									}
									Elements elements = document.select("img[src^=https://gigwalk]");
									LOGGER.info("---------------BulkUploadEngine :: Number of images to download :: {}", elements.size());
									for ( int j=0;j<elements.size();j++){
										URL imageURL = null;
										try {
											imageURL = new URL(elements.get(j).attr("src"));
											seqNoImageURLMap.put(j+1,imageURL);
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error creating URL for Image Download Link :: {}", e.getMessage());
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image link----------------\n");
											continue;
										}
									}
								} else if ( internalCustomerCode.equalsIgnoreCase("FAG")) {
									Document document = null;
									boolean retryable = false;

									try {
										document = Jsoup.connect(imageLink).validateTLSCertificates(false).get();
										//without validation of TLS certs turned off, it results in an error in handshake.
									} catch (Exception e) {
										LOGGER.error("---------------BulkUploadEngine :: Error Accessing image store link :: {}",e.getMessage());
										retryable = true;
									}

									if ( retryable ) {
										try {
											document = Jsoup.connect(imageLink).validateTLSCertificates(false).get();
											//without validation of TLS certs turned off, it results in an error in handshake.
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error Accessing image store link in retry :: {}", e.getMessage());
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next row----------------\n");
											continue;
										}
									}

									Elements elements = document.select("script[type=\"text/javascript\"]");
									for ( int j=0;j<elements.size();j++){
										String scriptTagContents= elements.get(j).toString();
										if ( scriptTagContents != null && !scriptTagContents.isEmpty() ) {
											String mediaPartRegex = ".*media = .*;";
											Pattern pattern = Pattern.compile(mediaPartRegex);
											Matcher matcher = pattern.matcher(scriptTagContents);
											while (matcher.find()) {
												String mediaPart = matcher.group(0);
												mediaPart = mediaPart.replaceAll("media = ", "");
												mediaPart = mediaPart.substring(0, mediaPart.length() - 1);
												mediaPart = mediaPart.trim();
												JsonParser parser = new JsonParser();
												JsonElement element = parser.parse(mediaPart);
												if( element.isJsonArray() ) {
													for ( JsonElement subElement : element.getAsJsonArray() ) {
														parse(subElement, seqNoImageURLMap);
													}
												} else {
													parse(element, seqNoImageURLMap);
												}
											}
										}
									}
								} else {
									URL imageURL = null;
									try {
										imageURL = new URL(imageLink);
										seqNoImageURLMap.put(imageLinkSequenceNumber,imageURL);
										imageLinkSequenceNumber = imageLinkSequenceNumber + 1;
									} catch (Exception e) {
										LOGGER.error("---------------BulkUploadEngine :: Error creating URL for Image Download Link :: {}", e.getMessage());
										LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image link----------------\n");
										continue;
									}
								}
							
							}
							
							photoQuestionImageURLMap.put(onePhotoQuestionId, seqNoImageURLMap.values().stream().collect(Collectors.toList()));
						}

						boolean atleastOneImageUploaded = false;
						String previewImageUUID = null;
						
						//now iterate over all imageURLs, download the image and saveImage in tables.
						int photoSequenceNumber = 0;
						for ( String onePhotoQuestionId : photoQuestionImageURLMap.keySet() ) {
							
							for ( URL imageURL : photoQuestionImageURLMap.get(onePhotoQuestionId) ) {
								photoSequenceNumber = photoSequenceNumber + 1;
								LOGGER.info("---------------BulkUploadEngine :: Downloading from ::{} ", imageURL.toString());

								int code = 200;

								if ( imageURL.toString().startsWith("http")) {
									boolean retryable = false;
									try {
										HttpURLConnection huc =  ( HttpURLConnection )  imageURL.openConnection ();
										huc.setInstanceFollowRedirects(true);
										huc.setRequestMethod ("GET");
										huc.addRequestProperty("User-Agent", "Mozilla/5.0");
										huc.connect() ;
										code = huc.getResponseCode();
										if ( code == 302 ) {
											LOGGER.info("---------------BulkUploadEngine :: Redirect URL : {}" ,huc.getHeaderField("Location"));
											imageURL = new URL(huc.getHeaderField("Location"));
										}
									} catch (Exception e) {
										LOGGER.error("---------------BulkUploadEngine :: Error checking URL for Image Download Link :: {}",e.getMessage());
										retryable = true;
									}

									if ( retryable ) {
										try {
											HttpURLConnection huc =  ( HttpURLConnection )  imageURL.openConnection ();
											huc.setInstanceFollowRedirects(true);
											huc.setRequestMethod ("GET");
											huc.addRequestProperty("User-Agent", "Mozilla/5.0");
											huc.connect() ;
											code = huc.getResponseCode();
											if ( code == 302 ) {
												LOGGER.info("---------------BulkUploadEngine :: Redirect URL : {}", huc.getHeaderField("Location"));
												imageURL = new URL(huc.getHeaderField("Location"));
											}
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error checking URL for Image Download Link in retry :: {}", e.getMessage() );
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image download link----------------\n");
											continue;
										}
									}
								}

								if ( code == 200 || code == 303 || code == 302 ){
									UUID uniqueKey = UUID.randomUUID();
									String imageFilePath = imageDirectoryForProject + uniqueKey.toString().trim() + ".jpg";
									String imageThumbnailPath = projectId + "/" + uniqueKey.toString().trim() + "-thm.jpg";
									String previewPath = projectId + "/" + uniqueKey.toString().trim() + "-prv.jpg";
									if ( imageURL.toString().startsWith("http")) {
										URL downloadURL = imageURL;
										if ( code == 303 ) {
											String downloadLink = imageURL.toExternalForm();
											downloadLink = downloadLink.replace("http:", "https:");
											try {
												downloadURL = new URL(downloadLink);
											} catch (Exception e) {
												LOGGER.error("---------------BulkUploadEngine :: Error creating URL for Image Download Link :: {}", e.getMessage());
												LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image----------------\n");
												continue;
											}
										}

										try {
											HttpURLConnection httpcon = (HttpURLConnection) downloadURL.openConnection();
											httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
											InputStream is = httpcon.getInputStream();
											OutputStream os = new FileOutputStream(imageFilePath);
											byte[] b = new byte[2048];
											int length;
											while ((length = is.read(b)) != -1) {
												os.write(b, 0, length);
											}
											is.close();
											os.close();
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error downoading from Image Download Link :: {}", e.getMessage());
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image link----------------\n");
											continue;
										}
										File file = new File(imageFilePath);
										if (!file.exists()) {
											file.getParentFile().mkdirs();
											file.getParentFile().setReadable(true);
											file.getParentFile().setWritable(true);
											file.getParentFile().setExecutable(true);
										}
									} else if ( imageURL.toString().startsWith("file")) {
										try {
											URL toURL = new URL("file://"+imageFilePath);
											Files.copy(Paths.get(imageURL.toString().replace("file:/", "/")),Paths.get(toURL.toURI()));
										} catch (Exception e) {
											LOGGER.error("---------------BulkUploadEngine :: Error downoading copying file from one location to other :: {}", e.getMessage());
											LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image link----------------\n");
											continue;
										}
									}

									File file = new File(imageFilePath);

									boolean isFileEligibleToStore = file.length() > 0L;
									//For Survey.com, greater than 10KB to skip thumb nails or placeholder files.
									if ( internalCustomerCode.equalsIgnoreCase("SRV") ) {
										isFileEligibleToStore = file.length() > 10000 ;
									}

									if ( isFileEligibleToStore ) {

										//Uploading file to GCS
										cloudStorageService.storeImageByPath(projectId+"", uniqueKey.toString().trim(), imageFilePath);

										try {
											if(Files.deleteIfExists(file.toPath())) {
												LOGGER.info("BulkUploadEngine: Thumbnail image deleted successfully from local storage.");
											}
										} catch (IOException e) {
											e.printStackTrace();
											LOGGER.error("EXCEPTION in BulkUploadEngine {}, {}", e.getMessage(), e);
										}

										LOGGER.info("---------------BulkUploadEngine :: Download Successful :: Storing to DB----------------\n");
										InputObject inputObject = new InputObject();
										if (!dateId.isEmpty()){
											inputObject.setVisitDate(dateId);
										} else {
											inputObject.setVisitDate(currentDateId);
										}
										inputObject.setStoreId(storeIdWithRetailCode);
										inputObject.setHostId("1");
										inputObject.setImageUUID(uniqueKey.toString().trim());
										inputObject.setCategoryId(categoryId.trim());
										inputObject.setLatitude("");
										inputObject.setLongitude("");
										inputObject.setTimeStamp(""+currTimestamp);
										inputObject.setUserId("web");
										inputObject.setSync(sync);
										inputObject.setAgentId(agentId);
										inputObject.setProjectId(projectId);
										inputObject.setTaskId(ticketId);
										inputObject.setImageHashScore("0");
										inputObject.setImageRotation("0");
										inputObject.setOrigWidth("0");
										inputObject.setOrigHeight("0");
										inputObject.setNewWidth("0");
										inputObject.setNewHeight("0");
										inputObject.setImageFilePath(projectId + "/" + uniqueKey.toString().trim() + ".jpg");
										inputObject.setThumbnailPath(imageThumbnailPath);
										inputObject.setPreviewPath(previewPath);
										inputObject.setImageUrl(imageURL.toString());
										inputObject.setSource("web");
										inputObject.setQuestionId(onePhotoQuestionId);
										inputObject.setSequenceNumber(""+photoSequenceNumber);
										LOGGER.info("---------------BulkUploadEngine :: Storing Image details to DB Start----------------\n");
										processImageService.storeImageDetails(inputObject, true); //true to indicate a bulk upload
										LOGGER.info("---------------BulkUploadEngine :: Storing Image details to DB End----------------\n");
										atleastOneImageUploaded = true;
										previewImageUUID = inputObject.getImageUUID();
									} else {
										LOGGER.error("---------------BulkUploadEngine :: Download Failed :: 0 byte or invalid file :: {}", imageFilePath );
									}
								} else {
									LOGGER.error("---------------BulkUploadEngine :: Download Failed :: Unexpected HTTP response code {}", code );
								}
							}
						}
						
						if ( atleastOneImageUploaded ) {
							if ( "SRV".equals(internalCustomerCode)) {
								if ( !storeVisitsAlreadyUploaded.contains(currentStoreVisit) ) {
									String imageLink = "";
									if (!photoQuestionImageLinkMap.isEmpty()) {
										if (!photoQuestionImageLinkMap.values().isEmpty() ) {
											List<List<String>> allImageLinks = photoQuestionImageLinkMap.values().stream().collect(Collectors.toList());
											for(List<String> imageLinks : allImageLinks) {
												if (!imageLinks.isEmpty()) {
													imageLink = imageLinks.get(0);
													break;
												}
											}
										}
									}
									//Make an entry in project store results table for this store
									insertOrUpdateStoreVisitResult(batchId,storeIdWithRetailCode, ticketId, agentId, dateId, imageLink);
									//Store project rep responses for each store
									processRepResponses(format, repResponseColumnMap, row, storeIdWithRetailCode, ticketId);
								} else {
									LOGGER.info("---------------BulkUploadEngine :: Store already exists in result table :: Skipping to image download----------------\n");
									existingStoreVisitsForBatchIdUpdate.add(currentStoreVisit);
								}
							}
							//Add one imageUUIUD as preview image. It will be updated to most significant image later during store visit result computation.
							if ( StringUtils.isNotBlank(previewImageUUID)) {
								processImageDao.updatePreviewImageUUIDForStoreVisit(projectId, storeIdWithRetailCode, ticketId, previewImageUUID);
							}
						}
					} //ends a non null row	in the excel sheet
				} //ends one record in the excel sheet
			} catch (Exception e) {
				LOGGER.error("---------------BulkUploadEngine :: Unexpected error while processing the file :: {}", e.getMessage());
			}
		}

		if ( workbook != null ) {
			try {
				workbook.close();
			} catch (Exception e) {
				LOGGER.error("---------------BulkUploadEngine :: Unexpected Exception while closing work book :: {}" , e.getMessage());

			}
		}

		// Set batchId to current batchId for all stores which are getting reprocessed in this upload
		LOGGER.info("---------------BulkUploadEngine :: Start updating batchId for stores which are getting reprocessed in this upload ----------------\n");
		processImageDao.updateBatchIdForProjectStoreResults(projectId, existingStoreVisitsForBatchIdUpdate, batchId);
		LOGGER.info("---------------BulkUploadEngine :: Updated batchId for stores which are getting reprocessed in this upload ----------------\n");

		// Set store result status to "Generate Aggregations" for all the stores uploaded in this import, using the batchId.
		LOGGER.info("---------------BulkUploadEngine :: Start moving all stores in this upload to Generate Aggregations status ----------------\n");
		String resultCode = "99"; // Generate Aggregations
		processImageDao.updateProjectStoreResultByBatchId(projectId,batchId,resultCode);
		LOGGER.info("---------------BulkUploadEngine :: Moved all stores in this upload to Generate Aggregations status ----------------\n");

		//Remove this project from upload tracker
		UploadStatusTracker.remove(projectId+"");
		//Done
		LOGGER.info("---------------BulkUploadEngine :: Completed Processing file :: {}", filenamePath);

	}
	private void processRepResponses(DecimalFormat format,
									 Map<String, String> repResponseColumnMap, HSSFRow row,
									 String storeIdWithRetailCode, String ticketId) {
		LOGGER.info("---------------BulkUploadEngine :: Storing Rep Responses to DB Start :: Responses Expected :: {}", projectQuestions.size());
		Map<String,String> repResponses = new HashMap<String,String>();
		for(String repResponseQuestionId : repResponseColumnMap.keySet()) {
			String responseColumn = repResponseColumnMap.get(repResponseQuestionId);
			if ( responseColumn != null && !responseColumn.isEmpty() ) {
				CellReference cr = new CellReference(responseColumn);
				if (row.getCell(cr.getCol()) != null) {
					String repResponse = "";
					CellType type = row.getCell(cr.getCol()).getCellTypeEnum();
					if (type == CellType.NUMERIC) {
						repResponse = format.format(row.getCell(cr.getCol()).getNumericCellValue());
					} else {
						repResponse = row.getCell(cr.getCol()).getStringCellValue();
					}
					repResponses.put(repResponseQuestionId, repResponse);
				}
			}
		}
		try {
			processImageDao.saveStoreVisitRepResponses(projectId, storeIdWithRetailCode, ticketId, repResponses);
			LOGGER.info("---------------BulkUploadEngine :: Storing Rep Responses to DB End----------------\n");
		} catch (Throwable t) {
			LOGGER.error("---------------BulkUploadEngine :: Storing Rep Responses to DB Failed :: {}", t);
		}
	}
	private void insertOrUpdateStoreVisitResult(String batchId,
												String storeIdWithRetailCode, String ticketId, String agentId,
												String dateId, String imageLink) {
		try {
			processImageDao.insertOrUpdateStoreResult(projectId, storeIdWithRetailCode, "0", "0", "0", "0", "0", agentId, ticketId, dateId, imageLink,batchId,"" );
			LOGGER.info("---------------BulkUploadEngine :: Inserted one record in store results table for this store...\n");
		} catch (Throwable t) {
			LOGGER.info("---------------BulkUploadEngine :: Error inserting one record in store results table for this store...{}", t );
		}
	}
	public List<ProjectQuestion> getProjectQuestions() {
		return projectQuestions;
	}
	public void setProjectQuestions(List<ProjectQuestion> projectQuestions) {
		this.projectQuestions = projectQuestions;
	}
	public String getRetailerCode() {
		return retailerCode;
	}
	public void setRetailerCode(String retailerCode) {
		this.retailerCode = retailerCode;
	}

	private void parse(JsonElement element, Map<Integer,URL> imageURLs){
		JsonObject obj = element.getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
		for (Map.Entry<String, JsonElement> entry: entries) {
			if ( entry.getKey().equals("mediaUrl") ) {
				//get subOrder value as sequence number
				Integer subOrder = 0;
				for (Map.Entry<String, JsonElement> subOrderEntry: entries) {
					if ( subOrderEntry.getKey().equals("subOrder") ) {
						subOrder = subOrderEntry.getValue().getAsInt();
					}
				}
				
				URL imageURL = null;
				try {
					imageURL = new URL(entry.getValue().getAsString());
					LOGGER.info("---------------BulkUploadEngine :: Found a mediaUrl :: {}", imageURL.toString());
					imageURLs.put(subOrder,imageURL);
				} catch (Exception e) {
					LOGGER.error("---------------BulkUploadEngine :: Error creating URL for Image Download Link :: {}", e.getMessage());
					LOGGER.error("---------------BulkUploadEngine :: Proceeding with next image link----------------\n");
				}
				return;
			} else if ( entry.getValue().isJsonArray() ) {
				for ( JsonElement subElement : entry.getValue().getAsJsonArray() ) {
					parse(subElement, imageURLs);
				}
			}
		}
	}

}


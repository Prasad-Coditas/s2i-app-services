package com.snap2buy.themobilebackend.async;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;


/**
 * Created by Anoop on 9/25/18.
 */
@Service
public class CloudStorageService {

	private final boolean OVERWRITE = true;
	
	private String containerName = "media";
	
	private String mediaStoreBaseUrl = null;
	
	private String mediaStoreUrl = null;
	
	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private BlobContainerClient containerClient;
	
	@Autowired
	private Environment env;

	@PostConstruct
	public void init() {
/*
		try {
			String connectionString = env.getProperty("AZURE_STORAGE_CONNECTION_STRING");
			
			// Create a BlobServiceClient object which will be used to create a container client
			BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();

			//Create a unique name for the container
			mediaStoreBaseUrl = env.getProperty("media_store_base_url_"+env.getProperty("instance"));
			
			mediaStoreUrl = mediaStoreBaseUrl + containerName + "/";

			// Return a container client object for the environment specific container
			containerClient = blobServiceClient.getBlobContainerClient(containerName);
			
		} catch (Exception e) {
			LOGGER.info("While initializing CloudStorageService");
			e.printStackTrace();
			LOGGER.error("EXCEPTION {}, {}", e.getMessage(), e);
		}
	*/}

	public void storeImageByPath(String directory, String fileName, String imageFilePath) {
		LOGGER.info("---------------CloudStorageService starts storeImageByPath::" + "directory={},filename={},imageFilePath={}----------------\n", directory, fileName, imageFilePath);
		String objectPathForFile = directory + "/" + fileName + ".jpg";

		try {
			// Get a reference to a blob
			BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
			
			LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
			
			// Upload the blob
			blobClient.uploadFromFile(imageFilePath, OVERWRITE);
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION   {}, {}", e.getMessage(), e);
			LOGGER.error("---------------CloudStorageService::storeImageByPath Failed..Retrying----------------\n");

			try {
				// Get a reference to a blob
				BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
				
				LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
				
				// Upload the blob
				blobClient.uploadFromFile(imageFilePath, OVERWRITE);
			} catch (Exception e1) {
				e1.printStackTrace();
				LOGGER.error("CloudStorageService::storeImageByPath Failed in Retry :: {}, {}", e.getMessage(), e);
			}
			
		}
		LOGGER.info("CloudStorageService Ends storeImageByPath : {}", objectPathForFile);
	}

	public String storeImage(String directory, String fileName, InputStream imageFileInputStream) {
		LOGGER.info("---------------CloudStorageService starts storeImage::" + "directory={},fileName={}", directory, fileName);
		String objectPathForFile = directory + "/" + fileName + ".jpg";
		byte[] imageBytes = null;
		
		Map<String, String> blobMetadata = Collections.singletonMap("type", "image-raw");
        BlobHttpHeaders blobHTTPHeaders = new BlobHttpHeaders().setContentDisposition("attachment")
            .setContentType("image/jpeg");

		try {
			// Get a reference to a blob
			BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
						
			LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
			
			imageBytes = IOUtils.toByteArray(imageFileInputStream);
			
			// Upload the blob
			blobClient.getBlockBlobClient().uploadWithResponse(new ByteArrayInputStream(imageBytes), imageBytes.length,
					blobHTTPHeaders, blobMetadata, null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION   {}, {}", e.getMessage(), e);
			LOGGER.error("CloudStorageService::storeImage Failed..Retrying.");
			
			try {
				// Get a reference to a blob
				BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
							
				LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
							
				// Upload the blob
				blobClient.getBlockBlobClient().uploadWithResponse(new ByteArrayInputStream(imageBytes), imageBytes.length,
						blobHTTPHeaders, blobMetadata, null, null, null, null, null);
			} catch (Exception e1) {
				e1.printStackTrace();
				LOGGER.error("CloudStorageService::storeImage Failed in Retry :: {}, {}", e.getMessage(), e);
			}
		}
		LOGGER.info("CloudStorageService Ends storeImage : {}", objectPathForFile);
		return objectPathForFile;
	}

	/**
	 * This Method returns String representation of the file.
	 * @param bucketFilePath
	 * @return
	 */
	public String getImageAnalysisMetadata(String directory, String fileName) {
		LOGGER.info("---------------CloudStorageService starts getImageAnalysisMetadata::" + "directory={},fileName={}", directory, fileName);
		String objectPathForFile = directory + "/" + fileName + ".json";
		String analysisJSON = null;

		// Get a reference to a blob
		BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
		
		// Download the blob's content to output stream, and then to a string.
        int dataSize = (int) blobClient.getProperties().getBlobSize();
		try ( ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize) ) {
			blobClient.download(outputStream);
			analysisJSON = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOGGER.error("EXCEPTION while fetching metadata json {}, {}", e.getMessage(), e);
		}

        return analysisJSON;
	}

	public String getBucketPath(Boolean isExternal) {
		return mediaStoreUrl;
	}
	
	public String getContainerName() {
		return containerName;
	}
	
	public void storeImageAnalysisMetadata(String directory, String fileName, String metaDataJson) {
		LOGGER.info("---------------CloudStorageService starts storeImageAnalysisMetadata::" + "directory={},fileName={}", directory, fileName);
		String objectPathForFile = directory + "/" + fileName + ".json";
		InputStream metaDataInputStream = IOUtils.toInputStream(metaDataJson);
		byte[] metaDataBytes = null;
		
		Map<String, String> blobMetadata = Collections.singletonMap("type", "analysis");
        BlobHttpHeaders blobHTTPHeaders = new BlobHttpHeaders().setContentDisposition("attachment")
            .setContentType("application/json; charset=utf-8");

		try {
			// Get a reference to a blob
			BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
									
			LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
			
			metaDataBytes = IOUtils.toByteArray(metaDataInputStream);
			
			// Upload the blob
			blobClient.getBlockBlobClient().uploadWithResponse(new ByteArrayInputStream(metaDataBytes), metaDataBytes.length,
					blobHTTPHeaders, blobMetadata, null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("EXCEPTION   {}, {}", e.getMessage(), e);
			LOGGER.error("CloudStorageService::storeImageAnalysisMetadata Failed..Retrying.");
			
			try {
				// Get a reference to a blob
				BlobClient blobClient = containerClient.getBlobClient(objectPathForFile);
				
				LOGGER.info("CloudStorageService uploading to BlobStorage with URL {}", blobClient.getBlobUrl());
										
				// Upload the blob
				blobClient.getBlockBlobClient().uploadWithResponse(new ByteArrayInputStream(metaDataBytes), metaDataBytes.length,
						blobHTTPHeaders, blobMetadata, null, null, null, null, null);
			} catch (Exception e1) {
				e1.printStackTrace();
				LOGGER.error("CloudStorageService::storeImageAnalysisMetadata Failed in Retry :: {}, {}", e.getMessage(), e);
			}
		} finally {
			try {
				metaDataInputStream.close();
			} catch (IOException e) {
				LOGGER.error("EXCEPTION while closing metadata json inputstream  {}, {}", e.getMessage(), e);
			}
		}
		LOGGER.info("CloudStorageService Ends storeImageAnalysisMetadata : {}", objectPathForFile);
	}
}

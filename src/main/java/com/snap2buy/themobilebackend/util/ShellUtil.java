package com.snap2buy.themobilebackend.util;

import com.snap2buy.themobilebackend.model.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by sachin on 2/4/16.
 */
public class ShellUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(ShellUtil.class);

    public static String executeCommand(String imageFilePath, String category, String uuid, String retailer, String store, String userId, String projectTypeId, String hostId) {
        String response = "";
        Boolean waitForResponse = true;
        String command = "invoke_image_analysis.sh";
        File f = new File("/root");
        LOGGER.info("---------------ShellUtil imageFilePath={}, category={}, uuid=, retailer={}, store={}, " +
                        "userId={}, projectTypeId= {}, hostId={}",
                imageFilePath,category,uuid,retailer,store,userId,projectTypeId,hostId);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", command);
        pb.environment().put("Image_File_Path", imageFilePath);
        pb.environment().put("Category", category);
        pb.environment().put("Uuid", uuid);
        pb.environment().put("Retailer_Code", retailer);
        pb.environment().put("Store_Id", store);
        pb.environment().put("User_Id", userId);
        pb.environment().put("Project_type_id", projectTypeId);
        pb.environment().put("hostId", hostId);
        pb.directory(f);
        pb.redirectErrorStream(true);

        LOGGER.info("Linux command: " + command);

        try {
            Process shell = pb.start();
            if (waitForResponse) {
                InputStream shellIn = shell.getInputStream();
                response = convertStreamToStr(shellIn);
                int shellExitStatus = shell.waitFor();
                LOGGER.info("Exit status {}", shellExitStatus);
                shellIn.close();
            }
        } catch (IOException e) {
            LOGGER.info("Error occured while executing Linux command. Error Description: {}", e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.info("Error occured while executing Linux command. Error Description: {}",e.getMessage());
        }
        LOGGER.info("---------------ShellUtil response={}", response);
        return response;
    }

    public static String readResult(String imageUUID) {
        String response = "";
        Boolean waitForResponse = true;
        String command = "read_result.sh";
        File f = new File("/root");
        LOGGER.info("---------------ShellUtil imageUUID={}", imageUUID);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", command);

        pb.environment().put("imageUUID", imageUUID);
        pb.directory(f);
        pb.redirectErrorStream(true);

        LOGGER.info("Linux command: {}", command);

        try {
            Process shell = pb.start();
            if (waitForResponse) {
                InputStream shellIn = shell.getInputStream();
                int shellExitStatus = shell.waitFor();
                LOGGER.info("Exit status{}",shellExitStatus);
                response = convertStreamToStr(shellIn);
                shellIn.close();
            }
        } catch (IOException e) {
            LOGGER.info("Error occured while executing Linux command. Error Description: {}", e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.info("Error occured while executing Linux command. Error Description: {}",e.getMessage());
        }
        LOGGER.info("---------------ShellUtil response={}", response);
        return response;
    }

    public static String convertStreamToStr(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "null";
        }
    }

    public static void createThumbnail(ImageStore imageStore, String containerName, String disk_path) {

        String response = "";
        Boolean waitForResponse = true;
        String command = "createThumbnail.sh";
        File f = new File("/root");
        LOGGER.info("---------------ShellUtil filepath={}, thumbnailPath={}, imageRotation={}, previewPath={}",
                imageStore.getImageFilePath(),imageStore.getThumbnailPath(),imageStore.getImageRotation(),imageStore.getPreviewPath());

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", command);

        pb.environment().put("CONTAINER_NAME", containerName);
        pb.environment().put("BLOB_RAW_FILE_PATH", imageStore.getProjectId() + "/"  + imageStore.getImageUUID() + ".jpg");
        pb.environment().put("LOCAL_RAW_FILE_PATH", disk_path +  imageStore.getProjectId() + "/" + imageStore.getImageUUID() + ".jpg");
        pb.environment().put("LOCAL_THM_FILE_PATH", disk_path + imageStore.getProjectId() + "/" + imageStore.getImageUUID() + "-thm.jpg");
        pb.environment().put("BLOB_THM_FILE_PATH", imageStore.getProjectId() + "/" + imageStore.getImageUUID() + "-thm.jpg");
        pb.environment().put("LOCAL_PRV_FILE_PATH", disk_path + imageStore.getProjectId() + "/" + imageStore.getImageUUID() + "-prv.jpg");
        pb.environment().put("BLOB_PRV_FILE_PATH", imageStore.getProjectId() + "/" + imageStore.getImageUUID() + "-prv.jpg");
        pb.environment().put("IMAGE_ROTATION", imageStore.getImageRotation());

        pb.directory(f);
        pb.redirectErrorStream(true);

        LOGGER.info("Linux command: {}", command);

        try {
            Process shell = pb.start();
            if (waitForResponse) {
                InputStream shellIn = shell.getInputStream();
                int shellExitStatus = shell.waitFor();
                LOGGER.info("Exit status {}", shellExitStatus);
                response = convertStreamToStr(shellIn);
                shellIn.close();
            }
        } catch (IOException e) {
            LOGGER.info("Error occurred while executing Linux command. Error Description: {}",e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.info("Error occurred while executing Linux command. Error Description: {}", e.getMessage());
        }
        LOGGER.info("---------------ShellUtil response= {}" , response);

        String data = response.split("RESPONSE_DATA:")[1];
        String values[] = data.split(",");

        imageStore.setOrigWidth(values[0].replace("\r","").replace("\n","").trim());
        imageStore.setOrigHeight(values[1].replace("\r","").replace("\n","").trim());
        imageStore.setNewWidth(values[2].replace("\r","").replace("\n","").trim());
        imageStore.setNewHeight(values[3].replace("\r","").replace("\n","").trim());
    }
}

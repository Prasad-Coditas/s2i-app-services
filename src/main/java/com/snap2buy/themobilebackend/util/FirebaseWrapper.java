package com.snap2buy.themobilebackend.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FirebaseWrapper {


    public static enum NotificationType {
        CHANGE_ROLE,
        IMAGE_ANALYSIS,
        MENU_CONFIGURATIONS
    }

    private static String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY = "AAAAmGGpNt8:APA91bF16UO6DkMgFmEjLx-7SLSqCH3EiLrAFzmRCUQbjxhbL7wHhug1sHnOVm-P1fJE2kSAbtbQ64Ml5ag3qdSejmAtJn_XUQTQqQ7u86l9-JqooW4R7qd15oyrraGhPvaBXfSuOwgy";

    private static Logger LOGGER = LoggerFactory.getLogger(FirebaseWrapper.class);


    public static void sendFirebaseNotification(JSONObject inputJson, String sendTo){
        try{
            JSONObject notificatioJson = new JSONObject();
            notificatioJson.put("title","HIDDEN NOTIFICATION");
            notificatioJson.put("body",inputJson);

            sendFirebaseRequest(notificatioJson, sendTo);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void sendFirebaseRequest(JSONObject notificatioJson, String sendTo) {

        JSONObject requestData = new JSONObject();
        requestData.put("to", sendTo);
        requestData.put("content_available",true);

        requestData.put("data", notificatioJson);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(FIREBASE_URL);

            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "key=" + SERVER_KEY);

            StringEntity stringEntity = new StringEntity(requestData.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            LOGGER.info("================== Firebase Response Code ================== {} ", httpResponse.getStatusLine().getStatusCode());
            String response = IOUtils.toString(httpResponse.getEntity().getContent());
            LOGGER.info("================== Firebase Response ================== {} " , response);
        } catch (ClientProtocolException e) {
            LOGGER.info("================== Firebase ClientProtocolException {}", e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.info("================== Firebase IOException {}", e);
            e.printStackTrace();
        }
    }
}


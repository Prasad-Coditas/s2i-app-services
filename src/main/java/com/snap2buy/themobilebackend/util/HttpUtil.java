package com.snap2buy.themobilebackend.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.snap2buy.themobilebackend.model.PrmResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sachin on 4/7/17.
 */
public class HttpUtil {

    private HttpClient  client = HttpClientBuilder.create().build();

    public static void main(String[] args) throws Exception {

        HttpUtil http = new HttpUtil();
        String token = http.getToken();
        System.out.println("token="+token);
        List<PrmResponse>prmResponses=http.getPrmResponse(token);
        List<String> filterQuestionIdList= Arrays.asList("1990174");//,"1987112","1987111","1987098","1987101","1987102","1987103","1987105","1987108","1987110");
        Map<String, List<PrmResponse>> prmResponseGroup = prmResponses.stream().collect(Collectors.groupingBy(PrmResponse::getServiceOrderId));

        List<PrmResponse> prmResponseFiltered =prmResponses.stream().filter(x -> x.getPhotoLink() != null || filterQuestionIdList.contains(x.getQuestionId())).collect(Collectors.toList());

        for (PrmResponse pr : prmResponseFiltered) {
            System.out.println("result2=" + pr.toString());
        }

    }
    private List<PrmResponse> getPrmResponse(String token) throws IOException {
        Gson gson = new Gson();
        String url = "http://devintegrations.qtraxweb.com/api/answers/getqtraxanswersbyaction";
        HttpPost post = new HttpPost(url);
        post.setHeader("Accept","application/json");
        post.setHeader("Authorization", "Bearer "+token);
        post.setHeader("Content-Type", "application/json");
        String inputString="{\"AssessmentId\":\"114817\"}";
        StringEntity jsonEntity = new StringEntity(inputString);
        post.setEntity(jsonEntity);
        for (Header h:post.getAllHeaders()){
            System.out.println(h.getName()+"="+h.getValue());
        }
        System.out.println("\nSending 'POST' request to URL : " + url);
        HttpResponse response = client.execute(post);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code : " + responseCode);

        String jsonString = EntityUtils.toString(response.getEntity());
        List<PrmResponse> prmResponses = gson.fromJson(jsonString, new TypeToken<List<PrmResponse>>(){}.getType());

//        JsonElement jsonElement= new JsonParser().parse(jsonString);
//        JsonArray jsonArray=jsonElement.getAsJsonArray();
//        for (JsonElement e: jsonArray){
//            //JsonObject jsObject = e.getAsJsonObject();
//            //System.out.println(jsObject.get("AssessmentName"));
//            prmResponses.add(gson.fromJson(e,PrmResponse.class));
//        }
//        for (PrmResponse pr : prmResponses) {
//            System.out.println("result2=" + pr.toString());
//        }
        return prmResponses;
    }

    private String getToken() throws IOException {
        String url = "http://devintegrations.qtraxweb.com/token";
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(getUrlEncodedParams());
        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code : " + responseCode);
        String jsonString = EntityUtils.toString(response.getEntity());
        return extractToken(jsonString);
    }

    public static String extractToken(String jsonLine) {
        JsonElement jsElement = new JsonParser().parse(jsonLine);
        return jsElement.getAsJsonObject().get("access_token").getAsString();
    }

    public static UrlEncodedFormEntity getUrlEncodedParams() throws UnsupportedEncodingException {
        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        //: Username=snap2insight&Password=Premium1!&grant_type=password
        postParams.add(new BasicNameValuePair("Username", "snap2insight"));
        postParams.add(new BasicNameValuePair("Password", "Premium1!"));
        postParams.add(new BasicNameValuePair("grant_type", "password"));
        UrlEncodedFormEntity urlEncodedFormEntity =  new UrlEncodedFormEntity(postParams);
        return urlEncodedFormEntity;
    }
}
package com.snap2buy.themobilebackend.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harshal on 25/09/18.
 */
public class SkuTypePOJO {

    private String skuTypeId;

    private String skuType;

    private List<Map<String, String>> found = new ArrayList<>();

    private List<Map<String, String>> notFound = new ArrayList<>();


    public String getSkuTypeId() {
        return skuTypeId;
    }

    public void setSkuTypeId(String skuTypeId) {
        this.skuTypeId = skuTypeId;
    }

    public String getSkuType() {
        return skuType;
    }

    public void setSkuType(String skyType) {
        this.skuType = skyType;
    }

    public List<Map<String, String>> getFound() {
        return found;
    }

    public void setFound(List<Map<String, String>> found) {
        this.found = found;
    }

    public List<Map<String, String>> getNotFound() {
        return notFound;
    }

    public void setNotFound(List<Map<String, String>> notFound) {
        this.notFound = notFound;
    }

    public void addFound(String upc, String name){
        if(null == this.found || found.isEmpty()){
            this.found = new ArrayList<>();
            Map<String, String> product = new HashMap<>();
            product.put("upc", upc);
            product.put("name", name);
            this.found.add(product);
        } else {
            Map<String, String> product = new HashMap<>();
            product.put("upc", upc);
            product.put("name", name);
            this.found.add(product);
        }
    }

    public void addNotFound(String upc, String name){

        if(null == this.notFound || this.notFound.isEmpty()){
            this.notFound = new ArrayList<>();
            Map<String, String> product = new HashMap<>();
            product.put("upc", upc);
            product.put("name", name);
            this.notFound.add(product);
        } else {
            Map<String, String> product = new HashMap<>();
            product.put("upc", upc);
            product.put("name", name);
            this.notFound.add(product);
        }

    }
    
    @Override
    public String toString() {
        return "SkuTypePOJO [skuTypeId=" + skuTypeId + ", skuType=" + skuType + ", found=" + found + ", notFound="
				+ notFound + "]";
    }
}

package com.snap2buy.themobilebackend.service;

import com.snap2buy.themobilebackend.model.InputObject;

/**
 * Created by sachin on 2/20/16.
 */
public interface QueryGenerationService {


    public String generateQuery(InputObject inputObject);
    public String getHeaders(InputObject inputObject);
}

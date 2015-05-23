package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
/*
 * CMPE 273 Lab4 - IzharR 010102118
 * This method is an interface definition of  the CRDTCLient 
 */
public interface CRDTInterface {

    void putCompleted (HttpResponse<JsonNode> response, String serverUrl);
    void getCompleted (HttpResponse<JsonNode> response, String serverUrl);

    void putFailed (Exception e);
    void getFailed (Exception e);
}
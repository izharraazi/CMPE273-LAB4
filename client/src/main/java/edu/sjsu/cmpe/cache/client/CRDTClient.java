package edu.sjsu.cmpe.cache.client;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.*;
import java.lang.InterruptedException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;


public class CRDTClient implements CRDTInterface {

    private ConcurrentHashMap<String, CacheServiceInterface> Server;
    private ArrayList<String> nextServer;
    private ConcurrentHashMap<String, ArrayList<String>> Results;
    private  final String Server1 = "http://localhost:3000";
    private  final String Server2 = "http://localhost:3001";
    private  final String Server3 = "http://localhost:3002";

    private static CountDownLatch countDownLatch;

    
    /*
     * CMPE 273 Lab4 - IzharR 010102118
     */
    public CRDTClient() {

        Server = new ConcurrentHashMap<String, CacheServiceInterface>(3);
        CacheServiceInterface cache0 = new DistributedCacheService(Server1, this);
        CacheServiceInterface cache1 = new DistributedCacheService(Server2, this);
        CacheServiceInterface cache2 = new DistributedCacheService(Server3, this);
        Server.put(Server1, cache0);
        Server.put(Server2, cache1);
        Server.put(Server3, cache2);
    }

    @Override
    public void putFailed(Exception e) {
        System.out.println("Sorry request cant be processed");
        countDownLatch.countDown();
    }

    @Override
    public void putCompleted(HttpResponse<JsonNode> response, String serverUrl) {
        int getCode = response.getCode();
        System.out.println("HTTP code =>[" + getCode + "] Response :::: server ====>" + serverUrl);
        nextServer.add(serverUrl);
        countDownLatch.countDown();
    }

    @Override
    public void getFailed(Exception e) {
        System.out.println("The request has failed");
        countDownLatch.countDown();
    }

    @Override
    public void getCompleted(HttpResponse<JsonNode> response, String serverUrl) {

        String value = null;
        if (response != null && response.getCode() == 200) {
            value = response.getBody().getObject().getString("value");
                System.out.println("Value from server [ " + serverUrl + " ] =>" + value);
            ArrayList ServerWithValue = Results.get(value);
            if (ServerWithValue == null) {
                ServerWithValue = new ArrayList(3);
            }
            ServerWithValue.add(serverUrl);

            Results.put(value, ServerWithValue);
        }

        countDownLatch.countDown();
    }



    public boolean put(long key, String value) throws InterruptedException {
        nextServer = new ArrayList(Server.size());
        countDownLatch = new CountDownLatch(Server.size());

        for (CacheServiceInterface cache : Server.values()) {
            cache.put(key, value);
        }

        countDownLatch.await();

        boolean isSuccess = Math.round((float)nextServer.size() / Server.size()) == 1;

        if (! isSuccess) {
            delete(key, value);
        }
        return isSuccess;
    }

    public void delete(long key, String value) {

        for (final String serverUrl : nextServer) {
            CacheServiceInterface server = Server.get(serverUrl);
            server.delete(key);
        }
    }
    
    public ArrayList<String> maxKeyForTable(ConcurrentHashMap<String, ArrayList<String>> table) {
        ArrayList<String> maxKeys= new ArrayList<String>();
        int maxValue = -1;
        for(Map.Entry<String, ArrayList<String>> entry : table.entrySet()) {
            if(entry.getValue().size() > maxValue) {
                maxKeys.clear(); 
                maxKeys.add(entry.getKey());
                maxValue = entry.getValue().size();
            }
            else if(entry.getValue().size() == maxValue)
            {
                maxKeys.add(entry.getKey());
            }
        }
        return maxKeys;
    }
    
    public String get(long key) throws InterruptedException {
        Results = new ConcurrentHashMap<String, ArrayList<String>>();
        countDownLatch = new CountDownLatch(Server.size());

        for (final CacheServiceInterface server : Server.values()) {
            server.get(key);
        }
        countDownLatch.await();
        String rightValue = Results.keys().nextElement();
        if (Results.keySet().size() > 1 || Results.get(rightValue).size() != Server.size()) {
            ArrayList<String> maxValues = maxKeyForTable(Results);
            if (maxValues.size() == 1) {
                rightValue = maxValues.get(0);
                ArrayList<String> fixServer = new ArrayList(Server.keySet());
                fixServer.removeAll(Results.get(rightValue));
                for (String serverUrl : fixServer) {
                    System.out.println(" Fixing [" + serverUrl + "]  value: " + rightValue);
                    CacheServiceInterface server = Server.get(serverUrl);
                    server.put(key, rightValue);
                }
            } else {
            }
        }
        return rightValue;
    }
}
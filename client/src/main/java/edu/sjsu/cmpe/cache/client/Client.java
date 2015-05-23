package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.Unirest;
import java.lang.*;

public class Client {

    public static void main(String[] args) throws Exception {
        System.out.println(" -- Starting Cache Client --");
        CRDTClient crdtClient = new CRDTClient();

        /*A---->Key1 */

        boolean result = crdtClient.put(1, "A");
        System.out.println(" Result:  " + result);
        Thread.sleep(30*1000);
        System.out.println("Step 1: put(1 => a) , sleeping 30s.");


        /* Update Key1--->B */

        crdtClient.put(1, "B");
        Thread.sleep(30*1000);
        System.out.println("Step 2: put(1 => b), sleeping for 30s");


       /*  Recall Key1*/

        String value = crdtClient.get(1);
        System.out.println("Step 3: get Key value => " + value);
        System.out.println(" -- Exiting.................. --");
        Unirest.shutdown();

    }

}

package com.shoppingbox.api;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.shoppingbox.service.MallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by nikhil.bansal on 17/04/14.
 */

@Path("/")
public class IngestionApi {

    final static Logger logger = LoggerFactory.getLogger(IngestionApi.class);

//    public IngestionApi(){
//        logger.info("HERE");
//        Global global = new Global();
//        global.onStop();
//        global.beforeStart();
//        global.onLoadConfig();
//        global.onStart();
//    }

    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMsg(@PathParam("param") String msg, @PathParam("another") String msg1) {

        logger.info("getMsg");
        String output = "{\n" +
                "\"type\": \"Mall\",\n" +
                "\"name\": \"Forum Value Mall\",\n" +
                "\"address\": {\n" +
                "\"address_lines\": [\"No 62, Whitefield Main Road\"],\n" +
                "\"locality\": \"Whitefield\",\n" +
                "\"city\": \"Bangalore\",\n" +
                "\"state\": \"Karnataka\",\n" +
                "\"country\": \"Bangalore\",\n" +
                "\"pincode\": 560066,\n" +
                "\"latitude\": 23.0000,\n" +
                "\"longitude\": 34.45454343\n" +
                "}\n" +
                "}\n";

        //return Response.status(200).entity(output).build();
        return output;

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/upsert/{type}")
    public String upsert(@PathParam("type") String type, String body) {
        String response = MallService.upsert(body);
        return response;
    }

    @GET
    @Path("/shutdown")
    @Produces(MediaType.APPLICATION_JSON)
    public String shutdown(){
        ODatabaseDocumentPool.global().close();
        String output = "{\n" +
                "\"type\": \"Mall\",\n" +
                "\"name\": \"Forum Value Mall\",\n" +
                "\"address\": {\n" +
                "\"address_lines\": [\"No 62, Whitefield Main Road\"],\n" +
                "\"locality\": \"Whitefield\",\n" +
                "\"city\": \"Bangalore\",\n" +
                "\"state\": \"Karnataka\",\n" +
                "\"country\": \"Bangalore\",\n" +
                "\"pincode\": 560066,\n" +
                "\"latitude\": 23.0000,\n" +
                "\"longitude\": 34.45454343\n" +
                "}\n" +
                "}\n";

        //return Response.status(200).entity(output).build();
        return output;
    }

}

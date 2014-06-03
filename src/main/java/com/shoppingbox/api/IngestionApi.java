package com.shoppingbox.api;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.metadata.schema.*;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.service.IService;
import com.shoppingbox.service.IndexService;
import com.shoppingbox.util.serviceinitialization.Services;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nikhil.bansal on 17/04/14.
 */

@Path("/")
public class IngestionApi {

    final static Logger logger = LoggerFactory.getLogger(IngestionApi.class);

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
//        if(DbHelper.getConnection() == null)    System.out.println("WTF");
        OSchemaProxy s = (OSchemaProxy) DbHelper.getConnection().getMetadata().getSchema();

        if (!s.getClasses().isEmpty()) {

            final List<OClass> classes = new ArrayList<OClass>(s.getClasses());
            Collections.sort(classes);

            for (OClass cls : classes) {
                System.out.println("name " + cls.getName());
                System.out.println("default-cluster-id " + cls.getDefaultClusterId());
                System.out.println("cluster-ids " + cls.getClusterIds());
                if (((OClassImpl) cls).getOverSizeInternal() > 1)
                    System.out.println("oversize " + ((OClassImpl) cls).getOverSizeInternal());
                if (cls.isStrictMode())
                    System.out.println("strictMode " + cls.isStrictMode());
                if (cls.getSuperClass() != null)
                    System.out.println("super-class " + cls.getSuperClass().getName());
                if (cls.getShortName() != null)
                    System.out.println("short-name " + cls.getShortName());
                if (cls.isAbstract())
                    System.out.println("abstract " + cls.isAbstract());
                if (((OClassImpl) cls).getCustomInternal() != null){
                    System.out.println("customFields " + ((OClassImpl) cls).getCustomInternal());
                }

                if (!cls.properties().isEmpty()) {
                    System.out.printf("properties");

                    final List<OProperty> properties = new ArrayList<OProperty>(cls.declaredProperties());
                    Collections.sort(properties);

                    for (OProperty p : properties) {
                        System.out.println("name " +  p.getName());
                        System.out.println("type" + p.getType().toString());
                        if (p.isMandatory())
                            System.out.println("mandatory" + p.isMandatory());
                        if (p.isReadonly())
                            System.out.println("readonly" + p.isReadonly());
                        if (p.isNotNull())
                            System.out.println("not-null" + p.isNotNull());
                        if (p.getLinkedClass() != null)
                            System.out.println("linked-class" + p.getLinkedClass().getName());
                        if (p.getLinkedType() != null)
                            System.out.println("linked-type" + p.getLinkedType().toString());
                        if (p.getMin() != null)
                            System.out.println("min" + p.getMin());
                        if (p.getMax() != null)
                            System.out.println("max" + p.getMax());
                        if (p.getRegexp() != null)
                            System.out.println("regexp" + p.getRegexp());
//                        ((OPropertyImpl)p).setCustomInternal("key", "value");
                        if (((OPropertyImpl) p).getCustomInternal() != null)
                            System.out.println("customFields" + ((OPropertyImpl) p).getCustomInternal());
                    }
                }
            }
        }
        ((OSchemaProxy) DbHelper.getConnection().getMetadata().getSchema()).saveInternal();

        //return Response.status(200).entity(output).build();
        return output;

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

    @POST
    @Path("/create/index")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createIndex(String body) {
        logger.info(String.format("createIndex : body - %s", body));
        try {
            IndexService.getInstance().createIndex(body);
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return Response.status(201).entity("").build();
    }

    @DELETE
    @Path("/delete/index")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dropIndex(String body) {
        logger.info(String.format("dropIndex : body - %s", body));
        try {
            IndexService.getInstance().dropIndex(body);
        } catch (JSONException e) {
            logger.error("Error", e);
        }
        return "{}";
    }

    @DELETE
    @Path("/delete/index/{indexname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String dropIndexByName(@PathParam("indexname") String indexname) {
        logger.info(String.format("dropIndexByName : indexname - %s", indexname));
        IndexService.getInstance().dropIndexByName(indexname);
        return "{}";
    }

    @GET
    @Path("/indexes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndexes(){
        logger.info(String.format("getIndexes"));
        return Response.status(Response.Status.OK).entity(IndexService.getInstance().getIndexes()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/upsert/{entity}")
    public String upsert(@PathParam("entity") String entity, String body) {
        IService iService = Services.servicesMap.get(entity);
        if(iService == null){
            logger.error(String.format("Cannot find service against entity - %s", entity));
            return "{}";
        }
        String response = iService.upsert(body);
        return response;
    }

}

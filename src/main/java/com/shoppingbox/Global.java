/*
     Copyright 2012-2013 
     Claudio Tesoriero - c.tesoriero-at-baasbox.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.shoppingbox;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.service.storage.StatisticsService;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Global {
    final static Logger logger = LoggerFactory.getLogger(Global.class);
	
	private static Boolean  justCreated = false;


	  public void beforeStart() {
		  logger.info("BaasBox is starting...");
		  logger.info("System details:");
		  logger.info(StatisticsService.os().toString());
		  logger.info(StatisticsService.memory().toString());
		  logger.info(StatisticsService.java().toString());
		  if (Boolean.parseBoolean(BBConfiguration.configuration.getString(BBConfiguration.DUMP_DB_CONFIGURATION_ON_STARTUP))) logger.info(StatisticsService.db().toString());
		 
		  logger.info("...Loading plugin...");
	  }

	  public void onLoadConfig(){
		  logger.debug("Global.onLoadConfig() called");
		  logger.info("BaasBox is preparing OrientDB Embedded Server...");
		  try{
			  OGlobalConfiguration.TX_LOG_SYNCH.setValue(Boolean.TRUE);
			  OGlobalConfiguration.TX_COMMIT_SYNCH.setValue(Boolean.TRUE);
			  
			  OGlobalConfiguration.NON_TX_RECORD_UPDATE_SYNCH.setValue(Boolean.TRUE);
			  
			  OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(Boolean.FALSE);
			  OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(Boolean.FALSE);
			  
			  OGlobalConfiguration.INDEX_MANUAL_LAZY_UPDATES.setValue(-1);
			  OGlobalConfiguration.FILE_LOCK.setValue(false);
			  
			  OGlobalConfiguration.FILE_DEFRAG_STRATEGY.setValue(1);
			  
			  OGlobalConfiguration.MEMORY_USE_UNSAFE.setValue(false);
			  
			  Orient.instance().startup();
              ODatabaseDocumentTx db = null;
              try {
                  final String url = "plocal:" + BBConfiguration.configuration.getString(BBConfiguration.DB_PATH);
                  db = new ODatabaseDocumentTx(url);
                  if (!db.getURL().startsWith("remote:") && !db.exists()) {
                      logger.info("DB does not exist, BaasBox will create a new one");
                      OrientGraph graph = new OrientGraph(db.getURL());
                      graph.shutdown();
                      justCreated  = true;
                  }else{
                      logger.info("DB already exists");
                  }
			  } catch (Throwable e) {
					logger.error("!! Error initializing BaasBox!", e);
					logger.error(ExceptionUtils.getFullStackTrace(e));
					throw e;
			  } finally {
		    	 if (db!=null && !db.isClosed()) db.close();
			  }
			  logger.info("DB has been created successfully");
		    }catch (Throwable e){
		    	logger.error("!! Error initializing BaasBox!", e);
		    	logger.error("Abnormal BaasBox termination.");
                //TODO decide what to do here
		    	//System.exit(-1);
		    }
		  logger.debug("Global.onLoadConfig() ended");
	  }

	  public void onStart() {
		 logger.debug("Global.onStart() called");
	    //Orient.instance().shutdown();

	    ODatabaseRecordTx db =null;
	    try{
	    	if (justCreated){
		    	try {
		    		//we MUST use admin/admin because the db was just created
		    		db = DbHelper.open(BBConfiguration.getAPPCODE(), "admin", "admin");
		    		DbHelper.setupDb(db);
		    	}catch (Throwable e){
					logger.error("!! Error initializing BaasBox!", e);
					logger.error(ExceptionUtils.getFullStackTrace(e));
					throw e;
		    	} finally {
		    		if (db!=null && !db.isClosed()) db.close();
		    	}
		    	justCreated=false;
	    	}
	    }catch (Throwable e){
	    	logger.error("!! Error initializing BaasBox!", e);
	    	logger.error("Abnormal BaasBox termination.");
            //TODO decide what to do here
	    	//System.exit(-1);
	    }
    	logger.info("Updating default users passwords...");
    	try {
    		db = DbHelper.open( BBConfiguration.getAPPCODE(), BBConfiguration.getBaasBoxAdminUsername(), BBConfiguration.getBaasBoxAdminPassword());
			DbHelper.updateDefaultUsers();

//			String bbid= Internal.INSTALLATION_ID.getValueAsString();
//			if (bbid==null) throw new Exception ("Unique id not found! Hint: could the DB be corrupted?");
//			logger.info("BaasBox unique id is " + bbid);
		} catch (Exception e) {
	    	logger.error("!! Error initializing BaasBox!", e);
	    	logger.error("Abnormal BaasBox termination.");
            return;
//	    	System.exit(-1);
		} finally {
    		if (db!=null && !db.isClosed()) db.close();
    	}

    	try{
    		db = DbHelper.open( BBConfiguration.getAPPCODE(), BBConfiguration.getBaasBoxAdminUsername(), BBConfiguration.getBaasBoxAdminPassword());
//    		IosCertificateHandler.init();
    	}catch (Exception e) {
	    	logger.error("!! Error initializing BaasBox!", e);
	    	logger.error("Abnormal BaasBox termination.");
	    	System.exit(-1);
		} finally {
            //TODO keeping the connection open now
    		//if (db!=null && !db.isClosed()) db.close();
    	}
    	logger.info("...done");

//    	overrideSettings();
//
//    	//activate metrics
//    	BaasBoxMetric.setExcludeURIStartsWith(com.shoppingbox.controllers.routes.Root.startMetrics().url());
//    	if (BBConfiguration.getComputeMetrics()) BaasBoxMetric.start();
//
//    	//prepare the Welcome Message
//	    String port=BBConfiguration.configuration.getString("http.port");
//	    if (port==null) port="9000";
//	    String address=Play.application().configuration().getString("http.address");
//	    if (address==null) address="localhost";
//
//	    //write the Welcome Message
//	    logger.info("");
//	    logger.info("To login into the amministration console go to http://" + address +":" + port + "/console");
//	    logger.info("Default credentials are: user:admin pass:admin AppCode: " + BBConfiguration.getAPPCODE());
//	    logger.info("Documentation is available at http://www.baasbox.com/documentation");
//		logger.debug("Global.onStart() ended");
//	    logger.info("BaasBox is Ready.");
	  }

//	private void overrideSettings() {
//		logger.info("Override settings...");
//    	//takes only the settings that begin with baasbox.settings
//    	Configuration bbSettingsToOverride=BBConfiguration.configuration.getConfig("baasbox.settings");
//    	//if there is at least one of them
//    	if (bbSettingsToOverride!=null) {
//    		//takes the part after the "baasbox.settings" of the key names
//    		Set<String> keys = bbSettingsToOverride.keys();
//    		Iterator<String> keysIt = keys.iterator();
//    		//for each setting to override
//    		while (keysIt.hasNext()){
//    			String key = keysIt.next();
//    			//is it a value to override?
//    			if (key.endsWith(".value")){
//    				//sets the overridden value
//    				String value = "";
//    				try {
//     					value = bbSettingsToOverride.getString(key);
//    					key = key.substring(0, key.lastIndexOf(".value"));
//						PropertiesConfigurationHelper.override(key,value);
//					} catch (Exception e) {
//						logger.error ("Error overriding the setting " + key + " with the value " + value.toString() + ": " +e.getMessage());
//					}
//    			}else if (key.endsWith(".visible")){ //or maybe we have to hide it when a REST API is called
//    				//sets the visibility
//    				Boolean value;
//    				try {
//     					value = bbSettingsToOverride.getBoolean(key);
//    					key = key.substring(0, key.lastIndexOf(".visible"));
//						PropertiesConfigurationHelper.setVisible(key,value);
//					} catch (Exception e) {
//						logger.error ("Error overriding the visible attribute for setting " + key + ": " +e.getMessage());
//					}
//    			}else if (key.endsWith(".editable")){ //or maybe we have to
//    				//sets the possibility to edit the value via REST API by the admin
//    				Boolean value;
//    				try {
//     					value = bbSettingsToOverride.getBoolean(key);
//    					key = key.substring(0, key.lastIndexOf(".editable"));
//						PropertiesConfigurationHelper.setEditable(key,value);
//					} catch (Exception e) {
//						logger.error ("Error overriding the editable attribute setting " + key + ": " +e.getMessage());
//					}
//    			}else {
//    				logger.error("The configuration key: " + key + " is invalid. value, visible or editable are missing");
//    			}
//    			key.subSequence(0, key.lastIndexOf("."));
//    		}
//    	}else logger.info ("...No setting to override...");
//    	logger.info ("...done");
//	}
//
//
//
//	  @Override
	  public void onStop() {
		logger.debug("Global.onStop() called");
	    logger.info("BaasBox is shutting down...");
	    try{

            OrientGraph db = null;
            try{
                if(DbHelper.getConnection() == null || DbHelper.getConnection().isClosed()){
                    DbHelper.open(BBConfiguration.getAPPCODE(), "admin", "admin");
                }
                db = new OrientGraph(DbHelper.getODatabaseDocumentTxConnection());
                if (db.getRawGraph().exists()) {
                    logger.info("DB exists, Dropping it");
                    db.getRawGraph().drop();
                }
            } catch (Throwable e) {
                logger.error("!! Error initializing BaasBox!", e);
                logger.error(ExceptionUtils.getFullStackTrace(e));
                throw e;
            } finally {
                if (db!=null && !db.getRawGraph().isClosed()) db.getRawGraph().close();
            }

	    	logger.info("Closing the DB connections...");
	    	ODatabaseDocumentPool.global().close();
	    	logger.info("Shutting down embedded OrientDB Server");
	    	Orient.instance().shutdown();
	    	logger.info("...ok");
	    }catch (ODatabaseException e){
	    	logger.error("Error closing the DB!",e);
	    }catch (Throwable e){
	    	logger.error("!! Error shutting down BaasBox!", e);
	    }
	    logger.info("Destroying session manager...");
//	    SessionTokenProvider.destroySessionTokenProvider();
	    logger.info("...BaasBox has stopped");
		logger.debug("Global.onStop() ended");
	  }

//	private void setCallIdOnResult(RequestHeader request, ObjectNode result) {
//		String callId = request.getQueryString("call_id");
//		if (!StringUtils.isEmpty(callId)) result.put("call_id",callId);
//	}
//
//	private ObjectNode prepareError(RequestHeader request, String error) {
//		ObjectNode result = Json.newObject();
//		ObjectMapper mapper = new ObjectMapper();
//			result.put("result", "error");
//			result.put("message", error);
//			result.put("resource", request.path());
//			result.put("method", request.method());
//			result.put("request_header", mapper.valueToTree(request.headers()));
//			result.put("API_version", BBConfiguration.configuration.getString(BBConfiguration.API_VERSION));
//			setCallIdOnResult(request, result);
//		return result;
//	}
//
//	  @Override
//	  public Result onBadRequest(RequestHeader request, String error) {
//		  ObjectNode result = prepareError(request, error);
//		  result.put("http_code", 400);
//		  Result resultToReturn =  badRequest(result);
//		  try {
//			if (logger.isDebugEnabled()) logger.debug("Global.onBadRequest:\n  + result: \n" + result.toString() + "\n  --> Body:\n" + new String(JavaResultExtractor.getBody(resultToReturn),"UTF-8"));
//		  }finally{
//			  return resultToReturn;
//		  }
//	  }
//
//	// 404
//	  @Override
//	    public Result onHandlerNotFound(RequestHeader request) {
//		  logger.debug("API not found: " + request.method() + " " + request);
//		  ObjectNode result = prepareError(request, "API not found");
//		  result.put("http_code", 404);
//		  Result resultToReturn= notFound(result);
//		  try {
//			  if (logger.isDebugEnabled()) logger.debug("Global.onBadRequest:\n  + result: \n" + result.toString() + "\n  --> Body:\n" + new String(JavaResultExtractor.getBody(resultToReturn),"UTF-8"));
//		  }finally{
//			  return resultToReturn;
//		  }
//	    }
//
//	  // 500 - internal server error
//	  @Override
//	  public Result onError(RequestHeader request, Throwable throwable) {
//		  logger.error("INTERNAL SERVER ERROR: " + request.method() + " " + request);
//		  ObjectNode result = prepareError(request, throwable.getMessage());
//		  result.put("http_code", 500);
//		  result.put("stacktrace", ExceptionUtils.getFullStackTrace(throwable));
//		  logger.error(ExceptionUtils.getFullStackTrace(throwable));
//		  Result resultToReturn= internalServerError(result);
//		  try {
//			  if (logger.isDebugEnabled()) logger.debug("Global.onBadRequest:\n  + result: \n" + result.toString() + "\n  --> Body:\n" + new String(JavaResultExtractor.getBody(resultToReturn),"UTF-8"));
//		  } finally{
//			  return resultToReturn;
//		  }
//	  }
//
//
//	@Override
//	public <T extends EssentialFilter> Class<T>[] filters() {
//
//		return new Class[]{com.shoppingbox.filters.LoggingFilter.class};
//	}
}
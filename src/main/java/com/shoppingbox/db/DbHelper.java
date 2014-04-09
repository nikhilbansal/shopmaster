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
package com.shoppingbox.db;


import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.shoppingbox.BBConfiguration;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.hook.HooksManager;
import com.shoppingbox.exception.*;
import com.shoppingbox.service.user.RoleService;
import com.shoppingbox.service.user.UserService;
import com.shoppingbox.util.QueryParams;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class DbHelper {

    final static Logger logger = LoggerFactory.getLogger(DbHelper.class);

	private static final String SCRIPT_FILE_NAME="db.sql";
	private static final String CONFIGURATION_FILE_NAME="configuration.conf";

	private static ThreadLocal<Boolean> dbFreeze = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {return Boolean.FALSE;};
	};

	private static ThreadLocal<String> appcode = new ThreadLocal<String>() {
		protected String initialValue() {return "";};
	};

	private static ThreadLocal<String> username = new ThreadLocal<String>() {
		protected String initialValue() {return "";};
	};
	
	private static ThreadLocal<String> password = new ThreadLocal<String>() {
		protected String initialValue() {return "";};
	};
	
	private static final String fetchPlan = "*:?";

	public static String currentUsername(){
		return username.get();
	}
	
	public static boolean isInTransaction(){
		 ODatabaseRecordTx db = getConnection();
		return !(db.getTransaction() instanceof OTransactionNoTx);
	}

	public static void requestTransaction(){
		ODatabaseRecordTx db = getConnection();
		if (!isInTransaction()){
			if (logger.isTraceEnabled()) logger.trace("Begin transaction");
			//db.begin();
		}
	}

	public static void commitTransaction(){
		ODatabaseRecordTx db = getConnection();
		if (isInTransaction()){
			if (logger.isTraceEnabled()) logger.trace("Commit transaction");
			//db.commit();
		}
	}

	public static void rollbackTransaction(){
		ODatabaseRecordTx db = getConnection();
		if (isInTransaction()){
			if (logger.isTraceEnabled()) logger.trace("Rollback transaction");
			//db.rollback();
		}		
	}

	public static String selectQueryBuilder (String from, boolean count, QueryParams criteria){
		String ret;
		if (count) ret = "select count(*) from ";
		else ret = "select " + criteria.getFields() + " from ";
		ret += from;
		if (criteria.getWhere()!=null && !criteria.getWhere().equals("")){
			ret += " where ( " + criteria.getWhere() + " )";
		}
		if (!StringUtils.isEmpty(criteria.getGroupBy())){
			ret += " group by ( " + criteria.getGroupBy() + " )";
		}
		if (!count && criteria.getOrderBy()!=null && !criteria.getOrderBy().equals("")){
			ret += " order by " + criteria.getOrderBy();
		}
		if (!count && (criteria.getPage()!=null && criteria.getPage()!=-1)){
			ret += " skip " + (criteria.getPage() * criteria.getRecordPerPage()) +
					" limit " + 	criteria.getRecordPerPage();
		}

		if (logger.isDebugEnabled()) logger.debug("queryBuilder: " + ret);
		return ret;
	}

	/***
	 * Prepares a select statement
	 * @param from the class to query
	 * @param count if true, perform a count instead of to retrieve the records
	 * @param criteria the criteria to apply in the 'where' clause of the select
	 * @return an OCommandRequest object ready to be passed to the {@link #(OCommandRequest, String[])} method
	 * @throws SqlInjectionException If the query is not a select statement
	 */
	public static OCommandRequest selectCommandBuilder(String from, boolean count, QueryParams criteria) throws SqlInjectionException{
		ODatabaseRecordTx db =  DbHelper.getConnection();
		OCommandRequest command = db.command(new OSQLSynchQuery<ODocument>(
				selectQueryBuilder(from, count, criteria)
				));
		if (!command.isIdempotent()) throw new SqlInjectionException();
		if (logger.isDebugEnabled()) logger.debug("commandBuilder: ");
		if (logger.isDebugEnabled()) logger.debug("  " + criteria.toString());
		if (logger.isDebugEnabled()) logger.debug("  " + command.toString());
		return command;
	}

	/***
	 * Executes a select eventually passing the parameters 
	 * @param command
	 * @param params positional parameters
	 * @return the List of the record retrieved (the command MUST be a select)
	 */
	public static List<ODocument> selectCommandExecute(OCommandRequest command, Object[] params){
		List<ODocument> queryResult = command.execute((Object[])params);
		return queryResult;
	}
	public static Integer sqlCommandExecute(OCommandRequest command, Object[] params){
		Integer updateQueryResult = command.execute((Object[])params);
		return updateQueryResult;
	}
	public static List<ODocument> commandExecute(OCommandRequest command, Object[] params){
          List<ODocument> queryResult = command.execute((Object[])params);
          return queryResult;
	}
	
	/**
	 * Prepares the command API to execute an arbitrary SQL statement
	 * @param theQuery
	 * @return
	 */
	public static OCommandRequest genericSQLStatementCommandBuilder (String theQuery){
		ODatabaseRecordTx db =  DbHelper.getConnection();
		OCommandRequest command = db.command(new OCommandSQL(theQuery));
		return command;
	}
	
	/***
	 * Executes a generic SQL command statements
	 * @param command the command to execute prepared by {@link #genericSQLStatementCommandBuilder(String)}
	 * @param params The positional parameters to pass to the statement
	 * @return
	 */
	public static Object genericSQLCommandExecute(OCommandRequest command, Object[] params){
		Object queryResult = command.execute((Object[])params);
		return queryResult;
	}
	
	/**
	 * Executes an arbitrary sql statement applying the positional parameters
	 * @param statement
	 * @param params
	 * @return
	 */
	public static Object genericSQLStatementExecute(String statement, Object[] params){
		OCommandRequest command = genericSQLStatementCommandBuilder(statement);
		Object ret = genericSQLCommandExecute(command,params);
		return ret;
	}
	
	public static void shutdownDB(boolean repopulate){
		ODatabaseRecordTx db = null;

		try{
			//WE GET THE CONNECTION BEFORE SETTING THE SEMAPHORE
			db = getConnection();

			synchronized(DbHelper.class)  {
				if(!dbFreeze.get()){
					dbFreeze.set(true);
				}
				db.drop();
				db.close();
				db.create();
				db.getLevel1Cache().clear();
				db.getLevel2Cache().clear();
				db.reload();
				db.getMetadata().reload();
				if(repopulate){
					HooksManager.registerAll(db);
					setupDb(db);
				}


			}

		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			synchronized(DbHelper.class)  {

				dbFreeze.set(false);

			}
		}

	}

	public static ODatabaseRecordTx getOrOpenConnection(String appcode, String username,String password) throws InvalidAppCodeException {
		ODatabaseRecordTx db= getConnection();
		if (db==null || db.isClosed()) db = open ( appcode,  username, password) ;
		return db;
	}
	
	public static ODatabaseRecordTx open(String appcode, String username,String password) throws InvalidAppCodeException {
		
		if (appcode==null || !appcode.equals(BBConfiguration.configuration.getString(BBConfiguration.APP_CODE)))
			throw new InvalidAppCodeException("Authentication info not valid or not provided: " + appcode + " is an Invalid App Code");
		if(dbFreeze.get()){
			throw new ShuttingDownDBException();
		}
		String databaseName=BBConfiguration.getDBDir();
		if (logger.isDebugEnabled()) logger.debug("opening connection on db: " + databaseName + " for " + username);
		
		new ODatabaseDocumentTx("plocal:" + BBConfiguration.getDBDir()).open(username,password);
		HooksManager.registerAll(getConnection());
		
		DbHelper.appcode.set(appcode);
		DbHelper.username.set(username);
		DbHelper.password.set(password);
		
		return getConnection();
	}

	public static ODatabaseRecordTx reconnectAsAdmin (){
		getConnection().close();
		try {
			return open (appcode.get(),BBConfiguration.getBaasBoxAdminUsername(),BBConfiguration.getBaasBoxAdminPassword());
		} catch (InvalidAppCodeException e) {
			throw new RuntimeException(e);
		}
	}

	public static ODatabaseRecordTx reconnectAsAuthenticatedUser (){
		getConnection().close();
		try {
			return open (appcode.get(),getCurrentHTTPUsername(),getCurrentHTTPPassword());
		} catch (InvalidAppCodeException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void close(ODatabaseRecordTx db) {
		if (logger.isDebugEnabled()) logger.debug("closing connection");
		if (db!=null && !db.isClosed()){
			//HooksManager.unregisteredAll(db);
			db.close();
		}else if (logger.isDebugEnabled()) logger.debug("connection already close or null");
	}

	public static ODatabaseRecordTx getConnection(){
		ODatabaseRecordTx db = null;
		try {
			db=(ODatabaseRecordTx)ODatabaseRecordThreadLocal.INSTANCE.get();
		}catch (ODatabaseException e){
			//swallow...
		}
		return db;
	}

	
	public static String getCurrentHTTPPassword(){
		//return (String) Http.Context.current().args.get("password");
        return null;
	}

	public static String getCurrentHTTPUsername(){
		//return (String) Http.Context.current().args.get("username");
        return null;
	}

	public static String getCurrentUserNameFromConnection(){
		return getConnection().getUser().getName();
	}

	public static boolean isConnectedLikeBaasBox(){
		return getCurrentHTTPUsername().equalsIgnoreCase(BBConfiguration.getBaasBoxUsername());
	}

	public static void createDefaultRoles() throws RoleNotFoundException, RoleAlreadyExistsException{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		RoleService.createInternalRoles();
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}

	public static void createDefaultUsers() throws Exception{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		UserService.createDefaultUsers();
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}

	
	public static void updateDefaultUsers() throws Exception{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		ODatabaseRecordTx db = DbHelper.getConnection();
		OUser user=db.getMetadata().getSecurity().getUser(BBConfiguration.getBaasBoxUsername());
		user.setPassword(BBConfiguration.getBaasBoxPassword());
		user.save();

		user=db.getMetadata().getSecurity().getUser(BBConfiguration.getBaasBoxAdminUsername());
		user.setPassword(BBConfiguration.getBaasBoxAdminPassword());
		user.save();

		if (logger.isTraceEnabled()) logger.trace("Method End");
	}

	@Deprecated
	public static void dropOrientDefault(){
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		//nothing to do here
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}

	public static void populateDB(ODatabaseRecordTx db) throws IOException{
        logger.info("Populating the db...");
//		InputStream is;
//		if (Play.application().isProd()) is	=Play.application().resourceAsStream(SCRIPT_FILE_NAME);
//		else is = new FileInputStream(Play.application().getFile("conf/"+SCRIPT_FILE_NAME));
        InputStream is = DbHelper.class.getClassLoader().getResourceAsStream(SCRIPT_FILE_NAME);
		List<String> script=IOUtils.readLines(is, "UTF-8");
		is.close();

		for (String line:script){
			if (logger.isDebugEnabled()) logger.debug(line);
			if (!line.startsWith("--") && !line.trim().isEmpty()){ //skip comments
				db.command(new OCommandSQL(line.replace(';', ' '))).execute();
			}
		}
	}

	public static void setupDb(ODatabaseRecordTx db) throws Exception{
		logger.info("Creating default roles...");
		DbHelper.createDefaultRoles();
		logger.info("Creating default users...");
		DbHelper.dropOrientDefault();
		populateDB(db);
		createDefaultUsers();
	}

	public static void exportData(String appcode,OutputStream os) throws UnableToExportDbException{
		ODatabaseRecordTx db = null;
		try{
            logger.info("open");
            //db = open(appcode, BBConfiguration.getBaasBoxAdminUsername(), BBConfiguration.getBaasBoxAdminPassword());
            db = DbHelper.getConnection();
            logger.info("after open");
            ODatabaseExport oe = new ODatabaseExport(db, os, new OCommandOutputListener() {
				@Override
				public void onMessage(String m) {
					logger.info(m);
				}
			});
            logger.info("Okay Before frezing");
			synchronized(DbHelper.class)  {
				if(!dbFreeze.get()){
					dbFreeze.set(true);
				}
			}
            logger.info("Okay Exporting");
			oe.setUseLineFeedForRecords(true);
			oe.setIncludeManualIndexes(true);
            logger.info("BEFORE Exporting");
            oe.exportDatabase();
            logger.info("AFTER Exporting");
			oe.close();
		}catch(Exception ioe){
			throw new UnableToExportDbException(ioe);
		}finally{
			if(db!=null && ! db.isClosed()){
				db.close();
			}
			dbFreeze.set(false);
		}
	}
	
	public static void importData(String appcode,String importData) throws UnableToImportDbException{
		ODatabaseRecordTx db = null;
		java.io.File f = null;
		try{
			logger.info("Initializing restore operation..:");
			logger.info("...dropping the old db..:");
			DbHelper.shutdownDB(false);
			f = java.io.File.createTempFile("import", ".json");
			FileUtils.writeStringToFile(f, importData);
			synchronized(DbHelper.class)  {
				if(!dbFreeze.get()){
					dbFreeze.set(true);
				}
			}

			db=getConnection(); 
			logger.info("...unregistering hooks...");
			HooksManager.unregisteredAll(db);
			logger.info("...drop the O-Classes...");
			db.getMetadata().getSchema().dropClass("OFunction");
			 db.getMetadata().getSchema().dropClass("OSchedule");
			 db.getMetadata().getSchema().dropClass("ORIDs");
			   ODatabaseDocumentTx dbd = new ODatabaseDocumentTx(db);
			ODatabaseImport oi = new ODatabaseImport(dbd, f.getAbsolutePath(), new OCommandOutputListener() {
				@Override
				public void onMessage(String m) {
					logger.info("Restore db: " + m);
				}
			});
			
			 oi.setIncludeManualIndexes(true);
			 oi.setUseLineFeedForRecords(true);
			 oi.setPreserveClusterIDs(true);
			 oi.setPreserveRids(true);
			 logger.info("...starting import procedure...");
			 oi.importDatabase();
			 oi.close();
			
			 logger.info("...setting up internal user credential...");
			 updateDefaultUsers();
			 logger.info("...registering hooks...");
			 HooksManager.registerAll(db);
		}catch(Exception ioe){
			logger.error("*** Error importing the db: ", ioe);
			throw new UnableToImportDbException(ioe);
		}finally{
			if(db!=null && ! db.isClosed()){
				db.close();
			}
			logger.info("...releasing the db...");
			dbFreeze.set(false);
			if(f!=null && f.exists()){
				f.delete();
			}
			logger.info("...restore terminated");
		}
	}

	public static OrientGraphNoTx getOGraphDatabaseConnection(){
		return new OrientGraphNoTx(getODatabaseDocumentTxConnection());
	}
	
	public static ODatabaseDocumentTx getODatabaseDocumentTxConnection(){
		return new ODatabaseDocumentTx(getConnection());
	}
	
}

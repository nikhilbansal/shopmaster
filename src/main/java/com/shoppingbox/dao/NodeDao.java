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
package com.shoppingbox.dao;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OQueryParsingException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.OSQLHelper;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.shoppingbox.dao.exception.*;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.enumerations.Permissions;
import com.shoppingbox.service.storage.BaasBoxPrivateFields;
import com.shoppingbox.util.QueryParams;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public abstract class NodeDao  {

    final static Logger logger = LoggerFactory.getLogger(NodeDao.class);

	public final String MODEL_NAME;
	public static final String CLASS_NODE_NAME = "_BB_Node";
	public static final String CLASS_VERTEX_NAME = "_BB_NodeVertex";
	
	public static final String FIELD_LINK_TO_VERTEX = "_links";
	public static final String FIELD_TO_DOCUMENT_FIELD = "_node";
	public static final String FIELD_CREATION_DATE = "_creation_date";
	
	public static final String EDGE_CLASS_CREATED = "Created";
	

	protected ODatabaseRecordTx db;

	 
	public NodeDao(String modelName) {
		super();
		this.MODEL_NAME=modelName;
		this.db=DbHelper.getConnection();
	}

	
	protected static HashMap<String,Object> backupBaasBoxFields(ODocument document){
		HashMap<String,Object> map = new HashMap<String,Object>();
		for (BaasBoxPrivateFields r : BaasBoxPrivateFields.values()){
			map.put(r.toString(), document.field(r.toString()));
		}
		return map;
	}
	
	protected static ODocument restoreBaasBoxFields(ODocument document, HashMap<String,Object> map){
		for (BaasBoxPrivateFields r : BaasBoxPrivateFields.values()){
			document.fields(r.toString(),map.get(r.toString()));
		}
		return document;
	}
	

	public void checkModelDocument(ODocument doc) throws InvalidModelException {
		if (doc!=null && !doc.getClassName().equalsIgnoreCase(this.MODEL_NAME))
			throw new InvalidModelException();
	}

	public Integer updateByQuery(String query) throws InvalidCriteriaException{
		OCommandRequest command = db.command(new OCommandSQL(
				query
				));
		Integer records=null;
		try{
			records=DbHelper.sqlCommandExecute(command, null);
		}catch (OQueryParsingException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}
		return records;
	}
	
	public List<ODocument> selectByQuery(String query) throws InvalidCriteriaException{
		List<ODocument> list=null;
		try{
			list = DbHelper.commandExecute(new OSQLSynchQuery<ODocument>(
				query
				), null);
		}catch (OQueryParsingException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}	
		return list;
	}
	
	public ODocument create() throws Throwable {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
        OrientGraphNoTx db = DbHelper.getOGraphDatabaseConnection();
		try{
				ODocument doc = new ODocument(this.MODEL_NAME);
				Vertex vertex = db.addVertex(CLASS_VERTEX_NAME);
				doc.field(FIELD_LINK_TO_VERTEX,vertex);
				doc.field(FIELD_CREATION_DATE,new Date());
				vertex.setProperty(FIELD_TO_DOCUMENT_FIELD,doc);
				UUID token = UUID.randomUUID();
				if (logger.isDebugEnabled()) logger.debug("CreateUUID.onRecordBeforeCreate: " + doc.getIdentity() + " -->> " + token.toString());
				doc.field(BaasBoxPrivateFields.ID.toString(),token.toString());
				doc.field(BaasBoxPrivateFields.AUTHOR.toString(),db.getRawGraph().getUser().getName());
			return doc;
		}catch (Throwable e){
			throw e;
		}finally{
			if (logger.isTraceEnabled()) logger.trace("Method End");
		}
	}
	


	protected  void save(ODocument document) throws InvalidModelException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		checkModelDocument(document);
		document.save();
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}
	

	public void update(ODocument originalDocument, ODocument documentToMerge) throws UpdateOldVersionException  {
		if (documentToMerge.getVersion()!=0 && documentToMerge.getVersion()!=originalDocument.getVersion()) throw new UpdateOldVersionException("The document to merge is older than the stored one v" +documentToMerge.getVersion() + " vs v"+documentToMerge.getVersion(),documentToMerge.getVersion(), originalDocument.getVersion());
		//backup the baasbox's fields 
		HashMap<String,Object> map = backupBaasBoxFields(originalDocument);
		//update the document
		originalDocument.merge(documentToMerge, false, false);
		//restore the baasbox's fields
		restoreBaasBoxFields(originalDocument, map);
		originalDocument.save();
	}
	


	public List<ODocument> get(QueryParams criteria) throws SqlInjectionException, InvalidCriteriaException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		List<ODocument> result = null;
		OCommandRequest command = DbHelper.selectCommandBuilder(MODEL_NAME, false, criteria);
		try{
			result = DbHelper.selectCommandExecute(command, criteria.getParams());
		}catch (OCommandExecutionException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
			
		}catch (OQueryParsingException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}catch (StringIndexOutOfBoundsException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check your query, the syntax and the parameters",e);
		}catch (IndexOutOfBoundsException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check your query, the syntax and the parameters",e);
		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return result;
	}


	public ODocument get(ORID rid) throws InvalidModelException, DocumentNotFoundException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		Object doc=db.load(rid);
		if (doc==null) throw new DocumentNotFoundException();
		if (!(doc instanceof ODocument)) throw new IllegalArgumentException(rid +" is a rid not referencing a valid Document");
		try{
			checkModelDocument((ODocument)doc);
		}catch(InvalidModelException e){
			//the rid may reference a ORecordBytes which is not a ODocument
			throw new InvalidModelException("the rid " + rid + " is not valid belong to the collection " + this.MODEL_NAME);
		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return (ODocument)doc;
	}


	public ODocument get(String rid) throws InvalidModelException, ODatabaseException, DocumentNotFoundException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		Object orid=OSQLHelper.parseValue(rid, null);
		if ((orid==null) || !(orid instanceof ORecordId) || (orid.toString().equals(OSQLHelper.VALUE_NOT_PARSED))) throw new IllegalArgumentException(rid +" is not a valid rid");
		Object odoc=get((ORecordId)orid);
		
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return (ODocument)odoc;
	}
 

	public boolean exists(ODocument document) throws InvalidModelException, DocumentNotFoundException {
		return exists(document.getRecord().getIdentity());
	}


	public boolean exists(ORID rid) throws InvalidModelException, DocumentNotFoundException {
		ODocument doc = get(rid);
		return (doc!=null);
	}


	public boolean exists(String rid) throws InvalidModelException, ODatabaseException, DocumentNotFoundException {
		ODocument doc = get(rid);
		return (doc!=null);
	}

	public ODocument revokePermission(ODocument document, Permissions permission, OUser user) {
		return PermissionsHelper.revoke(document, permission, user); 
	}

	public ODocument revokePermission(ODocument document, Permissions permission, ORole role) {
		return PermissionsHelper.revoke(document, permission, role);
	}
	
	public ODocument grantPermission(ODocument document, Permissions permission, OUser user) {
		return PermissionsHelper.grant(document, permission, user);
		 
	}

	public ODocument grantPermission(ODocument document, Permissions permission, ORole role) {
		return PermissionsHelper.grant(document, permission, role);
	}
	
	public ODocument grantPermission(ODocument document, Permissions permission, ORole[] roles) {
		for (ORole role : roles){
			grantPermission(document, permission, role);
		}
		return document;
	}

	
	public long getCount(){
		return DbHelper.getODatabaseDocumentTxConnection().countClass(MODEL_NAME);
	}
	
	public long getCount(QueryParams criteria) throws SqlInjectionException{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		List<ODocument> result = null;
		OCommandRequest command = DbHelper.selectCommandBuilder(MODEL_NAME, true, criteria);
		try{
			result = DbHelper.selectCommandExecute(command, criteria.getParams());
		}catch (OCommandExecutionException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
			
		}catch (OQueryParsingException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return ((Long)result.get(0).field("count")).longValue();
	}
	
	
	public void delete(String rid) throws Throwable{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		ODocument doc = get(rid);
		delete(doc.getIdentity());
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}
	
	public void delete(ORID rid) throws Throwable{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
//		OGraphDatabase db = DbHelper.getOGraphDatabaseConnection();
//		//retrieve the vertex associated to this node
//		try{
//			DbHelper.requestTransaction();
//			ODocument vertex = (ODocument)((ODocument)db.load(rid)).field(FIELD_LINK_TO_VERTEX);
//			db.removeVertex(vertex);
//			db.delete(rid);
//			DbHelper.commitTransaction();
//		}catch (Throwable e){
//			DbHelper.rollbackTransaction();
//			throw e;
//		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}
	
	
}
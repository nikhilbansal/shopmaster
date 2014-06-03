package com.shoppingbox.dao;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OQueryParsingException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.OSQLHelper;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.shoppingbox.dao.exception.*;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.models.Identifier;
import com.shoppingbox.models.Identifiers;
import com.shoppingbox.models.Pair;
import com.shoppingbox.util.IdentifiersGlobal;
import com.shoppingbox.util.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class NodeDao{

    final static Logger logger = LoggerFactory.getLogger(NodeDao.class);

    public final String CLASS_NAME;

	public ODatabaseDocument db;

	public NodeDao(String className) {
		this.CLASS_NAME = className;
		this.db = DbHelper.getConnection();
	}

	public void checkModelDocument(ODocument doc) throws InvalidClassException {
		if (doc!=null && !doc.getClassName().equalsIgnoreCase(this.CLASS_NAME))
			throw new InvalidClassException();
	}

	public Integer executeQuery(String query) throws InvalidCriteriaException{
		Integer records=null;
		try{
			records=DbHelper.sqlCommandExecute(DbHelper.genericSQLStatementCommandBuilder(query), null);
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
			list = DbHelper.selectCommandExecute(new OSQLSynchQuery<ODocument>(query), null);
		}catch (OQueryParsingException e ){
			throw new InvalidCriteriaException("Invalid criteria. Please check if your querystring is encoded in a corrected way. Double check the single-quote and the quote characters",e);
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}	
		return list;
	}
	
	public ODocument create() throws Throwable {
//		if (logger.isTraceEnabled()) logger.trace("Method Start");
//        OrientGraphNoTx db = DbHelper.getOGraphDatabaseConnection();
//		try{
//				ODocument doc = new ODocument(this.CLASS_NAME);
//				Vertex vertex = db.addVertex(CLASS_VERTEX_NAME);
//				doc.field(FIELD_LINK_TO_VERTEX,vertex);
//				doc.field(FIELD_CREATION_DATE,new Date());
//				vertex.setProperty(FIELD_TO_DOCUMENT_FIELD,doc);
//				UUID token = UUID.randomUUID();
//				if (logger.isDebugEnabled()) logger.debug("CreateUUID.onRecordBeforeCreate: " + doc.getIdentity() + " -->> " + token.toString());
//				doc.field(BaasBoxPrivateFields.ID.toString(),token.toString());
//				doc.field(BaasBoxPrivateFields.AUTHOR.toString(),db.getRawGraph().getUser().getName());
//			return doc;
//		}catch (Throwable e){
//			throw e;
//		}finally{
//			if (logger.isTraceEnabled()) logger.trace("Method End");
//		}
        return null;
	}

	public ODocument save(ODocument document) throws InvalidClassException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		checkModelDocument(document);
		ODocument savedODocument = document.save();
		if (logger.isTraceEnabled()) logger.trace("Method End");
        return savedODocument;
	}

	public void update(ODocument originalDocument, ODocument documentToMerge) throws UpdateOldVersionException  {
		if (documentToMerge.getVersion()!=0 && documentToMerge.getVersion()!=originalDocument.getVersion()) throw new UpdateOldVersionException("The document to merge is older than the stored one v" +documentToMerge.getVersion() + " vs v"+documentToMerge.getVersion(),documentToMerge.getVersion(), originalDocument.getVersion());
		originalDocument.merge(documentToMerge, true, false);
		originalDocument.save();
	}

	public List<ODocument> get(QueryParams criteria) throws SqlInjectionException, InvalidCriteriaException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		List<ODocument> result = null;
		OCommandRequest command = DbHelper.selectCommandBuilder(this.CLASS_NAME, false, criteria);
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

	public ODocument get(ORID rid) throws InvalidClassException, DocumentNotFoundException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		Object doc=db.load(rid);
		if (doc==null) throw new DocumentNotFoundException();
		if (!(doc instanceof ODocument)) throw new IllegalArgumentException(rid +" is a rid not referencing a valid Document");
		try{
			checkModelDocument((ODocument)doc);
		}catch(InvalidClassException e){
			throw new InvalidClassException("the rid " + rid + " is not valid belong to the class");
		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return (ODocument)doc;
	}

	public ODocument get(String rid) throws InvalidClassException, ODatabaseException, DocumentNotFoundException {
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		Object orid=OSQLHelper.parseValue(rid, null);
		if ((orid==null) || !(orid instanceof ORecordId) || (orid.toString().equals(OSQLHelper.VALUE_NOT_PARSED))) throw new IllegalArgumentException(rid +" is not a valid rid");
		Object odoc=get((ORecordId)orid);
		
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return (ODocument)odoc;
	}

	public boolean exists(ODocument document) throws InvalidClassException, DocumentNotFoundException {
		return exists(document.getRecord().getIdentity());
	}

	public boolean exists(ORID rid) throws InvalidClassException, DocumentNotFoundException {
		ODocument doc = get(rid);
		return (doc!=null);
	}

	public boolean exists(String rid) throws InvalidClassException, ODatabaseException, DocumentNotFoundException {
		ODocument doc = get(rid);
		return (doc!=null);
	}

	public long getCount(){
		return DbHelper.getODatabaseDocumentTxConnection().countClass(this.CLASS_NAME);
	}
	
	public long getCount(QueryParams criteria) throws SqlInjectionException{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		List<ODocument> result = null;
		OCommandRequest command = DbHelper.selectCommandBuilder("some class name", true, criteria);
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
		try{
			DbHelper.requestTransaction();
			db.delete(rid);
			DbHelper.commitTransaction();
		}catch (Throwable e){
			DbHelper.rollbackTransaction();
			throw e;
		}
		if (logger.isTraceEnabled()) logger.trace("Method End");
	}

    public Identifiers getIdentifiers(ODocument doc) {
        Map<String, List<IdentifiersGlobal.IdentifierCsv>> identifierMap = IdentifiersGlobal.INSTANCE.idenfifierMap;
        if(identifierMap == null){
            logger.warn("identifierMap is null, trying to reinitialize");
            IdentifiersGlobal.INSTANCE.initialize();
            identifierMap = IdentifiersGlobal.INSTANCE.idenfifierMap;
            if(identifierMap == null){
                logger.error("identifierMap is null even after re initialization");
                throw new IllegalStateException("identifierMap is null even after re initialization");
            }
        }
        if(!identifierMap.containsKey(this.CLASS_NAME)) return null;
        List<IdentifiersGlobal.IdentifierCsv> identifiersGlobalList = identifierMap.get(this.CLASS_NAME);
        Identifiers identifiers = new Identifiers();
        for(IdentifiersGlobal.IdentifierCsv identifierCsv : identifiersGlobalList){
            Identifier identifier = new Identifier();
            for(String eachId : identifierCsv.getIdentifier()){
                Object eachIdValue = doc.field(eachId);
                if(eachIdValue == null) throw new IllegalArgumentException(String.format("Doc %s doesn't contain id attribute %s", doc, eachId));
                identifier.getIdentifierPairs().add(new Pair(eachId, eachIdValue));
            }
            if(!identifier.getIdentifierPairs().isEmpty()) identifiers.getIdentifiers().add(identifier);
        }
        return identifiers;
    }

}
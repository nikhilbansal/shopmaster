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

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.shoppingbox.dao.exception.InvalidCriteriaException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.util.QueryParams;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.OSQLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;


public class GenericDao {

    final static Logger logger = LoggerFactory.getLogger(GenericDao.class);
	public static GenericDao getInstance(){
		return new GenericDao();
	}
	
	private GenericDao(){}
	
	public ODocument get(String rid) throws IllegalArgumentException{
		Object orid=OSQLHelper.parseValue(rid, null);
		if (!(orid instanceof ORecordId)) throw new IllegalArgumentException(rid +" is not a valid rid");
		ODocument doc=get((ORecordId)orid);
		return doc;
	}
	
	public ODocument get(ORID rid) {
        ODatabaseDocument db =DbHelper.getConnection();
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		ODocument doc=db.load(rid);
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return doc;
	}
	
	public ORID getRidByUUID(UUID id){
		return getRidByUUID(id.toString());
	}
	
	public ORID getRidByUUID(String id){
        ODatabaseDocument db =DbHelper.getConnection();
		OIndex<?> index = db.getMetadata().getIndexManager().getIndex("_BB_Node.id");
		ORID rid = (ORID) index.get(id);  
		return rid;
	}
	
	public List<ODocument>executeQuery(String oclass, QueryParams criteria) throws SqlInjectionException{
		OCommandRequest command = DbHelper.selectCommandBuilder(oclass, false, criteria);
		List<ODocument> result = null;
		try{
			result = DbHelper.selectCommandExecute(command, criteria.getParams());
		}catch (OCommandSQLParsingException e){
			throw new InvalidCriteriaException("Invalid criteria. Please check the syntax of you 'where' and/or 'orderBy' clauses. Hint: if you used < or > operators, put spaces before and after them",e);
		}
		return result;
	}
	


	public void executeCommand(String commandString, Object[] params) {
        ODatabaseDocument db =  DbHelper.getConnection();
		OCommandRequest command=db.command(new OCommandSQL(commandString));
		command.execute(params);
	}
	
}

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

import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.dao.exception.InvalidCollectionException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.DbHelper;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentDao extends NodeDao {

    final static Logger logger = LoggerFactory.getLogger(DocumentDao.class);
	
	protected CollectionDao collDao;
	
	protected DocumentDao(String collectionName) throws InvalidCollectionException {
		super(collectionName);
		collDao = CollectionDao.getInstance();
		try {
			if (!collDao.existsCollection(collectionName)) throw new InvalidCollectionException("The collection " + collectionName + " does not exists");
		} catch (SqlInjectionException e) {
			throw new InvalidCollectionException(e);
		} 

	}

	public static DocumentDao getInstance(String collectionName) throws InvalidCollectionException{
		return new DocumentDao(collectionName);
	}
	
	@Override
	public ODocument create() throws Throwable{
		if (logger.isTraceEnabled()) logger.trace("Method Start");
		DbHelper.requestTransaction();
		ODocument doc = super.create();
		if (logger.isTraceEnabled()) logger.trace("Method End");
		return doc;
	}//getNewModelInstance
	
	public ODocument save(ODocument document) throws InvalidClassException {
		return super.save(document);
	}


	

}

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
package com.shoppingbox.service.storage;


import com.shoppingbox.dao.CollectionDao;
import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.dao.exception.InvalidCollectionException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.util.QueryParams;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.List;


public class CollectionService {

	public static void drop(String collectionName) throws InvalidCollectionException, SqlInjectionException, Exception{
		CollectionDao dao = CollectionDao.getInstance();
		dao.delete(collectionName);
	}
	
	public static ODocument create(String collectionName) throws Throwable, InvalidCollectionException, InvalidClassException {
		CollectionDao dao = CollectionDao.getInstance();
		ODocument doc=dao.create(collectionName);
		return doc;
	}
	
	public static boolean exists(String collectionName) throws InvalidCollectionException, SqlInjectionException{
		CollectionDao dao = CollectionDao.getInstance();
		return dao.existsCollection(collectionName);
	}
	
	public static ODocument get(String collectionName) throws SqlInjectionException {
		CollectionDao dao = CollectionDao.getInstance();
		return dao.getByName(collectionName);
	}
	
	public static List<ODocument> getCollections(QueryParams criteria) throws SqlInjectionException, InvalidCollectionException{
		CollectionDao dao = CollectionDao.getInstance();
		return dao.get(criteria);
	}
	
	public static long getCollectionsCount(QueryParams criteria) throws SqlInjectionException, InvalidCollectionException{
		CollectionDao dao = CollectionDao.getInstance();
		return dao.getCount(criteria);
	}
}

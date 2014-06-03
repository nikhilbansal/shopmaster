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

import com.google.common.collect.ImmutableMap;
import com.orientechnologies.orient.core.OConstants;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.BBConfiguration;
import com.shoppingbox.dao.AssetDao;
import com.shoppingbox.dao.CollectionDao;
import com.shoppingbox.dao.DocumentDao;
import com.shoppingbox.dao.UserDao;
import com.shoppingbox.dao.exception.InvalidCollectionException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.util.QueryParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Claudio Tesoriero
 *
 */
public class StatisticsService {

    final static Logger logger = LoggerFactory.getLogger(DbHelper.class);

		public static ImmutableMap data() throws SqlInjectionException, InvalidCollectionException{
			if (logger.isTraceEnabled()) logger.trace("Method Start");
			UserDao userDao = UserDao.getInstance();
			CollectionDao collDao = CollectionDao.getInstance();
			AssetDao assetDao = AssetDao.getInstance();
            ODatabaseDocument db = DbHelper.getConnection();
			
			long usersCount =userDao.getCount();
			long assetsCount = assetDao.getCount();
			long collectionsCount = collDao.getCount();
			List<ODocument> collections = collDao.get(QueryParams.getInstance());
			ArrayList<ImmutableMap> collMap = new ArrayList<ImmutableMap>();
			for(ODocument doc:collections){
				String collectionName = doc.field(CollectionDao.NAME);
				DocumentDao docDao = DocumentDao.getInstance(collectionName);
				long numberOfRecords=0;
				try{
					numberOfRecords=docDao.getCount();
					OClass myClass = db.getMetadata().getSchema().getClass(collectionName);
					long size=0;
					for (int clusterId : myClass.getClusterIds()) {
					  size += db.getClusterRecordSizeById(clusterId);
					}
					collMap.add(ImmutableMap.of(
							"name",collectionName,
							"records", numberOfRecords,
							"size",size
							));
				}catch (Throwable e){
					logger.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
			ImmutableMap response = ImmutableMap.of(
					"users", usersCount,
					"collections", collectionsCount,
					"collections_details", collMap,
					"assets",assetsCount
					);
			if (logger.isDebugEnabled()) logger.debug(response.toString());
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return response;
		}
		
		public static String dbConfiguration() {
			if (logger.isTraceEnabled()) logger.trace("Method Start");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			OGlobalConfiguration.dumpConfiguration(ps);
			String content = "";
			try {
				content=baos.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				content=baos.toString();
			}
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return content;
		}
		
		public static ImmutableMap db() {
			if (logger.isTraceEnabled()) logger.trace("Method Start");
            ODatabaseDocument db = DbHelper.getConnection();
			HashMap dbProp= new HashMap();
			dbProp.put("version", OConstants.getVersion());
			dbProp.put("url", OConstants.ORIENT_URL);
			dbProp.put("path", db.getStorage().getConfiguration().getDirectory());
			dbProp.put("timezone", db.getStorage().getConfiguration().getTimeZone());
			dbProp.put("locale.language", db.getStorage().getConfiguration().getLocaleLanguage());
			dbProp.put("locale.country", db.getStorage().getConfiguration().getLocaleCountry());
			
			ImmutableMap response = ImmutableMap.of(
					"properties", dbProp,
					"size", db.getSize(),
					"status", db.getStatus(),
					"configuration", dbConfiguration(),
					"physical_size",FileUtils.sizeOfDirectory(new File (BBConfiguration.getDBDir()))
					);
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return response;
		}
		
		public static ImmutableMap os() {
			if (logger.isTraceEnabled()) logger.trace("Method Start");
			ImmutableMap response=null;
			if (BBConfiguration.getStatisticsSystemOS()){
				response = ImmutableMap.of(
						"os_name", System.getProperty("os.name"),
						"os_arch",  System.getProperty("os.arch"),
						"os_version",  System.getProperty("os.version"),
						"processors",  Runtime.getRuntime().availableProcessors()
						);
			}else{
				response = ImmutableMap.of(
						"os_name", "N/A",
						"os_arch", "N/A",
						"os_version",  "N/A",
						"processors",  0
						);				
			}
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return response;
		}
		
		public static ImmutableMap java() {
			if (logger.isTraceEnabled()) logger.trace("Method Start");
			ImmutableMap response = ImmutableMap.of(
					"java_class_version", System.getProperty("java.class.version"),
					"java_vendor",  System.getProperty("java.vendor"),
					"java_vendor_url",  System.getProperty("java.vendor.url"),
					"java_version",  System.getProperty("java.version")
					);
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return response;
		}	
		
		public static ImmutableMap memory() {
			if (logger.isTraceEnabled()) logger.trace("Method Start");
			ImmutableMap response=null;

			if (BBConfiguration.getStatisticsSystemMemory()){
				Runtime rt = Runtime.getRuntime(); 
				long maxMemory=rt.maxMemory();
				long freeMemory=rt.freeMemory();
				long totalMemory=rt.totalMemory();
				response = ImmutableMap.of(
						"max_allocable_memory",maxMemory,
						"current_allocate_memory", totalMemory,
						"used_memory_in_the_allocate_memory",totalMemory - freeMemory,
						"free_memory_in_the_allocated_memory", freeMemory
						);
			}else{
				response = ImmutableMap.of(
						"max_allocable_memory",0,
						"current_allocate_memory", 0,
						"used_memory_in_the_allocate_memory",0 ,
						"free_memory_in_the_allocated_memory", 0
						);
			}
			
			if (logger.isTraceEnabled()) logger.trace("Method End");
			return response;
		}	
		
}

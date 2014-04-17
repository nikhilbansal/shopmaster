package com.shoppingbox.dao;

import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.exception.IndexNotFoundException;
import com.shoppingbox.util.QueryParams;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.List;

public abstract class IndexDao {
	public final String INDEX_NAME;
	public static final String MODEL_NAME = "_BB_Index";
	protected ODatabaseRecordTx db;
	
	protected IndexDao(String indexName) throws IndexNotFoundException {
		this.INDEX_NAME=indexName.toUpperCase();
		this.db=DbHelper.getConnection();
		//this.index = db.getMetadata().getIndexManager().getIndex(indexName);
		//if (index==null) throw new IndexNotFoundException("The index " + indexName + " does not exists");
		//if (!index.getType().equals(OClass.INDEX_TYPE.DICTIONARY.toString())) throw new IndexNotFoundException("The index " + indexName + " is not a dictionary");
	}
	
	public IndexDao put (String key,Object value){
		String indexKey = this.INDEX_NAME+":"+key;
		ODocument newValue = getODocument(key);
		if(newValue==null){
			newValue = new ODocument(MODEL_NAME);
			newValue.field("key",indexKey);
		}
		
		newValue.field("value",value);
		newValue.save();
		//index.put(key, newValue);
		return this;
	}
	
	private ODocument getODocument(String key){
		String indexKey = this.INDEX_NAME+":"+key;
		QueryParams qp = QueryParams.getInstance();
		qp.where("key = ?").params(new String[]{indexKey});
		try{
			List<ODocument> docs = GenericDao.getInstance().executeQuery(MODEL_NAME, qp);
			if(docs==null || docs.isEmpty()){
				return null;
			}else{
				return docs.get(0);
			}
		}catch(SqlInjectionException sie){
			throw new RuntimeException(sie);
		}
	}
	
	public Object get (String key){
		ODocument valueOnDb=getODocument(key);
		if (valueOnDb==null) return null;
		return valueOnDb.field("value");
	}
}

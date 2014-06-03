package com.shoppingbox.service;

import com.orientechnologies.orient.core.index.OIndex;
import com.shoppingbox.dao.IndexDao;
import com.shoppingbox.util.Constants;
import com.shoppingbox.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhilbansal on 01/06/14.
 */
public class IndexService {

    final static Logger logger = LoggerFactory.getLogger(IndexService.class);

    static private IndexService instance = new IndexService();

    static public IndexService getInstance() {
        return instance;
    }

    public OIndex<?> createIndex(String input) throws JSONException {
        logger.info(String.format("createIndex : input - %s", input));
        JSONObject indexJsonObject = new JSONObject(input);

        String indexType;
        try{
            indexType = (String) JsonUtils.extractField(indexJsonObject, Constants.INDEX_TYPE);
        }catch (IllegalArgumentException e){
            logger.error(String.format("createIndex : input %s doesn't contain %s", input, Constants.INDEX_TYPE));
            throw new IllegalArgumentException(String.format("createIndex : input %s doesn't contain %s", input, Constants.INDEX_TYPE));
        }

        String className;
        try{
            className = (String) JsonUtils.extractField(indexJsonObject, Constants.CLASS_NAME);
        }catch (IllegalArgumentException e){
            logger.error(String.format("createIndex : input %s doesn't contain %s", input, Constants.CLASS_NAME));
            throw new IllegalArgumentException(String.format("createIndex : input %s doesn't contain %s", input, Constants.CLASS_NAME));
        }

        JSONArray fieldsTypeArray = (JSONArray) JsonUtils.extractField(indexJsonObject, Constants.FIELDS_TYPE_ARRAY);

        return IndexDao.getInstance().createIndex(className, indexType, fieldsTypeArray);
    }

    public void dropIndex(String input) throws JSONException {
        logger.info(String.format("dropIndex : input - %s", input));
        JSONObject indexJsonObject = new JSONObject(input);

        String className;
        try{
            className = (String) JsonUtils.extractField(indexJsonObject, Constants.CLASS_NAME);
        }catch (IllegalArgumentException e){
            logger.error(String.format("createIndex : input %s doesn't contain %s", input, Constants.CLASS_NAME));
            throw new IllegalArgumentException(String.format("createIndex : input %s doesn't contain %s", input, Constants.CLASS_NAME));
        }

        JSONArray fieldsTypeArray = (JSONArray) JsonUtils.extractField(indexJsonObject, Constants.FIELDS_TYPE_ARRAY);

        IndexDao.getInstance().dropIndex(className, fieldsTypeArray);
    }

    public void dropIndexByName(String indexName) {
        logger.info(String.format("dropIndex : indexName - %s", indexName));
        IndexDao.getInstance().dropIndex(indexName);
    }

    public java.util.Collection<? extends OIndex<?>> getIndexes() {
        logger.info(String.format("getIndexes"));
        return IndexDao.getIndexes();
    }
}

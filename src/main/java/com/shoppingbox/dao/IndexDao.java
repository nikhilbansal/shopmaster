package com.shoppingbox.dao;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.index.OCompositeIndexDefinition;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexManagerProxy;
import com.orientechnologies.orient.core.index.OPropertyIndexDefinition;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.util.Constants;
import com.shoppingbox.util.DaoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nikhilbansal on 01/06/14.
 */
public class IndexDao {

    final static Logger logger = LoggerFactory.getLogger(IndexDao.class);

    static private IndexDao instance = new IndexDao();

    static public IndexDao getInstance() {
        return instance;
    }

    public static OIndex<?> createIndex(final String className, String indexType, JSONArray fieldsTypeArray) {
        logger.info(String.format("createIndex : className - %s indexType - %s fieldsTypeArray - %s", className, indexType, fieldsTypeArray));
        ODatabaseDocument db = DbHelper.getConnection();
        OIndexManagerProxy indexManager = db.getMetadata().getIndexManager();
        String iType = DaoUtils.getIndexType(indexType).toString();

        List<String> fieldNamesList = new ArrayList<String>();

        OIndex<?> oIndex;
        String iName, fieldName, fieldType;

        switch (fieldsTypeArray.length()){
            case 0:
                logger.error("createIndex : fieldTypes.length = 0");
                throw new IllegalArgumentException("createIndex : fieldTypes.length = 0");
            case 1:
                logger.info("createIndex : creating simple index");
                try {
                    JSONObject jsonObject = fieldsTypeArray.getJSONObject(0);
                    Iterator<String> keysIterator = jsonObject.keys();
                    if (keysIterator.hasNext())
                    {
                        fieldName = keysIterator.next();
                        fieldType = jsonObject.getString(fieldName);
                        fieldNamesList.add(fieldName);
                    } else {
                        throw new IllegalArgumentException(String.format("createIndex : fieldsTypeArray.length()=1 but cannot extract first element from it"));
                    }
                } catch (JSONException e) {
                    throw new IllegalArgumentException(String.format("createIndex : cannot extract first element's key value from fieldsTypeArray - %s", fieldsTypeArray));
                }
                iName = DaoUtils.getIndexName(className, Constants.INDEX_DELIMITER, fieldNamesList);
                if(IndexDao.getInstance().existsIndex(iName)){
                    logger.error(String.format("Index %s already exists", iName));
                    throw new IllegalArgumentException(String.format("Index %s already exists", iName));
                }
                oIndex = indexManager.createIndex(iName, iType,
                        new OPropertyIndexDefinition(className, fieldName, DaoUtils.getOType(fieldType)),
                        new int[]{db.getClusterIdByName(className)}, null, null);
                break;
            default:
                logger.info("createIndex : creating composite index");
                List<OPropertyIndexDefinition> oPropertyIndexDefinitionList = new ArrayList<OPropertyIndexDefinition>();
                for(int i = 0; i < fieldsTypeArray.length(); i++){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = fieldsTypeArray.getJSONObject(i);
                        Iterator<String> keysIterator = jsonObject.keys();
                        if (keysIterator.hasNext())
                        {
                            fieldName = keysIterator.next();
                            fieldType = jsonObject.getString(fieldName);
                            fieldNamesList.add(fieldName);
                        } else {
                            throw new IllegalArgumentException(String.format("createIndex : fieldsTypeArray.length()=1 but cannot extract first element from it"));
                        }
                    } catch (JSONException e) {
                        throw new IllegalArgumentException(String.format("createIndex : cannot extract %d element's key value from fieldsTypeArray - %s", i, fieldsTypeArray));
                    }
                    oPropertyIndexDefinitionList.add(new OPropertyIndexDefinition(className, fieldName, DaoUtils.getOType(fieldType)));
                }
                iName = DaoUtils.getIndexName(className, Constants.INDEX_DELIMITER, fieldNamesList);
                if(IndexDao.getInstance().existsIndex(iName)){
                    logger.error(String.format("Index %s already exists", iName));
                    throw new IllegalArgumentException(String.format("Index %s already exists", iName));
                }
                oIndex = indexManager.createIndex(iName, iType,
                        new OCompositeIndexDefinition(className, oPropertyIndexDefinitionList),
                        new int[]{db.getClusterIdByName(className)}, null, null);
                break;
        }

        return oIndex;
    }

    public static void dropIndex(final String CLASS_NAME, JSONArray fieldsArray) throws JSONException {
        List<String> fields = new ArrayList<String>();
        for(int i = 0; i < fieldsArray.length(); i++ ){
            fields.add((String) fieldsArray.get(i));
        }
        dropIndex(DaoUtils.getIndexName(CLASS_NAME, Constants.INDEX_DELIMITER, fields));
    }

    public static void dropIndex(final String indexName){
        ODatabaseDocument db = DbHelper.getConnection();
        final OIndexManagerProxy indexManager = db.getMetadata().getIndexManager();
        indexManager.dropIndex(indexName);
        indexManager.reload();
    }

    public static OIndex<?> getIndex(final String CLASS_NAME, List<String> fields){
        return getIndex(DaoUtils.getIndexName(CLASS_NAME, Constants.INDEX_DELIMITER, fields));
    }

    public static OIndex<?> getIndex(final String indexName){
        ODatabaseDocument db = DbHelper.getConnection();
        final OIndexManagerProxy indexManager = db.getMetadata().getIndexManager();
        return indexManager.getIndex(indexName);
    }

    public static java.util.Collection<? extends OIndex<?>> getIndexes() {
        ODatabaseDocument db = DbHelper.getConnection();
        final OIndexManagerProxy indexManager = db.getMetadata().getIndexManager();
        return indexManager.getIndexes();
    }

    public static Boolean existsIndex(final String indexName){
        OIndexManagerProxy indexManager = DbHelper.getConnection().getMetadata().getIndexManager();
        return indexManager.existsIndex(indexName);
    }
}

package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.StoreDao;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */

public class StoreService implements IService{
    final static Logger logger = LoggerFactory.getLogger(StoreService.class);

    static private StoreService instance = new StoreService();

    static public StoreService getInstance() {
        return instance;
    }

    public String upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            StoreDao storeDao = StoreDao.getInstance();
            ODocument storeODocument = new ODocument(Constants.STORE_CLASS_NAME);
            storeODocument.fromJSON(input);
            storeDao.upsert(storeODocument);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class StoreDao extends NodeDao{

    final static Logger logger = LoggerFactory.getLogger(StoreDao.class);

    protected StoreDao() {
        super(Constants.STORE_CLASS_NAME);
    }

    public static StoreDao getInstance(){
        return new StoreDao();
    }

    public ODocument upsert(ODocument storeODocument) throws InvalidClassException {
        super.save(storeODocument);
        return storeODocument;
    }

}

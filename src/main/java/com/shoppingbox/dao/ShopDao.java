package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class ShopDao extends NodeDao{
    final static Logger logger = LoggerFactory.getLogger(ShopDao.class);

    public final static String CLASS_NAME="Shop";

    public static ShopDao getInstance(){
        return new ShopDao();
    }

    protected ShopDao() {
        super(CLASS_NAME);
    }

    public ODocument upsert(ODocument shopODocument) throws InvalidClassException {
        super.save(shopODocument);
        return shopODocument;
    }

//    public ODocument search(String username){
//        OIndex idx = db.getMetadata().getIndexManager().getIndex(USER_NAME_INDEX);
//        OIdentifiable record = (OIdentifiable) idx.get( username );
//        return record;
//    }
}


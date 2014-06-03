package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class BrandDao extends NodeDao{
    final static Logger logger = LoggerFactory.getLogger(BrandDao.class);

    public static BrandDao getInstance(){
        return new BrandDao();
    }

    protected BrandDao() {
        super(Constants.BRAND_CLASS_NAME);
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


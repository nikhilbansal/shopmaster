package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 17/04/14.
 */
public class MallDao extends NodeDao{
    final static Logger logger = LoggerFactory.getLogger(MallDao.class);

    public final static String CLASS_NAME="Mall";

    public static MallDao getInstance(){
        return new MallDao();
    }

    protected MallDao() {
        super(CLASS_NAME);
    }

    public ODocument upsert(ODocument mallODocument) throws InvalidClassException {
        return super.save(mallODocument);
    }

//    public ODocument search(String username){
//        OIndex idx = db.getMetadata().getIndexManager().getIndex(USER_NAME_INDEX);
//        OIdentifiable record = (OIdentifiable) idx.get( username );
//        return record;
//    }
}

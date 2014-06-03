package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 17/04/14.
 */
public class MallDao extends NodeDao{

    final static Logger logger = LoggerFactory.getLogger(MallDao.class);

    public static MallDao getInstance(){
        return new MallDao();
    }

    protected MallDao() {
        super(Constants.MALL_CLASS_NAME);
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

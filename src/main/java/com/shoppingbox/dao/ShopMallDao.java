package com.shoppingbox.dao;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.InvalidClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class ShopMallDao extends NodeDao{

    final static Logger logger = LoggerFactory.getLogger(ShopMallDao.class);

    public final static String CLASS_NAME="ShopMall";

    protected ShopMallDao() {
        super(CLASS_NAME);
    }

    public static ShopMallDao getInstance(){
        return new ShopMallDao();
    }

    public ODocument upsert(ODocument shopmallODocument) throws InvalidClassException {
        super.save(shopmallODocument);
        return shopmallODocument;
    }

}

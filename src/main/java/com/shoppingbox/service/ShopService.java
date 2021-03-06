package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.BrandDao;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */

public class ShopService implements IService{
    final static Logger logger = LoggerFactory.getLogger(ShopService.class);

    static private ShopService instance = new ShopService();

    static public ShopService getInstance() {
        return instance;
    }

    public String upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            BrandDao brandDao = BrandDao.getInstance();
            ODocument shopODocument = new ODocument(Constants.BRAND_CLASS_NAME);
            shopODocument.fromJSON(input);
            brandDao.upsert(shopODocument);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

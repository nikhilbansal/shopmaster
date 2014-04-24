package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.ShopDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */

public class ShopService {
    final static Logger logger = LoggerFactory.getLogger(ShopService.class);

    public static void upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            ShopDao shopDao = ShopDao.getInstance();
            ODocument shopODocument = new ODocument(ShopDao.CLASS_NAME);
            shopODocument.fromJSON(input);
            shopDao.upsert(shopODocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

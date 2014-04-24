package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.ShopMallDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 18/04/14.
 */

public class ShopMallService {
    final static Logger logger = LoggerFactory.getLogger(ShopMallService.class);

    public static void upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            ShopMallDao shopmallDao = ShopMallDao.getInstance();
            ODocument shopmallODocument = new ODocument(ShopMallDao.CLASS_NAME);
            shopmallODocument.fromJSON(input);
            shopmallDao.upsert(shopmallODocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.MallDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 17/04/14.
 */

public class MallService {
    final static Logger logger = LoggerFactory.getLogger(MallService.class);

    public static String upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            MallDao mallDao = MallDao.getInstance();
            ODocument mallODocument = new ODocument(MallDao.CLASS_NAME);
            mallODocument.fromJSON(input);
            return mallDao.upsert(mallODocument).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

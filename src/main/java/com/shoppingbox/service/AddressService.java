package com.shoppingbox.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.AddressDao;
import com.shoppingbox.dao.IndexDao;
import com.shoppingbox.models.Identifiers;
import com.shoppingbox.models.Pair;
import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhilbansal on 23/05/14.
 */
public class AddressService implements IService{
    final static Logger logger = LoggerFactory.getLogger(AddressService.class);

    static private AddressService instance = new AddressService();

    static public AddressService getInstance() {
        return instance;
    }

    public String upsert(String input) {
        try {
            logger.info(String.format("upsert : %s", input));
            AddressDao addressDao = AddressDao.getInstance();
            ODocument addressODocument = new ODocument(Constants.ADDRESS_CLASS_NAME);
            addressODocument.fromJSON(input);
            Identifiers addressIdentifiers = addressDao.getIdentifiers(addressODocument);
            // Lookup to get existing document
            //addressDao.get(addressIdentifiers);
            return addressDao.save(addressODocument).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package com.shoppingbox.dao;

import com.shoppingbox.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nikhil.bansal on 17/04/14.
 */
public class AddressDao extends NodeDao{

    final static Logger logger = LoggerFactory.getLogger(AddressDao.class);

    public static AddressDao getInstance(){
        return new AddressDao();
    }

    protected AddressDao() {
        super(Constants.ADDRESS_CLASS_NAME);
    }

}

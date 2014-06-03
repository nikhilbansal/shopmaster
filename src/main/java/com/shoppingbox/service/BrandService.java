package com.shoppingbox.service;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class BrandService implements IService{

    static private BrandService instance = new BrandService();

    static public BrandService getInstance() {
        return instance;
    }

    @Override
    public String upsert(String input) {
        return null;
    }
}

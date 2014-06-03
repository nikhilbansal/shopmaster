package com.shoppingbox.service;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
public class CategoryService implements IService{

    static private CategoryService instance = new CategoryService();

    static public CategoryService getInstance() {
        return instance;
    }

    @Override
    public String upsert(String input) {
        return null;
    }
}

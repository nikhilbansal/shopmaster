package com.shoppingbox.util.serviceinitialization;

import com.shoppingbox.service.*;
import com.shoppingbox.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nikhilbansal on 02/06/14.
 */
public class Services {
    public static Map<String, IService> servicesMap;

    static {
        Map<String, IService> map = new HashMap<String, IService>();
        map.put(Constants.ADDRESS_CLASS_NAME, AddressService.getInstance());
        map.put(Constants.BRAND_CLASS_NAME, BrandService.getInstance());
        map.put(Constants.CATEGORY_CLASS_NAME, CategoryService.getInstance());
        map.put(Constants.MALL_CLASS_NAME, MallService.getInstance());
        map.put(Constants.SHOP_CLASS_NAME, ShopService.getInstance());
        map.put(Constants.STORE_CLASS_NAME, StoreService.getInstance());
    }
}

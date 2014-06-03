package com.shoppingbox.util;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nikhilbansal on 03/06/14.
 */
public class JsonUtils {

//    final static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static Object extractField(JSONObject indexJsonObject, String field) throws JSONException {
        Object fieldValue = indexJsonObject.get(field);
        if(fieldValue == null){
            throw new IllegalArgumentException(String.format("extractField : indexJsonObject - %s doesn't contain field - %s", indexJsonObject, field));
        }
        return fieldValue;
    }
}

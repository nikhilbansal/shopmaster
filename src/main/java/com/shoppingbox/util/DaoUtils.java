package com.shoppingbox.util;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.shoppingbox.models.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nikhilbansal on 01/06/14.
 */
public class DaoUtils {
    final static Logger logger = LoggerFactory.getLogger(DaoUtils.class);
    private static final List<String> INDEX_TYPE_LIST;

    static {
        INDEX_TYPE_LIST = new ArrayList<String>();
        INDEX_TYPE_LIST.add(OClass.INDEX_TYPE.UNIQUE.toString());
        INDEX_TYPE_LIST.add(OClass.INDEX_TYPE.NOTUNIQUE.toString());
        INDEX_TYPE_LIST.add(OClass.INDEX_TYPE.FULLTEXT.toString());
        INDEX_TYPE_LIST.add(OClass.INDEX_TYPE.DICTIONARY.toString());
    }

    public static OClass.INDEX_TYPE getIndexType(String indexType) throws IllegalArgumentException{
        logger.info("************ " + OClass.INDEX_TYPE.UNIQUE.toString());
        if(!INDEX_TYPE_LIST.contains(indexType)){
            logger.error(String.format("indexType %s not allowed", indexType));
            throw new IllegalArgumentException(String.format("indexType %s not allowed", indexType));
        }
        return OClass.INDEX_TYPE.valueOf(indexType);
    }

    public static String getIndexName(String className, String indexSeparator, List<String> fields){
        String indexName = className;
        for(String field : fields)
            indexName += indexSeparator + field;
        return indexName;
    }

    public static OType[] getOTypes(List<String> operandTypes){
        OType[] oTypes = new OType[operandTypes.size()];
        for(int i = 0; i < operandTypes.size(); i++){
            oTypes[i] = getOType(operandTypes.get(i));
        }
        return oTypes;
    }

    public static OType getOType(String operandType){
        return OType.valueOf(operandType);
    }

    public static <F,S> List<F> getFirsts(Pair<F,S> [] pairs) {
        List<F> firsts = new ArrayList<F>();
        for (int i = 0 ; i < pairs.length; i++){
            firsts.add(pairs[i].getFirst());
        }
        return firsts;
    }

    public static <F,S> List<S> getSeconds(Pair<F,S> [] pairs) {
        List<S> seconds = new ArrayList<S>();
        for (int i = 0 ; i < pairs.length; i++){
            seconds.add(pairs[i].getSecond());
        }
        return seconds;
    }
}

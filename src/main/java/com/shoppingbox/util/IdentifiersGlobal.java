package com.shoppingbox.util;

import com.orientechnologies.orient.core.metadata.schema.*;
import com.shoppingbox.db.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by nikhilbansal on 26/05/14.
 */
public enum IdentifiersGlobal {
    INSTANCE;
    final static Logger logger = LoggerFactory.getLogger(IdentifiersGlobal.class);
    public static final String IDENTIFIERS = "identifiers";
    public static final String IDLIST_DELIMITER = "\\|";
    public Map<String, List<IdentifierCsv>> idenfifierMap;

    public final class IdentifierCsv {
        private String[] identifier;
        private String ID_DELIMITER = ",";
        public IdentifierCsv(String identifierCsv) {
            logger.info("\tidentifierCsv : " + identifierCsv);
            identifier = identifierCsv.split(ID_DELIMITER);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "identifier=" + Arrays.toString(identifier) +
                    '}';
        }

        public String[] getIdentifier() {
            return identifier;
        }
    }

    public void initialize()
    {
        logger.info("\tinitialize IdentifiersGlobal");
        if(idenfifierMap == null) idenfifierMap = new HashMap<String, List<IdentifierCsv>>();
        OSchemaProxy s = (OSchemaProxy) DbHelper.getConnection().getMetadata().getSchema();
        if (!s.getClasses().isEmpty()) {
            final List<OClass> classes = new ArrayList<OClass>(s.getClasses());
            Collections.sort(classes);
            for (OClass cls : classes) {
                logger.info(String.format("Class : %s", cls.getName()));
                OClassImpl clsImpl = (OClassImpl) cls;
                if (clsImpl.getCustomInternal() != null && clsImpl.getCustomInternal().get(IDENTIFIERS) != null){
                    idenfifierMap.put(clsImpl.getName(), getIdentifiers(clsImpl.getCustomInternal().get(IDENTIFIERS).trim()));
                }
            }
        }

    }

    private List<IdentifierCsv> getIdentifiers(String identifiers) {
        logger.info("\tinitialize IdentifiersGlobal : " + identifiers);
        List<IdentifierCsv> identifierList = new ArrayList<IdentifierCsv>();
        final String[] identifiersArray = identifiers.split(IDLIST_DELIMITER);
        for(String identifierCsv : identifiersArray){
            identifierList.add(new IdentifierCsv(identifierCsv));
        }
        return identifierList;
    }

    @Override
    public String toString() {
        return "IdentifiersGlobal{" +
                "idenfifierMap=" + idenfifierMap +
                '}';
    }
}
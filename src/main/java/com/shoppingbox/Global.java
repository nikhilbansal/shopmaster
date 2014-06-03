package com.shoppingbox;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.service.storage.StatisticsService;
import com.shoppingbox.util.IdentifiersGlobal;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Global {
    final static Logger logger = LoggerFactory.getLogger(Global.class);

    private static Boolean justCreated = false;


    public void beforeStart() {
        logger.info("BaasBox is starting...");
        logger.info("System details:");
        logger.info(StatisticsService.os().toString());
        logger.info(StatisticsService.memory().toString());
        logger.info(StatisticsService.java().toString());
        if (Boolean.parseBoolean(BBConfiguration.configuration.getString(BBConfiguration.DUMP_DB_CONFIGURATION_ON_STARTUP)))
            logger.info(StatisticsService.db().toString());

        logger.info("...Loading plugin...");
    }

    public void onLoadConfig() {
        logger.debug("Global.onLoadConfig() called");
        logger.info("BaasBox is preparing OrientDB Embedded Server...");
        try {
            OGlobalConfiguration.TX_LOG_SYNCH.setValue(Boolean.TRUE);
            OGlobalConfiguration.TX_COMMIT_SYNCH.setValue(Boolean.TRUE);

            OGlobalConfiguration.NON_TX_RECORD_UPDATE_SYNCH.setValue(Boolean.TRUE);

            OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(Boolean.FALSE);
            OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(Boolean.FALSE);

            OGlobalConfiguration.INDEX_MANUAL_LAZY_UPDATES.setValue(-1);
            OGlobalConfiguration.FILE_LOCK.setValue(false);

            OGlobalConfiguration.FILE_DEFRAG_STRATEGY.setValue(1);

            OGlobalConfiguration.MEMORY_USE_UNSAFE.setValue(false);

            Orient.instance().startup();
            ODatabaseDocumentTx db = null;
            try {
                final String url = "plocal:" + BBConfiguration.configuration.getString(BBConfiguration.DB_PATH);
                db = new ODatabaseDocumentTx(url);
                db.getLevel1Cache().invalidate();
                db.getLevel1Cache().clear();
                if (!db.getURL().startsWith("remote:") && !db.exists()) {
                    logger.info("DB does not exist, BaasBox will create a new one");
//                    OrientGraph graph = new OrientGraph(db.getURL());
//                    graph.shutdown();
                    db.create();
                    justCreated = true;
                } else {
                    logger.info("DB already exists");
                }
            } catch (Throwable e) {
                logger.error("!! Error initializing BaasBox!", e);
                logger.error(ExceptionUtils.getFullStackTrace(e));
                throw e;
            } finally {
                if (db != null && !db.isClosed()) db.close();
            }
        } catch (Throwable e) {
            logger.error("!! Error initializing BaasBox!", e);
            logger.error("Abnormal BaasBox termination.");
        }
        logger.debug("Global.onLoadConfig() ended");
    }

    public void onStart() {
        logger.debug("Global.onStart() called");
        //Orient.instance().shutdown();

        ODatabaseDocument db = null;
        try {
            if (justCreated) {
                try {
                    //we MUST use admin/admin because the db was just created
                    db = DbHelper.open("admin", "admin");
                    DbHelper.setupDb(db);
                } catch (Throwable e) {
                    logger.error("!! Error initializing BaasBox!", e);
                    logger.error(ExceptionUtils.getFullStackTrace(e));
                    throw e;
                } finally {
                    if (db != null && !db.isClosed()) db.close();
                }
                justCreated = false;
            }
            db = DbHelper.open("admin", "admin");
            IdentifiersGlobal.INSTANCE.initialize();
            logger.info("IdentifiersGlobal : \n" + IdentifiersGlobal.INSTANCE.toString());
            db.close();
        } catch (Throwable e) {
            logger.error("!! Error initializing BaasBox!", e);
            logger.error("Abnormal BaasBox termination.");
        }
    }

    public void onStop() {
        logger.debug("Global.onStop() called");
        logger.info("BaasBox is shutting down...");
        try {

            //OrientGraph db = null;
            ODatabaseDocument db = null;
            try {
                try{
                    db = DbHelper.getConnection();
                }catch (Exception e){
                    //Do nothing
                }finally{
                    db = DbHelper.open("admin", "admin");
                }
                if(db.exists()) db.drop();
//                db = new OrientGraph(DbHelper.getODatabaseDocumentTxConnection());
//                if (db.getRawGraph().exists()) {
//                    logger.info("DB exists, Dropping it");
//                    db.getRawGraph().drop();
//                }
            } catch (Throwable e) {
                logger.error("!! Error initializing BaasBox!", e);
                logger.error(ExceptionUtils.getFullStackTrace(e));
                throw e;
            } finally {
//                if (db != null && !db.getRawGraph().isClosed()) db.getRawGraph().close();
                if(db != null && db.isClosed()) db.close();
            }

            logger.info("Closing the DB connections...");
            ODatabaseDocumentPool.global().close();
            logger.info("Shutting down embedded OrientDB Server");
            Orient.instance().shutdown();
            logger.info("...ok");
        } catch (ODatabaseException e) {
            logger.error("Error closing the DB!", e);
        } catch (Throwable e) {
            logger.error("!! Error shutting down BaasBox!", e);
        }
        logger.info("Destroying session manager...");
        logger.info("...BaasBox has stopped");
        logger.debug("Global.onStop() ended");
    }
}
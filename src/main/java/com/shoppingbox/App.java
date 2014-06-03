package com.shoppingbox;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        final Global global = new Global();
        try {
            global.onStop();
            global.beforeStart();
            global.onLoadConfig();
            global.onStart();
//            ODatabaseRecordTx db = DbHelper.open(BBConfiguration.getAPPCODE(), BBConfiguration.getBaasBoxAdminUsername(), BBConfiguration.getBaasBoxAdminPassword());
//            new ExportJob("/Users/nikhil.bansal/Downloads/orientdb-community-1.7-rc1/backup.zip", "1234567890").run();
//        } catch (InvalidAppCodeException e) {
//            e.printStackTrace();
        } finally {
//            global.onStop();
        }
    }
}

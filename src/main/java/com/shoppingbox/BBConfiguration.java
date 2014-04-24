/*
     Copyright 2012-2013 
     Claudio Tesoriero - c.tesoriero-at-baasbox.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.shoppingbox;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class BBConfiguration implements IBBConfigurationKeys {


    public static Configuration configuration;

    static {
        try {
            configuration = new PropertiesConfiguration("application.conf");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    ;
	private static Boolean computeMetrics;
	
	
	@Deprecated
	public static String getRealm(){
		return configuration.getString(REALM);
	}
	
	public static int getMVCCMaxRetries(){
		return configuration.getInt(MVCC_MAX_RETRIES);
	}
	
	public static String getBaasBoxUsername(){
		return configuration.getString(ANONYMOUS_USERNAME);
	}
	
	public static String getBaasBoxPassword(){
		return configuration.getString(ANONYMOUS_PASSWORD);
	}
	
	public static String getBaasBoxAdminUsername(){
		return configuration.getString(ADMIN_USERNAME);
	}
	
	public static String getBaasBoxAdminPassword(){
		return configuration.getString(ADMIN_PASSWORD);
	}

	public static  Boolean getStatisticsSystemOS(){
		return configuration.getBoolean(STATISTICS_SYSTEM_OS);
	}	

	public static Boolean getStatisticsSystemMemory(){
		return configuration.getBoolean(STATISTICS_SYSTEM_MEMORY);
	}

	public static Boolean getWriteAccessLog(){
		return configuration.getBoolean(WRITE_ACCESS_LOG);
	}
	
	public static String getApiVersion(){
		return configuration.getString(API_VERSION);
	}
	public static String getDBDir(){
		return configuration.getString(DB_PATH);
	}
	
	public static Boolean getWrapResponse(){
		return Boolean.valueOf(configuration.getString(WRAP_RESPONSE));
	}
	
//	public static String getAPPCODE() {
//		return configuration.getString(APP_CODE);
//	}
	
	public static String getDBBackupDir() {
		return configuration.getString(DB_BACKUP_PATH);
	}
	
	public static String getPushCertificateFolder(){
		return configuration.getString(PUSH_CERTIFICATES_FOLDER);
	}

	public static String getRootPassword() {
		return configuration.getString(ROOT_PASSWORD);
	}

	public static boolean getComputeMetrics() {
		if (computeMetrics==null) 
			computeMetrics=(!StringUtils.isEmpty(configuration.getString(ROOT_PASSWORD)) 
				&& 	BooleanUtils.isTrue(configuration.getBoolean(CAPTURE_METRICS)));
		return computeMetrics;
	}

	public static void overrideConfigurationComputeMetrics(boolean computeMetrics) {
		BBConfiguration.computeMetrics = computeMetrics;
	}
	
	
}

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
package com.shoppingbox.dao;


import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.shoppingbox.db.DbHelper;

import java.util.Map;

public class RoleDao {
		/**
		 * Field of a role that state the inherited role.
		 */
		public static final String 	FIELD_INHERITED = "inheritedRole";
		

		public static final String  READER_BASE_ROLE = "reader";
		
		public static ORole getRole(String name){
            ODatabaseDocument db = DbHelper.getConnection();
			return db.getMetadata().getSecurity().getRole(name);
		}
		
		public static ORole createRole(String name,String inheritedRoleName){
            ODatabaseDocument db = DbHelper.getConnection();
			ORole inheritedRole = db.getMetadata().getSecurity().getRole(inheritedRoleName);
			final ORole role =  db.getMetadata().getSecurity().createRole(name,inheritedRole.getMode());
			role.getDocument().field(FIELD_INHERITED,inheritedRole.getDocument().getRecord());
			role.save();
	        return role;
		}
		
		public static ORole createRole(String name,ORole.ALLOW_MODES mode,Map rules){
            ODatabaseDocument db = DbHelper.getConnection();
			final ORole role =  db.getMetadata().getSecurity().createRole(name,mode);
			role.getDocument().field("rules",rules);
			role.save();
	        return role;
		}

		public static boolean exists(String roleName) {
			return (DbHelper.getConnection().getMetadata().getSecurity().getRole(roleName)!=null);
		}

		public static void delete(String name) {
			ORole role = getRole(name);
			role.getDocument().delete();
			
		}
}

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

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.metadata.security.OUser.STATUSES;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.dao.exception.UserAlreadyExistsException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.enumerations.DefaultRoles;
import com.shoppingbox.exception.UserNotFoundException;
import com.shoppingbox.util.QueryParams;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;


public class UserDao extends NodeDao  {
    final static Logger logger = LoggerFactory.getLogger(UserDao.class);

	public final static String MODEL_NAME="_BB_User";
	public final static String USER_LINK = "user";
	private final static String USER_NAME_INDEX = "ouser.name";

	public final static String USER_PUSH_TOKEN="pushToken";
	public final static String USER_DEVICE_OS="os";
	public final static String USER_LOGIN_INFO="login_info";
	public final static String SOCIAL_LOGIN_INFO="sso_tokens";

	public final static String USER_ATTRIBUTES_CLASS = "_BB_UserAttributes";
	public final static String ATTRIBUTES_VISIBLE_BY_ANONYMOUS_USER="visibleByAnonymousUsers";
	public final static String ATTRIBUTES_VISIBLE_BY_REGISTERED_USER="visibleByRegisteredUsers";
	public final static String ATTRIBUTES_VISIBLE_BY_FRIENDS_USER="visibleByFriends";
	public final static String ATTRIBUTES_VISIBLE_ONLY_BY_THE_USER="visibleByTheUser";
	public final static String USER_SIGNUP_DATE="signUpDate";
	public final static String ATTRIBUTES_SYSTEM="system";
	public static final String GENERATED_USERNAME = "generated_username";

	public static UserDao getInstance(){
		return new UserDao();
	}

	protected UserDao() {
		super(MODEL_NAME);
	}

	@Override
	@Deprecated
	public ODocument create(){
		throw new IllegalAccessError("To create a new user call create(String username, String password) or create(String username, String password, String role)");
	}

	public ODocument create(String username, String password) throws UserAlreadyExistsException {
		return create(username, password, null);
	};

	public ODocument create(String username, String password, String role) throws UserAlreadyExistsException,InvalidParameterException {
        OrientGraphNoTx graph = DbHelper.getOGraphDatabaseConnection();
		if (existsUserName(username)) throw new UserAlreadyExistsException("User " + username + " already exists");
		OUser user=null;
		if (role==null) user=db.getMetadata().getSecurity().createUser(username,password,new
				String[]{DefaultRoles.REGISTERED_USER.toString()});
		else {
			ORole orole = RoleDao.getRole(role);
			if (orole==null) throw new InvalidParameterException("Role " + role + " does not exists");
			user=db.getMetadata().getSecurity().createUser(username,password,new String[]{role});
		}
//        graph.createVertexType(CLASS_VERTEX_NAME);
        Vertex vertex = graph.addVertex(CLASS_VERTEX_NAME, CLASS_VERTEX_NAME);
        ODatabaseDocumentTx db = DbHelper.getODatabaseDocumentTxConnection();
		ODocument doc = new ODocument(this.MODEL_NAME);
		doc.field(FIELD_LINK_TO_VERTEX,vertex);
		doc.field(FIELD_CREATION_DATE,new Date());
		vertex.setProperty(FIELD_TO_DOCUMENT_FIELD,doc);

		doc.field(USER_LINK,user.getDocument().getIdentity());
		doc.save();
        graph.commit();
		return doc;
	}

	public boolean existsUserName(String username){
		OIndex idx = db.getMetadata().getIndexManager().getIndex(USER_NAME_INDEX);
		OIdentifiable record = (OIdentifiable) idx.get( username );
		return (record!=null);
	}

	public ODocument getByUserName(String username) throws SqlInjectionException{
		ODocument result=null;
		QueryParams criteria = QueryParams.getInstance().where("user.name=?").params(new String [] {username});
		List<ODocument> resultList= super.get(criteria);
		if (resultList!=null && resultList.size()>0) result = resultList.get(0);
		return result;
	}
	
	public List<ODocument> getByUsernames(List<String> usernames) throws SqlInjectionException{
		QueryParams criteria = QueryParams.getInstance().where("user.name in ?").params(new Object [] {usernames});
		List<ODocument> resultList= super.get(criteria);
		
		return resultList;
	}

//	public ODocument getBySocialUserId(UserInfo ui) throws SqlInjectionException{
//		ODocument result=null;
//		StringBuffer where = new StringBuffer(UserDao.ATTRIBUTES_SYSTEM).append(".");
//		where.append(UserDao.SOCIAL_LOGIN_INFO).append("[").append(ui.getFrom()).append("]").append(".id").append(" = ?");
//		QueryParams criteria = QueryParams.getInstance().where(where.toString()).params(new String [] {ui.getId()});
//		List<ODocument> resultList= super.get(criteria);
//		if (logger.isDebugEnabled()) logger.debug("Found "+resultList.size() +" elements for given tokens");
//		if (resultList!=null && resultList.size()>0) result = resultList.get(0);
//
//		return result;
//	}

	public void disableUser(String username) throws UserNotFoundException{
		db = DbHelper.reconnectAsAdmin();
		OUser user = db.getMetadata().getSecurity().getUser(username);
		if (user==null) throw new UserNotFoundException("The user " + username + " does not exist.");
		user.setAccountStatus(STATUSES.SUSPENDED);
		user.save();
		//cannot resume the old connection because now the user is disabled
	}
	
	public void enableUser(String username) throws UserNotFoundException{
		db = DbHelper.reconnectAsAdmin();
		OUser user = db.getMetadata().getSecurity().getUser(username);
		if (user==null) throw new UserNotFoundException("The user " + username + " does not exist.");
		user.setAccountStatus(STATUSES.ACTIVE);
		user.save();
	}

}

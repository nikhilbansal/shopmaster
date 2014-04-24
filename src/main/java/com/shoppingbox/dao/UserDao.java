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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.List;


public class UserDao extends NodeDao  {
    final static Logger logger = LoggerFactory.getLogger(UserDao.class);

	public final static String CLASS_NAME="OUser";
	private final static String USER_NAME_INDEX = "ouser.name";

	public final static String USER_PUSH_TOKEN="pushToken";
	public final static String USER_DEVICE_OS="os";
	public final static String USER_LOGIN_INFO="login_info";
	public final static String SOCIAL_LOGIN_INFO="sso_tokens";

	public final static String ATTRIBUTES_SYSTEM="system";
	public static final String GENERATED_USERNAME = "generated_username";

	public static UserDao getInstance(){
		return new UserDao();
	}

	protected UserDao() {
		super(CLASS_NAME);
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
		if (existsUserName(username)) throw new UserAlreadyExistsException("User " + username + " already exists");
		OUser user=null;
		if (role==null) user=db.getMetadata().getSecurity().createUser(username,password,new
				String[]{DefaultRoles.REGISTERED_USER.toString()});
		else {
			ORole orole = RoleDao.getRole(role);
			if (orole==null) throw new InvalidParameterException("Role " + role + " does not exists");
			user=db.getMetadata().getSecurity().createUser(username,password,new String[]{role});
		}
        return null;
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

//    public interface AccountDAO{
//        public boolean save(Account account);
//        public boolean update(Account account);
//        public boolean findByAccountNumber(int accountNumber);
//        public boolean delete(Account account);
//
//    }

}

package com.shoppingbox.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.shoppingbox.BBConfiguration;
import com.shoppingbox.dao.GenericDao;
import com.shoppingbox.dao.ResetPwdDao;
import com.shoppingbox.dao.RoleDao;
import com.shoppingbox.dao.UserDao;
import com.shoppingbox.dao.exception.InvalidClassException;
import com.shoppingbox.dao.exception.ResetPasswordException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.enumerations.DefaultRoles;
import com.shoppingbox.exception.UserNotFoundException;
import com.shoppingbox.util.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserService {

    final static Logger logger = LoggerFactory.getLogger(DbHelper.class);

	public static void createDefaultUsers(){
		try{
            logger.info("Creating baasbox user");
			//the shopping default user used to connect to the DB like anonymous user
			String username=BBConfiguration.getBaasBoxUsername();
			String password=BBConfiguration.getBaasBoxPassword();
			UserService.signUp(username, password,new Date(),DefaultRoles.ANONYMOUS_USER.toString(), null,null,null,null,false);
	
			//the shoppingbox default user used to act internally as the administrator
			username=BBConfiguration.getBaasBoxAdminUsername();
			password=BBConfiguration.getBaasBoxAdminPassword();
			UserService.signUp(username, password,new Date(),DefaultRoles.ADMIN.toString(), null,null,null,null,false);

			moveUserToRole("admin",DefaultRoles.BASE_ADMIN.toString(), DefaultRoles.ADMIN.toString());
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	public static List<ODocument> getUsers(QueryParams criteria) throws SqlInjectionException{
		UserDao dao = UserDao.getInstance();
		return dao.get(criteria);
	}



	
	public static ODocument getCurrentUser() throws SqlInjectionException{
		return getUserProfilebyUsername(DbHelper.getCurrentUserNameFromConnection());
	}

	public static OUser getOUserByUsername(String username){
		return DbHelper.getConnection().getMetadata().getSecurity().getUser(username);	
	}
	
	public static ODocument getUserProfilebyUsername(String username) throws SqlInjectionException{
		UserDao dao = UserDao.getInstance();
		ODocument userDetails=null;
		userDetails=dao.getByUserName(username);
		return userDetails;
	}
	
	public static String getUsernameByProfile(ODocument profile) throws InvalidClassException {
		UserDao dao = UserDao.getInstance();
		dao.checkModelDocument(profile);
		return (String)((ODocument)profile.field("user")).field("name");
	}

	public static ODocument  signUp (
			String username,
			String password,
			Date signupDate,
			JsonNode nonAppUserAttributes,
			JsonNode privateAttributes,
			JsonNode friendsAttributes,
			JsonNode appUsersAttributes,
			boolean generated) throws Exception{
		return signUp (
				username,
				password,
				signupDate,
				null,
				nonAppUserAttributes,
				privateAttributes,
				friendsAttributes,
				appUsersAttributes,
				generated) ;
	}

	public static void registerDevice(HashMap<String,Object> data) throws SqlInjectionException{
		ODocument user=getCurrentUser();
		ODocument systemProps=user.field(UserDao.ATTRIBUTES_SYSTEM);
		ArrayList<ODocument> loginInfos=systemProps.field(UserDao.USER_LOGIN_INFO);
		String pushToken=(String) data.get(UserDao.USER_PUSH_TOKEN);
		boolean found=false;
		for (ODocument loginInfo : loginInfos){

			if (loginInfo.field(UserDao.USER_PUSH_TOKEN)!=null && loginInfo.field(UserDao.USER_PUSH_TOKEN).equals(pushToken)){
				found=true;
				break;
			}
		}
		if (!found){
			loginInfos.add(new ODocument(data));
			systemProps.save();
		}
	}
	public static void unregisterDevice(String pushToken) throws SqlInjectionException{
		ODocument user=getCurrentUser();
		ODocument systemProps=user.field(UserDao.ATTRIBUTES_SYSTEM);
		ArrayList<ODocument> loginInfos=systemProps.field(UserDao.USER_LOGIN_INFO);
		for (ODocument loginInfo : loginInfos){
			if (loginInfo.field(UserDao.USER_PUSH_TOKEN)!=null && loginInfo.field(UserDao.USER_PUSH_TOKEN).equals(pushToken)){
				loginInfos.remove(loginInfo);
				break;
			}
		}
		systemProps.save();
	}
	
	

	public static void logout(String pushToken) throws SqlInjectionException {
		ODocument user=getCurrentUser();
		ODocument systemProps=user.field(UserDao.ATTRIBUTES_SYSTEM);
		ArrayList<ODocument> loginInfos=systemProps.field(UserDao.USER_LOGIN_INFO);
		for (ODocument loginInfo : loginInfos){
			if (loginInfo.field(UserDao.USER_PUSH_TOKEN)!=null && loginInfo.field(UserDao.USER_PUSH_TOKEN).equals(pushToken)){
				loginInfos.remove(loginInfo);
				break;
			}
		}
		systemProps.save();
	}

	public static ODocument  signUp (
            String username,
            String password,
            Date signupDate,
            String role,
            JsonNode nonAppUserAttributes,
            JsonNode privateAttributes,
            JsonNode friendsAttributes,
            JsonNode appUsersAttributes,boolean generated) throws Exception {

        ODatabaseRecordTx db = DbHelper.getConnection();
        UserDao dao = UserDao.getInstance();
        try {
            if (role == null) dao.create(username, password);
            else dao.create(username, password, role);
        } catch (Exception e) {
            DbHelper.rollbackTransaction();
            throw e;
        }
        return null;
    }

	public static void changePasswordCurrentUser(String newPassword) {
		ODatabaseRecordTx db = DbHelper.getConnection();
		String username=db.getUser().getName();
		db = DbHelper.reconnectAsAdmin();
		db.getMetadata().getSecurity().getUser(username).setPassword(newPassword).save();
		//DbHelper.removeConnectionFromPool();
	}
	
	public static void changePassword(String username, String newPassword) throws SqlInjectionException, UserNotFoundException {
		ODatabaseRecordTx db=DbHelper.getConnection();
		db = DbHelper.reconnectAsAdmin();
		UserDao udao=UserDao.getInstance();
		ODocument user = udao.getByUserName(username);
		if(user==null){
			if (logger.isDebugEnabled()) logger.debug("User " + username + " does not exist");
			throw new UserNotFoundException("User " + username + " does not exist");
		}
		db.getMetadata().getSecurity().getUser(username).setPassword(newPassword).save();
	}

	public static boolean exists(String username) {
		UserDao udao=UserDao.getInstance();
		return udao.existsUserName(username);
	}

	public static void resetUserPasswordFinalStep(String username, String newPassword) throws SqlInjectionException, ResetPasswordException {
		ODocument user = UserDao.getInstance().getByUserName(username);
		ODocument ouser = ((ODocument) user.field("user"));
		ouser.field("password",newPassword).save();
		ResetPwdDao.getInstance().setResetPasswordDone(username);
	}

	public static void removeSocialLoginTokens(ODocument user , String socialNetwork) throws ODatabaseException{
		DbHelper.requestTransaction();
		try{
			ODocument systemProps=user.field(UserDao.ATTRIBUTES_SYSTEM);
			Map<String,ODocument>  ssoTokens = systemProps.field(UserDao.SOCIAL_LOGIN_INFO);
			if(ssoTokens == null){
				throw new ODatabaseException(socialNetwork + " is not linked with this account");
			}else{
				ssoTokens.remove(socialNetwork);
				systemProps.field(UserDao.SOCIAL_LOGIN_INFO,ssoTokens);
				user.field(UserDao.ATTRIBUTES_SYSTEM,systemProps);
				systemProps.save();
				user.save();
				if (logger.isDebugEnabled()) logger.debug("saved tokens for user ");
				DbHelper.commitTransaction();
			}
		}catch(Exception e){
			e.printStackTrace();
			DbHelper.rollbackTransaction();
			throw new ODatabaseException("unable to add tokens");
		}

	}
	
	public static void moveUsersToRole(String from, String to) {
		String sqlAdd="update ouser add roles = {TO_ROLE} where roles contains {FROM_ROLE}";
		String sqlRemove="update ouser remove roles = {FROM_ROLE} where roles contains {FROM_ROLE}";
		ORole fromRole=RoleDao.getRole(from);
		ORole toRole=RoleDao.getRole(to);
		
		ORID fromRID=fromRole.getDocument().getRecord().getIdentity();
		ORID toRID=toRole.getDocument().getRecord().getIdentity();
		
		sqlAdd=sqlAdd.replace("{TO_ROLE}", toRID.toString()).replace("{FROM_ROLE}", fromRID.toString());
		sqlRemove=sqlRemove.replace("{TO_ROLE}", toRID.toString()).replace("{FROM_ROLE}", fromRID.toString());
		
		GenericDao.getInstance().executeCommand(sqlAdd, new String[] {});
		GenericDao.getInstance().executeCommand(sqlRemove, new String[] {});
	}
	
	public static void addUserToRole(String username,String role){
		boolean admin = true;
		if(!DbHelper.currentUsername().equals(BBConfiguration.getBaasBoxAdminUsername())){
			DbHelper.reconnectAsAdmin();
			admin = false;
		}
		String sqlAdd="update ouser add roles = {TO_ROLE} where name = ?";
		ORole toRole=RoleDao.getRole(role);
		ORID toRID=toRole.getDocument().getRecord().getIdentity();
		sqlAdd=sqlAdd.replace("{TO_ROLE}", toRID.toString());
		GenericDao.getInstance().executeCommand(sqlAdd, new String[] {username});
		if(!admin){
			DbHelper.reconnectAsAuthenticatedUser();
		}
		
	}
	
	public static void removeUserFromRole(String username,String role){
		boolean admin = false;
		if(!DbHelper.currentUsername().equals(BBConfiguration.getBaasBoxAdminUsername())){
			DbHelper.reconnectAsAdmin();
			admin = true;
		}
		String sqlRemove="update ouser remove roles = {FROM_ROLE} where roles contains {FROM_ROLE} and name = ?";
		ORole fromRole=RoleDao.getRole(role);
		ORID fromRID=fromRole.getDocument().getRecord().getIdentity();
		sqlRemove=sqlRemove.replace("{FROM_ROLE}", fromRID.toString());
		GenericDao.getInstance().executeCommand(sqlRemove, new String[] {username});
		if(admin){
			DbHelper.reconnectAsAuthenticatedUser();
		}
	}
	
	public static void moveUserToRole(String username,String from, String to) {
		String sqlAdd="update ouser add roles = {TO_ROLE} where roles contains {FROM_ROLE} and name = ?";
		String sqlRemove="update ouser remove roles = {FROM_ROLE} where roles contains {FROM_ROLE} and name = ?";
		
		ORole fromRole=RoleDao.getRole(from);
		ORole toRole=RoleDao.getRole(to);
		
		ORID fromRID=fromRole.getDocument().getRecord().getIdentity();
		ORID toRID=toRole.getDocument().getRecord().getIdentity();
		
		sqlAdd=sqlAdd.replace("{TO_ROLE}", toRID.toString()).replace("{FROM_ROLE}", fromRID.toString());
		sqlRemove=sqlRemove.replace("{TO_ROLE}", toRID.toString()).replace("{FROM_ROLE}", fromRID.toString());

		GenericDao.getInstance().executeCommand(sqlAdd, new String[] {username});
		GenericDao.getInstance().executeCommand(sqlRemove, new String[] {username});
	}
	
	
	
	public static void disableUser(String username) throws UserNotFoundException{
		UserDao.getInstance().disableUser(username);
	}

	public static void disableCurrentUser() throws UserNotFoundException{
		String username = DbHelper.currentUsername();
		disableUser(username);
	}
	
	public static void enableUser(String username) throws UserNotFoundException{
		UserDao.getInstance().enableUser(username);
	}

	public static List<ODocument> getUserProfilebyUsernames(List<String> usernames) throws SqlInjectionException {
		return UserDao.getInstance().getByUsernames(usernames);
		
	}

}

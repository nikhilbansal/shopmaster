package com.shoppingbox.service.user;

import com.shoppingbox.dao.RoleDao;
import com.shoppingbox.dao.UserDao;
import com.shoppingbox.dao.exception.InvalidCriteriaException;
import com.shoppingbox.dao.exception.SqlInjectionException;
import com.shoppingbox.util.QueryParams;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FriendShipService {
	public static final String FRIEND_ROLE_NAME=RoleDao.FRIENDS_OF_ROLE;
	public static final String WHERE_FRIENDS="(" + UserDao.USER_LINK + ".roles contains (name=?))";
	
	private static String getWhereFromCriteria(QueryParams criteria){
		String where=WHERE_FRIENDS;
		if (!StringUtils.isEmpty(criteria.getWhere())) where += " and (" + criteria.getWhere() + ")";
		return where;
	}
	
	private static Object[] addFriendShipRoleToCriteria(QueryParams criteria, String friendShipRole){
		Object[] params = criteria.getParams();
		Object[] newParams = new Object[] {friendShipRole};
		Object[] veryNewParams = ArrayUtils.addAll(newParams, params);
		return veryNewParams;
	}
	
	public static List<ODocument> getFriendsOf(String username, QueryParams criteria) throws InvalidCriteriaException, SqlInjectionException {
		String friendShipRole=RoleDao.getFriendRoleName(username);
		criteria.where(getWhereFromCriteria(criteria));
		criteria.params(addFriendShipRoleToCriteria(criteria, friendShipRole));
		UserDao udao= UserDao.getInstance();
		return udao.get(criteria);
	}

	public static long getCountFriendsOf(String username, QueryParams criteria) throws InvalidCriteriaException, SqlInjectionException {
		String friendShipRole=RoleDao.getFriendRoleName(username);
		criteria.where(getWhereFromCriteria(criteria));
		criteria.params(addFriendShipRoleToCriteria(criteria, friendShipRole));
		UserDao udao= UserDao.getInstance();
		return udao.getCount(criteria);
	}
}

package com.shoppingbox.configuration.index;

import com.shoppingbox.dao.IndexDao;
import com.shoppingbox.exception.IndexNotFoundException;

public class IndexPasswordRecoveryConfiguration extends IndexDao {
	private final static String indexName="_bb_password_recovery";
	
	public IndexPasswordRecoveryConfiguration() throws IndexNotFoundException{
		super (indexName);
	}


	
}

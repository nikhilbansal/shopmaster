package com.shoppingbox.db.async;

import com.shoppingbox.db.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportJob implements Runnable{

    final static Logger logger = LoggerFactory.getLogger(ImportJob.class);

	private String content;
	private String appcode;
	byte[] buffer = new byte[2048];
	public ImportJob(String appcode,String content){
		this.content = content;
		this.appcode = appcode;
	}

	@Override
	public void run() {
		try{    
			DbHelper.importData(appcode,content);
		}catch(Exception e){
            logger.error(e.getMessage());
			throw new RuntimeException(e);
		}

	}



}

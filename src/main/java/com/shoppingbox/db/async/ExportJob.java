package com.shoppingbox.db.async;

import com.shoppingbox.BBConfiguration;
import com.shoppingbox.BBInternalConstants;
import com.shoppingbox.db.DbHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//public class ExportJob implements Runnable{
public class ExportJob{

    final static Logger logger = LoggerFactory.getLogger(ExportJob.class);	
	private String fileName;
	private String appcode;
	public ExportJob(String fileName,String appcode){
		this.fileName = fileName;
		this.appcode = appcode;
	}
	
//	@Override
	public void run() {
		FileOutputStream dest = null;
		ZipOutputStream zip = null;
		try{
			File f = new File(this.fileName);
			dest = new FileOutputStream(f);
			zip = new ZipOutputStream(dest);
			
			ByteArrayOutputStream content =new ByteArrayOutputStream();
            DbHelper.exportData(this.appcode,content);

            byte[] contentArr = content.toByteArray();
			logger.info(String.format("Writing %d bytes ",contentArr.length));
			File tmpJson = File.createTempFile("export", ".json");
			File manifest = File.createTempFile("manifest", ".txt");
			
			
			
			FileUtils.writeByteArrayToFile(tmpJson, contentArr, false);
			FileUtils.writeStringToFile(manifest, BBInternalConstants.IMPORT_MANIFEST_VERSION_PREFIX+BBConfiguration.getApiVersion());
			
			ZipEntry entry = new ZipEntry("export.json");
			zip.putNextEntry(entry);
    		zip.write(FileUtils.readFileToByteArray(tmpJson));
    		zip.closeEntry();
    		
    		ZipEntry entryManifest = new ZipEntry("manifest.txt");
			zip.putNextEntry(entryManifest);
    		zip.write(FileUtils.readFileToByteArray(manifest));
    		zip.closeEntry();
    		
    		tmpJson.delete();
    		manifest.delete();
    		content.close();
    		

		}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			try{
				if(zip!=null)
					zip.close();
				if(dest!=null)
					dest.close();
			
			}catch(Exception ioe){
				ioe.getStackTrace();
				logger.error(ioe.getMessage());
			}
		}
	}
	
	
	

}

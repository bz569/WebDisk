package com.webdisk.util;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.mail.NoSuchProviderException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;
import com.webdisk.mail.CS_console_new;

public class UploadUtil
{
	private static final String TAG = "UploadUtil";
	
	// TODO 需要修改为从.config.db中读取
	private static String[] USERNAME = {"cyberbox1@163.com","cyberbox2@163.com","cyberbox3@163.com","cyberbox4@163.com","cyberbox5@163.com"};
	private static String[] PASSWORD = {"cyberbox","cyberbox","cyberbox","cyberbox","cyberbox"};
	private static String SERVER = "imap.163.com";
	private static String USER_ID = "363";
	private static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";
	
	

//	private static final int IMPORT_START = 200;
//	private static final int IMPORT_FINISH = 202;
	private static final int IMPORT = 200;
	
	private SVNApplication uploadApp;
	
	private String srcFilePath;
	private String dstUrl;
	private String fileName;
	private String suffix;
	private File srcFile;
	private String fileID;
	private SVNProperties mProperties;
	
	private Handler uploadhandler;
	
	public UploadUtil(SVNApplication uploadApp, Handler uploadHandler, String srcFilePath, String dstUrl)
	{
		this.uploadApp = uploadApp;
		this.uploadhandler = uploadHandler;
		this.srcFilePath = srcFilePath;
		this.dstUrl = dstUrl;
		
//		File tmpfile = new File(srcFilePath);
		this.srcFile = new File(srcFilePath);
		this.fileName = srcFile.getName();
		this.suffix = fileName.substring(fileName.lastIndexOf(".")+1);
		
		this.fileID = FileUtil.genFileId();
		
		//设置SVNProperties
		mProperties = new SVNProperties();
		
		mProperties.put("magicgourd:id", USER_ID + fileID);
		mProperties.put("magicgourd:owner", uploadApp.getCurrentConnection().getUsername());
		mProperties.put("magicgourd:size", Long.toString(srcFile.length()));
			//生成timestamp
		Timestamp t = new Timestamp(new Date().getTime());
		mProperties.put("magicgourd:timestamp", t.toString());
		
		Log.i(TAG, "UploadInfo:srcFilePath=" + srcFilePath + ";dstUrl=" + dstUrl + ";fileName=" + fileName
					+ ";suffix=" + suffix);
	}
	

	public void startUpload()
	{
		if((srcFile.length() <= 5242880) && ("txt".equals(suffix) || "doc".equals(suffix) || "docx".equals(suffix) || "pdf".equals(suffix) 
				|| "ppt".equals(suffix) || "pptx".equals(suffix)))// TODO 直接上传到SVN服务器的文件类型
		{
			mProperties.put("magicgourd:ismail", Integer.toString(0));
			importToSVN();
		}
		else
		{
			mProperties.put("magicgourd:ismail", Integer.toString(1));
			uploadToMail();
		}
	}
		

	private void importToSVN()
	{
		new ImportThread().start();
	}
	
	class ImportThread extends Thread
	{

		@Override
		public void run()
		{
//			Message importStartMsg = new Message();
//			importStartMsg.what = IMPORT_START;
//			uploadhandler.sendMessage(importStartMsg);
//			
//			uploadApp.doImport(srcFilePath, dstUrl);
//			
//			Message importFinishMsg = new Message();
//			importStartMsg.what = IMPORT_FINISH;
//			uploadhandler.sendMessage(importStartMsg);
			
			Message importMsg = new Message();
			importMsg.what = IMPORT;
			importMsg.arg1 = 0;
			uploadhandler.sendMessage(importMsg);
			
			uploadApp.doImport(srcFilePath, dstUrl, mProperties);
			
			importMsg = uploadhandler.obtainMessage(IMPORT);
			importMsg.arg1 = 1;
			uploadhandler.sendMessage(importMsg);
			
			super.run();
		}
		
	}
	
	private void uploadToMail()
	{
		File tmpFile = new File(CACHE_DIR + fileName);
		try
		{
			tmpFile.createNewFile();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		final String tmpFilePath = tmpFile.getPath();
		
		final String mID = USER_ID + fileID;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Message importMsg = new Message();
				importMsg.what = IMPORT;
				importMsg.arg1 = 0;
				uploadhandler.sendMessage(importMsg);
				
				CS_console_new mMailSender = null;
				try
				{
					mMailSender = new CS_console_new(USERNAME, PASSWORD, SERVER);
				} catch (NoSuchProviderException e)
				{
					e.printStackTrace();
				}
				
				mMailSender.send(mID, srcFilePath, CACHE_DIR);
				
				Log.i(TAG, "上传空文件至svn：" + tmpFilePath);
				uploadApp.doImport(tmpFilePath, dstUrl, mProperties);
				File cacheFile = new File(tmpFilePath);
				cacheFile.delete();
				
				importMsg = uploadhandler.obtainMessage(IMPORT);
				importMsg.arg1 = 1;
				uploadhandler.sendMessage(importMsg);
			}
		}).start();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	public SVNApplication getUploadApp()
	{
		return uploadApp;
	}

	public void setUploadApp(SVNApplication uploadApp)
	{
		this.uploadApp = uploadApp;
	}

	public String getSrcFilePath()
	{
		return srcFilePath;
	}

	public void setSrcFilePath(String srcFilePath)
	{
		this.srcFilePath = srcFilePath;
	}

	public String getDstUrl()
	{
		return dstUrl;
	}

	public void setDstUrl(String desUrl)
	{
		this.dstUrl = desUrl;
	}

	public String getFileName()
	{
		return fileName;
	}


	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}


	public Handler getUploadhandler()
	{
		return uploadhandler;
	}

	public void setUploadhandler(Handler uploadhandler)
	{
		this.uploadhandler = uploadhandler;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}
	
	
	
}

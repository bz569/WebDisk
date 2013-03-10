package com.webdisk.util;

import java.io.File;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;

public class UploadUtil
{
	private static final String TAG = "UploadUtil";
	
//	private static final int IMPORT_START = 200;
//	private static final int IMPORT_FINISH = 202;
	private static final int IMPORT = 200;
	
	private SVNApplication uploadApp;
	
	private String srcFilePath;
	private String dstUrl;
	private String fileName;
	private String suffix;
	
	private Handler uploadhandler;
	
	public UploadUtil(SVNApplication uploadApp, Handler uploadHandler, String srcFilePath, String dstUrl)
	{
		this.uploadApp = uploadApp;
		this.uploadhandler = uploadHandler;
		this.srcFilePath = srcFilePath;
		this.dstUrl = dstUrl;
		
		File tmpfile = new File(srcFilePath);
		this.fileName = tmpfile.getName();
		this.suffix = fileName.substring(fileName.lastIndexOf(".")+1);
		
		Log.i(TAG, "UploadInfo:srcFilePath=" + srcFilePath + ";dstUrl=" + dstUrl + ";fileName=" + fileName
					+ ";suffix=" + suffix);
	}
	

	public void startUpload()
	{
		if("txt".equals(suffix) || "doc".equals(suffix) || "docx".equals(suffix) || "pdf".equals(suffix) 
				|| "ppt".equals(suffix) || "pptx".equals(suffix))// TODO 直接上传到SVN服务器的文件类型
		{
			importToSVN();
		}
		else
		{
			//　TODO 上传至邮箱
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
			
			uploadApp.doImport(srcFilePath, dstUrl);
			
			importMsg = uploadhandler.obtainMessage(IMPORT);
			importMsg.arg1 = 1;
			uploadhandler.sendMessage(importMsg);
			
			super.run();
		}
		
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

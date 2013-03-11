package com.webdisk.util;

import java.security.acl.LastOwnerException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;

public class CopyUtil
{
	private static final String TAG = "CopyUtil";
	
	private static final int COPY_MSG = 13;
	private static final int COPY_START = 133;
	private static final int COPY_SUCCESS = 131;
	private static final int COPY_ERROR = 130;
	
	private SVNApplication copyApp;
	
	private String srcPath;
	private String dstPath;
	private String suffix;
	private String fileName;
	private Handler copyServiceHandler;
	
	
	public CopyUtil(SVNApplication copyApp, Handler copyServiceHandler, String srcPath, String dstPath)
	{
		this.copyApp = copyApp;
		this.copyServiceHandler = copyServiceHandler;
		this.srcPath = srcPath;
		this.dstPath = dstPath;
		
		this.fileName = srcPath.substring(srcPath.lastIndexOf("/") + 1, srcPath.length());
		this.suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		
		Log.i(TAG, "New CopyUtil: " + "srcPath=" + srcPath + ";dstPath=" + dstPath 
				+ ";filename=" + fileName + ";suffix=" + suffix);
	}
	
	public void startCopy()
	{
		if("txt".equals(suffix) || "doc".equals(suffix) || "docx".equals(suffix) || "pdf".equals(suffix) 
				|| "ppt".equals(suffix) || "pptx".equals(suffix))// TODO 直接上传到SVN服务器的文件类型
		{
			copyOnSVN();
		}
	}

	private void copyOnSVN()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Message copyMsg = new Message();
				copyMsg.what = COPY_MSG;
				copyMsg.arg1 = COPY_START;
				copyServiceHandler.sendMessage(copyMsg);
				
				if(copyApp.doCopy(srcPath, dstPath))
				{
					copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
					copyMsg.arg1 = COPY_SUCCESS;
					copyServiceHandler.sendMessage(copyMsg);
				}
				else
				{
					copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
					copyMsg.arg1 = COPY_ERROR;
					copyServiceHandler.sendMessage(copyMsg);
				}
			}
		}).start();
	}
	
	
	
	// TODO Delete From Mail
}

package com.webdisk.util;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;

public class DownloadUtil
{
	private static final String TAG = "DownloadUtil";
	
	private static final int EXPORT_START = 0;
	private static final int EXPORT_FINISH = 2;
	
	
	
	private SVNApplication downloadApp;
	
	private Handler downloadHandler;
	
	private String srcUrl;
	public String getSrcUrl()
	{
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl)
	{
		this.srcUrl = srcUrl;
	}

	public String getDesPath()
	{
		return desPath;
	}

	public void setDesPath(String desPath)
	{
		this.desPath = desPath;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}

	private String desPath;
	private String fileName;
	private String suffix;
	
	
	public DownloadUtil(SVNApplication mApp, Handler downloadHandler , String srcUrl, String desPath)
	{
		this.downloadApp = mApp;
		this.downloadHandler = downloadHandler;
		this.srcUrl = srcUrl;
		this.desPath = desPath;

		String[] tmp = srcUrl.split("/");
		fileName = tmp[tmp.length-1];
		
		tmp = fileName.split("\\.");
		suffix = tmp[tmp.length-1];
		
		Log.i(TAG, "application=" + downloadApp + ";DownloadFileInfo:url=" + srcUrl + ";desPath=" + desPath + ";fileName=" + fileName + ";suffix=" + suffix);
	}
	
	public void startDownload() 
	{
		if("txt".equals(suffix) || "doc".equals(suffix) || "docx".equals(suffix) || "pdf".equals(suffix) 
				|| "ppt".equals(suffix) || "pptx".equals(suffix))// TODO 添加直接从SVN服务器下载的文件类型
		{
			exportFormSVN();
		}
		else
		{
			// TODO 从邮箱下载
		}
	}

	private void exportFormSVN()
	{
		
		new ExportThread().start();
	}
	
	private class ExportThread extends Thread 
	{

		@Override
		public void run()
		{
			File sdPath = new File(desPath);
			SVNURL svnUrl = null;
			try
			{
				svnUrl = SVNURL.parseURIEncoded(srcUrl);
			} catch (SVNException e)
			{
				e.printStackTrace();
			}
			
			Message downloadStartMsg = new Message();
			downloadStartMsg.what = EXPORT_START;
			downloadHandler.sendMessage(downloadStartMsg);
			
			downloadApp.doExport(SVNRevision.HEAD, sdPath, svnUrl);
			
			Message downloadFinishMsg = new Message();
			downloadFinishMsg.what = EXPORT_FINISH;
			downloadHandler.sendMessage(downloadFinishMsg);
			
			super.run();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}





























package com.webdisk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;

public class DownloadUtil
{
	private static final String TAG = "DownloadUtil";
	
	private static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";
	
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
//		if("txt".equals(suffix) || "doc".equals(suffix) || "docx".equals(suffix) || "pdf".equals(suffix) 
//				|| "ppt".equals(suffix) || "pptx".equals(suffix))// TODO 添加直接从SVN服务器下载的文件类型
//		{
//			exportFormSVN();
//		}
//		else
//		{
//			// TODO 从邮箱下载
//		}
		//发送开始下载消息
		Message downloadStartMsg = new Message();
		downloadStartMsg.what = EXPORT_START;
		downloadHandler.sendMessage(downloadStartMsg);
		
		//从SVN获取目标文件props
		String isMail = downloadApp.doGetProperty(srcUrl, "magicgourd:ismail");
		String mId = downloadApp.doGetProperty(srcUrl, "magicgourd:id");
		Log.i(TAG, "getProp: isMail = " + isMail + ";id=" + mId);
		
		if(isMail.equals("0"))
		{
			exportFromSVN();
			
		}
		else if(isMail.equals("1"))
		{
			// TODO 从邮箱下载
			Log.i(TAG, "从邮箱下载");
		}
		else if(isMail.equals("PROP_NOT_EXSIT"))//从web端上传的文件没有svn properties
		{
			//将文件export至cache文件夹
			File cacheFile = new File(CACHE_DIR + fileName);
			
			SVNURL svnUrl = null;
			try
			{
				svnUrl = SVNURL.parseURIEncoded(srcUrl);
			} catch (SVNException e)
			{
				e.printStackTrace();
			}
			downloadApp.doExport(SVNRevision.HEAD, cacheFile, svnUrl);
			
			String firstLine = "";
			try
			{
				FileReader fr = new FileReader(cacheFile);
				BufferedReader br = new BufferedReader(fr);
				firstLine = br.readLine();
				
				br.close();
				fr.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
//			String judgeString = firstLine.substring(0, 10);
//			Log.i(TAG, "judgeStr=" + judgeString);
//			if(!judgeString.equals("magicgourd"))
			if(!firstLine.contains("magicgourd"))
			{
				//　TODO 将cache文件直接移动到目标文件夹
				boolean result = cacheFile.renameTo(new File(desPath + "/" + fileName));
				Log.i(TAG, "直接移动cache文件至：" + desPath + "/" + fileName + "result=" + result);
				
				//清楚生成的cacheFile，发送完成消息
				cacheFile.delete();
				Message downloadFinishMsg = new Message();
				downloadFinishMsg.what = EXPORT_FINISH;
				downloadHandler.sendMessage(downloadFinishMsg);
			}
			else
			{
				// TODO 从邮箱下载
				Log.i(TAG, "从邮箱下载");
			}
		}
		
	}

	private void exportFromSVN()
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
			
//			Message downloadStartMsg = new Message();
//			downloadStartMsg.what = EXPORT_START;
//			downloadHandler.sendMessage(downloadStartMsg);
//			
			downloadApp.doExport(SVNRevision.HEAD, sdPath, svnUrl);
			
			Message downloadFinishMsg = new Message();
			downloadFinishMsg.what = EXPORT_FINISH;
			downloadHandler.sendMessage(downloadFinishMsg);
			
			super.run();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}





























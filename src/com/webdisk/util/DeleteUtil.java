package com.webdisk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.mail.NoSuchProviderException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;
import com.webdisk.mail.CS_console_new;

public class DeleteUtil
{
	private static final String TAG = "DeleteUtil";
	
	private static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";
	
	private static String[] USERNAME = {"cyberbox1@163.com","cyberbox2@163.com","cyberbox3@163.com","cyberbox4@163.com","cyberbox5@163.com"};
	private static String[] PASSWORD = {"cyberbox","cyberbox","cyberbox","cyberbox","cyberbox"};
	private static String SERVER = "imap.163.com";

	private SVNApplication deleteApp;
	
	private String deleteUrl;
	private String deleteFileName;
	
	public DeleteUtil(SVNApplication deleteApp, String deleteUrl)
	{
		this.deleteApp = deleteApp;
		this.deleteUrl = deleteUrl;
		
		String[] tmp = deleteUrl.split("/");
		this.deleteFileName = tmp[tmp.length-1];
		
		Log.i(TAG, "delete：url=" + this.deleteUrl + ";name=" + deleteFileName);
		
	}
	
	public boolean doDelete()
	{
		String isMail = deleteApp.doGetProperty(deleteUrl, "magicgourd:ismail");
		String mId = deleteApp.doGetProperty(deleteUrl, "magicgourd:id");
		
		
		if(isMail.equals("1"))
		{
			// TODO 从邮箱删除
			Log.i(TAG, "从邮箱删除");
			CS_console_new mMaildeleter;
			try
			{
				mMaildeleter = new CS_console_new(USERNAME, PASSWORD, SERVER);
				mMaildeleter.delete(mId, true);
			} catch (NoSuchProviderException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else if(isMail.equals("PROP_NOT_EXSIT"))//从web端上传的文件没有svn properties
		{
			//将文件export至cache文件夹
			File cacheFile = new File(CACHE_DIR + deleteFileName);
			
			SVNURL svnUrl = null;
			try
			{
				svnUrl = SVNURL.parseURIEncoded(deleteUrl);
			} catch (SVNException e)
			{
				e.printStackTrace();
				return false;
			}
			deleteApp.doExport(SVNRevision.HEAD, cacheFile, svnUrl);
			
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
				return false;
			} catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			
			if(firstLine.contains("magicgourd"))
			{
				try
				{
					FileReader fr = new FileReader(cacheFile);
					BufferedReader br = new BufferedReader(fr);
					String line = "";
					while ((line=br.readLine())!=null) 
					{
						if(line.contains("magicgourd:id"))
						{
							String[] tmp = line.split("==");
							mId = tmp[tmp.length-1];
							break;
						}
			        }
					
					br.close();
					fr.close();
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
					return false;
				} catch (IOException e)
				{
					e.printStackTrace();
					return false;
				}
				
				// TODO 从邮箱删除，删除临时文件
				CS_console_new mMaildeleter;
				try
				{
					mMaildeleter = new CS_console_new(USERNAME, PASSWORD, SERVER);
					mMaildeleter.delete(mId, true);
				} catch (NoSuchProviderException e)
				{
					e.printStackTrace();
					return false;
				}
				cacheFile.delete();
				
			}
		}
		
		return doDeleteFormSVN();
	}
	
	private boolean doDeleteFormSVN()
	{
		Log.i(TAG, "Delete " + this.deleteFileName + " from " + this.deleteUrl);
		return deleteApp.doDelete(deleteUrl, deleteFileName);
	}
	
	// TODO Delete From Mail
}

package com.webdisk.util;

import android.util.Log;

import com.webdisk.application.SVNApplication;

public class DeleteUtil
{
	private static final String TAG = "DeleteUtil";
	
	private SVNApplication deleteApp;
	
	private String deleteUrl;
	private String deleteFileName;
	
	public DeleteUtil(SVNApplication deleteApp, String deleteUrl)
	{
		this.deleteApp = deleteApp;
		this.deleteUrl = deleteUrl;
		
		String[] tmp = deleteUrl.split("/");
		this.deleteFileName = tmp[tmp.length-1];
		
	}
	
	public boolean doDeleteFormSVN()
	{
		Log.i(TAG, "Delete " + this.deleteFileName + " from " + this.deleteUrl);
		return deleteApp.doDelete(deleteUrl, deleteFileName);
	}
	
	// TODO Delete From Mail
}

package com.webdisk.model;

import java.util.ArrayList;

public class UserConfig
{
	private ArrayList<MailInfo> mailList;
	private String userID;
	
	public UserConfig(String userID, ArrayList<MailInfo> mailList)
	{
		this.userID = userID;
		this.mailList = mailList;
	}
	
	public void addMailInfo(MailInfo mailInfo)
	{
		mailList.add(mailInfo);
	}

	public ArrayList<MailInfo> getMailList()
	{
		return mailList;
	}

	public void setMailList(ArrayList<MailInfo> mailList)
	{
		this.mailList = mailList;
	}

	public String getUserID()
	{
		return userID;
	}

	public void setUserID(String userID)
	{
		this.userID = userID;
	}
	
	
}

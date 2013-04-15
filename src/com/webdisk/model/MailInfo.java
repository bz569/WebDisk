package com.webdisk.model;

public class MailInfo
{
	private String server_adr;
	private String username;
	private String password;
	
	public MailInfo()
	{
		this.server_adr = null;
		this.username = null;
		this.password = null;
	}
	
	public MailInfo(String server_adr, String username, String password)
	{
		this.server_adr = server_adr;
		this.username = username;
		this.password = password;
	}
	
	public String getServer_adr()
	{
		return server_adr;
	}
	public void setServer_adr(String server_adr)
	{
		this.server_adr = server_adr;
	}
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	
	
}

package com.webdisk.mail;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import android.util.Log;

public class SampleDelete {
	private String username,password,server,folderName;
	public SampleDelete(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
	public void deleteFuzzy(String Myid){//模糊删除  Myid中若有一部分匹配就删除此邮件
		Properties props = System.getProperties();
		Session session = Session.getInstance(props, null);
		Store store;
		try {
			store = session.getStore("imap");
			store.connect(this.server, this.username, this.password);
			Folder folder = store.getFolder(folderName);//"cyberbox"
			if(folder == null)
				Log.i("in","Folder is empty");
			folder.open(Folder.READ_WRITE);
			Message[] msgs = folder.getMessages();
			int i = 0 , num = msgs.length;//i = 1 , num = folder.getMessageCount();
			while(i  < num){
				Message msg = msgs[i];//folder.getMessage(i);
				Enumeration headers = msg.getAllHeaders();
				while (headers.hasMoreElements()) {
					Header h = (Header) headers.nextElement();
					String mID = h.getName();                
					if(mID.contains("Myid")){
//						System.out.println(mID+ ":" + h.getValue()+" "+i);
						if(this.isEqual(Myid, h.getValue())){//找到确定邮件//h.getValue().contains(Myid)
							System.out.println("删除：" + msg.getSubject() +" Myid" +h.getValue());
							msg.setFlag(Flags.Flag.DELETED, true);  
							//msg.saveChanges();							
						}
					}
				}	
				i++;
			}
			folder.close(false);  
			store.close();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
			
	}
		
	public void deleteDefine(String Myid_i){
		Properties props = System.getProperties();
		Session session = Session.getInstance(props, null);
		Store store;
		try {
			store = session.getStore("imap");
			store.connect(this.server, this.username, this.password);
			Folder folder = store.getFolder(folderName);//"cyberbox"
			if(folder == null)
				Log.i("in","Folder is empty");
			folder.open(Folder.READ_WRITE);
			Message[] msgs = folder.getMessages();
			int i = 0 , num = msgs.length;//i = 1 , num = folder.getMessageCount();
			//Log.i("in","num:" + num);
			while(i  < num){
				Message msg = msgs[i];//folder.getMessage(i);
				Enumeration headers = msg.getAllHeaders();
				while (headers.hasMoreElements()) {
					Header h = (Header) headers.nextElement();
					String mID = h.getName();                
					if(mID.contains("Myid")){
//						System.out.println(mID+ ":" + h.getValue()+" "+i);
						if(h.getValue().equals(Myid_i)){//找到确定邮件//h.getValue().contains(Myid)
							System.out.println("删除：" + msg.getSubject() +" Myid" +h.getValue());
							msg.setFlag(Flags.Flag.DELETED, true);  
							//msg.saveChanges();  
						}
					}
				}	
				i++;
			}
			folder.close(false);  
			store.close();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	private boolean isEqual(String Myid,String Myid_i){
		if(Myid_i.contains(":")){
			String newId = Myid_i.substring(0, Myid_i.lastIndexOf(":"));
			return newId.equals(Myid);
		}
		else
			return Myid_i.equals(Myid);
	}
	public static void main(String args[]){
		SampleDelete de = new SampleDelete("TestForLqq@163.com","19901111","imap.163.com","cyberbox");
		de.deleteDefine("24213532151:1");
	}
}



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

/**
 * 用于删除邮件的类
 * @author forever_xuan7
 *
 */
public class SampleDelete {
	private String username,password,server,folderName;
	
	/**
	 * 构造方法
	 * @param username 邮箱名
	 * @param password 邮箱密码
	 * @param server 邮箱服务器
	 * @param folderName 邮箱的文件夹名
	 */
	public SampleDelete(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
	
	/**
	 * 模糊删除 删除邮件id包含Myid的所有邮件
	 * @param Myid 需要删除的邮件所含的id
	 */
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
		
	/**
	 * 精确删除 删除邮件id匹配Myid的所有邮件
	 * @param Myid_i 需要删除的邮件的id
	 */
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
	
	/**
	 * 辅助方法 对两个字符串进行模糊匹配 若字符串的':'之前的元素均匹配 则返回true (例：this.isEqual("12434","12434:23")返回true,this.isEqual("12435","12434:23")返回true)
	 * @param Myid 不含有':'的字符串
	 * @param Myid_i 可能含有':'的字符串
	 * @return 匹配结果
	 */
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



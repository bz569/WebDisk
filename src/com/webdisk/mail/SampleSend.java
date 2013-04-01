package com.webdisk.mail;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;
/**
 * 用于发送邮件的类
 * @author forever_xuan7
 *
 */
public class SampleSend {
	private String username,password,server,folderName;
	/**
	 * 构造方法
	 * @param username 邮箱名
	 * @param password 邮箱密码
	 * @param server 邮箱服务器
	 * @param folderName 邮箱的文件夹名
	 */
	public SampleSend(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
	/**
	 * 发送的方法
	 * @param file 附件的file对象
	 * @param fileName 附件的名字
	 * @param subjectName 主题名
	 * @param Myid Myid的String
	 * @param nChunk 片数的String
	 * @param content 文本内容
	 * @return 是否发送成功
	 */
	public boolean send(File file, String fileName, String subjectName, String Myid, String nChunk, String content){//content 一般不用。
		try{																										
			Properties props = System.getProperties();
			Session session = Session.getInstance(props, null);
			Store store = session.getStore("imap");
			store.connect(this.server, this.username, this.password);
			Folder folder = store.getFolder(folderName);//"cyberbox"
			if(folder == null)
				Log.i("in","Folder is empty");
			folder.open(Folder.READ_WRITE);
			//创建信息:
			Message mailMessage = new MimeMessage(session);
			Address from = new InternetAddress(username);   // 创建邮件发送者地址   
			mailMessage.setFrom(from);  // 设置邮件消息的发送者    
			Address to = new InternetAddress(username);    // 创建邮件的接收者地址，并设置到邮件消息中   
			mailMessage.setRecipient(Message.RecipientType.TO,to);   
			mailMessage.setSubject(subjectName);  // 设置邮件消息的主题    
			mailMessage.setSentDate(new Date());   // 设置邮件消息发送的时间   
			Multipart multipart = new MimeMultipart();// 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
			MimeBodyPart contentPart = new MimeBodyPart();
			contentPart.setText(content);
			multipart.addBodyPart(contentPart);// 设置邮件的文本内容
			if (file.exists()) {	
				BodyPart affixBody = new MimeBodyPart();
				DataSource source = new FileDataSource(file);
				affixBody.setDataHandler(new DataHandler(source));// 添加附件的内容
				/* 添加附件的标题这里很重要，通过下面的Base64编码的转换可以保证你的
				sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();// 中文附件标题名在发送时不会变成乱码
				String fileName = "=?GBK?B?" + enc.encode(mailBody.getAffixName().getBytes()) + "?=";
				*/
				affixBody.setFileName(fileName);
				multipart.addBodyPart(affixBody);
			}
			mailMessage.setContent(multipart);// 将multipart对象放到message中
			//添加信息头
			mailMessage.addHeader("Myid", Myid);
			mailMessage.addHeader("nChunk", nChunk);
			//发送信息：
			folder.appendMessages(new Message[]{mailMessage});
			return true;
		}catch(MessagingException e){
			Log.i("in", e.toString());
			return false;
		}
	}
}

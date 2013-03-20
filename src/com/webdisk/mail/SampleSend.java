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

public class SampleSend {
	private String username,password,server,folderName;
	public SampleSend(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
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

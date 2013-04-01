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
 * ���ڷ����ʼ�����
 * @author forever_xuan7
 *
 */
public class SampleSend {
	private String username,password,server,folderName;
	/**
	 * ���췽��
	 * @param username ������
	 * @param password ��������
	 * @param server ���������
	 * @param folderName ������ļ�����
	 */
	public SampleSend(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
	/**
	 * ���͵ķ���
	 * @param file ������file����
	 * @param fileName ����������
	 * @param subjectName ������
	 * @param Myid Myid��String
	 * @param nChunk Ƭ����String
	 * @param content �ı�����
	 * @return �Ƿ��ͳɹ�
	 */
	public boolean send(File file, String fileName, String subjectName, String Myid, String nChunk, String content){//content һ�㲻�á�
		try{																										
			Properties props = System.getProperties();
			Session session = Session.getInstance(props, null);
			Store store = session.getStore("imap");
			store.connect(this.server, this.username, this.password);
			Folder folder = store.getFolder(folderName);//"cyberbox"
			if(folder == null)
				Log.i("in","Folder is empty");
			folder.open(Folder.READ_WRITE);
			//������Ϣ:
			Message mailMessage = new MimeMessage(session);
			Address from = new InternetAddress(username);   // �����ʼ������ߵ�ַ   
			mailMessage.setFrom(from);  // �����ʼ���Ϣ�ķ�����    
			Address to = new InternetAddress(username);    // �����ʼ��Ľ����ߵ�ַ�������õ��ʼ���Ϣ��   
			mailMessage.setRecipient(Message.RecipientType.TO,to);   
			mailMessage.setSubject(subjectName);  // �����ʼ���Ϣ������    
			mailMessage.setSentDate(new Date());   // �����ʼ���Ϣ���͵�ʱ��   
			Multipart multipart = new MimeMultipart();// ��multipart����������ʼ��ĸ����������ݣ������ı����ݺ͸���
			MimeBodyPart contentPart = new MimeBodyPart();
			contentPart.setText(content);
			multipart.addBodyPart(contentPart);// �����ʼ����ı�����
			if (file.exists()) {	
				BodyPart affixBody = new MimeBodyPart();
				DataSource source = new FileDataSource(file);
				affixBody.setDataHandler(new DataHandler(source));// ��Ӹ���������
				/* ��Ӹ����ı����������Ҫ��ͨ�������Base64�����ת�����Ա�֤���
				sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();// ���ĸ����������ڷ���ʱ����������
				String fileName = "=?GBK?B?" + enc.encode(mailBody.getAffixName().getBytes()) + "?=";
				*/
				affixBody.setFileName(fileName);
				multipart.addBodyPart(affixBody);
			}
			mailMessage.setContent(multipart);// ��multipart����ŵ�message��
			//�����Ϣͷ
			mailMessage.addHeader("Myid", Myid);
			mailMessage.addHeader("nChunk", nChunk);
			//������Ϣ��
			folder.appendMessages(new Message[]{mailMessage});
			return true;
		}catch(MessagingException e){
			Log.i("in", e.toString());
			return false;
		}
	}
}

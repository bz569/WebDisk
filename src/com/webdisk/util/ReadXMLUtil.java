package com.webdisk.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Environment;
import android.util.Log;

import com.webdisk.model.MailInfo;
import com.webdisk.model.UserConfig;

public class ReadXMLUtil
{
	private static final String TAG = "ReadXMLUtil";
	
	private static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";
	
	public static UserConfig getConfigFromXML()
	{
		Log.i(TAG, "��ȡXML");
		
		ArrayList<MailInfo> mailList = new ArrayList<MailInfo>();
		String userID = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		
		factory = DocumentBuilderFactory.newInstance();
		try
		{
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(CACHE_DIR + ".email.xml"));
			
			Element root = document.getDocumentElement();
			NodeList LStoreNodes = root.getElementsByTagName("LStore_list");
			
			Element ListElement = (Element) LStoreNodes.item(0);
			NodeList groupNodes = ListElement.getElementsByTagName("email_group");
			
			
			//��������email_group
			for(int i = 0; i < groupNodes.getLength(); i++)
			{
				Element groupElement = (Element) LStoreNodes.item(0);
				NodeList itemNodes = groupElement.getElementsByTagName("email_item");
				Log.i(TAG, "itemNodes.length=" + itemNodes.getLength());
				
				//����ÿ��group�е�email_item
				MailInfo mailInfo = null;
				for(int j = 0; j < itemNodes.getLength(); j++)
				{
					mailInfo = new MailInfo();
					//��ȡÿһ���ڵ�
					Element mailInfoItem = (Element) (itemNodes.item(j));
					//��ȡserver_addr����ֵ
					mailInfo.setServer_adr(mailInfoItem.getAttribute("server_addr"));
					Log.i(TAG, "server_addr=" + mailInfoItem.getAttribute("server_addr"));
					//��ȡusername����ֵ
					mailInfo.setUsername(mailInfoItem.getAttribute("username"));
					Log.i(TAG, "username=" + mailInfoItem.getAttribute("username"));
					//��ȡpassword����ֵ
					mailInfo.setPassword(mailInfoItem.getAttribute("password"));
					Log.i(TAG, "password=" + mailInfoItem.getAttribute("password"));
					
					//��ӵ�mailList��
					mailList.add(mailInfo);
				}
			}
			
			//��ȡUserID
			Element userIDElement = (Element) (root.getElementsByTagName("user_id").item(0));
			userID = userIDElement.getFirstChild().getNodeValue().replaceAll("\n", "");
			Log.i(TAG, "userID=" + userID);
			
		} catch (ParserConfigurationException e)
		{
			e.printStackTrace();
			Log.i("TAG", "err");
		} catch (IOException e)
		{
			e.printStackTrace();
			Log.i("TAG", "err");
		} catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new UserConfig(userID, mailList);
		
	}
	
	
	
	
	
	


}

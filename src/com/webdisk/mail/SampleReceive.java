package com.webdisk.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;

import android.util.Log;

public class SampleReceive {
	private String username,password,server,folderName;
	private boolean finishFlag = false;
	private String Myid_i = null;
	private String storeFileName = null;
	public SampleReceive(String username,String password,String server,String folderName){
		this.username = username;
		this.password = password;
		this.server = server;
		this.folderName = folderName;
	}
	public boolean receive(String Myid,String storeDir,String storeFileName){//storeFileName作为附件的名字
		this.storeFileName = storeFileName;
		try {
			Properties props = System.getProperties();
			Session session = Session.getInstance(props, null);
			Store store = session.getStore("imap");
			store.connect(this.server, this.username, this.password);
			Folder folder = store.getFolder(folderName);//"cyberbox"
			if(folder == null)
				Log.i("in","Folder is empty");
			folder.open(Folder.READ_ONLY);
			//Message[] msgs = folder.getMessages();
			//根据Myid找制定的邮件
			int num = this.getMyidFromMsgs(folder,Myid);
			if(num == -1)
				return false;
			Part part = folder.getMessage(num);//不确定
			//this.subjectname = "templeFile";//((Message)part).getSubject();
			return this.saveAttchMent(part,storeDir);//保存附件
			
			//return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	private int getMyidFromMsgs(Folder folder,String Myid) throws MessagingException{//根据Myid找制定的邮件
		int num = folder.getMessageCount();
		int i = 1;
		Log.i("in","开始查找");
		while(i ++ < num){
			Enumeration headers = folder.getMessage(i).getAllHeaders();
			while (headers.hasMoreElements()) {
				Header h = (Header) headers.nextElement();
				String mID = h.getName();                
				if(mID.contains("Myid")){
					//System.out.println(h.getName() + ":" + h.getValue());
					if(h.getValue().equals(Myid)){
                		Myid_i = h.getValue();
                		Log.i("in","已经找到 Myid_i:" + i);
						return i;
					}
                }
			}	
		}
		return -1;
	}
	
	private boolean saveAttchMent(Part part,String storeDir) throws UnsupportedEncodingException, MessagingException, IOException{
		if(!isContainAttch(part))
			return false;
		String fileName = "";
		if(part.isMimeType("multipart/*")){
			Multipart mp = (Multipart) part.getContent();
			//System.out.println("mp.count:" + mp.getCount());
			for(int i = 0 ; i < mp.getCount() ; i++){
				BodyPart mpart = mp.getBodyPart(i);
				String dispostion = mpart.getDisposition();
				if((dispostion != null)&&(dispostion.equals(Part.ATTACHMENT)||dispostion.equals(Part.INLINE))){
					
					fileName = mpart.getFileName();
					if(fileName.toLowerCase().indexOf("gb2312") != -1){
						fileName = MimeUtility.decodeText(fileName);
					}
					return saveFile(storeDir,storeFileName,mpart.getInputStream());//用主题名作为附件名
					//return saveFile(storeDir,fileName,mpart.getInputStream());
				}else if(mpart.isMimeType("multipart/*")){
					saveAttchMent(mpart,storeDir);
				}
			}
			
		}else if(part.isMimeType("message/rfc822")){
			saveAttchMent((Part) part.getContent(),storeDir);
		}
		return false;
	}
	//确定是否含有附件
	private boolean isContainAttch(Part part) {
	        boolean flag = false;
	        try{
	        //String contentType = part.getContentType();
	        	if(part.isMimeType("multipart/*")){
	        		Multipart multipart = (Multipart) part.getContent();
	        		int count = multipart.getCount();
	        		for(int i = 0 ; i < count ; i++ ){
	        			BodyPart bodypart = multipart.getBodyPart(i);
	        			String dispostion = bodypart.getDisposition();
	        			if((dispostion != null)&&(dispostion.equals(Part.ATTACHMENT)||dispostion.equals(Part.INLINE))){
	        				flag = true;
	        			}else if(bodypart.isMimeType("multipart/*")){
	        				flag = isContainAttch(bodypart);
	        			}else{
	        				String conType = bodypart.getContentType();
	        				if(conType.toLowerCase().indexOf("appliaction")!=-1){
	        					flag = true;
	        				}
	        				if(conType.toLowerCase().indexOf("name")!=-1){
	        					flag = true;
	        				}
	        			}
	        		}
	        	}else if(part.isMimeType("message/rfc822")){
	        		flag = isContainAttch((Part) part.getContent());
	        	}
	        }catch (MessagingException e1){
	        	return false;
	        }catch (IOException e2){
	        	return false;
	        }
	        return flag;
	    }
	
	private boolean saveFile(String storeDir , String fileName,InputStream in) throws IOException{
		char last = storeDir.charAt(storeDir.length() - 1);
		if(last != '/' || last != '\\')
			storeDir = storeDir + "/";
        File storefile = new File(storeDir + fileName);
        Log.i("in","开始保存附件 ：路径为:" + storefile.toString());
        
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storefile));
            bis = new BufferedInputStream(in);
            int c;
            while((c= bis.read())!=-1){
                bos.write(c);
                bos.flush();
            }
            finishFlag = true;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            bos.close();
            bis.close();
            
        }
        return false;
	}
	
	public String getMyid_i(){
		return Myid_i;
	}
}

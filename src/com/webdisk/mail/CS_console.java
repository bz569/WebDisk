package com.webdisk.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.*;

import android.util.Log;
public class CS_console {
	final public static int lengthOf1M = 1048576;
	private String[] username,password;
	private String server = null;
	private int numOfEmailbox = 5;
	private String folderName = "cyberbox";
	private threadSampleSender[] sendThreads = null;
	private threadSampleReceiver[] receiveThreads = null;
	private boolean sendFlag = false , receiveFlag = false;
//	public CS_console(){}//����ʱʹ��
	public CS_console(String[] username,String[] password,String server) throws NoSuchProviderException{
		this.username = username;
		this.password = password;
		this.server = server;
	}
	public void setNumOfEmailBox(int num){
		numOfEmailbox = num;
	}
	public void setFolderName(String folderName){
		this.folderName = folderName;
	}

	
	
	public int send(String Myid , int chunkLength , String dir ,String templeDir){//��ʱʹ��ȫ���������ٷ���//����Ҫ��pc�˵ĸ�����ͳһ
		//maxOfChunk��ÿ�����ĳ���	
		Log.i("in","��ʼ ");
		File file = new File(dir);
		if(!file.exists()){
			Log.i("in","û���ҵ��ļ�");
			return 0;
		}
		if(file.length() <= chunkLength){
			//new threadSampleSender(0 ,templeDir ,"_" + this.getFileName(dir) + ".part" + 0 ,Myid+":"+ 0 ,1 ).start();
			SampleSend sender = new SampleSend(username[0],password[0],server,folderName);
			if(sender.send(new File(dir),this.getFileName(dir),this.getFileName(dir),Myid,"1",""))
				Log.i("in","�ɹ�����С�ļ�");
			else
				Log.i("in","fail");
			
			return 1;
		}
		int nChunk = (int)file.length() / chunkLength + 1;
		int startPos = nChunk % numOfEmailbox;
		//����Ŀ¼:
		Log.i("in","����Ŀ¼");
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir + "/");
		files.mkdirs();
				
		//����ļ���
		String fileName = this.getFileName(dir);
		Log.i("in","��ʼ ����ļ�" +dir);
		try {
			this.split(dir, templeDir, chunkLength);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//�����ļ�
		Log.i("in","��ʼ �����ļ�");
		sendThreads = new threadSampleSender[numOfEmailbox];
		for(int i = 0 ; i < nChunk ; ){
			Log.i("in","��ʼ�ϴ�:" + i);
			if(i < numOfEmailbox){
				String templeName = "_" + fileName + ".part" + i;
				sendThreads[startPos] = new threadSampleSender(startPos ,templeDir ,templeName ,Myid+":"+i ,nChunk );//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
				sendThreads[startPos].start();
				startPos = (startPos + 1) % numOfEmailbox;
				i++;
			}
			else if(i >= numOfEmailbox ){
				if(sendThreads[startPos].isFinish()){
					String templeName = "_" + fileName + ".part" + i;
					sendThreads[startPos] = new threadSampleSender(startPos ,templeDir ,templeName ,Myid+":"+i ,nChunk);//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
					sendThreads[startPos].start();
					startPos = (startPos + 1) % numOfEmailbox;
					i++;
				}
				else{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
			else{}
		}
		//�ж��ϴ��Ƿ��Ѿ�����
		int i = 0 ;
		Log.i("in","�ж��Ƿ����");
		while(true){
			if(sendThreads[i].isFinish())
				i++;
			if(i >= numOfEmailbox)
				break;
		}
		Log.i("in","���ͽ���");
		/*
		//ɾ���ļ���//��Ϊ�ϴ�������ɾ��
		for(int t = 0 ; t < nChunk ; t ++){
			String templeFile = templeDir + "_" + fileName + ".part" + t;
			File tem = new File(templeFile);
			if(tem.exists())
				tem.delete();
		}
		*/
		sendFlag = true;
		return nChunk;
	}
	public void receive(String Myid , int nChunk , String templeDir  , String fileDir , String fileName) throws IOException{//��ʱ����ȫ�����ص���ʱ�ļ��к���ƴװ//,String templeName
		String templeName = null;
		int startPos = nChunk % numOfEmailbox;
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir);
		files.mkdirs();
		if(fileDir.charAt(fileDir.length() - 1) != '/')
			fileDir = fileDir + "/";
		File files1 = new File(fileDir);
		files1.mkdirs();
		
		receiveThreads = new threadSampleReceiver[numOfEmailbox];
		for(int i = 0 ; i < nChunk ; ){
			if(i < numOfEmailbox){
				String Myid_i = Myid + ":" + i;
				templeName =  "_" + fileName + ".part" + i;
				receiveThreads[startPos] = new threadSampleReceiver(startPos , Myid_i ,templeDir,templeName);
				receiveThreads[startPos].start();
				startPos = (startPos + 1) % numOfEmailbox;
				i++;
			}
			else if(i >= numOfEmailbox && i < nChunk){
				if(receiveThreads[startPos].isFinish()){//�ȴ��ϸ��ϴ�����
					String Myid_i = Myid + ":" + i;
					templeName = "_" + fileName + ".part" + i;
					receiveThreads[startPos] = new threadSampleReceiver(startPos , Myid_i ,templeDir,templeName);
					receiveThreads[startPos].start();
					startPos = (startPos + 1) % numOfEmailbox;
					i++;
				}	
				else {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {	
						e.printStackTrace();
					}
				}
			}
			else{}
		}
		int i = 0 ;
		Log.i("in","�ж��Ƿ����");
		while(true){
			if(receiveThreads[i].isFinish())
				i++;
			if(i >= numOfEmailbox)
				break;
		}
		Log.i("in","�жϽ���");
		//�����ļ�
		install( templeDir ,fileName, nChunk ,fileDir + fileName);
		
		
		//ɾ���ļ���
		for(int t = 0 ; t < nChunk ; t ++){
			templeName = templeDir + "_" + fileName + ".part" + t;
			File tem = new File(templeName);
			if(tem.exists())
				tem.delete();
		}
		
		receiveFlag = true;
	}
	
	//��ַ�����
	public void split(String fileAddress ,String templeDir , int chunkLength) throws Exception{
		Log.i("in","0");
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		
		String name = this.getFileName(fileAddress) , templeAddress = null;
		//�õ��ļ� ��֣�
		File file1 = new File(fileAddress);
		
		FileInputStream in =new FileInputStream(file1);
		boolean end = true;
		int i = 0;
		while(end){
			Log.i("in","while" +i);
			int c = 0,count= 0;
			templeAddress = templeDir + "_" + name + ".part" + i;
			File file2 = new File(templeAddress);
			FileOutputStream out =new FileOutputStream(file2);
			byte[] b = new byte[1024];
			c = in.read(b);
			while(count++ < chunkLength/1024){
				if(c == -1){
					end = false;
					break;
				}
				out.write(b,0,c);
				c = in.read(b);
			}
			Log.i("in","while ok:" + i +""+templeAddress);
			out.close();
			i++;
		}
		in.close();
		
	}
	//���Ϸ���
	public void install(String templeDir ,String fileName, int nChunk ,String fileAddress) throws IOException{//FileNameΪ��װ����ļ������λ��+�ļ��� //,String templeName
		String path = getPath(fileAddress);
		new File(path).mkdirs();
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File newFile = new File(fileAddress);
		FileOutputStream fo = new FileOutputStream(newFile);
		
		for(int i = 0 ; i < nChunk ; i++){
			String templeLiitleName = templeDir + "_" + fileName + ".part" + i;
			File teFile = new File(templeLiitleName);
			if(teFile.exists()){
				FileInputStream fi = new FileInputStream(teFile);
				
				byte[] b = new byte[1024];
				int c = fi.read(b);
				/*
				while((c = fi.read()) != -1){
					fo.write(c);
				}
				*/
				while( c != -1){
					fo.write(b, 0, c);
					c = fi.read(b);
				}
				fi.close();
				//teFile.delete();//������ɾ����������ļ���ʱ��
			}
		}	
		fo.close();
	}

	//С����
	private String getFileName(String dir){
		String name = dir.substring(dir.lastIndexOf("/")+1);
		return name;
	}
	public String getPath(String dir){
		String path  = dir.substring(0, dir.lastIndexOf("/"));
		return path;
	}
	
	//�ڲ��ࣺ
	//���͵Ľ��̣�
	class threadSampleSender extends Thread
	{
		private boolean flag= false , end = false; 
		private int startPos,nChunk;
		private String templeDir,templeName,Myid_i;
		public threadSampleSender(int startPos , String templeDir , String templeName , String Myid_i , int nChunk){
			this.startPos = startPos;
			this.templeDir = templeDir;
			this.templeName = templeName;
			this.Myid_i = Myid_i;
			this.nChunk = nChunk;
			
		}
		public void run(){
			SampleSend sender = new SampleSend(username[startPos],password[startPos],server,folderName);
			File temFile = new File(templeDir + templeName);
			if(temFile.exists()){
				flag = sender.send(temFile ,templeName ,templeName ,Myid_i,nChunk+"" ,"");
				temFile.delete();
				Log.i("in","temFile is exists");
			}
			end = true;
			
			Log.i("in",Myid_i + "���ͽ���" +end);
		}
		public boolean isFinish(){
			return end;
		}
		public boolean isOK(){
			return flag;
		}
	}
	//���յĽ���
	class threadSampleReceiver extends Thread{
		private boolean flag= false , end = false; 
		private int startPos;
		private String Myid,storeDir,fileName;
		public threadSampleReceiver(int startPos ,String Myid ,String storeDir, String fileName){//String Myid,String storeDir
			this.startPos = startPos;
			this.Myid= Myid;
			this.storeDir = storeDir;
			this.fileName = fileName;
		}
		public void run(){
			SampleReceive receiver = new SampleReceive(username[startPos] ,password[startPos] ,server,folderName);
			flag = receiver.receive(Myid,storeDir,fileName);
			end = true;
		}
		
		public boolean isFinish(){
			return end;
		}	
		public boolean isOK(){
			return flag;
		}
	}
	
	public boolean isSend(){
		return sendFlag;
	}
	public boolean isReceive(){
		return receiveFlag;
	}
	
	
	public static void main(String args[]){
		String[] username = {"cyberbox1@163.com","cyberbox2@163.com","cyberbox3@163.com","cyberbox4@163.com","cyberbox5@163.com"};
		String[] password = {"cyberbox","cyberbox","cyberbox","cyberbox","cyberbox"};
		String server = "imap.163.com";
		
		try {
			CS_console cs = new CS_console(username,password,server);
			//cs.send("18889203411", 1048576, "E:/1/�ʺ�����.mp3", "E:/1/12");
			cs.receive("18889203411", 7, "E:/1/12",  "E:/1/123/" ,"�ʺ�����.mp3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

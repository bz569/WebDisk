package com.webdisk.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.mail.*;

import android.util.Log;

public class CS_console_new {
	final public static int lengthOf1M = 1048576;
	private String[] username,password;
	private String server = null;
	private int numOfEmailbox = 5;
	private String folderName = "cyberbox";
	private threadSampleSender[] sendThreads = null;
	private threadSampleReceiver[] receiveThreads = null;
	private int chunkLength = 1024*1024;
	private boolean sendFlag = false , receiveFlag = false;
	//public CS_console_new(){}//测试时使用
	public CS_console_new(String[] username,String[] password,String server) throws NoSuchProviderException{
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
	public void setChunkLength(int chunkLength){
		this.chunkLength = chunkLength;
	}
	
	
	public int send(String Myid , String dir ,String templeDir){//暂时使用全部拆分完后再发送//还需要和pc端的附件名统一
		//maxOfChunk：每块最大的长度	
		Log.i("in","开始 ");
		File file = new File(dir);
		if(!file.exists()){
			Log.i("in","没有找到文件");
			return 0;
		}
		if(file.length() <= chunkLength){
			//new threadSampleSender(0 ,templeDir ,"_" + this.getFileName(dir) + ".part" + 0 ,Myid+":"+ 0 ,1 ).start();
			SampleSend sender = new SampleSend(username[0],password[0],server,folderName);
			if(sender.send(new File(dir),this.getFileName(dir),this.getFileName(dir),Myid,"1",""))
				Log.i("in","成功发送小文件");
			else
				Log.i("in","fail");
			
			return 1;
		}
		int nChunk = (int)file.length() / chunkLength + 1;
		int startPos = nChunk % numOfEmailbox;
		//创建目录:
		Log.i("in","创建目录");
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir + "/");
		files.mkdirs();
				
		//拆分文件：
		String fileName = this.getFileName(dir);
		Log.i("in","开始 拆分文件" +dir);
		try {
			this.split(dir, templeDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//发送文件
		Log.i("in","开始 发送文件");
		sendThreads = new threadSampleSender[numOfEmailbox];
		boolean notEnd = true;
		int i = 0;//i表示当前发送的模块id
		int count = 0 ;//count表示开始(或者发送结束)的模块数
		while(notEnd){
			
			if(i < numOfEmailbox){
				Log.i("in","开始上传:" + i);
				String templeName = "." + fileName + ".part" + i;
				sendThreads[startPos] = new threadSampleSender(startPos ,templeDir ,templeName ,Myid+":"+i ,nChunk );//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
				sendThreads[startPos].start();
				i++;
				count++;
			}
			else if(i >= numOfEmailbox && i < nChunk){
					if(!sendThreads[startPos].isFinish())
						continue;
					else{
						int last_i = sendThreads[startPos].getNumOfMyid();
						if(last_i + numOfEmailbox < nChunk){
							int now_i = last_i + numOfEmailbox;
							Log.i("in","开始上传:" + now_i);
							String templeName = "." + fileName + ".part" + now_i;
							sendThreads[startPos] = new threadSampleSender(startPos ,templeDir ,templeName ,Myid+":"+now_i ,nChunk);//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
							sendThreads[startPos].start();
							count++;
						}
					}
			}
			else{
			}
			startPos = (startPos + 1) % numOfEmailbox;
			if(count >= nChunk)
				notEnd = false;
		}
		//判断上传是否已经结束
		int i1 = 0 ;
		Log.i("in","最后一片已经开始上传 ，判断是否结束");
		while(true){
			if(sendThreads[i1].isFinish())
				i1++;
			if(i1 >= numOfEmailbox)
				break;
		}
		Log.i("in","发送结束");
		/*
		//删除文件：//改为上传结束后删除
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
	
	public void receive(String Myid , int nChunk , String templeDir  , String fileDir , String fileName) throws IOException{//暂时采用全部下载到临时文件夹后再拼装//,String templeName
		String templeName = null;
		
		
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir);
		files.mkdirs();
		if(fileDir.charAt(fileDir.length() - 1) != '/')
			fileDir = fileDir + "/";
		File files1 = new File(fileDir);
		files1.mkdirs();
		
		receiveThreads = new threadSampleReceiver[numOfEmailbox];
		int startPos = nChunk % numOfEmailbox;
		boolean notEnd = true;
		int count = 0;
		int i = 0;
		while(notEnd){
			if(i < numOfEmailbox){
				Log.i("in","开始下载i:" + i +"startPos:" +startPos);
				String Myid_i = Myid + ":" + i;
				templeName =  "." + fileName + ".part" + i;
				receiveThreads[startPos] = new threadSampleReceiver(startPos , Myid_i ,templeDir,templeName);
				receiveThreads[startPos].start();
				i++;
				count++;
			}
			else if(i >= numOfEmailbox && i < nChunk){
				if(!receiveThreads[startPos].isFinish())
					continue;
				else{
					int last_i = receiveThreads[startPos].getNumOfMyid();
					if(last_i + numOfEmailbox < nChunk){
						int now_i = last_i + numOfEmailbox;
						Log.i("in","开始下载:" + now_i);
						String Myid_i = Myid + ":" + now_i;
						templeName = "." + fileName + ".part" + now_i;
						receiveThreads[startPos] = new threadSampleReceiver(startPos , Myid_i ,templeDir,templeName);
						receiveThreads[startPos].start();
						count++;
					}	
				}
			}
			startPos = (startPos + 1) % numOfEmailbox ; 
			if(count >= nChunk)
				notEnd = false;
		}
		int i1 = 0 ;
		Log.i("in","判断是否结束");
		while(true){
			if(receiveThreads[i1].isFinish())
				i1++;
			if(i1 >= numOfEmailbox)
				break;
		}
		Log.i("in","下载结束 开始整合");
		//整合文件
		install_random( templeDir ,fileName, nChunk ,fileDir + fileName);
		Log.i("in","整合完毕");

		/*
		//删除文件：//已经迁移到整合文件的时候
		for(int t = 0 ; t < nChunk ; t ++){
			templeName = templeDir + "_" + fileName + ".part" + t;
			File tem = new File(templeName);
			if(tem.exists())
				tem.delete();
		}
		*/
		receiveFlag = true;
	}
	
	//拆分方法：
	public void split(String fileAddress ,String templeDir ) throws Exception{
		Log.i("in","0");
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		
		String name = this.getFileName(fileAddress) , templeAddress = null;
		//得到文件 拆分：
		File file1 = new File(fileAddress);
		
		FileInputStream in =new FileInputStream(file1);
		boolean end = true;
		int i = 0;
		while(end){
			Log.i("in","while" +i);
			int c = 0,count= 0;
			templeAddress = templeDir + "." + name + ".part" + i;
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
	//整合方法
	public void install(String templeDir ,String fileName, int nChunk ,String fileAddress) throws IOException{//FileName为组装后的文件存入的位置+文件名 //,String templeName
		String path = getPath(fileAddress);
		new File(path).mkdirs();
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File newFile = new File(fileAddress);
		FileOutputStream fo = new FileOutputStream(newFile);
		
		for(int i = 0 ; i < nChunk ; i++){
			String templeLiitleName = templeDir + "." + fileName + ".part" + i;
			File teFile = new File(templeLiitleName);
			if(teFile.exists()){
				FileInputStream fi = new FileInputStream(teFile);
				
				byte[] b = new byte[1024];
				int c = fi.read(b);
				while( c != -1){
					fo.write(b, 0, c);
					c = fi.read(b);
				}
				fi.close();
				new threadDelete(teFile).start();
			}
		}	
		fo.close();
	}

	public void install_random(String templeDir , String fileName , int nChunk , String fileAddress) throws FileNotFoundException{
		String path = getPath(fileAddress);
			new File(path).mkdirs();
		File outFile = new File(fileAddress);
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		for(int i = 0 ; i < nChunk ; i++){
			String templeLiitleName = templeDir + "." + fileName + ".part" + i;
			File inFile = new File(templeLiitleName);
			if(inFile.exists())
				new receiveThreadRandom(outFile,inFile,i).start();
		}	
	}
	class receiveThreadRandom extends Thread{
		private RandomAccessFile out =null;
		File outFile = null,inFile = null;
		private InputStream in = null;
		int i = 0 ;
		public receiveThreadRandom(File outFile , File inFile,int i) throws FileNotFoundException{
			//this.out = out;
			this.outFile = outFile;
			this.inFile = inFile;
			this.i = i;
			out  = new RandomAccessFile(outFile, "rw");
			in = new FileInputStream(inFile);
		}
		public void run(){
				try {
					int startPos = chunkLength*i;
					int c = 0 ;
					out.seek(startPos);
					Log.i("in",i+": start");
					byte[] b = new byte[1024];
					c = in.read(b);
					while(c != -1){
						out.write(b,0,c);
						c = in.read(b);
					}
					in.close();
					inFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
		
		}
	}
	//小方法
	private String getFileName(String dir){
		String name = dir.substring(dir.lastIndexOf("/")+1);
		return name;
	}
	public String getPath(String dir){
		String path  = dir.substring(0, dir.lastIndexOf("/"));
		return path;
	}
	public boolean isSend(){
		return sendFlag;
	}
	public boolean isReceive(){
		return receiveFlag;
	}
	
	//内部类：
	//发送的进程：
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
				Log.i("in",templeDir + templeName +" is exists");
				flag = sender.send(temFile ,templeName ,templeName ,Myid_i,nChunk+"" ,"");
				temFile.delete();
				
			}
			end = true;
			
			Log.i("in",Myid_i + "发送结束" +flag);
		}
		public boolean isFinish(){
			return end;
		}
		public boolean isOK(){
			return flag;
		}
		public int getNumOfMyid(){
			String num = Myid_i.substring(Myid_i.lastIndexOf(":")+1);
			return Integer.decode(num);
		}
	}
	//接收的进程
	class threadSampleReceiver extends Thread{
		private boolean flag= false , end = false; 
		private int startPos;
		private String Myid_i,storeDir,fileName;
		public threadSampleReceiver(int startPos ,String Myid_i ,String storeDir, String fileName){//String Myid,String storeDir
			this.startPos = startPos;
			this.Myid_i= Myid_i;
			this.storeDir = storeDir;
			this.fileName = fileName;
		}
		public void run(){
			SampleReceive receiver = new SampleReceive(username[startPos] ,password[startPos] ,server,folderName);
			Log.i("in",Myid_i + " 开始接收");
			flag = receiver.receive(Myid_i,storeDir,fileName);
			end = true;
			Log.i("in",Myid_i + " 接收完成 " +flag);
		}
		
		public boolean isFinish(){
			return end;
		}	
		public boolean isOK(){
			return flag;
		}
		public int getNumOfMyid(){
			String num = Myid_i.substring(Myid_i.lastIndexOf(":")+1);
			return Integer.decode(num);
		}
	}
	//删除的进程
	class threadDelete extends Thread{
		private File file = null;
		public threadDelete (File file){
			this.file = file;
		}
		public void run(){
			if(file.exists())
				file.delete();
		}
	}
	
	
	
	public static void main(String args[]){
		String[] username = {"cyberbox1@163.com","cyberbox2@163.com","cyberbox3@163.com","cyberbox4@163.com","cyberbox5@163.com"};
		String[] password = {"cyberbox","cyberbox","cyberbox","cyberbox","cyberbox"};
		String server = "imap.163.com";
		
		try {
			CS_console_new cs = new CS_console_new(username,password,server);
			//cs.send("19901111", 1024*1024, "E:/1/彩虹天堂.mp3", "E:/1/12"))
			//cs.receive("19901111", 7, "E:/1/12",  "E:/1/123/" ,"彩虹天堂.mp3");
			//cs.split("E:/1/lalala.mp3", "E:/1/12", 1024*1024);
			//cs.install_random("E:/1/12", "lalala.mp3", 8, "E:/1/123/完美主义.mp3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

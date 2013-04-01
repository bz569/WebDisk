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
/**
 * 此类用于发送、接收和删除邮件
 * @author forever_xuan7
 *
 */
public class CS_console_new {
	//final public static int lengthOf1M = 1024*1024;
	private String[] username,password;
	private String server = null;
	private int numOfEmailbox = 5;
	private int numOfThread = 2;
	private String folderName = "cyberbox";
	private threadSampleSender[] sendThreads = null;
	private threadSampleReceiver[] receiveThreads = null;
	private int chunkLength = 1024*1024*5;
	private boolean sendFlag = false , receiveFlag = false;
	//public CS_console_new(){}//测试时使用
	/**
	 * 构造方法
	 * @param username 邮箱名的数组
	 * @param password 邮箱密码的数组
	 * @param server 服务器名
	 * @throws NoSuchProviderException
	 */
	public CS_console_new(String[] username,String[] password,String server) throws NoSuchProviderException{
		this.username = username;
		this.password = password;
		this.server = server;
	}
	
	/**
	 * 设置邮箱的个数
	 * @param num 最大的个数
	 */
	public void setNumOfEmailBox(int num){
		numOfEmailbox = num;
	} 
	
	/**
	 * 设置保存于邮箱中的文件夹名
	 * @param folderName 文件夹名
	 */
	public void setFolderName(String folderName){
		this.folderName = folderName;
	}
	
	/**
	 * 设置分片的大小  默认为1048576 * 5(5M)
	 * @param chunkLength 分片的大小
	 */
	public void setChunkLength(int chunkLength){
		this.chunkLength = chunkLength;
	}
	
	/**
	 * 按Myid删除删除邮件
	 * @param Myid 邮件的Myid 
	 * @param isFuzzy 是否采用模糊模式
	 * true：删除所有邮件id中包含Myid的邮件(例：若Myid为28367298,邮件id为28367298:1,则该邮件将被删除)
	 * false：删除指定Myid的邮件
	 */
	public void delete(final String Myid ,boolean isFuzzy ){
		if(isFuzzy){
			for( int i = 0 ; i < numOfEmailbox ; i++)
				new SampleDelete(username[i],password[i],server,folderName).deleteFuzzy(Myid);
		}
		else{
			for( int i = 0 ; i < numOfEmailbox ; i++)
				new SampleDelete(username[i],password[i],server,folderName).deleteDefine(Myid);
		}
	}
	
	/**
	 * 发送文件至邮箱的方法
	 * @param Myid 发送邮件添加的Myid
	 * @param dir 文件所在位置
	 * @param templeDir 临时文件存放打位置(因发送时可能会拆分,需临时文件夹存放临时文件,发送完毕后删除)
	 * @return 返回发送的片数,0表示没有发送成功
	 */
	public int send(String Myid , String dir ,String templeDir){//暂时使用全部拆分完后再发送//还需要和pc端的附件名统一
		Log.i("in","开始 ");
		File file = new File(dir);
		if(!file.exists()){
			Log.i("in","没有找到文件");
			return 0;
		}
		if(file.length() <= chunkLength){
			//new threadSampleSender(0 ,templeDir ,"_" + this.getFileName(dir) + ".part" + 0 ,Myid+":"+ 0 ,1 ).start();
			SampleSend sender = new SampleSend(username[0],password[0],server,folderName);
			if(sender.send(new File(dir),this.getFileName(dir),this.getFileName(dir),Myid+":0","1",""))
				Log.i("in","成功发送小文件");
			else
				Log.i("in","fail");
			
			return 1;
		}
		int nChunk = (int)file.length() / chunkLength + 1;
		int startPos = (int)(Long.parseLong(Myid) % numOfEmailbox);
//		long startPos = (long)Integer.decode(Myid) % numOfEmailbox;
		//创建目录:
		Log.i("in","创建目录 ___ startPos:" +startPos + "___nChunk:" + nChunk);
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
		sendThreads = new threadSampleSender[2];
		boolean notEnd = true;
		int i = 0;//i表示当前发送的模块id
		int nowThread = 0;
		while(notEnd){
			if(i < numOfThread && i < nChunk){
				Log.i("in","开始上传:" + i);
				
				sendThreads[nowThread] = new threadSampleSender(startPos ,templeDir ,fileName ,Myid ,nChunk ,i );//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
				sendThreads[nowThread].start();
				i++;
				startPos = (startPos + 1) % numOfEmailbox;
			}
			else if(i >= numOfThread && i < numOfEmailbox && i < nChunk){
					if(!sendThreads[nowThread].isFinish())
						continue;
					else{
						Log.i("in","开始上传:" + i);
						sendThreads[nowThread] = new threadSampleSender(startPos ,templeDir ,fileName ,Myid ,nChunk ,i );//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
						sendThreads[nowThread].start();
						i++;
						startPos = (startPos +1) % numOfEmailbox;
					}
			}
			else{
			}
			nowThread = (nowThread + 1) % numOfThread;
			if(i >= numOfEmailbox)
				notEnd = false;
		}
		//判断上传是否已经结束
		int i1 = 0 ;
		Log.i("in","最后一个邮箱已经开始上传 ，判断是否结束");
		while(true){
			if(sendThreads[i1] == null)
				i1++;
			else{
				if(sendThreads[i1].isFinish())
					i1++;
			}
			if(i1 >= numOfThread)
				break;
		}
		Log.i("in","发送结束");

		sendFlag = true;
		return nChunk;
	}
	
	/**
	 * 从邮箱接收文件的方法
	 * @param Myid 此文件的Myid
	 * @param templeDir 临时文件夹
	 * @param fileDir 存放文件的位置
	 * @param fileName 保存文件的名字
	 * @throws IOException
	 */
	public void receive(String Myid , String templeDir  , String fileDir , String fileName) throws IOException{//暂时采用全部下载到临时文件夹后再拼装//,String templeName
		
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir);
		files.mkdirs();
		if(fileDir.charAt(fileDir.length() - 1) != '/')
			fileDir = fileDir + "/";
		File files1 = new File(fileDir);
		files1.mkdirs();
		
		receiveThreads = new threadSampleReceiver[numOfEmailbox];
		//int startPos = nChunk % numOfEmailbox;
		int i = 0;
		while(i < numOfEmailbox){
				//Log.i("in","开始下载i:" + i );
				//String Myid_i = Myid + ":" + i;
				//templeName =  "." + fileName + ".part" + i;
				receiveThreads[i] = new threadSampleReceiver(i , Myid ,templeDir,fileName);
				receiveThreads[i].start();
				i++;
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
		install(templeDir ,fileName,fileDir + fileName);
		receiveFlag = true;
	}
	
	/**
	 * 内部  拆分方法
	 * @param fileAddress 文件的路径
	 * @param templeDir 拆分后存放文件的位置
	 * @throws Exception
	 */
	private void split(String fileAddress ,String templeDir ) throws Exception{
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
			Log.i("in","while" +i+"___chunkLength:" + chunkLength);
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
			Log.i("in","while ok:" + i +"_"+count);
			out.close();
			i++;
		}
		in.close();
		
	}

	/**
	 * 内部 整合方法
	 * @param templeDir 临时文件夹的位置
	 * @param fileName 临时文件的文件名(共有部分 .xxxxx.part1,.xxxxx.part2形式存在)
	 * @param fileAddress 存放文件的路径
	 * @throws IOException
	 */
	private void install(String templeDir ,String fileName,String fileAddress) throws IOException{//FileName为组装后的文件存入的位置+文件名 //,String templeName
		String path = getPath(fileAddress);
		new File(path).mkdirs();
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File newFile = new File(fileAddress);
		FileOutputStream fo = new FileOutputStream(newFile);
		
		for(int i = 0 ;  ; i++){
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
			else{
				break;
			}
		}	
		fo.close();
	}

	/**
	 * 内部 另一种整合文件的方法 已废弃
	 * @param templeDir 临时文件夹的位置
	 * @param fileName 临时文件的文件名
	 * @param fileAddress 存放文件的路径
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unused")
	private void install_random(String templeDir , String fileName , String fileAddress) throws FileNotFoundException{
		String path = getPath(fileAddress);
			new File(path).mkdirs();
		File outFile = new File(fileAddress);
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		for(int i = 0 ;  ; i++){
			String templeLiitleName = templeDir + "." + fileName + ".part" + i;
			File inFile = new File(templeLiitleName);
			if(inFile.exists())
				new receiveThreadRandom(outFile,inFile,i).start();
			else
				break;
		}	
	}
	
	/**
	 * 用于整合文件的方法 install_random 因方法已废弃 故此类也已废弃
	 * @author forever_xuan7
	 *
	 */
	class receiveThreadRandom extends Thread{
		private RandomAccessFile out =null;
		File outFile = null,inFile = null;
		private InputStream in = null;
		int i = 0 ;
		public receiveThreadRandom(File outFile , File inFile,int i) throws FileNotFoundException{
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
					//Log.i("in",i+": start");
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

	/**
	 * 辅助方法 从路径中得到文件名
	 * @param dir 路径
	 * @return
	 */
	private String getFileName(String dir){
		String name = dir.substring(dir.lastIndexOf("/")+1);
		return name;
	}
	
	/**
	 * 辅助方法 从路径中得到文件夹的位置
	 * @param dir 路径
	 * @return
	 */
	public String getPath(String dir){
		String path  = dir.substring(0, dir.lastIndexOf("/"));
		return path;
	}
	/**
	 * 得到发送的状态
	 * @return 是否发送完成
	 */
	public boolean isSend(){
		return sendFlag;
	}
	/**
	 * 得到接收的状态
	 * @return 是否接收完成
	 */
	public boolean isReceive(){
		return receiveFlag;
	}
	
	//内部类：
	/**
	 * 用于发送过程的内部类  使发送可以多线程完成
	 * @author forever_xuan7
	 *
	 */
	class threadSampleSender extends Thread
	{
		private boolean flag= false , end = false; 
		private int startPos,nChunk,init_i;
		private String templeDir,fileName,Myid;
		public threadSampleSender(int startPos , String templeDir , String fileName , String Myid , int nChunk , int init_i){
			this.startPos = startPos;
			this.templeDir = templeDir;
			this.fileName = fileName;
			this.Myid = Myid;
			this.nChunk = nChunk;
			this.init_i = init_i;
			
		}
		public void run(){
			SampleSend sender = new SampleSend(username[startPos],password[startPos],server,folderName);
			
			int now_i = init_i;
			while(now_i < nChunk){
				String templeName = "." + fileName + ".part" + now_i;
				File temFile = new File(templeDir + templeName);
				if(temFile.exists()){
					flag = sender.send(temFile ,templeName ,templeName ,Myid+":"+now_i,nChunk+"" ,"");
					temFile.delete();	
					Log.i("in",Myid+":"+now_i + "发送结束" +flag);
				}
			now_i = now_i + numOfEmailbox;
			}
			end = true;
			
			Log.i("in",username[startPos]+ "发送结束" );
		}
		public boolean isFinish(){
			return end;
		}
		public boolean isOK(){
			return flag;
		}
		/*
		public int getNumOfMyid(){
			String num = Myid_i.substring(Myid_i.lastIndexOf(":")+1);
			return Integer.decode(num);
		}
		*/
	}
	/**
	 * 用于接收过程的内部类  使接收可以多线程完成
	 * @author forever_xuan7
	 *
	 */
	class threadSampleReceiver extends Thread{
		private boolean flag= false , end = false; 
		private int i;
		private String Myid,storeDir,fileName;
		public threadSampleReceiver(int i ,String Myid ,String storeDir, String fileName){//String Myid,String storeDir
			this.i = i;
			this.Myid= Myid;
			this.storeDir = storeDir;
			this.fileName = fileName;
		}
		public void run(){
			SampleReceive receiver = new SampleReceive(username[i] ,password[i] ,server,folderName);
			Log.i("in","邮箱 " + username[i]+ " 开始接收");
			receiver.receive(Myid,storeDir,fileName);
			flag = receiver.isFinish();
			end = true;
			Log.i("in","邮箱 " + username[i]+ " 接收结束 " +flag);
		}
		
		public boolean isFinish(){
			return end;
		}	
		public boolean isOK(){
			return flag;
		}
		
	}
	/**
	 * 用于删除过程的内部类  使删除可以多线程完成
	 * @author forever_xuan7
	 *
	 */
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
			cs.send("19900517",  "E:/1/彩虹天堂.mp3", "E:/1/12");
			//cs.receive("19901111", "E:/1/12",  "E:/1/123/" ,"彩虹天堂.mp3");
			//cs.split("E:/1/lalala.mp3", "E:/1/12");
			//cs.install_random("E:/1/12", "lalala.mp3", "E:/1/123/完美主义.mp3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

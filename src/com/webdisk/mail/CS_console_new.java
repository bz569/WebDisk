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
 * �������ڷ��͡����պ�ɾ���ʼ�
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
	//public CS_console_new(){}//����ʱʹ��
	/**
	 * ���췽��
	 * @param username ������������
	 * @param password �������������
	 * @param server ��������
	 * @throws NoSuchProviderException
	 */
	public CS_console_new(String[] username,String[] password,String server) throws NoSuchProviderException{
		this.username = username;
		this.password = password;
		this.server = server;
	}
	
	/**
	 * ��������ĸ���
	 * @param num ���ĸ���
	 */
	public void setNumOfEmailBox(int num){
		numOfEmailbox = num;
	} 
	
	/**
	 * ���ñ����������е��ļ�����
	 * @param folderName �ļ�����
	 */
	public void setFolderName(String folderName){
		this.folderName = folderName;
	}
	
	/**
	 * ���÷�Ƭ�Ĵ�С  Ĭ��Ϊ1048576 * 5(5M)
	 * @param chunkLength ��Ƭ�Ĵ�С
	 */
	public void setChunkLength(int chunkLength){
		this.chunkLength = chunkLength;
	}
	
	/**
	 * ��Myidɾ��ɾ���ʼ�
	 * @param Myid �ʼ���Myid 
	 * @param isFuzzy �Ƿ����ģ��ģʽ
	 * true��ɾ�������ʼ�id�а���Myid���ʼ�(������MyidΪ28367298,�ʼ�idΪ28367298:1,����ʼ�����ɾ��)
	 * false��ɾ��ָ��Myid���ʼ�
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
	 * �����ļ�������ķ���
	 * @param Myid �����ʼ���ӵ�Myid
	 * @param dir �ļ�����λ��
	 * @param templeDir ��ʱ�ļ���Ŵ�λ��(����ʱ���ܻ���,����ʱ�ļ��д����ʱ�ļ�,������Ϻ�ɾ��)
	 * @return ���ط��͵�Ƭ��,0��ʾû�з��ͳɹ�
	 */
	public int send(String Myid , String dir ,String templeDir){//��ʱʹ��ȫ���������ٷ���//����Ҫ��pc�˵ĸ�����ͳһ
		Log.i("in","��ʼ ");
		File file = new File(dir);
		if(!file.exists()){
			Log.i("in","û���ҵ��ļ�");
			return 0;
		}
		if(file.length() <= chunkLength){
			//new threadSampleSender(0 ,templeDir ,"_" + this.getFileName(dir) + ".part" + 0 ,Myid+":"+ 0 ,1 ).start();
			SampleSend sender = new SampleSend(username[0],password[0],server,folderName);
			if(sender.send(new File(dir),this.getFileName(dir),this.getFileName(dir),Myid+":0","1",""))
				Log.i("in","�ɹ�����С�ļ�");
			else
				Log.i("in","fail");
			
			return 1;
		}
		int nChunk = (int)file.length() / chunkLength + 1;
		int startPos = (int)(Long.parseLong(Myid) % numOfEmailbox);
//		long startPos = (long)Integer.decode(Myid) % numOfEmailbox;
		//����Ŀ¼:
		Log.i("in","����Ŀ¼ ___ startPos:" +startPos + "___nChunk:" + nChunk);
		if(templeDir.charAt(templeDir.length() - 1) != '/')
			templeDir = templeDir + "/";
		File files = new File(templeDir + "/");
		files.mkdirs();
				
		//����ļ���
		String fileName = this.getFileName(dir);
		Log.i("in","��ʼ ����ļ�" +dir);
		try {
			this.split(dir, templeDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//�����ļ�
		Log.i("in","��ʼ �����ļ�");
		sendThreads = new threadSampleSender[2];
		boolean notEnd = true;
		int i = 0;//i��ʾ��ǰ���͵�ģ��id
		int nowThread = 0;
		while(notEnd){
			if(i < numOfThread && i < nChunk){
				Log.i("in","��ʼ�ϴ�:" + i);
				
				sendThreads[nowThread] = new threadSampleSender(startPos ,templeDir ,fileName ,Myid ,nChunk ,i );//int startPos , String templeDir , String templeName , String Myid_i , int nChunk
				sendThreads[nowThread].start();
				i++;
				startPos = (startPos + 1) % numOfEmailbox;
			}
			else if(i >= numOfThread && i < numOfEmailbox && i < nChunk){
					if(!sendThreads[nowThread].isFinish())
						continue;
					else{
						Log.i("in","��ʼ�ϴ�:" + i);
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
		//�ж��ϴ��Ƿ��Ѿ�����
		int i1 = 0 ;
		Log.i("in","���һ�������Ѿ���ʼ�ϴ� ���ж��Ƿ����");
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
		Log.i("in","���ͽ���");

		sendFlag = true;
		return nChunk;
	}
	
	/**
	 * ����������ļ��ķ���
	 * @param Myid ���ļ���Myid
	 * @param templeDir ��ʱ�ļ���
	 * @param fileDir ����ļ���λ��
	 * @param fileName �����ļ�������
	 * @throws IOException
	 */
	public void receive(String Myid , String templeDir  , String fileDir , String fileName) throws IOException{//��ʱ����ȫ�����ص���ʱ�ļ��к���ƴװ//,String templeName
		
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
				//Log.i("in","��ʼ����i:" + i );
				//String Myid_i = Myid + ":" + i;
				//templeName =  "." + fileName + ".part" + i;
				receiveThreads[i] = new threadSampleReceiver(i , Myid ,templeDir,fileName);
				receiveThreads[i].start();
				i++;
		}
		int i1 = 0 ;
		Log.i("in","�ж��Ƿ����");
		while(true){
			if(receiveThreads[i1].isFinish())
				i1++;
			if(i1 >= numOfEmailbox)
				break;
		}
		Log.i("in","���ؽ��� ��ʼ����");
		install(templeDir ,fileName,fileDir + fileName);
		receiveFlag = true;
	}
	
	/**
	 * �ڲ�  ��ַ���
	 * @param fileAddress �ļ���·��
	 * @param templeDir ��ֺ����ļ���λ��
	 * @throws Exception
	 */
	private void split(String fileAddress ,String templeDir ) throws Exception{
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
	 * �ڲ� ���Ϸ���
	 * @param templeDir ��ʱ�ļ��е�λ��
	 * @param fileName ��ʱ�ļ����ļ���(���в��� .xxxxx.part1,.xxxxx.part2��ʽ����)
	 * @param fileAddress ����ļ���·��
	 * @throws IOException
	 */
	private void install(String templeDir ,String fileName,String fileAddress) throws IOException{//FileNameΪ��װ����ļ������λ��+�ļ��� //,String templeName
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
	 * �ڲ� ��һ�������ļ��ķ��� �ѷ���
	 * @param templeDir ��ʱ�ļ��е�λ��
	 * @param fileName ��ʱ�ļ����ļ���
	 * @param fileAddress ����ļ���·��
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
	 * ���������ļ��ķ��� install_random �򷽷��ѷ��� �ʴ���Ҳ�ѷ���
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
	 * �������� ��·���еõ��ļ���
	 * @param dir ·��
	 * @return
	 */
	private String getFileName(String dir){
		String name = dir.substring(dir.lastIndexOf("/")+1);
		return name;
	}
	
	/**
	 * �������� ��·���еõ��ļ��е�λ��
	 * @param dir ·��
	 * @return
	 */
	public String getPath(String dir){
		String path  = dir.substring(0, dir.lastIndexOf("/"));
		return path;
	}
	/**
	 * �õ����͵�״̬
	 * @return �Ƿ������
	 */
	public boolean isSend(){
		return sendFlag;
	}
	/**
	 * �õ����յ�״̬
	 * @return �Ƿ�������
	 */
	public boolean isReceive(){
		return receiveFlag;
	}
	
	//�ڲ��ࣺ
	/**
	 * ���ڷ��͹��̵��ڲ���  ʹ���Ϳ��Զ��߳����
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
					Log.i("in",Myid+":"+now_i + "���ͽ���" +flag);
				}
			now_i = now_i + numOfEmailbox;
			}
			end = true;
			
			Log.i("in",username[startPos]+ "���ͽ���" );
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
	 * ���ڽ��չ��̵��ڲ���  ʹ���տ��Զ��߳����
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
			Log.i("in","���� " + username[i]+ " ��ʼ����");
			receiver.receive(Myid,storeDir,fileName);
			flag = receiver.isFinish();
			end = true;
			Log.i("in","���� " + username[i]+ " ���ս��� " +flag);
		}
		
		public boolean isFinish(){
			return end;
		}	
		public boolean isOK(){
			return flag;
		}
		
	}
	/**
	 * ����ɾ�����̵��ڲ���  ʹɾ�����Զ��߳����
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
			cs.send("19900517",  "E:/1/�ʺ�����.mp3", "E:/1/12");
			//cs.receive("19901111", "E:/1/12",  "E:/1/123/" ,"�ʺ�����.mp3");
			//cs.split("E:/1/lalala.mp3", "E:/1/12");
			//cs.install_random("E:/1/12", "lalala.mp3", "E:/1/123/��������.mp3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}

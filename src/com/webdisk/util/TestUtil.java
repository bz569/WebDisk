package com.webdisk.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.webdisk.application.SVNApplication;

import android.annotation.SuppressLint;
import android.os.Environment;

/**
 * ���ں�̨���Ը����
 * @author ZhangBoxuan
 *
 */
@SuppressLint("SimpleDateFormat")
public class TestUtil
{
	private final static String dstTestUrl = "http://10.109.34.24/wangpan/2574402613.qq.com/Test/";
	private final static String srcTestPath = Environment.getExternalStorageDirectory() + "/Webdisk/cache/Test/";
	
	public void doTest(SVNApplication mApp)
	{
		//����������Խ����log�ļ�
		File logFile = new File(Environment.getExternalStorageDirectory() + "/Webdisk/cache/TestLog.log");
		try
		{
			logFile.createNewFile();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
		String testStartTime = df.format(new Date());
		logToFile("------------------��ʼ����-------------" + testStartTime + "\n");
		
		//�������ڲ��Ե�Ŀ¼
		File testDir = new File(srcTestPath);
		testDir.mkdir();
		
		//������100���ļ��ϴ�
		logToFile("��һ�����ϴ�100���ļ�\n");
		//��¼��ʼʱ��
		long startTime = System.currentTimeMillis(); 
		for(int i = 0; i < 100; i++)
		{
			//�����ļ�
			String fileName = i + ".txt";
			String filePath = srcTestPath + fileName;
			File testFile = new File(filePath);
			try
			{
				testFile.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			//�ϴ�
			UploadUtil uploader = new UploadUtil(mApp, null, filePath, dstTestUrl);
			uploader.startUpload();
		}
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		//�����д����־
		logToFile("----�ϴ���������ʱ" + usedTime + "ms----\n");
		
		//��100���ļ��������ѡ10���ļ����в���,��д����־
		HashSet<Integer> pickedFiles = pickFiles();
		Iterator<Integer> it = pickedFiles.iterator();
		logToFile("�ڶ�����ѡȡ�ļ�");
		while(it.hasNext())
		{
			logToFile(it.next().toString() + ".txt, ");
		}
		logToFile("�����ļ���������");
		
		//�����½��ļ���
		logToFile("�����½��ļ���:moveTest, copyTest");
		mApp.doMkDir("http://10.109.34.24/wangpan/2574402613.qq.com/Test/moveTest");
		mApp.doMkDir("http://10.109.34.24/wangpan/2574402613.qq.com/Test/copyTest");
		
		//��ѡ�е��ļ����Ƶ�moveTest
		logToFile("���Ը����ļ� ==> " + dstTestUrl + "copyTest");
		startTime = System.currentTimeMillis();
		it = pickedFiles.iterator();
		while(it.hasNext())
		{
			String fileName = it.next() + ".txt";
			CopyUtil copier = new CopyUtil(mApp, null, srcTestPath + fileName, dstTestUrl + "copyTest");
			copier.startCopy();
		}
		endTime = System.currentTimeMillis();
		usedTime = endTime - startTime;
		logToFile("----���ƽ�������ʱ" + usedTime + "ms----\n");
		
		// TODO �������Թ���
		
	}
	
	private void logToFile(String content)
	{
		File logFile = new File(Environment.getExternalStorageDirectory() + "/Webdisk/cache/Log/TestLog.log");
		FileWriter wr;
		try
		{
			wr = new FileWriter(logFile);
			wr.write(content);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private HashSet<Integer> pickFiles()
	{
		HashSet<Integer> result = new HashSet<Integer>();
		
		while(result.size() < 10)
		{
			int ranNum = (int)(Math.random() * 100);
			result.add(ranNum);
		}
		
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

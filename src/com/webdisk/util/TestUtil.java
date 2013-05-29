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
 * 用于后台测试各项功能
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
		//建立保存测试结果的log文件
		File logFile = new File(Environment.getExternalStorageDirectory() + "/Webdisk/cache/TestLog.log");
		try
		{
			logFile.createNewFile();
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String testStartTime = df.format(new Date());
		logToFile("------------------开始测试-------------" + testStartTime + "\n");
		
		//建立用于测试的目录
		File testDir = new File(srcTestPath);
		testDir.mkdir();
		
		//先生成100个文件上传
		logToFile("第一步：上传100个文件\n");
		//记录开始时间
		long startTime = System.currentTimeMillis(); 
		for(int i = 0; i < 100; i++)
		{
			//生成文件
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
			
			//上传
			UploadUtil uploader = new UploadUtil(mApp, null, filePath, dstTestUrl);
			uploader.startUpload();
		}
		long endTime = System.currentTimeMillis();
		long usedTime = endTime - startTime;
		//将结果写入日志
		logToFile("----上传结束：耗时" + usedTime + "ms----\n");
		
		//从100个文件中随机挑选10个文件进行操作,并写入日志
		HashSet<Integer> pickedFiles = pickFiles();
		Iterator<Integer> it = pickedFiles.iterator();
		logToFile("第二步：选取文件");
		while(it.hasNext())
		{
			logToFile(it.next().toString() + ".txt, ");
		}
		logToFile("进行文件操作测试");
		
		//测试新建文件夹
		logToFile("测试新建文件夹:moveTest, copyTest");
		mApp.doMkDir("http://10.109.34.24/wangpan/2574402613.qq.com/Test/moveTest");
		mApp.doMkDir("http://10.109.34.24/wangpan/2574402613.qq.com/Test/copyTest");
		
		//将选中的文件复制到moveTest
		logToFile("测试复制文件 ==> " + dstTestUrl + "copyTest");
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
		logToFile("----复制结束：耗时" + usedTime + "ms----\n");
		
		// TODO 继续测试过程
		
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

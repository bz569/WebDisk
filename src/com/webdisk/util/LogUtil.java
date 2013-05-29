package com.webdisk.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import android.os.Environment;

public class LogUtil
{
	public static void logToFile(String content)
	{
//		File logFile = new File(Environment.getExternalStorageDirectory() + "/RobotiumTest/TestLog");
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");       
		String logFileName = "log_" + sDateFormat.format(new java.util.Date()) + ".log"; 
		File logFile = new File(Environment.getExternalStorageDirectory() + "/RobotiumTest/TestLog/" + logFileName);
		
		BufferedWriter writer;
		try
		{
			writer = new BufferedWriter(new FileWriter(logFile,true));//true表示追加到末尾
			writer.append(content);
			writer.flush();//使用Buffered***时一定要先清缓冲区再关闭流
			writer.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		FileWriter wr;
//		try
//		{
//			wr = new FileWriter(logFile);
//			wr.write(content);
//			System.out.println("写入日志" + content);
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		
	}
}

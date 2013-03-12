package com.webdisk.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;

public class FileUtil
{
	public static String genFileId()
	{
		String headerStr = "00001";
		
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		
		int time = hour * 10000 + min * 100 + sec;
		String timeStr = Integer.toBinaryString(time);
		
		while(timeStr.length() < 20)
		{
			timeStr = "0" + timeStr;
		}
		
		Random rdm = new Random(System.currentTimeMillis());
        int rd = Math.abs(rdm.nextInt())%127+1;
		String rdStr = Integer.toBinaryString(rd);
		
		while(rdStr.length() < 7)
		{
			rdStr = "0" + rdStr;
		}
		
		String binaryFileID = headerStr + timeStr + rdStr;
		
		BigInteger tmp = new BigInteger(binaryFileID, 2);
		String fileID = tmp.toString();
		
		return fileID;
	}
}

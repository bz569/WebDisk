package com.webdisk.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Random;

import com.webdisk.model.UserConfig;

public class FileUtil
{
	public static String genFileId()
	{
		// arrayID
		String arrayIDStr = "000";
		// flag
		String flagStr = "01";
		String headerStr = arrayIDStr + flagStr;
		
		//������ɲ���
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);

		int time = hour * 10000 + min * 100 + sec;
		String timeStr = Integer.toBinaryString(time);

		while (timeStr.length() < 20)
		{
			timeStr = "0" + timeStr;
		}

		Random rdm = new Random(System.currentTimeMillis());
		int rd = Math.abs(rdm.nextInt()) % 127 + 1;
		String rdStr = Integer.toBinaryString(rd);

		while (rdStr.length() < 7)
		{
			rdStr = "0" + rdStr;
		}

		String binaryFileID = headerStr + timeStr + rdStr;

		// ��xml�ж�ȡuserid
		UserConfig userConfig = ReadXMLUtil.getConfigFromXML();
		String userID = userConfig.getUserID();
		String bUserID = Integer.toBinaryString(Integer.parseInt(userID));
		
		//ƴ��userid��fileID
		String bMID = bUserID + binaryFileID;

		// תΪʮ����
		BigInteger tmp = new BigInteger(bMID, 2);
		String mID = tmp.toString();

		return mID;
	}

}

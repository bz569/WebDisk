package com.webdisk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.acl.LastOwnerException;

import javax.mail.NoSuchProviderException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.application.SVNApplication;
import com.webdisk.mail.CS_console_new;

public class CopyUtil
{
	private static final String TAG = "CopyUtil";
	
	private static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";
	
	private static String[] USERNAME = {"cyberbox1@163.com","cyberbox2@163.com","cyberbox3@163.com","cyberbox4@163.com","cyberbox5@163.com"};
	private static String[] PASSWORD = {"cyberbox","cyberbox","cyberbox","cyberbox","cyberbox"};
	private static String SERVER = "imap.163.com";
	private static String USER_ID = "363";

	private static final int COPY_MSG = 13;
	private static final int COPY_START = 133;
	private static final int COPY_SUCCESS = 131;
	private static final int COPY_ERROR = 130;

	private SVNApplication copyApp;

	private String srcPath;
	private String dstPath;
	private String suffix;
	private String fileName;
	private Handler copyServiceHandler;

	public CopyUtil(SVNApplication copyApp, Handler copyServiceHandler,
			String srcPath, String dstPath)
	{
		this.copyApp = copyApp;
		this.copyServiceHandler = copyServiceHandler;
		this.srcPath = srcPath;
		this.dstPath = dstPath;

		this.fileName = srcPath.substring(srcPath.lastIndexOf("/") + 1,
				srcPath.length());
		this.suffix = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());

		Log.i(TAG, "New CopyUtil: " + "srcPath=" + srcPath + ";dstPath="
				+ dstPath + ";filename=" + fileName + ";suffix=" + suffix);
	}

	public void startCopy()
	{
		// if("txt".equals(suffix) || "doc".equals(suffix) ||
		// "docx".equals(suffix) || "pdf".equals(suffix)
		// || "ppt".equals(suffix) || "pptx".equals(suffix))// TODO
		// ֱ���ϴ���SVN���������ļ�����
		// {
		// copyOnSVN();
		// }

		// ���Ϳ�ʼ������Ϣ
		Message copyMsg = new Message();
		copyMsg.what = COPY_MSG;
		copyMsg.arg1 = COPY_START;
		copyServiceHandler.sendMessage(copyMsg);

		// ��SVN��ȡĿ���ļ�props
		String isMail = copyApp.doGetProperty(srcPath, "magicgourd:ismail");
		String mId = copyApp.doGetProperty(srcPath, "magicgourd:id");
		String size = copyApp.doGetProperty(srcPath, "magicgourd:size");
		Log.i(TAG, "COPY1:getProp: isMail = " + isMail + ";id=" + mId);

		if (isMail.equals("0"))//�ļ�ȫ����svn
		{
			copyOnSVN();
		} else if (isMail.equals("1"))//������������
		{
			copyOnMail(mId, size);
		} else if (isMail.equals("PROP_NOT_EXSIT"))// ��web���ϴ����ļ�û��svn properties
		{
			// ���ļ�export��cache�ļ���
			File cacheFile = new File(CACHE_DIR + fileName);

			SVNURL svnUrl = null;
			try
			{
				svnUrl = SVNURL.parseURIEncoded(srcPath);
			} catch (SVNException e)
			{
				e.printStackTrace();
			}
			copyApp.doExport(SVNRevision.HEAD, cacheFile, svnUrl);

			String firstLine = "";
			try
			{
				FileReader fr = new FileReader(cacheFile);
				BufferedReader br = new BufferedReader(fr);
				firstLine = br.readLine();

				br.close();
				fr.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			// String judgeString = firstLine.substring(0, 10);
			// Log.i(TAG, "judgeStr=" + judgeString);
			// if(!judgeString.equals("magicgourd"))
			if (!firstLine.contains("magicgourd"))//�ļ�ȫ����svn��
			{
				//ֱ����svn�и���
				Log.i(TAG, "��SVNֱ�Ӹ���web���ϴ����ļ�");
				copyOnSVN();

				// ������ɵ�cacheFile�����������Ϣ
				cacheFile.delete();
				//���͸��������Ϣ
				copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
				copyMsg.arg1 = COPY_SUCCESS;
				copyServiceHandler.sendMessage(copyMsg);
			} else
			{
				//�����临��
				Log.i(TAG, "�����临��web���ϴ����ļ�");
				// ��cacheFile��ȡid
				try
				{
					FileReader fr = new FileReader(cacheFile);
					BufferedReader br = new BufferedReader(fr);
					String line = "";
					while ((line = br.readLine()) != null)
					{
						if (line.contains("magicgourd:id"))
						{
							String[] tmp = line.split("==");
							mId = tmp[tmp.length - 1];
						}
						if(line.contains("magicgourd:size"))
						{
							String[] tmp = line.split("==");
							size = tmp[tmp.length - 1];
						}
					}

					br.close();
					fr.close();
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				// �����ļ�
				Log.i(TAG, "mID of file from WEB: " + mId);
				copyOnMail(mId, size);
				
				//ɾ��cache�ļ�
				cacheFile.delete();
				//���͸��������Ϣ
				copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
				copyMsg.arg1 = COPY_SUCCESS;
				copyServiceHandler.sendMessage(copyMsg);
			}
		}
	}

	private void copyOnSVN()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Message copyMsg = new Message();
				copyMsg.what = COPY_MSG;
				copyMsg.arg1 = COPY_START;
				copyServiceHandler.sendMessage(copyMsg);

				if (copyApp.doCopy(srcPath, dstPath))
				{
					copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
					copyMsg.arg1 = COPY_SUCCESS;
					copyServiceHandler.sendMessage(copyMsg);
				} else
				{
					copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
					copyMsg.arg1 = COPY_ERROR;
					copyServiceHandler.sendMessage(copyMsg);
				}
			}
		}).start();
	}

	private void copyOnMail(final String mId, final String size)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// TODO �����临��
				Log.i(TAG, "������������cache");
				//�����ص�����
				try
				{
					CS_console_new mMailDownloader = new CS_console_new(USERNAME, PASSWORD, SERVER);
					Log.i(TAG, "copy���������ļ�����" + CACHE_DIR + "copy_" + fileName);
					mMailDownloader.receive(mId, CACHE_DIR, CACHE_DIR, "copy_" + fileName);
				} catch (NoSuchProviderException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				
				//���ϴ���Ŀ��λ��
				File tmpFile = new File(CACHE_DIR + fileName);//���ļ������ϴ���svn
				try
				{
					tmpFile.createNewFile();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}	
				
				String tmpFilePath = tmpFile.getPath();
				
				String fileID = FileUtil.genFileId();
				String mID = USER_ID + fileID;
				
				//�����µ�SVN����
				SVNProperties mProperties = new SVNProperties();
				mProperties.put("magicgourd:id", USER_ID + fileID);
				mProperties.put("magicgourd:owner", copyApp.getCurrentConnection().getUsername());
				mProperties.put("magicgourd:size", size);
				mProperties.put("magicgourd:timestamp", Long.toString(System.currentTimeMillis()));
				mProperties.put("magicgourd:ismail", Integer.toString(1));
				
				//�ϴ�������
				CS_console_new mMailSender = null;
				try
				{
					mMailSender = new CS_console_new(USERNAME, PASSWORD, SERVER);
				} catch (NoSuchProviderException e)
				{
					e.printStackTrace();
				}
				
				Log.i(TAG, "New File ID=" + mID);
				mMailSender.send(mID, CACHE_DIR + "copy_" + fileName, CACHE_DIR);
				
				//�ϴ����ļ���SVN
				Log.i(TAG, "�ϴ����ļ���svn��" + tmpFilePath + "==>" + dstPath);
				copyApp.doImport(tmpFilePath, dstPath.substring(0, dstPath.lastIndexOf("/") + 1), mProperties);
				//ɾ�����ɵ���ʱ�ļ�
				File downloadFile = new File(CACHE_DIR + "copy_" + fileName);
				downloadFile.delete();
				File cacheFile = new File(tmpFilePath);
				cacheFile.delete();
				
				//���͸��������Ϣ
				Message copyMsg = copyServiceHandler.obtainMessage(COPY_MSG);
				copyMsg.arg1 = COPY_SUCCESS;
				copyServiceHandler.sendMessage(copyMsg);
			}
		}).start();
	}
}

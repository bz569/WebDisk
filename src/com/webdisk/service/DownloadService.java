package com.webdisk.service;

import com.webdisk.activity.ShowFileActivity;
import com.webdisk.application.SVNApplication;
import com.webdisk.util.DownloadUtil;
import com.webdisk.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadService extends IntentService
{
	private static final String TAG = "DownloadService";
	
	private static final int EXPORT_START = 0;
	private static final int EXPORT_FINISH = 2;
	
	private SVNApplication app;
	
	private String srcUrl;
	private String desPath;
	private String fileName;
	
	private Handler downloadServiceHandler= new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case EXPORT_START:
				{
					Log.i(TAG, "��ʼ����");
					showExportNotification(false);
					break;
				}
				case EXPORT_FINISH:
				{
					Log.i(TAG, "�������");
					showExportNotification(true);
					stopSelf();
					break;
				}
			}
			
			super.handleMessage(msg);
		}
	};
	
	public DownloadService()
	{
		super("DownloadService");

	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		app = (SVNApplication)getApplication();
		
		srcUrl = intent.getStringExtra("SRC_PATH");
		desPath = intent.getStringExtra("DES_PATH");
		
		Log.i(TAG, "intent:" + srcUrl);

		DownloadUtil downloader = new DownloadUtil(app, downloadServiceHandler, srcUrl, desPath);
		fileName = downloader.getFileName();
		downloader.startDownload();
	}
	
	//��svn������ֱ��export�ļ�ʱ��֪ͨ������
	private void showExportNotification(boolean finish)
	{
		Notification notification;
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, ShowFileActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (!finish)
		{
			notification = new Notification(R.drawable.ic_launcher, "��������", System.currentTimeMillis());
			notification.setLatestEventInfo(this, "����", "��������:" + fileName, contentIntent);
		}
		else
		{
			notification = new Notification(R.drawable.ic_launcher, "�������", System.currentTimeMillis());
			notification.setLatestEventInfo(this, "����", fileName + "�������", contentIntent);
		}
//		notification.defaults=Notification.DEFAULT_LIGHTS;
		manager.notify(R.layout.activity_download, notification);
	}

}

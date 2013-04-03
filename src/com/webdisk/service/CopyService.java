package com.webdisk.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.webdisk.R;
import com.webdisk.activity.ShowFileActivity;
import com.webdisk.application.SVNApplication;
import com.webdisk.util.CopyUtil;

public class CopyService extends IntentService
{
	private static final String TAG = "CopyService";
	
	private static final int COPY_MSG = 13;
	private static final int COPY_START = 133;
	private static final int COPY_SUCCESS = 131;
	private static final int COPY_ERROR = 130;
	
	private SVNApplication mApp;
	
	private String srcFilePath;
	private String dstPath;
	private String fileName;
	
	private Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			if(msg.what == COPY_MSG)
			{
				switch(msg.arg1)
				{
					case COPY_START:
					{
						showImportNotification(COPY_START);
						break;
					}
					case COPY_ERROR:
					{
						showImportNotification(COPY_ERROR);
						break;
					}
					case COPY_SUCCESS:
					{
						showImportNotification(COPY_SUCCESS);
						
						//通过broadcast发送消息
						Intent intent = new Intent().setAction("com.webdisk.broadcast.REFRESH");
						sendBroadcast(intent);
						Log.i(TAG, "send msg by broadcast");
						
						break;
					}
				}
			}
			stopSelf();
			super.handleMessage(msg);
		}
		
	};
	
	public CopyService()
	{
		super("CopuService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG, "测试copy");
		
		mApp = (SVNApplication) getApplication();
		
		srcFilePath = intent.getStringExtra("SRC_FILE_PATH");
		dstPath = intent.getStringExtra("DST_PATH");
		fileName = intent.getStringExtra("FILE_NAME");
		
		CopyUtil copier = new CopyUtil(mApp, mHandler, srcFilePath, dstPath);
		copier.startCopy();
	}
	
	//通知栏内容
	private void showImportNotification(int status)
	{
		Notification notification;
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, ShowFileActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (status == COPY_START)
		{
		notification = new Notification(R.drawable.ic_launcher_48, "正在复制", System.currentTimeMillis());
		notification.setLatestEventInfo(this, "CyberBox", "正在复制:" + fileName, contentIntent);
		}
		else if(status == COPY_SUCCESS)
		{
			notification = new Notification(R.drawable.ic_launcher_48, "复制完成", System.currentTimeMillis());
			notification.setLatestEventInfo(this, "CyberBox", fileName + "复制完成", contentIntent);
		}
		else
		{
			notification = new Notification(R.drawable.ic_launcher_48, "复制失败", System.currentTimeMillis());
			notification.setLatestEventInfo(this, "CyberBox", fileName + "复制失败", contentIntent);
		}
//		notification.defaults=Notification.DEFAULT_LIGHTS;
		manager.notify(R.layout.activity_download, notification);
	}
	
}

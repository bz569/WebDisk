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
import com.webdisk.util.UploadUtil;

public class UploadService extends IntentService
{
	private static final String TAG = "UploadService";
	
//	private static final int IMPORT_START = 200;
//	private static final int IMPORT_FINISH = 202;
	
	private static final int IMPORT = 200;
	
	private SVNApplication app;
	
	private String srcFilePath;
	private String dstUrl;
	private String fileName;
	
	private Handler uploadServiceHandler= new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
//			switch(msg.what)
//			{
//				case IMPORT_START:
//				{
//					Log.i(TAG, "开始上传");
//					showImportNotification(false);
//					break;
//				}
//				case IMPORT_FINISH:
//				{
//					Log.i(TAG, "下载完成");
//					showImportNotification(true);
//					stopSelf();
//					break;
//				}
//			}
			if(msg.what == IMPORT)
			{
				switch(msg.arg1)
				{
					case 0:
					{
						Log.i(TAG, "开始上传");
						showImportNotification(false);
						break;
					}
					case 1:
					{
						Log.i(TAG, "上传完成");
						showImportNotification(true);
//						stopSelf();
						
						//通过broadcast发送消息
						Intent intent = new Intent().setAction("com.webdisk.broadcast.UPLOAD_FINISH");
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
	
	public UploadService()
	{
		super("UploadService");

	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG, "UploadService Start");
		
		app = (SVNApplication)getApplication();
		
		srcFilePath = intent.getStringExtra("SRC_FILE_PATH");
		dstUrl = intent.getStringExtra("DST_URL");

		UploadUtil uploader = new UploadUtil(app, uploadServiceHandler, srcFilePath, dstUrl);
		fileName = uploader.getFileName();
		uploader.startUpload();
	}
	
	//从svn服务器直接export文件时，通知栏内容
		private void showImportNotification(boolean finish)
		{
			Notification notification;
			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent(this, ShowFileActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (!finish)
			{
				notification = new Notification(R.drawable.ic_launcher, "正在上传", System.currentTimeMillis());
				notification.setLatestEventInfo(this, "CyberBox", "正在上传:" + fileName, contentIntent);
			}
			else
			{
				notification = new Notification(R.drawable.ic_launcher, "上传完成", System.currentTimeMillis());
				notification.setLatestEventInfo(this, "CyberBox", fileName + "上传完成", contentIntent);
			}
//			notification.defaults=Notification.DEFAULT_LIGHTS;
			manager.notify(R.layout.activity_download, notification);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

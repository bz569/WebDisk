package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.webdisk.R;
import com.webdisk.adapter.UploadFileListAdapter;
import com.webdisk.application.SVNApplication;
import com.webdisk.service.UploadService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends Activity
{
	
	private final static String TAG = "UploadActivity";
	
	private final static String WEBDISK_ROOT_URL = "http://10.109.34.24/wangpan/";
	
	private SVNApplication mApp;
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private TextView tv_showUploadPath;
	private ListView lv_showFile;
	private Button btn_upload;
	private Button btn_cancel;
	
	private List<String> items = null;
	private List<String> paths = null;
	private ArrayList<String> svnItemList = null;
//	private String rootPath = "/sdcard";
//	private String curPath = "/sdcard"; // TODO �˴��������̻����ļ�·��
	private String rootPath = Environment.getExternalStorageDirectory().toString();
	private String curPath = Environment.getExternalStorageDirectory().toString(); // TODO �˴��������̻����ļ�·��
	
	private String srcFilePath;
	private String dstPath;
	private String dstUrl;
	
	private boolean isExist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		mApp = (SVNApplication)getApplication();
		
		Intent intent = getIntent();
		svnItemList = intent.getStringArrayListExtra("SVN_DIR_FILES");
		dstPath = intent.getStringExtra("UPLOAD_DST_PATH");
		dstUrl = WEBDISK_ROOT_URL + mApp.getCurrentConnection().getUsername() + "/" + dstPath;
		
		btn_naviationPrevious = (Button) findViewById(R.id.btn_upload_naviationPrevious);
		btn_upload = (Button) findViewById(R.id.btn_upload);
		btn_cancel = (Button) findViewById(R.id.btn_upload_cancel);
		tv_showFolderName = (TextView) findViewById(R.id.tv_upload_showFolderName);
		tv_showUploadPath = (TextView) findViewById(R.id.tv_showUploadFolderPath);
		lv_showFile = (ListView)findViewById(R.id.lv_upload_showFile);
		
		//ΪButton���ô���Ч��
		 btn_naviationPrevious.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.drawable.icon_navigation_previous_item_touched);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.drawable.icon_navigation_previous_item);
					}
					
					return false;
				}
			});
		 
		 
		 btn_upload.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.color.halo_darkblue);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.color.halo_lightblue);
					}
					
					return false;
				}
			});
		 
		 btn_cancel.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.color.halo_darkblue);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.color.halo_lightblue);
					}
					
					return false;
				}
			});
		 
		 
		 
		 getFileDir(rootPath);
		 
		 lv_showFile.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					File file = new File(paths.get(position));
					if(file.isDirectory())
					{
						curPath = paths.get(position);
						getFileDir(paths.get(position));
					}
					else
					{
						//��ȡ�ϴ��ļ���·��
//						srcFilePath = paths.get(position);
						if(((ListView)parent).getTag() == null)
						{
							((ListView)parent).setTag(view);
							view.setBackgroundResource(R.color.filelist_selected);
							srcFilePath = paths.get(position);
						}
						else if(((View)((ListView)parent).getTag()).equals(view))//�ظ���ѡһ����Ŀ
						{
							srcFilePath = null;
							((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
							((ListView)parent).setTag(null);
						}
						else
						{
							((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
							view.setBackgroundResource(R.color.filelist_selected);
							srcFilePath = paths.get(position);
							((ListView)parent).setTag(view);
						}
						
						Log.i(TAG, "ѡȡ�ļ�" + srcFilePath);
//						if(((ListView)parent).getTag() != null)
//						{
//							((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
//							
//					
//						}
//						else
//						{
//							((ListView)parent).setTag(view);
//							view.setBackgroundResource(R.color.filelist_selected);
//							srcFilePath = paths.get(position);
//						}
					}
				}
			 	
			});
		 
		 //Ϊ���ذ�ť����OnClickListener
		 btn_naviationPrevious.setOnClickListener(new Button.OnClickListener()
		 {
			@Override
			public void onClick(View v)
			{
				Log.i(TAG, "��ǰ·����" + curPath);
				File curFile = new File(curPath);
				curPath = curFile.getParent();
				Log.i(TAG, "�ϲ�·����" + curPath);
				
				getFileDir(curPath);
			}
		 });
		 
		 //Ϊ�ϴ���ť����OnclickListener
		 btn_upload.setOnClickListener(new Button.OnClickListener()
		 {
			@Override
			public void onClick(View v)
			{
				Log.i(TAG, "�ϴ��ļ�src=" + srcFilePath + ";dst=" + dstUrl);
				isExist = false;
//				mApp.doImport(srcFilePath, dstUrl);
//				finish();
				
				if(srcFilePath != null)
				{
					String fileName = new File(srcFilePath).getName();
					//�ж�Ŀ���ļ������Ƿ��������ļ�
					for(String tmp:svnItemList)
					{
						if(fileName.equals(tmp))
						{
							isExist = true;
						}
						Log.i(TAG, "tmp=" + tmp + ";isExist=" + isExist);
					}
					
					if(isExist)
					{
						Toast.makeText(UploadActivity.this, R.string.file_exist, Toast.LENGTH_SHORT).show();
					}
					else
					{
						Intent intent = new Intent(UploadActivity.this, UploadService.class);
						intent.putExtra("SRC_FILE_PATH", srcFilePath);
						intent.putExtra("DST_URL", dstUrl);
						startService(intent);
						finish();
					}
				}
				else
				{
					Toast.makeText(UploadActivity.this, R.string.please_select_file, Toast.LENGTH_SHORT).show();
				}
			}
		 });
		 
		 //Ϊcancle��ť����onclickListener
		 btn_cancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
		 
		 // TODO textview��ʾ�ϴ��ļ�Ŀ¼
		 tv_showUploadPath.setText(dstPath);
		 
	}
	
	private void getFileDir(String filePath)
	{
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		
		File f = new File(filePath);
		
		
		Log.i(TAG, "filePath=" + filePath + "&rootPath=" + rootPath);
		
		//��ǰĿ¼Ϊ��Ŀ¼ʱ
		if(filePath.equals(rootPath))
		{
			Log.i(TAG, "filePath == rootPath");
			tv_showFolderName.setText(R.string.mywebdisk);
			btn_naviationPrevious.setEnabled(false);
			btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item_disable);
		}
		else
		{
			tv_showFolderName.setText(f.getName());
			btn_naviationPrevious.setEnabled(true);
			btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item);
		}
		
		File[] files = f.listFiles();
		
		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			items.add(file.getName());
			paths.add(file.getPath());
		}
		
		lv_showFile.setAdapter(new UploadFileListAdapter(this, items, paths));
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//����ʵ�巵�ذ�������
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			//��ǰΪ��Ŀ¼ʱ������
			if(curPath.equals(rootPath))
			{
				finish();
			}
			else//��ǰ��Ϊ��Ŀ¼ʱ�������ϲ�
			{
				File curFile = new File(curPath);
				curPath = curFile.getParent();
				Log.i(TAG, "�ϲ�·����" + curPath);
				
				getFileDir(curPath);
			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
		
		
	}
	
}

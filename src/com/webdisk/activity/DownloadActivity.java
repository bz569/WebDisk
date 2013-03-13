package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.webdisk.R;
import com.webdisk.adapter.UploadFileListAdapter;
import com.webdisk.application.SVNApplication;
import com.webdisk.service.DownloadService;
import com.webdisk.util.DownloadUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends Activity
{
	
	private final static String TAG = "DownloadActivity";
	
	private SVNApplication mApp;
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private TextView tv_showDownloadFile;
	private ListView lv_showFile;
	private Button btn_download;
	private Button btn_cancel;
	private Button btn_newFolder;
	
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = Environment.getExternalStorageDirectory().toString();
	private String curPath = Environment.getExternalStorageDirectory().toString();
	
	private String filePath = null;//Ҫ���ص��ļ�·��
	
	private PopupWindow newFolderDialog;
	private View view;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		mApp = (SVNApplication)getApplication();
		
		Intent intent = getIntent();
		filePath = intent.getStringExtra("FILE_PATH");
		Log.i(TAG, "filePath=" + filePath);
		
		btn_naviationPrevious = (Button) findViewById(R.id.btn_download_naviationPrevious);
		btn_download = (Button) findViewById(R.id.btn_download);
		btn_cancel = (Button) findViewById(R.id.btn_download_cancel);
		btn_newFolder = (Button)findViewById(R.id.btn_download_newfolder);
		tv_showFolderName = (TextView) findViewById(R.id.tv_download_showFolderName);
		tv_showDownloadFile = (TextView) findViewById(R.id.tv_showdownloadFileName);
		lv_showFile = (ListView)findViewById(R.id.lv_download_showFile);
		
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
		 
		 btn_newFolder.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.drawable.icon_newfolder_touched);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.drawable.icon_newfolder);
					}
					
					return false;
				}
			});
		 
		 btn_download.setOnTouchListener(new Button.OnTouchListener()
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
						//�˴���Ӷ��ļ��Ĳ���
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
		 
		 //Ϊnewfolder��ť����OnclickListener
		 btn_newFolder.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showNewFolderDialog(v);
			}
		});
		 
		 //Ϊdownload��ť����onClickListener
		 btn_download.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
//				DownloadUtil downloaderUtil = new DownloadUtil(mApp, mHandler, filePath, curPath);
//				downloaderUtil.startDownload();
//				finish();
				//��ʼ��̨����
				Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
				intent.putExtra("SRC_PATH", filePath);
				intent.putExtra("DES_PATH", curPath);
				startService(intent);
				finish();
			}
		});
		 
		 //Ϊcancle��ť����onClickListener
		 btn_cancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				finish();
			}
		});
		 
		 //textview��ʾ�����ļ��ļ�
		 String[] tmp = filePath.split("/");
		 tv_showDownloadFile.setText(tmp[tmp.length-1]);
		 
		 
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
			tv_showFolderName.setText(R.string.sdcard);
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
			if(!files[i].getName().startsWith("."))
			{
				File file = files[i];
				items.add(file.getName());
				paths.add(file.getPath());
			}
		}
		
//		Collections.sort(items);
//		Collections.sort(paths);
		
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
	
	private void showNewFolderDialog(View parent) 
	{
		final EditText et_folderName;
		Button btn_confirm;
		Button btn_cancel;
		
		if (newFolderDialog == null) 
		{
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
			view = layoutInflater.inflate(R.layout.window_newfolder, null);

			newFolderDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			et_folderName = (EditText) view.findViewById(R.id.et_folderName);
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmNewFolder);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelNewFolder);
			
			Log.i(TAG, "et_folderName=" + et_folderName);
			et_folderName.setText(R.string.newfolder);
			
			//Ϊ������ť����OnTouchListener
			OnTouchListener mOnTouchListener = new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.color.halo_lightblue);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.color.white);
					}
					
					return false;
				}
			};
			
			btn_confirm.setOnTouchListener(mOnTouchListener);
			btn_cancel.setOnTouchListener(mOnTouchListener);
			
			btn_cancel.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					newFolderDialog.dismiss();
				}
			});
			
			btn_confirm.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					String newFolderPath = curPath + "/" +et_folderName.getText().toString();
					Log.i(TAG, "newFolderPath=" + newFolderPath);
					
					File newFolder = new File(newFolderPath);
					
					if(newFolder.exists())//�ļ����Ѿ�����ʱ
					{
						Toast.makeText(DownloadActivity.this, R.string.folder_exist, Toast.LENGTH_SHORT).show();
					}
					else
					{
						newFolder.mkdir();
						getFileDir(curPath);
						newFolderDialog.dismiss();
					}
				}
			});
		}

		// ʹ��ۼ�
		newFolderDialog.setFocusable(true);
		// ����������������ʧ
		newFolderDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		newFolderDialog.setBackgroundDrawable(new BitmapDrawable());
		
		newFolderDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
}

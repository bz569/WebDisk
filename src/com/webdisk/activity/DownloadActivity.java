package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.webdisk.R;
import com.webdisk.adapter.UploadFileListAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

public class DownloadActivity extends Activity
{
	
	private final static String TAG = "DownloadActivity";
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private TextView tv_showDownloadFile;
	private ListView lv_showFile;
	private Button btn_download;
	private Button btn_cancel;
	private Button btn_newFolder;
	
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/sdcard";
	private String curPath = "/sdcard"; // TODO �˴��������̻����ļ�·��
	
	private PopupWindow newFolderDialog;
	private View view;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
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
		 
		 // TODO textview��ʾ�����ļ��ļ�
		 
		 
		 
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
	
	private void showNewFolderDialog(View parent) 
	{
		EditText et_folderName;
		Button btn_confirm;
		Button btn_cancel;
		
		if (newFolderDialog == null) {
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
			
			// TODO ��Ӳ�����ť��OnclickListener,����½��ļ��в������˴�Ϊ��ϵͳĿ¼�½��ļ���
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

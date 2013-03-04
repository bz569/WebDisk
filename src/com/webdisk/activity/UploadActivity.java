package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.webdisk.R;
import com.webdisk.adapter.UploadFileListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UploadActivity extends Activity
{
	
	private final static String TAG = "UploadActivity";
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private TextView tv_showUploadPath;
	private ListView lv_showFile;
	private Button btn_upload;
	private Button btn_cancel;
	
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/sdcard";
	private String curPath = "/sdcard"; // TODO 此处设置网盘缓存文件路径
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		btn_naviationPrevious = (Button) findViewById(R.id.btn_upload_naviationPrevious);
		btn_upload = (Button) findViewById(R.id.btn_upload);
		btn_cancel = (Button) findViewById(R.id.btn_upload_cancel);
		tv_showFolderName = (TextView) findViewById(R.id.tv_upload_showFolderName);
		tv_showUploadPath = (TextView) findViewById(R.id.tv_showUploadFolderPath);
		lv_showFile = (ListView)findViewById(R.id.lv_upload_showFile);
		
		//为Button设置触摸效果
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
						//此处添加对文件的操作
					}
				}
			 	
			});
		 
		 //为返回按钮设置OnClickListener
		 btn_naviationPrevious.setOnClickListener(new Button.OnClickListener()
		 {
			@Override
			public void onClick(View v)
			{
				Log.i(TAG, "当前路径：" + curPath);
				File curFile = new File(curPath);
				curPath = curFile.getParent();
				Log.i(TAG, "上层路径：" + curPath);
				
				getFileDir(curPath);
			}
		 });
		 
		 // TODO textview显示上传文件目录
		 
		 
		 
	}
	
	private void getFileDir(String filePath)
	{
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		
		File f = new File(filePath);
		
		
		Log.i(TAG, "filePath=" + filePath + "&rootPath=" + rootPath);
		
		//当前目录为根目录时
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
		//设置实体返回按键动作
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			//当前为根目录时，返回
			if(curPath.equals(rootPath))
			{
				finish();
			}
			else//当前不为根目录时，返回上层
			{
				File curFile = new File(curPath);
				curPath = curFile.getParent();
				Log.i(TAG, "上层路径：" + curPath);
				
				getFileDir(curPath);
			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
		
		
	}
	
}

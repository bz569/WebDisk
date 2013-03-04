package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.webdisk.R;
import com.webdisk.adapter.ShowFileListAdapter;
import com.webdisk.adapter.OverflowMenuAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ShowFileActivity extends Activity
{
	
	private final static String TAG = "ShowFileActivity";
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private Button btn_newFile;
	private Button btn_overfolw;
	private ListView lv_showFile;
	
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/sdcard";
	private String curPath = "/sdcard"; // TODO 此处设置网盘缓存文件路径
	
	private PopupWindow overflowMenu;
	private PopupWindow newFolderDialog;
	private ListView lv_group;
	private View view;
	private List<String> groups;
	
	private long exitTime = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showfile);
		
		btn_naviationPrevious = (Button)findViewById(R.id.btn_naviationPrevious);
		btn_newFile = (Button)findViewById(R.id.btn_uploadFile);
		btn_overfolw = (Button)findViewById(R.id.btn_overfolw);
		tv_showFolderName = (TextView)findViewById(R.id.tv_showFolderName);
		lv_showFile = (ListView)findViewById(R.id.lv_showFile);
		
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
		 
		 btn_newFile.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.drawable.icon_content_new_touched);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.drawable.icon_content_new);
					}
					
					return false;
				}
			});
		 
		 btn_overfolw.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.drawable.icon_action_overflow_touched);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.drawable.icon_action_overflow);
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
		 //为overflow设置监听器
		 btn_overfolw.setOnClickListener(new View.OnClickListener() 
		 {
				@Override
				public void onClick(View v) {
					showOverflowMenu(v);
				}
		 });
		 //为upload按钮添加监听器
		 btn_newFile.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ShowFileActivity.this, UploadActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
		 
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
		
		lv_showFile.setAdapter(new ShowFileListAdapter(this, items, paths));
	}
	
	private void showOverflowMenu(final View parent) 
	{

		if (overflowMenu == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(R.layout.menu_overflowmenu, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// 加载数据
			groups = new ArrayList<String>();
			groups.add("刷新");
			groups.add("新建文件夹");

			OverflowMenuAdapter groupAdapter = new OverflowMenuAdapter(this, groups);
			lv_group.setAdapter(groupAdapter);
			// 创建一个PopuWidow对象
			overflowMenu = new PopupWindow(view, 200, LayoutParams.WRAP_CONTENT);
		}

		// 使其聚集
		overflowMenu.setFocusable(true);
		// 设置允许在外点击消失
		overflowMenu.setOutsideTouchable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		overflowMenu.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// 显示的位置为:屏幕的宽度的一半-overflowMenu的高度的一半
		int xPos = windowManager.getDefaultDisplay().getWidth() / 2
				- overflowMenu.getWidth() / 2;

		Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
				+ windowManager.getDefaultDisplay().getWidth() / 2);
		//
		Log.i("coder", "overflowMenu.getWidth()/2:" + overflowMenu.getWidth() / 2);

		Log.i("coder", "xPos:" + xPos);

		overflowMenu.showAsDropDown(parent, xPos, 0);

		lv_group.setOnItemClickListener(new OnItemClickListener() 
		{

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) 
			{

				Toast.makeText(ShowFileActivity.this,
						"groups.get(position)" + groups.get(position), Toast.LENGTH_SHORT)
						.show();
				
				// TODO 为overflow菜单中各项添加监听器
				if(groups.get(position).equals("新建文件夹"))
				{
					showNewFolderDialog(parent);
				}
				
				if (overflowMenu != null) 
				{
					overflowMenu.dismiss();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//设置实体返回按键动作
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			//当前为根目录时，连按返回键退出程序
			if(curPath.equals(rootPath))
			{
				if((System.currentTimeMillis() - exitTime) > 2000)
				{
					Toast.makeText(ShowFileActivity.this, R.string.confirm_quit, Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				}
				else
				{
					finish();
					System.exit(0);
				}
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
	
	private void showNewFolderDialog(View parent) 
	{
		EditText et_folderName;
		Button btn_confirm;
		Button btn_cancel;
		
		if (newFolderDialog == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
			view = layoutInflater.inflate(R.layout.window_newfolder, null);

			newFolderDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO 添加操作按钮的OnclickListener,添加新建文件夹操作
			et_folderName = (EditText) view.findViewById(R.id.et_folderName);
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmNewFolder);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelNewFolder);
			
			Log.i(TAG, "et_folderName=" + et_folderName);
			et_folderName.setText(R.string.newfolder);
			
			//为两个按钮设置OnTouchListener
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
			
		}

		// 使其聚集
		newFolderDialog.setFocusable(true);
		// 设置允许在外点击消失
		newFolderDialog.setOutsideTouchable(false);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		newFolderDialog.setBackgroundDrawable(new BitmapDrawable());
		
		newFolderDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

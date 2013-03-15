package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.adapter.PasteFileListAdapter;
import com.webdisk.adapter.ShowFileListAdapter;
import com.webdisk.application.SVNApplication;
import com.webdisk.service.CopyService;
import com.webdisk.util.CopyUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

public class PasteActivity extends Activity
{
	
	private final static String TAG = "ShowFileActivity";
	private final static String ROOT_URL = "http://10.109.34.24/wangpan/";
	
	private SVNApplication mApp;
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
//	private Button btn_newFolder;
	private ListView lv_showFile;
	private Button btn_paste;
	private Button btn_cancel;
	private TextView tv_showHint;
	private TextView tv_showFileName;
	
	private List<SVNDirEntry> mDirs;
	private List<List<SVNDirEntry>> mDirCache;
	private boolean mDirCacheInit = false;
	private String mCurDir = "";
	private SVNRevision mCurRevision = SVNRevision.HEAD;
	
	private PasteFileListAdapter mAdapter;
	
	private ProgressDialog mLoadingDialog;
	
	private String srcFilePath;
	private boolean isMove;
	private boolean isFolder;
	private String dstPath;
	private String fileName;
	
	private PopupWindow newFolderDialog;
	private View view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paste);
		
		mApp = (SVNApplication) getApplication();
		
		Intent intent = getIntent();
		srcFilePath = intent.getStringExtra("SRC_FILE_PATH");
		isMove = intent.getBooleanExtra("IS_MOVE", false);
		isFolder = intent.getBooleanExtra("IS_FOLDER", false);
		
		btn_naviationPrevious = (Button)findViewById(R.id.btn_paste_naviationPrevious);
//		btn_newFolder = (Button)findViewById(R.id.btn_paste_newfolder);
		btn_paste = (Button) findViewById(R.id.btn_paste);
		btn_cancel = (Button) findViewById(R.id.btn_paste_cancel);
		tv_showFolderName = (TextView)findViewById(R.id.tv_paste_showFolderName);
		lv_showFile = (ListView)findViewById(R.id.lv_paste_showFile);
		tv_showHint = (TextView)findViewById(R.id.tv_showCopyFileHint);
		tv_showFileName = (TextView)findViewById(R.id.tv_showCopyFileName);
		
		//设置复制和移动功能不同的显示
		if(isMove)
		{
			if(isFolder)
			{
				tv_showHint.setText(R.string.show_move_folder);
			}
			else
			{
				tv_showHint.setText(R.string.show_move_file);
			}
			btn_paste.setText(R.string.move);
		}
		else
		{
			tv_showHint.setText(R.string.show_copy_file);
			btn_paste.setText(R.string.copy);
		}
		//设置提示文件名
		fileName = srcFilePath.substring(srcFilePath.lastIndexOf("/")+1, srcFilePath.length());
		tv_showFileName.setText(fileName);
		
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
		 
//		 btn_newFolder.setOnTouchListener(new Button.OnTouchListener()
//			{
//				public boolean onTouch(View v, MotionEvent event)
//				{
//					if(event.getAction() == MotionEvent.ACTION_DOWN)
//					{
//						v.setBackgroundResource(R.drawable.icon_newfolder_touched);
//					}
//					else if(event.getAction() == MotionEvent.ACTION_UP)
//					{
//						v.setBackgroundResource(R.drawable.icon_newfolder);
//					}
//					
//					return false;
//				}
//			});
		 
		 btn_paste.setOnTouchListener(new Button.OnTouchListener()
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
		 
		 
		 
//		 getFileDir(rootPath);
		 mDirCache = new ArrayList<List<SVNDirEntry>>();
		 updateDataAndList();
		 
		 lv_showFile.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					SVNDirEntry entry = mDirs.get(position);
					if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0)
					{
						mCurDir = mCurDir + entry.getName() + "/";
						updateDataAndList();
					}
					else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0)
					{
						//  文件无操作
					}
				}
			 	
			});
		 
		 //为返回按钮设置OnClickListener
		 btn_naviationPrevious.setOnClickListener(new Button.OnClickListener()
		 {
			@Override
			public void onClick(View v)
			{
				
//				getFileDir(curPath);
				do
				{
					mCurDir = mCurDir.substring(0, mCurDir.length() - 1);
				}
				while (mCurDir.endsWith("/") == false && mCurDir.compareTo("") != 0);
			
				mDirs = mDirCache.remove(mDirCache.size() - 1);
				updateList();
			}
		 });
		 
//		 //为newfolder按钮设置OnclickListener
//		 btn_newFolder.setOnClickListener(new Button.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				showNewFolderDialog(v);
//			}
//		});
		 
		 //为cancle按钮设置onclickListener
		 btn_cancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
		 
		 //为paste按钮设置onclickListener
		 btn_paste.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dstPath = ROOT_URL + mApp.getCurrentConnection().getUsername() + "/" + mCurDir + fileName;
				Log.i(TAG, "Copy file( " + srcFilePath + ") to " + dstPath + ";isMove=" + isMove);
				boolean isExist = false;
				
				if(srcFilePath != null)
				{
					for(int i=0; i < mDirs.size(); i++)
					{
						if(mDirs.get(i).getKind().compareTo(SVNNodeKind.FILE) == 0)
						{
							if(mDirs.get(i).getName().equals(fileName))
							{
								isExist = true;
								break;
							}
						}
					}
					
					if(isExist)
					{
						Toast.makeText(PasteActivity.this, R.string.file_exist, Toast.LENGTH_SHORT).show();
					}
					else
					{
						if(!isMove)//复制
						{
							Intent intent = new Intent(PasteActivity.this, CopyService.class);
							intent.putExtra("SRC_FILE_PATH", srcFilePath);
							intent.putExtra("DST_PATH", dstPath);
							intent.putExtra("FILE_NAME", fileName);
							startService(intent);
						}
						else//移动
						{
							new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									mApp.doMove(srcFilePath, dstPath);
									
									//通过broadcast发送消息
									Intent intent = new Intent().setAction("com.webdisk.broadcast.REFRESH");
									intent.putExtra("MSG", "move_finish");
									sendBroadcast(intent);
									Log.i(TAG, "send msg by broadcast");
								}
							}).start();
						}
						
						finish();
					}
				}
				
//				if(!isMove)
//				{
//					Intent intent = new Intent(PasteActivity.this, CopyService.class);
//					intent.putExtra("SRC_FILE_PATH", srcFilePath);
//					intent.putExtra("DST_PATH", dstPath);
//					intent.putExtra("FILE_NAME", fileName);
//					startService(intent);
//				}
				
//				finish();
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
			if(mCurDir.compareTo("") == 0)
			{
				finish();
			}
			else//当前不为根目录时，返回上层
			{
				do
				{
					mCurDir = mCurDir.substring(0, mCurDir.length() - 1);
				}
				while (mCurDir.endsWith("/") == false && mCurDir.compareTo("") != 0);
			
				mDirs = mDirCache.remove(mDirCache.size() - 1);
				updateList();
			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
//	private void showNewFolderDialog(View parent) 
//	{
//		EditText et_folderName;
//		Button btn_confirm;
//		Button btn_cancel;
//		
//		if (newFolderDialog == null) {
//			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//					
//			view = layoutInflater.inflate(R.layout.window_newfolder, null);
//
//			newFolderDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//			
//			//  添加操作按钮的OnclickListener,添加新建文件夹操作
//			et_folderName = (EditText) view.findViewById(R.id.et_folderName);
//			btn_confirm = (Button) view.findViewById(R.id.btn_confirmNewFolder);
//			btn_cancel = (Button) view.findViewById(R.id.btn_cancelNewFolder);
//			
//			Log.i(TAG, "et_folderName=" + et_folderName);
//			et_folderName.setText(R.string.newfolder);
//			
//			//为两个按钮设置OnTouchListener
//			OnTouchListener mOnTouchListener = new Button.OnTouchListener()
//			{
//				public boolean onTouch(View v, MotionEvent event)
//				{
//					if(event.getAction() == MotionEvent.ACTION_DOWN)
//					{
//						v.setBackgroundResource(R.color.halo_lightblue);
//					}
//					else if(event.getAction() == MotionEvent.ACTION_UP)
//					{
//						v.setBackgroundResource(R.color.white);
//					}
//					
//					return false;
//				}
//			};
//			
//			btn_confirm.setOnTouchListener(mOnTouchListener);
//			btn_cancel.setOnTouchListener(mOnTouchListener);
//			
//		}
//
//		// 使其聚集
//		newFolderDialog.setFocusable(true);
//		// 设置允许在外点击消失
//		newFolderDialog.setOutsideTouchable(false);
//		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
//		newFolderDialog.setBackgroundDrawable(new BitmapDrawable());
//		
//		newFolderDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);
//
//	}
	
	//更新数据及ListView
		private void updateDataAndList() 
		{
			mLoadingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loading), true, false);
			
//			Thread thread = new Thread(this);
//			thread.start();
		
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					updateData();
					handler.sendEmptyMessage(0);					
				}
			}).start();
		}
		
		@SuppressWarnings("unchecked")
		private void updateData() 
		{
			if (mDirCacheInit)
				mDirCache.add(mDirs);
			else
				mDirCacheInit = true;
			
			mDirs = new ArrayList<SVNDirEntry>();
			
			try {
				Collection<SVNDirEntry> coll = mApp.getAllDirectories(mCurRevision, mCurDir);
				
				if (coll != null) {
					Iterator<SVNDirEntry> it = coll.iterator();
				
					if (it != null)
						while (it.hasNext())
						{
							SVNDirEntry tmpEntry = it.next();
							if(!tmpEntry.getName().startsWith("."))
							{
								mDirs.add(tmpEntry);
							}
						}
				
					Collections.sort(mDirs);
				}
				else {
					mDirs.add(new SVNDirEntry(null, null, "- " + getResources().getString(R.string.empty) + " -", SVNNodeKind.NONE, 0, false, 0, null, "", ""));
				}
			}
			catch(Exception e) 
			{
				// no ticket was selected go back to ticket screen
				// tell the user we are going to work
//	        	Toast toast=Toast.makeText(this, getString(R.string.no_connection_selected), Toast.LENGTH_SHORT);
//	    		toast.show();
	    		e.printStackTrace();
	    		this.finish();
			}
			
		}
		
		private void updateList() 
		{
			//设置目录名
			if (mCurDir.compareTo("") == 0)
			{
				tv_showFolderName.setText(R.string.mywebdisk);
				btn_naviationPrevious.setEnabled(false);
				btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item_disable);
			}
			else
			{
				String[] folders = mCurDir.split("/");
				String title = folders[folders.length-1];
				tv_showFolderName.setText(title);
				
				btn_naviationPrevious.setEnabled(true);
				btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item);
			}
			
			//设置ListView显示内容
			mAdapter = new PasteFileListAdapter(PasteActivity.this, mDirs);
			lv_showFile.setAdapter(mAdapter);
		}
		
//		public void run() 
//		{
//			updateData();
//			handler.sendEmptyMessage(0);
//		}
			
			
		
		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				mLoadingDialog.dismiss();
				updateList();
			}
		};
	
	
	
}

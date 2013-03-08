package com.webdisk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.adapter.ShowFileListAdapter;
import com.webdisk.adapter.OverflowMenuAdapter;
import com.webdisk.application.SVNApplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ShowFileActivity extends Activity implements Runnable
{
	
	private final static String TAG = "ShowFileActivity";
	
	private SVNApplication mApp;
	
	private List<SVNDirEntry> mDirs;
	private List<List<SVNDirEntry>> mDirCache;
	private boolean mDirCacheInit = false;
	private String mCurDir = "";
	private SVNRevision mCurRevision = SVNRevision.HEAD;
	
	private ShowFileListAdapter mAdapter;
	
	private Button btn_naviationPrevious;
	private TextView tv_showFolderName;
	private Button btn_newFile;
	private Button btn_overfolw;
	private ListView lv_showFile;
	
	private List<String> items = null;
	private List<String> paths = null;
	
	private ProgressDialog mLoadingDialog;
	
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
		
		mApp = (SVNApplication)getApplication();
		
		btn_naviationPrevious = (Button)findViewById(R.id.btn_naviationPrevious);
		btn_newFile = (Button)findViewById(R.id.btn_uploadFile);
		btn_overfolw = (Button)findViewById(R.id.btn_overfolw);
		tv_showFolderName = (TextView)findViewById(R.id.tv_showFolderName);
		lv_showFile = (ListView)findViewById(R.id.lv_showFile);

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
		 
//		 getFileDir(rootPath);
		 
		 mDirCache = new ArrayList<List<SVNDirEntry>>();
		 updateDataAndList();
		 
		 lv_showFile.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
//					File file = new File(paths.get(position));
//					if(file.isDirectory())
//					{
//						curPath = paths.get(position);
////						getFileDir(paths.get(position));
//						updateDataAndList();
//					}
//					else
//					{
//						//�˴���Ӷ��ļ��Ĳ���
//					}
					
					SVNDirEntry entry = mDirs.get(position);
					if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0)
					{
						mCurDir = mCurDir + entry.getName() + "/";
						updateDataAndList();
					}
					else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0)
					{
						// TODO �˴���Ӷ��ļ��Ĳ���
					}
				}
			 	
			});
		 
		 //Ϊ���ذ�ť����OnClickListener
		 btn_naviationPrevious.setOnClickListener(new Button.OnClickListener()
		 {
			@Override
			public void onClick(View v)
			{
//				Log.i(TAG, "��ǰ·����" + curPath);
//				File curFile = new File(curPath);
//				curPath = curFile.getParent();
//				Log.i(TAG, "�ϲ�·����" + curPath);
//				
//				getFileDir(curPath);
//				updateDataAndList();
				
				do
				{
					mCurDir = mCurDir.substring(0, mCurDir.length() - 1);
				}
				while (mCurDir.endsWith("/") == false && mCurDir.compareTo("") != 0);
			
				mDirs = mDirCache.remove(mDirCache.size() - 1);
				updateList();
			}
		 });
		 //Ϊoverflow���ü�����
		 btn_overfolw.setOnClickListener(new View.OnClickListener() 
		 {
				@Override
				public void onClick(View v) {
					showOverflowMenu(v);
				}
		 });
		 //Ϊupload��ť��Ӽ�����
		 btn_newFile.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//���е�ǰĿ¼�е���Ŀ,������һ��Activity
				ArrayList<String> itemNameList = new ArrayList<String>();
				SVNDirEntry tmpEntry = null;
				for(int i=0; i < mDirs.size(); i++)
				{
					tmpEntry = mDirs.get(i);
					if(tmpEntry.getKind().compareTo(SVNNodeKind.FILE) == 0)
					{
						itemNameList.add(tmpEntry.getName());
					}
				}
				
				
				Intent intent = new Intent(ShowFileActivity.this, UploadActivity.class);
				intent.putExtra("UPLOAD_DST_PATH", mCurDir);
				intent.putStringArrayListExtra("SVN_DIR_FILES", itemNameList);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
		 
	}
	
//	private void getFileDir(String filePath)
//	{
//		items = new ArrayList<String>();
//		paths = new ArrayList<String>();
//		
//		File f = new File(filePath);
//		
//		
//		Log.i(TAG, "filePath=" + filePath + "&rootPath=" + rootPath);
//		
//		//��ǰĿ¼Ϊ��Ŀ¼ʱ
//		if(filePath.equals(rootPath))
//		{
//			Log.i(TAG, "filePath == rootPath");
//			tv_showFolderName.setText(R.string.mywebdisk);
//			btn_naviationPrevious.setEnabled(false);
//			btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item_disable);
//		}
//		else
//		{
//			tv_showFolderName.setText(f.getName());
//			btn_naviationPrevious.setEnabled(true);
//			btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item);
//		}
//		
//		File[] files = f.listFiles();
//		
//		for(int i = 0; i < files.length; i++)
//		{
//			File file = files[i];
//			items.add(file.getName());
//			paths.add(file.getPath());
//		}
//		
//		mAdapter = new ShowFileListAdapter(ShowFileActivity.this, mDirs);
//		lv_showFile.setAdapter(mAdapter);
//	}
	
	private void showOverflowMenu(final View parent) 
	{

		if (overflowMenu == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(R.layout.menu_overflowmenu, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// ��������
			groups = new ArrayList<String>();
			groups.add("ˢ��");
			groups.add("�½��ļ���");

			OverflowMenuAdapter groupAdapter = new OverflowMenuAdapter(this, groups);
			lv_group.setAdapter(groupAdapter);
			// ����һ��PopuWidow����
			overflowMenu = new PopupWindow(view, 200, LayoutParams.WRAP_CONTENT);
		}

		// ʹ��ۼ�
		overflowMenu.setFocusable(true);
		// ����������������ʧ
		overflowMenu.setOutsideTouchable(true);

		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		overflowMenu.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// ��ʾ��λ��Ϊ:��Ļ�Ŀ�ȵ�һ��-overflowMenu�ĸ߶ȵ�һ��
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

				
				// TODO Ϊoverflow�˵��и�����Ӽ�����
				if(groups.get(position).equals("�½��ļ���"))
				{
					showNewFolderDialog(parent);
				}
				else if(groups.get(position).equals("ˢ��"))
				{
					refreshDataAndList();
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
		//����ʵ�巵�ذ�������
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
//			//��ǰΪ��Ŀ¼ʱ���������ؼ��˳�����
//			if(curPath.equals(rootPath))
//			{
//				if((System.currentTimeMillis() - exitTime) > 2000)
//				{
//					Toast.makeText(ShowFileActivity.this, R.string.confirm_quit, Toast.LENGTH_SHORT).show();
//					exitTime = System.currentTimeMillis();
//				}
//				else
//				{
//					finish();
//					System.exit(0);
//				}
//			}
//			else//��ǰ��Ϊ��Ŀ¼ʱ�������ϲ�
//			{
//				File curFile = new File(curPath);
//				curPath = curFile.getParent();
//				Log.i(TAG, "�ϲ�·����" + curPath);
//				
////				getFileDir(curPath);
//				updateDataAndList();
//			}
			
			if (mCurDir.compareTo("") == 0)
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
			else
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
	
	private void showNewFolderDialog(View parent) 
	{
		EditText et_folderName;
		Button btn_confirm;
		Button btn_cancel;
		
		if (newFolderDialog == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
			view = layoutInflater.inflate(R.layout.window_newfolder, null);

			newFolderDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO ��Ӳ�����ť��OnclickListener,����½��ļ��в���
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
			
		}

		// ʹ��ۼ�
		newFolderDialog.setFocusable(true);
		// ����������������ʧ
		newFolderDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		newFolderDialog.setBackgroundDrawable(new BitmapDrawable());
		
		newFolderDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	
	//�������ݼ�ListView
	private void updateDataAndList() 
	{
		mLoadingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loading), true, false);
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	//ˢ��
	private void refreshDataAndList()
	{
		mLoadingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loading), true, false);
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Log.i(TAG, "��ʼˢ��");
				refreshData();
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
						mDirs.add(it.next());
			
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
        	Toast toast=Toast.makeText(this, getString(R.string.no_connection_selected), Toast.LENGTH_SHORT);
    		toast.show();
    		e.printStackTrace();
    		this.finish();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void refreshData() 
	{
		mDirs = new ArrayList<SVNDirEntry>();
		
		try {
			Collection<SVNDirEntry> coll = mApp.getAllDirectories(mCurRevision, mCurDir);
			
			if (coll != null) {
				Iterator<SVNDirEntry> it = coll.iterator();
			
				if (it != null)
					while (it.hasNext())
						mDirs.add(it.next());
			
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
        	Toast toast=Toast.makeText(this, getString(R.string.no_connection_selected), Toast.LENGTH_SHORT);
    		toast.show();
    		e.printStackTrace();
    		this.finish();
		}
	}
	
	private void updateList() 
	{
		//����Ŀ¼��
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
		
		//����ListView��ʾ����
		mAdapter = new ShowFileListAdapter(ShowFileActivity.this, mDirs);
		lv_showFile.setAdapter(mAdapter);
	}
	
	public void run() 
	{
		updateData();
		handler.sendEmptyMessage(0);
	}
		
		
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			mLoadingDialog.dismiss();
			updateList();
			
		}
	};
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

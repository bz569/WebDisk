package com.webdisk.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.adapter.ShowFileListAdapter;
import com.webdisk.adapter.OverflowMenuAdapter;
import com.webdisk.application.SVNApplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	private static final String mPREFERENCES = "userInfo";
	private final static String WEBDISK_ROOT_URL = "http://10.109.34.24/wangpan/";

	private final static int DELETE_MSG = 11;
	private final static int DELETE_SUCCESS = 111;
	private final static int DELETE_ERROR = 110;
	private final static int RENAME_MSG = 12;
	private final static int RENAME_SUCCESS = 121;
	private final static int RENAME_ERROR = 120;
	private final static int NEW_FOLDER_MSG = 14;
	private final static int NEW_FOLDER_SUCCESS = 141;
	private final static int NEW_FOLDER_ERROR = 140;

	private SVNApplication mApp;
	private SharedPreferences sharedPreferences;

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

	private ProgressDialog mLoadingDialog;

	private PopupWindow overflowMenu;
	private PopupWindow newFolderDialog;
	private ListView lv_group;
	private View view;
	private List<String> groups;

	private long exitTime = 0;

	public Handler showFileHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case DELETE_MSG:
			{
				if (msg.arg1 == DELETE_SUCCESS)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.delete_success, Toast.LENGTH_SHORT).show();
				} else if (msg.arg1 == DELETE_ERROR)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.delete_error, Toast.LENGTH_SHORT).show();
				}

				refreshDataAndList();
				break;
			}
			case RENAME_MSG:
			{
				if (msg.arg1 == RENAME_SUCCESS)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.rename_success, Toast.LENGTH_SHORT).show();
				} else if (msg.arg1 == RENAME_ERROR)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.rename_error, Toast.LENGTH_SHORT).show();
				}

				refreshDataAndList();
				break;
			}
			case NEW_FOLDER_MSG:
			{
				if(msg.arg1 == NEW_FOLDER_SUCCESS)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.new_folder_success, Toast.LENGTH_SHORT).show();
					refreshDataAndList();
				}
				else if(msg.arg1 == NEW_FOLDER_ERROR)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.new_folder_error, Toast.LENGTH_SHORT).show();
				}
			}
			
			
			}

			super.handleMessage(msg);
		}

	};

	// 设置broadcat receiver接收广播消息
	BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals("com.webdisk.broadcast.REFRESH"))// 收到上传完成的广播
			{
				if (intent.getStringExtra("MSG") != null
						&& intent.getStringExtra("MSG").equals("move_finish"))
				{
					Toast.makeText(ShowFileActivity.this, R.string.move_finish,
							Toast.LENGTH_SHORT).show();
				}

				refreshDataAndList();
			}
			// if(intent.getAction().equals("com.webdisk.broadcast.MOVE_FINISH"))
			// {
			// Toast.makeText(ShowFileActivity.this, R.string.move_finish,
			// Toast.LENGTH_SHORT).show();
			// refreshDataAndList();
			// }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showfile);

		// IntentFilter intentFilter = new
		// IntentFilter("com.webdisk.broadcast.UPLOAD_FINISH");
		// registerReceiver(mReceiver, intentFilter);

		mApp = (SVNApplication) getApplication();
		sharedPreferences = this.getSharedPreferences(mPREFERENCES,
				Context.MODE_WORLD_READABLE);

		btn_naviationPrevious = (Button) findViewById(R.id.btn_naviationPrevious);
		btn_newFile = (Button) findViewById(R.id.btn_uploadFile);
		btn_overfolw = (Button) findViewById(R.id.btn_overfolw);
		tv_showFolderName = (TextView) findViewById(R.id.tv_showFolderName);
		lv_showFile = (ListView) findViewById(R.id.lv_showFile);

		// 为Button设置触摸效果
		btn_naviationPrevious.setOnTouchListener(new Button.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundResource(R.drawable.icon_navigation_previous_item_touched);
				} else if (event.getAction() == MotionEvent.ACTION_UP)
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
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundResource(R.drawable.icon_content_new_touched);
				} else if (event.getAction() == MotionEvent.ACTION_UP)
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
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundResource(R.drawable.icon_action_overflow_touched);
				} else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					v.setBackgroundResource(R.drawable.icon_action_overflow);
				}

				return false;
			}
		});

		// getFileDir(rootPath);

		mDirCache = new ArrayList<List<SVNDirEntry>>();
		updateDataAndList();

		lv_showFile.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// File file = new File(paths.get(position));
				// if(file.isDirectory())
				// {
				// curPath = paths.get(position);
				// // getFileDir(paths.get(position));
				// updateDataAndList();
				// }
				// else
				// {
				// //此处添加对文件的操作
				// }

				SVNDirEntry entry = mDirs.get(position);
				if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0)
				{
					mCurDir = mCurDir + entry.getName() + "/";
					updateDataAndList();
				} else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0)
				{
					// 此处添加对文件的操作如果是文件，弹出下载界面
					SVNDirEntry entry1 = mDirs.get(position);
					String filePath = entry1.getURL().toDecodedString();
					Log.i(TAG, "filePath=" + filePath);
					view.setBackgroundDrawable(null);
					Intent intent = new Intent(ShowFileActivity.this, DownloadActivity.class);
					intent.putExtra("FILE_PATH", filePath);
					startActivity(intent);
					
				}
			}

		});

		// 为返回按钮设置OnClickListener
		btn_naviationPrevious.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Log.i(TAG, "当前路径：" + curPath);
				// File curFile = new File(curPath);
				// curPath = curFile.getParent();
				// Log.i(TAG, "上层路径：" + curPath);
				//
				// getFileDir(curPath);
				// updateDataAndList();

				do
				{
					mCurDir = mCurDir.substring(0, mCurDir.length() - 1);
				} while (mCurDir.endsWith("/") == false
						&& mCurDir.compareTo("") != 0);

				mDirs = mDirCache.remove(mDirCache.size() - 1);
				updateList();
			}
		});
		// 为overflow设置监听器
		btn_overfolw.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showOverflowMenu(v);
			}
		});
		// 为upload按钮添加监听器
		btn_newFile.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// 罗列当前目录中的项目,传入下一个Activity
				ArrayList<String> itemNameList = new ArrayList<String>();
				SVNDirEntry tmpEntry = null;
				for (int i = 0; i < mDirs.size(); i++)
				{
					tmpEntry = mDirs.get(i);
					if (tmpEntry.getKind().compareTo(SVNNodeKind.FILE) == 0)
					{
						itemNameList.add(tmpEntry.getName());
					}
				}

				Intent intent = new Intent(ShowFileActivity.this,
						UploadActivity.class);
				intent.putExtra("UPLOAD_DST_PATH", mCurDir);
				intent.putStringArrayListExtra("SVN_DIR_FILES", itemNameList);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
			}
		});

	}

	// private void getFileDir(String filePath)
	// {
	// items = new ArrayList<String>();
	// paths = new ArrayList<String>();
	//
	// File f = new File(filePath);
	//
	//
	// Log.i(TAG, "filePath=" + filePath + "&rootPath=" + rootPath);
	//
	// //当前目录为根目录时
	// if(filePath.equals(rootPath))
	// {
	// Log.i(TAG, "filePath == rootPath");
	// tv_showFolderName.setText(R.string.mywebdisk);
	// btn_naviationPrevious.setEnabled(false);
	// btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item_disable);
	// }
	// else
	// {
	// tv_showFolderName.setText(f.getName());
	// btn_naviationPrevious.setEnabled(true);
	// btn_naviationPrevious.setBackgroundResource(R.drawable.icon_navigation_previous_item);
	// }
	//
	// File[] files = f.listFiles();
	//
	// for(int i = 0; i < files.length; i++)
	// {
	// File file = files[i];
	// items.add(file.getName());
	// paths.add(file.getPath());
	// }
	//
	// mAdapter = new ShowFileListAdapter(ShowFileActivity.this, mDirs);
	// lv_showFile.setAdapter(mAdapter);
	// }

	private void showOverflowMenu(final View parent)
	{

		if (overflowMenu == null)
		{
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(R.layout.menu_overflowmenu, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// 加载数据
			groups = new ArrayList<String>();
			groups.add("刷新");
			groups.add("新建文件夹");
			groups.add("注销");
			groups.add("退出");

			OverflowMenuAdapter groupAdapter = new OverflowMenuAdapter(this,
					groups);
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
		Log.i("coder", "overflowMenu.getWidth()/2:" + overflowMenu.getWidth()
				/ 2);

		Log.i("coder", "xPos:" + xPos);

		overflowMenu.showAsDropDown(parent, xPos, 0);

		lv_group.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id)
			{

				//  为overflow菜单中各项添加监听器
				if (groups.get(position).equals("新建文件夹"))
				{
					showNewFolderDialog(parent);
				} else if (groups.get(position).equals("刷新"))
				{
					refreshDataAndList();
				} else if (groups.get(position).equals("注销"))
				{
					// TODO 注销时删除cache文件
					// 取消sharedpreferences中自动登录的设置
					Editor editor = sharedPreferences.edit();
					editor.putBoolean("AUTO_LOGIN_ISCHECK", false).commit();
					// 跳转到登陆界面
					Intent intent = new Intent(ShowFileActivity.this,
							LoginActivity.class);
					startActivity(intent);
					finish();
				}else if (groups.get(position).equals("退出"))
				{
					finish();
					System.exit(0);
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
		// 设置实体返回按键动作
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN)
		{
			// //当前为根目录时，连按返回键退出程序
			// if(curPath.equals(rootPath))
			// {
			// if((System.currentTimeMillis() - exitTime) > 2000)
			// {
			// Toast.makeText(ShowFileActivity.this, R.string.confirm_quit,
			// Toast.LENGTH_SHORT).show();
			// exitTime = System.currentTimeMillis();
			// }
			// else
			// {
			// finish();
			// System.exit(0);
			// }
			// }
			// else//当前不为根目录时，返回上层
			// {
			// File curFile = new File(curPath);
			// curPath = curFile.getParent();
			// Log.i(TAG, "上层路径：" + curPath);
			//
			// // getFileDir(curPath);
			// updateDataAndList();
			// }

			if (mCurDir.compareTo("") == 0)
			{
				if ((System.currentTimeMillis() - exitTime) > 2000)
				{
					Toast.makeText(ShowFileActivity.this,
							R.string.confirm_quit, Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else
				{
					finish();
					System.exit(0);
				}
			} else
			{
				do
				{
					mCurDir = mCurDir.substring(0, mCurDir.length() - 1);
				} while (mCurDir.endsWith("/") == false
						&& mCurDir.compareTo("") != 0);

				mDirs = mDirCache.remove(mDirCache.size() - 1);
				updateList();
			}

			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_DOWN)
		{
			showOverflowMenu(btn_overfolw);
		}
		return super.onKeyDown(keyCode, event);

	}

	private void showNewFolderDialog(View parent)
	{
		final EditText et_folderName;
		Button btn_confirm;
		Button btn_cancel;

		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		view = layoutInflater.inflate(R.layout.window_newfolder, null);

		newFolderDialog = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		//  添加操作按钮的OnclickListener,添加新建文件夹操作
		et_folderName = (EditText) view.findViewById(R.id.et_folderName);
		btn_confirm = (Button) view.findViewById(R.id.btn_confirmNewFolder);
		btn_cancel = (Button) view.findViewById(R.id.btn_cancelNewFolder);

		Log.i(TAG, "et_folderName=" + et_folderName);
		et_folderName.setText(R.string.newfolder);

		// 为两个按钮设置OnTouchListener
		OnTouchListener mOnTouchListener = new Button.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundResource(R.color.halo_lightblue);
				} else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					v.setBackgroundResource(R.color.white);
				}

				return false;
			}
		};
		
		//为确定按钮设置onclickListener
		btn_confirm.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String newFolderName = et_folderName.getText().toString();
				final String dstPath = WEBDISK_ROOT_URL + mApp.getCurrentConnection().getUsername() 
								+ "/" + mCurDir + newFolderName;
				Log.i(TAG, "new folder at " + dstPath);
				
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						Message newFolderMsg = new Message();
						newFolderMsg.what = NEW_FOLDER_MSG;
						
						if(mApp.doMkDir(dstPath))
						{
							newFolderMsg.arg1 = NEW_FOLDER_SUCCESS;
						}
						else
						{
							newFolderMsg.arg1 = NEW_FOLDER_ERROR;
						}
						
						showFileHandler.sendMessage(newFolderMsg);
					}
				}).start();
				
				newFolderDialog.dismiss();
			}
		});
		
		btn_cancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				newFolderDialog.dismiss();
			}
		});

		btn_confirm.setOnTouchListener(mOnTouchListener);
		btn_cancel.setOnTouchListener(mOnTouchListener);

		// 使其聚集
		newFolderDialog.setFocusable(true);
		// 设置允许在外点击消失
		newFolderDialog.setOutsideTouchable(false);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		newFolderDialog.setBackgroundDrawable(new BitmapDrawable());

		newFolderDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}

	// 更新数据及ListView
	private void updateDataAndList()
	{
		mLoadingDialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.loading), true, false);

		Thread thread = new Thread(this);
		thread.start();
	}

	// 刷新
	private void refreshDataAndList()
	{
		mLoadingDialog = ProgressDialog.show(this, "", getResources()
				.getString(R.string.loading), true, false);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Log.i(TAG, "开始刷新");
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

		try
		{
			Collection<SVNDirEntry> coll = mApp.getAllDirectories(mCurRevision,
					mCurDir);

			if (coll != null)
			{
				Iterator<SVNDirEntry> it = coll.iterator();

				if (it != null)
					while (it.hasNext())
					{
						SVNDirEntry tmpEntry = it.next();
						if (!tmpEntry.getName().startsWith("."))
						{
							mDirs.add(tmpEntry);
						}
					}

				Collections.sort(mDirs);
			} else
			{
				mDirs.add(new SVNDirEntry(null, null, "- "
						+ getResources().getString(R.string.empty) + " -",
						SVNNodeKind.NONE, 0, false, 0, null, "", ""));
			}
		} catch (Exception e)
		{
			// no ticket was selected go back to ticket screen
			// tell the user we are going to work
			// Toast toast=Toast.makeText(this,
			// getString(R.string.no_connection_selected), Toast.LENGTH_SHORT);
			// toast.show();
			e.printStackTrace();
			this.finish();
		}

	}

	@SuppressWarnings("unchecked")
	private void refreshData()
	{
		mDirs = new ArrayList<SVNDirEntry>();

		try
		{
			Collection<SVNDirEntry> coll = mApp.getAllDirectories(mCurRevision,
					mCurDir);

			if (coll != null)
			{
				Iterator<SVNDirEntry> it = coll.iterator();

				if (it != null)
					while (it.hasNext())
					{
						SVNDirEntry tmpEntry = it.next();
						if (!tmpEntry.getName().startsWith("."))
						{
							mDirs.add(tmpEntry);
						}
					}

				Collections.sort(mDirs);
			} else
			{
				mDirs.add(new SVNDirEntry(null, null, "- "
						+ getResources().getString(R.string.empty) + " -",
						SVNNodeKind.NONE, 0, false, 0, null, "", ""));
			}
		} catch (Exception e)
		{
			// no ticket was selected go back to ticket screen
			// tell the user we are going to work
			// Toast toast=Toast.makeText(this,
			// getString(R.string.no_connection_selected), Toast.LENGTH_SHORT);
			// toast.show();
			e.printStackTrace();
			this.finish();
		}
	}

	private void updateList()
	{
		// 设置目录名
		if (mCurDir.compareTo("") == 0)
		{
			tv_showFolderName.setText(R.string.mywebdisk);
			btn_naviationPrevious.setEnabled(false);
			btn_naviationPrevious
					.setBackgroundResource(R.drawable.icon_navigation_previous_item_disable);
		} else
		{
			String[] folders = mCurDir.split("/");
			String title = folders[folders.length - 1];
			tv_showFolderName.setText(title);

			btn_naviationPrevious.setEnabled(true);
			btn_naviationPrevious
					.setBackgroundResource(R.drawable.icon_navigation_previous_item);
		}

		// 设置ListView显示内容
		mAdapter = new ShowFileListAdapter(ShowFileActivity.this,
				showFileHandler, mDirs);
		lv_showFile.setAdapter(mAdapter);
	}

	public void run()
	{
		updateData();
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			mLoadingDialog.dismiss();
			updateList();

		}
	};

	@Override
	protected void onStart()
	{
		super.onStart();
		// 注册receiver
		IntentFilter intentFilter = new IntentFilter(
				"com.webdisk.broadcast.REFRESH");
		registerReceiver(mReceiver, intentFilter);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		// 注销receiver
		unregisterReceiver(mReceiver);
	}

}

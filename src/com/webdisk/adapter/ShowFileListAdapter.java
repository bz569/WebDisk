package com.webdisk.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.webdisk.R;
import com.webdisk.activity.DownloadActivity;
import com.webdisk.activity.PasteActivity;
import com.webdisk.activity.ShowFileActivity;

public class ShowFileListAdapter extends BaseAdapter
{
	private static final String TAG = "ShowFileListAdapter";
	
	private LayoutInflater mInflater;
	private Bitmap icon_file;
	private Bitmap icon_folder;
	private List<SVNDirEntry> mdir;
	private Context context;
	
	private PopupWindow actionMenu;
	private PopupWindow renameDialog;
	private PopupWindow deleteDialog;
	private PopupWindow showFileInfoDialg;
	
	private View view;
	
	private Button btn_copy;
	private Button btn_move;
	private Button btn_rename;
	private Button btn_showFileInfo;
	private Button btn_delete;
	private Button btn_download;
	
	public ShowFileListAdapter(Context context, List<SVNDirEntry> dirnames)
	{
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.mdir = dirnames;
		
		icon_file = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file);
		icon_folder = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_folder);
	}
	
	
	@Override
	public int getCount()
	{
		return mdir.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mdir.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.row_showfile, null);
			holder = new ViewHolder();
			holder.btn_fileAction = (Button)convertView.findViewById(R.id.btn_row_expandFileAction);
			holder.iv_showFileImage = (ImageView)convertView.findViewById(R.id.iv_row_showFileImage);
			holder.tv_fileInfo = (TextView)convertView.findViewById(R.id.tv_row_showFileInfo);
			holder.tv_fileName = (TextView)convertView.findViewById(R.id.tv_row_showFileName);
			
			holder.btn_fileAction.setOnTouchListener(new Button.OnTouchListener()
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundResource(R.drawable.icon_navigation_expand_touched);
					}
					else if(event.getAction() == MotionEvent.ACTION_UP)
					{
						v.setBackgroundResource(R.drawable.icon_navigation_expand);
					}
					
					return false;
				}
			});
		
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		
//		File f = new File(paths.get(position).toString());
//		
//		//为每一行设置内容
//		holder.tv_fileName.setText(f.getName());
//		if(f.isDirectory())
//		{
//			holder.iv_showFileImage.setImageBitmap(icon_folder);
//		}
//		else
//		{
//			holder.iv_showFileImage.setImageBitmap(icon_file);
//		}
		
		SVNDirEntry entry = mdir.get(position);
		
		holder.tv_fileName.setText(entry.getName());
		
		if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0) //当前为文件夹时
		{
			holder.iv_showFileImage.setImageBitmap(icon_folder);
		}
		else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0) //当前为文件时
		{
			holder.iv_showFileImage.setImageBitmap(icon_file);
		}
		
		// TODO 为btn_fileAction设置弹出菜单
		holder.btn_fileAction.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showActionMenu(v, position);
			}
		});
		
		return convertView;
	}
	
	private class ViewHolder
	  {
		ImageView iv_showFileImage;
		TextView tv_fileName;
		TextView tv_fileInfo;
		Button btn_fileAction;
	  }
	
	private void showActionMenu(final View parent, final int position) 
	{

		if (actionMenu == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(context);
					
			view = layoutInflater.inflate(R.layout.menu_fileaction, null);

			actionMenu = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO 初始化各按钮
			btn_copy = (Button) view.findViewById(R.id.btn_copy);
			btn_move = (Button) view.findViewById(R.id.btn_move);
			btn_rename = (Button) view.findViewById(R.id.btn_rename);
			btn_showFileInfo = (Button) view.findViewById(R.id.btn_fileInfo);
			btn_delete = (Button) view.findViewById(R.id.btn_delete);
			btn_download = (Button) view.findViewById(R.id.btn_download);
		}
		
		// 使其聚集
		actionMenu.setFocusable(true);
		// 设置允许在外点击消失
		actionMenu.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		actionMenu.setBackgroundDrawable(new BitmapDrawable());

		// TODO 下面哪种菜单出现方式好
//		actionMenu.showAsDropDown(parent, 0, 0);
		actionMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
		
		// TODO 添加操作按钮的OnclickListener
		//复制
		btn_copy.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, PasteActivity.class);
				context.startActivity(intent);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
				
			}
		});
		//移动
		btn_move.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, PasteActivity.class);
				context.startActivity(intent);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
			}
		});
		//重命名
		btn_rename.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showReNameDialog(parent);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
				
			}
		});
		//显示文件详情
		btn_showFileInfo.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showFileInfoDialog(parent);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
			}
		});
		//删除
		btn_delete.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showDeleteDialog(parent);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
			}
		});
		//下载
		btn_download.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
				
				SVNDirEntry entry = mdir.get(position);
				String filePath = entry.getURL().toString();
//				Log.i(TAG, "filePath=" + filePath);
				
				Intent intent = new Intent(context, DownloadActivity.class);
				intent.putExtra("FILE_PATH", filePath);
				context.startActivity(intent);
			}
		});
		//如果目标为文件夹，禁止下载按钮
		SVNDirEntry targetEntry = mdir.get(position);
		if(targetEntry.getKind().compareTo(SVNNodeKind.DIR) == 0)
		{
			btn_download.setEnabled(false);
		}
		else if(targetEntry.getKind().compareTo(SVNNodeKind.FILE) == 0)
		{
			btn_download.setEnabled(true);
		}
		
	}
	
	private void showReNameDialog(View parent) 
	{
		EditText et_NewName;
		Button btn_confirm;
		Button btn_cancel;
		
		if (renameDialog == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(context);
					
			view = layoutInflater.inflate(R.layout.window_rename, null);

			renameDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO 添加操作按钮的OnclickListener,添加新建文件夹操作
			et_NewName = (EditText) view.findViewById(R.id.et_newFileName);
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmRename);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelRename);
			
			
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
		renameDialog.setFocusable(true);
		// 设置允许在外点击消失
		renameDialog.setOutsideTouchable(false);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		renameDialog.setBackgroundDrawable(new BitmapDrawable());
		
		renameDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	private void showFileInfoDialog(View parent) 
	{
		TextView tv_showFileInfo = null;
		Button btn_close = null;
		
		if (showFileInfoDialg == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(context);
					
			view = layoutInflater.inflate(R.layout.windows_showfileinfo, null);

			showFileInfoDialg = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO 添加操作按钮的OnclickListener,添加新建文件夹操作
			tv_showFileInfo = (TextView) view.findViewById(R.id.tv_showFileInfo);
			btn_close = (Button) view.findViewById(R.id.btn_closeFileInfo);
			
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
			
			btn_close.setOnTouchListener(mOnTouchListener);
			
			// TODO 在此处设置用TextView显示文件信息(一定要在此处设置？)
			tv_showFileInfo.setText("此处显示文件详细信息");
			
		}

		
		// 使其聚集
		showFileInfoDialg.setFocusable(true);
		// 设置允许在外点击消失
		showFileInfoDialg.setOutsideTouchable(false);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		showFileInfoDialg.setBackgroundDrawable(new BitmapDrawable());
		
		showFileInfoDialg.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	private void showDeleteDialog(View parent) 
	{
		TextView tv_showInfo;
		Button btn_confirm;
		Button btn_cancel;
		
		if (deleteDialog == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(context);
					
			view = layoutInflater.inflate(R.layout.window_delete, null);

			deleteDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO 添加操作按钮的OnclickListener,添加新建文件夹操作
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmDelete);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelDelete);
			tv_showInfo = (TextView) view.findViewById(R.id.tv_confirmDelete);
			
			
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
			
			// TODO 在此处设置删除对话框提示内容（file/folder）
			tv_showInfo.setText(R.string.delete_file);
			
		}

		// 使其聚集
		deleteDialog.setFocusable(true);
		// 设置允许在外点击消失
		deleteDialog.setOutsideTouchable(false);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		deleteDialog.setBackgroundDrawable(new BitmapDrawable());
		
		deleteDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
}

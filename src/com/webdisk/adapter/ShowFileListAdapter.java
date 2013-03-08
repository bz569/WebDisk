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
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter.LengthFilter;
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
import com.webdisk.application.SVNApplication;
import com.webdisk.util.DeleteUtil;

public class ShowFileListAdapter extends BaseAdapter
{
	private static final String TAG = "ShowFileListAdapter";
	
	private final static int DELETE_MSG = 11;
	private final static int DELETE_SUCCESS = 111;
	private final static int DELETE_ERROR = 110;
	
	private LayoutInflater mInflater;
	private Bitmap icon_file;
	private Bitmap icon_folder;
	private List<SVNDirEntry> mdir;
	private Context context;
	private Handler showFileHandler;
	
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
	
	
	public ShowFileListAdapter(Context context, Handler showFileHandler, List<SVNDirEntry> dirnames)
	{
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.showFileHandler = showFileHandler;
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
//		//Ϊÿһ����������
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
		
		if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0) //��ǰΪ�ļ���ʱ
		{
			holder.iv_showFileImage.setImageBitmap(icon_folder);
		}
		else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0) //��ǰΪ�ļ�ʱ
		{
			holder.iv_showFileImage.setImageBitmap(icon_file);
		}
		
		// TODO Ϊbtn_fileAction���õ����˵�
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
			
			// TODO ��ʼ������ť
			btn_copy = (Button) view.findViewById(R.id.btn_copy);
			btn_move = (Button) view.findViewById(R.id.btn_move);
			btn_rename = (Button) view.findViewById(R.id.btn_rename);
			btn_showFileInfo = (Button) view.findViewById(R.id.btn_fileInfo);
			btn_delete = (Button) view.findViewById(R.id.btn_delete);
			btn_download = (Button) view.findViewById(R.id.btn_download);
		}
		
		// ʹ��ۼ�
		actionMenu.setFocusable(true);
		// ����������������ʧ
		actionMenu.setOutsideTouchable(true);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		actionMenu.setBackgroundDrawable(new BitmapDrawable());

		// TODO �������ֲ˵����ַ�ʽ��
//		actionMenu.showAsDropDown(parent, 0, 0);
		actionMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);
		
		// TODO ��Ӳ�����ť��OnclickListener
		//����
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
		//�ƶ�
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
		//������
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
		//��ʾ�ļ�����
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
		//ɾ��
		btn_delete.setOnClickListener(new Button.OnClickListener()
		{
			SVNDirEntry entry = mdir.get(position);
			String filePath = entry.getURL().toString();
			
			@Override
			public void onClick(View v)
			{
				showDeleteDialog(parent, filePath);
				
				if (actionMenu != null) 
				{
					actionMenu.dismiss();
				}
			}
		});
		
		//����
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
		//���Ŀ��Ϊ�ļ��У���ֹ���ء�ɾ����ť
		SVNDirEntry targetEntry = mdir.get(position);
		if(targetEntry.getKind().compareTo(SVNNodeKind.DIR) == 0)
		{
			btn_download.setEnabled(false);
			btn_delete.setEnabled(false);
		}
		else if(targetEntry.getKind().compareTo(SVNNodeKind.FILE) == 0)
		{
			btn_download.setEnabled(true);
			btn_delete.setEnabled(true);
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
			
			// TODO ��Ӳ�����ť��OnclickListener,����½��ļ��в���
			et_NewName = (EditText) view.findViewById(R.id.et_newFileName);
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmRename);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelRename);
			
			
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
		renameDialog.setFocusable(true);
		// ����������������ʧ
		renameDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
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
			
			// TODO ��Ӳ�����ť��OnclickListener,����½��ļ��в���
			tv_showFileInfo = (TextView) view.findViewById(R.id.tv_showFileInfo);
			btn_close = (Button) view.findViewById(R.id.btn_closeFileInfo);
			
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
			
			btn_close.setOnTouchListener(mOnTouchListener);
			
			// TODO �ڴ˴�������TextView��ʾ�ļ���Ϣ(һ��Ҫ�ڴ˴����ã�)
			tv_showFileInfo.setText("�˴���ʾ�ļ���ϸ��Ϣ");
			
		}

		
		// ʹ��ۼ�
		showFileInfoDialg.setFocusable(true);
		// ����������������ʧ
		showFileInfoDialg.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		showFileInfoDialg.setBackgroundDrawable(new BitmapDrawable());
		
		showFileInfoDialg.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	private void showDeleteDialog(View parent, final String deleteFilePath) 
	{
		TextView tv_showInfo;
		Button btn_confirm;
		Button btn_cancel;
		
//		if (deleteDialog == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater.from(context);
					
			view = layoutInflater.inflate(R.layout.window_delete, null);

			deleteDialog = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			
			// TODO ��Ӳ�����ť��OnclickListener,����½��ļ��в���
			btn_confirm = (Button) view.findViewById(R.id.btn_confirmDelete);
			btn_cancel = (Button) view.findViewById(R.id.btn_cancelDelete);
			tv_showInfo = (TextView) view.findViewById(R.id.tv_confirmDelete);
			
			
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
			
			// TODO �ڴ˴�����ɾ���Ի�����ʾ���ݣ�file/folder��
			String[] tmp = deleteFilePath.split("/");
			String hint = context.getString(R.string.delete_file) + tmp[tmp.length-1];
			tv_showInfo.setText(hint);
			
			//Ϊdelete��ť����onclickListener
			btn_confirm.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.i(TAG, "To Delete��" + deleteFilePath);
//					SVNApplication deleteApp = (SVNApplication) context.getApplicationContext();
//					DeleteUtil deleter = new DeleteUtil(deleteApp, deleteFilePath);
					Log.i(TAG, "start delete");
//					if(deleter.doDeleteFormSVN())
//					{
//						Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show();
//					}
//					else
//					{
//						Toast.makeText(context, R.string.delete_error, Toast.LENGTH_SHORT).show();
//					}
					
					new deleteThread(deleteFilePath).start();
					
					deleteDialog.dismiss();
				}
			});
			
			btn_cancel.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					deleteDialog.dismiss();
				}
			});
			
//		}

		// ʹ��ۼ�
		deleteDialog.setFocusable(true);
		// ����������������ʧ
		deleteDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		deleteDialog.setBackgroundDrawable(new BitmapDrawable());
		
		deleteDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
	
	
	private class deleteThread extends Thread
	{
		private String deleteFilePath;

		public deleteThread(String deleteFilePath)
		{
			this.deleteFilePath = deleteFilePath;
		}
		
		@Override
		public void run()
		{
			SVNApplication deleteApp = (SVNApplication) context.getApplicationContext();
			DeleteUtil deleter = new DeleteUtil(deleteApp, deleteFilePath);
			
			Message deleteMsg = new Message();
			deleteMsg.what = DELETE_MSG;
			
			if(deleter.doDeleteFormSVN())
			{
				deleteMsg.arg1 = DELETE_SUCCESS;
			}
			else
			{
				deleteMsg.arg1 = DELETE_ERROR;
			}
			
			showFileHandler.sendMessage(deleteMsg);
			
			super.run();
		}
		
	}
}

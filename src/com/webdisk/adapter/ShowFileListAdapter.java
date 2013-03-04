package com.webdisk.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	private LayoutInflater mInflater;
	private Bitmap icon_file;
	private Bitmap icon_folder;
	private List<String> items;
	private List<String> paths;
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
	
	public ShowFileListAdapter(Context context, List<String> it, List<String> pa)
	{
		mInflater = LayoutInflater.from(context);
		items = it;
		paths = pa;
		this.context = context;
		
		icon_file = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file);
		icon_folder = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_folder);
	}
	
	
	@Override
	public int getCount()
	{
		return items.size();
	}

	@Override
	public Object getItem(int position)
	{
		return items.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
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
		
		File f = new File(paths.get(position).toString());
		
		//Ϊÿһ����������
		holder.tv_fileName.setText(f.getName());
		if(f.isDirectory())
		{
			holder.iv_showFileImage.setImageBitmap(icon_folder);
		}
		else
		{
			holder.iv_showFileImage.setImageBitmap(icon_file);
		}
		
		// TODO Ϊbtn_fileAction���õ����˵�
		holder.btn_fileAction.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showActionMenu(v);
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
	
	private void showActionMenu(final View parent) 
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
				
				Intent intent = new Intent(context, DownloadActivity.class);
				context.startActivity(intent);
			}
		});

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
	
	private void showDeleteDialog(View parent) 
	{
		TextView tv_showInfo;
		Button btn_confirm;
		Button btn_cancel;
		
		if (deleteDialog == null) {
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
			tv_showInfo.setText(R.string.delete_file);
			
		}

		// ʹ��ۼ�
		deleteDialog.setFocusable(true);
		// ����������������ʧ
		deleteDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		deleteDialog.setBackgroundDrawable(new BitmapDrawable());
		
		deleteDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}
}

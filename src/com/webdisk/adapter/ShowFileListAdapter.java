package com.webdisk.adapter;

import java.io.File;
import java.io.FilePermission;
import java.security.acl.LastOwnerException;
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
	private final static int RENAME_MSG = 12;
	private final static int RENAME_SUCCESS = 121;
	private final static int RENAME_ERROR = 120;

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

	public ShowFileListAdapter(Context context, Handler showFileHandler,
			List<SVNDirEntry> dirnames)
	{
		mInflater = LayoutInflater.from(context);
		this.context = context;
		this.showFileHandler = showFileHandler;
		this.mdir = dirnames;

		icon_file = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.icon_file);
		icon_folder = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.icon_folder);
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

		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.row_showfile, null);
			holder = new ViewHolder();
			holder.btn_fileAction = (Button) convertView
					.findViewById(R.id.btn_row_expandFileAction);
			holder.iv_showFileImage = (ImageView) convertView
					.findViewById(R.id.iv_row_showFileImage);
			// holder.tv_fileInfo =
			// (TextView)convertView.findViewById(R.id.tv_row_showFileInfo);
			holder.tv_fileName = (TextView) convertView
					.findViewById(R.id.tv_row_showFileName);

			holder.btn_fileAction
					.setOnTouchListener(new Button.OnTouchListener()
					{
						public boolean onTouch(View v, MotionEvent event)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								v.setBackgroundResource(R.drawable.icon_navigation_expand_touched);
							} else if (event.getAction() == MotionEvent.ACTION_UP)
							{
								v.setBackgroundResource(R.drawable.icon_navigation_expand);
							}

							return false;
						}
					});

			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		// File f = new File(paths.get(position).toString());
		//
		// //Ϊÿһ����������
		// holder.tv_fileName.setText(f.getName());
		// if(f.isDirectory())
		// {
		// holder.iv_showFileImage.setImageBitmap(icon_folder);
		// }
		// else
		// {
		// holder.iv_showFileImage.setImageBitmap(icon_file);
		// }

		SVNDirEntry entry = mdir.get(position);

		holder.tv_fileName.setText(entry.getName());

		if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0) // ��ǰΪ�ļ���ʱ
		{
			holder.iv_showFileImage.setImageBitmap(icon_folder);
			// ���ļ��н�ֹfileAction��ť
			// holder.btn_fileAction.setEnabled(false);
			// holder.btn_fileAction.setVisibility(View.GONE);
		} else if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0) // ��ǰΪ�ļ�ʱ
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
		final SVNDirEntry entry = mdir.get(position);
		final String filePath = entry.getURL().toDecodedString();

		if (entry.getKind().compareTo(SVNNodeKind.FILE) == 0)// �ļ�����
		{
			// if (actionMenu == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater
					.from(context);

			view = layoutInflater.inflate(R.layout.menu_fileaction, null);

			actionMenu = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);

			// ��ʼ������ť
			btn_copy = (Button) view.findViewById(R.id.btn_copy);
			btn_move = (Button) view.findViewById(R.id.btn_move);
			btn_rename = (Button) view.findViewById(R.id.btn_rename);
			btn_showFileInfo = (Button) view.findViewById(R.id.btn_fileInfo);
			btn_delete = (Button) view.findViewById(R.id.btn_delete);
			btn_download = (Button) view.findViewById(R.id.btn_download);
			// }

			// ʹ��ۼ�
			actionMenu.setFocusable(true);
			// ����������������ʧ
			actionMenu.setOutsideTouchable(true);
			// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
			actionMenu.setBackgroundDrawable(new BitmapDrawable());

			// actionMenu.showAsDropDown(parent, 0, 0);
			actionMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);

			// ��Ӳ�����ť��OnclickListener
			// ����
			btn_copy.setOnClickListener(new Button.OnClickListener()
			{
				// SVNDirEntry entry = mdir.get(position);
				// String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(context, PasteActivity.class);
					intent.putExtra("IS_MOVE", false);
					intent.putExtra("SRC_FILE_PATH", filePath);
					context.startActivity(intent);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}

				}
			});
			// �ƶ�
			btn_move.setOnClickListener(new Button.OnClickListener()
			{
				SVNDirEntry entry = mdir.get(position);
				String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(context, PasteActivity.class);
					intent.putExtra("IS_MOVE", true);
					intent.putExtra("SRC_FILE_PATH", filePath);
					intent.putExtra("IS_FOLDER", false);
					context.startActivity(intent);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}
				}
			});
			// ������
			btn_rename.setOnClickListener(new Button.OnClickListener()
			{
				// SVNDirEntry entry = mdir.get(position);
				// String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					showReNameDialog(parent, filePath, false);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}

				}
			});
			// ��ʾ�ļ�����
			btn_showFileInfo.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					showFileInfoDialog(parent, filePath);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}
				}
			});
			// ɾ��
			btn_delete.setOnClickListener(new Button.OnClickListener()
			{
				// SVNDirEntry entry = mdir.get(position);
				// String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					showDeleteDialog(parent, filePath, false);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}
				}
			});

			// ����
			btn_download.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}

					// SVNDirEntry entry = mdir.get(position);
					// String filePath = entry.getURL().toDecodedString();
					// Log.i(TAG, "filePath=" + filePath);

					Intent intent = new Intent(context, DownloadActivity.class);
					intent.putExtra("FILE_PATH", filePath);
					context.startActivity(intent);
				}
			});
		} else if (entry.getKind().compareTo(SVNNodeKind.DIR) == 0)// �ļ���ʱ
		{
			// if (actionMenu == null) {
			LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater
					.from(context);

			view = layoutInflater.inflate(R.layout.menu_folderaction, null);

			actionMenu = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);

			// ��ʼ������ť
			btn_move = (Button) view.findViewById(R.id.btn_folder_move);
			btn_rename = (Button) view.findViewById(R.id.btn_folder_rename);
			btn_delete = (Button) view.findViewById(R.id.btn_folder_delete);
			// }

			// ʹ��ۼ�
			actionMenu.setFocusable(true);
			// ����������������ʧ
			actionMenu.setOutsideTouchable(true);
			// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
			actionMenu.setBackgroundDrawable(new BitmapDrawable());

			// actionMenu.showAsDropDown(parent, 0, 0);
			actionMenu.showAtLocation(parent, Gravity.CENTER, 0, 0);

			// ��Ӳ�����ť��OnclickListener
			// �ƶ�
			btn_move.setOnClickListener(new Button.OnClickListener()
			{
				SVNDirEntry entry = mdir.get(position);
				String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(context, PasteActivity.class);
					intent.putExtra("IS_MOVE", true);
					intent.putExtra("IS_FOLDER", true);
					intent.putExtra("SRC_FILE_PATH", filePath);
					context.startActivity(intent);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}
				}
			});
			// ������
			btn_rename.setOnClickListener(new Button.OnClickListener()
			{
				// SVNDirEntry entry = mdir.get(position);
				// String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					showReNameDialog(parent, filePath, true);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}

				}
			});
			// ɾ��
			btn_delete.setOnClickListener(new Button.OnClickListener()
			{
				// SVNDirEntry entry = mdir.get(position);
				// String filePath = entry.getURL().toDecodedString();

				@Override
				public void onClick(View v)
				{
					showDeleteDialog(parent, filePath, true);

					if (actionMenu != null)
					{
						actionMenu.dismiss();
					}
				}
			});

		}
		// //���Ŀ��Ϊ�ļ��У���ֹ���ء�ɾ�����ƶ������ơ����鰴ť
		// SVNDirEntry targetEntry = mdir.get(position);
		// if(targetEntry.getKind().compareTo(SVNNodeKind.DIR) == 0)
		// {
		// btn_download.setEnabled(false);
		// btn_delete.setEnabled(false);
		// }
		// else if(targetEntry.getKind().compareTo(SVNNodeKind.FILE) == 0)
		// {
		// btn_download.setEnabled(true);
		// btn_delete.setEnabled(true);
		// }

	}

	private void showReNameDialog(View parent, final String srcFilePath,
			final boolean isFolder)
	{
		final EditText et_newName;
		Button btn_confirm;
		Button btn_cancel;
		final String srcFileName = srcFilePath.substring(
				srcFilePath.lastIndexOf("/") + 1, srcFilePath.length());

		LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater
				.from(context);

		view = layoutInflater.inflate(R.layout.window_rename, null);

		renameDialog = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		et_newName = (EditText) view.findViewById(R.id.et_newFileName);
		btn_confirm = (Button) view.findViewById(R.id.btn_confirmRename);
		btn_cancel = (Button) view.findViewById(R.id.btn_cancelRename);

		// ����et_newName����Ϊԭ�ļ���
		if (!isFolder)
		{
			et_newName.setText(srcFileName.substring(0,
					srcFileName.lastIndexOf(".")));
		} else
		{
			et_newName.setText(srcFileName);
		}

		// Ϊ������ť����OnTouchListener
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

		btn_confirm.setOnTouchListener(mOnTouchListener);
		btn_cancel.setOnTouchListener(mOnTouchListener);

		// Ϊ��������ť����onclickListener
		btn_confirm.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// String srcFileName =
				// srcFilePath.substring(srcFilePath.lastIndexOf("/")+1,
				// srcFilePath.length());

				final String newName;
				if (!isFolder)
				{
					String suffix = srcFileName.substring(
							srcFileName.lastIndexOf("."), srcFileName.length());
					newName = et_newName.getText().toString() + suffix;
					// Log.i(TAG, "Rename file " + srcFilePath + " as " +
					// newName);
				} else
				{
					newName = et_newName.getText().toString();
				}

				if (et_newName.getText().toString().equals(""))
				{
					Toast.makeText(context, R.string.input_new_file_name,
							Toast.LENGTH_SHORT).show();
				} else if (newName.equals(srcFileName))
				{
					renameDialog.dismiss();
				} else
				{
					Log.i(TAG, "Rename file " + srcFilePath + " as " + newName);
					Toast.makeText(context, R.string.renaming,
							Toast.LENGTH_SHORT).show();
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							Message renameMsg = new Message();
							renameMsg.what = RENAME_MSG;

							SVNApplication renameApp = (SVNApplication) context
									.getApplicationContext();
							if (renameApp.doRename(srcFilePath, newName))
							{
								renameMsg.arg1 = RENAME_SUCCESS;
							} else
							{
								renameMsg.arg1 = RENAME_ERROR;
							}
							showFileHandler.sendMessage(renameMsg);
						}
					}).start();

					renameDialog.dismiss();
				}
			}
		});

		// Ϊȡ����ť����onclickListener
		btn_cancel.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				renameDialog.dismiss();
			}
		});

		// ʹ��ۼ�
		renameDialog.setFocusable(true);
		// ����������������ʧ
		renameDialog.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		renameDialog.setBackgroundDrawable(new BitmapDrawable());

		renameDialog.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}

	private void showFileInfoDialog(View parent, String filePath)
	{
		TextView tv_showFileInfo = null;
		Button btn_close = null;

		LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater
				.from(context);

		view = layoutInflater.inflate(R.layout.windows_showfileinfo, null);

		showFileInfoDialg = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		tv_showFileInfo = (TextView) view.findViewById(R.id.tv_showFileInfo);
		btn_close = (Button) view.findViewById(R.id.btn_closeFileInfo);

		//������ʾ����
		String fileName  = filePath.substring(filePath.lastIndexOf("/") + 1);
		String showFileName = context.getString(R.string.info_fileName) + " " + fileName;
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		String showFileKind = null;
		
		// TODO ��������ļ�����
		if(".txt".equals(suffix) || ".doc".equals(suffix) || ".pdf".equals(suffix))
		{
			showFileKind = context.getString(R.string.info_fileSuffix) + " �ĵ��ļ�(" + suffix + ")"; 
		}
		else if(".wma".equals(suffix) || ".mp3".equals(suffix) || ".wav".equals(suffix))
		{
			showFileKind = context.getString(R.string.info_fileSuffix) + " �����ļ�(" + suffix + ")"; 
		}
		else if(".MP4".equals(suffix) || ".wmv".equals(suffix) || ".rmvb".equals(suffix))
		{
			showFileKind = context.getString(R.string.info_fileSuffix) + " ��Ƶ�ļ�(" + suffix + ")"; 
		}
		else if(".jpg".equals(suffix) || ".png".equals(suffix) || ".bmp".equals(suffix))
		{
			showFileKind = context.getString(R.string.info_fileSuffix) + " ͼƬ�ļ�(" + suffix + ")"; 
		}
		else
		{
			showFileKind = context.getString(R.string.info_fileSuffix) + " �����ļ�(" + suffix + ")"; 
		}
		
		String fileInfo = showFileName + "\n" + showFileKind;
		
		tv_showFileInfo.setText(fileInfo);
		
		
		// Ϊ������ť����OnTouchListener
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

		btn_close.setOnTouchListener(mOnTouchListener);

		// TODO �ڴ˴�������TextView��ʾ�ļ���Ϣ(һ��Ҫ�ڴ˴����ã�)
//		tv_showFileInfo.setText("�˴���ʾ�ļ���ϸ��Ϣ");

		// ʹ��ۼ�
		showFileInfoDialg.setFocusable(true);
		// ����������������ʧ
		showFileInfoDialg.setOutsideTouchable(false);
		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		showFileInfoDialg.setBackgroundDrawable(new BitmapDrawable());

		showFileInfoDialg.showAtLocation(parent, Gravity.CENTER, 0, 0);

	}

	private void showDeleteDialog(View parent, final String deleteFilePath,
			final boolean isFolder)
	{
		TextView tv_showInfo;
		Button btn_confirm;
		Button btn_cancel;

		LayoutInflater layoutInflater = (LayoutInflater) LayoutInflater
				.from(context);

		view = layoutInflater.inflate(R.layout.window_delete, null);

		deleteDialog = new PopupWindow(view, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		btn_confirm = (Button) view.findViewById(R.id.btn_confirmDelete);
		btn_cancel = (Button) view.findViewById(R.id.btn_cancelDelete);
		tv_showInfo = (TextView) view.findViewById(R.id.tv_confirmDelete);

		// Ϊ������ť����OnTouchListener
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

		btn_confirm.setOnTouchListener(mOnTouchListener);
		btn_cancel.setOnTouchListener(mOnTouchListener);

		// TODO �ڴ˴�����ɾ���Ի�����ʾ���ݣ�file/folder��
		if (!isFolder)
		{
			String[] tmp = deleteFilePath.split("/");
			String hint = context.getString(R.string.delete_file) + "\n"
					+ tmp[tmp.length - 1];
			tv_showInfo.setText(hint);
		} else
		{
			String[] tmp = deleteFilePath.split("/");
			String hint = context.getString(R.string.delete_folder) + "\n"
					+ tmp[tmp.length - 1];
			tv_showInfo.setText(hint);
		}

		// Ϊdelete��ť����onclickListener
		btn_confirm.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.i(TAG, "To Delete��" + deleteFilePath);
				// SVNApplication deleteApp = (SVNApplication)
				// context.getApplicationContext();
				// DeleteUtil deleter = new DeleteUtil(deleteApp,
				// deleteFilePath);
				Log.i(TAG, "start delete");
				// if(deleter.doDeleteFormSVN())
				// {
				// Toast.makeText(context, R.string.delete_success,
				// Toast.LENGTH_SHORT).show();
				// }
				// else
				// {
				// Toast.makeText(context, R.string.delete_error,
				// Toast.LENGTH_SHORT).show();
				// }

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

		// }

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
			SVNApplication deleteApp = (SVNApplication) context
					.getApplicationContext();
			DeleteUtil deleter = new DeleteUtil(deleteApp, deleteFilePath);

			Message deleteMsg = new Message();
			deleteMsg.what = DELETE_MSG;

			if (deleter.doDelete())
			{
				deleteMsg.arg1 = DELETE_SUCCESS;
			} else
			{
				deleteMsg.arg1 = DELETE_ERROR;
			}

			showFileHandler.sendMessage(deleteMsg);

			super.run();
		}

	}
}

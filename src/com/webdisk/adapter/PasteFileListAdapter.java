package com.webdisk.adapter;

import java.io.File;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.webdisk.R;

public class PasteFileListAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	private Bitmap icon_file;
	private Bitmap icon_folder;
	private Context context;
	private List<SVNDirEntry> mdir;
	
	private PopupWindow actionMenu;
	
	public PasteFileListAdapter(Context context, List<SVNDirEntry> dirnames)
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
ViewHolder holder;
		
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.row_pastefile, null);
			holder = new ViewHolder();
			holder.iv_showFileImage = (ImageView)convertView.findViewById(R.id.iv_row_paste_showFileImage);
			holder.tv_fileName = (TextView)convertView.findViewById(R.id.tv_row_paste_showFileName);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		
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
		
		
		return convertView;
	}
	
	private class ViewHolder
	  {
		ImageView iv_showFileImage;
		TextView tv_fileName;
	  }
}

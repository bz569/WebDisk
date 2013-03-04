package com.webdisk.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
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
	private List<String> items;
	private List<String> paths;
	private Context context;
	
	private PopupWindow actionMenu;
	
	public PasteFileListAdapter(Context context, List<String> it, List<String> pa)
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
		
		File f = new File(paths.get(position).toString());
		
		//为每一行设置内容
		holder.tv_fileName.setText(f.getName());
		if(f.isDirectory())
		{
			holder.iv_showFileImage.setImageBitmap(icon_folder);
		}
		else
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

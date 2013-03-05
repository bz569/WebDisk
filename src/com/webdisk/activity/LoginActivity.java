package com.webdisk.activity;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.application.SVNApplication;
import com.webdisk.model.Connection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class LoginActivity extends Activity {
	
	
	private static final String TAG = "LoginActivity";
	
	private static final int LOGIN_SUCCESS = 101;
	private static final int LOGIN_ERROR = 100;
	
	private SVNApplication app;

	private EditText et_accountName;
	private EditText et_psw;
	private Button btn_login;
	private Button btn_regNow;
	
	private SVNURL fileUrl = null;
	
	private Handler mHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
				case LOGIN_SUCCESS:
				{
					Log.i(TAG, "login success");
					Intent intent = new Intent(LoginActivity.this, ShowFileActivity.class);
					startActivity(intent);
					finish();
					break;
				}
				case LOGIN_ERROR:
				{
					Log.i(TAG, "login error");
					
					Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
					et_psw.setText("");
					break;
				}
			}
			
			super.handleMessage(msg);
		}
	};
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        this.app = (SVNApplication)getApplication();
        
        et_accountName = (EditText)findViewById(R.id.et_accoutName);
        et_psw = (EditText)findViewById(R.id.et_psw);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_regNow = (Button)findViewById(R.id.btn_regNow);
        
        //为两个按钮设置按下效果
        btn_login.setOnTouchListener(new Button.OnTouchListener()
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
        
        btn_regNow.setOnTouchListener(new Button.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					v.setBackgroundResource(R.color.halo_darkorange);
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					v.setBackgroundResource(R.color.halo_lightorange);
				}
				
				return false;
			}
		});
        
        //为登陆按钮设置按下时的监听器
        btn_login.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String userName = et_accountName.getText().toString();
				final String password = et_psw.getText().toString();
				final String cachePath = "/mnt/sdcard/Webdisk/cache/";// TODO 此路径需要修改
				
				final File cacheFolder = new File(cachePath);
				if(!cacheFolder.exists())
				{
					cacheFolder.mkdir();
				}
					
				
				
				try
				{
					// TODO 需要修改为邮箱的映射文件
					fileUrl = SVNURL.parseURIEncoded("http://10.109.34.24/wangpan/2574402613.qq.com/test.txt");
				} catch (SVNException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				final String url = "http://10.109.34.24/wangpan/" + userName;
				Log.i(TAG, "URL=" + url);
				
//				Connection thisConnection = new Connection();
//				
//				// prep data for connection
//				thisConnection.setName("webdisk");
//				thisConnection.setUrl(url);
//				thisConnection.setUsername(userName);
//				thisConnection.setPassword(password);
//				
//				app.setCurrentConnection(thisConnection);
//				
//				app.initAuthManager();
				
//				String login_info = app.doExport(SVNRevision.HEAD, new File("/mnt/sdcard/test"), fileUrl);
//				
//				if("svn: Authentication required for '<http://10.109.34.24:80> hello svn'".equals(login_info))//登录失败
//				{
//					Log.i(TAG, "login error");
//					
//					Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
//					et_psw.setText("");
//				}
//				else
//				{
//					Log.i(TAG, "login success");
//					Intent intent = new Intent(LoginActivity.this, ShowFileActivity.class);
//					startActivity(intent);
//					finish();
//				}
				
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						Connection thisConnection = new Connection();
						
						// prep data for connection
						thisConnection.setName("webdisk");
						thisConnection.setUrl(url);
						thisConnection.setUsername(userName);
						thisConnection.setPassword(password);
						
						app.setCurrentConnection(thisConnection);
						
						app.initAuthManager();
						
						String login_info = app.doExport(SVNRevision.HEAD, cacheFolder, fileUrl);
						
						if("svn: Authentication required for '<http://10.109.34.24:80> hello svn'".equals(login_info))//登录失败
						{
							Message message = new Message();

							message.what = LOGIN_ERROR;
							mHandler.sendMessage(message);
						}
						else
						{
							Message message = new Message();

							message.what= LOGIN_SUCCESS;
							mHandler.sendMessage(message);
						}
					}
				}).start();
			}
		});
        
        
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
    
}

package com.webdisk.activity;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.application.SVNApplication;
import com.webdisk.model.Connection;

import android.os.Bundle;
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
	
	private SVNApplication app;

	private EditText et_accountName;
	private EditText et_psw;
	private Button btn_login;
	private Button btn_regNow;
	
	
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
				String userName = et_accountName.getText().toString();
				String password = et_psw.getText().toString();
				String sdPath = "/mnt/sdcard/test";
				
				SVNURL fileUrl = null;
				try
				{
					// TODO 需要修改为邮箱的映射文件
					fileUrl = SVNURL.parseURIEncoded("http://10.109.34.24/wangpan/2574402613.qq.com/test.txt");
				} catch (SVNException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String url = "http://10.109.34.24/wangpan/" + userName;
				Log.i(TAG, "URL=" + url);
				
				Connection thisConnection = new Connection();
				
				// prep data for save
				thisConnection.setName("webdisk");
				thisConnection.setUrl(url);
				thisConnection.setUsername(userName);
				thisConnection.setPassword(password);
				
				app.setCurrentConnection(thisConnection);
				
				app.initAuthManager();
				
				String login_info = app.doExport(SVNRevision.HEAD, new File("/mnt/sdcard/test"), fileUrl);
				
				if("svn: Authentication required for '<http://10.109.34.24:80> hello svn'".equals(login_info))//登录失败
				{
					Log.i(TAG, "login error");
					
					Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
					et_psw.setText("");
				}
				else
				{
					Log.i(TAG, "login success");
					Intent intent = new Intent(LoginActivity.this, ShowFileActivity.class);
					startActivity(intent);
					finish();
				}
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

package com.webdisk.activity;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.webdisk.R;
import com.webdisk.application.SVNApplication;
import com.webdisk.model.Connection;
import com.webdisk.util.AESUtil;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;



@SuppressLint("WorldReadableFiles")
public class LoginActivity extends Activity {
	
	
	private static final String TAG = "LoginActivity";
	private static final String mPREFERENCES = "userInfo";
	
	private static final int LOGIN_START = 98;
	private static final int LOGIN_SUCCESS = 101;
	private static final int LOGIN_AUTH_ERROR = 100;
	private static final int LOGIN_NO_CONNECTION = 99;
	
	private SVNApplication app;
	private SharedPreferences sharedPreferences;

	private EditText et_accountName;
	private EditText et_psw;
	private Button btn_login;
	private Button btn_regNow;
	private CheckBox cb_savePsw;
	private CheckBox cb_autoLogin;
	
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
					//��SharedPreference�б������뱣����ε��û���
					Editor editor = sharedPreferences.edit();
			        editor.putString("userName", et_accountName.getText().toString()).commit();
					//��ѡ�Զ���¼���������
			        if(cb_autoLogin.isChecked())
			        {
			        	cb_savePsw.setChecked(true);
			        	editor.putBoolean("AUTO_LOGIN_ISCHECK", true).commit();
			        }
			        else
			        {
			        	editor.putBoolean("AUTO_LOGIN_ISCHECK", false).commit();
			        }
			        
					//�����ѡ��ס���룬��SharedPreference�б�������
					if(cb_savePsw.isChecked())
					{
//						editor.putString("password", password).commit();
						try
						{
							editor.putString("password", AESUtil.encrypt(et_accountName.getText().toString(), et_psw.getText().toString())).commit();
						} catch (Exception e)
						{
							e.printStackTrace();
							Log.i(TAG, "AES���ܴ���");
						}
		                editor.putBoolean("SAVEPSW_ISCHECK", true).commit(); 
					}
					else//���û�й�ѡ�����֮ǰ�������Ŀ
					{
						editor.putString("password", "").commit();
		                editor.putBoolean("SAVEPSW_ISCHECK", false).commit(); 
					}
					
					Intent intent = new Intent(LoginActivity.this, ShowFileActivity.class);
					startActivity(intent);
					finish();
					break;
				}
				case LOGIN_AUTH_ERROR:
				{
					Log.i(TAG, "login error");
					
					Toast.makeText(LoginActivity.this, R.string.login_auth_error, Toast.LENGTH_SHORT).show();
					et_psw.setText("");
					btn_login.setText(R.string.login);
					btn_login.setEnabled(true);
					break;
				}
				case LOGIN_NO_CONNECTION:
				{
					Log.i(TAG, "login error");
					
					Toast.makeText(LoginActivity.this, R.string.login_no_connection, Toast.LENGTH_SHORT).show();
					et_psw.setText("");
					btn_login.setText(R.string.login);
					btn_login.setEnabled(true);
					break;
				}
				case LOGIN_START:
				{
					btn_login.setText(R.string.logining);
					btn_login.setEnabled(false);
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
        sharedPreferences = this.getSharedPreferences(mPREFERENCES,Context.MODE_WORLD_READABLE);  
        
        et_accountName = (EditText)findViewById(R.id.et_accoutName);
        et_psw = (EditText)findViewById(R.id.et_psw);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_regNow = (Button)findViewById(R.id.btn_regNow);
        cb_savePsw = (CheckBox)findViewById(R.id.cb_savePsw);
        cb_autoLogin = (CheckBox)findViewById(R.id.cb_autoLogin);
        
        //��ȡSharedPreference�б��������
        et_accountName.setText(sharedPreferences.getString("userName", ""));
        if (sharedPreferences.getBoolean("SAVEPSW_ISCHECK", false)) //֮ǰ���ü�ס����
        {
//        	et_psw.setText(sharedPreferences.getString("password", ""));
        	
        	String pswDecrypted = "";
        	try
			{
				pswDecrypted = AESUtil.decrypt(sharedPreferences.getString("userName", ""), sharedPreferences.getString("password", ""));
				Log.i(TAG, "AES���ܽ����" + pswDecrypted);
			} catch (Exception e)
			{
				e.printStackTrace();
				Log.i(TAG, "AES���ܴ���");
			}
        	et_psw.setText(pswDecrypted);
        	cb_savePsw.setChecked(true);
        }
        
        //�Ƿ��Զ���¼
        if(sharedPreferences.getBoolean("AUTO_LOGIN_ISCHECK", false))
        {
        	cb_autoLogin.setChecked(true);
        	login();
        }
        
        
        //Ϊ������ť���ð���Ч��
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
        
        // �����Զ���¼checkbox�ļ�����
        cb_autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
				{
					cb_savePsw.setChecked(true);
				}
			}
		});
        
        //Ϊ��½��ť���ð���ʱ�ļ�����
        btn_login.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String userName = et_accountName.getText().toString();
				final String password = et_psw.getText().toString();
				final String cachePath = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";// TODO ��·����Ҫ�޸�
				Log.i(TAG, "cachePath=" + cachePath);
				final File cacheFolder = new File(cachePath);
//				//��SharedPreference�б������뱣����ε��û���
//				Editor editor = sharedPreferences.edit();
//		        editor.putString("userName", userName).commit();
		        
//		        //��ѡ�Զ���¼���������
//		        if(cb_autoLogin.isChecked())
//		        {
//		        	cb_savePsw.setChecked(true);
//		        	editor.putBoolean("AUTO_LOGIN_ISCHECK", true).commit();
//		        }
//		        else
//		        {
//		        	editor.putBoolean("AUTO_LOGIN_ISCHECK", false).commit();
//		        }
//		        
//				//�����ѡ��ס���룬��SharedPreference�б�������
//				if(cb_savePsw.isChecked())
//				{
////					editor.putString("password", password).commit();
//					try
//					{
//						editor.putString("password", AESUtil.encrypt(userName, password)).commit();
//					} catch (Exception e)
//					{
//						e.printStackTrace();
//						Log.i(TAG, "AES���ܴ���");
//					}
//	                editor.putBoolean("SAVEPSW_ISCHECK", true).commit(); 
//				}
//				else//���û�й�ѡ�����֮ǰ�������Ŀ
//				{
//					editor.putString("password", "").commit();
//	                editor.putBoolean("SAVEPSW_ISCHECK", false).commit(); 
//				}
				
				login();
				
//				if(!cacheFolder.exists())
//				{
//					cacheFolder.mkdir();
//				}
//					
//				try
//				{
//					fileUrl = SVNURL.parseURIEncoded("http://10.109.34.24/wangpan/" + userName + "/.config.db");
//				} catch (SVNException e)
//				{
//					e.printStackTrace();
//				}
//				
//				final String url = "http://10.109.34.24/wangpan/" + userName;
//				Log.i(TAG, "URL=" + url);
//				
//				
//				new Thread(new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						Message loginStartMsg = new Message();
//						loginStartMsg.what = LOGIN_START;
//						mHandler.sendMessage(loginStartMsg);
//						
//						Connection thisConnection = new Connection();
//						
//						// prep data for connection
//						thisConnection.setName("webdisk");
//						thisConnection.setUrl(url);
//						thisConnection.setUsername(userName);
//						thisConnection.setPassword(password);
//						
//						app.setCurrentConnection(thisConnection);
//						
//						app.initAuthManager();
//						
//						String login_info = app.doExport(SVNRevision.HEAD, cacheFolder, fileUrl);
//						
//						if("svn: Authentication required for '<http://10.109.34.24:80> hello svn'".equals(login_info))//��¼ʧ��
//						{
//							Message message = new Message();
//							message.what = LOGIN_AUTH_ERROR;
//							mHandler.sendMessage(message);
//						}
//						else if((getString(R.string.success)).equals(login_info))
//						{
//							Message message = new Message();
//							message.what = LOGIN_SUCCESS;
//							mHandler.sendMessage(message);
//						}
//						else
//						{
//							Message message = new Message();
//							message.what= LOGIN_NO_CONNECTION;
//							mHandler.sendMessage(message);
//						}
//					}
//				}).start();
			}
		});
        
        
    }

	private void login()
	{
		final String userName = et_accountName.getText().toString();
		final String password = et_psw.getText().toString();
		final String cachePath = Environment.getExternalStorageDirectory() + "/Webdisk/cache/";// TODO ��·����Ҫ�޸�
		Log.i(TAG, "cachePath=" + cachePath);
		final File cacheFolder = new File(cachePath);
		
		if(!cacheFolder.exists())
		{
			cacheFolder.mkdir();
		}
			
		try
		{
			// TODO ��Ҫ�޸�Ϊ�����ӳ���ļ�
			fileUrl = SVNURL.parseURIEncoded("http://10.109.34.24/wangpan/" + userName + "/.config.db");
		} catch (SVNException e)
		{
			e.printStackTrace();
		}
		
		final String url = "http://10.109.34.24/wangpan/" + userName;
		Log.i(TAG, "URL=" + url);
		
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Message loginStartMsg = new Message();
				loginStartMsg.what = LOGIN_START;
				mHandler.sendMessage(loginStartMsg);
				
				Connection thisConnection = new Connection();
				
				// prep data for connection
				thisConnection.setName("webdisk");
				thisConnection.setUrl(url);
				thisConnection.setUsername(userName);
				thisConnection.setPassword(password);
				
				app.setCurrentConnection(thisConnection);
				
				app.initAuthManager();
				
				String login_info = app.doExport(SVNRevision.HEAD, cacheFolder, fileUrl);
				
				if("svn: Authentication required for '<http://10.109.34.24:80> hello svn'".equals(login_info))//��¼ʧ��
				{
					Message message = new Message();
					message.what = LOGIN_AUTH_ERROR;
					mHandler.sendMessage(message);
				}
				else if((getString(R.string.success)).equals(login_info))
				{
					Message message = new Message();
					message.what = LOGIN_SUCCESS;
					mHandler.sendMessage(message);
				}
				else
				{
					Message message = new Message();
					message.what= LOGIN_NO_CONNECTION;
					mHandler.sendMessage(message);
				}
			}
		}).start();
	}
    
}

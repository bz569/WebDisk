package com.webdisk.activity;

import com.webdisk.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable(){

		public void run() {
		Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
		startActivity(intent);
		SplashActivity.this.finish();
		}
		}, 2500);
	}

}

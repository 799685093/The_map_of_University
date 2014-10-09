package com.wpy.map.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import com.wpy.map.R;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGHT = 3000; // �ӳ�3��  
	 private SharedPreferences preferences;  
	  private Editor editor;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		 preferences = getSharedPreferences("phone", Context.MODE_PRIVATE); 
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (preferences.getBoolean("firststart", true)) {
					editor = preferences.edit();  
				    //����¼��־λ����Ϊfalse���´ε�¼ʱ������ʾ�״ε�¼����  
				    editor.putBoolean("firststart", false);  
				    editor.commit(); 
				    Intent intent=new Intent();
					intent.setClass(SplashActivity.this,GuideViewActivity.class);
					SplashActivity.this.startActivity(intent);
					SplashActivity.this.finish();
				}else{
					 Intent intent=new Intent();
						intent.setClass(SplashActivity.this,MainActivity.class);
						SplashActivity.this.startActivity(intent);
						SplashActivity.this.finish();
					
				}
				
			}
		},SPLASH_DISPLAY_LENGHT);
	}


}

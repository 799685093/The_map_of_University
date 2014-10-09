package com.wpy.map.activity;

import com.wpy.map.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class AboutActivity extends Activity implements OnClickListener {

	private ImageButton btn_back_about;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		btn_back_about = (ImageButton) this.findViewById(R.id.btn_back_about);
		btn_back_about.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_about:
			this.finish();
			break;
		}
	}
}

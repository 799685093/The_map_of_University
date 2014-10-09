package com.wpy.map.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wpy.map.R;
import com.wpy.map.entity.Activities;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

/**
 * ���ϸ����ҳ��
 * 
 * @author wpy
 * 
 */
public class IntroActivityDetailActivity extends Activity implements
		OnClickListener {

	private TextView tv_activity_title;
	private ImageButton btn_back_activity;
	private TextView tv_Organizers;
	private TextView tv_activity_time;
	private TextView tv_address;
	private TextView tv_activity_details;
	private Button btn_reminder;

	private Activities activities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introdetailactivity);
		String name = getIntent().getExtras().getString("name");// ��ȡintent�е�ֵ
		initwidget();
		getdata(name);
		setdata();
	}

	/**
	 * ��ʼ���ؼ�
	 */
	private void initwidget() {
		tv_activity_title = (TextView) this
				.findViewById(R.id.tv_activity_title);
		btn_back_activity = (ImageButton) this
				.findViewById(R.id.btn_back_activity);
		btn_back_activity.setOnClickListener(this);
		tv_Organizers = (TextView) this.findViewById(R.id.tv_Organizers);
		tv_activity_time = (TextView) this.findViewById(R.id.tv_activity_time);
		tv_address = (TextView) this.findViewById(R.id.tv_address);
		tv_activity_details = (TextView) this
				.findViewById(R.id.tv_activity_details);
		btn_reminder = (Button) this.findViewById(R.id.btn_reminder);
		btn_reminder.setOnClickListener(this);

	}

	/**
	 * �ӷ�������ȡ����
	 * 
	 * @param name
	 *            �������
	 * @return ʵ��
	 */
	private void getdata(String name) {
		// ���������ѯ��������
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "findSimple");
		map.put("name", name);
		String url = HttpUtil.BASE_URL + "servlet/ActivityServleat";
		try {
			String jsonString = HttpUtil.postRequest(url, map);
			activities = JsonUtil.getSimple(
					JsonUtil.getJsonValueByKey(jsonString, "activities"),
					Activities.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʾ����
	 * 
	 */
	private void setdata() {
		tv_activity_title.setText(activities.name);
		tv_Organizers.setText(activities.organizer);
		tv_activity_time.setText(activities.begintime + " - "
				+ activities.endtime);
		tv_address.setText(activities.address);
		tv_activity_details.setText(activities.intro);
	}

	/**
	 * ��ť�ĵ���¼�����
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_activity:
			this.finish();
			break;
		case R.id.btn_reminder:
			Intent intent = new Intent(this, AlarmDialogActivity.class);
			// ����Ҫ����Ϣ�ŵ�Intent��
			intent.putExtra("activities", activities);
			startActivity(intent);
			break;
		}
	}
}

package com.wpy.map.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wpy.map.R;
import com.wpy.map.entity.Activities;

/**
 * 设置活动提醒对话框
 * 
 * @author wpy
 * 
 */
public class AlarmDialogActivity extends Activity implements OnClickListener {
	private Activities activities;
	private TextView tv_data;
	private TextView tv_time;
	private TextView tv_content;
	private RelativeLayout btn_cancle;
	private RelativeLayout btn_confim;

	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;

	private Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alarm);
		activities = (Activities) getIntent()
				.getSerializableExtra("activities");
		splitstringtime(activities.begintime);
		calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);// 得到当前的年份
		initwidget();
	}

	/**
	 * 初始化控件
	 */
	private void initwidget() {
		tv_data = (TextView) this.findViewById(R.id.tv_data);
		tv_data.setText(year + "年" + month + "月" + day + "日");
		tv_time = (TextView) this.findViewById(R.id.tv_time);
		tv_time.setOnClickListener(this);
		tv_content = (TextView) this.findViewById(R.id.tv_content);
		tv_content.setText(activities.name);
		btn_cancle = (RelativeLayout) this.findViewById(R.id.btn_cancle);
		btn_cancle.setOnClickListener(this);
		btn_confim = (RelativeLayout) this.findViewById(R.id.btn_confim);
		btn_confim.setOnClickListener(this);
	}

	/**
	 * 时间字符分割 5月9日 周五 00:00
	 * 
	 * @param s
	 * @return
	 */
	private void splitstringtime(String s) {
		String[] mStrings = s.split(" ");
		String[] y = mStrings[0].split("月");
		month = Integer.parseInt(y[0]);
		String[] d = y[1].split("日");
		day = Integer.parseInt(d[0]);
	}

	/**
	 * 点击事件监听
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_time:
			Calendar currentTime = Calendar.getInstance();
			// 创建一个TimePickerDialog实例，并把它显示出来。
			new TimePickerDialog(this, 0, // 绑定监听器
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
								int minuteofhour) {
							// 根据用户选择时间来设置Calendar对象
							hour = hourOfDay;
							minute = minuteofhour;
							// 设置显示
							tv_time.setText(hour + "时" + minute + "分");
						}
					}, currentTime.get(Calendar.HOUR_OF_DAY),
					currentTime.get(Calendar.MINUTE), false).show();
			break;
		case R.id.btn_cancle:
			this.finish();
			break;
		case R.id.btn_confim:
			if (tv_time.getText().toString().equals("点击选择提醒时间")) {
				Toast.makeText(this, "请选择提醒的时间", Toast.LENGTH_LONG).show();
				return;
			} else {
				calendar.clear();
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month - 1);
				calendar.set(Calendar.DAY_OF_MONTH, day);
				calendar.set(Calendar.HOUR, hour);
				calendar.set(Calendar.MINUTE, minute);
				// 获取AlarmManager对象
				AlarmManager aManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
				// 指定启动AlarmActivity组件
				Intent intent = new Intent(this,
						EventRemindDialogActivity.class);
				intent.putExtra("activities", activities);
				// 创建PendingIntent对象
				PendingIntent pi = PendingIntent.getActivity(this,
						activities.id, intent, 0);
				// 设置AlarmManager将在Calendar对应的时间启动指定组件
				aManager.set(AlarmManager.RTC_WAKEUP,
						calendar.getTimeInMillis(), pi);
				String str = String.format("%tF %<tT",
						calendar.getTimeInMillis());
				Log.e("time", str);
				// 显示闹铃设置成功的提示信息
				Toast.makeText(this, "成功添加提醒", Toast.LENGTH_SHORT).show();
				this.finish();
			}
			break;
		}
	}
}

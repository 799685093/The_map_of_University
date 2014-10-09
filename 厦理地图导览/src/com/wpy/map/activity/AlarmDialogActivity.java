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
 * ���û���ѶԻ���
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
		year = calendar.get(Calendar.YEAR);// �õ���ǰ�����
		initwidget();
	}

	/**
	 * ��ʼ���ؼ�
	 */
	private void initwidget() {
		tv_data = (TextView) this.findViewById(R.id.tv_data);
		tv_data.setText(year + "��" + month + "��" + day + "��");
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
	 * ʱ���ַ��ָ� 5��9�� ���� 00:00
	 * 
	 * @param s
	 * @return
	 */
	private void splitstringtime(String s) {
		String[] mStrings = s.split(" ");
		String[] y = mStrings[0].split("��");
		month = Integer.parseInt(y[0]);
		String[] d = y[1].split("��");
		day = Integer.parseInt(d[0]);
	}

	/**
	 * ����¼�����
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_time:
			Calendar currentTime = Calendar.getInstance();
			// ����һ��TimePickerDialogʵ������������ʾ������
			new TimePickerDialog(this, 0, // �󶨼�����
					new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker tp, int hourOfDay,
								int minuteofhour) {
							// �����û�ѡ��ʱ��������Calendar����
							hour = hourOfDay;
							minute = minuteofhour;
							// ������ʾ
							tv_time.setText(hour + "ʱ" + minute + "��");
						}
					}, currentTime.get(Calendar.HOUR_OF_DAY),
					currentTime.get(Calendar.MINUTE), false).show();
			break;
		case R.id.btn_cancle:
			this.finish();
			break;
		case R.id.btn_confim:
			if (tv_time.getText().toString().equals("���ѡ������ʱ��")) {
				Toast.makeText(this, "��ѡ�����ѵ�ʱ��", Toast.LENGTH_LONG).show();
				return;
			} else {
				calendar.clear();
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month - 1);
				calendar.set(Calendar.DAY_OF_MONTH, day);
				calendar.set(Calendar.HOUR, hour);
				calendar.set(Calendar.MINUTE, minute);
				// ��ȡAlarmManager����
				AlarmManager aManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
				// ָ������AlarmActivity���
				Intent intent = new Intent(this,
						EventRemindDialogActivity.class);
				intent.putExtra("activities", activities);
				// ����PendingIntent����
				PendingIntent pi = PendingIntent.getActivity(this,
						activities.id, intent, 0);
				// ����AlarmManager����Calendar��Ӧ��ʱ������ָ�����
				aManager.set(AlarmManager.RTC_WAKEUP,
						calendar.getTimeInMillis(), pi);
				String str = String.format("%tF %<tT",
						calendar.getTimeInMillis());
				Log.e("time", str);
				// ��ʾ�������óɹ�����ʾ��Ϣ
				Toast.makeText(this, "�ɹ��������", Toast.LENGTH_SHORT).show();
				this.finish();
			}
			break;
		}
	}
}

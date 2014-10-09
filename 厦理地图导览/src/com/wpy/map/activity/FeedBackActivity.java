package com.wpy.map.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wpy.map.R;
import com.wpy.map.util.HttpUtil;

public class FeedBackActivity extends Activity implements OnClickListener {

	private ImageButton btn_back_feedback;
	private EditText et_feed_content;
	private EditText et_feed_contact;
	private Button btn_submit;

	private UpLoadAsyncTask upLoadAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		initwidget();
	}

	/**
	 * 初始化控件
	 */
	private void initwidget() {
		btn_back_feedback = (ImageButton) this
				.findViewById(R.id.btn_back_feedback);
		btn_back_feedback.setOnClickListener(this);
		et_feed_content = (EditText) this.findViewById(R.id.et_feed_content);
		et_feed_contact = (EditText) this.findViewById(R.id.et_feed_contact);
		btn_submit = (Button) this.findViewById(R.id.btn_submit);
		btn_submit.setOnClickListener(this);
	}

	/**
	 * 按钮的监听事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_feedback:
			this.finish();
			break;
		case R.id.btn_submit:
			if (et_feed_content.getText().toString().trim().isEmpty()) {
				Toast.makeText(this, "请输入您的意见或建议！", Toast.LENGTH_LONG).show();
				return;
			} else {
				String content = et_feed_content.getText().toString().trim();
				String contact = et_feed_contact.getText().toString().trim();
				if (contact.isEmpty()) {
					contact = "";
				}
				Map<String, String> map = new HashMap<String, String>();
				map.put("action_flag", "add");
				map.put("content", content);
				map.put("contact", contact);
				map.put("status", "未阅读");
				String url = HttpUtil.BASE_URL + "servlet/FeedbackServlet";
				upLoadAsyncTask = new UpLoadAsyncTask();
				upLoadAsyncTask.execute(map, url);
				this.finish();
			}
			break;
		}
	}

	/**
	 * 异步任务上传用户求助的时间以及坐标
	 * 
	 * @author wpy
	 * 
	 */
	private class UpLoadAsyncTask extends AsyncTask<Object, Integer, String> {

		@SuppressWarnings("unchecked")
		@Override
		protected String doInBackground(Object... params) {
			Map<String, String> map = (Map<String, String>) params[0];
			String url = (String) params[1];
			String result = null;
			try {
				result = HttpUtil.postRequest(url, map);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("更新成功")) {
				Toast.makeText(FeedBackActivity.this, "已将数据成功上传到服务器",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(FeedBackActivity.this, "上传失败", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}

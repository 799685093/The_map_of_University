package com.wpy.map.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wpy.map.R;
import com.wpy.map.entity.News;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

@SuppressLint("HandlerLeak")
public class NewsDetailActivity extends Activity implements OnClickListener {
	private final int FINISH = 0; // �����̵߳�״̬�Ľ���
	private ViewFlipper mNewsBodyFlipper;// ��Ļ�л��ؼ�
	private LayoutInflater mNewsBodyInflater;
	private float mStartX;// ��ָ���µĿ�ʼλ��
	private ArrayList<HashMap<String, Object>> mNewsData;
	private int mPosition = 0;// �������λ��
	private int mCursor;// ����������ŵ����λ��
	private int mNid;// ���ű��
	private TextView mNewsDetails;// ������ϸ����
	private Button previous;// ��һƪ����
	private Button next;// ��һƪ����
	private ImageButton news_btn_back;// ���ذ�ť
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case FINISH:
				// �ѻ�ȡ����������ʾ��������
				mNewsDetails.setText(Html.fromHtml(msg.obj.toString()));
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsdetail);
		previous = (Button) this
				.findViewById(R.id.newsdetails_title_previous_btn);
		previous.setOnClickListener(this);
		next = (Button) this.findViewById(R.id.newsdetails_title_next_btn);
		next.setOnClickListener(this);
		news_btn_back = (ImageButton) this
				.findViewById(R.id.newsdetail_btn_back);
		news_btn_back.setOnClickListener(this);
		// ��ȡ���ݵ�����
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		// ��ȡ���ż���
		Serializable s = bundle.getSerializable("newsDate");
		mNewsData = (ArrayList<HashMap<String, Object>>) s;
		// ��ȡ���λ��
		mCursor = mPosition = bundle.getInt("position");
		// ��̬����������ͼ������ֵ
		mNewsBodyInflater = getLayoutInflater();
		inflateView(0);
	}

	private void inflateView(int index) {
		// ��̬����������ͼ������ֵ
		View newsBodyLayout = mNewsBodyInflater.inflate(R.layout.news_body,
				null);
		// ��ȡ������Ż�����Ϣ
		HashMap<String, Object> hashMap = mNewsData.get(mPosition);
		// ���ű���
		TextView newsTitle = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_title);
		newsTitle.setText(hashMap.get("newslist_item_title").toString());
		// ����ʱ��ͳ���
		TextView newsSource = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_source);
		newsSource.setText(hashMap.get("newslist_item_source").toString());
		TextView newsTime = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_ptime);
		newsTime.setText(hashMap.get("newslist_item_ptime").toString());
		// ���ű��
		mNid = (Integer) hashMap.get("newslist_item_id");
		// ��������ͼ��ӵ�Flipper��
		mNewsBodyFlipper = (ViewFlipper) findViewById(R.id.news_body_flipper);
		mNewsBodyFlipper.addView(newsBodyLayout, index);

		// ������Body��Ӵ����¼�
		mNewsDetails = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_details);
		mNewsDetails.setOnTouchListener(new NewsBodyOnTouchListener());

		// �����߳�
		new UpdateNewsThread().start();
	}

	private class UpdateNewsThread extends Thread {
		@Override
		public void run() {
			// �������ϻ�ȡ����
			String newsBody = getNewsBody();
			Message msg = mHandler.obtainMessage();
			msg.arg1 = FINISH;
			msg.obj = newsBody;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * ��ȡ������ϸ��Ϣ
	 * 
	 * @return
	 */
	private String getNewsBody() {
		// String retStr = "��������ʧ�ܣ����Ժ�����";
		// ���������ѯ��������
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "getNewsById");
		map.put("id", String.valueOf(mNid));
		String url = HttpUtil.BASE_URL + "servlet/NewsServlet";
		String jsonString = null;
		try {
			jsonString = HttpUtil.postRequest(url, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		News news = JsonUtil.getSimple(
				JsonUtil.getJsonValueByKey(jsonString, "news"), News.class);
		String retStr = news.content;
		return retStr;
	}

	/**
	 * ��ť�ĵ���¼�
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newsdetails_title_previous_btn:
			showPrevious();
			break;
		case R.id.newsdetails_title_next_btn:
			showNext();
			break;
		case R.id.newsdetail_btn_back:
			this.finish();
			break;
		}
	}

	/**
	 * ��������NewsBody�����¼����һ���
	 */
	private class NewsBodyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			// ��ָ����
			case MotionEvent.ACTION_DOWN:
				// ��¼��ʼ����
				mStartX = event.getX();
				break;
			// ��ָ̧��
			case MotionEvent.ACTION_UP:
				// ���󻬶�
				if (event.getX() > mStartX) {
					showPrevious();
				}
				// ���һ���
				else if (event.getX() < mStartX) {
					showNext();
				}
				break;
			}
			return true;
		}
	}

	/**
	 * ��һ������
	 */
	private void showPrevious() {
		if (mPosition > 0) {
			mPosition--;
			// ��¼��ǰ���ű��
			HashMap<String, Object> hashMap = mNewsData.get(mPosition);
			mNid = (Integer) hashMap.get("newslist_item_id");
			if (mCursor > mPosition) {
				mCursor = mPosition;
				inflateView(0);
				mNewsBodyFlipper.showNext();// ��ʾ��һҳ
			}
			mNewsBodyFlipper.setInAnimation(this, R.anim.push_right_in);// ������һҳ����ʱ�Ķ���
			mNewsBodyFlipper.setOutAnimation(this, R.anim.push_right_out);// ���嵱ǰҳ��ȥ�Ķ���
			mNewsBodyFlipper.showPrevious();// ��ʾ��һҳ
		} else {
			Toast.makeText(this, "û����ƪ������", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ��һ������
	 */
	private void showNext() {
		// �ж��Ƿ������һƪwin��
		if (mPosition < mNewsData.size() - 1) {
			// ������һ������
			mNewsBodyFlipper.setInAnimation(this, R.anim.push_left_in);
			mNewsBodyFlipper.setOutAnimation(this, R.anim.push_left_out);
			mPosition++;
			// �ж���һ���Ƿ��Ѿ�����
			if (mPosition >= mNewsBodyFlipper.getChildCount()) {
				inflateView(mNewsBodyFlipper.getChildCount());
			}
			// ��ʾ��һ��
			mNewsBodyFlipper.showNext();
		} else {
			Toast.makeText(this, "û����ƪ������", Toast.LENGTH_SHORT).show();
		}
	}
}

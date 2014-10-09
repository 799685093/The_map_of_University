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
	private final int FINISH = 0; // 代表线程的状态的结束
	private ViewFlipper mNewsBodyFlipper;// 屏幕切换控件
	private LayoutInflater mNewsBodyInflater;
	private float mStartX;// 手指按下的开始位置
	private ArrayList<HashMap<String, Object>> mNewsData;
	private int mPosition = 0;// 点击新闻位置
	private int mCursor;// 用来标记新闻点击的位置
	private int mNid;// 新闻编号
	private TextView mNewsDetails;// 新闻详细内容
	private Button previous;// 上一篇新闻
	private Button next;// 下一篇新闻
	private ImageButton news_btn_back;// 返回按钮
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case FINISH:
				// 把获取到的新闻显示到界面上
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
		// 获取传递的数据
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		// 获取新闻集合
		Serializable s = bundle.getSerializable("newsDate");
		mNewsData = (ArrayList<HashMap<String, Object>>) s;
		// 获取点击位置
		mCursor = mPosition = bundle.getInt("position");
		// 动态创建新闻视图，并赋值
		mNewsBodyInflater = getLayoutInflater();
		inflateView(0);
	}

	private void inflateView(int index) {
		// 动态创建新闻视图，并赋值
		View newsBodyLayout = mNewsBodyInflater.inflate(R.layout.news_body,
				null);
		// 获取点击新闻基本信息
		HashMap<String, Object> hashMap = mNewsData.get(mPosition);
		// 新闻标题
		TextView newsTitle = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_title);
		newsTitle.setText(hashMap.get("newslist_item_title").toString());
		// 发布时间和出处
		TextView newsSource = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_source);
		newsSource.setText(hashMap.get("newslist_item_source").toString());
		TextView newsTime = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_ptime);
		newsTime.setText(hashMap.get("newslist_item_ptime").toString());
		// 新闻编号
		mNid = (Integer) hashMap.get("newslist_item_id");
		// 把新闻视图添加到Flipper中
		mNewsBodyFlipper = (ViewFlipper) findViewById(R.id.news_body_flipper);
		mNewsBodyFlipper.addView(newsBodyLayout, index);

		// 给新闻Body添加触摸事件
		mNewsDetails = (TextView) newsBodyLayout
				.findViewById(R.id.news_body_details);
		mNewsDetails.setOnTouchListener(new NewsBodyOnTouchListener());

		// 启动线程
		new UpdateNewsThread().start();
	}

	private class UpdateNewsThread extends Thread {
		@Override
		public void run() {
			// 从网络上获取新闻
			String newsBody = getNewsBody();
			Message msg = mHandler.obtainMessage();
			msg.arg1 = FINISH;
			msg.obj = newsBody;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 获取新闻详细信息
	 * 
	 * @return
	 */
	private String getNewsBody() {
		// String retStr = "网络连接失败，请稍后再试";
		// 向服务器查询单条数据
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
	 * 按钮的点击事件
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
	 * 处理新闻NewsBody触摸事件左右滑动
	 */
	private class NewsBodyOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			// 手指按下
			case MotionEvent.ACTION_DOWN:
				// 记录起始坐标
				mStartX = event.getX();
				break;
			// 手指抬起
			case MotionEvent.ACTION_UP:
				// 往左滑动
				if (event.getX() > mStartX) {
					showPrevious();
				}
				// 往右滑动
				else if (event.getX() < mStartX) {
					showNext();
				}
				break;
			}
			return true;
		}
	}

	/**
	 * 上一条新闻
	 */
	private void showPrevious() {
		if (mPosition > 0) {
			mPosition--;
			// 记录当前新闻编号
			HashMap<String, Object> hashMap = mNewsData.get(mPosition);
			mNid = (Integer) hashMap.get("newslist_item_id");
			if (mCursor > mPosition) {
				mCursor = mPosition;
				inflateView(0);
				mNewsBodyFlipper.showNext();// 显示下一页
			}
			mNewsBodyFlipper.setInAnimation(this, R.anim.push_right_in);// 定义下一页进来时的动画
			mNewsBodyFlipper.setOutAnimation(this, R.anim.push_right_out);// 定义当前页出去的动画
			mNewsBodyFlipper.showPrevious();// 显示上一页
		} else {
			Toast.makeText(this, "没有上篇新闻了", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 下一条新闻
	 */
	private void showNext() {
		// 判断是否是最后一篇win问
		if (mPosition < mNewsData.size() - 1) {
			// 设置下一屏动画
			mNewsBodyFlipper.setInAnimation(this, R.anim.push_left_in);
			mNewsBodyFlipper.setOutAnimation(this, R.anim.push_left_out);
			mPosition++;
			// 判断下一屏是否已经创建
			if (mPosition >= mNewsBodyFlipper.getChildCount()) {
				inflateView(mNewsBodyFlipper.getChildCount());
			}
			// 显示下一屏
			mNewsBodyFlipper.showNext();
		} else {
			Toast.makeText(this, "没有下篇新闻了", Toast.LENGTH_SHORT).show();
		}
	}
}

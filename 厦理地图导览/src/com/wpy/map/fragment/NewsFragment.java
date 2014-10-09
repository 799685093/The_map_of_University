package com.wpy.map.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.wpy.map.R;
import com.wpy.map.activity.NewsDetailActivity;
import com.wpy.map.entity.News;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

public class NewsFragment extends Fragment implements OnRefreshListener {
	private final int NEWSCOUNT = 6; // 显示新闻的条数
	private final int SUCCESS = 0;// 加载成功
	private final int NONEWS = 1;// 该栏目下没有新闻
	private final int NOMORENEWS = 2;// 该栏目下没有更多新闻
	private final int LOADERROR = 3;// 加载失败
	private ListView mNewslist; // 新闻列表
	private SimpleAdapter mNewslistAdapter; // 为新闻内容提供需要显示的列表
	private ArrayList<HashMap<String, Object>> mNewsData; // 存储新闻信息的数据集合
	// private LayoutInflater mInflater; // 用来动态载入没有loadmore_layout界面
	private Button mLoadmoreButton; // 加载更多按钮
	private LoadNewsAsyncTack mLoadNewsAsyncTack; // 声明LoadNewsAsyncTack引用
	private SwipeRefreshLayout swipeLayout;// 下拉刷新

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("InlinedApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_fragment, null);
		mNewsData = new ArrayList<HashMap<String, Object>>();// 存储新闻信息的数据集合
		mNewslistAdapter = new SimpleAdapter(getActivity(), mNewsData,
				R.layout.list_item_news, new String[] { "newslist_item_title",
						"newslist_item_digest", "newslist_item_source",
						"newslist_item_ptime" }, new int[] {
						R.id.list_item_title, R.id.list_item_digest,
						R.id.list_item_source, R.id.list_item_ptime });
		mNewslist = (ListView) view.findViewById(R.id.news_listview);
		// 下拉刷新
		swipeLayout = (SwipeRefreshLayout) view
				.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);// 定义下拉刷新的监听
		// 定义下拉刷新的颜色
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		View footerView = inflater.inflate(R.layout.list_loadmore, null);
		mNewslist.addFooterView(footerView);// 在LiseView下面添加“加载更多”
		mNewslist.setAdapter(mNewslistAdapter);// 显示列表
		// listview点击跳转
		mNewslist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(),
						NewsDetailActivity.class);
				// 把需要的信息放到Intent中
				intent.putExtra("newsDate", mNewsData);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
		mLoadmoreButton = (Button) footerView.findViewById(R.id.loadmore_btn);
		mLoadmoreButton.setOnClickListener(loadmoreListener);
		// 第一次获取新闻列表
		mLoadNewsAsyncTack = new LoadNewsAsyncTack();
		mLoadNewsAsyncTack.execute(0, true);
		return view;
	}

	/**
	 * 获取新闻类表
	 * 
	 * @param newsList
	 *            保存新闻信息的集合
	 * @param startNid
	 *            分页
	 * @param firstTime
	 *            是否第一次加载
	 * @return
	 */
	private int getNews(List<HashMap<String, Object>> newsList, int startNid,
			boolean firstTime) {
		if (firstTime) {
			// 如果是第一次，则清空集合里数据
			newsList.clear();
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "getAllNews");
		map.put("startNid", String.valueOf(startNid));
		map.put("count", String.valueOf(NEWSCOUNT));
		String url = HttpUtil.BASE_URL + "/servlet/NewsServlet";
		List<News> list = new ArrayList<News>();
		String jsonString = null;
		try {
			jsonString = HttpUtil.postRequest(url, map);
		} catch (Exception e) {
			e.printStackTrace();
			return LOADERROR; // 加载新闻失败
		}
		if (jsonString == null) {
			Toast.makeText(getActivity(), "没有连接服务器，请检测", Toast.LENGTH_LONG)
					.show();
			return NONEWS;
		}
		list = JsonUtil.getMoreList(
				JsonUtil.getJsonValueByKey(jsonString, "news"), News.class);
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				HashMap<String, Object> hashMap = new HashMap<String, Object>();
				News news = list.get(i);
				hashMap.put("newslist_item_id", news.id);
				hashMap.put("newslist_item_title", news.title);
				hashMap.put("newslist_item_digest", news.content);
				hashMap.put("newslist_item_source", news.soure);
				hashMap.put("newslist_item_ptime", news.time);
				hashMap.put("picpath", news.picpath);
				newsList.add(hashMap);
			}
			return SUCCESS;
		} else {
			if (firstTime) {
				return NONEWS;// 没有新闻
			} else {
				return NOMORENEWS; // 没有更多新闻
			}
		}
	}

	/**
	 * 为“加载更多”按钮定义匿名内部类
	 */
	private OnClickListener loadmoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mLoadNewsAsyncTack = new LoadNewsAsyncTack();
			switch (v.getId()) {
			case R.id.loadmore_btn:
				mLoadNewsAsyncTack.execute(mNewsData.size(), false); // 不是第一次加载新闻里列表
				break;
			}
		}
	};

	/**
	 * 异步更新UI
	 * 
	 * @author wpy
	 * 
	 */
	private class LoadNewsAsyncTack extends AsyncTask<Object, Integer, Integer> {
		@Override
		protected void onPreExecute() {
			// 设置LoadMore Button 显示文本
			mLoadmoreButton.setText("正在加载，请稍候...");
		}

		// 在后台运行
		@Override
		protected Integer doInBackground(Object... params) {
			return getNews(mNewsData, (Integer) params[0], (Boolean) params[1]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
			// 该栏目没有新闻
			case NONEWS:
				Toast.makeText(getActivity(), "暂时没有新闻", Toast.LENGTH_LONG)
						.show();
				break;
			case NOMORENEWS:
				Toast.makeText(getActivity(), "没有更多新闻", Toast.LENGTH_LONG)
						.show();
				break;
			case LOADERROR:
				Toast.makeText(getActivity(), "获取新闻失败", Toast.LENGTH_LONG)
						.show();
				break;
			}
			mNewslistAdapter.notifyDataSetChanged(); // 通知ListView更新数据
			// 设置LoadMore Button 显示文本
			mLoadmoreButton.setText("加载更多");
		}
	}

	/**
	 * 下拉刷新的实现
	 */
	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
		mLoadNewsAsyncTack = new LoadNewsAsyncTack();
		mLoadNewsAsyncTack.execute(0, true);
	}
}
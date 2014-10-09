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
	private final int NEWSCOUNT = 6; // ��ʾ���ŵ�����
	private final int SUCCESS = 0;// ���سɹ�
	private final int NONEWS = 1;// ����Ŀ��û������
	private final int NOMORENEWS = 2;// ����Ŀ��û�и�������
	private final int LOADERROR = 3;// ����ʧ��
	private ListView mNewslist; // �����б�
	private SimpleAdapter mNewslistAdapter; // Ϊ���������ṩ��Ҫ��ʾ���б�
	private ArrayList<HashMap<String, Object>> mNewsData; // �洢������Ϣ�����ݼ���
	// private LayoutInflater mInflater; // ������̬����û��loadmore_layout����
	private Button mLoadmoreButton; // ���ظ��ఴť
	private LoadNewsAsyncTack mLoadNewsAsyncTack; // ����LoadNewsAsyncTack����
	private SwipeRefreshLayout swipeLayout;// ����ˢ��

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("InlinedApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.news_fragment, null);
		mNewsData = new ArrayList<HashMap<String, Object>>();// �洢������Ϣ�����ݼ���
		mNewslistAdapter = new SimpleAdapter(getActivity(), mNewsData,
				R.layout.list_item_news, new String[] { "newslist_item_title",
						"newslist_item_digest", "newslist_item_source",
						"newslist_item_ptime" }, new int[] {
						R.id.list_item_title, R.id.list_item_digest,
						R.id.list_item_source, R.id.list_item_ptime });
		mNewslist = (ListView) view.findViewById(R.id.news_listview);
		// ����ˢ��
		swipeLayout = (SwipeRefreshLayout) view
				.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);// ��������ˢ�µļ���
		// ��������ˢ�µ���ɫ
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		View footerView = inflater.inflate(R.layout.list_loadmore, null);
		mNewslist.addFooterView(footerView);// ��LiseView������ӡ����ظ��ࡱ
		mNewslist.setAdapter(mNewslistAdapter);// ��ʾ�б�
		// listview�����ת
		mNewslist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(),
						NewsDetailActivity.class);
				// ����Ҫ����Ϣ�ŵ�Intent��
				intent.putExtra("newsDate", mNewsData);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
		mLoadmoreButton = (Button) footerView.findViewById(R.id.loadmore_btn);
		mLoadmoreButton.setOnClickListener(loadmoreListener);
		// ��һ�λ�ȡ�����б�
		mLoadNewsAsyncTack = new LoadNewsAsyncTack();
		mLoadNewsAsyncTack.execute(0, true);
		return view;
	}

	/**
	 * ��ȡ�������
	 * 
	 * @param newsList
	 *            ����������Ϣ�ļ���
	 * @param startNid
	 *            ��ҳ
	 * @param firstTime
	 *            �Ƿ��һ�μ���
	 * @return
	 */
	private int getNews(List<HashMap<String, Object>> newsList, int startNid,
			boolean firstTime) {
		if (firstTime) {
			// ����ǵ�һ�Σ�����ռ���������
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
			return LOADERROR; // ��������ʧ��
		}
		if (jsonString == null) {
			Toast.makeText(getActivity(), "û�����ӷ�����������", Toast.LENGTH_LONG)
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
				return NONEWS;// û������
			} else {
				return NOMORENEWS; // û�и�������
			}
		}
	}

	/**
	 * Ϊ�����ظ��ࡱ��ť���������ڲ���
	 */
	private OnClickListener loadmoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mLoadNewsAsyncTack = new LoadNewsAsyncTack();
			switch (v.getId()) {
			case R.id.loadmore_btn:
				mLoadNewsAsyncTack.execute(mNewsData.size(), false); // ���ǵ�һ�μ����������б�
				break;
			}
		}
	};

	/**
	 * �첽����UI
	 * 
	 * @author wpy
	 * 
	 */
	private class LoadNewsAsyncTack extends AsyncTask<Object, Integer, Integer> {
		@Override
		protected void onPreExecute() {
			// ����LoadMore Button ��ʾ�ı�
			mLoadmoreButton.setText("���ڼ��أ����Ժ�...");
		}

		// �ں�̨����
		@Override
		protected Integer doInBackground(Object... params) {
			return getNews(mNewsData, (Integer) params[0], (Boolean) params[1]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
			// ����Ŀû������
			case NONEWS:
				Toast.makeText(getActivity(), "��ʱû������", Toast.LENGTH_LONG)
						.show();
				break;
			case NOMORENEWS:
				Toast.makeText(getActivity(), "û�и�������", Toast.LENGTH_LONG)
						.show();
				break;
			case LOADERROR:
				Toast.makeText(getActivity(), "��ȡ����ʧ��", Toast.LENGTH_LONG)
						.show();
				break;
			}
			mNewslistAdapter.notifyDataSetChanged(); // ֪ͨListView��������
			// ����LoadMore Button ��ʾ�ı�
			mLoadmoreButton.setText("���ظ���");
		}
	}

	/**
	 * ����ˢ�µ�ʵ��
	 */
	@Override
	public void onRefresh() {
		swipeLayout.setRefreshing(false);
		mLoadNewsAsyncTack = new LoadNewsAsyncTack();
		mLoadNewsAsyncTack.execute(0, true);
	}
}
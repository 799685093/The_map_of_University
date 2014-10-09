package com.wpy.map.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wpy.map.R;
import com.wpy.map.activity.IntroDetailActivity;
import com.wpy.map.entity.Place;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

public class QueryFragment extends Fragment implements OnClickListener {

	private EditText site;
	private RelativeLayout btn_search;
	private ListView listView;
	private List<HashMap<String, Object>> list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.query_fragment, null);
		site = (EditText) view.findViewById(R.id.site);
		btn_search = (RelativeLayout) view.findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);
		listView = (ListView) view.findViewById(R.id.query_listview);
		list = new ArrayList<HashMap<String, Object>>();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(),
						IntroDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("placename",
						list.get(position).get("placename").toString());
				intent.putExtras(bundle);
				startActivity(intent);
				// Toast.makeText(getActivity(), list.toString(),
				// Toast.LENGTH_LONG).show();
			}
		});
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search:
			QueryAsync queryAsync = new QueryAsync();
			queryAsync.execute();
			break;
		}
	}

	private class QueryAsync extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			search();
			return null;
		}

	}

	private void search() {
		String placename = site.getText().toString().trim();
		if (placename.length() == 0) {
			Toast.makeText(getActivity(), "请输入要查询的地点！", Toast.LENGTH_LONG)
					.show();
			return;
		}
		// 向服务器查询单条数据
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "queryPlaceByName");
		map.put("placename", placename);
		String url = HttpUtil.BASE_URL + "servlet/PlaceServlet";
		String jsonString = null;
		Place place = null;
		List<Place> placelist = new ArrayList<Place>();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		try {
			jsonString = HttpUtil.postRequest(url, map);
		} catch (Exception e) {
			e.printStackTrace();
			/**
			 * 添加对话框，提示提示错误信息
			 */
			// AlertDialog.Builder builder = new
			// AlertDialog.Builder(getActivity());
		}
		if (jsonString.equals("Nothing")) {
			Toast.makeText(getActivity(), "查无此地点", Toast.LENGTH_LONG).show();
			return;
		} else {
			placelist = JsonUtil.getMoreList(
					JsonUtil.getJsonValueByKey(jsonString, "place"),
					Place.class);
			list.clear();
			for (int i = 0; i < placelist.size(); i++) {
				place = placelist.get(i);
				hashMap.put("placename", place.placename);
				// hashMap.put("placecontent", place.content);
				list.add(hashMap);
			}
			SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(),
					list, R.layout.item_list, new String[] { "placename" },
					new int[] { R.id.name });
			listView.setAdapter(simpleAdapter);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		site.setText("");
	}
}
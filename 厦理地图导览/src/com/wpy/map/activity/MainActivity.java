package com.wpy.map.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.wpy.map.R;
import com.wpy.map.common.MyApplication;
import com.wpy.map.fragment.HomeFragment;
import com.wpy.map.fragment.MoreFragment;
import com.wpy.map.fragment.NewsFragment;
import com.wpy.map.fragment.QueryFragment;

/**
 * @author wpy �����������Զ���TabHost
 */
public class MainActivity extends FragmentActivity {
	// ����FragmentTabHost����
	private FragmentTabHost mTabHost;

	// ����һ������
	private LayoutInflater layoutInflater;

	// �������������Fragment����
	@SuppressWarnings("rawtypes")
	private Class fragmentArray[] = { HomeFragment.class, NewsFragment.class,
			QueryFragment.class, MoreFragment.class };

	// ������������Ű�ťͼƬ
	private int mImageViewArray[] = { R.drawable.tab_home_btn,
			R.drawable.tab_news_btn, R.drawable.tab_query_btn,
			R.drawable.tab_more_btn };

	// Tabѡ�������
	private String mTextviewArray[] = { "��ҳ", "����", "��ѯ", "����" };

	@SuppressWarnings("unused")
	private boolean mIsEngineInitSuccess = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_layout);
		// ��ʼ����������
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, MyApplication.baidukey,
				mKeyVerifyListener);
		initView();
	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			// ������ʼ�����첽�ģ���ҪһС��ʱ�䣬�������־��ʶ�������Ƿ��ʼ���ɹ���Ϊtrueʱ����ܷ��𵼺�
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};
	private BNKeyVerifyListener mKeyVerifyListener = new BNKeyVerifyListener() {

		@Override
		public void onVerifySucc() {
			Toast.makeText(MainActivity.this, "keyУ��ɹ�", Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onVerifyFailed(int arg0, String arg1) {
			Toast.makeText(MainActivity.this, "keyУ��ʧ��", Toast.LENGTH_LONG)
					.show();
		}
	};

	/**
	 * ��ʼ�����
	 */
	private void initView() {
		// ʵ�������ֶ���
		layoutInflater = LayoutInflater.from(this);

		// �õ�TabHost����
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		// ��ʼ��TabHost
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		// �õ�fragment�ĸ���
		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			// Ϊÿһ��Tab��ť����ͼ�ꡢ���ֺ�����
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i])
					.setIndicator(getTabItemView(i));
			// ��Tab��ť��ӽ�Tabѡ���
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			// ����Tab��ť�ı���
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}
	}

	/**
	 * ��Tab��ť����ͼ�������
	 */
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);

		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);

		return view;
	}
}

package com.wpy.map.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.util.common.CoordinateTransformUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.wpy.map.R;
import com.wpy.map.common.ChangeGeoPoint;
import com.wpy.map.entity.Place;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

/**
 * У԰�����Ľ���ҳ��
 * 
 * @author wpy
 * 
 */
public class IntroDetailActivity extends Activity implements OnClickListener {

	private ViewPager viewPager;// android-support-v4�еĻ������

	private List<View> dots;// ͼƬ�������ĵ���ЩԲ��

	private TextView tv_title;// ҳ��ı�����������
	private TextView tv_image_title;// ͼƬ�ı���
	private int currenItem = 0;// ��ǰͼƬ��������/��ǰ��ʾView��λ��

	private TextView tv_intro;// ���������ֽ���

	private ImageButton btn_back;
	private Button btn_gohere;

	private Place place = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();// ʵ����һ��imageload����
	DisplayImageOptions options;// ͼƬ�Ĳ������ö���
	private static final String STATE_POSITION = "STATE_POSITION";

	private String[] imagesUrl;// ͼƬ�ĵ�ַ
	private String[] imagesTitle;// ͼƬ�ı���

	private ScheduledExecutorService scheduledExecutorService;// �ṩ�˰�ʱ�䰲��ִ������Ĺ���

	// ��λ���
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();
	private GeoPoint mylocationPoint;
	private GeoPoint placeygeoPoint;

	/**
	 * �л���ǰ��ʾ��ͼƬ
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			viewPager.setCurrentItem(currenItem);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_introdetail);
		// �õ�IntroActivity��������ֵ
		String placename = getIntent().getExtras().getString("placename");
		// ���ñ�����
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		tv_title.setText(placename);

		// ��ʼ����ť������Ӽ����¼�
		btn_back = (ImageButton) this.findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_gohere = (Button) this.findViewById(R.id.btn_gohere);
		btn_gohere.setOnClickListener(this);

		// ���������ѯ��������
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "findSimple");
		map.put("placename", placename);
		String url = HttpUtil.BASE_URL + "servlet/PlaceServlet";
		try {
			String jsonString = HttpUtil.postRequest(url, map);
			place = JsonUtil.getSimple(
					JsonUtil.getJsonValueByKey(jsonString, "place"),
					Place.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ȡ��Place�е�����
		String placeContent = place.content;
		String placeImagesUrls = place.picpath;
		String placeImageTitles = place.pictitle;
		placeygeoPoint = ChangeGeoPoint.StringToGeoPoint(place.coordinate);
		imagesTitle = placeImageTitles.split(";");
		String[] picPath = placeImagesUrls.split(";");
		imagesUrl = new String[picPath.length];
		// ���ͼƬ��·��
		for (int i = 0; i < picPath.length; i++) {
			imagesUrl[i] = HttpUtil.BASE_URL + picPath[i];
		}

		// ���ͼƬ�·���С��
		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.v_dot0));
		dots.add(findViewById(R.id.v_dot1));
		dots.add(findViewById(R.id.v_dot2));
		dots.add(findViewById(R.id.v_dot3));
		dots.add(findViewById(R.id.v_dot4));

		tv_image_title = (TextView) findViewById(R.id.tv_image_title);
		tv_image_title.setText(imagesTitle[0]);// Ĭ������ͼƬ�ı���Ϊ��һ��

		tv_intro = (TextView) findViewById(R.id.tv_intro);
		tv_intro.setText(placeContent);
		// ���֮ǰ�б����û�����
		if (savedInstanceState != null) {
			currenItem = savedInstanceState.getInt(STATE_POSITION);
		}
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				// ͼƬ����ʧ��ʱ�õĵ�ͼƬ
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();
		viewPager = (ViewPager) findViewById(R.id.vp);
		// �������viewPagerҳ���������
		viewPager.setAdapter(new MyViewPagerAdapter(imagesUrl));
		// ����һ������������viewPager�е�ҳ��ı�ʱ����
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
		viewPager.setCurrentItem(currenItem); // ��ʾ��ǰλ�õ�View

		mLocationClient = new LocationClient(getApplicationContext());// ����LocationClient��
		mLocationClient.registerLocationListener(mBdLocationListener);// ע���������

		setLocationOption();
		mLocationClient.start();// ��ʼ��λ
		if (mLocationClient != null && mLocationClient.isStarted()) {
			// ����λ��������������첽�ģ���λ����ڼ�������onReceiveLocation�л�ȡ��
			mLocationClient.requestLocation();
		} else {
			Log.d("RouteGuideActivity", "locClient is null or not started");
		}
	}

	/**
	 * ���ö�λ����ز���
	 */
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// ���ö�λģʽ �߾��ȶ�λ �ĵ�
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȵľ�γ�ȣ�Ĭ��ֵgcj02
		option.setScanSpan(10000);// ����λ����ļ��ʱ��Ϊ10000ms
		option.disableCache(true);// ��ֹ���û��涨λ
		option.setIsNeedAddress(true);// ���صĶ�λ���������ַ��Ϣ
		mLocationClient.setLocOption(option);
	}

	/**
	 * ʵ��BDLocationListener�ӿ� ���ж�λ
	 * 
	 * @author wpy
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		/**
		 * �����첽���صĶ�λ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			mylocationPoint = new GeoPoint(
					(int) (location.getLatitude() * 1e6),
					(int) (location.getLongitude() * 1e6));
		}

		/**
		 * �����첽���ص�POI��ѯ�����������BDLocation���Ͳ���
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// �����û�����
		outState.putInt(STATE_POSITION, viewPager.getCurrentItem());
	}

	@Override
	protected void onStart() {
		// ����һ�������̳߳�
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		// ��Activity��ʾ������ÿ�������л�һ��ͼƬ��ʾ
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 5,
				TimeUnit.SECONDS);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// ��Activity���ɼ�ʱֹͣ�л�
		scheduledExecutorService.shutdown();
		super.onStop();
	}

	/**
	 * �����л�����
	 * 
	 * @author wpy
	 * 
	 */
	private class ScrollTask implements Runnable {
		@Override
		public void run() {
			synchronized (viewPager) {
				System.out.println("currentItem: " + currenItem);
				currenItem = (currenItem + 1) % imagesUrl.length;
				handler.obtainMessage().sendToTarget(); // ͨ��Handler�л�ͼƬ
			}
		}

	}

	/**
	 * ��viewPagerҳ���е�״̬�����ı�ʱ����
	 * 
	 * @author wpy
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		/**
		 * �˷�������״̬�ı��ʱ�����
		 */
		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		/**
		 * ��ҳ���ڻ�����ʱ�����ô˷���
		 */
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		/**
		 * �˷�����ҳ����ת���õ����ã�position���㵱ǰѡ�е�ҳ���Position��λ�ñ�ţ�
		 */
		@Override
		public void onPageSelected(int position) {
			currenItem = position;
			tv_image_title.setText(imagesTitle[position]);
			dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
			oldPosition = position;
		}

	}

	/**
	 * ���viewPagerҳ���������
	 * 
	 * @author wpy
	 * 
	 */
	private class MyViewPagerAdapter extends PagerAdapter {
		private String[] images;
		private LayoutInflater inflater;

		public MyViewPagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		/**
		 * ����������ǻ�ȡ��ǰ���������
		 */
		@Override
		public int getCount() {
			return images.length;
		}

		/**
		 * ���������returnһ������������������PagerAdapter������ѡ���Ă����� ���ڵ�ǰ��ViewPager��
		 */
		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_viewpager_image,
					view, false);
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);
			imageLoader.displayImage(images[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) { // ��ȡͼƬʧ������
							case IO_ERROR: // �ļ�I/O����
								message = "Input/Output error";
								break;
							case DECODING_ERROR: // �������
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED: // �����ӳ�
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY: // �ڴ治��
								message = "Out Of Memory error";
								break;
							case UNKNOWN: // ԭ����
								message = "Unknown error";
								break;
							}
							Toast.makeText(IntroDetailActivity.this, message,
									Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE); // ����ʾԲ�ν�����
						}
					});

			((ViewPager) view).addView(imageLayout, 0); // ��ͼƬ���ӵ�ViewPager
			return imageLayout;
		}

		/**
		 * ������������Ƴ���ǰ��view
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		/**
		 * ��������������ж��Ƿ��ɶ������ɽ���
		 */
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}

	}

	/**
	 * �ؼ������¼���ʵ��
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_gohere:
			launchNavigator(mylocationPoint, placeygeoPoint);
			break;
		}
	}

	/**
	 * ��������յ�ľ�γ����Ϣ�������Ϣ������������
	 */
	public void launchNavigator(GeoPoint pointStart, GeoPoint pointEnd) {
		// ����ת�������ٶȵ�ͼ���õ�bd09����ת��Ϊgcj02����
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJStart = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointStart.getLongitudeE6() / 1e6),
						(Double) (pointStart.getLatitudeE6() / 1e6));
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJend = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointEnd.getLongitudeE6() / 1e6),
						(Double) (pointEnd.getLatitudeE6() / 1e6));
		// ��������
		BaiduNaviManager.getInstance().launchNavigator(
				this,
				ptGCJStart.getLatitudeE6() / 1e5,// �������1e5����Ϊgcj02��������С�����5λ�������ǵ������6λ��
				ptGCJStart.getLongitudeE6() / 1e5, "",
				ptGCJend.getLatitudeE6() / 1e5,
				ptGCJend.getLongitudeE6() / 1e5, "",
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // ��·��ʽ
				false, // true��ʵ���� falseģ�⵼��
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // �����߲���
				new OnStartNavigationListener() { // ��ת����

					@Override
					public void onJumpToNavigator(Bundle configParams) {
						Intent intent = new Intent(IntroDetailActivity.this,
								BNavigatorActivity.class);
						intent.putExtras(configParams);
						startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
					}
				});
	}
}

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
 * 校园场所的介绍页面
 * 
 * @author wpy
 * 
 */
public class IntroDetailActivity extends Activity implements OnClickListener {

	private ViewPager viewPager;// android-support-v4中的滑动组件

	private List<View> dots;// 图片标题正文的那些圆点

	private TextView tv_title;// 页面的标题栏的文字
	private TextView tv_image_title;// 图片的标题
	private int currenItem = 0;// 当前图片的索引号/当前显示View的位置

	private TextView tv_intro;// 场所的文字介绍

	private ImageButton btn_back;
	private Button btn_gohere;

	private Place place = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();// 实例化一个imageload对象
	DisplayImageOptions options;// 图片的参数配置对象
	private static final String STATE_POSITION = "STATE_POSITION";

	private String[] imagesUrl;// 图片的地址
	private String[] imagesTitle;// 图片的标题

	private ScheduledExecutorService scheduledExecutorService;// 提供了按时间安排执行任务的功能

	// 定位相关
	private LocationClient mLocationClient = null;
	private BDLocationListener mBdLocationListener = new MyLocationListener();
	private GeoPoint mylocationPoint;
	private GeoPoint placeygeoPoint;

	/**
	 * 切换当前显示的图片
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
		// 得到IntroActivity传过来的值
		String placename = getIntent().getExtras().getString("placename");
		// 设置标题栏
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		tv_title.setText(placename);

		// 初始化按钮，并添加监听事件
		btn_back = (ImageButton) this.findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_gohere = (Button) this.findViewById(R.id.btn_gohere);
		btn_gohere.setOnClickListener(this);

		// 向服务器查询单条数据
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

		// 取出Place中的数据
		String placeContent = place.content;
		String placeImagesUrls = place.picpath;
		String placeImageTitles = place.pictitle;
		placeygeoPoint = ChangeGeoPoint.StringToGeoPoint(place.coordinate);
		imagesTitle = placeImageTitles.split(";");
		String[] picPath = placeImagesUrls.split(";");
		imagesUrl = new String[picPath.length];
		// 添加图片的路径
		for (int i = 0; i < picPath.length; i++) {
			imagesUrl[i] = HttpUtil.BASE_URL + picPath[i];
		}

		// 添加图片下方的小点
		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.v_dot0));
		dots.add(findViewById(R.id.v_dot1));
		dots.add(findViewById(R.id.v_dot2));
		dots.add(findViewById(R.id.v_dot3));
		dots.add(findViewById(R.id.v_dot4));

		tv_image_title = (TextView) findViewById(R.id.tv_image_title);
		tv_image_title.setText(imagesTitle[0]);// 默认设置图片的标题为第一个

		tv_intro = (TextView) findViewById(R.id.tv_intro);
		tv_intro.setText(placeContent);
		// 如果之前有保存用户数据
		if (savedInstanceState != null) {
			currenItem = savedInstanceState.getInt(STATE_POSITION);
		}
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				// 图片加载失败时用的的图片
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();
		viewPager = (ViewPager) findViewById(R.id.vp);
		// 设置填充viewPager页面的适配器
		viewPager.setAdapter(new MyViewPagerAdapter(imagesUrl));
		// 设置一个监听器，当viewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
		viewPager.setCurrentItem(currenItem); // 显示当前位置的View

		mLocationClient = new LocationClient(getApplicationContext());// 声明LocationClient类
		mLocationClient.registerLocationListener(mBdLocationListener);// 注册监听函数

		setLocationOption();
		mLocationClient.start();// 开始定位
		if (mLocationClient != null && mLocationClient.isStarted()) {
			// 发起定位请求。请求过程是异步的，定位结果在监听函数onReceiveLocation中获取。
			mLocationClient.requestLocation();
		} else {
			Log.d("RouteGuideActivity", "locClient is null or not started");
		}
	}

	/**
	 * 设置定位的相关参数
	 */
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式 高精度定位 耗电
		option.setCoorType("bd09ll");// 返回的定位结果是百度的经纬度，默认值gcj02
		option.setScanSpan(10000);// 发起定位请求的间隔时间为10000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		mLocationClient.setLocOption(option);
	}

	/**
	 * 实现BDLocationListener接口 进行定位
	 * 
	 * @author wpy
	 * 
	 */
	public class MyLocationListener implements BDLocationListener {

		/**
		 * 接收异步返回的定位结果，参数是BDLocation类型参数
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
		 * 接收异步返回的POI查询结果，参数是BDLocation类型参数
		 */
		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// 保存用户数据
		outState.putInt(STATE_POSITION, viewPager.getCurrentItem());
	}

	@Override
	protected void onStart() {
		// 建立一个调度线程池
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		// 当Activity显示出来后，每两秒钟切换一次图片显示
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 5,
				TimeUnit.SECONDS);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 当Activity不可见时停止切换
		scheduledExecutorService.shutdown();
		super.onStop();
	}

	/**
	 * 换行切换任务
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
				handler.obtainMessage().sendToTarget(); // 通过Handler切换图片
			}
		}

	}

	/**
	 * 当viewPager页面中的状态发生改变时调用
	 * 
	 * @author wpy
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		/**
		 * 此方法是在状态改变的时候调用
		 */
		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		/**
		 * 当页面在滑动的时候会调用此方法
		 */
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		/**
		 * 此方法是页面跳转完后得到调用，position是你当前选中的页面的Position（位置编号）
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
	 * 填充viewPager页面的适配器
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
		 * 这个方法，是获取当前窗体界面数
		 */
		@Override
		public int getCount() {
			return images.length;
		}

		/**
		 * 这个方法，return一個对象，这个对象表明了PagerAdapter适配器选择哪個对象 放在当前的ViewPager中
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
							switch (failReason.getType()) { // 获取图片失败类型
							case IO_ERROR: // 文件I/O错误
								message = "Input/Output error";
								break;
							case DECODING_ERROR: // 解码错误
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED: // 网络延迟
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY: // 内存不足
								message = "Out Of Memory error";
								break;
							case UNKNOWN: // 原因不明
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
							spinner.setVisibility(View.GONE); // 不显示圆形进度条
						}
					});

			((ViewPager) view).addView(imageLayout, 0); // 将图片增加到ViewPager
			return imageLayout;
		}

		/**
		 * 这个方法，是移出当前的view
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		/**
		 * 这个方法，用于判断是否由对象生成界面
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
	 * 控件监听事件的实现
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
	 * 将起点与终点的经纬度信息等相关信息传给导航界面
	 */
	public void launchNavigator(GeoPoint pointStart, GeoPoint pointEnd) {
		// 坐标转换，将百度地图所用的bd09坐标转换为gcj02坐标
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJStart = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointStart.getLongitudeE6() / 1e6),
						(Double) (pointStart.getLatitudeE6() / 1e6));
		com.baidu.nplatform.comapi.basestruct.GeoPoint ptGCJend = CoordinateTransformUtil
				.transferBD09ToGCJ02(
						(Double) (pointEnd.getLongitudeE6() / 1e6),
						(Double) (pointEnd.getLatitudeE6() / 1e6));
		// 启动导航
		BaiduNaviManager.getInstance().launchNavigator(
				this,
				ptGCJStart.getLatitudeE6() / 1e5,// 必须除以1e5，因为gcj02的坐标是小数点后5位，而我们的坐标的6位的
				ptGCJStart.getLongitudeE6() / 1e5, "",
				ptGCJend.getLatitudeE6() / 1e5,
				ptGCJend.getLongitudeE6() / 1e5, "",
				NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, // 算路方式
				false, // true真实导航 false模拟导航
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, // 在离线策略
				new OnStartNavigationListener() { // 跳转监听

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

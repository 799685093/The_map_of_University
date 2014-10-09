package com.wpy.map.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wpy.map.R;
import com.wpy.map.activity.IntroActivity;
import com.wpy.map.activity.IntroActivityMapActivity;
import com.wpy.map.activity.RecourseActivity;
import com.wpy.map.activity.RouteGuideActivity;
import com.wpy.map.activity.StreetViewActivity;
import com.wpy.map.view.TurnplateView;
import com.wpy.map.view.TurnplateView.OnTurnplateListener;

public class HomeFragment extends Fragment implements OnTurnplateListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/**
		 * 获取屏幕的分辨率
		 */
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);

		/**
		 * 设置主页面的背景图片
		 */
		getActivity().getWindow().setBackgroundDrawableResource(
				R.drawable.index_bg);
		/**
		 * 获取屏幕的宽和高
		 */
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;
		TurnplateView turnplateView = new TurnplateView(getActivity(),
				width / 2, height / 2, width / 3);
		turnplateView.setOnTurnplateListener(this);
		return turnplateView;
	}

	@Override
	public void onPointTouch(int flag) {
		Intent intent = new Intent();
		switch (flag) {
		case 0:
			// 校园介绍
			intent.setClass(getActivity(), IntroActivity.class);
			startActivity(intent);
			break;
		case 1:
			// 校园活动
			intent.setClass(getActivity(), IntroActivityMapActivity.class);
			startActivity(intent);
			break;
		case 2:
			// 导航
			intent.setClass(getActivity(), RouteGuideActivity.class);
			startActivity(intent);
			break;
		case 3:
			// 校园求助
			intent.setClass(getActivity(), RecourseActivity.class);
			startActivity(intent);
			break;
		case 4:
			// 分享
			Intent intentshare = new Intent(Intent.ACTION_SEND);// 启动分享发送的属性
			intentshare.setType("text/plain");// 分享发送的数据类型
			intentshare.putExtra(Intent.EXTRA_SUBJECT, "与你分享");// 分享 主题
			intentshare.putExtra(Intent.EXTRA_TEXT, "我想跟你分享这一款软件。。。");// 分享内容
			startActivity(Intent.createChooser(intentshare, "分享"));// 目标应用选择对话框的标题
			break;
		case 5:
			// 街景
			intent.setClass(getActivity(), StreetViewActivity.class);
			startActivity(intent);
			break;
		}
	}
}

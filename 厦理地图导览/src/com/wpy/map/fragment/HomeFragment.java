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
		 * ��ȡ��Ļ�ķֱ���
		 */
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);

		/**
		 * ������ҳ��ı���ͼƬ
		 */
		getActivity().getWindow().setBackgroundDrawableResource(
				R.drawable.index_bg);
		/**
		 * ��ȡ��Ļ�Ŀ�͸�
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
			// У԰����
			intent.setClass(getActivity(), IntroActivity.class);
			startActivity(intent);
			break;
		case 1:
			// У԰�
			intent.setClass(getActivity(), IntroActivityMapActivity.class);
			startActivity(intent);
			break;
		case 2:
			// ����
			intent.setClass(getActivity(), RouteGuideActivity.class);
			startActivity(intent);
			break;
		case 3:
			// У԰����
			intent.setClass(getActivity(), RecourseActivity.class);
			startActivity(intent);
			break;
		case 4:
			// ����
			Intent intentshare = new Intent(Intent.ACTION_SEND);// ���������͵�����
			intentshare.setType("text/plain");// �����͵���������
			intentshare.putExtra(Intent.EXTRA_SUBJECT, "�������");// ���� ����
			intentshare.putExtra(Intent.EXTRA_TEXT, "������������һ�����������");// ��������
			startActivity(Intent.createChooser(intentshare, "����"));// Ŀ��Ӧ��ѡ��Ի���ı���
			break;
		case 5:
			// �־�
			intent.setClass(getActivity(), StreetViewActivity.class);
			startActivity(intent);
			break;
		}
	}
}

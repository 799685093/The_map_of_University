package com.wpy.map.fragment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.Toast;

import com.wpy.map.R;
import com.wpy.map.activity.AboutActivity;
import com.wpy.map.activity.FeedBackActivity;
import com.wpy.map.activity.GuideViewActivity;
import com.wpy.map.entity.UpdataInfo;
import com.wpy.map.util.DownLoadUtil;
import com.wpy.map.util.HttpUtil;
import com.wpy.map.util.JsonUtil;

public class MoreFragment extends Fragment implements OnClickListener {

	private UpdataInfo updataInfo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.more_fragment, null);
		TableRow intro_fun = (TableRow) view
				.findViewById(R.id.function_Introduction);
		intro_fun.setOnClickListener(this);

		TableRow updates = (TableRow) view.findViewById(R.id.updates);
		updates.setOnClickListener(this);

		TableRow feedback = (TableRow) view.findViewById(R.id.feedback);
		feedback.setOnClickListener(this);

		TableRow about_us = (TableRow) view.findViewById(R.id.about_us);
		about_us.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.function_Introduction:
			// 功能介绍
			intent.setClass(getActivity(), GuideViewActivity.class);
			startActivity(intent);
			break;
		case R.id.updates:
			// 检测更新
			CheckSync checkSync = new CheckSync();
			checkSync.execute();
			break;
		case R.id.feedback:
			// 反馈
			intent.setClass(getActivity(), FeedBackActivity.class);
			startActivity(intent);
			break;
		case R.id.about_us:
			// 关于
			intent.setClass(getActivity(), AboutActivity.class);
			startActivity(intent);
			break;
		}
	}

	private class CheckSync extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			checkUpda();
			return null;
		}

	}

	/**
	 * 检测是否进行更新
	 */
	private void checkUpda() {
		// 得到当前的版本号
		PackageManager manager = getActivity().getPackageManager();
		int versioncode = 0;
		try {
			PackageInfo info = manager.getPackageInfo(getActivity()
					.getPackageName(), 0);
			versioncode = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		// 获取服务器的版本号
		updataInfo = null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("action_flag", "getupdainfo");
		String url = HttpUtil.BASE_URL + "servlet/UpdataInfoServlet";
		try {
			String jsonString = HttpUtil.postRequest(url, map);
			if (jsonString != null) {
				updataInfo = JsonUtil.getSimple(
						JsonUtil.getJsonValueByKey(jsonString, "updatainfo"),
						UpdataInfo.class);
				// 比较版本号，判断是否更新
				if (versioncode != 0 && versioncode < updataInfo.versioncode) {
					showUpdateDialog();
				} else {
					Toast.makeText(getActivity(), "当前的版本已是最新的",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), "网络连接异常", Toast.LENGTH_SHORT)
						.show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 显示更新的对话框
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("检测到新版本");
		builder.setMessage("是否下载更新?");
		builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadApk();
			}

		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	/**
	 * 从服务器下载APK
	 */
	private void downloadApk() {
		final ProgressDialog pd; // 进度条对话框
		pd = new ProgressDialog(getActivity());
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File dir = new File(Environment.getExternalStorageDirectory(),
					"/download/update");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String apkPath = Environment.getExternalStorageDirectory()
					+ "/download/update/" + updataInfo.versionname + ".apk";
			UpdateTask task = new UpdateTask(updataInfo.apkurl, apkPath, pd);
			pd.show();
			new Thread(task).start();
		} else {
			Toast.makeText(getActivity(), "SD卡不可用，请插入SD卡", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 下载的线程
	 * 
	 */
	class UpdateTask implements Runnable {
		private String path;
		private String filePath;
		private ProgressDialog progressDialog;

		public UpdateTask(String path, String filePath, ProgressDialog pd) {
			this.path = path;
			this.filePath = filePath;
		}

		@Override
		public void run() {
			try {
				File file = DownLoadUtil
						.getFile(path, filePath, progressDialog);
				progressDialog.dismiss();
				install(file);
			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();
				Toast.makeText(getActivity(), "更新失败", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * 安装apk
	 * 
	 * @param file
	 *            要安装的apk的目录
	 */
	private void install(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		getActivity().finish();
		startActivity(intent);
	}
}
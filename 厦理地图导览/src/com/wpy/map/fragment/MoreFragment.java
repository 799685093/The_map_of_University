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
			// ���ܽ���
			intent.setClass(getActivity(), GuideViewActivity.class);
			startActivity(intent);
			break;
		case R.id.updates:
			// ������
			CheckSync checkSync = new CheckSync();
			checkSync.execute();
			break;
		case R.id.feedback:
			// ����
			intent.setClass(getActivity(), FeedBackActivity.class);
			startActivity(intent);
			break;
		case R.id.about_us:
			// ����
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
	 * ����Ƿ���и���
	 */
	private void checkUpda() {
		// �õ���ǰ�İ汾��
		PackageManager manager = getActivity().getPackageManager();
		int versioncode = 0;
		try {
			PackageInfo info = manager.getPackageInfo(getActivity()
					.getPackageName(), 0);
			versioncode = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		// ��ȡ�������İ汾��
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
				// �Ƚϰ汾�ţ��ж��Ƿ����
				if (versioncode != 0 && versioncode < updataInfo.versioncode) {
					showUpdateDialog();
				} else {
					Toast.makeText(getActivity(), "��ǰ�İ汾�������µ�",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), "���������쳣", Toast.LENGTH_SHORT)
						.show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ��ʾ���µĶԻ���
	 */
	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("��⵽�°汾");
		builder.setMessage("�Ƿ����ظ���?");
		builder.setPositiveButton("����", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadApk();
			}

		}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	/**
	 * �ӷ���������APK
	 */
	private void downloadApk() {
		final ProgressDialog pd; // �������Ի���
		pd = new ProgressDialog(getActivity());
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("�������ظ���");
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
			Toast.makeText(getActivity(), "SD�������ã������SD��", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * ���ص��߳�
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
				Toast.makeText(getActivity(), "����ʧ��", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * ��װapk
	 * 
	 * @param file
	 *            Ҫ��װ��apk��Ŀ¼
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
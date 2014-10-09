package com.wpy.map.view;

import com.wpy.map.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TurnplateView extends View implements OnTouchListener {

	private OnTurnplateListener onTurnplateListener;

	public void setOnTurnplateListener(OnTurnplateListener onTurnplateListener) {
		this.onTurnplateListener = onTurnplateListener;
	}

	/**
	 * 画笔：点、线
	 */
	private Paint mPaint = new Paint();
	/**
	 * 画笔：圆
	 */
	private Paint paintCircle = new Paint();
	/**
	 * 图标列表
	 */
	private Bitmap[] icons = new Bitmap[10];
	/**
	 * point列表
	 */
	private Point[] points;
	/**
	 * 数目
	 */
	private static final int PONIT_NUM = 6;

	/**
	 * 圆心坐标
	 */
	private int mPointX = 0, mPointY = 0;
	/**
	 * 半径
	 */
	private int mRadius = 0;
	/**
	 * 每两个点间隔的角度
	 */
	private int mDegreeDelta;
	/**
	 * 每次转动的角度差
	 */
	private int tempDegree = 0;
	/**
	 * 选中的图标标识 999：未选中任何图标
	 */
	private int chooseBtn = 999;
	/**
	 * Matrix矩阵由 9 个 float 值构成，是一个 3*3 的矩阵
	 * Matrix的操作，总共分为translate(平移)，rotate(旋转)，scale(缩放)和skew(倾斜)四种，
	 */
	private Matrix mMatrix = new Matrix();

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            上下文
	 * @param px
	 *            宽度
	 * @param py
	 *            长度
	 * @param radius
	 *            半径
	 */
	public TurnplateView(Context context, int px, int py, int radius) {
		super(context);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);// 设置线的宽度
		paintCircle.setAntiAlias(true);// 设置为无锯齿
		paintCircle.setColor(Color.WHITE);
		loadIcons();// 获取图片
		mPointX = px;
		mPointY = py;
		mRadius = radius;
		initPoints();// 初始化每一个点
		computeCoordinates();// 计算每个点的坐标

	}

	/**
	 * 重写屏幕点击事件处理方法dispatchTouchEvent
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			resetPointAngle(event.getX(), event.getY());// 重新计算每一个点的角度
			computeCoordinates();
			invalidate();// 刷新view，触发onDraw方法
			break;
		case MotionEvent.ACTION_UP:
			switchScreen(event);
			tempDegree = 0;
			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
			// 系统在运行到一定程度下无法继续响应你的后续动作时会产生此事件。
			// 一般仅在代码中将其视为异常分支情况处理
			break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap bitmap = ((BitmapDrawable) (getResources()
				.getDrawable(R.drawable.index_middle_bg))).getBitmap();
		canvas.drawBitmap(bitmap, mPointX - bitmap.getWidth() / 2, mPointY
				- bitmap.getHeight() / 2, null);
		for (int index = 0; index < PONIT_NUM; index++) {
			drawInCenter(canvas, points[index].bitmap, points[index].x,
					points[index].y, points[index].flag);
		}
	}

	/**
	 * 把点放到图片中心处
	 * 
	 * @param canvas
	 * @param bitmap
	 * @param left
	 * @param top
	 * @param flag
	 */
	private void drawInCenter(Canvas canvas, Bitmap bitmap, float left,
			float top, int flag) {
		canvas.drawPoint(left, top, mPaint);
		if (chooseBtn == flag) {
			mMatrix.setScale(70f / bitmap.getWidth(), 70f / bitmap.getHeight());
			mMatrix.postTranslate(left - 35, top - 35);
			canvas.drawBitmap(bitmap, mMatrix, null);
		} else {
			canvas.drawBitmap(bitmap, left - bitmap.getWidth() / 2, top
					- bitmap.getHeight() / 2, null);
		}
	}

	/**
	 * 装载图片
	 * 
	 * @param key
	 * @param d
	 */
	public void loadBitmaps(int key, Drawable d) {
		// 根据参数创建位图
		Bitmap bitmap = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888);
		// 创建对应bitmap的画布
		Canvas canvas = new Canvas(bitmap);
		d.setBounds(0, 0, 180, 180);
		d.draw(canvas);// 图片加载到bitmap上
		icons[key] = bitmap;
	}

	/**
	 * 获取所有图片
	 */
	private void loadIcons() {
		Resources r = getResources();
		loadBitmaps(0, r.getDrawable(R.drawable.introduce_menu_bg));
		loadBitmaps(1, r.getDrawable(R.drawable.activity_menu_bg));
		loadBitmaps(2, r.getDrawable(R.drawable.navigate_menu_bg));
		loadBitmaps(3, r.getDrawable(R.drawable.help_menu_bg));
		loadBitmaps(4, r.getDrawable(R.drawable.share_menu_bg));
		loadBitmaps(5, r.getDrawable(R.drawable.streeview_menu_bg));
	}

	/**
	 * 初始化每一个点
	 */
	private void initPoints() {
		points = new Point[PONIT_NUM];
		Point point;
		int angle = 0;
		mDegreeDelta = 360 / PONIT_NUM;

		for (int index = 0; index < PONIT_NUM; index++) {
			point = new Point();
			point.angle = angle;
			angle = angle + mDegreeDelta;
			point.bitmap = icons[index];
			point.flag = index;
			points[index] = point;
		}
	}

	/**
	 * 计算每个点的坐标
	 */
	private void computeCoordinates() {
		Point point;
		for (int index = 0; index < PONIT_NUM; index++) {
			point = points[index];
			point.x = mPointX
					+ (float) (mRadius * Math.cos(point.angle * Math.PI / 180));
			point.y = mPointY
					+ (float) (mRadius * Math.sin(point.angle * Math.PI / 180));
			point.x_c = mPointX + (point.x - mPointX) / 2;
			point.y_c = mPointY + (point.y - mPointY) / 2;
		}
	}

	/**
	 * 重新计算每一个点的角度
	 * 
	 * @param x
	 * @param y
	 */
	private void resetPointAngle(float x, float y) {
		int degree = computeMigrationAngle(x, y);// 计算偏移的角度
		for (int index = 0; index < PONIT_NUM; index++) {
			points[index].angle = points[index].angle + degree;
			if (points[index].angle > 360) {
				points[index].angle = points[index].angle - 360;
			} else if (points[index].angle < 0) {
				points[index].angle = points[index].angle + 360;
			}
		}
	}

	/**
	 * 计算偏移的角度
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int computeMigrationAngle(float x, float y) {
		int a = 0;
		float distance = (float) Math
				.sqrt(((x - mPointX) * (x - mPointX) + (y - mPointY)
						* (y - mPointY)));
		int degree = (int) (Math.acos((x - mPointX) / distance) * 180 / Math.PI);
		if (y < mPointY) {
			degree = -degree;
		}
		if (tempDegree != 0) {
			a = degree - tempDegree;
		}
		tempDegree = degree;
		return a;
	}

	private void switchScreen(MotionEvent event) {
		computeCurrentDistance(event.getX(), event.getY());
		onTurnplateListener.onPointTouch(chooseBtn);
	}

	/**
	 * 计算触摸位置与各个圆点的距离
	 * 
	 * @param x
	 * @param y
	 */
	private void computeCurrentDistance(float x, float y) {
		for (Point point : points) {
			float distance = (float) Math
					.sqrt(((x - point.x) * (x - point.x) + (y - point.y)
							* (y - point.y)));
			if (distance < 31) {
				chooseBtn = point.flag;
				break;
			} else {
				chooseBtn = 999;
			}
		}
	}

	public class Point {
		int flag;// 位置标识
		Bitmap bitmap;// 图片
		int angle;// 角度
		float x;// x坐标
		float y;// y坐标
		float x_c;// 点与圆心的中心x坐标
		float y_c;// 点与圆心的中心y坐标

	}

	public static interface OnTurnplateListener {
		public void onPointTouch(int flag);
	}

	/**
	 * OnTouchListener接口的监听方法，OnTouchListener接口是用来处理手机屏幕事件的监听接口，
	 * 当为View的范围内触摸按下、抬起或滑动等动作时都会触发该事件
	 * 
	 * @param v
	 *            事件源对象
	 * @param event
	 *            事件封装类的对象，其中封装了触发事件的详细信息，同样包括事件的类型、触发时间等信息。
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

}

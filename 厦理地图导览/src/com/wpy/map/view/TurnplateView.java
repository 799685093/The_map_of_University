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
	 * ���ʣ��㡢��
	 */
	private Paint mPaint = new Paint();
	/**
	 * ���ʣ�Բ
	 */
	private Paint paintCircle = new Paint();
	/**
	 * ͼ���б�
	 */
	private Bitmap[] icons = new Bitmap[10];
	/**
	 * point�б�
	 */
	private Point[] points;
	/**
	 * ��Ŀ
	 */
	private static final int PONIT_NUM = 6;

	/**
	 * Բ������
	 */
	private int mPointX = 0, mPointY = 0;
	/**
	 * �뾶
	 */
	private int mRadius = 0;
	/**
	 * ÿ���������ĽǶ�
	 */
	private int mDegreeDelta;
	/**
	 * ÿ��ת���ĽǶȲ�
	 */
	private int tempDegree = 0;
	/**
	 * ѡ�е�ͼ���ʶ 999��δѡ���κ�ͼ��
	 */
	private int chooseBtn = 999;
	/**
	 * Matrix������ 9 �� float ֵ���ɣ���һ�� 3*3 �ľ���
	 * Matrix�Ĳ������ܹ���Ϊtranslate(ƽ��)��rotate(��ת)��scale(����)��skew(��б)���֣�
	 */
	private Matrix mMatrix = new Matrix();

	/**
	 * ���캯��
	 * 
	 * @param context
	 *            ������
	 * @param px
	 *            ���
	 * @param py
	 *            ����
	 * @param radius
	 *            �뾶
	 */
	public TurnplateView(Context context, int px, int py, int radius) {
		super(context);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);// �����ߵĿ��
		paintCircle.setAntiAlias(true);// ����Ϊ�޾��
		paintCircle.setColor(Color.WHITE);
		loadIcons();// ��ȡͼƬ
		mPointX = px;
		mPointY = py;
		mRadius = radius;
		initPoints();// ��ʼ��ÿһ����
		computeCoordinates();// ����ÿ���������

	}

	/**
	 * ��д��Ļ����¼�������dispatchTouchEvent
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			resetPointAngle(event.getX(), event.getY());// ���¼���ÿһ����ĽǶ�
			computeCoordinates();
			invalidate();// ˢ��view������onDraw����
			break;
		case MotionEvent.ACTION_UP:
			switchScreen(event);
			tempDegree = 0;
			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
			// ϵͳ�����е�һ���̶����޷�������Ӧ��ĺ�������ʱ��������¼���
			// һ����ڴ����н�����Ϊ�쳣��֧�������
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
	 * �ѵ�ŵ�ͼƬ���Ĵ�
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
	 * װ��ͼƬ
	 * 
	 * @param key
	 * @param d
	 */
	public void loadBitmaps(int key, Drawable d) {
		// ���ݲ�������λͼ
		Bitmap bitmap = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888);
		// ������Ӧbitmap�Ļ���
		Canvas canvas = new Canvas(bitmap);
		d.setBounds(0, 0, 180, 180);
		d.draw(canvas);// ͼƬ���ص�bitmap��
		icons[key] = bitmap;
	}

	/**
	 * ��ȡ����ͼƬ
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
	 * ��ʼ��ÿһ����
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
	 * ����ÿ���������
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
	 * ���¼���ÿһ����ĽǶ�
	 * 
	 * @param x
	 * @param y
	 */
	private void resetPointAngle(float x, float y) {
		int degree = computeMigrationAngle(x, y);// ����ƫ�ƵĽǶ�
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
	 * ����ƫ�ƵĽǶ�
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
	 * ���㴥��λ�������Բ��ľ���
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
		int flag;// λ�ñ�ʶ
		Bitmap bitmap;// ͼƬ
		int angle;// �Ƕ�
		float x;// x����
		float y;// y����
		float x_c;// ����Բ�ĵ�����x����
		float y_c;// ����Բ�ĵ�����y����

	}

	public static interface OnTurnplateListener {
		public void onPointTouch(int flag);
	}

	/**
	 * OnTouchListener�ӿڵļ���������OnTouchListener�ӿ������������ֻ���Ļ�¼��ļ����ӿڣ�
	 * ��ΪView�ķ�Χ�ڴ������¡�̧��򻬶��ȶ���ʱ���ᴥ�����¼�
	 * 
	 * @param v
	 *            �¼�Դ����
	 * @param event
	 *            �¼���װ��Ķ������з�װ�˴����¼�����ϸ��Ϣ��ͬ�������¼������͡�����ʱ�����Ϣ��
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

}

package com.scratch.example;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * 实现类似刮刮效果的自定义view
 * 
 * @author tom
 * 
 */
public class ScratchView extends View {

	private static final String TAG = ScratchView.class.getSimpleName();
	private Paint mOutterPaint;
	private Path mPath;
	private Canvas mCanvas;
	private Bitmap mBitmap;

	private int mLastX;
	private int mLastY;

	private Bitmap mOutterBitmap;

	private String mText;
	private Paint mBackPaint;

	/**
	 * 记录刮奖信息文本的宽和高
	 */
	private Rect mTextBound;
	private int mTextSize;
	private int mTextColor;

	// 判断遮盖层区域是否消除达到阈值
	private volatile boolean mComplete = false;

	private OnCompleteListener mListener;
	
	public void setOnCompleteListener(OnCompleteListener listener){
		this.mListener = listener;
	}
	
	
	/**
	 * 完成动作的回调
	 * 
	 * @author tom
	 * 
	 */
	public interface OnCompleteListener {
		void complete();
	}

	public ScratchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScratchView(Context context) {
		this(context, null);
	}

	public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		TypedArray a = null;
		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.scratch, defStyleAttr, 0);
			int n = a.getIndexCount();
			for (int i = 0; i < n; i++) {
				int attr = a.getIndex(i);
				switch (attr) {
				case R.styleable.scratch_text:
					mText = a.getString(attr);
					break;

				case R.styleable.scratch_textColor:
					mTextColor = a.getColor(attr, 0x000000);
					break;

				case R.styleable.scratch_textSize:
					mTextSize = (int) a.getDimension(attr, TypedValue
							.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
									getResources().getDisplayMetrics()));
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (a != null)
				a.recycle();
		}

	}

	/**
	 * 进行一些初始化操作
	 */
	private void init() {
		mOutterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPath = new Path();

		mOutterBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.fg_guaguaka);
		mText = "谢谢惠顾!";
		mTextBound = new Rect();
		mBackPaint = new Paint();
		mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				22, getResources().getDisplayMetrics());

	}

	public void setText(String text) {
		this.mText = text;
		// 获取当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		int width = getMeasuredWidth();
//		int height = getMeasuredHeight();
//		// 初始化我们的bitmap
//		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//		mCanvas = new Canvas(mBitmap);
//
//		setupOutPaint();
//		setUpBackPaint();
//
//		mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30,
//				mOutterPaint);
//		mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height),
//				null);
	}

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int width = w;
		int height = h;
		// 初始化我们的bitmap
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);

		setupOutPaint();
		setUpBackPaint();

		mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30,
				mOutterPaint);
		mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height),
				null);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2, getHeight() / 2 + mTextBound.height() / 2, mBackPaint);
		if(!mComplete){
			drawPath();
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
		
		if(mComplete){
			if(null != mListener){
				mListener.complete();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastX = x;
			mLastY = y;
			mPath.moveTo(mLastX, mLastY);
			break;

		case MotionEvent.ACTION_MOVE:
			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);
			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}
			mLastX = x;
			mLastY = y;

			break;
		case MotionEvent.ACTION_UP:
			if (!mComplete) {
				new Thread(mRunnable).start();
			}

			break;
		default:
			break;
		}

		if (!mComplete) {
			invalidate();
		}
		return true;
	}

	private void setUpBackPaint() {
		mBackPaint.setColor(mTextColor);
		mBackPaint.setStyle(Style.FILL);
		mBackPaint.setTextSize(mTextSize);
		// 获取得当前画笔绘制文本的宽和高
		mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
	}

	private void setupOutPaint() {
		mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
		mOutterPaint.setAntiAlias(true);
		mOutterPaint.setDither(true);
		mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
		mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
		mOutterPaint.setStyle(Style.FILL);
		mOutterPaint.setStrokeWidth(20);
	}
	
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			int w = getWidth();
			int h = getHeight();
			float wipeArea = 0;
			float totalArea = w * h;
			Bitmap bitmap = mBitmap;
			int[] mPixels = new int[w * h];
			//获取bitmap上的所有像素信息
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j * w;
					if(mPixels[index] == 0){
						wipeArea ++;
					}
				}
			}
			
			if(wipeArea > 0 && totalArea > 0){
				int percent = (int) (wipeArea * 100 / totalArea);
				Log.e(TAG, percent + "");
				if(percent > 60){
					//清除掉图层区域
					mComplete = true;
					postInvalidate();
				}
			}
		}
	};
	
	private void drawPath(){
		mOutterPaint.setStyle(Style.STROKE);
		mOutterPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		mCanvas.drawPath(mPath, mOutterPaint);
	}
}

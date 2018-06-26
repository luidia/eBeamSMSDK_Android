package com.pnf.pen.drawingview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pnf.pen.test.MainDefine;

import java.util.ArrayList;

public class DrawView extends View {
	final int MODE_DRAWING_MODE = 0;
	final int MODE_ERASER_BIG_MODE = 1;
	final int MODE_ERASER_SMALL_MODE = 2;

	Context mContext;
	
	Paint mPaint;
	Bitmap mBitmap;
	Canvas mCanvas;
	RectF m_rectMain;
	Path mPath;
	
	Path rectPath;

	public Paint eraserSmallPaint;
	public Paint eraserBigPaint;

	public Paint clearPaint;

	
	int iDrawCnt = 0;
	
	PointF	previousPoint1 = new PointF();
	PointF	previousPoint2 = new PointF();
	PointF	currentPoint = new PointF();
	
	ArrayList<PointF> m_Stroke;

	int m_Mode = MODE_DRAWING_MODE;
	float eraseSmallThick = 4;
	float eraseBigThick = 20;
	
	public DrawView(Context c)
	{
		super(c);
		initView(c);
	}
	
	public DrawView(Context c, AttributeSet attrs) 
	{
		super(c, attrs);
		initView(c);
		
	}
	
	void initView(Context c)
	{
		mContext = c;
		
		m_Stroke = new ArrayList<PointF>();
		
		mPath = new Path();
		rectPath = new Path();
		
		if(mPaint == null){
			mPaint = new Paint();
			
			mPaint.setStrokeWidth(1);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setColor(Color.rgb(0, 0, 0));
			mPaint.setAntiAlias(true);
			
		}
		
		if(clearPaint == null){
			clearPaint = new Paint();
			clearPaint.setStyle(Paint.Style.FILL);
			clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		}

		if(eraserSmallPaint == null){
			eraserSmallPaint = new Paint();
			eraserSmallPaint.setStyle(Paint.Style.STROKE);
			eraserSmallPaint.setStrokeCap(Paint.Cap.ROUND);
			eraserSmallPaint.setColor(0xFFffffff);
			eraserSmallPaint.setAntiAlias(true);
			eraserSmallPaint.setStrokeWidth(eraseSmallThick);
		}

		if(eraserBigPaint == null){
			eraserBigPaint = new Paint();
			eraserBigPaint.setStyle(Paint.Style.STROKE);
			eraserBigPaint.setStrokeCap(Paint.Cap.ROUND);
			eraserBigPaint.setColor(0xFFffffff);
			eraserBigPaint.setAntiAlias(true);
			eraserBigPaint.setStrokeWidth(eraseBigThick);
		}
		
		m_rectMain = new RectF(0 , 0, MainDefine.iDisGetWidth, MainDefine.iDisGetHeight);
		if(mBitmap == null){
			mBitmap = Bitmap.createBitmap(MainDefine.iDisGetWidth, MainDefine.iDisGetHeight, Bitmap.Config.ARGB_4444);
			mCanvas = new Canvas(mBitmap);
		}
	}
	
	void initData(){
		if(mPath != null){
			mPath.reset();
		}
		if(mPath != null){
			rectPath.reset();
		}
		
		if(mCanvas != null){
			mCanvas.drawPaint(clearPaint);
			
		}
		
		invalidate();
	}
	
	void changeDrawingSize(int _width ,int _height){
		if(m_rectMain == null || mBitmap == null || mBitmap.getWidth() != _width || mBitmap.getHeight() != _height){
			if(mBitmap != null){
				mBitmap.recycle();
				mBitmap = null;
		    }
		    
		    m_rectMain = new RectF(0 , 0, _width, _height);
		    
		    mBitmap = Bitmap.createBitmap(_width ,_height ,Bitmap.Config.ARGB_4444);
			mCanvas = new Canvas(mBitmap);
		}
		
		btnClearAll();
		setEraserThick();
	}
	
	@Override
	protected void onDraw(Canvas c) {
		c.drawBitmap(mBitmap, 0, 0, null);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		float x = event.getX();
//		float y = event.getY();
//		RectF drawRect = null;
//		
//		if(isPenDraw){
//			return false;
//		}
//		
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			DoMouseDown(x ,y);
//			
//			break;
//		case MotionEvent.ACTION_MOVE:
//			DoMouseDragged(x ,y);
//			
//			drawRect = new RectF();
//			rectPath.computeBounds(drawRect, true);
//			invalidate(RectFtoRect(drawRect,10));
//			
//			rectPath.reset();
//			break;
//		case MotionEvent.ACTION_UP:
//			DoMouseUp(x ,y);
//			
//			drawRect = new RectF();
//			rectPath.computeBounds(drawRect, true);
//			invalidate(RectFtoRect(drawRect,10));
//			
//			rectPath.reset();
//			break;
//		}
		
		return true;
	}
	
	void invalidatePath(){
		RectF drawRect = new RectF();
		rectPath.computeBounds(drawRect, true);
		invalidate(RectFtoRect(drawRect,10));
	}
	
	void DoMouseDown(float x ,float y){
		iDrawCnt = 0;
		
		previousPoint1 = new PointF(x ,y);
		previousPoint2 = new PointF(x ,y);
		currentPoint = new PointF(x ,y);
	}
	
	void DoMouseDragged(float x ,float y){
		PointF mid1 = new PointF();
		PointF mid2 = new PointF();
		
		if(iDrawCnt == 0 ){
			previousPoint2 = new PointF(previousPoint1.x ,previousPoint1.y);
			currentPoint = new PointF(x ,y);
		}else{
			previousPoint2 = new PointF(previousPoint1.x ,previousPoint1.y);
			previousPoint1 = new PointF(currentPoint.x ,currentPoint.y);
			currentPoint = new PointF(x ,y);
		}
		
		mid1 = BizMidPoint(previousPoint1, previousPoint2);
		mid2 = BizMidPoint(currentPoint, previousPoint1);
		
		mPath.moveTo(mid1.x, mid1.y);
		mPath.quadTo(previousPoint1.x, previousPoint1.y, mid2.x, mid2.y);

		if(m_Mode == MODE_ERASER_SMALL_MODE){
			mCanvas.drawPath(mPath, eraserSmallPaint);
		}else if(m_Mode == MODE_ERASER_BIG_MODE){
			mCanvas.drawPath(mPath, eraserBigPaint);
		}else{
			mCanvas.drawPath(mPath, mPaint);
		}

		iDrawCnt++;
		
		rectPath.moveTo(mid1.x, mid1.y);
		rectPath.quadTo(previousPoint1.x, previousPoint1.y, mid2.x, mid2.y);
		
		RectF drawRect = new RectF();
		rectPath.computeBounds(drawRect, true);
		invalidate(RectFtoRect(drawRect,40));
	}

	void DoMouseUp(float x ,float y){
		if(iDrawCnt == 0) return;
		iDrawCnt = 0;
		
		previousPoint2 = new PointF(previousPoint1.x ,previousPoint1.y);
		previousPoint1 = new PointF(currentPoint.x ,currentPoint.y);
		currentPoint = new PointF(x ,y);
		
		PointF mid1 = BizMidPoint(previousPoint1, previousPoint2);
		PointF mid2 = BizMidPoint(currentPoint, previousPoint1);
		
		mPath.moveTo(mid1.x, mid1.y);
		mPath.quadTo(previousPoint1.x, previousPoint1.y, mid2.x, mid2.y);
		mPath.lineTo(currentPoint.x, currentPoint.y);

		if(m_Mode == MODE_ERASER_SMALL_MODE){
			mCanvas.drawPath(mPath, eraserSmallPaint);
		}else if(m_Mode == MODE_ERASER_BIG_MODE){
			mCanvas.drawPath(mPath, eraserBigPaint);
		}else{
			mCanvas.drawPath(mPath, mPaint);
		}
		
		rectPath.moveTo(mid1.x, mid1.y);
		rectPath.quadTo(previousPoint1.x, previousPoint1.y, mid2.x, mid2.y);
		rectPath.moveTo(currentPoint.x, currentPoint.y);
		
		RectF drawRect = new RectF();
		rectPath.computeBounds(drawRect, true);
		invalidate(RectFtoRect(drawRect,40));
		
		mPath.reset();
		rectPath.reset();
	}
	
	void setPenMode(int mode){
		m_Mode = mode;
	}
	
	void setPenColor(int color){
		mPaint.setColor(color);
	}

	void setEraserThick(){
		if(MainDefine.penController.isPenMode()){
			eraseSmallThick = MainDefine.penController.getMarkerEraserThick(false);
			eraseBigThick = MainDefine.penController.getMarkerEraserThick(false);
		}else{
			eraseSmallThick = 4;
			eraseBigThick = 20;
		}

		eraserSmallPaint.setStrokeWidth(eraseSmallThick);
		eraserBigPaint.setStrokeWidth(eraseBigThick);
	}
	
	void btnClearAll()
	{
		mCanvas.drawPaint(clearPaint);
		
		invalidate();
	}
	
	PointF BizMidPoint(PointF pt,PointF pt2)
	{
		return new PointF((pt.x + pt2.x)/2, (pt.y + pt2.y)/2);
	}
	
	Rect RectFtoRect(RectF rectf,float thick)
	{
		Rect rect = new Rect((int)(rectf.left-thick) ,(int)(rectf.top-thick) ,(int)(rectf.right+thick) ,(int)(rectf.bottom+thick));
		return rect;
	}
}
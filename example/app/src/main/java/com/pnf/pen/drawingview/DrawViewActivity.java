package com.pnf.pen.drawingview;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.luidia.ebeam.sdk.data.PenData;
import com.luidia.ebeam.sdk.constants.PenEvent;
import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenEventListener;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

public class DrawViewActivity extends Activity implements PenEventListener, PenMessageListener {
	ImageView drawViewBGImgView;
	
	FrameLayout drawNoteLayer;
	DrawView drawView;

	@Override
	protected void onResume() {
		super.onResume();

		MainDefine.penController.setPenEventListener(this);
		MainDefine.penController.setPenMessageListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
	}
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }
	
	@Override
	public void onBackPressed()
	{
		setResult(RESULT_OK,null);
		finish();
		return;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.drawview);
		
		drawViewBGImgView = (ImageView) findViewById(R.id.drawViewBGImgView);
		
		drawNoteLayer = (FrameLayout) findViewById(R.id.drawNoteLay);
		drawView = (DrawView) findViewById(R.id.drawView);
		
		Point changeSize = MainDefine.changeResolution();
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(changeSize.x ,changeSize.y);
        params.gravity = Gravity.CENTER;
        drawNoteLayer.setLayoutParams(params);
    	
    	drawView.setLayoutParams(new FrameLayout.LayoutParams( LayoutParams.MATCH_PARENT ,LayoutParams.MATCH_PARENT));
		
		drawView.changeDrawingSize(changeSize.x ,changeSize.y);

		drawView.invalidate();
	}
	
	public void ClearAllBtnClicked(View v){
		drawView.btnClearAll();
	}

	public void penModeBtnClicked(View v){
		drawView.setPenMode(drawView.MODE_DRAWING_MODE);
	}

	public void eraserModeBtnClicked(View v){
		drawView.setPenMode(drawView.MODE_ERASER_BIG_MODE);
	}

	public void CloseBtnClicked(View v){
		setResult(RESULT_OK,null);
		finish();
	}

	@Override
	public void onPenEvent(int penState, int RawX, int RawY, Object obj)
	{
		PenData penData = (PenData)obj;
		PointF ptConv = MainDefine.penController.getCoordinatePosition(RawX ,RawY ,penData.bRight);
		
		switch(penState)
		{
		case PenEvent.PEN_DOWN:
			switch(penData.MakerPenStatus){
				case PenEvent.MARKERPEN_RED_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_red));
					break;
				case PenEvent.MARKERPEN_GREEN_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_green));
					break;
				case PenEvent.MARKERPEN_YELLOW_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_yellow));
					break;
				case PenEvent.MARKERPEN_BLUE_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_blue));
					break;
				case PenEvent.MARKERPEN_PURPLE_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_purple));
					break;
				case PenEvent.MARKERPEN_BLACK_MARKER:
					drawView.setPenMode(drawView.MODE_DRAWING_MODE);
					drawView.setPenColor(getResources().getColor(R.color.marker_color_black));
					break;
				case PenEvent.MARKERPEN_ERASER_CAP:
					drawView.setPenMode(drawView.MODE_ERASER_SMALL_MODE);
					break;
				case PenEvent.MARKERPEN_BIG_ERASER:
					drawView.setPenMode(drawView.MODE_ERASER_BIG_MODE);
					break;
			}

			drawView.DoMouseDown(ptConv.x ,ptConv.y);
			break;
		case PenEvent.PEN_MOVE:
			drawView.DoMouseDragged(ptConv.x ,ptConv.y);
			drawView.invalidatePath();
			break;
		case PenEvent.PEN_UP:
			drawView.DoMouseUp(ptConv.x ,ptConv.y);
			drawView.invalidatePath();
			break;
			
			
		case PenEvent.PEN_HOVER:
			break;
		case PenEvent.PEN_HOVER_DOWN:
			break;
		case PenEvent.PEN_HOVER_MOVE:
			break;
		}
	}
	
	@Override
	public void onPenMessage(int what, int arg1, int arg2, Object obj) {
		if(what == PenMessage.PNF_MSG_FAIL_LISTENING){
			return;
		}
		else if(what == PenMessage.PNF_MSG_PEN_RMD_ERROR){
			Toast.makeText(
					getApplicationContext(),
					"RMD_ERROR",
					Toast.LENGTH_SHORT)
					.show();
			return;
		}
	}
}

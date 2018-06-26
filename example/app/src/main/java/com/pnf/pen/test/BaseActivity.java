package com.pnf.pen.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.luidia.ebeam.sdk.EBeamPenController;
import com.luidia.ebeam.sdk.constants.ModelCode;

public class BaseActivity extends Activity {
	@Override
	protected void onResume() {
		super.onResume();
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
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
	}

	@Override
	public void onBackPressed()
	{
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		boolean isResultOK = resultCode == Activity.RESULT_OK?true:false;
		if(isResultOK){
			new Thread(new Runnable() {
				public void run() {
					runOnUiThread(new Runnable(){
						@Override
						public void run() {

							System.exit(0);
						}
					});
				}
			}).start();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.baseview);
		
		/*
    	 * LCD 크기 셋팅
    	 */
		Point LCDSize = new Point();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(LCDSize);
		MainDefine.iDisGetWidth = LCDSize.x;
		MainDefine.iDisGetHeight = LCDSize.y;

		/*
    	 * PNFBtLib 셋팅
    	 */
    	MainDefine.penController = EBeamPenController.create(getApplicationContext());
    	MainDefine.penController.setModelCode(ModelCode.eBeamSmartMaker);

		Intent introIntent = new Intent(BaseActivity.this, MainActivity.class);
		startActivityForResult(introIntent, 0);
	}
}

package com.pnf.pen.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

import java.util.Timer;
import java.util.TimerTask;

public class PNFBluetoothViewActivity extends Activity implements PenMessageListener {
	final int SDKVER_LOLLIPOP = 21;
	
	private final int REQUEST_ENABLE_BT = 1;
	private final int REQUEST_SELECT_DEVICE = 2;
	private final int REQUEST_BLUETOOTH_CONNECT_FAIL_POPUP = 3;

	TimerTask bluetoothConnectTask = null;
	Timer bluetoothConnectTimer = null;
	
	int timerCnt = 0;
	
	String selectedDeviceName;
	String selectedDeviceAddress;

	ProgressDialog progDialog;

	@Override
    protected void onResume() {
        super.onResume();

		MainDefine.penController.setPenEventListener(null);
		MainDefine.penController.setPenMessageListener(this);
    }
	
    @Override
    protected void onPause() 
    {
    	super.onPause();
    }
    
    @Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart(){//액티비티의 표시를 중단했을 때 불린다.
		super.onStart();
	}
    
    @Override
	protected void onDestroy() {
		super.onDestroy();

		stopBluetoothTimer();
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
		return;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == Activity.RESULT_OK){
				if(MainDefine.penController.isBluetoothEnabled()){
					goBluetoothSearch();
				}else{
					setResult(RESULT_CANCELED, null);
					finish();
				}
			}else{
				setResult(RESULT_CANCELED, null);
				finish();
			}
		}else if(requestCode == REQUEST_SELECT_DEVICE){
			if(resultCode == Activity.RESULT_OK){
				boolean isSuccess = data.getExtras().getBoolean("isSuccess");
				if(isSuccess){
					if(MainDefine.penController.isBluetoothEnabled()){
						selectedDeviceName = data.getStringExtra(BluetoothDevice.EXTRA_NAME);
						selectedDeviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
						
						if(MainDefine.penController == null ||
								MainDefine.penController.isPenMode() ||
								selectedDeviceAddress.isEmpty()) {
							setResult(RESULT_CANCELED, null);
							finish();
						}else{
							goBluetoothConnect();
						}
					}else{
						goBluetoothEnable();
					}
				}else{
					Toast.makeText(
							getApplicationContext(),
							"eBeam Device connect fail",
							Toast.LENGTH_SHORT)
							.show();

					setResult(RESULT_CANCELED, null);
					finish();
				}
			}else{
				setResult(RESULT_CANCELED, null);
				finish();
			}
		}else if(requestCode == REQUEST_BLUETOOTH_CONNECT_FAIL_POPUP){
			if(resultCode == Activity.RESULT_OK){
				goBluetoothSearch();
			}else{
				setResult(RESULT_CANCELED, null);
				finish();
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_connect_view);
		/*
		 * progress show
		 */


		/*
		 * other data setting
		 */
		boolean isFinish = false;
		try{
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(
						getApplicationContext(),
						"It does not support Bluetooth 4.0",
						Toast.LENGTH_SHORT)
						.show();

				isFinish = true;
			}
		}catch(Exception e){
			Toast.makeText(
					getApplicationContext(),
					"It does not support Bluetooth 4.0",
					Toast.LENGTH_SHORT)
					.show();

			isFinish = true;
		}

		if(isFinish){
			setResult(RESULT_CANCELED, null);
			finish();
		}else{
			if(MainDefine.penController.isBluetoothEnabled()){
				goBluetoothSearch();
			}else{
				goBluetoothEnable();
			}


		}
	}

	void goBluetoothSearch(){
		if(MainDefine.penController.isPenMode()){
			setResult(RESULT_CANCELED, null);
			finish();
		}else{
			if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP){
				Intent intent = new Intent(PNFBluetoothViewActivity.this, DeviceListScanActivity.class);
				startActivityForResult(intent, REQUEST_SELECT_DEVICE);
			}else{
				Intent intent = new Intent(PNFBluetoothViewActivity.this, DeviceListLeScanActivity.class);
				startActivityForResult(intent, REQUEST_SELECT_DEVICE);
			}
		}
		
	}
	
	void goBluetoothConnect(){
		MainDefine.penController.connect(selectedDeviceName ,selectedDeviceAddress);

		startBluetoothTimer();

		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setMessage("eBeam device connecting.");
		progDialog.show();
	}
	
	void startBluetoothTimer(){
		if(bluetoothConnectTimer == null){
			timerCnt = 0;
			
			bluetoothConnectTimer = new Timer();
			bluetoothConnectTask = new TimerTask()
			{
				final int timerMaxCnt = 40;
				
				@Override
				public void run()
				{
					if(timerCnt > timerMaxCnt){
						bluetoothConnectHandler.sendEmptyMessage(1);
					}else{
						bluetoothConnectHandler.sendEmptyMessage(0);
					}
					
					timerCnt++;
				}
			};
			bluetoothConnectTimer.schedule(bluetoothConnectTask, 100 ,500);
		}
    }



	void stopBluetoothTimer(){
		if(bluetoothConnectTask != null){
			bluetoothConnectTask.cancel();
			bluetoothConnectTask = null; 
		}

		if(bluetoothConnectTimer != null){
			bluetoothConnectTimer.cancel();
//			bluetoothConnectTimer.purge();
			bluetoothConnectTimer = null;
		}
    }
	
	@SuppressLint("HandlerLeak")
	Handler bluetoothConnectHandler = new Handler() 
	{        
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
			case 0:
				//연결 중..
				if(timerCnt%5 == 0){
					progDialog.setMessage("eBeam device connecting.");
				}else if(timerCnt%5 == 1){
					progDialog.setMessage("eBeam device connecting...");
				}else if(timerCnt%5 == 2){
					progDialog.setMessage("eBeam device connecting.....");
				}else if(timerCnt%5 == 3){
					progDialog.setMessage("eBeam device connecting.......");
				}else{
					progDialog.setMessage("eBeam device connecting.........");
				}
				break;
			case 1:
				//연결 실패
				stopBluetoothTimer();
				if(progDialog != null) {
					progDialog.dismiss();
					progDialog = null;
				}

				MainDefine.penController.disconnect();

				Toast.makeText(
						getApplicationContext(),
						"eBeam Device connect fail",
						Toast.LENGTH_SHORT)
						.show();

				setResult(RESULT_CANCELED, null);
				finish();
				break;
			default:
				//완료
				if(progDialog != null) {
					progDialog.dismiss();
					progDialog = null;
				}

				setResult(RESULT_OK, null);
				finish();
				break;
			}
		}
	};
	
	void goBluetoothEnable(){
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, REQUEST_ENABLE_BT);
	}
	
	public void bluetoothConnectCloseClicked(View v){
		setResult(RESULT_CANCELED, null);
		finish();
	}

	@Override
	public void onPenMessage(int what, int arg1, int arg2, Object obj) {
		if(what == PenMessage.PNF_MSG_DISCONNECTED ||
				what == PenMessage.PNF_MSG_FAIL_LISTENING ||
				what == PenMessage.PNF_MSG_CONNECTING_FAIL){
			bluetoothConnectHandler.sendEmptyMessage(1);
		}
		else if(what == PenMessage.PNF_MSG_FIRST_DATA_RECV){
			stopBluetoothTimer();

			bluetoothConnectHandler.sendEmptyMessage(2);
		}
	}
}
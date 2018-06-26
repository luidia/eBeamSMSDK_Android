package com.pnf.pen.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListLeScanActivity extends Activity {
    BluetoothAdapter mBluetoothAdapter;

    ListView bluetoothDevicesList;

    int bleScanCnt = 0;
	TimerTask bleScanTask = null;
	Timer bleScanTimer = null;

    DeviceAdapter deviceAdapter;

	@Override
    protected void onResume() {
        super.onResume();

		MainDefine.penController.setPenEventListener(null);
		MainDefine.penController.setPenMessageListener(null);
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
	protected void onStart(){
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopScanTimer();
		stopScanLeDevice();
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
		setResult(RESULT_CANCELED, null);
		finish();

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
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bluetooth_device_list);

        bluetoothDevicesList = (ListView) findViewById(R.id.bluetoothDevicesList);

    	/*
		 * other data setting
		 */
        mBluetoothAdapter = MainDefine.penController.getBluetoothAdapter();
        mBluetoothAdapter.cancelDiscovery();

        deviceAdapter = new DeviceAdapter(this);
        int[] colors = {
                0,
                0xFFdedede,
                0xFFdedede};
        bluetoothDevicesList.setDivider(new GradientDrawable(Orientation.LEFT_RIGHT, colors));
        bluetoothDevicesList.setDividerHeight(1);
        bluetoothDevicesList.setVerticalFadingEdgeEnabled(false);

        bluetoothDevicesList.setAdapter(deviceAdapter);
        bluetoothDevicesList.setOnItemClickListener(mDeviceClickListener);

        goBluetoothScan();
    }

    void goBluetoothScan(){
    	bleScanCnt = 0;

    	startScanTimer();
    	startScanLeDevice();
	}

    public void menuDeviceCloseBtnClicked(View v){
		setResult(RESULT_CANCELED, null);
		finish();
    }

    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	void stopScanLeDevice() {
    	mBluetoothAdapter.stopLeScan(mLeScanCallback);

		mBluetoothAdapter.cancelDiscovery();
	}

    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void startScanLeDevice() {
    	mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if(device != null &&
                			device.getName() != null &&
                			!device.getName().isEmpty() &&
							(device.getName().contains("eSM") || device.getName().contains("eBP"))){

                		BLEDeviceData btDevice = new BLEDeviceData(device ,rssi);
                		addDevice(btDevice);
                	}
                }
            });
        }
    };

    void addDevice(BLEDeviceData addDeviceData) {
        boolean deviceFind = false;
        int findIdx = 0;
        for (int i=0;i<deviceAdapter.getCount();i++) {
        	BLEDeviceData deviceData = deviceAdapter.getItem(i);
            if (addDeviceData.btDevice.getAddress().equals(deviceData.btDevice.getAddress())) {
            	deviceFind = true;
            	findIdx = i;
                break;
            }
        }

        if (deviceFind) {
        	deviceAdapter.setItem(findIdx ,addDeviceData);
        }else{
        	deviceAdapter.addItem(addDeviceData);
        }
    }

    void startScanTimer(){
		if(bleScanTimer == null){
			bleScanTimer = new Timer();
			bleScanTask = new TimerTask()
			{
				@Override
				public void run()
				{
					bluetoothScanHandler.sendEmptyMessage(0);
				}
			};
			bleScanTimer.schedule(bleScanTask ,100 ,1000);
		}
    }

	void stopScanTimer(){
		if(bleScanTask != null){
			bleScanTask.cancel();
			bleScanTask = null;
		}

		if(bleScanTimer != null){
			bleScanTimer.cancel();
//			bleScanTimer.purge();
			bleScanTimer = null;
		}
    }

	@SuppressLint("HandlerLeak")
	Handler bluetoothScanHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(deviceAdapter.getCount() > 0){
				if(deviceAdapter.getCount() > 1){
					deviceAdapter.sortDeviceList();
				}

				deviceAdapter.notifyDataSetChanged();
			}
		}
	};

    OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	stopScanTimer();
        	stopScanLeDevice();


            BluetoothDevice device = deviceAdapter.getItem(position).btDevice;

            Bundle bundle = new Bundle();
            bundle.putString(BluetoothDevice.EXTRA_NAME, device.getName());
            bundle.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress());

            Intent intent = new Intent();
            intent.putExtra("isSuccess", true);
            intent.putExtras(bundle);

			setResult(RESULT_OK, intent);
			finish();
        }
    };


    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BLEDeviceData> deviceDataList;
        LayoutInflater inflater;

        public DeviceAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            deviceDataList = new ArrayList<BLEDeviceData>();
        }

        @Override
        public int getCount() {
            return deviceDataList.size();
        }

        @Override
        public BLEDeviceData getItem(int position) {
            return deviceDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        void clearItem(){
        	deviceDataList.clear();
        }

        void addItem(BLEDeviceData addDevice){
        	deviceDataList.add(addDevice);
        }

        void setItem(int index ,BLEDeviceData addDevice){
        	deviceDataList.set(index ,addDevice);
        }

        void sortDeviceList(){
        	boolean isSort = false;
        	int tempRssi = 0;
        	for(BLEDeviceData deviceData : deviceDataList){
        		if(deviceData.rssiDistance <= tempRssi){
        			tempRssi = deviceData.rssiDistance;
        		}else{
        			isSort = true;
        		}
        	}
        	if(isSort){
        		if(deviceDataList.size() > 1){
        			Collections.sort(deviceDataList ,deviceComparator);
        		}
        	}
        }

        //Comparator 를 만든다.
        public Comparator<BLEDeviceData> deviceComparator = new Comparator<BLEDeviceData>() {
        	@SuppressWarnings("unused")
			private final Collator collator = Collator.getInstance();
        	@Override
        	public int compare(BLEDeviceData object1 ,BLEDeviceData object2) {
        		//큰수 부터
        		return ((Integer)object2.rssiDistance).compareTo(((Integer)object1.rssiDistance));
        	}
        };

        @SuppressLint("InflateParams")
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.bluetooth_device_element, null);
            }

            BLEDeviceData deviceData = deviceDataList.get(position);

            final TextView deviceName = ((TextView) vg.findViewById(R.id.deviceName));
            final TextView deviceAddress = ((TextView) vg.findViewById(R.id.deviceAddress));
            final TextView deviceDistance = ((TextView) vg.findViewById(R.id.deviceDistance));

            String btDeviceName = deviceData.btDevice.getName();
            String btDeviceAddress = deviceData.btDevice.getAddress();
            int btDeviceRSSIDistance = deviceData.rssiDistance;

            if(!btDeviceName.isEmpty() && btDeviceName.length() > 12){
            	btDeviceName = btDeviceName.substring(0, 12);
            }

            deviceName.setText(btDeviceName);
            deviceAddress.setText(btDeviceAddress);
            deviceDistance.setText(String.valueOf(btDeviceRSSIDistance)+"dB");
            return vg;
        }
    }
}
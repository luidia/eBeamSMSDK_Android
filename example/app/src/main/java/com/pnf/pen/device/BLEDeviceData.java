package com.pnf.pen.device;

import android.bluetooth.BluetoothDevice;

public class BLEDeviceData {
	BluetoothDevice btDevice = null;
	int rssiDistance = 0;

	public BLEDeviceData(BluetoothDevice _btDevice ,int _rssiDistance){
		btDevice = _btDevice;
		rssiDistance = _rssiDistance;
	}
}

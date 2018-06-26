package com.pnf.pen.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.luidia.ebeam.sdk.data.PenData;
import com.luidia.ebeam.sdk.constants.DeviceAudio;
import com.luidia.ebeam.sdk.constants.DeviceDirection;
import com.luidia.ebeam.sdk.constants.PenEvent;
import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenEventListener;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.calibration.CalibrationMarkerActivity;
import com.pnf.pen.device.DeviceChangeAudioViewActivity;
import com.pnf.pen.device.DeviceChangeNameViewActivity;
import com.pnf.pen.device.PNFBluetoothViewActivity;
import com.pnf.pen.drawingview.DrawViewActivity;

import java.util.Locale;

public class MainActivity extends Activity implements PenEventListener, PenMessageListener {
    PowerManager.WakeLock mWakeLock;

    final byte REQUEST_BLUETOOTH_CONNECT = 0x00;
    final byte REQUEST_BLUETOOTH_CHANGE_NAME = 0x01;
    final byte REQUEST_BLUETOOTH_CHANGE_AUDIO = 0x02;
    final byte REQUEST_DRAWVIEW = 0x03;
    final byte REQUEST_CALIBRATIONVIEW = 0x04;

    final byte ALERTVIEW_APP_EXIT = 0x01;

    ScrollView writeLogScrollView;
    TextView writeLogTextView;

    TextView modelCodeValueTextView;
    TextView hwVerValueTextView;
    TextView dspVerValueTextView;
    TextView mcuVerValueTextView;
    TextView packetCountValueTextView;
    TextView nameValueTextView;
    TextView positionValueTextView;
    TextView audioLangValueTextView;
    TextView statusValueTextView;
    TextView rawXValueTextView;
    TextView rawYValueTextView;
    TextView convXValueTextView;
    TextView convYValueTextView;
    TextView smartMakerValueTextView;

    TextView downCountValueTextView;
    TextView moveCountValueTextView;
    TextView upCountValueTextView;
    TextView totalCountValueTextView;
    TextView kbyteValueTextView;
    TextView byteValueTextView;

    Button deviceStateBtn;

    int penErrorCnt = 0;
    int packetCnt = 0;
    int downCnt = 0;
    int moveCnt = 0;
    int upCnt = 0;

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
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override
    public void onBackPressed() {
        showAlertView(ALERTVIEW_APP_EXIT);
        return;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isResultOK = resultCode == Activity.RESULT_OK ? true : false;

        switch (requestCode) {
            case REQUEST_BLUETOOTH_CONNECT:
                if (isResultOK) {
                    penErrorCnt = 0;

                    addDebugText("ebeam device connected");

                    deviceStateBtn.setText("Disconnect");
                    modelCodeValueTextView.setText(String.valueOf(MainDefine.penController.getModelCode()));
                    dspVerValueTextView.setText(String.format(Locale.US, "%c", (byte) MainDefine.penController.getDSP()));
                    mcuVerValueTextView.setText(String.format(Locale.US, "%2d", (byte) MainDefine.penController.getMCU1()) + "." + String.format(Locale.US, "%02d", (byte) MainDefine.penController.getMCU2()));
                    hwVerValueTextView.setText("" + MainDefine.penController.getStationVersion());
                    nameValueTextView.setText("" + MainDefine.penController.getStationName());

                    int stationPosition = MainDefine.penController.getStationPosition();
                    switch (stationPosition) {
                        case DeviceDirection.DEVICE_DIRECTION_LEFT:
                            positionValueTextView.setText("LEFT");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_TOP:
                            positionValueTextView.setText("TOP");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_RIGHT:
                            positionValueTextView.setText("RIGHT");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_BOTTOM:
                            positionValueTextView.setText("BOTTOM");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_BOTH:
                            positionValueTextView.setText("BOTH");
                            break;
                    }

                    int audioLangCode = MainDefine.penController.getLanguageCode();
                    switch (audioLangCode) {
                        case DeviceAudio.DEVICE_AUDIO_LANG_ENGLISH:
                            audioLangValueTextView.setText("ENGLISH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_KOREA:
                            audioLangValueTextView.setText("KOREA");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_SPANISH:
                            audioLangValueTextView.setText("SPANISH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_GERMAN:
                            audioLangValueTextView.setText("GERMAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_FRENCH:
                            audioLangValueTextView.setText("FRENCH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_ITALIAN:
                            audioLangValueTextView.setText("ITALIAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_JAPANESE:
                            audioLangValueTextView.setText("JAPANESE");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_PROTUGUESE:
                            audioLangValueTextView.setText("PROTUGUESE");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_RUSSIAN:
                            audioLangValueTextView.setText("RUSSIAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_CHINESE_SIMPLIFIED:
                            audioLangValueTextView.setText("CHINESE(SIMPLIFIED)");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_CHINESE_TRADITIONAL:
                            audioLangValueTextView.setText("CHINESE(TRADITIONAL)");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_ARABIC:
                            audioLangValueTextView.setText("ARABIC");
                            break;
                    }
                }
                break;
            case REQUEST_BLUETOOTH_CHANGE_NAME:
                if (isResultOK) {
                    addDebugText("change name success.");

                    nameValueTextView.setText("" + MainDefine.penController.getStationName());
                } else {
                    if (!MainDefine.penController.isPenMode()) {
                        addDebugText("ebeam device disconnected");

                        deviceStateBtn.setText("Connect");
                    }
                }
                break;
            case REQUEST_BLUETOOTH_CHANGE_AUDIO:
                if (isResultOK) {
                    addDebugText("change audio success.");

                    int audioLangCode = MainDefine.penController.getLanguageCode();
                    switch (audioLangCode) {
                        case DeviceAudio.DEVICE_AUDIO_LANG_ENGLISH:
                            audioLangValueTextView.setText("ENGLISH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_KOREA:
                            audioLangValueTextView.setText("KOREA");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_SPANISH:
                            audioLangValueTextView.setText("SPANISH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_GERMAN:
                            audioLangValueTextView.setText("GERMAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_FRENCH:
                            audioLangValueTextView.setText("FRENCH");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_ITALIAN:
                            audioLangValueTextView.setText("ITALIAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_JAPANESE:
                            audioLangValueTextView.setText("JAPANESE");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_PROTUGUESE:
                            audioLangValueTextView.setText("PROTUGUESE");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_RUSSIAN:
                            audioLangValueTextView.setText("RUSSIAN");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_CHINESE_SIMPLIFIED:
                            audioLangValueTextView.setText("CHINESE(SIMPLIFIED)");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_CHINESE_TRADITIONAL:
                            audioLangValueTextView.setText("CHINESE(TRADITIONAL)");
                            break;
                        case DeviceAudio.DEVICE_AUDIO_LANG_ARABIC:
                            audioLangValueTextView.setText("ARABIC");
                            break;
                    }
                } else {
                    if (!MainDefine.penController.isPenMode()) {
                        addDebugText("ebeam device disconnected");

                        deviceStateBtn.setText("Connect");
                    }
                }
                break;
            case REQUEST_DRAWVIEW:
                if (MainDefine.penController.isPenMode()) {
                    if (isResultOK) {

                    }
                } else {
                    addDebugText("ebeam device disconnected");

                    deviceStateBtn.setText("Connect");
                }
                break;
            case REQUEST_CALIBRATIONVIEW:
                if (isResultOK) {
                    addDebugText("setting calibration success.");

                    int stationPosition = MainDefine.penController.getStationPosition();
                    switch (stationPosition) {
                        case DeviceDirection.DEVICE_DIRECTION_LEFT:
                            positionValueTextView.setText("LEFT");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_TOP:
                            positionValueTextView.setText("TOP");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_RIGHT:
                            positionValueTextView.setText("RIGHT");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_BOTTOM:
                            positionValueTextView.setText("BOTTOM");
                            break;
                        case DeviceDirection.DEVICE_DIRECTION_BOTH:
                            positionValueTextView.setText("BOTH");
                            break;
                    }
                } else {
                    if (!MainDefine.penController.isPenMode()) {
                        addDebugText("ebeam device disconnected");

                        deviceStateBtn.setText("Connect");
                    }
                }
                break;
        }
    }

    @SuppressLint("Wakelock")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeLogScrollView = (ScrollView) findViewById(R.id.writeLogScrollView);
        writeLogTextView = (TextView) findViewById(R.id.writeLogTextView);

        modelCodeValueTextView = (TextView) findViewById(R.id.modelCodeValueTextView);
        hwVerValueTextView = (TextView) findViewById(R.id.hwVerValueTextView);
        dspVerValueTextView = (TextView) findViewById(R.id.dspVerValueTextView);
        mcuVerValueTextView = (TextView) findViewById(R.id.mcuVerValueTextView);
        packetCountValueTextView = (TextView) findViewById(R.id.packetCountValueTextView);
        nameValueTextView = (TextView) findViewById(R.id.nameValueTextView);
        positionValueTextView = (TextView) findViewById(R.id.positionValueTextView);
        audioLangValueTextView = (TextView) findViewById(R.id.audioLangValueTextView);
        statusValueTextView = (TextView) findViewById(R.id.statusValueTextView);
        rawXValueTextView = (TextView) findViewById(R.id.rawXValueTextView);
        rawYValueTextView = (TextView) findViewById(R.id.rawYValueTextView);
        convXValueTextView = (TextView) findViewById(R.id.convXValueTextView);
        convYValueTextView = (TextView) findViewById(R.id.convYValueTextView);

        downCountValueTextView = (TextView) findViewById(R.id.downCountValueTextView);
        moveCountValueTextView = (TextView) findViewById(R.id.moveCountValueTextView);
        upCountValueTextView = (TextView) findViewById(R.id.upCountValueTextView);
        totalCountValueTextView = (TextView) findViewById(R.id.totalCountValueTextView);
        kbyteValueTextView = (TextView) findViewById(R.id.kbyteValueTextView);
        byteValueTextView = (TextView) findViewById(R.id.byteValueTextView);
        smartMakerValueTextView = (TextView) findViewById(R.id.smartMakerValueTextView);

        deviceStateBtn = (Button) findViewById(R.id.deviceStateBtn);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
        mWakeLock.acquire();
    }

    public void drawingBtnClicked(View v) {
        Intent intent = new Intent(MainActivity.this, DrawViewActivity.class);
        startActivityForResult(intent, REQUEST_DRAWVIEW);
    }

    public void calibrationBtnClicked(View v) {
        if (MainDefine.penController.isPenMode()) {
            Intent intent = new Intent(MainActivity.this, CalibrationMarkerActivity.class);
            startActivityForResult(intent, REQUEST_CALIBRATIONVIEW);
        }
    }

    public void connectPenBtnClicked(View v) {
        if (MainDefine.penController.isPenMode()) {
            MainDefine.penController.disconnect();

            addDebugText("ebeam device disconnected");
            deviceStateBtn.setText("Connect");
        } else {
            Intent intent = new Intent(MainActivity.this, PNFBluetoothViewActivity.class);
            startActivityForResult(intent, REQUEST_BLUETOOTH_CONNECT);
        }
    }

    public void changeNameBtnClicked(View v) {
        if (MainDefine.penController.isPenMode()) {
            Intent intent = new Intent(MainActivity.this, DeviceChangeNameViewActivity.class);
            startActivityForResult(intent, REQUEST_BLUETOOTH_CHANGE_NAME);
        }
    }

    public void changeAudioBtnClicked(View v) {
        if (MainDefine.penController.isPenMode()) {
            Intent intent = new Intent(MainActivity.this, DeviceChangeAudioViewActivity.class);
            startActivityForResult(intent, REQUEST_BLUETOOTH_CHANGE_AUDIO);
        }
    }

    public void clearLogBtnClicked(View v) {
        writeLogTextView.setText("");
    }


    public void packetCountClearClicked(View v) {
        packetCnt = 0;
        updatePacketCnt();
    }

    public void countClearClicked(View v) {
        downCnt = 0;
        moveCnt = 0;
        upCnt = 0;

        updateDrawCnt();
    }

    void showAlertView(int alertTag) {
        AlertDialog.Builder builder = null;
        AlertDialog alert = null;

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);

        switch (alertTag) {
            case ALERTVIEW_APP_EXIT:
                builder.setTitle("Quit App");
                builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        setResult(RESULT_OK, null);
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
        }

        alert = builder.create();
        alert.show();
    }

    void updatePacketCnt() {
        packetCountValueTextView.setText("" + packetCnt);
    }

    void updateDrawCnt() {
        downCountValueTextView.setText("" + downCnt);
        moveCountValueTextView.setText("" + moveCnt);
        upCountValueTextView.setText("" + upCnt);
        totalCountValueTextView.setText("" + (downCnt + moveCnt + upCnt));

        float byteData = 0;
        if (downCnt + moveCnt + upCnt == 0)
            byteValueTextView.setText("0");
        else {
            byteValueTextView.setText("" + (((downCnt + moveCnt + upCnt) * 6) + ((downCnt + moveCnt + upCnt) / 12) + 1));
            byteData = ((downCnt + moveCnt + upCnt) * 6) + ((downCnt + moveCnt + upCnt) / 12) + 1;
        }
        kbyteValueTextView.setText("" + (byteData / 1024.f));
    }

    void addDebugText(String text) {
        String orgText = writeLogTextView.getText().toString();
        String inputText = "";
        if (orgText.isEmpty()) {
            inputText = text;
        } else {
            inputText = orgText + "\n" + text;
        }

        writeLogTextView.setText(inputText);
        writeLogScrollView.post(new Runnable() {
            @Override
            public void run() {
                writeLogScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

    }


    @Override
    public void onPenMessage(int what, int arg1, int arg2, Object obj) {
        if (what == PenMessage.PNF_MSG_FAIL_LISTENING) {
            addDebugText("PNF_MSG_FAIL_LISTENING");

            deviceStateBtn.setText("Connect");
            return;
        }

        if (what == PenMessage.PNF_MSG_INVALID_PROTOCOL) {
            return;
        } else if (what == PenMessage.PNF_MSG_PEN_RMD_ERROR) {
            penErrorCnt++;
            if (penErrorCnt > 5) {
                Toast.makeText(
                        getApplicationContext(),
                        "RMD_ERROR",
                        Toast.LENGTH_SHORT)
                        .show();
                penErrorCnt = 0;
            }
            return;
        } else if (what == PenMessage.PNF_MSG_ENV_DATA) {
            PenData penData = (PenData) obj;
            int Pen_Station_Battery = (int) penData.Pen_Station_Battery;
            int Pen_Battery = (int) penData.Pen_Battery;

//			if(Pen_Battery == 0)
//			{
//				subMenuDeviceLayout.setChangeDevice2ProgressValuetoText("HIGH");
//			}
//			else
//			{
//				subMenuDeviceLayout.setChangeDevice2ProgressValuetoText("LOW");
//			}
        }

        packetCnt++;
        updatePacketCnt();
    }

    @Override
    public void onPenEvent(int what, int RawX, int RawY, Object obj) {

        if (MainDefine.penController == null) {
            addDebugText("PenController is not set");
            return;
        }

        PenData penData = (PenData) obj;
        PointF ptConv = MainDefine.penController.getCoordinatePosition(RawX, RawY, penData.bRight);
//		int penTemperature = penData.Pen_Temperature;
//		int penPressure = penData.Pen_Pressure;

        packetCnt++;

        updatePacketCnt();

        statusValueTextView.setText("" + what);
        rawXValueTextView.setText("" + RawX);
        rawYValueTextView.setText("" + RawY);
        convXValueTextView.setText("" + ptConv.x);
        convYValueTextView.setText("" + ptConv.y);

        /*
         * MARKER PEN STATE
         */
        if (MainDefine.penController.getModelCode() == 5 || MainDefine.penController.getModelCode() == 6) {
            switch (penData.MakerPenStatus) {
                case PenEvent.MARKERPEN_RED_MARKER:
                    smartMakerValueTextView.setText("RED");
                    break;
                case PenEvent.MARKERPEN_GREEN_MARKER:
                    smartMakerValueTextView.setText("GREEN");
                    break;
                case PenEvent.MARKERPEN_YELLOW_MARKER:
                    smartMakerValueTextView.setText("YELLOW");
                    break;
                case PenEvent.MARKERPEN_BLUE_MARKER:
                    smartMakerValueTextView.setText("BLUE");
                    break;
                case PenEvent.MARKERPEN_PURPLE_MARKER:
                    smartMakerValueTextView.setText("PURPLE");
                    break;
                case PenEvent.MARKERPEN_BLACK_MARKER:
                    smartMakerValueTextView.setText("BLACK");
                    break;
                case PenEvent.MARKERPEN_ERASER_CAP:
                    smartMakerValueTextView.setText("ERASER CAP");
                    break;
                case PenEvent.MARKERPEN_LOW_BATTERY:
                    smartMakerValueTextView.setText("LOW BATTERY");
                    break;
                case PenEvent.MARKERPEN_BIG_ERASER:
                    smartMakerValueTextView.setText("BIG ERASER");
                    break;
            }
        }

        switch (what) {
            case PenEvent.PEN_DOWN:
                downCnt++;
                break;
            case PenEvent.PEN_MOVE:
                moveCnt++;
                break;
            case PenEvent.PEN_UP:
                upCnt++;
                break;

            case PenEvent.PEN_HOVER:
                break;
            case PenEvent.PEN_HOVER_DOWN:
                break;
            case PenEvent.PEN_HOVER_MOVE:
                break;
        }

        updateDrawCnt();
    }
}

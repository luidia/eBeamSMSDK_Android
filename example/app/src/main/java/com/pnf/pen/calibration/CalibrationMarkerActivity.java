package com.pnf.pen.calibration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.luidia.ebeam.sdk.data.PenData;
import com.luidia.ebeam.sdk.constants.DeviceDirection;
import com.luidia.ebeam.sdk.constants.PenEvent;
import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenEventListener;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

import java.util.Timer;
import java.util.TimerTask;

public class CalibrationMarkerActivity extends Activity implements PenEventListener, PenMessageListener {
    final int DEVICE_CALIBRATION_MODE_START = 1;
    final int DEVICE_CALIBRATION_MODE_STOP = 2;
    final int DEVICE_CALIBRATION_SEND = 3;
    final int DEVICE_CALIBRATION_MODE_COMPLETE = 4;

    ImageView markerLeftImgView;
    ImageView markerTopImgView;
    ImageView markerRightImgView;
    ImageView markerBottomImgView;
    ImageView markerBothImgView;

    ImageView markerPoint1ImgView;
    ImageView markerPoint2ImgView;

    int devicePosition;
    int setCaliPosition;

    int deviceSendDataState = 0;

    int calibTimerCnt = 0;
    TimerTask calibTask = null;
    Timer calibTimer = null;

    PointF m_posCoordinate[] = new PointF[4];
    PointF m_posRestultPoint[] = new PointF[4];

    int m_nCoordinateCounter;

    boolean isPenStart = false;
    int RawX = 0;
    int RawY = 0;

    ProgressDialog progDialog;

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
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED, null);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        boolean isResultOK = resultCode == Activity.RESULT_OK?true:false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_marker);

        markerLeftImgView = (ImageView) findViewById(R.id.markerLeftImgView);
        markerTopImgView = (ImageView) findViewById(R.id.markerTopImgView);
        markerRightImgView = (ImageView) findViewById(R.id.markerRightImgView);
        markerBottomImgView = (ImageView) findViewById(R.id.markerBottomImgView);
        markerBothImgView = (ImageView) findViewById(R.id.markerBothImgView);

        markerPoint1ImgView = (ImageView) findViewById(R.id.markerPoint1ImgView);
        markerPoint2ImgView = (ImageView) findViewById(R.id.markerPoint2ImgView);

        m_posCoordinate[0] = new PointF(0, 0);
        m_posCoordinate[1] = new PointF(MainDefine.iDisGetWidth, 0);
        m_posCoordinate[2] = new PointF(MainDefine.iDisGetWidth, MainDefine.iDisGetHeight);
        m_posCoordinate[3] = new PointF(0, MainDefine.iDisGetHeight);

        devicePosition = MainDefine.penController.getStationPosition();
        setCaliPosition = devicePosition;

        setCalibrationSendStart();

        markerPositionUpdate();
    }

    void markerPositionUpdate(){
        m_nCoordinateCounter = 0;
        isPenStart = false;

        int markerLeftResID = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_LEFT?R.drawable.marker_left_p:R.drawable.marker_left;
        float markerLeftAlpha = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_LEFT?1.0f:0.4f;

        int markerTopResID = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_TOP?R.drawable.marker_top_p:R.drawable.marker_top;
        float markerTopAlpha = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_TOP?1.0f:0.4f;

        int markerRightResID = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_RIGHT?R.drawable.marker_left_p:R.drawable.marker_left;
        float markerRightAlpha = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_RIGHT?1.0f:0.4f;

        int markerBottomResID = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_BOTTOM?R.drawable.marker_bottom_p:R.drawable.marker_bottom;
        float markerBottomAlpha = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_BOTTOM?1.0f:0.4f;

        int markerBothResID = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_BOTH?R.drawable.marker_left_p:R.drawable.marker_left;
        float markerBothAlpha = setCaliPosition==DeviceDirection.DEVICE_DIRECTION_BOTH?1.0f:0.4f;

        markerLeftImgView.setImageResource(markerLeftResID);
        markerLeftImgView.setAlpha(markerLeftAlpha);

        markerTopImgView.setImageResource(markerTopResID);
        markerTopImgView.setAlpha(markerTopAlpha);

        markerRightImgView.setImageResource(markerRightResID);
        markerRightImgView.setAlpha(markerRightAlpha);

        markerBottomImgView.setImageResource(markerBottomResID);
        markerBottomImgView.setAlpha(markerBottomAlpha);

        markerBothImgView.setImageResource(markerBothResID);
        markerBothImgView.setAlpha(markerBothAlpha);

        markerPoint1ImgView.setVisibility(View.VISIBLE);
        markerPoint2ImgView.setVisibility(View.GONE);
    }

    public void markerRetryClicked(View v){
        markerPositionUpdate();
    }

    public void markerCloseClicked(View v){
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public void markerLeftClicked(View v){
        if(setCaliPosition != DeviceDirection.DEVICE_DIRECTION_LEFT){
            setCaliPosition = DeviceDirection.DEVICE_DIRECTION_LEFT;
            MainDefine.penController.setStationPosition(setCaliPosition);

            markerPositionUpdate();
        }
    }

    public void markerTopClicked(View v){
        if(setCaliPosition != DeviceDirection.DEVICE_DIRECTION_TOP){
            setCaliPosition = DeviceDirection.DEVICE_DIRECTION_TOP;
            MainDefine.penController.setStationPosition(setCaliPosition);

            markerPositionUpdate();
        }
    }

    public void markerRightClicked(View v){
        if(setCaliPosition != DeviceDirection.DEVICE_DIRECTION_RIGHT){
            setCaliPosition = DeviceDirection.DEVICE_DIRECTION_RIGHT;
            MainDefine.penController.setStationPosition(setCaliPosition);

            markerPositionUpdate();
        }
    }

    public void markerBottomClicked(View v){
        if(setCaliPosition != DeviceDirection.DEVICE_DIRECTION_BOTTOM){
            setCaliPosition = DeviceDirection.DEVICE_DIRECTION_BOTTOM;
            MainDefine.penController.setStationPosition(setCaliPosition);

            markerPositionUpdate();
        }
    }

    public void markerBothClicked(View v){
        if(setCaliPosition != DeviceDirection.DEVICE_DIRECTION_BOTH){
            setCaliPosition = DeviceDirection.DEVICE_DIRECTION_BOTH;
            MainDefine.penController.setStationPosition(setCaliPosition);

            markerPositionUpdate();
        }
    }

     void setCaliPoint(int position){
        if(position < 2){
            markerPoint1ImgView.setVisibility(View.GONE);
            markerPoint2ImgView.setVisibility(View.VISIBLE);
        }else{
            coordinateComplete();
        }
    }

    void coordinateComplete(){
        if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_RIGHT){
            m_posRestultPoint[0] = new PointF(m_posRestultPoint[3].x,m_posRestultPoint[1].y);
            m_posRestultPoint[2] = new PointF(m_posRestultPoint[1].x,m_posRestultPoint[3].y);
        }else{
            m_posRestultPoint[1] = new PointF(m_posRestultPoint[2].x,m_posRestultPoint[0].y);
            m_posRestultPoint[3] = new PointF(m_posRestultPoint[0].x,m_posRestultPoint[2].y);
        }

        progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("calibration setting.");
        progDialog.show();

        deviceSendDataState = DEVICE_CALIBRATION_SEND;
        MainDefine.penController.setCalibrationSendData(
                setCaliPosition,
                m_posRestultPoint[0],
                m_posRestultPoint[3],
                m_posRestultPoint[2],
                m_posRestultPoint[1]);

        startCalibrationCheckTimer();
    }

    void setCalibrationSendStart(){
        MainDefine.penController.startCalibMode();

        progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("start calibration.");
        progDialog.show();

        deviceSendDataState = DEVICE_CALIBRATION_MODE_START;
        startCalibrationCheckTimer();
    }

    void setCalibrationSendStop(){
        stopCalibrationCheckTimer();

        MainDefine.penController.stopCalibMode();

        deviceSendDataState = DEVICE_CALIBRATION_MODE_STOP;
        startCalibrationCheckTimer();
    }

    void setCalibrationSendComplete(){
        MainDefine.penController.stopCalibMode();

        deviceSendDataState = DEVICE_CALIBRATION_MODE_COMPLETE;
        startCalibrationCheckTimer();
    }

    public void stopCalibrationCheckTimer(){
        if(calibTask != null){
            calibTask.cancel();
            calibTask = null;
        }

        if(calibTimer != null){
            calibTimer.cancel();
//			calibTimer.purge();
            calibTimer = null;
        }
    }

    public void startCalibrationCheckTimer(){
        if(calibTimer == null){
            calibTimerCnt = 0;

            calibTimer = new Timer();
            calibTask = new TimerTask() {
                @Override
                public void run() {
                    calibHandler.sendEmptyMessage(deviceSendDataState);
                }
            };
            calibTimer.schedule(calibTask, 1000 * 10);
        }
    }

    /*
    limit time 10sec
     */
    @SuppressLint("HandlerLeak")
    Handler calibHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case DEVICE_CALIBRATION_MODE_START:
                    deviceSendDataState = 0;

                    setCalibrationSendStop();
                    break;
                case DEVICE_CALIBRATION_MODE_STOP:
                    deviceSendDataState = 0;
                    if(progDialog != null) {
                        progDialog.dismiss();
                        progDialog = null;
                    }

                    setResult(RESULT_CANCELED, null);
                    finish();
                    break;

                case DEVICE_CALIBRATION_SEND:
                    deviceSendDataState = 0;

                    setCalibrationSendStop();
                    break;

                case DEVICE_CALIBRATION_MODE_COMPLETE:
                    deviceSendDataState = 0;

                    setCalibrationSendStop();
                    break;
            }
        }
    };

    @Override
    public void onPenMessage(int what, int arg1, int arg2, Object obj) {
        switch(what)
        {
            /*
             * BT STATUS
             */
            case PenMessage.PNF_MSG_DISCONNECTED:
            case PenMessage.PNF_MSG_FAIL_LISTENING:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
            case PenMessage.PNF_MSG_DI_OK:
                stopCalibrationCheckTimer();

                if(deviceSendDataState == DEVICE_CALIBRATION_MODE_START){
                    if(progDialog != null){
                        progDialog.dismiss();
                        progDialog = null;
                    }
                }else if(deviceSendDataState == DEVICE_CALIBRATION_MODE_STOP){
                    if(progDialog != null) {
                        progDialog.dismiss();
                        progDialog = null;
                    }

                    setResult(RESULT_CANCELED, null);
                    finish();
                }
                else if(deviceSendDataState == DEVICE_CALIBRATION_SEND){
                    setCalibrationSendComplete();
                }else if(deviceSendDataState == DEVICE_CALIBRATION_MODE_COMPLETE){
                    if(progDialog != null) {
                        progDialog.dismiss();
                        progDialog = null;
                    }

                    RectF resultRect = new RectF(
                            m_posRestultPoint[0].x ,
                            m_posRestultPoint[0].y ,
                            m_posRestultPoint[2].x ,
                            m_posRestultPoint[2].y);

                    Point rectCenter = new Point((int) resultRect.centerX() ,(int) resultRect.centerY());

                    int rectWid = (int) (resultRect.width());
                    int rectHei = (int) (resultRect.height());

                    RectF calibRect = new RectF(
                            rectCenter.x - rectWid/2.0f ,
                            rectCenter.y - rectHei/2.0f ,
                            rectCenter.x + rectWid/2.0f ,
                            rectCenter.y + rectHei/2.0f);

                    MainDefine.penController.setCalibrationData(m_posCoordinate, 0, calibRect);

                    setResult(RESULT_OK, null);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onPenEvent(int what, int penX, int penY ,Object obj)
    {
        if(what == PenEvent.PEN_DOWN || what == PenEvent.PEN_MOVE)
        {
            isPenStart = true;

            RawX = penX;
            RawY = penY;
        }else if(what == PenEvent.PEN_UP)
        {
            if(!isPenStart) return;
            isPenStart = false;

            if(m_nCoordinateCounter < 2)
            {
                if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_BOTH){
                    PenData penData = (PenData)obj;

                    if(!penData.bRight){
                        return;
                    }else{
                        //정상 데이타
                        if(m_nCoordinateCounter == 0){

                        }else{
                            if(RawX < m_posRestultPoint[0].x ||
                                    RawY < m_posRestultPoint[0].y
                                    ){
                                return;
                            }
                        }
                    }

                    if(m_nCoordinateCounter == 0){
                        m_posRestultPoint[0] = new PointF(RawX ,RawY);
                    }else if(m_nCoordinateCounter == 1){
                        m_posRestultPoint[2] = new PointF(RawX ,RawY);
                    }
                }else{
                    if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_LEFT){
                        PenData penData = (PenData)obj;

                        if(!penData.bRight){
//								//반대 데이타
                            return;
                        }else{
                            //정상 데이타
                            if(m_nCoordinateCounter == 0){

                            }else{
                                if(RawX < m_posRestultPoint[0].x ||
                                        RawY < m_posRestultPoint[0].y
                                        ){
                                    return;
                                }
                            }
                        }

                    }else if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_RIGHT){
                        PenData penData = (PenData)obj;

                        if(penData.bRight){
                            //반대 데이타
                            return;
                        }else{
                            //정상 데이타
                            if(m_nCoordinateCounter == 0){

                            }else{
                                if(RawX > m_posRestultPoint[1].x ||
                                        RawY < m_posRestultPoint[1].y
                                        ){
                                    return;
                                }
                            }
                        }

                    }else if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_TOP){
                        PenData penData = (PenData)obj;

                        if(!penData.bRight){
                            //반대 데이타
                            return;
                        }


                        //정상 데이타
                        if(m_nCoordinateCounter == 0){

                        }else{
                            if(RawX < m_posRestultPoint[0].x ||
                                    RawY < m_posRestultPoint[0].y
                                    ){
                                return;
                            }
                        }
                    }else if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_BOTTOM){
                        PenData penData = (PenData)obj;

                        if(!penData.bRight){
                            //반대 데이타
                            return;
                        }

                        //정상 데이타
                        if(m_nCoordinateCounter == 0){

                        }else{
                            if(RawX < m_posRestultPoint[0].x ||
                                    RawY < m_posRestultPoint[0].y
                                    ){
                                return;
                            }
                        }
                    }

                    if(m_nCoordinateCounter == 0){
                        if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_RIGHT){
                            m_posRestultPoint[1] = new PointF(RawX ,RawY);
                        }else{
                            m_posRestultPoint[0] = new PointF(RawX ,RawY);
                        }
                    }else if(m_nCoordinateCounter == 1){
                        if(setCaliPosition == DeviceDirection.DEVICE_DIRECTION_RIGHT){
                            m_posRestultPoint[3] = new PointF(RawX ,RawY);
                        }else{
                            m_posRestultPoint[2] = new PointF(RawX ,RawY);
                        }
                    }
                }

                m_nCoordinateCounter++;
                setCaliPoint(m_nCoordinateCounter);
            }
        }
    }
}

package com.pnf.pen.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

import java.util.Timer;
import java.util.TimerTask;

public class DeviceChangeNameViewActivity extends Activity implements PenMessageListener {
    EditText changeNameEditText;
    ProgressDialog progDialog;

    TimerTask checkTask = null;
    Timer checkTimer = null;

    @Override
    protected void onResume() {
        super.onResume();

        MainDefine.penController.setPenEventListener(null);
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
        setContentView(R.layout.activity_device_change_name_view);

        changeNameEditText = (EditText) findViewById(R.id.changeNameEditText);

        changeNameEditText.setText(MainDefine.penController.getStationName());
    }


    public void setEditTextFocusClicked(View v){
        changeNameEditText.setFocusableInTouchMode(true);
        changeNameEditText.requestFocus();
    }

    public void changeNameClicked(View v){
        setChangeNameSend();
    }

    public void closeClicked(View v){
        setResult(RESULT_CANCELED, null);
        finish();
    }

    void setChangeNameSend(){
        String changeDeviceName = changeNameEditText.getText().toString().trim();
        if(!changeDeviceName.isEmpty()){
            MainDefine.penController.setStationName(changeDeviceName);

            progDialog = new ProgressDialog(this);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setMessage("device name changing.");
            progDialog.show();

            startCheckTimer();
        }
    }

    public void stopCheckTimer(){
        if(checkTask != null){
            checkTask.cancel();
            checkTask = null;
        }

        if(checkTimer != null){
            checkTimer.cancel();
//			checkTimer.purge();
            checkTimer = null;
        }
    }

    public void startCheckTimer(){
        if(checkTimer == null){
            checkTimer = new Timer();
            checkTask = new TimerTask() {
                @Override
                public void run() {
                    calibHandler.sendEmptyMessage(0);
                }
            };
            checkTimer.schedule(checkTask, 1000 * 10);
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
            if(progDialog != null) {
                progDialog.dismiss();
                progDialog = null;
            }

            Toast.makeText(
                    getApplicationContext(),
                    "fail.",
                    Toast.LENGTH_SHORT)
                    .show();

            setResult(RESULT_CANCELED, null);
            finish();
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
                stopCheckTimer();

                setResult(RESULT_OK, null);
                finish();
                break;
        }
    }
}

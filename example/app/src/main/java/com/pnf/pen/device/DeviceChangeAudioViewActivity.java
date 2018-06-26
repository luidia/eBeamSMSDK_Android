package com.pnf.pen.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.luidia.ebeam.sdk.constants.PenMessage;
import com.luidia.ebeam.sdk.listener.PenMessageListener;
import com.pnf.pen.test.MainDefine;
import com.pnf.pen.test.R;

import java.util.Timer;
import java.util.TimerTask;

public class DeviceChangeAudioViewActivity extends Activity implements PenMessageListener {
    TextView currentLangText;
    ListView devicesAudioList;

    DeviceAudioAdapter deviceAudioAdapter;

    ProgressDialog progDialog;

    int smLangCode = 0;

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
        setContentView(R.layout.activity_device_change_audio_view);

        currentLangText = (TextView) findViewById(R.id.currentLangText);
        devicesAudioList = (ListView) findViewById(R.id.devicesAudioList);

        deviceAudioAdapter = new DeviceAudioAdapter(getApplicationContext());
        int[] colors = {
                0,
                0xFFdedede,
                0xFFdedede};
        devicesAudioList.setDivider(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors));
        devicesAudioList.setDividerHeight(1);
        devicesAudioList.setVerticalFadingEdgeEnabled(false);

        devicesAudioList.setAdapter(deviceAudioAdapter);
        devicesAudioList.setOnItemClickListener(mDeviceClickListener);

        smLangCode = MainDefine.penController.getLanguageCode();
        currentLangText.setText("current language : "+deviceAudioAdapter.AUDIO_LANGUAGE[smLangCode]);
    }

    public void menuCloseBtnClicked(View v){
        setResult(RESULT_CANCELED, null);
        finish();
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

    AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(smLangCode == position){
                setResult(RESULT_CANCELED, null);
                finish();
            }else{
                progDialog = new ProgressDialog(DeviceChangeAudioViewActivity.this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage("device audio changing.");
                progDialog.show();

                MainDefine.penController.setDeviceAudio(position);

                startCheckTimer();
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
                stopCheckTimer();

                setResult(RESULT_OK, null);
                finish();
                break;
        }
    }

    class DeviceAudioAdapter extends BaseAdapter {
        final String[] AUDIO_LANGUAGE = new String[]{
                "ENGLISH" ,
                "KOREA" ,
                "SPANISH" ,
                "GERMAN" ,
                "FRENCH" ,
                "ITALIAN" ,
                "JAPANESE" ,
                "PROTUGUESE" ,
                "RUSSIAN" ,
                "CHINESE(SIMPLIFIED)" ,
                "CHINESE(TRADITIONAL)" ,
                "ARABIC"
        };

        LayoutInflater inflater;

        public DeviceAudioAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return AUDIO_LANGUAGE.length;
        }

        @Override
        public String getItem(int position)
        {
            return AUDIO_LANGUAGE[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.audio_device_element, null);
            }

            TextView languageName = ((TextView) vg.findViewById(R.id.languageName));
            languageName.setText(AUDIO_LANGUAGE[position]);

            return vg;
        }
    }
}


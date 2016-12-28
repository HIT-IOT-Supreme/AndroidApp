package com.dusz7.an.toothfairy.activity;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dusz7.an.toothfairy.R;
import com.dusz7.an.toothfairy.util.HTTPUtil;
import com.dusz7.an.toothfairy.util.InternetUtil;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


    //判断主activity是不是在最上层
    public static boolean isForeground = false;

    private TextView pushTest;
    private TextView clockTimeTextView;
    private ImageButton speakAssistantButton;
    private ImageButton clockButton;
    private ImageButton oneClickButton;
    private ImageButton sleepingButton;
    private ImageButton wakingButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //关于推送平台接收信息
        registerMessageReceiver();  // used for receive msg

        pushTest = (TextView)findViewById(R.id.push_test);
        clockTimeTextView = (TextView)findViewById(R.id.clock_time_text_view);
        speakAssistantButton = (ImageButton) findViewById(R.id.speak_assistant_button);
        clockButton = (ImageButton) findViewById(R.id.clock_button);
        oneClickButton = (ImageButton) findViewById(R.id.one_click_button);
        sleepingButton = (ImageButton)findViewById(R.id.sleeping_button);
        wakingButton = (ImageButton)findViewById(R.id.waking_button);

        speakAssistantButton.setImageDrawable(getResources().getDrawable(R.drawable.speak));
        speakAssistantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SpeakActivity.class);
                startActivity(intent);

            }
        });
        clockButton.setImageDrawable(getResources().getDrawable(R.drawable.clock));
        clockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        String showTime = "";
                        if(hourOfDay < 10){
                            showTime+="0";
                        }
                        showTime+=hourOfDay;
                        showTime+=" : ";
                        if(minute < 10){
                            showTime+="0";
                        }
                        showTime+=minute;
                        clockTimeTextView.setText(showTime);

                        String clockTime = hourOfDay+":"+minute+":"+"0";
                        final JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("time",clockTime);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        Thread addClockThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String urlStr = getResources().getString(R.string.my_url)+"clock/";
                                HTTPUtil addClock = new HTTPUtil(urlStr);
                                String result = addClock.postMethod(jsonObject.toString());
                                if(result.equals("OK")){
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this,"添加成功！",Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }else {
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this,"添加失败~",Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        });


                        if(InternetUtil.isNetworkConnected(MainActivity.this)){
                            addClockThread.start();
                        }

                    }
                },8,0,true).show();
            }
        });
        oneClickButton.setImageDrawable(getResources().getDrawable(R.drawable.lock));
        oneClickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Thread closeAllThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String urlStr = "";
                        urlStr = getResources().getString(R.string.my_url)+"control/"+"close_all/";

                        HTTPUtil closeAll = new HTTPUtil(urlStr);
                        String result = closeAll.getMethod();

                    }
                });
                if (InternetUtil.isNetworkConnected(MainActivity.this)){
                    closeAllThread.start();
                }
            }
        });
        sleepingButton.setImageDrawable(getResources().getDrawable(R.drawable.moon));
        sleepingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        wakingButton.setImageDrawable(getResources().getDrawable(R.drawable.waking));
        wakingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    //关于接收推送平台的自定义信息
    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.dusz7.an.toothfairy.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(messge + "\n");

                setCostomMsg(showMsg.toString());
            }
        }
    }

    private void setCostomMsg(String msg) {
        if (null != pushTest) {
//            pushTest.setText(msg);
//            pushTest.setVisibility(android.view.View.VISIBLE);
        }

        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vinn");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "90");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端

        //3.开始合成
        mTts.startSpeaking(msg, mSynListener);
    }
    //合成监听器
    SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            Log.d("stop","是结束了哦");
            final Thread tellItEndThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String urlStr = "";
                    urlStr = getResources().getString(R.string.my_url)+"control/"+"speech_end/";

                    HTTPUtil tellItEnd = new HTTPUtil(urlStr);
                    String result = tellItEnd.getMethod();

                }
            });
            if (InternetUtil.isNetworkConnected(MainActivity.this)){
                tellItEndThread.start();
            }
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }

    };
}

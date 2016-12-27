package com.dusz7.an.toothfairy.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dusz7.an.toothfairy.R;
import com.dusz7.an.toothfairy.util.PushUtil;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


public class MainActivity extends AppCompatActivity {


    //判断主activity是不是在最上层
    public static boolean isForeground = false;

    private TextView pushTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID +"=585e8f4f");

        registerMessageReceiver();  // used for receive msg

        pushTest = (TextView)findViewById(R.id.push_test);


//        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
//        SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(MainActivity.this, null);
//        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
//        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
//        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
//
//        //听写监听器
//        RecognizerListener mRecoListener = new RecognizerListener(){
//            //听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
//            //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
//            //关于解析Json的代码可参见MscDemo中JsonParser类；
//            //isLast等于true时会话结束。
//            public void onResult(RecognizerResult results, boolean isLast) {
//                Log.d("Result:",results.getResultString ());}
//            //会话发生错误回调接口
//            public void onError(SpeechError error) {
//                error.getPlainDescription(true); //获取错误码描述
//            }
//            //开始录音
//            public void onBeginOfSpeech() {}
//
//            @Override
//            public void onVolumeChanged(int i, byte[] bytes) {
//
//            }
//
//            //结束录音
//            public void onEndOfSpeech() {}
//            //扩展用接口
//            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
//        };
//
//        //3.开始听写
//        mIat.startListening(mRecoListener);

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


    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
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
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!PushUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
                setCostomMsg(showMsg.toString());
            }
        }
    }

    private void setCostomMsg(String msg){
        if (null != pushTest) {
            pushTest.setText(msg);
            pushTest.setVisibility(android.view.View.VISIBLE);
        }
    }
}

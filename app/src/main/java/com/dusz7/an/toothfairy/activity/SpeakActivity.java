package com.dusz7.an.toothfairy.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dusz7.an.toothfairy.R;
import com.dusz7.an.toothfairy.util.HTTPUtil;
import com.dusz7.an.toothfairy.util.InternetUtil;
import com.dusz7.an.toothfairy.util.SpeakJsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by dusz2 on 2016/12/28 0028.
 */

public class SpeakActivity extends AppCompatActivity {

    private ImageButton startButton;

    private TextView showTextView;

    private static String TAG = SpeakActivity.class.getSimpleName();
    private Toast mToast;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    SpeechRecognizer mAsr;
    private SpeechRecognizer mIat;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String myKey;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak);

        startButton = (ImageButton) findViewById(R.id.start_speaking_button);

        showTextView = (TextView)findViewById(R.id.show_content_textview);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);


        //语音听写
        mIat=SpeechRecognizer.createRecognizer(this,mInitListener);

        setParam();
        int ret=mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("失败,错误码：" + ret);
        } else {
            showTip("可以开始说话了~");
        }

        startButton.setImageDrawable(getResources().getDrawable(R.drawable.speak_icon));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setParam();
                int ret=mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("失败,错误码：" + ret);
                } else {
                    showTip("可以开始说话了~");
                }
            }
        });

    }

    /**
     * 听写监听器。
     */
    final RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
//                showTip("可以开始说话了");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
//                showTip("结束说话");
        }

        @Override
        public void onResult(final RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//                showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
        }
    };

    /**
     * 听写结果处理
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = SpeakJsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        final StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        showTextView.setText(resultBuffer.toString());

        if(resultBuffer.toString().equals("天气")){
            myKey = "weather";
        }else if(resultBuffer.toString().equals("知乎")){
            myKey = "zhihu";
        }else {
            myKey = "robot";
        }

        final Thread getAIThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String urlStr = "";
                if(!myKey.equals("robot")){
                    urlStr = getResources().getString(R.string.my_url)+myKey+"/";
                }else{
                    urlStr = getResources().getString(R.string.my_url)+myKey+"/"+ InternetUtil.urlToUTF8(resultBuffer.toString())+"/";
                }

                HTTPUtil getAI = new HTTPUtil(urlStr);
                String result = getAI.getMethod();
                String content = "";
                if(result != null && result != ""){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        content = jsonObject.getString("info");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
                SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(SpeakActivity.this, null);
                //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
                mTts.setParameter(SpeechConstant.VOICE_NAME, "vinn");//设置发音人
                mTts.setParameter(SpeechConstant.SPEED, "90");//设置语速
                mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端

                //3.开始合成
                mTts.startSpeaking(content, mSynListener);
            }
        });
        if (InternetUtil.isNetworkConnected(SpeakActivity.this)){
            getAIThread.start();
        }
    }

    //合成监听器
    SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
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

    /**
     * 初始化监听器
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("test", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败，错误码：" + code);
            }
        }
    };


    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1500");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

}

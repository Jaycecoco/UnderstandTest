package com.huangxiao.understandtest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.UnderstanderResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG="Understand_Activity";
    //语音语义识别
    private SpeechUnderstander mSpeechUnderstander;
    //文本语义识别
   // private TextUnderstander mTextUnderstand;
    private Toast mToast;
    private EditText editText;
    private Button button;

    @SuppressLint("ShowToast")
    protected void onCreate(Bundle savedInstanceState) {
        SpeechUtility.createUtility(MainActivity.this, "appid=" + "582d21ba");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
        mSpeechUnderstander=SpeechUnderstander.createUnderstander(MainActivity.this,mSpeechInitListener);
        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
    }

    //语音监听器初始化
    private InitListener mSpeechInitListener=new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG,"speechUnderstandListener init() code="+code);
            if(code != ErrorCode.SUCCESS){
                showTip("初始化失败"+code);
            }
        }
    };

    //showTip函数
    private void showTip( String str){
        mToast.setText(str);
        mToast.show();
    }

    //findView函数
    private void findView(){
        editText=(EditText)findViewById(R.id.edit_text);
        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    //点击button
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button:
                setParam();

                if(mSpeechUnderstander.isUnderstanding()){
                    mSpeechUnderstander.stopUnderstanding();
                    showTip("停止录音");
                }else {
                    int flag=mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
                    if(flag!=ErrorCode.SUCCESS){
                        showTip("语义解码失败"+flag);
                    }else{
                        showTip("请开始说话");
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 语义理解回调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                Log.d(TAG, result.getResultString());

                // 显示
                String text = result.getResultString();
                editText.setText(text);
                }
             else {
                showTip("识别结果不正确。");
            }
            // 读取json结果中的各种字段
            try {
                JSONObject resultJson = new JSONObject(result.getResultString());
               String text=resultJson.optString("text");
                Log.d(TAG,text);
                //应答码
                int rc=resultJson.optInt("rc");
                //服务类型，这里是提醒schedule
                String service=resultJson.optString("service");
                Log.d(TAG,service);
                //操作 这里是create 建立
                String operation=resultJson.optString("operation");
                Log.d(TAG,operation);
                //解析semantic Json 语义结构化表示
                String semantic=resultJson.optString("semantic");
                Log.d(TAG,semantic);
                JSONObject semanticJSon=new JSONObject(semantic);
                //slots
                String slots=semanticJSon.optString("slots");
                Log.d(TAG,slots);
                JSONObject slotsJSon=new JSONObject(slots);
                //提醒内容content
                String content=slotsJSon.optString("content");
                Log.d(TAG,content);

                String name=slotsJSon.optString("name");
                Log.d(TAG,name);

                String datetime=slotsJSon.optString("datetime");
                Log.d(TAG,datetime);
                JSONObject datetimeJSon=new JSONObject(datetime);

                String date=datetimeJSon.optString("date");
                Log.d(TAG,date);

                String dataOrig=datetimeJSon.optString("dateOrig");
                Log.d(TAG,dataOrig);

                String time=datetimeJSon.optString("time");
                Log.d(TAG,time);

                String timeOrig=datetimeJSon.optString("timeOrig");
                Log.d(TAG,timeOrig);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, data.length+"");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    private void setParam(){
        // 清空参数
        mSpeechUnderstander.setParameter(SpeechConstant.PARAMS, null);


        // 设置返回结果格式
        mSpeechUnderstander.setParameter(SpeechConstant.RESULT_TYPE, "json");

        mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");


        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }


}

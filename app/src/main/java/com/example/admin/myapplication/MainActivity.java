package com.example.admin.myapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.et_code)
    EditText etCode;
    @BindView(R.id.btn_getCode)
    Button btnGetCode;

    private static final int GET_SUCCESS = 1;//获取验证码成功
    private static final int SUBMIT_SUCCESS = 2;//验证成功
    private static final int CHECK_FAILE = 3;//检查失败
    private String mPhone;//手机号
    private String mCode;//验证码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBasic();
        initSMSSDK();
    }

    private void initBasic() {
        ButterKnife.bind(this);
        //Logger.init("MainActivity");
    }

    private void initSMSSDK() {
        //注册EventHandler监听，每次短信SDK操作回调,
        // 在EventHandler的4个回调方法都可能不在UI线程下，需要使用到消息处理机制。
        SMSSDK.registerEventHandler(new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                super.afterEvent(event, result, data);
                //判断返回的结果
                if(result == SMSSDK.RESULT_COMPLETE) {
                    //服务器返回成功
                    if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        mHandler.sendEmptyMessage(GET_SUCCESS );
                    }else if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //校验成功
                        mHandler.sendEmptyMessage(SUBMIT_SUCCESS);
                    }
                }else {
                    //服务器返回错误码
                    Throwable throwable = (Throwable) data;
                    Message msg = Message.obtain();
                    msg.what = CHECK_FAILE;
                    msg.obj = throwable;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    //获取验证码
    @OnClick(R.id.btn_getCode)
    public void getVerifyCode() {
        mPhone = etPhone.getText().toString();
        //判断手机号是否为空并且检查手机号的有效性
        if(!TextUtils.isEmpty(mPhone) && checkPhoneValid(mPhone)) {
            SMSSDK.getVerificationCode("86", mPhone);//申请验证码，结果都在EventHandler监听返回
            btnGetCode.setEnabled(false);//禁止按钮的可点击性
            mTimer.start();//开始倒计时
        }else {
           // Logger.d("请输入正确的手机号码");
        }
    }

    //验证验证码
    @OnClick(R.id.btn_verify)
    public void submitVerify() {
        mCode = etCode.getText().toString();
        //判断手机号和验证码都不为空
        if(!TextUtils.isEmpty(mCode) && !TextUtils.isEmpty(mPhone)) {
            SMSSDK.submitVerificationCode("86",mPhone,mCode);//提交验证信息，结果都在EventHandler监听返回
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_SUCCESS ://获取成功处理
                    //Logger.d("获取验证码成功....");
                    break;
                case SUBMIT_SUCCESS ://验证成功处理
                    //Logger.d("验证成功");
                    mTimer.cancel();
                    etPhone.setText("");
                    etCode.setText("");
                    break;
                case CHECK_FAILE://服务器返回错误处理
                    Throwable data = (Throwable) msg.obj;
                    //Logger.d(data.getMessage());
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 倒计时60s，使用CountDownTimer类，只需实现onTick()和onFinish()方法
     */
    private CountDownTimer mTimer = new CountDownTimer(60000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            //时间间隔固定回调该方法
            btnGetCode.setText(millisUntilFinished/1000+"s重新获取");
        }
        @Override
        public void onFinish() {
            //倒计时结束时，回调该方法
            btnGetCode.setText("重新获取");
            btnGetCode.setEnabled(true);
        }
    };

    /**
     * 检查手机号码
     */
    private boolean checkPhoneValid(String mobiles){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销所有EventHandler监听，避免内存泄露
        SMSSDK.unregisterAllEventHandler();
    }
}


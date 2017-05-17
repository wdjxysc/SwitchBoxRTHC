package com.rthc.switchboxrthc.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rthc.switchboxrthc.MyApplication;
import com.rthc.switchboxrthc.R;
import com.rthc.wdj.bluetoothtoollib.SwitchBox;
import com.rthc.wdj.bluetoothtoollib.cmd.Cmd;

public class AtyListActivity extends Activity {

    //UI
    AutoCompleteTextView scanNameEditText;

    Context context = this;

    ProgressBar progressBar;
    TextView noticeTextView;

    EditText relayEditText;
    CheckBox relayCheckBox;

    static SwitchBox switchBox;
    String bleNameHead = "SwitchBox_";

    SharedPreferences spScanDevice;

    final String SCAN_DEVICE = "SCAN_DEVICE";
    final String LAST_SCAN_DEVICE = "LAST_SCAN_DEVICE";

    RadioGroup radioGroup;


    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aty_list);

        //申请权限 及初始化
        request();

        initView();



        test();
    }

    void request(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            switchBox = new SwitchBox(context);
        }else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //未授权 请求用户授权
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }else{
                //已授权
                switchBox = new SwitchBox(context);
            }
        }
    }

    void test(){
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    switchBox = new SwitchBox(context);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    void initView() {
        scanNameEditText = (AutoCompleteTextView) findViewById(R.id.scanNameEditText);
        spScanDevice = context.getSharedPreferences(SCAN_DEVICE, Context.MODE_PRIVATE);
        scanNameEditText.setText(spScanDevice.getString(LAST_SCAN_DEVICE, ""));
//        initHistory(SCAN_NAME, scanNameEditText);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        noticeTextView = (TextView) findViewById(R.id.noticeTextView);

        relayEditText = (EditText) findViewById(R.id.relayEditText);
        relayCheckBox = (CheckBox) findViewById(R.id.relayCheckBox);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.fourBytesRB:
                        MyApplication.currentNodeIdType = Cmd.RF_NODE_ID_TYPE.NODE_ID_4_BYTES;
                        break;
                    case R.id.towBytesRB:
                        MyApplication.currentNodeIdType = Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES;
                        break;
                }
            }
        });

        initListener();
    }

    void initListener() {
        findViewById(R.id.scanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                noticeTextView.setText("正在扫描设备" + scanNameEditText.getText());

//                saveHistory(SCAN_NAME, scanNameEditText);
                hideSoftKeyboard();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭连接
                        switchBox.close();

                        //连接设备  设备的名字 超时时间
                        switchBox.setPackageItemsIntervalTime(300);
                        final boolean success = switchBox.scanDevice(getScanName(), 15000);

                        SharedPreferences.Editor editor = spScanDevice.edit();
                        editor.putString(LAST_SCAN_DEVICE, scanNameEditText.getText().toString());
                        editor.apply();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context, "已连接设备，可以进行操作", Toast.LENGTH_SHORT).show();
                                    noticeTextView.setText("已连接设备，可以进行操作" + scanNameEditText.getText().toString());

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
                                    noticeTextView.setText("连接失败");
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        (findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animZoomIn = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_in);
                findViewById(R.id.btn1).startAnimation(animZoomIn);
                Animation animZoomOut = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_out);
                findViewById(R.id.btn1).startAnimation(animZoomOut);
                if(relayCheckBox.isChecked()) {
                    try {
                        switchBox.setRelayInfo(relayCheckBox.isChecked(), Integer.parseInt(relayEditText.getText().toString()));
                    } catch (Exception ex) {
                        Toast.makeText(context, "中继号非法", Toast.LENGTH_SHORT).show();
                    }
                }
                MainActivity.switchBox = switchBox;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        (findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animZoomIn = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_in);
                findViewById(R.id.btn2).startAnimation(animZoomIn);
                Animation animZoomOut = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_out);
                findViewById(R.id.btn2).startAnimation(animZoomOut);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially();
                if(relayCheckBox.isChecked()) {
                    try {
                        switchBox.setRelayInfo(relayCheckBox.isChecked(), Integer.parseInt(relayEditText.getText().toString()));
                    } catch (Exception ex) {
                        Toast.makeText(context, "中继号非法", Toast.LENGTH_SHORT).show();
                    }
                }

                MeterSettingActivity.switchBox = switchBox;
                Intent intent = new Intent(getApplicationContext(), MeterSettingActivity.class);
                startActivity(intent);
            }
        });

        (findViewById(R.id.btn3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animZoomIn = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_in);
                findViewById(R.id.btn3).startAnimation(animZoomIn);
                Animation animZoomOut = AnimationUtils.loadAnimation(context, R.anim.btn_zoom_out);
                findViewById(R.id.btn3).startAnimation(animZoomOut);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially();
                if(relayCheckBox.isChecked()) {
                    try {
                        switchBox.setRelayInfo(relayCheckBox.isChecked(), Integer.parseInt(relayEditText.getText().toString()));
                    } catch (Exception ex) {
                        Toast.makeText(context, "中继号非法", Toast.LENGTH_SHORT).show();
                    }
                }

                BoxSettingActivity.switchBox = switchBox;
                Intent intent = new Intent(getApplicationContext(), BoxSettingActivity.class);
                startActivity(intent);
            }
        });



        relayCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relayCheckBox.isChecked()) {
                    relayEditText.setEnabled(true);
                } else {
                    relayEditText.setEnabled(false);
                }
            }
        });


        (findViewById(R.id.meterManagerBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RLMeterManagerActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 根据编号获得完整蓝牙名
     *
     * @return
     */
    String getScanName() {
        return bleNameHead + scanNameEditText.getText();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(scanNameEditText.getWindowToken(), 0); //强制隐藏键盘
    }

    /**
     * 是否在最近2s内已按下一次back键
     */
    boolean isOnKeyBack;

    Handler handler = new Handler();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(isOnKeyBack){
                this.switchBox.close();
            }else {
                Toast.makeText(this,"再按一次返回键退出",Toast.LENGTH_SHORT).show();
                isOnKeyBack = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isOnKeyBack = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

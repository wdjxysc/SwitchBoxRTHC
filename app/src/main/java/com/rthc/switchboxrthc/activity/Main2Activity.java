package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rthc.switchboxrthc.R;
import com.rthc.wdj.bluetoothtoollib.MeterHandler;
import com.rthc.wdj.bluetoothtoollib.SwitchBox;
import com.rthc.wdj.bluetoothtoollib.ValveHandler;
import com.rthc.wdj.bluetoothtoollib.cmd.BleCmd;
import com.rthc.wdj.bluetoothtoollib.cmd.Cmd;
import com.rthc.wdj.bluetoothtoollib.cmd.MeterStateConst;

import java.util.ArrayList;
import java.util.HashMap;

public class Main2Activity extends Activity{

    ProgressBar progressBar;
    TextView noticeTextView;

    EditText scanNameEditText;
    Button scanBtn;

    EditText meterIdEditText;
    Button readBtn;
    Button openBtn;
    Button closeBtn;

    EditText meterIdEditText1;
    Button readBtn1;
    Button openBtn1;
    Button closeBtn1;

    EditText meterIdEditText2;
    Button readBtn2;
    Button openBtn2;
    Button closeBtn2;



    Context context;

    SwitchBox switchBox;

    String TAG = "SwitchBox";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        context = this;

        noticeTextView = (TextView) findViewById(R.id.noticeTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        scanNameEditText = (EditText) findViewById(R.id.scanNameEditText);
        scanBtn = (Button) findViewById(R.id.scanBtn);

        meterIdEditText = (EditText) findViewById(R.id.meterIdEditText);
        readBtn = (Button) findViewById(R.id.readBtn);
        openBtn = (Button) findViewById(R.id.openBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);

        meterIdEditText1 = (EditText) findViewById(R.id.meterIdEditText1);
        readBtn1 = (Button) findViewById(R.id.readBtn1);
        openBtn1 = (Button) findViewById(R.id.openBtn1);
        closeBtn1 = (Button) findViewById(R.id.closeBtn1);

        meterIdEditText2 = (EditText) findViewById(R.id.meterIdEditText2);
        readBtn2 = (Button) findViewById(R.id.readBtn2);
        openBtn2 = (Button) findViewById(R.id.openBtn2);
        closeBtn2 = (Button) findViewById(R.id.closeBtn2);



        ArrayList<String> arrayList = new ArrayList<String>();

        //初始化
        try {
            switchBox = new SwitchBox(context);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

        initListener();
    }

    @Override
    protected void onDestroy() {
        switchBox.close();

        super.onDestroy();
    }

    private void initListener() {

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                noticeTextView.setText("正在扫描设备" + scanNameEditText.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭连接
                        switchBox.close();

                        //连接设备  设备的名字 超时时间
                        final boolean success = switchBox.scanDevice(scanNameEditText.getText().toString(), 15000);

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

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readValue(getMeterId(meterIdEditText), BleCmd.CTR_MODULE_ID_JIEXUN);
            }
        });

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openValve(getMeterId(meterIdEditText), BleCmd.CTR_MODULE_ID_JIEXUN);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeValve(getMeterId(meterIdEditText), BleCmd.CTR_MODULE_ID_JIEXUN);
            }
        });


        readBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readValue(getMeterId(meterIdEditText1),BleCmd.CTR_MODULE_ID_SKYSHOOT);
            }
        });

        openBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openValve(getMeterId(meterIdEditText1), BleCmd.CTR_MODULE_ID_SKYSHOOT);
            }
        });

        closeBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeValve(getMeterId(meterIdEditText1),BleCmd.CTR_MODULE_ID_SKYSHOOT);
            }
        });


        readBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readValue(getMeterId(meterIdEditText2),BleCmd.CTR_MODULE_ID_LIERDA);
            }
        });

        openBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openValve(getMeterId(meterIdEditText2),BleCmd.CTR_MODULE_ID_LIERDA);

            }
        });

        closeBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeValve(getMeterId(meterIdEditText2),BleCmd.CTR_MODULE_ID_LIERDA);
            }
        });
    }

    /**
     * 抄表
     */
    void readValue(String meterId,int moduleId) {
        if (meterId == null) return;
        progressBar.setVisibility(View.VISIBLE);
        noticeTextView.setText("抄表中...");
        switchBox.readMeter(meterId, moduleId, new MeterHandler() {

            @Override
            public int callback(final float result,final HashMap map) {
                Log.i(TAG, "得到结果" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "成功，结果：" + result, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        String valveStateStr = "";
                        if (map.get(Cmd.KEY_VALVE_STATE) == MeterStateConst.STATE_VALVE.OPEN) {
                            valveStateStr = "开";
                        } else if (map.get(Cmd.KEY_VALVE_STATE) == MeterStateConst.STATE_VALVE.CLOSE) {
                            valveStateStr = "关";
                        } else {
                            valveStateStr = "异常";
                        }

                        String power36Str;
                        if (map.get(Cmd.KEY_BATTERY_3_6_STATE) == MeterStateConst.STATE_POWER_3_6_V.LOW) {
                            power36Str = "低";
                        } else {
                            power36Str = "正常";
                        }

                        String power6Str;
                        if (map.get(Cmd.KEY_BATTERY_6_STATE) == MeterStateConst.STATE_POWER_6_V.LOW) {
                            power6Str = "低";
                        } else {
                            power6Str = "正常";
                        }

                        String str = "成功，结果：" + result
                                + "\n阀门状态:" + valveStateStr
                                + "\n3.6V电压:" + power36Str
                                + "\n6V电压:" + power6Str;

                        noticeTextView.setText(str);
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        noticeTextView.setText("超时");
                    }
                });
            }
        });
    }

    /**
     * 开阀
     */
    void openValve(String meterId,int moduleId) {
        if (meterId == null) return;
        progressBar.setVisibility(View.VISIBLE);
        noticeTextView.setText("开阀中...");
        switchBox.openValve(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success){
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            noticeTextView.setText("成功");
                        }else{
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            noticeTextView.setText("成功");
                        }

                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        noticeTextView.setText("超时");
                    }
                });
            }
        });
    }


    /**
     * 关阀
     */
    void closeValve(String meterId, int moduleId) {
        if (meterId == null) return;
        progressBar.setVisibility(View.VISIBLE);
        noticeTextView.setText("关阀中...");
        switchBox.closeValve(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, "关阀成功", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            noticeTextView.setText("关阀成功");
                        } else {
                            Toast.makeText(context, "关阀失败", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            noticeTextView.setText("关阀失败");
                        }
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        noticeTextView.setText("超时");
                    }
                });
            }
        });
    }

//    /**
//     * 获取模块ID
//     * @return
//     */
//    int getModuleId() {
//        int bleModuleType = BleCmd.CTR_MODULE_ID_JIEXUN;
//
//        int index = moduleIdSpinner.getSelectedItemPosition();
//        switch (index) {
//            case 0:
//                bleModuleType = BleCmd.CTR_MODULE_ID_JIEXUN;
//                break;
//            case 1:
//                bleModuleType = BleCmd.CTR_MODULE_ID_SKYSHOOT;
//                break;
//            case 2:
//                bleModuleType = BleCmd.CTR_MODULE_ID_LIERDA;
//                break;
//        }
//
//        return bleModuleType;
//    }

    /**
     * 获取表ID
     * @return
     */
    String getMeterId(EditText editText) {
        String meterId = editText.getText().toString();
        if (meterId.length() != 14 && meterId.length() != 8) {
            Toast.makeText(context, "表号错误", Toast.LENGTH_SHORT).show();
            return null;
        }

        return meterId;
    }
}

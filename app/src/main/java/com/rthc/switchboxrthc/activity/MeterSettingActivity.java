package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rthc.switchboxrthc.MyApplication;
import com.rthc.switchboxrthc.R;
import com.rthc.wdj.bluetoothtoollib.MeterHandler;
import com.rthc.wdj.bluetoothtoollib.SwitchBox;
import com.rthc.wdj.bluetoothtoollib.ValveHandler;
import com.rthc.wdj.bluetoothtoollib.cmd.BleCmd;
import com.rthc.wdj.bluetoothtoollib.cmd.Cmd;
import com.rthc.wdj.bluetoothtoollib.cmd.MeterStateConst;

import java.util.HashMap;

public class MeterSettingActivity extends Activity {

    Button scanBtn;
    TextView noticeTextView;
    EditText scanNameEditText;

    EditText oldAddressIdEditText;
    EditText newAddressIdEditText;
    EditText nodeIdEditText;
    EditText meterValueEditText;
    EditText valveStateEditText;
    EditText batteryStateEditText;
    EditText netIdEditText;

    RadioButton valveStateOpenRadioBtn;
    RadioButton valveStateCloseRadioBtn;
    RadioButton valveStateErrorRadioBtn;

    Button readValueBtn;
    Button writeMeterIdBtn;
    Button writeMeterValueBtn;
    Button readMeterStateBtn;
    Button writeMeterStateBtn;
    Button writeMeterNetIdBtn;
    Button openBtn;
    Button closeBtn;
    Button resetMeterBtn;

    BtnClickListener listener;

    Handler handler = new Handler();

    ProgressBar progressBar1;

    Context context = this;

    public static SwitchBox switchBox;

    String TAG = "SwitchBox";

    final String SCAN_DEVICE = "SCAN_DEVICE";
    final String LAST_SCAN_DEVICE = "LAST_SCAN_DEVICE";
    SharedPreferences spScanDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_setting);

        //初始化
        try {
            //设置每包数据发送间隔
            switchBox.setPackageItemsIntervalTime(300);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

        InitView();
    }

    private void InitView() {

        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

        oldAddressIdEditText = (EditText) findViewById(R.id.oldAddressIdEditText);
        newAddressIdEditText = (EditText) findViewById(R.id.newAddressIdEditText);
        nodeIdEditText = (EditText) findViewById(R.id.nodeIdEditText);
        meterValueEditText = (EditText) findViewById(R.id.meterValueEditText);
        valveStateEditText = (EditText) findViewById(R.id.valveStateEditText);
        batteryStateEditText = (EditText) findViewById(R.id.batteryStateEditText);
        netIdEditText = (EditText) findViewById(R.id.netidEditText);


        valveStateCloseRadioBtn = (RadioButton) findViewById(R.id.valveStateCloseRadioBtn);
        valveStateOpenRadioBtn = (RadioButton) findViewById(R.id.valveStateOpenRadioBtn);
        valveStateErrorRadioBtn = (RadioButton) findViewById(R.id.valveStateErrorRadioBtn);

        readValueBtn = (Button) findViewById(R.id.readValueBtn);
        writeMeterIdBtn = (Button) findViewById(R.id.writeMeterIdBtn);
        writeMeterValueBtn = (Button) findViewById(R.id.writeMeterValueBtn);
        readMeterStateBtn = (Button) findViewById(R.id.readMeterStateBtn);
        writeMeterStateBtn = (Button) findViewById(R.id.writeMeterStateBtn);
        writeMeterNetIdBtn = (Button) findViewById(R.id.writeMeterNetIdBtn);
        openBtn = (Button) findViewById(R.id.openBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);
        resetMeterBtn = (Button) findViewById(R.id.resetMeterIdBtn);

        listener = new BtnClickListener();

        readValueBtn.setOnClickListener(listener);
        writeMeterIdBtn.setOnClickListener(listener);
        writeMeterValueBtn.setOnClickListener(listener);
        readMeterStateBtn.setOnClickListener(listener);
        writeMeterStateBtn.setOnClickListener(listener);
        writeMeterNetIdBtn.setOnClickListener(listener);
        openBtn.setOnClickListener(listener);
        closeBtn.setOnClickListener(listener);
        resetMeterBtn.setOnClickListener(listener);

        scanBtn = (Button) findViewById(R.id.scanBtn);
        noticeTextView = (TextView) findViewById(R.id.noticeTextView);

        scanNameEditText = (EditText) findViewById(R.id.scanNameEditText);
        spScanDevice = context.getSharedPreferences(SCAN_DEVICE, Context.MODE_PRIVATE);
        scanNameEditText.setText(spScanDevice.getString(LAST_SCAN_DEVICE, ""));


        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
            nodeIdEditText.setText(oldAddressIdEditText.getText().toString().substring(10, 14));
        } else {
            nodeIdEditText.setText(oldAddressIdEditText.getText().toString().substring(8, 14));
        }

        oldAddressIdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (oldAddressIdEditText.getText().toString().length() == 14) {
                    if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                        nodeIdEditText.setText(oldAddressIdEditText.getText().toString().substring(10, 14));
                    } else {
                        nodeIdEditText.setText(oldAddressIdEditText.getText().toString().substring(8, 14));
                    }

                    if (oldAddressIdEditText.getText().toString().toUpperCase().equals("AAAAAAAAAAAAAA")) {
                        nodeIdEditText.setText("FFFFFFFF");
                        nodeIdEditText.setEnabled(false);
                    } else {
                        nodeIdEditText.setEnabled(true);
                    }
                } else {
                    nodeIdEditText.setEnabled(true);
                }
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar1.setVisibility(View.VISIBLE);

                noticeTextView.setText("正在扫描设备" + scanNameEditText.getText());

//                saveHistory(SCAN_NAME, scanNameEditText);
                hideSoftKeyboard();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭连接
                        switchBox.close();

                        //连接设备  设备的名字 超时时间
                        final boolean success = switchBox.scanDevice(getScanName(), 15000);

                        SharedPreferences.Editor editor = spScanDevice.edit();
                        editor.putString(LAST_SCAN_DEVICE, scanNameEditText.getText().toString());
                        editor.apply();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    progressBar1.setVisibility(View.INVISIBLE);
                                    Toast.makeText(context, "已连接设备，可以进行操作", Toast.LENGTH_SHORT).show();
                                    noticeTextView.setText("已连接设备，可以进行操作" + scanNameEditText.getText().toString());
                                } else {
                                    progressBar1.setVisibility(View.INVISIBLE);
                                    Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
                                    noticeTextView.setText("连接失败");
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(scanNameEditText.getWindowToken(), 0); //强制隐藏键盘
    }


    private class BtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            final String meterAddressText = getOldMeterId();
            if (meterAddressText == null) return;

            final String nodeIdText = nodeIdEditText.getText().toString();

            try {
                Long.parseLong(nodeIdText, 16);
            } catch (Exception ex) {
                Toast.makeText(getApplication(), "输入错误", Toast.LENGTH_SHORT).show();
                return;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    showSVProgressHUD("正在操作...");
                }
            });

            final int viewid = view.getId();
            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (viewid == readValueBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            readMeter2(meterAddressText);
                        } else {
                            readMeter(meterAddressText);
                        }
                    } else if (viewid == writeMeterIdBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            setMeterId2(meterAddressText);
                        } else {
                            setMeterId(meterAddressText);
                        }
                    } else if (viewid == resetMeterBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            resetMeter2(meterAddressText);
                        } else {
                            resetMeter(meterAddressText);
                        }
                    } else if (viewid == writeMeterValueBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            writeMeterValue2(meterAddressText);
                        } else {
                            writeMeterValue(meterAddressText);
                        }
                    } else if (viewid == readMeterStateBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            readMeterState2(meterAddressText);
                        } else {
                            readMeterState(meterAddressText);
                        }
                    } else if (viewid == writeMeterStateBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            writeMeterState2(meterAddressText);
                        } else {
                            writeMeterState(meterAddressText);
                        }
                    } else if (viewid == writeMeterNetIdBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            writeMeterNetId2(meterAddressText);
                        } else {
                            writeMeterNetId(meterAddressText);
                        }

                    } else if (viewid == openBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            openMeterValve2(meterAddressText);
                        } else {
                            openMeterValve(meterAddressText);
                        }
                    } else if (viewid == closeBtn.getId()) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            closeMeterValve2(meterAddressText);
                        } else {
                            closeMeterValve(meterAddressText);
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 抄表
     *
     * @param meterAddressText
     */
    void readMeter(String meterAddressText) {
        switchBox.readMeter(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = showResult(hashMap);
                        if(success){
                            final String valuenowstr = hashMap.get(Cmd.KEY_VALUE_NOW).toString();
                            final MeterStateConst.STATE_VALVE valvestate = (MeterStateConst.STATE_VALVE) hashMap.get(Cmd.KEY_VALVE_STATE);
                            String battarystatestr = "";
                            if (hashMap.containsKey(Cmd.KEY_BATTERY_3_6_STATE) && hashMap.containsKey(Cmd.KEY_BATTERY_3_6_STATE)) {
                                //RTHC表含 3.6V 6V电池状态
                                battarystatestr = "3.6V:" + hashMap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + hashMap.get(Cmd.KEY_BATTERY_6_STATE).toString();
                            } else {
                                //Lierda表不含 3.6V 6V电池状态  含电压 及 温度
                                battarystatestr = "电压:" + hashMap.get(Cmd.KEY_BATTERY_VALUE).toString() + "|温度:" + hashMap.get(Cmd.KEY_TEMPERATURE);
                                battarystatestr = "";
                            }

                            final String finalBattarystatestr = battarystatestr;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    meterValueEditText.setText(valuenowstr);
                                    batteryStateEditText.setText(finalBattarystatestr);

                                    switch (valvestate) {
                                        case OPEN:
                                            valveStateOpenRadioBtn.setChecked(true);
                                            break;
                                        case CLOSE:
                                            valveStateCloseRadioBtn.setChecked(true);
                                            break;
                                        case ERROR:
                                            valveStateErrorRadioBtn.setChecked(true);
                                            break;
                                    }
                                }
                            });
                        }
                    }
                });


                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }

    /**
     * 抄表  2ID
     *
     * @param meterAddressText
     */
    void readMeter2(String meterAddressText) {
        switchBox.readMeter2(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = showResult(hashMap);

                        if(success){
                            final String valuenowstr = hashMap.get(Cmd.KEY_VALUE_NOW).toString();
                            final MeterStateConst.STATE_VALVE valvestate = (MeterStateConst.STATE_VALVE) hashMap.get(Cmd.KEY_VALVE_STATE);
                            String battarystatestr = "";
                            if (hashMap.containsKey(Cmd.KEY_BATTERY_3_6_STATE) && hashMap.containsKey(Cmd.KEY_BATTERY_3_6_STATE)) {
                                //RTHC表含 3.6V 6V电池状态
                                battarystatestr = "3.6V:" + hashMap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + hashMap.get(Cmd.KEY_BATTERY_6_STATE).toString();
                            } else {
                                //Lierda表不含 3.6V 6V电池状态  含电压 及 温度
                                battarystatestr = "电压:" + hashMap.get(Cmd.KEY_BATTERY_VALUE).toString() + "|温度:" + hashMap.get(Cmd.KEY_TEMPERATURE);
                                battarystatestr = "";
                            }

                            final String finalBattarystatestr = battarystatestr;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    meterValueEditText.setText(valuenowstr);
                                    batteryStateEditText.setText(finalBattarystatestr);

                                    switch (valvestate) {
                                        case OPEN:
                                            valveStateOpenRadioBtn.setChecked(true);
                                            break;
                                        case CLOSE:
                                            valveStateCloseRadioBtn.setChecked(true);
                                            break;
                                        case ERROR:
                                            valveStateErrorRadioBtn.setChecked(true);
                                            break;
                                    }
                                }
                            });
                        }
                    }
                });


                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 设置表ID
     *
     * @param meterAddressText
     */
    void setMeterId(String meterAddressText) {
        String newMeterId = getNewMeterId();
        if (newMeterId == null) return;

        //有两种写ID方式  1.广播写ID  2.知道原ID写新ID
        if (meterAddressText.toUpperCase().equals("AAAAAAAAAAAAAA")) {
            //此处使用专用广播命令 发送指令
            switchBox.writeMeterIdByBroadcast(newMeterId, getModuleId(newMeterId), new MeterHandler() {
                @Override
                public int callback(float v, final HashMap hashMap) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResult(hashMap);
                        }
                    });
                    return 0;
                }

                @Override
                public void timeOut() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("超时");
                        }
                    });
                }
            });
        } else {
            switchBox.writeMeterId(meterAddressText, newMeterId, getModuleId(meterAddressText), new MeterHandler() {
                @Override
                public int callback(float v, final HashMap hashMap) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResult(hashMap);
                        }
                    });
                    return 0;
                }

                @Override
                public void timeOut() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("超时");
                        }
                    });
                }
            });
        }
    }


    /**
     * 设置表ID   2ID
     *
     * @param meterAddressText
     */
    void setMeterId2(String meterAddressText) {
        String newMeterId = getNewMeterId();
        if (newMeterId == null) return;

        //有两种写ID方式  1.广播写ID  2.知道原ID写新ID
        if (meterAddressText.toUpperCase().equals("AAAAAAAAAAAAAA")) {
            //此处使用专用广播命令 发送指令
            switchBox.writeMeterIdByBroadcast2(newMeterId, getModuleId(newMeterId), new MeterHandler() {
                @Override
                public int callback(float v, final HashMap hashMap) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResult(hashMap);
                        }
                    });
                    return 0;
                }

                @Override
                public void timeOut() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("超时");
                        }
                    });
                }
            });
        } else {
            switchBox.writeMeterId2(meterAddressText, newMeterId, getModuleId(meterAddressText), new MeterHandler() {
                @Override
                public int callback(float v, final HashMap hashMap) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResult(hashMap);
                        }
                    });

                    return 0;
                }

                @Override
                public void timeOut() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("超时");
                        }
                    });
                }
            });
        }
    }

    /**
     * 复位表
     *
     * @param meterAddressText
     */
    void resetMeter(String meterAddressText) {
        String meterId = meterAddressText;
        //如果原表地址写的是AAAAAAAAAAAAAA 表明是在广播写表地址 此时可能要复位表 所以在此直接用新表ID作为目标表
        if (meterAddressText.toUpperCase().equals("AAAAAAAAAAAAAA")) {
            meterId = getNewMeterId();
        }
        meterId = getNewMeterId();//复位表操作直接使用新表ID
        switchBox.resetMeter(meterId, getModuleId(meterId), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 复位表  2ID
     *
     * @param meterAddressText
     */
    void resetMeter2(String meterAddressText) {
        String meterId = meterAddressText;
        //如果原表地址写的是AAAAAAAAAAAAAA 表明是在广播写表地址 此时可能要复位表 所以在此直接用新表ID作为目标表
        if (meterAddressText.toUpperCase().equals("AAAAAAAAAAAAAA")) {
            meterId = getNewMeterId();
        }
        meterId = getNewMeterId();//复位表操作直接使用新表ID
        switchBox.resetMeter2(meterId, getModuleId(meterId), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表底数
     *
     * @param meterAddressText
     */
    void writeMeterValue(String meterAddressText) {
        float value = Float.parseFloat(meterValueEditText.getText().toString());
        switchBox.writeMeterValue(meterAddressText, value, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表底数    2ID
     *
     * @param meterAddressText
     */
    void writeMeterValue2(String meterAddressText) {
        float value = Float.parseFloat(meterValueEditText.getText().toString());
        switchBox.writeMeterValue2(meterAddressText, value, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 读表状态
     *
     * @param meterAddressText
     */
    void readMeterState(String meterAddressText) {
        switchBox.readMeterState(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = showResult(hashMap);

                        if(success){
                            final MeterStateConst.STATE_VALVE valveState = (MeterStateConst.STATE_VALVE) hashMap.get(Cmd.KEY_VALVE_STATE);
                            final String batteryStateStr = "3.6V:" + hashMap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + hashMap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    switch (valveState) {
                                        case OPEN:
                                            valveStateOpenRadioBtn.setChecked(true);
                                            break;
                                        case CLOSE:
                                            valveStateCloseRadioBtn.setChecked(true);
                                            break;
                                        case ERROR:
                                            valveStateErrorRadioBtn.setChecked(true);
                                            break;
                                    }
                                    batteryStateEditText.setText(batteryStateStr);
                                }
                            });
                        }
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 读表状态   2ID
     *
     * @param meterAddressText
     */
    void readMeterState2(String meterAddressText) {
        switchBox.readMeterState2(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = showResult(hashMap);

                        if(success){
                            final MeterStateConst.STATE_VALVE valveState = (MeterStateConst.STATE_VALVE) hashMap.get(Cmd.KEY_VALVE_STATE);
                            final String batteryStateStr = "3.6V:" + hashMap.get(Cmd.KEY_BATTERY_3_6_STATE).toString() + "|6V:" + hashMap.get(Cmd.KEY_BATTERY_6_STATE).toString();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    switch (valveState) {
                                        case OPEN:
                                            valveStateOpenRadioBtn.setChecked(true);
                                            break;
                                        case CLOSE:
                                            valveStateCloseRadioBtn.setChecked(true);
                                            break;
                                        case ERROR:
                                            valveStateErrorRadioBtn.setChecked(true);
                                            break;
                                    }
                                    batteryStateEditText.setText(batteryStateStr);
                                }
                            });
                        }
                    }
                });


                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表状态
     *
     * @param meterAddressText
     */
    void writeMeterState(String meterAddressText) {
        //默认为正常关阀
        switchBox.writeMeterState(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表状态  2ID
     *
     * @param meterAddressText
     */
    void writeMeterState2(String meterAddressText) {
        //默认为正常关阀
        switchBox.writeMeterState2(meterAddressText, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表网络ID
     *
     * @param meterAddressText
     */
    void writeMeterNetId(String meterAddressText) {
        int netid = Integer.parseInt(netIdEditText.getText().toString(), 16);
        switchBox.writeMeterNetId(meterAddressText, netid, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });
                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 写表网络ID  2ID
     *
     * @param meterAddressText
     */
    void writeMeterNetId2(String meterAddressText) {
        int netid = Integer.parseInt(netIdEditText.getText().toString(), 16);
        switchBox.writeMeterNetId2(meterAddressText, netid, getModuleId(meterAddressText), new MeterHandler() {
            @Override
            public int callback(float v, final HashMap hashMap) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showResult(hashMap);
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 开阀
     *
     * @param meterAddressText
     */
    void openMeterValve(String meterAddressText) {
        switchBox.openValve(meterAddressText, getModuleId(meterAddressText), new ValveHandler() {
            @Override
            public int callback(final boolean b) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (b) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("成功");
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("失败");
                        }
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 开阀  2ID
     *
     * @param meterAddressText
     */
    void openMeterValve2(String meterAddressText) {
        switchBox.openValve2(meterAddressText, getModuleId(meterAddressText), new ValveHandler() {
            @Override
            public int callback(final boolean b) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (b) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("成功");
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("失败");
                        }
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }


    /**
     * 关阀
     *
     * @param meterAddressText
     */
    void closeMeterValve(String meterAddressText) {
        switchBox.closeValve(meterAddressText, getModuleId(meterAddressText), new ValveHandler() {
            @Override
            public int callback(final boolean b) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (b) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("成功");
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("失败");
                        }
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }

    /**
     * 关阀  2ID
     *
     * @param meterAddressText
     */
    void closeMeterValve2(String meterAddressText) {
        switchBox.closeValve2(meterAddressText, getModuleId(meterAddressText), new ValveHandler() {
            @Override
            public int callback(final boolean b) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (b) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("成功");
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            dismissSVProgressHUD("失败");
                        }
                    }
                });

                return 0;
            }

            @Override
            public void timeOut() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                        dismissSVProgressHUD("超时");
                    }
                });
            }
        });
    }

    public boolean showResult(HashMap map){
        if(map.containsKey(Cmd.KEY_SUCCESS)){
            if((int)map.get(Cmd.KEY_SUCCESS) == 1){
                Toast.makeText(context, "成功" + map.get(Cmd.KEY_DATA_BYTES_STR), Toast.LENGTH_LONG).show();
                dismissSVProgressHUD("成功");
                return true;
            }else if(map.containsKey(Cmd.KEY_ERR_MESSAGE)) {
                Toast.makeText(context, "失败--" + map.get(Cmd.KEY_ERR_MESSAGE) + map.get(Cmd.KEY_DATA_BYTES_STR), Toast.LENGTH_SHORT).show();
                dismissSVProgressHUD("失败");
            }else {
                Toast.makeText(context, "失败--" + map.get(Cmd.KEY_ERR_MESSAGE), Toast.LENGTH_SHORT).show();
                dismissSVProgressHUD("失败");
            }
        }else {
            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
            dismissSVProgressHUD("失败");
        }

        return false;
    }


    private void showSVProgressHUD(String noticeStr) {
        progressBar1.setVisibility(View.VISIBLE);
        noticeTextView.setText(noticeStr);
    }

    private void dismissSVProgressHUD(String noticeStr) {
        progressBar1.setVisibility(View.INVISIBLE);
        noticeTextView.setText(noticeStr);
    }

    /**
     * 根据表ID获取模块ID
     *
     * @return
     */
    int getModuleId(String meterId) {
        //默认为绿色模块
        int moduleId = BleCmd.CTR_MODULE_ID_SKYSHOOT;

        if (meterId.length() == 8) { //利尔达模块
            moduleId = BleCmd.CTR_MODULE_ID_LIERDA;
        } else if (meterId.length() == 14) {
            if (meterId.substring(4, 6).equals("16")) { //绿色模块
                moduleId = BleCmd.CTR_MODULE_ID_SKYSHOOT;
            } else if (meterId.substring(4, 6).equals("15")) { //蓝色模块
                moduleId = BleCmd.CTR_MODULE_ID_JIEXUN;
            }
        }

        return moduleId;
    }

    /**
     * 获取原表ID
     *
     * @return
     */
    String getOldMeterId() {
        String meterId = oldAddressIdEditText.getText().toString();
        if (meterId.length() != 14 && meterId.length() != 8) {
            Toast.makeText(this, "表号错误", Toast.LENGTH_SHORT).show();
            return null;
        }

        return meterId;
    }


    /**
     * 获取新表ID
     *
     * @return
     */
    String getNewMeterId() {
        String meterId = newAddressIdEditText.getText().toString();
        if (meterId.length() != 14 && meterId.length() != 8) {
            Toast.makeText(this, "表号错误", Toast.LENGTH_SHORT).show();
            return null;
        }

        return meterId;
    }


    String bleNameHead = "SwitchBox_";

    /**
     * 根据编号获得完整蓝牙名
     *
     * @return
     */
    String getScanName() {
        return bleNameHead + scanNameEditText.getText();
    }

}

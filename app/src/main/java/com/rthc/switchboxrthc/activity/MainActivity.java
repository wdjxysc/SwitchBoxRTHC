package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends Activity {

    public static final String SCAN_NAME = "scan_name";
    public static final String METER_ID = "meter_id";
    ProgressBar progressBar;
    TextView noticeTextView;

    AutoCompleteTextView meterIdEditText;

    AutoCompleteTextView scanNameEditText;
    Button scanBtn;

    Button readBtn;

    Button openBtn;

    Button closeBtn;
    Spinner moduleIdSpinner;

    ProgressBar progressBar1;
    ImageView imageView;
    TextView resultTextView;
    TextView dataValueTextView;
    TextView detailDataTextView;

    Button minusBtn;
    Button addBtn;

    Context context = this;

    public static SwitchBox switchBox;

    String TAG = "SwitchBox";

    String bleNameHead = "SwitchBox_";

    final String SCAN_DEVICE = "SCAN_DEVICE";
    final String LAST_SCAN_DEVICE = "LAST_SCAN_DEVICE";
    SharedPreferences spScanDevice;

    /**
     * ??????????????????????????????
     *
     * @param field
     * @param auto
     */
    private void initHistory(String field, AutoCompleteTextView auto) {
        SharedPreferences sp = getSharedPreferences("commad", 0);
        String longhistory = sp.getString("history", "nothing");
        String[] hisArrays = longhistory.split(",");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, hisArrays);
        //?????????????????????????????50????????????????
        if (hisArrays.length > 50) {
            String[] newArrays = new String[50];
            System.arraycopy(hisArrays, 0, newArrays, 0, 50);
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, newArrays);
        }
        auto.setAdapter(adapter);
        auto.setDropDownHeight(350);
        auto.setThreshold(1);
//        auto.setCompletionHint("???????????????5??????????????");
        auto.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                if (hasFocus) {
                    view.showDropDown();
                }
            }
        });
    }


    /**
     * ????????????????????????????
     *
     * @param field
     * @param auto
     */
    private void saveHistory(String field, AutoCompleteTextView auto) {
        String text = auto.getText().toString();
        SharedPreferences sp = getSharedPreferences("commad", Context.MODE_PRIVATE);
        String longhistory = sp.getString(field, "nothing");
        if (!longhistory.contains(text + ",")) {
            StringBuilder sb = new StringBuilder(longhistory);
            sb.insert(0, text + ",");
            sp.edit().putString(field, sb.toString()).apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        noticeTextView = (TextView) findViewById(R.id.noticeTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        scanNameEditText = (AutoCompleteTextView) findViewById(R.id.scanNameEditText);
        spScanDevice = context.getSharedPreferences(SCAN_DEVICE, Context.MODE_PRIVATE);
        scanNameEditText.setText(spScanDevice.getString(LAST_SCAN_DEVICE, ""));
//        initHistory(SCAN_NAME, scanNameEditText);
        scanBtn = (Button) findViewById(R.id.scanBtn);

        minusBtn = (Button) findViewById(R.id.minusBtn);
        addBtn = (Button) findViewById(R.id.addBtn);

        meterIdEditText = (AutoCompleteTextView) findViewById(R.id.meterIdEditText);
        initHistory("history", meterIdEditText);
        readBtn = (Button) findViewById(R.id.readBtn);
        openBtn = (Button) findViewById(R.id.openBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);

        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        imageView = (ImageView) findViewById(R.id.imageView);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        dataValueTextView = (TextView) findViewById(R.id.dataValueTextView);
        detailDataTextView = (TextView) findViewById(R.id.detailDataTextView);

        moduleIdSpinner = (Spinner) findViewById(R.id.moduleIdSpinner);

        ArrayList<String> arrayList = new ArrayList<String>();

        String[] strings = getResources().getStringArray(R.array.moduleArray);

        Collections.addAll(arrayList, strings);

        moduleIdSpinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayList));

        /**
         //初始化
         try {
         switchBox = new SwitchBox(context);
         } catch (Exception e) {
         Log.i(TAG,e.getMessage());
         }
         **/

        initListener();

        hideSoftKeyboard();
    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(scanNameEditText.getWindowToken(), 0); //强制隐藏键盘
    }

    private void initListener() {

        scanBtn.setOnClickListener(new View.OnClickListener() {
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
                        switchBox.setPackageItemsIntervalTime(500);
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


        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                saveHistory("history", meterIdEditText);

                if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                    readValue2();
                } else {
                    readValue();
                }
            }
        });

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                saveHistory("history", meterIdEditText);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            openValve2();
                        } else {
                            openValve();
                        }
                    }
                }).setNegativeButton("取消", null)
                        .setTitle("提示")
                        .setMessage("确定要开阀？").show();

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                saveHistory("history", meterIdEditText);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (MyApplication.currentNodeIdType == Cmd.RF_NODE_ID_TYPE.NODE_ID_2_BYTES) {
                            closeValve2();
                        } else {
                            closeValve();
                        }

                    }
                }).setNegativeButton("取消", null)
                        .setTitle("提示")
                        .setMessage("确定要关阀？").show();
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    long id = Long.parseLong(meterIdEditText.getText().toString());
                    if ((id + "").length() < 8) {
                        return;
                    }
                    id--;
                    meterIdEditText.setText(id + "");
                } catch (Exception e) {
                    Toast.makeText(context, "表号错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    long id = Long.parseLong(meterIdEditText.getText().toString());
                    if ((id + "").length() < 8) {
                        return;
                    }
                    id++;
                    meterIdEditText.setText(id + "");
                } catch (Exception e) {
                    Toast.makeText(context, "表号错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * 抄表
     */
    void readValue() {
        final String meterId = getMeterId();

        if (meterId == null) return;

        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在抄表...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.readMeter(meterId, moduleId, new MeterHandler() {

            @Override
            public int callback(final float result, final HashMap map) {
                Log.i(TAG, "得到结果" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result < 0) {
                            Toast.makeText(context, "失败：" + map.get(Cmd.KEY_ERR_MESSAGE), Toast.LENGTH_SHORT).show();
                            resultTextView.setText("失败");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        } else {
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

                            String dataStr = "" + result;
                            String str = "   阀门状态:" + valveStateStr
                                    + "\n3.6V电压:" + power36Str
                                    + "   6V电压:" + power6Str;
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                            resultTextView.setText("成功");
                            dataValueTextView.setText(dataStr);
                            detailDataTextView.setText(str);
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        }

                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });


        Log.i("rthc", "dafsdfasd");
    }


    /**
     * 抄表  2ID
     */
    void readValue2() {
        final String meterId = getMeterId();

        if (meterId == null) return;

        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在抄表...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.readMeter2(meterId, moduleId, new MeterHandler() {

            @Override
            public int callback(final float result, final HashMap map) {
                Log.i(TAG, "得到结果" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result < 0) {
                            Toast.makeText(context, "失败：" + map.get(Cmd.KEY_ERR_MESSAGE), Toast.LENGTH_SHORT).show();
                            resultTextView.setText("失败");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        } else {
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

                            String dataStr = "" + result;
                            String str = "   阀门状态:" + valveStateStr
                                    + "\n3.6V电压:" + power36Str
                                    + "   6V电压:" + power6Str;
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                            resultTextView.setText("成功");
                            dataValueTextView.setText(dataStr);
                            detailDataTextView.setText(str);
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        }

                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });


        Log.i("rthc", "dafsdfasd");
    }

    /**
     * 开阀
     */
    void openValve() {
        final String meterId = getMeterId();

        if (meterId == null) return;
        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在开阀...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.openValve(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("开阀成功");
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("开阀失败");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        }
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });
    }


    /**
     * 开阀  2ID
     */
    void openValve2() {
        final String meterId = getMeterId();

        if (meterId == null) return;
        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在开阀...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.openValve2(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("开阀成功");
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("开阀失败");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        }
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });
    }


    /**
     * 关阀
     */
    void closeValve() {
        final String meterId = getMeterId();

        if (meterId == null) return;
        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在关阀...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.closeValve(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("关阀成功");
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("关阀成功");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        }
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });
    }


    /**
     * 关阀   2ID
     */
    void closeValve2() {
        final String meterId = getMeterId();

        if (meterId == null) return;
        int moduleId = getModuleId(meterId);
        if (moduleId == 0) {
            Toast.makeText(this, "未知表号", Toast.LENGTH_SHORT).show();
            return;
        }
        resultTextView.setText("正在关阀...");
        detailDataTextView.setText("");
        dataValueTextView.setText("--.--");
        progressBar1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        switchBox.closeValve2(meterId, moduleId, new ValveHandler() {
            @Override
            public int callback(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("关阀成功");
                            imageView.setImageResource(R.mipmap.sign_check_icon);
                        } else {
                            Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                            resultTextView.setText("关阀成功");
                            imageView.setImageResource(R.mipmap.sign_error_icon);
                        }
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
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
                        resultTextView.setText("超时");
                        progressBar1.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(R.mipmap.sign_error_icon);
                    }
                });
            }
        });
    }

    /**
     * 获取模块ID
     *
     * @return
     */
    int getModuleId() {
        int bleModuleType = BleCmd.CTR_MODULE_ID_JIEXUN;

        int index = moduleIdSpinner.getSelectedItemPosition();
        switch (index) {
            case 0:
                bleModuleType = BleCmd.CTR_MODULE_ID_JIEXUN;
                break;
            case 1:
                bleModuleType = BleCmd.CTR_MODULE_ID_SKYSHOOT;
                break;
            case 2:
                bleModuleType = BleCmd.CTR_MODULE_ID_LIERDA;
                break;
        }

        return bleModuleType;
    }

    /**
     * 根据表ID获取模块ID
     *
     * @return
     */
    int getModuleId(String meterId) {
        int moduleId = 0;


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
     * 获取表ID
     *
     * @return
     */
    String getMeterId() {
        String meterId = meterIdEditText.getText().toString();
        if (meterId.length() != 14 && meterId.length() != 8) {
            Toast.makeText(context, "表号错误", Toast.LENGTH_SHORT).show();
            return null;
        }

        return meterId;
    }


    /**
     * 根据编号获得完整蓝牙名
     *
     * @return
     */
    String getScanName() {
        return bleNameHead + scanNameEditText.getText();
    }
}

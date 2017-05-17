package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rthc.switchboxrthc.R;
import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.bean.Meter;
import com.rthc.switchboxrthc.db.DBManager;
import com.rthc.switchboxrthc.svprogresshud.SVProgressHUD;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RLMeterInfoActivity extends Activity {

    TextView floorTextView;
    TextView houseIndexTextView;
    EditText houseNumEditText;
    EditText householderNameEditText;
    EditText meterIdEditText;
    EditText netIdEditText;

    Button setBtn;
    Button saveBtn;
    Button scanBtn;

    BtnOnClickListener btnOnClickListener = new BtnOnClickListener();

    House house;

    List<House> houseList;

    int index;

    Handler handler = new Handler();

    Context context;

    Thread thread;

//    SerialPortTool serialPortTool;

    DBManager dbManager;



    final int SCAN_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_info);

        context = this;

        initData();

        initView();
    }


    @Override
    protected void onDestroy() {
//        serialPortTool.closePort(SerialPortTool.PowerLevel.POWER_RFID);
        dbManager.closeDB();
        super.onDestroy();
    }

    private void initData() {

        dbManager = new DBManager(getApplication());

//        try {
//            serialPortTool = new SerialPortTool(SerialPortTool.HANDLE_COMM_PORT, SerialPortTool.HANDLE_COMM_PORT_BR);
//            serialPortTool.openPort(SerialPortTool.PowerLevel.POWER_RFID);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        house = getIntent().getParcelableExtra("House");

        houseList = getIntent().getParcelableArrayListExtra("HouseList");

        index = getIntent().getIntExtra("Index", -1);

    }

    private void initView() {
        floorTextView = (TextView) findViewById(R.id.floorTextView);
        houseIndexTextView = (TextView) findViewById(R.id.houseIndexTextView);
        houseNumEditText = (EditText) findViewById(R.id.houseNumEditText);
        householderNameEditText = (EditText) findViewById(R.id.householderNameEditText);
        meterIdEditText = (EditText) findViewById(R.id.meterIdEditText);
        netIdEditText = (EditText) findViewById(R.id.netIdEditText);
        setBtn = (Button) findViewById(R.id.setBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        scanBtn = (Button) findViewById(R.id.scanBtn);

        floorTextView.setText(house.floor);
        houseIndexTextView.setText(house.houseIndex + "");
        houseNumEditText.setText(house.houseNum);
        householderNameEditText.setText(house.householderName);
        if(house.meter.meterID != null) {
            if (house.meter.meterID.substring(0, 4).equals("2305")) {
                meterIdEditText.setText(house.meter.meterID.substring(4));
            }else {
                meterIdEditText.setText(house.meter.meterID);
            }
            netIdEditText.setText(house.meter.netID);
        }else {
            meterIdEditText.setText("");
            netIdEditText.setText(house.houseNetId);
        }

        setBtn.setOnClickListener(btnOnClickListener);
        saveBtn.setOnClickListener(btnOnClickListener);
        scanBtn.setOnClickListener(btnOnClickListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (thread != null) {
                    if (thread.getState() == Thread.State.TERMINATED) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("House", house);
                        bundle.putParcelableArrayList("HouseList", (ArrayList<House>) houseList);
                        bundle.putInt("Index", getIntent().getIntExtra("Index", -1));
                        intent.putExtras(bundle);
                        setResult(0, intent);

                        this.finish();
                    }
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("House", house);
                    bundle.putParcelableArrayList("HouseList", (ArrayList<House>) houseList);
                    bundle.putInt("Index", getIntent().getIntExtra("Index", -1));
                    intent.putExtras(bundle);
                    setResult(0, intent);

                    this.finish();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private class BtnOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.setBtn:
                    //do something...
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Set();
                        }
                    });
                    thread.start();
                    break;
                case R.id.saveBtn:
                    //do something...
                    Save();
                    break;
                case R.id.scanBtn:
                    Intent intent = new Intent(getApplicationContext(), ScanQrcodeActivity.class);
                    startActivityForResult(intent, SCAN_REQUEST_CODE );
                    break;
            }
        }
    }


    /**
     * 设置表netId
     */
    private void Set() {
        Meter meter = new Meter();

        String meterId = meterIdEditText.getText().toString();
        if (meterId.length() == 10) {
            if (meterId.substring(0, 2).equals("15")) {
                meterId = "2305" + meterId;//瑞泰恒创
            } else if(meterId.substring(0, 2).equals("16")) {
                meterId = "2305" + meterId;//瑞泰恒创 绿色模块
            } else {
                Toast.makeText(this,"表号非法", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (meterId.length() == 8) {
            if (meterId.substring(0, 2).equals("10")) {
                meterId = "" + meterId;//利尔达
            } else {
                Toast.makeText(this,"表号非法", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            return;
        }

        meter.meterID = meterId;
        meter.meterSerialNum = meter.meterID.substring(4);
        meter.netID = netIdEditText.getText().toString();
        meter.installDate = new Date();
        meter.isActivated = 1;


//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                SVProgressHUD.showWithStatus(context, "正在设置...");
//            }
//        });
//
//        //设置表模块参数 netId
//        boolean b;
//
//        b = Cmd.SetMeterNetId(meter, serialPortTool);
//
//        if (!b) {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    SVProgressHUD.showErrorWithStatus(context, "失败");
//                }
//            });
//        } else {
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    SVProgressHUD.showSuccessWithStatus(context, "成功");
//                }
//            });
//        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                SVProgressHUD.showSuccessWithStatus(context, "成功");
            }
        });

        //保存
        //数据库插入meter
        List<Meter> meterList = dbManager.queryMeter(meter.meterID);
        if (meterList.size() == 0) {
            meterList = new ArrayList<Meter>();
            meterList.add(meter);
            dbManager.addMeter(meterList);
        } else {
            meterList = new ArrayList<Meter>();
            meterList.add(meter);
            dbManager.updateMeter(meterList);
        }

        //数据库更新house信息
        house.houseNum = houseNumEditText.getText().toString();
        house.householderName = householderNameEditText.getText().toString();
        house.meter.meterID = meter.meterID;
        house.meter.netID = netIdEditText.getText().toString();
        house.meter.isActivated = 1;
        house.houseNetId = house.meter.netID;
        List<House> houseListUpdate = new ArrayList<>();
        houseListUpdate.add(house);
        dbManager.updateHouse(houseListUpdate);

        houseList.set(index,house);
    }


    /**
     * 根据输入表号生成相应的完整表号 存入数据库 成功后跳到下一户
     */
    public void Save() {

        Meter meter = new Meter();
        String meterId = meterIdEditText.getText().toString();
        if (meterId.length() == 10) {
            if (meterId.substring(0, 2).equals("15")) {
                meterId = "2305" + meterId;//瑞泰恒创
            } else if(meterId.substring(0, 2).equals("16")) {
                meterId = "2305" + meterId;//瑞泰恒创 绿色模块
            } else {
                Toast.makeText(this,"表号非法", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (meterId.length() == 8) {
            if (meterId.substring(0, 2).equals("10")) {
                meterId = "" + meterId;//利尔达
            } else {
                Toast.makeText(this,"表号非法", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            return;
        }

        meter.meterID = meterId;
        meter.meterSerialNum = meter.meterID.substring(4);
        meter.netID = netIdEditText.getText().toString();
        meter.installDate = new Date();
        meter.isActivated = 1;

        //数据库插入meter
        List<Meter> meterList = dbManager.queryMeter(meter.meterID);
        if (meterList.size() == 0) {
            meterList = new ArrayList<Meter>();
            meterList.add(meter);
            dbManager.addMeter(meterList);
        } else {
            meterList = new ArrayList<Meter>();
            meterList.add(meter);
            dbManager.updateMeter(meterList);
        }

        //数据库更新house信息
        house.houseNum = houseNumEditText.getText().toString();
        house.householderName = householderNameEditText.getText().toString();
        house.meter.meterID = meter.meterID.toString();
        house.meter.netID = netIdEditText.getText().toString();
        house.meter.isActivated = 1;
        house.houseNetId = house.meter.netID;
        List<House> houseListUpdate = new ArrayList<House>();
        houseListUpdate.add(house);
        dbManager.updateHouse(houseListUpdate);

        houseList.set(index,house);

        SVProgressHUD.showSuccessWithStatus(context, "保存成功");

        //跳转下一户
        if (index < houseList.size() - 1) {
            index++;

            house = houseList.get(index);

            floorTextView.setText(house.floor);
            houseIndexTextView.setText(house.houseIndex + "");
            houseNumEditText.setText(house.houseNum);
            householderNameEditText.setText(house.householderName);

            if(house.meter.meterID != null) {

                if (house.meter.meterID.substring(0, 4).equals("2305")) {
                    meterIdEditText.setText(house.meter.meterID.substring(4));
                }else {
                    meterIdEditText.setText(house.meter.meterID);
                }
                netIdEditText.setText(house.meter.netID);
            }else {
                meterIdEditText.setText("");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case SCAN_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    String result = data.getStringExtra("code");
                    meterIdEditText.setText(result);
                }
                break;
            default:
                break;
        }
    }
}

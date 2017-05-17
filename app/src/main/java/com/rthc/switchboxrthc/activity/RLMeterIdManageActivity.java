package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rthc.switchboxrthc.R;
import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.db.DBManager;

import java.util.ArrayList;
import java.util.List;

public class RLMeterIdManageActivity extends Activity {

    EditText buildingsIdEditText;
    EditText buildingsNameEditText;
    EditText banIdEditText;
    EditText unitEditText;
    EditText oneFloorHouseNumEditText;
    EditText floorBeginEditText;
    EditText floorEndEditText;
    EditText netIdEditText;

    Button filingBtn;


    BtnOnClickListener btnOnClickListener = new BtnOnClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_id_manage);

        initView();
    }

    private void initView() {
        buildingsIdEditText = (EditText) findViewById(R.id.buildingsIdEditText);
        buildingsNameEditText = (EditText) findViewById(R.id.buildingsNameEditText);
        banIdEditText = (EditText) findViewById(R.id.banIdEditText);
        unitEditText = (EditText) findViewById(R.id.unitEditText);
        oneFloorHouseNumEditText = (EditText) findViewById(R.id.oneFloorHouseNumEditText);
        floorBeginEditText = (EditText) findViewById(R.id.floorBeginEditText);
        floorEndEditText = (EditText) findViewById(R.id.floorEndEditText);

        netIdEditText = (EditText) findViewById(R.id.netIdEditText);

        filingBtn = (Button) findViewById(R.id.filingBtn);

        filingBtn.setOnClickListener(btnOnClickListener);
    }

    private class BtnOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.filingBtn:
                    filing();
                    break;
            }
        }
    }

    /**
     * 建档
     */
    private void filing() {
        String buildingsId = buildingsIdEditText.getText().toString();
        String buildingsName = buildingsNameEditText.getText().toString();
        String banId = banIdEditText.getText().toString();
        String unit = unitEditText.getText().toString();
        String oneFloorHouseNum = oneFloorHouseNumEditText.getText().toString();
        String beginFloor = floorBeginEditText.getText().toString();
        String endFloor = floorEndEditText.getText().toString();
        String netId = netIdEditText.getText().toString();
        int netIdInt = 0;
        try {
            netIdInt = Integer.parseInt(netId);
        } catch (Exception ex) {
            Toast.makeText(this, "网络ID不能为空,范围1~255", Toast.LENGTH_SHORT).show();
            return;
        }

        int oneFloorHouseNumInt = Integer.parseInt(oneFloorHouseNum);
        int beginFloorInt = Integer.parseInt(beginFloor);
        int endFloorInt = Integer.parseInt(endFloor);

        List<House> houseList = new ArrayList<House>();
        for (int i = beginFloorInt; i < endFloorInt + 1; i++) {
            for (int j = 1; j < oneFloorHouseNumInt + 1; j++) {
                House house = new House(buildingsId, buildingsName, banId, unit, String.format("%d", i), j, null, null, null, null);
                house.houseNetId = String.format("%d", netIdInt);
                houseList.add(house);
            }
        }

        DBManager dbManager = new DBManager(this);
        //如果该楼改栋改单元存在 则不添加
        if (dbManager.queryHouse(buildingsId, banId, unit).size() == 0) {
            dbManager.addHouse(houseList);
        } else {
            Toast.makeText(this, "此单元已经添加过，不能重复添加", Toast.LENGTH_SHORT).show();
            dbManager.closeDB();
            return;
        }
        dbManager.closeDB();
        this.finish();
    }
}

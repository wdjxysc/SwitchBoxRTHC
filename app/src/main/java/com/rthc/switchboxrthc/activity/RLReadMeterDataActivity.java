package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rthc.switchboxrthc.BleBoxOperation;
import com.rthc.switchboxrthc.R;
import com.rthc.switchboxrthc.action.ItemAction;
import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.bean.MeterData;
import com.rthc.switchboxrthc.db.DBManager;
import com.rthc.switchboxrthc.svprogresshud.SVProgressHUD;
import com.rthc.switchboxrthc.ui.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RLReadMeterDataActivity extends Activity {


    Spinner buildingsSpinner;
    Spinner banIdSpinner;
    Spinner unitIdSpinner;

    EditText floorEditText;
    EditText houseNumEditText;

    Button searchBtn;
    Button readMetersBtn;

    RecyclerView recyclerView;

    List<House> buildingsList = new ArrayList<House>();
    List<House> banList = new ArrayList<House>();
    List<House> unitList = new ArrayList<House>();

    List<String> buildingsListStr = new ArrayList<String>();
    List<String> banListStr = new ArrayList<String>();
    List<String> unitListStr = new ArrayList<String>();

    List<House> houseList = new ArrayList<House>();

    DBManager dbManager;

    MeterDataAdapter meterDataAdapter;

    MyOnItemSelectedListener myOnItemSelectedListener;

    ArrayAdapter buildingsAdapter;
    ArrayAdapter banIdAdapter;
    ArrayAdapter unitIdAdapter;

//    SerialPortTool serialPortTool;

    Thread thread;

    House selectHouse;

    Context context;

    int selectPosition;


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.rthc.switchboxrthc.R.layout.activity_rlread_meter_data);
        context = this;

        dbManager = new DBManager(this);

        initData();

        initView();
    }

    @Override
    protected void onDestroy() {
//        serialPortTool.closePort(SerialPortTool.PowerLevel.POWER_RFID);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        House house = data.getParcelableExtra("House");
        int index = data.getIntExtra("Index", -1);

        if (index != -1) {
            houseList.set(index, house);
            meterDataAdapter.notifyDataSetChanged();
        }
    }

    private void initData() {
        buildingsList = dbManager.queryBuildings();
        for (House house : buildingsList) {
            buildingsListStr.add(house.buildingsId + ":" + house.buildingsName);
        }
        if (buildingsList.size() > 0) {
            banList = dbManager.queryBan(buildingsList.get(0).buildingsId);
            for (House house : banList) {
                banListStr.add(house.banId);
            }

            if (banList.size() > 0) {
                unitList = dbManager.queryUnit(buildingsList.get(0).buildingsId, banList.get(0).banId);
                for (House house : unitList) {
                    unitListStr.add(house.unitId);
                }

                if (unitList.size() > 0)
                    houseList = dbManager.queryHouse(buildingsList.get(0).buildingsId, banList.get(0).banId, unitList.get(0).unitId);
            }
        }
    }


    private void initView() {
        buildingsSpinner = (Spinner) findViewById(R.id.buildingsSpinner);
        banIdSpinner = (Spinner) findViewById(R.id.banIdSpinner);
        unitIdSpinner = (Spinner) findViewById(R.id.unitIdSpinner);

        floorEditText = (EditText)findViewById(R.id.floorEditText);
        houseNumEditText = (EditText)findViewById(R.id.houseNumEditText);

        searchBtn = (Button)findViewById(R.id.searchBtn);
        readMetersBtn = (Button) findViewById(R.id.readMetersBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String floor = floorEditText.getText().toString();
                String houseNum = houseNumEditText.getText().toString();

                List<House> houseList1 = new ArrayList<House>();
                if(!floor.isEmpty() && !houseNum.isEmpty()){
                    houseList1 = dbManager.queryHouse(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            floor,
                            houseNum);
                }else if(!floor.isEmpty()){
                    houseList1 = dbManager.queryHouseByAndFloor(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            floor);
                }else if(!houseNum.isEmpty()){
                    houseList1 = dbManager.queryHouseByAndHouseNum(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            houseNum);
                }else{
                    houseList1 = dbManager.queryHouse(
                            buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId);
                }

                houseList.clear();
                houseList.addAll(houseList1);

                meterDataAdapter.notifyDataSetChanged();
            }
        });


        readMetersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(context);
                progressDialog.setMax(houseList.size());
                progressDialog.setTitle("抄表中");
                progressDialog.setMessage("0个成功,0个异常");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(0);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();

//                if(thread != null && thread.getState() == Thread.State.TERMINATED){
//                    return;
//                }
                //读取列表中所有表数据
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        Looper.prepare();
                        final int total = houseList.size();
                        int successCount = 0;


                        for (int i = 0;i < houseList.size(); i++){
                            Log.i("rthc","i:" + i);
//                            if(!progressDialog.isShowing()) break;
                            if(houseList.get(i).meter.meterID != null){

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MeterData meterData = BleBoxOperation.SyncRead(AtyListActivity.switchBox, houseList.get(i).meter);
                                if (meterData == null) {

                                } else {
                                    successCount++;
                                    ArrayList<MeterData> meterDatas = new ArrayList<MeterData>();
                                    meterDatas.add(meterData);

                                    dbManager.addMeterData(meterDatas);

                                    houseList.get(i).meterDataLast = meterData;
                                }
                            }

                            final int finalI = i+1;
                            final int finalSuccessCount = successCount;

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage( finalSuccessCount+"个成功,"+ (finalI-finalSuccessCount) +"个异常");
                                    progressDialog.setProgress(finalI);

                                }
                            });
                        }


                        final int finalSuccessCount1 = successCount;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();

                                Toast.makeText(context, "抄表完成:" + finalSuccessCount1 + "成功," + (total-finalSuccessCount1) + "异常",Toast.LENGTH_SHORT).show();
                                meterDataAdapter.notifyDataSetChanged();
                            }
                        });
//                        Looper.loop();
                    }
                });

                thread.start();
                Log.i("rthc", "thread.start().......");
            }
        });


        buildingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buildingsListStr);
        buildingsSpinner.setAdapter(buildingsAdapter);
        banIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, banListStr);
        banIdSpinner.setAdapter(banIdAdapter);
        unitIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitListStr);
        unitIdSpinner.setAdapter(unitIdAdapter);

        myOnItemSelectedListener = new MyOnItemSelectedListener();
        buildingsSpinner.setOnItemSelectedListener(myOnItemSelectedListener);
        banIdSpinner.setOnItemSelectedListener(myOnItemSelectedListener);
        unitIdSpinner.setOnItemSelectedListener(myOnItemSelectedListener);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        meterDataAdapter = new MeterDataAdapter(houseList, R.layout.item_read_meter_data);
        meterDataAdapter.setOnItemClickListener(new ItemAction() {
            @Override
            public void OnItemClick(View view, int position) {
                selectHouse = houseList.get(position);
                Log.i("wdj", position + "--------" + selectHouse.
                        meter.meterID);

                selectPosition = position;

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readData();
                    }
                });
                thread.start();
                Log.i("rthc", "thread.start().......");

                /**
                 Intent intent = new Intent();
                 intent.setClassName(getApplicationContext(), "com.example.activity.RLMeterInfoActivity");
                 Bundle bundle = new Bundle();
                 bundle.putParcelable("House",houseList.get(position));
                 bundle.putInt("Index",position);
                 intent.putExtras(bundle);
                 startActivityForResult(intent, 0);
                 */
            }

            @Override
            public void OnItemLongClick(View view, int position) {

            }
        });

        recyclerView.setAdapter(meterDataAdapter);
    }

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.buildingsSpinner:
                    break;
                case R.id.banIdSpinner:
                    break;
                case R.id.unitIdSpinner:
                    break;
            }

            if (buildingsList.size() > 0) {
                banListStr.clear();
                banList = dbManager.queryBan(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId);
                for (House house : banList) {
                    banListStr.add(house.banId);
                }

                banIdAdapter.notifyDataSetChanged();

                if (banList.size() > 0) {
                    unitListStr.clear();
                    unitList = dbManager.queryUnit(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId);
                    for (House house : unitList) {
                        unitListStr.add(house.unitId);
                    }
                    unitIdAdapter.notifyDataSetChanged();
                }
            }

            houseList.clear();

            houseList.addAll(dbManager.queryHouse(
                    buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                    banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                    unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId));


            meterDataAdapter.notifyDataSetChanged();
        }


        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class MeterDataAdapter extends RecyclerView.Adapter<MeterDataAdapter.MeterInfoViewHolder> {

        ItemAction itemAction;

        private List<House> items;
        private int itemLayout;//可以以此判断对应视图 以加载不同视图数据


        /**
         * adapter构造函数
         *
         * @param items      items
         * @param itemLayout itemLayout
         */
        public MeterDataAdapter(List<House> items, int itemLayout) {
            this.items = items;
            this.itemLayout = itemLayout;
        }


        /**
         * 创建viewholder 即单元视图 可以根据viewType设置不同布局的视图
         *
         * @param parent   viewGroup
         * @param viewType 单元视图类型
         * @return return
         */
        @Override
        public MeterInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, null);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
            return new MeterInfoViewHolder(view, itemAction);
        }

        /**
         * 绑定数据至视图
         *
         * @param holder   视图
         * @param position 视图位置
         */
        @Override
        public void onBindViewHolder(MeterInfoViewHolder holder, int position) {
            holder.houseNumTextView.setText(items.get(position).houseNum);
            if (position == 11) {
                Log.i("wdj", "11");
            }
            if (items.get(position).meter.netID != null) {
                holder.meterIdTextView.setText(items.get(position).meter.meterID);
                Log.i("wdj", position + "--------------" + items.get(position).meter.meterID);
            } else {
                holder.meterIdTextView.setText("--");
            }
            if (items.get(position).meterDataLast != null) {
                holder.dataTextView.setText(String.format("%.3f", items.get(position).meterDataLast.dataValue));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                holder.timeTextView.setText(sdf.format(items.get(position).meterDataLast.dataTime));

                if (items.get(position).meter.meterID.substring(0, 2).equals("10")) {
                    holder.meterStateTextView.setText("阀门-" + items.get(position).meterDataLast.stateValve
                            + "  电压-" + items.get(position).meterDataLast.powerValue);
                } else {
                    holder.meterStateTextView.setText("阀门-" + items.get(position).meterDataLast.stateValve
                            + "  3.6V-" + items.get(position).meterDataLast.statePower36V
                            + "  6V-" + items.get(position).meterDataLast.statePower6V);
                }

            } else {
                holder.dataTextView.setText("--");
                holder.timeTextView.setText("--");
                holder.meterStateTextView.setText("--");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setOnItemClickListener(ItemAction itemAction) {
            this.itemAction = itemAction;
        }


        class MeterInfoViewHolder extends RecyclerView.ViewHolder {

            ItemAction itemAction;

            TextView houseNumTextView;
            TextView meterIdTextView;
            TextView dataTextView;
            TextView timeTextView;
            TextView meterStateTextView;

            public MeterInfoViewHolder(View itemView, final ItemAction itemAction) {
                super(itemView);
                this.itemAction = itemAction;

                houseNumTextView = (TextView) itemView.findViewById(R.id.houseNumTextView);
                meterIdTextView = (TextView) itemView.findViewById(R.id.meterIdTextView);
                dataTextView = (TextView) itemView.findViewById(R.id.dataTextView);
                timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
                meterStateTextView = (TextView) itemView.findViewById(R.id.meterStateTextView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemAction.OnItemClick(view, getAdapterPosition());
                    }
                });
            }
        }
    }


    /**
     * 读取数据并更新到界面
     */
    private void readData() {
        if (selectHouse.meter.meterID != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SVProgressHUD.showWithStatus(context, "正在抄表...");
                }
            });


//            MeterData meterData = Cmd.ReadMeterData(selectHouse.meter, serialPortTool);
            MeterData meterData = BleBoxOperation.SyncRead(AtyListActivity.switchBox, selectHouse.meter);

            if (meterData == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SVProgressHUD.showErrorWithStatus(context, "失败");
                    }
                });
            } else {

                ArrayList<MeterData> meterDatas = new ArrayList<MeterData>();
                meterDatas.add(meterData);

                dbManager.addMeterData(meterDatas);

                houseList.get(selectPosition).meterDataLast = meterData;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SVProgressHUD.showSuccessWithStatus(context, "成功");
                        meterDataAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (thread != null) {

                    Thread.State state = thread.getState();
                    if (thread.getState() == Thread.State.TERMINATED)
                        this.finish();
                } else {
                    this.finish();
                }
                Log.i("dsadasd", "dasdsadsadasdas");

                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }
}

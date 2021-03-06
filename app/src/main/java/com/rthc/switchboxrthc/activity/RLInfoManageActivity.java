package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.rthc.switchboxrthc.R;
import com.rthc.switchboxrthc.action.ItemAction;
import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.db.DBManager;
import com.rthc.switchboxrthc.ui.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class RLInfoManageActivity extends Activity {

    Spinner buildingsSpinner;
    Spinner banIdSpinner;
    Spinner unitIdSpinner;

    EditText floorEditText;
    EditText houseNumEditText;

    Button searchBtn;

    RecyclerView recyclerView;

    List<House> buildingsList = new ArrayList<House>();
    List<House> banList = new ArrayList<House>();
    List<House> unitList = new ArrayList<House>();

    List<String> buildingsListStr = new ArrayList<String>();
    List<String> banListStr = new ArrayList<String>();
    List<String> unitListStr = new ArrayList<String>();

    List<House> houseList = new ArrayList<House>();

    DBManager dbManager;

    MeterInfoAdapter meterInfoAdapter;

    MyOnItemSelectedListener myOnItemSelectedListener;

    ArrayAdapter buildingsAdapter;
    ArrayAdapter banIdAdapter;
    ArrayAdapter unitIdAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_manage);

        dbManager = new DBManager(this);

        initData();

        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        houseList.clear();
        List<House> changedHouseList = data.getParcelableArrayListExtra("HouseList");
        houseList.addAll(changedHouseList);
        meterInfoAdapter.notifyDataSetChanged();
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

        floorEditText = (EditText) findViewById(R.id.floorEditText);
        houseNumEditText = (EditText) findViewById(R.id.houseNumEditText);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String floor = floorEditText.getText().toString();
                String houseNum = houseNumEditText.getText().toString();

                List<House> houseList1 = new ArrayList<House>();
                if (!floor.isEmpty() && !houseNum.isEmpty()) {
                    houseList1 = dbManager.queryHouse(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            floor,
                            houseNum);
                } else if (!floor.isEmpty()) {
                    houseList1 = dbManager.queryHouseByAndFloor(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            floor);
                } else if (!houseNum.isEmpty()) {
                    houseList1 = dbManager.queryHouseByAndHouseNum(buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId,
                            houseNum);
                } else {
                    houseList1 = dbManager.queryHouse(
                            buildingsList.get(buildingsSpinner.getSelectedItemPosition()).buildingsId,
                            banList.get(banIdSpinner.getSelectedItemPosition()).banId,
                            unitList.get(unitIdSpinner.getSelectedItemPosition()).unitId);
                }

                houseList.clear();
                houseList.addAll(houseList1);

                meterInfoAdapter.notifyDataSetChanged();
            }
        });

        buildingsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buildingsListStr);
        buildingsSpinner.setAdapter(buildingsAdapter);
        banIdAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, banListStr);
        banIdSpinner.setAdapter(banIdAdapter);
        unitIdAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, unitListStr);
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
        meterInfoAdapter = new MeterInfoAdapter(houseList, R.layout.meter_info_item);
        meterInfoAdapter.setOnItemClickListener(new ItemAction() {
            @Override
            public void OnItemClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), RLMeterInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("House", houseList.get(position));
                bundle.putParcelableArrayList("HouseList", (ArrayList<House>) houseList);
                bundle.putInt("Index", position);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }

            @Override
            public void OnItemLongClick(View view, int position) {

            }
        });

        recyclerView.setAdapter(meterInfoAdapter);
    }

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.buildingsSpinner:

                    break;
                case R.id.banIdEditText:
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


            meterInfoAdapter.notifyDataSetChanged();
        }


        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class MeterInfoAdapter extends RecyclerView.Adapter<MeterInfoAdapter.MeterInfoViewHolder> {

        ItemAction itemAction;

        private List<House> items;
        private int itemLayout;//可以以此判断对应视图 以加载不同视图数据


        /**
         * adapter构造函数
         *
         * @param items      items
         * @param itemLayout itemLayout
         */
        public MeterInfoAdapter(List<House> items, int itemLayout) {
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
            holder.floorTextView.setText(items.get(position).floor);
            holder.houseIndexTextView.setText(items.get(position).houseIndex + "");
            holder.houseNumTextView.setText(items.get(position).houseNum);
            holder.initStateTextView.setText((items.get(position).meter.isActivated == 0) ? "未激活" : "已激活");

            if (items.get(position).meter.meterID == null) {
                holder.netIdTextView.setText(items.get(position).houseNetId);
            } else {
                holder.netIdTextView.setText(items.get(position).meter.netID);
            }

            if (items.get(position).meter.isActivated == 0) {
                int color = getResources().getColor(R.color.textColor_unInitialize);
                holder.initStateTextView.setTextColor(color);
            } else {
                int color = getResources().getColor(R.color.textColor_initialized);
                holder.initStateTextView.setTextColor(color);
            }
            holder.householderNameTextView.setText((items.get(position).householderName == null) ? "" : houseList.get(position).householderName);
            holder.meterIdTextView.setText(items.get(position).meter.meterID);
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

            TextView floorTextView;
            TextView houseIndexTextView;
            TextView houseNumTextView;
            TextView initStateTextView;
            TextView householderNameTextView;
            TextView meterIdTextView;
            TextView netIdTextView;

            public MeterInfoViewHolder(View itemView, final ItemAction itemAction) {
                super(itemView);
                this.itemAction = itemAction;

                floorTextView = (TextView) itemView.findViewById(R.id.floorTextView);
                houseIndexTextView = (TextView) itemView.findViewById(R.id.houseIndexTextView);
                houseNumTextView = (TextView) itemView.findViewById(R.id.houseNumTextView);
                initStateTextView = (TextView) itemView.findViewById(R.id.initStateTextView);
                householderNameTextView = (TextView) itemView.findViewById(R.id.householderNameTextView);
                meterIdTextView = (TextView) itemView.findViewById(R.id.meterIdTextView);
                netIdTextView = (TextView) itemView.findViewById(R.id.netIdTextView);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemAction.OnItemClick(view, getAdapterPosition());
                    }
                });
            }
        }
    }

}

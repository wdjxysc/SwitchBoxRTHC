package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rthc.switchboxrthc.R;
import com.rthc.switchboxrthc.action.ItemAction;
import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.bean.MeterData;
import com.rthc.switchboxrthc.db.DBManager;
import com.rthc.switchboxrthc.ui.DividerItemDecoration;
import com.rthc.wdj.bluetoothtoollib.cmd.Const;

import java.util.ArrayList;
import java.util.List;

public class RLMeterDataListActivity extends Activity {

    RecyclerView recyclerView;

    List<MeterData> meterDataList = new ArrayList<MeterData>();

    DBManager dbManager;

    MeterDataAdapter meterDataAdapter;

    Context context;

    House house;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rlmeter_data_list);
        context = this;

        dbManager = new DBManager(this);

        initData();

        initView();
    }

    private void initData() {
        house = getIntent().getParcelableExtra("House");
        meterDataList = dbManager.queryMeterData(house.meter.meterID);
    }


    private void initView() {


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setLayoutManager(linearLayoutManager);
        meterDataAdapter = new MeterDataAdapter(meterDataList, R.layout.item_data);
        meterDataAdapter.setOnItemClickListener(new ItemAction() {
            @Override
            public void OnItemClick(View view, int position) {

            }

            @Override
            public void OnItemLongClick(View view, int position) {

            }
        });

        recyclerView.setAdapter(meterDataAdapter);
    }



    private class MeterDataAdapter extends RecyclerView.Adapter<MeterDataAdapter.MeterInfoViewHolder> {

        ItemAction itemAction;

        private List<MeterData> items;
        private int itemLayout;//可以以此判断对应视图 以加载不同视图数据


        /**
         * adapter构造函数
         *
         * @param items      items
         * @param itemLayout itemLayout
         */
        public MeterDataAdapter(List<MeterData> items, int itemLayout) {
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

            if (items.get(position).dataTime != null) {
                holder.dataValueTextView.setText(String.format("%.3f", items.get(position).dataValue));

                holder.dataTimeTextView.setText(Const.TIME_FORMAT.format(items.get(position).dataTime));
            } else {
                holder.dataValueTextView.setText("--");
                holder.dataTimeTextView.setText("--");
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

            TextView dataValueTextView;
            TextView dataTimeTextView;

            public MeterInfoViewHolder(View itemView, final ItemAction itemAction) {
                super(itemView);
                this.itemAction = itemAction;

                dataValueTextView = (TextView) itemView.findViewById(R.id.dataValueTextView);
                dataTimeTextView = (TextView) itemView.findViewById(R.id.dataTimeTextView);

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

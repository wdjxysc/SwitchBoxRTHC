package com.rthc.switchboxrthc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rthc.switchboxrthc.R;

public class RLMeterManagerActivity extends Activity {

    Button meterIdManageBtn;
    Button infoManageBtn;
    Button goReadMeterDataActivityBtn;
    Button goHistoryActivityBtn;

    btnOnClickListener btnOnClickListener = new btnOnClickListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rlmeter_manager);

        initView();
    }

    private void initView() {
        meterIdManageBtn = (Button)findViewById(R.id.meterIdManageBtn);
        infoManageBtn = (Button)findViewById(R.id.infoManageBtn);
        goReadMeterDataActivityBtn = (Button)findViewById(R.id.goReadMeterDataActivityBtn);
        goHistoryActivityBtn = (Button)findViewById(R.id.goHistoryActivityBtn);

        meterIdManageBtn.setOnClickListener(btnOnClickListener);
        infoManageBtn.setOnClickListener(btnOnClickListener);
        goReadMeterDataActivityBtn.setOnClickListener(btnOnClickListener);
        goHistoryActivityBtn.setOnClickListener(btnOnClickListener);
    }

    private class btnOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int viewId = view.getId();

            Intent intent;
            switch (viewId){
                case R.id.meterIdManageBtn:
                    intent = new Intent(getApplicationContext(),RLMeterIdManageActivity.class);
                    startActivity(intent);
                    break;
                case R.id.infoManageBtn:
                    intent = new Intent(getApplicationContext(),RLInfoManageActivity.class);
                    startActivity(intent);
                    break;
                case R.id.goReadMeterDataActivityBtn:
                    intent = new Intent(getApplicationContext(),RLReadMeterDataActivity.class);
                    startActivity(intent);
                    break;
                case R.id.goHistoryActivityBtn:
                    intent = new Intent(getApplicationContext(),RLMeterDataActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }
}

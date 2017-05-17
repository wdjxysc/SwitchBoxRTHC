package com.rthc.switchboxrthc.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.rthc.switchboxrthc.R;
import com.rthc.wdj.bluetoothtoollib.SwitchBox;
import com.rthc.wdj.bluetoothtoollib.cmd.Const;

import java.util.ArrayList;

public class BoxSettingActivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<String>();
    ArrayAdapter adapter;

    Button writeBtn;
    Spinner breathSpinner;
    EditText netidEditText;

    public static SwitchBox switchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_setting);

        breathSpinner = (Spinner) findViewById(R.id.breathSpinner);

        list.add("2s");
        list.add("4s");
        list.add("6s");
        list.add("8s");
        list.add("10s");
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breathSpinner.setAdapter(adapter);
        breathSpinner.setSelection(2);

        netidEditText = (EditText) findViewById(R.id.netidEditText);
        writeBtn = (Button) findViewById(R.id.writeBtn);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int netId = Integer.parseInt(netidEditText.getText().toString(), 16);
                boolean b1 = switchBox.setBoxCenterParam(netId, breathSpinner.getSelectedItemPosition(), Const.RfModuleType.JIEXUN );
                boolean b2 = switchBox.setBoxCenterParam(netId, breathSpinner.getSelectedItemPosition(), Const.RfModuleType.SKY_SHOOT );

                if(b1 && b2){
                    Toast.makeText(BoxSettingActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BoxSettingActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

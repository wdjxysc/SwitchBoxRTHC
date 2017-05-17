package com.rthc.switchboxrthc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.rthc.switchboxrthc.R;

public class TestActivity extends AppCompatActivity {
    ProgressDialog progressDialog;

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        context = this;

        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),TestActivity1.class);
                startActivity(intent);

            }
        });

        findViewById(R.id.dialogBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(context);

                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
                progressDialog.setProgress(0);
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0;i<100;i++) {

                            if(!progressDialog.isShowing()){
                                break;
                            }
                            try {

                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Log.i("rthc","i:" + i);
                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress(finalI +1);
                                }
                            });
                        }

                        progressDialog.dismiss();
                    }
                }).start();
            }
        });
    }
}

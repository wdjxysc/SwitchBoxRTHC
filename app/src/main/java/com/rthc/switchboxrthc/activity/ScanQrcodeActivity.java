package com.rthc.switchboxrthc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.Result;
import com.rthc.switchboxrthc.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrcodeActivity extends Activity implements ZXingScannerView.ResultHandler {

    final int MY_PERMISSIONS_REQUEST_CAMERA = 1;


    ZXingScannerView zXingScannerView;
    Button lightBtn;
    Button useBtn;
    Button reScanBtn;
    TextView resultTextView;


    Context context;

    String TAG = "QR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        zXingScannerView = (ZXingScannerView) findViewById(R.id.scannerView);
        lightBtn = (Button)findViewById(R.id.lightBtn);
        useBtn = (Button) findViewById(R.id.useBtn);
        reScanBtn = (Button) findViewById(R.id.reScanBtn);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        lightBtn.setOnClickListener(myClickListener);
        useBtn.setOnClickListener(myClickListener);
        reScanBtn.setOnClickListener(myClickListener);

        context = this;


        findViewById(R.id.reScanBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zXingScannerView.stopCamera();
                zXingScannerView.startCamera();
            }
        });
        request();
    }


    void request(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            Log.i(TAG, "request: sdk version below M");
            zXingScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            zXingScannerView.startCamera();          // Start camera on resume
        }else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //未授权 请求用户授权
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }else{

                zXingScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                zXingScannerView.startCamera();          // Start camera on resume
            }
        }
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        Log.v(TAG, result.getText()); // Prints scan results
        Log.v(TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        resultTextView.setText(result.getText());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    zXingScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                    zXingScannerView.startCamera();          // Start camera on resume
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("rthc","用户拒绝使用摄像头");
                    this.finish();
                }
                return;
            }
        }
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.lightBtn:

                    break;
                case R.id.useBtn:
                    Intent intent = new Intent();
                    intent.putExtra("code", resultTextView.getText().toString());
                    setResult(RESULT_OK, intent);

                    ((Activity)context).finish();
                    break;
                case R.id.reScanBtn:
                    resultTextView.setText("");
                    zXingScannerView.stopCamera();
                    zXingScannerView.startCamera();
                    break;
            }

        }
    };
}

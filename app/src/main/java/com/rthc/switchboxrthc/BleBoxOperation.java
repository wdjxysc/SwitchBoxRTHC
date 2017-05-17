package com.rthc.switchboxrthc;

import android.util.Log;

import com.rthc.switchboxrthc.bean.Meter;
import com.rthc.switchboxrthc.bean.MeterData;
import com.rthc.wdj.bluetoothtoollib.MeterHandler;
import com.rthc.wdj.bluetoothtoollib.SwitchBox;
import com.rthc.wdj.bluetoothtoollib.cmd.BleCmd;
import com.rthc.wdj.bluetoothtoollib.cmd.Cmd;
import com.rthc.wdj.bluetoothtoollib.cmd.MeterStateConst;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/6/12.
 */
public class BleBoxOperation {

    static final Object lock = new Object();

    static MeterData mMeterData;

    static MeterHandler handler =  new MeterHandler() {
        @Override
        public int callback(float v, HashMap hashMap) {


            if(v>0) {
                mMeterData = new MeterData();

                mMeterData.meterID = hashMap.get(Cmd.KEY_METER_ID).toString();
                mMeterData.dataValue = Double.parseDouble(hashMap.get(Cmd.KEY_VALUE_NOW).toString());
                mMeterData.dataTime = new Date();
                mMeterData.stateValve = (MeterStateConst.STATE_VALVE) hashMap.get(Cmd.KEY_VALVE_STATE);
                if (mMeterData.meterID.substring(0, 2).equals("10")) { //利尔达
                    mMeterData.powerValue = Double.parseDouble(hashMap.get(Cmd.KEY_BATTERY_VALUE).toString());
                } else {                                          //捷迅
                    mMeterData.statePower36V = (MeterStateConst.STATE_POWER_3_6_V) hashMap.get(Cmd.KEY_BATTERY_3_6_STATE);
                    mMeterData.statePower6V = (MeterStateConst.STATE_POWER_6_V) hashMap.get(Cmd.KEY_BATTERY_6_STATE);
                }
            }else {
                Log.i("rthc", "抄表失败");
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        lock.notify();
                        Log.i("rthc", "synchronized notify：read failed");
                    }
                }
            }).start();


            return 0;
        }

        @Override
        public void timeOut() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                lock.notify();
                Log.i("rthc","synchronized notify：timeout");
            }
        }
    };

    public static MeterData SyncRead(SwitchBox switchBox, Meter meter){

        MeterData meterData = new MeterData();


        synchronized (lock){
            Log.i("rthc","synchronized start");

            mMeterData = null;

//            Looper.prepare();
            switchBox.readMeter(meter.meterID, getModuleId(meter.meterID), handler);


            try {
                lock.wait(40000);
                Log.i("rthc","synchronized wakeup");
            }catch (Exception ex){
                ex.printStackTrace();
            }

            meterData = mMeterData;
//            Looper.loop();
        }

        Log.i("rthc","synchronized end");
        return meterData;
    }






    /**
     * 根据表ID获取模块ID
     *
     * @return
     */
    static int getModuleId(String meterId) {
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
}

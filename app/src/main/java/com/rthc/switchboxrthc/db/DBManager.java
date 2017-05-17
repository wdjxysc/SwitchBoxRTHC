package com.rthc.switchboxrthc.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rthc.switchboxrthc.bean.House;
import com.rthc.switchboxrthc.bean.Meter;
import com.rthc.switchboxrthc.bean.MeterData;
import com.rthc.wdj.bluetoothtoollib.cmd.MeterStateConst;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2015/9/21.
 */
public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add meters
     *
     * @param meters
     */
    public void addMeter(List<Meter> meters) {
        db.beginTransaction();  //开始事务
        try {
            for (Meter meter : meters) {
                db.execSQL("INSERT INTO METER VALUES(null, ?, ?, ?, ?, ?, ?)", new Object[]{meter.meterID, meter.netID, meter.meterSerialNum, meter.isActivated, meter.installDate, meter.productionDate});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception ex) {
            Log.i("wdj", ex.getMessage());
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * update meter
     *
     * @param meterList
     */
    public void updateMeter(List<Meter> meterList) {

        for (Meter meter : meterList) {
            ContentValues cv = new ContentValues();
            cv.put("NET_ID", meter.netID);
            cv.put("IS_ACTIVATED", meter.isActivated);
            cv.put("INSTALL_DATE", String.valueOf(meter.installDate));
            cv.put("PRODUCTION_DATE", String.valueOf(meter.productionDate));

            db.update("METER", cv, "METER_ID = ?", new String[]{meter.meterID});
        }
    }

    /**
     * delete by meterid
     *
     * @param meter
     */
    public void deleteMeter(Meter meter) {
        db.delete("METER", "METER_ID = ?", new String[]{String.valueOf(meter.meterID)});
    }

    /**
     * query all meters, return list
     *
     * @return List<Meter>
     */
    public List<Meter> queryMeter() {
        ArrayList<Meter> meters = new ArrayList<Meter>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.meterSerialNum = c.getString(c.getColumnIndex("METER_SERIAL_NUM"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                meter.productionDate = sdf.parse(c.getString(c.getColumnIndex("PRODUCTION_DATE")));
                meter.installDate = sdf.parse(c.getString(c.getColumnIndex("INSTALL_DATE")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            meters.add(meter);
        }
        c.close();
        return meters;
    }


    /**
     * query all meters, return list
     *
     * @return List<Meter>
     */
    public List<Meter> queryMeter(String meterId) {
        ArrayList<Meter> meters = new ArrayList<Meter>();
        Cursor c = db.rawQuery("SELECT * FROM METER WHERE METER_ID = ?", new String[]{meterId});
        while (c.moveToNext()) {
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.meterSerialNum = c.getString(c.getColumnIndex("METER_SERIAL_NUM"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if (c.getString(c.getColumnIndex("PRODUCTION_DATE")) != null)
                    meter.productionDate = sdf.parse(c.getString(c.getColumnIndex("PRODUCTION_DATE")));
                if (c.getString(c.getColumnIndex("INSTALL_DATE")) != null)
                    meter.installDate = sdf.parse(c.getString(c.getColumnIndex("INSTALL_DATE")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            meters.add(meter);
        }
        c.close();
        return meters;
    }

    /**
     * query all meters, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM METER", null);
        return c;
    }


    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }


    /**
     * add houses
     *
     * @param houses
     */
    public void addHouse(List<House> houses) {
        db.beginTransaction();  //开始事务
        try {
            for (House house : houses) {
                db.execSQL("INSERT INTO HOUSE VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{house.buildingsId, house.buildingsName, house.banId, house.unitId, house.floor, house.houseIndex, house.houseNum, house.householderName, house.houseNetId, null});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }


    /**
     * query buildings, return list
     *
     * @return List<House>
     */
    public List<House> queryBuildings() {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT DISTINCT BUILDINGS_ID,BUILDINGS_NAME FROM HOUSE", null);
        while (c.moveToNext()) {
            House house = new House();
            house.buildingsId = c.getString(c.getColumnIndex("BUILDINGS_ID"));
            house.buildingsName = c.getString(c.getColumnIndex("BUILDINGS_NAME"));

            houses.add(house);
        }
        c.close();
        return houses;
    }

    /**
     * query bans by buildingsId, return list
     *
     * @return List<House>
     */
    public List<House> queryBan(String buildingsId) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT DISTINCT BAN_ID FROM HOUSE WHERE BUILDINGS_ID = ?", new String[]{buildingsId});
        while (c.moveToNext()) {
            House house = new House();
            house.banId = c.getString(c.getColumnIndex("BAN_ID"));

            houses.add(house);
        }
        c.close();
        return houses;
    }


    /**
     * query unit by banId and buildingsId, return list
     *
     * @return List<House>
     */
    public List<House> queryUnit(String buildingsId, String banId) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT DISTINCT UNIT_ID FROM HOUSE WHERE BUILDINGS_ID = ? AND BAN_ID = ?", new String[]{buildingsId, banId});
        while (c.moveToNext()) {
            House house = new House();
            house.unitId = c.getString(c.getColumnIndex("UNIT_ID"));

            houses.add(house);
        }
        c.close();
        return houses;
    }

    /**
     * query house by banId and buildingsId and unitId, return list
     *
     * @return List<House>
     */
    public List<House> queryHouse(String buildingsId, String banId, String unitId) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT * FROM HOUSE left outer join METER on METER.METER_ID=HOUSE.METER_ID WHERE BUILDINGS_ID = ? AND BAN_ID = ? AND UNIT_ID = ?", new String[]{buildingsId, banId, unitId});
        while (c.moveToNext()) {
            House house = new House();
            house.buildingsId = c.getString(c.getColumnIndex("BUILDINGS_ID"));
            house.buildingsName = c.getString(c.getColumnIndex("BUILDINGS_NAME"));
            house.banId = c.getString(c.getColumnIndex("BAN_ID"));
            house.unitId = c.getString(c.getColumnIndex("UNIT_ID"));
            house.floor = c.getString(c.getColumnIndex("FLOOR"));
            house.houseIndex = c.getInt(c.getColumnIndex("HOUSE_INDEX"));
            house.houseNum = c.getString(c.getColumnIndex("HOUSE_NUM"));
            house.householderName = c.getString(c.getColumnIndex("HOUSEHOLDER_NAME"));
            house.houseNetId = c.getString(c.getColumnIndex("HOUSE_NET_ID"));
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));
            house.meter = meter;

            if (house.meter.meterID != null) {
                Cursor c1 = db.rawQuery("select * from METER_DATA where METER_ID = ?", new String[]{house.meter.meterID});
                if (c1.moveToNext()) {
                    MeterData meterData = new MeterData();
                    meterData.meterID = house.meter.meterID;
                    meterData.dataValue = c1.getDouble(c1.getColumnIndex("DATA_VALUE"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    try {
                        meterData.dataTime = sdf.parse(c1.getString(c1.getColumnIndex("DATA_TIME")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                c1.close();
            }

            if (house.meter.meterID != null) {

                List<MeterData> meterDatas = this.queryMeterData(meter.meterID);
                if (meterDatas.size() > 0) {
                    house.meterDataLast = meterDatas.get(0);
                }
            }


            houses.add(house);
        }
        c.close();
        return houses;
    }

    /**
     * query house by banId and buildingsId and unitId, return list
     *
     * @return List<House>
     */
    public List<House> queryHouseByAndFloor(String buildingsId, String banId, String unitId, String floor) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT * FROM HOUSE left outer join METER on METER.METER_ID=HOUSE.METER_ID WHERE BUILDINGS_ID = ? AND BAN_ID = ? AND UNIT_ID = ? AND FLOOR = ?",
                new String[]{buildingsId, banId, unitId, floor});
        while (c.moveToNext()) {
            House house = new House();
            house.buildingsId = c.getString(c.getColumnIndex("BUILDINGS_ID"));
            house.buildingsName = c.getString(c.getColumnIndex("BUILDINGS_NAME"));
            house.banId = c.getString(c.getColumnIndex("BAN_ID"));
            house.unitId = c.getString(c.getColumnIndex("UNIT_ID"));
            house.floor = c.getString(c.getColumnIndex("FLOOR"));
            house.houseIndex = c.getInt(c.getColumnIndex("HOUSE_INDEX"));
            house.houseNum = c.getString(c.getColumnIndex("HOUSE_NUM"));
            house.householderName = c.getString(c.getColumnIndex("HOUSEHOLDER_NAME"));
            house.houseNetId = c.getString(c.getColumnIndex("HOUSE_NET_ID"));
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));
            house.meter = meter;

            if (house.meter.meterID != null) {
                Cursor c1 = db.rawQuery("select * from METER_DATA where METER_ID = ?", new String[]{house.meter.meterID});
                if (c1.moveToNext()) {
                    MeterData meterData = new MeterData();
                    meterData.meterID = house.meter.meterID;
                    meterData.dataValue = c1.getDouble(c1.getColumnIndex("DATA_VALUE"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    try {
                        meterData.dataTime = sdf.parse(c1.getString(c1.getColumnIndex("DATA_TIME")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                c1.close();
            }

            if (house.meter.meterID != null) {

                List<MeterData> meterDatas = this.queryMeterData(meter.meterID);
                if (meterDatas.size() > 0) {
                    house.meterDataLast = meterDatas.get(0);
                }
            }


            houses.add(house);
        }
        c.close();
        return houses;
    }


    /**
     * query house by banId and buildingsId and unitId, return list
     *
     * @return List<House>
     */
    public List<House> queryHouseByAndHouseNum(String buildingsId, String banId, String unitId, String houseNum) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT * FROM HOUSE left outer join METER on METER.METER_ID=HOUSE.METER_ID WHERE BUILDINGS_ID = ? AND BAN_ID = ? AND UNIT_ID = ? AND HOUSE_NUM = ?",
                new String[]{buildingsId, banId, unitId, houseNum});
        while (c.moveToNext()) {
            House house = new House();
            house.buildingsId = c.getString(c.getColumnIndex("BUILDINGS_ID"));
            house.buildingsName = c.getString(c.getColumnIndex("BUILDINGS_NAME"));
            house.banId = c.getString(c.getColumnIndex("BAN_ID"));
            house.unitId = c.getString(c.getColumnIndex("UNIT_ID"));
            house.floor = c.getString(c.getColumnIndex("FLOOR"));
            house.houseIndex = c.getInt(c.getColumnIndex("HOUSE_INDEX"));
            house.houseNum = c.getString(c.getColumnIndex("HOUSE_NUM"));
            house.householderName = c.getString(c.getColumnIndex("HOUSEHOLDER_NAME"));
            house.houseNetId = c.getString(c.getColumnIndex("HOUSE_NET_ID"));
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));
            house.meter = meter;

            if (house.meter.meterID != null) {
                Cursor c1 = db.rawQuery("select * from METER_DATA where METER_ID = ?", new String[]{house.meter.meterID});
                if (c1.moveToNext()) {
                    MeterData meterData = new MeterData();
                    meterData.meterID = house.meter.meterID;
                    meterData.dataValue = c1.getDouble(c1.getColumnIndex("DATA_VALUE"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    try {
                        meterData.dataTime = sdf.parse(c1.getString(c1.getColumnIndex("DATA_TIME")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                c1.close();
            }

            if (house.meter.meterID != null) {

                List<MeterData> meterDatas = this.queryMeterData(meter.meterID);
                if (meterDatas.size() > 0) {
                    house.meterDataLast = meterDatas.get(0);
                }
            }


            houses.add(house);
        }
        c.close();
        return houses;
    }


    /**
     * query house by banId and buildingsId and unitId, return list
     *
     * @return List<House>
     */
    public List<House> queryHouse(String buildingsId, String banId, String unitId, String floor, String houseNum) {
        ArrayList<House> houses = new ArrayList<House>();
        Cursor c = db.rawQuery("SELECT * FROM HOUSE left outer join METER on METER.METER_ID=HOUSE.METER_ID WHERE BUILDINGS_ID = ? AND BAN_ID = ? AND UNIT_ID = ? AND FLOOR = ? AND HOUSE_NUM = ?",
                new String[]{buildingsId, banId, unitId, floor, houseNum});
        while (c.moveToNext()) {
            House house = new House();
            house.buildingsId = c.getString(c.getColumnIndex("BUILDINGS_ID"));
            house.buildingsName = c.getString(c.getColumnIndex("BUILDINGS_NAME"));
            house.banId = c.getString(c.getColumnIndex("BAN_ID"));
            house.unitId = c.getString(c.getColumnIndex("UNIT_ID"));
            house.floor = c.getString(c.getColumnIndex("FLOOR"));
            house.houseIndex = c.getInt(c.getColumnIndex("HOUSE_INDEX"));
            house.houseNum = c.getString(c.getColumnIndex("HOUSE_NUM"));
            house.householderName = c.getString(c.getColumnIndex("HOUSEHOLDER_NAME"));
            house.houseNetId = c.getString(c.getColumnIndex("HOUSE_NET_ID"));
            Meter meter = new Meter();
            meter.meterID = c.getString(c.getColumnIndex("METER_ID"));
            meter.netID = c.getString(c.getColumnIndex("NET_ID"));
            meter.isActivated = c.getInt(c.getColumnIndex("IS_ACTIVATED"));
            house.meter = meter;

            if (house.meter.meterID != null) {
                Cursor c1 = db.rawQuery("select * from METER_DATA where METER_ID = ?", new String[]{house.meter.meterID});
                if (c1.moveToNext()) {
                    MeterData meterData = new MeterData();
                    meterData.meterID = house.meter.meterID;
                    meterData.dataValue = c1.getDouble(c1.getColumnIndex("DATA_VALUE"));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    try {
                        meterData.dataTime = sdf.parse(c1.getString(c1.getColumnIndex("DATA_TIME")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                c1.close();
            }

            if (house.meter.meterID != null) {

                List<MeterData> meterDatas = this.queryMeterData(meter.meterID);
                if (meterDatas.size() > 0) {
                    house.meterDataLast = meterDatas.get(0);
                }
            }


            houses.add(house);
        }
        c.close();
        return houses;
    }


    public void updateHouse(List<House> houseList) {

        for (House house : houseList) {
            ContentValues cv = new ContentValues();
            cv.put("METER_ID", house.meter.meterID);
            cv.put("HOUSE_NUM", house.houseNum);
            cv.put("HOUSEHOLDER_NAME", house.householderName);

            int num = db.update("HOUSE", cv, "BUILDINGS_ID = ? AND BAN_ID = ? AND UNIT_ID = ? AND FLOOR = ? AND HOUSE_INDEX = ? AND HOUSE_NET_ID = ?",
                    new String[]{house.buildingsId, house.banId, house.unitId, house.floor, house.houseIndex + "", house.houseNetId});

            Log.i("wdj", "update state:" + num + "");
        }
    }

    /**
     * add meterDatas
     *
     * @param meterDatas
     */
    public void addMeterData(List<MeterData> meterDatas) {
        db.beginTransaction();  //开始事务
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (MeterData meterData : meterDatas) {

                int state_valve = 0;

                switch (meterData.stateValve) {
                    case CLOSE:
                        state_valve = 0;
                        break;
                    case OPEN:
                        state_valve = 1;
                        break;
                    case ERROR:
                        state_valve = 2;
                        break;
                }
                int state_power_3_6_v = 0;
                if (meterData.statePower36V != null)
                    switch (meterData.statePower36V) {
                        case LOW:
                            state_power_3_6_v = 0;
                            break;
                        case OK:
                            state_power_3_6_v = 1;
                            break;
                    }
                int state_power_6_v = 0;
                if (meterData.statePower6V != null)
                    switch (meterData.statePower6V) {
                        case LOW:
                            state_power_6_v = 0;
                            break;
                        case OK:
                            state_power_6_v = 1;
                            break;
                    }


                double power_value = meterData.powerValue;


                db.execSQL("INSERT INTO METER_DATA VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new Object[]{meterData.meterID, meterData.dataValue, sdf.format(meterData.dataTime), state_valve, state_power_3_6_v, state_power_6_v, power_value, null});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception ex) {
            Log.i("wdj", ex.getMessage());
        } finally {
            db.endTransaction();    //结束事务
        }
    }


    /**
     * query meterData by meterId
     *
     * @return List<MeterData>
     */
    public List<MeterData> queryMeterData(String meterId) {
        ArrayList<MeterData> meterDatas = new ArrayList<MeterData>();
        Cursor c = db.rawQuery("SELECT * FROM METER_DATA WHERE METER_ID = ? ORDER BY DATA_TIME DESC", new String[]{meterId});
        while (c.moveToNext()) {
            MeterData meterData = new MeterData();

            meterData.meterID = meterId;
            meterData.dataValue = c.getDouble(c.getColumnIndex("DATA_VALUE"));

            int state_valve = c.getInt(c.getColumnIndex("STATE_VALVE"));
            int state_power_3_6_v = c.getInt(c.getColumnIndex("STATE_POWER_3_6_V"));
            int state_power_6_v = c.getInt(c.getColumnIndex("STATE_POWER_6_V"));
            int power_value = c.getInt(c.getColumnIndex("POWER_VALUE"));
            meterData.powerValue = power_value;

            switch (state_valve) {
                case 0:
                    meterData.stateValve = MeterStateConst.STATE_VALVE.CLOSE;
                    break;
                case 1:
                    meterData.stateValve = MeterStateConst.STATE_VALVE.OPEN;
                    break;
                case 2:
                    meterData.stateValve = MeterStateConst.STATE_VALVE.ERROR;
                    break;
            }

            switch (state_power_3_6_v) {
                case 0:
                    meterData.statePower36V = MeterStateConst.STATE_POWER_3_6_V.LOW;
                    break;
                case 1:
                    meterData.statePower36V = MeterStateConst.STATE_POWER_3_6_V.OK;
                    break;
            }

            switch (state_power_6_v) {
                case 0:
                    meterData.statePower6V = MeterStateConst.STATE_POWER_6_V.LOW;
                    break;
                case 1:
                    meterData.statePower6V = MeterStateConst.STATE_POWER_6_V.OK;
                    break;
            }


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            try {
                meterData.dataTime = sdf.parse(c.getString(c.getColumnIndex("DATA_TIME")));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            meterDatas.add(meterData);
        }
        c.close();
        return meterDatas;
    }
}

package com.rthc.switchboxrthc.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.rthc.wdj.bluetoothtoollib.cmd.MeterStateConst;

import java.util.Date;

/**
 * Created by Administrator on 2015/9/3.
 * 表数据
 */
public class MeterData implements Parcelable {

    /**
     * 表ID
     */
    public String meterID;

    /**
     * 抄表数据
     */
    public double dataValue;

    /**
     * 数据时间 数据的实际时间
     */
    public Date dataTime;

    /**
     * 抄表时间 抄表员抄表时间
     */
    public Date readDataTime;

    /**
     * 阀门状态
     */
    public MeterStateConst.STATE_VALVE stateValve;

    /**
     * 3.6v电源状态
     */
    public MeterStateConst.STATE_POWER_3_6_V statePower36V;

    /**
     * 6v电源状态
     */
    public MeterStateConst.STATE_POWER_6_V statePower6V;

    /**
     * 电源电压  利尔达有
     */
    public double powerValue;

    public MeterData(){

    }

    public MeterData(String meterID, double dataValue, Date dataTime, Date readDataTime){
        this.meterID = meterID;
        this.dataValue = dataValue;
        this.dataTime = dataTime;
        this.readDataTime = readDataTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.meterID);
        dest.writeDouble(this.dataValue);
        dest.writeLong(dataTime != null ? dataTime.getTime() : -1);
        dest.writeLong(readDataTime != null ? readDataTime.getTime() : -1);
        dest.writeInt(this.stateValve == null ? -1 : this.stateValve.ordinal());
        dest.writeInt(this.statePower36V == null ? -1 : this.statePower36V.ordinal());
        dest.writeInt(this.statePower6V == null ? -1 : this.statePower6V.ordinal());
        dest.writeDouble(this.powerValue);
    }

    protected MeterData(Parcel in) {
        this.meterID = in.readString();
        this.dataValue = in.readDouble();
        long tmpDataTime = in.readLong();
        this.dataTime = tmpDataTime == -1 ? null : new Date(tmpDataTime);
        long tmpReadDataTime = in.readLong();
        this.readDataTime = tmpReadDataTime == -1 ? null : new Date(tmpReadDataTime);
        int tmpStateValve = in.readInt();
        this.stateValve = tmpStateValve == -1 ? null : MeterStateConst.STATE_VALVE.values()[tmpStateValve];
        int tmpStatePower36V = in.readInt();
        this.statePower36V = tmpStatePower36V == -1 ? null : MeterStateConst.STATE_POWER_3_6_V.values()[tmpStatePower36V];
        int tmpStatePower6V = in.readInt();
        this.statePower6V = tmpStatePower6V == -1 ? null : MeterStateConst.STATE_POWER_6_V.values()[tmpStatePower6V];
        this.powerValue = in.readDouble();
    }

    public static final Creator<MeterData> CREATOR = new Creator<MeterData>() {
        public MeterData createFromParcel(Parcel source) {
            return new MeterData(source);
        }

        public MeterData[] newArray(int size) {
            return new MeterData[size];
        }
    };
}
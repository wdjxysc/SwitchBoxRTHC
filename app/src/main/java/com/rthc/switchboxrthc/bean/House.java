package com.rthc.switchboxrthc.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/11/19.
 */
public class House implements Parcelable {

    /**
     * 小区编号
     */
    public String buildingsId;

    /**
     * 小区名字
     */
    public String buildingsName;

    /**
     * 楼栋
     */
    public String banId;

    /**
     * 单元
     */
    public String unitId;

    /**
     * 楼层
     */
    public String floor;

    /**
     * 房序
     */
    public int houseIndex;

    /**
     * 门牌号
     */
    public String houseNum;

    /**
     * 户主名
     */
    public String householderName;

    /**
     * 表ID
     */
    public Meter meter;

    /**
     * 最近一次数据
     */
    public MeterData meterDataLast;

    /**
     * 建档时填写的netId
     */
    public String houseNetId;

    public House(){

    }

    public House(String buildingsId, String buildingsName, String banId, String unitId, String floor, int houseIndex, String houseNum, String householderName, Meter meter, MeterData meterDataLast){
        this.buildingsId = buildingsId;
        this.buildingsName = buildingsName;
        this.banId = banId;
        this.unitId = unitId;
        this.floor = floor;
        this.houseIndex = houseIndex;
        this.houseNum = houseNum;
        this.householderName = householderName;
        this.meter = meter;
        this.meterDataLast = meterDataLast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.buildingsId);
        dest.writeString(this.buildingsName);
        dest.writeString(this.banId);
        dest.writeString(this.unitId);
        dest.writeString(this.floor);
        dest.writeInt(this.houseIndex);
        dest.writeString(this.houseNum);
        dest.writeString(this.householderName);
        dest.writeParcelable(this.meter, 0);
        dest.writeParcelable(this.meterDataLast, 0);
        dest.writeString(this.houseNetId);
    }

    protected House(Parcel in) {
        this.buildingsId = in.readString();
        this.buildingsName = in.readString();
        this.banId = in.readString();
        this.unitId = in.readString();
        this.floor = in.readString();
        this.houseIndex = in.readInt();
        this.houseNum = in.readString();
        this.householderName = in.readString();
        this.meter = in.readParcelable(Meter.class.getClassLoader());
        this.meterDataLast = in.readParcelable(MeterData.class.getClassLoader());
        this.houseNetId = in.readString();
    }

    public static final Creator<House> CREATOR = new Creator<House>() {
        public House createFromParcel(Parcel source) {
            return new House(source);
        }

        public House[] newArray(int size) {
            return new House[size];
        }
    };
}

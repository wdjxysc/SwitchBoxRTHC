package com.rthc.switchboxrthc.bean;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Administrator on 2015/9/22.
 * 单表信息
 */
public class Meter implements Parcelable {
    /**
     * 表ID
     */
    public String meterID;

    /**
     * 网络ID
     */
    public String netID;

    /**
     * 节点ID
     */
    public String nodeID;

    /**
     * 表流水号
     */
    public String meterSerialNum;

    /**
     * 是否激活使用
     */
    public int isActivated;

    /**
     * 生产日期
     */
    public Date productionDate;

    /**
     * 安装日期
     */
    public Date installDate;

    public Meter() {

    }

    public Meter(String meterID, String netID, String nodeID, int isActivated, String meterSerialNum, Date productionDate) {
        this.meterID = meterID;
        this.netID = netID;
        this.nodeID = nodeID;
        this.isActivated = isActivated;
        this.meterSerialNum = meterSerialNum;
        this.productionDate = productionDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.meterID);
        dest.writeString(this.netID);
        dest.writeString(this.nodeID);
        dest.writeString(this.meterSerialNum);
        dest.writeInt(this.isActivated);
        dest.writeLong(productionDate != null ? productionDate.getTime() : -1);
        dest.writeLong(installDate != null ? installDate.getTime() : -1);
    }

    protected Meter(Parcel in) {
        this.meterID = in.readString();
        this.netID = in.readString();
        this.nodeID = in.readString();
        this.meterSerialNum = in.readString();
        this.isActivated = in.readInt();
        long tmpProductionDate = in.readLong();
        this.productionDate = tmpProductionDate == -1 ? null : new Date(tmpProductionDate);
        long tmpInstallDate = in.readLong();
        this.installDate = tmpInstallDate == -1 ? null : new Date(tmpInstallDate);
    }

    public static final Parcelable.Creator<Meter> CREATOR = new Parcelable.Creator<Meter>() {
        public Meter createFromParcel(Parcel source) {
            return new Meter(source);
        }

        public Meter[] newArray(int size) {
            return new Meter[size];
        }
    };
}

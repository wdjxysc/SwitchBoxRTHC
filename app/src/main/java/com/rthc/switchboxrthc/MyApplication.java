package com.rthc.switchboxrthc;

import android.app.Application;

import com.rthc.wdj.bluetoothtoollib.cmd.Cmd;

/**
 * Created by Administrator on 2016/9/18.
 *
 * @author wdjxysc
 */
public class MyApplication extends Application {


    /**
     * 当前选择的ID格式 4byte id  还是 2byte id
     */
    public static Cmd.RF_NODE_ID_TYPE currentNodeIdType = Cmd.RF_NODE_ID_TYPE.NODE_ID_4_BYTES;


}

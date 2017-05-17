package com.rthc.switchboxrthc.action;

import android.view.View;

/**
 * Created by Administrator on 2015/11/20.
 */
public interface ItemAction {
    void OnItemClick(View view, int position);
    void OnItemLongClick(View view, int position);
}

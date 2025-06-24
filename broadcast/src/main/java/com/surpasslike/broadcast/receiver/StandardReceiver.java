package com.surpasslike.broadcast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// 广播接收者必须要继承BroadcastReceiver
public class StandardReceiver extends BroadcastReceiver {

    public static final String STANDARD_ACTION = "com.surpasslike.broadcast.standard";

    private String TAG = "StandardReceiver";

    // 一旦接收到标准广播就会触发onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(STANDARD_ACTION)) {
            Log.d(TAG, "收到了一个标准广播");
        }
    }
}
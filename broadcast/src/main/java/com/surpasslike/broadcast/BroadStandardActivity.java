package com.surpasslike.broadcast;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.surpasslike.broadcast.receiver.StandardReceiver;

public class BroadStandardActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broad_standard);
        findViewById(R.id.btn_send_standard).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //发送标准广播
        Intent intent = new Intent(StandardReceiver.STANDARD_ACTION);
        sendBroadcast(intent);
    }

    StandardReceiver standardReceiver = new StandardReceiver();

    @Override
    protected void onStart() {
        super.onStart();
        //  创建一个意图过滤器,只处理STANDARD_ACTION广播
        IntentFilter filter = new IntentFilter(StandardReceiver.STANDARD_ACTION);
        // 注册接收器,注册之后才能正常接收广播
        registerReceiver(standardReceiver, filter,RECEIVER_EXPORTED );
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销接收器.注销之后就不再接收广播
        unregisterReceiver(standardReceiver);
    }
}
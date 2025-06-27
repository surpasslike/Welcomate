package com.surpasslike.welcomate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.surpasslike.welcomate.databinding.ActivityGuestHomeBinding;

public class GuestHomeActivity extends AppCompatActivity {

    private ActivityGuestHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuestHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        String welcomeMessage = "Welcome, Guest!";
        binding.tvWelcomeMessage.setText(welcomeMessage);

        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuestHomeActivity.this, com.surpasslike.setting.SettingActivity.class);
                startActivity(intent);
            }
        });

        binding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
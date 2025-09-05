package com.example.practica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProfileUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "LOGIN_UPDATED":
                    String newLogin = intent.getStringExtra("new_login");
                    // Обновите логин в UI
                    break;
                case "USERNAME_UPDATED":
                    String newUsername = intent.getStringExtra("new_username");
                    // Обновите имя пользователя в UI
                    break;
            }
        }
    }
}
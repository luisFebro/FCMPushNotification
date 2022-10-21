package com.febro.fcmpushnotification;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String tk = MyFirebaseMessagingService.getToken(this);
        MyFirebaseMessagingService.subscribeUser(this);
        Log.d("LOGG_hello", "notifToken: " + tk);
    }
}
package com.febro.fcmpushnotification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class NotificationScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);
       // binding = DataBindingUtil.setContentView(this, R.layout.notification_screen);
        Button notifBtnVw = findViewById(R.id.back_notif_btn);

        notifBtnVw.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ImageView notifImgVw = findViewById(R.id.img_notif);
        TextView descVw = findViewById(R.id.desc);

        String imgUrl = "";
        String messageBody = "";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imgUrl = bundle.getString("imgUrl");
            messageBody = bundle.getString("messageBody");
        }
        descVw.setText(messageBody);

        // lib: https://github.com/square/picasso
        Picasso.get().load(imgUrl).into(notifImgVw);
    }
}
package com.tam.fittimetable;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        button = (Button) findViewById(R.id.login);
        LinearLayout myLayout = (LinearLayout) this.findViewById(R.id.loginLayout);
        getSupportActionBar().hide();
        myLayout.requestFocus();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });
    }
   public void openActivity2(){
        Intent intent = new Intent(this, TimetableActivity.class);
        startActivity(intent);

    }
}

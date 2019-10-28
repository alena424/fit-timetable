package com.tam.fittimetable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.tam.fittimetable.R;
import com.tam.fittimetable.backend.core.data.Subject;
import com.tam.fittimetable.backend.core.data.SubjectManager;
import com.tam.fittimetable.backend.core.extract.DownloadException;

import java.text.ParseException;


public class MainActivity extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        button = (Button) findViewById(R.id.login);
        LinearLayout myLayout = (LinearLayout) this.findViewById(R.id.loginLayout);
        myLayout.requestFocus();

        //Downloader.setMyContext(this);

        //new AsyncCaller().execute();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openActivity2();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (DownloadException e) {
                    e.printStackTrace();
                }
            }
        });
    }
   public void openActivity2() throws ParseException, DownloadException {

        EditText nameEdit = findViewById(R.id.loginName);
        EditText passwordEdit = findViewById(R.id.passwordId);
       String name = String.valueOf(nameEdit.getText());
       String password = String.valueOf(passwordEdit.getText());

      // SubjectManager courses = SubjectManager.get();
       /*for (Subject course: SubjectManager.get().getSubjects()
            ) {
           System.out.println(course.toString());
       }
        System.out.println(name +" "+ password);*/

       Intent intent = new Intent(this, StaticActivity.class);
       startActivity(intent);

    }
}

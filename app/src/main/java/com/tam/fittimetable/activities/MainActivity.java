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
import com.tam.fittimetable.backend.core.extract.AsyncCaller;
import com.tam.fittimetable.backend.core.extract.DownloadException;
import com.tam.fittimetable.backend.core.extract.Downloader;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

        Downloader.setMyContext(this);

        //new AsyncCaller().execute();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncCaller().execute();
                try {
                    openActivity2();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (DownloadException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
   public void openActivity2() throws ParseException, IOException {

        EditText nameEdit = findViewById(R.id.loginName);
        EditText passwordEdit = findViewById(R.id.passwordId);
       String name = String.valueOf(nameEdit.getText());
       String password = String.valueOf(passwordEdit.getText());

      // SubjectManager courses = SubjectManager.get();
      // InputStream caInput = this.getResources().openRawResource(R.raw.fitcacert);
       for (Subject course: SubjectManager.get().getSubjects()
            ) {
           System.out.println(course.toJson());
       }
        System.out.println(name +" "+ password);

       /*Intent intent = new Intent(this, StaticActivity.class);
       startActivity(intent);*/

    }
}

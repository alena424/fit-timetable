package com.tam.fittimetable.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.tam.fittimetable.R;
import com.tam.fittimetable.backend.core.data.Strings;
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        button = (Button) findViewById(R.id.login);
        LinearLayout myLayout = (LinearLayout) this.findViewById(R.id.loginLayout);
        myLayout.requestFocus();

        Downloader.setMyContext(this);

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, Strings.WRITE_REQUEST_CODE);
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
           //System.out.println(course.toJson());
       }
        System.out.println(name +" "+ password);
       System.out.println(SubjectManager.get().getJson());

       /*Intent intent = new Intent(this, StaticActivity.class);
       startActivity(intent);*/

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Strings.WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Granted.

                    System.out.println("Garantovany pristup");

                }
                else{
                    //Denied.
                }
                break;
        }
    }
}

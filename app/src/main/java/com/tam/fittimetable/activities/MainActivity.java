package com.tam.fittimetable.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.JsonArray;
import com.tam.fittimetable.R;
import com.tam.fittimetable.backend.core.data.Strings;
import com.tam.fittimetable.backend.core.data.Subject;
import com.tam.fittimetable.backend.core.data.SubjectManager;
import com.tam.fittimetable.backend.core.extract.AsyncCaller;
import com.tam.fittimetable.backend.core.extract.DownloadException;
import com.tam.fittimetable.backend.core.extract.Downloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.tam.fittimetable.util.ExtensionsKt.showToast;


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

        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;

        EditText nameEdit = findViewById(R.id.loginName);
        EditText passwordEdit = findViewById(R.id.passwordId);

        if ( System.getProperty("login") != null ){
            nameEdit.setText(System.getProperty("login"));
        }
        if ( System.getProperty("password") != null ){
            passwordEdit.setText(System.getProperty("password"));
        }

        Downloader.setMyContext(this);
        Downloader.recreateDir();

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

System.out.println(Build.VERSION.SDK_INT);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
            requestPermissions(permissions, Strings.WRITE_REQUEST_CODE);
        } else {
            updateAndroidSecurityProvider(this);
        }

        //new AsyncCaller().execute();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();


            }
        });
    }

    private void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            showToast(this,"Google Play Services not available.");
        }
    }

   public void openActivity2(){
       EditText nameEdit = findViewById(R.id.loginName);
       EditText passwordEdit = findViewById(R.id.passwordId);

       String name = String.valueOf(nameEdit.getText());
       String password = String.valueOf(passwordEdit.getText());

      // SubjectManager courses = SubjectManager.get();
      // InputStream caInput = this.getResources().openRawResource(R.raw.fitcacert);
      // for (Subject course: SubjectManager.get().getSubjects()
           // ) {
           //System.out.println(course.toJson());
       //}

      System.out.println(name +" tadz je heslo "+ name + " " +password);

       final Intent intent = new Intent(this, StaticActivity.class);
       ExecutorService es = Executors.newSingleThreadExecutor();
       Downloader.setAuth(name,password);
       System.setProperty("login", name);
       System.setProperty("password", password);
       FileOutputStream fos = null;

       try {
          // SubjectManager manager = new SubjectManager();

           Future future = es.submit(new SubjectManager().get());
           es.awaitTermination(5, TimeUnit.SECONDS);
           future.get(); // status of task
         //  System.out.println("result je " +result);
           /*for (Subject s : SubjectManager.get().getSubjects()) {
               System.out.println(s);
           }*/
           //JsonArray jsonData = SubjectManager.get().getJson(this);
           JsonArray jsonData = SubjectManager.json;
           FileOutputStream fileOutputStream = this.openFileOutput(Strings.FILE_NAME, MODE_PRIVATE);
           fileOutputStream.write(jsonData.toString().getBytes());
           System.out.println(jsonData.toString());


           //SubjectManager.get().getSubjects();

           showToast(this,"Úspěšně přihlášen");
           System.setProperty("login", name);
           System.setProperty("password", password);

           //new AsyncCaller().execute();

           startActivity(intent);
       } catch (IOException e) {
           e.printStackTrace();
           showToast(this,e.getMessage());
       } catch (ParseException e) {
           e.printStackTrace();
           showToast(this,e.getMessage());

       } catch (InterruptedException e) {
           e.printStackTrace();
           showToast(this,e.getMessage());
       } catch (ExecutionException e) {
           e.printStackTrace();
           showToast(this,e.getMessage());
       } finally {
           if (fos != null){
               try {
                   fos.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
       //showToast(this,"FAIL");

       //   while (SubjectManager.get().running);

      // System.out.println(SubjectManager.get().getJson());

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

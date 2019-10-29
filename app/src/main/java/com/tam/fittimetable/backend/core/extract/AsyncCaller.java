package com.tam.fittimetable.backend.core.extract;

import android.os.AsyncTask;

import com.tam.fittimetable.backend.core.data.Subject;
import com.tam.fittimetable.backend.core.data.SubjectManager;

import java.text.ParseException;

public class AsyncCaller extends AsyncTask<Void, Void, Void>
{
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        System.out.println("Start of downloading");
    }
    @Override
    protected Void doInBackground(Void... params) {

        //this method will be running on background thread so don't update UI frome here
        //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
        System.out.println("Downloading in progress");

        try {
            Thread m = new Thread(SubjectManager.get());
            m.run();
            for (Subject s : SubjectManager.get().getSubjects()) {
                System.out.println(s);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (DownloadException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        //this method will be running on UI thread
        System.out.println("End of downloading");
    }

}
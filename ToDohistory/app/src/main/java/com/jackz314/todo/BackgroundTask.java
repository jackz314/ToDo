package com.jackz314.todo;

import android.os.AsyncTask;

/**
 * Created by zhang on 2017/9/13.
 */

public class BackgroundTask extends AsyncTask<Void,Void,Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}

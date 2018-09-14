package com.jackz314.todo;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

//get data from url then return the data received
public class GetFromURL{

    private static URLCallBack callBack;

    public static URLCallBack getCallBack() {
        return callBack;
    }

    public static void setCallBack(URLCallBack callBack) {
        GetFromURL.callBack = callBack;
    }

    public static void getFromURL(URLCallBack urlCallBack, String URL){
        callBack = urlCallBack;
        new GetURLAsyncTask(callBack, URL).execute();
    }

    public interface URLCallBack{

        void onURLContentReceived(String content);

        void onRequestError(Exception e);

    }

    private static class GetURLAsyncTask extends AsyncTask<String, String, Void>{

        String result = "";
        String urlStr = "";
        URLCallBack urlCallBack;

        public GetURLAsyncTask(URLCallBack urlCallBack, String URL){
            this.urlCallBack = urlCallBack;
            urlStr = URL;
        }

        protected void onPreExecute() {

        }
        @Override
        protected Void doInBackground(String... params) {

            BufferedReader reader = null;
            try {
                URL url = new URL(urlStr);
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder buffer = new StringBuilder();
            /*not using the char[] reading method
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);*/
                int read;
                while ((read = reader.read()) != -1){//result is used as a line to line temporary variable here
                    buffer.append((char)read);
                }
                result = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
                urlCallBack.onRequestError(e);
            }
            return null;
        }
        protected void onPostExecute(Void v) {
            urlCallBack.onURLContentReceived(result);
        }
    }


}
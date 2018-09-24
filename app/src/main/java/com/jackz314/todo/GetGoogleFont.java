package com.jackz314.todo;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;

//note that the default weight should be 400 if requesting a font with default format of it
public class GetGoogleFont {

    private static GoogleFontCallback callback;

    private static Handler fontHandler;

    private static Handler getFontHandlerThreadHandler() {
        if (fontHandler == null) {
            HandlerThread handlerThread = new HandlerThread("google_fonts_handler");
            handlerThread.start();
            fontHandler = new Handler(handlerThread.getLooper());
        }
        return fontHandler;
    }

    public static void requestGoogleFont(final GoogleFontCallback googleFontCallback, String query, final Context context){
        callback = googleFontCallback;
        //no need as the default value for best effort is true
        //query.append("&besteffort=").append(true);//request with best effort every time just to avoid weird issues
        FontRequest request = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                query,
                R.array.com_google_android_gms_fonts_certs);
        System.out.println("Getting Font : " + query.substring(query.indexOf("name=") + 1));
        FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                googleFontCallback.onFontRetrieved(typeface);
                System.out.println("Retrieved mTypeface " + typeface.toString());
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                googleFontCallback.onFontRequestError(reason);
            }
        };

        FontsContractCompat.requestFont(context, request, callback, getFontHandlerThreadHandler());
    }

    public static void requestGoogleFont(final GoogleFontCallback googleFontCallback, String fontName, int weight, boolean italic, boolean mono, final Context context){
        requestGoogleFont(googleFontCallback, getFontQueryString(fontName, weight, italic, mono), context);
    }

    public static String getFontQueryString(String fontName, int weight, boolean italic, boolean mono){
        //form query string
        StringBuilder query = new StringBuilder();
        query.append("name=").append(fontName)
                .append("&weight=").append(weight);
        if(italic){
            query.append("&italic=1");//means that request italic
        }
        if(mono){
            query.append("&width=100");//means that request monospace
        }
        return query.toString();
    }

    public static GoogleFontCallback getCallback() {
        return callback;
    }

    public interface GoogleFontCallback {

        void onFontRetrieved(Typeface typeface);

        void onFontRequestError(int errorCode);

    }

}

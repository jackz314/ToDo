package com.jackz314.todo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jackz314.todo.utils.ColorUtils;

import static com.jackz314.todo.MainActivity.getMacAddr;

public class AboutActivity extends AppCompatActivity {
    Button supportBtn,rateBtn;
    TextView introText,contactText,versionText,emailContact,licensesText;
    SharedPreferences sharedPreferences;
    int themeColor,textColor,backgroundColor;
    ConstraintLayout aboutView;
    ColorUtils colorUtils;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        supportBtn = (Button)findViewById(R.id.support_button);
        rateBtn = (Button)findViewById(R.id.rate_button);
        introText = (TextView)findViewById(R.id.intro_text);
        contactText = (TextView)findViewById(R.id.contact_text);
        emailContact = (TextView)findViewById(R.id.email_contact);
        versionText = (TextView)findViewById(R.id.version_text);
        licensesText = (TextView)findViewById(R.id.licenses);
        sharedPreferences = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorActualPrimary));
        textColor=sharedPreferences.getInt(getString(R.string.text_color_key), Color.BLACK);
        backgroundColor=sharedPreferences.getInt(getString(R.string.background_color_key),Color.WHITE);
        aboutView = (ConstraintLayout)findViewById(R.id.aboutView);
        Window window = this.getWindow();
        window.setStatusBarColor(themeColor);
        window.setNavigationBarColor(themeColor);
        ActionBar actionBar = getSupportActionBar();
        Drawable actionBarColor = new ColorDrawable(themeColor);
        actionBarColor.setColorFilter(themeColor, PorterDuff.Mode.DST);
        actionBar.setBackgroundDrawable(actionBarColor);
        String versionName = BuildConfig.VERSION_NAME;
        versionText.setText(getString(R.string.version_text)+versionName);
        versionText.setTextColor(colorUtils.lighten(textColor,0.4));
        introText.setTextColor(textColor);
        contactText.setTextColor(textColor);
        emailContact.setLinkTextColor(colorUtils.lighten(themeColor,0.2));
        String systemInfo = "";
        String macAddress = getMacAddr().replace(":","-");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemInfo = "System Info: " + "\n" + "("+ Build.MANUFACTURER + "||\n" + Build.BRAND + "||\n" + Build.DEVICE + "||\n" + Build.MODEL + "||\n"+ Build.HARDWARE + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.CODENAME + "||\n" + Build.VERSION.SDK_INT + "||\n" +  Build.VERSION.INCREMENTAL + "||\n" + Build.VERSION.SECURITY_PATCH + "||\n" + macAddress + ")";
        }else {
            systemInfo = "System Info: " + "\n" + "(" + Build.MANUFACTURER + "||\n"+ Build.BRAND + "||\n"+ Build.DEVICE + "||\n"+ Build.MODEL + "||\n" + Build.HARDWARE + "||\n" + Build.VERSION.SDK_INT + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.INCREMENTAL + "||\n" + macAddress + ") ";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            emailContact.setText(Html.fromHtml("<a href=\"mailto:"+"jackz314sci@gmail.com"+"?subject="+getString(R.string.email_subject)+"&body="+getString(R.string.email_content)+systemInfo+"\" >"+getString(R.string.email)+"</a>",Html.FROM_HTML_MODE_LEGACY));
        } else {
            emailContact.setText(Html.fromHtml("<a href=\"mailto:"+"jackz314sci@gmail.com"+"?subject="+getString(R.string.email_subject)+"&body="+getString(R.string.email_content)+systemInfo+"\" >"+getString(R.string.email)+"</a>"));
        }
        emailContact.setClickable(true);
        emailContact.setMovementMethod(LinkMovementMethod.getInstance());
        supportBtn.setTextColor(textColor);
        supportBtn.setBackgroundColor(colorUtils.darken(backgroundColor,0.3));
        rateBtn.setBackgroundColor(colorUtils.darken(backgroundColor,0.3));
        rateBtn.setTextColor(textColor);
        aboutView.setBackgroundColor(backgroundColor);
        //final SpannableString linkedMsg = new SpannableString(getString(R.string.library_license_txt));
        //Linkify.addLinks(linkedMsg, Linkify.ALL);test
        licensesText.setPaintFlags(licensesText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        licensesText.setTextColor(ColorUtils.lighten(textColor,0.4));
        licensesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(AboutActivity.this, R.layout.about_dialog, null);
                TextView textView = (TextView) view.findViewById(R.id.message);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setText(R.string.library_license_txt);
                textView.setLinkTextColor(ColorUtils.lighten(themeColor,0.2));
                final AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this)
                        .setView(view)
                        .setTitle(getString(R.string.licenses))
                        .setPositiveButton(getString(R.string.got_it), null)
                        .show();
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor);
                ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

            }
        });
        supportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(R.id.support_button));
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "donate button");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://paypal.me/jackz314payme/1"));
                startActivity(browserIntent);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),getString(R.string.thanks_for_support),Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
                //put donate method here!
            }
        });
        rateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(R.id.rate_button));
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "rate button");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                if (intent.resolveActivity(getPackageManager()) != null) { //no problem with play store
                    startActivity(intent);
                } else { //no play store found use web page!
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                    if (intent.resolveActivity(getPackageManager()) != null) { //no problem with play store
                        startActivity(intent);
                    }
                    else Toast.makeText(getApplicationContext(),getString(R.string.impossible_text),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

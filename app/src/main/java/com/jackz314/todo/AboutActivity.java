package com.jackz314.todo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.w3c.dom.Text;

import static com.jackz314.todo.R.color.colorPrimary;

public class AboutActivity extends AppCompatActivity {
    Button supportBtn,rateBtn;
    TextView introText,contactText,versionText,emailContact;
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
        sharedPreferences = getSharedPreferences("settings_data",MODE_PRIVATE);
        themeColor=sharedPreferences.getInt(getString(R.string.theme_color_key),getResources().getColor(R.color.colorPrimary));
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
        String systemInfo ="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemInfo = "("+ Build.MANUFACTURER + "||\n" + Build.BRAND + "||\n" + Build.DEVICE + "||\n" + Build.MODEL + "||\n"+ Build.HARDWARE + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.CODENAME + "||\n" + Build.VERSION.SDK_INT + "||\n" +  Build.VERSION.INCREMENTAL + "||\n" + Build.VERSION.SECURITY_PATCH + ")";
        }else {
            systemInfo = "(" + Build.MANUFACTURER + "||\n"+ Build.BRAND + "||\n"+ Build.DEVICE + "||\n"+ Build.MODEL + "||\n" + Build.HARDWARE + "||\n" + Build.VERSION.SDK_INT + "||\n" + Build.VERSION.RELEASE + "||\n" + Build.VERSION.INCREMENTAL + ") ";
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
        supportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(R.id.support_button));
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "donate button");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
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

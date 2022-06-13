package com.example.smartcarquymvp.control;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartcarquymvp.NetworkImpl;
import com.example.smartcarquymvp.R;

public class ControlActivity extends AppCompatActivity implements ContractControl.ControlView {

    boolean doubleBackToExitPressedOnce = false;

    private WebView webView;
    private ImageView imgConnect, imgWarnFront, imgWarnBehind;
    private TextView lblStateShow;
    private JoystickView jsViewLeft, jsViewRight;

    private ControlPresenter presenter;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        webView = findViewById(R.id.webView);
        imgConnect = findViewById(R.id.imgConnect);
        imgWarnFront = findViewById(R.id.imgWarnFront);
        imgWarnBehind = findViewById(R.id.imgWarnBehind);
        lblStateShow = findViewById(R.id.lblStateShow);
        jsViewRight = findViewById(R.id.jsViewRight);
        jsViewLeft = findViewById(R.id.jsViewLeft);

        //Hide img Warning
        imgWarnFront.setVisibility(View.INVISIBLE);
        imgWarnBehind.setVisibility(View.INVISIBLE);

        //Set background for Webview
        webView.setBackground(getDrawable(R.drawable.raspberrypi_background));
        webView.setBackgroundColor(0x00000000);

        presenter = new ControlPresenter(ControlActivity.this, new NetworkImpl());

        imgConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.creOrDisConnect();
            }
        });

        jsViewLeft.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
//                System.out.println("Angle: " + angle+ " Power: "+ power + " Direction: " + direction);
                presenter.sendData("WHEEL#"+ direction);
            }
        },JoystickView.DEFAULT_LOOP_INTERVAL);

        jsViewRight.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
//                System.out.println("Angle: " + angle+ " Power: "+ power + " Direction: " + direction);
                presenter.sendData("CAMERA#"+direction);
            }
        },JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    protected void onStart(){
        super.onStart();
//        presenter.connectVideo_UDP();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void connect(View v){
        presenter.creOrDisConnect();
    }

    @Override
    public void startWebView(String ipAddress) {
        //Create video stream
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(presenter.URLVideo(ipAddress));
    }

    @Override
    public void stopWebView() {
        //Clear webView
        webView.loadUrl("about:blank");
        //Change background
        webView.setBackground(getDrawable(R.drawable.raspberrypi_background));
    }

    @Override
    public void changeColorImgConnecting() {
        //Đổi hình connect và màu xanh
        imgConnect.setImageResource(R.drawable.connect);
        imgConnect.setColorFilter(Color.parseColor("#2296F3"));
    }

    @Override
    public void changeColorImgDisconnect() {
        //Đổi hình disconnect và màu đen
        imgConnect.setImageResource(R.drawable.disconnected);
        imgConnect.setColorFilter(Color.parseColor("#000000"));
    }

    @Override
    public void changeBgWebViewDisconnect(){
        webView.setBackground(getDrawable(R.drawable.raspberrypi_background));
    }

    @Override
    public void visibleWarningFront(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgWarnFront.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void visibleWarningBehind(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgWarnBehind.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void goneWarningFront(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgWarnFront.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void goneWarningBehind(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgWarnBehind.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void setTextView(String stringTextView){
        lblStateShow.setText(stringTextView);
    }

    @Override
    public void getToast(String notification) {
        Toast.makeText(ControlActivity.this, notification, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void getDisableTouch() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        },0);
    }

    @Override
    public void getEnableTouch() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        },500);
    }

    @Override
    public String getUUIDFromSharedPref(){
        SharedPreferences sharedPreferences = null;
        sharedPreferences = getSharedPreferences("DataPreferences", MODE_PRIVATE);
        String uuid = sharedPreferences.getString("UUID", null);
        return uuid;
    }

    @Override
    public void runMethodOnUIThread(Runnable runnable){
        runOnUiThread(runnable);
    }
}
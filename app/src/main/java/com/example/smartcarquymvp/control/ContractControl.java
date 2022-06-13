package com.example.smartcarquymvp.control;

import javax.net.ssl.SSLSocketFactory;

public interface ContractControl {
    interface Feature{
        void captureImage();

        void modifySpeed();

        void cautionBarrier();
    }

    interface ControlView {
        void getDisableTouch();
        void getEnableTouch();

        void startWebView(String ipAddress);
        void stopWebView();

        void changeColorImgConnecting();
        void changeColorImgDisconnect();

        void changeBgWebViewDisconnect();

        //Show/hide warning front
        void visibleWarningFront();
        void goneWarningFront();

        //Show/hide warning behind
        void visibleWarningBehind();
        void goneWarningBehind();

        void setTextView(String stringTextView);
        void getToast(String notification);

        String getUUIDFromSharedPref();

        void runMethodOnUIThread(Runnable runnable);

    }

    interface ControlPresenter {
        void creOrDisConnect();

        void sendData(String messenge);

        String URLVideo(String ipAddress);
    }
}

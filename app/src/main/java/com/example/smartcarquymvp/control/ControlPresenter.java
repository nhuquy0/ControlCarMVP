package com.example.smartcarquymvp.control;

import android.os.Handler;
import android.util.Log;

import com.example.smartcarquymvp.MyObserver;
import com.example.smartcarquymvp.NetworkInterface;

public class ControlPresenter implements ContractControl.ControlPresenter, MyObserver.ObserverNetworkModel {

    private final ContractControl.ControlView mainControlView;
    private final NetworkInterface.Network modelNetwork;
    Handler handler = new Handler();

    public ControlPresenter(ContractControl.ControlView mainControlView, NetworkInterface.Network modelNetwork) {
        this.mainControlView = mainControlView;
        this.modelNetwork = modelNetwork;
        modelNetwork.attachObserverStateConnecting(ControlPresenter.this);
        modelNetwork.attachObserverMessFromServer((ControlPresenter.this));
    }

    @Override
    public String URLVideo(String ipAddress) {
        return "http://" + ipAddress + ":" + modelNetwork.getPortVideo() + "/";
    }

    @Override
    public void creOrDisConnect() {
        if(modelNetwork.getStateConnecting() == false) {
            //Tạo kết nối
//            mainControlView.getDisableTouch();
            modelNetwork.createConnectControl();
            //Gửi uuid cho Server kiểm tra xem port Control có cùng máy với port Login hay k
            modelNetwork.sendDataTCP(mainControlView.getUUIDFromSharedPref());
            mainControlView.startWebView(modelNetwork.getIPAddress());
            if(modelNetwork.getSocket() != null) {
                modelNetwork.readDataUDP();
            }
            mainControlView.changeColorImgConnecting();
//            mainView.setTextView("On");
            modelNetwork.checkConnect();
//            mainControlView.getEnableTouch();

        } else if(modelNetwork.getStateConnecting() == true){
            //Ngắt kết nối
//            mainControlView.getDisableTouch();
            modelNetwork.disconnectControl();
            mainControlView.stopWebView();
            mainControlView.changeColorImgDisconnect();
//            mainControlView.getEnableTouch();
        }
    }

    @Override
    public void sendData(String messenge) {
        modelNetwork.sendDataTCP(messenge);
    }

    @Override
    public void updateStateConnecting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(modelNetwork.getStateConnecting() == true) {
                    mainControlView.changeColorImgConnecting();
////                    supView.setTextView("On");
                }
                else if(modelNetwork.getStateConnecting() == false){
                    mainControlView.changeColorImgDisconnect();
                    mainControlView.stopWebView();
//                  supView.setTextView("Off");
                }
            }
        });
    }

    @Override
    public void updateFrontWarning() {
        String front = modelNetwork.getMessFront();
        String[] splitFront = front.split("#");
        String frontSplited1 = splitFront[0];
        String frontSplited2 = splitFront[1];
        Log.v("SmartCar",frontSplited1 + frontSplited2);
        if (frontSplited2.equals("1")) {
            //Ẩn imgWarning
            mainControlView.goneWarningFront();
        }else if (frontSplited2.equals("0")){
            //Chớp imgWarning
            mainControlView.visibleWarningFront();
        }
    }

    @Override
    public void updateBehindWarning() {
        String behind = modelNetwork.getMessBehind();
        String[] splitBehind = behind.split("#");
        String behindSplited1 = splitBehind[0];
        String behindSplited2 = splitBehind[1];
        Log.v("SmartCar",behindSplited1 + behindSplited2);
        if (behindSplited2.equals("1")) {
            //Ẩn imgWarning
            mainControlView.goneWarningBehind();
        }else if (behindSplited2.equals("0")){
            //Chớp imgWarning
            mainControlView.visibleWarningBehind();
        }
    }

    @Override
    public void updateTemp() {

    }

    @Override
    public void updateHum() {

    }
}

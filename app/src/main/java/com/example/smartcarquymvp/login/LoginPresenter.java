package com.example.smartcarquymvp.login;

import android.util.Log;

import com.example.smartcarquymvp.NetworkInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

public class LoginPresenter implements ContractLogin.LoginPresenter{

    String ipAddress;

    ContractLogin.LoginView mainLoginView;
    NetworkInterface.Network modelNetwork;

    public LoginPresenter(ContractLogin.LoginView mainloginView, NetworkInterface.Network modelNetwork, SSLSocketFactory socketFactory) {
        this.mainLoginView = mainloginView;
        this.modelNetwork = modelNetwork;
        modelNetwork.setSocketFactory(socketFactory);
    }

    @Override
    public void sendAccountLogin(){
        this.ipAddress = mainLoginView.getIPAddressServer();
        if(ipAddress != null){
            if(checkFormIPAddress(ipAddress)){
                Log.v("SmartCar", "Valid ip address");
                modelNetwork.setIPAddress(this.ipAddress);
                modelNetwork.createConnect();
                modelNetwork.sendDataTCP("login#"+mainLoginView.getUUID() + "#" + mainLoginView.getUsername()+ "#" + mainLoginView.getPassword());
                modelNetwork.readDataTCP();
//              modelNetwork.killReadDataTCP(10000);
                String mesrecv = modelNetwork.getMesLogin();
                Log.v("SmartCar", "..........." + mesrecv);
                if (mesrecv != null) {
                    if (mesrecv.equals("LoginSuccess")) {
                        //Nếu Login thành công sẽ lưu lại username và chuyển sang Activity Control
                        mainLoginView.saveUsernamePreference();
                        modelNetwork.setStateConnecting(Boolean.FALSE);
                        mainLoginView.changeToControlActivity();
                    } else if (mesrecv.equals("LoginFailed")) {
                        mainLoginView.getToast("Tài khoản hoặc mật khẩu không chính xác");
                        modelNetwork.setSocketTCPTSLNull();
                    }
                }
            }
        }else {
            Log.v("SmartCar","Invalid ip address");
        }
//        modelNetwork.disconnect();
    }

    @Override
    public void autoLogin(){
        this.ipAddress = "192.168.1.175";
        modelNetwork.setIPAddress(this.ipAddress);
        modelNetwork.createConnect();
        if(modelNetwork.getSocket() != null) {
            modelNetwork.sendDataTCP("autologin#" + mainLoginView.getUUID() + "#" + mainLoginView.getUsername());
            modelNetwork.readDataTCP();
            String mesrecv = modelNetwork.getMesLogin();
            Log.v("SmartCar", "..........." + mesrecv);
            if (mesrecv != null) {
                if (mesrecv.equals("LoginSuccess")) {
                    //Nếu Login thành công sẽ lưu lại username và chuyển sang Activity Control
                    mainLoginView.changeToControlActivity();
//                  modelNetwork.checkConnect();
                } else if (mesrecv.equals("LoginFailed")) {
//                    modelNetwork.sendDataTCP(mainLoginView.getUUID() + "#" + mainLoginView.getUsername() + "#" + mainLoginView.getPassword());
//                    modelNetwork.readDataTCP();
//                    mesrecv = modelNetwork.getMesRecvFromServer();
//                    if (!mesrecv.equals("LoginFailed")) {
//                        //Nếu Login thành công sẽ chuyển sang Activity Control
//                        mainLoginView.changeToControlActivity();
                    mainLoginView.getToast("Auto Login failed\nPlease login manual");
                    modelNetwork.setSocketTCPTSLNull();
//                    }
                }
            }
        }
//        modelNetwork.disconnect();
    }

    private boolean checkFormIPAddress(String IPv4) {
        //CHeck IP Address
        String regexForm = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        Pattern p=Pattern.compile(regexForm,Pattern.CASE_INSENSITIVE);
        Matcher m=p.matcher(IPv4);
        return m.matches();
    }
}

package com.example.smartcarquymvp.login;

import javax.net.ssl.SSLSocketFactory;

public interface ContractLogin {

    interface LoginView{
        //Lấy địa chỉ ip của Server
        String getIPAddressServer();

        //Lấy uuid
        String getUUID();

        String getUsername();

        void setPassword(String password);
        String getPassword();

        //Save username if login success
        void saveUsernamePreference();

        void changeToControlActivity();

        void getToast(String notification);

//        SSLSocketFactory getSocketFactory();
    }

    interface LoginPresenter{
        void sendAccountLogin();

        void autoLogin();

    }
}

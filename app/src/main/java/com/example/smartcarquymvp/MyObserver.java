package com.example.smartcarquymvp;

public interface MyObserver{
    interface ObserverNetworkModel {
        void updateStateConnecting();
        void updateFrontWarning();
        void updateBehindWarning();
        void updateTemp();
        void updateHum();
    }

    interface ObserverDisconnect{
        void updateDisconnect();
    }

    interface ObserverConnect{
        void updateConnect();
    }
}

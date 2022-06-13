package com.example.smartcarquymvp;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public interface NetworkInterface {
    interface Network {
        //Tạo socketTSL
        void setSocketTCPTSLNull();


        //Set SocketFactory
        void setSocketFactory(SSLSocketFactory socketFactory);

        //Lấy thông tin socket
        SSLSocket getSocket();

        //Get socket state
        boolean getSocketState();

        //Set and get ip
        void setIPAddress(String ipAddress);
        String getIPAddress();

        //Lấy tin nhắn từ server
        String getMesLogin();
        String getMessUDPControl();
        String getMessFront();
        String getMessBehind();
        String getMessTemp();
        String getMessHum();


        //Lấy biến trạng thái stateConnecting từ model Network
        void setStateConnecting(boolean stateConnecting);
        boolean getStateConnecting();

        //Tạo kết nối với server
        void createConnect();

        //Ngắt kết nối với server
        void disconnect();
        void disconnectControl();

        void createConnectControl();

        //Gửi dữ liệu
        void sendDataTCP(String messenge);

        //Đọc dữ liệu
        void readDataUDP();
        void readDataTCP();

        //Kill threadReadData nếu Server k trả dữ liệu
        void killReadDataTCP(int timeKillThreadReadDataTCP);

        //Luôn gửi đến server "text"/giây để kiểm tra trạng thái kết nối đến server, sử dụng port của hàm createConnect()
        void checkConnect();

        //Lấy portVideo từ Client
        int getPortVideo();

        //Tạo thông báo khi có sự kiện thay đổi biến stateConnecting
        void attachObserverStateConnecting(MyObserver.ObserverNetworkModel obs);
        void attachObserverMessFromServer(MyObserver.ObserverNetworkModel obs);
    }
}

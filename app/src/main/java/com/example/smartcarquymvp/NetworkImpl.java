package com.example.smartcarquymvp;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class NetworkImpl implements NetworkInterface.Network, Serializable {

    private String messLogin;
    private String messUDPControl;
    private String messFront, messBehind, messTemp, messHum;
    private static String ipAddress;
    private static final int portVideo = 8000;
    private static final int portLogin = 8001;
    private static final int portSendData = 8002;
    private static final int portReadUDP = 8003;

    private static boolean stateConnecting = false;

    private static SSLSocketFactory socketFactory;
    private static SSLSocket socketTCPTSL;
    private static OutputStream outputStream;
    private static InputStream inputStream;
    private static DataOutputStream dataStreamOut;
//    private DataInputStream dataStreamIn;
    private static BufferedReader dataStreamIn;
    private static DatagramSocket socketUDPReader;
    private DatagramPacket packet;

    Thread threadConnect, threadProcessDataUDP, threadDisconnect, threadDisconnectControl, threadSendDataTCP, threadReadDataTCP,
            threadReadDataUDP, threadCheckConnect, threadKillReadDataTCP;

    private List<MyObserver.ObserverNetworkModel> stateConnectingObservers = new ArrayList<>();
    private List<MyObserver.ObserverNetworkModel> messFromServerObservers = new ArrayList<>();

    public NetworkImpl() {
        //Nothing
    }

    private void setMessLogin(String messLogin){
            this.messLogin = messLogin;
    }

    @Override
    public String getMesLogin() {
        return messLogin;
    }

    @Override
    public String getMessFront() {
        return messFront;
    }

    private void setMessFront(String messFront) {
        if(this.messFront != messFront) {
            this.messFront = messFront;
            //thông báo cho Observer
            notifyFrontWarning();
        }
    }

    @Override
    public String getMessBehind() {
        return messBehind;
    }

    private void setMessBehind(String messBehind) {
        if(this.messBehind != messBehind) {
            this.messBehind = messBehind;
            //thông báo cho Observer
            notifyBehindWarning();
        }
    }

    @Override
    public String getMessTemp() {
        return messTemp;
    }

    private void setMessTemp(String messTemp) {
        this.messTemp = messTemp;
        //thông báo cho Observer
        notifyTemp();
    }

    @Override
    public String getMessHum() {
        return messHum;
    }

    private void setMessHum(String messHum) {
        this.messHum = messHum;
        //thông báo cho Observer
        notifyHum();
    }

    private void setMessUDPControl(String messUDPControl){
        if(messUDPControl != null) {
            if(messUDPControl.contains("FRONT")){
                messFront = messUDPControl;
            }
            else if(messUDPControl.contains("BEHIND")){
                messBehind = messUDPControl;
            }
            else if(messUDPControl.contains("TEMP")){
                messTemp = messUDPControl;
            }
            else if(messUDPControl.contains("HUM")){
                messHum = messUDPControl;
            }
            this.notifyFrontWarning();
        }
    }

    @Override
    public String getMessUDPControl(){
        return messUDPControl;
    }

    @Override
    public void setStateConnecting(boolean stateConnecting) {
        if(this.stateConnecting != stateConnecting) {
            this.stateConnecting = stateConnecting;
            this.notifyStateConnectingAllObserver();
            Log.v("SmartCar","Connecting state: "+this.stateConnecting);
        }
    }

    @Override
    public void setSocketFactory(SSLSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    @Override
    public void setSocketTCPTSLNull() {
        this.socketTCPTSL = null;
    }

    @Override
    public SSLSocket getSocket() {
        return socketTCPTSL;
    }

    @Override
    public boolean getSocketState() {
        return socketTCPTSL.isClosed();
    }

    @Override
    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public int getPortVideo() {
        return portVideo;
    }

    @Override
    public String getIPAddress() {
        return ipAddress;
    }

    @Override
    public boolean getStateConnecting() {
        return this.stateConnecting;
    }

    @Override
    public void createConnect() {
        //Create SenderTCP stream
        threadConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Create connect TCP with timeout 5s
                    InetSocketAddress inetAddress = new InetSocketAddress(ipAddress, portLogin);
                    socketTCPTSL = (SSLSocket)socketFactory.createSocket();
                    socketTCPTSL.connect(inetAddress,5000);
                    socketTCPTSL.setSoTimeout(5000);

                    if(socketTCPTSL != null){
                        Log.v("SmartCar","Socket connected to server");
                        outputStream = socketTCPTSL.getOutputStream();
                        dataStreamOut = new DataOutputStream(outputStream);

                        inputStream = socketTCPTSL.getInputStream();
                        dataStreamIn = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8),100);
                    }
                } catch(SocketTimeoutException ex) {
                    Log.v("SmartCar","Socket State: Can't connect to server");
                    socketTCPTSL = null;
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadConnect.setName("Thread Connect");
        threadConnect.start();
        try {
            threadConnect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createConnectControl() {
        //Create SenderTCP stream
        threadConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Create connect TCP with timeout 5s
                    InetSocketAddress inetAddress = new InetSocketAddress(ipAddress, portSendData);
                    socketTCPTSL = (SSLSocket)socketFactory.createSocket();
                    socketTCPTSL.connect(inetAddress,5000);
                    socketTCPTSL.setSoTimeout(5000);

                    if(socketTCPTSL != null){
                        Log.v("SmartCar","Socket state: Connected to server");
                        outputStream = socketTCPTSL.getOutputStream();
                        dataStreamOut = new DataOutputStream(outputStream);

                        inputStream = socketTCPTSL.getInputStream();
                        dataStreamIn = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8),100);

                        //Create connect UDP
                        socketUDPReader = new DatagramSocket();
                        InetAddress address = InetAddress.getByName(ipAddress);
                        byte[] bufferSend = new byte[1];
                        packet = new DatagramPacket(bufferSend, bufferSend.length, address, portReadUDP);
                        socketUDPReader.send(packet);

                        setStateConnecting(true);
                    }
                } catch(SocketException ex) {
                    Log.v("SmartCar","Socket state: Can't connect to server");
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadConnect.setName("Thread Connect");
        threadConnect.start();
        try {
            threadConnect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void createConnectUDP() {
//        threadControlConnect = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //Create connect UDP
//                    socketUDPReader = new DatagramSocket();
//                    InetAddress address = InetAddress.getByName(ipAddress);
//                    byte[] bufferSend = new byte[1];
//                    packet = new DatagramPacket(bufferSend, bufferSend.length, address, portReadUDP);
//                    socketUDPReader.send(packet);
//
//                    byte[] bufferRead = new byte[20];
//                    //Tạo packet để nhận messenge từ server
//                    packet = new DatagramPacket(bufferRead, bufferRead.length);
//
//                    socketUDPReader.receive(packet);
//                    //Mở packet từ server
//                    String recvMsg = new String(packet.getData(), 0, packet.getLength());
////                    System.out.println(recvMsg);
//                    if (recvMsg.equals("connected") || recvMsg.equals("0") || recvMsg.equals("1")) {
//                        setStateConnecting(true);
//                    }
//                } catch(SocketException ex) {
//                    System.out.println("I/O error: " + ex.getMessage());
//                } catch(IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        threadControlConnect.setName("Thread ControlConnect");
//        threadControlConnect.start();
//        try {
//            threadControlConnect.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void disconnect() {
        threadDisconnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Close socket and data stream
//                    String messenge = "end";
//                    dataStreamOut.writeUTF(messenge);
//                    sendDataTCP(messenge);
                    Thread.sleep(100);
                    dataStreamOut.close();
                    outputStream.close();
                    socketTCPTSL.close();
                    socketUDPReader.close();
                    setStateConnecting(false);

                    //Ngắt thread disconnect đang chạy
                    Thread.currentThread();
                    if(!Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
//                    setStateConnecting(false);
                }
            }
        });
        threadDisconnect.setName("Thread Disconnect");
        threadDisconnect.start();
        try {
            threadDisconnect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnectControl() {
        threadDisconnectControl = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Close socket and data stream
                    String messenge = "end";
//                    dataStreamOut.writeUTF(messenge);
                    sendDataTCP(messenge);
                    Thread.sleep(100);

                    dataStreamOut.close();
                    outputStream.close();
                    dataStreamIn.close();
                    inputStream.close();
                    socketTCPTSL.close();
                    socketUDPReader.close();

                    setStateConnecting(false);
                    //Ngắt thread disconnect đang chạy
                    Thread.currentThread();
                    if(!Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
//                    setStateConnecting(false);
                }
            }
        });
        threadDisconnectControl.setName("Thread Disconnect Control");
        threadDisconnectControl.start();
        try {
            threadDisconnectControl.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDataTCP(String messenge) {
        threadSendDataTCP = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataStreamOut.writeUTF(messenge);
                    System.out.println(messenge);

                }catch (IOException e) {
                    e.printStackTrace();
                    setStateConnecting(false);
                }
            }
        });
        threadSendDataTCP.setName("Thread SendData");
        threadSendDataTCP.start();
    }

    @Override
    public void readDataTCP() {
        threadReadDataTCP = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true){
                        String messRecv = dataStreamIn.readLine();
                        if(messRecv != null){
                            setMessLogin(messRecv);
                            Log.v("SmartCar","Messenge TCP from server: "+messRecv);
                            break;
                        }
                    }
                    Thread.currentThread();
                    if(!Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadReadDataTCP.setName("Thread ReadData TCP");
        threadReadDataTCP.start();
        try {
            threadReadDataTCP.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readDataUDP() {
        threadReadDataUDP = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] bufferRead = new byte[20];
                        //Tạo packet để nhận messenge từ server
                        packet = new DatagramPacket(bufferRead, bufferRead.length);
                        socketUDPReader.receive(packet);
                        //Mở packet từ server
                        String recvMsg = new String(packet.getData(), 0, packet.getLength()); //dù tin nhắn rỗng nhưng biến String không bao giờ null
//                        setMessUDPControl(recvMsg);
                        threadProcessDataUDP = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(recvMsg.contains("FRONT")){
                                    setMessFront(recvMsg);
                                }
                                else if(recvMsg.contains("BEHIND")){
                                    setMessBehind(recvMsg);
                                }
                                else if(recvMsg.contains("TEMP")){
                                    setMessTemp(recvMsg);
                                }
                                else if(recvMsg.contains("HUM")){
                                    setMessHum(recvMsg);
                                }
                                Log.v("SmartCar","Messenge UDP from server: "+recvMsg);
                            }
                        });
                        threadProcessDataUDP.setName("Thread Process Data UDP");
                        threadProcessDataUDP.start();
                        if(recvMsg.equals("connected") || recvMsg.equals("0") || recvMsg.equals("1")){
                            setStateConnecting(true);
                        }
                        else if(recvMsg.equals("end") || recvMsg.equals("")){
                            setStateConnecting(false);
                            break;
                        }
                    }
                    socketUDPReader.close();
                    //Ngắt Thread readData
                    Thread.currentThread();
                    if(!Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadReadDataUDP.setName("Thread ReadDataUDP");
        threadReadDataUDP.start();
    }

    @Override
    public void checkConnect() {
        threadCheckConnect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (stateConnecting == true) {
                        Thread.currentThread();
                        Thread.sleep(2000);
                        sendDataTCP("0");
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                    setStateConnecting(false);
//                    Thread.currentThread().interrupt();
                }
            }
        });
        threadCheckConnect.setName("Thread CheckConnect");
        threadCheckConnect.start();
    }

    @Override
    public void killReadDataTCP(int timeKillThreadReadDataTCP){
        threadKillReadDataTCP = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(timeKillThreadReadDataTCP);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
//                if(!threadReadDataTCP.isInterrupted()) {
                threadReadDataTCP.interrupt();
//                }
            }
        });
        threadKillReadDataTCP.start();
    }

    @Override
    public void attachObserverStateConnecting(MyObserver.ObserverNetworkModel obs) {
        if (!stateConnectingObservers.contains(obs)) {
            stateConnectingObservers.add(obs);
        }
    }

    private void notifyStateConnectingAllObserver() {
        for (MyObserver.ObserverNetworkModel obs : stateConnectingObservers) {
            obs.updateStateConnecting();
        }
    }

    @Override
    public void attachObserverMessFromServer(MyObserver.ObserverNetworkModel obs) {
        if (!messFromServerObservers.contains(obs)) {
            messFromServerObservers.add(obs);
        }
    }

    private void notifyFrontWarning(){
        for (MyObserver.ObserverNetworkModel obs : messFromServerObservers) {
            obs.updateFrontWarning();
        }
    }

    private void notifyBehindWarning(){
        for (MyObserver.ObserverNetworkModel obs : messFromServerObservers) {
            obs.updateBehindWarning();
        }
    }

    private void notifyTemp(){
        for (MyObserver.ObserverNetworkModel obs : messFromServerObservers) {
            obs.updateTemp();
        }
    }

    private void notifyHum(){
        for (MyObserver.ObserverNetworkModel obs : messFromServerObservers) {
            obs.updateHum();
        }
    }
}

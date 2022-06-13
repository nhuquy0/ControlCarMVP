//package com.example.smartcarquymvp;
//
//import android.os.Handler;
//import android.util.Log;
//
//import com.example.smartcarquymvp.control.ContractControl;
//
//public class ObserverImpl implements MyObserver.ObserverNetworkModel {
//
//    ContractControl.ControlView supControlView;
//
//    NetworkInterface.Network modelNetwork = new NetworkImpl();
//    Handler handler = new Handler();
//
//
//    public ObserverImpl(ContractControl.ControlView supControlView) {
//        this.supControlView = supControlView;
//    }
//
//    @Override
//    public void updateStateConnecting() {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if(modelNetwork.getStateConnecting() == true) {
//                    supControlView.changeColorImgConnecting();
//////                    supView.setTextView("On");
//                }
//                else if(modelNetwork.getStateConnecting() == false){
//                    supControlView.changeColorImgDisconnect();
//                    supControlView.stopWebView();
////                  supView.setTextView("Off");
//                }
//            }
//        });
//    }
//
//    @Override
//    public void updateMesRecvFromServer() {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String mesRecvFromServer = modelNetwork.getMessUDPControl();
//                    String[] sliptMesRecvFromServer = mesRecvFromServer.split("#");
//                    String messSplited1 = sliptMesRecvFromServer[0];
//                    String messSplited2 = sliptMesRecvFromServer[1];
//                    Log.v("SmartCar",messSplited1 + messSplited2);
//                    if (messSplited1.equals("FRONT")) {
//                        if (messSplited2.equals("1")) {
//                            //Ẩn imgWarning
//                            supControlView.goneWarningFront();
//                        }else if (messSplited2.equals("0")){
//                            //Chớp imgWarning
//                            supControlView.visibleWarningFront();
//                            Thread.sleep(500);
////                            supControlView.goneWarningFront();
////                            Thread.sleep(500);
//                        }
//                    } else if (messSplited1.equals("BEHIND")) {
//                        if (messSplited2.equals("1")) {
//                            //Ẩn imgWarning
//                            supControlView.goneWarningBehind();
//                        } else if (messSplited2.equals("0")){
//                            //Chớp imgWarning
//                            supControlView.visibleWarningBehind();
//                            Thread.sleep(500);
////                            supControlView.goneWarningBehind();
////                            Thread.sleep(500);
//                        }
//                    }
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//}

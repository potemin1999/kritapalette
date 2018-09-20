package com.ilya.kritapalette.net;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class KritaClient {

    public Thread clientThread;
    boolean awaitConnect = false;
    boolean awaitDisconnect = false;
    boolean connected = false;
    byte[] toWrite;
    Socket clientSocket;
    String ip;
    int port;

    public KritaClient(){
        clientThread = new Thread(this::threadRun);
        clientThread.start();
    }

    public void connect(String ip,int port){
        this.ip = ip;
        this.port = port;
        awaitConnect = true;
        synchronized (clientThread) {
            clientThread.notify();
        }
    }

    public void send(byte[] data){
        toWrite = data;
        synchronized (clientThread) {
            clientThread.notify();
        }
    }

    public void disconnect(){
        awaitDisconnect = true;
        synchronized (clientThread) {
            clientThread.notify();
        }
    }


    private void threadRun(){
        try{
            synchronized (clientThread) {
                while (!awaitConnect) {
                    clientThread.wait();
                }
                clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(ip,port));
                OutputStream os = clientSocket.getOutputStream();
                while (!awaitDisconnect) {
                    clientThread.wait();
                    byte[] data = toWrite;
                    if (data == null) continue;
                    System.out.println("sending: " + Arrays.toString(data));
                    os.write(data);
                }
                clientSocket.close();
            }
        }catch(Throwable t){}
    }

}

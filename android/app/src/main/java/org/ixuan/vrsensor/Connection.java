package org.ixuan.vrsensor;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import org.ixuan.vrsensor.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ProtocolException;

public class Connection implements Runnable {
    public static Connection connection = null;

    public static final int MESSAGE_START = 521;
    public static final int MESSAGE_IOEXCEPTION = 940;
    public static final int MESSAGE_FAIL = 529;
    public static final int MESSAGE_SUCCESS = 612;
    public static final int MESSAGE_DISCONNECTED = 821;

    private Thread receiverThread;
    private Thread senderThread;
    private Looper senderLooper;
    private Handler senderHandler;

    private Handler handler;
    private String host;
    private int port;

    private byte[] identify;
    private DatagramSocket socket;


    public Connection(Handler handler, String host, int port) throws IllegalAccessException {
        this.handler = handler;
        this.host = host;
        this.port = port;
        if (connection != null) {
            throw new IllegalAccessException("dup con");
        }
        connection = this;
        receiverThread = new Thread(this);
        senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                senderLooper = Looper.myLooper();
                senderHandler = new Handler();
                Looper.loop();
            }
        });
        receiverThread.start();
        senderThread.start();
    }

    private void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, new InetSocketAddress(host, port));
        socket.send(packet);
    }

    private void sendDirectly(byte[] data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(identify.length + data.length);
            baos.write(identify);
            baos.write(data);
            send(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Message.obtain(handler, MESSAGE_DISCONNECTED).sendToTarget();
            close();
        }
    }

    public void send(Packet packet) throws IOException {
        //TODO Add .identify & .socket check

        if (Thread.currentThread() == senderThread) {
            sendDirectly(packet.toBinary());
        } else {
            final byte[] data = packet.toBinary();
            senderHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendDirectly(data);
                }
            });
        }
    }

    public void close() {
        if (socket == null) {
            return;
        }
        socket.disconnect();
        socket.close();

        receiverThread.interrupt();
        senderLooper.quit();
        senderThread.interrupt();
        connection = null;
    }

    /**
     * initial connection and register device to server
     *
     * @return true if hello successful
     */
    private boolean hello() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer;
        try {
            //initial Socket
            socket = new DatagramSocket(new InetSocketAddress(0));

            //construct "hello" packet
            baos.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            baos.write(Build.MODEL.getBytes("UTF-8"));
            baos.write(0);
            buffer = baos.toByteArray();

            //Send hello message
            Message.obtain(handler, MESSAGE_START).sendToTarget();
            send(buffer);

            //wait for response
            buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(1000);
            socket.receive(packet);

            //Parse response
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            buffer = new byte[3];
            bais.read(buffer);
            if (buffer[0] != 79 || buffer[1] != 75 || buffer[2] != 0) {
                throw new ProtocolException("Protocol Error");
            }
            identify = new byte[8];
            bais.read(identify);

            Log.d("VR", "Sensor Register OK");
            socket.setSoTimeout(0);
        } catch (ProtocolException e) {
            Message.obtain(handler, MESSAGE_FAIL).sendToTarget();
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Message.obtain(handler, MESSAGE_IOEXCEPTION).sendToTarget();
            e.printStackTrace();
            return false;
        }
        Message.obtain(handler, MESSAGE_SUCCESS).sendToTarget();
        return true;
    }

    @Override
    public void run() {
        try {
            if (!hello())
                return;
            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    socket.receive(packet);

                    if (packet.getLength() == 0)
                        break;
                } catch (IOException e) {
                    e.printStackTrace();
                    Message.obtain(handler, MESSAGE_DISCONNECTED).sendToTarget();
                    break;
                }
            }
        } finally {
            Message.obtain(handler, MESSAGE_DISCONNECTED).sendToTarget();
            close();
        }
    }

    public Handler getSenderHandler() {
        return senderHandler;
    }
}

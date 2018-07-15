package sample.sample_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Kazuki on 2018/07/02.
 */

public class BluetoothClientThread extends Thread {
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private final BluetoothAdapter mAdapter;
    ConnectedThread mThread;
    TextView mTxtRcv;

    // UUIDの生成
    //public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");   // SPP
    public static final UUID MY_UUID = UUID.fromString("00001101-1000-1000-8000-00805F9B34FB");

    public BluetoothClientThread(BluetoothDevice device, BluetoothAdapter adapter, TextView txtRcv){
        BluetoothSocket socket = null;
        mDevice = device;
        mAdapter = adapter;
        mTxtRcv = txtRcv;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try{
            // MY_UUID is the app's UUID string, alse used by the server code
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = socket;
    }

    public void run() {
        setName("LNT-BT01");

        // 接続要求を出す前に検索を中止する。これをしないと接続が遅くなる。結果、接続が失敗しやすくなる。
        mAdapter.cancelDiscovery();

        try {
            // socketを通してデバイスを接続。これは、接続成功 or 例外発生するまで処理が止まる。
            mSocket.connect();
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return;
        }

        // 接続したときの処理を入れる
        mThread = new ConnectedThread(mSocket, mTxtRcv);
        mThread.start();
    }

    public void write(byte[] bytes) {
        mThread.write(bytes);
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

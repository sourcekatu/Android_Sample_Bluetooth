package sample.sample_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by kaihatsuk on 2018/07/02.
 */

public class BluetoothServerThread extends Thread {
    // サーバー側の処理
    // UUID：Bluetoothプロファイルごとに決められた値
    private final BluetoothServerSocket mBluetoothServerSocket;

    // UUIDの生成
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothServerThread(BluetoothAdapter bluetoothAdapter)
    {
        BluetoothServerSocket bluetoothServerSocket = null;

        try{
            bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Sample_Bluetooth", MY_UUID);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mBluetoothServerSocket = bluetoothServerSocket;
    }

    public void run() {
        BluetoothSocket socket = null;

        // Keep listening until exception occurs or a socket is returned
        while (true){
            try {
                socket = mBluetoothServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                // thread処理を記述

                try {
                    mBluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // Will cancel the listening socket, and cause the thread to finish
    public void cancel() {
        try {
            mBluetoothServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

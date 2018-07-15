package sample.sample_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by kaihatsuk on 2018/07/02.
 */

public class BluetoothServerThread extends Thread {
    // サーバー側の処理
    // UUID：Bluetoothプロファイルごとに決められた値
    private final BluetoothServerSocket mBluetoothServerSocket;
    TextView mTxtRcv;
    ConnectedThread mThread;

    Handler handler = new Handler(Looper.getMainLooper());
    Context mContext;

    // UUIDの生成
    //public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");   // SPP
    public static final UUID MY_UUID = UUID.fromString("00001101-1000-1000-8000-00805F9B34FB");

    public BluetoothServerThread(BluetoothAdapter bluetoothAdapter, Context context, TextView txtRcv)
    {
        BluetoothServerSocket bluetoothServerSocket = null;
        mContext = context;
        mTxtRcv = txtRcv;

        try{
            // 名前は適当。
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Sample_Bluetooth", MY_UUID);
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
                // 接続要求を受信するか、例外が発生するまで処理は止まる
                socket = mBluetoothServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                // 接続したときの処理を入れる
                mThread = new ConnectedThread(socket, mTxtRcv);
                mThread.start();

                // Do work to manage the connection (in a separate thread)
                // thread処理を記述
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接続成功", Toast.LENGTH_LONG).show();
                    }
                });

                // Bluetoothの場合は、接続が完了すればサーバーはクローズしてもOK。
                try {
                    mBluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        mThread.write(bytes);
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
package sample.sample_bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Kazuki on 2018/07/02.
 */

public class ConnectedThread extends Thread {
    private final BluetoothSocket mSocket;
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    TextView mTxtRcv;
    Handler mHandler = new Handler(Looper.getMainLooper());
    byte[] mBuffer = new byte[1024];


    public ConnectedThread(BluetoothSocket socket, TextView txtRcv){
        mSocket = socket;
        mTxtRcv = txtRcv;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        // ストリームの取得
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    public void run(){
        int byteLength;

        while (true){
            try {
                byteLength = mInputStream.read(mBuffer);

                // UIスレッドに送信する処理
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTxtRcv.setText(new String(mBuffer));
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // メインスレッドからBluetoothデバイスに送信する処理
    public void write(byte[] bytes) {
        try {
            mOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // キャンセル処理
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

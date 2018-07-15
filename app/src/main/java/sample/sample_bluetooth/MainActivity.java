package sample.sample_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_ENABLE_BT = 1;        // インテントを受け取ったときのID。適当
    final int MY_DEVICE_DETECT_ENABLE = 2;  // インテントを受け取ったときのID。適当
    Button mbtnBluetoothEnable;         // Bluetooth有効
    Button mbtnBluetoothDisable;        // Bluetooth無効
    Button mbtnAskDevice;                // 過去に接続したことがある端末の問い合わせ
    Button mbtnSearchDevice;            // 端末を検索
    Button mbtnMyDeviceDetectEnable;    // 自分の端末を相手に見つけてもらうためにシグナル発信
    Button mbtnStartServer;             // サーバー
    Button mbtnStartClient;             // クライアント
    Button mbtnSend;
    EditText edtSend;
    TextView txtRcv;
    ListView mLstAsk;
    ListView mLstSerach;
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter mArrayAdapter;
    ArrayAdapter mArrayAdapter2;
    ArrayList<BluetoothDevice> mBluetoothDeviceAskList;
    ArrayList<BluetoothDevice> mBluetoothDeviceSearchList;
    BluetoothClientThread mClientThread;
    BluetoothServerThread mServerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 端末がBluetoothを搭載していることを確認
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        edtSend = findViewById(R.id.editText);
        txtRcv = findViewById(R.id.textView);

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        // Bluetooth有効
        mbtnBluetoothEnable = (Button) findViewById(R.id.button);
        mbtnBluetoothEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 端末のBluetoothを有効にする
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });

        // Bluetooth無効
        mbtnBluetoothDisable = (Button)findViewById(R.id.button2);
        mbtnBluetoothDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
            }
        });

        // 他の端末から自分の端末を300秒間検知可能にする
        mbtnMyDeviceDetectEnable = (Button)findViewById(R.id.button5);
        mbtnMyDeviceDetectEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(discoverableIntent, MY_DEVICE_DETECT_ENABLE);
            }
        });

        // 過去に接続したことのあるデバイスのリストを表示
        mbtnAskDevice = (Button) findViewById(R.id.button3);
        mLstAsk = (ListView) findViewById(R.id.ListView);
        mbtnAskDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (mBluetoothDeviceAskList != null){
                    mBluetoothDeviceAskList.clear();
                }
                mBluetoothDeviceAskList = new ArrayList<>();

                // If there are paired devices
                if (pairedDevices.size() > 0)
                {
                    for (BluetoothDevice device : pairedDevices){
                        mBluetoothDeviceAskList.add(device);

                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }

                // 見つかったデバイスををリスト表示
               mLstAsk.setAdapter(mArrayAdapter);
            }
        });

        // リストから選択したデバイスに対して接続要求
        mLstAsk.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // スレッドを破棄した後に新しくスレッド生成
                if (mClientThread != null) {
                    if (mClientThread.isAlive()) {
                        mClientThread.cancel();

                        while (mClientThread.isAlive()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                mClientThread = new BluetoothClientThread(mBluetoothDeviceAskList.get(position), mBluetoothAdapter, txtRcv);
                mClientThread.start();
            }
        });

        // 新しく検索して見つかったデバイスのリストを表示
        mbtnSearchDevice = (Button) findViewById(R.id.button4);
        mLstSerach = (ListView) findViewById(R.id.ListView2);
        mbtnSearchDevice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (mBluetoothDeviceSearchList != null){
                    mBluetoothDeviceSearchList.clear();
                }
                mBluetoothDeviceSearchList = new ArrayList<>();

                mArrayAdapter2 = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

                // 新しくデバイスを見つける時は、デバイスからブロードキャストが飛んでくるので、それを受けられるようにしておく
                // Register the BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                mBluetoothAdapter.startDiscovery();
            }
        });

        // 新しく見つかったデバイスのリストから選択したデバイスに対して接続要求
        mLstSerach.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // スレッドを破棄した後に新しくスレッド生成
                if (mClientThread != null) {
                    if (mClientThread.isAlive()) {
                        mClientThread.cancel();

                        while (mClientThread.isAlive()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                mClientThread = new BluetoothClientThread(mBluetoothDeviceSearchList.get(position), mBluetoothAdapter, txtRcv);
                mClientThread.start();
            }
        });

        // サーバースレッド起動
        mbtnStartServer = (Button) findViewById(R.id.button6);
        mbtnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mServerThread != null){
                    if (mServerThread.isAlive()){
                        mServerThread.cancel();
                    }

                    while (mServerThread.isAlive()){
                        try{
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                mServerThread = new BluetoothServerThread(mBluetoothAdapter, MainActivity.this, txtRcv);
                mServerThread.start();
            }
        });

        mbtnSend = (Button) findViewById(R.id.button8);
        mbtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mClientThread != null) {
                    //byte[] bytes = new byte[edtSend.length() + 2];
                    byte[] bytes = new byte[4 + 2];
                    byte[] ascii_Head = new byte[]{0x1B};
                    byte[] ascii_Terminator = new byte[]{0x0D};

                    String str = null;

                    //str = new String(ascii_Head, "US-ASCII") + String.valueOf(edtSend.getText()) + new String(ascii_Terminator, "US-ASCII") ;
                    str = new String(String.valueOf(edtSend.getText())) ;

                    bytes = str.getBytes();
                    mClientThread.write(bytes);
                }

                if (mServerThread != null){
                    byte[] bytes = new byte[edtSend.length() + 3];
                    String str = Integer.toHexString(0x1B) + String.valueOf(edtSend.getText()) + Integer.toHexString(0x0D);
                    bytes = str.getBytes();
                    mServerThread.write(bytes);
                }
            }
        });

//        // クライアントスレッド起動
//        mbtnStartClient = (Button) findViewById(R.id.button7);
//        mbtnStartClient.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                BluetoothClientThread clientThread = new BluetoothClientThread(mBluetoothDevice, mBluetoothAdapter);
//                clientThread.start();
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            // Bluetoothが有効になった。
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // デバイスが接続履歴に存在していない場合のみリストに格納
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mBluetoothDeviceSearchList.add(device);

                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter2.add(device.getName() + "\n" + device.getAddress());
                }
            }

            // 新しく見つかったデバイスのリスト作成
            mLstSerach.setAdapter(mArrayAdapter2);
        }
    };
}

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Set;

public class MainActivity extends AppCompatActivity{

    final int REQUEST_ENABLE_BT = 1;
    final int MY_DEVICE_DETECT_ENABLE = 2;
    Button mbtnBluetoothEnable;
    Button mbtnBluetoothDisable;
    Button mbtnAskDevice;
    Button mbtnSearchDevice;
    Button mbtnMyDeviceDetectEnable;
    ListView mListView;
    ListView mListView2;
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter mArrayAdapter;
    ArrayAdapter mArrayAdapter2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 端末がBluetoothを搭載していることを確認
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
        mListView = (ListView) findViewById(R.id.ListView);
        mbtnAskDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                // If there are paired devices
                if (pairedDevices.size() > 0)
                {
                    for (BluetoothDevice device : pairedDevices){
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }

                mListView.setAdapter(mArrayAdapter);
            }
        });

        // 新しく検索して見つかったデバイスのリストを表示
        mbtnSearchDevice = (Button) findViewById(R.id.button4);
        mListView2 = (ListView) findViewById(R.id.ListView2);
        mbtnSearchDevice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mArrayAdapter2 = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

                // Register the BroadcastReceiver
                //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                mBluetoothAdapter.startDiscovery();
            }
        });


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
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter2.add(device.getName() + "\n" + device.getAddress());
                }
            }

            mListView2.setAdapter(mArrayAdapter2);
        }
    };

}

package com.example.blunet;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class DevicesActivity extends AppCompatActivity {
    public String itemList[] = {};
    ArrayList<String> arrayList;
    ArrayList<BluetoothDevice> scanList = new ArrayList<BluetoothDevice>();
    ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    //BroadcastReceiver mBroadcastReceiver;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice store;
    public String item;
    public static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(DevicesActivity.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        pairedDevices();
    }
    protected void onResume()
    {
        super.onResume();
        scanList = new ArrayList<BluetoothDevice>();
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BT);
        }
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_BT);
        }
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_BT);
        }
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_BT);
        }
        /*bluetoothAdapter.startDiscovery();
        mBroadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                try
                {
                    String action = intent.getAction();
                    if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if(!scanList.contains(device.getName()))
                        {
                            Log.i("BLUETOOTH DEVICES", device.getName());
                            scanList.add(device);
                        }
                    }
                    if(action.equals(bluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                    {
                        Log.i("Discovery Finished","Finished");
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(DevicesActivity.this,"Trouble Receiving",Toast.LENGTH_SHORT).show();
                    Log.i("Trouble Receiving","Now");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(bluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        try
        {
            registerReceiver(mBroadcastReceiver, intentFilter);
        }
        catch(Exception e)
        {
            Toast.makeText(DevicesActivity.this,"Trouble Receiving", Toast.LENGTH_SHORT).show();
        }*/
    }
    public void pairedDevices()
    {
        if (ContextCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
        }
        arrayList = new ArrayList<String>(Arrays.asList(itemList));
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {
            String deviceName = device.getName();
            Log.i("Guitars", deviceName);
            arrayList.add("         " + deviceName);
            deviceList.add(device);
        }
        itemList = arrayList.toArray(itemList);
        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(DevicesActivity.this,R.layout.white_list, itemList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = (String) parent.getItemAtPosition(position);
                for(int i = 0;;i++)
                {
                    if(itemList[i].equals(item))
                    {
                        store = deviceList.get(i);
                        break;
                    }
                }
                String[] str = item.split("         ");
                Toast.makeText(DevicesActivity.this, "Recipient Selected:" + str[1], Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DevicesActivity.this, MainActivity.class);
                intent.putExtra("item", str[1]);
                intent.putExtra("device", store);
                startActivity(intent);
            }
        });
    }
}
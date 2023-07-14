package com.example.blunet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static int counter = 0;
    public BluetoothSocket mmSocket;
    BluetoothSocket socket = null;
    public int flag = 0;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice receiver;
    UUID MY_UUID = UUID.fromString("f6823ab8-97a8-4299-9375-ece54a328f5a");
    private ImageButton helpButton, devicesButton, sendButton;
    public static String messageList[] = {};
    public static byte[] bytes = {};
    public EditText editText;
    private final int REQUEST_ENABLE_BT = 2;
    String recipient, hint;
    public static final int MESSAGE_READ = 0;
    ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Tooth not supported", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            try {
                bluetoothAdapter.enable();
            } catch (Exception e) {
                Log.i("Couldn't enable bluetooth!", e.toString());
            }
        }
        AcceptThread t1 = new AcceptThread();
        t1.start();
        ListView listView = (ListView) findViewById(R.id.messageList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.white_list, messageList);
        listView.setAdapter(arrayAdapter);
        editText = (EditText) findViewById(R.id.message);
        Intent intent = getIntent();
        recipient = intent.getStringExtra("item");
        receiver = intent.getParcelableExtra("device");
        if (recipient == null) {
            hint = " Select Recipient";
        } else {
            hint = " To : " + recipient;
            ConnectThread T1 = new ConnectThread(receiver);
            T1.start();
        }
        editText.setHint(hint);
        helpButton = (ImageButton) findViewById(R.id.help);
        devicesButton = (ImageButton) findViewById(R.id.devices);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpActivity();
            }
        });
        devicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDevicesActivity();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                DateTimeFormatter dtf;
                LocalDateTime now;
                if (recipient != null) {
                    if (!editText.getText().toString().isEmpty()) {
                        String c;
                        ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(messageList));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                            now = LocalDateTime.now();
                            c = "";
                            if (counter > 0)
                                c = "\n";
                            String message = c + "    Sent Message : " + editText.getText().toString() + "\n\n    To : " + recipient + "\n    Date/Time : " + dtf.format(now) + "\n";
                            arrayList.add(message);
                            bytes = editText.getText().toString().getBytes();
                            Log.i("How is is null", bytes.toString());
                            try {
                                connectedThread.writer(bytes);
                            } catch (Exception e) {
                                Log.i("Writer exception", e.toString());
                            }
                        }
                        messageList = arrayList.toArray(messageList);
                        ListView listView = (ListView) findViewById(R.id.messageList);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.white_list, messageList);
                        listView.setAdapter(arrayAdapter);
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                listView.setSelection(listView.getCount() - 1);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Please Enter Text", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please Select Recipient", Toast.LENGTH_SHORT).show();
                }
                editText.setText("");
            }
        });
    }

    public void openHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void openDevicesActivity() {
        Intent intent = new Intent(this, DevicesActivity.class);
        startActivity(intent);
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                }
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluNet", MY_UUID);
                Log.i("BluetoothServerSocket","Listening");
            }
            catch (IOException e)
            {
                Log.i("Socket's listen() method failed", e.toString());
            }
            mmServerSocket = tmp;
        }
        public void run()
        {
            while (true)
            {
                try
                {
                    Log.i("accept","blocking call started");
                    socket = mmServerSocket.accept();
                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                    }
                    String temp = socket.getRemoteDevice().getName();
                    Log.i("Connected","accepted");
                    recipient = temp;
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(MainActivity.this,"Connected to : " + temp,Toast.LENGTH_SHORT).show();
                            editText.setHint("To : " + temp);
                        }
                    });
                }
                catch (IOException e)
                {
                    Log.i("Socket's accept() method failed", e.toString());
                    break;
                }

                if (socket != null)
                {
                    try
                    {
                        mmServerSocket.close();
                    }
                    catch (IOException e)
                    {
                        Log.i("Problem closing","It is what it is");
                    }
                    break;
                }
            }
        }
    }
    public class ConnectThread extends Thread
    {
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try
            {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                }
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.i("Finding the Device","Yeah doing it");
            }
            catch (IOException e)
            {
                Log.i("Socket's create() method failed", e.toString());
            }
            mmSocket = tmp;
        }

        public void run()
        {

            try {
                Log.i("Blocking call","connect");
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                }
                mmSocket.connect();
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                        }
                        Toast.makeText(MainActivity.this,"Connected to : " + mmSocket.getRemoteDevice().getName(),Toast.LENGTH_SHORT).show();
                    }
                });
                connectedThread = new ConnectedThread(mmSocket);
                Log.i("Connected","Yeah you heard it right");
                connectedThread.start();
                flag = 1;
            }
            catch (IOException connectException)
            {
                try
                {
                    Log.i("mmSocket close","Client's End");
                    mmSocket.close();
                }
                catch (IOException closeException)
                {
                    Log.i("Could not close the client socket", closeException.toString());
                }
                return;
            }
        }
    }
    public class ConnectedThread extends Thread
    {
        public final InputStream mmInStream;
        public final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                Log.i("Inputstream got!","Yeah its been received");
            }
            catch (IOException e)
            {
                Log.i("Error occurred when creating input stream", e.toString());
            }
            try
            {
                tmpOut = socket.getOutputStream();
                Log.i("Outputstream got!","Yeah its been received");
            }
            catch (IOException e)
            {
                Log.i("Error occurred when creating output stream", e.toString());
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run()
        {
            int numBytes;
            while (true)
            {
                try
                {
                    byte[] mmBuffer;
                    mmBuffer = new byte[1024];
                    Log.i("Initiating Read","Yes doing it");
                    numBytes = mmInStream.read(mmBuffer);
                    Log.i("Initiating Read2","Yes doing it");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            DateTimeFormatter dtf = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                            }
                            LocalDateTime now = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                now = LocalDateTime.now();
                            }
                            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                            View view = inflater.inflate(R.layout.white_list, null);
                            TextView textView =view.findViewById(android.R.id.text1);
                            textView.setTextColor(Color.parseColor("#00FF00"));
                            ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(messageList));
                            String messageText = new String(mmBuffer);
                            Log.i("Message text",messageText);
                            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                            }
                            String message = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                message = "\n" + "    Received Message : " + messageText + "\n\n   from : " + mmSocket.getRemoteDevice().getName() + "\n   Date/Time : " + dtf.format(now) + "\n";
                            }
                            arrayList.add(message);
                            messageList = arrayList.toArray(messageList);
                            ListView listView = (ListView) findViewById(R.id.messageList);
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.white_list, messageList);
                            listView.setAdapter(arrayAdapter);
                            listView.setSelection(listView.getCount() - 1);
                            //textView.setTextColor(-1);

                        }
                    });
                }
                catch (IOException e)
                {
                    Log.i("Input stream was disconnected", e.toString());
                    break;
                }
            }
        }
        public void writer(byte[] bytes)
        {
            try
            {
                Log.i("y","y");
                mmOutStream.write(bytes);
                Log.i("Message written","Yeah!");
            }
            catch (IOException e)
            {
                Log.i("Error occurred when sending data", e.toString());
            }
        }
    }
}

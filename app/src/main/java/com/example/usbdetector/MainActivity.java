package com.example.usbdetector;

import androidx.appcompat.app.AppCompatActivity;


import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    Button btnCheck;
    TextView textInfo, test, testInter, testReceived;
    private static final String TAG = "MainActivity";
    UsbDevice device = null;
    UsbDeviceConnection connect;
    UsbConfiguration usbConfig;
    UsbInterface usbInter;
    UsbEndpoint endPoint1;
    String result = "";
    UsbManager manager;
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCheck = (Button) findViewById(R.id.check);
        textInfo = (TextView) findViewById(R.id.info);
        test = (TextView) findViewById(R.id.test);
        testInter = (TextView) findViewById(R.id.testInter);
        testReceived = findViewById(R.id.testReceived);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        final PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);


        btnCheck.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                checkInfo();
                CheckUsbDevice(permissionIntent);
            }
        });
    }
    /**
     * Check the device whether has USB-HOST feature.
     */
    public static boolean hasUsbHostFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
    }
    private void checkInfo() {

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        String i = "";
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            i += "\n" +
                    "DeviceID: " + device.getDeviceId() + "\n" +
                    "DeviceName: " + device.getDeviceName() + "\n" +
                    "DeviceClass: " + device.getDeviceClass() + " - "
                    + translateDeviceClass(device.getDeviceClass()) + "\n" +
                    "DeviceSubClass: " + device.getDeviceSubclass() + "\n" +
                    "VendorID: " + device.getVendorId() + "\n" +
                    "ProductID: " + device.getProductId() + "\n" +
                    "ProductName: " + device.getProductName() + "\n" +
                    "Serial#: " + device.getSerialNumber() + "\n";
        }

        textInfo.setText(i);
    }

    private void CheckInterfaceEndpoints(){
        // Cycle through interfaces and print out endpoint info
        String values = "";

        for (int i=0; i<device.getInterfaceCount(); i++)
        {
            String epDirString = "No endpoints";
            String epTypeString = "No endpoints";

            if (device.getInterface(i).getEndpointCount() > 0)
            {
                epDirString = String.valueOf(device.getInterface(i).getEndpoint(0).getDirection());
                epTypeString = String.valueOf(device.getInterface(i).getEndpoint(0).getType());
            }

            values += "Int. " + i + " EP count: " + device.getInterface(i).getEndpointCount() +
                    " || EP direction: " + epDirString + " || EP type: " + epTypeString + "\n";

            if (device.getInterface(i).getEndpointCount() == 2)
            {
                epDirString = String.valueOf(device.getInterface(i).getEndpoint(1).getDirection());
                epTypeString = String.valueOf(device.getInterface(i).getEndpoint(1).getType());

                values += "Int. " + i + " EP count: " + device.getInterface(i).getEndpointCount() +
                        " || EP direction: " + epDirString + " || EP type: " + epTypeString + "\n";
            }
        }

        testInter.setText(values);
    }

    private void CheckUsbDevice(PendingIntent permissionIntent){  //check connected device then ask for permission

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        Boolean found = false;
        String vendor = "";
        String product = "";
        result = "";


        while(deviceIterator.hasNext() && !found){      //check all connected devices and look for correct one
            device = deviceIterator.next();
            vendor = String.valueOf(device.getVendorId());
            product = String.valueOf(device.getProductId());
            if(vendor.equals("9334") && product.equals("4112")){
                found = true;
            }
        }
        if(found) {                                                            //if correct device connected set message
            result += "True: Vend " + vendor + "Pro " + product + "\n";
            test.setText(result);
            Log.d(TAG, "Correct Device ");
        }else {
            test.setText("Incorrect Device");
            Log.d(TAG, "Incorrect Device Vend " + vendor + " Pro " + product +"\n");
            return;
        }
        if(hasUsbHostFeature(this)){                                       //make sure phone can run device
            result += "USBHOSTFEATURE found \n";
            test.setText(result);
        }else {
            test.setText("Lacks Usb Host feature");
            Log.d(TAG, "Lacks Usb Host feature");
            return;
        }

        manager.requestPermission(device, permissionIntent);

    }

    private void AtteptComm(){
        connect = manager.openDevice(device);
        usbInter = device.getInterface(1);
        endPoint1 = usbInter.getEndpoint(1); //1
        if (usbInter == null) {
            result += "inter failed \n";
            test.setText(result);
        } else {
            result += "not null inter \n";
            test.setText(result);
        }
        if (endPoint1 == null) {
            result += "endpoint failed \n";
            test.setText(result);
        } else {
            result += "not null endpoint \n";
            test.setText(result);
        }

        CheckInterfaceEndpoints();

        byte bytes[] ;//= {0x50, 0x2c, (byte)0xCB, (byte)0xff};
        String x = ":0\n";
        bytes = x.getBytes();
        //byte Rbytes[] = {' '};

        int ks = bytes.length;


        int y= 0;
        Boolean k = connect.claimInterface(usbInter, true);
        y = connect.bulkTransfer(endPoint1, bytes, bytes.length, 500);
        Log.d(TAG, "Connection made data sent");
        /*try {
            Thread.sleep(50);
        }catch(Exception e){

        }*/

        UsbEndpoint endPoint2 = usbInter.getEndpoint(0); // 0


        byte buffer[] = {0};
        int xyz = connect.bulkTransfer(endPoint2, buffer, 27, 500);
        /*while(xyz > 0){
            testReceived.setText("final values " + String.valueOf(xyz));  //new String(Rbytes)
            xyz = connect.bulkTransfer(endPoint2, buffer, 57600, 500);
        }*/

        //y = connect.bulkTransfer(endPoint2, Rbytes, 10, 0);
        //testReceived.setText(String.valueOf(y));  //new String(Rbytes)
        //testReceived.set
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d(TAG, "permission Given for device " + device);
                        AtteptComm();

                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };



    private String translateDeviceClass(int deviceClass){
        switch(deviceClass){
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "Application specific USB class";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB class for audio devices";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB class for CDC devices (communications device class)";
            case UsbConstants.USB_CLASS_COMM:
                return "USB class for communication devices";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB class for content security devices";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB class for content smart card devices";
            case UsbConstants.USB_CLASS_HID:
                return "USB class for human interface devices (for example, mice and keyboards)";
            case UsbConstants.USB_CLASS_HUB:
                return "USB class for USB hubs";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB class for mass storage devices";
            case UsbConstants.USB_CLASS_MISC:
                return "USB class for wireless miscellaneous devices";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB class indicating that the class is determined on a per-interface basis";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB class for physical devices";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB class for printers";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB class for still image devices (digital cameras)";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "Vendor specific USB class";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB class for video devices";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB class for wireless controller devices";
            default: return "Unknown USB class!";

        }
    }



}



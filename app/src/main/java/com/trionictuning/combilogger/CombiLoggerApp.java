package com.trionictuning.combilogger;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.trionictuning.combilogger.kwp.KWPCANDevice;
import com.trionictuning.combilogger.kwp.KWPSession;
import com.trionictuning.combilogger.trionic.T7Binary;
import com.trionictuning.combilogger.usb.CombiAdapter;
import com.trionictuning.combilogger.usb.UsbConnectionReceiver;

public class CombiLoggerApp extends Application {
    private static final String TAG = CombiLoggerApp.class.getSimpleName();
    private UsbManager mUsbManager;
    private CombiAdapter mDevice;
    private static CombiLoggerApp gInstance;
    private KWPSession mKWPSession;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private T7Binary mBinary;

    public CombiLoggerApp() {
        super();
        gInstance = this;

        Log.d(TAG, "App started");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        UsbDevice device = findUsbDevice();

        if (device != null) {
            setUsbDevice(device);
        }
    }

    @Override
    public void onTerminate() {
        //stopConnection();
        super.onTerminate();
    }

    public void setUsbDevice(UsbDevice device) {
        if (device == null) {
            mDevice = null;
            Log.d(TAG,"USB device is null");
            return;
        }

        if (mUsbManager.hasPermission(device)) {
            Log.d(TAG,"Assigning device");
            mDevice = new CombiAdapter(device);
        } else {
            Log.d(TAG,"Asking for permission");
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                    this, 0, new Intent(UsbConnectionReceiver.ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(device, mPermissionIntent);
        }
    }

    private UsbDevice findUsbDevice() {
        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        for(UsbDevice device : deviceMap.values())  {
            if (CombiAdapter.checkDevice(device)) {
                return device;
            }
        }

        return null;
    }

    public boolean startKWPSession() {
        if (this.mDevice == null) {
            UsbDevice dev = findUsbDevice();
            if (dev == null) {
                Log.d(TAG,"Cannot start session: device is null");
                return false;
            }
            else
            {
                this.mDevice = new CombiAdapter(dev);
            }
        }

        KWPCANDevice kwpcanDevice = new KWPCANDevice(this.mDevice);
        this.mKWPSession = new KWPSession(kwpcanDevice);

        mExecutor.submit(this.mKWPSession);

        return true;
    }

    public void stopKWPSession() {
        this.mKWPSession.stop();
    }

    public UsbManager getUsbManager() {
        return mUsbManager;
    }

    public static CombiLoggerApp getInstance() {
        return gInstance;
    }

    public void loadBinary(String name) {
        mBinary = T7Binary.fromJSONFile(name.substring(0, 8).toLowerCase());
        mKWPSession.setBinary(mBinary);
    }


}

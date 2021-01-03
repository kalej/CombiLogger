package com.trionictuning.combilogger.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.trionictuning.combilogger.CombiLoggerApp;

public class UsbConnectionReceiver extends BroadcastReceiver {
    private static final String COMBIADAPTER_NAME = "CombiAdapter";
    public static final String ACTION_USB_PERMISSION = "com.trionictuning.combilogger.USB_PERMISSION";
    private static final String TAG = UsbConnectionReceiver.class.getSimpleName();

    CombiLoggerApp mApp;
    UsbManager mUsbManager;

    public UsbConnectionReceiver() {
        super();
        mApp = CombiLoggerApp.getInstance();
        mUsbManager = mApp.getUsbManager();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
        {
            Log.d(TAG, "Device attached");
            UsbDevice fromIntent = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mApp, 0, new Intent(ACTION_USB_PERMISSION), 0);
                mUsbManager.requestPermission(fromIntent, mPermissionIntent);

                return;
            }
            else
            {
                CombiLoggerApp.getInstance().setUsbDevice(fromIntent);
            }
        }
        else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
        {
            Log.d(TAG, "Device detached");
            CombiLoggerApp.getInstance().setUsbDevice(null);
        }
        else if (ACTION_USB_PERMISSION.equals(action)) {
            Log.d(TAG, "Usb permission dialog closed");
            synchronized (this) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.d(TAG, "Device from intent: " + device);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.d(TAG, "Setting usb device...");
                    CombiLoggerApp.getInstance().setUsbDevice(device);
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
            }
        }
    }
}
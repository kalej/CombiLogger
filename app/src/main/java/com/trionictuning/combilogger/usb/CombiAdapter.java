package com.trionictuning.combilogger.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.trionictuning.combilogger.CombiLoggerApp;
import com.trionictuning.combilogger.can.CANMessage;
import com.trionictuning.combilogger.can.ICANDevice;
import com.trionictuning.combilogger.can.ICANListener;
import com.trionictuning.combilogger.util.HexUtils;

public class CombiAdapter implements ICANDevice {
    private static final String TAG = CombiAdapter.class.getSimpleName();

    private static final int VID = 0xFFFF;
    private static final int PID = 0x0005;

    private static final byte EP_IN_ID = 0x2;
    private static final byte EP_OUT_ID = 0x5;

    private static final short TRANSFER_BLOCK_SIZE = 256;
    private static final int ADC_NUM_CHANNELS = 5;
    private static final int EXCHANGE_TIMEOUT = 3000;
    private static final byte[] OPEN_CAN = new byte[]{1};
    private static final byte[] CLOSE_CAN = new byte[]{0};
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final UsbDevice mDevice;
    private UsbInterface mInterface;
    private UsbEndpoint mOutEndpoint, mInEndpoint;
    private UsbDeviceConnection mConnection;
    private UsbConnectionThread mConnectionThread;
    private boolean isOpen;
    private Set<ICANListener> mCANListeners = Collections.synchronizedSet(new HashSet());;
    private final Queue<CombiUsbPacket> mUsbPacketQueue = new ArrayDeque<>();
    private final UsbConnectionThread.Listener mListener = new UsbConnectionThread.Listener() {
        @Override
        public void onNewUsbPacket(CombiUsbPacket packet) {
            if (packet.getCmdCode() != CombiUsbPacket.CMD_CAN_FRAME)
                mUsbPacketQueue.add(packet);
            else {
                CANMessage msg = CANMessage.fromBytes(packet.getCmdData());
                passCANMsgToListeners(msg);
            }
        }

        @Override
        public void onRunError(Exception e) {

        }
    };

    public CombiAdapter(UsbDevice device) {
        this.mDevice = device;
        this.isOpen = false;
    }

    public static boolean checkDevice(UsbDevice device) {
        if (device == null)
            return false;

        Log.d(TAG, String.format("Checking device %04X %04X", device.getVendorId(), device.getProductId()));

        return (device.getVendorId() == VID) && (device.getProductId() == PID);
    }

    public boolean open() {
        if (this.mDevice == null)
            return false;

        for (int intfIdx = 0; intfIdx < this.mDevice.getInterfaceCount(); intfIdx++) {
            UsbInterface usbInterface = this.mDevice.getInterface(intfIdx);
            if (usbInterface.getEndpointCount() == 2) {
                this.mInterface = usbInterface;
                break;
            }
        }

        if (this.mInterface == null)
            return false;

        for (int epIdx = 0; epIdx < this.mInterface.getEndpointCount(); epIdx++) {
            UsbEndpoint endpoint = this.mInterface.getEndpoint(epIdx);

            if (endpoint.getAddress() == EP_OUT_ID &&
                    endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                this.mOutEndpoint = endpoint;
            } else if (endpoint.getAddress() == (0x80 | EP_IN_ID)
                    && endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                this.mInEndpoint = endpoint;
            }
        }

        if (this.mInEndpoint == null || this.mOutEndpoint == null)
            return false;

        try {
            mConnection = CombiLoggerApp.getInstance().getUsbManager().openDevice(this.mDevice);
        } catch (Exception ex) {
            return false;
        }

        if (mConnection == null)
            return false;

        mConnection.claimInterface(this.mInterface, true);
        this.isOpen = true;

        mConnectionThread = new UsbConnectionThread(mInEndpoint, mOutEndpoint, mConnection, mListener);
        mExecutor.submit(mConnectionThread);

        setCANBitrate(500000);
        openCAN();
        return true;
    }

    public void close() {
        if (!this.isOpen)
            return;

        closeCAN();
        this.mConnectionThread.stop();
        while (!this.mConnectionThread.isStopped());
        this.mConnection.close();
        this.isOpen = false;
    }

    public void addListener(ICANListener listener) {
        Log.d(TAG,"Adding listener");
        mCANListeners.add(listener);
    }

    public void removeListener(ICANListener listener) {
        Log.d(TAG,"Removing listener");
        mCANListeners.remove(listener);
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void sendMessage(CANMessage msg) {
        Log.d(TAG, String.format("tx: %03X: %s", msg.getId(), HexUtils.hexlify(msg.getData())));
        CombiUsbPacket request = new CombiUsbPacket(CombiUsbPacket.CMD_CAN_TXFRAME, msg.toBytes());
        mConnectionThread.sendUsbPacket(request);
    }

    private void passCANMsgToListeners(CANMessage msg) {
        for (ICANListener listener : mCANListeners)
            if (listener.acceptsMessage(msg.getId())) {
                Log.d(TAG, String.format("rx: %03X: %s", msg.getId(), HexUtils.hexlify(msg.getData())));
                listener.handleMessage(msg);
            }
    }

    public void openCAN() {
        CombiUsbPacket request = new CombiUsbPacket(CombiUsbPacket.CMD_CAN_OPEN, OPEN_CAN);
        mConnectionThread.sendUsbPacket(request);
    }

    public void closeCAN() {
        CombiUsbPacket request = new CombiUsbPacket(CombiUsbPacket.CMD_CAN_OPEN, CLOSE_CAN);
        mConnectionThread.sendUsbPacket(request);
    }

    public void setCANBitrate(int bitrate) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(bitrate);
        byteBuffer.rewind();

        CombiUsbPacket request = new CombiUsbPacket(CombiUsbPacket.CMD_CAN_BITRATE, byteBuffer.array());
        mConnectionThread.sendUsbPacket(request);
    }
}

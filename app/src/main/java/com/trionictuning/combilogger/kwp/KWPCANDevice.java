package com.trionictuning.combilogger.kwp;

import java.util.LinkedList;
import java.util.Queue;

import com.trionictuning.combilogger.can.CANMessage;
import com.trionictuning.combilogger.can.ICANDevice;
import com.trionictuning.combilogger.can.ICANListener;
import com.trionictuning.combilogger.can.listeners.CANCollectingListener;
import com.trionictuning.combilogger.kwp.request.KWPStartCommunicationRequest;
import com.trionictuning.combilogger.kwp.response.KWPPositiveResponse;

public class KWPCANDevice implements IKWPDevice, ICANListener{
    private static final String TAG = KWPCANDevice.class.getSimpleName();

    private static CANMessage T7_INIT_CONNECTION_MESSAGE =
            new CANMessage(0x220, new byte[]{0x3F, (byte)0x81, 0x00, 0x11, 0x02, 0x40, 0x00, 0x00});
    private static int T7_INIT_REPLY_ID = 0x238;

    private ICANDevice mCANDevice;
    private CANCollectingListener mCollectingListener;
    private IKWPListener mKWPListener;

    public KWPCANDevice(ICANDevice canDevice) {
        this.mCANDevice = canDevice;
        this.mCANDevice.addListener(this);

        this.mCollectingListener = new CANCollectingListener(this.mCANDevice);
        this.mCANDevice.addListener(this.mCollectingListener);
        this.mCANDevice.addListener(this.mConfirmationListener);
    }

    @Override
    public void startSession() {
    }

    LinkedList<CANMessage> mPendingMessages = new LinkedList<>();
    @Override
    public void sendRequest(KWPRequest request) {
        if (request instanceof KWPStartCommunicationRequest) {
            mCANDevice.sendMessage(T7_INIT_CONNECTION_MESSAGE);
            return;
        }

        mPendingMessages.addAll(splitRequest(request));
        trySendMessage();
        //mCANDevice.sendMessage(mPendingMessages.poll());
    }

    private ICANListener mConfirmationListener = new ICANListener() {
        @Override
        public void handleMessage(CANMessage msg) {
            KWPCANDevice.this.trySendMessage();
        }

        @Override
        public boolean acceptsMessage(int id) {
            return id == 0x270;
        }
    };

    private void trySendMessage() {
        CANMessage msg = mPendingMessages.poll();

        if (msg == null)
            return;
        else {
            mCANDevice.sendMessage(msg);
        }
    }

    @Override
    public boolean open() {
        return mCANDevice.open();
    }

    @Override
    public boolean close() {
        this.mCANDevice.close();
        return true;
    }

    @Override
    public boolean isOpen() {
        return this.mCANDevice.isOpen();
    }

    @Override
    public void setListener(IKWPListener listener) {
        this.mKWPListener = listener;
        this.mCollectingListener.setKWPListener(listener);
    }

    private static Queue<CANMessage> splitRequest(KWPRequest request) {
        LinkedList<CANMessage> result = new LinkedList<>();
        byte[] data = request.toBytes();

        int msgCount = (data.length + 6 - 1) / 6;
        byte flag;

        for (int i = 0; i < msgCount; i++) {
            byte[] msgData = new byte[8];
            //msgData[0] = (byte)(flag | (msgCount - i - 1));
            flag = 0;

            if (i == 0)
                flag |= 0x40;

            if (i != msgCount - 1)
                flag |= 0x80;

            msgData[0] = (byte)(flag | (msgCount - i - 1));
            msgData[1] = (byte)0xA1;


            int start = 6 * i;
            int count = (data.length - start) < 6 ? (data.length - start) : 6;

            for (int j = 0; j < count; j++) {
                msgData[2 + j] = data[start + j];
            }

            CANMessage msg = new CANMessage(0x240, msgData);
            result.add(msg);
        }

        return result;
    }

    @Override
    public void handleMessage(CANMessage msg) {
        this.mKWPListener.handleResponse(
                new KWPPositiveResponse(KWPServiceId.StartCommunication, null)
        );
    }

    @Override
    public boolean acceptsMessage(int id) {
        return id == T7_INIT_REPLY_ID;
    }
}

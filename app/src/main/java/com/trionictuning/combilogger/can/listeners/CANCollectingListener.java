package com.trionictuning.combilogger.can.listeners;

import java.nio.ByteBuffer;

import com.trionictuning.combilogger.can.CANMessage;
import com.trionictuning.combilogger.can.ICANDevice;
import com.trionictuning.combilogger.can.ICANListener;
import com.trionictuning.combilogger.kwp.IKWPListener;
import com.trionictuning.combilogger.kwp.KWPResponse;

public class CANCollectingListener implements ICANListener {
    private static final String TAG = CANCollectingListener.class.getSimpleName();

    private ICANDevice mCANDevice;
    private IKWPListener mKWPListener;

    public CANCollectingListener(ICANDevice canDevice) {
        mCANDevice = canDevice;
    }

    public void setKWPListener(IKWPListener kwpListener) {
        this.mKWPListener = kwpListener;
    }

    /// Example of a KWP request asking for the VIN:
    ///
    /// 240h [40 A1 02 1A 90 00 00 00]
    /// 258h [C3 BF 13 5A 90 59 53 33] YS3
    /// 266h [40 A1 3F 83 00 00 00 00]
    /// 258h [82 BF 45 46 35 38 43 39] EF58C9
    /// 266h [40 A1 3F 82 00 00 00 00]
    /// 258h [81 BF 59 31 32 33 34 35] Y12345
    /// 266h [40 A1 3F 81 00 00 00 00]
    /// 258h [80 BF 36 37 00 00 00 00] 67
    /// 266h [40 A1 3F 80 00 00 00 00]
    ///
    /// First line: The KWP request is [02,1A,90] (length=2, Mode=1A, PID=90). The KWP request is
    /// wrapped in a CAN message with the header [40,A1] (40=row 0). The message is sent with
    /// CAN ID x240.
    ///
    /// Second line: The request results in a multi row reply and this is the first row.
    /// [13,5A,90] is the KWP reply header (13=length, 5A=request Mode+0x40, 90=PID]. [C3,BF] is the
    /// CAN header for the wrapped KWP reply (C3=first message, fourth row (3+1)). [59,53,33]
    /// is the start of the result (VIN).
    /// The message is sent from the ECU with CAN ID 0x258.
    ///
    /// Third line: Acknowledgement of the second line. [40,A1,3F,83] (3 is the row number).
    /// The message is sent with CAN ID 0x266.
    private ByteBuffer mCollectedResult;
    private int mExpectedMsgCount;

    @Override
    public void handleMessage(CANMessage msg) {
        byte[] data = msg.getData();
        int current;

        current = data[0] & 0x3F;
        if ((data[0] & 0x40) != 0) {
            if (mCollectedResult == null) {
                mCollectedResult = ByteBuffer.allocate(data[2] + 1);
                mExpectedMsgCount = current;
            }
        }

        int position = 6 * (mExpectedMsgCount - current);
        int remainingCapacity = mCollectedResult.capacity() - position;
        mCollectedResult.position(position);
        mCollectedResult.put(data, 2, remainingCapacity > 6 ? 6 : remainingCapacity);

        CANMessage confirmation = new CANMessage(0x266,
                new byte[]{0x40, (byte)0xA1, 0x3F, (byte)(data[0] & ~0x40), 0x00, 0x00, 0x00, 0x00});

        mCANDevice.sendMessage(confirmation);

        if (current == 0) {
            mCollectedResult.rewind();
            byte[] collected = mCollectedResult.array();

            this.mKWPListener.handleResponse(KWPResponse.fromBytes(collected));
            mCollectedResult = null;
        }
    }

    @Override
    public boolean acceptsMessage(int id) {
        return id == 0x258;
    }
}

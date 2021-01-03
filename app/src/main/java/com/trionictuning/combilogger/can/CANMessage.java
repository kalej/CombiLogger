package com.trionictuning.combilogger.can;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CANMessage {
    private static final String TAG = CANMessage.class.getSimpleName();

    private static final int ID_FIELD_LENGTH = 4;
    private static final int DATA_FIELD_LENGTH = 8;
    private static final int LENGTH_FIELD_LENGTH = 1;
    private static final int IS_EXT_FIELD_LENGTH = 1;
    private static final int IS_REM_FIELD_LENGTH = 1;

    private int id;
    private byte[] data;
    private boolean isExtended;
    private boolean isRemote;

    public CANMessage(int id, byte[] data, boolean isExtended, boolean isRemote) {
        this.id = id;
        this.data = data;
        this.isExtended = isExtended;
        this.isRemote = isRemote;
    }

    public CANMessage(int id, byte[] data) {
        this(id, data, false, false);
    }

    public CANMessage(int id) {
        this(id, new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, false, false);
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isExtended() {
        return isExtended;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(ID_FIELD_LENGTH + DATA_FIELD_LENGTH +
                LENGTH_FIELD_LENGTH + IS_EXT_FIELD_LENGTH + IS_REM_FIELD_LENGTH);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(id);
        byteBuffer.put(data);
        byteBuffer.put((byte)data.length);
        byteBuffer.put((byte)(isExtended?1:0));
        byteBuffer.put((byte)(isRemote?1:0));

        byteBuffer.rewind();
        return byteBuffer.array();
    }

    public static CANMessage fromBytes(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int id = byteBuffer.getInt();
        byte[] msgData = new byte[8];
        byteBuffer.get(msgData);
        int length = byteBuffer.get();
        boolean isExtended = byteBuffer.get()==1;
        boolean isRemote = byteBuffer.get()==1;

        return new CANMessage(id, msgData, isExtended, isRemote);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%03X", this.id));
        if (this.data != null) {
            sb.append(": ");
            for (int i = 0; i < this.data.length; i++)
                sb.append(String.format("%02X", this.data[i]));
        }
        return sb.toString();
    }
}

package com.trionictuning.combilogger.usb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CombiUsbPacket {
    public static final byte TERM_ACK = 0x0;
    public static final byte TERM_NACK = (byte)0xff;

    public static final byte CMD_BRD_FWVERSION = 0x20;
    public static final byte CMD_BRD_ADCFILTER = 0x21;
    public static final byte CMD_BRD_ADC = 0x22;
    public static final byte CMD_BRD_EGT = 0x23;

    public static final byte CMD_CAN_OPEN = (byte)0x80;
    public static final byte CMD_CAN_BITRATE = (byte)0x81;
    public static final byte CMD_CAN_FRAME = (byte)0x82;
    public static final byte CMD_CAN_TXFRAME = (byte)0x83;

    public static final byte CMD_CAN_ECUCONNECT = (byte)0x89;
    public static final byte CMD_CAN_READFLASH = (byte)0x8a;
    public static final byte CMD_CAN_WRITEFLASH = (byte)0x8b;

    private static final int CMD_CODE_FIELD_LENGTH = 1;
    private static final int COUNT_BYTES_FIELD_LENGTH = 2;
    private static final int ACK_FIELD_LENGTH = 1;
    public static final int MIN_PACKET_LENGTH = CMD_CODE_FIELD_LENGTH + COUNT_BYTES_FIELD_LENGTH
            + ACK_FIELD_LENGTH;

    private byte mCmdCode;
    private byte[] mCmdData;
    private byte mAck;

    public CombiUsbPacket(byte cmdCode) {
        this.mCmdCode = cmdCode;
        this.mCmdData = null;
        this.mAck = TERM_ACK;
    }

    public CombiUsbPacket(byte cmdCode, byte[] cmdData) {
        this.mCmdCode = cmdCode;
        this.mCmdData = cmdData;
        this.mAck = TERM_ACK;
    }

    private CombiUsbPacket(byte cmdCode, byte[] cmdData, byte ack) {
        this.mCmdCode = cmdCode;
        this.mCmdData = cmdData;
        this.mAck = ack;
    }

    public byte getCmdCode() {
        return mCmdCode;
    }

    public byte[] getCmdData() {
        return mCmdData;
    }

    public byte getAck() {
        return mAck;
    }

    public byte[] toBytes() {
        ByteBuffer byteBuffer;
        if (mCmdData != null) {
           byteBuffer = ByteBuffer.allocate(
                    CMD_CODE_FIELD_LENGTH + COUNT_BYTES_FIELD_LENGTH +
                            mCmdData.length + ACK_FIELD_LENGTH);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);

            byteBuffer.put(mCmdCode);
            byteBuffer.putShort((short) mCmdData.length);
            byteBuffer.put(mCmdData);
        } else {
            byteBuffer = ByteBuffer.allocate(
                    CMD_CODE_FIELD_LENGTH + COUNT_BYTES_FIELD_LENGTH + ACK_FIELD_LENGTH);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);

            byteBuffer.put(mCmdCode);
            byteBuffer.putShort((short)0);
        }
        byteBuffer.put(mAck);

        byteBuffer.rewind();
        return byteBuffer.array();
    }

    public static CombiUsbPacket fromBytes(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byte cmdCode = byteBuffer.get();
        short length = byteBuffer.getShort();
        byte[] cmdData = new byte[length];
        byteBuffer.get(cmdData);
        byte ack = byteBuffer.get();

        return new CombiUsbPacket(cmdCode, cmdData, ack);
    }
}

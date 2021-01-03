package com.trionictuning.combilogger.usb;

public class UsbByteBuffer {
    private static final int SIZE = 256;

    private byte[] buffer;
    private int readIdx, writeIdx, avaliable;

    public UsbByteBuffer() {
        this.buffer = new byte[SIZE];
        this.readIdx = 0;
        this.writeIdx = 0;
        this.avaliable = 0;
    }

    public void put(byte[] bytes) {
        int part1Len = Math.min(SIZE - writeIdx, bytes.length);
        int part2Len = bytes.length - part1Len;

        System.arraycopy(bytes, 0, buffer, writeIdx, part1Len);
        System.arraycopy(bytes, part1Len, buffer, 0, part2Len);

        writeIdx = getNextPosition(writeIdx, bytes.length);
        avaliable += bytes.length;
    }

    public CombiUsbPacket getPacket() {
        if (avaliable < CombiUsbPacket.MIN_PACKET_LENGTH)
            return null;

        int dataLen = (buffer[getNextPosition(readIdx, 1)] << 8) |
                (buffer[getNextPosition(readIdx, 2)]);
        int packetLen = dataLen + CombiUsbPacket.MIN_PACKET_LENGTH;

        if (avaliable < packetLen)
            return null;


        byte[] packetData = new byte[packetLen];
        int part1Len = Math.min(SIZE - readIdx, packetLen);
        int part2Len = packetLen - part1Len;

        System.arraycopy(buffer, readIdx, packetData, 0, part1Len);
        System.arraycopy(buffer, 0, packetData, part1Len, part2Len);

        readIdx = getNextPosition(readIdx, packetLen);
        avaliable -= packetLen;

        return CombiUsbPacket.fromBytes(packetData);
    }

    private int getNextPosition(int currentPosition, int offset) {
        return (currentPosition + offset) % SIZE;
    }
}

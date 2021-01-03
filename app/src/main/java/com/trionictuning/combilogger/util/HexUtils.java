package com.trionictuning.combilogger.util;

public class HexUtils {
    public static String hexlify(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (byte b: data) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}

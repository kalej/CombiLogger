package com.trionictuning.combilogger.kwp;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.trionictuning.combilogger.util.HexUtils;

public class KWPRequest {
    private static final String TAG = KWPRequest.class.getSimpleName();

    private KWPServiceId mService;
    private byte[] mParams;

    public KWPRequest(KWPServiceId service, byte[] params) {
        this.mService = service;
        this.mParams = params;
    }

    public KWPRequest(KWPServiceId service) {
        this(service, null);
    }

    public byte[] toBytes() {
        byte size = 1;

        if (this.mParams != null) {
            size += this.mParams.length;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(size + 1);

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(size);
        byteBuffer.put(mService.getCode());

        if (this.mParams != null) {
            byteBuffer.put(this.mParams);
        }

        byteBuffer.rewind();
        byte[] result = byteBuffer.array();
        Log.d(TAG, HexUtils.hexlify(result));

        return result;
    }
}

package com.trionictuning.combilogger.kwp.request;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;
import com.trionictuning.combilogger.logging.RealtimeValue;

public class KWPDynamicallyDefineLocalIdRequest extends KWPRequest {
    private static final String TAG = KWPDynamicallyDefineLocalIdRequest.class.getSimpleName();

    public KWPDynamicallyDefineLocalIdRequest(RealtimeValue value) {
        super(KWPServiceId.DynamicallyDefineLocalIdentifier, makeDefinition(value));
    }

    private static byte[] makeDefinition(RealtimeValue value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        byteBuffer.put((byte)0xF0);

        byteBuffer.put(value.getDynamicDefinition());

        int end = byteBuffer.position();
        byteBuffer.rewind();

        byte[] result = Arrays.copyOfRange(byteBuffer.array(), 0, end);

        return result;
    }
}

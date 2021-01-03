package com.trionictuning.combilogger.kwp;

import android.util.Log;

import java.util.Arrays;

import com.trionictuning.combilogger.kwp.response.KWPNegativeResponse;
import com.trionictuning.combilogger.kwp.response.KWPPositiveResponse;
import com.trionictuning.combilogger.util.HexUtils;

public class KWPResponse {
    private static final String TAG = KWPResponse.class.getSimpleName();
    private KWPServiceId mServiceId;

    protected KWPResponse(KWPServiceId serviceId) {
        this.mServiceId = serviceId;
    }

    public KWPServiceId getRequestServiceId() {
        return mServiceId;
    }

    public static KWPResponse fromBytes(byte[] bytes) {
        int totalLength = bytes[0];

        Log.d(TAG, HexUtils.hexlify(bytes));

        if (bytes[1] == 0x7F) {
            //negative response
            return new KWPNegativeResponse(
                    KWPServiceId.byCode(bytes[2]),
                    KWPNegativeResponseCode.byCode(bytes[3])
            );
        } else {
            return new KWPPositiveResponse(
                    KWPServiceId.byCode((byte)(bytes[1] & ~0x40)),
                    Arrays.copyOfRange(bytes, 2, bytes.length)
            );
        }

    }
}

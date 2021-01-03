package com.trionictuning.combilogger.kwp.response;

import java.util.Arrays;

public class KWPReadDataByLocalIdResponse extends KWPPositiveResponse {
    public KWPReadDataByLocalIdResponse(KWPPositiveResponse response) {
        super(response.getRequestServiceId(), response.mParams);
    }

    public byte getLocalId() {
        return mParams[0];
    }

    public byte[] getValue() {
        return Arrays.copyOfRange(mParams, 1, mParams.length);
    }
}

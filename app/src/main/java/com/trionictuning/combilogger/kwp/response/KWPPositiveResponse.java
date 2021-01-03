package com.trionictuning.combilogger.kwp.response;

import com.trionictuning.combilogger.kwp.KWPResponse;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPPositiveResponse extends KWPResponse {
    byte[] mParams;

    public KWPPositiveResponse(KWPServiceId serviceId, byte[] params) {
        super(serviceId);

        this.mParams = params;
    }
}

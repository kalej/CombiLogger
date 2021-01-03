package com.trionictuning.combilogger.kwp.response;

import com.trionictuning.combilogger.kwp.KWPNegativeResponseCode;
import com.trionictuning.combilogger.kwp.KWPResponse;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPNegativeResponse extends KWPResponse {
    private KWPNegativeResponseCode mCode;

    public KWPNegativeResponse(KWPServiceId serviceId, KWPNegativeResponseCode code) {
        super(serviceId);
        this.mCode = code;
    }

    public KWPNegativeResponseCode getCode() {
        return mCode;
    }
}

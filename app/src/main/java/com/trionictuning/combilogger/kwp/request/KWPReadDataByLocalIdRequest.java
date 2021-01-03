package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPReadDataByLocalIdRequest extends KWPRequest {
    public KWPReadDataByLocalIdRequest(byte localId) {
        super(KWPServiceId.ReadDataByLocalIdentifier, new byte[]{localId});
    }
}

package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPStartCommunicationRequest extends KWPRequest {
    public KWPStartCommunicationRequest() {
        super(KWPServiceId.StartCommunication);
    }
}

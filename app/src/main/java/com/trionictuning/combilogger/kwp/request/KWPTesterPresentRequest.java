package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPTesterPresentRequest extends KWPRequest {
    public KWPTesterPresentRequest() {
        super(KWPServiceId.TesterPresent);
    }
}

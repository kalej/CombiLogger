package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPSecurityAccessRequest extends KWPRequest {
    public KWPSecurityAccessRequest(byte accessLevel) {
        super(KWPServiceId.SecurityAccess, new byte[]{accessLevel});
    }

    public KWPSecurityAccessRequest(byte accessLevel, int key) {
        super(KWPServiceId.SecurityAccess, new byte[]{accessLevel, (byte)(key >> 8), (byte)key});
    }
}

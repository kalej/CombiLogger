package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPReadMemoryByAddressRequest extends KWPRequest {
    public KWPReadMemoryByAddressRequest(int address, int length) {
        super(KWPServiceId.ReadMemoryByAddress,
                new byte[]{
                        (byte)(address >> 16),
                        (byte)(address >> 8),
                        (byte)(address),
                        (byte)(length),
                        1,
                        0
                });
    }
}

package com.trionictuning.combilogger.kwp.response;

import com.trionictuning.combilogger.kwp.KWPServiceId;
import com.trionictuning.combilogger.kwp.request.KWPSecurityAccessRequest;

public class KWPSecurityAccessResponse extends KWPPositiveResponse {
    Integer mSeed;
    byte mAccessLevel;
    boolean mAccessGranted;

    public KWPSecurityAccessResponse(KWPPositiveResponse response) {
        this(response.getRequestServiceId(), response.mParams);

        mAccessLevel = mParams[0];

        if (mParams[1] == 0x34) {
            mAccessGranted = true;
        } else {
            mAccessGranted = false;

            if (mParams.length > 2) {
                mSeed = (mParams[1] << 8) | (mParams[2] & 0xFF);
            }
        }
    }

    private KWPSecurityAccessResponse(KWPServiceId serviceId, byte[] params) {
        super(serviceId, params);
    }

    public boolean isAccessGranted() {
        return mAccessGranted;
    }

    public KWPSecurityAccessRequest makeReply() {
        return new KWPSecurityAccessRequest((byte)(mAccessLevel + 1), seedResponse());
    }

    private int seedResponse() {
        return (((mSeed << 2) ^ 0x8142 ) + 0xdcaa) & 0xFFFF;
    }

    public byte getAccessLevel() {
        return mAccessLevel;
    }
}

package com.trionictuning.combilogger.kwp.response;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.trionictuning.combilogger.kwp.KWPServiceId;
import com.trionictuning.combilogger.kwp.request.KWPReadEcuIdentificationRequest;


public class KWPReadEcuIdentificationResponse extends KWPPositiveResponse {
    KWPReadEcuIdentificationRequest.EcuIdentification mEcuIdentification;
    String mStringValue;

    public KWPReadEcuIdentificationResponse(KWPPositiveResponse response) {
        this(response.getRequestServiceId(), response.mParams);
    }

    private KWPReadEcuIdentificationResponse(KWPServiceId serviceId, byte[] params) {
        super(serviceId, params);

        mEcuIdentification = KWPReadEcuIdentificationRequest.EcuIdentification.byCode(mParams[0]);
        byte[] value = Arrays.copyOfRange(mParams, 1, mParams.length);
        mStringValue = new String(value, Charset.forName("ASCII"));
    }

    public KWPReadEcuIdentificationRequest.EcuIdentification getEcuIdentification() {
        return mEcuIdentification;
    }

    public String getStringValue() {
        return mStringValue;
    }
}

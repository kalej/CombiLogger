package com.trionictuning.combilogger.kwp.request;

import com.trionictuning.combilogger.kwp.KWPRequest;
import com.trionictuning.combilogger.kwp.KWPServiceId;

public class KWPReadEcuIdentificationRequest extends KWPRequest {
    public enum EcuIdentification {
        VIN((byte)0x90),
        HardwareNumber((byte)0x91),
        ImmobilizerCode((byte)0x92),
        SoftwarePartNumber((byte)0x94),
        SoftwareVersion((byte)0x95),
        EngineType((byte)0x97);

        private byte mCode;

        EcuIdentification(byte code) {
            this.mCode = code;
        }

        public byte getCode() {
            return mCode;
        }

        public static EcuIdentification byCode(byte code) {
            for(EcuIdentification ecuId: EcuIdentification.values()) {
                if ( code == ecuId.mCode ) {
                    return ecuId;
                }
            }

            return null;
        }
    }

    public KWPReadEcuIdentificationRequest(EcuIdentification ecuId) {
        super(KWPServiceId.ReadEcuIdentification, new byte[]{ecuId.getCode()});
    }
}

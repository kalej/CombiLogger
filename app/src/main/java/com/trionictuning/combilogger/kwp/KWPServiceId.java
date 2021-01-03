package com.trionictuning.combilogger.kwp;

import java.util.HashMap;

public enum KWPServiceId {
    StartDiagnosticSession(0x10),
    EcuReset(0x11),
    ReadFreezeFrameData(0x12),
    ClearDiagnosticInformation(0x14),
    ReadDiagnosticTroubleCodesByStatus(0x18),
    ReadEcuIdentification(0x1A),
    StopDiagnosticSession(0x20),
    ReadDataByLocalIdentifier(0x21),
    ReadMemoryByAddress(0x23),
    SecurityAccess(0x27),
    DynamicallyDefineLocalIdentifier(0x2C),
    InputOutputControlByLocalIdentifier(0x30),
    StartRoutineByLocalIdentifier(0x31),
    RequestRoutineResultsByLocalIdentifier(0x33),
    RequestDownload(0x34),
    RequestUpload(0x35),
    TransferData(0x36),
    RequestTransferExit(0x37),
    WriteDataByLocalIdentifier(0x3B),
    WriteMemoryByAddress(0x3D),
    TesterPresent(0x3E),
    StartCommunication(0x81),
    StopCommunication(0x82),
    AccessTimingParameters(0x83)
    /*,

    ReadDataByCommonIdentifier(0x22),
    WriteDataByCommonIdentifier(0x2E),
    SetDataRates(0x26),
    StopRepeatedDataTransmission(0x25),
    ReadDiagnosticTroubleCodes(0x13),
    ReadStatusOfDiagnosticTroubleCodes(0x17),
    InputOutputControlByCommonIdentifier(0x2F),
    StartRoutineByAddress(0x38),
    StopRoutineByLocalIdentifier(0x32),
    StopRoutineByAddress(0x39),
    RequestRoutineResultsByAddress(0x3A)*/;

    private byte mCode;

    private static HashMap<Byte, KWPServiceId> gKWPServiceMap = new HashMap<>();

    KWPServiceId(int code) {
        byte bCode = (byte)code;
        this.mCode = bCode;
    }

    public static KWPServiceId byCode(byte code) {
        if (gKWPServiceMap.isEmpty()) {
            for(KWPServiceId svc: KWPServiceId.values()) {
                gKWPServiceMap.put(svc.mCode, svc);
            }
        }

        return gKWPServiceMap.get(code);
    }

    public byte getCode() {
        return mCode;
    }
}

package com.trionictuning.combilogger.kwp;

import java.util.HashMap;

public enum KWPNegativeResponseCode {
    GeneralReject(0x10),
    ServiceNotSupported(0x11),
    SubFunctionNotSupportedOrInvalidFormat(0x12),
    BusyRepeatRequest(0x21),
    ConditionsNotCorrectOrRequestSequenceError(0x22),
    RoutineNotCompleteOrServiceInProgress(0x23),
    RequestOutOfRange(0x31),
    SecurityAccessDeniedOrRequested(0x33),
    InvalidKey(0x35),
    ExceedNumberOfAttempts(0x36),
    RequiredTimeDelayNotExpired(0x37),
    DownloadNotAccepted(0x40),
    ImproperDownloadType(0x41),
    CannotDownloadToSpecifiedAddress(0x42),
    CannotDownloadNumberOfBytesRequested(0x43),
    UploadNotAccepted(0x50),
    ImproperUploadType(0x51),
    CannotUploadFromSpecifiedAddress(0x52),
    CannotUploadNumberOfBytesRequested(0x53),
    TransferSuspended(0x71),
    TransferAborted(0x72),
    IllegalAddressInBlockTransfer(0x74),
    IllegalByteCountInBlockTransfer(0x75),
    IllegalBlockTransferType(0x76),
    BlockTransferDataChecksumError(0x77),
    RequestCorrectlyReceivedResponsePending(0x78),
    IncorrectByteCountDuringBlockTransfer(0x79),
    ServiceNotSupportedInActiveDiagnosticSession(0x80);
    private byte mCode;

    private static HashMap<Byte, KWPNegativeResponseCode> gKWPServiceMap = new HashMap<>();

    KWPNegativeResponseCode(int code) {
        byte bCode = (byte)code;
        this.mCode = bCode;
    }

    public static KWPNegativeResponseCode byCode(byte code) {
        if (gKWPServiceMap.isEmpty()) {
            for(KWPNegativeResponseCode svc: KWPNegativeResponseCode.values()) {
                gKWPServiceMap.put(svc.mCode, svc);
            }
        }

        return gKWPServiceMap.get(code);
    }

    public byte getCode() {
        return mCode;
    }
}

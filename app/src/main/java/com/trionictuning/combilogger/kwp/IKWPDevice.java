package com.trionictuning.combilogger.kwp;

public interface IKWPDevice {
    void startSession();
    void sendRequest(KWPRequest request);
    boolean open();
    boolean close();
    boolean isOpen();
    void setListener(IKWPListener listener);
}

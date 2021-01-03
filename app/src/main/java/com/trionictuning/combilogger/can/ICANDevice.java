package com.trionictuning.combilogger.can;

public interface ICANDevice {
    boolean open();
    void close();
    boolean isOpen();
    void sendMessage(CANMessage msg);
    void addListener(ICANListener listener);
    void removeListener(ICANListener listener);
}
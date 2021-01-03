package com.trionictuning.combilogger.can;

public interface ICANListener {
    void handleMessage(CANMessage msg);
    boolean acceptsMessage(int id);
}

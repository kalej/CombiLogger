package com.trionictuning.combilogger.can.listeners;

import android.util.Log;

import com.trionictuning.combilogger.can.CANMessage;
import com.trionictuning.combilogger.can.ICANListener;

public class OutTEngineListener implements ICANListener {
    private static final String TAG = OutTEngineListener.class.getSimpleName();
    private static final String OUT_T_ENGINE = "Out.T_Engine";

    private Integer tEngValue = null;

    @Override
    public void handleMessage(CANMessage msg) {
        Integer newTEng = (0 | msg.getData()[1]) - 40;

        Log.i(TAG, OUT_T_ENGINE + '=' + newTEng);
    }

    @Override
    public boolean acceptsMessage(int id) {
        return id == 0x5C0;
    }
}

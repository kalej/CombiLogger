package com.trionictuning.combilogger.logging;

import android.os.Handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.trionictuning.combilogger.kwp.response.KWPReadDataByLocalIdResponse;
import com.trionictuning.combilogger.trionic.T7Binary;

public class RealtimeManager {
    private static final String[] gLiveValueNames = new String[] {
            "ActualIn.n_Engine",
            "Lambda.LambdaInt",
            "m_Request",
            "ECMStat.ST_ActiveAirDem",
            "Out.M_Engine",
            "MAF.m_AirInlet",
            "ActualIn.T_Engine",
            "ActualIn.T_AirInlet",
            "ActualIn.p_AirInlet",
            "IgnProt.fi_Offset",
            "ActualIn.v_Vehicle",
            "Out.X_AccPedal",
            "Lambda.Status",
            "FCut.CutStatus",
            "Out.PWM_BoostCntrl",
            "Out.fi_Ignition",
            "TorqueProt.M_LowLim",
            "ActualIn.p_AirAmbient",
    };

    private T7Binary mBinary;

    //Trionic 7 accepts maximum of 60 dynamically defined identifiers
    private ArrayList<RealtimeValue> mRealtimeValues = new ArrayList<>(60);
    private static RealtimeManager gInstance;

    public static RealtimeManager getInstance() {
        if (gInstance == null) {
            gInstance = new RealtimeManager();
        }

        return gInstance;
    }

    public void setBinary(T7Binary binary) {
        this.mBinary = binary;

        for(String name: gLiveValueNames) {
            RealtimeValue value = new RealtimeValue(this.mBinary.getSymbolByName(name));
            value.setDynamicallyDefinedId(mRealtimeValues.size());
            mRealtimeValues.add(value);
        }
    }

    public boolean binaryLoaded() {
        return this.mBinary != null;
    }

    public RealtimeValue getRTValue(int i) {
        return mRealtimeValues.get(i);
    }

    public int getRTValueCount() {
        return mRealtimeValues.size();
    }

    public String[] getNames() {
        return gLiveValueNames;
    }

    public void onReadValues(KWPReadDataByLocalIdResponse resp) {
        ByteBuffer buffer = ByteBuffer.wrap(resp.getValue());
        buffer.rewind();
        buffer.order(ByteOrder.BIG_ENDIAN);

        for(RealtimeValue value: mRealtimeValues) {
            value.readValueBytes(buffer);
        }

        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(0);
        }
    }

    private Handler mHandler;
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
}

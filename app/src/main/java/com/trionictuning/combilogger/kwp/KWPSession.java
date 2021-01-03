package com.trionictuning.combilogger.kwp;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;

import com.trionictuning.combilogger.CombiLoggerApp;
import com.trionictuning.combilogger.kwp.request.KWPDynamicallyDefineLocalIdRequest;
import com.trionictuning.combilogger.kwp.request.KWPReadDynamicallyDefinedDataRequest;
import com.trionictuning.combilogger.kwp.request.KWPReadEcuIdentificationRequest;
import com.trionictuning.combilogger.kwp.request.KWPSecurityAccessRequest;
import com.trionictuning.combilogger.kwp.request.KWPStartCommunicationRequest;
import com.trionictuning.combilogger.kwp.request.KWPTesterPresentRequest;
import com.trionictuning.combilogger.kwp.response.KWPNegativeResponse;
import com.trionictuning.combilogger.kwp.response.KWPPositiveResponse;
import com.trionictuning.combilogger.kwp.response.KWPReadDataByLocalIdResponse;
import com.trionictuning.combilogger.kwp.response.KWPReadEcuIdentificationResponse;
import com.trionictuning.combilogger.kwp.response.KWPSecurityAccessResponse;
import com.trionictuning.combilogger.logging.RealtimeManager;
import com.trionictuning.combilogger.trionic.T7Binary;

public class KWPSession implements IKWPListener, Runnable {
    private static final String TAG = KWPSession.class.getSimpleName();

    private IKWPDevice mDevice;
    private State mState;
    private boolean mGotSecurityAccess;

    Handler mReadDataPeriodicHandler = new Handler();
    private Runnable mReadDataRunnable = new Runnable() {
        @Override
        public void run() {
            KWPSession.this.enqueueRequest(new KWPReadDynamicallyDefinedDataRequest());
            mReadDataPeriodicHandler.postDelayed(this, 500);
        }
    };

    /*Keep alive timeout is 6000ms in Trionic7, so we start sending half a second earlier
    to avoid the risk of T7 breaking KWP session.
     */
    Handler mKeepAlivePeriodicHandler = new Handler();
    Runnable mKeepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            KWPSession.this.enqueueRequest(new KWPTesterPresentRequest());
        }
    };

    public KWPSession(IKWPDevice device) {
        this.mDevice = device;
        this.mState = State.STOPPED;
        this.mGotSecurityAccess = false;
    }

    @Override
    public void run() {
        this.mState = State.RUNNING;

        this.mDevice.setListener(this);
        this.mDevice.open();

        enqueueRequest(new KWPStartCommunicationRequest());

        while(this.mState.equals(State.RUNNING)) {
            if (!mRequestQueue.isEmpty()) {
                this.mDevice.sendRequest(mRequestQueue.pollFirst());

                mKeepAlivePeriodicHandler.removeCallbacks(mKeepAliveRunnable);
                mKeepAlivePeriodicHandler.postDelayed(mKeepAliveRunnable, 5500);
            }
        }

        mKeepAlivePeriodicHandler.removeCallbacks(mKeepAliveRunnable);
        mReadDataPeriodicHandler.removeCallbacks(mReadDataRunnable);
        this.mDevice.close();
    }

    public void stop() {
        this.mState = State.STOPPED;
    }

    private LinkedList<KWPRequest> mRequestQueue = new LinkedList<>();
    private void enqueueRequest(KWPRequest request) {
        mRequestQueue.addLast(request);
    }

    @Override
    public void handleResponse(KWPResponse response) {
        if (response instanceof KWPPositiveResponse) {
            switch (response.getRequestServiceId()) {
                case StartCommunication:
                    enqueueRequest(
                            new KWPReadEcuIdentificationRequest(
                                    KWPReadEcuIdentificationRequest.EcuIdentification.SoftwareVersion
                            )
                    );
                    break;
                case TesterPresent:
                    Log.d(TAG, "Alive!");
                    break;
                case ReadEcuIdentification:
                    KWPReadEcuIdentificationResponse ecuIdResp =
                            new KWPReadEcuIdentificationResponse((KWPPositiveResponse)response);

                    if (ecuIdResp.getEcuIdentification() ==
                            KWPReadEcuIdentificationRequest.EcuIdentification.SoftwareVersion) {
                        String swVersion = ecuIdResp.getStringValue();
                        Log.d(TAG, "Software version: " + swVersion);

                        CombiLoggerApp.getInstance().loadBinary(swVersion);
                    }

                    enqueueRequest(
                        new KWPSecurityAccessRequest((byte)5)
                    );
                    break;
                case SecurityAccess:
                    KWPSecurityAccessResponse secResp =
                            new KWPSecurityAccessResponse((KWPPositiveResponse)response);

                    if (secResp.isAccessGranted()) {
                        Log.d(TAG, "Security access granted");
                        mGotSecurityAccess = true;

                        if (RealtimeManager.getInstance().binaryLoaded()) {
                            startDynamicDefinitionOfLocalIds();
                        }
                        break;
                    }

                    if (secResp.getAccessLevel() == 0x05) {
                        enqueueRequest(secResp.makeReply());
                    } else {
                        Log.d(TAG, "Security access grant failed");
                    }
                    break;
                case DynamicallyDefineLocalIdentifier:
                    if (mCurrentRealtimeIdx < RealtimeManager.getInstance().getRTValueCount()) {
                        dynamicDefineLocalIds();
                    }
                    else {
                        Log.d(TAG, "Realtime values defined");
                        mReadDataPeriodicHandler.post(mReadDataRunnable);
                    }
                    break;
                case ReadDataByLocalIdentifier:
                    KWPReadDataByLocalIdResponse resp =
                            new KWPReadDataByLocalIdResponse((KWPPositiveResponse)response);

                    if (resp.getLocalId() == (byte)0xF0) {
                        RealtimeManager.getInstance().onReadValues(resp);
                    }
                    break;
            }
        } else if (response instanceof KWPNegativeResponse) {

        }
    }

    private enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public void setBinary(T7Binary binary) {
        RealtimeManager.getInstance().setBinary(binary);

        if (this.mGotSecurityAccess) {
            startDynamicDefinitionOfLocalIds();
        }
    }

    private int mCurrentRealtimeIdx;
    private void startDynamicDefinitionOfLocalIds() {
        mCurrentRealtimeIdx = 0;

        dynamicDefineLocalIds();
    }

    private void dynamicDefineLocalIds() {
        KWPDynamicallyDefineLocalIdRequest request =
                new KWPDynamicallyDefineLocalIdRequest(
                        RealtimeManager.getInstance().getRTValue(mCurrentRealtimeIdx));

        enqueueRequest(request);

        mCurrentRealtimeIdx++;
    }
}

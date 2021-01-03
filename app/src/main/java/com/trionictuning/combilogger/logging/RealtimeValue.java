package com.trionictuning.combilogger.logging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

import com.trionictuning.combilogger.trionic.Symbol;

public class RealtimeValue {
    private static final String TAG = RealtimeValue.class.getSimpleName();
    @NonNull
    private Symbol mSymbol;

    @Nullable
    private Integer mArrayIndex;

    @Nullable
    private Integer mDynamicallyDefinedId;

    private Long mValue;

    public RealtimeValue(@NonNull Symbol symbol) {
        this.mSymbol = symbol;
    }

    @NonNull
    public Symbol getSymbol() {
        return mSymbol;
    }

    @Nullable
    public Integer getArrayIndex() {
        return mArrayIndex;
    }

    @Nullable
    public Integer getDynamicallyDefinedId() {
        return mDynamicallyDefinedId;
    }

    public void setArrayIndex(Integer arrayIndex) {
        this.mArrayIndex = arrayIndex;
    }

    public void setDynamicallyDefinedId(Integer dynamicallyDefinedId) {
        this.mDynamicallyDefinedId = dynamicallyDefinedId;
    }

    public String getName() {
        String result = this.mSymbol.getName();
        if (this.mSymbol.isArray()) {
            result += String.format("[%d]", this.mArrayIndex);
        }

        return result;
    }

    public byte[] getDynamicDefinition() {
        /*
        return new byte[]{
                (byte) 0x03,
                this.mDynamicallyDefinedId.byteValue(),
                (byte) this.mSymbol.getType().getSize(),
                (byte) (this.mSymbol.getAddress() >> 16),
                (byte) (this.mSymbol.getAddress() >> 8),
                (byte) (this.mSymbol.getAddress() >> 0)
        };
         */
        if (this.mSymbol.isArray()) {
            //we can define array element only by address, otherwise we will get whole array in response
            int address = this.mSymbol.getAddress() + this.mArrayIndex*this.mSymbol.getType().getSize();

            return new byte[]{
                    (byte) 0x03,
                    this.mDynamicallyDefinedId.byteValue(),
                    (byte) this.mSymbol.getType().getSize(),
                    (byte) (address >> 16),
                    (byte) (address >> 8),
                    (byte) (address >> 0)
            };
        } else if (this.mSymbol.getLocalId() != null) {
            return new byte[]{
                    (byte) 0x01,
                    this.mDynamicallyDefinedId.byteValue(),
                    (byte) 0x00,
                    this.mSymbol.getLocalId().byteValue(),
                    (byte) 0x00
            };
        } else {
            int symbolNumber = this.mSymbol.getNumber();
            return new byte[]{
                (byte) 0x03,
                this.mDynamicallyDefinedId.byteValue(),
                (byte) 0x00,
                (byte) 0x80,
                (byte) (symbolNumber >> 8),
                (byte) (symbolNumber >> 0)
            };
        }
    }

    public void readValueBytes(ByteBuffer buffer) {
        long v = 0;
        switch (this.mSymbol.getType()) {
            case BYTE:
                v = buffer.get();
                break;
            case WORD:
                v = buffer.getShort();
                break;
            case LONG:
                v = buffer.getInt();
                break;
        }

        if (this.mSymbol.getMask() != null) {
            v = (v & this.mSymbol.getMask()) != 0 ? 1 : 0;

        } else if (!this.mSymbol.isSigned()) {
            v &= (1L << (this.mSymbol.getType().getSize() * 8)) - 1;
        }

        this.mValue = v;

        //Logger.d(String.format("%s = %d", this.getName(), v));
    }

    public String getValue() {
        if (this.mValue == null) {
            return "----";
        } else {
            return this.mValue.toString();
        }
    }
}

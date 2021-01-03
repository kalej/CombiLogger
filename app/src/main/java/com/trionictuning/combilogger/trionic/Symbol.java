package com.trionictuning.combilogger.trionic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Symbol {
    private static final String JSON_NUMBER = "number";
    private static final String JSON_NAME = "name";
    private static final String JSON_ADDRESS = "address";
    private static final String JSON_LENGTH = "length";
    private static final String JSON_TYPE = "type";
    private static final String JSON_MASK = "mask";
    private static final String JSON_LOCAL_ID = "local_id";
    private static final String JSON_SIGNED = "signed";

    public enum Type {
        BYTE("byte", 1), WORD ("word", 2), LONG("long", 4);

        @NonNull
        String mName;

        @NonNull
        int mSize;

        Type(String name, int size) {
            this.mName = name;
            this.mSize = size;
        }

        public static Type byName(String name) {
            for(Type t: Type.values()) {
                if (t.mName.equals(name))
                    return t;
            }

            return null;
        }

        @NonNull
        public String getName() {
            return mName;
        }

        @NonNull
        public int getSize() {
            return mSize;
        }
    }

    @Nullable
    private Integer mNumber;

    @NonNull
    private String mName;

    @Nullable
    private Integer mAddress;

    @NonNull
    private Integer mLength;

    @NonNull
    private Type mType;

    @Nullable
    private Integer mMask;

    @Nullable
    private Integer mLocalId;

    @NonNull
    private Boolean mSigned;

    public Symbol(JSONObject json) throws JSONException {
        this.mName = json.getString(JSON_NAME);
        this.mNumber = json.getInt(JSON_NUMBER);

        String address = json.getString(JSON_ADDRESS);
        if (address.startsWith("0x")) {
            this.mAddress = Integer.parseInt(address.substring(2), 16);
        } else {
            this.mAddress = Integer.parseInt(address);
        }

        try {
            String localId = json.getString(JSON_LOCAL_ID);

            if (localId.startsWith("0x")) {
                this.mLocalId = Integer.parseInt(localId.substring(2), 16);
            } else {
                this.mLocalId = Integer.parseInt(localId);
            }
        }
        catch (NullPointerException ex) {
            //That's OK if the symbol cannot be referenced by local id. Most of them cannot.
        }
        catch (JSONException ex) {
            //That's OK if the symbol cannot be referenced by local id. Most of them cannot.
        }

        try {
            // If this is an ordinary symbol a NPE would be thrown
            String mask = json.getString(JSON_MASK);

            if (mask.startsWith("0x")) {
                this.mMask = Integer.parseInt(mask.substring(2), 16);
            } else {
                this.mMask = Integer.parseInt(mask);
            }

            this.mLength = 2;
            this.mType = Type.WORD;
            this.mSigned = true;
        }
        catch (JSONException ex) {
            //That's OK if the symbol is not a single bit in a word. Most of them are not.
            this.mLength = json.getInt(JSON_LENGTH);
            this.mType = Type.byName(json.getString(JSON_TYPE));
            this.mSigned = json.getBoolean(JSON_SIGNED);
        }
    }

    @Nullable
    public Integer getNumber() {
        return mNumber;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @Nullable
    public Integer getAddress() {
        return mAddress;
    }

    @NonNull
    public Integer getLength() {
        return mLength;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Nullable
    public Integer getMask() {
        return mMask;
    }

    @Nullable
    public Integer getLocalId() {
        return mLocalId;
    }

    @NonNull
    public Boolean isSigned() {
        return mSigned;
    }

    public boolean isArray() {
        return (this.mLength / this.mType.mSize) > 1;
    }
}

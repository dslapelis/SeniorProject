package com.lunarstack.seniorproject;

/**
 * Created by danielslapelis on 4/16/18.
 */

public class Message {

    // two statuses -- 0 means it was sent, 1 means it was received.
    private int mStatus;
    private String mMessage;

    public Message (String message, int status) {
        this.mStatus = status;
        this.mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getStatus() {
        return mStatus;
    }
}

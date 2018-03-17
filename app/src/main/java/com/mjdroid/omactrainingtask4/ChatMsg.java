package com.mjdroid.omactrainingtask4;

/**
 * Created by Mazen on 3/17/2018.
 */
public class ChatMsg {
    private String mMsg;
    private int mState = MSG_PENDING;
    private boolean mOutgoing = OUTGOING;

    public static final int MSG_PENDING = R.drawable.msg_status_gray_waiting;
    public static final int MSG_SENT = R.drawable.msg_status_server_receive;
    public static final int MSG_SEEN = R.drawable.msg_status_client_read;
    public static final boolean OUTGOING = true;

    public ChatMsg(String msg, int state, boolean outgoing){
        mMsg = msg;
        mState = state;
        mOutgoing = outgoing;
    }

    public String getMsg(){
        return mMsg;
    }

    public int getState() {
        return mState;
    }

    public boolean isOutgoing(){
        return mOutgoing;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public void setState(int state) {
        mState = state;
    }

}

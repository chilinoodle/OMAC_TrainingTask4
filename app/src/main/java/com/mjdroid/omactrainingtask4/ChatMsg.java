package com.mjdroid.omactrainingtask4;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

/**
 * Created by Mazen on 3/17/2018.
 */
public class ChatMsg {
    private String mMsg;
    private int mState = MSG_PENDING;
    private boolean mOutgoing = OUTGOING;
    private String mImageFileName;

    public static final int MSG_PENDING = R.drawable.msg_status_gray_waiting;
    public static final int MSG_SENT = R.drawable.msg_status_server_receive;
    public static final int MSG_SEEN = R.drawable.msg_status_client_read;
    public static final boolean OUTGOING = true;

    public ChatMsg(String msg, int state, boolean outgoing){
        mMsg = msg;
        mState = state;
        mOutgoing = outgoing;
    }

    public ChatMsg(String msg, int state, boolean outgoing, String imageFileName) {
        mMsg = msg;
        mState = state;
        mOutgoing = outgoing;
        mImageFileName = imageFileName;
    }

    public String getMsg(){
        return mMsg;
    }

    public String getImageFileName() {
        return mImageFileName;
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

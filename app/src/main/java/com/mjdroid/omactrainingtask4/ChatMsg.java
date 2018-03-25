package com.mjdroid.omactrainingtask4;


import java.util.Date;

/**
 * Created by Mazen on 3/17/2018.
 */
public class ChatMsg {
    private String mMsg;
    private int mState = MSG_PENDING;
    private boolean mOutgoing = OUTGOING;
    private String mImageFileName;
    private String mImagePath;
    private long mTime;
    private boolean mHasImage = WITHOUT_IMAGE;
    private boolean mHasVideo = WITHOUT_VIDEO;

    public static final int MSG_PENDING = R.drawable.msg_status_gray_waiting;
    public static final int MSG_SENT = R.drawable.msg_status_server_receive;
    public static final int MSG_SEEN = R.drawable.msg_status_client_read;
    public static final boolean OUTGOING = true;
    public static final boolean INCOMING = false;
    public static final boolean WITH_IMAGE = true;
    public static final boolean WITH_VIDEO = true;
    public static final boolean WITHOUT_IMAGE = false;
    public static final boolean WITHOUT_VIDEO = false;


    public ChatMsg(String msg, int state, boolean outgoing, boolean hasImage, boolean hasVideo, long time){
        mMsg = msg;
        mState = state;
        mOutgoing = outgoing;
        mHasImage = hasImage;
        mHasVideo = hasVideo;
        mTime = time;
    }

    public ChatMsg(String msg, int state, boolean outgoing, boolean hasImage, boolean hasVideo, long time, String imageFileName, String imagePath) {
        mMsg = msg;
        mState = state;
        mOutgoing = outgoing;
        mHasImage = hasImage;
        mHasVideo = hasVideo;
        mTime = time;
        mImageFileName = imageFileName;
        mImagePath = imagePath;
    }

    public String getMsg(){
        return mMsg;
    }

    public String getImageFileName() {
        return mImageFileName;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public int getState() {
        return mState;
    }

    public boolean isOutgoing(){
        return mOutgoing;
    }

    public boolean hasImage() {
        return mHasImage;
    }

    public boolean hasVideo() {
        return mHasVideo;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public void setState(int state) {
        mState = state;
    }

    public long getMsgTime () {
        return mTime;
    }

    public void setMsgTime (long time) {
        mTime = time;
    }

}

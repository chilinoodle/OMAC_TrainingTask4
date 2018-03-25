package com.mjdroid.omactrainingtask4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Mazen on 3/17/2018.
 */
public class ChatAdapter extends ArrayAdapter<ChatMsg> {

    public ChatAdapter(Context context, ArrayList<ChatMsg> msgs) {
        super(context, 0, msgs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View chatItemView = convertView;

        if (chatItemView == null) {
            chatItemView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, parent, false);
        }

        ImageView mainImage = (ImageView) chatItemView.findViewById(R.id.main_image);
        TextView mainMsg = (TextView) chatItemView.findViewById(R.id.main_msg);

        ChatMsg currentMsg = getItem(position);

        /*Log.v("ChatMsg at",""+position);
        Log.v("Text", currentMsg.getMsg());
        Log.v("state", "" + currentMsg.getState());
        Log.v("Time", "" + currentMsg.getMsgTime());
        Log.v("Has image", "" + currentMsg.hasImage());
        Log.v("Has video", "" + currentMsg.hasVideo());
        Log.v("------","------");*/

        Bitmap imageBM;

        if (currentMsg.hasImage()) {

            String pathAndName = currentMsg.getImageFileName();
            File image = new File(pathAndName);
            imageBM = BitmapFactory.decodeFile(image.getAbsolutePath());
            mainImage.setVisibility(View.VISIBLE);
            mainMsg.setVisibility(View.GONE);

            try {
                int height = imageBM.getHeight();
                int width = imageBM.getWidth();
                Bitmap imageBMTN = Bitmap.createScaledBitmap(imageBM, width / 10, height / 10, false);
                mainImage.setImageBitmap(imageBMTN);
            }
            catch (NullPointerException e) {
                mainImage.setVisibility(View.GONE);
                mainMsg.setVisibility(View.VISIBLE);
                mainMsg.setText("(Image was deleted locally)");
            }

        } else {
            mainImage.setVisibility(View.GONE);
            mainMsg.setVisibility(View.VISIBLE);
            mainMsg.setText(currentMsg.getMsg());
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();
        long msgTime = currentMsg.getMsgTime();
        int msgAge = (int) (currentTime - msgTime);
        String timeString;
        SimpleDateFormat sdf;

        if (msgAge < 10 * 1000) {
            timeString = "Just a moment ago";
        } else if (msgAge < 15 * 1000) {
            timeString = (int) Math.floor(msgAge / 1000) + " seconds ago";
        } else if (msgAge < 60 * 1000) {
            timeString = "Less than a minute ago";
        } else if (msgAge < 30 * 60 * 1000) {
            timeString = (int) Math.floor(msgAge / (60 * 1000)) + " minutes ago";
        } else if (msgAge < 24 * 60 * 60 * 1000){
            sdf = new SimpleDateFormat("HH:mm");
            timeString = sdf.format(msgTime);
        } else {
            sdf = new SimpleDateFormat("dd/MM");
            timeString = sdf.format(msgTime);
        }

        TextView timeView = (TextView) chatItemView.findViewById(R.id.time);
        timeView.setText(timeString);

        ImageView statusView = (ImageView) chatItemView.findViewById(R.id.state);
        statusView.setImageResource(currentMsg.getState());

        LinearLayout bubble = (LinearLayout) chatItemView.findViewById(R.id.msg_bubble);

        if(currentMsg.isOutgoing() == ChatMsg.OUTGOING) {
            bubble.setBackgroundResource(R.drawable.balloon_outgoing_normal);
            ((RelativeLayout) chatItemView).setGravity(Gravity.RIGHT);
            statusView.setVisibility(View.VISIBLE);
        } else {
            bubble.setBackgroundResource(R.drawable.balloon_incoming_normal);
            ((RelativeLayout) chatItemView).setGravity(Gravity.LEFT);
            statusView.setVisibility(View.GONE);
        }

        return chatItemView;
    }

}

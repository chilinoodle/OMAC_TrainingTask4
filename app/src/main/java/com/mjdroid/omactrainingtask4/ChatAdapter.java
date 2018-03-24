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
import java.util.ArrayList;

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

        ChatMsg currentMsg = getItem(position);

        if (currentMsg.getImageFileName() != null) {
            ImageView imageView = (ImageView) chatItemView.findViewById(R.id.main_image);
            imageView.setVisibility(View.VISIBLE);
            String pathAndName = currentMsg.getImageFileName();
            File image = new File(pathAndName);
            Bitmap imageBM = BitmapFactory.decodeFile(image.getAbsolutePath());
            imageView.setImageBitmap(imageBM);

        } else {
            ImageView imageView = (ImageView) chatItemView.findViewById(R.id.main_image);
            imageView.setVisibility(View.GONE);
            TextView msgView = (TextView) chatItemView.findViewById(R.id.main_msg);
            msgView.setText(currentMsg.getMsg());
        }

        TextView timeView = (TextView) chatItemView.findViewById(R.id.time);
        timeView.setText("12 seconds ago, 7:45 am");

        ImageView statusView = (ImageView) chatItemView.findViewById(R.id.state);
        statusView.setImageResource(currentMsg.getState());

        LinearLayout bubble = (LinearLayout) chatItemView.findViewById(R.id.msg_bubble);

        if(currentMsg.isOutgoing()) {
            bubble.setBackgroundResource(R.drawable.balloon_outgoing_normal);
            ((RelativeLayout) chatItemView).setGravity(Gravity.RIGHT);
        } else {
            bubble.setBackgroundResource(R.drawable.balloon_incoming_normal);
            ((RelativeLayout) chatItemView).setGravity(Gravity.LEFT);
            statusView.setVisibility(View.GONE);
        }

        return chatItemView;
    }

}

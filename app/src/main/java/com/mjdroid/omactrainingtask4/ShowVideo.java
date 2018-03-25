package com.mjdroid.omactrainingtask4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

public class ShowVideo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        VideoView videoContainer = (VideoView) findViewById(R.id.video_container);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);

        Intent receivedImage = getIntent();

        if (receivedImage.getStringExtra("videoName") != null) {
            String fileName = receivedImage.getStringExtra("videoName");
            String filePath = receivedImage.getStringExtra("videoPath");
            videoContainer.setVideoPath(filePath+"/"+fileName);
            videoContainer.start();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToChat = new Intent(ShowVideo.this, ConversationActivity.class);
                startActivity(backToChat);
            }
        });
    }
}

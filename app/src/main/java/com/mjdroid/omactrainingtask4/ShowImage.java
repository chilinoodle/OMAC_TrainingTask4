package com.mjdroid.omactrainingtask4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class ShowImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        ImageView imageContainer = (ImageView) findViewById(R.id.image_container);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);

        Intent receivedImage = getIntent();

        if (receivedImage.getStringExtra("imagePath") != null) {
            String fileNameAndPath = receivedImage.getStringExtra("imagePath");
            File imageFile = new File(fileNameAndPath);
            Bitmap imageBM = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            imageContainer.setImageBitmap(imageBM);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToChat = new Intent(ShowImage.this, ConversationActivity.class);
                startActivity(backToChat);
            }
        });
    }
}

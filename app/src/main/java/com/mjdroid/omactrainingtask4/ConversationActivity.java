package com.mjdroid.omactrainingtask4;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ConversationActivity extends AppCompatActivity {

    public ArrayList<ChatMsg> chatArray;
    public ListView chatList;
    public ChatAdapter adapter;
    public EditText chatEdit;
    public boolean isOutgoing = ChatMsg.OUTGOING;
    public boolean withImage = ChatMsg.WITH_IMAGE;
    public boolean withVideo = ChatMsg.WITH_VIDEO;
    public boolean withoutImage = ChatMsg.WITHOUT_IMAGE;
    public boolean withoutVideo = ChatMsg.WITHOUT_VIDEO;
    public SoundPool.Builder spb;
    public AudioAttributes.Builder aab;
    public SoundPool sp;
    public boolean loaded = false;
    public AudioManager am;
    public float volume;
    public int sendSound;
    public int receiveSound;
    public final int MY_PERMISSIONS_REQUEST_CAMERA=333;
    public final int MY_PERMISSIONS_REQUEST_STORAGE=444;
    public final int REQUEST_IMAGE_CAPTURE = 222;
    public final int REQUEST_VIDEO_CAPTURE = 555;
    public String mCurrentPhotoPath;
    public String mCurrentVideoPath;
    public String currentImageFileName;
    public String currentVideoFileName;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar conversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(conversationToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (ContextCompat.checkSelfPermission(ConversationActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        if (ContextCompat.checkSelfPermission(ConversationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
        }

        ImageView sendButton = (ImageView) findViewById(R.id.send_button);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        ImageView camButton = (ImageView) findViewById(R.id.cam_icon);

        chatEdit = (EditText) findViewById(R.id.chat_edit);
        chatList = (ListView) findViewById(R.id.chat_list);

        loadChatList();
        updateAdapter();
        prepareSounds();

        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent backToMain = new Intent(ConversationActivity.this, MainActivity.class);
                    startActivity(backToMain);
                }
            });
        }


        if (sendButton.isSoundEffectsEnabled()) {
            sendButton.setSoundEffectsEnabled(false);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long msgTime = Calendar.getInstance().getTimeInMillis();
                //ChatMsg(String msg, int state, boolean outgoing, boolean hasImage, boolean hasVideo, long time)
                ChatMsg msg = new ChatMsg(chatEdit.getText().toString(), ChatMsg.MSG_PENDING, isOutgoing, withoutImage, withoutVideo, msgTime);
                chatArray.add(msg);
                if (msg.isOutgoing()) {
                    playSend();

                } else {
                    playReceived();
                }
                updateAdapter();
                saveChatList();
                chatEdit.getText().clear();
            }
        });

        if (camButton != null) {
            camButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            long msgTime = Calendar.getInstance().getTimeInMillis();

            try {
                createThumbnail(currentImageFileName, mCurrentPhotoPath);
            }
            catch (Exception e) {
                Toast.makeText(ConversationActivity.this, "Thumbnail was not created", Toast.LENGTH_SHORT).show();
            }
            //ChatMsg(String msg, int state, boolean outgoing, boolean hasImage, boolean hasVideo, long time, String imageFileName, String imagePath)
            ChatMsg msg = new ChatMsg("", ChatMsg.MSG_PENDING, isOutgoing, withImage, withoutVideo, msgTime, currentImageFileName, mCurrentPhotoPath);
            chatArray.add(msg);
            if (msg.isOutgoing()) {
                playSend();

            } else {
                playReceived();
            }
            updateAdapter();
            saveChatList();

        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            long msgTime = Calendar.getInstance().getTimeInMillis();

            try {
                createThumbnail(currentVideoFileName, mCurrentVideoPath);
            }
            catch (Exception e) {
                Toast.makeText(ConversationActivity.this, "Thumbnail was not created", Toast.LENGTH_SHORT).show();
            }
            //ChatMsg(String msg, int state, boolean outgoing, boolean hasImage, boolean hasVideo, long time, String imageFileName, String imagePath)
            ChatMsg msg = new ChatMsg("", ChatMsg.MSG_PENDING, isOutgoing, withoutImage, withVideo, msgTime, currentVideoFileName, mCurrentVideoPath);
            chatArray.add(msg);
            if (msg.isOutgoing()) {
                playSend();

            } else {
                playReceived();
            }
            updateAdapter();
            saveChatList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.switch_conversation:
                if (isOutgoing == ChatMsg.OUTGOING) {
                    isOutgoing = ChatMsg.INCOMING;
                } else if (isOutgoing == ChatMsg.INCOMING) {
                    isOutgoing = ChatMsg.OUTGOING;
                }
                return true;

            case R.id.mark_sent:
                for(ChatMsg msg: chatArray) {
                    if (msg.getState() == ChatMsg.MSG_PENDING) {
                        msg.setState(ChatMsg.MSG_SENT);
                    }
                }
                updateAdapter();
                saveChatList();
                return true;

            case R.id.mark_seen:
                for(ChatMsg msg: chatArray) {
                    if (msg.getState() == ChatMsg.MSG_SENT) {
                        msg.setState(ChatMsg.MSG_SEEN);
                    }
                }
                updateAdapter();
                saveChatList();
                return true;

            case R.id.delete_conversation:
                deleteChatHistory();
                saveChatList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateAdapter() {
        adapter = new ChatAdapter(ConversationActivity.this,chatArray);
        chatList.setAdapter(adapter);
        chatList.setSelection(chatList.getAdapter().getCount() - 1);
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMsg imageMsg = chatArray.get(position);
                if (imageMsg.hasImage()) {
                    Intent showImage = new Intent(ConversationActivity.this, ShowImage.class);
                    showImage.putExtra("imagePath", imageMsg.getImagePath());
                    showImage.putExtra("imageName", imageMsg.getImageFileName());
                    startActivity(showImage);
                } else if (imageMsg.hasVideo()) {
                    Intent showVideo = new Intent(ConversationActivity.this, ShowVideo.class);
                    showVideo.putExtra("videoPath", imageMsg.getImagePath());
                    showVideo.putExtra("videoName", imageMsg.getImageFileName());
                    startActivity(showVideo);
                }
            }
        });
    }

    public void saveChatList() {
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor =sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(chatArray);
        editor.putString("chat list", json);
        editor.apply();
    }

    public void loadChatList() {
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);

        //TO PRINT TO LOG SHAREDPREFERENCES CONTENT:
        /*Map<String, ?> allEntries = sp.getAll();
        for (Map.Entry<String,?> entry : allEntries.entrySet()) {
            Log.v("map value", entry.getKey() + ": " + entry.getValue().toString());
        }*/

        Gson gson = new Gson();
        String json = sp.getString("chat list", null);
        Type type = new TypeToken<ArrayList<ChatMsg>>() {}.getType();
        chatArray = gson.fromJson(json, type);

        //TO PRINT TO LOG CHAT ARRAY AFTER LOAD:
        /*for (int i=0; i < chatArray.size(); i++) {
            ChatMsg cm = chatArray.get(i);
            Log.v("Chat text", cm.getMsg());
            Log.v("Has image", ""+cm.hasImage());
            Log.v("Has video", ""+cm.hasVideo());
            Log.v("State", ""+cm.getState());
            Log.v("Is outgoing", ""+cm.isOutgoing());
        }*/

        if (chatArray == null) {
            chatArray = new ArrayList<ChatMsg>();
        }
    }

    public void deleteChatHistory() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
                        sp.edit().clear().apply();
                        chatArray.clear();
                        adapter.clear();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
        builder.setMessage("Are you sure you want to clear chat history?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

    public void prepareSounds() {
        //Getting system volume values to pass them in the SoundPool when played
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        float currentVolume = (float) am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        float maxVolume = (float) am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        volume = currentVolume / maxVolume;

        //Need to build audio attributes before building a SoundPool
        aab = new AudioAttributes.Builder();
        aab.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION);
        aab.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);

        //Creating a SoundPool Builder to apply the Attributes built before
        spb = new SoundPool.Builder();
        spb.setMaxStreams(5);
        spb.setAudioAttributes(aab.build());

        //Building the actual SoundPool
        sp = spb.build();
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        //Loading the sounds in the SoundPool
        sendSound = sp.load(this,R.raw.send_message,1);
        receiveSound = sp.load(this,R.raw.incoming,1);
    }

    public void playReceived() {
        if(loaded) {
            float volumeLeft = volume;
            float volumeRight = volume;
            int soundId = sp.play(receiveSound,volumeLeft,volumeRight,1,0,1f);
        }
    }

    public void playSend() {
        if(loaded) {
            float volumeLeft = volume;
            float volumeRight = volume;
            int soundId = sp.play(sendSound,volumeLeft,volumeRight,1,0,1f);
        }
    }

    public String createImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        return "MJ-" + timeStamp + ".jpg";
    }

    public String createVideoFileName() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        return "MJ-" + timeStamp + ".mp4";
    }

    public void createThumbnail(String fileName, String filePath) throws Exception {

        String extension = fileName.substring(fileName.length() - 4);

        if (extension.equals(".jpg")) {
            Bitmap originalImage = BitmapFactory.decodeFile(filePath + "/" + fileName);
            int h = originalImage.getHeight();
            int w = originalImage.getWidth();
            Bitmap imageThnBm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath + "/" + fileName), (int) Math.floor(w / 10), (int) Math.floor(h / 10));
            File imageThnDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MJ_Pictures/.thumbnails");
            if (!imageThnDir.exists()) {
                if (!imageThnDir.mkdirs()) {
                    Toast.makeText(ConversationActivity.this, "Directory was not made", Toast.LENGTH_SHORT).show();
                }
            }
            File imageThn = new File(imageThnDir + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(imageThn);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            imageThnBm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            imageThnBm.recycle();

        } else if (extension.equals(".mp4")) {
            Bitmap videoThnBm = ThumbnailUtils.createVideoThumbnail(filePath + "/" + fileName, MediaStore.Images.Thumbnails.MINI_KIND);
            File videoThnDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MJ_Videos/.thumbnails");
            if (!videoThnDir.exists()) {
                if (!videoThnDir.mkdirs()){
                    Toast.makeText(ConversationActivity.this, "Directory was not made", Toast.LENGTH_SHORT).show();
                }
            }
            String fileNameWithoutExtension = fileName.substring(0,fileName.length()-4);
            File videoThn = new File(videoThnDir + "/" + fileNameWithoutExtension + ".jpg");
            FileOutputStream fos = new FileOutputStream(videoThn);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            videoThnBm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            videoThnBm.recycle();
        }


    }

    public void showDialog(){
        final String actions[] = {"Take a Photo","Capture Video"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
        builder.setTitle("Choose Action");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedAction = actions[which];
                switch (selectedAction) {
                    case "Take a Photo":
                        takePhoto();
                        break;
                    case "Capture Video":
                        captureVideo();
                        break;
                }
            }
        });
        builder.show();
    }

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MJ_Pictures");
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir();
        }
        mCurrentPhotoPath = imageDirectory.getAbsolutePath();
        String imageName = createImageFileName();
        currentImageFileName = imageName;
        File imageFile = new File(imageDirectory, imageName);
        Uri photoUri = Uri.fromFile(imageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void captureVideo(){
        Intent captureVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MJ_Videos");
        if (!videoDirectory.exists()) {
            videoDirectory.mkdir();
        }
        mCurrentVideoPath = videoDirectory.getAbsolutePath();
        String videoName = createVideoFileName();
        currentVideoFileName = videoName;
        File videoFile = new File(videoDirectory, videoName);
        Uri videoUri = Uri.fromFile(videoFile);
        captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        startActivityForResult(captureVideoIntent, REQUEST_VIDEO_CAPTURE);
    }
}



package com.mjdroid.omactrainingtask4;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class ConversationActivity extends AppCompatActivity {

    public ArrayList<ChatMsg> chatArray;
    public ListView chatList;
    public ChatAdapter adapter;
    public EditText chatEdit;
    public boolean isOutgoing = ChatMsg.OUTGOING;
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
    public String mCurrentPhotoPath;
    public String currentImageFileName;


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
                ChatMsg msg = new ChatMsg(chatEdit.getText().toString(), ChatMsg.MSG_PENDING, isOutgoing);
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
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    mCurrentPhotoPath = imageDirectory.getAbsolutePath();
                    String imageName = createImageFileName();
                    currentImageFileName = imageName;
                    File imageFile = new File(imageDirectory,imageName);
                    Uri photoUri = Uri.fromFile(imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    //HERE WAS THE BLOCK THAT IS NOW IN onActivityResult

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
            //THIS BLOCK WAS IN OnClickListener of camButton:
            ChatMsg msg = new ChatMsg("",ChatMsg.MSG_PENDING,isOutgoing,mCurrentPhotoPath + "/" + currentImageFileName);
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
                if (isOutgoing == true) {
                    isOutgoing = false;
                } else if (isOutgoing == false) {
                    isOutgoing = true;
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
    }

    public void saveChatList() {
        SharedPreferences sp = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor =sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(chatArray);
        editor.putString("chat list", json);
        editor.apply();
    }

    public void loadChatList() {
        SharedPreferences sp = getSharedPreferences("shared preferences",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sp.getString("chat list", null);
        Type type = new TypeToken<ArrayList<ChatMsg>>() {}.getType();
        chatArray = gson.fromJson(json, type);

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
}



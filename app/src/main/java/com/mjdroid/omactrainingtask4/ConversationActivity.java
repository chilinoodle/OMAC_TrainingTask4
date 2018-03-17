package com.mjdroid.omactrainingtask4;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.lang.reflect.Type;
import java.util.ArrayList;


public class ConversationActivity extends AppCompatActivity {

    public ArrayList<ChatMsg> chatArray;
    public ListView chatList;
    public ChatAdapter adapter;
    public EditText chatEdit;
    public boolean isOutgoing = ChatMsg.OUTGOING;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar conversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        setSupportActionBar(conversationToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView sendButton = (ImageView) findViewById(R.id.send_button);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        chatEdit = (EditText) findViewById(R.id.chat_edit);
        chatList = (ListView) findViewById(R.id.chat_list);

        loadChatList();
        updateAdapter();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(ConversationActivity.this,MainActivity.class);
                startActivity(backToMain);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatArray.add(new ChatMsg(chatEdit.getText().toString(), ChatMsg.MSG_PENDING, isOutgoing));
                updateAdapter();
                saveChatList();
                chatEdit.getText().clear();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                chatArray.clear();
                adapter.clear();
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
        chatList.setSelection(chatList.getAdapter().getCount()-1);
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
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        sp.edit().remove("shared preferences").commit();
    }

}

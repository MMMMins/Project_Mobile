package com.inhatc.project_mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private String loginUID;
    private String otherUID;
    private String roomKey;
    private String roomName;
    private String userName;
    private FirebaseDatabase mFirebase;
    private DatabaseReference mDatabase = null;
    private ListView chatListView;
    private TextView txtTitle;
    private EditText edtInputMessage;
    private ImageButton btnInput;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private ChatMessage chatMessage;

    private boolean roomCheck = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Bundle extras = getIntent().getExtras();
        loginUID = extras.getString("loginUID");
        otherUID = extras.getString("otherUID");
        roomKey = extras.getString("roomKey");
        roomName = extras.getString("roomName");



        txtTitle = findViewById(R.id.txtRoomName);
        txtTitle.setText(roomName+"님과의 채팅방");
        chatListView = findViewById(R.id.chatListView);
        edtInputMessage = findViewById(R.id.edtChat);
        btnInput = findViewById(R.id.btnSend);
        btnInput.setOnClickListener(this);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, chatMessages, loginUID);
        chatListView.setAdapter(chatAdapter);

        Log.e("전달 LoginUID", loginUID);
        Log.e("전달 otherUID", otherUID);
        Log.e("전달 roomKey", roomKey);

        mFirebase = FirebaseDatabase.getInstance();
        getMessageList();
    }

    @Override
    public void onClick(View v){
        if(v == btnInput){
            mDatabase = mFirebase.getReference("messages");
            String message = edtInputMessage.getText().toString();
            String name = userName;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            UUID uid = UUID.randomUUID();
            chatMessage = new ChatMessage(message, name, timestamp.toString(), loginUID);
            HashMap<String, Object> map = new HashMap<>();
            map.put("message",message);
            map.put("name",name);
            map.put("timestamp",timestamp.toString());
            map.put("uid",loginUID);
            if(roomCheck) {
                mDatabase.child(roomKey).child(uid.toString()).updateChildren(map);
                getMessage();
            }
            else{
                mDatabase.child(roomKey).child(uid.toString()).setValue(chatMessage);
                getMessageList();
            }
            edtInputMessage.setText("");
        }
    }

    public void getMessage(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.add(chatMessage);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public void getMessageList() {
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                HashMap<String,String> map = (HashMap<String, String>) task.getResult().child("users").child(loginUID).getValue();
                userName = map.get("name");
                if (task.getResult().child("messages").hasChild(roomKey)) {
                    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
                    Query query = messagesRef.child(roomKey).orderByChild("time");

                    Log.e("query", query.toString());
                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                            String messageID = dataSnapshot.getKey();
                            Log.e("EEE1", dataSnapshot.getValue().toString());
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            chatMessages.add(chatMessage);
                            chatAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
                else{
                    roomCheck = false;
                }
            }
        });
    }
}
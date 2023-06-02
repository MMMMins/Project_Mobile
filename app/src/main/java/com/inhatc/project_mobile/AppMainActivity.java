package com.inhatc.project_mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppMainActivity extends AppCompatActivity implements View.OnClickListener{

    private TabHost tabHost;
    private TabHost.TabSpec tabSpec;
    private ListView userList;
    private FirebaseDatabase mFirebase;
    private DatabaseReference mDatabase = null;
    private ArrayAdapter<String> adpater;

    private EditText edtSearch;
    private Button btnSearch;
    private Button btnFriInsert;
    private TextView txtSearchEmail;
    private TextView txtSearchName;
    private ListView roomList;
    private String loginUID;
    private String loginName;
    private List<User> tempList;
    private String roomName;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmain);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnFriInsert = findViewById(R.id.btnFriInsert);
        btnSearch.setOnClickListener(this);
        btnFriInsert.setOnClickListener(this);
        txtSearchName = findViewById(R.id.txtSearchName);
        txtSearchEmail = findViewById(R.id.txtSearchEmail);
        userList = findViewById(R.id.lstUser);
        roomList = findViewById(R.id.lstRoom);

        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        tabSpec = tabHost.newTabSpec("친구목록").setIndicator("친구목록").setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("메시지").setIndicator("메시지").setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("친구추가").setIndicator("친구추가").setContent(R.id.tab3);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
        mFirebase = FirebaseDatabase.getInstance();

        tabHost.getTabWidget().getChildAt(1).setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // 전달된 데이터 가져오기
            loginUID = extras.getString("UID");
        }

        //유저 목록 출력
        getUserList();

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomName = tempList.get(position).getName();
                isChatRoom(tempList.get(position).getUid());
            }
        });
    }


    @Override
    public void onClick(View v){
        if(v == btnSearch){
            getSearchUser(edtSearch.getText().toString());
        }
        if(v == btnFriInsert){
            friendsListInsert(txtSearchEmail.getText().toString());
        }
        if(v == tabHost.getTabWidget().getChildAt(1)){

        }
    }

    public void goChatRoom(String otherUID, String roomKey){
        Intent chatIntent = new Intent(AppMainActivity.this, ChatActivity.class);
        chatIntent.putExtra("loginUID", loginUID);
        chatIntent.putExtra("otherUID", otherUID);
        chatIntent.putExtra("roomKey", roomKey);
        chatIntent.putExtra("roomName", roomName);
        startActivity(chatIntent);
    }
    public void isChatRoom(String otherUID){
        mDatabase = mFirebase.getReference("chatRoom");
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.e("isChatRoom() :", "DB 연동실패");
                }else{
                    String roomKey;
                    HashMap<String, Object> map = (HashMap<String, Object>) task.getResult().child("chatRooms").getValue();
                    if(map.containsKey("users@"+loginUID+"@"+otherUID)){
                        roomKey = (String) map.get("users@"+loginUID+"@"+otherUID);
                    }else if(map.containsKey("users@"+otherUID+"@"+loginUID)){
                        roomKey = (String) map.get("users@"+otherUID+"@"+loginUID);
                    }else{
                        UUID uid = UUID.randomUUID();
                        Map<String, Object> chatUidSet = new HashMap<>();
                        roomKey = uid.toString();
                        chatUidSet.put("users@"+loginUID+"@"+otherUID,roomKey);
                        mDatabase.child("chatRooms").updateChildren(chatUidSet);
                    }
                    goChatRoom(otherUID, roomKey);
                }
            }
        });
    }

    //email을 uid로 변환
    public String emailToUUID(@NonNull Task<DataSnapshot> task, String searchEmail){
        HashMap<String, HashMap<String, Object>> userMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("users").getValue();
        String[] uuidList = userMap.keySet().toArray(new String[0]);
        String searchUID = null;

        //검색한 이메일의 User UID 찾기
        for(String userUID : uuidList){
            // User UID 탐색중에 로그인한 UID와 같다면 패스
            if(loginUID.equals(userUID.trim())) continue;

            // 검색한 Email과 찾은 Email이 같다면
            String targetEmail = userMap.get(userUID.trim()).get("email").toString();
            if(searchEmail.equals(targetEmail)){
                searchUID = userUID.trim();
                break;
            }
        }
        return searchUID;
    }

    // 친구추가 버튼클릭시
    public void friendsListInsert(String insertEmail){
        Log.d("method :", "friendsListInsert 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d("friendsListInsert :", "DB 연동실패");
                }
                else {
                    Log.d("friendsListInsert :", "DB 연동성공");

                    String searchUID = emailToUUID(task, insertEmail);
                    HashMap<String, Object> updateMap = new HashMap<>();
                    if(task.getResult().child("friends").getChildrenCount() == 0){
                        updateMap.put(searchUID,true);
                        Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                        mFirebase.getReference().child("friends").child(loginUID).setValue(updateMap);
                        return;
                    }

                    if(task.getResult().child("friends").child(loginUID).getChildrenCount() == 0){
                        updateMap.put(searchUID,true);
                        Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                        mFirebase.getReference().child("friends").child(loginUID).setValue(updateMap);
                        return;
                    }
                    HashMap<String, HashMap<String, Object>> friendsMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("friends").getValue();
                    String[] fuidArray = friendsMap.get(loginUID).keySet().toArray(new String[0]);
                    if(fuidArray.length != 0) {
                        for (String fuid : fuidArray) {
                            if (searchUID.equals(fuid)) {
                                Log.d("friendsListInsert :", "해당 유저와 이미 친구 사이입니다.");
                                return;
                            }
                        }
                    }
                    updateMap.put(searchUID, true);
                    mFirebase.getReference().child("friends").child(loginUID).updateChildren(updateMap);
                    Log.d("friendsListInsert :", insertEmail+"("+searchUID+") 이 성공적으로 갱신되었습니다.");
                }
            }
        });
        Log.d("method :", "friendsListInsert 메소드 종료");
    }

    // 이메일 검색시
    public void getSearchUser(String searchEmail){
        Log.d("method :", "getSearchUser 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    System.out.println("실패");
                    Log.d("getSearchUser :", "DB 연동실패");
                }
                else {
                    Log.d("getSearchUser :", "DB 연동성공");
                    String searchUID = emailToUUID(task, searchEmail);
                    if(searchUID == null){
                        txtSearchEmail.setText("");
                        txtSearchName.setText("");
                        btnFriInsert.setVisibility(View.INVISIBLE);
                        Log.d("getSearchUser :", "검색결과가 없습니다.");
                        return;
                    }

                    HashMap<String, HashMap<String, Object>> userMap = (HashMap<String, HashMap<String, Object>>) task.getResult().child("users").getValue();
                    txtSearchName.setText(userMap.get(searchUID).get("name").toString());
                    txtSearchEmail.setText(userMap.get(searchUID).get("email").toString());
                    btnFriInsert.setVisibility(View.VISIBLE);
                    Log.d("getSearchUser :", "검색결과(name:"+userMap.get(searchUID).get("name")+"|email:"+userMap.get(searchUID).get("email")+")");
                }
            }
        });
        Log.d("method :", "getSearchUser 메소드 종료");
    }
    public void getUserList(){
        tempList = new ArrayList<>();
        Log.d("method :", "getUserList 메소드 실행");
        mDatabase = mFirebase.getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("getUserList :", "데이터 조회 시작");
                List<String> list = new ArrayList<>();
                List<String> friendsList = new ArrayList<>();
                String friendsKey;

                Log.d("getUserList :", "친구 관계 조회");

                if(dataSnapshot.child("friends").getChildrenCount() == 0) return;
                for(DataSnapshot postSnapshot: dataSnapshot.child("friends").getChildren()){
                    //친구관계 Key -> (Key -> 친구UID)
                    friendsKey = postSnapshot.getKey().trim();

                    Map<String, Boolean> map = (HashMap<String, Boolean>) postSnapshot.getValue();
                    String[] friendsUID = map.keySet().toArray(new String[0]);
                    //로그인한 사람 UID랑 똑같은 키를 찾아서 저장
                    if(friendsKey.equals(loginUID)){
                        for(String uid : friendsUID){
                            friendsList.add(uid);
                        }
                    }
                }

                Log.d("getUserList :", "친구 리스트 구현");
                //친구목록
                if(friendsList.size() == 0) return;
                for(DataSnapshot postSnapshot: dataSnapshot.child("users").getChildren()){
                    friendsKey = postSnapshot.getKey();
                    int keyIndex = friendsList.indexOf(friendsKey);
                    if(keyIndex != -1){
                        HashMap<String,String> map = (HashMap<String, String>) postSnapshot.getValue();
                        tempList.add(new User(map.get("name"),map.get("email"), friendsKey));
                        list.add(map.get("name"));
                    }
                }
                adpater = new ArrayAdapter<String>(AppMainActivity.this, android.R.layout.simple_list_item_1, list);
                userList.setAdapter(adpater);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.d("method :", "getUserList 메소드 종료");
    }


}

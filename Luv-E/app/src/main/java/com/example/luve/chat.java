package com.example.luve;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class chat extends AppCompatActivity {

    DatabaseReference reference, typing_ref;
    FirebaseUser user;
    String chat_user, room;
    EditText message;
    RecyclerView conversation;
    Button send_msg;
    CircleImageView chat_user_profile;
    TextView chat_user_name, chat_typing;
    final ArrayList<String> from = new ArrayList<>();
    final ArrayList<String> con = new ArrayList<>();
    final ArrayList<String> con_seen = new ArrayList<>();
    final ArrayList<String> con_key = new ArrayList<>();
    private conAdapter mAdapter;
    private ValueEventListener typing_listener;
    private ChildEventListener childEventListener;
    private boolean typing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();


        RelativeLayout relativeLayout = findViewById(R.id.chat_main_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(500);
        animationDrawable.start();


        chat_user_name = findViewById(R.id.chat_user_name);
        chat_user_profile = findViewById(R.id.chat_user_profile);
        chat_typing = findViewById(R.id.typing);
        message = findViewById(R.id.message);
        send_msg = findViewById(R.id.send_msg);
        conversation = findViewById(R.id.conversation);
        conversation.setHasFixedSize(true);

        message.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    showTyping(true);
                }
                timer.cancel();
                timer = new Timer();
                long TYPING_DELAY = 900;
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                showTyping(false);
                            }
                        },
                        TYPING_DELAY
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        conversation.setLayoutManager(layoutManager);
        conversation.setHasFixedSize(true);


        chat_user = getIntent().getStringExtra("User");
        chat_user_name.setText(getIntent().getStringExtra("User_name"));

        if (Objects.requireNonNull(getIntent().getExtras()).containsKey("User_profile")) {
            Glide.with(this).load(getIntent().getStringExtra("User_profile")).into(chat_user_profile);
        }


        String string = user.getUid() + chat_user;
        char[] chars = string.toCharArray();
        Arrays.sort(chars);
        room = String.valueOf(chars);


        reference = FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("con");
        typing_ref = FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("typing");


        FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("in").child(user.getUid()).setValue(true);

        final String finalString = room;
        FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("participants")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            FirebaseDatabase.getInstance().getReference().child("chats").child(finalString).child("participants").push().setValue(user.getUid());
                            FirebaseDatabase.getInstance().getReference().child("chats").child(finalString).child("participants").push().setValue(chat_user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(message.getText().toString().trim())) {
                    reference.push().child(user.getUid()).child("msg").setValue(message.getText().toString().trim());
                    message.getText().clear();
                }
            }
        });

        getCon();
    }

    private void showTyping(boolean typing) {
        typing_ref.child(user.getUid()).setValue(typing);
    }

    private void getCon() {
        mAdapter = new conAdapter(chat.this, from, con, con_seen, con_key, reference);
        conversation.setAdapter(mAdapter);

        childEventListener = reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                con_key.add(dataSnapshot.getKey());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    from.add(snapshot.getKey());
                    con.add(String.valueOf(snapshot.child("msg").getValue()));
                    con_seen.add(String.valueOf(snapshot.child("seen").getValue()));
                    if (Objects.requireNonNull(snapshot.getKey()).equals(chat_user)) {
                        reference.child(Objects.requireNonNull(dataSnapshot.getKey())).child(snapshot.getKey()).child("seen").setValue(true);
                    }
                }
                mAdapter.notifyDataSetChanged();
                conversation.scrollToPosition(con.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                int index = con_key.indexOf(dataSnapshot.getKey());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    con_seen.add(index, String.valueOf(snapshot.child("seen").getValue()));
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TEST", "onChildRemoved: " + dataSnapshot);
                int index = con_key.indexOf(dataSnapshot.getKey());
                con_key.remove(index);
                from.remove(index);
                con.remove(index);
                con_seen.remove(index);
                mAdapter.notifyItemRemoved(index);
                mAdapter.notifyItemRangeChanged(index, con_key.size());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        typing_listener = typing_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(chat_user).exists())
                    typing = (boolean) dataSnapshot.child(chat_user).getValue();

                if (typing) {
                    chat_typing.setVisibility(View.VISIBLE);
                } else
                    chat_typing.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(childEventListener);
        typing_ref.removeEventListener(typing_listener);
    }

    @Override
    protected void onPause() {
        FirebaseDatabase.getInstance().getReference().child("chats").child(room).child("in").child(user.getUid()).setValue(false);
        super.onPause();
    }
}
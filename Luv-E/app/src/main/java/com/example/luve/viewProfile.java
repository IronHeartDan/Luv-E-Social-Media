package com.example.luve;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class viewProfile extends AppCompatActivity implements View.OnClickListener {


    FirebaseUser user;
    String person_id, remove_key, remove_fkey, remove_reqkey;
    ImageView view_profile_pic;
    TextView view_display_name;
    DatabaseReference F_reference, R_reference;
    Button action;
    ValueEventListener F_valueEventListener, A_valueEventListener;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);


        Toolbar toolbar = findViewById(R.id.view_profile_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        user = FirebaseAuth.getInstance().getCurrentUser();
        view_profile_pic = findViewById(R.id.view_profile_pic);
        view_display_name = findViewById(R.id.view_display_name);
        action = findViewById(R.id.action);

        person_id = (String) Objects.requireNonNull(getIntent().getExtras()).get("Id");

        if (getIntent().getExtras().get("Profile_pic") != null) {
            Glide.with(this).load(getIntent().getExtras().get("Profile_pic"))
                    .into(view_profile_pic);
        }

        view_display_name.setText((String) getIntent().getExtras().get("Display_name"));
        getSupportActionBar().setTitle((String) getIntent().getExtras().get("User_name"));

        F_reference = FirebaseDatabase.getInstance().getReference().child("friends");
        R_reference = FirebaseDatabase.getInstance().getReference().child("requests");

        F_valueEventListener = F_reference.child(user.getUid()).orderByValue().equalTo((String) getIntent().getExtras().get("Id"))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                remove_fkey = snapshot.getKey();
                            }
                            action.setTextColor(getColor(R.color.clear));
                            action.setText("Remove");
                            action.setTag(1);
                        } else {
                            Query query = R_reference.child((String) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("Id"))).orderByValue().equalTo(user.getUid());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            remove_key = snapshot.getKey();
                                        }
                                        action.setTextColor(getColor(R.color.colorAccent));
                                        action.setText("Requested");
                                        action.setTag(2);
                                    } else {
                                        A_valueEventListener = R_reference.child(user.getUid()).orderByValue().equalTo((String) getIntent().getExtras().get("Id"))
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                remove_reqkey = snapshot.getKey();
                                                            }
                                                            action.setTextColor(getColor(R.color.colorAccent));
                                                            action.setText("Accept");
                                                            action.setTag(4);
                                                        } else {
                                                            action.setTextColor(getColor(R.color.colorAccent));
                                                            action.setText("Add Friend");
                                                            action.setTag(3);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        action.setOnClickListener(viewProfile.this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action) {
            switch ((int) v.getTag()) {
                case 1:
                    action.setEnabled(false);
                    F_reference.child(user.getUid()).child(remove_fkey).removeValue();
                    F_reference.child((String) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("Id"))).orderByValue().equalTo(user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        F_reference.child((String) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("Id"))).child(Objects.requireNonNull(snapshot.getKey())).removeValue();
                                    }
                                    action.setEnabled(true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    action.setTag(3);
                    action.setTextColor(getColor(R.color.colorAccent));
                    action.setText("Add Friend");
                    break;
                case 2:
                    R_reference.child((String) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("Id"))).child(remove_key)
                            .removeValue();
                    action.setTag(3);
                    action.setTextColor(getColor(R.color.colorAccent));
                    action.setText("Add Friend");
                    break;

                case 3:
                    action.setEnabled(false);
                    R_reference.child((String) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("Id")))
                            .push().setValue(user.getUid());
                    Query query = R_reference.child((String) Objects.requireNonNull(getIntent().getExtras().get("Id"))).orderByValue().equalTo(user.getUid());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                remove_key = snapshot.getKey();
                            }
                            action.setEnabled(true);
                            action.setTag(2);
                            action.setTextColor(getColor(R.color.colorAccent));
                            action.setText("Requested");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    break;

                case 4:
                    action.setEnabled(false);
                    R_reference.child(user.getUid()).child(remove_reqkey)
                            .removeValue();
                    F_reference.child(user.getUid()).push()
                            .setValue(Objects.requireNonNull(getIntent().getExtras()).get("Id"));
                    F_reference.child((String) Objects.requireNonNull(getIntent().getExtras().get("Id"))).push().setValue(user.getUid());
                    action.setEnabled(true);
                    action.setTag(1);
                    action.setTextColor(getColor(R.color.clear));
                    action.setText("Remove");
                    break;
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
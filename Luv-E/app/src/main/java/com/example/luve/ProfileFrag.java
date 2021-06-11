package com.example.luve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFrag extends Fragment {
    ImageView profile_pic_view;
    TextView display_name_view, user_name_view;
    String user_name, display_name, profile_pic;
    FirebaseUser user;
    DatabaseReference reference;
    ValueEventListener valueEventListener;

    public ProfileFrag() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profile_pic_view = view.findViewById(R.id.profile_pic);
        display_name_view = view.findViewById(R.id.display_name);
        user_name_view = view.findViewById(R.id.user_name);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance()
                .getReference().child("users").child(user.getUid());
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_name = (String) dataSnapshot.child("User_name").getValue();
                display_name = (String) dataSnapshot.child("Display_name").getValue();
                profile_pic = (String) dataSnapshot.child("Profile_pic").getValue();
                setData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void setData() {
        Glide.with(Objects.requireNonNull(getContext())).load(profile_pic).placeholder(R.drawable.ic_account_circle_black_24dp).into(profile_pic_view);
        display_name_view.setText(display_name);
        user_name_view.setText(user_name);
    }


    @Override
    public void onDestroy() {
        reference.removeEventListener(valueEventListener);
        super.onDestroy();
    }
}
package com.example.luve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class notification extends Fragment {

    private DatabaseReference reference;
    private final ArrayList<String> noti_user_name = new ArrayList<>();
    private final ArrayList<String> noti_display_name = new ArrayList<>();
    private final ArrayList<String> noti_profile_pic = new ArrayList<>();
    private final ArrayList<String> noti_id = new ArrayList<>();
    private final ArrayList<String> keys = new ArrayList<>();
    private ChildEventListener childEventListener;
    private notiAdapt adapter;

    public notification() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);


        final RecyclerView listView = view.findViewById(R.id.notification);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        assert user != null;
        adapter = new notiAdapt(getActivity(), noti_user_name, noti_display_name, noti_profile_pic, user.getUid(), keys, noti_id);
        listView.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference().child("requests").child(user.getUid());


        childEventListener = reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                keys.add(0, dataSnapshot.getKey());
                noti_id.add(0, (String) dataSnapshot.getValue());
                getDetails((String) dataSnapshot.getValue());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int index = noti_id.indexOf(dataSnapshot.getValue());
                noti_user_name.remove(index);
                noti_display_name.remove(index);
                noti_profile_pic.remove(index);
                keys.remove(index);
                noti_id.remove(index);
                adapter.notifyItemRemoved(index);
                adapter.notifyItemRangeChanged(index, noti_id.size());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void getDetails(String ofUser) {
        FirebaseDatabase.getInstance().getReference().child("users").child(ofUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        noti_user_name.add(0, (String) dataSnapshot.child("User_name").getValue());
                        noti_display_name.add(0, (String) dataSnapshot.child("Display_name").getValue());
                        noti_profile_pic.add(0, (String) dataSnapshot.child("Profile_pic").getValue());
                        adapter.notifyItemInserted(0);
                        adapter.notifyItemRangeChanged(0, noti_id.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public void onDestroy() {
        reference.removeEventListener(childEventListener);
        super.onDestroy();
    }
}
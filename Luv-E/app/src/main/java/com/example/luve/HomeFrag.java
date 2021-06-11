package com.example.luve;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFrag extends Fragment {

    RecyclerView listView;
    final ArrayList<String> ppl_username = new ArrayList<>();
    final ArrayList<String> ppl_displayname = new ArrayList<>();
    final ArrayList<String> ppl_profilepic = new ArrayList<>();
    final ArrayList<String> ppl_id = new ArrayList<>();
    final ArrayList<String> friends = new ArrayList<>();
    final ArrayList<Boolean> friends_friend = new ArrayList<>();
    pplAdapt adapter;
    ProgressBar loading_ppl;
    ValueEventListener valueEventListener, listener;
    private FirebaseUser user;
    private DatabaseReference reference;

    public HomeFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        loading_ppl = view.findViewById(R.id.loading_ppl);
        listView = view.findViewById(R.id.showPPL);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new pplAdapt(getActivity(), ppl_username, ppl_displayname, ppl_profilepic, ppl_id, friends_friend);
        listView.setAdapter(adapter);


        valueEventListener = reference.child("friends").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friends.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friends.add(Objects.requireNonNull(snapshot.getValue()).toString());
                    }

                }
                Show_Friends(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }


    public void Show_Friends(final ArrayList<String> friends) {
        ppl_username.clear();
        ppl_displayname.clear();
        ppl_profilepic.clear();
        ppl_id.clear();
        friends_friend.clear();
        for (int i = 0; i < friends.size(); i++) {
            reference.child("users").child(friends.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    ppl_id.add(dataSnapshot1.getKey());
                    ppl_username.add((String) dataSnapshot1.child("User_name").getValue());
                    ppl_displayname.add((String) dataSnapshot1.child("Display_name").getValue());
                    ppl_profilepic.add((String) dataSnapshot1.child("Profile_pic").getValue());
                    friends_friend.add(true);
                    adapter.notifyDataSetChanged();
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    public void search(final String search) {
        loading_ppl.setVisibility(View.VISIBLE);
        reference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ppl_username.clear();
                ppl_displayname.clear();
                ppl_profilepic.clear();
                ppl_id.clear();
                friends_friend.clear();
                int limit = 0;
                String check;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    check = (String) snapshot.child("User_name").getValue();
                    if (!user.getUid().equals(snapshot.getKey())) {
                        assert check != null;
                        if (check.contains(search)) {
                            ppl_id.add(snapshot.getKey());
                            ppl_username.add((String) snapshot.child("User_name").getValue());
                            ppl_displayname.add((String) snapshot.child("Display_name").getValue());
                            ppl_profilepic.add((String) snapshot.child("Profile_pic").getValue());
                            friends_friend.add(false);
                            limit++;

                            if (limit == 5) {
                                break;
                            }
                        }
                    }
                }
                loading_ppl.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onDestroy() {
        reference.child("friends").child(user.getUid()).removeEventListener(valueEventListener);
        super.onDestroy();
    }
}
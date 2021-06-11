package com.example.luve;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    FirebaseAuth mAuth;
    ImageView main_pic;
    ViewPager2 viewPager;
    EditText search_ppl;
    SearchView searchView;
    BottomNavigationView bottomNavigationView;
    ValueEventListener valueEventListener;
    DatabaseReference reference;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.main_tool_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);


        bottomNavigationView = findViewById(R.id.btm_nav);
        searchView = findViewById(R.id.search_ppl);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.show_home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.show_notification:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.edit_profile:
                        startActivity(new Intent(MainActivity.this, upload_details.class));

                }
                return false;
            }
        });

        main_pic = findViewById(R.id.main_pic);
        Glide.with(this).load(R.drawable.ic_account_circle_black_24dp).into(main_pic);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                HomeFrag frag = (HomeFrag) getSupportFragmentManager().getFragments().get(2);
                frag.Show_Friends(frag.friends);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText) && newText.length() > 0 && !(newText.contains(" "))) {
                    HomeFrag frag = (HomeFrag) getSupportFragmentManager().getFragments().get(2);
                    frag.search(newText.toLowerCase().trim());
                } else if (!(newText.contains(" ")) && newText.length() == 0) {
                    HomeFrag frag = (HomeFrag) getSupportFragmentManager().getFragments().get(2);
                    frag.Show_Friends(frag.friends);
                }
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(main_pic);
            }


        }


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        valueEventListener = reference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Intent intent;
                        if (!dataSnapshot.exists()) {
                            intent = new Intent(MainActivity.this, upload_details.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        } else {
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w("TEST", "getInstanceId failed", task.getException());
                                                return;
                                            }

                                            // Get new Instance ID token
                                            String token = Objects.requireNonNull(task.getResult()).getToken();
                                            //Upload To Server
                                            FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getUid())).child("Token").setValue(token);
                                        }
                                    });
                            Glide.with(getApplicationContext()).load(mAuth.getCurrentUser().getPhotoUrl())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(main_pic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
        FragmentStateAdapter adapter = new pageAdapterUser(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);


        if (getIntent().getExtras() != null) {
            viewPager.setCurrentItem(1);
        }

//        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                boolean connected = snapshot.getValue(Boolean.class);
//                if (connected) {
//                    Log.d("TEST", "onDataChange: Connected");
//                } else {
//                    Log.d("TEST", "onDataChange: Disconected");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                System.err.println("Listener was cancelled");
//            }
//        });


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.logout:

                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent home = new Intent(MainActivity.this, SignIn.class);
                            startActivity(home);
                            finishAffinity();
                        }
                    }
                });

                break;
            case R.id.main_pic:
                viewPager.setCurrentItem(2);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(search_ppl.getApplicationWindowToken(), 0);
        search_ppl.clearFocus();
        return true;
    }

    public static class pageAdapterUser extends FragmentStateAdapter {
        public pageAdapterUser(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);

        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new HomeFrag();
                    break;
                case 1:
                    fragment = new notification();
                    break;
                case 2:
                    fragment = new ProfileFrag();
                    break;
            }

            assert fragment != null;
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
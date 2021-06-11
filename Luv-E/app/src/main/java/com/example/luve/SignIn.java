package com.example.luve;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    FirebaseAuth mAuth;
    ProgressBar loading;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);


        RelativeLayout relativeLayout = findViewById(R.id.sign_in_background);
        AnimationDrawable animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(500);
        animationDrawable.start();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("users");
        loading = findViewById(R.id.loading);
        findViewById(R.id.gsign).setOnClickListener(this);
        findViewById(R.id.gsign_up).setOnClickListener(this);
        findViewById(R.id.change_layout).setOnClickListener(this);
        findViewById(R.id.signIn).setOnClickListener(this);
        findViewById(R.id.signUp).setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    private void GSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                loading.setVisibility(View.GONE);

                findViewById(R.id.gsign).setEnabled(true);
                findViewById(R.id.gsign_up).setEnabled(true);
                findViewById(R.id.signIn).setEnabled(true);
                findViewById(R.id.signUp).setEnabled(true);
                findViewById(R.id.change_layout).setEnabled(true);
                Toast.makeText(SignIn.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // [END onactivityresult]

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            assert user != null;
                            reference.child(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int check = 0;
                                            if (dataSnapshot.exists()) {
                                                check = 1;
                                            }
                                            Intent intent = new Intent(SignIn.this, MainActivity.class);
                                            startActivity(intent);
                                            if (check == 0) {
                                                intent = new Intent(SignIn.this, upload_details.class);
                                                startActivity(intent);
                                            }
                                            finishAffinity();
                                            loading.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        } else {
                            findViewById(R.id.gsign).setEnabled(true);
                            findViewById(R.id.gsign_up).setEnabled(true);
                            findViewById(R.id.signIn).setEnabled(true);
                            findViewById(R.id.signUp).setEnabled(true);
                            findViewById(R.id.change_layout).setEnabled(true);
                            Toast.makeText(SignIn.this, task.getResult() + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // [END auth_with_google]

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gsign:

            case R.id.gsign_up:
                v.setEnabled(false);
                findViewById(R.id.signIn).setEnabled(false);
                findViewById(R.id.signUp).setEnabled(false);
                findViewById(R.id.change_layout).setEnabled(false);
                GSignIn();
                loading.setVisibility(View.VISIBLE);
                break;

            case R.id.change_layout:
                Button button = findViewById(R.id.change_layout);
                if (v.getTag().equals("0")) {
                    findViewById(R.id.imageView2).setBackgroundResource(R.drawable.sign_up_font);
                    v.setTag("1");
                    button.setText("Already A User? Sign In");

                    findViewById(R.id.sign_in_layout).setVisibility(View.GONE);
                    findViewById(R.id.sign_up_layout).setVisibility(View.VISIBLE);
                } else {
                    v.setTag("0");
                    button.setText("Need New Account? Sign Up");
                    findViewById(R.id.imageView2).setBackgroundResource(R.drawable.log_in_font);
                    findViewById(R.id.sign_up_layout).setVisibility(View.GONE);
                    findViewById(R.id.sign_in_layout).setVisibility(View.VISIBLE);
                }
                break;


            case R.id.signUp:
                EditText editText1, editText2;
                editText1 = findViewById(R.id.sign_up_email);
                editText2 = findViewById(R.id.sign_up_pass);

                if (!TextUtils.isEmpty(editText1.getText().toString().trim()) && !TextUtils.isEmpty(editText2.getText().toString().trim())) {
                    findViewById(R.id.signUp).setEnabled(false);
                    findViewById(R.id.gsign_up).setEnabled(false);
                    findViewById(R.id.change_layout).setEnabled(false);
                    loading.setVisibility(View.VISIBLE);
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(editText1.getText().toString().trim(), editText2.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loading.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                                        startActivity(intent);
                                        intent = new Intent(SignIn.this, upload_details.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    }else{
                                        findViewById(R.id.gsign_up).setEnabled(true);
                                        findViewById(R.id.signUp).setEnabled(true);
                                        findViewById(R.id.change_layout).setEnabled(true);
                                        Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else
                    Toast.makeText(SignIn.this, "Empty Fields", Toast.LENGTH_SHORT).show();

                break;

            case R.id.signIn:
                EditText editText3, editText4;
                editText3 = findViewById(R.id.sign_in_email);
                editText4 = findViewById(R.id.sign_in_pass);

                if (!TextUtils.isEmpty(editText3.getText().toString().trim()) && !TextUtils.isEmpty(editText4.getText().toString().trim())) {
                    findViewById(R.id.signIn).setEnabled(false);
                    findViewById(R.id.gsign).setEnabled(false);
                    findViewById(R.id.change_layout).setEnabled(false);
                    loading.setVisibility(View.VISIBLE);
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(editText3.getText().toString().trim(), editText4.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loading.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    }else{
                                        findViewById(R.id.gsign).setEnabled(true);
                                        findViewById(R.id.signIn).setEnabled(true);
                                        findViewById(R.id.change_layout).setEnabled(true);
                                        Toast.makeText(SignIn.this, Objects.requireNonNull(task.getException()).getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else
                    Toast.makeText(SignIn.this, "Empty Fields", Toast.LENGTH_SHORT).show();

                break;
        }


    }

}

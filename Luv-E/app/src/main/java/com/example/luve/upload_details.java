package com.example.luve;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class upload_details extends AppCompatActivity implements View.OnClickListener {

    private static final int OKAY = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7;
    RelativeLayout edit_profile_pic;
    Button save;
    EditText edit_user_name, edit_display_name;
    TextView validation;
    ImageView preview_pic, rotate_right, rotate_left;
    String user_name, display_name;
    ProgressBar progressBar, load_save;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    StorageReference storage;
    Uri uri = null;
    Bitmap bitmap = null;
    Matrix mMatrix, mat;
    String org_username;
    Boolean upload = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_details);

        Toolbar toolbar = findViewById(R.id.upload_details_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload or Edit Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        edit_profile_pic = findViewById(R.id.edit_profile_pic);
        save = findViewById(R.id.save);
        edit_user_name = findViewById(R.id.edit_user_name);
        edit_display_name = findViewById(R.id.edit_display_name);
        preview_pic = findViewById(R.id.preview_pic);
        progressBar = findViewById(R.id.load_image);
        load_save = findViewById(R.id.load_save);
        validation = findViewById(R.id.validation);
        rotate_left = findViewById(R.id.rotate_left);
        rotate_right = findViewById(R.id.rotate_right);

        rotate_right.setOnClickListener(this);
        rotate_left.setOnClickListener(this);


        edit_profile_pic.setOnClickListener(this);
        save.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");


        edit_display_name.setText(user.getDisplayName());

        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(preview_pic);
        } else {
            Glide.with(this).load(R.drawable.ic_account_circle_black_24dp)
                    .into(preview_pic);
        }
        databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    org_username = (String) dataSnapshot.child("User_name").getValue();
                    edit_user_name.setText(org_username);
                    upload = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        edit_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                user_name = edit_user_name.getText().toString().trim().toLowerCase();
                if (!user_name.equals(org_username)) {
                    checkUserName();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_profile_pic:
                progressBar.setVisibility(View.VISIBLE);

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        new AlertDialog.Builder(this)
                                .setTitle("Permission Required")
                                .setMessage("Storage Permission is required to change Profile Pic")
                                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(upload_details.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                    }
                                })
                                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressBar.setVisibility(View.GONE);
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();

                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }


                } else {
                    Intent gallery = new Intent(Intent.ACTION_PICK);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, OKAY);
                }
                break;

            case R.id.save:
                user_name = edit_user_name.getText().toString().trim().toLowerCase();
                display_name = edit_display_name.getText().toString().trim();
                if (!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(display_name) && TextUtils.isEmpty(validation.getText().toString())) {
                    checkUri();
                    save.setVisibility(View.GONE);
                } else
                    Toast.makeText(upload_details.this, "Fill Details", Toast.LENGTH_SHORT).show();
                break;

            case R.id.rotate_left:
                mMatrix = new Matrix();
                mat = preview_pic.getImageMatrix();
                mMatrix.set(mat);
                mMatrix.setRotate(-90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), mMatrix, false);
                preview_pic.setImageBitmap(bitmap);
                break;

            case R.id.rotate_right:
                mMatrix = new Matrix();
                mat = preview_pic.getImageMatrix();
                mMatrix.set(mat);
                mMatrix.setRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), mMatrix, false);
                preview_pic.setImageBitmap(bitmap);
                break;
        }
    }

    private void checkUri() {
        load_save.setVisibility(View.VISIBLE);
        if (uri != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();
            final StorageReference storageReference = storage.child("profile_pic/" + user.getUid() + ".jpg");
            storageReference.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                uri = task.getResult();
                                saveChanges();
                            }
                        });
                    }
                }
            });

        } else {
            uri = user.getPhotoUrl();
            saveChanges();
        }
    }

    private void checkUserName() {
        databaseReference.orderByChild("User_name").equalTo(user_name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int check = 0;
                        if (dataSnapshot.getValue() != null) {
                            check = 1;
                        }

                        if (check == 1) {
                            validation.setText("UserName Not Available");
                            load_save.setVisibility(View.GONE);
                        } else {
                            validation.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void saveChanges() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .setDisplayName(display_name)
                .build();

        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("User_name", user_name);
                hashMap.put("Display_name", display_name);
                hashMap.put("Profile_pic", String.valueOf(uri));

                databaseReference.child(user.getUid()).setValue(hashMap);
                upload = true;
                load_save.setVisibility(View.GONE);
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OKAY && resultCode == RESULT_OK) {
            assert data != null;
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            preview_pic.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
            rotate_left.setVisibility(View.VISIBLE);
            rotate_right.setVisibility(View.VISIBLE);

        } else
            progressBar.setVisibility(View.GONE);
    }


    @Override
    public void onBackPressed() {
        if (upload) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                startActivityForResult(gallery, OKAY);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
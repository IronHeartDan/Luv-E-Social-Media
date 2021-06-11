package com.example.luve;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class notiAdapt extends RecyclerView.Adapter<notiAdapt.PlaceHolder> implements View.OnClickListener {

    private final FirebaseUser user;
    private final DatabaseReference Accepto_reference;
    private final DatabaseReference Same_reference;
    private final DatabaseReference Deleto_reference;
    private final Activity activity;
    private final ArrayList<String> user_name;
    private final ArrayList<String> display_name;
    private final ArrayList<String> profile_pic;
    private final ArrayList<String> keys;
    private final ArrayList<String> ids;

    notiAdapt(Activity activity, ArrayList<String> user_name, ArrayList<String> display_name
            , ArrayList<String> profile_pic, String refName, ArrayList<String> keys, ArrayList<String> ids) {
        this.activity = activity;
        this.user_name = user_name;
        this.display_name = display_name;
        this.profile_pic = profile_pic;
        this.keys = keys;
        this.ids = ids;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.Deleto_reference = FirebaseDatabase.getInstance().getReference().child("requests").child(refName);
        this.Accepto_reference = FirebaseDatabase.getInstance().getReference().child("friends").child(refName);
        this.Same_reference = FirebaseDatabase.getInstance().getReference().child("friends");
    }


    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification, parent, false);
        return new PlaceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder placeHolder, int position) {
        placeHolder.click_notification.setTag(position);
        placeHolder.noti_user_name.setText(user_name.get(position));
        placeHolder.noti_display_name.setText(display_name.get(position));
        int flag = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (flag) {
            case Configuration.UI_MODE_NIGHT_YES:
                Glide.with(activity).load(profile_pic.get(position)).placeholder(R.drawable.ic_account_circle_white_24dp).into(placeHolder.imageView);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                Glide.with(activity).load(profile_pic.get(position)).placeholder(R.drawable.ic_account_circle_black_24dp).into(placeHolder.imageView);
                break;
        }

        placeHolder.accept.setTag(position);
        placeHolder.delete.setTag(position);
        placeHolder.accept.setEnabled(true);
        placeHolder.delete.setEnabled(true);

        placeHolder.click_notification.setOnClickListener(notiAdapt.this);
        placeHolder.accept.setOnClickListener(notiAdapt.this);
        placeHolder.delete.setOnClickListener(notiAdapt.this);

    }

    @Override
    public int getItemCount() {
        return user_name.size();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.accept_req:
                v.setEnabled(false);
                Same_reference.child(ids.get((Integer) v.getTag()))
                        .push().setValue(user.getUid());
                Accepto_reference.push().setValue(ids.get((Integer) v.getTag())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Deleto_reference.child(keys.get((Integer) v.getTag())).removeValue();
                    }
                });
                break;

            case R.id.delete_req:
                v.setEnabled(false);
                Deleto_reference.child(keys.get((Integer) v.getTag())).removeValue();
                break;

            case R.id.click_notification:
                Intent intent = new Intent(activity, viewProfile.class);
                intent.putExtra("User_name", user_name.get((int) v.getTag()));
                intent.putExtra("Display_name", display_name.get((int) v.getTag()));
                intent.putExtra("Profile_pic", profile_pic.get((int) v.getTag()));
                intent.putExtra("Id", ids.get((int) v.getTag()));
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                break;
        }
    }

    static class PlaceHolder extends RecyclerView.ViewHolder {
        final RelativeLayout click_notification;
        final ImageView imageView;
        final TextView noti_user_name;
        final TextView noti_display_name;
        final Button accept;
        final Button delete;

        PlaceHolder(@NonNull View view) {
            super(view);
            this.click_notification = view.findViewById(R.id.click_notification);
            this.imageView = view.findViewById(R.id.noti_profile_pic);
            this.noti_user_name = view.findViewById(R.id.noti_user_name);
            this.noti_display_name = view.findViewById(R.id.noti_display_name);
            this.accept = view.findViewById(R.id.accept_req);
            this.delete = view.findViewById(R.id.delete_req);
        }
    }
}

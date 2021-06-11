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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class pplAdapt extends RecyclerView.Adapter<pplAdapt.Placeholder> implements View.OnClickListener {

    private final Activity activity;
    private final ArrayList<String> user_name;
    private final ArrayList<String> display_name;
    private final ArrayList<String> profile_pic;
    private final ArrayList<String> ids;
    private final ArrayList<Boolean> friends;

    pplAdapt(Activity activity, ArrayList<String> user_name, ArrayList<String> display_name, ArrayList<String> profile_pic,
             ArrayList<String> ids, ArrayList<Boolean> friends) {
        this.activity = activity;
        this.user_name = user_name;
        this.display_name = display_name;
        this.profile_pic = profile_pic;
        this.ids = ids;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.friends = friends;
    }

    @NonNull
    @Override
    public Placeholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ppl, parent, false);
        return new Placeholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Placeholder placeholder, int position) {
        placeholder.ppl_click.setTag(position);
        placeholder.ppl_click.setOnClickListener(this);
        placeholder.textView_username.setText(user_name.get(position));
        placeholder.textView_displayname.setText(display_name.get(position));
        int flag = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (flag) {
            case Configuration.UI_MODE_NIGHT_YES:
                Glide.with(activity).load(profile_pic.get(position)).placeholder(R.drawable.ic_account_circle_white_24dp).into(placeholder.imageView);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                Glide.with(activity).load(profile_pic.get(position)).placeholder(R.drawable.ic_account_circle_black_24dp).into(placeholder.imageView);
                break;
        }
        placeholder.button.setTag(position);

        if (friends.get(position)) {
            placeholder.button.setVisibility(View.VISIBLE);
            placeholder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(activity, chat.class);
                    chatIntent.putExtra("User", ids.get((int) v.getTag()));
                    chatIntent.putExtra("User_name", user_name.get((int) v.getTag()));
                    chatIntent.putExtra("User_profile", profile_pic.get((int) v.getTag()));
                    activity.startActivity(chatIntent);

                }
            });
        } else placeholder.button.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, viewProfile.class);
        intent.putExtra("User_name", user_name.get((int) v.getTag()));
        intent.putExtra("Display_name", display_name.get((int) v.getTag()));
        intent.putExtra("Profile_pic", profile_pic.get((int) v.getTag()));
        intent.putExtra("Id", ids.get((int) v.getTag()));
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    static class Placeholder extends RecyclerView.ViewHolder {
        final RelativeLayout ppl_click;
        final TextView textView_username;
        final TextView textView_displayname;
        final ImageView imageView;
        final Button button;

        Placeholder(@NonNull View view) {
            super(view);
            this.ppl_click = view.findViewById(R.id.ppl_click);
            this.textView_username = view.findViewById(R.id.user_name_PPl);
            this.textView_displayname = view.findViewById(R.id.display_name_PPl);
            this.imageView = view.findViewById(R.id.profile_pic_PPl);
            this.button = view.findViewById(R.id.start_bunking);
        }
    }
}

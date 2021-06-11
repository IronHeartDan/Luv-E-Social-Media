package com.example.luve;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class conAdapter extends RecyclerView.Adapter<conAdapter.PlaceHolder> {

    private final Context context;
    private final ArrayList<String> from;
    private final ArrayList<String> con;
    private final ArrayList<String> con_seen;
    private final ArrayList<String> con_key;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final DatabaseReference unsend_ref;

    public conAdapter(Context context, ArrayList<String> from, ArrayList<String> con, ArrayList<String> con_seen, ArrayList<String> con_key, DatabaseReference unsend_ref) {
        this.context = context;
        this.from = from;
        this.con = con;
        this.con_seen = con_seen;
        this.con_key = con_key;
        this.unsend_ref = unsend_ref;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PlaceHolder placeHolder;
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_mes, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_mes, parent, false);
        }
        placeHolder = new PlaceHolder(view);
        return placeHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, final int position) {
        holder.mess.setText(con.get(position));
        holder.view.setTag(con_key.get(position));
        holder.view.setId(position);

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                assert user != null;
                if (user.getUid().equals(from.get(v.getId()))) {
                    new AlertDialog.Builder(context)
                            .setTitle("Message")
                            .setMessage(con.get(v.getId()))
                            .setPositiveButton("Unsend", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unsend_ref.child(v.getTag().toString()).removeValue();
                                }
                            })
                            .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Message", con.get(v.getId()));
                                    assert clipboard != null;
                                    clipboard.setPrimaryClip(clip);
                                }
                            })
                            .create()
                            .show();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Message")
                            .setMessage(con.get(v.getId()))
                            .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Message", con.get(v.getId()));
                                    assert clipboard != null;
                                    clipboard.setPrimaryClip(clip);
                                }
                            })
                            .create()
                            .show();
                }
                return false;
            }
        });

        if (position == con.size() - 1) {
            assert user != null;
            if (from.get(position).equals(user.getUid())) {
                holder.seen.setVisibility(View.VISIBLE);
                if (con_seen.get(position).equals("true")) {
                    holder.seen.setText("Seen");
                } else {
                    holder.seen.setText("Delivered");
                }
            }
        } else {
            holder.seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return con.size();
    }

    @Override
    public int getItemViewType(int position) {
        assert user != null;
        if (from.get(position).equals(user.getUid())) {
            return 0;
        } else
            return 1;
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        final TextView mess;
        final TextView seen;
        final View view;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            this.mess = itemView.findViewById(R.id.mess);
            this.seen = itemView.findViewById(R.id.seen);
        }
    }
}

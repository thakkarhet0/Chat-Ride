package com.example.chatroom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.GlideApp;
import com.example.chatroom.R;
import com.example.chatroom.databinding.ViewerLayoutBinding;
import com.example.chatroom.models.Viewer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ViewerAdapter extends RecyclerView.Adapter<ViewerAdapter.UViewHolder> {

    HashMap<String, Viewer> viewers;
    ViewerLayoutBinding binding;
    private String[] mKeys;

    public ViewerAdapter(HashMap<String, Viewer> viewers) {
        updateData(viewers);
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewerLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UViewHolder(binding);
    }

    public void updateData(HashMap<String, Viewer> viewers) {
        this.viewers = viewers;
        mKeys = viewers.keySet().toArray(new String[viewers.size()]);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        if (mKeys.length < position + 1) return;

        Viewer viewer = viewers.get(mKeys[position]);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        if (viewer.getPhotoRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(viewer.getUid()).child(viewer.getPhotoRef());
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imageView11);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(binding.imageView11);
        }

        binding.textView6.setText(viewer.getName());

    }

    @Override
    public int getItemCount() {
        return this.viewers.size();
    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        ViewerLayoutBinding binding;

        public UViewHolder(@NonNull ViewerLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

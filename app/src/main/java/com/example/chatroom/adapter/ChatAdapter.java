package com.example.chatroom.adapter;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.GlideApp;
import com.example.chatroom.R;
import com.example.chatroom.databinding.ChatLayoutBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UViewHolder> {

    ArrayList<Chat> chats;

    Chatroom chatroom;

    ChatLayoutBinding binding;

    IChatAdapter am;

    MapView mapView;

    FirebaseFirestore db;

    public static final int MAP_HEIGHT = 500;
    MapHelper mapHelper;

    User user;

    public ChatAdapter(Chatroom chatroom, ArrayList<Chat> chats) {
        this.chatroom = chatroom;
        this.chats = chats;
        db = FirebaseFirestore.getInstance();
    }

    private void sendMap(OnMapReadyCallback callback) {
        mapView = new MapView(binding.getRoot().getContext());
        mapView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, MAP_HEIGHT));
        binding.txtwrap.addView(mapView);
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(callback);
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ChatLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        am = (IChatAdapter) parent.getContext();
        user = am.getUser();
        mapHelper = am.getMapHelper();
        return new UViewHolder(binding);
    }

    private void sendExtraInfo(String msg) {
        binding.textViewext.setVisibility(View.VISIBLE);
        binding.textViewext.setText(msg);
        binding.textViewext.setTypeface(null, Typeface.ITALIC);
    }

    private void toggleDelete(boolean show) {
        int view;
        if (show) view = View.VISIBLE;
        else view = View.GONE;
        binding.imageView3.setVisibility(view);
    }

    private void toggleLike(boolean show, boolean show_likes) {
        int view;
        if (show) view = View.VISIBLE;
        else view = View.GONE;
        binding.imageView.setVisibility(view);
        if (!show_likes)
            binding.textView9.setVisibility(View.GONE);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        Chat chat = chats.get(position);

        binding = holder.binding;

        binding.textViewext.setVisibility(View.GONE);

        if (chat.getOwnerId().equals(user.getId())) {
            binding.chatlayout.setBackgroundResource(R.drawable.outgoing_bubble);
        }

        binding.textView7.setText(chat.getOwnerName());

        binding.textView17.setText(Utils.getPrettyTime(chat.getCreated_at()));

        if (chat.getOwnerRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwnerId()).child(chat.getOwnerRef());
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imageView2);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(binding.imageView2);
        }

        toggleDelete(false);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (chat.getChatType() == Chat.CHAT_MESSAGE) {

            binding.textView8.setText(chat.getContent());

            binding.textView9.setText(chat.getLikedBy().size() + " ♥");

            holder.liked = chat.getLikedBy().contains(user.getId());
            if (holder.liked) binding.imageView.setImageResource(R.drawable.like_favorite);

            if (chat.getOwnerId().equals(user.getId())) {
                toggleLike(false, true);
                toggleDelete(true);
            }

            binding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.liked) {
                        chat.unLike(user.getId());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                binding.imageView.setImageResource(R.drawable.like_not_favorite);
                                holder.liked = false;
                            }
                        });
                    } else {
                        chat.addLike(user.getId());
                        HashMap<String, Object> upd = new HashMap<>();
                        upd.put("likedBy", chat.getLikedBy());
                        am.toggleDialog(true);
                        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                am.toggleDialog(false);
                                binding.imageView.setImageResource(R.drawable.like_favorite);
                                holder.liked = true;
                            }
                        });
                    }
                }
            });

        } else if (chat.getChatType() == Chat.CHAT_LOCATION) {
            toggleLike(false, false);

            sendMap(googleMap -> {
                String[] loc = chat.getContent().split("\n");
                LatLng ltlg = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
                mapHelper.clearMarkers();
                mapHelper.addMarker(googleMap, ltlg, "User Location");
            });
            sendExtraInfo("Sent Location");

            if (chat.getOwnerId().equals(user.getId())) {
                toggleDelete(true);
            }

        } else if (chat.getChatType() == Chat.CHAT_RIDE_REQUEST) {
            toggleLike(false, false);
            String[] loc = chat.getContent().split("\n");

            sendMap(googleMap -> {
                LatLng ltlg = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
                LatLng ltlg2 = new LatLng(Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));
                mapHelper.clearMarkers();
                mapHelper.addMarker(googleMap, ltlg, "Rider Location");
                mapHelper.addMarker(googleMap, ltlg2, "Drop Location");
            });

            if (!chat.getOwnerId().equals(user.getId())) {
                sendExtraInfo("Requested a Ride!\nTap for more info");
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Utils.DB_CHAT, chat);
                        Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_rideDetailsFragment, bundle);
                    }
                });
            } else {
                toggleDelete(true);
                sendExtraInfo("You requested a ride in this chatroom!\nWait for offers");
            }

        } else if (chat.getChatType() == Chat.CHAT_RIDE_OFFER) {
            toggleLike(false, false);
            toggleDelete(false);

            String[] names = chat.getContent().split("\n");
            if (names[0].equals(user.getId())) {
                sendMap(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        LatLng ltlg = new LatLng(Double.parseDouble(names[3]), Double.parseDouble(names[4]));
                        mapHelper.clearMarkers();
                        mapHelper.addMarker(googleMap, ltlg, "Driver Location");
                    }
                });
                sendExtraInfo("You received a ride offer from " + names[1] + "!\nTap for more info");
                if (user.getRideReq() != null)
                    user.getRideReq().addOffer(chat.getId());
                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Utils.DB_CHAT, chat);
                        Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_rideOfferDetailFragment, bundle);
                    }
                });
            } else if (chat.getOwnerId().equals(user.getId())) {
                binding.textView8.setVisibility(View.GONE);
                toggleDelete(true);
                sendExtraInfo("You sent a ride offer to " + names[2] + "!");
            } else {
                binding.getRoot().setVisibility(View.GONE);
            }
        } else if (chat.getChatType() == Chat.CHAT_RIDE_STARTED) {
            toggleLike(false, false);
            toggleDelete(false);
            binding.textView8.setVisibility(View.GONE);

            String[] names = chat.getContent().split("\n");
            if (names[1].equals(user.getId())) {
                user.addRide(names[0]);
                sendExtraInfo("Your ride with " + names[3] + " has started!\nTap for more info");
            } else if (chat.getOwnerId().equals(user.getId())) {
                sendExtraInfo("Your drive with " + names[2] + " has started!\nTap for more info");
            } else {
                binding.getRoot().setVisibility(View.GONE);
            }
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(holder.itemView).navigate(R.id.action_chatroomFragment_to_tripListFragment);
                }
            });
        }

        binding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                am.toggleDialog(true);
                db.collection(Utils.DB_RIDE_REQ).document(chat.getId()).delete();
                db.collection(Utils.DB_RIDE_OFFER).document(chat.getId()).delete();
                DocumentReference dbc = db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(chat.getId());
                dbc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        am.toggleDialog(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(view.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.chats.size();
    }

    public interface IChatAdapter {

        void toggleDialog(boolean show);

        MapHelper getMapHelper();

        User getUser();

    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        ChatLayoutBinding binding;
        boolean liked = false;

        public UViewHolder(@NonNull ChatLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

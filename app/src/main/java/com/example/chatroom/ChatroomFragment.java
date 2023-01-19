package com.example.chatroom;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.ChatAdapter;
import com.example.chatroom.adapter.ViewerAdapter;
import com.example.chatroom.databinding.FragmentChatroomBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideOffer;
import com.example.chatroom.models.RideReq;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.example.chatroom.models.Viewer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ChatroomFragment extends Fragment {

    FragmentChatroomBinding binding;

    IChat am;

    FirebaseAuth mAuth;

    User user;

    FirebaseFirestore db;

    Chatroom chatroom;

    MapHelper mapHelper;

    RideReq rideReq;

    HashMap<String, Viewer> viewers;

    Trip trip;

    ViewerAdapter viewerAdapter;

    RideOffer rideOffer;

    NavController navController;

    @Override
    public void onStop() {
        super.onStop();
        am.actionBar(false);
        removeViewer();
        mapHelper.stopUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeRideStuff();
    }

    public void removeViewer() {
        chatroom.removeViewer(user.getId());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update("viewers", chatroom.getViewers());
    }

    public void removeRideStuff() {
        if (rideReq != null && rideReq.getMsg_id() != null) {
            user.setRideReq(null);
            db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(rideReq.getMsg_id()).delete();
            db.collection(Utils.DB_RIDE_REQ).document(rideReq.getMsg_id()).delete();
            for (String id : rideReq.getOffer_ids())
                db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(id).delete();
        }
        if (rideOffer != null && rideOffer.getMsg_id() != null) {
            user.setRideOffer(null);
            db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(rideOffer.getMsg_id()).delete();
            db.collection(Utils.DB_RIDE_OFFER).document(rideOffer.getMsg_id()).delete();
        }
        if (trip != null && trip.getMsg_id() != null) {
            db.collection(Utils.DB_TRIPS).document(trip.getId()).update("ongoing", false);
            user.setTrip(null);
            db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).document(trip.getMsg_id()).delete();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IChat) {
            am = (IChat) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        chatroom = user.getChatroom();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mapHelper = am.getMapHelper();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        am.actionBar(true);
        if (!chatroom.getViewers().containsKey(user.getId())) {
            chatroom.addViewer(user.getId(), new Viewer(user.getId(), user.getPhotoref(), user.getDisplayName()));
            db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update("viewers", chatroom.getViewers());
        }
    }

    public void updateTripDetails() {
        HashMap<String, Object> upd = new HashMap<>();
        upd.put("ongoing", false);
        upd.put("end_at", FieldValue.serverTimestamp());
        db.collection(Utils.DB_TRIPS).document(user.getTrip().getId()).update(upd);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Chatroom " + chatroom.getName());

        if (user.ride_req && user.getRideReq() != null) {
            rideReq = user.getRideReq();
            user.ride_req = false;
            sendRequestChat();
        } else if (user.ride_offer && user.getRideOffer() != null) {
            rideOffer = user.getRideOffer();
            user.ride_offer = false;
            sendOfferChat();
        } else if (user.ride_started && user.getTrip() != null) {
            trip = user.getTrip();
            user.ride_started = false;
            sendRideStartedChat();
        } else if (user.ride_finished && user.getTrip() != null && !user.getTrip().isOngoing()) {
            user.ride_finished = false;
            Toast.makeText(getActivity(), "Trip was finished!", Toast.LENGTH_SHORT).show();
            updateTripDetails();
            removeRideStuff();
            user.setTrip(null);
        } else if (user.ride_finished) {
            user.ride_finished = false;
            Toast.makeText(getActivity(), "Trip was finished!", Toast.LENGTH_SHORT).show();
        }

        am.toggleDialog(true);

        binding = FragmentChatroomBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.viewerslist.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.viewerslist.setLayoutManager(llm);
        viewerAdapter = new ViewerAdapter(new HashMap<>());
        binding.viewerslist.setAdapter(viewerAdapter);

        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                chatroom.setViewers(new HashMap<>());
                HashMap<String, HashMap> temp = (HashMap<String, HashMap>) value.get("viewers");
                if (temp == null) temp = new HashMap<>();
                for (Map.Entry<String, HashMap> entry :
                        temp.entrySet()) {
                    Viewer viewer = new Viewer();
                    String key = entry.getKey();
                    HashMap val = entry.getValue();
                    viewer.setName((String) val.get("name"));
                    viewer.setPhotoRef((String) val.get("photoRef"));
                    viewer.setUid((String) val.get("uid"));
                    chatroom.addViewer(key, viewer);
                }
                viewerAdapter.updateData(chatroom.getViewers());
                if (viewers != null && viewers.size() > 0)
                    binding.viewerslist.smoothScrollToPosition(viewers.size() - 1);
            }
        });


        binding.chatsView.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext());
        binding.chatsView.setLayoutManager(llm);

        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).orderBy("created_at", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                am.toggleDialog(false);
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                ArrayList<Chat> chats = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Chat chat = doc.toObject(Chat.class);
                    chat.setId(doc.getId());
                    chats.add(chat);
                }
                ChatAdapter chatAdapter = new ChatAdapter(chatroom, chats);
                binding.chatsView.setAdapter(chatAdapter);
            }
        });

        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.editTextTextPersonName.getText().toString();
                if (msg.isEmpty()) {
                    Toast.makeText(getContext(), "Send a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendChat(msg, Chat.CHAT_MESSAGE);
            }
        });

        return view;
    }

    public void sendRideStartedChat() {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("created_at", FieldValue.serverTimestamp());
        chat.put("content", trip.getId() + "\n" + trip.getDriverId() + "\n" + trip.getDriverName() + "\n" + trip.getRiderName());
        chat.put("owner", new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())));
        chat.put("chatType", Chat.CHAT_RIDE_STARTED);
        chat.put("likedBy", new ArrayList<>());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    trip.setMsg_id(task.getResult().getId());
                    Toast.makeText(getContext(), "Your trip has started", Toast.LENGTH_SHORT).show();
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void sendOfferChat() {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("created_at", FieldValue.serverTimestamp());
        chat.put("content", rideOffer.getRiderId() + "\n" + rideOffer.getOfferorName() + "\n" + rideOffer.getRiderName() + "\n" + rideOffer.getDriver_location().get(0) + "\n" + rideOffer.getDriver_location().get(1));
        chat.put("owner", new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())));
        chat.put("chatType", Chat.CHAT_RIDE_OFFER);
        chat.put("likedBy", new ArrayList<>());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    rideOffer.setMsg_id(task.getResult().getId());

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("msg_id", null);
                    data.put("ride_id", rideOffer.getRide_id());
                    data.put("rider", rideOffer.getRider());
                    data.put("offeror", rideOffer.getOfferor());
                    data.put("driver_location", rideOffer.getDriver_location());
                    data.put("pickup_location", rideOffer.getPickup_location());
                    data.put("drop_location", rideOffer.getDrop_location());
                    data.put("pickup_name", rideOffer.getPickup_name());
                    data.put("drop_name", rideOffer.getDrop_name());

                    db.collection(Utils.DB_RIDE_OFFER).document(rideOffer.getMsg_id())
                            .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "You sent a ride offer", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void sendRequestChat() {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("created_at", FieldValue.serverTimestamp());
        chat.put("content", rideReq.getPickup_location().get(0) + "\n" + rideReq.getPickup_location().get(1) + "\n" + rideReq.getDrop_location().get(0) + "\n" + rideReq.getDrop_location().get(1) + "\n" + rideReq.getPickup_name() + "\n" + rideReq.getDrop_name());
        chat.put("owner", new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())));
        chat.put("chatType", Chat.CHAT_RIDE_REQUEST);
        chat.put("likedBy", new ArrayList<>());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    rideReq.setMsg_id(task.getResult().getId());

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("msg_id", rideReq.getMsg_id());
                    data.put("pickup_location", rideReq.getPickup_location());
                    data.put("drop_location", rideReq.getDrop_location());
                    data.put("requester", rideReq.getRequester());
                    data.put("pickup_name", rideReq.getPickup_name());
                    data.put("drop_name", rideReq.getDrop_name());

                    db.collection(Utils.DB_RIDE_REQ).document(rideReq.getMsg_id())
                            .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "You requested a ride", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    public void sendChat(String msg, int chatType) {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("created_at", FieldValue.serverTimestamp());
        chat.put("content", msg);
        chat.put("owner", new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())));
        chat.put("chatType", chatType);
        chat.put("likedBy", new ArrayList<>());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                    binding.editTextTextPersonName.setText("");
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                navController.popBackStack();
                return true;
            case R.id.action_request_ride:
                navController.navigate(R.id.action_chatroomFragment_to_mapsFragment);
                return true;
            case R.id.action_send_location:
                mapHelper.getLastLocation(new MapHelper.ILastLocation() {
                    @Override
                    public void onUpdate(Location location) {
                        sendLocationChat(location.getLatitude(), location.getLongitude());
                    }

                    @Override
                    public boolean stopAfterOneUpdate() {
                        return true;
                    }
                });
                return true;
            case R.id.action_your_rides:
                navController.navigate(R.id.action_chatroomFragment_to_tripListFragment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void sendLocationChat(double lat, double longi) {
        sendChat(lat + "\n" + longi, Chat.CHAT_LOCATION);
    }

    interface IChat {

        void toggleDialog(boolean show);

        User getUser();

        void actionBar(boolean show);

        MapHelper getMapHelper();

    }

}
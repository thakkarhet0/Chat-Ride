package com.example.chatroom;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentRiderDetailsBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideOffer;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class RideDetailsFragment extends Fragment {

    FragmentRiderDetailsBinding binding;
    Chat chat;
    User user;
    Chatroom chatroom;
    IRiderDetails am;
    MapHelper mapHelper;
    FirebaseFirestore db;
    private GoogleMap mMap;
    String pickup_name, drop_name;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRiderDetails) {
            am = (IRiderDetails) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        chatroom = user.getChatroom();
        mapHelper = am.getMapHelper();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chat = (Chat) getArguments().getSerializable(Utils.DB_CHAT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapHelper.stopUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Rider Details");
        binding = FragmentRiderDetailsBinding.inflate(inflater, container, false);

        binding.textView20.setText(chat.getOwnerName());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView3);

        String[] loc = chat.getContent().split("\n");

        pickup_name = loc[4];
        drop_name = loc[5];

        binding.sourceTextView.setText(pickup_name);
        binding.destinationTextView.setText(drop_name);

        ArrayList<Double> pickup = new ArrayList<>(Arrays.asList(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));
        ArrayList<Double> drop = new ArrayList<>(Arrays.asList(Double.parseDouble(loc[2]), Double.parseDouble(loc[3])));

        LatLng mOrigin = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        LatLng mDestination = new LatLng(Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mapHelper.clearMarkers();
                    mapHelper.addMarker(mMap, mOrigin, "Origin");
                    mapHelper.addMarker(mMap, mDestination, "Destination");
                }
            });
        }

        if (chat.getOwnerRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwnerId()).child(chat.getOwnerRef());
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imageView7);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(binding.imageView7);
        }

        binding.button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapHelper.hasLocationPerms() && mapHelper.isLocationEnabled()) {

                    mapHelper.getLastLocation(new MapHelper.ILastLocation() {

                        @Override
                        public void onUpdate(Location location) {
                            sendRideOffer(location.getLatitude(), location.getLongitude(), pickup, drop);
                        }

                        @Override
                        public boolean stopAfterOneUpdate() {
                            return true;
                        }
                    });

                } else {
                    mapHelper.sendLocOffMessage();
                }
            }
        });

        binding.button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
            }
        });

        binding.textView24.setText(Utils.getPrettyTime(chat.getCreated_at()));
        return binding.getRoot();
    }

    public void sendRideOffer(double lat, double longi, ArrayList<Double> pickup, ArrayList<Double> drop) {
        RideOffer rideOffer = new RideOffer(chat.getId(), new ArrayList<>(Arrays.asList(chat.getOwnerId(), chat.getOwnerName(), chat.getOwnerRef())), new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())), new ArrayList<>(Arrays.asList(lat, longi)), pickup, drop, pickup_name, drop_name);
        user.setRideOffer(rideOffer);
        user.ride_offer = true;
        Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
    }

    interface IRiderDetails {

        User getUser();

        MapHelper getMapHelper();

    }
}
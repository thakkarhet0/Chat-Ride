package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentRideOfferDetailBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideOffer;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RideOfferDetailFragment extends Fragment {

    FragmentRideOfferDetailBinding binding;

    Chat chat;

    User user;

    GoogleMap mMap;

    MapHelper mapHelper;

    NavController navController;

    FirebaseFirestore db;

    RideOffer rideOffer;

    IOfferDetails am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOfferDetails) {
            am = (IOfferDetails) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Ride Offer");
        binding = FragmentRideOfferDetailBinding.inflate(inflater, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView4);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                }
            });
        }

        am.toggleDialog(true);
        db.collection(Utils.DB_RIDE_OFFER).document(chat.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                am.toggleDialog(false);
                if (task.isSuccessful()) {
                    rideOffer = task.getResult().toObject(RideOffer.class);
                    binding.textView27.setText(rideOffer.getOfferorName());
                    binding.textView28.setText(Utils.getPrettyTime(chat.getCreated_at()));
                    if (rideOffer.getOfferorRef() != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(rideOffer.getOfferorId()).child(rideOffer.getOfferorRef());
                        GlideApp.with(view)
                                .load(storageReference)
                                .into(binding.imageView8);
                    } else {
                        GlideApp.with(view)
                                .load(R.drawable.profile_image)
                                .into(binding.imageView8);
                    }
                    ArrayList<Double> driver_location = rideOffer.getDriver_location();
                    mapHelper.clearMarkers();
                    mapHelper.addMarker(mMap, new LatLng(driver_location.get(0), driver_location.get(1)), "Driver Location");
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        binding.button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                am.toggleDialog(true);

                Trip trip = new Trip(rideOffer.getRide_id(), new Date(), rideOffer.getRider(), rideOffer.getOfferor(), rideOffer.getPickup_location(), rideOffer.getDriver_location(), rideOffer.getDrop_location(), rideOffer.getPickup_name(), rideOffer.getDrop_name());

                HashMap<String, Object> data = new HashMap<>();
                data.put("ride_id", trip.getRide_id());
                data.put("started_at", trip.getStarted_at());
                data.put("rider", trip.getRider());
                data.put("driver", trip.getDriver());
                data.put("ongoing", trip.isOngoing());
                data.put("rider_location", trip.getRider_location());
                data.put("driver_location", trip.getDriver_location());
                data.put("drop_location", trip.getDrop_location());
                data.put("pickup_name", trip.getPickup_name());
                data.put("drop_name", trip.getDrop_name());

                db.collection(Utils.DB_TRIPS).add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        am.toggleDialog(false);
                        if (task.isSuccessful()) {
                            trip.setId(task.getResult().getId());
                            user.setTrip(trip);

                            user.addRide(trip.getId());

                            db.collection(Utils.DB_PROFILE).document(user.getId()).update("rides", FieldValue.arrayUnion(trip.getId()));
                            db.collection(Utils.DB_PROFILE).document(trip.getDriverId()).update("rides", FieldValue.arrayUnion(trip.getId()));

                            user.ride_started = true;
                            Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });

            }
        });

        binding.button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        return view;
    }

    interface IOfferDetails {

        User getUser();

        MapHelper getMapHelper();

        void toggleDialog(boolean show);

    }

}
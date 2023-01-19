package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentMapsBinding;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideReq;
import com.example.chatroom.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;


public class MapsFragment extends Fragment {

    FragmentMapsBinding binding;


    AutocompleteSupportFragment autocompleteFragment, autocompleteFragment2;
    GoogleMap mMap;

    MapHelper mapHelper;
    private LatLng mOrigin;
    String oName;
    private LatLng mDestination;
    String dName;
    User user;
    IRequestRide am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRequestRide) {
            am = (IRequestRide) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        mapHelper = am.getMapHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Request Ride");
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);

        mapHelper.clearMarkers();

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                }
            });
        }

        String apiKey = getString(R.string.api_key);

        Places.initialize(getContext(), apiKey);

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment2);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getLatLng();
                oName = place.getName();
                mapHelper.addMarker(mMap, mOrigin, oName, null);
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        autocompleteFragment2 = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment3);

        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getLatLng();
                dName = place.getName();
                mapHelper.addMarker(mMap, mDestination, dName);
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        binding.button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOrigin != null & mDestination != null) {
                    RideReq rideReq = new RideReq(new ArrayList<>(Arrays.asList(mOrigin.latitude, mOrigin.longitude)), new ArrayList<>(Arrays.asList(mDestination.latitude, mDestination.longitude)), new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())), new ArrayList<>(), oName, dName);
                    user.setRideReq(rideReq);
                    user.ride_req = true;
                    Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();

                } else {
                    Toast.makeText(getContext(), "Please select pickup and destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    interface IRequestRide {

        User getUser();

        MapHelper getMapHelper();

    }


}
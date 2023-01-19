package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.TripAdapter;
import com.example.chatroom.databinding.FragmentTripListBinding;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class TripListFragment extends Fragment {

    FragmentTripListBinding binding;

    User user;

    ITripList am;

    FirebaseFirestore db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITripList) {
            am = (ITripList) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Your Trips");
        binding = FragmentTripListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.tripview.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.tripview.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.tripview.getContext(),
                llm.getOrientation());
        binding.tripview.addItemDecoration(dividerItemDecoration);

        binding.button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
            }
        });

        am.toggleDialog(true);

        db.collection(Utils.DB_TRIPS).orderBy("started_at", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                am.toggleDialog(false);
                ArrayList<Trip> rides = new ArrayList<>();
                int i = user.getRides().size() + 1;
                int j = 0;
                for (QueryDocumentSnapshot doc : value) {
                    if (user.getRides().contains(doc.getId())) {
                        Trip ride = doc.toObject(Trip.class);
                        ride.setNumber(i - ++j);
                        ride.setId(doc.getId());
                        rides.add(ride);
                    }
                }
                binding.tripview.setAdapter(new TripAdapter(user, rides));
            }
        });

        return view;
    }

    interface ITripList {
        User getUser();

        void toggleDialog(boolean show);
    }
}
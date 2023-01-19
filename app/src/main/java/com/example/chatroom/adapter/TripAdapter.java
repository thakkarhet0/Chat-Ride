package com.example.chatroom.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatroom.R;
import com.example.chatroom.databinding.TripLayoutBinding;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;

import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.UViewHolder> {

    ArrayList<Trip> trips;

    User user;

    TripLayoutBinding binding;

    public TripAdapter(User user, ArrayList<Trip> trips) {
        this.user = user;
        this.trips = trips;
    }

    @NonNull
    @Override
    public UViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = TripLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        holder.binding.textView25.setText("Trip #" + trip.getNumber());

        holder.binding.sourceId.setText("Source: " + trip.getPickup_name());
        holder.binding.destinationId.setText("Dest: " + trip.getDrop_name());

        if (trip.getDriverId().equals(user.getId())) {
            holder.binding.textView30.setText("Rider: " + trip.getRiderName());
            holder.binding.textView33.setText("Driver: You");
        } else {
            holder.binding.textView30.setText("Driver: " + trip.getDriverName());
            holder.binding.textView33.setText("Rider: You");
        }

        holder.binding.textView32.setText(Utils.getPrettyTime(trip.getStarted_at()));
        holder.binding.textView31.setText(trip.getStatus());

        if (trip.isOngoing()) {
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Utils.DB_TRIPS, trip);
                    Navigation.findNavController(holder.itemView).navigate(R.id.action_tripListFragment_to_tripInfoFragment, bundle);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.trips.size();
    }

    public static class UViewHolder extends RecyclerView.ViewHolder {

        TripLayoutBinding binding;

        public UViewHolder(@NonNull TripLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}

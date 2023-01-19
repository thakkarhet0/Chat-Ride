package com.example.chatroom;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatroom.adapter.ChatAdapter;
import com.example.chatroom.adapter.ChatroomAdapter;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.User;

public class MainActivity extends AppCompatActivity implements LoginFragment.ILogin, RideOfferDetailFragment.IOfferDetails, TripInfoFragment.ITripInfo, TripListFragment.ITripList, RideDetailsFragment.IRiderDetails, MapsFragment.IRequestRide, ChatroomFragment.IChat, CreateNewAccountFragment.IRegister, ChatroomsFragment.IChatRoom, UsersFragment.IUsers, EditProfileFragment.IEditUser, UserProfileFragment.IUserProfile, ChatroomAdapter.IChatRoomAdapter, ChatAdapter.IChatAdapter {

    ProgressDialog dialog;
    User user = null;
    private MapHelper mhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mhelper = new MapHelper(this);
    }

    public void actionBar(boolean show) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(show);
    }

    public MapHelper getMapHelper() {
        return mhelper;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void alert(String alert) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.info)
                .setMessage(alert)
                .setPositiveButton(R.string.okay, null)
                .show());
    }

    public void toggleDialog(boolean show) {
        toggleDialog(show, null);
    }

    public void toggleDialog(boolean show, String msg) {
        if (show) {
            dialog = new ProgressDialog(this);
            if (msg == null)
                dialog.setMessage(getString(R.string.loading));
            else
                dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.show();
        } else {
            dialog.dismiss();
        }
    }

}
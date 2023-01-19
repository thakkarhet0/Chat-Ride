package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentUserProfileBinding;
import com.example.chatroom.models.User;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

public class UserProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;

    NavController navController;

    IUserProfile am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileFragment.IEditUser) {
            am = (IUserProfile) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.userProfile);

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        User user = am.getUser();
        binding.nameTextViewId.setText(user.getFirstname().toUpperCase() + " " + user.getLastname().toUpperCase());
        binding.genderTextViewId.setText(user.getGender());
        binding.cityTextViewId.setText(user.getCity());
        binding.emailTextViewId.setText(user.getEmail());

        if (user.getPhotoref() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getId()).child(user.getPhotoref());
            GlideApp.with(view)
                    .load(storageReference)
                    .into(binding.userProfileImageView);
        } else {
            GlideApp.with(view)
                    .load(R.drawable.profile_image)
                    .into(binding.userProfileImageView);
        }

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.bottomNavigation.setSelectedItemId(R.id.profileIcons);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        navController.navigate(R.id.action_userProfileFragment_to_chatroomsFragmentNav);
                        return true;
                    case R.id.usersIcon:
                        navController.navigate(R.id.action_userProfileFragment_to_usersFragment);
                        return true;
                    case R.id.profileIcons:
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_userProfileFragment_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        //...Editing profile
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_userProfileFragment_to_editProfileFragment);
            }
        });

        return view;

    }

    interface IUserProfile {
        void alert(String msg);

        User getUser();

        void setUser(User user);
    }
}
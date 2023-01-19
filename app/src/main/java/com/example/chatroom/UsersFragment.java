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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.UsersAdapter;
import com.example.chatroom.databinding.FragmentUsersBinding;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class UsersFragment extends Fragment {

    FragmentUsersBinding binding;

    IUsers am;

    NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUsers) {
            am = (IUsers) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.users);

        binding = FragmentUsersBinding.inflate(inflater, container, false);

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        View view = binding.getRoot();

        binding.usersView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.usersView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.usersView.getContext(),
                llm.getOrientation());
        binding.usersView.addItemDecoration(dividerItemDecoration);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        am.toggleDialog(true);
        CollectionReference ddb = db.collection(Utils.DB_PROFILE);
        ddb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                am.toggleDialog(false);
                if (task.isSuccessful()) {
                    ArrayList<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        User user = snapshot.toObject(User.class);
                        user.setId(snapshot.getId());
                        users.add(user);
                    }
                    binding.usersView.setAdapter(new UsersAdapter(users));
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.usersIcon);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        navController.navigate(R.id.action_usersFragment_to_chatroomsFragmentNav);
                        return true;
                    case R.id.usersIcon:
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_usersFragment_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_usersFragment_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        return view;
    }

    interface IUsers {

        void toggleDialog(boolean show);

    }
}
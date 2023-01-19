package com.example.chatroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.ChatroomAdapter;
import com.example.chatroom.databinding.FragmentChatroomsBinding;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;


public class ChatroomsFragment extends Fragment {

    final private String TAG = "demo";

    FragmentChatroomsBinding binding;

    IChatRoom am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IChatRoom) {
            am = (IChatRoom) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (am.getUser() == null) throw new RuntimeException("User not found");

        getActivity().setTitle(R.string.chatrooms);

        binding = FragmentChatroomsBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        binding.chatroomsview.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.chatroomsview.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.chatroomsview.getContext(),
                llm.getOrientation());
        binding.chatroomsview.addItemDecoration(dividerItemDecoration);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        am.toggleDialog(true);

        CollectionReference ref = db.collection(Utils.DB_CHATROOM);
        Query query = ref.orderBy("created_at", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                am.toggleDialog(false);
                ArrayList<Chatroom> chatrooms = new ArrayList<>();
                int i = 0;
                for (QueryDocumentSnapshot doc : value) {
                    Chatroom chatroom = doc.toObject(Chatroom.class);
                    chatroom.setId(doc.getId());
                    chatroom.setNumber(++i);
                    chatrooms.add(chatroom);
                }
                binding.chatroomsview.setAdapter(new ChatroomAdapter(chatrooms));
            }
        });

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.create_new_chatroom, null);
                builder.setView(dialogView)
                        .setTitle("Enter Chatroom Name")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        EditText editText = (EditText) dialogView.findViewById(R.id.chatroomNameEditTextId);
                        String newChatroomName = editText.getText().toString();
                        if(newChatroomName.isEmpty() || newChatroomName == null ){
                            editText.setError("Please enter name");
                        } else{
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser cur = mAuth.getCurrentUser();
                            HashMap<String, Object> chatroom = new HashMap<>();
                            chatroom.put("name",newChatroomName );
                            chatroom.put("created_at", FieldValue.serverTimestamp());
                            chatroom.put("created_by", cur.getDisplayName());
                            chatroom.put("viewers", new HashMap<>());
                            am.toggleDialog(true);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection(Utils.DB_CHATROOM).add(chatroom).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    am.toggleDialog(false);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Chatroom Created", Toast.LENGTH_LONG).show();
                                    } else {
                                        task.getException().printStackTrace();
                                    }
                                }
                            });

                            dialog.dismiss();
                        }
                    }
                });

            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.chatroomsIcon);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        return true;
                    case R.id.usersIcon:
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_usersFragment);
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        return view;

    }

    public interface IChatRoom {

        void toggleDialog(boolean show);

        User getUser();

    }
}
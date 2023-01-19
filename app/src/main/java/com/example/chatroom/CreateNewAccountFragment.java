package com.example.chatroom;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentCreateNewAccountBinding;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CreateNewAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    final private String TAG = "demo";
    private static final int PICK_IMAGE_GALLERY = 100;
    private static final int PICK_IMAGE_CAMERA = 101;

    NavController navController;

    Uri imageUri;
    String fileName;
    String email, password, firstName, lastName, city, gender;

    FragmentCreateNewAccountBinding binding;

    IRegister am;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_GALLERY && data != null && resultCode == RESULT_OK) {
            imageUri = data.getData();
            binding.userImage.setImageURI(imageUri);
        }
        else if (requestCode == PICK_IMAGE_CAMERA && data != null && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            binding.userImage.setImageBitmap(imageBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), imageBitmap, "Title", null);
            imageUri =  Uri.parse(path);

        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRegister) {
            am = (IRegister) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    private void storeUserInfoToFirestore(String firstName, String lastName, String city, String gender, String email, String fileName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        data.put("firstname", firstName);
        data.put("lastname", lastName);
        data.put("city", city);
        data.put("gender", gender);
        data.put("email", email);
        data.put("photoref", fileName);
        data.put("rides", new ArrayList<>());

        db.collection(Utils.DB_PROFILE)
                .document(mAuth.getUid())
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        am.setUser(new User(firstName, lastName, fileName, city, email, gender, mAuth.getUid()));
                        navController.navigate(R.id.action_createNewAccountFragment_to_chatroomsFragmentNav);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.createAccount);

        binding = FragmentCreateNewAccountBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        binding.userImage.setImageResource(R.drawable.profile_image);

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        //....Register Button......
        binding.registerButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = binding.createFragmentFirstNameId.getText().toString();
                lastName = binding.createFragmentLastNameId.getText().toString();
                city = binding.createFragmentCityNameId.getText().toString();
                email = binding.createFragmentEmailId.getText().toString();
                password = binding.createFragmentPasswordId.getText().toString();
                RadioButton radioButton = binding.getRoot().findViewById(binding.radioGroup.getCheckedRadioButtonId());
                gender = radioButton.getText().toString();

                if(firstName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterFirstName));
                }else if(lastName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterLastName));
                }else if(city.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterCity));
                } else if(email.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }else if(password.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterPassword));
                }else if(gender.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.chooseGender));
                }else {

                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(firstName + " " + lastName).build();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.updateProfile(profileUpdates);

                                        //...Store image in firebase storage
                                        if (imageUri != null) {
                                            fileName = UUID.randomUUID().toString() + ".jpg";

                                            storeUserInfoToFirestore(firstName, lastName, city, gender, email, fileName);
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                            storageReference.child(user.getUid()).child(fileName).putFile(imageUri);
                                        } else {
                                            storeUserInfoToFirestore(firstName, lastName, city, gender, email, null);
                                        }

                                    } else
                                        getAlertDialogBox(task.getException().getMessage());

                                }
                            });
                }

            }
        });

        //....Cancel Button......
        binding.cancelButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_createNewAccountFragment_to_loginFragmentNav);
            }
        });

        //......Select Images from gallery........
        binding.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        return view;
    }

    public void getAlertDialogBox(String errorMessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.errorMessage))
                .setMessage(errorMessage);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    //....Select Image from gallery or camera
    public void selectImage() {
        final CharSequence[] options = {"Gallery", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.imagePick)
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(options[which].equals("Gallery")){
                            dialog.dismiss();
                            Intent takePictureFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureFromGallery, PICK_IMAGE_GALLERY);
                        }else if(options[which].equals("Camera")){
                            dialog.dismiss();
                            Intent takePictureFromCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePictureFromCamera, PICK_IMAGE_CAMERA);
                        }
                    }
                });
        builder.create().show();
    }



    interface IRegister {

        void setUser(User user);
    }
}
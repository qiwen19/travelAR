package com.ict3104.t10_idk_2020.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ict3104.t10_idk_2020.ChangePasswordActivity;
import com.ict3104.t10_idk_2020.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;
    private Uri filePath;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    DocumentReference docRef;

    private final int PICK_IMAGE_REQUEST = 71;
    private Button btnChangeImage;
    private ImageView imageViewProfilePhoto;
    /* Gender String */
    RadioGroup radioGroupProfileGender;
    RadioButton male, female;
    String gender = "";

    DatePickerDialog picker;
    LocalDate currentDate;
    LocalDate selectedDate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        View v = getView();

        /* Edit TextView*/
        final TextView displayName =  v.findViewById(R.id.textViewProfile);
        final EditText firstName = v.findViewById(R.id.editTextProfileFirstName);
        final EditText lastName = v.findViewById(R.id.editTextProfileLastName);
        final EditText email = v.findViewById(R.id.editTextProfileEmail);
        final EditText dobEditText = v.findViewById(R.id.editTextProfileDOB);
        /* RadioButton */
        /* male = v.findViewById(R.id.male);
        female = v.findViewById(R.id.female); */
        radioGroupProfileGender = v.findViewById(R.id.radioGroupProfileGender);
        radioGroupProfileGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.male:
                        gender = "male";
                        break;
                    case R.id.female:
                        gender = "female";
                        break;
                }
            }
        });

        imageViewProfilePhoto = v.findViewById(R.id.imageViewProfilePhoto);

        /* DatePicker */
        dobEditText.setInputType(InputType.TYPE_NULL);
        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final java.util.Calendar cldr = java.util.Calendar.getInstance();
                int day = cldr.get(java.util.Calendar.DAY_OF_MONTH);
                int month = cldr.get(java.util.Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dobEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                                currentDate = LocalDate.now(ZoneId.systemDefault());
                            }
                        }, year, month, day);
                picker.show();
                // Log.d("selected date and current date", "selectedDate: "+selectedDate+", currentDate:" + currentDate);
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final String uid = mAuth.getCurrentUser().getUid();//"UuhyLAvHgKLHhN89E6DH";
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Account").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        // Get user data
                        String em = document.getString("email");
                        String fn = document.getString("firstName");
                        String ln = document.getString("lastName");
                        String image = document.getString("image");
                        String gender = document.getString("gender");
                        String dob = document.getString("dob");
                        String displayMsg = String.format("Hi %s", fn);
                        //Set editTexts with profile information
                        email.setText(em);
                        firstName.setText(fn);
                        lastName.setText(ln);
                        dobEditText.setText(dob);
                        if (gender.equals("male")){
                            radioGroupProfileGender.check(R.id.male);
                        } else if (gender.equals("female")){
                            radioGroupProfileGender.check(R.id.female);
                        }
                        displayName.setText(displayMsg);
                        //dob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

                        if (image.isEmpty()){
                            imageViewProfilePhoto.setImageResource(R.drawable.logo);
                        } else{
                            Picasso.with(getContext()).load(image).into(imageViewProfilePhoto);
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        /* Button */
        Button btnSaveProfile = v.findViewById(R.id.btnSaveProfile);
        Button btnChangePasswordPage = v.findViewById(R.id.btnChangePasswordPage);

        btnChangeImage = v.findViewById(R.id.btnChangeImage);
        imageViewProfilePhoto = v.findViewById(R.id.imageViewProfilePhoto);

        /* Button save image and update database */
        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        /* Button Login OnClick - Validate Input then update database */
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), filePath.toString(), Toast.LENGTH_LONG).show();
                String firstNameError = validateFirstName(firstName.getText().toString());
                String lastNameError = validateLastName(lastName.getText().toString());
                String emailError = validateEmail(email.getText().toString());
                String DobError = validateDob(dobEditText.getText().toString());
                String GenderError = validateGender(gender);

                /* If all inputs not empty */
                if(firstNameError.equals("") && lastNameError.equals("")&& emailError.equals("") && DobError.equals("")&& GenderError.equals("")) {

                    docRef.update("firstName", firstName.getText().toString());
                    docRef.update("lastName", lastName.getText().toString());
                    docRef.update("dob", dobEditText.getText().toString());
                    docRef.update("gender", gender);
                    //docRef.update("profileImage", )
                    String displayMsg = String.format("Hi %s", firstName.getText().toString());
                    displayName.setText(displayMsg);

                    // Only gmail allowed
                    user.updateEmail(email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Log.d(TAG, "User email address updated.");
                                        //Toast.makeText(getActivity(), "The email updated.", Toast.LENGTH_SHORT).show();
                                        docRef.update("email",email.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User email address updated.");
                                                        }
                                                    }
                                                });
                                        Toast toast = Toast.makeText(getContext(), "Your profile have been updated", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    else{
                                        Toast toast = Toast.makeText(getContext(), "Email unavailable", Toast.LENGTH_SHORT);
                                        toast.show();
                                        email.setError("Email unavailable");
                                    }
                                }
                            });
                }
                else if (!firstNameError.equals("")){
                    firstName.setError(firstNameError);
                }
                else if (!lastNameError.equals("")){
                    lastName.setError(lastNameError);
                }
                else if (!emailError.equals("")){
                    email.setError(emailError);
                }
                else if (!DobError.equals("")){
                    lastName.setError(lastNameError);
                }
                else if (!GenderError.equals("")){
                    email.setError(emailError);
                }

                // Upload Image
                if(filePath != null)
                {
                    uploadImage();
                }
            }});

        btnChangePasswordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private String validateFirstName(String firstName){
        String error_msg = "";
        if(firstName.isEmpty()){
            error_msg = "This field is required";
        }

        return error_msg;
    }

    private String validateLastName(String lastName){
        String error_msg = "";
        if(lastName.isEmpty()){
            error_msg = "This field is required";
        }

        return error_msg;
    }

    private String validateEmail(String email){
        String error_msg = "";
        if(email.isEmpty()){
            error_msg = "This field is required";
        }else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            error_msg = "Invalid email";
        }
        return error_msg;
    }

    private String validateDob(String dob){
        String error_msg = "";
        if(dob.isEmpty()){
            error_msg = "This field is required";
        }
        return error_msg;
    }

    private String validateGender(String gender){
        String error_msg = "";
        if(gender.isEmpty()){
            error_msg = "This field is required";
        }
        return error_msg;
    }

    private void  chooseImage(){
       Intent intent = new Intent();
       intent.setType("image/*");
       intent.setAction(Intent.ACTION_GET_CONTENT);
       startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageViewProfilePhoto.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void uploadImage() {
        //mAuth = FirebaseAuth.getInstance();
        //String uid = mAuth.getCurrentUser().getUid();     //"UuhyLAvHgKLHhN89E6DH";
        try {
            final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            //Toast.makeText(getActivity(), filePath.toString(), Toast.LENGTH_LONG).show();
            UploadTask uploadTask = ref.putFile(filePath);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        addUploadToDb(downloadUri.toString());
                    }
                }
            });
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addUploadToDb(String uri){
        final String uid = mAuth.getCurrentUser().getUid();//"UuhyLAvHgKLHhN89E6DH";
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Account").document(uid);
        docRef.update("image", uri);
    }
}
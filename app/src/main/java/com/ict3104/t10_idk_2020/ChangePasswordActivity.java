package com.ict3104.t10_idk_2020;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ict3104.t10_idk_2020.ui.profile.ProfileFragment;

import static android.content.ContentValues.TAG;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;
    private String password;
    FragmentTransaction fragmentTransaction;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.banner_background_gradient));

        /* Edit TextView*/
        final EditText currentPassword = findViewById(R.id.editTextProfileCurrentPassword);
        final EditText newPassword = findViewById(R.id.editTextProfileNewPassword);
        final EditText comfirmPassword = findViewById(R.id.editTextProfileNewConfirmPassword);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Account").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        password = document.getString("password");;
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        /* Button */
        Button btnChangePassword = findViewById(R.id.btnChangePassword);

        /* Button Login OnClick - Validate Input then update database */
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentpw = currentPassword.getText().toString();
                String newpw = newPassword.getText().toString();
                String compw = comfirmPassword.getText().toString();

                String newPasswordError = validateNewPassword(newpw, compw);
                String comfirmPasswordError = validateCurrentPassword(password, currentpw);

                if (newPasswordError.equals("") && comfirmPasswordError.equals("")){
                    docRef.update("password", newpw);
                    user.updatePassword(newpw).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Password updated");
                            } else {
                                Log.d(TAG, "Error password not updated");
                            }
                        }
                    });
                    Toast toast = Toast.makeText(getApplicationContext(), "Your password have been updated", Toast.LENGTH_SHORT);
                    toast.show();
                    onBackPressed();
                }

                else if (!newPasswordError.equals("")) {
                    newPassword.setError(newPasswordError);
                    comfirmPassword.setError(newPasswordError);
                }
                else {
                    currentPassword.setError(comfirmPasswordError);
                }
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public String validateCurrentPassword(String password, String current){
        String error_msg = "";
        if (current.isEmpty()){
            error_msg = "This field is required";
        }
        else if (!current.equals(password)){
            error_msg = "Wrong password";
        }
        return error_msg;
    }

    public String validateNewPassword(String new1, String new2){
        String error_msg = "";
        if (new1.isEmpty() && new2.isEmpty()){
            error_msg = "This field is required";
        }
        else if(new1.length() < 8){
            error_msg = "Must be at least 8 characters long";
        }
        else if (!new1.equals(new2)){
                error_msg = "New password do not match";
        }
        return  error_msg;
    }
}

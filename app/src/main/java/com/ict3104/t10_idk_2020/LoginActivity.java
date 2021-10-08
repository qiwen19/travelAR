package com.ict3104.t10_idk_2020;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ict3104.t10_idk_2020.repository.IntegerCallBack;
import com.ict3104.t10_idk_2020.viewModel.AccountViewModel;

public class LoginActivity extends AppCompatActivity {
    AccountViewModel accountViewModel;
    TextView textViewRegisterHere;
    TextView textViewErrorMsg;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnLogin;
    private FirebaseAuth mAuth;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            /* Re-direct to MainActivity */
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getLocationPermission();

        /* ViewModel */
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        /* TextView */
        TextView textViewForgotPassword = findViewById(R.id.textViewForgetPassword);

        textViewRegisterHere = findViewById(R.id.textViewRegisterHere);
        textViewErrorMsg = findViewById(R.id.textViewErrorMsg);

        /* EditText */
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);


        /* Button */
        btnLogin = findViewById(R.id.btnLogin);

        /* textViewRegisterHere OnClick */
        textViewRegisterHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        /* btnLogin OnClick */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Get user input */
                final String email = editTextEmail.getText().toString();
                final String password = editTextPassword.getText().toString();

                /* Check if input is empty */
                String inputError = validateInput(email, password);

                /* If input has no error */
                if(inputError.equals("")){
                    /* Check if user exist */
                    accountViewModel.isUserExist(email, password, new IntegerCallBack() {
                        @Override
                        public void onCallBack(int count) {
                            if(count == 1){ /* Must be 1 for a successful login */
                                mAuth = FirebaseAuth.getInstance();
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d("RESULT", "signInWithEmail:success");
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    /* Re-direct to MainActivity */
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w("RESULT", "signInWithEmail:failure", task.getException());
                                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });

                }else{
                    textViewErrorMsg.setText(inputError);
                }
            }
        });
      
        /* Forget Password Onclick - Start ForgetPassword activity */
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgetpasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    public String validateInput(String email, String password){
        String error_msg = "";
        /* Check for empty inputs */
        if(email.isEmpty() || password.isEmpty()){
            error_msg = "Incorrect email or password";
        }
        return error_msg;
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}
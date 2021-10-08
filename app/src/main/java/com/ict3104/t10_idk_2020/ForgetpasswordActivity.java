package com.ict3104.t10_idk_2020;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgetpasswordActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);
        getSupportActionBar().hide();

        /* TextView */
        TextView textViewRememberPassword = findViewById(R.id.textViewRememberPassword);

        /* Button */
        Button btnSendLink = findViewById(R.id.btnSendLink);

        /* Email Edit Text */
        final EditText editTextEmail_forgetPwd = findViewById(R.id.editTextEmail_forgetPwd);

        /* Remember password OnClick - Start register activity */
        textViewRememberPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        /* Button Login OnClick - Send Link then start login activity */
        btnSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextEmail_forgetPwd.getText().toString().isEmpty()){
                    Toast.makeText(ForgetpasswordActivity.this, "Please fill in a valid email", Toast.LENGTH_LONG).show();
                } else {
                    auth.sendPasswordResetEmail(editTextEmail_forgetPwd.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // do something when mail was sent successfully.
                                        Toast.makeText(ForgetpasswordActivity.this, editTextEmail_forgetPwd.getText().toString(), Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ForgetpasswordActivity.this, editTextEmail_forgetPwd.getText().toString(), Toast.LENGTH_LONG).show();
                                        //Toast.makeText(ForgotpasswordActivity.this, "Fail to send request password email", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }



}

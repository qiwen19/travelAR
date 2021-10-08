package com.ict3104.t10_idk_2020;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ict3104.t10_idk_2020.model.Account;
import com.ict3104.t10_idk_2020.repository.BooleanCallBack;
import com.ict3104.t10_idk_2020.repository.IntegerCallBack;
import com.ict3104.t10_idk_2020.viewModel.AccountViewModel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    AccountViewModel accountViewModel;
    TextInputLayout layout_firstName;
    TextInputLayout layout_lastName;
    TextInputLayout layout_email;
    TextInputLayout layout_password;
    TextInputLayout layout_confirmPassword;
    TextInputLayout layout_code;
    TextInputLayout layout_gender;
    TextInputLayout layout_dob;

    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextConfirmPassword;
    EditText editTextPlannerCode;
    EditText editTextDOB;

    DatePickerDialog picker;
    LocalDate currentDate;
    LocalDate selectedDate;

    CheckBox checkBox;
    RadioButton male, female;
    Button btnRegister;
    TextView textViewLoginBack;
    private FirebaseAuth mAuth;

    /* Gender String */
    String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        /* ViewModel */
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        /* TextInputLayout */
        layout_firstName = findViewById(R.id.layout_firstName);
        layout_lastName = findViewById(R.id.layout_lastName);
        layout_email = findViewById(R.id.layout_email);
        layout_password = findViewById(R.id.layout_password);
        layout_confirmPassword = findViewById(R.id.layout_confirmPassword);
        layout_code = findViewById(R.id.layout_code);
        layout_gender = findViewById(R.id.layout_gender);
        layout_dob = findViewById(R.id.layout_dob);

        /* EditText */
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPlannerCode = findViewById(R.id.editTextPlannerCode);
        editTextDOB = findViewById(R.id.editTextDOB);

        /* DatePicker */
        editTextDOB.setInputType(InputType.TYPE_NULL);
        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                editTextDOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                                currentDate = LocalDate.now(ZoneId.systemDefault());
                            }
                        }, year, month, day);
                picker.show();
                // Log.d("selected date and current date", "selectedDate: "+selectedDate+", currentDate:" + currentDate);
            }
        });

        /* RadioButton */
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        /* Checkbox */
        checkBox = findViewById(R.id.checkBox);

        /* TextView */
        textViewLoginBack = findViewById(R.id.textViewLoginBack);

        /* textViewRegisterHere OnClick */
        textViewLoginBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        /* Button */
        btnRegister = findViewById(R.id.btnRegister);

        /* btnRegister OnClick */
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Get user input from EditText */
                final String firstName = editTextFirstName.getText().toString();
                final String lastName = editTextLastName.getText().toString();
                final String email = editTextEmail.getText().toString();
                final String password = editTextPassword.getText().toString();
                final String confirmPassword = editTextConfirmPassword.getText().toString();
                final String code = editTextPlannerCode.getText().toString();
                final String DOB = editTextDOB.getText().toString();

                /* initialise user input */
                String firstNameError = "";
                String lastNameError = "";
                String emailError = "";
                String passwordError = "";
                String confirmPasswordError = "";
                String codeError = "";
                String dobError = "";
                String genderError = "";

                /* Clear error messages */
                layout_firstName.setError(firstNameError);
                layout_lastName.setError(lastNameError);
                layout_email.setError(emailError);
                layout_password.setError(passwordError);
                layout_confirmPassword.setError(confirmPasswordError);
                layout_code.setError(codeError);
                layout_gender.setError(genderError);
                layout_dob.setError(dobError);

                /* Validate user input */
                firstNameError = validateFirstName(firstName);
                lastNameError = validateLastName(lastName);
                emailError = validateEmail(email);
                passwordError = validatePassword(password);
                confirmPasswordError = validateConfirmPassword(password, confirmPassword);
                codeError = validateCode(code);
                genderError = validateGender(gender);
                dobError = validateDOB(DOB);

                /* If all inputs are valid */
                if(firstNameError.equals("") && lastNameError.equals("")&& emailError.equals("") && passwordError.equals("") && confirmPasswordError.equals("") && codeError.equals("") && genderError.equals("") && dobError.equals("")){
                /* Check if account already exists */
                    //Toast.makeText(RegisterActivity.this, DOB, Toast.LENGTH_LONG).show();

                    accountViewModel.isAccountExist(email, new IntegerCallBack() {
                        @Override
                        public void onCallBack(int count) {
                            Log.d("RESULT1", Integer.toString(count));
                            if(count == 0) {
                                if (checkBox.isChecked() && !code.isEmpty()) {
                                    if (code.equals("4321")) {
                                        /* Insert into DB */
                                        final Account account = new Account();
                                        account.setFirstName(firstName);
                                        account.setLastName(lastName);
                                        account.setEmail(email);
                                        account.setPassword(password);
                                        account.setStatus("1");
                                        account.setDOB(DOB);
                                        account.setGender(gender);
                                        account.setImage("");

                                        mAuth = FirebaseAuth.getInstance();
                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            final FirebaseUser user = mAuth.getCurrentUser();
                                                            // Sign in success, update UI with the signed-in user's information
                                                            Log.d("RESULTS", "createUserWithEmail:success");
                                                            Log.d("RESULTS", user.getUid());
                                                            accountViewModel.insertAccount(user.getUid(), account, new BooleanCallBack() {
                                                                @Override
                                                                public void onCallBack(boolean result) {
                                                                    Log.d("RESULT", Boolean.toString(result));
                                                                    if (result) {
                                                                        displaySuccessDialog();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            Log.w("RESULTS", "createUserWithEmail:failure", task.getException());
                                                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else{
                                        Toast.makeText(RegisterActivity.this, "Invalid code",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else{
                                    /* Insert into DB */
                                    final Account account = new Account();
                                    account.setFirstName(firstName);
                                    account.setLastName(lastName);
                                    account.setEmail(email);
                                    account.setPassword(password);
                                    account.setStatus("0");
                                    account.setDOB(DOB);
                                    account.setGender(gender);
                                    account.setImage("");

                                    mAuth = FirebaseAuth.getInstance();
                                    mAuth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        final FirebaseUser user = mAuth.getCurrentUser();
                                                        // Sign in success, update UI with the signed-in user's information
                                                        Log.d("RESULTS", "createUserWithEmail:success");
                                                        Log.d("RESULTS", user.getUid());
                                                        accountViewModel.insertAccount(user.getUid(), account, new BooleanCallBack() {
                                                            @Override
                                                            public void onCallBack(boolean result) {
                                                                Log.d("RESULT", Boolean.toString(result));
                                                                if (result) {
                                                                    displaySuccessDialog();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        Log.w("RESULTS", "createUserWithEmail:failure", task.getException());
                                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }else{
                                layout_email.setError("Email already exist");
                                Toast.makeText(RegisterActivity.this, "Failed to create account.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    /* Display error messages */
                    layout_firstName.setError(firstNameError);
                    layout_lastName.setError(lastNameError);
                    layout_email.setError(emailError);
                    layout_password.setError(passwordError);
                    layout_confirmPassword.setError(confirmPasswordError);
                    layout_code.setError(codeError);
                    layout_dob.setError(dobError);
                    layout_gender.setError(genderError);
                }
            }
        });
    }
    public void onCheckboxClicked(View view) {
        if (checkBox.isChecked()){
            editTextPlannerCode.setVisibility(View.VISIBLE);
        } else {
            editTextPlannerCode.setVisibility(View.GONE);
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked =((RadioButton) view).isChecked();

        //Check which radio button was clicked
        switch(view.getId()){
            case R.id.male:
                if(checked)
                    gender = "Male";
                break;
            case R.id.female:
                if(checked)
                    gender = "Female";
                break;
        }
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

    private String validatePassword(String password){
        String error_msg = "";
        if(password.isEmpty()){
            error_msg = "This field is required";
        }else if(password.length() < 8){
            error_msg = "Must be at least 8 characters long";
        }

        return error_msg;
    }

    private String validateConfirmPassword(String password, String confirmPassword){
        String error_msg = "";
        if(confirmPassword.isEmpty()){
            error_msg = "This field is required";
        }else if(!confirmPassword.equals(password)){
            error_msg = "Confirm password does not match";
        }

        return error_msg;
    }
    private String validateCode(String code){
        String error_msg = "";
        if(checkBox.isChecked() && code.isEmpty()){
            error_msg = "Please enter the correct code";
        }
        return error_msg;
    }

    private String validateGender(String gender){
        String error_msg = "";
        if(gender.isEmpty()){
            error_msg = "Please select a gender";
        }
        return error_msg;
    }

    private String validateDOB(String dob){
        String error_msg = "";
        if(dob.isEmpty()){
            error_msg = "This field is required";
        /*} else if(!dob.isEmpty()){
            if (selectedDate.isBefore(currentDate)) {
                error_msg = "Please choose a valid date";
            }*/
        }
        return error_msg;
    }

    private void displaySuccessDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        alertDialog.setTitle("Congratulations!");
        alertDialog.setMessage("Your account has been created successfully");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }
}
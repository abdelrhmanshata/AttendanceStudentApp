package com.example.attendancestudentapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendancestudentapp.Auth.Model.ModelProfessor;
import com.example.attendancestudentapp.Auth.Model.ModelStudent;
import com.example.attendancestudentapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {
    EditText registerFullName, registerEmail, phoneNumber;
    Button next, goToLogin;
    RadioGroup jobTitle, gender;
    String Title = "", Gender = "";

    FirebaseDatabase database;
    DatabaseReference UserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFullName = findViewById(R.id.et_FullName);
        registerEmail = findViewById(R.id.et_address);
        phoneNumber = findViewById(R.id.et_phone);
        next = findViewById(R.id.btn_next);
        goToLogin = findViewById(R.id.btn_got_to_login);
        jobTitle = findViewById(R.id.radioGroupJob);
        gender = findViewById(R.id.radioGroupGender);

        database = FirebaseDatabase.getInstance();
        UserReference = database.getReference("Users");


        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        next.setOnClickListener(v -> {
            //Extract the data from edit texts

            //Get complete phone number
            String _getUserEnteredPhoneNumber = Objects.requireNonNull(phoneNumber.getText()).toString().trim();
            if (TextUtils.isEmpty(_getUserEnteredPhoneNumber)) {
                phoneNumber.setError("" + getResources().getString(R.string.phone_is_required));
                phoneNumber.setFocusable(true);
                phoneNumber.requestFocus();
                return;
            } else {
                //Remove first zero if entered!
                if (_getUserEnteredPhoneNumber.charAt(0) == '0') {
                    _getUserEnteredPhoneNumber = _getUserEnteredPhoneNumber.substring(1);
                }
            }
            //Complete phone number
            String UserPhoneNumber = "+" + "20" + _getUserEnteredPhoneNumber;

            if (UserPhoneNumber.length() < 11) {
                phoneNumber.setError("" + getResources().getString(R.string.please_enter_number));
                phoneNumber.setFocusable(true);
                phoneNumber.requestFocus();
                return;
            }

            String UserEmail = Objects.requireNonNull(registerEmail.getText()).toString().trim();
            if (UserEmail.isEmpty()) {
                registerEmail.setError("" + getResources().getString(R.string.email_required));
                registerEmail.setFocusable(true);
                registerEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
                registerEmail.setError("" + getResources().getString(R.string.please_enter_email));
                registerEmail.setFocusable(true);
                registerEmail.requestFocus();
                return;
            }

            String fullName = Objects.requireNonNull(registerFullName.getText()).toString().trim();
            if (fullName.isEmpty()) {
                registerFullName.setError("" + getResources().getString(R.string.username_is_required));
                registerFullName.setFocusable(true);
                registerFullName.requestFocus();
                return;
            }

            RadioButton selectedJopTitle;
            if (jobTitle.getCheckedRadioButtonId() == -1) {
                Toasty.info(RegisterActivity.this, "" + getResources().getString(R.string.select_gender), Toast.LENGTH_SHORT).show();
                return;
            } else {
                selectedJopTitle = findViewById(jobTitle.getCheckedRadioButtonId());
                if (selectedJopTitle.getId() == R.id.student) {
                    Title = "Student";
                } else {
                    Title = "Professor";
                }
            }

            RadioButton selectedGender;
            if (gender.getCheckedRadioButtonId() == -1) {
                Toasty.info(RegisterActivity.this, "" + getResources().getString(R.string.select_gender), Toast.LENGTH_SHORT).show();
                return;
            } else {
                selectedGender = findViewById(gender.getCheckedRadioButtonId());
                if (selectedGender.getId() == R.id.male) {
                    Gender = "Male";
                } else {
                    Gender = "Female";
                }
            }

            try {
                // Check UserPhone Number Is Found or not
                UserReference.child(UserPhoneNumber).child("JobTitle").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String JopTitle = snapshot.getValue(String.class);
                        if (JopTitle == null) {
                            //Now data is validated
                            if (Title == "Student") {

                                ModelStudent student = new ModelStudent();
                                student.setsKey(UserPhoneNumber);
                                student.setsPhoneNumber(UserPhoneNumber);
                                student.setsEmail(UserEmail);
                                student.setsFullName(fullName);
                                student.setsJopTitle(Title);
                                student.setsGender(Gender);
                                Intent sIntent = new Intent(RegisterActivity.this, RegisterStudentActivity.class);
                                sIntent.putExtra("Student", student);
                                startActivity(sIntent);

                            } else if (Title == "Professor") {

                                ModelProfessor professor = new ModelProfessor();
                                professor.setpKey(UserPhoneNumber);
                                professor.setpPhoneNumber(UserPhoneNumber);
                                professor.setpEmail(UserEmail);
                                professor.setpFullName(fullName);
                                professor.setpJopTitle(Title);
                                professor.setpGender(Gender);
                                Intent pIntent = new Intent(RegisterActivity.this, RegisterProfessorActivity.class);
                                pIntent.putExtra("Professor", professor);
                                startActivity(pIntent);
                            }
                            finish();
                        } else {
                            Toasty.info(RegisterActivity.this, "" + getResources().getString(R.string.phone_is_found), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("", error.getMessage());
                    }
                });
            } catch (NullPointerException e) {
                Log.d("", e.getMessage());
            }

        });
    }
}
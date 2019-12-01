package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.rmit.androidass2.R;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SignUpActivity extends AppCompatActivity {
    EditText firstName, lastName, phone, emailSignup, passwordSignup;
    private FirebaseAuth mAuth;
    String TAG = "SignUpActivity";
    RadioGroup genderButtonGroup;
    RadioButton genderRadioButton;
    String gender;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean isChecked = false;
    String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        phone = findViewById(R.id.phone);
        emailSignup = findViewById(R.id.emailsignup);
        passwordSignup = findViewById(R.id.passwordsignup);
        mAuth = FirebaseAuth.getInstance();
        final Button showpassword = findViewById(R.id.showpasswordsignup);
        final Button hidepassword = findViewById(R.id.hidepasswordsignup);
        genderButtonGroup = findViewById(R.id.genderbuttongroup);
        TextView signinfromsignup = findViewById(R.id.signinfromsignup);
        signinfromsignup.setClickable(true);
        hidepassword.setVisibility(View.INVISIBLE);

        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordSignup.setInputType(InputType.TYPE_CLASS_TEXT);
                showpassword.setVisibility(View.INVISIBLE);
                hidepassword.setVisibility(View.VISIBLE);

            }
        });

        hidepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordSignup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showpassword.setVisibility(View.VISIBLE);
                hidepassword.setVisibility(View.INVISIBLE);

            }
        });

        signinfromsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

        genderButtonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedButton = genderButtonGroup.findViewById(checkedId);
                gender = checkedButton.getText().toString();
                isChecked= true;

            }
        });
        Button signUp = findViewById(R.id.signup);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

    }


    private void writeNewUser(final String userId, final String firstname,final String lastname,final String phone,final String gender, final String email) {
        User user = new User(firstname, lastname, phone, gender, email);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();

                db.collection("Tokens").document(userId).set(new UserToken(token))
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG,"Failed to update token ID");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG,"Updated token ID");
                            }
                        });

            }
        });
        db.collection("Users").document(userId).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Success" + userId, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private User registerUser() {
        String userfirstname = firstName.getText().toString().trim();
        String userlastname = lastName.getText().toString().trim();
        String userphone = phone.getText().toString().trim();
        String usergender;
        String useremail = emailSignup.getText().toString().trim();
        if (!isChecked) {
            usergender = "Other";
        } else {
           usergender = gender;
        }
        return new User(userfirstname, userlastname, userphone, usergender, useremail);
    }

//    Check validation of inputs
    private boolean isValid() {
        if (firstName.getText().toString().trim().equalsIgnoreCase("")) {
            firstName.setError("This field can not be blank");
            return false;
        }
        if (lastName.getText().toString().trim().equalsIgnoreCase("")) {
            lastName.setError("This field can not be blank");
            return false;
        }
        if (phone.getText().toString().trim().equalsIgnoreCase("")) {
            phone.setError("This field can not be blank");
            return false;
        }

        if (TextUtils.isEmpty(emailSignup.getText()) && !Patterns.EMAIL_ADDRESS.matcher(emailSignup.getText()).matches()) {
            emailSignup.setError("Invalid Email");
            return false;
        }
        if(passwordSignup.getText().toString().trim().equalsIgnoreCase("") || passwordSignup.getText().length() < 5){
            passwordSignup.setError("This field can not be blank and less than 5 characters");
            return false;
        }

        return true;
    }


// Sign Up with email and password
    private void signup() {
        if (isValid()) {
            mAuth.createUserWithEmailAndPassword(emailSignup.getText().toString(), passwordSignup.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "onComplete: SignUp");
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                User signedUpUser = registerUser();

                                writeNewUser(user.getUid(),signedUpUser.getFirstname(), signedUpUser.getLastname(), signedUpUser.getPhone(), signedUpUser.getGender(), signedUpUser.getEmail());
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                emailSignup.setText("");
                                emailSignup.setError("Invalid Email");
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

//    Hide the key board
    public void hideKeyBoard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);
    }
}

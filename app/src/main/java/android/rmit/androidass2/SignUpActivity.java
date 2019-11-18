package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
        String token;

        genderButtonGroup = findViewById(R.id.genderbuttongroup);

        TextView signinfromsignup = findViewById(R.id.signinfromsignup);

        signinfromsignup.setClickable(true);
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

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                User user = new User(firstname, lastname, phone, gender, email,token);
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
        });


    }

    private User registerUser() {

        String userfirstname = firstName.getText().toString().trim();
        String userlastname = lastName.getText().toString().trim();
        String userphone = phone.getText().toString().trim();
        String usergender;
        if (!isChecked) {
            usergender = "Other";
        } else {
           usergender = gender;
        }

        return new User(userfirstname, userlastname, userphone, usergender);

    }

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
        if (emailSignup.getText().toString().trim().equalsIgnoreCase("")){
            emailSignup.setError("This field can not be blank");
            return false;
        }
        if(passwordSignup.getText().toString().trim().equalsIgnoreCase("")){
            passwordSignup.setError("This field can not be blank");
            return false;
        }


        return true;
    }



    private void signup() {

        if (isValid()) {
            mAuth.createUserWithEmailAndPassword(emailSignup.getText().toString(), passwordSignup.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                User signedUpUser = registerUser();
                                writeNewUser(user.getUid(),signedUpUser.getFirstname(), signedUpUser.getLastname(), signedUpUser.getPhone(), signedUpUser.getGender(),signedUpUser.getEmail());
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                            } else {
                                // If sign in fails, display a message to the user.
                                emailSignup.setText("");
                                emailSignup.setError("Email has already signed Up");
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }
    public void hideKeyBoard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);
    }
}

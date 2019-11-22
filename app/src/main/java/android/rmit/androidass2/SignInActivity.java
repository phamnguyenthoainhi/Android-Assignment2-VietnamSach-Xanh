package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.rmit.androidass2.R;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;


    EditText emailSignIn;
    EditText passwordSignIn;


    String TAG = "SignInActivity";
    private boolean isValid() {

        if (emailSignIn.getText().toString().trim().equalsIgnoreCase("")){
            emailSignIn.setError("This field can not be blank");
            return false;
        }
        if(passwordSignIn.getText().toString().trim().equalsIgnoreCase("")){
            passwordSignIn.setError("This field can not be blank");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        emailSignIn = findViewById(R.id.emailsignin);
        passwordSignIn = findViewById(R.id.passwordsignin);

        Button signIn = findViewById(R.id.signin);



        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signin();
            }
        });

        TextView signupfromsignin = findViewById(R.id.signupfromsignin);

        signupfromsignin.setClickable(true);
        signupfromsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });
    }

    public void signin() {
        if (isValid()) {
            mAuth.signInWithEmailAndPassword(emailSignIn.getText().toString(), passwordSignIn.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("uid", user.getUid());
                                    editor.commit();



                                        startActivity(new Intent(SignInActivity.this,  MapsActivity.class));
                                        finish();
                                }

//                            updateUI(user);
                            } else {
                                emailSignIn.setFocusable(true);
                                emailSignIn.setError("Email is incorrect or has not been signed up");

                                passwordSignIn.setError("Incorrect password");
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
//                            updateUI(null);
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

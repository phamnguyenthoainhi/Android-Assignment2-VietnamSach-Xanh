package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;


    EditText emailSignIn;
    EditText passwordSignIn;
    LoginButton loginButton;
    Button showpassword;
    String email;

    FirebaseFirestore db;

    String TAG = "SignInActivity";


    private CallbackManager callbackManager;


//    Check validation of input
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            final FirebaseUser user = mAuth.getCurrentUser();
                            User newuser = new User();

                            Log.d(TAG, "onComplete: currentuser "+ user.getDisplayName());
                            //System.out.println("NAME: " + user.getDisplayName());
                            newuser.setEmail(user.getEmail());
                            newuser.setFirstname(user.getDisplayName());

                            addUser(newuser, user.getUid());
                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String token = instanceIdResult.getToken();

                                    db.collection("Tokens").document(user.getUid()).set(new UserToken(token))
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
                            if (user.isEmailVerified()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("uid", user.getUid());
                                editor.commit();

                                //startActivity(new Intent(SignInActivity.this,  MapsActivity.class));
                                finish();

                            } else {
                                startActivity(new Intent(SignInActivity.this, VerifyEmail.class));
                                finish();
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }

// Add new user to database
    public void addUser(User user, String docid){

        db.collection("Users")
                .document(docid)
                .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: adduser");
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "onStart: "+currentUser);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailSignIn = findViewById(R.id.emailsignin);
        passwordSignIn = findViewById(R.id.passwordsignin);



        showpassword = findViewById(R.id.showpasswordsignin);
        final Button hidepassword = findViewById(R.id.hidepasswordsignin);

        hidepassword.setVisibility(View.INVISIBLE);

        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordSignIn.setInputType(InputType.TYPE_CLASS_TEXT);
                showpassword.setVisibility(View.INVISIBLE);
                hidepassword.setVisibility(View.VISIBLE);

            }
        });

        hidepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordSignIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showpassword.setVisibility(View.VISIBLE);
                hidepassword.setVisibility(View.INVISIBLE);

            }
        });

        Button signIn = findViewById(R.id.signin);
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById ( R.id.login_button);
        List<String>permissions = new ArrayList<>();
        permissions.add("email");
        permissions.add("public_profile");
        loginButton.setPermissions(permissions);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult.getAccessToken().getToken());
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });






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
            }
        });
    }

//    Sign In with email and password
    public void signin() {
        if (isValid()) {
            mAuth.signInWithEmailAndPassword(emailSignIn.getText().toString(), passwordSignIn.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                if (user.isEmailVerified()) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("uid", user.getUid());
                                    editor.commit();
                                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                        @Override
                                        public void onSuccess(InstanceIdResult instanceIdResult) {
                                            String token = instanceIdResult.getToken();

                                            db.collection("Tokens").document(user.getUid()).set(new UserToken(token))
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
                                    startActivity(new Intent(SignInActivity.this,  MapsActivity.class));

                                } else {
                                    startActivity(new Intent(SignInActivity.this,  VerifyEmail.class));

                                }
                            } else {
                                emailSignIn.setFocusable(true);
                                emailSignIn.setError("Email is incorrect or not yet signed up");
                                passwordSignIn.setError("Incorrect password");
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }

//    Hide the keyboard
    public void hideKeyBoard(View view) {
                InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.
                getWindowToken(), 0);
    }


}

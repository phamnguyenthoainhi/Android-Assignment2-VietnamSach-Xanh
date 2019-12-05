package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ManageAccountActivity extends AppCompatActivity {
    private static final String TAG = "ManageAccountActivity";

    TextView logoutbtn;
    Button save;
    Button edit;
    String currentUser ;
    TextView welcome;
    EditText accountfirstname;
    EditText accountemail;
    EditText accountphone;
    EditText accountlastname;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser loggeduser;
    User user;
    ArrayList<String> sidlist = new ArrayList<>();
    ArrayList<Site> sites = new ArrayList<>();


//    Fetch related information of logged in user
    public void fetchCurrentUser() {
        db.collection("Users").document(loggeduser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = new User();

                    if (task.getResult().get("lastname") == null) {
                        user.setLastname("");
                    } else {
                        user.setLastname(task.getResult().get("lastname").toString());
                        accountlastname.setText(user.getLastname());

                        welcome.setText("Welcome, "+ user.getFirstname() + " " + user.getLastname());
                    }

                    if (task.getResult().get("phone") == null) {
                        user.setPhone("");

                    } else {
                        user.setPhone(task.getResult().get("phone").toString());
                        accountphone.setText(user.getPhone());
                    }

                    if (task.getResult().get("gender") == null) {
                        user.setGender("Other");

                    } else {
                        user.setGender(task.getResult().get("gender").toString());

                    }

                    if (task.getResult().get("firstname") == null){
                        user.setFirstname("");
                    } else {
                        user.setFirstname(task.getResult().get("firstname").toString());
                        accountfirstname.setText(user.getFirstname());
                        welcome.setText("Welcome, "+ user.getFirstname() + " " + user.getLastname());
                    }

                    if (task.getResult().get("email") == null) {
                        user.setEmail("");
                    } else {
                        user.setEmail(task.getResult().get("email").toString());
                        accountemail.setText(user.getEmail());
                    }
                }
            }
        });
    }

//    Fetch all the site that the user has joined
    public void fetchSitesByUser() {
        Log.d(TAG, "onComplete: fetch site by user: "+ loggeduser.getUid());
        db.collection("Users").document(loggeduser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: fetch site by user " + task.getResult().get("sites"));

                    sidlist = (ArrayList<String>) task.getResult().get("sites");
                    if (sidlist != null ){
                        for (final String sid: sidlist) {
                            db.collection("Sites").document(sid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Site site = new Site();
                                    site.setLocation(task.getResult().get("location").toString());
                                    site.setName(task.getResult().get("name").toString());
                                    site.setDateTime((Long) task.getResult().get("dateTime"));
                                    sites.add(site);

                                    RecyclerView recyclerView = findViewById(R.id.historyrecyclerview);
                                    recyclerView.setHasFixedSize(true);
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(ManageAccountActivity.this);
                                    recyclerView.setLayoutManager(layoutManager);
                                    HistorySiteAdapter adapter = new HistorySiteAdapter(sites);
                                    recyclerView.setAdapter(adapter);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

// Update user with new values
    private void updateUser(){
        db.collection("Users").document(loggeduser.getUid()).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully updated user.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to update user.");
                    }
                });
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        SharedPreferences shared = getSharedPreferences("id", MODE_PRIVATE);
        currentUser = (shared.getString("uid", ""));
        logoutbtn = findViewById(R.id.logoutbtn);
        save = findViewById(R.id.saveaccount);
        edit = findViewById(R.id.editaccount);
        welcome = findViewById(R.id.welcomeaccount);
        accountemail = findViewById(R.id.accountemail);
        accountfirstname = findViewById(R.id.accountfirstname);
        accountlastname = findViewById(R.id.accountlastname);
        accountphone = findViewById(R.id.accountphone);
        accountfirstname.setEnabled(false);
        accountlastname.setEnabled(false);
        accountphone.setEnabled(false);
        accountemail.setEnabled(false);

        save.setVisibility(View.INVISIBLE);
        logoutbtn.setText("Log Out");
        logoutbtn.setClickable(true);
        mAuth = FirebaseAuth.getInstance();
        loggeduser = mAuth.getCurrentUser();

        fetchCurrentUser();
        fetchSitesByUser();


        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                finish();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setVisibility(View.INVISIBLE);
                save.setVisibility(View.VISIBLE);
                accountfirstname.setEnabled(true);
                accountlastname.setEnabled(true);
                accountphone.setEnabled(true);
                accountemail.setEnabled(true);
                accountfirstname.requestFocus();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageAccountActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Do you want to apply these changes?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.setFirstname(accountfirstname.getText().toString());
                                user.setLastname(accountlastname.getText().toString());
                                user.setPhone(accountphone.getText().toString());
                                user.setEmail(accountemail.getText().toString());

                                if (!accountemail.getText().toString().isEmpty()) {
                                    loggeduser.updateEmail(accountemail.getText().toString().trim());
                                }
                                welcome.setText("Welcome, "+ accountfirstname.getText().toString() + " " + accountlastname.getText().toString());
                                updateUser();
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                accountfirstname.setText(user.getFirstname());
                                accountlastname.setText(user.getLastname());
                                accountphone.setText(user.getPhone());
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();

                save.setVisibility(View.INVISIBLE);
                edit.setVisibility(View.VISIBLE);

                accountfirstname.setEnabled(false);
                accountlastname.setEnabled(false);
                accountphone.setEnabled(false);
            }
        });

        Button back = findViewById(R.id.fromaccount);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent (ManageAccountActivity.this, MapsActivity.class));
                finish();
            }
        });
        Log.d(TAG, "onCreate: fetchsites "+ sites);

    }

}

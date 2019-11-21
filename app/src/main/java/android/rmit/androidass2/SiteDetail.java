package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.rmit.androidass2.R;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.List;

public class SiteDetail extends AppCompatActivity {
    TextView sitelocation;
    TextView sitedate;
    TextView siteinfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Site site = new Site();
    String TAG = "Site Detail";
    Button invite;
    String siteId="";
    EditText email;
    SharedPreferences sharedPreferences;
    String userId;

    public void fetchSelectedSite(String selectedSite) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        db.collection("Sites").document(selectedSite).get()

                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentSnapshot = task.getResult();
                    site = documentSnapshot.toObject(Site.class);

                    site.setId(documentSnapshot.getId());
                    sitelocation.setText(site.getLocation());
                    siteinfo.setText(site.getName());
                    sitedate.setText(convertDate(site.getDateTime()));
                    Log.d(TAG, "onComplete: site detail object"+ site.getDateTime());


                }
            }
        })
        ;

    }

    public String convertDate(long millsec) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millsec);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        int mHour = calendar.get(Calendar.HOUR);
        int mMinute = calendar.get(Calendar.MINUTE);

        String s = "";

        if (mHour <= 9 && mMinute <= 9) {
            s = "0" + mHour + ":" + "0" + mMinute + ", " + mDay + "/" + mMonth + "/" + mYear;
        }
        if (mHour <= 9 && mMinute > 9) {
            s = "0" + mHour + ":" + mMinute + ", " + mDay + "/" + mMonth + "/" + mYear;
        }
        if (mHour > 9 && mMinute > 9) {
            s = mHour + ":" + mMinute + ", " + mDay + "/" + mMonth + "/" + mYear;
        }
        if (mHour > 9 && mMinute  <= 9) {
            s = mHour + ":" + "0" + mMinute + ", " + mDay + "/" + mMonth + "/" + mYear;
        }

        return s;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);
        sitedate = findViewById(R.id.sitedate);
        siteinfo = findViewById(R.id.siteinfo);
        sitelocation = findViewById(R.id.sitelocation);
        Button backbtn = findViewById(R.id.back);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SiteDetail.this, MapsActivity.class));
            }
        });
        onNewIntent(getIntent());
        sharedPreferences = getSharedPreferences("id", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("uid",null);
        final Button join = findViewById(R.id.joinbutton);



        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((site.getOwner())!=null && !((site.getOwner()).equals(userId))){
                    join.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SiteDetail.this)
                                    .setTitle("Confirmation")
                                    .setMessage("Do you want to join this clean up site? \n" + site.getLocation())
                                    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(SiteDetail.this, "Success", Toast.LENGTH_SHORT).show();

                                            List<String> volunteers = site.getVolunteers();
                                            volunteers.add(userId);
                                            site.setVolunteers(volunteers);


                                            DocumentReference siteRef = db.collection("SitesVolunteers").document(site.getId());
                                            siteRef.update("volunteers",volunteers)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG,"Successfully updated!");
                                                            join.setVisibility(View.GONE);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG,"Failed to update.");
                                                        }
                                                    });
                                            db.collection("Users").document(userId).get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            User user = documentSnapshot.toObject(User.class);
                                                            if(user!=null){
                                                                List<String> volunteeredSites = user.getSites();
                                                                volunteeredSites.add(site.getId());
                                                                db.collection("Users").document(userId)
                                                                        .update("sites",volunteeredSites)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Log.d(TAG,"Successfully updated user.");
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.w(TAG,"Failed to update user.");
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });

                                        }
                                    })
                                    .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(SiteDetail.this, "Failure", Toast.LENGTH_SHORT).show();
                                            dialogInterface.dismiss();
                                        }
                                    });

                            builder.create().show();
                        }
                    });

                }else{
                    join.setVisibility(View.GONE);
                }}


        });
        invite = findViewById(R.id.invite);

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogInvite();

//                Intent intent = new Intent(SiteDetail.this,InviteActivity.class);
//                intent.putExtra("siteId",siteId);
//                startActivity(intent);
            }
        });

        Log.d(TAG, "onCreate: test" + convertDate(1300018752992l));

    }

    public void showDialogInvite() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(SiteDetail.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.activity_invite, null);
        email=dialogView.findViewById(R.id.email_invite);

        Button invite =dialogView.findViewById(R.id.invite);

        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);




        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("Users").whereEqualTo("email", email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){



                            Log.d(TAG, "onComplete: invite "+ task.getResult().getDocuments());



//                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);



//                            User user = documentSnapshot.toObject(User.class);
//                            if(user.getUserNotifications().size()>0){
//                                notifications.addAll(user.getUserNotifications());
//                            }

//                            UserNotification userNotification = new UserNotification("You have a new invitation!", "invitation", siteId, userId, documentSnapshot.getId());

//                            List<FieldValue> fieldValues = new ArrayList<>();
//                            for(UserNotification userNotification:notifications){
//                                fieldValues.add(FieldValue.arrayUnion(userNotification));
//                            }
//
//                            List<HashMap<String,String>> notifs = new ArrayList<>();
//
//                            for(UserNotification userNotification:notifications){
//                                HashMap<String,String> notif = new HashMap<>();
//                                notif.put("content",userNotification.getContent());
//                                notif.put("type",userNotification.getType());
//                                notif.put("siteId",userNotification.getSiteId());
//                                notif.put("from",userId);
//                                notif.put("to",documentSnapshot.getId());
//                                notifs.add(notif);
//                            }

//                            db.collection("Notifications").add(userNotification)
//                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                        @Override
//                                        public void onSuccess(DocumentReference documentReference) {
//                                            Toast.makeText(SiteDetail.this, "Successfully posted notification", Toast.LENGTH_SHORT).show();
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(SiteDetail.this, "Failed to post notification", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                            finish();
                        }
                        else{
                            Toast.makeText(SiteDetail.this, "failed to get user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){

        final Bundle bundle = intent.getExtras();
        Log.d(TAG, "processIntent: " + (String)bundle.get("id"));
        fetchSelectedSite((String)bundle.get("id"));
        siteId = (String)bundle.get("id");
    }




}

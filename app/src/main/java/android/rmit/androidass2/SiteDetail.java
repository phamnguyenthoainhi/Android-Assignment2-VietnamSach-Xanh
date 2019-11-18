package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.util.Formatter;
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

    public void fetchSelectedSite(String selectedSite) {
//        SharedPreferences sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
//
//        final String selectedSite = (sharedPreferences.getString("sid", ""));
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
//        db.collection("sites").whereArrayContains(selected)

        db.collection("Sites").document(selectedSite).get()

                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentSnapshot = task.getResult();
                    site = documentSnapshot.toObject(Site.class);
                    site.setId(documentSnapshot.getId());
//                    site.setLocation(task.getResult().get("location").toString());
//                    site.setOwner(task.getResult().get("owner").toString());
                    sitelocation.setText(site.getLocation());
                    System.out.println("Test " + site.getLocation());
                }
            }
        })
        ;


//        Toast.makeText(this, "" + site.getLocation(), Toast.LENGTH_SHORT).show();

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

        final Button join = findViewById(R.id.joinbutton);

        SharedPreferences sharedPreferences = getSharedPreferences("id", Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString("uid",null);

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

                                            DocumentReference siteRef = db.collection("Sites").document(site.getId());
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
                Intent intent = new Intent(SiteDetail.this,InviteActivity.class);
                intent.putExtra("siteId",siteId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent){
        final Bundle bundle = intent.getExtras();
        fetchSelectedSite((String)bundle.get("id"));
        siteId = (String)bundle.get("id");
    }


}

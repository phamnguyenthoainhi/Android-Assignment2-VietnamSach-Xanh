package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class SiteInfoActivity extends AppCompatActivity {

    Site site;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView name;
    TextView address;
    TextView dateTime;

    Button invite;

    public void fetchSite(String id){
        db.collection("Sites").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    site = documentSnapshot.toObject(Site.class);
                    site.setId(documentSnapshot.getId());
                    //Toast.makeText(SiteInfoActivity.this, site.toString(), Toast.LENGTH_SHORT).show();

                    name.setText(site.getName());
                    address.setText(site.getLocation());
                    dateTime.setText(site.getDateTime()+"");

                }
                else{
                    Log.d("Load site","Failed to load site.");
                    Toast.makeText(SiteInfoActivity.this, "failed", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_info);

        Intent intent = getIntent();

        final Bundle bundle = intent.getExtras();
        fetchSite((String)bundle.get("id"));

        name = findViewById(R.id.site_name);
        address = findViewById(R.id.site_addess);
        dateTime = findViewById(R.id.date_time);

        invite = findViewById(R.id.invite);

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SiteInfoActivity.this,InviteActivity.class);
                intent.putExtra("siteId",(String)bundle.get("id"));
                startActivity(intent);
            }
        });


    }
}

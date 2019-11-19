package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SitesActivity extends AppCompatActivity implements SiteAdapter.SiteViewHolder.OnSiteListener {
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;
    Button joinbtn;


    String TAG = "Site Activity";

    ArrayList<Site> sites = new ArrayList<>();

    public void fetchCurrentUser() {
        SharedPreferences shared = getSharedPreferences("id", MODE_PRIVATE);
        String currentUser = (shared.getString("uid", ""));
        db.collection("Users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = new User();
                    user.setFirstname(task.getResult().get("firstname").toString());
                    user.setLastname(task.getResult().get("lastname").toString());
                    user.setPhone(task.getResult().get("phone").toString());
                    user.setGender(task.getResult().get("gender").toString());
                }
            }
        });
    }

    public void fetchSites() {
        db.collection("Sites")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Site site = document.toObject(Site.class);
                                sites.add(site);
                                adapter.notifyDataSetChanged();
                            }

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void initRecyclerView() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SiteAdapter(sites, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fetchSites();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sites);
        View siteView = getLayoutInflater().inflate(R.layout.site, null);
        fetchCurrentUser();

        initRecyclerView();
        Button createSite = findViewById(R.id.addnewsite);
        createSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SitesActivity.this,CreateSiteActivity.class));
            }
        });
    }

    @Override
    public void onSiteClick(int position) {
        sites.get(position);
        Toast.makeText(this, ""+sites.get(position).getId(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SitesActivity.this, ManageSiteActivity.class));
    }
}

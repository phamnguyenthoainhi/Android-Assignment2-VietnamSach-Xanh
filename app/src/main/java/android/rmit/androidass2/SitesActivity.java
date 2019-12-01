package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SitesActivity extends AppCompatActivity implements SiteAdapter.SiteViewHolder.OnSiteListener {
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;
    String TAG = "Site Activity";
    private String superuser = "QnZasbpIgNMYpCQ8BIy682YwxS93";
    ArrayList<Site> sites;


    FirebaseUser loggeduser;


//    Fetch all information of current user
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

                    }
                    if (task.getResult().get("phone") == null) {
                        user.setPhone("");

                    } else {
                        user.setPhone(task.getResult().get("phone").toString());

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

                    }

                }
            }
        });
    }


    public void fetchSites(){
        db.collection("Sites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }

//    Fetch information of a site based on owner id
    public void fetchSiteByOwnerId() {
        if (loggeduser.getUid() != null) {
            db.collection("Sites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            Log.d(TAG, "onComplete: fetch" + queryDocumentSnapshot.toString());
                        }
                        db.collection("Sites").whereEqualTo("owner", loggeduser.getUid())
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Site site = document.toObject(Site.class);
                                        site.setId(document.getId());
                                        sites.add(site);
                                        initRecyclerView();

                                    }
                                }
                            }
                        });
                    }
                }
            });

        }
    }

//    Fetch all sites if current user is super user
    public void fetchSiteBySuperUser() {
        if (loggeduser!= null) {
            db.collection("Sites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            Log.d(TAG, "onComplete: fetch" + queryDocumentSnapshot.toString());
                            Site site = queryDocumentSnapshot.toObject(Site.class);
                            site.setId(queryDocumentSnapshot.getId());
                            sites.add(site);
                            initRecyclerView();
                        }
                    }
                }
            });
        }
    }


    public void initRecyclerView() {

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SiteAdapter(sites, this);
        new ItemTouchHelper(siteTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

    }

//    Delete a site
    private void deleteSite(final String collectionName, String siteId){
        db.collection(collectionName).document(siteId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SitesActivity.this, "Successfully deleted specified document in collection" +collectionName+ ".", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to delete in collection "+collectionName+".");
                    }
                });
    }

    private void deleteSiteInUserList(final String userId, final String siteId){
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser!=null && currentUser.getSites()!=null){
                            List<String> joinedSite = currentUser.getSites();
                            joinedSite.remove(siteId);
                            db.collection("Users").document(userId)
                                    .update("sites",joinedSite)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(SitesActivity.this, "Successfully removed site from user "+ userId +"'s list.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Failed to remove site from user "+userId+"'s list.");
                                        }
                                    });

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to get user");
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sites);

        sites = new ArrayList<>();
        
        View siteView = getLayoutInflater().inflate(R.layout.site, null);
        mAuth= FirebaseAuth.getInstance();
        loggeduser = mAuth.getCurrentUser();
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        Button reportbtn = findViewById(R.id.report);

        if (!loggeduser.getUid().equals(superuser)) {
            reportbtn.setVisibility(View.INVISIBLE);
            fetchSiteByOwnerId();
        } else {
           fetchSiteBySuperUser();
        }

        reportbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SitesActivity.this, ReportActivity.class));
            }
        });

        fetchCurrentUser();

        Button button = findViewById(R.id.fromsites);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SitesActivity.this, MapsActivity.class));
            }
        });

        Button createSite = findViewById(R.id.addnewsite);
        createSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SitesActivity.this,CreateSiteActivity.class));
            }
        });
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipetorefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sites = new ArrayList<>();
                fetchSites();
                if (loggeduser.getUid().equals(superuser)){
                    fetchSiteBySuperUser();
                } else {
                    fetchSiteByOwnerId();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public void onSiteClick(int position) {
        sites.get(position);
        Toast.makeText(this, ""+sites.get(position).getId(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SitesActivity.this, ManageSiteActivity.class);
        intent.putExtra("selectedsiteid",sites.get(position).getId());
        startActivity(intent);
        finish();
    }

    @Override
    public void deleteSite(final int position) {
        final Site site = sites.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(SitesActivity.this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this site? \n" + site.getLocation())
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        db.collection("SitesVolunteers").document(site.getId()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Site result = documentSnapshot.toObject(Site.class);
                                        if (result!=null && result.getVolunteers()!=null){
                                            List<String> volunteers = result.getVolunteers();
                                            for(final String volunteer:volunteers){
                                                deleteSiteInUserList(volunteer,site.getId());
                                            }
                                        }
                                        deleteSite("SitesVolunteers",site.getId());
                                    }
                                });

                        deleteSite("Sites",site.getId());
                        deleteSite("Reports",site.getId());
                        sites.remove(site);
                        adapter.notifyDataSetChanged();

                    }


                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                });
        builder.create().show();

    }

    ItemTouchHelper.SimpleCallback siteTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (sites.size() > 0) {
                final Site site = sites.get(viewHolder.getAdapterPosition());
                AlertDialog.Builder builder = new AlertDialog.Builder(SitesActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Do you want to delete this site? \n" + site.getLocation())
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection("SitesVolunteers").document(site.getId()).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Site result = documentSnapshot.toObject(Site.class);
                                                if (result!=null && result.getVolunteers()!=null){
                                                    List<String> volunteers = result.getVolunteers();
                                                    for(final String volunteer:volunteers){
                                                        deleteSiteInUserList(volunteer,site.getId());
                                                    }
                                                }
                                                deleteSite("SitesVolunteers",site.getId());
                                            }
                                        });

                                deleteSite("Sites",site.getId());
                                deleteSite("Reports",site.getId());
                                sites.remove(site);
                                adapter.notifyDataSetChanged();

                            }


                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                adapter.notifyDataSetChanged();
                            }
                        });
                builder.create().show();
            }

        }
    };
}

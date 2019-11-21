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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SitesActivity extends AppCompatActivity implements SiteAdapter.SiteViewHolder.OnSiteListener {
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;
    Button joinbtn;
    String TAG = "Site Activity";

    ArrayList<Site> sites;

    String currentUser ;


    public void fetchCurrentUser() {
        Log.d(TAG, "fetchCurrentUser: "+currentUser);
        System.out.println("hello");

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

    public void fetchSites(){
        db.collection("Sites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                }
            }
        });

    }

    public void fetchSiteByOwnerId() {
        if (currentUser != null) {
            db.collection("Sites").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            Log.d(TAG, "onComplete: fetch" + queryDocumentSnapshot.toString());
                        }

                        db.collection("Sites").whereEqualTo("owner", currentUser)
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

    public void initRecyclerView() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SiteAdapter(sites, this);
        new ItemTouchHelper(siteTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

            SharedPreferences shared = getSharedPreferences("id", MODE_PRIVATE);
            currentUser = (shared.getString("uid", ""));

        fetchSiteByOwnerId();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sites);

        sites = new ArrayList<>();
        Button exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        View siteView = getLayoutInflater().inflate(R.layout.site, null);
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);

        fetchCurrentUser();



        Button createSite = findViewById(R.id.addnewsite);
        createSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SitesActivity.this,CreateSiteActivity.class));
                finish();
            }
        });
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipetorefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sites = new ArrayList<>();
                fetchSites();
                fetchSiteByOwnerId();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        ArrayList<Site> prevList = new ArrayList<>();
//        prevList.addAll(sites);
//        fetchSiteByOwnerId();
//        if (prevList.size()!=sites.size()){
//            adapter.notifyDataSetChanged();
//        }
//    }

    @Override
    public void onSiteClick(int position) {
        sites.get(position);
        Toast.makeText(this, ""+sites.get(position).getId(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SitesActivity.this, ManageSiteActivity.class);
        intent.putExtra("selectedsiteid",sites.get(position).getId());
        startActivity(intent);

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
                        db.collection("Sites").document(site.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("SitesVolunteers").document(site.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(SitesActivity.this, "Successfully deleted site.", Toast.LENGTH_SHORT).show();
                                                        db.collection("Reports").document(site.getId())
                                                                .delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(SitesActivity.this, "Successfully deleted associated report.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG,"Failed to delete report.");
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG,"Failed to delete site with volunteers list.");
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG,"Failed to delete site.");
                                    }
                                });
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
            final Site site = sites.get(viewHolder.getAdapterPosition());
//            sites.remove(viewHolder.getAdapterPosition());
//            TODO: add delete function for backend
            AlertDialog.Builder builder = new AlertDialog.Builder(SitesActivity.this)
                    .setTitle("Confirmation")
                    .setMessage("Do you want to delete this site? \n" + site.getLocation())
                    .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.collection("Sites").document(site.getId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            db.collection("SitesVolunteers").document(site.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(SitesActivity.this, "Successfully deleted site.", Toast.LENGTH_SHORT).show();
                                                            db.collection("Reports").document(site.getId())
                                                                    .delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(SitesActivity.this, "Successfully deleted associated report.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(TAG,"Failed to delete report.");
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG,"Failed to delete site with volunteers list.");
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG,"Failed to delete site.");
                                        }
                                    });
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

//            adapter.notifyDataSetChanged();

        }

    };

}

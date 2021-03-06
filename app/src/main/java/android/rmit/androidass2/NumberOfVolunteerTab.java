package android.rmit.androidass2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class NumberOfVolunteerTab extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "NumberOfVolunteerTab";
    Site site;
    ArrayList<String> volunteersid = new ArrayList<>();

    TextView volunteername ;
    TextView volunteerphone;
    TextView volunteeremail;


    ArrayList<User> volunteers = new ArrayList<>();
    User volunteer;
    RecyclerView recyclerView;
    Context context;
    VolunteerAdapter volunteerAdapter;


//Fetch all volunteers of a site
    public ArrayList<String> fetchVolunteersId(String id) {

        db.collection("SitesVolunteers").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            volunteersid = (ArrayList<String>) documentSnapshot.get("volunteers");
                            Log.d(TAG, "onComplete: volunteers in a site "+ volunteersid);

                            fetchvolunteerinfo(volunteers);
                        }


                    }
                });
        return volunteersid;
    }


//Fetch all information of users who are volunteers
    public void fetchvolunteerinfo(final ArrayList<User> volunteers) {
        if (!volunteersid.isEmpty()) {
            for (String id: volunteersid) {
                db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        volunteer = new User();
                        if(task.getResult().get("email")!=null) {
                            volunteer.setEmail(task.getResult().get("email").toString());
                        } else {
                            volunteer.setEmail("");
                        }
                        if(task.getResult().get("firstname")!=null) {
                            volunteer.setFirstname(task.getResult().get("firstname").toString());
                        } else {
                            volunteer.setFirstname("");
                        }
                        if(task.getResult().get("lastname")!=null) {
                            volunteer.setLastname(task.getResult().get("lastname").toString());
                        } else {
                            volunteer.setLastname("");
                        }
                        if(task.getResult().get("phone")!=null) {
                            volunteer.setPhone(task.getResult().get("phone").toString());
                        } else {
                            volunteer.setPhone("");
                        }

                        volunteers.add(volunteer);
                        volunteerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }


    public void initRecycleView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        volunteerAdapter = new VolunteerAdapter(volunteers, context);
        recyclerView.setAdapter(volunteerAdapter);
    }

    public void requestData(final String siteId, String userId){
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        HashMap<String, String> request = new HashMap<>();
                        request.put("siteId",siteId);
                        request.put("email",(String)documentSnapshot.get("email"));

                        db.collection("Requests").add(request)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(context, "Your request has been sent. Please check your email for incoming mail containing the requested data.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Fail to create request. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to fetch user.",e);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.volunteer_tab, container, false);
        ManageSiteActivity manageSiteActivity = (ManageSiteActivity) getActivity();
        SharedPreferences sharedPreferences = manageSiteActivity.getSharedPreferences("id",Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString("uid",null);
        final String sid = manageSiteActivity.getid();
        context = getContext();
        recyclerView = view.findViewById(R.id.vltrecyclerview);

        fetchVolunteersId(sid);

        Button download = view.findViewById(R.id.downloadbutton);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirmation")
                        .setMessage("Do you want to request for this site's data?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestData(sid, FirebaseAuth.getInstance().getUid());
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
        });

        initRecycleView();

        return view;
    }


}
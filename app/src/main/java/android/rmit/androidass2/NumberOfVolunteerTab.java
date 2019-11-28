package android.rmit.androidass2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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


    public ArrayList<String> fetchVolunteersId(String id) {
        Log.d(TAG, "fetchVolunteersId: called");

        db.collection("Sites").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        volunteersid = (ArrayList<String>) documentSnapshot.get("volunteers");

                        fetchvolunteerinfo();

                    }
                });
        return volunteersid;

    }

    public void fetchvolunteerinfo() {
        Log.d(TAG, "fetchvolunteerinfo: called");
        if (!volunteersid.isEmpty()) {
            Log.d(TAG, "fetchvolunteerinfo: not empty");
            for (String id: volunteersid) {
                db.collection("Users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        volunteer = new User();
                        volunteers = new ArrayList<>();
                        volunteer.setEmail(task.getResult().get("email").toString());
                        volunteer.setFirstname(task.getResult().get("firstname").toString());
                        volunteer.setLastname(task.getResult().get("lastname").toString());
                        volunteer.setPhone(task.getResult().get("phone").toString());
                        volunteers.add(volunteer);
                        Log.d(TAG, "onComplete: fetch volunteers" + volunteers.toString());
                        
                        recyclerView.setHasFixedSize(true);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(layoutManager);
                        VolunteerAdapter volunteerAdapter = new VolunteerAdapter(volunteers, context);
                        recyclerView.setAdapter(volunteerAdapter);



                    }
                });
            }
        }
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
                                requestData(sid,userId);
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

        return view;
    }


}
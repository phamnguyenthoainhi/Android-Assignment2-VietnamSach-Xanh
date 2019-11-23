package android.rmit.androidass2;

import android.os.Bundle;
import android.rmit.androidass2.ManageSiteActivity;
import android.rmit.androidass2.R;
import android.rmit.androidass2.Site;
import android.rmit.androidass2.User;
import android.rmit.androidass2.VolunteerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import android.content.Context;
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






    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.volunteer_tab, container, false);



        ManageSiteActivity manageSiteActivity = (ManageSiteActivity) getActivity();
        String sid = manageSiteActivity.getid();
        context = getContext();
        recyclerView = view.findViewById(R.id.vltrecyclerview);

        Log.d(TAG, "onCreateView: id "+ sid);

        fetchVolunteersId(sid);








        return view;
    }


}
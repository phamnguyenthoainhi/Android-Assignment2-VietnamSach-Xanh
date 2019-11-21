package android.rmit.androidass2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class OutcomeTab extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TAG = "Outcome Tab";
    private EditText garbage;
    Button edit;
     EditText volunteers;

    void fetchDataReport(String id){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        db.collection("Reports").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DataReport dataReport = documentSnapshot.toObject(DataReport.class);
                        if (dataReport!=null){
                            garbage.setText(dataReport.getAmountOfGarbage()+"");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,e);
                    }
                });
    }

    void updateReport(String id, String newData){
        DocumentReference siteRef = db.collection("Reports").document(id);
        siteRef.update("amountOfGarbage",Integer.parseInt(newData))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Successfully updated!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to update.");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.outcome_tab, container, false);

        garbage = view.findViewById(R.id.noGarbage);
        volunteers = view.findViewById(R.id.noVolunteers);
        garbage.setEnabled(false);
        volunteers.setEnabled(false);

        final Button edit = view.findViewById(R.id.editbuttonoutcome);
        final Button save = view.findViewById(R.id.savebuttonoutcome);


        save.setVisibility(View.INVISIBLE);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                save.setVisibility(View.VISIBLE);
                edit.setVisibility(View.INVISIBLE);
                garbage.setEnabled(true);
                volunteers.setEnabled(true);

            }
        });

        ManageSiteActivity manageSiteActivity = (ManageSiteActivity) getActivity();
        final String sid = manageSiteActivity.getid();

        fetchDataReport(sid);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirmation")
                        .setMessage("Do you want to update?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateReport(sid,garbage.getText().toString());
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
                save.setVisibility(View.INVISIBLE);

                edit.setVisibility(View.VISIBLE);
                garbage.setEnabled(false);
                volunteers.setEnabled(false);
            }
        });

        return view;
    }


}

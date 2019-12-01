package android.rmit.androidass2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DetailTab extends Fragment {
    private EditText sitelocation;
    private EditText sitedate;
    private EditText siteinfo;
    private Site site;
    private Button savebtn;
    private Button editbtn;
    private List<Place.Field> fields;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private long dateTime;


    private static final String TAG = "DetailTab";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


//    Fetch all the related information of a site based on its id
    private void fetchdetailbyid(String id) {
        db.collection("Sites").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentSnapshot = task.getResult();
                    site = documentSnapshot.toObject(Site.class);

                    site.setId(documentSnapshot.getId());
                    sitelocation.setText(site.getLocation());
                    siteinfo.setText(site.getName());
                    sitedate.setText(convertDate(site.getDateTime()));
                    dateTime=site.getDateTime();

                    Calendar currentValues = Calendar.getInstance();
                    currentValues.setTimeInMillis(site.getDateTime());

                    datePicker.updateDate(currentValues.get(Calendar.YEAR),currentValues.get(Calendar.MONTH),currentValues.get(Calendar.DAY_OF_MONTH));
                    timePicker.setCurrentHour(currentValues.get(Calendar.HOUR));
                    timePicker.setCurrentMinute(currentValues.get(Calendar.MINUTE));
                }

            }
        });

    }

//    Convert from milliseconds to form "HH:mm:ss , dd/mm/yy"
    private String convertDate(long millsec) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millsec);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
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

//    Update site with new values
    private void updateSite(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        db.collection("Sites").document(site.getId())
                .set(site)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Successfully updated site info.");
                        db.collection("SitesVolunteers").document(site.getId())
                                .update("location",site.getLocation(),
                                        "dateTime",site.getDateTime(),
                                        "name", site.getName()
                                )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG,"Successfully updated site volunteer info.");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Failed to update site info.");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.detail_tab, container, false);

        final ManageSiteActivity manageSiteActivity = (ManageSiteActivity) getActivity();
        final String sid = manageSiteActivity.getid();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        sitelocation = view.findViewById(R.id.sitelocationtab);
        sitedate = view.findViewById(R.id.sitedatetab);
        siteinfo = view.findViewById(R.id.siteinfotab);
        editbtn = view.findViewById(R.id.editbuttondetail);
        savebtn = view.findViewById(R.id.savebuttondetail);
        fetchdetailbyid(sid);

        if (currentUser.getUid().equals("QnZasbpIgNMYpCQ8BIy682YwxS93")){
            editbtn.setVisibility(View.INVISIBLE);
        }

        sitelocation.setEnabled(false);
        sitedate.setEnabled(false);
        siteinfo.setEnabled(false);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editbtn.setVisibility(View.INVISIBLE);
                sitelocation.setEnabled(true);
                sitedate.setEnabled(true);
                siteinfo.setEnabled(true);
                savebtn.setVisibility(View.VISIBLE);


            }
        });
        savebtn.setVisibility(View.INVISIBLE);
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirmation")
                        .setMessage("Do you want to apply these changes?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                site.setDateTime(dateTime);
                                site.setLocation(sitelocation.getText().toString());
                                site.setName(siteinfo.getText().toString());
                                updateSite();
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sitelocation.setText(site.getLocation());
                                sitedate.setText(convertDate(site.getDateTime()));
                                siteinfo.setText(site.getName());
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
                savebtn.setVisibility(View.INVISIBLE);
                editbtn.setVisibility(View.VISIBLE);
                sitelocation.setEnabled(false);
                sitedate.setEnabled(false);
                siteinfo.setEnabled(false);

            }
        });



        fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS);

        sitelocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields)
                        .setCountry("VN")
                        .build(manageSiteActivity);
                startActivityForResult(intent,AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        String key = getString(R.string.google_maps_key);
        if(!Places.isInitialized()){
            Places.initialize(manageSiteActivity.getApplicationContext(),key);
        }

        PlacesClient placesClient = Places.createClient(manageSiteActivity);
        final View dialogView = View.inflate(manageSiteActivity, R.layout.picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(manageSiteActivity).create();

        datePicker = dialogView.findViewById(R.id.date_picker);
        timePicker = dialogView.findViewById(R.id.time_picker);

        datePicker.setMinDate(1587229200000L);
        datePicker.setMaxDate(1587747600000L);

        sitedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (timePicker.getCurrentHour() >= 6 && timePicker.getCurrentHour() < 21) {
                            Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(),
                                    timePicker.getCurrentHour(),
                                    timePicker.getCurrentMinute());

                            long time = calendar.getTimeInMillis();

                            dateTime = time;

                            sitedate.setText(convertDate(time));

                            alertDialog.dismiss();
                        }
                        else{
                            Toast.makeText(view.getContext(), "Please pick a valid time between 6 AM and 9 PM", Toast.LENGTH_SHORT).show();
                        }
                    }});
                alertDialog.setView(dialogView);
                alertDialog.show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AUTOCOMPLETE_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                Place place = Autocomplete.getPlaceFromIntent(data);
                sitelocation.setText(place.getAddress());
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("Autocomplete", status.getStatusMessage());
        }else if(resultCode == RESULT_CANCELED){}
    }
}


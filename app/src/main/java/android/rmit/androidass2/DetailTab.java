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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
import static android.content.Context.MODE_PRIVATE;

public class DetailTab extends Fragment {
    EditText sitelocation;
    EditText sitedate;
    EditText siteinfo;
    Site site;
    Button savebtn;
    Button editbtn;
    Button deletebtn;
    List<Place.Field> fields;
    int AUTOCOMPLETE_REQUEST_CODE = 1;


    private static final String TAG = "DetailTab";
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void fetchdetailbyid(String id) {
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
                }

            }
        });

    }

    public String convertDate(long millsec) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millsec);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        int mHour = calendar.get(Calendar.HOUR);
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

    private void updateSite(final String sid, final String newInfo){

        db.collection("Sites").document(sid)
                .set(site)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Successfully updated site info.");
                        db.collection("SitesVolunteers").document(sid)
                                .update("location",site.getLocation(),
                                        "dateTime",site.getDateTime(),
                                        "name", newInfo
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

        sitelocation = view.findViewById(R.id.sitelocationtab);
        sitedate = view.findViewById(R.id.sitedatetab);
        siteinfo = view.findViewById(R.id.siteinfotab);
        editbtn = view.findViewById(R.id.editbuttondetail);
        savebtn = view.findViewById(R.id.savebuttondetail);
        deletebtn = view.findViewById(R.id.deletedetail);
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
                deletebtn.setVisibility(View.VISIBLE);

            }
        });
        savebtn.setVisibility(View.INVISIBLE);
        deletebtn.setVisibility(View.INVISIBLE);

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirmation")
                        .setMessage("Do you want to apply these changes?")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateSite(sid,siteinfo.getText().toString());

                                savebtn.setVisibility(View.INVISIBLE);
                                editbtn.setVisibility(View.VISIBLE);
                                sitelocation.setEnabled(false);
                                sitedate.setEnabled(false);
                                siteinfo.setEnabled(false);
                                deletebtn.setVisibility(View.INVISIBLE);
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

        fetchdetailbyid(sid);


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
        sitedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);

                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute());

                        long time = calendar.getTimeInMillis();

                        site.setDateTime(time);

                        sitedate.setText(convertDate(time));

                        alertDialog.dismiss();
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
                site.setLocation(place.getAddress());
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("Autocomplete", status.getStatusMessage());
        }else if(resultCode == RESULT_CANCELED){}
    }
}

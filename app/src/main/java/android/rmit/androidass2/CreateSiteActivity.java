package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.common.collect.Maps;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateSiteActivity extends AppCompatActivity {


    String TAG="Add site";
    long time;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_site);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS));

        autocompleteSupportFragment.setCountry("VN");

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i("AUTO COMPLETE","Place: "+place.getName()+", "+place.getId());
                location = place.getAddress();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("AUTO COMPLETE","An error occurred: "+status);
            }
        });

        String key = getString(R.string.google_maps_key);
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),key);
        }

        PlacesClient placesClient = Places.createClient(this);

        final EditText name = findViewById(R.id.name);
        final View dialogView = View.inflate(this, R.layout.picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        final View dateTime = findViewById(R.id.time);

        dateTime.setOnClickListener(new View.OnClickListener() {
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

                        time = calendar.getTimeInMillis();

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy hh:mm", Locale.getDefault());

                        ((EditText)dateTime).setText(simpleDateFormat.format(time));

                        alertDialog.dismiss();
                    }});
                alertDialog.setView(dialogView);
                alertDialog.show();
            }
        });

        Button submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("id",MODE_PRIVATE);
                String userId = sharedPreferences.getString("uid",null);
                final Site site = new Site(location,time, name.getText().toString(), userId);


                db.collection("Sites")
                        .add(site)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG,"Site added with ID: "+documentReference.getId());

                                final String id = documentReference.getId();

                                db.collection("Reports")
                                        .document(documentReference.getId())
                                        .set(new DataReport())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(CreateSiteActivity.this, "Successfully added base report", Toast.LENGTH_SHORT).show();
                                                db.collection("SitesVolunteers")
                                                        .document(id)
                                                        .set(site)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(CreateSiteActivity.this, "Create site with volunteers", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(CreateSiteActivity.this, "Failed to create site with volunteers", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateSiteActivity.this, "Failed to add base report", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                //startActivity(new Intent(CreateSiteActivity.this, MapsActivity.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG,"Error",e);
                            }
                        });

            }
        });
    }
}

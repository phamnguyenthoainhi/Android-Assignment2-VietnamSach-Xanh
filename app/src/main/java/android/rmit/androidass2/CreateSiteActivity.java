package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CreateSiteActivity extends AppCompatActivity {


    String TAG="Add site";
    long time;
    String location;
    EditText locationInput;
    List<Place.Field> fields;
    int AUTOCOMPLETE_REQUEST_CODE = 1;

    DatePicker datePicker;
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_site);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        locationInput = findViewById(R.id.location);

        fields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS);

        locationInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields)
                        .setCountry("VN")
                        .build(CreateSiteActivity.this);
                startActivityForResult(intent,AUTOCOMPLETE_REQUEST_CODE);
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
        datePicker = dialogView.findViewById(R.id.date_picker);
        timePicker = dialogView.findViewById(R.id.time_picker);
        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.setMaxDate(1587747600000L);

        //datePicker.updateDate(2020,3,22);
        timePicker.setCurrentHour(9);
        timePicker.setCurrentMinute(0);

        dateTime.setOnClickListener(new View.OnClickListener() {
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

                            time = calendar.getTimeInMillis();

                            ((EditText) dateTime).setText(convertDate(time));


                            alertDialog.dismiss();
                        }
                        else{
                            Toast.makeText(CreateSiteActivity.this, "Please pick a valid time between 6 AM and 9 PM", Toast.LENGTH_SHORT).show();
                        }
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

                FirebaseUser loggedUser = FirebaseAuth.getInstance().getCurrentUser();
                final Site site = new Site(location,time, name.getText().toString(), loggedUser.getUid());


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
                                                                //startActivity(new Intent(CreateSiteActivity.this,SitesActivity.class));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AUTOCOMPLETE_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationInput.setText(place.getAddress());
                location = place.getAddress();
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("Autocomplete", status.getStatusMessage());
        }else if(resultCode == RESULT_CANCELED){}
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

}
package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReportActivity extends AppCompatActivity {
    TextView totalGarbage;
    TextView totalVolunteers;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Double> listOfGarbage;
    Long sum = 0L;
    private static final String TAG = "ReportActivity";
    Button fromreport;



    public void fetchReport() {
            db.collection("Reports").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {

                            Long num = (Long) queryDocumentSnapshot.get("amountOfGarbage");
                            sum += num;
                        }

                        totalGarbage.setText(sum.toString());
                    }
                }
            });

    }
    public void fetchVolunteers() {
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Integer count = 0;
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {



                        ArrayList<String> sites = (ArrayList<String>) queryDocumentSnapshot.get("sites");


                        if (!(sites == null || sites.size() == 0)) {
                            Log.d(TAG, "onComplete: 11");
                            count +=1;
                        }

                    }
                    totalVolunteers.setText(String.valueOf(count));

                }

            }
        });
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        totalGarbage = findViewById(R.id.totalgarbage);
        totalVolunteers = findViewById(R.id.totalVolunteers);
        fromreport = findViewById(R.id.fromreport);
        fromreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReportActivity.this, SitesActivity.class));
            }
        });
        fetchReport();
        fetchVolunteers();
    }
}

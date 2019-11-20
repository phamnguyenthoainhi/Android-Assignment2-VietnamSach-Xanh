package android.rmit.androidass2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InviteActivity extends AppCompatActivity {

    EditText email;
    Button invite;
    Bundle bundle;

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        email=findViewById(R.id.email_invite);
        invite = findViewById(R.id.invite);

        SharedPreferences sharedPreferences = getSharedPreferences("id",MODE_PRIVATE);
        final String userId = sharedPreferences.getString("uid",null);

        Intent intent = getIntent();
        bundle = intent.getExtras();


        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("Users").whereEqualTo("email",email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);


                            UserNotification userNotification = new UserNotification("You have a new invitation!", "invitation", (String) bundle.get("siteId"), userId, documentSnapshot.getId());

                            db.collection("Notifications").add(userNotification)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(InviteActivity.this, "Successfully posted notification", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(InviteActivity.this, "Failed to post notification", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            finish();
                        }
                        else{
                            Toast.makeText(InviteActivity.this, "failed to get user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}

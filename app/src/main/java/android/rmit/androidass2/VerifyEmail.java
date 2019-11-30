package android.rmit.androidass2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyEmail extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private static final String TAG = "VerifyEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Button verify = findViewById(R.id.verifybtn);
        Button skip = findViewById(R.id.skipbtn);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.sendEmailVerification().
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: successful verifed email");
                                }
                            }
                        });

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VerifyEmail.this, MapsActivity.class));
            }
        });
    }
}

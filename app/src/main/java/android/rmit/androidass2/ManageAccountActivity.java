package android.rmit.androidass2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.rmit.androidass2.R;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ManageAccountActivity extends AppCompatActivity {
    TextView logoutbtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        logoutbtn = findViewById(R.id.logoutbtn);
        logoutbtn.setText("Log Out");
        logoutbtn.setClickable(true);
        mAuth = FirebaseAuth.getInstance();
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ManageAccountActivity.this, MapsActivity.class));
            }
        });
    }
}

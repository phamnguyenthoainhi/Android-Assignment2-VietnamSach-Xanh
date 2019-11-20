package android.rmit.androidass2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.DataCollectionDefaultChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class ManageSiteActivity extends AppCompatActivity {
    TabItem volunteer;
    TabItem detail;
    TabItem outcome;
    ViewPager viewPager;
    TabLayout tabLayout;
    PagerController pagerController;
    Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Manage Site Acitivity";
    String sid="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_manage_site);



        tabLayout = findViewById(R.id.tablayout);
        volunteer = findViewById(R.id.volunteertab);
        toolbar = findViewById(R.id.toolbar);
        detail = findViewById(R.id.detailtab);
        outcome = findViewById(R.id.outcometab);
        viewPager = findViewById(R.id.viewpager);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        sid = (String)bundle.get("id");

        Button back = findViewById(R.id.backfrommanagesite);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(ManageSiteActivity.this, SitesActivity.class));
                finish();
            }
        });
        pagerController = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerController);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public String getId(){
        return sid;
    }
    }




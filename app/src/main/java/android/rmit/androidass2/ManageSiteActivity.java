package android.rmit.androidass2;

import android.content.Intent;
import android.os.Bundle;
import android.rmit.androidass2.R;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;


public class ManageSiteActivity extends AppCompatActivity {
    TabItem volunteer;
    TabItem detail;
    TabItem outcome;
    ViewPager viewPager;
    TabLayout tabLayout;
    PagerController pagerController;
    Toolbar toolbar;
    private FirebaseAuth mAuth;

    private static final String TAG = "ManageSiteActivity";
    String sid = "";

    FirebaseUser currentUser;

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
        Button back = findViewById(R.id.backfrommanagesite);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        sid = getIntent().getExtras().getString("selectedsiteid");


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageSiteActivity.this, SitesActivity.class));
                finish();
            }
        });
        pagerController = new PagerController(getSupportFragmentManager(), tabLayout.getTabCount());
//        setSupportActionBar(toolbar);
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
    public String getid(){
        return sid;
    }


}
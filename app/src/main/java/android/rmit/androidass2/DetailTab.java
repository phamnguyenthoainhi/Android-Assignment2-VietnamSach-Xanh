package android.rmit.androidass2;

import android.os.Bundle;
import android.rmit.androidass2.ManageSiteActivity;
import android.rmit.androidass2.R;
import android.rmit.androidass2.Site;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetailTab extends Fragment {
    EditText sitelocation;
    EditText sitedate;
    EditText siteinfo;
    Site site;
    Button savebtn;
    Button editbtn;
    Button deletebtn;
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
        int mMonth = calendar.get(Calendar.MONTH);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_tab, container, false);

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
                savebtn.setVisibility(View.INVISIBLE);
                editbtn.setVisibility(View.VISIBLE);
                sitelocation.setEnabled(false);
                sitedate.setEnabled(false);
                siteinfo.setEnabled(false);
                deletebtn.setVisibility(View.INVISIBLE);
            }
        });





        ManageSiteActivity manageSiteActivity = (ManageSiteActivity) getActivity();
        String sid = manageSiteActivity.getid();
       fetchdetailbyid(sid);


        return view;
    }




}

package android.rmit.androidass2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    ArrayList<Site> mySiteList ;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "Join site";


    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.site, parent, false);

        return(new SiteViewHolder(view));
    }

    public SiteAdapter(ArrayList<Site> siteList) {
        mySiteList = siteList;
    }

    @Override
    public void onBindViewHolder(@NonNull final SiteViewHolder holder, final int position) {
        SharedPreferences sharedPreferences = holder.context.getSharedPreferences("id",Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString("uid",null);
        holder.sitelocation.setText(mySiteList.get(position).getLocation());

        holder.sitelocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.context,SiteInfoActivity.class);
                intent.putExtra("id",mySiteList.get(position).getId());
                holder.context.startActivity(intent);
            }
        });

        boolean contains = mySiteList.get(position).getVolunteers().contains(userId);

        if(!contains){
            if((mySiteList.get(position).getOwner())!=null && !((mySiteList.get(position).getOwner()).equals(userId))){
                holder.join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.context)
                                .setTitle("Confirmation")
                                .setMessage("Do you want to join this clean up site? \n" + mySiteList.get(position).getLocation())
                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(holder.context, "Success", Toast.LENGTH_SHORT).show();

                                        List<String>volunteers = mySiteList.get(position).getVolunteers();
                                        volunteers.add(userId);
                                        mySiteList.get(position).setVolunteers(volunteers);

                                        DocumentReference siteRef = db.collection("Sites").document(mySiteList.get(position).getId());
                                        siteRef.update("volunteers",volunteers)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG,"Successfully updated!");
                                                        holder.join.setVisibility(View.GONE);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG,"Failed to update.");
                                                    }
                                                });

                                    }
                                })
                                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(holder.context, "Failure", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                    }
                                });

                        builder.create().show();
                    }
                });

            }else{
                holder.join.setVisibility(View.GONE);
            }}
        else{holder.join.setVisibility(View.GONE);}
    }

    @Override
    public int getItemCount() {
        return mySiteList.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView sitelocation;
        public Button join;
        public Context context;
        public SiteViewHolder(View v) {
            super(v);
            sitelocation = v.findViewById(R.id.sitelocation);
            join = v.findViewById(R.id.joinbtn);
            context = v.getContext();
        }


    }
}
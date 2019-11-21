package android.rmit.androidass2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    ArrayList<Site> mySiteList ;
    SiteViewHolder.OnSiteListener myOnSiteListener;
    private static final String TAG = "SiteAdapter";


    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.site, parent, false);


        return(new SiteViewHolder(view, myOnSiteListener));
    }


    SiteAdapter(ArrayList<Site> siteList, SiteViewHolder.OnSiteListener onSiteListener) {
        this.mySiteList = siteList;
        this.myOnSiteListener = onSiteListener;
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        holder.sitename.setText(mySiteList.get(position).getName());
        holder.sitelocation.setText(mySiteList.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return mySiteList.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // each data item is just a string in this case
        TextView sitelocation;
        TextView sitename;
        Button deletesite;
        OnSiteListener onSiteListener;
        SiteViewHolder(View v, OnSiteListener onSiteListener) {
            super(v);
            sitelocation = v.findViewById(R.id.sitelocation);
            sitename = v.findViewById(R.id.sitename);
            deletesite = v.findViewById(R.id.deleteasitee);
            deletesite.setVisibility(View.INVISIBLE);

            this.onSiteListener = onSiteListener;
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

 
        }

        @Override
        public void onClick(View v) {
            deletesite.setVisibility(View.INVISIBLE);
            onSiteListener.onSiteClick(getAdapterPosition());
            
        }

        @Override
        public boolean onLongClick(View v){
            onSiteListener.deleteSite(getAdapterPosition());
            return false;
        }

        public interface OnSiteListener{
            void onSiteClick(int position);
            void deleteSite(int position);
        }


    }

}

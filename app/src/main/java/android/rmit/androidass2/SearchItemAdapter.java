package android.rmit.androidass2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder> {

    ArrayList<Site> mySiteList ;
    SearchItemViewHolder.OnSiteListener myOnSiteListener;

    private static final String TAG = "SearchItemAdapter";



    @NonNull
    @Override
    public SearchItemAdapter.SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_layout, parent, false);

        return(new SearchItemViewHolder(view, myOnSiteListener));
    }


    SearchItemAdapter(ArrayList<Site> siteList, SearchItemViewHolder.OnSiteListener onSiteListener) {
        this.mySiteList = siteList;
        this.myOnSiteListener = onSiteListener;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchItemViewHolder holder, int position) {

//        holder.sitelocation.setText(mySiteList.get(position).getLocation());
        holder.searchsitelocation.setText(mySiteList.get(position).getLocation());
        holder.seachsitename.setText(mySiteList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mySiteList.size();

    }

    public void filter(CharSequence text, ArrayList<Site> sites) {
        Log.d(TAG, "filter: " +text);
        Log.d(TAG, "filter: mysites" + sites.toString());
        ArrayList<Site> filteredSites = new ArrayList<>();
//        ArrayList<Site> filteredSites = new ArrayList<>();
        for (Site site: sites) {
            Log.d(TAG, "filter: mysites" + site.getLocation());
            if (site.getLocation().contains(text)) {
                Log.d(TAG, "filter: contains " + site);
                filteredSites.add(site);
            }

        }
        Log.d(TAG, "filter: finished" +filteredSites.toString());
        mySiteList = filteredSites;

        notifyDataSetChanged();
//        return  mySiteList;
    }

    public static class SearchItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        TextView seachsitename;
        TextView searchsitelocation;
        OnSiteListener onSiteListener;
        SearchItemViewHolder(View v, OnSiteListener onSiteListener) {
            super(v);
            seachsitename = v.findViewById(R.id.searchitemname);
            searchsitelocation = v.findViewById(R.id.searchitemlocation);
            this.onSiteListener = onSiteListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            onSiteListener.onSiteClick(getAdapterPosition());
        }


        public interface OnSiteListener{
            void onSiteClick(int position);
        }




    }










}

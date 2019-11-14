package android.rmit.androidass2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    ArrayList<Site> mySiteList ;
    TextView siteLocation;


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
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        holder.sitelocation.setText(mySiteList.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return mySiteList.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView sitelocation;
        public SiteViewHolder(View v) {
            super(v);
            sitelocation = v.findViewById(R.id.sitelocation);

        }


    }
}

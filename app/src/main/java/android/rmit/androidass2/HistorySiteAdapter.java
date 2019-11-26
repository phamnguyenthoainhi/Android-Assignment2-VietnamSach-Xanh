package android.rmit.androidass2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistorySiteAdapter extends RecyclerView.Adapter<HistorySiteAdapter.HistorySiteViewHolder>{
    ArrayList<Site> sites = new ArrayList<>();

    public HistorySiteAdapter(ArrayList<Site> sites) {
        this.sites = sites;
    }

    public static class HistorySiteViewHolder extends RecyclerView.ViewHolder {

        TextView historysitename;
        TextView historysitedate;
        public HistorySiteViewHolder(@NonNull View itemView) {
            super(itemView);

            historysitedate = itemView.findViewById(R.id.historysitedate);
            historysitename = itemView.findViewById(R.id.historysitename);

        }

    }




    @NonNull
    @Override
    public HistorySiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_site, parent, false);


        return (new HistorySiteViewHolder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull HistorySiteViewHolder holder, int position) {
        holder.historysitename.setText(sites.get(position).getName());
        holder.historysitedate.setText(convertDate(sites.get(position).getDateTime()));

    }

    private String convertDate(long millsec) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millsec);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH) + 1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);



        return mDay+"/"+mMonth+""+mYear;
    }


    @Override
    public int getItemCount() {
        return sites.size();
    }
}
package android.rmit.androidass2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {
    private static final String TAG = "VolunteerAdapter";
    private ArrayList<User> volunteerList;
    private Context context;

    public VolunteerAdapter(ArrayList<User> volunteerList, Context context) {
        this.context = context;
        this.volunteerList = volunteerList;

    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer, parent,false);

        VolunteerViewHolder volunteerViewHolder = new VolunteerViewHolder(view);
        return volunteerViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.volunteername.setText(volunteerList.get(position).getFirstname()+" "+ volunteerList.get(position).getLastname());
        holder.volunteerphone.setText(volunteerList.get(position).getPhone());
        holder.volunteeremail.setText(volunteerList.get(position).getEmail());

    }

    @Override
    public int getItemCount() {
        return volunteerList.size();
    }

    public class VolunteerViewHolder extends RecyclerView.ViewHolder {
        TextView volunteername ;
        TextView volunteerphone;
        TextView volunteeremail;
        LinearLayout volunteerlayout;


        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            volunteeremail = itemView.findViewById(R.id.volunteeremail);
            volunteerphone = itemView.findViewById(R.id.volunteerphone);
            volunteername = itemView.findViewById(R.id.volunteername);
            volunteerlayout = itemView.findViewById(R.id.volunteerlayout);

        }
    }
}

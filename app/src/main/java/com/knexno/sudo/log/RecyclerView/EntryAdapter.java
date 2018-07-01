package com.knexno.sudo.log.RecyclerView;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.knexno.sudo.log.EditActivity;
import com.knexno.sudo.log.R;

import static com.knexno.sudo.log.Utils.Contract.INTENT_ENTRY_UID;

public class EntryAdapter extends FirebaseRecyclerAdapter<Entry, EntryAdapter.ViewHolder>{

    public EntryAdapter(@NonNull FirebaseRecyclerOptions<Entry> options) {
        super(options);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView entryHeading, entryBody, entryDate;

        public ViewHolder(View itemView) {
            super(itemView);
            this.entryHeading = itemView.findViewById(R.id.entry_heading);
            this.entryBody = itemView.findViewById(R.id.entry_body);
            this.entryDate = itemView.findViewById(R.id.entry_date);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Entry model) {

        int len = model.getHeading().length();
        if (len > 26){
            len = 26;
        }
        holder.entryHeading.setText(model.getHeading().substring(0, len));
        holder.entryBody.setText(model.getBody());
        holder.entryDate.setText(model.getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), EditActivity.class)
                        .putExtra(INTENT_ENTRY_UID, model.getUid());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_entry, parent, false);

        return new ViewHolder(view);
    }

}

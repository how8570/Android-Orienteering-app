package com.example.test.qrcode_to_database.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.qrcode_to_database.EventMapActivity;
import com.example.test.qrcode_to_database.R;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventHolder> {

    private Context context;
    private ArrayList<Event> events;

    public EventAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_row, parent, false);

        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {

        holder.mImageView.setImageResource(events.get(position).getImg());
        holder.mTitle.setText(events.get(position).getTitle());
        holder.mDescription.setText(events.get(position).getDescription());
        holder.eventUUID = events.get(position).getEventUUID();

        holder.setEventClickListener(new EventClickListener() {
            @Override
            public void onEventClickListener(View v, int position) {
                Intent intent = new Intent(context, EventMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", events.get(position).getTitle());
                bundle.putString("eventUUID", events.get(position).getEventUUID());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}

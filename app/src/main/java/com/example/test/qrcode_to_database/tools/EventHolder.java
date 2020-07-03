package com.example.test.qrcode_to_database.tools;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.qrcode_to_database.R;

public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView mImageView;
    TextView mTitle;
    TextView mDescription;
    EventClickListener eventClickListener;

    EventHolder(@NonNull View itemView) {
        super(itemView);

        mImageView = itemView.findViewById(R.id.mImage);
        mTitle = itemView.findViewById(R.id.mTitle);
        mDescription = itemView.findViewById(R.id.mDescription);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.eventClickListener.onEventClickListener(v, getLayoutPosition());
    }

    public void setEventClickListener(EventClickListener ec) {
        this.eventClickListener = ec;
    }
}


package com.example.jonathan.ics;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<cHolder> {

    List<LoggingElement> exceptions;
    private LogActivity logActivity;


    public RecyclerViewAdapter(List<LoggingElement> exc, LogActivity logActivity){
        exceptions=exc;
        this.logActivity = logActivity;
    }
    @Override
    public cHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                //.R.layout.simple_list_item_2
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerelement, parent, false);
        cHolder holder = new cHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               TextView stack= logActivity.findViewById(R.id.stacktrace);
               stack.setText(holder.content.getText());
               stack.setVisibility(View.VISIBLE);
               stack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView stack= logActivity.findViewById(R.id.stacktrace);
                        stack.setVisibility(View.INVISIBLE);
                    }
               });
            }
        });
        return holder;
    }

    @Override
    public int getItemCount() {
        return exceptions.size();
    }
    @Override
    public void onBindViewHolder(cHolder holder, int i) {
        LoggingElement obj = exceptions.get(i);
        holder.setFields(obj);
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



}

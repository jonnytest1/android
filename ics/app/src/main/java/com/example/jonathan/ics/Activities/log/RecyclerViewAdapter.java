package com.example.jonathan.ics.Activities.log;

import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jonathan.ics.R;
import com.example.jonathan.ics.model.LoggingElement;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<cHolder> {

    List<LoggingElement> exceptions;
    private LogActivity logActivity;


    public RecyclerViewAdapter(List<LoggingElement> exceptions, LogActivity logActivity){
        this.exceptions =exceptions;
        this.logActivity = logActivity;
    }
    @Override
    public cHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerelement, parent, false);
        cHolder holder = new cHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               TextView stack= logActivity.findViewById(R.id.stacktrace);
               stack.setMovementMethod(new ScrollingMovementMethod());
               stack.setText(holder.loggingElement.getContent());
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
        holder.assignTo(obj);
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



}

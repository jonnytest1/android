package com.example.jonathan.ics;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class cHolder extends  RecyclerView.ViewHolder{

    TextView title;
    TextView content;
    TextView date;
    cHolder(View itemView){
        super(itemView);
        title=itemView.findViewById(R.id.text1);
        content=itemView.findViewById(R.id.text2);
        date = itemView.findViewById(R.id.date);
    };

    public void setFields(LoggingElement obj) {
        title.setText(obj.getTitle());
        content.setText(obj.getContent());
        date.setText(obj.getDate());
    }

}

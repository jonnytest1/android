package com.example.jonathan.ics.Activities.log;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.jonathan.ics.model.LoggingElement;
import com.example.jonathan.ics.R;

public class cHolder extends  RecyclerView.ViewHolder{

    TextView title;
    TextView content;
    TextView date;

    LoggingElement loggingElement;
    cHolder(View itemView){
        super(itemView);

        title=itemView.findViewById(R.id.text1);
        content=itemView.findViewById(R.id.text2);
        date = itemView.findViewById(R.id.date);
    };

    public void assignTo(LoggingElement obj) {
        this.loggingElement =obj;
        title.setText(obj.getTitle());
        if(obj.isErrorStack()) {
            content.setText(obj.getContent());
            title.setTextColor(Color.RED);
        }else{
            content.setText("");
            title.setTextColor(Color.parseColor("#FF5366CC"));
        }
        date.setText(obj.getDate());
    }

}

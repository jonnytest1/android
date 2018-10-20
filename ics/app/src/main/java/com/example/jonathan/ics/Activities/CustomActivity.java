package com.example.jonathan.ics.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.jonathan.ics.util.interfaceHandler;

public class CustomActivity extends AppCompatActivity {

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        interfaceHandler.setCurrentToolbar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        interfaceHandler.init(this);
    }

    public void gotoClass(Class<? extends CustomActivity> activity){
        Intent k = new Intent(this, activity);
        startActivity(k);
    }
}

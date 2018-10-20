package com.example.jonathan.ics.Activities.log;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.jonathan.ics.Activities.CustomActivity;
import com.example.jonathan.ics.Activities.main.SettingsActivity;
import com.example.jonathan.ics.R;
import com.example.jonathan.ics.exceptions.ExampleException;
import com.example.jonathan.ics.model.LoggingElement;
import com.example.jonathan.ics.util.interfaceHandler;
import com.example.jonathan.ics.util.storage.Storage;

import java.util.List;

public class LogActivity extends CustomActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        RecyclerView recyclerView= findViewById(R.id.logs);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        //@android:layout/two_line_list_item

        Toolbar myToolbar = findViewById(R.id.toolbarLog);
        setSupportActionBar(myToolbar);

        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        interfaceHandler.getStorage().write(Storage.storageVariables.log,"[]");
                        return true;
                    case R.id.exampleexception:
                        try{
                            throw new ExampleException();
                        }catch (ExampleException e){
                            interfaceHandler.getStorage().log(e);
                        }
                    case R.id.action_settings:
                        gotoClass(SettingsActivity.class);
                    default:
                        return false;
                }
            }
        });
        List<LoggingElement> logStrings = interfaceHandler.getStorage().getLog();
        RecyclerViewAdapter adapter=new RecyclerViewAdapter(logStrings,this);
        recyclerView.setAdapter(adapter);

        interfaceHandler.getStorage().register(Storage.storageVariables.log,data->{
            RecyclerViewAdapter newAdapter=new RecyclerViewAdapter(interfaceHandler.getStorage().parseToList(data),this);
            recyclerView.setAdapter(newAdapter);
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }
}

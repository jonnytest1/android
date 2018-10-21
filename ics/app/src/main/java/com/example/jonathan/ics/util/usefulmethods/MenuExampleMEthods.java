package com.example.jonathan.ics.util.usefulmethods;

import android.view.Menu;
import android.view.MenuItem;

import com.example.jonathan.ics.Activities.CustomActivity;
import com.example.jonathan.ics.R;

public class MenuExampleMEthods extends CustomActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

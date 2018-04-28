package com.example.jonathan.ics;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ListenerInterface {



    enum vars {
        calender,mail,password,account,lastChecked,basMail
    }

    enum actions{
        OnError,started,finished,registered,comR

    }

    public final MainActivity mainA=this;

    String [] perms=new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED
    };

    final boolean[] nextCalender = {true};
    TextView lastCheck;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPerms();
                } else {
                    System.exit(1);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        System.out.println("created");
        requestPerms();
    }
    private boolean requestPerms(){

        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                interfaceHandler.show(perm + " is not set",this);
                requestPermissions(perms, 5);
                return false;
            }
        }

        interfaceHandler.init(this);

        if(!isMyServiceRunning(Calendar2.class)){
            Intent MyIntentService = new Intent(getBaseContext(), Calendar2.class);
            startService(MyIntentService);
        }
        setupBroadCast();
        updateDatefield();
        mailpwfields();
        accountsSpinner();
        calenderSpinner();


        return true;
    }

    @Override
    public String OnReceiveMessage(interfaceHandler.tts target, Enum action, String value, String value2) {
        if(action.equals(actions.started)){
            String uri = "@android:drawable/ic_popup_sync";  // where myresource (without the extension) is the file
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            ((FloatingActionButton) findViewById(R.id.fab)).setImageDrawable(res);
        }else if(action.equals(actions.OnError)){
            interfaceHandler.show(value,mainA);
        }else if(action.equals(actions.finished)){
            lastCheck.setText(value);
            interfaceHandler.note("updated");
            interfaceHandler.write(vars.lastChecked,value,mainA);
            String uri = "@android:drawable/stat_notify_sync_noanim";
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            ((FloatingActionButton) findViewById(R.id.fab)).setImageDrawable(res);
        }else  if(action.equals(actions.registered)){

            findViewById(R.id.fab).getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.DARKEN);
        }
        return null;
    }

    @Override
    public void OnNewListenerRegistered(interfaceHandler.tts target) {
        if(target.equals(interfaceHandler.tts.SE)){
            String answer= MyOwnBroadCastThing.push(interfaceHandler.tts.SE,Calendar2.actions.com,"requestStatus","");
            interfaceHandler.note(answer);
            if(answer.equals("true")){
                findViewById(R.id.fab).getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
            }
        }
    }

    public void setupBroadCast(){
        lastCheck=(TextView)findViewById(R.id.textViewChecked);
        if(MyOwnBroadCastThing.register(this,interfaceHandler.tts.MA).size()>1){
            findViewById(R.id.fab).getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
        };
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void updateDatefield(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fab.bringToFront();

        if(Calendar2.islistening) {
            findViewById(R.id.fab).getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
        }
        final TextView lastCheck = (TextView)findViewById(R.id.textViewChecked);
        lastCheck.setSingleLine(false);
        String lastC= interfaceHandler.get(vars.lastChecked,this);
        if(lastC!=null){
            lastCheck.setText(lastC);
        }

    }
    private void mailpwfields(){

        final EditText Email = (EditText)findViewById(R.id.email);
        Email.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN){
                    return false;
                }
                if(keyCode==KeyEvent.KEYCODE_ENTER){
                     interfaceHandler.write(vars.mail,Email.getText().toString(),mainA);
                    checkMail();
                    return true;
                }
                return false;
            }
        });
        Email.setText(interfaceHandler.get(vars.mail,this));

        final EditText pw = (EditText)findViewById(R.id.password);
        pw.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN){
                    return false;
                }
                if(keyCode==KeyEvent.KEYCODE_ENTER){
                     interfaceHandler.write(vars.password,pw.getText().toString(),mainA);
                    checkMail();
                    return true;
                }
                return false;
            }
        });
        pw.setText( interfaceHandler.get(vars.password,this));

        final EditText basmail = (EditText)findViewById(R.id.editText5);
        basmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN){
                    return false;
                }
                if(keyCode==KeyEvent.KEYCODE_ENTER){
                     interfaceHandler.write(vars.basMail,basmail.getText().toString(),mainA);

                    return true;
                }

                return false;
            }
        });
        basmail.setText( interfaceHandler.get(vars.basMail,this));
    }
    private void calenderSpinner(){

        final Spinner calender=(Spinner)findViewById(R.id.spinner2);
        calender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != parent.getCount() - 1) {
                    final String chosen = calender.getSelectedItem().toString();
                    if(nextCalender[0]) {
                         interfaceHandler.show("this calenders content will be lost", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!chosen.equals( interfaceHandler.get(vars.calender,mainA))) {
                                     interfaceHandler.write(vars.calender, chosen,mainA);
                                }

                            }
                        },mainA);
                    }
                } else {
                    if(nextCalender[0]) {
                         interfaceHandler.write(vars.calender, null,mainA);
                    }
                }
                nextCalender[0] = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private String accountsSpinner(){
        final Spinner accounts=(Spinner)findViewById(R.id.spinner);
        final boolean[] nextAccounts = {true};
        accounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != parent.getCount() - 1) {
                        String acct = ((Spinner) findViewById(R.id.spinner)).getSelectedItem().toString();
                        if(nextAccounts[0]) {
                            if(!acct.equals( interfaceHandler.get(vars.account,mainA))) {
                                 interfaceHandler.write(vars.account, acct,mainA);
                            }
                        }
                        final String[] filter = new String[]{CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
                        Cursor cursor = getBaseContext().getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), filter, null, null, null, null);
                        ArrayList<String> cals2 = new ArrayList<>();


                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String accN = cursor.getString(0);
                                String calenderS = cursor.getString(1);
                                if (!cals2.contains(calenderS) && accN.equals(acct)) {
                                    cals2.add(calenderS);
                                }
                            }
                        }
                        cals2.add("choose one");
                        Spinner calender = (Spinner) findViewById(R.id.spinner2);

                        ArrayAdapter<String> aA = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, cals2);
                        calender.setSelection(aA.getCount() - 1);
                        calender.setAdapter(aA);
                        String calenderString =  interfaceHandler.get(vars.calender,mainA);
                        if (calenderString != null) {
                            int spinnerPosition = aA.getPosition(calenderString);
                            if (spinnerPosition != -1) {
                                nextCalender[0] = false;
                                calender.setSelection(spinnerPosition);
                            }
                        } else {
                            nextCalender[0] = false;
                            calender.setSelection(aA.getCount() - 1);
                        }
                    } else {
                        if(nextAccounts[0]) {
                             interfaceHandler.write(vars.account, null,mainA);
                        }
                    }
                     nextAccounts[0]=true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        final String[] filter= new String[]{CalendarContract.Calendars.ACCOUNT_NAME};
        Cursor cursor = getBaseContext().getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),filter, null, null, null, null);
        ArrayList<String> cals= new ArrayList<>();

        if (cursor != null) {
            while(cursor.moveToNext()){
                String accN=cursor.getString(0);
                if(!cals.contains(accN)) {
                    cals.add(accN);
                }
            }
        }
        cals.add("choose one");
        ArrayAdapter<String> ad= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cals);
        accounts.setAdapter(ad);
        String compareValue =  interfaceHandler.get(vars.account,mainA);
        if (compareValue != null) {
            int spinnerPosition = ad.getPosition(compareValue);
            if(spinnerPosition!=-1) {
                nextAccounts[0] =false;
                accounts.setSelection(spinnerPosition);
                return compareValue;
            }
        }
        nextAccounts[0] =false;
        accounts.setSelection(ad.getCount()-1);
        return null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void checkMail(){
        interfaceHandler.update(interfaceHandler.tts.SE,Calendar2.actions.check,"",this);
    }


}

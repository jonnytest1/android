package com.example.jonathan.ics;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v7.widget.RecyclerView;
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

import static com.example.jonathan.ics.MailScheduler.registerScheduler;
import static com.example.jonathan.ics.R.id.refresh;


public class MainActivity extends AppCompatActivity {
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    enum vars {
        calender,mail,password,account,lastChecked,basMail,log
    }

    enum actions{
        OnError,started,finished,registered,comR,logUpdate

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        System.out.println("created");
        requestPerms();

    }
    private void requestPerms(){

        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                interfaceHandler.show(perm + " is not set",this);
                requestPermissions(perms, 5);
                return;
            }
        }
        setConsoleListener();

        TextView text = findViewById(R.id.schedulerValue);
        if(!registerScheduler(getBaseContext())){
            text.setText("already registered");
        }else{
            text.setText("had to register new");
        }
        setupBroadCast();
        updateDatefield();
        mailpwfields();
        accountsSpinner();
        calenderSpinner();


    }
    private void setLogText(){
        //TODO

    }

    private void moveToLog(){
        Intent k = new Intent(this, LogActivity.class);
        startActivity(k);
    }


    private void setConsoleListener() {
        View layout = findViewById(R.id.schedulerRow);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLog();
            }
        });
    }

    public void setupBroadCast(){
        IntentFilter iFilter=new IntentFilter();
        for(actions i : actions.values()){
            iFilter.addAction(i.toString());
        }
        final TextView lastCheck= findViewById(R.id.textViewChecked);
        interfaceHandler.getIBM(getBaseContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(actions.started.toString().equals(action)){
                    String uri = "@android:drawable/ic_popup_sync";  // where myresource (without the extension) is the file
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    ((FloatingActionButton) findViewById(refresh)).setImageDrawable(res);
                }else if(actions.OnError.toString().equals(action)){
                    interfaceHandler.show(intent.getStringExtra("value"),mainA);
                }else if(actions.finished.toString().equals(action)){
                    String value=intent.getStringExtra("value");
                    lastCheck.setText(value);
                    interfaceHandler.write(vars.lastChecked,value,mainA);
                    String uri = "@android:drawable/stat_notify_sync_noanim";
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    ((FloatingActionButton) findViewById(refresh)).setImageDrawable(res);
                }else  if(actions.registered.toString().equals(action)){
                    findViewById(refresh).getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
                }else if(actions.comR.toString().equals(action)){
                    if("answerStatus".equals(intent.getStringExtra("value"))){
                        findViewById(refresh).getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
                    }
                }else if(actions.logUpdate.toString().equals(action)){
                    setLogText();
                }
            }
        },iFilter);

    }

    public void updateDatefield(){

        FloatingActionButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MailScheduler.startInNewThread(getBaseContext());
            }
        });
        refresh.getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
        refresh.bringToFront();

        FloatingActionButton clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                        System.out.println("clearing");
                        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) )";
                        String account= interfaceHandler.get(MainActivity.vars.account,getBaseContext());

                        if(account==null){
                            interfaceHandler.update(MainActivity.actions.OnError,"account not defined",getBaseContext());
                            return;
                        }

                        String calenderToDelete= interfaceHandler.get(MainActivity.vars.calender,getBaseContext());
                        if(calenderToDelete==null){
                            interfaceHandler.update(MainActivity.actions.OnError,"calender is not defined",getBaseContext());
                            return;
                        }
                        String[] selectionArgs = new String[]{account};

                        Context context = getApplicationContext();
                        Cursor cursor;
                        cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), EVENT_PROJECTION, selection, selectionArgs, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            if ( cursor.getCount() > 0) {
                                while (!cursor.isAfterLast()) {
                                    if (cursor.getString(cursor.getColumnIndex("calendar_displayName")).equals(calenderToDelete)) {

                                        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
                                        int calenderID = cursor.getInt(cursor.getColumnIndex("_id"));
                                        ContentResolver contentR= context.getContentResolver();
                                        contentR.delete(CALENDAR_URI, "calendar_id=" + calenderID, null);
                                   /* Bundle extras = new Bundle();
                                    extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                                    extras.putBoolean(ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS, true);
                                    ContentResolver.requestSync(new Account(), CalendarContract.Authority, extras);*/
                                    }
                                    cursor.moveToNext();
                                }
                            }
                            cursor.close();
                        }
                        interfaceHandler.show("cleared edit calender",mainA);
                    ContentResolver.setMasterSyncAutomatically(false);
                    //interfaceHandler.note("cleared calender");
                    ContentResolver.setMasterSyncAutomatically(true);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        clear.bringToFront();


        final TextView lastCheck = findViewById(R.id.textViewChecked);
        lastCheck.setSingleLine(false);
        String lastC= interfaceHandler.get(vars.lastChecked,this);
        if(lastC!=null){
            lastCheck.setText(lastC);
        }

    }
    private void mailpwfields(){

        final EditText Email = findViewById(R.id.email);
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

        final EditText pw = findViewById(R.id.password);
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

        final EditText basmail = findViewById(R.id.editText5);
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

        final Spinner calender= findViewById(R.id.spinner2);
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
    private void accountsSpinner(){
        final Spinner accounts= findViewById(R.id.spinner);
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
                            cursor.close();
                        }
                        cals2.add("choose one");
                        Spinner calender = findViewById(R.id.spinner2);

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
            cursor.close();
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
                return;
            }
        }
        nextAccounts[0] =false;
        accounts.setSelection(ad.getCount()-1);
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

        MailScheduler.startInNewThread(this);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public TextView textView;

        Holder(TextView v){
            super(v);
            textView = v;
        }
    }
}

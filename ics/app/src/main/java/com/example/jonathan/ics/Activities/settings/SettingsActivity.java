package com.example.jonathan.ics.Activities.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.jonathan.ics.Activities.CustomActivity;
import com.example.jonathan.ics.Activities.log.LogActivity;
import com.example.jonathan.ics.R;
import com.example.jonathan.ics.repositories.DatabaseService;
import com.example.jonathan.ics.services.invokers.SchedulerMail;
import com.example.jonathan.ics.util.interfaceHandler;
import com.example.jonathan.ics.util.storage.Storage;

import java.util.ArrayList;

import static com.example.jonathan.ics.R.id.refresh;
import static com.example.jonathan.ics.services.invokers.SchedulerMail.registerScheduler;




public class SettingsActivity extends CustomActivity {

    public enum actions{
        OnError,started,finished,registered,comR,logUpdate
    }

    public final SettingsActivity self =this;

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
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        System.out.println("created");
        requestPerms();

    }
    private void requestPerms(){

        for (String perm : perms) {
            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                interfaceHandler.getInstance().show(perm + " is not set");
                requestPermissions(perms, 5);
                return;
            }
        }

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

    public void setupBroadCast(){
        IntentFilter iFilter=new IntentFilter();
        for(actions i : actions.values()){
            iFilter.addAction(i.toString());
        }
        final TextView lastCheck= findViewById(R.id.textViewChecked);
        interfaceHandler.getInstance().getIBM(getBaseContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(actions.started.toString().equals(action)){
                    String uri = "@android:drawable/ic_popup_sync";  // where myresource (without the extension) is the file
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable res = getResources().getDrawable(imageResource);
                    ((FloatingActionButton) findViewById(refresh)).setImageDrawable(res);
                }else if(actions.OnError.toString().equals(action)){
                    interfaceHandler.getInstance().show(intent.getStringExtra("value"));
                }else if(actions.finished.toString().equals(action)){
                    String value=intent.getStringExtra("value");
                    lastCheck.setText(value);
                    interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.lastChecked,value);
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
                }
            }
        },iFilter);
    }

    public void updateDatefield(){

        FloatingActionButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(view -> SchedulerMail.startInNewThread(getBaseContext()));
        refresh.getBackground().setColorFilter(Color.parseColor("#ff99cc00"), PorterDuff.Mode.DARKEN);
        refresh.bringToFront();

        FloatingActionButton clear = findViewById(R.id.clear);
        clear.setOnClickListener(view -> {
            try {
                System.out.println("clearing");
                String account= interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.account);
                if(account==null){
                    interfaceHandler.getInstance().update(actions.OnError,"account not defined",getBaseContext());
                    return;
                }
                String calenderToDelete= interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.calender);
                if(calenderToDelete==null){
                    interfaceHandler.getInstance().update(actions.OnError,"calender is not defined",getBaseContext());
                    return;
                }
                new DatabaseService(self).deleteEntries(account,calenderToDelete);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
        clear.bringToFront();


        final TextView lastCheck = findViewById(R.id.textViewChecked);
        lastCheck.setSingleLine(false);
        String lastC= interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.lastChecked);
        if(lastC!=null){
            lastCheck.setText(lastC);
        }

    }
    private void mailpwfields(){

        final EditText Email = findViewById(R.id.email);
        Email.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction()!=KeyEvent.ACTION_DOWN){
                return false;
            }
            if(keyCode==KeyEvent.KEYCODE_ENTER){
                 interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.mail,Email.getText().toString());
                checkMail();
                return true;
            }
            return false;
        });
        Email.setText(interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.mail));

        final EditText pw = findViewById(R.id.password);
        pw.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction()!=KeyEvent.ACTION_DOWN){
                return false;
            }
            if(keyCode==KeyEvent.KEYCODE_ENTER){
                 interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.password,pw.getText().toString());
                checkMail();
                return true;
            }
            return false;
        });
        pw.setText( interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.password));

        final EditText basmail = findViewById(R.id.editText5);
        basmail.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction()!=KeyEvent.ACTION_DOWN){
                return false;
            }
            if(keyCode==KeyEvent.KEYCODE_ENTER){
                 interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.basMail,basmail.getText().toString());

                return true;
            }

            return false;
        });
        basmail.setText( interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.basMail));
    }
    private void calenderSpinner(){

        final Spinner calender= findViewById(R.id.spinner2);
        calender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != parent.getCount() - 1) {
                    final String chosen = calender.getSelectedItem().toString();
                    if(nextCalender[0]) {
                         interfaceHandler.getInstance().show("this calenders content will be lost", v -> {
                             if (!chosen.equals( interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.calender))) {
                                  interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.calender, chosen);
                             }
                         });
                    }
                } else {
                    if(nextCalender[0]) {
                         interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.calender, null);
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
                    String account = ((Spinner) findViewById(R.id.spinner)).getSelectedItem().toString();
                    if(nextAccounts[0]) {
                        if(!account.equals( interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.account))) {
                             interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.account, account);
                        }
                    }
                    ArrayList<String> cals2 = new DatabaseService(self).getCalendarsForAccount(account);
                    cals2.add("choose one");
                    Spinner calender = findViewById(R.id.spinner2);

                    ArrayAdapter<String> aA = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, cals2);
                    calender.setSelection(aA.getCount() - 1);
                    calender.setAdapter(aA);
                    String calenderString =  interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.calender);
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
                         interfaceHandler.getInstance().getStorage().write(Storage.storageVariables.account, null);
                    }
                }
                 nextAccounts[0]=true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayList<String> cals = new DatabaseService(this).getAccounts();
        cals.add("choose one");
        ArrayAdapter<String> ad= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cals);
        accounts.setAdapter(ad);
        String compareValue =  interfaceHandler.getInstance().getStorage().get(Storage.storageVariables.account);
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

    public void checkMail(){
        SchedulerMail.startInNewThread(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            gotoClass(LogActivity.class);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        gotoClass(LogActivity.class);
    }
}

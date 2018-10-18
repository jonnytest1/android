package com.example.jonathan.rocketchat;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //
       /* findViewById(R.id.layout).setOnGenericMotionListener(new View.OnGenericMotionListener() {
             @Override
             public boolean onGenericMotion(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(event)) {
                     // Don't forget the negation here
                     float delta = -RotaryEncoder.getRotaryAxisValue(event) * RotaryEncoder.getScaledScrollFactor(
                             getContext());

                     // Swap these axes if you want to do horizontal scrolling instead
                     v.scrollBy(0, Math.round(delta));

                     return true;
                 }

                 return false;
             }
        });*/

        // Enables Always-on
        setAmbientEnabled();
    }

    public Context getContext() {
        return this.getBaseContext();
    };
}


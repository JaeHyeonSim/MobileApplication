package ddwu.mobile.finalproject.ma01_20200982;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.menu_bottom);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               switch (item.getItemId()) {
                   case R.id.tab_schedule:
                       callScheduleActivity();
                       break;
                   case R.id.tab_map:
                       checkPermission();
                       if (checkPermission()) {
                           callMapActivity();
                       }
                       break;
                   case R.id.tab_alarm:
                       callAlarmActivity();
                       break;
                   case R.id.tab_setting:
                       callSettingActivity();
                       break;
               }
               return false;
           }
       });

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_schedule:
                callScheduleActivity();
                break;
            case R.id.btn_map:
                checkPermission();
                if (checkPermission()) {
                    callMapActivity();
                }
                break;
            case R.id.btn_alarm:
                callAlarmActivity();
                break;
            case R.id.btn_setting:
                callSettingActivity();
                break;
        }
    }

    private boolean checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return false;
        }
    }

    private void callScheduleActivity() {
        Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
        startActivity(intent);
    }

    private void callMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    private void callAlarmActivity() {
        Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
        startActivity(intent);
    }

    private void callSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }


}
package ddwu.mobile.finalproject.ma01_20200982;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setPerm:
                checkPermission();
                break;
        }
    }

    private void checkPermission() {
        // 권한 요청
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

}

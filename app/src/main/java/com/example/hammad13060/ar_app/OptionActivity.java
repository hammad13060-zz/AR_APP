package com.example.hammad13060.ar_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        startSensorService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startSensorService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSensorService();
    }

    public void onFacilityIdentification(View v) {
        Intent i = new Intent(this, FacilityIdentificationActivity.class);
        startActivity(i);
    }

    public void onSmartNavigation(View v) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void startSensorService() {
        if (!State.sensorServiceRunning) {
            startService(new Intent(this, SensorService.class));
        }
    }
}

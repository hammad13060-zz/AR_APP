package com.example.hammad13060.ar_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class NavigationSetupActivity extends AppCompatActivity {

    private boolean destinationSelected = false;
    private DestinationContainer destinationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_setup);
    }

    public void onSelectDestination(View view) {
        DestinationOptionsDialog.showDialog(this);
    }

    public void onStartJourney(View view) {

    }

    public void onDestinationSelected(DestinationContainer container) {
        TextView textView = (TextView) findViewById(R.id.destination_text_view);
        Log.d(NavigationSetupActivity.class.getName(), "Destination: " + container.getName());
        textView.setText(container.getName());
        destinationContainer = container;
        destinationSelected = true;
    }
}

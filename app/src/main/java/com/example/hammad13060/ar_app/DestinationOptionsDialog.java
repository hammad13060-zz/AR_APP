package com.example.hammad13060.ar_app;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hammad13060 on 03/03/17.
 */

public class DestinationOptionsDialog {
    public static String destinations[] = {"Dest1", "Dest2", "Dest3", "Dest4"};
    public static Map<String, DestinationContainer> destinationMap = new HashMap<>();
    static {
        destinationMap.put(destinations[0], new DestinationContainer(destinations[0], 1.0, 1.0, 1.0));
        destinationMap.put(destinations[1], new DestinationContainer(destinations[1], 2.0, 2.0, 2.0));
        destinationMap.put(destinations[2], new DestinationContainer(destinations[2], 3.0, 3.0, 3.0));
        destinationMap.put(destinations[3], new DestinationContainer(destinations[3], 4.0, 4.0, 4.0));
    }
    public static void showDialog(final NavigationSetupActivity context) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Choose Destination");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.destination_container);
        for (int i = 0; i < destinations.length; i++) {
            arrayAdapter.add(destinations[i]);
        }

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DestinationContainer destinationContainer = destinationMap.get(destinations[which]);
                context.onDestinationSelected(destinationContainer);
            }
        });
        builderSingle.show();
    }
}

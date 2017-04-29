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
    public static String destinations[] = {"Room B-406", "Room B-407", "Room B-408", "Room B-4010", "CDX", "Acad Entry 1", "Acad Entry 2"};
    public static Map<String, DestinationContainer> destinationMap = new HashMap<>();
    public static double x = 722250.000, y = 3159632.00;
    static {
        destinationMap.put(destinations[0], new DestinationContainer(destinations[0],
                116+x, 7.3+y, 237.455994));
        destinationMap.put(destinations[1], new DestinationContainer(destinations[1],
                85.328733+x, 4.245343+y, 228.476078));
        destinationMap.put(destinations[2], new DestinationContainer(destinations[2],
                89.232023+x, -7.962436+y, 232.051479));
        destinationMap.put(destinations[3], new DestinationContainer(destinations[3],
                99.245795+x, 5.679707+y, 228.311822));
        //destinationMap.put(destinations[4], new DestinationContainer(destinations[4], x+4.52, y+70.76, 223.056));
        //destinationMap.put(destinations[5], new DestinationContainer(destinations[5], x+21.13, y+89.37, 223.056));
        //destinationMap.put(destinations[6], new DestinationContainer(destinations[6], x-11.42, y+88.04, 223.056));
        destinationMap.put(destinations[4], new DestinationContainer(destinations[4], x+70.76, y+4.52, 223.056));
        destinationMap.put(destinations[5], new DestinationContainer(destinations[5], x+89.37, y+21.13, 223.056));
        destinationMap.put(destinations[6], new DestinationContainer(destinations[6], x+88.04, y-11.42, 223.056));
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

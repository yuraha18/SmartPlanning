package com.eplan.yuraha.easyplanning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.eplan.yuraha.easyplanning.API.Synchronizer;



public class InternetListener extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent )
    {
        if (hasActiveInternetConnection(context)) {
           Synchronizer.startSynchronization(context);
        }

    }

    public static boolean hasActiveInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected;

        if (activeNetwork!=null)
      isConnected = activeNetwork.isConnectedOrConnecting();

        else
        isConnected = false;

        return isConnected;
    }

}

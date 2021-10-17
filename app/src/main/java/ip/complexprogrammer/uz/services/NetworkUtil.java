package ip.complexprogrammer.uz.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static boolean getConnectivityStatusString(Context context) {
        boolean internet_yes=false;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                internet_yes=true;
                return internet_yes;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                internet_yes=true;
                return internet_yes;
            }
        } else {
            return internet_yes;
        }
        return internet_yes;
    }
}

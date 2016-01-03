package kaoxcix.weathercast.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class checkUtils {
    private Context context;

    public checkUtils(Context context) {
        this.context = context;
    }

    public Boolean isEnglishAlphabetString(String string){
        if(string.matches("[a-zA-Z ]+")) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

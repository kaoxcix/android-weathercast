package kaoxcix.weathercast.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toolbar;

import kaoxcix.weathercast.R;

public class viewUtils {

    public viewUtils() {
    }

    public void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

//    public void setActionBarColor(Context context,int color) {
//        context.get
//        ActionBar actionBar = getSupportActionBar();
//        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            context.setSupportActionBar(toolbar);
//            context.
//        }
//        if (activity.getSupportActionBar() != null) {
//            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.GREEN));
//        }
//    }



}

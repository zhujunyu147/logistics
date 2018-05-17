package honeywell.honeywelllogistics.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by zhujunyu on 2018/5/15.
 */

public class Utils {
    public static void showToast(Context context, String msg) {
        Toast mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }
}

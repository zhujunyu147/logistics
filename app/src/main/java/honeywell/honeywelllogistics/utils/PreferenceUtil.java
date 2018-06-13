package honeywell.honeywelllogistics.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceUtil {


    public static final String TIME_OUT = "6000";
    public static final String HTTP_MODE = "HTTP_MODE";
    public static final String HTTP_DEFAULT = "HTTP_POST";
    public static final String URL_ADDRESS = "";
    public static final String URL_DEFAULT_ADDRESS = "http://222.126.229.158:8580/wms-extra-api/WmsInterfaceForHN/inboundCar";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("LOGISTICS", Context.MODE_PRIVATE);
    }

    public static void saveUrlAddress(Context context, String address) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(URL_ADDRESS, address);
        edit.commit();
    }


    public static void saveHttpMethod(Context context, String method) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(HTTP_MODE, method);
        edit.commit();
    }

    public static String getString(Context context, String key, String faillValue) {
        return getSharedPreferences(context).getString(key, faillValue);
    }

}

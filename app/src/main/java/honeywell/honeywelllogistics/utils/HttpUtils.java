package honeywell.honeywelllogistics.utils;

import android.content.Context;
import android.util.Log;


import com.honeywell.net.FastTube;
import com.honeywell.net.core.TubeOptions;
import com.honeywell.net.listener.JSONTubeListener;
import com.honeywell.net.utils.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honeywell.honeywelllogistics.bean.Result;
import honeywell.honeywelllogistics.task.TubeTask;


/**
 * Created by zhujunyu on 2017/3/13.
 */

public class HttpUtils {
    private static final String TAG = "HttpUtils";

    public static final int CONN_TIMEOUT = 8 * 1000;

    public static final int RECONN_TIMES = 2;

    /**
     * get正常方法
     *
     * @param context
     * @param url
     * @param listener
     */
    public static void getString(Context context,
                                 String url, HttpEntity entity1, TubeTask listener) {
        if (!Utils.isNetworkAvailable(context)) {
            Result result = new Result();
            result.resultCode = 500;
            listener.onSuccess(result);
        }
        TubeOptions opt = new TubeOptions.Builder()
                .setConnectionTimeOut(CONN_TIMEOUT)
                .setReconnectionTimes(RECONN_TIMES)
                .setHeaders(getHeaderParamsMap(context)).create();
        Log.e("request url", "" + url);
        Logger.e("TUBE",""+"retry times: -> " + RECONN_TIMES);

        FastTube.getInstance().getString(url, opt, listener);
    }


    public static void postString(Context context,
                                 String url, HttpEntity entity1, TubeTask listener) {
        if (!Utils.isNetworkAvailable(context)) {
            Result result = new Result();
            result.resultCode = 500;
            listener.onSuccess(result);
        }
        TubeOptions opt = new TubeOptions.Builder()
                .setConnectionTimeOut(CONN_TIMEOUT)
                .setReconnectionTimes(RECONN_TIMES).setPostEntity(entity1)
                .setHeaders(getHeaderParamsMap(context)).create();
        Log.e("request url", "" + url);
        Logger.e("TUBE",""+"retry times: -> " + RECONN_TIMES);

        FastTube.getInstance().getString(url, opt, listener);
    }


    /**
     * get正常方法
     *
     * @param context
     * @param url
     * @param listener
     */
    public static void getPureString(Context context,
                                     String url, TubeTask listener) {
        TubeOptions opt = new TubeOptions.Builder()
                .setConnectionTimeOut(CONN_TIMEOUT)
                .setReconnectionTimes(RECONN_TIMES)
                .setHeaders(getHeaderParamsMap(context)).create();

        FastTube.getInstance().getString(url, opt, listener);
    }


    public static void getJSON(Context context,
                               String url, HttpEntity entity1, JSONTubeListener<?> listener) {

        TubeOptions opt = new TubeOptions.Builder()
                .setConnectionTimeOut(CONN_TIMEOUT).setHttpMethod(TubeOptions.HTTP_METHOD_POST)
                .setReconnectionTimes(RECONN_TIMES).setPostEntity(entity1)
                .create();

        FastTube.getInstance().getJSON(url, opt, listener);
    }

    public static void getJSON(Context context, Map<String, String> head,
                               String url, HttpEntity entity1, JSONTubeListener<?> listener) {

        TubeOptions opt = new TubeOptions.Builder()
                .setHeaders(head)
                .setConnectionTimeOut(CONN_TIMEOUT).setHttpMethod(TubeOptions.HTTP_METHOD_POST)
                .setReconnectionTimes(RECONN_TIMES).setPostEntity(entity1)
                .create();

        FastTube.getInstance().getJSON(url, opt, listener);
    }

    /**
     * 正常请求header
     *
     * @param context
     * @return
     */
    public static Map<String, String> getHeaderParamsMap(Context context) {
        final String cookie = PreferenceUtil.getString(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        Map<String, String> map = new HashMap<>();
        map.put(Constants.KEY_COOKIE, cookie);
        map.put(Constants.KEY_CONTENT_TYPE,"application/json");
        return map;
    }


    public static void postString(Context context, String url,List<NameValuePair> list,TubeTask listener) {

        // post参数的list
        TubeOptions opt = new TubeOptions.Builder().setPostEntity(list)
                .setHeaders(getHeaderParamsMap(context)).create();
        FastTube.getInstance().post(url, opt, listener);

    }


}

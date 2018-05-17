package honeywell.honeywelllogistics.utils;

import com.honeywell.net.utils.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by zhujunyu on 2018/5/15.
 */

public class RequestEntityUtils {

    public static HttpEntity getRequestEntity(JSONObject json) {
        String JsonStr = json.toString();
        HttpEntity entity1 = null;
        try {
            entity1 = new StringEntity(JsonStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Logger.e("request params",""+JsonStr);
        return entity1;
    }
}

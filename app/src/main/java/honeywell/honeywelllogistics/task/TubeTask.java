package honeywell.honeywelllogistics.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.StringTubeListener;

import org.json.JSONObject;

import honeywell.honeywelllogistics.R;
import honeywell.honeywelllogistics.bean.DataItem;
import honeywell.honeywelllogistics.bean.Result;
import honeywell.honeywelllogistics.interfaces.IResponse;
import honeywell.honeywelllogistics.utils.Constants;
import honeywell.honeywelllogistics.utils.Utils;


/**
 * Created by zhujunyu on 2017/3/13.
 */

public class TubeTask implements StringTubeListener<Result> {

    private Context mContext;
    private IResponse mResponse;
    private int flag;

    public TubeTask(Context context, int flag, IResponse mResponse) {
        this.mContext = context;
        this.mResponse = mResponse;
        this.flag = flag;
    }

    @Override
    public Result doInBackground(String water) throws Exception {
        Result result = new Result();

        if (TextUtils.isEmpty(water)) {
            result.resultCode = Result.RESULT_ERROR;
            result.strArray = new String[1];
            result.strArray[0] = water;
            return result;
        }


        switch (flag) {

            case Constants.GetDataFlag.HON_IN:
            case Constants.GetDataFlag.HON_OUT:
                JSONObject json = new JSONObject(water);
                Boolean isOK = json.optBoolean("success");
                result.resultCode = Result.RESULT_OK;
                String message = json.optString("message");
                result.strArray = new Object[2];
                result.strArray[0] = message;

                DataItem dataItem = new DataItem();
                JSONObject jsonData = json.optJSONObject("data");
                if (jsonData != null) {
                    dataItem.setGateMessage(jsonData.optString("gateMessage"));
                    dataItem.setLocDetail(jsonData.optString("locDetail"));
                    dataItem.setVin(jsonData.optString("vin"));
                }
                result.strArray[1] = dataItem;

                if (isOK) {
                    result.resultCode = Result.RESULT_OK;

                } else {
                    result.resultCode = Result.RESULT_ERROR;
                }

                break;

            default:
                result.resultCode = Result.RESULT_OK;
                break;
        }

        return result;
    }

    @Override
    public void onSuccess(Result result) {
        if (result.resultCode == 500) {
            Utils.showToast(mContext, mContext.getResources().getString(R.string.no_network));
        }
        try {
            mResponse.response(result.resultList, result.resultCode, result.strArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(TubeException e) {
        Log.d("TUBE TASK", "onFailed" + e.getLocalizedMessage());
        Result result = new Result();
        result.resultCode = Result.RESULT_EXCEPTION;
        this.onSuccess(result);
    }


}

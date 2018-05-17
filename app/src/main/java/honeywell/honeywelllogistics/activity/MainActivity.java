package honeywell.honeywelllogistics.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.net.utils.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import honeywell.honeywelllogistics.R;
import honeywell.honeywelllogistics.interfaces.IResponse;
import honeywell.honeywelllogistics.task.TubeTask;
import honeywell.honeywelllogistics.utils.Constants;
import honeywell.honeywelllogistics.utils.HttpUtils;
import honeywell.honeywelllogistics.utils.PreferenceUtil;
import honeywell.honeywelllogistics.utils.RequestEntityUtils;
import honeywell.honeywelllogistics.utils.Utils;

public class MainActivity extends Activity {

    private ImageView mImageViewSetting;
    private Button mBtnInBound;
    private TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        String address = PreferenceUtil.getString(this, PreferenceUtil.URL_ADDRESS, PreferenceUtil.URL_DEFAULT_ADDRESS);
        if (address.contains("outboundCar")) {
            mTvStatus.setText("出库");
        } else {
            mTvStatus.setText("入库");
        }


    }

    private void initView() {
        mImageViewSetting = findViewById(R.id.iv_setting);
        mImageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });
        mBtnInBound = findViewById(R.id.btn_in);
        mBtnInBound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inBoundCar("TEST000000001");
//                outBoundCar("TEST000000001");

            }
        });
        mTvStatus = findViewById(R.id.boundStatus);
    }


    private void inBoundCar(String qrCodeIdentification) {

        String method = PreferenceUtil.getString(this, PreferenceUtil.HTTP_MODE, PreferenceUtil.HTTP_DEFAULT);

        String address = PreferenceUtil.getString(this, PreferenceUtil.URL_ADDRESS, PreferenceUtil.URL_DEFAULT_ADDRESS);


        // http://222.126.229.158:8780/wms-extra-app/WmsInterfaceForHN/inboundCar
        // http://222.126.229.158:8780/wms-extra-app/WmsInterfaceForHN/outboundCar

        Log.e("method:", "aa:" + method);
        method = "HTTP_POST";
        try {
            if (method.equals(Constants.HTTP_GET)) {
                JSONObject requestJson = new JSONObject();
                requestJson.put("qrCodeIdentification", qrCodeIdentification);


                String url = address + "?" + "qrCodeIdentification=" + qrCodeIdentification;
                HttpUtils.getString(MainActivity.this, url, RequestEntityUtils.getRequestEntity(requestJson), new TubeTask(MainActivity.this, Constants.GetDataFlag.HON_IN, new IResponse() {
                    @Override
                    public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {

                        if (resultCode == 0) {

                            Utils.showToast(MainActivity.this, "入库成功");
                            return;
                        }
                        Utils.showToast(MainActivity.this, "入库失败");
                        return;
                    }
                }));

            } else {
                JSONObject requestJson = new JSONObject();
                requestJson.put("qrCodeIdentification", qrCodeIdentification);

                HttpUtils.postString(MainActivity.this, address, RequestEntityUtils.getRequestEntity(requestJson), new TubeTask(MainActivity.this, Constants.GetDataFlag.HON_IN, new IResponse() {
                    @Override
                    public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                        if (resultCode == 0) {

                            Utils.showToast(MainActivity.this, "入库成功");
                            return;
                        }
                        Utils.showToast(MainActivity.this, "入库失败");
                        return;
                    }
                }));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }





}

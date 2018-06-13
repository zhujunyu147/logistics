package honeywell.honeywelllogistics.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honeywell.net.utils.Logger;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import honeywell.honeywelllogistics.R;
import honeywell.honeywelllogistics.bean.DataItem;
import honeywell.honeywelllogistics.bean.DetailItem;
import honeywell.honeywelllogistics.interfaces.IResponse;
import honeywell.honeywelllogistics.manager.ClientManager;
import honeywell.honeywelllogistics.task.TubeTask;
import honeywell.honeywelllogistics.utils.Constants;
import honeywell.honeywelllogistics.utils.HttpUtils;
import honeywell.honeywelllogistics.utils.PreferenceUtil;
import honeywell.honeywelllogistics.utils.RequestEntityUtils;
import honeywell.honeywelllogistics.utils.Utils;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class MainActivity extends Activity {
    private String scanResponseString;
    private ImageView mImageViewSetting;
    //    private Button mBtnInBound;
    private TextView mTvStatus;
    private TextView mTvCarNum;
    private TextView mTvRequestStatus;
    private TextView mTvErrorMessage;
    private LinearLayout mLinearLayout;
    private TextView mTvLocation;
    private Button mBtnInBound;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isRequesting = false;
    private ProgressDialog progressDialog;
    private Dialog mDialog;
    private Button mButtonConnect;
    public static final int REQUEST_CODE = 100;
    private boolean mConnected;
    private String overallMacAddress;
    private UUID mService = UUID.fromString("0000ffE0-0000-1000-8000-00805f9b34fb");
    private UUID mCharacter = UUID.fromString("0000ffE1-0000-1000-8000-00805f9b34fb");


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
        mLinearLayout = findViewById(R.id.response);
        mLinearLayout.setVisibility(View.GONE);
        mTvLocation = findViewById(R.id.tv_location);

        mImageViewSetting = findViewById(R.id.iv_setting);
        mImageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputServer = new EditText(MainActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("请输入管理员密码").setView(inputServer);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        if ("123456".equals(inputServer.getText().toString())) {
                            mDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                            startActivity(intent);
                        } else {
                            Utils.showToast(MainActivity.this, "密码错误");
                            mDialog.dismiss();
                        }

                    }
                });
                mDialog = builder.show();


            }
        });
        mBtnInBound = findViewById(R.id.btn_in);
        mBtnInBound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inBoundCar("dd18463148914");
//                outBoundCar("TEST000000001");
            }
        });
        mTvStatus = findViewById(R.id.boundStatus);
        mTvCarNum = findViewById(R.id.tv_car_num);
        mTvRequestStatus = findViewById(R.id.request_status);
        mTvErrorMessage = findViewById(R.id.error_message);
        mButtonConnect = findViewById(R.id.btn_connect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, ScanDeviceActivity.class));

                if (com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED == ClientManager.getClient().getConnectStatus(overallMacAddress)) {

                    showDisConnectDialog();
                } else {

                    Log.e("AAAAAAA", "openBluetooth:"+ClientManager.getClient().isBluetoothOpened());
                    //判断蓝牙
                    if (ClientManager.getClient().isBluetoothOpened()) {
                        requestLocationPermission();
                    } else {
                        ClientManager.getClient().openBluetooth();
                    }


                }


            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data == null) {
                return;
            }
            String mac = data.getExtras().getString("mac");
            oprateConnect(mac);
        }
    }

    private void oprateConnect(String macAddress) {
        showLoading("蓝牙连接中。。。");
        closePrevious(macAddress);
        overallMacAddress = macAddress;
        ClientManager.getClient().registerConnectStatusListener(overallMacAddress, mConnectStatusListener);
        connectDevice(macAddress);
    }

    private void closePrevious(String macAddress) {
        if (TextUtils.isEmpty(overallMacAddress)) {
            return;
        }

        if (macAddress.equals(overallMacAddress)) {
            return;
        }
        ClientManager.getClient().disconnect(overallMacAddress);
        ClientManager.getClient().unregisterConnectStatusListener(overallMacAddress, mConnectStatusListener);


    }

    private void searchServiceAndCharacter(BleGattProfile profile) {

        ClientManager.getClient().notify(overallMacAddress, mService, mCharacter, mNotifyRsp);

//        List<BleGattService> services = profile.getServices();
//        for (BleGattService service : services) {
//            List<BleGattCharacter> characters = service.getCharacters();
//            for (BleGattCharacter character : characters) {
//                if(character.getProperty()==16){
////                    mService = service.getUUID();
////                    mCharacter = character.getUuid();
//                    Log.e("AAAAAAA", "mService:" + mService.toString());
//                    Log.e("AAAAAAA", "mCharacter:" + mCharacter.toString());
//                    ClientManager.getClient().notify(overallMacAddress, mService, mCharacter, mNotifyRsp);
//                    break;
//                }
//            }
//        }

    }


    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {

            if (service.equals(mService) && character.equals(mCharacter)) {

                try {
                    String result = new String(value, "UTF-8");
                    Log.e("AAAAAAA", "mNotifyRsp:" + result);
                    result = replaceBlank(result);
                    mTvCarNum.setText(result);
                    inBoundCar(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                Log.e("AAAAAAA", "订阅通知成功:");
            } else {
                Log.e("AAAAAAA", "订阅通知失败:");
            }
        }
    };


    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }


    private void connectDevice(String macAddress) {
        //29:F6:10:6A:A3:BE
        Log.e("AAAAAAA", "点击连接:" + macAddress);
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        ClientManager.getClient().connect(macAddress, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                BluetoothLog.e("蓝牙连接状态:" + code);
                BluetoothLog.v(String.format("profile:\n%s", profile));
                if (code == REQUEST_SUCCESS) {
                    Log.e("AAAAAAA", "蓝牙请求成功:" + code);
                    searchServiceAndCharacter(profile);

                } else {
                    Log.e("AAAAAAA", "蓝牙请求失败:" + code);
                }


            }
        });
    }


    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            mConnected = (status == STATUS_CONNECTED);
            if (mConnected) {
                mButtonConnect.setText("已连接" + overallMacAddress);
                dismissLoading();
            } else {
                mButtonConnect.setText("未连接设备");
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissLoading();
                }
            }, 2000);


        }
    };


    private void inBoundCar(String qrCodeIdentification) {
        if (isRequesting) {
            return;
        }
        mLinearLayout.setVisibility(View.GONE);
        isRequesting = true;
        showLoading("正在加载中，请稍等......");
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

                        praseResult(resultCode, objects);
                    }
                }));

            } else {
                JSONObject requestJson = new JSONObject();
                requestJson.put("qrCodeIdentification", qrCodeIdentification);

                HttpUtils.postString(MainActivity.this, address, RequestEntityUtils.getRequestEntity(requestJson), new TubeTask(MainActivity.this, Constants.GetDataFlag.HON_IN, new IResponse() {
                    @Override
                    public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                        praseResult(resultCode, objects);
                    }
                }));
            }

        } catch (Exception e) {
            isRequesting = false;
            dismissLoading();
            e.printStackTrace();
        }
    }


    private void praseResult(int resultCode, Object... objects) {
        isRequesting = false;
        dismissLoading();
        mLinearLayout.setVisibility(View.VISIBLE);
        Log.e("AAAAAAA:", "resultCode:" + resultCode);
        Log.e("AAAAAAA:", "objects:" + objects);
        if (objects == null) {
            return;
        }

        String errorMessage = (String) objects[0];
        mTvErrorMessage.setText(errorMessage);

        if (resultCode == 0) {
            DataItem dataItem = (DataItem) objects[1];
            mTvLocation.setText(dataItem.getLocDetail());
            mTvRequestStatus.setText("成功");
            mTvRequestStatus.setTextColor(Color.GREEN);
            Utils.showToast(MainActivity.this, "出库成功");
            return;
        }
        mTvRequestStatus.setText("失败");
        mTvRequestStatus.setTextColor(Color.RED);
        Utils.showToast(MainActivity.this, "出库失败");
        DataItem dataItem = (DataItem) objects[1];
        mTvLocation.setText(dataItem.getLocDetail());

    }


    private void showLoading(String text) {

        progressDialog = new ProgressDialog(MainActivity.this);//1.创建一个ProgressDialog的实例
        progressDialog.setMessage(text);//3.设置显示内容
        progressDialog.setCancelable(true);//4.设置可否用back键关闭对话框
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();//5.将ProgessDialog显示出来

    }

    private void dismissLoading() {

        if (progressDialog != null) {
            progressDialog.cancel();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void showExitDialog() {
        AlertDialog mAlertDialog = new AlertDialog.Builder(this).setMessage("确认退出？").setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        mAlertDialog.show();
    }


    protected void showDisConnectDialog() {
        AlertDialog mAlertDialog = new AlertDialog.Builder(this).setMessage("断开蓝牙？").setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClientManager.getClient().disconnect(overallMacAddress);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        mAlertDialog.show();
    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }else {
            Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("AAAAAAA", "申请成功");
                    Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClientManager.getClient().disconnect(overallMacAddress);
        ClientManager.getClient().unregisterConnectStatusListener(overallMacAddress, mConnectStatusListener);
    }
}

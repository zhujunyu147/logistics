package honeywell.honeywelllogistics.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;
import java.util.List;

import honeywell.honeywelllogistics.R;
import honeywell.honeywelllogistics.adapter.DeviceListAdapter;
import honeywell.honeywelllogistics.manager.ClientManager;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by zhujunyu on 2018/5/23.
 */

public class ScanDeviceActivity extends Activity {


    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private List<SearchResult> mDevices;
    private boolean mConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        initView();
    }

    private void initView() {
        mDevices = new ArrayList<SearchResult>();
        mListView = findViewById(R.id.list_item);
        mAdapter = new DeviceListAdapter(this);
        mAdapter.setClickCallback(new DeviceListAdapter.ClickCallback() {
            @Override
            public void onclick(SearchResult result) {
                String macAddress = result.getAddress();
//                ClientManager.getClient().registerConnectStatusListener(macAddress, mConnectStatusListener);
                Log.e("AAAAAAA", "点击连接:" + result.getName());
//                connectDevice(macAddress);
                Intent mIntent = new Intent();
                mIntent.putExtra("mac", macAddress);
                ScanDeviceActivity.this.setResult(MainActivity.REQUEST_CODE, mIntent);
                ScanDeviceActivity.this.finish();
            }
        });
        mListView.setAdapter(mAdapter);


        ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
            }
        });
        searchDevice();
    }


    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);

            Log.e("AAAAAAA", "mConnected:" + status);


        }
    };


    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {

        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device) && !"NULL".equals(device.getName())) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);
            }

        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped");
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled");
        }
    };

    /*****************************/

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

                } else {
                    Log.e("AAAAAAA", "蓝牙请求失败:" + code);
                }

            }
        });
    }

}

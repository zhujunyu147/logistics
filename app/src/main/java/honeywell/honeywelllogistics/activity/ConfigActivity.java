package honeywell.honeywelllogistics.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import honeywell.honeywelllogistics.R;
import honeywell.honeywelllogistics.utils.PreferenceUtil;
import honeywell.honeywelllogistics.utils.Utils;

/**
 * Created by zhujunyu on 2018/5/14.
 */

public class ConfigActivity extends Activity {


    private Spinner mSpinner;
    private List<String> dataList;
    private ArrayAdapter<String> adapter;
    private Button mButtonSave;
    private String httpFrefix = "http://";
    private EditText mEtAdress;
    private RadioButton rbMethdGet;
    private RadioButton rbMethdPost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);
        initView();
        initData();
    }

    private void initView() {
        mSpinner = findViewById(R.id.spinner);
        mEtAdress = findViewById(R.id.et_address);
        rbMethdGet = findViewById(R.id.get);
        rbMethdPost = findViewById(R.id.post);
    }

    private void initData() {
        dataList = new ArrayList<String>();
        dataList.add("http://");
        dataList.add("https://");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataList);
        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为spinner绑定我们定义好的数据适配器
        mSpinner.setAdapter(adapter);
        //为spinner绑定监听器，这里我们使用匿名内部类的方式实现监听器
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                httpFrefix = dataList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEtAdress.setText("222.126.229.158:8580/wms-extra-api/WmsInterfaceForHN/inboundCar");
        String address = PreferenceUtil.getString(this, PreferenceUtil.URL_ADDRESS, PreferenceUtil.URL_DEFAULT_ADDRESS);
        if (!TextUtils.isEmpty(address)) {
            String result;
            if (address.startsWith("http://")) {
                result = address.replace("http://", "");
            } else  {
                result = address.replace("https://", "");
            }
            mEtAdress.setText(result);
        }


        mButtonSave = findViewById(R.id.save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mEtAdress.getText())) {
                    Utils.showToast(ConfigActivity.this, "请输入有效地址...");
                    return;
                }
                String wholeAdress = httpFrefix + mEtAdress.getText().toString().trim();

                PreferenceUtil.saveUrlAddress(ConfigActivity.this, wholeAdress);

                PreferenceUtil.saveHttpMethod(ConfigActivity.this, rbMethdGet.isChecked() ? "HTTP_GET" : "HTTP_POST");


                Utils.showToast(ConfigActivity.this, "保存成功...");
                ConfigActivity.this.finish();

            }
        });

        String method = PreferenceUtil.getString(this, PreferenceUtil.HTTP_MODE, PreferenceUtil.HTTP_DEFAULT);

        if (method.equals("HTTP_POST")) {
            rbMethdPost.setChecked(true);
        } else {
            rbMethdGet.setChecked(true);
        }


    }

}

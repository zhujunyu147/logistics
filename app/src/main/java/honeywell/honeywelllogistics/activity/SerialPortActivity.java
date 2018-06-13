package honeywell.honeywelllogistics.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.serialport.SerialPort;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhujunyu on 2018/5/17.
 */

public abstract class SerialPortActivity extends Activity {
    protected MyApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    Log.e("AAAAA", "AAAAA----" + size);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Error");
        dialog.setMessage(resourceId);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SerialPortActivity.this.finish();
            }
        });
        dialog.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (MyApplication) getApplication();
//        try {
//            mSerialPort = mApplication.getSerialPort();
//            mOutputStream = mSerialPort.getOutputStream();
//            mInputStream = mSerialPort.getInputStream();
//
//			/* Create a receiving thread */
//            mReadThread = new ReadThread();
//            mReadThread.start();
//        } catch (SecurityException e) {
//            DisplayError(R.string.error_security);
//        } catch (IOException e) {
//            DisplayError(R.string.error_unknown);
//        } catch (InvalidParameterException e) {
//            DisplayError(R.string.error_configuration);
//        }
    }

    @Override
    protected void onDestroy() {
        if (mReadThread != null) mReadThread.interrupt();
        mApplication.closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

}

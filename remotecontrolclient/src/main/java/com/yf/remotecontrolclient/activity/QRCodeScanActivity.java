package com.yf.remotecontrolclient.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yf.minalibrary.common.MessageType;
import com.yf.minalibrary.message.FileMessage;
import com.yf.remotecontrolclient.R;
import com.yf.remotecontrolclient.minaclient.tcp.DeviceInfo;
import com.yf.remotecontrolclient.minaclient.tcp.ServerDataDisposeCenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;


public class QRCodeScanActivity extends BaseActivity implements QRCodeView.Delegate {
    public static final String ID_HEAD_FLAG = "YF_HUAERSI";
    @BindView(R.id.zxingview) QRCodeView zxingview;
    @BindView(R.id.open_light) Button openLight;

    private boolean isOpenLight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodescan);
        ButterKnife.bind(this);
        zxingview.setDelegate(this);
        openLight.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isOpenLight){
                    zxingview.closeFlashlight();
                    isOpenLight = false;
                } else {
                    zxingview.openFlashlight();
                    FileMessage.FileBean bean = new FileMessage.FileBean(Environment.getExternalStorageDirectory()+"/test.png");
                    FileMessage fileMessage = new FileMessage(ServerDataDisposeCenter.getLocalSenderId(),
                            ServerDataDisposeCenter.getRemoteReceiverId(),MessageType.MESSAGE_FILE,bean);
                    Log.d("IoClientHandler", "Received filename = " + bean.getFileName());
                    File file = new File(Environment.getExternalStorageDirectory() + "/tupian");
                    boolean b = file.exists();
                    if (!b) {
                        b = file.mkdir();
                    }
                    if (b) {
                        try {
                            FileOutputStream os = new FileOutputStream(file.getPath() + "/" + bean.getFileName());
                            os.write(bean.getFileContent());
                            os.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    @Override protected void onStart() {
        super.onStart();
        zxingview.startCamera();
        zxingview.showScanRect();
        zxingview.startSpot();
    }

    @Override protected void onStop() {
        zxingview.stopCamera();
        super.onStop();
    }

    @Override protected void onDestroy() {
        zxingview.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d("QRCodeScanActivity", result);
        vibrate();
        String[] strings = result.split(",");
        if (strings.length > 1 && strings[0].equals(ID_HEAD_FLAG)) {
            ServerDataDisposeCenter.addDevice(new DeviceInfo("room1", strings[1]));
            Toast.makeText(this, "设备已添加", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "请确认二维码是否匹配", Toast.LENGTH_SHORT).show();
            zxingview.startSpot();
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.d("QRCodeScanActivity", "打开相机出错");
    }
}

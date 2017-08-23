package com.google.zxing.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.google.zxing.client.android.BaseCaptureActivity;
import com.google.zxing.listener.ResultListener;
import com.google.zxing.utils.PicDecode;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 模仿微信的扫描界面
 */
public class WeChatCaptureActivity extends BaseCaptureActivity {

    private static final String TAG = WeChatCaptureActivity.class.getSimpleName();
    private static int colorPrimary = 0;
    private SurfaceView surfaceView;
    private AutoScannerView autoScannerView;
    private TextView mTitle;
    private LinearLayout lLeft;
    private LinearLayout lRight;
    private RelativeLayout titlebarBackground;
    private SeekBar mSeekbar;
    private ImageView mSelect;
    private Intent intent = new Intent();
    private boolean isTorchOpenning = false;
    private static String title = "二维码扫描";
    private static ResultListener resultListener;
    private static final String tag = "WeChatCaptureActivity";
    public static String result;

    public static void init(@NonNull Activity context, ResultListener resultListener, int colorPrimary, @NonNull String title) {
        try {//检查颜色是否为0
            if (!title.equals("")) {
                WeChatCaptureActivity.title = title;
            }
            if (colorPrimary == 0) {
                colorPrimary = context.getResources().getColor(R.color.colorPrimary);
            } else {
                Log.d(TAG, colorPrimary + "");
            }
        } catch (Exception e) {
            colorPrimary = context.getResources().getColor(R.color.colorPrimary);
            e.printStackTrace();
        }

        WeChatCaptureActivity.colorPrimary = colorPrimary;

        try {
            context.startActivityForResult((new Intent()).setClass(context, WeChatCaptureActivity.class), 1001);
        } catch (Exception e) {
            e.printStackTrace();

        }
        if (resultListener != null) {
            WeChatCaptureActivity.resultListener = resultListener;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri uri;
        Uri uriTemp = null;
        if (resultCode == RESULT_OK) {//识别返回的本地二维码图片
            try {
                uriTemp = data.getData();
                Log.e(tag, resultCode + "onActivityResult " + uriTemp.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (uriTemp != null) {
                uri = uriTemp;
            } else {
                return;
            }
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result = PicDecode.scanImage(WeChatCaptureActivity.this, uri).getText();

                        if (!result.equals("")) {
                            WeChatCaptureActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    putResult(result);
                                }
                            });
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(tag, e.getMessage());
                    }
                }
            })).start();
        }
    }

    @Override//检查摄像头权限
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            toast("获取摄像头权限失败");
        }
    }

    void toast(String s) {
        Toast.makeText(WeChatCaptureActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wechat_capture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            }
        }

        //---------------------------
        titlebarBackground = (RelativeLayout) findViewById(R.id.titlebar_background);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        autoScannerView = (AutoScannerView) findViewById(R.id.autoscanner_view);
        mTitle = (TextView) findViewById(R.id.titlebar_tv_title);
        lLeft = (LinearLayout) findViewById(R.id.titlebar_ll_left);
        lRight = (LinearLayout) findViewById(R.id.titlebar_ll_right);
        mSeekbar = (SeekBar) findViewById(R.id.zoom_seekbar);
        mSelect = (ImageView) findViewById(R.id.iv_select_photo);
        //---------------------------
        titlebarBackground.setBackgroundColor(colorPrimary);
        mSelect.setBackgroundColor(colorPrimary);
        //---------------------------
        mTitle.setText(title);

        lRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//闪光灯


                if (isTorchOpenning) {
                    isTorchOpenning = false;
                    lRight.setBackgroundColor(getResources().getColor(R.color.transparent));
                } else {
                    isTorchOpenning = true;
                    lRight.setBackgroundColor(getResources().getColor(R.color.viewfinder_mask));
                }
                getCameraManager().setTorch(isTorchOpenning);
            }
        });

        lLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPic();
            }
        });

        mSeekbar.setProgress(0);//监听滑动栏数值，变焦
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                getCameraManager().zoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //获取最大变焦值
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int max = getCameraManager().getMaxZoom();
                            Log.d("maxZoom", max + "");
                            mSeekbar.setMax(max);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 50);


    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScannerView.setCameraManager(cameraManager);
    }

    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        result = rawResult.getText();
        putResult(result);
//        对此次扫描结果不满意可以调用
//        reScan();
    }

    private void selectPic() {//选择本地二维码
        Intent innerIntent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_PICK);
        }
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        startActivityForResult(wrapperIntent, 1002);
    }

    private void putResult(String result) {//返回扫描结果
        intent.putExtra("result", result);
        playBeepSoundAndVibrate(true, true);
        setResult(1001, intent);//返回string结果
        if (resultListener != null) {
            resultListener.onResult(result);
        }
        finish();
    }
}

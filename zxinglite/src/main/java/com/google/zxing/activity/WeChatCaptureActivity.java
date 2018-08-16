package com.google.zxing.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
import com.google.zxing.utils.PhotoBitmapUtils;
import com.google.zxing.utils.PicDecode;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 模仿微信的扫描界面
 */
public class WeChatCaptureActivity extends BaseCaptureActivity {
    private static final String TAG = "WeChatCaptureActivity";
    private static final int select_requestCode = 0xb002;
    public static final int scan_requestCode = 0xb001;
    public Bitmap bitmap = null;
    private static int colorPrimary = 0;
    private static String title = "";
    private static ResultListener resultListener;
    private Handler handlerZoom;
    private Thread resultThread;
    private static int max = 0;
    public String result;
    private Runnable getMaxZoomRunnable;
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


    public static void init(@NonNull Activity context, ResultListener resultListener, int colorPrimary, @NonNull String title) {
        try {//检查颜色是否为0
            title = context.getString(R.string.default_title);
            if (!TextUtils.isEmpty(title)) {
                WeChatCaptureActivity.title = title;
            }
            if (colorPrimary == 0) {
                colorPrimary = context.getResources().getColor(R.color.colorPrimary);
            } else {
            }
        } catch (Exception e) {
            colorPrimary = context.getResources().getColor(R.color.colorPrimary);
            e.printStackTrace();
        }

        WeChatCaptureActivity.colorPrimary = colorPrimary;//主题色


        if (resultListener != null) {//结果监听器
            WeChatCaptureActivity.resultListener = resultListener;
        } else {
            WeChatCaptureActivity.resultListener = null;
        }

        try {
            context.startActivityForResult((new Intent()).setClass(context, WeChatCaptureActivity.class), scan_requestCode);
        } catch (Exception e) {
            e.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (uriTemp != null) {
                uri = uriTemp;
            } else {

                return;
            }
            resultThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                        result = PicDecode.scanImage(WeChatCaptureActivity.this, uri).getText();

                        if (!TextUtils.isEmpty(result)) {
                            surfaceView.post(new Runnable() {
                                @Override
                                public void run() {
                                    putResult(result);
                                }
                            });
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        result = getString(R.string.no_result);
                        surfaceView.post(new Runnable() {
                            @Override
                            public void run() {
                                putResult(result);
                            }
                        });
                        e.printStackTrace();
                    }
                }
            });
            resultThread.start();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override//检查摄像头权限
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            toast(getString(R.string.get_camera_fail));
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            toast(getString(R.string.get_sd_fail));
        }
    }

    void toast(String s) {
        Toast.makeText(WeChatCaptureActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wechat_capture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
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
                if (mSeekbar.getMax() == 0) {
                    if (handlerZoom != null) {
                        handlerZoom.postDelayed(getMaxZoomRunnable, 10);
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //获取最大变焦值
        getMaxZoomRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    max = getCameraManager().getMaxZoom();
                    Log.d("maxZoom", max + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mSeekbar.setMax(max);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        handlerZoom = new Handler();
        handlerZoom.postDelayed(getMaxZoomRunnable, 100);


    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScannerView.setCameraManager(cameraManager);
    }

    @Override
    protected void onDestroy() {
        colorPrimary = 0;
        mSelect.setImageResource(0);

        if (resultThread != null && resultThread.isAlive()) {
            try {
                resultThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            resultThread = null;
        }
        if (handlerZoom != null) {
            handlerZoom.removeCallbacksAndMessages(null);
        }
        handlerZoom = null;
        resultListener = null;

        super.onDestroy();
    }

    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        result = rawResult.getText();
        this.bitmap=barcode;
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
        Intent wrapperIntent = Intent.createChooser(innerIntent, getString(R.string.choose_qrcode));
        startActivityForResult(wrapperIntent, select_requestCode);
    }

    private void putResult(String result) {//返回扫描结果
        intent.putExtra("result", result);
        Bundle bundle=new Bundle();
        if (bitmap!=null) {
            Float scale=200f/(float)bitmap.getWidth();
            bitmap= PhotoBitmapUtils.getScaleBitmap(bitmap,scale);
            bundle.putParcelable("bitmap",bitmap);
        }
        intent.putExtras(bundle);
        playBeepSoundAndVibrate(true, true);
        setResult(RESULT_OK, intent);//返回string结果
        if (resultListener != null) {
            resultListener.onResult(result);
        }
        if (result != getString(R.string.no_result)) {
            finish();
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

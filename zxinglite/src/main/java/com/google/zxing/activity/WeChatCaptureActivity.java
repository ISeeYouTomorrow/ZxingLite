package com.google.zxing.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.R;
import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.google.zxing.client.android.BaseCaptureActivity;
import com.google.zxing.listener.ResultListener;

/**
 * 模仿微信的扫描界面
 */
public class WeChatCaptureActivity extends BaseCaptureActivity {

    private static final String TAG = WeChatCaptureActivity.class.getSimpleName();
    private static int colorPrimary=0;
    private SurfaceView surfaceView;
    private AutoScannerView autoScannerView;
    private TextView mTitle;
    private LinearLayout lLeft;
    private LinearLayout lRight;
    private RelativeLayout titlebarBackground;
    private Intent intent=new Intent();
    private boolean isTorchOpenning=false;
    private static String title="二维码扫描";
    private static ResultListener resultListener;
    public static String result;

    public static void init(@NonNull Activity context,ResultListener resultListener, int colorPrimary, @NonNull String title){
        try {//检查颜色是否为0
            if(!title.equals("")){
                WeChatCaptureActivity.title=title;
            }
            if (colorPrimary == 0) {
                colorPrimary=context.getResources().getColor(R.color.colorPrimary);
            } else {
                Log.d(TAG, colorPrimary + "");
            }

        } catch (Exception e) {
            colorPrimary=context.getResources().getColor(R.color.colorPrimary);
            e.printStackTrace();
        }

        WeChatCaptureActivity.colorPrimary=colorPrimary;

        try {
            context.startActivityForResult((new Intent()).setClass(context,WeChatCaptureActivity.class),1001);
        } catch (Exception e) {
            e.printStackTrace();

        }
        if (resultListener!=null) {
            WeChatCaptureActivity.resultListener=resultListener;
        } else {
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wechat_capture);


        //---------------------------
        titlebarBackground=(RelativeLayout) findViewById(R.id.titlebar_background);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        autoScannerView = (AutoScannerView) findViewById(R.id.autoscanner_view);
        mTitle =(TextView) findViewById(R.id.titlebar_tv_title);
        lLeft=(LinearLayout) findViewById(R.id.titlebar_ll_left);
        lRight=(LinearLayout) findViewById(R.id.titlebar_ll_right);
        //---------------------------
        titlebarBackground.setBackgroundColor(colorPrimary);
        mTitle.setText(title);

        lRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//闪光灯


                if (isTorchOpenning) {
                    isTorchOpenning=false;
                    lRight.setBackgroundColor(getResources().getColor(R.color.transparent));
                } else {
                    isTorchOpenning=true;
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
        result=rawResult.getText();
        intent.putExtra("result",result);
        playBeepSoundAndVibrate(true, true);
        setResult(1001,intent);//返回string结果
        resultListener.onResult(result);
        finish();
//        对此次扫描结果不满意可以调用
//        reScan();
    }
}

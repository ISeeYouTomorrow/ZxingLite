package yangxixi.zxinglib;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.activity.WeChatCaptureActivity;
import com.google.zxing.listener.ResultListener;

public class MainActivity extends AppCompatActivity implements ResultListener{

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //法一
        if(requestCode==1001){
            try {

                if (data!=null) {
                    String result=data.getStringExtra("result");
                    toast(result);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("main",e.getMessage().toString());
            }
        }
    }

    void toast(String s){
        Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button defaultStart = (Button) findViewById(R.id.default_start);
        defaultStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DefaultCaptureActivity.class);
                startActivity(intent);
            }
        });

        Button weStart = (Button) findViewById(R.id.wechat_start);
        weStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WeChatCaptureActivity.init(MainActivity.this,MainActivity.this,getResources().getColor(R.color.colorPrimary),"");
//      或者
//    WeChatCaptureActivity.init(MainActivity.this,null,getResources().getColor(R.color.colorPrimary),"");


            }
        });

    }

    @Override
    public void onResult(String result) {//法二
        toast("onResult: "+result);
    }
}

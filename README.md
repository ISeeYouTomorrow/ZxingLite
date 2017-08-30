# 基于zxing的仿微信二维码扫描界面
## 本项目fork自yangxixi88/ZxingLite，在原项目基础上添加了本地图片识别,闪光灯和结果监听器,识别出结果后可以通过onActivityResult或者自定义ResultListener获取String结果
# 使用方法
#### Add it in your root build.gradle at the end of repositories:
```java
allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
#### Add the dependency
```java
dependencies {
	        compile 'com.github.iamlocky:ZxingLite:1.0.1'
	}
```
#### 如果gradle报错则改为
```java
compile ('com.github.iamlocky:ZxingLite:1.0.1',{
        exclude group: 'com.android.support', module:'appcompat-v7'
    })
```
#### manifests需要有三项权限
```java
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.VIBRATE" />
```
#### 如果启动报错找不到activity则添加
```java
<activity
            android:name="com.google.zxing.activity.WeChatCaptureActivity"
            android:screenOrientation="portrait"
            android:launchMode="standard"
            />
```
#### 准备工作做完了（注意高版本要动态申请权限）
## 开始使用
#### 直接用ResultListener
```java
WeChatCaptureActivity.init(context, new ResultListener() {
            @Override
            public void onResult(String s) {
                //处理返回的结果s;
            }
        }, getResources().getColor(R.color.colorPrimary), "二维码扫描");
```
#### 或者第二个参数传null，直接用onActivityResult
```java
WeChatCaptureActivity.init(context, null, getResources().getColor(R.color.colorPrimary), "二维码扫描2");
```
```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
			String result = data.getStringExtra("result");
			//处理结果
        }
    }
```
## 效果图
![](https://github.com/iamlocky/ZxingLite/blob/master/screenShots/Screenshot_2017-08-30-18-45-44-283_com.ygip.ipbas.png)
![](https://github.com/iamlocky/ZxingLite/blob/master/screenShots/Screenshot_2017-08-30-18-46-24-274_com.ygip.ipbas.png)
![](https://github.com/iamlocky/ZxingLite/blob/master/screenShots/Screenshot_2017-08-30-19-00-25-533_yangxixi.zxing.png)

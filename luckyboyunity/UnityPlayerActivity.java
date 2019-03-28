package com.jhz.luckyboyunity;

import com.efrobot.library.RobotManager;
import com.efrobot.library.task.SpeechGroupManager;
import com.unity3d.player.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.efrobot.library.RobotState;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.net.BaseSendRequestListener;
import com.efrobot.library.net.NetClient;
import com.efrobot.library.net.NetMessage;
import com.efrobot.library.net.TextMessage;
import com.efrobot.library.net.utils.NetUtil;
import com.efrobot.library.urlconfig.UrlConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;
import com.efrobot.claw.game.sdk.*;



public class UnityPlayerActivity extends Activity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private   AlertObject alerObj;
    private   BaseHandler mBaseHandler;
    private   boolean canPlay = false;
    private  boolean isPay=false;
    private  boolean isForeground=true;
    private  int times=10;
    private  Timer timer;
    private  String carwTime="";
    private  boolean isCarw=false;
    private  String orderNumber="";
    private  String openId="";
    private SpeechGroupManager mGroupManager;
    private int count;

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        L.d(TAG, "---------------onCreate------------");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        count=0;
        mUnityPlayer = new UnityPlayer(this);
        Splash.getInstance().onCreate(mUnityPlayer,savedInstanceState);
        Splash.getInstance().onShowSplash(R.drawable.loading_2);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
        mGroupManager=SpeechGroupManager.getInstance(RobotManager.getInstance(this));
        ClawGameManager.getInstance(getApplicationContext()).init();//初始化
        alerObj=new AlertObject(this);
        timer = new Timer();// 实例化Timer类
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        L.d(TAG, "---------------onDestroy------------");
        mUnityPlayer.quit();
        super.onDestroy();
        alerObj.unRegister();//解除监听
    }

    // Pause Unity
    @Override protected void onPause()
    {
        L.d(TAG, "---------------onPause------------");
        alerObj.isAppQuit=true;//此时已退出游戏
		mGroupManager.reset();
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        L.d(TAG, "---------------onResume------------");
        alerObj.isAppQuit=false;//继续
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        L.d(TAG, "---------------onStart------------");
        if(isForeground) {
            super.onStart();
            mUnityPlayer.start();
        }
        else
        {
            onDestroy();
            timer.cancel();
            Intent intent = new Intent();
            PackageManager packageManager = getPackageManager();
            intent = packageManager.getLaunchIntentForPackage("com.jhz.luckyboyunity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
            startActivity(intent);
        }
    }

    @Override protected void onStop()
    {
        L.d(TAG, "---------------onStop------------");
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
   // @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

    public String getRobotId() {
        //获取小胖唯一编码
        String number = RobotState.getInstance(getApplicationContext()).getRobotNumber();
        if (number == null) {
            number = "";
        }
        return number;
    }

    public void SpeakWords(String msg)
    {
        L.d("SpeakWords", "unity调用了SpeakWords方法");
        alerObj.speak(msg);
    }

    public void ShakeWave(int time)
    {
        L.d("ShakeWave", "unity调用了ShakeWave方法");
        alerObj.wave(time);
    }
    public void OpenLight(boolean n, int time)
    {
        L.d("OpenLight", "unity调用了OpenLight方法");
        alerObj.light(n,time);
    }
    //摆动翅膀闪光带
    public  void ShakeWaveLight(boolean state)
    {
        L.d("ShakeWaveLight", "unity调用了ShakeWaveLight方法");
        alerObj.wonDoll(state);
    }

    /**
     * 初始化一个Handler，如果需要使用Handler，先调用此方法，
     */
    public  void initHandler( ) {
        mBaseHandler = new BaseHandler(this);
    }

    /**
     * 返回Handler，在此之前确定已经调用initHandler（）
     *
     * @return Handler
     */
    public  Handler getHandler() {
        initHandler();
        return mBaseHandler;
    }

    protected   class BaseHandler extends Handler {
        private final WeakReference<UnityPlayerActivity> mObjects;

        public BaseHandler(UnityPlayerActivity mPresenter) {
            mObjects = new WeakReference<UnityPlayerActivity>(mPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            UnityPlayerActivity mPresenter = mObjects.get();
            if (mPresenter != null)
            mPresenter.handleMessage(msg);
}
}
        private void handleMessage(Message msg) {

        }

   //自动出礼物
    public  void AutoPresent()
    {
        //ClawGameManager.getInstance(getApplicationContext()).getControlManagerInstance(getApplicationContext()).pushRod();
        ClawGameManager.getInstance(getApplicationContext()).getControlManagerInstance(getApplicationContext()).trackForwardOnce(2);//抓成功出娃娃
    }
    //是否能玩
    public  boolean isCanPlay()
    {
        if(ClawGameManager.getInstance(getApplicationContext()).checkDollExit() ==  ClawGameStatus.VALUE_DOLL_NO_HAS)
        {
          //UnityPlayer.UnitySendMessage("AndroidCallUnity","AndroidCall","NoHas");
            return false;
        }
        return  true;
    }
    //获得游戏模式
    public  String GetGameModeData()
    {
      return  Constants.GetGameModeData();
    }
    //获得问题 及答案
    public  String GetQuestionAnswer()
    {
        return  alerObj.jsonAnswer();
    }
    //答题结束
    public  void AnswerStartOrEnd(boolean state)
    {
        alerObj.RegisterWingListener(state);
    }

    //获得支付页面语音
    public  String GetPayPageVoice(){return alerObj.jsonPayVoice();}

    //关闭Splash页面
    public  void  HideSplash(){ Splash.getInstance().onHideSplash();}
    //是否测试环境
    public  boolean IsText(){return  Constants.IsText();}

    //选择进入那个游戏
    public String SelectGame(){ return Constants.SelectGame();}

    //获得优惠券数据
    public  String GetOnSaleNumberData(){ return   alerObj.jsonOnSaleContent();}
    //更新优惠券状态
    public  void UpdateOnSaleValue(String id){ alerObj.updateOnSaleValue(id);}

  //切到前台
    private  void  MoveForeground()
    {
        L.d("MoveForeground", "切到前台");
        isForeground=true;
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        am.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
        UnityPlayer.UnitySendMessage("AndroidCallUnity","AndroidCall","PaySuccess");
    }

    public void CustomQuit()
    {
        L.d("CustomQuit", "开始退出");
        moveTaskToBack(true);
        isForeground=false;
        timer.schedule(new TimerTask() {
            public void run() {
                if(times<=0&&!isPay)
                {
                    L.d("CustomQuit", "没有支付游戏推出");
                    finish();
                }
                else
                {
                    if(!isPay) {
                       // GetPayStatus(orderNumber,false);
                    }
                    else {
                        L.d("timer", "已支付完成");
                        MoveForeground();
                        timer.cancel();
                    }
                }
                times=times-1;
            }
        },0, 1000);// 这里百毫秒

    }



    /********************** 下面是下来关闭游戏逻辑 ********************/

    private long startTime = -1;
    private long endTime = -1;
    private int touchFirstId = -1;
    private int touchSecondId = -1;
    private float touchFirstStartX = -1;
    private float touchFirstStartY = -1;
    private float touchSecondStartX = -1;
    private float touchSecondStartY = -1;
    private float touchFirstEndX = -1;
    private float touchFirstEndY = -1;
    private float touchSecondEndX = -1;
    private float touchSecondEndY = -1;

    private void initTouchEventData() {
        startTime = -1;
        endTime = -1;
        touchFirstId = -1;
        touchSecondId = -1;
        touchFirstStartX = -1;
        touchFirstStartY = -1;
        touchSecondStartX = -1;
        touchSecondStartY = -1;
        touchFirstEndX = -1;
        touchFirstEndY = -1;
        touchSecondEndX = -1;
        touchSecondEndY = -1;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "viewSizeHandle: down"+event.getActionIndex());
                Log.e(TAG, "onTouchEvent1: x = " + event.getX() + " , y = " + event.getY() );

                initTouchEventData();
                touchFirstId = event.getPointerId(event.getActionIndex());
                startTime = System.currentTimeMillis();
                touchFirstStartX = event.getX(event.getActionIndex());
                touchFirstStartY = event.getY(event.getActionIndex());

                if (!(touchFirstStartY < 30 && (touchFirstStartX > 1230 || touchFirstStartX < 70))) {
                    initTouchEventData();
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "viewSizeHandle: point down"+event.getActionIndex());

                //第一个点没有记录
                if (touchFirstId == -1) {
                    initTouchEventData();
                    break;
                }

                if (touchSecondId != -1 || System.currentTimeMillis() - startTime >= 500) {
                    initTouchEventData();
                    break;
                }

                touchSecondId = event.getPointerId(event.getActionIndex());
                touchSecondStartX = event.getX(event.getActionIndex());
                touchSecondStartY = event.getY(event.getActionIndex());

                if (touchSecondStartY >= 30
                        || (touchSecondStartX <= 1230 && touchSecondStartX >= 70)
                        || (touchFirstStartX > 700 && touchSecondStartX > 700)
                        || (touchFirstStartX < 700 && touchSecondStartX < 700)) {
                    initTouchEventData();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "viewSizeHandle-: move"+event.getActionIndex());
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "viewSizeHandle: up"+event.getActionIndex());
                Log.e(TAG, "onTouchEvent2: x = " + event.getX() + " , y = " + event.getY() );

                if (touchFirstId == -1 || touchSecondId == -1) {
                    initTouchEventData();
                    break;
                }

                int currentId = event.getPointerId(event.getActionIndex());

                if (currentId == touchFirstId) {
                    if (touchFirstEndX != -1) {
                        initTouchEventData();
                        break;
                    }

                    touchFirstEndX = event.getX(event.getActionIndex());
                    touchFirstEndY = event.getY(event.getActionIndex());

                    if (Math.abs(touchFirstEndX - touchFirstStartX) >= 70 || touchFirstEndY <= 750) {
                        initTouchEventData();
                        break;
                    }
                } else if (currentId == touchSecondId) {
                    if (touchSecondEndX != -1) {
                        initTouchEventData();
                        break;
                    }

                    touchSecondEndX = event.getX(event.getActionIndex());
                    touchSecondEndY = event.getY(event.getActionIndex());

                    if (Math.abs(touchSecondEndX - touchSecondStartX) >= 70 || touchSecondEndY <= 750) {
                        initTouchEventData();
                        break;
                    }
                } else {
                    initTouchEventData();
                    break;
                }

                if (System.currentTimeMillis() - endTime < 500) {
                    /******************关闭游戏逻辑*********************/

                    UnityPlayer.UnitySendMessage("AndroidCallUnity","CodePageGameQuit","");

                    /******************关闭游戏逻辑*********************/
                    initTouchEventData();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "viewSizeHandle: point up"+event.getActionIndex());

                if (touchFirstId == -1) break;

                int pointerId = event.getPointerId(event.getActionIndex());

                if (pointerId == touchFirstId) {
                    touchFirstEndX = event.getX(event.getActionIndex());
                    touchFirstEndY = event.getY(event.getActionIndex());

                    if (Math.abs(touchFirstEndX - touchFirstStartX) >= 70 || touchFirstEndY <= 750) {
                        initTouchEventData();
                        break;
                    }
                } else if (pointerId == touchSecondId) {
                    touchSecondEndX = event.getX(event.getActionIndex());
                    touchSecondEndY = event.getY(event.getActionIndex());

                    if (Math.abs(touchSecondEndX - touchSecondStartX) >= 70 || touchSecondEndY <= 750) {
                        initTouchEventData();
                        break;
                    }
                } else {
                    initTouchEventData();
                    break;
                }

                endTime = System.currentTimeMillis();

                break;
        }
        return true;
    }

}

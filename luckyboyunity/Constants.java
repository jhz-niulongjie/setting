package com.jhz.luckyboyunity;

import android.os.Environment;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.net.utils.FileUtil;
import com.efrobot.library.urlconfig.UrlConstants;

import junit.framework.Test;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


import static android.content.ContentValues.TAG;

/**
 * @项目名称:幸运抓娃娃
 * @类名称：Contants
 * @类描述：接口公共类
 * @创建人：luyuqin
 * @创建时间：2017/11/2314:51
 * @修改时间：2017/11/2314:51
 * @备注：
 */



public class Constants {

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "/efrobot/";
    private static final String URL_TYPE_IN_CONFIG_PATH= ROOT_PATH + "robot_url_in.config";
    private static final String URL_TYPE_OUT_CONFIG_PATH= ROOT_PATH + "robot_url_out.config";

    private  static final  String FiveRoundConfig=Environment.getExternalStorageDirectory() + "/.config/.efrobot/game_config";

    private  static  String  ReadSettingValue="";


    //是否是测试环境
    public  static  boolean IsText()
    {
        File filePath = new File(URL_TYPE_IN_CONFIG_PATH);
        return  filePath.exists();
    }

   //设置中的数据
    public  static String  GetGameModeData()
    {
       // 支付模式|三局|5个题目|pass 3|进入游戏|抓娃娃游戏|开启礼品模式 0|是否开启常驻支付页面 0
        //     |是否显示自定义结束弹窗 0 |结束页面显示时间 0|结束页面显示图片路径|结束页面语音数据
        String[] values=new String[]{"0","3","5","3","0","0","0","0","0","0","",""};
        try {
            if(ReadSettingValue==null||ReadSettingValue=="") {
                ReadSettingValue = ReadSettingValueFun();
            }
            if(ReadSettingValue!=null||ReadSettingValue!="")
            {
                L.d(TAG, "ReadSettingValue ==="+ReadSettingValue);
                JSONObject jsonObject = new JSONObject(ReadSettingValue);
                if(jsonObject.has("model")){
                    values[0]=jsonObject.optString("model");
                }
                if(jsonObject.has("mission")) {
                    values[1]=jsonObject.optString("mission");
                }
                if(jsonObject.has("question")) {
                    values[2]=jsonObject.optString("question");
                }
                if(jsonObject.has("pass")) {
                    values[3]=jsonObject.optString("pass");
                }
                if(jsonObject.has("gift_model")) {
                    values[4]=jsonObject.optString("gift_model");// 是否进入游戏
                }
                if(jsonObject.has("game_type")) {
                    values[5]=jsonObject.optString("game_type");//游戏类型 0抓娃娃  1幸运转转
                }
                if(jsonObject.has("reward")) {
                    values[6]=jsonObject.optString("reward");//开启礼品模式 为0 关闭 为1
                }
                if(jsonObject.has("pay_model")) {
                    values[7]=jsonObject.optString("pay_model");//开启常驻支付页面 为0 关闭 为1
                }
                if(jsonObject.has("end_model")) {
                    values[8]=jsonObject.optString("end_model");//开启自定义结束弹窗 为0 关闭 为1
                }
                if(jsonObject.has("show_time")) {
                    values[9]=jsonObject.optString("show_time");//结束页面显示时间
                }
                if(jsonObject.has("inmage_path")) {
                    values[10]=jsonObject.optString("inmage_path");//结束页面图片路径 以逗号隔开
                }
                if(jsonObject.has("lexical_matching")) {
                    values[11]=jsonObject.optString("lexical_matching");//结束页面语音
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        StringBuilder sbd=new StringBuilder();
        for (int i=0;i<values.length;i++)
        {
            sbd.append(values[i]);
            if(i!=values.length-1)
                sbd.append("|");
        }
        return  sbd.toString();
    }

  //选择游戏类型
    public static String SelectGame()
    {
        String defualStr="0";// 抓娃娃游戏
        try {
            String settingValue=  ReadSettingValueFun();
            if(settingValue!=null||settingValue!="")
            {
                ReadSettingValue=settingValue;
                JSONObject jsonObject = new JSONObject(settingValue);
                if (jsonObject.has("game_type")) {
                    return jsonObject.optString("game_type");//游戏类别
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  defualStr;
    }

    private static String ReadSettingValueFun()
    {
        File filePath = new File(FiveRoundConfig);
        if(filePath.exists())
        {
            try
            {
                StringBuilder localStr=new StringBuilder();
                InputStreamReader  inputRead=new InputStreamReader(new FileInputStream(filePath),"utf-8");
                BufferedReader  bufferReader=new BufferedReader(inputRead);
                String linStr=null;
                while ((linStr=bufferReader.readLine())!=null)
                {
                    localStr.append(linStr);
                }
                bufferReader.close();
                inputRead.close();
                linStr=localStr.toString();
                if(linStr!=null||linStr!="")
                {
                    return  linStr;
                }
                return null;
            }
            catch(Exception e)
            {
                L.d(TAG, "--ReadSettingValue error-");
                e.printStackTrace();
            }
        }
        return  null;
    }

}

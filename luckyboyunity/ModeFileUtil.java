package com.jhz.luckyboyunity;

import android.os.Environment;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by chirenjie on 2018/9/17.
 */
public class ModeFileUtil {

    public static final String SPEECH_MODEL = "speech_model";
    public static final String PAY_MODEL = "pay_model";
    public static final int PAY_MODEL_OPEN = 1;
    public static final int PAY_MODEL_CLOSE= 0;

    public static final String GAME_SPEECH_MODEL = "game_speech_model";
    public static final int GAME_SPEECH_MODEL_MODEL_OPEN = 1;
    public static final int GAME_SPEECH_MODEL_MODEL_CLOSE= 0;

    public static final String CLOSE_FACE_MODEL = "close_face_model";
    public static final int CLOSE_FACE_MODEL_NOMAL_RUNNING = 1;
    public static final int CLOSE_FACE_MODEL_ENTER_LAUNCHER= 0;

    public static final String CHECK_MODEL = "check_model";
    public static final int CHECK_MODEL_ZORE = 0;
    public static final int CHECK_MODEL_ONE= 1;

    private static String FILE_PATH = "";
	
	static {
		try {
            String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            sdcardPath += "/.config/.efrobot";
            File file = new File(sdcardPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            FILE_PATH = sdcardPath + "/game_config";
//            readFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    public static JSONObject readFile() throws JSONException {
        String s = "";
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            String encoding = "UTF-8";
            Long filelength = file.length();
            byte[] filecontent = new byte[filelength.intValue()];
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            s = new String(filecontent, encoding);
        } catch (FileNotFoundException e) {
            //系统强制解决的问题：文件没有找到
            e.printStackTrace();
        } catch (IOException e) {
            //文件读写异常
            e.printStackTrace();
        }
        JSONObject jsonObject = null;
        if (TextUtils.isEmpty(s)) {
            jsonObject = new JSONObject();
            jsonObject.put("mission", 3);
            jsonObject.put("charging_pile", 1);
            jsonObject.put("model", 0);
            jsonObject.put("question", 5);
            jsonObject.put("pass", 3);
            jsonObject.put(SPEECH_MODEL, 0);
            jsonObject.put(CLOSE_FACE_MODEL, ModeFileUtil.CLOSE_FACE_MODEL_ENTER_LAUNCHER);
            jsonObject.put(CHECK_MODEL, ModeFileUtil.CHECK_MODEL_ZORE);
            jsonObject.put(PAY_MODEL, ModeFileUtil.PAY_MODEL_CLOSE);
            jsonObject.put(GAME_SPEECH_MODEL, ModeFileUtil.GAME_SPEECH_MODEL_MODEL_CLOSE);
            writeFile(jsonObject.toString());
        } else {
            jsonObject = new JSONObject(s);
        }
        return jsonObject;
    }

    private static void writeFile(String content) {
        try {
            //使用这个构造函数时，如果存在kuka.txt文件，
            //则先把这个文件给删除掉，然后创建新的kuka.txt
            FileWriter writer = new FileWriter(FILE_PATH);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 存放信息
     *
     * @param key
     * @param content
     */
    public static void putContent(String key, int content) {
        try {
            JSONObject jsonObject = readFile();
            jsonObject.put(key, content);
            writeFile(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取信息
     *
     * @param key
     */
    public static int getContent(String key) {
        try {
            JSONObject jsonObject = readFile();
            return jsonObject.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

package com.yixia.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/6 下午4:41
 * 修改时间：2017/4/6 下午4:41
 * Description:
 */

public class Util {

    /**
     * 获取当前应用程序的包名
     *
     * @param context 上下文对象
     * @return 返回包名
     */
    public static String getAppPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * 判断array(list)是否为空
     */
    public static boolean isEmpty(List<?> array) {
        return array == null || array.size() <= 0;
    }

    public static boolean isEmpty(HashSet<?> set) {
        return set == null || set.size() <= 0;
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.size() <= 0;
    }

    public static <T> boolean isEmpty(T... obj) {
        return obj == null || obj.length <= 0;
    }

    /**
     * map转json
     *
     * @param map Map<String, Object>转成json 其中Object指基本数据类型和String
     * @return String
     */
    public static String map2Json(Map<String, Object> map) {
        if (isEmpty(map))
            return "{}";
        return new JSONObject(map).toString();
    }

    /**
     * 仅使用一个字符进行分割
     */
    public static ArrayList<String> string2List(String text, char splitter) {
        return array2List(string2Array(text, splitter));
    }

    public static <E> ArrayList<E> array2List(E[] arr) {
        List<E> eList = java.util.Arrays.asList(arr);

        ArrayList<E> list = new ArrayList<>(eList.size());
        list.addAll(eList);
        return list;
    }

    public static ArrayList<Integer> string2IntList(String text, char splitter, final boolean sort) {
        ArrayList<Integer> array = string2IntList(text, splitter);
        if (isEmpty(array))
            return null;
        Collections.sort(array, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return sort ? o1 - o2 : o2 - o1;
            }
        });
        return array;
    }

    public static ArrayList<Integer> string2IntList(String text, char splitter) {
        String[] strings = string2Array(text, splitter);
        if (isEmpty(strings))
            return null;
        ArrayList<Integer> array = new ArrayList<>();
        for (String string : strings) {
            try {
                array.add(Integer.valueOf(string));
            } catch (Exception e) {
                Logger.e("string2IntArray", e);
            }
        }
        if (isEmpty(array))
            return null;
        return array;
    }

    /**
     * 仅使用一个字符进行分割
     */
    public static String[] string2Array(String text, char splitter) {
        if (TextUtils.isEmpty(text))
            text = "";

        String reg = "\\" + Character.toString(splitter);
        return text.split(reg);
    }

    public static String array2Strings(char splitter, Object... objs) {
        String value = "";
        if (isEmpty(objs))
            return value;

        int length = objs.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                value += splitter;
            }

            value += objs[i].toString();
        }
        return value;
    }

    public static <T> String array2Strings(List<T> array, char splitter) {
        return array2Strings(array, String.valueOf(splitter));
    }

    public static <T> String array2Strings(List<T> arr, String splitter) {
        if (isEmpty(arr))
            return "";

        String value = "";
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) {
                value += splitter;
            }

            value += arr.get(i).toString();
        }
        return value;
    }

    public static String urlEncoder(String params) {
        String paramsResult = "";
        try {
            paramsResult = URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.e("encoder", e);
        }
        return paramsResult;
    }

    public static String byte2hex(byte[] bytes) {
        if (isEmpty(bytes))
            return "";
        StringBuffer hs = new StringBuffer(bytes.length);
        for (byte b : bytes) {
            String current = Integer.toHexString(b & 0xFF);
            if (current.length() == 1)
                hs = hs.append("0").append(current);
            else {
                hs = hs.append(current);
            }
        }
        return String.valueOf(hs);
    }

    public static void installApp(Context context, String filePath) {
        if (hasRootPermission()) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                PrintWriter printWriter = new PrintWriter(process.getOutputStream());
                printWriter.println("chmod 777 " + filePath);
                printWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
                printWriter.println("pm install -r " + filePath);
                printWriter.flush();
                printWriter.close();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    public static boolean hasRootPermission() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter printWriter = new PrintWriter(process.getOutputStream());
            printWriter.flush();
            printWriter.close();
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }
}
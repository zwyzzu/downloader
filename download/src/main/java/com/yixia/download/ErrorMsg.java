package com.yixia.download;

import android.util.Pair;

import java.util.HashMap;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/18 下午7:56
 * 修改时间:2017/5/18 下午7:56
 * Description:错误码对照表
 */

public class ErrorMsg extends HashMap<Integer, String> {

    public final static int ERROR_CODE_PUT_TASK = 10000;
    public final static int ERROR_CODE_NETWORK = 10010;
    public final static int ERROR_CODE_REQUEST = 10050;
    public final static int ERROR_CODE_REQUEST_VERIFY_FAILED = 10051;
    public final static int ERROR_CODE_REQUEST_RENAME_FAILED = 10052;
    public final static int ERROR_CODE_RESPONSE = 10100;

    {
        this.put(ERROR_CODE_PUT_TASK, "添加任务失败");
        this.put(ERROR_CODE_NETWORK, "网络请求时异常");
        this.put(ERROR_CODE_REQUEST, "处理返回结果时错误");
        this.put(ERROR_CODE_REQUEST_VERIFY_FAILED, "文件校验不成功");
        this.put(ERROR_CODE_REQUEST_RENAME_FAILED, "文件重命名失败");
        this.put(ERROR_CODE_RESPONSE, "返回状态异常");
    }

    private static ErrorMsg instance;

    public static ErrorMsg getInstance() {
        if (instance == null) {
            synchronized (ErrorMsg.class) {
                if (instance == null) {
                    instance = new ErrorMsg();
                }
            }
        }
        return instance;
    }

    private ErrorMsg() {
    }

    @Override
    public String get(Object key) {
        String value = super.get(key);
        return value == null ? "\'" + String.valueOf(key) + "\':未知错误" : value;
    }

    public static String getMsg(Integer errCode) {
        return getInstance().get(errCode);
    }

    public static Pair<Integer, String> getMsgPair(Integer errCode) {
        return Pair.create(errCode, getMsg(errCode));
    }
}

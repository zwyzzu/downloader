package com.yixia.exception;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/4/28 下午5:55
 * 修改时间:2017/4/28 下午5:55
 * Description:未初始化异常
 */

public class UnInitializedException extends RuntimeException {
    private static final long serialVersionUID = 7638968983723396577L;

    public UnInitializedException() {
        super("Has not been initialized");
    }
}

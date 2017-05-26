package com.yixia.download;

import android.content.Context;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/19 下午1:48
 * 修改时间:2017/5/19 下午1:48
 * Description:下载app
 */

public abstract class DownloadApp {

    public static DownloadApp newInstance(Context context) {
        return new DownloadAppImpl(context);
    }

    /**
     * 设置监听器
     * @param listener 下载监听
     * @return 当前实体
     */
    public abstract DownloadApp setListener(Downloader.DownloadListener listener);
    /**
     * add download task
     *
     * @param context    Context
     * @param url        task url
     * @param title      task title
     * @param iconUrl    task icon url
     * @param verifyCode Server-side generated file md5 verify code
     * @return task id(url md5)
     */
    public abstract boolean download(String url, String title, String iconUrl, String verifyCode, long fileSize);

    /**
     * restart task
     *
     * @param url task url
     */
    public abstract void reStart(String url);

    /**
     * stop task
     *
     * @param url task url
     * @return true stop success
     */
    public abstract boolean stop(String url);

    /**
     * delete task
     *
     * @param url task id
     * @return true delete success
     */
    public abstract boolean delete(String url);
}

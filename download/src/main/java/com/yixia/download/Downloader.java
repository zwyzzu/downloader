package com.yixia.download;

import android.content.Context;

import com.yixia.download.entity.DownloadEntity;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/16 下午4:17
 * 修改时间:2017/5/16 下午4:17
 * Description:下载管理类
 */

public abstract class Downloader {

    public static void initialized(Context context, int maxThreadSize, float blockSizeMb, boolean autoDownloadAll) {
        getInstance().init(context, maxThreadSize, blockSizeMb, autoDownloadAll);
    }

    private static Downloader instance;

    public static Downloader getInstance() {
        if (instance == null) {
            synchronized (Downloader.class) {
                if (instance == null) {
                    instance = new DownloaderImpl();
                }
            }
        }
        return instance;
    }

    /***********************************************************************************************
     * 初始化下载器
     *
     * @param context 上下文
     * @return 当前实体
     */
    public abstract Downloader init(Context context, int maxThreadSize, float blockSizeMb, boolean autoDownloadAll);

    /**
     * register listener for download info
     *
     * @param listener DownloadListener
     * @return this object
     */
    public abstract Downloader register(DownloadListener listener);

    /**
     * remove listener for download info
     *
     * @param listener DownloadListener
     */
    public abstract void unRegister(DownloadListener listener);

    /**
     * add download task
     *
     * @param url        task url
     * @param title      task title
     * @param iconUrl    task icon url
     * @param verifyCode Server-side generated file md5 verify code
     * @param dldType    download type
     * @return task id(url md5)
     */
    public abstract String addTask(String url, String title, String iconUrl, String verifyCode, long fileSize, DownloadEntity.DldType dldType);

    /**
     * add download task
     *
     * @param url        task url
     * @param title      task title
     * @param iconUrl    task icon url
     * @param verifyCode Server-side generated file md5 verify code
     * @param dldType    download type
     * @param listener   DownloadListener
     * @return task id(url md5)
     */
    public abstract String addTask(String url, String title, String iconUrl, String verifyCode, long fileSize, DownloadEntity.DldType dldType, DownloadListener listener);

    /**
     * restart task
     *
     * @param url task url
     */
    public abstract void reStartTaskByUrl(String url);

    /**
     * restart task
     *
     * @param id task id
     */
    public abstract void reStartTaskById(String id);

    /**
     * stop task
     *
     * @param url task url
     * @return true stop success
     */
    public abstract boolean stopTaskByUrl(String url);

    /**
     * stop task
     *
     * @param id task id
     * @return true stop success
     */
    public abstract boolean stopTaskById(String id);

    /**
     * delete task
     *
     * @param url task id
     * @return true delete success
     */
    public abstract boolean delTaskByUrl(String url);

    /**
     * delete task
     *
     * @param id task id
     * @return true delete success
     */
    public abstract boolean delTaskById(String id);

    public interface DownloadListener {

        /**
         * Notification that the status of task.
         *
         * @param taskId  The task's id whose start download
         * @param taskUrl The task's url whose start download
         * @param status  true add task success otherwise false
         */
        void onAddTask(String taskId, String taskUrl, boolean status);

        /**
         * Notification that the task start download
         * Clients can use the taskId or taskUrl parameter to distinguish task start download
         *
         * @param taskId  The task's id whose start download
         * @param taskUrl The task's url whose start download
         */
        void onStart(String taskId, String taskUrl);

        /**
         * Notification that the progress level has changed.
         * Clients can use the taskId or taskUrl parameter to distinguish task changes
         *
         * @param taskId   The task's id whose progress has changed
         * @param taskUrl  The task's url whose progress has changed
         * @param progress The current progress level. This will be in the range 0..100
         */
        void onProgressChanged(String taskId, String taskUrl, float progress);

        /**
         * Notification that the task stop download
         * Clients can use the taskId or taskUrl parameter to distinguish task stop download
         *
         * @param taskId  The task's id whose stop download
         * @param taskUrl The task's url whose stop download
         */
        void onStop(String taskId, String taskUrl);

        /**
         * Notification that the task has download complete.
         * Clients can use the taskId or taskUrl parameter to distinguish task download complete
         *
         * @param taskId  The task's id whose has download complete
         * @param taskUrl The task's url whose has download complete
         */
        void onSuccess(String taskId, String taskUrl, String localPath);

        /**
         * Notification that the task download failed
         * Clients can use the taskId or taskUrl parameter to distinguish task stop download
         *
         * @param taskId  The task's id whose download failed
         * @param taskUrl The task's url whose download failed
         */
        void onFailed(String taskId, String taskUrl, int errCode, String errMsg);
    }
}

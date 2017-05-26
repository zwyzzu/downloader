package com.yixia.download.db;

import android.content.Context;

import com.yixia.download.entity.DownloadEntity;

import java.util.List;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/15 下午2:50
 * 修改时间:2017/5/15 下午2:50
 * Description:数据库管理
 */

public abstract class DBManager {
    public static DBManager initialized(Context context) {
        return getInstance().init(context);
    }

    private static DBManager instance;

    public static DBManager getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManagerImpl();
                }
            }
        }
        return instance;
    }

    /********************************************************************************************
     * 初始化，调用数据库之前必须初始化
     *
     * @param context 上下文
     * @return 返回当前实例对象
     */
    public abstract DBManager init(Context context);

    /**
     * 添加下载任务
     *
     * @param entity 下载任务实例
     * @return true add success; false add failed
     */
    public abstract boolean addDownload(DownloadEntity entity);

    /**
     * 删除下载任务根据任务ID
     *
     * @param id will delete task's id
     * @return true find and delete success
     */
    public abstract boolean delDownloadById(String id);

    /**
     * 删除下载任务根据任务url
     *
     * @param url will delete task's url
     * @return true find and delete success
     */
    public abstract boolean delDownloadByUrl(String url);

    /**
     * 更新下载任务进度
     *
     * @param id       will update task's id
     * @param progress task download progress
     * @return true find and update success
     */
    public abstract boolean updateDownloadProgress(String id, float progress);


    /**
     * 更新下载任务进度
     *
     * @param id     will update task's id
     * @param entity task entity
     * @return true find and update success
     */
    public abstract boolean updateDownload(String id, DownloadEntity entity);

    /**
     * 获取下载任务根据任务ID
     *
     * @param id want task's id
     * @return null find failed
     */
    public abstract DownloadEntity getDownloadById(String id);

    /**
     * 获取下载任务根据任务ID
     *
     * @param url want task's url
     * @return null find failed
     */
    public abstract DownloadEntity getDownloadByUrl(String url);

    /**
     * 获取所有的下载任务列表
     *
     * @return all download task
     */
    public abstract List<DownloadEntity> getAllDownload();
}

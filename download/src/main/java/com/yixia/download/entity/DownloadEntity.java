package com.yixia.download.entity;

import com.yixia.util.DirMgmt;

import java.io.Serializable;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/15 下午1:57
 * 修改时间:2017/5/15 下午1:57
 * Description:下载基类
 */

public interface DownloadEntity extends Serializable {
    /**
     * 下载任务ID
     *
     * @return id not empty;This download task is unUsable if id is empty.
     */
    String getId();

    /**
     * 下载任务url
     *
     * @return url not empty；This download task is unUsable if url is empty.
     */
    String getUrl();

    /**
     * 下载任务title
     *
     * @return task's title
     */
    String getTitle();

    /**
     * 下载任务icon的url
     *
     * @return task's icon url
     */
    String getIcon();

    /**
     * 下载任务类型
     *
     * @return task's type
     */
    DldType getType();

    /**
     * 下载文件大小,文件字节数
     *
     * @return file size
     */
    long getSize();

    /**
     * 下载进度，保留小数点2为，下载完成时为100.00；如果想得到百分比需要自行除100(@progress / 100)
     *
     * @return progress
     */
    float getProgress();

    /**
     * 下载校验码，下载完成后对文件进行MD5计算并进行对比
     *
     * @return server file md5
     */
    String getVerifyCode();

    /**
     * 下载任务存储路径
     *
     * @return local file path
     */
    String getPath();

    /**
     * 下载任务文件名
     *
     * @return file name
     */
    String getName();

    /**
     * 任务创建时间
     *
     * @return the time of create
     */
    long createTime();

    /**
     * 任务最后下载时间
     *
     * @return the time of last download
     */
    long lastDownloadTime();

    /**
     * 任务是否可用
     *
     * @return false id or url is empty, otherwise true
     */
    boolean usable();

    public enum DldType {
        UNKNOWN("unknown", 0, DirMgmt.WorkDir.OTHER, ".other", "未知类型"),
        APP("app", 1, DirMgmt.WorkDir.APK, ".apk", "应用"),
        MEDIA("media", 2, DirMgmt.WorkDir.MEDIA, ".mp4", "媒体"),
        IMAGE("image", 3, DirMgmt.WorkDir.IMAGE, ".img", "图片");
        public String name;
        public int code;
        public DirMgmt.WorkDir dir;
        public String extension;
        public String desc;

        DldType(String name, int code, DirMgmt.WorkDir dir, String extension, String desc) {
            this.name = name;
            this.code = code;
            this.dir = dir;
            this.extension = extension;
            this.desc = desc;
        }

        public static DldType getType(int code) {
            for (DldType type : DldType.values()) {
                if (type.code == code)
                    return type;
            }
            return UNKNOWN;
        }
    }
}

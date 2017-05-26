package com.yixia.download.entity;

import android.text.TextUtils;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/15 下午2:02
 * 修改时间:2017/5/15 下午2:02
 * Description:
 */

public class DownloadEntityImpl implements DownloadEntity {
    private static final long serialVersionUID = 5563155319419044453L;

    private String id;
    private String url;
    private String title;
    private String icon;
    private DldType dldType;
    private long fileSize;
    private float progress;
    private String md5;
    private String path;
    private String name;
    private long createTime;
    private long lastModified;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getIcon() {
        return this.icon;
    }

    public void setType(DldType dldType) {
        this.dldType = dldType;
    }

    public void setType(int dldTypeCode) {
        this.setType(DldType.getType(dldTypeCode));
    }

    @Override
    public DldType getType() {
        return this.dldType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public long getSize() {
        return this.fileSize;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Override
    public float getProgress() {
        return this.progress;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String getVerifyCode() {
        return this.md5;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long createTime() {
        return this.createTime;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public long lastDownloadTime() {
        return this.lastModified;
    }

    @Override
    public boolean usable() {
        return !TextUtils.isEmpty(id) && !TextUtils.isEmpty(url);
    }
}

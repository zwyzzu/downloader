package com.yixia.download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yixia.db.DatabaseHelper;
import com.yixia.download.entity.DownloadEntity;
import com.yixia.download.entity.DownloadEntityImpl;
import com.yixia.exception.UnInitializedException;
import com.yixia.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/15 下午2:51
 * 修改时间:2017/5/15 下午2:51
 * Description:数据库管理类实现类
 */

class DBManagerImpl extends DBManager {

    private final int DATABASE_VERSION_1 = 1;
    private final String TABLE_NAME_DOWNLOAD = "download_data";
    private final String SQL_CREATE_TABLE_DOWNLOAD = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DOWNLOAD + " (_id TEXT NOT NULL PRIMARY KEY, title TEXT, url TEXT, icon TEXT, type INTEGER, progress FLOAT, size LONG, md5 TEXT, path TEXT, name TEXT, createTime LONG, lastModified LONG)";
    private final String SQL_WHERECLAUSE_DOWNLOAD_ID = " _id = ? ";
    private final String SQL_WHERECLAUSE_DOWNLOAD_URL = " url = ? ";
    private final String SQL_QUERY_DOWNLOAD = "SELECT _id, title, url, icon, type, progress, size, md5, path, name, createTime, lastModified FROM " + TABLE_NAME_DOWNLOAD;
    private final String SQL_QUERY_DOWNLOAD_BY_ID = SQL_QUERY_DOWNLOAD + " where " + SQL_WHERECLAUSE_DOWNLOAD_ID;
    private final String SQL_QUERY_DOWNLOAD_BY_URL = SQL_QUERY_DOWNLOAD + " where " + SQL_WHERECLAUSE_DOWNLOAD_URL;

    private boolean initialized = false;
    private DatabaseHelper helper;

    @Override
    public DBManager init(Context context) {
        if (this.initialized)
            return this;
        this.initialized = true;
        this.initDatabase(context);
        return this;
    }

    private void initDatabase(Context context) {
        this.helper = DatabaseHelper.create(context.getApplicationContext(), DatabaseHelper.DatabaseConfig.newInstance("com_yixia_download", DATABASE_VERSION_1, new DatabaseHelper.UpgradeListener() {
            @Override
            public void onCreate(SQLiteDatabase database) {
                if (!database.isOpen())
                    helper.onOpen(database);
                database.execSQL(SQL_CREATE_TABLE_DOWNLOAD);//创建下载任务表
                Logger.d(SQL_QUERY_DOWNLOAD_BY_ID);
                Logger.d(SQL_QUERY_DOWNLOAD_BY_URL);
            }

            @Override
            public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
                if (!database.isOpen())
                    helper.onOpen(database);
                switch (oldVersion) {
                    case DATABASE_VERSION_1://version = 2 时添加的新表，下同，(version is case +1)
                    default:
                        break;
                }
            }
        }));
    }

    /*
     * _id TEXT NOT NULL PRIMARY KEY, title TEXT, url TEXT, icon TEXT, type INTEGER, progress FLOAT, md5 TEXT, path TEXT, name TEXT, lastModified LONG
     */
    @Override
    public boolean addDownload(DownloadEntity entity) {
        this.throwUnInitializedException();
        if (entity == null)
            return false;
        try {
            SQLiteDatabase database = helper.open();
            ContentValues values = new ContentValues();
            values.put("_id", entity.getId());
            values.put("title", entity.getTitle());
            values.put("url", entity.getUrl());
            values.put("icon", entity.getIcon());
            values.put("type", entity.getType().code);
            values.put("progress", entity.getProgress());
            values.put("size", entity.getSize());
            values.put("md5", entity.getVerifyCode());
            values.put("path", entity.getPath());
            values.put("name", entity.getName());
            values.put("createTime", System.currentTimeMillis());
            values.put("lastModified", System.currentTimeMillis());
            long raw = database.insertWithOnConflict(TABLE_NAME_DOWNLOAD, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return raw > -1;
        } catch (DatabaseHelper.NullDataBaseException e) {
            return false;
        }
    }

    @Override
    public boolean delDownloadById(String id) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(id))
            return false;
        try {
            SQLiteDatabase database = helper.open();
            int count = database.delete(TABLE_NAME_DOWNLOAD, SQL_WHERECLAUSE_DOWNLOAD_ID, new String[]{id});
            return count > 0;
        } catch (DatabaseHelper.NullDataBaseException e) {
            return false;
        }
    }

    @Override
    public boolean delDownloadByUrl(String url) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(url))
            return false;
        try {
            SQLiteDatabase database = helper.open();
            int count = database.delete(TABLE_NAME_DOWNLOAD, SQL_WHERECLAUSE_DOWNLOAD_URL, new String[]{url});
            return count > 0;
        } catch (DatabaseHelper.NullDataBaseException e) {
            return false;
        }
    }

    @Override
    public boolean updateDownloadProgress(String id, float progress) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(id))
            return false;
        try {
            SQLiteDatabase database = helper.open();
            ContentValues values = new ContentValues();
            values.put("progress", progress);
            values.put("lastModified", System.currentTimeMillis());
            int count = database.update(TABLE_NAME_DOWNLOAD, values, SQL_WHERECLAUSE_DOWNLOAD_ID, new String[]{id});
            return count > 0;
        } catch (DatabaseHelper.NullDataBaseException e) {
            return false;
        }
    }

    @Override
    public boolean updateDownload(String id, DownloadEntity entity) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(id))
            return false;
        DownloadEntity oldEntity = this.getDownloadById(id);
        if (oldEntity == null) {
            return this.addDownload(entity);
        }
        try {
            SQLiteDatabase database = helper.open();
            ContentValues values = new ContentValues();
            values.put("_id", id);
            values.put("title", entity.getTitle());
            values.put("url", entity.getUrl());
            values.put("icon", entity.getIcon());
            values.put("type", entity.getType().code);
            values.put("progress", entity.getProgress());
            values.put("size", entity.getSize());
            values.put("md5", entity.getVerifyCode());
            values.put("path", entity.getPath());
            values.put("name", entity.getName());
            values.put("lastModified", System.currentTimeMillis());
            long raw = database.update(TABLE_NAME_DOWNLOAD, values, SQL_WHERECLAUSE_DOWNLOAD_ID, new String[]{id});
            return raw > -1;
        } catch (DatabaseHelper.NullDataBaseException e) {
            Logger.e("updatePlayLog", e);
            return false;
        }
    }

    @Override
    public DownloadEntity getDownloadById(String id) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(id))
            return null;
        try {
            SQLiteDatabase database = helper.open();
            Cursor cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_ID, new String[]{id});
            if (cursor == null)
                return null;
            DownloadEntityImpl entity = null;
            while (cursor.moveToNext()) {
                entity = cursor2DownloadEntity(cursor);
                if (entity != null && entity.usable()) {
                    break;
                }
            }
            cursor.close();
            return entity;
        } catch (DatabaseHelper.NullDataBaseException e) {
            Logger.e("getReportData", e);
        }
        return null;
    }

    @Override
    public DownloadEntity getDownloadByUrl(String url) {
        this.throwUnInitializedException();
        if (TextUtils.isEmpty(url))
            return null;
        try {
            SQLiteDatabase database = helper.open();
            Cursor cursor = database.rawQuery(SQL_QUERY_DOWNLOAD_BY_URL, new String[]{url});
            if (cursor == null)
                return null;
            DownloadEntityImpl entity = null;
            while (cursor.moveToNext()) {
                entity = cursor2DownloadEntity(cursor);
                if (entity != null && entity.usable()) {
                    break;
                }
            }
            cursor.close();
            return entity;
        } catch (DatabaseHelper.NullDataBaseException e) {
            Logger.e("getReportData", e);
        }
        return null;
    }

    @Override
    public List<DownloadEntity> getAllDownload() {
        this.throwUnInitializedException();
        try {
            SQLiteDatabase database = helper.open();
            Cursor cursor = database.rawQuery(SQL_QUERY_DOWNLOAD, null);
            if (cursor == null)
                return null;
            List<DownloadEntity> array = new ArrayList<>();
            while (cursor.moveToNext()) {
                DownloadEntityImpl entity = cursor2DownloadEntity(cursor);
                if (entity != null && entity.usable()) {
                    array.add(entity);
                }
            }
            cursor.close();
            return array;
        } catch (DatabaseHelper.NullDataBaseException e) {
            Logger.e("getReportData", e);
        }
        return null;
    }

    //SELECT _id, title, url, icon, type, progress, size, md5, path, name, createTime, lastModified
    private DownloadEntityImpl cursor2DownloadEntity(Cursor c) {
        String _id = c.getString(0);
        String url = c.getString(2);
        if (TextUtils.isEmpty(_id) || TextUtils.isEmpty(url)) {
            return null;
        }
        DownloadEntityImpl entity = new DownloadEntityImpl();
        entity.setId(_id);
        entity.setUrl(url);
        entity.setTitle(c.getString(1));
        entity.setIcon(c.getString(3));
        entity.setType(c.getInt(4));
        entity.setProgress(c.getFloat(5));
        entity.setFileSize(c.getLong(6));
        entity.setMd5(c.getString(7));
        entity.setPath(c.getString(8));
        entity.setName(c.getString(9));
        entity.setCreateTime(c.getLong(10));
        entity.setLastModified(c.getLong(11));
        return entity;
    }

    private void throwUnInitializedException() {
        if (!initialized) {
            throw new UnInitializedException();
        }
    }
}

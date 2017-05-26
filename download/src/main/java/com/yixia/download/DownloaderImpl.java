package com.yixia.download;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import com.yixia.cipher.MD5;
import com.yixia.download.db.DBManager;
import com.yixia.download.entity.DownloadEntity;
import com.yixia.download.entity.DownloadEntityImpl;
import com.yixia.http.XHttp;
import com.yixia.http.XHttpClient;
import com.yixia.http.XHttpException;
import com.yixia.util.Device;
import com.yixia.util.DirMgmt;
import com.yixia.util.FileUtil;
import com.yixia.util.Logger;
import com.yixia.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/16 下午4:18
 * 修改时间:2017/5/16 下午4:18
 * Description:下载器实现类
 */

class DownloaderImpl extends Downloader {

    private final String TAG = "Downloader";
    private DBManager dbManager;
    private ArrayList<DownloadListener> listeners = new ArrayList<>();
    private HashMap<String, DownloadInfo> tasks = new HashMap<>();
    private XHttpClient httpClient;
    private float blockSizeMb = 2.0f;
    private boolean autoDownloadAll = false;
    private boolean initialized = false;

    @Override
    public Downloader init(Context context, int maxThreadSize, float blockSizeMb, boolean autoDownloadAll) {
        if (this.initialized)
            return this;
        this.initialized = true;
        context = context.getApplicationContext();
        this.blockSizeMb = blockSizeMb <= 0 ? this.blockSizeMb : blockSizeMb;
        this.autoDownloadAll = autoDownloadAll;
        this.initDatabase(context);
        this.initDir(context);
        this.initHttp(maxThreadSize);
        this.updateTasks(context);
        return this;
    }

    private void initDatabase(Context context) {
        DBManager.initialized(context);
        this.dbManager = DBManager.getInstance();
        this.dbManager.init(context);
    }

    private void initDir(Context context) {
        DirMgmt.getInstance().init(context);
    }

    private void initHttp(int maxThreadSize) {
        httpClient = XHttp.newHttpClient(maxThreadSize);
    }

    private void updateTasks(Context context) {
        List<DownloadEntity> dldEntities = this.dbManager.getAllDownload();
        if (Util.isEmpty(dldEntities)) {
            return;
        }

        if (!this.autoDownloadAll || Device.NetWork.netWorkType(context) != Device.NetType.WIFI)
            return;

        for (DownloadEntity entity : dldEntities) {
            if (!entity.usable())
                continue;
            this.addTask(entity);
        }
    }

    @Override
    public Downloader register(DownloadListener listener) {
        this.listeners.add(listener);
        return this;
    }

    @Override
    public void unRegister(DownloadListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public String addTask(String url, String title, String iconUrl, String verifyCode, long fileSize, DownloadEntity.DldType dldType) {
        return this.addTask(url, title, iconUrl, verifyCode, fileSize, dldType, null);
    }

    @Override
    public String addTask(String url, String title, String iconUrl, String verifyCode, long fileSize, DownloadEntity.DldType dldType, DownloadListener listener) {
        String taskId = MD5.md5Encode(url);
        DownloadInfo info;
        DownloadEntity entity;
        if (this.tasks.containsKey(taskId) && (info = this.tasks.get(taskId)) != null && (entity = info.entity) != null) {
            ((DownloadEntityImpl) entity).setTitle(title);
            this.tasks.get(taskId).listener = listener;
            this.reStartTaskById(taskId);
            return taskId;
        }
        entity = this.createDldEntity(taskId, url, title, iconUrl, verifyCode, fileSize, dldType);
        info = this.create(entity, listener);
        this.tasks.put(taskId, info);
        this.dbManager.updateDownload(taskId, entity);
        this.addTask(entity);
        return taskId;
    }

    private DownloadEntity createDldEntity(String id, String url, String title, String iconUrl, String verifyCode, long fileSize, DownloadEntity.DldType dldType) {
        DownloadEntityImpl entity = new DownloadEntityImpl();
        entity.setId(id);
        entity.setUrl(url);
        entity.setTitle(title);
        entity.setIcon(iconUrl);
        entity.setMd5(verifyCode);
        entity.setFileSize(fileSize);
        entity.setType(dldType);
        return entity;
    }

    @Override
    public void reStartTaskByUrl(String url) {
        this.reStartTaskById(MD5.md5Encode(url));
    }

    @Override
    public void reStartTaskById(String id) {
        if (!this.tasks.containsKey(id))
            return;
        DownloadInfo info = this.tasks.get(id);
        DownloadEntity entity = info.entity;
        if ((info.future == null || info.future.isCancelled() || info.future.isDone())) {
            this.addTask(entity);
        }
    }

    @Override
    public boolean stopTaskByUrl(String url) {
        return this.stopTaskById(MD5.md5Encode(url));
    }

    @Override
    public boolean stopTaskById(String id) {
        if (this.tasks.containsKey(id)) {
            DownloadInfo info = this.tasks.get(id);
            if (this.taskCancel(info.future)) {
                info.future = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delTaskByUrl(String url) {
        return this.delTaskById(MD5.md5Encode(url));
    }

    @Override
    public boolean delTaskById(String id) {
        if (this.tasks.containsKey(id)) {
            DownloadInfo info = this.tasks.get(id);
            this.taskCancel(info.future);
            this.tasks.remove(id);
        }
        return this.dbManager.delDownloadById(id);
    }

    private void addTask(DownloadEntity entity) {
        new AddTaskAsyncTask(entity).execute();
    }

    private boolean taskCancel(Future<?> future) {
        return future != null && !future.isDone() && !future.isCancelled() && future.cancel(true);
    }

    private DownloadInfo create(DownloadEntity entity, Future<?> future) {
        DownloadInfo info = new DownloadInfo();
        info.entity = entity;
        info.future = future;
        return info;
    }

    private DownloadInfo create(DownloadEntity entity, DownloadListener listener) {
        DownloadInfo info = new DownloadInfo();
        info.entity = entity;
        info.listener = listener;
        return info;
    }

    private class DownloadInfo {
        private DownloadEntity entity;
        private DownloadListener listener;
        private Future<?> future;
    }

    private class AddTaskAsyncTask extends AsyncTask<DownloadEntity, Integer, AddTaskResult> {

        private DownloadEntity entity;
        AddTaskAsyncTask(DownloadEntity entity) {
            this.entity = entity;
        }

        @Override
        protected AddTaskResult doInBackground(DownloadEntity... params) {
            if (entity == null) {
                return null;
            }
            AddTaskResult result = new AddTaskResult(entity.getId());
            DownloadHandler.DownloadCache cache = createCache(entity);
            if (cache.exists(cache.filePath()) && this.verifyFinalFile(cache)) {
                result.status = NOTIFY_SUCCESS;
                result.filePath = cache.filePath();
                return result;
            }

            if (cache.exists(cache.tempPath()) && tasks.containsKey(entity.getId())) {
                DownloadInfo info = tasks.get(entity.getId());
                Future<?> future;
                if (info != null && (future = info.future) != null && !future.isCancelled()) {
                    if (future.isDone()) {
                        if (this.verifyFinalFile(cache)) {
                            result.status = NOTIFY_SUCCESS;
                            result.filePath = cache.filePath();
                            return result;
                        }
                    } else {
                        Logger.d(TAG, "progress: load material: " + cache.tempPath() + ". msg: download not completed.");
                        result.status = NOTIFY_PROGRESS;
                        return result;
                    }
                }
            }

            result.cache = cache;
            return result;
        }

        /**
         * 校验最终文件
         *
         * @return true 校验成功
         */
        private boolean verifyFinalFile(DownloadHandler.DownloadCache cache) {
            File file = cache.file();
            boolean result = cache.exists(file);
            if (result && cache.verify(file)) {
                Logger.d(TAG, "success: load material: " + file.getAbsolutePath() + ". msg: cache found.");
                return true;
            }
            FileUtil.deleteFile(file);
            return false;
        }

        private DownloadHandler.DownloadCache createCache(DownloadEntity entity) {
            String localDir = TextUtils.isEmpty(entity.getPath()) ? DirMgmt.getInstance().getPath(entity.getType().dir) : entity.getPath();
            String tmpName = entity.getId();
            String name = tmpName + entity.getType().extension;
            return new DownloadHandler.DownloadCache(localDir, name, tmpName, entity.getVerifyCode());
        }

        @Override
        protected void onPostExecute(AddTaskResult result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.cache != null) {
                    try {
                        Future<?> future = httpClient.get(entity.getUrl(), result.cache.getDir(), result.cache.tempName, true, true, blockSizeMb, 3, new XDownloadHandler(entity.getId(), result.cache, entity.getSize()));
                        DownloadInfo info;
                        if (!tasks.containsKey(entity.getId()) || (info = tasks.get(entity.getId())) == null) {
                            info = create(entity, future);
                            tasks.put(entity.getId(), info);
                        }
                        info.future = future;
                        result.status = NOTIFY_ADD_TASK_SUCCESS;
                    } catch (XHttpException e) {
                        result.status = NOTIFY_FAILED;
                        result.err = ErrorMsg.getMsgPair(ErrorMsg.ERROR_CODE_PUT_TASK);
                    }
                }
                DownloaderImpl.this.notify(result.taskId, result.status, result.filePath, result.err);
            }
        }
    }

    private class AddTaskResult {
        private String taskId;
        private String filePath;
        private int status = -1;
        private Pair<Integer, String> err;
        private DownloadHandler.DownloadCache cache;

        private AddTaskResult(String taskId) {
            this.taskId = taskId;
        }
    }

    private class XDownloadHandler extends DownloadHandler {

        private XDownloadHandler(String taskId, DownloadCache cache, long fileSize) {
            super(taskId, cache, fileSize);
        }

        @Override
        public void onStart(String url) {
            super.onStart(url);
            DownloaderImpl.this.notify(taskId, NOTIFY_START, null, null);
        }

        @Override
        void onStop(String url) {
            super.onStop(url);
            DownloaderImpl.this.notify(taskId, NOTIFY_STOP, null, null);
        }

        @Override
        public void onSuccess(SLMResp resp) {
            this.updateProgress(100);
            DownloaderImpl.this.notify(taskId, NOTIFY_SUCCESS, resp.localPath, null);
            Logger.d(TAG, "progress: load material: " + cache.tempPath() + ". msg: download not completed.");
        }

        @Override
        public void onProgress(PLMResp resp) {
            this.updateProgress(resp.progress);
            DownloaderImpl.this.notify(taskId, NOTIFY_PROGRESS, null, null);
        }

        private void updateProgress(float progress) {
            DownloadInfo info;
            DownloadEntity entity;
            if ((info = tasks.get(taskId)) != null && (entity = info.entity) != null) {
                ((DownloadEntityImpl) entity).setProgress(progress);
            }
        }

        @Override
        public void onFailed(ELMResp resp) {
            DownloaderImpl.this.notify(taskId, NOTIFY_FAILED, null, Pair.create(resp.errCode, resp.errMsg));
        }
    }

    private final int NOTIFY_ADD_TASK_SUCCESS = 0;
    private final int NOTIFY_ADD_TASK_FAILED = 1;
    private final int NOTIFY_START = 2;
    private final int NOTIFY_STOP = 3;
    private final int NOTIFY_PROGRESS = 4;
    private final int NOTIFY_SUCCESS = 5;
    private final int NOTIFY_FAILED = 6;

    private void notify(String id, int type, String localPath, Pair<Integer, String> errPair) {
        if (!this.tasks.containsKey(id))
            return;
        DownloadInfo info = this.tasks.get(id);
        if (info == null)
            return;
        DownloadEntity entity = info.entity;
        if (entity == null)
            return;

        String url = entity.getUrl();
        DownloadListener[] array = listeners.toArray(new DownloadListener[listeners.size() + 1]);
        array[array.length - 1] = info.listener;

        switch (type) {
            case NOTIFY_ADD_TASK_FAILED:
                this.notify(id, url, array, new Command<Object>() {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onAddTask(taskId, url, false);
                    }
                });
                break;
            case NOTIFY_ADD_TASK_SUCCESS:
                this.notify(id, url, array, new Command<Object>() {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onAddTask(taskId, url, true);
                    }
                });
                break;
            case NOTIFY_START:
                this.notify(id, url, array, new Command<Object>() {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onStart(taskId, url);
                    }
                });
                break;
            case NOTIFY_STOP:
                this.notify(id, url, array, new Command<Object>() {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onStop(taskId, url);
                    }
                });
                break;
            case NOTIFY_PROGRESS:
                this.notify(id, url, array, new Command<Float>(entity.getProgress()) {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onProgressChanged(taskId, url, this.obj);
                    }
                });
                break;
            case NOTIFY_SUCCESS:
                this.notify(id, url, array, new Command<String>(localPath) {
                    @Override
                    public void execute(String taskId, String url, DownloadListener listener) {
                        listener.onSuccess(taskId, url, this.obj);
                    }
                });
                break;
            case NOTIFY_FAILED:
                if (errPair == null)
                    break;
                this.notify(id, url, array, new Command<Pair<Integer, String>>(errPair) {
                    @Override
                    void execute(String taskId, String url, DownloadListener listener) {
                        listener.onFailed(taskId, url, this.obj.first, this.obj.second);
                    }
                });
                break;
        }
    }

    private void notify(String taskId, String url, DownloadListener[] array, Command command) {
        if (command == null || Util.isEmpty(array))
            return;
        for (DownloadListener listener : array) {
            if (listener == null)
                continue;
            command.execute(taskId, url, listener);
        }
    }

    private abstract class Command<T> {
        T obj;

        Command() {
        }

        Command(T obj) {
            this();
            this.obj = obj;
        }

        abstract void execute(String taskId, String url, DownloadListener listener);
    }
}

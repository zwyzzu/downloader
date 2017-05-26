package com.yixia.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.yixia.cipher.MD5;
import com.yixia.download.db.DBManager;
import com.yixia.http.XDownloadHandler;
import com.yixia.http.XHttpRequest;
import com.yixia.http.XHttpResponse;
import com.yixia.util.FileUtil;
import com.yixia.util.Logger;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/11 下午4:14
 * 修改时间：2017/4/11 下午4:14
 * Description:下载handler处理
 */

public abstract class DownloadHandler extends XDownloadHandler implements Handler.Callback {
    private final int MSG_START = 0;
    private final int MSG_STOP = 1;
    private final int MSG_PROGRESS = 2;
    private final int MSG_SUCCESS = 3;
    private final int MSG_FAILED = 4;
    private final int MSG_VERIFY = 5;

    private final int DEFAULT_FILE_SIZE = 0;

    private final float PROGRESS_100 = 100.0f;
    private final float PROGRESS_90 = 90.0f;

    private Handler handler;
    public String taskId;
    private long fileSize = DEFAULT_FILE_SIZE;
    public DownloadCache cache;
    private float progress;
    private float PROGRESS_BASE;

    public DownloadHandler(DownloadCache cache) {
        this("", cache, 0);
    }

    public DownloadHandler(String taskId, DownloadCache cache, long fileSize) {
        if (Looper.myLooper() != null) {
            this.handler = new Handler(this);
        }
        this.taskId = taskId;
        this.cache = cache;
        this.fileSize = fileSize;
        this.PROGRESS_BASE = cache.hasVerifyCode() ? PROGRESS_90 : PROGRESS_100;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START:
                this.onStart((String) msg.obj);
                break;
            case MSG_STOP:
                this.onStop((String) msg.obj);
                break;
            case MSG_PROGRESS:
                this.onProgress((PLMResp) msg.obj);
                break;
            case MSG_SUCCESS:
                this.onSuccess((SLMResp) msg.obj);
                break;
            case MSG_FAILED:
                this.onFailed((ELMResp) msg.obj);
                break;
            case MSG_VERIFY:
                this.clearVerifyMsg();
                if (progress < PROGRESS_100) {
                    this.onProgress(PLMResp.create((String) msg.obj, progress));
                    progress += 0.50f;
                    try {
                        DecimalFormat decimalFormat = new DecimalFormat("#.00");
                        progress = Float.valueOf(decimalFormat.format(progress));
                    } catch (Exception e) {
                        Logger.e("onProgressChanged", e);
                    }
                    this.sendVerifyMsg((String) msg.obj);
                }
                break;
        }
        return true;
    }

    void onStart(String url) {
        Logger.d(TAG, url);
    }

    void onStop(String url) {
        Logger.d(TAG, url);
    }

    public abstract void onSuccess(SLMResp resp);

    public void onProgress(PLMResp resp) {
        Logger.d(TAG, "progress = " + resp.progress + "\nurl = " + resp.url);
    }

    public abstract void onFailed(ELMResp resp);

    @Override
    public void onStart(XHttpRequest req) {
        super.onStart(req);
        if (this.handler == null)
            return;
        this.handler.sendMessage(this.handler.obtainMessage(MSG_START, req.getUrlString()));
    }

    @Override
    public void onStop(XHttpRequest req) {
        super.onStop(req);
        if (this.handler == null)
            return;
        this.handler.sendMessage(this.handler.obtainMessage(MSG_STOP, req.getUrlString()));
    }

    @Override
    public void onProgressChanged(XHttpRequest req, long hasDownloadSize) {
        if (this.handler == null || fileSize <= DEFAULT_FILE_SIZE)
            return;
        try {
            progress = (hasDownloadSize * PROGRESS_BASE) / fileSize;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            progress = Float.valueOf(decimalFormat.format(progress));
            this.sendProgressMsg(req.getUrlString());
        } catch (Exception e) {
            Logger.e("onProgressChanged", e);
        }
    }

    @Override
    public void onSuccess(XHttpRequest req, XHttpResponse resp) {
        try {
            this.sendVerifyMsg(req.getUrlString());
            File tmpFile = TextUtils.isEmpty(resp.getLocalFile()) ? cache.tempFile() : new File(resp.getLocalFile());
            if (!cache.verify(tmpFile)) {
                this.clearVerifyMsg();
                FileUtil.deleteFile(tmpFile);
                this.handleFailed(ELMResp.create(req.getUrlString(), ErrorMsg.ERROR_CODE_REQUEST_VERIFY_FAILED, ErrorMsg.getMsg(ErrorMsg.ERROR_CODE_REQUEST_VERIFY_FAILED), resp.getTimeUsed()));
                Logger.d(TAG, "verify file failed.");
                return;
            }
            this.clearVerifyMsg();

            progress = 100.00f;
            this.sendProgressMsg(req.getUrlString());

            File newFile = cache.file();
            if (tmpFile.renameTo(newFile) && newFile.exists()) {
                this.handleSuccess(SLMResp.create(req.getUrlString(), newFile.getAbsolutePath(), resp.getTimeUsed()));
                Logger.d(TAG, "success: load material: " + req.getUrlString() + ", time used: " + resp.getTimeUsed() + "ms");
            } else {
                this.handleFailed(ELMResp.create(req.getUrlString(), ErrorMsg.ERROR_CODE_REQUEST_RENAME_FAILED, ErrorMsg.getMsg(ErrorMsg.ERROR_CODE_REQUEST_RENAME_FAILED), resp.getTimeUsed()));
                Logger.d(TAG, "rename file failed.");
            }
        } catch (Exception e) {
            this.handleFailed(ELMResp.create(req.getUrlString(), ErrorMsg.ERROR_CODE_REQUEST, e.getMessage(), resp.getTimeUsed()));
            Logger.e(TAG, "load material: " + req.getUrlString() + " failed", e);
        }
    }

    private void sendProgressMsg(String url) {
        DBManager.getInstance().updateDownloadProgress(taskId, progress);
        if (this.handler != null)
            this.handler.sendMessage(this.handler.obtainMessage(MSG_PROGRESS, PLMResp.create(url, progress)));
    }

    private void sendVerifyMsg(String url) {
        if (this.handler == null)
            return;
        this.handler.sendMessageDelayed(this.handler.obtainMessage(MSG_VERIFY, url), 500);
    }

    private void clearVerifyMsg() {
        if (this.handler == null)
            return;
        this.handler.removeMessages(MSG_VERIFY);
    }

    @Override
    public void onFailed(XHttpRequest req, XHttpResponse resp) {
        if (resp.getCode() == 416) {
            this.onSuccess(req, resp);
            return;
        }
        this.handleFailed(ELMResp.create(req.getUrlString(), ErrorMsg.ERROR_CODE_RESPONSE, resp.getMsg(), resp.getTimeUsed()));
        Logger.d(TAG, "load material: " + req.getUrlString() + " failed, msg: " + resp.getMsg());
    }

    @Override
    public void onError(XHttpRequest req, String errMsg) {
        this.handleFailed(ELMResp.create(req.getUrlString(), ErrorMsg.ERROR_CODE_NETWORK, errMsg, 0));
        Logger.e(TAG, "load material: " + req.getUrlString() + " failed, msg: " + errMsg);
    }

    @Override
    public void onRetry(XHttpRequest req, String reason) {
        Logger.d(TAG, "load material: " + req.getUrlString() + " failed, msg: " + reason + ", will retry later.");
    }

    private void handleSuccess(SLMResp resp) {
        if (this.handler == null)
            return;
        this.handler.sendMessage(this.handler.obtainMessage(MSG_SUCCESS, resp));
    }

    private void handleFailed(ELMResp resp) {
        if (this.handler == null)
            return;
        this.handler.sendMessage(this.handler.obtainMessage(MSG_FAILED, resp));
    }

    public static class PLMResp {
        public final String url;
        public final float progress;

        private PLMResp(String url, float progress) {
            this.url = url;
            this.progress = progress;
        }

        public static PLMResp create(String url, float progress) {
            return new PLMResp(url, progress);
        }
    }

    public static class SLMResp {
        public final String url;
        public final String localPath;
        public final long timeUsed;

        private SLMResp(String url, String localPath, long timeUsed) {
            this.url = url;
            this.localPath = localPath;
            this.timeUsed = timeUsed;
        }

        public static SLMResp create(String url, String localPath, long timeUsed) {
            return new SLMResp(url, localPath, timeUsed);
        }
    }

    public static class ELMResp {
        public final String url;
        public final int errCode;
        public final String errMsg;
        public final long timeUsed;

        private ELMResp(String url, int errCode, String errMsg, long timeUsed) {
            this.url = url;
            this.errCode = errCode;
            this.errMsg = errMsg;
            this.timeUsed = timeUsed;
        }

        public static ELMResp create(String url, int errCode, String errMsg, long timeUsed) {
            return new ELMResp(url, errCode, errMsg, timeUsed);
        }
    }

    /**************/
    public static class DownloadCache {
        public String dir;
        public String name;
        public String tempName;
        private String md5;

        public DownloadCache(String dir, String name, String tempName) {
            this(dir, name, tempName, "");
        }

        public DownloadCache(String dir, String name, String tempName, String md5) {
            this.setDir(dir);
            this.name = name;
            this.tempName = tempName;
            this.md5 = md5;
        }

        private void setDir(String dir) {
            this.dir = dir;
            FileUtil.makeDirs(this.dir);
        }

        public String getDir() {
            return this.dir;
        }

        public File file() {
            return new File(this.filePath());
        }

        public String filePath() {
            return FileUtil.pathAddBackslash(this.dir) + this.name;
        }

        public File tempFile() {
            return new File(this.tempPath());
        }

        public String tempPath() {
            return FileUtil.pathAddBackslash(this.dir) + this.tempName;
        }

        public boolean verify(File file) {
            return TextUtils.isEmpty(md5) || md5.equalsIgnoreCase(MD5.md5EncodeFile(file));
        }

        public boolean hasVerifyCode() {
            return !TextUtils.isEmpty(this.md5);
        }

        public boolean exists(String file) {
            return FileUtil.fileExists(file);
        }

        public boolean exists(File file) {
            return FileUtil.fileExists(file);
        }
    }
}

package com.yixia.download;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.yixia.download.entity.DownloadEntity;
import com.yixia.util.Device;
import com.yixia.util.Logger;
import com.yixia.util.NetWorkObservable;
import com.yixia.util.Util;

/**
 * Author: zhangwy(张维亚)
 * Email:  zhangweiya@yixia.com
 * 创建时间:2017/5/19 下午1:49
 * 修改时间:2017/5/19 下午1:49
 * Description:下载apk实现类
 */

class DownloadAppImpl extends DownloadApp implements Downloader.DownloadListener {
    private Context context;
    private Downloader.DownloadListener listener;
    private String taskId;
    private String url;
    private String title;
    private String iconUrl;
    private String verifyCode;
    private long fileSize;

    DownloadAppImpl(Context context) {
        super();
        this.context = context;
        NetWorkObservable.initialize(context);
        Downloader.initialized(context, 2, 2.0f, false);
    }

    @Override
    public DownloadApp setListener(Downloader.DownloadListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public boolean download(String url, String title, String iconUrl, String verifyCode, long fileSize) {
        this.url = url;
        this.title = title;
        this.iconUrl = iconUrl;
        this.verifyCode = verifyCode;
        this.fileSize = fileSize;
        taskId = Downloader.getInstance().addTask(url, title, iconUrl, verifyCode, fileSize, DownloadEntity.DldType.APP, this);
        return !TextUtils.isEmpty(taskId);
    }

    @Override
    public void reStart(String url) {
        Downloader.getInstance().reStartTaskByUrl(url);
    }

    @Override
    public boolean stop(String url) {
        return Downloader.getInstance().stopTaskByUrl(url);
    }

    @Override
    public boolean delete(String url) {
        NetWorkObservable.getInstance().unRegister(netWorkObserver);
        return Downloader.getInstance().delTaskByUrl(url);
    }

    @Override
    public void onAddTask(String taskId, String taskUrl, boolean status) {
        if (this.listener != null) {
            this.listener.onAddTask(taskId, taskUrl, status);
        } else if (!status) {
            Toast.makeText(context, "添加任务失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart(String taskId, String taskUrl) {
        NetWorkObservable.getInstance().register(netWorkObserver);
        if (this.listener != null)
            this.listener.onStart(taskId, taskUrl);
    }

    @Override
    public void onProgressChanged(String taskId, String taskUrl, float progress) {
        if (this.listener != null)
            this.listener.onProgressChanged(taskId, taskUrl, progress);
    }

    @Override
    public void onStop(String taskId, String taskUrl) {
        NetWorkObservable.getInstance().unRegister(netWorkObserver);
        if (this.listener != null)
            this.listener.onStop(taskId, taskUrl);
    }

    @Override
    public void onSuccess(String taskId, String taskUrl, String localPath) {
        NetWorkObservable.getInstance().unRegister(netWorkObserver);
        if (this.listener != null) {
            this.listener.onSuccess(taskId, taskUrl, localPath);
        }
        this.showInstall(localPath);
    }

    @Override
    public void onFailed(String taskId, String taskUrl, int errCode, String errMsg) {
        NetWorkObservable.getInstance().unRegister(netWorkObserver);
        if (this.listener != null) {
            this.listener.onFailed(taskId, taskUrl, errCode, errMsg);
        }
    }

    private void showInstall(final String filePath) {
        DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Util.installApp(context, filePath);
            }
        };
        DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        try {
            WindowUtil.createAlertDialog(this.context, R.string.app_name, "安装" + title, "确定", ok, "取消", cancel).show();
        } catch (Throwable e) {
            Logger.e("showInstallApk", e);
        }
    }

    private NetWorkObservable.NetWorkObserver netWorkObserver = new NetWorkObservable.NetWorkObserver() {

        private boolean ignore = false;
        private Dialog netWorkDialog;
        @Override
        public void updateNetWork(Device.NetType netType) {
            if (ignore) {
                NetWorkObservable.getInstance().unRegister(this);
                return;
            }
            if (netType == Device.NetType.WIFI) {
                return;
            }
            this.showNetWork();
        }

        private void showNetWork() {
            if (netWorkDialog != null && netWorkDialog.isShowing())
                return;
            DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NetWorkObservable.getInstance().unRegister(netWorkObserver);
                    dialog.dismiss();
                    ignore = true;
                }
            };
            DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NetWorkObservable.getInstance().unRegister(netWorkObserver);
                    dialog.dismiss();
                    stop(url);
                }
            };
            try {
                netWorkDialog = WindowUtil.createAlertDialog(context, R.string.app_name, "当前非WIFI状态，是否继续下载？", "继续", ok, "停止", cancel);
                netWorkDialog.show();
            } catch (Throwable e) {
                Logger.e("showNetWork", e);
            }
        }
    };
}

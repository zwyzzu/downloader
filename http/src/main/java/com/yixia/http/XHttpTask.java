package com.yixia.http;

import java.net.HttpURLConnection;
import java.util.concurrent.Future;


public abstract class XHttpTask implements Runnable {
    protected final int READ_TIMEOUT = 10000; //10s
    protected final int SIZE_READ_BUFFER = 16*1024;
    protected final int CONNECT_TIMEOUT = 10000; //10s
    protected int maxRetryCount = 1;
    private   boolean stop = false;
    protected Future<?> future;

    protected XHttpRequest request = null;
    protected XHttpResponse response = null;
    protected XHttpHandler handler = null;

    protected HttpURLConnection conn = null;

    protected String userAgent;

    public XHttpTask(String url, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(url);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public XHttpTask(String hostPath, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(hostPath, params);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public XHttpTask(String host, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(host, path, params);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public XHttpTask(String host, int port, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(host, port, path, params);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public XHttpTask(String remoteFile, String localDir, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(remoteFile, localDir);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public XHttpTask(String remoteFile, String localDir, String fileName, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this.request = new XHttpRequest(remoteFile, localDir, fileName);
        this.maxRetryCount = maxRetryCount;
        this.handler = handler;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    protected abstract void request() throws XHttpException;

    private void query() throws Throwable {
        boolean success = false;
        while (this.maxRetryCount-- > 0 && !success) {
            if (isStop())
                break;
            try {
                if (this.handler != null)
                    this.handler.onStart(this.request);

                this.request();
                success = true;
            } catch (Throwable e) {
                if (this.maxRetryCount <= 0) {
                    throw e;
                } else {
                    if (this.handler != null) {
                        String msg = e.getMessage();
                        if (msg == null) {
                            msg = "null, unknown error";
                        }
                        this.handler.onRetry(this.request, msg);
                    }
                }
            }
        }
    }

    private void inform() throws XHttpException {
        if (this.handler == null) {
            return;
        }
        if (this.stop) {
            this.handler.onStop(request);
            return;
        }

        if (this.response == null) {
            throw new XHttpException("null response");
        }

        if (this.response.getCode() == HttpURLConnection.HTTP_OK) {
            this.handler.onSuccess(this.request, this.response);
        } else {
            this.handler.onFailed(this.request, this.response);
        }
    }

    public void run() {
        try {
            this.query();

            this.inform();

        } catch (Throwable e) {
            if (this.handler != null)
                this.handler.onError(this.request, e.getMessage());
        }
    }

    protected final boolean isStop() {
        return stop || (stop = (future != null && future.isCancelled()));
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
package com.yixia.http;

import java.util.concurrent.Future;

public interface XHttpClient {

    Future<?> get(String url, XHttpHandler handler) throws XHttpException;

    Future<?> get(String hostPath, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> get(String host, String path, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> get(String host, int port, String path, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> get(String remoteFile, String localDir, XHttpHandler handler) throws XHttpException;

    Future<?> get(String remoteFile, String localDir, String fileName, boolean append, XHttpHandler handler) throws XHttpException;

    Future<?> post(String url, XHttpHandler handler) throws XHttpException;

    Future<?> post(String hostPath, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> post(String host, String path, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> post(String host, int port, String path, XHttpParams params, XHttpHandler handler) throws XHttpException;

    Future<?> post(String remotePath, String localFile, XHttpHandler handler) throws XHttpException;

    Future<?> get(String url, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String hostPath, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String host, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String host, int port, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String remoteFile, String localDir, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String remoteFile, String localDir, String fileName, boolean append, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> get(String remoteFile, String localDir, String fileName, boolean append, boolean block, float blockSizeMb, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> post(String url, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> post(String hostPath, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> post(String host, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> post(String host, int port, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    Future<?> post(String remotePath, String localFile, int maxRetryCount, XHttpHandler handler) throws XHttpException;

    void setUserAgent(String userAgent);

    void destroy();
}

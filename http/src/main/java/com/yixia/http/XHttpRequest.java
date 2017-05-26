package com.yixia.http;

import java.io.File;
import java.net.URL;

/**
 * data structure for http request
 */
public class XHttpRequest {
    /**
     * request url object
     */
    private URL url;

    /**
     * local file directory for get file from remote server
     */
    private String localDir;

    /**
     * local file name for download/upload from/to remote server
     */
    private String fileName;

    public XHttpRequest(String url) throws XHttpException {
        try {
            this.url = new URL(url);
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public XHttpRequest(String hostPath, XHttpParams params) throws XHttpException {
        try {
            if (params != null)
                this.url = new URL(hostPath + "?" + params.encode());
            else
                this.url = new URL(hostPath);
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public XHttpRequest(String host, String path, XHttpParams params) throws XHttpException {
        try {
            if (path != null && !path.isEmpty()) {
                if (params != null)
                    this.url = new URL("http://" + host + path + "?" + params.encode());
                else
                    this.url = new URL("http://" + host + path);
            } else {
                if (params != null)
                    this.url = new URL("http://" + host + "/?" + params.encode());
                else
                    this.url = new URL("http://" + host + "/?");
            }
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public XHttpRequest(String host, int port, String path, XHttpParams params) throws XHttpException {
        try {
            if (path != null && !path.isEmpty()) {
                if (params != null)
                    this.url = new URL("http://" + host + ":" + String.valueOf(port) + path + "?" + params.encode());
                else
                    this.url = new URL("http://" + host + ":" + String.valueOf(port) + path);
            } else {
                if (params != null)
                    this.url = new URL("http://" + host + ":" + String.valueOf(port) + "/?" + params.encode());
                else
                    this.url = new URL("http://" + host + ":" + String.valueOf(port) + "/?");
            }
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public XHttpRequest(String url, String localDir) throws XHttpException {
        if (localDir == null || localDir.equals("")) {
            throw new XHttpException("local dir must not be null!");
        }

        try {
            this.url = new URL(url);
            this.localDir = localDir;
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public XHttpRequest(String url, String localDir, String fileName) throws XHttpException {
        if (localDir == null || localDir.equals("")) {
            throw new XHttpException("local dir must not be null!");
        }

        if (fileName == null || fileName.equals("")) {
            throw new XHttpException("file name must not be null!");
        }

        try {
            this.url = new URL(url);
            this.localDir = localDir;
            this.fileName = fileName;
        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        }
    }

    public String getUrlString() {
        return this.url.toString();
    }

    public String getProtocol() {
        return this.url.getProtocol();
    }

    public String getHost() {
        return this.url.getHost();
    }

    public int getPort() {
        return this.url.getPort();
    }

    public String getPath() {
        return this.url.getPath();
    }

    public String getQuery() {
        return this.url.getQuery();
    }

    public String getLocalDir() {
        if (localDir == null)
            return "";

        return localDir;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLocalFile() {
        if (localDir != null && fileName != null) {
            return localDir + File.separator + fileName;
        }

        return null;
    }
}

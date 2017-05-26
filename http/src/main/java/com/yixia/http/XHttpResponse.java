package com.yixia.http;

import java.util.List;
import java.util.Map;

/**
 * data structure for the http request response
 */
public class XHttpResponse {
    //http response code
    private int code = -1;
    //http response message
    private String msg = null;
    //http response headers
    private Map<String, List<String>> headers = null;
    //http response content
    private byte[] content = null;
    //local file path for http response file
    private String localFile;

    //request time used in million seconds
    private long timeUsed = 0;

    public XHttpResponse(int code, String msg, Map<String, List<String>> headers, byte[] content, long timeUsed) {
        this.code = code;
        this.msg = msg;
        this.headers = headers;
        this.content = content;
        this.timeUsed = timeUsed;
    }

    public XHttpResponse(int code, String msg, Map<String, List<String>> headers, String localFile, long timeUsed) {
        this.code = code;
        this.msg = msg;
        this.headers = headers;
        this.localFile = localFile;
        this.timeUsed = timeUsed;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return msg;
    }

    public String getHeaderField(String key) {
        if (this.headers == null || !this.headers.containsKey(key))
            return null;

        List<String> lst = this.headers.get(key);

        if (lst != null && lst.size() > 0) {
            return lst.get(0);
        }

        return null;
    }

    public List<String> getHeaderFields(String key) {
        if (this.headers == null || !this.headers.containsKey(key))
            return null;

        return this.headers.get(key);
    }

    public byte[] getContent() {
        return this.content;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public String getLocalFile() {
        if (this.localFile == null)
            return "";

        return this.localFile;
    }
}

package com.yixia.http;

public class XHttp {
    private static XHttpClient defaultClient = null;

    public static synchronized XHttpClient defaultHttpClient() {
        if (defaultClient == null)
            defaultClient = new XHttpAsyncClient(false);
        return defaultClient;
    }

    public static XHttpClient newHttpClient() {
        return new XHttpAsyncClient(false);
    }

    public static XHttpClient newSingleHttpClient() {
        return new XHttpAsyncClient(true);
    }

    public static XHttpClient newHttpClient(int maxPoolSize) {
        return new XHttpAsyncClient(maxPoolSize);
    }
}

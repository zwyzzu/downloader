package com.yixia.http;

/**
 * recall handler for http request after the request has executed
 */
public abstract class XHttpHandler {
    protected final String TAG = this.getClass().getSimpleName();
    //object for recall
    protected Object obj = null;

    public XHttpHandler() {

    }

    public XHttpHandler(Object obj) {
        this.obj = obj;
    }

    /**
     * notify the invoker that send request now
     *
     * @param req: the http request
     */
    public void onStart(XHttpRequest req) {
    }

    /**
     * notify the invoker that send request now
     *
     * @param req: the http request
     */
    public void onStop(XHttpRequest req) {
    }

    /**
     * notify the invoker that the request has success, which means the
     * http response code is 200 OK
     *
     * @param req:  the http request
     * @param resp: the relate response
     */
    public abstract void onSuccess(XHttpRequest req, XHttpResponse resp);

    /**
     * notify the invoker that the request has failed, which means the
     * there is response, but the response code is not 200 OK
     *
     * @param req:  the http request
     * @param resp: the relate response
     */
    public abstract void onFailed(XHttpRequest req, XHttpResponse resp);

    /**
     * notify the invoker that the request has an error, which means the
     * request has not executed
     *
     * @param req:    the http request
     * @param errMsg: the relate error message
     */
    public abstract void onError(XHttpRequest req, String errMsg);

    /**
     * notify the invoker that the request will retry later
     * request has not executed
     *
     * @param req:    the http request
     * @param reason: the retry reason
     */
    public abstract void onRetry(XHttpRequest req, String reason);
}


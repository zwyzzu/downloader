package com.yixia.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class XHttpPostTask extends XHttpTask {

    public XHttpPostTask(String url, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(url, maxRetryCount, handler);
    }

    public XHttpPostTask(String hostPath, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(hostPath, params, maxRetryCount, handler);
    }

    public XHttpPostTask(String host, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(host, path, params, maxRetryCount, handler);
    }

    public XHttpPostTask(String host, int port, String path, XHttpParams params, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(host, port, path, params, maxRetryCount, handler);
    }

    protected void request() throws XHttpException {
        //the response content reading input stream
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        try {
            //record the request start time point
            long startTime = System.currentTimeMillis();

            //prepare the url connection
            URL url = new URL(request.getProtocol(), request.getHost(), request.getPort(), request.getPath());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            if (getUserAgent() != null) {
                conn.setRequestProperty("User-agent", getUserAgent());
            }

            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Connection", "Close");
            String query = request.getQuery();
            if (query != null) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(query.getBytes());
            }

            //get the response code and message
            int code = conn.getResponseCode();
            String msg = conn.getResponseMessage();
            String encoding = conn.getContentEncoding();
            Map<String, List<String>> headers = conn.getHeaderFields();

            //read the response content
            if (encoding != null && encoding.toLowerCase().matches("gzip")) {
                in = new GZIPInputStream(new BufferedInputStream(conn.getInputStream()));
            } else {
                in = new BufferedInputStream(conn.getInputStream());
            }

            //for read response content from remote host
            byte[] buf = new byte[SIZE_READ_BUFFER];
            baos = new ByteArrayOutputStream();

            //read the response content
            int sz = in.read(buf);
            while (sz != -1) {
                baos.write(buf, 0, sz);
                sz = in.read(buf);
            }

            //record the request end time point
            long endTime = System.currentTimeMillis();

            //make the response object
            this.response = new XHttpResponse(code, msg, headers, baos.toByteArray(), endTime - startTime);

        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (baos != null) {
                    baos.close();
                }

                //close the connection
                conn.disconnect();
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }
}

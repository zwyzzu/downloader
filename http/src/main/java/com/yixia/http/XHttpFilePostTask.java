package com.yixia.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class XHttpFilePostTask extends XHttpTask {
    private File localFile;

    public XHttpFilePostTask(String remotePath, String localFile, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(remotePath, maxRetryCount, handler);
        this.localFile = new File(localFile);
    }

    protected void request() throws XHttpException {
        InputStream fin = null;
        OutputStream os = null;
        InputStream in = null;
        ByteArrayOutputStream baos = null;

        try {
            //record the request start time point
            long startTime = System.currentTimeMillis();

            //prepare the url connection
            URL url = new URL(request.getUrlString());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setUseCaches(false);
            if (getUserAgent() != null) {
                conn.setRequestProperty("User-agent", getUserAgent());
            }

            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Connection", "Keep-Alive");
            Random rand = new Random(System.currentTimeMillis());
            String boundary = "---------------------------7d" + Long.toHexString(rand.nextLong());
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            StringBuffer start = new StringBuffer();
            start.append("--").append(boundary).append("\r\n");
            start.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(this.localFile.getName()).append("\"\r\n");
            start.append("Content-Type: application/octet-stream\r\n");
            start.append("Content-Transfer-Encoding: binary\r\n\r\n");

            String end = "\r\n--" + boundary + "--\r\n";

            byte[] startByte = start.toString().getBytes();
            byte[] endByte = end.getBytes();

            long contentLength = this.localFile.length() + startByte.length + endByte.length;
            conn.addRequestProperty("Content-Length", String.valueOf(contentLength));

            //output stream for connection
            os = conn.getOutputStream();
            os.write(startByte);

            //data buffer for read and write
            byte[] buf = new byte[SIZE_READ_BUFFER];
            fin = new FileInputStream(this.localFile);
            int sz = fin.read(buf);
            while (sz != -1) {
                os.write(buf, 0, sz);
                sz = fin.read(buf);
            }

            os.write(endByte);
            os.flush();

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
            baos = new ByteArrayOutputStream();
            //read the response content
            sz = in.read(buf);
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
                if (fin != null) {
                    fin.close();
                }

                if (os != null) {
                    os.close();
                }

                if (in != null) {
                    in.close();
                }

                if (baos != null) {
                    baos.close();
                }
                conn.disconnect();
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }
}

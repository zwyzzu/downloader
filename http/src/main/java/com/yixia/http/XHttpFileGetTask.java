package com.yixia.http;

import com.yixia.cipher.MD5;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class XHttpFileGetTask extends XHttpTask {
    private boolean block = false;
    private boolean append = false;
    private long appendPos = 0;
    private final long BLOCK_SIZE_MB = 1048576;//1Mb
    private long blockSize = BLOCK_SIZE_MB;//
    private XDownloadHandler downloadHandler;

    /**
     * 构造函数下载文件
     *
     * @param remoteFile    远程URL
     * @param localDir      本地文件目录
     * @param maxRetryCount 最大尝试次数
     * @param handler       回调
     * @throws XHttpException 异常
     */
    public XHttpFileGetTask(String remoteFile, String localDir, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(remoteFile, localDir, maxRetryCount, handler);
        this.setDownloadHandler(handler);
    }

    /**
     * 构造函数具有断点续传功能
     *
     * @param remoteFile    远程URL
     * @param localDir      本地文件目录
     * @param fileName      本地文件名
     * @param append        true 断点续传
     * @param maxRetryCount 最大尝试次数
     * @param handler       回调
     * @throws XHttpException 异常
     */
    public XHttpFileGetTask(String remoteFile, String localDir, String fileName, boolean append, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        super(remoteFile, localDir, fileName, maxRetryCount, handler);
        this.append = append;
        this.setDownloadHandler(handler);
    }

    /**
     * 构造函数具有分块功能和断点续传功能
     *
     * @param remoteFile    远程URL
     * @param localDir      本地文件目录
     * @param fileName      本地文件名
     * @param append        true 断点续传
     * @param block         true 分块下载
     * @param blockSizeMb   分块下载的块大小单位位Mb
     * @param maxRetryCount 最大尝试次数
     * @param handler       回调
     * @throws XHttpException 异常
     */
    public XHttpFileGetTask(String remoteFile, String localDir, String fileName, boolean append, boolean block, float blockSizeMb, int maxRetryCount, XHttpHandler handler) throws XHttpException {
        this(remoteFile, localDir, fileName, append, maxRetryCount, handler);
        this.block = block;
        if (this.block) {
            if (blockSizeMb <= 0) {
                this.block = false;
                return;
            }
            this.blockSize = (long) (BLOCK_SIZE_MB * blockSizeMb);
        }
    }

    private void setDownloadHandler(XHttpHandler handler) {
        if (handler != null && handler instanceof XDownloadHandler)
            this.downloadHandler = (XDownloadHandler) handler;
    }

    protected void request() throws XHttpException {
        if (isStop())
            return;
        // the response content reading input stream
        InputStream in = null;
        // the file output stream
        FileOutputStream fos = null;
        {
            if (this.append) {
                File file = new File(super.request.getLocalFile());
                if (file.exists() && file.isFile()) {
                    this.appendPos = file.length();
                    if (this.appendPos <= 0) {
                        this.append = false;
                    }
                }
            }
        }

        try {
            // record the request start time point
            long startTime = System.currentTimeMillis();

            // prepare the url connection
            URL url = new URL(request.getUrlString());
            do {
                if (this.isStop()) {
                    return;
                }
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                if (getUserAgent() != null) {
                    conn.setRequestProperty("User-agent", getUserAgent());
                }

                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
                conn.setRequestProperty("Connection", "Keep-Alive");

                if ((this.append && this.appendPos > 0) || (this.block && this.blockSize > 0)) {
                    StringBuilder range = new StringBuilder("bytes=").append(this.appendPos).append("-");
                    if (this.block && this.blockSize > 0)
                        range.append(this.appendPos + blockSize - 1);
                    conn.setRequestProperty("Range", range.toString());
                }

                // get the response code and message
                int code = conn.getResponseCode();
                String msg = conn.getResponseMessage();
                Map<String, List<String>> headers = conn.getHeaderFields();

                if (code >= 200 && code < 300) {
                    // check the response partial content
                    if (this.append && code != HttpURLConnection.HTTP_PARTIAL) {
                        this.append = false;
                    }

                    String encoding = conn.getContentEncoding();
                    // read the response content
                    if (encoding != null && encoding.toLowerCase().matches("gzip")) {
                        in = new GZIPInputStream(new BufferedInputStream(conn.getInputStream()));
                    } else {
                        in = new BufferedInputStream(conn.getInputStream());
                    }

                    // get the file name of the request file
                    // output local file path
                    String filePath = super.request.getLocalDir() + File.separator + fileName();

                    // open the out put file stream
                    if (fos == null)
                        fos = new FileOutputStream(filePath, this.append);

                    // for read response content from remote host
                    byte[] buf = new byte[SIZE_READ_BUFFER];

                    long length = 0;
                    // read the response content
                    int sz;
                    while ((sz = in.read(buf)) != -1) {
                        length += sz;
                        fos.write(buf, 0, sz);
                        if (downloadHandler != null) {
                            downloadHandler.onProgressChanged(this.request, this.appendPos + length);
                        }
                    }

                    if (this.isStop()) {
                        return;
                    }

                    if (this.block && length == blockSize) {
                        this.appendPos += blockSize;
                        continue;
                    }

                    // record the request end time point
                    long endTime = System.currentTimeMillis();

                    code = (code == HttpURLConnection.HTTP_PARTIAL) ? HttpURLConnection.HTTP_OK : code;
                    // make the response object
                    this.response = new XHttpResponse(code, msg, headers, filePath, endTime - startTime);
                    break;
                } else {
                    // record the request end time point
                    long endTime = System.currentTimeMillis();
                    this.response = new XHttpResponse(code, msg, headers, "", endTime - startTime);
                    break;
                }
            } while (block);

        } catch (Exception e) {
            throw new XHttpException(e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (fos != null) {
                    fos.close();
                }

                // close the connection
                conn.disconnect();
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    private String fileName() {
        String fileName = super.request.getFileName();
        if (fileName == null) {
            fileName = getFileNameFromCD(conn.getHeaderField("Content-Disposition"));
            if (fileName == null) {
                fileName = getFileNameFromPath(super.request.getPath());
                if (fileName == null) {
                    fileName = MD5.md5Encode(super.request.getUrlString());
                }
            }
        }
        return fileName;
    }

    private String getFileNameFromCD(String cd) {
        if (cd == null || cd.equals(""))
            return null;

        Pattern pattern = Pattern.compile("filename=\"*([^;\"]+)");
        Matcher matcher = pattern.matcher(cd);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String getFileNameFromPath(String path) {
        if (path == null || path.equals(""))
            return null;

        Pattern pattern = Pattern.compile("([^/\\\\]+)$");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

}

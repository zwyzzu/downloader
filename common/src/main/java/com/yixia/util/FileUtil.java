package com.yixia.util;

import android.text.TextUtils;

import java.io.File;

/**
 * Author: zhangwy(张维亚)
 * 创建时间：2017/4/7 上午11:20
 * 修改时间：2017/4/7 上午11:20
 * Description:
 */

public class FileUtil {
    private final static String TAG = "FileUtil";

    public static boolean makeDirs(String dir) {
        return makeDirs(new File(dir));
    }

    public static boolean makeDirs(File file) {
        return file.mkdirs();
    }

    // 去掉path中的反斜线
    public static String pathRemoveBackslash(String path) {
        if (path == null)
            return path;
        if (path.length() == 0)
            return path;
        //
        char ch = path.charAt(path.length() - 1);
        if (ch == '/' || ch == '\\')
            return path.substring(0, path.length() - 1);
        return path;

    }

    // 在path中添加反斜线
    public static String pathAddBackslash(String path) {
        if (path == null)
            return java.io.File.separator;
        if (path.length() == 0)
            return java.io.File.separator;
        //
        char ch = path.charAt(path.length() - 1);
        if (ch == '/' || ch == '\\')
            return path;
        return path + java.io.File.separator;
    }

    /**
     * clear the files in the directory that match the regex
     *
     * @param regex: regex of the file name
     */
    public static void clear(String dir, String regex) {
        try {
            File[] files = new File(dir).listFiles();
            if (Util.isEmpty(files))
                return;
            for (File file : files) {
                if (regex != null) {
                    if (file.getName().matches(regex)) {
                        file.delete();
                    }
                } else {
                    file.delete();
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "clear(String dir, String regex)", e);
        }
    }

    public static void clear(String dir, String regex, long millionSecondsAgo) {
        try {
            File[] files = new File(dir).listFiles();
            if (Util.isEmpty(files))
                return;
            for (File file : files) {
                if (regex != null) {
                    if (file.getName().matches(regex)) {
                        if (System.currentTimeMillis() - file.lastModified() > millionSecondsAgo) {
                            file.delete();
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - file.lastModified() > millionSecondsAgo) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "clear(String dir, String regex, long millionSecondsAgo)", e);
        }
    }

    public static boolean findFileByName(String dir, String fileName) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory())
            return false;

        File files[] = dirFile.listFiles();
        if(files==null||files.length==0) return false;
        for (File file : files) {
            String name = file.getName();
            name = name.substring(0, name.length() - 4);
            if (name.equalsIgnoreCase(fileName)) {
                return true;
            }
        }

        return false;
    }

    // 删除文件
    public static boolean deleteFile(String filename) {
        return deleteFile(new File(filename));
    }

    // 删除文件
    public static boolean deleteFile(File file) {
        try {
            return !fileExists(file) || file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    // 文件查找
    public static boolean fileExists(String fileName) {
        try {
            String path = extractFilePath(fileName);// 获取文件路径
            String name = extractFileName(fileName);// 获取文件名
            return fileExists(new File(path, name));// 判断是否能在path中找到name文件，如果找到返回true否则返回false
        } catch (Exception err) {
            return false;
        }
    }

    // 文件查找
    public static boolean fileExists(File file) {
        try {
            return file.exists();
        } catch (Exception err) {
            return false;
        }
    }

    // 获取文件路径
    public static String extractFilePath(String filename) {
        filename = pathRemoveBackslash(filename);
        //
        int pos = filename.lastIndexOf('/');
        if (-1 == pos)
            return "";
        return filename.substring(0, pos);
    }

    // 获取文件名
    public static String extractFileName(String filename) {
        if (!TextUtils.isEmpty(filename) && hasPathSlash(filename)) {
            int i = filename.lastIndexOf('/');
            if (i < 0)
                i = filename.lastIndexOf("\\");
            //
            if ((i > -1) && (i < (filename.length()))) {
                return filename.substring(i + 1);
            }
        }
        return filename;
    }

    // 判断字符段中是否存在反斜线
    static boolean hasPathSlash(String path) {
        return !TextUtils.isEmpty(path) && (path.contains("/") || path.contains("\\"));
    }
}

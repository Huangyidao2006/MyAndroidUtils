package com.hj.android.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hj at 2022/5/20 18:07
 */
public class FileUtil {
    public static byte[] readAssetFile(Context context, String filePath) {
        try {
            InputStream ins = context.getAssets().open(filePath);
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();

            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getDir(String path) {
        int lastIndex = path.lastIndexOf(File.separatorChar);
        if (-1 == lastIndex) {
            return path;
        }

        return (lastIndex == path.length() - 1) ? path : path.substring(0, lastIndex + 1);
    }

    /**
     * 创建目录。
     *
     * @param path 文件或者目录路径
     * @return 创建成功或者已经存在返回true，否则false
     */
    public static boolean createDir(String path) {
        String dir = getDir(path);
        File file = new File(dir);

        if (!file.exists()) {
            return file.mkdirs();
        }

        return true;
    }

    /**
     * 创建文件或者目录。
     *
     * @param path 路径
     * @return 成功则返回文件对象，失败返回null
     */
    public static File createFile(String path) {
        boolean ret = createDir(path);
        if (!ret) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return file;
    }

    /**
     * 数据保存到文件，若文件目录不存在则会创建各级目录。
     *
     * @param data 数据
     * @param path 文件路径
     * @return 是否成功
     */
    public static boolean saveToFile(byte[] data, String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean ret = createDir(path);
            if (!ret) {
                return false;
            }

            try {
                ret = file.createNewFile();
                if (!ret) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (file.isDirectory()) {
                return false;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除文件（夹）。
     *
     * @param file 文件
     * @return 是否成功
     */
    public static boolean delFile(File file) {
        if (null == file) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        }

        boolean deleted = true;

        File[] subFiles = file.listFiles();
        if (null != subFiles) {
            for (File subFile : subFiles) {
                deleted = delFile(subFile);

                if (!deleted) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 按列表删除文件。
     *
     * @param fileList
     */
    public static void delFlies(List<File> fileList) {
        if (null == fileList) {
            return;
        }

        for (File file : fileList) {
            try {
                FileUtil.delFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检测文件是否存在。
     *
     * @param path 文件全路径
     * @return 是否存在
     */
    public static boolean exist(String path) {
        File file = new File(path);

        return file.exists();
    }

    /**
     * 获取文件（夹）的大小，单位：字节。
     *
     * @param file          文件（夹）对象
     * @param sizeDetails   用于存储文件夹下文件夹大小信息
     * @param onlyDirDetail 是否只需要子文件夹信息
     * @return
     */
    public static long getFileSizeBytes(File file, Map<File, Long> sizeDetails,
                                        boolean onlyDirDetail) {
        if (!file.exists()) {
            return 0;
        }

        if (file.isFile()) {
            return file.length();
        }

        long totalSizeBytes = 0;

        File[] subFiles = file.listFiles();
        if (null != subFiles) {
            for (File subFile : subFiles) {
                long subFileSizeBytes = getFileSizeBytes(subFile, null, onlyDirDetail);

                if (null != sizeDetails) {
                    if (onlyDirDetail) {
                        if (subFile.isDirectory()) {
                            sizeDetails.put(subFile.getAbsoluteFile(), subFileSizeBytes);
                        }
                    } else {
                        sizeDetails.put(subFile.getAbsoluteFile(), subFileSizeBytes);
                    }
                }

                totalSizeBytes += subFileSizeBytes;
            }
        }

        return totalSizeBytes;
    }

    /**
     * 获取文件的大小，单位：字节。
     *
     * @param filePath 文件路径
     * @return
     */
    public static long getFileSizeBytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return 0;
        }

        if (file.isFile()) {
            return file.length();
        }

        return 0;
    }

    /**
     * 将文件读出为String。
     *
     * @param path 文件路径
     * @return 读取出返回null
     */
    public static String readAsString(String path) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 从assets目录读取字符文件。
     *
     * @param filePath 文件路径
     * @return 文件内容，失败返回null
     */
    public static String readAssetAsString(Context context, String filePath) {
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open(filePath);
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取目录下的文件。
     *
     * @param dir      目录
     * @param suffixes 文件后缀名列表
     * @param subDirs  是否处理子目录
     * @return 文件列表
     */
    public static List<File> listFile(String dir, List<String> suffixes, boolean subDirs) {
        List<File> result = new ArrayList<>();

        File file = new File(dir);
        if (!file.exists() || file.isFile()) {
            return result;
        }

        File[] subFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (suffixes == null || suffixes.isEmpty()) {
                    return true;
                }

                for (String it : suffixes) {
                    if (pathname.getAbsolutePath().endsWith(it)) {
                        return true;
                    }
                }

                return false;
            }
        });

        if (null != subFiles) {
            for (File f : subFiles) {
                if (f.isFile()) {
                    result.add(f);
                } else {
                    if (subDirs) {
                        // 还要处理子目录
                        List<File> subResult = listFile(f.getAbsolutePath(), suffixes, true);
                        result.addAll(subResult);
                    }
                }
            }
        }

        return result;
    }
}

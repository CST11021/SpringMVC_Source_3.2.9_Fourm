package com.whz.utils.file;

import org.apache.commons.io.FileUtils;

import java.io.*;

public class FileUtil {

    public void test() {

        // 请参考：org.apache.commons.io.FileUtils 工具类

        FileUtils.getTempDirectory();
    }

    public static void main(String[] args) {
        File dir = new File("/Users/wanghongzhan/Documents/whz/temp");
        System.out.println(getFile(dir, "test1", "test2"));
    }

    /**
     * 根据路径名（或文件名）返回一个File对象，例如：
     *
     * getFile(new File("/Users/whz/temp"), "test1", "test2.xml");
     * 相当于：
     * new File("/Users/whz/temp/test1/test2.xml");
     *
     * @param directory
     * @param names
     * @return
     */
    public static File getFile(File directory, String... names) {
        if (directory == null) {
            throw new NullPointerException("directorydirectory must not be null");
        }
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = directory;
        for (String name : names) {
            file = new File(file, name);
        }
        return file;
    }

    /**
     * 获取文件对象的输入流，用于读取文件内容，注意：该文件对象不能是目录
     * @param file
     * @return
     * @throws IOException 文件不存在、文件是一个目录或文件不可读取时抛出异常
     */
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
     * 获取文件对象的输出流，用于往文件写入数据
     * @param file 要写入数据的文件对象
     * @return
     * @throws IOException 文件不存在、文件是目录或文件不可写时抛出异常
     */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * 获取文件对象的输出流，用于往文件写入数据，如果
     * @param file  要写入数据的文件对象
     * @param append 为true时，当文件存在数据则追加数据
     * @return
     * @throws IOException 文件不存在、文件是目录或文件不可写时抛出异常
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

}

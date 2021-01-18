package com.dn.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Slf4j
public class ClassUtils {

    /**
     * 从一个指定路径下查找所有的类
     *
     * @param packageName
     */
    private static ArrayList<Class> getAllClass(String packageName) {
        log.info("packageName to search：" + packageName);
        List<String> classNameList = getClassName(packageName);
        ArrayList<Class> list = new ArrayList<>();

        for (String className : classNameList) {
            try {
                list.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                log.error("load class from name failed:" + className + e.getMessage());
                throw new RuntimeException("load class from name failed:" + className + e.getMessage());
            }
        }
        log.info("find list size :" + list.size());
        return list;
    }


    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            log.debug("file type : " + type);
            if (type.equals("file")) {
                String fileSearchPath = url.getPath();
                log.debug("fileSearchPath: " + fileSearchPath);
                fileSearchPath = fileSearchPath.substring(0, fileSearchPath.indexOf("/classes"));
                log.debug("fileSearchPath: " + fileSearchPath);
                fileNames = getClassNameByFile(fileSearchPath);
            } else if (type.equals("jar")) {
                try {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();
                    fileNames = getClassNameByJar(jarFile, packagePath);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("open Package URL failed：" + e.getMessage());
                }

            } else {
                throw new RuntimeException("file system not support! cannot load MsgProcessor！");
            }
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath 文件路径
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                myClassName.addAll(getClassNameByFile(childFile.getPath()));
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(JarFile jarFile, String packagePath) {
        List<String> myClassName = new ArrayList<String>();
        try {
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                //LOG.info("entrys jarfile:"+entryName);
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                    //LOG.debug("Find Class :"+entryName);
                }
            }
        } catch (Exception e) {
            log.error("发生异常:" + e.getMessage());
            throw new RuntimeException("发生异常:" + e.getMessage());
        }
        return myClassName;
    }


    /**
     * 获取包路径
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getClassPath() throws UnsupportedEncodingException {
        String url = URLDecoder.decode(ClassUtils.class.getResource("/").getPath(), Charset.defaultCharset().name());
        if (url.startsWith("/")) {
            url = url.replaceFirst("/", "");
        }
        url = url.replaceAll("/", "\\\\");
        return url;
    }

    private static final List<Class> classList = new LinkedList();

    // 类就绪后就开始扫描当前路径下的所有类
    static {
        try {
            getClassNameByFile(getClassPath()).forEach(className -> {
                try {
                    // 扫描用户路径下的所有类, 将那些不是接口的并且不是抽象类的
                    Class<?> aClass = Class.forName(className);
                    if (!aClass.isInterface() && !Modifier.isAbstract(aClass.getModifiers())) {
                        classList.add(aClass);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定类型的实现类
     *
     * @param clazz
     * @return
     */
    public static List<Class> getImplementationClass(Class clazz) {
        List<Class> result = classList.stream().filter(clazz::isAssignableFrom).collect(Collectors.toList());
        return result;
    }
}

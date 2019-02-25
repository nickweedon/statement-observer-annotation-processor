package com.redwyvern.util;

import com.redwyvern.statementobserver.StatementSubject;
import net.openhft.compiler.CachedCompiler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Scanner;

public class ResourceUtil {

    public static Class<?> getClassFromJavaResourceFile(String fullQualifiedClassName, String resourcePath) {
        CachedCompiler cc = new CachedCompiler(null, null);

        try {
            return cc.loadFromJava("au.org.weedon.redblacktree.HelloWorldSubject", ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubject.java"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to compile class '" + fullQualifiedClassName + "' from resource path '" + resourcePath + "'");
        }
    }


    public static <T> T getInstanceFromJavaResourceFile(String fullQualifiedClassName, String resourcePath) {
        Class<?> clazz = getClassFromJavaResourceFile(fullQualifiedClassName, resourcePath);
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate compiled class '" + fullQualifiedClassName + "' from resource path '" + resourcePath + "'");
        }
    }


    public static InputStream getInputStream(String resourceFileName) {
        ClassLoader classLoader = ResourceUtil.class.getClassLoader();
        URL resource = classLoader.getResource(resourceFileName);
        if(resource == null) {
            throw new RuntimeException("Could not find resource '" + resourceFileName + "'");
        }
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw new RuntimeException("Exception while opening resource '" + resourceFileName + "'", e);
        }
    }

    public static String getTrimmedFileContents(InputStream resourceInputStream) {
        return getFileContents(resourceInputStream).trim();
    }

    public static String getFileContents(InputStream resourceInputStream) {

        StringBuilder result = new StringBuilder();
        try (Scanner scanner = new Scanner(resourceInputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append(System.lineSeparator());
            }
        }

        return result.toString();
    }

    public static String getFileContents(String resourceFileName) {
        return getFileContents(getInputStream(resourceFileName));
    }

}

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

    //static final String STANDARD_LINE_ENDING = System.lineSeparator();
    static final String STANDARD_LINE_ENDING = "\n";

    public static Class<?> getClassFromJavaResourceFile(String fullQualifiedClassName, String resourcePath) {

        ClassLoader classLoader = ResourceUtil.class.getClassLoader();


        try {
            return classLoader.loadClass(fullQualifiedClassName);
        } catch (ClassNotFoundException e) {
            // If the class was not found then fall through and compile it
        }

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

    public static String normalizeLine(String inputLine) {
        return inputLine.replaceAll("\r", "").replaceAll("\n", "") + STANDARD_LINE_ENDING;
    }

    public static String normalizeLineEndings(String inputString) {
        StringBuilder result = new StringBuilder();
        try (Scanner scanner = new Scanner(inputString)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(normalizeLine(line));
            }
        }

        return result.toString();
    }

    public static String getFileContents(InputStream resourceInputStream) {

        StringBuilder result = new StringBuilder();
        try (Scanner scanner = new Scanner(resourceInputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(normalizeLine(line));
            }
        }

        return result.toString();
    }

    public static String getFileContents(String resourceFileName) {
        return getFileContents(getInputStream(resourceFileName));
    }

}

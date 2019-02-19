package com.redwyvern.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class ResourceUtil {

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

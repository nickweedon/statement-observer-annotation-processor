package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.codemodel.CodeLine;
import com.redwyvern.statementobserver.codemodel.Statement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

public class SubjectHelper {

    public static int getStackTraceLine(int depth) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2 + depth];
        return stackTraceElement.getLineNumber();
    }

    static String getSubjectClassResourceClassFileCodePath(Class<?> subjectClass) {
        String className = subjectClass.getSimpleName();
        if(!className.endsWith("Subject")) {
            throw new RuntimeException("Cannot retrieve a ClassFileCode serialized object for a class that is not an observer subject");
        }
        // Remove the 'Subject' suffix
        className = className.substring(0, className.length() - "Subject".length());
        className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        String relativePath = subjectClass.getPackageName().replace('.', '/');
        return "statementobserver" + "/" + relativePath + "/" + className + ".ser";
    }

    public static ClassFileCode loadResourceClassFileCode(Class<?> subjectClass) throws IOException, ClassNotFoundException {
        return loadResourceClassFileCode(getSubjectClassResourceClassFileCodePath(subjectClass));
    }

    public static Statement getExecutingStatement(ClassFileCode classFileCode, Integer currentlyExecutingLine) {
        if(currentlyExecutingLine == null) {
            throw new RuntimeException("'currentlyExecutingLine' is null. Most likely an attempt was made to retrieve the executing statement from outside of observer callback.");
        }
        CodeLine codeLine = classFileCode.getCodeLineMap().get(currentlyExecutingLine);
        if(codeLine == null) {
            throw new RuntimeException("Missing source code for currently executing line (" + currentlyExecutingLine + ")");
        }
        if(codeLine.getStatement() == null) {
            throw new RuntimeException("Extracted code line for currently executing line does not belong to a statement");
        }

        return codeLine.getStatement();
    }

    private static ClassFileCode loadResourceClassFileCode(String resourceRelativeFilename) throws IOException, ClassNotFoundException {
        InputStream resourceInputStream = getResourceInputStream(resourceRelativeFilename);
        ObjectInputStream objectInputStream = new ObjectInputStream(resourceInputStream);
        return (ClassFileCode) objectInputStream.readObject();
    }

    public static InputStream getResourceInputStream(String resourceFileName) {
        ClassLoader classLoader = SubjectHelper.class.getClassLoader();
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
}

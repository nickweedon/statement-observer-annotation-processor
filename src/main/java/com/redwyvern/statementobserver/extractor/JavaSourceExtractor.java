package com.redwyvern.statementobserver.extractor;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class JavaSourceExtractor {

    private static final int TAB_SPACE = 4;
    private static final String TEST_FILE = "testFile.java";
    //private static final String TEST_FILE = "smallTestFile.java";

    static class TabExpansionFilterInputStream extends FilterInputStream {

        private final Queue<Character> streamQueue = new LinkedList<>();
        private final int tabSpaces;

        protected TabExpansionFilterInputStream(InputStream inputStream, int tabSpaces) {
            super(inputStream);
            this.tabSpaces = tabSpaces;
        }

        @Override
        public int read() throws IOException {

            if(streamQueue.size() != 0) {
                return streamQueue.poll();
            }

            int next = super.read();

            if((char) next != '\t') {
                return next;
            }

            for(int i = 0; i < tabSpaces; i++) {
                streamQueue.add(' ');
            }

            //noinspection ConstantConditions
            return streamQueue.poll();
        }

        @Override
        public int read(byte[] byteArray) throws IOException {
            return this.read(byteArray, 0, byteArray.length);
        }

        @Override
        public int read(byte[] byteArray, int off, int len) throws IOException {
            if(byteArray == null) {
                throw new NullPointerException("byteArray is null");
            }
            if(off < 0 || len < 0 || len > byteArray.length - off) {
                throw new IndexOutOfBoundsException("offset and/or length are out of bounds");
            }

            if(len == 0) {
                return 0;
            }
            int value = this.read();

            if(value == -1) {
                return -1;
            }

            byteArray[off] = (byte) value;

            int i = off + 1;
            for(; i < byteArray.length; ++i) {
                value = this.read();
                if(value == -1) {
                    return i - off;
                }
                byteArray[i] = (byte) value;
            }

            return i - off;
        }
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }



    public static ClassFileCode extractSource(InputStream classFileInputStream) {


        ANTLRInputStream inputStream = null;

        try {
            // Convert tabs to spaces before parsing
            InputStream fileInputStream = new TabExpansionFilterInputStream(classFileInputStream, TAB_SPACE);
            inputStream = new ANTLRInputStream(fileInputStream);

            //System.out.println(getStringFromInputStream(fileInputStream));

        } catch (IOException e) {
            e.printStackTrace();
        }

        Java9Lexer javaSourceLexer = new Java9Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(javaSourceLexer);
        Java9Parser javaSourceParser = new Java9Parser(commonTokenStream);

        Java9Parser.CompilationUnitContext classdefContext = javaSourceParser.compilationUnit();

        JavaFileMethodExtractorVisitor methodExtractorVisitor = new JavaFileMethodExtractorVisitor(commonTokenStream);

        methodExtractorVisitor.visit(classdefContext);

        ClassFileCode classFileCode = methodExtractorVisitor.getClassFileCode();

        JavaStatementRecognizerVisitor statementRecognizerVisitor = new JavaStatementRecognizerVisitor(classFileCode, commonTokenStream);

        statementRecognizerVisitor.visit(classdefContext);

        return classFileCode;
    }
}

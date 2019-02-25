package com.redwyvern.statementobserver;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.statementobserver.generator.SubjectGeneratorVisitor;
import com.redwyvern.util.ResourceUtil;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class SubjectGeneratorVisitorTest {

    @Test
    public void shouldGenerateSubjectFromBasicInput() throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InputStream fileInputStream = ResourceUtil.getInputStream("javaclassinput/basicInput.java");
        CharStream inputStream = CharStreams.fromStream(fileInputStream);

        Java9Lexer javaSourceLexer = new Java9Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(javaSourceLexer);
        Java9Parser javaSourceParser = new Java9Parser(commonTokenStream);
        Java9Parser.CompilationUnitContext classdefContext = javaSourceParser.compilationUnit();
        SubjectGeneratorVisitor subjectGeneratorVisitor = new SubjectGeneratorVisitor(commonTokenStream, outputStream);
        subjectGeneratorVisitor.visit(classdefContext);

        String generatedClass = new String(outputStream.toByteArray());
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubject.java").trim();

        assertThat(generatedClass, equalTo(expectedGeneratedClass));
    }

    @Test
    public void shouldGenerateSubjectFromBasicImplementsRunnableInput() throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InputStream fileInputStream = ResourceUtil.getInputStream("javaclassinput/basicImplementsRunnableInput.java");
        CharStream inputStream = CharStreams.fromStream(fileInputStream);

        Java9Lexer javaSourceLexer = new Java9Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(javaSourceLexer);
        Java9Parser javaSourceParser = new Java9Parser(commonTokenStream);
        Java9Parser.CompilationUnitContext classdefContext = javaSourceParser.compilationUnit();
        SubjectGeneratorVisitor subjectGeneratorVisitor = new SubjectGeneratorVisitor(commonTokenStream, System.out);
        subjectGeneratorVisitor.visit(classdefContext);

/*
        String generatedClass = new String(outputStream.toByteArray());
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/RunnableHelloWorldSubject.java").trim();
        assertThat(generatedClass, equalTo(expectedGeneratedClass));
*/
    }

}

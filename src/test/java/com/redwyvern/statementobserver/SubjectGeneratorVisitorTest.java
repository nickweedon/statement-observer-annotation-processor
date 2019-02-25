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

import java.io.IOException;
import java.io.InputStream;


public class SubjectGeneratorVisitorTest {

    @Test
    public void shouldGenerateSubjectFromBasicInput() throws IOException {


        InputStream fileInputStream = ResourceUtil.getInputStream("javaclassinput/basicInput.java");
        CharStream inputStream = CharStreams.fromStream(fileInputStream);

        Java9Lexer javaSourceLexer = new Java9Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(javaSourceLexer);
        Java9Parser javaSourceParser = new Java9Parser(commonTokenStream);
        Java9Parser.CompilationUnitContext classdefContext = javaSourceParser.compilationUnit();
        SubjectGeneratorVisitor subjectGeneratorVisitor = new SubjectGeneratorVisitor(commonTokenStream);
        subjectGeneratorVisitor.visit(classdefContext);
    }
}

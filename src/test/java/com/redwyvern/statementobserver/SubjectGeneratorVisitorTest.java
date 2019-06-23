package com.redwyvern.statementobserver;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.statementobserver.generator.SubjectGeneratorVisitor;
import com.redwyvern.util.ResourceUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class SubjectGeneratorVisitorTest {

    @Getter
    @AllArgsConstructor
    static class ParseInput {
        private CommonTokenStream commonTokenStream;
        private ParseTree parseTreeRoot;

        public static ParseInput fromResource(String resourceFile) throws IOException {
            InputStream fileInputStream = ResourceUtil.getInputStream(resourceFile);
            CharStream inputStream = CharStreams.fromStream(fileInputStream);

            Java9Lexer javaSourceLexer = new Java9Lexer(inputStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(javaSourceLexer);
            Java9Parser javaSourceParser = new Java9Parser(commonTokenStream);
            return new ParseInput(commonTokenStream, javaSourceParser.compilationUnit());
        }
    }


    private String executeSubjectGeneratorVisitor(String inputResource) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ParseInput parseInput = ParseInput.fromResource(inputResource);
        SubjectGeneratorVisitor subjectGeneratorVisitor = new SubjectGeneratorVisitor(parseInput.getCommonTokenStream(), outputStream);
        subjectGeneratorVisitor.visit(parseInput.getParseTreeRoot());

        return ResourceUtil.normalizeLineEndings(new String(outputStream.toByteArray()));
    }

    @Test
    public void shouldGenerateSubjectFromBasicInput() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectHeader.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldGenerateSubjectFromBasicImplementsRunnableInput() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicImplementsRunnableInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldImplementsRunnableSubjectHeader.java").trim();


        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldGenerateSubjectFromBasicExtendsObjectInput() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicExtendsObjectInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldExtendsObjectSubjectHeader.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }


    @Test
    public void shouldGenerateSubjectFromBasicExtendsObjectAndImplementsRunnableInput() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicExtendsObjectAndImplementsRunnableInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldExtendsObjectAndImplementsRunnableSubjectHeader.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldAddSubjectImportWhenNoImport() throws IOException {

        //TODO: Need to add code to add the import statement ("import com.redwyvern.statementobserver.StatementObservable") if missing.

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicInputNoImport.java").trim();
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectHeader.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldAddSubjectImportWhenExistingOtherImports() throws IOException {

        //TODO: Need to add code to add the import statement ("import com.redwyvern.statementobserver.StatementObservable") if missing.

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicInputMultiOtherImports.java").trim();
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectHeaderWithOtherImports.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }


    ////////////////////////////////////////// Statement tick() insertion tests ///////////////////////////////////////

    @Test
    public void shouldGenerateTickFromDoStuff() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/basicInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectDoStuffTick.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldGenerateTickFromNestedIf() throws IOException {

        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/nestedIfInput.java");
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectNestedIfTick.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    @Test
    public void shouldGenerateTickFromNested() throws IOException {


        String generatedClass = executeSubjectGeneratorVisitor("javaclassinput/nestedInput.java").trim();
        String expectedGeneratedClass = ResourceUtil.getFileContents("subjectgenerator/HelloWorldSubjectNestedTick.java").trim();

        assertThat(generatedClass, startsWith(expectedGeneratedClass));
    }

    //TODO: Implement more of the 'tick()' tests to finish fleshing out this functionality


}

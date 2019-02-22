package com.redwyvern.statementobserver.extractor;

import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.util.JavaSourceUtil;
import com.redwyvern.util.ResourceUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JavaStatementRecognizerTest {

    // Test java class input
    private static final String SIMPLE_ASSIGNMENT_CLASS = "simpleAssignment";
    private static final String BASIC_CLASS = "basic";
    private static final String NESTED_CLASS = "nested";
    private static final String SWITCH_STATEMENT_CLASS = "switchStatement";
    private static final String MULTILINE_MULTIMETHOD_CLASS = "multiLineMultiMethod";
    private static final String LAMBDA_INLINE_CLASS = "lambdaInline";
    private static final String LAMBDA_MULTILINE_CLASS = "lambdaMultiline";
    private static final String LAMBDA_MULTILINE_ASSIGNMENT_CLASS = "lambdaMultilineAssignment";
    private static final String NESTED_IF_CLASS = "nestedIf";

    private static final String COMPLEX_CLASS_DEMO = "complexClass";

    private static InputStream getInput(String testClassName) {
        return ResourceUtil.getInputStream("javaclassinput/" + testClassName + "Input.java");
    }

    private static InputStream getExpected(String testClassName) {
        return ResourceUtil.getInputStream("statementrecognizer/" + testClassName + "Expected.java");
    }

    private void assertParseFile(String testClassName) {
        ClassFileCode classFileCode = JavaSourceExtractor.extractSource(getInput(testClassName));
        String classCodeWithStatements = JavaSourceUtil.printCode(classFileCode);
        String expectedText = ResourceUtil.getTrimmedFileContents(getExpected(testClassName)).replaceAll("\r\n", "\n");
        String actualText = classCodeWithStatements.trim().replaceAll("\r\n", "\n");

        assertThat(actualText, equalTo(expectedText));
    }

    private void showResult(String testClassName) {
        ClassFileCode classFileCode = JavaSourceExtractor.extractSource(getInput(testClassName));
        String classCodeWithStatements = JavaSourceUtil.printCode(classFileCode);
        System.out.println(classCodeWithStatements);
    }


    @Test
    public void instantiationShouldNotThrow() {
        new JavaStatementRecognizerTest();
    }

    @Test
    public void shouldParseBasicJavaFile() {
        assertParseFile(BASIC_CLASS);
    }

    @Test
    public void shouldParseSimpleAssignmentClassJavaFile() {
        assertParseFile(SIMPLE_ASSIGNMENT_CLASS);
    }

    @Test
    public void shouldParseNestedClassJavaFile() {
        assertParseFile(NESTED_CLASS);
    }

    @Test
    public void shouldParseSwitchStatementClassJavaFile() {
        assertParseFile(SWITCH_STATEMENT_CLASS);
    }

    @Test
    public void shouldParseMultiLineMultiMethodClassJavaFile() {
        assertParseFile(MULTILINE_MULTIMETHOD_CLASS);
    }

    @Test
    public void shouldParseLambdaInlineClassJavaFile() {
        assertParseFile(LAMBDA_INLINE_CLASS);
    }

    @Test
    public void shouldParseLambdaMultilineClassJavaFile() {
        assertParseFile(LAMBDA_MULTILINE_CLASS);
    }

    @Test
    public void shouldParseLambdaMultilineAssignmentClassJavaFile() {
        assertParseFile(LAMBDA_MULTILINE_ASSIGNMENT_CLASS);
    }

    @Test
    public void shouldParseNestedIfClassJavaFile() {
        assertParseFile(NESTED_IF_CLASS);
    }

    // This is really just more of a demo to see how it works on a real class file
    @Test
    @Ignore
    public void statementRecognizerDemo() {
        showResult(COMPLEX_CLASS_DEMO);
    }

}

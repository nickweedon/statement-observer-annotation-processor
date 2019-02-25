package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.util.ResourceUtil;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// Be careful about inserting new code into this test since it relies on testing against line numbers
public class SubjectHelperTest {

    public static final int HELLO_WORLD_METHOD_COUNT = 1;
    public static final int HELLO_WORLD_LINE_COUNT = 4;

    @Test
    public void shouldReturnCorrectResourcePathForClassCode() {

        Class<?> helloWorldSubjectClazz = ResourceUtil.getClassFromJavaResourceFile("au.org.weedon.redblacktree.HelloWorldSubject", "subjectgenerator/HelloWorldSubject.java");

        assertThat(
                SubjectHelper.getSubjectClassResourceClassFileCodePath(helloWorldSubjectClazz),
                equalTo("statementobserver/au/org/weedon/redblacktree/helloWorld.ser"));
    }

    @Test
    public void shouldLoadResourceClassFileCode() throws IOException, ClassNotFoundException {

        Class<?> helloWorldSubjectClazz = ResourceUtil.getClassFromJavaResourceFile("au.org.weedon.redblacktree.HelloWorldSubject", "subjectgenerator/HelloWorldSubject.java");
        ClassFileCode classFileCode = SubjectHelper.loadResourceClassFileCode(helloWorldSubjectClazz);

        assertThat(classFileCode.getClassMethodCodeMap().size(), equalTo(HELLO_WORLD_METHOD_COUNT));
        assertThat(classFileCode.getCodeLineMap().size(), equalTo(HELLO_WORLD_LINE_COUNT));
    }

}

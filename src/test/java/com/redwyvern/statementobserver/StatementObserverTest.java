package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.Statement;
import com.redwyvern.util.ResourceUtil;
import net.openhft.compiler.CachedCompiler;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StatementObserverTest {

    @Test
    public void shouldObserveCodeAsItExecutes() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        StatementSubject helloWorldSubject = ResourceUtil.getInstanceFromJavaResourceFile("au.org.weedon.redblacktree.HelloWorldSubject", "subjectgenerator/HelloWorldSubject.java");
        Method doStuffMethod = helloWorldSubject.getClass().getMethod("doStuff");

        List<String> statementList = new ArrayList<>();

        StatementObserver statementObserver = statementSubject -> {
            try {
                Statement executingStatement = statementSubject.getExecutingStatement();
                statementList.add(executingStatement.getStatementCode());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        helloWorldSubject.addObserver(statementObserver);
        doStuffMethod.invoke(helloWorldSubject);

        assertThat(statementList, equalTo(Arrays.asList(
                "System.out.println(\"Hello there...\");",
                "System.out.println(\"Do more stuff...\");"
        )));
    }

}

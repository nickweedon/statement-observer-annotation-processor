package com.redwyvern.statementobserver;

import au.org.weedon.redblacktree.HelloWorldSubject;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.codemodel.Statement;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// Be careful about inserting new code into this test since it relies on testing against line numbers
public class StatementObserverTest {

    @Test
    public void shouldObserveCodeAsItExecutes() {

        HelloWorldSubject helloWorldSubject = new HelloWorldSubject();

        StatementObserver statementObserver = statementSubject -> {
            try {
                Statement executingStatement = statementSubject.getExecutingStatement();
                System.out.println("Executing: '" + executingStatement.getStatementCode() + "'");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        helloWorldSubject.addObserver(statementObserver);

        helloWorldSubject.doStuff();

    }

}

package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

@StatementObservable
public class RunnableHelloWorld extends Object {

    @Override
    public void run() {
        System.out.println("Hello there...");
        System.out.println("Do more stuff...");
    }
}

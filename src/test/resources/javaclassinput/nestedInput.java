package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

@StatementObservable
public class HelloWorld {

    public class Silly {
        public void doStuff() {
            System.out.println("Hello there...");
            System.out.println("Do more stuff...");
        }
    }
}

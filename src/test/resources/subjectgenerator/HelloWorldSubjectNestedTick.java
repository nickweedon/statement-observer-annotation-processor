package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

@StatementObservable
public class HelloWorldSubject implements com.redwyvern.statementobserver.StatementSubject {

    public class Silly {
        public void doStuff() {
            tick(); System.out.println("Hello there...");
            tick(); System.out.println("Do more stuff...");
        }

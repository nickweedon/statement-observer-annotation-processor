package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

@StatementObservable
public class HelloWorldSubject implements StatementSubject {

    public class Silly {
        public void doStuff() {
            tick(); System.out.println("Hello there...");
            tick(); System.out.println("Do more stuff...");
        }

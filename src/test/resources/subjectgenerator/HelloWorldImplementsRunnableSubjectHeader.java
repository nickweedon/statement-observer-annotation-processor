package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

@StatementObservable
public class RunnableHelloWorldSubject implements Runnable, StatementSubject {

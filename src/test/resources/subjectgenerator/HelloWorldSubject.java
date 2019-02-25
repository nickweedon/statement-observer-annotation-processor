package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

@StatementObservable
public class HelloWorldSubject implements com.redwyvern.statementobserver.StatementSubject {

    public void doStuff() {
        tick(); System.out.println("Hello there...");
        tick(); System.out.println("Do more stuff...");
    }

    ///////////////////////////// Statement Observer injected code ///////////////////////
    private java.util.concurrent.CopyOnWriteArrayList<com.redwyvern.statementobserver.StatementObserver> statementObserverList = new java.util.concurrent.CopyOnWriteArrayList<>();
    private Integer currentlyExecutingLine;
    private static java.util.concurrent.atomic.AtomicReference<com.redwyvern.statementobserver.codemodel.ClassFileCode> classFileCode = new java.util.concurrent.atomic.AtomicReference<>();

    private void tick() {
        currentlyExecutingLine = com.redwyvern.statementobserver.SubjectHelper.getStackTraceLine(1);
        for(com.redwyvern.statementobserver.StatementObserver statementObserver : statementObserverList) {
            statementObserver.notifyNewStatement(this);
        }
        currentlyExecutingLine = null;
    }

    @Override
    public void addObserver(com.redwyvern.statementobserver.StatementObserver statementObserver) {
        statementObserverList.add(statementObserver);
    }

    @Override
    public void removeObserver(com.redwyvern.statementobserver.StatementObserver statementObserver) {
        statementObserverList.remove(statementObserver);
    }

    @Override
    public com.redwyvern.statementobserver.codemodel.Statement getExecutingStatement() throws java.io.IOException, ClassNotFoundException {
        classFileCode.compareAndSet(null, com.redwyvern.statementobserver.SubjectHelper.loadResourceClassFileCode(HelloWorldSubject.class));
        return com.redwyvern.statementobserver.SubjectHelper.getExecutingStatement(classFileCode.get(), currentlyExecutingLine);
    }

}

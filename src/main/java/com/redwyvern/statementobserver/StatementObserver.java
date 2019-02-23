package com.redwyvern.statementobserver;

public interface StatementObserver {

    void notifyNewStatement(StatementSubject statementSubject);
}

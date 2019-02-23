package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.Statement;

import java.io.IOException;

public interface StatementSubject {
    void addObserver(StatementObserver statementObserver);
    void removeObserver(StatementObserver statementObserver);
    Statement getExecutingStatement() throws IOException, ClassNotFoundException;
}

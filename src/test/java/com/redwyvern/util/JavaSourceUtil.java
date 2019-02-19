package com.redwyvern.util;

import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.codemodel.ClassMethodCode;
import com.redwyvern.statementobserver.codemodel.CodeLine;
import com.redwyvern.statementobserver.codemodel.Statement;

import java.util.Optional;

public class JavaSourceUtil {

    private static final String NL = System.getProperty("line.separator");

    public static String printCode(ClassFileCode classFileCode) {

        StringBuilder classCodeText = new StringBuilder();

        for(ClassMethodCode classMethodCode : classFileCode.getClassMethodCodeMap().values()) {

            StringBuilder methodBodyText = new StringBuilder();
            StringBuilder methodBlock = new StringBuilder();
            for(CodeLine codeLine : classMethodCode.getCodeLines()) {
                //methodBlock += codeLine.getLineNumber() + ": " + codeLine.getCode();
                if(isLowerStatement(codeLine) || isUpperStatement(codeLine)) {
/*
                    methodBlock.append(codeLine.getStatementRange().toString());
                    methodBlock.append(" =>");
*/
                    int lowerIndex = getLowerStatementBounds(codeLine).orElse(-1);
                    int upperIndex = getUpperStatementBounds(codeLine).orElse(-1);

                    String code = codeLine.getCode();
                    int i = 0;
                    for (; i < code.length(); ++i) {
                        if(i == lowerIndex || i == upperIndex) {
                            methodBlock.append("**");
                        }
                        methodBlock.append(code.charAt(i));
                    }
                    if(i == upperIndex) {
                        methodBlock.append("**");
                    }
                    methodBlock.append(NL);
                } else {
/*
                    if(codeLine.getStatementRange() != null) {
                        methodBlock.append(codeLine.getStatementRange().toString());
                        methodBlock.append(" => ");
                    }
*/
                    methodBlock.append(codeLine.getCode());
                    methodBlock.append(NL);
                }

            }

            //String methodBlock = String.join("", codeLines.stream().map(CodeLine::getCode).collect(Collectors.toList()));

            //methodBlock = String.join(NL, lines);

            methodBodyText
                    .append(NL)
                    .append(classMethodCode.getReturnType())
                    .append(" ")
                    .append(classMethodCode.getFullClassName())
                    .append("#")
                    .append(classMethodCode.getMethodSignature())
                    .append(" ")
                    .append(methodBlock)
                    .append(NL);

            classCodeText.append(methodBodyText);
        }
        return classCodeText.toString();
    }

    private static boolean isUpperStatement(CodeLine codeLine) {
        return getUpperStatementBounds(codeLine).isPresent();
    }

    private static boolean isLowerStatement(CodeLine codeLine) {
        return getLowerStatementBounds(codeLine).isPresent();
    }

    private static Optional<Integer> getUpperStatementBounds(CodeLine codeLine) {
        Statement statement = codeLine.getStatement();
        if(statement == null) {
            return Optional.empty();
        }
        if(statement.getCodeLineRange().upperEndpoint() != codeLine) {
            return Optional.empty();
        }
        return Optional.of(statement.getColumnRange().upperEndpoint());
    }

    private static Optional<Integer> getLowerStatementBounds(CodeLine codeLine) {
        Statement statement = codeLine.getStatement();
        if(statement == null) {
            return Optional.empty();
        }
        if(statement.getCodeLineRange().lowerEndpoint() != codeLine) {
            return Optional.empty();
        }
        return Optional.of(statement.getColumnRange().lowerEndpoint());
    }



}

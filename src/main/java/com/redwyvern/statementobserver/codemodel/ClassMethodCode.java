package com.redwyvern.statementobserver.codemodel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClassMethodCode implements Serializable {

    private static final long serialVersionUID = -4218571547277393618L;

    private final String returnType;
    private final String fullClassName;
    private final String methodName;
    private final String methodSignature;
    private final int methodIndent;
    private final List<CodeLine> codeLines = new ArrayList<>();
}

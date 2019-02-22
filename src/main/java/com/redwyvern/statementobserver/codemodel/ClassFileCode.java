package com.redwyvern.statementobserver.codemodel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class ClassFileCode {
    private final String fileName;
    private final Map<String, ClassMethodCode> classMethodCodeMap = new HashMap<>();
    private final Map<Integer, CodeLine> codeLineMap = new HashMap<>();
}

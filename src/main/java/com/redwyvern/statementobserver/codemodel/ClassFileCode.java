package com.redwyvern.statementobserver.codemodel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class ClassFileCode implements Serializable {

    private static final long serialVersionUID = 6917064186798331454L;

    private final String fileName;
    private final Map<String, ClassMethodCode> classMethodCodeMap = new HashMap<>();
    private final Map<Integer, CodeLine> codeLineMap = new HashMap<>();
}

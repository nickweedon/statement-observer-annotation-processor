package com.redwyvern.statementobserver.generator;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SubjectPreprocessResult {

    private Set<String> imports = new HashSet<>();
    private boolean hasPackageDeclaration;
    private boolean hasImplements;
    private boolean hasExtends;
}

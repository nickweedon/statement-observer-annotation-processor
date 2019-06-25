package com.redwyvern.statementobserver.generator;

import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectPreprocessorVisitor  extends Java9ParserBaseVisitor<Void> {

    private final SubjectPreprocessResult result = new SubjectPreprocessResult();
    private final Java9Parser parser;

    public SubjectPreprocessorVisitor(Java9Parser parser) {
        this.parser = parser;
    }

    @Override
    public Void visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        result.setHasPackageDeclaration(true);
        return super.visitPackageDeclaration(ctx);
    }

    @Override
    public Void visitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx) {

        ParseTreePattern pattern = parser.compileParseTreePattern("import <typeName>;", Java9Parser.RULE_singleTypeImportDeclaration);
        List<String> importList = pattern
                .findAll(ctx, "//importDeclaration/*").stream()
                .map((match) -> match.get("typeName").getText()).collect(Collectors.toList());

        result.setImports(new HashSet<>(importList));
        if(!importList.isEmpty()) {
            result.setLastImportPackage(importList.get(importList.size() - 1));
        }

        return super.visitOrdinaryCompilation(ctx);
    }

    public SubjectPreprocessResult getResult() {
        return result;
    }
}
